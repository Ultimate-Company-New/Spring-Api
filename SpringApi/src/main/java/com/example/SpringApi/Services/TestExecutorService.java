package com.example.SpringApi.Services;

import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Separate service for async test execution.
 * This class exists because @Async doesn't work when calling methods within the same class.
 */
@Service
public class TestExecutorService {
    
    private static final ContextualLogger logger = ContextualLogger.getLogger(TestExecutorService.class);
    
    // In-memory storage for active test executions
    private static final Map<String, TestExecutionStatusModel> activeExecutions = new ConcurrentHashMap<>();
    private static final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();
    
    // Patterns for parsing Maven/Surefire output
    private static final Pattern TESTS_RUN_PATTERN = Pattern.compile(
        "Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+)"
    );
    private static final Pattern RUNNING_TEST_CLASS_PATTERN = Pattern.compile(
        "Running ([\\w.]+Test)"
    );
    
    /**
     * Gets or creates the status for an execution.
     */
    public TestExecutionStatusModel getOrCreateStatus(String executionId) {
        return activeExecutions.computeIfAbsent(executionId, k -> {
            TestExecutionStatusModel status = new TestExecutionStatusModel();
            status.setExecutionId(executionId);
            status.setStatus("PENDING");
            return status;
        });
    }
    
    /**
     * Stores initial status with expected test count.
     */
    public void storeStatus(String executionId, TestExecutionStatusModel status) {
        activeExecutions.put(executionId, status);
    }
    
    /**
     * Sets the expected total tests count for an execution.
     * This allows proper progress percentage calculation.
     */
    public void setExpectedTestCount(String executionId, int expectedCount) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status != null) {
            status.setTotalTests(expectedCount);
        }
    }
    
    /**
     * Gets the status for an execution.
     * For RUNNING status, adds time-based progress estimation for smoother UX.
     */
    public TestExecutionStatusModel getStatus(String executionId) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status == null) {
            return null;
        }
        
        // Smooth progress estimation while Maven runs.
        // Maven/Surefire often only prints summaries at the end of a test class, so we provide an estimated
        // completedTests value that steadily increases (but never reaches 100% until completion).
        if ("RUNNING".equals(status.getStatus())
                && status.getStartedAt() != null
                && status.getTotalTests() > 0
                && status.getCompletedTests() < status.getTotalTests()) {
            long elapsedMs = java.time.Duration.between(status.getStartedAt(), LocalDateTime.now()).toMillis();

            int totalTests = status.getTotalTests();

            // While RUNNING, never show 100% (keep 1 test "remaining" until Maven completes)
            int maxWhileRunning = Math.max(0, totalTests - 1);
            int actualCompletedClamped = Math.min(status.getCompletedTests(), maxWhileRunning);

            // Heuristic duration: small fixed overhead + per-test cost
            long expectedDurationMs = 2000L + (long) totalTests * 400L;
            double ratio = expectedDurationMs > 0
                    ? Math.min(0.95d, (double) elapsedMs / (double) expectedDurationMs)
                    : 0.0d;
            int estimatedCompleted = (int) Math.floor(ratio * totalTests);
            int smoothedCompleted = Math.max(actualCompletedClamped, Math.min(maxWhileRunning, Math.max(0, estimatedCompleted)));

            // Create a snapshot with estimated values (don't modify original)
            TestExecutionStatusModel snapshot = new TestExecutionStatusModel();
            snapshot.setExecutionId(status.getExecutionId());
            snapshot.setStatus(status.getStatus());
            snapshot.setServiceName(status.getServiceName());
            snapshot.setMethodName(status.getMethodName());
            snapshot.setStartedAt(status.getStartedAt());
            snapshot.setCompletedAt(status.getCompletedAt());
            snapshot.setTotalTests(totalTests);
            snapshot.setCompletedTests(smoothedCompleted);
            snapshot.setPassedTests(status.getPassedTests());
            snapshot.setFailedTests(status.getFailedTests());
            snapshot.setSkippedTests(status.getSkippedTests());
            snapshot.setDurationMs(elapsedMs);
            snapshot.setErrorMessage(status.getErrorMessage());
            snapshot.getResults().addAll(status.getResults());
            return snapshot;
        }
        
        return status;
    }
    
    /**
     * Executes tests asynchronously using Maven.
     * This method returns immediately while tests run in the background.
     */
    @Async("asyncExecutor")
    public void executeTestsAsync(String executionId, String testClassName, String testMethodFilter, String serviceName) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status == null) {
            return;
        }
        
        status.setStatus("RUNNING");
        long startTime = System.currentTimeMillis();
        
        try {
            // Build Maven command with verbose output for progress tracking
            List<String> command = new ArrayList<>();
            command.add("mvn");
            command.add("test");
            command.add("-Dsurefire.useFile=false");  // Output to console, not files
            command.add("-DtrimStackTrace=false");     // Full stack traces
            
            if (testClassName != null) {
                if (testMethodFilter != null) {
                    command.add("-Dtest=" + testClassName + "#" + testMethodFilter);
                } else {
                    command.add("-Dtest=" + testClassName);
                }
            }
            
            // Find project directory
            Path projectDir = findProjectDirectory();
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(projectDir.toFile());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            runningProcesses.put(executionId, process);
            
            // Read output and parse results in real-time
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Parse for progress updates
                    parseTestOutput(line, status);
                    
                    // Track when a test class starts running
                    Matcher runningMatcher = RUNNING_TEST_CLASS_PATTERN.matcher(line);
                    if (runningMatcher.find()) {
                        status.setStatus("RUNNING");
                    }
                }
            }
            
            int exitCode = process.waitFor();
            runningProcesses.remove(executionId);
            
            // Parse final results from surefire reports
            parseSurefireReports(status, projectDir, testClassName);
            
            status.setDurationMs(System.currentTimeMillis() - startTime);
            status.setCompletedAt(LocalDateTime.now());
            status.setStatus(exitCode == 0 ? "COMPLETED" : "COMPLETED_WITH_FAILURES");
            
            if (exitCode != 0 && status.getResults().isEmpty()) {
                status.setErrorMessage("Tests failed. Exit code: " + exitCode);
            }
            
        } catch (Exception e) {
            logger.error(e);
            status.setStatus("FAILED");
            status.setErrorMessage("Test execution failed: " + e.getMessage());
            status.setCompletedAt(LocalDateTime.now());
            status.setDurationMs(System.currentTimeMillis() - startTime);
            runningProcesses.remove(executionId);
        }
    }
    
    /**
     * Finds the Spring API project directory.
     */
    private Path findProjectDirectory() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        
        // Check if we're already in SpringApi directory
        if (Files.exists(currentDir.resolve("pom.xml")) && 
            Files.exists(currentDir.resolve("src/test/java"))) {
            return currentDir;
        }
        
        // Try SpringApi subdirectory
        Path springApiDir = currentDir.resolve("SpringApi");
        if (Files.exists(springApiDir.resolve("pom.xml"))) {
            return springApiDir;
        }
        
        // Try parent directories
        Path parent = currentDir.getParent();
        if (parent != null) {
            Path parentSpringApi = parent.resolve("SpringApi");
            if (Files.exists(parentSpringApi.resolve("pom.xml"))) {
                return parentSpringApi;
            }
        }
        
        return currentDir;
    }
    
    /**
     * Parses Maven test output line to extract progress information.
     * Updates the status in real-time as test results come in.
     */
    private void parseTestOutput(String line, TestExecutionStatusModel status) {
        // Parse lines like: "Tests run: 6, Failures: 0, Errors: 0, Skipped: 0"
        Matcher runMatcher = TESTS_RUN_PATTERN.matcher(line);
        if (runMatcher.find()) {
            int testsRun = Integer.parseInt(runMatcher.group(1));
            int failures = Integer.parseInt(runMatcher.group(2));
            int errors = Integer.parseInt(runMatcher.group(3));
            int skipped = Integer.parseInt(runMatcher.group(4));
            
            // Update cumulative counts
            status.setCompletedTests(status.getCompletedTests() + testsRun);
            status.setPassedTests(status.getPassedTests() + testsRun - failures - errors - skipped);
            status.setFailedTests(status.getFailedTests() + failures + errors);
            status.setSkippedTests(status.getSkippedTests() + skipped);
            
            // If we didn't know total tests, update it
            if (status.getTotalTests() < status.getCompletedTests()) {
                status.setTotalTests(status.getCompletedTests());
            }
            return;
        }
        
        // Parse "Running <TestClass>" to indicate progress
        Matcher classMatcher = RUNNING_TEST_CLASS_PATTERN.matcher(line);
        if (classMatcher.find()) {
            // A test class is starting - keep status as RUNNING
            status.setStatus("RUNNING");
        }
    }
    
    /**
     * Parses Surefire XML reports to get detailed test results.
     */
    private void parseSurefireReports(TestExecutionStatusModel status, Path projectDir, String testClassName) {
        Path surefireDir = projectDir.resolve("target/surefire-reports");
        if (!Files.exists(surefireDir)) {
            return;
        }
        
        try {
            Files.list(surefireDir)
                .filter(p -> p.toString().endsWith(".xml"))
                .filter(p -> testClassName == null || p.getFileName().toString().contains(testClassName))
                .forEach(xmlFile -> parseXmlReport(xmlFile, status));
        } catch (IOException e) {
            logger.error(e);
        }
    }
    
    /**
     * Parses a single Surefire XML report file.
     */
    private void parseXmlReport(Path xmlFile, TestExecutionStatusModel status) {
        try {
            String content = Files.readString(xmlFile);
            
            // Parse testcase elements
            Pattern testcasePattern = Pattern.compile(
                "<testcase\\s+name=\"([^\"]+)\"[^>]*classname=\"([^\"]+)\"[^>]*time=\"([^\"]+)\"[^>]*(?:/>|>([\\s\\S]*?)</testcase>)",
                Pattern.MULTILINE
            );
            
            Matcher matcher = testcasePattern.matcher(content);
            while (matcher.find()) {
                String testName = matcher.group(1);
                double timeSeconds = Double.parseDouble(matcher.group(3));
                String innerContent = matcher.group(4);
                
                TestExecutionStatusModel.TestResultInfo result = new TestExecutionStatusModel.TestResultInfo();
                result.setTestMethodName(testName);
                result.setDurationMs((long)(timeSeconds * 1000));
                
                // Extract method name from test name (e.g., "toggleAddress_AddressFound_Success" -> "toggleAddress")
                if (testName.contains("_")) {
                    result.setMethodName(testName.substring(0, testName.indexOf("_")));
                }
                
                // Check for failure or error
                if (innerContent != null && (innerContent.contains("<failure") || innerContent.contains("<error"))) {
                    result.setStatus("FAILED");
                    
                    // Extract error message
                    Pattern messagePattern = Pattern.compile("message=\"([^\"]+)\"");
                    Matcher msgMatcher = messagePattern.matcher(innerContent);
                    if (msgMatcher.find()) {
                        result.setErrorMessage(msgMatcher.group(1));
                    }
                    
                    // Extract stack trace (content between tags)
                    Pattern stackPattern = Pattern.compile(">([^<]+)</(failure|error)>");
                    Matcher stackMatcher = stackPattern.matcher(innerContent);
                    if (stackMatcher.find()) {
                        result.setStackTrace(stackMatcher.group(1).trim());
                    }
                } else if (innerContent != null && innerContent.contains("<skipped")) {
                    result.setStatus("SKIPPED");
                } else {
                    result.setStatus("PASSED");
                }
                
                // Avoid duplicates
                boolean exists = status.getResults().stream()
                    .anyMatch(r -> r.getTestMethodName().equals(testName));
                if (!exists) {
                    status.getResults().add(result);
                }
            }
            
            // Update totals from results
            int passed = (int) status.getResults().stream().filter(r -> "PASSED".equals(r.getStatus())).count();
            int failed = (int) status.getResults().stream().filter(r -> "FAILED".equals(r.getStatus())).count();
            int skipped = (int) status.getResults().stream().filter(r -> "SKIPPED".equals(r.getStatus())).count();
            
            status.setTotalTests(status.getResults().size());
            status.setCompletedTests(status.getResults().size());
            status.setPassedTests(passed);
            status.setFailedTests(failed);
            status.setSkippedTests(skipped);
            
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
