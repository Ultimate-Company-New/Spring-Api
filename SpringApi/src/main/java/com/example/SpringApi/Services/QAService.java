package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.DatabaseModels.TestRun;
import com.example.SpringApi.Models.DatabaseModels.TestRunResult;
import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Models.ResponseModels.LatestTestResultResponseModel;
import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
import com.example.SpringApi.Models.ResponseModels.QAResponseModel;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Models.ResponseModels.TestRunResponseModel;
import com.example.SpringApi.Repositories.LatestTestResultRepository;
import com.example.SpringApi.Repositories.TestRunRepository;
import com.example.SpringApi.Services.Interface.IQASubTranslator;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Logging.ContextualLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for QA endpoint-to-test mapping operations.
 * 
 * This service provides comprehensive information about all public service
 * methods
 * and their associated unit tests. It uses reflection to dynamically scan
 * service
 * classes and reads test source files from the filesystem to match test methods
 * to service methods based on naming conventions.
 * 
 * Test method naming convention: methodName_Result_Outcome
 * Example: toggleAddress_AddressNotFound_ThrowsNotFoundException
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class QAService extends BaseService implements IQASubTranslator {

    // Package paths for scanning
    private static final String SERVICES_PACKAGE = "com.example.SpringApi.Services";

    // Test source file path (relative to project root)
    private static final String TEST_SOURCE_PATH = "src/test/java/com/example/SpringApi/Services/Tests";

    // Automated API tests path (relative - Spring-PlayWright-Automation project)
    private static final String AUTOMATED_API_TESTS_PATH = "src/test/java/com/ultimatecompany/tests/ApiTests";

    private static final Set<String> EXCLUDED_METHODS = new HashSet<>(Arrays.asList(
            "equals", "hashCode", "toString", "getClass", "notify", "notifyAll", "wait",
            "getUser", "getUserId", "getClientId", "getLoginName"));

    // Service-to-Controller mapping for API routes
    private static final Map<String, ServiceControllerMapping> SERVICE_MAPPINGS = new LinkedHashMap<>();

    static {
        // Initialize service mappings with controller and base path information
        SERVICE_MAPPINGS.put("AddressService",
                new ServiceControllerMapping("AddressController", "/api/Address", "AddressServiceTest"));
        SERVICE_MAPPINGS.put("ClientService",
                new ServiceControllerMapping("ClientController", "/api/Client", "ClientServiceTest"));
        SERVICE_MAPPINGS.put("LeadService",
                new ServiceControllerMapping("LeadController", "/api/Lead", "LeadServiceTest"));
        SERVICE_MAPPINGS.put("LoginService",
                new ServiceControllerMapping("LoginController", "/api/Login", "LoginServiceTest"));
        SERVICE_MAPPINGS.put("MessageService",
                new ServiceControllerMapping("MessageController", "/api/Message", "MessageServiceTest"));
        SERVICE_MAPPINGS.put("PackageService",
                new ServiceControllerMapping("PackageController", "/api/Package", "PackageServiceTest"));
        SERVICE_MAPPINGS.put("PaymentService",
                new ServiceControllerMapping("PaymentController", "/api/Payments", "PaymentServiceTest"));
        SERVICE_MAPPINGS.put("PickupLocationService", new ServiceControllerMapping("PickupLocationController",
                "/api/PickupLocation", "PickupLocationServiceTest"));
        SERVICE_MAPPINGS.put("ProductService",
                new ServiceControllerMapping("ProductController", "/api/Product", "ProductServiceTest"));
        SERVICE_MAPPINGS.put("ProductReviewService", new ServiceControllerMapping("ProductReviewController",
                "/api/ProductReview", "ProductReviewServiceTest"));
        SERVICE_MAPPINGS.put("PromoService",
                new ServiceControllerMapping("PromoController", "/api/Promo", "PromoServiceTest"));
        SERVICE_MAPPINGS.put("PurchaseOrderService", new ServiceControllerMapping("PurchaseOrderController",
                "/api/PurchaseOrder", "PurchaseOrderServiceTest"));
        SERVICE_MAPPINGS.put("ShipmentService",
                new ServiceControllerMapping("ShipmentController", "/api/Shipment", "ShipmentServiceTest"));
        SERVICE_MAPPINGS.put("ShipmentProcessingService",
                new ServiceControllerMapping("ShippingController", "/api/Shipping", "ShipmentProcessingServiceTest"));
        SERVICE_MAPPINGS.put("ShippingService",
                new ServiceControllerMapping("ShippingController", "/api/Shipping", "ShippingServiceTest"));
        SERVICE_MAPPINGS.put("TodoService",
                new ServiceControllerMapping("TodoController", "/api/Todo", "TodoServiceTest"));
        SERVICE_MAPPINGS.put("UserService",
                new ServiceControllerMapping("UserController", "/api/User", "UserServiceTest"));
        SERVICE_MAPPINGS.put("UserGroupService",
                new ServiceControllerMapping("UserGroupController", "/api/UserGroup", "UserGroupServiceTest"));
        SERVICE_MAPPINGS.put("UserLogService",
                new ServiceControllerMapping("UserLogController", "/api/UserLog", "UserLogServiceTest"));
    }

    // ==================== REPOSITORIES ====================

    private static final ContextualLogger logger = ContextualLogger.getLogger(QAService.class);

    private final TestRunRepository testRunRepository;
    private final LatestTestResultRepository latestTestResultRepository;
    private final TestExecutorService testExecutorService;

    @Autowired
    public QAService(TestRunRepository testRunRepository,
            LatestTestResultRepository latestTestResultRepository,
            TestExecutorService testExecutorService) {
        this.testRunRepository = testRunRepository;
        this.latestTestResultRepository = latestTestResultRepository;
        this.testExecutorService = testExecutorService;
    }

    /**
     * Helper class to store service-to-controller mapping information.
     */
    private static class ServiceControllerMapping {
        final String controllerName;
        final String basePath;
        final String testClassName;

        ServiceControllerMapping(String controllerName, String basePath, String testClassName) {
            this.controllerName = controllerName;
            this.basePath = basePath;
            this.testClassName = testClassName;
        }
    }

    /**
     * Helper class to store test method information including display name.
     */
    private static class TestMethodInfo {
        final String methodName;
        final String displayName;
        final String declaringTestClassName;

        TestMethodInfo(String methodName, String displayName, String declaringTestClassName) {
            this.methodName = methodName;
            this.displayName = displayName;
            this.declaringTestClassName = declaringTestClassName;
        }
    }

    /**
     * Tracks a class scope while parsing Java source so we can map @Test methods to
     * their declaring class
     * (including @Nested inner classes).
     */
    private static class ClassScope {
        final String name;
        final int startBraceDepth;

        ClassScope(String name, int startBraceDepth) {
            this.name = name;
            this.startBraceDepth = startBraceDepth;
        }
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Returns all QA dashboard data in a single response.
     * Includes services with methods/tests, coverage summary, and available
     * services.
     * 
     * @return QADashboardResponseModel with all dashboard data
     */
    @Override
    public QADashboardResponseModel getDashboardData() {
        // Get all services with their methods, tests, and last run info
        List<QAResponseModel> services = getAllEndpointsWithTests();

        // Calculate coverage summary
        int totalMethods = 0;
        int totalMethodsWithCoverage = 0;
        int totalTests = 0;
        List<QADashboardResponseModel.ServiceBreakdownData> serviceBreakdown = new ArrayList<>();

        for (QAResponseModel service : services) {
            totalMethods += service.getTotalMethods();
            totalMethodsWithCoverage += service.getMethodsWithCoverage();
            totalTests += service.getTotalTests();

            serviceBreakdown.add(new QADashboardResponseModel.ServiceBreakdownData(
                    service.getServiceName(),
                    service.getTotalMethods(),
                    service.getMethodsWithCoverage(),
                    service.getTotalTests(),
                    service.getCoveragePercentage()));
        }

        QADashboardResponseModel.CoverageSummaryData coverageSummary = new QADashboardResponseModel.CoverageSummaryData();
        coverageSummary.setTotalServices(services.size());
        coverageSummary.setTotalMethods(totalMethods);
        coverageSummary.setTotalMethodsWithCoverage(totalMethodsWithCoverage);
        coverageSummary.setTotalTests(totalTests);
        coverageSummary.setOverallCoveragePercentage(totalMethods > 0
                ? Math.round(((double) totalMethodsWithCoverage / totalMethods) * 100.0 * 100.0) / 100.0
                : 0.0);
        coverageSummary.setServiceBreakdown(serviceBreakdown);

        // Get available services
        List<String> availableServices = new ArrayList<>(SERVICE_MAPPINGS.keySet());

        // Get automated API tests (separate section from unit tests)
        QADashboardResponseModel.AutomatedApiTestsData automatedApiTests = getAutomatedApiTests();

        return new QADashboardResponseModel(services, coverageSummary, availableServices, automatedApiTests);
    }

    /**
     * Discovers and returns all automated API tests from the Spring-PlayWright-Automation project.
     * Uses relative path: Spring-PlayWright-Automation/src/test/java/com/ultimatecompany/tests/ApiTests
     *
     * @return AutomatedApiTestsData with categories and test files, or empty data if path not found
     */
    private QADashboardResponseModel.AutomatedApiTestsData getAutomatedApiTests() {
        Path apiTestsDir = resolveAutomatedApiTestsPath();
        if (apiTestsDir == null || !Files.isDirectory(apiTestsDir)) {
            return new QADashboardResponseModel.AutomatedApiTestsData(
                    "../Spring-PlayWright-Automation/" + AUTOMATED_API_TESTS_PATH, 0, new ArrayList<>());
        }

        List<QADashboardResponseModel.AutomatedApiTestCategory> categories = new ArrayList<>();
        int totalTests = 0;

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(apiTestsDir)) {
            List<Path> entries = new ArrayList<>();
            dirStream.forEach(entries::add);
            entries.sort(Comparator.comparing(p -> p.getFileName().toString()));

            for (Path entry : entries) {
                if (!Files.isDirectory(entry)) {
                    continue;
                }
                String categoryName = entry.getFileName().toString();
                if (categoryName.startsWith(".")) {
                    continue;
                }

                List<QADashboardResponseModel.AutomatedApiTestInfo> tests = new ArrayList<>();
                try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(entry, "*.java")) {
                    for (Path file : fileStream) {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".java")) {
                            String testClass = fileName.substring(0, fileName.length() - 5);
                            String relativePath = categoryName + "/" + fileName;
                            tests.add(new QADashboardResponseModel.AutomatedApiTestInfo(testClass, relativePath));
                            totalTests++;
                        }
                    }
                }
                tests.sort(Comparator.comparing(QADashboardResponseModel.AutomatedApiTestInfo::getTestClass));
                categories.add(new QADashboardResponseModel.AutomatedApiTestCategory(
                        categoryName, categoryName, tests));
            }
        } catch (IOException e) {
            logger.error(e);
            return new QADashboardResponseModel.AutomatedApiTestsData(
                    "../Spring-PlayWright-Automation/" + AUTOMATED_API_TESTS_PATH, 0, new ArrayList<>());
        }

        return new QADashboardResponseModel.AutomatedApiTestsData(
                "../Spring-PlayWright-Automation/" + AUTOMATED_API_TESTS_PATH, totalTests, categories);
    }

    /**
     * Resolves the path to the automated API tests folder.
     * Tries multiple relative paths from common project roots (workspace sibling layout).
     */
    private Path resolveAutomatedApiTestsPath() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path playwrightRoot = Paths.get("Spring-PlayWright-Automation");
        List<Path> possiblePaths = new ArrayList<>(Arrays.asList(
                currentDir.resolve("..").resolve(playwrightRoot).resolve(AUTOMATED_API_TESTS_PATH),
                currentDir.resolve("..").resolve("..").resolve(playwrightRoot).resolve(AUTOMATED_API_TESTS_PATH),
                currentDir.resolve(playwrightRoot).resolve(AUTOMATED_API_TESTS_PATH)
        ));

        Path parent = currentDir.getParent();
        if (parent != null) {
            possiblePaths.add(parent.resolve("Spring-PlayWright-Automation").resolve(AUTOMATED_API_TESTS_PATH));
            if (parent.getParent() != null) {
                possiblePaths.add(parent.getParent().resolve("Spring-PlayWright-Automation")
                        .resolve(AUTOMATED_API_TESTS_PATH));
            }
        }

        for (Path path : possiblePaths) {
            Path normalized = path.normalize();
            if (Files.isDirectory(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    /**
     * Returns a comprehensive list of all services with their public methods and
     * associated unit tests.
     * 
     * @return List of QAResponseModel with full endpoint-to-test mapping
     */
    @Override
    public List<QAResponseModel> getAllEndpointsWithTests() {
        List<QAResponseModel> serviceInfoList = new ArrayList<>();

        for (Map.Entry<String, ServiceControllerMapping> entry : SERVICE_MAPPINGS.entrySet()) {
            String serviceName = entry.getKey();
            ServiceControllerMapping mapping = entry.getValue();

            QAResponseModel serviceInfo = buildServiceInfo(serviceName, mapping);
            if (serviceInfo != null) {
                serviceInfoList.add(serviceInfo);
            }
        }

        return serviceInfoList;
    }

    /**
     * Returns endpoint-to-test mapping for a specific service.
     * 
     * @param serviceName The name of the service (e.g., "AddressService",
     *                    "UserService", or just "Address")
     * @return QAResponseModel for the specified service
     * @throws NotFoundException if the service is not found
     */
    @Override
    public QAResponseModel getEndpointsWithTestsByService(String serviceName) {
        // Normalize service name (add "Service" suffix if not present)
        String normalizedServiceName = serviceName;
        if (!serviceName.endsWith("Service")) {
            normalizedServiceName = serviceName + "Service";
        }

        ServiceControllerMapping mapping = SERVICE_MAPPINGS.get(normalizedServiceName);
        if (mapping == null) {
            throw new NotFoundException("Service not found: " + serviceName + ". Available services: "
                    + String.join(", ", SERVICE_MAPPINGS.keySet()));
        }

        QAResponseModel serviceInfo = buildServiceInfo(normalizedServiceName, mapping);
        if (serviceInfo == null) {
            throw new NotFoundException("Could not load service class: " + normalizedServiceName);
        }

        return serviceInfo;
    }

    /**
     * Returns a summary of test coverage across all services.
     * 
     * @return Map containing coverage statistics
     */
    @Override
    public Map<String, Object> getCoverageSummary() {
        List<QAResponseModel> serviceInfoList = new ArrayList<>();
        int totalMethods = 0;
        int totalMethodsWithCoverage = 0;
        int totalTests = 0;

        for (Map.Entry<String, ServiceControllerMapping> entry : SERVICE_MAPPINGS.entrySet()) {
            String serviceName = entry.getKey();
            ServiceControllerMapping mapping = entry.getValue();

            QAResponseModel serviceInfo = buildServiceInfo(serviceName, mapping);
            if (serviceInfo != null) {
                serviceInfoList.add(serviceInfo);
                totalMethods += serviceInfo.getTotalMethods();
                totalMethodsWithCoverage += serviceInfo.getMethodsWithCoverage();
                totalTests += serviceInfo.getTotalTests();
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalServices", serviceInfoList.size());
        summary.put("totalMethods", totalMethods);
        summary.put("totalMethodsWithCoverage", totalMethodsWithCoverage);
        summary.put("totalTests", totalTests);
        summary.put("overallCoveragePercentage",
                totalMethods > 0
                        ? Math.round(((double) totalMethodsWithCoverage / totalMethods) * 100.0 * 100.0) / 100.0
                        : 0.0);

        // Add per-service breakdown
        List<Map<String, Object>> serviceBreakdown = new ArrayList<>();
        for (QAResponseModel service : serviceInfoList) {
            Map<String, Object> serviceStats = new LinkedHashMap<>();
            serviceStats.put("serviceName", service.getServiceName());
            serviceStats.put("totalMethods", service.getTotalMethods());
            serviceStats.put("methodsWithCoverage", service.getMethodsWithCoverage());
            serviceStats.put("totalTests", service.getTotalTests());
            serviceStats.put("coveragePercentage", Math.round(service.getCoveragePercentage() * 100.0) / 100.0);
            serviceBreakdown.add(serviceStats);
        }
        summary.put("serviceBreakdown", serviceBreakdown);

        return summary;
    }

    /**
     * Returns a list of all available service names.
     * 
     * @return List of service names
     */
    @Override
    public List<String> getAvailableServices() {
        return new ArrayList<>(SERVICE_MAPPINGS.keySet());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Builds a QAResponseModel for a given service using reflection for service
     * class
     * and filesystem scanning for test files.
     * 
     * @param serviceName The name of the service class
     * @param mapping     The controller mapping information
     * @return QAResponseModel with method and test information, or null if class
     *         not found
     */
    private QAResponseModel buildServiceInfo(String serviceName, ServiceControllerMapping mapping) {
        try {
            // Load the service class
            Class<?> serviceClass = Class.forName(SERVICES_PACKAGE + "." + serviceName);

            // Read test methods from the test source file
            List<TestMethodInfo> allTestMethods = readTestMethodsFromFile(mapping.testClassName);

            // Fetch latest test results for this service (if user is authenticated)
            Map<String, LatestTestResult> latestResultsMap = new HashMap<>();
            try {
                Long clientId = getClientId();
                if (clientId != null) {
                    List<LatestTestResult> latestResults = latestTestResultRepository
                            .findByClientIdAndServiceNameOrderByTestMethodNameAsc(clientId, serviceName);
                    for (LatestTestResult result : latestResults) {
                        latestResultsMap.put(result.getTestMethodName(), result);
                    }
                }
            } catch (Exception e) {
                // If we can't get latest results (e.g., unauthenticated), continue without them
            }

            // Create service info
            QAResponseModel serviceInfo = new QAResponseModel(
                    serviceName,
                    mapping.controllerName,
                    mapping.basePath,
                    mapping.testClassName);

            // Get all public methods from the service class (excluding Object methods and
            // BaseService methods)
            for (Method method : serviceClass.getDeclaredMethods()) {
                // Only include public methods that are not excluded
                if (Modifier.isPublic(method.getModifiers()) && !EXCLUDED_METHODS.contains(method.getName())) {
                    QAResponseModel.MethodInfo methodInfo = new QAResponseModel.MethodInfo();
                    methodInfo.setMethodName(method.getName());
                    methodInfo.setApiRoute(mapping.basePath + "/" + method.getName());
                    methodInfo.setDescription(extractMethodDescription(method));

                    // Find associated unit tests with their display names and last run info
                    List<QAResponseModel.TestInfo> associatedTests = findAssociatedTests(method.getName(),
                            allTestMethods, latestResultsMap);
                    methodInfo.addUnitTests(associatedTests);

                    serviceInfo.addMethod(methodInfo);
                }
            }

            return serviceInfo;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Reads test methods from a test source file by parsing the Java source code.
     * This method scans the filesystem for the test file and extracts @Test methods
     * along with their @DisplayName annotations.
     * 
     * @param testClassName The name of the test class file (without .java
     *                      extension)
     * @return List of TestMethodInfo containing method names and display names
     */
    private List<TestMethodInfo> readTestMethodsFromFile(String testClassName) {
        List<TestMethodInfo> testMethods = new ArrayList<>();

        // Try multiple possible paths for the test file
        List<Path> possiblePaths = new ArrayList<>();

        // Current working directory based paths
        possiblePaths.add(Paths.get(TEST_SOURCE_PATH, testClassName + ".java"));
        possiblePaths.add(Paths.get("SpringApi", TEST_SOURCE_PATH, testClassName + ".java"));

        // Try to find the project root by looking for pom.xml
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        possiblePaths.add(currentDir.resolve(TEST_SOURCE_PATH).resolve(testClassName + ".java"));
        possiblePaths.add(currentDir.resolve("SpringApi").resolve(TEST_SOURCE_PATH).resolve(testClassName + ".java"));

        // Also check parent directories
        Path parent = currentDir.getParent();
        if (parent != null) {
            possiblePaths.add(parent.resolve("SpringApi").resolve(TEST_SOURCE_PATH).resolve(testClassName + ".java"));
            possiblePaths.add(parent.resolve(TEST_SOURCE_PATH).resolve(testClassName + ".java"));
        }

        Path testFilePath = null;
        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                testFilePath = path;
                break;
            }
        }

        if (testFilePath == null || !Files.exists(testFilePath)) {
            return testMethods; // Return empty list if file doesn't exist
        }

        try {
            List<String> lines = Files.readAllLines(testFilePath);

            // Lightweight line-based parser so we can map @Test methods to their declaring
            // class,
            // including @Nested inner classes (needed for Maven/Surefire selectors).
            Pattern classDeclPattern = Pattern.compile("\\bclass\\s+(\\w+)\\b");
            Pattern displayNamePattern = Pattern.compile("@DisplayName\\(\"([^\"]+)\"\\)");
            Pattern testAnnotationPattern = Pattern.compile("@Test\\b|@ParameterizedTest\\b");
            Pattern methodSignaturePattern = Pattern.compile("\\b(?:public\\s+)?void\\s+(\\w+)\\s*\\(([^)]*)\\)");

            String pendingDisplayName = null;
            boolean inTestAnnotationBlock = false;
            boolean pendingParameterizedTest = false;
            int pendingValueSourceCount = 0;
            boolean collectingValueSource = false;
            StringBuilder valueSourceBuffer = new StringBuilder();

            String pendingClassName = null;
            int pendingClassBraceDepth = -1;

            int braceDepth = 0;
            ArrayDeque<ClassScope> classStack = new ArrayDeque<>();

            Set<String> foundMethods = new HashSet<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();

                // If a class declaration starts, clear any pending display name (likely
                // class-level @DisplayName)
                Matcher classMatcher = classDeclPattern.matcher(line);
                if (classMatcher.find()) {
                    pendingClassName = classMatcher.group(1);
                    pendingClassBraceDepth = braceDepth;
                    if (!inTestAnnotationBlock) {
                        pendingDisplayName = null;
                    }
                }

                // Capture @DisplayName for the next test method (order can vary)
                Matcher displayMatcher = displayNamePattern.matcher(line);
                if (displayMatcher.find()) {
                    pendingDisplayName = displayMatcher.group(1);
                }

                // Capture @ValueSource for parameterized tests (may span multiple lines)
                if (line.contains("@ValueSource")) {
                    collectingValueSource = true;
                    valueSourceBuffer.setLength(0);
                    valueSourceBuffer.append(line);
                    if (line.contains(")")) {
                        pendingValueSourceCount = parseValueSourceCount(valueSourceBuffer.toString());
                        collectingValueSource = false;
                    }
                } else if (collectingValueSource) {
                    valueSourceBuffer.append(" ").append(line);
                    if (line.contains(")")) {
                        pendingValueSourceCount = parseValueSourceCount(valueSourceBuffer.toString());
                        collectingValueSource = false;
                    }
                }

                // Capture @Test / @ParameterizedTest start
                if (testAnnotationPattern.matcher(line).find()) {
                    inTestAnnotationBlock = true;
                    pendingParameterizedTest = line.contains("@ParameterizedTest");
                    if (!pendingParameterizedTest) {
                        // Reset any stale parameterized metadata
                        pendingValueSourceCount = 0;
                    }
                }

                // If we're in a test annotation block, the next method signature is the test
                // method
                if (inTestAnnotationBlock) {
                    Matcher methodMatcher = methodSignaturePattern.matcher(line);
                    if (methodMatcher.find()) {
                        String methodName = methodMatcher.group(1);
                        String paramList = methodMatcher.group(2);

                        String declaringTestClassName = buildDeclaringTestClassName(testClassName, classStack);

                        // Expand @ParameterizedTest + @ValueSource into individual test cases the way
                        // Surefire reports them
                        if (pendingParameterizedTest && pendingValueSourceCount > 0) {
                            String paramTypes = extractParameterTypesForSurefireName(paramList);
                            for (int i = 1; i <= pendingValueSourceCount; i++) {
                                String expandedName = methodName + "(" + paramTypes + ")[" + i + "]";
                                if (!foundMethods.contains(expandedName)) {
                                    String expandedDisplayName = pendingDisplayName != null
                                            ? pendingDisplayName + " [" + i + "]"
                                            : null;
                                    testMethods.add(new TestMethodInfo(expandedName, expandedDisplayName,
                                            declaringTestClassName));
                                    foundMethods.add(expandedName);
                                }
                            }
                        } else {
                            // Normal @Test (or @ParameterizedTest without a detectable @ValueSource)
                            if (!foundMethods.contains(methodName)) {
                                testMethods.add(
                                        new TestMethodInfo(methodName, pendingDisplayName, declaringTestClassName));
                                foundMethods.add(methodName);
                            }
                        }

                        // Reset for next test method
                        inTestAnnotationBlock = false;
                        pendingDisplayName = null;
                        pendingParameterizedTest = false;
                        pendingValueSourceCount = 0;
                        collectingValueSource = false;
                        valueSourceBuffer.setLength(0);
                    }
                }

                // Update brace depth and class stack
                for (int i = 0; i < rawLine.length(); i++) {
                    char c = rawLine.charAt(i);
                    if (c == '{') {
                        braceDepth++;
                        if (pendingClassName != null && braceDepth == pendingClassBraceDepth + 1) {
                            classStack.addLast(new ClassScope(pendingClassName, braceDepth));
                            pendingClassName = null;
                            pendingClassBraceDepth = -1;
                        }
                    } else if (c == '}') {
                        braceDepth--;
                        while (!classStack.isEmpty() && classStack.peekLast().startBraceDepth > braceDepth) {
                            classStack.removeLast();
                        }
                    }
                }
            }

        } catch (IOException e) {
            // If we can't read the file, return empty list
        }

        return testMethods;
    }

    /**
     * Parses a @ValueSource annotation string and returns how many values it
     * contains.
     * Supports the common pattern: @ValueSource(strings = {"a","b"}) and similar.
     */
    private int parseValueSourceCount(String valueSourceAnnotation) {
        if (valueSourceAnnotation == null) {
            return 0;
        }

        int openBrace = valueSourceAnnotation.indexOf('{');
        int closeBrace = valueSourceAnnotation.indexOf('}');
        if (openBrace != -1 && closeBrace != -1 && closeBrace > openBrace) {
            String inner = valueSourceAnnotation.substring(openBrace + 1, closeBrace).trim();
            if (inner.isEmpty()) {
                return 0;
            }

            String[] parts = inner.split(",");
            int count = 0;
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    count++;
                }
            }
            return count;
        }

        // Fallback: single value form without braces, e.g. strings = "abc" or ints = 1
        Matcher m = Pattern.compile("=\\s*([^,\\)]+)").matcher(valueSourceAnnotation);
        if (m.find()) {
            String val = m.group(1).trim();
            return val.isEmpty() ? 0 : 1;
        }

        return 0;
    }

    /**
     * Converts a Java parameter list (e.g. "String invalidPostalCode") into the
     * Surefire-reported type list
     * used for parameterized test case names (e.g. "String").
     */
    private String extractParameterTypesForSurefireName(String paramList) {
        if (paramList == null) {
            return "";
        }
        String trimmed = paramList.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        List<String> types = new ArrayList<>();
        String[] params = trimmed.split(",");
        for (String param : params) {
            String p = param.trim();
            if (p.isEmpty())
                continue;

            // Remove leading annotations (best-effort)
            while (p.startsWith("@")) {
                int space = p.indexOf(' ');
                if (space == -1) {
                    p = "";
                    break;
                }
                p = p.substring(space + 1).trim();
            }
            if (p.isEmpty())
                continue;

            if (p.startsWith("final ")) {
                p = p.substring("final ".length()).trim();
            }

            String[] tokens = p.split("\\s+");
            if (tokens.length == 0)
                continue;

            String typeToken = tokens[0];
            // Remove generic args (best-effort)
            typeToken = typeToken.replaceAll("<.*>", "");
            // Strip package prefix
            int dot = typeToken.lastIndexOf('.');
            if (dot != -1) {
                typeToken = typeToken.substring(dot + 1);
            }

            types.add(typeToken);
        }

        return String.join(",", types);
    }

    /**
     * Builds a Maven/Surefire-friendly test class selector for the current parsing
     * scope.
     * Example: "AddressServiceTest$GetAddressByIdTests"
     */
    private String buildDeclaringTestClassName(String outerTestClassName, ArrayDeque<ClassScope> classStack) {
        if (outerTestClassName == null || outerTestClassName.trim().isEmpty()) {
            return outerTestClassName;
        }
        if (classStack == null || classStack.isEmpty()) {
            return outerTestClassName;
        }

        List<String> names = new ArrayList<>();
        for (ClassScope scope : classStack) {
            names.add(scope.name);
        }

        int outerIndex = names.indexOf(outerTestClassName);
        if (outerIndex == -1) {
            return outerTestClassName;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = outerIndex; i < names.size(); i++) {
            if (i == outerIndex) {
                sb.append(names.get(i));
            } else {
                sb.append("$").append(names.get(i));
            }
        }
        return sb.toString();
    }

    /**
     * Finds unit test methods that are associated with a given service method.
     * Tests are matched based on naming convention: methodName_Result_Outcome
     * 
     * @param methodName       The service method name to find tests for
     * @param allTestMethods   List of all test method info objects
     * @param latestResultsMap Map of test method name to latest test result
     * @return List of TestInfo that are associated with the service method
     */
    private List<QAResponseModel.TestInfo> findAssociatedTests(String methodName, List<TestMethodInfo> allTestMethods,
            Map<String, LatestTestResult> latestResultsMap) {
        List<QAResponseModel.TestInfo> associatedTests = new ArrayList<>();

        for (TestMethodInfo testInfo : allTestMethods) {
            String testName = testInfo.methodName;

            // Primary pattern: methodName_Result_Outcome (underscore-separated)
            if (testName.startsWith(methodName + "_")) {
                QAResponseModel.TestInfo qaTestInfo = new QAResponseModel.TestInfo(testName, testInfo.displayName);
                qaTestInfo.setDeclaringTestClassName(testInfo.declaringTestClassName);
                // Populate last run info if available
                if (latestResultsMap.containsKey(testName)) {
                    qaTestInfo.populateFromLatestResult(latestResultsMap.get(testName));
                }
                associatedTests.add(qaTestInfo);
                continue;
            }

            // Secondary pattern: methodName followed by uppercase letter (camelCase
            // variant)
            if (testName.startsWith(methodName) && testName.length() > methodName.length()) {
                char nextChar = testName.charAt(methodName.length());
                if (Character.isUpperCase(nextChar)) {
                    QAResponseModel.TestInfo qaTestInfo = new QAResponseModel.TestInfo(testName, testInfo.displayName);
                    qaTestInfo.setDeclaringTestClassName(testInfo.declaringTestClassName);
                    // Populate last run info if available
                    if (latestResultsMap.containsKey(testName)) {
                        qaTestInfo.populateFromLatestResult(latestResultsMap.get(testName));
                    }
                    associatedTests.add(qaTestInfo);
                }
            }
        }

        return associatedTests;
    }

    /**
     * Extracts a description from a method name by converting camelCase to readable
     * format.
     * 
     * @param method The method to extract description for
     * @return A human-readable description string
     */
    private String extractMethodDescription(Method method) {
        String methodName = method.getName();

        StringBuilder description = new StringBuilder();
        for (int i = 0; i < methodName.length(); i++) {
            char c = methodName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                description.append(" ");
            }
            if (i == 0) {
                description.append(Character.toUpperCase(c));
            } else {
                description.append(Character.toLowerCase(c));
            }
        }

        return description.toString();
    }

    // ==================== TEST RUN TRACKING METHODS ====================

    /**
     * Saves a test run with its individual results.
     * Also updates the LatestTestResult table for each test.
     * 
     * @param request The test run request containing service info and results
     * @return TestRunResponseModel with the saved test run data
     */
    @Override
    @Transactional
    public TestRunResponseModel saveTestRun(TestRunRequestModel request) {
        // Validate request
        if (request == null) {
            throw new BadRequestException("Test run request cannot be null");
        }
        if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
            throw new BadRequestException("Service name is required");
        }
        if (request.getResults() == null || request.getResults().isEmpty()) {
            throw new BadRequestException("At least one test result is required");
        }

        Long clientId = getClientId();
        Long userId = getUserId();
        String userName = getUser();

        // Create the test run
        TestRun testRun = new TestRun(
                request.getServiceName(),
                request.getRunType() != null ? request.getRunType() : "SERVICE",
                userId,
                userName,
                clientId);
        testRun.setEnvironment(request.getEnvironment() != null ? request.getEnvironment() : "localhost");

        // Save the test run first to get the ID
        testRun = testRunRepository.save(testRun);

        // Process each result
        ServiceControllerMapping mapping = SERVICE_MAPPINGS.get(request.getServiceName());
        String testClassName = mapping != null ? mapping.testClassName : request.getServiceName() + "Test";

        for (TestRunRequestModel.TestResultData resultData : request.getResults()) {
            // Create and add the result
            TestRunResult result = new TestRunResult(
                    request.getServiceName(),
                    resultData.getMethodName() != null ? resultData.getMethodName() : "",
                    testClassName,
                    resultData.getTestMethodName(),
                    resultData.getDisplayName(),
                    resultData.getStatus(),
                    resultData.getDurationMs() != null ? resultData.getDurationMs() : 0,
                    resultData.getErrorMessage(),
                    resultData.getStackTrace(),
                    clientId);
            testRun.addResult(result);

            // Update or create the latest result entry
            upsertLatestTestResult(result, testRun.getTestRunId(), userId, userName, clientId);
        }

        // Mark the test run as complete
        testRun.complete();

        // Save all changes
        testRun = testRunRepository.save(testRun);

        return new TestRunResponseModel(testRun);
    }

    /**
     * Returns the latest test results for a service or all services.
     * 
     * @param serviceName The service name to filter by (null or empty for all
     *                    services)
     * @return List of LatestTestResultResponseModel
     */
    @Override
    public List<LatestTestResultResponseModel> getLatestTestResults(String serviceName) {
        Long clientId = getClientId();

        List<LatestTestResult> results;
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            // Normalize service name
            String normalizedServiceName = serviceName;
            if (!serviceName.endsWith("Service")) {
                normalizedServiceName = serviceName + "Service";
            }
            results = latestTestResultRepository.findByClientIdAndServiceNameOrderByTestMethodNameAsc(
                    clientId, normalizedServiceName);
        } else {
            results = latestTestResultRepository.findByClientIdOrderByServiceNameAscTestMethodNameAsc(clientId);
        }

        return results.stream()
                .map(LatestTestResultResponseModel::new)
                .collect(Collectors.toList());
    }

    /**
     * Updates or creates a LatestTestResult entry for a test.
     */
    private void upsertLatestTestResult(TestRunResult result, Long testRunId, Long userId,
            String userName, Long clientId) {
        Optional<LatestTestResult> existingOpt = latestTestResultRepository
                .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
                        clientId, result.getServiceName(), result.getTestClassName(), result.getTestMethodName());

        if (existingOpt.isPresent()) {
            // Update existing
            LatestTestResult existing = existingOpt.get();
            existing.updateFromResult(result, testRunId, userId, userName);
            latestTestResultRepository.save(existing);
        } else {
            // Create new
            LatestTestResult newResult = new LatestTestResult(
                    result.getServiceName(),
                    result.getTestClassName(),
                    result.getTestMethodName(),
                    result.getStatus(),
                    result.getDurationMs(),
                    result.getErrorMessage(),
                    result.getStackTrace(),
                    testRunId,
                    userId,
                    userName,
                    clientId);
            latestTestResultRepository.save(newResult);
        }
    }

    // ==================== TEST EXECUTION METHODS ====================

    /**
     * Starts an async test execution using Maven.
     * Returns immediately with status object for polling.
     * 
     * @param request The test execution request
     * @return TestExecutionStatusModel with executionId and initial status
     */
    @Override
    public TestExecutionStatusModel startTestExecution(TestExecutionRequestModel request) {
        // Validate request
        if (request == null) {
            throw new BadRequestException("Test execution request cannot be null");
        }

        // Generate unique execution ID
        String executionId = UUID.randomUUID().toString();

        // Determine test scope and build Maven command
        String testClassName = null;
        String testMethodFilter = null;
        String serviceName = null;

        if (Boolean.TRUE.equals(request.getRunAll())) {
            // Run all tests - no filter
            serviceName = "ALL";
        } else if (request.getTestNames() != null && !request.getTestNames().isEmpty()) {
            // Run specific test methods
            testClassName = request.getTestClassName();
            if (testClassName == null || testClassName.isEmpty()) {
                throw new BadRequestException("testClassName is required when running specific tests");
            }

            // Surefire cannot select an individual parameterized invocation (e.g.
            // method(String)[1]).
            // If UI passes a parameterized case name, normalize it to the base method name
            // so the tests actually run.
            List<String> executableTestNames = request.getTestNames().stream()
                    .filter(Objects::nonNull)
                    .map(this::stripParameterizedSuffix)
                    .distinct()
                    .collect(Collectors.toList());

            // If caller passed the OUTER test class (e.g. AddressServiceTest) but the
            // method lives in a @Nested class,
            // resolve to the declaring nested class so surefire can actually find the test.
            if (request.getTestNames().size() == 1 && !testClassName.contains("$")) {
                String resolvedClass = resolveDeclaringTestClassForTestMethod(testClassName,
                        executableTestNames.get(0));
                if (resolvedClass != null && resolvedClass.contains("$")) {
                    testClassName = resolvedClass;
                }
            }

            testMethodFilter = String.join("+", executableTestNames);
            serviceName = testClassName;
        } else if (request.getMethodName() != null && !request.getMethodName().isEmpty()) {

            // 1. Determine Test Class Name
            if (request.getTestClassName() != null && !request.getTestClassName().isEmpty()) {
                testClassName = request.getTestClassName();
                // If serviceName is also provided, use it; otherwise default to class name
                serviceName = request.getServiceName() != null ? request.getServiceName() : testClassName;
            } else if (request.getServiceName() != null && !request.getServiceName().isEmpty()) {
                // Resolve from Service Name
                String normalizedServiceName = request.getServiceName();
                if (!normalizedServiceName.endsWith("Service")) {
                    normalizedServiceName = normalizedServiceName + "Service";
                }
                ServiceControllerMapping mapping = SERVICE_MAPPINGS.get(normalizedServiceName);
                if (mapping == null) {
                    // Try to guess or fail. For now, assume strict mapping or standard naming
                    testClassName = normalizedServiceName + "Test";
                } else {
                    testClassName = mapping.testClassName;
                }
                serviceName = normalizedServiceName;
            } else {
                throw new BadRequestException(
                        "Must specify serviceName or testClassName when running tests by method name");
            }

            String methodName = request.getMethodName();

            // 2. Resolve associated unit tests
            List<TestMethodInfo> allTestMethods = readTestMethodsFromFile(testClassName);
            List<QAResponseModel.TestInfo> associatedTests = findAssociatedTests(methodName, allTestMethods,
                    Collections.emptyMap());

            if (associatedTests.isEmpty()) {
                throw new BadRequestException(
                        "No tests found for method: " + methodName + " in class " + testClassName);
            }

            // 3. Construct Surefire Filter
            // Strip parameterized suffixes and get unique test names
            List<String> executableTestNames = associatedTests.stream()
                    .map(QAResponseModel.TestInfo::getTestMethodName)
                    .map(this::stripParameterizedSuffix)
                    .distinct()
                    .collect(Collectors.toList());

            // If all tests are in a nested class, resolve to that nested class
            // Check if we have a single nested class for all tests
            String declaringClass = associatedTests.stream()
                    .map(QAResponseModel.TestInfo::getDeclaringTestClassName)
                    .filter(dcn -> dcn != null && dcn.contains("$"))
                    .findFirst()
                    .orElse(null);

            if (declaringClass != null && !testClassName.contains("$")) {
                // All tests are in a nested class, update testClassName to run that specific
                // class
                // But surefire -Dtest=Outer$Nested means we run everything in nested.
                // If we also provide method names, we do -Dtest=Outer$Nested#method1+method2
                testClassName = declaringClass;
            } else if (associatedTests.size() == 1 && !testClassName.contains("$")) {
                // Single test - resolve its declaring class
                String resolvedClass = associatedTests.get(0).getDeclaringTestClassName();
                if (resolvedClass != null && resolvedClass.contains("$")) {
                    testClassName = resolvedClass;
                }
            }

            testMethodFilter = String.join("+", executableTestNames);
        } else {
            throw new BadRequestException("Must specify runAll, testNames, or methodName+testClassName");
        }

        // Calculate expected test count for progress tracking
        int expectedTestCount = calculateExpectedTestCount(serviceName, request.getMethodName(),
                request.getTestNames());

        // Create initial status
        TestExecutionStatusModel status = new TestExecutionStatusModel(executionId, serviceName,
                request.getMethodName(), expectedTestCount);

        // Store in TestExecutorService (separate service so @Async works)
        testExecutorService.storeStatus(executionId, status);

        // Start async execution in separate service (returns immediately)
        testExecutorService.executeTestsAsync(executionId, testClassName, testMethodFilter, serviceName);

        return status;
    }

    /**
     * Resolves the declaring test class selector for a specific test method name.
     * For @Nested tests, this returns "OuterTest$NestedClass".
     */
    private String resolveDeclaringTestClassForTestMethod(String outerTestClassName, String testMethodName) {
        if (outerTestClassName == null || outerTestClassName.trim().isEmpty()
                || testMethodName == null || testMethodName.trim().isEmpty()) {
            return null;
        }

        List<TestMethodInfo> testMethods = readTestMethodsFromFile(outerTestClassName);
        String normalizedRequested = stripParameterizedSuffix(testMethodName);
        for (TestMethodInfo testInfo : testMethods) {
            if (testMethodName.equals(testInfo.methodName)) {
                return testInfo.declaringTestClassName;
            }
            // Parameterized tests: match on base method name as well
            if (normalizedRequested.equals(stripParameterizedSuffix(testInfo.methodName))) {
                return testInfo.declaringTestClassName;
            }
        }
        return null;
    }

    /**
     * Strips the Surefire parameterized suffix from a test case name.
     * Example: "foo(String)[2]" -> "foo"
     */
    private String stripParameterizedSuffix(String testName) {
        if (testName == null) {
            return null;
        }
        return testName.replaceAll("\\([^)]*\\)\\[\\d+\\]$", "");
    }

    /**
     * Calculates the expected number of tests based on the execution scope.
     */
    private int calculateExpectedTestCount(String serviceName, String methodName, List<String> testNames) {
        // If running specific tests, we know the exact count
        if (testNames != null && !testNames.isEmpty()) {
            return testNames.size();
        }

        // Try to get from dashboard data (which scans test files)
        try {
            QADashboardResponseModel dashboard = getDashboardData();

            if ("ALL".equals(serviceName)) {
                // Count all tests across all services
                return dashboard.getServices().stream()
                        .flatMap(s -> s.getMethods().stream())
                        .mapToInt(QAResponseModel.MethodInfo::getTestCount)
                        .sum();
            }

            // Find the specific service
            QAResponseModel service = dashboard.getServices().stream()
                    .filter(s -> s.getServiceName().equals(serviceName))
                    .findFirst()
                    .orElse(null);

            if (service != null) {
                if (methodName != null && !methodName.isEmpty()) {
                    // Count tests for specific method
                    return service.getMethods().stream()
                            .filter(m -> m.getMethodName().equals(methodName))
                            .mapToInt(QAResponseModel.MethodInfo::getTestCount)
                            .sum();
                } else {
                    // Count all tests for the service
                    return service.getMethods().stream()
                            .mapToInt(QAResponseModel.MethodInfo::getTestCount)
                            .sum();
                }
            }
        } catch (Exception e) {
            // If we can't determine, return 0 (will be updated during execution)
            logger.error(e);
        }

        return 0;
    }

    /**
     * Gets the current status and progress of a test execution.
     */
    @Override
    public TestExecutionStatusModel getTestExecutionStatus(String executionId) {
        TestExecutionStatusModel status = testExecutorService.getStatus(executionId);
        if (status == null) {
            throw new NotFoundException("Test execution not found: " + executionId);
        }
        return status;
    }
}
