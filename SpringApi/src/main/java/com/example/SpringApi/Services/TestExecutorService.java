package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Services.Interface.ITestExecutorService;
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
 * Service for asynchronous test execution via Maven/Surefire.
 * Maintains in-memory status for active runs and parses Maven output for real-time progress.
 * Implemented as a separate service so @Async works when invoked from QAService.
 */
@Service
public class TestExecutorService implements ITestExecutorService {

    private static final Map<String, TestExecutionStatusModel> activeExecutions = new ConcurrentHashMap<>();
    private static final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    private static final Pattern TESTS_RUN_PATTERN = Pattern.compile(
        "Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+)"
    );
    private static final Pattern RUNNING_TEST_CLASS_PATTERN = Pattern.compile(
        "Running ([\\w.]+Test)"
    );

    // ============================================================================
    // PUBLIC API - ITestExecutorService implementation
    // ============================================================================

    /**
     * Gets existing status for an execution, or creates a new PENDING status if none exists.
     */
    @Override
    public TestExecutionStatusModel getOrCreateStatus(String executionId) {
        return activeExecutions.computeIfAbsent(executionId, TestExecutionStatusModel::new);
    }

    /**
     * Stores or overwrites the status for an execution.
     */
    @Override
    public void storeStatus(String executionId, TestExecutionStatusModel status) {
        activeExecutions.put(executionId, status);
    }

    /**
     * Sets the expected total test count for an execution.
     */
    @Override
    public void setExpectedTestCount(String executionId, int expectedCount) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status != null) {
            status.setTotalTests(expectedCount);
        }
    }

    /**
     * Gets the current status for an execution.
     * For RUNNING status, returns a snapshot with time-based progress estimation.
     */
    @Override
    public TestExecutionStatusModel getStatus(String executionId) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status == null) {
            return null;
        }

        if ("RUNNING".equals(status.getStatus())
                && status.getStartedAt() != null
                && status.getTotalTests() > 0
                && status.getCompletedTests() < status.getTotalTests()) {
            long elapsedMs = java.time.Duration.between(status.getStartedAt(), LocalDateTime.now()).toMillis();
            int totalTests = status.getTotalTests();
            int maxWhileRunning = Math.max(0, totalTests - 1);
            int actualCompletedClamped = Math.min(status.getCompletedTests(), maxWhileRunning);
            long expectedDurationMs = 2000L + (long) totalTests * 400L;
            double ratio = expectedDurationMs > 0
                    ? Math.min(0.95d, (double) elapsedMs / (double) expectedDurationMs)
                    : 0.0d;
            int estimatedCompleted = (int) Math.floor(ratio * totalTests);
            int smoothedCompleted = Math.max(actualCompletedClamped, Math.min(maxWhileRunning, Math.max(0, estimatedCompleted)));

            return TestExecutionStatusModel.createProgressSnapshot(status, totalTests, smoothedCompleted, elapsedMs);
        }

        return status;
    }

    /**
     * Starts asynchronous test execution via Maven. Returns immediately; tests run in background.
     */
    @Override
    @Async("asyncExecutor")
    public void executeTestsAsync(String executionId, String testClassName, String testMethodFilter, String serviceName) {
        TestExecutionStatusModel status = activeExecutions.get(executionId);
        if (status == null) {
            return;
        }

        status.setStatus("RUNNING");
        long startTime = System.currentTimeMillis();

        try {
            List<String> command = buildMavenCommand(testClassName, testMethodFilter);
            Path projectDir = findProjectDirectory();
            Process process = new ProcessBuilder(command)
                .directory(projectDir.toFile())
                .redirectErrorStream(true)
                .start();

            runningProcesses.put(executionId, process);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseTestOutput(line, status);
                    Matcher runningMatcher = RUNNING_TEST_CLASS_PATTERN.matcher(line);
                    if (runningMatcher.find()) {
                        status.setStatus("RUNNING");
                    }
                }
            }

            int exitCode = process.waitFor();
            runningProcesses.remove(executionId);

            parseSurefireReports(status, projectDir, testClassName);

            status.setDurationMs(System.currentTimeMillis() - startTime);
            status.setCompletedAt(LocalDateTime.now());
            status.setStatus(exitCode == 0 ? "COMPLETED" : "COMPLETED_WITH_FAILURES");

            if (exitCode != 0 && status.getResults().isEmpty()) {
                status.setErrorMessage(String.format(ErrorMessages.TestExecutorErrorMessages.TestsFailedExitCodeFormat, exitCode));
            }

        } catch (IOException e) {
            markExecutionFailed(executionId, status, startTime, String.format(ErrorMessages.TestExecutorErrorMessages.IoErrorDuringExecutionFormat, e.getMessage()));
            throw new RuntimeException(ErrorMessages.TestExecutorErrorMessages.IoFailed, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markExecutionFailed(executionId, status, startTime, String.format(ErrorMessages.TestExecutorErrorMessages.InterruptedFormat, e.getMessage()));
            throw new RuntimeException(ErrorMessages.TestExecutorErrorMessages.Interrupted, e);
        } catch (Exception e) {
            markExecutionFailed(executionId, status, startTime, String.format(ErrorMessages.TestExecutorErrorMessages.ExecutionFailedFormat, e.getMessage()));
            throw new RuntimeException(ErrorMessages.TestExecutorErrorMessages.ExecutionFailed, e);
        }
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    private List<String> buildMavenCommand(String testClassName, String testMethodFilter) {
        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.add("test");
        command.add("-Dsurefire.useFile=false");
        command.add("-DtrimStackTrace=false");
        if (testClassName != null) {
            if (testMethodFilter != null) {
                command.add("-Dtest=" + testClassName + "#" + testMethodFilter);
            } else {
                command.add("-Dtest=" + testClassName);
            }
        }
        return command;
    }

    private void markExecutionFailed(String executionId, TestExecutionStatusModel status, long startTime, String message) {
        status.setStatus("FAILED");
        status.setErrorMessage(message);
        status.setCompletedAt(LocalDateTime.now());
        status.setDurationMs(System.currentTimeMillis() - startTime);
        runningProcesses.remove(executionId);
    }

    /**
     * Locates the Spring API project directory (containing pom.xml and src/test/java).
     * Checks current dir, SpringApi subdir, and parent directories.
     */
    private Path findProjectDirectory() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));

        if (Files.exists(currentDir.resolve("pom.xml")) && Files.exists(currentDir.resolve("src/test/java"))) {
            return currentDir;
        }

        Path springApiDir = currentDir.resolve("SpringApi");
        if (Files.exists(springApiDir.resolve("pom.xml"))) {
            return springApiDir;
        }

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
     * Parses a single Maven output line for "Tests run: X, Failures: Y..." and updates status.
     */
    private void parseTestOutput(String line, TestExecutionStatusModel status) {
        Matcher runMatcher = TESTS_RUN_PATTERN.matcher(line);
        if (runMatcher.find()) {
            int testsRun = Integer.parseInt(runMatcher.group(1));
            int failures = Integer.parseInt(runMatcher.group(2));
            int errors = Integer.parseInt(runMatcher.group(3));
            int skipped = Integer.parseInt(runMatcher.group(4));

            status.setCompletedTests(status.getCompletedTests() + testsRun);
            status.setPassedTests(status.getPassedTests() + testsRun - failures - errors - skipped);
            status.setFailedTests(status.getFailedTests() + failures + errors);
            status.setSkippedTests(status.getSkippedTests() + skipped);

            if (status.getTotalTests() < status.getCompletedTests()) {
                status.setTotalTests(status.getCompletedTests());
            }
            return;
        }

        Matcher classMatcher = RUNNING_TEST_CLASS_PATTERN.matcher(line);
        if (classMatcher.find()) {
            status.setStatus("RUNNING");
        }
    }

    /**
     * Parses Surefire XML reports in target/surefire-reports for detailed test results.
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
            throw new RuntimeException(ErrorMessages.TestExecutorErrorMessages.FailedToListSurefireReports, e);
        }
    }

    /**
     * Parses a single Surefire XML report file and populates test results.
     */
    private void parseXmlReport(Path xmlFile, TestExecutionStatusModel status) {
        try {
            String content = Files.readString(xmlFile);

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
                result.setDurationMs((long) (timeSeconds * 1000));

                if (testName.contains("_")) {
                    result.setMethodName(testName.substring(0, testName.indexOf("_")));
                }

                if (innerContent != null && (innerContent.contains("<failure") || innerContent.contains("<error"))) {
                    result.setStatus("FAILED");
                    Pattern messagePattern = Pattern.compile("message=\"([^\"]+)\"");
                    Matcher msgMatcher = messagePattern.matcher(innerContent);
                    if (msgMatcher.find()) {
                        result.setErrorMessage(msgMatcher.group(1));
                    }
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

                boolean exists = status.getResults().stream()
                    .anyMatch(r -> r.getTestMethodName().equals(testName));
                if (!exists) {
                    status.getResults().add(result);
                }
            }

            status.updateTotalsFromResults();

        } catch (IOException e) {
            throw new RuntimeException(String.format(ErrorMessages.TestExecutorErrorMessages.FailedToParseSurefireReportFormat, xmlFile.getFileName()), e);
        }
    }
}
