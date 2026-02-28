package springapi.services;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springapi.ErrorMessages;
import springapi.authentication.JwtTokenProvider;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.logging.ContextualLogger;
import springapi.models.databasemodels.LatestTestResult;
import springapi.models.databasemodels.TestRun;
import springapi.models.databasemodels.TestRunResult;
import springapi.models.requestmodels.TestExecutionRequestModel;
import springapi.models.requestmodels.TestRunRequestModel;
import springapi.models.responsemodels.LatestTestResultResponseModel;
import springapi.models.responsemodels.QaDashboardResponseModel;
import springapi.models.responsemodels.QaResponseModel;
import springapi.models.responsemodels.TestExecutionStatusModel;
import springapi.models.responsemodels.TestRunResponseModel;
import springapi.repositories.LatestTestResultRepository;
import springapi.repositories.TestRunRepository;
import springapi.services.interfaces.QaSubTranslator;

/**
 * Service class for QA endpoint-to-test mapping operations.
 *
 * <p>
 * This service provides comprehensive information about all public service
 * methods and their
 * associated unit tests. It uses reflection to dynamically scan service classes
 * and reads test
 * source files from the filesystem to match test methods to service methods
 * based on naming
 * conventions.
 *
 * <p>
 * Test method naming convention: methodName_Result_Outcome Example:
 * toggleAddress_AddressNotFound_ThrowsNotFoundException
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class QaService extends BaseService implements QaSubTranslator {

  // Package paths for scanning
  private static final String SERVICES_PACKAGE = "springapi.services";
  private static final String SERVICE_SUFFIX = "Service";
  private static final String JAVA_EXTENSION = ".java";
  private static final String SPRING_API_DIR = "SpringApi";
  private static final String USER_DIR_PROPERTY = "user.dir";
  private static final String STATUS_RUNNING = "RUNNING";
  private static final String POM_XML = "pom.xml";
  private static final String PLAYWRIGHT_RELATIVE_ROOT = "../Spring-PlayWright-Automation/";
  private static final String PLAYWRIGHT_PROJECT_DIR = "Spring-PlayWright-Automation";

  // Test source file paths (relative to project root)
  // Keep both paths for backward compatibility with older layouts.
  private static final String TEST_SOURCE_PATH = "src/test/java/springapi/servicetests";
  private static final String LEGACY_TEST_SOURCE_PATH = "src/test/java/springapi/services/tests";

  // Automated API tests path (relative - Spring-PlayWright-Automation project)
  private static final String AUTOMATED_API_TESTS_PATH = "src/test/java/com/ultimatecompany/tests/ApiTests";

  private static final Set<String> EXCLUDED_METHODS = new HashSet<>(
      Arrays.asList(
          "equals",
          "hashCode",
          "toString",
          "getClass",
          "notify",
          "notifyAll",
          "wait",
          "getUser",
          "getUserId",
          "getClientId",
          "getLoginName"));

  // Service-to-Controller mapping for API routes
  private static final Map<String, ServiceControllerMapping> SERVICE_MAPPINGS = new LinkedHashMap<>();

  // Test execution tracking (moved from TestExecutorService)
  private static final Map<String, TestExecutionStatusModel> activeExecutions = new ConcurrentHashMap<>();
  private static final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

  private static final Pattern TESTS_RUN_PATTERN = Pattern
      .compile("Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+)");
  private static final Pattern RUNNING_TEST_CLASS_PATTERN = Pattern.compile("Running ([\\w.]+Test)");

  static {
    // Initialize service mappings with controller and base path information
    SERVICE_MAPPINGS.put(
        "AddressService",
        new ServiceControllerMapping("AddressController", "/api/Address", "AddressServiceTest"));
    SERVICE_MAPPINGS.put(
        "ClientService",
        new ServiceControllerMapping("ClientController", "/api/Client", "ClientServiceTest"));
    SERVICE_MAPPINGS.put(
        "LeadService",
        new ServiceControllerMapping("LeadController", "/api/Lead", "LeadServiceTest"));
    SERVICE_MAPPINGS.put(
        "LoginService",
        new ServiceControllerMapping("LoginController", "/api/Login", "LoginServiceTest"));
    SERVICE_MAPPINGS.put(
        "MessageService",
        new ServiceControllerMapping("MessageController", "/api/Message", "MessageServiceTest"));
    SERVICE_MAPPINGS.put(
        "PackageService",
        new ServiceControllerMapping("PackageController", "/api/Package", "PackageServiceTest"));
    SERVICE_MAPPINGS.put(
        "PaymentService",
        new ServiceControllerMapping("PaymentController", "/api/Payments", "PaymentServiceTest"));
    SERVICE_MAPPINGS.put(
        "PickupLocationService",
        new ServiceControllerMapping(
            "PickupLocationController", "/api/PickupLocation", "PickupLocationServiceTest"));
    SERVICE_MAPPINGS.put(
        "ProductService",
        new ServiceControllerMapping("ProductController", "/api/Product", "ProductServiceTest"));
    SERVICE_MAPPINGS.put(
        "ProductReviewService",
        new ServiceControllerMapping(
            "ProductReviewController", "/api/ProductReview", "ProductReviewServiceTest"));
    SERVICE_MAPPINGS.put(
        "QaService", new ServiceControllerMapping("QaController", "/api/QA", "QAServiceTest"));
    SERVICE_MAPPINGS.put(
        "PromoService",
        new ServiceControllerMapping("PromoController", "/api/Promo", "PromoServiceTest"));
    SERVICE_MAPPINGS.put(
        "PurchaseOrderService",
        new ServiceControllerMapping(
            "PurchaseOrderController", "/api/PurchaseOrder", "PurchaseOrderServiceTest"));
    SERVICE_MAPPINGS.put(
        "ShipmentService",
        new ServiceControllerMapping("ShipmentController", "/api/Shipment", "ShipmentServiceTest"));
    SERVICE_MAPPINGS.put(
        "ShipmentProcessingService",
        new ServiceControllerMapping(
            "ShippingController", "/api/Shipping", "ShipmentProcessingServiceTest"));
    SERVICE_MAPPINGS.put(
        "ShippingService",
        new ServiceControllerMapping("ShippingController", "/api/Shipping", "ShippingServiceTest"));
    SERVICE_MAPPINGS.put(
        "TodoService",
        new ServiceControllerMapping("TodoController", "/api/Todo", "TodoServiceTest"));
    SERVICE_MAPPINGS.put(
        "UserService",
        new ServiceControllerMapping("UserController", "/api/User", "UserServiceTest"));
    SERVICE_MAPPINGS.put(
        "UserGroupService",
        new ServiceControllerMapping(
            "UserGroupController", "/api/UserGroup", "UserGroupServiceTest"));
    SERVICE_MAPPINGS.put(
        "UserLogService",
        new ServiceControllerMapping("UserLogController", "/api/UserLog", "UserLogServiceTest"));
  }

  // ==================== REPOSITORIES ====================

  private static final ContextualLogger logger = ContextualLogger.getLogger(QaService.class);

  private final TestRunRepository testRunRepository;
  private final LatestTestResultRepository latestTestResultRepository;

  /** Executes qa service. */
  @Autowired
  public QaService(
      TestRunRepository testRunRepository,
      LatestTestResultRepository latestTestResultRepository,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.testRunRepository = testRunRepository;
    this.latestTestResultRepository = latestTestResultRepository;
  }

  /** Helper class to store service-to-controller mapping information. */
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

  /** Helper class to store test method information including display name. */
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
   * their declaring.
   * class (including @Nested inner classes).
   */
  private static class ClassScope {
    final String name;
    final int startBraceDepth;

    ClassScope(String name, int startBraceDepth) {
      this.name = name;
      this.startBraceDepth = startBraceDepth;
    }
  }

  /**
   * Returns all QA dashboard data in a single response. Includes services with
   * methods/tests,.
   * coverage summary, and available services.
   *
   * @return QaDashboardResponseModel with all dashboard data
   */
  @Override
  public QaDashboardResponseModel getDashboardData() {
    // Get all services with their methods, tests, and last run info
    List<QaResponseModel> services = getAllEndpointsWithTests();

    // Calculate coverage summary
    int totalMethods = 0;
    int totalMethodsWithCoverage = 0;
    int totalTests = 0;
    List<QaDashboardResponseModel.ServiceBreakdownData> serviceBreakdown = new ArrayList<>();

    for (QaResponseModel service : services) {
      totalMethods += service.getTotalMethods();
      totalMethodsWithCoverage += service.getMethodsWithCoverage();
      totalTests += service.getTotalTests();

      serviceBreakdown.add(
          new QaDashboardResponseModel.ServiceBreakdownData(
              service.getServiceName(),
              service.getTotalMethods(),
              service.getMethodsWithCoverage(),
              service.getTotalTests(),
              service.getCoveragePercentage()));
    }

    double overallCoverage = totalMethods > 0
        ? Math.round(((double) totalMethodsWithCoverage / totalMethods) * 100.0 * 100.0) / 100.0
        : 0.0;
    QaDashboardResponseModel.CoverageSummaryData coverageSummary = new QaDashboardResponseModel.CoverageSummaryData(
        services.size(),
        totalMethods,
        totalMethodsWithCoverage,
        totalTests,
        overallCoverage,
        serviceBreakdown);

    // Get available services
    List<String> availableServices = new ArrayList<>(SERVICE_MAPPINGS.keySet());

    // Get automated API tests (separate section from unit tests)
    QaDashboardResponseModel.AutomatedApiTestsData automatedApiTests = getAutomatedApiTests();

    return new QaDashboardResponseModel(
        services, coverageSummary, availableServices, automatedApiTests);
  }

  /**
   * Returns a comprehensive list of all services with their public methods and
   * associated unit.
   * tests.
   *
   * @return List of QaResponseModel with full endpoint-to-test mapping
   */
  @Override
  public List<QaResponseModel> getAllEndpointsWithTests() {
    List<QaResponseModel> serviceInfoList = new ArrayList<>();

    for (Map.Entry<String, ServiceControllerMapping> entry : SERVICE_MAPPINGS.entrySet()) {
      String serviceName = entry.getKey();
      ServiceControllerMapping mapping = entry.getValue();

      QaResponseModel serviceInfo = buildServiceInfo(serviceName, mapping);
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
   *                    "UserService", or just
   *                    "Address")
   * @return QaResponseModel for the specified service
   * @throws NotFoundException if the service is not found
   */
  @Override
  public QaResponseModel getEndpointsWithTestsByService(String serviceName) {
    if (serviceName == null) {
      throw new NullPointerException(ErrorMessages.QaErrorMessages.SERVICE_NAME_NULL);
    }

    String trimmedServiceName = serviceName.trim();
    // Normalize service name (add "Service" suffix if not present)
    String normalizedServiceName = trimmedServiceName;
    if ("qa".equalsIgnoreCase(trimmedServiceName)
        || ("qa" + SERVICE_SUFFIX).equalsIgnoreCase(trimmedServiceName)) {
      normalizedServiceName = "QaService";
    } else if (!trimmedServiceName.endsWith(SERVICE_SUFFIX)) {
      normalizedServiceName = trimmedServiceName + SERVICE_SUFFIX;
    }

    ServiceControllerMapping mapping = SERVICE_MAPPINGS.get(normalizedServiceName);
    if (mapping == null) {
      throw new NotFoundException(
          String.format(
              ErrorMessages.QaErrorMessages.SERVICE_NOT_FOUND_FORMAT,
              trimmedServiceName,
              String.join(", ", SERVICE_MAPPINGS.keySet())));
    }

    QaResponseModel serviceInfo = buildServiceInfo(normalizedServiceName, mapping);
    if (serviceInfo == null) {
      throw new NotFoundException(
          String.format(
              ErrorMessages.QaErrorMessages.COULD_NOT_LOAD_SERVICE_CLASS_FORMAT,
              normalizedServiceName));
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
    List<QaResponseModel> serviceInfoList = new ArrayList<>();
    int totalMethods = 0;
    int totalMethodsWithCoverage = 0;
    int totalTests = 0;

    for (Map.Entry<String, ServiceControllerMapping> entry : SERVICE_MAPPINGS.entrySet()) {
      String serviceName = entry.getKey();
      ServiceControllerMapping mapping = entry.getValue();

      QaResponseModel serviceInfo = buildServiceInfo(serviceName, mapping);
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
    summary.put(
        "overallCoveragePercentage",
        totalMethods > 0
            ? Math.round(((double) totalMethodsWithCoverage / totalMethods) * 100.0 * 100.0) / 100.0
            : 0.0);

    // Add per-service breakdown
    List<Map<String, Object>> serviceBreakdown = new ArrayList<>();
    for (QaResponseModel service : serviceInfoList) {
      Map<String, Object> serviceStats = new LinkedHashMap<>();
      serviceStats.put("serviceName", service.getServiceName());
      serviceStats.put("totalMethods", service.getTotalMethods());
      serviceStats.put("methodsWithCoverage", service.getMethodsWithCoverage());
      serviceStats.put("totalTests", service.getTotalTests());
      serviceStats.put(
          "coveragePercentage", Math.round(service.getCoveragePercentage() * 100.0) / 100.0);
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
    return java.util.Collections.unmodifiableList(new ArrayList<>(SERVICE_MAPPINGS.keySet()));
  }

  /**
   * Builds a QaResponseModel for a given service using reflection for service
   * class and filesystem.
   * scanning for test files.
   *
   * @param serviceName The name of the service class
   * @param mapping     The controller mapping information
   * @return QaResponseModel with method and test information, or null if class
   *         not found
   */
  private QaResponseModel buildServiceInfo(String serviceName, ServiceControllerMapping mapping) {
    try {
      Class<?> serviceClass = Class.forName(SERVICES_PACKAGE + "." + serviceName);
      List<TestMethodInfo> allTestMethods = readTestMethodsFromFile(mapping.testClassName);

      Map<String, LatestTestResult> latestResultsMap = new HashMap<>();
      try {
        Long clientId = getClientId();
        if (clientId != null) {
          List<LatestTestResult> latestResults = latestTestResultRepository
              .findByClientIdAndServiceNameOrderByTestMethodNameAsc(
                  clientId, serviceName);
          for (LatestTestResult result : latestResults) {
            latestResultsMap.put(result.getTestMethodName(), result);
          }
        }
      } catch (Exception e) {
        // If we can't get latest results (e.g., unauthenticated), continue without them
      }

      QaResponseModel serviceInfo = new QaResponseModel(
          serviceName, mapping.controllerName, mapping.basePath, mapping.testClassName);

      for (Method method : serviceClass.getDeclaredMethods()) {
        if (Modifier.isPublic(method.getModifiers())
            && !EXCLUDED_METHODS.contains(method.getName())) {
          QaResponseModel.MethodInfo methodInfo = new QaResponseModel.MethodInfo(
              method.getName(),
              mapping.basePath + "/" + method.getName(),
              extractMethodDescription(method));

          List<QaResponseModel.TestInfo> associatedTests = findAssociatedTests(method.getName(), allTestMethods,
              latestResultsMap);
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
   * This method scans.
   * the filesystem for the test file and extracts @Test methods along with
   * their @DisplayName
   * annotations.
   *
   * @param testClassName The name of the test class file (without .java
   *                      extension)
   * @return List of TestMethodInfo containing method names and display names
   */
  private List<TestMethodInfo> readTestMethodsFromFile(String testClassName) {
    List<TestMethodInfo> testMethods = new ArrayList<>();

    // Try multiple possible paths for the test file
    List<Path> possiblePaths = new ArrayList<>();

    List<String> sourceRoots = List.of(TEST_SOURCE_PATH, LEGACY_TEST_SOURCE_PATH);

    // Try to find the project root by looking for pom.xml
    Path currentDir = Paths.get(System.getProperty(USER_DIR_PROPERTY));
    Path parent = currentDir.getParent();

    for (String sourceRoot : sourceRoots) {
      // Current working directory based paths
      possiblePaths.add(Paths.get(sourceRoot, testClassName + JAVA_EXTENSION));
      possiblePaths.add(Paths.get(SPRING_API_DIR, sourceRoot, testClassName + JAVA_EXTENSION));
      possiblePaths.add(currentDir.resolve(sourceRoot).resolve(testClassName + JAVA_EXTENSION));
      possiblePaths.add(
          currentDir
              .resolve(SPRING_API_DIR)
              .resolve(sourceRoot)
              .resolve(testClassName + JAVA_EXTENSION));

      // Also check parent directories
      if (parent != null) {
        possiblePaths.add(
            parent
                .resolve(SPRING_API_DIR)
                .resolve(sourceRoot)
                .resolve(testClassName + JAVA_EXTENSION));
        possiblePaths.add(parent.resolve(sourceRoot).resolve(testClassName + JAVA_EXTENSION));
      }
    }

    // Also try with directory segments lowercased (e.g. "QA/StartTestExecutionTest"
    // → "qa/StartTestExecutionTest") to handle filesystems where the test class
    // path
    // uses a different case than the actual on-disk directory.
    String normalizedTestClassName = testClassName;
    int lastSlash = Math.max(testClassName.lastIndexOf('/'), testClassName.lastIndexOf('\\'));
    if (lastSlash > 0) {
      String dirPart = testClassName.substring(0, lastSlash).toLowerCase();
      String filePart = testClassName.substring(lastSlash + 1);
      normalizedTestClassName = dirPart + "/" + filePart;
    }

    if (!normalizedTestClassName.equals(testClassName)) {
      for (String sourceRoot : sourceRoots) {
        possiblePaths.add(Paths.get(sourceRoot, normalizedTestClassName + JAVA_EXTENSION));
        possiblePaths.add(
            Paths.get(SPRING_API_DIR, sourceRoot, normalizedTestClassName + JAVA_EXTENSION));
        possiblePaths.add(
            currentDir.resolve(sourceRoot).resolve(normalizedTestClassName + JAVA_EXTENSION));
        possiblePaths.add(
            currentDir
                .resolve(SPRING_API_DIR)
                .resolve(sourceRoot)
                .resolve(normalizedTestClassName + JAVA_EXTENSION));
        if (parent != null) {
          possiblePaths.add(
              parent
                  .resolve(SPRING_API_DIR)
                  .resolve(sourceRoot)
                  .resolve(normalizedTestClassName + JAVA_EXTENSION));
          possiblePaths.add(
              parent.resolve(sourceRoot).resolve(normalizedTestClassName + JAVA_EXTENSION));
        }
      }
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
                  String expandedDisplayName = pendingDisplayName != null ? pendingDisplayName + " [" + i + "]" : null;
                  testMethods.add(
                      new TestMethodInfo(
                          expandedName, expandedDisplayName, declaringTestClassName));
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
   * contains. Supports the.
   * common pattern: @ValueSource(strings = {"a","b"}) and similar.
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
    Matcher m = Pattern.compile("=\\s*([^,)]+)").matcher(valueSourceAnnotation);
    if (m.find()) {
      String val = m.group(1).trim();
      return val.isEmpty() ? 0 : 1;
    }

    return 0;
  }

  /**
   * Converts a Java parameter list (e.g. "String invalidPostalCode") into the
   * Surefire-reported.
   * type list used for parameterized test case names (e.g. "String").
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
      if (!p.isEmpty()) {
        // Remove leading annotations (best-effort)
        while (p.startsWith("@")) {
          int space = p.indexOf(' ');
          if (space == -1) {
            p = "";
          } else {
            p = p.substring(space + 1).trim();
          }
        }

        if (p.startsWith("final ")) {
          p = p.substring("final ".length()).trim();
        }

        if (!p.isEmpty()) {
          String[] tokens = p.split("\\s+");
          if (tokens.length > 0) {
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
        }
      }
    }

    return String.join(",", types);
  }

  /**
   * Builds a Maven/Surefire-friendly test class selector for the current parsing
   * scope. Example:.
   * "AddressServiceTest$GetAddressByIdTests"
   */
  private String buildDeclaringTestClassName(
      String outerTestClassName, ArrayDeque<ClassScope> classStack) {
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
   * Tests are matched.
   * based on naming convention: methodName_Result_Outcome
   *
   * @param methodName       The service method name to find tests for
   * @param allTestMethods   List of all test method info objects
   * @param latestResultsMap Map of test method name to latest test result
   * @return List of TestInfo that are associated with the service method
   */
  private List<QaResponseModel.TestInfo> findAssociatedTests(
      String methodName,
      List<TestMethodInfo> allTestMethods,
      Map<String, LatestTestResult> latestResultsMap) {
    List<QaResponseModel.TestInfo> associatedTests = new ArrayList<>();

    for (TestMethodInfo testInfo : allTestMethods) {
      String testName = testInfo.methodName;

      // Primary pattern: methodName_Result_Outcome (underscore-separated)
      if (testName.startsWith(methodName + "_")) {
        QaResponseModel.TestInfo qaTestInfo = new QaResponseModel.TestInfo(
            testName, testInfo.displayName, testInfo.declaringTestClassName);
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
          QaResponseModel.TestInfo qaTestInfo = new QaResponseModel.TestInfo(
              testName, testInfo.displayName, testInfo.declaringTestClassName);
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

  /**
   * Saves a test run with its individual results. Also updates the
   * LatestTestResult table for each.
   * test.
   *
   * @param request The test run request containing service info and results
   * @return TestRunResponseModel with the saved test run data
   */
  @Override
  @Transactional
  public TestRunResponseModel saveTestRun(TestRunRequestModel request) {
    // Validate request
    if (request == null) {
      throw new BadRequestException(ErrorMessages.QaErrorMessages.TEST_RUN_REQUEST_CANNOT_BE_NULL);
    }
    if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.QaErrorMessages.SERVICE_NAME_REQUIRED);
    }
    if (request.getResults() == null || request.getResults().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.QaErrorMessages.AT_LEAST_ONE_TEST_RESULT_REQUIRED);
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
    testRun.setEnvironment(
        request.getEnvironment() != null ? request.getEnvironment() : "localhost");

    // Save the test run first to get the ID
    testRun = testRunRepository.save(testRun);

    // Process each result
    ServiceControllerMapping mapping = SERVICE_MAPPINGS.get(request.getServiceName());
    String testClassName = mapping != null ? mapping.testClassName : request.getServiceName() + "Test";

    for (TestRunRequestModel.TestResultData resultData : request.getResults()) {
      // Create and add the result
      TestRunResult result = new TestRunResult();
      result.setServiceName(request.getServiceName());
      result.setMethodName(resultData.getMethodName() != null ? resultData.getMethodName() : "");
      result.setTestClassName(testClassName);
      result.setTestMethodName(resultData.getTestMethodName());
      result.setDisplayName(resultData.getDisplayName());
      result.setStatus(resultData.getStatus());
      result.setDurationMs(resultData.getDurationMs() != null ? resultData.getDurationMs() : 0);
      result.setErrorMessage(resultData.getErrorMessage());
      result.setStackTrace(resultData.getStackTrace());
      result.setClientId(clientId);
      result.setExecutedAt(LocalDateTime.now());
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
      if (!serviceName.endsWith(SERVICE_SUFFIX)) {
        normalizedServiceName = serviceName + SERVICE_SUFFIX;
      }
      results = latestTestResultRepository.findByClientIdAndServiceNameOrderByTestMethodNameAsc(
          clientId, normalizedServiceName);
    } else {
      results = latestTestResultRepository.findByClientIdOrderByServiceNameAscTestMethodNameAsc(clientId);
    }

    return results.stream()
        .map(LatestTestResultResponseModel::new)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /** Updates or creates a LatestTestResult entry for a test. */
  private void upsertLatestTestResult(
      TestRunResult result, Long testRunId, Long userId, String userName, Long clientId) {
    Optional<LatestTestResult> existingOpt = latestTestResultRepository
        .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
            clientId,
            result.getServiceName(),
            result.getTestClassName(),
            result.getTestMethodName());

    if (existingOpt.isPresent()) {
      // Update existing
      LatestTestResult existing = existingOpt.get();
      existing.updateFromResult(result, testRunId, userId, userName);
      latestTestResultRepository.save(existing);
    } else {
      // Create new
      LatestTestResult newResult = new LatestTestResult();
      newResult.setServiceName(result.getServiceName());
      newResult.setTestClassName(result.getTestClassName());
      newResult.setTestMethodName(result.getTestMethodName());
      newResult.setStatus(result.getStatus());
      newResult.setDurationMs(result.getDurationMs());
      newResult.setErrorMessage(result.getErrorMessage());
      newResult.setStackTrace(result.getStackTrace());
      newResult.setLastRunId(testRunId);
      newResult.setLastRunByUserId(userId);
      newResult.setLastRunByUserName(userName);
      newResult.setLastRunAt(LocalDateTime.now());
      newResult.setClientId(clientId);
      latestTestResultRepository.save(newResult);
    }
  }

  /**
   * Starts an async test execution using Maven. Returns immediately with status
   * object for polling.
   *
   * @param request The test execution request
   * @return TestExecutionStatusModel with executionId and initial status
   */
  @Override
  public TestExecutionStatusModel startTestExecution(TestExecutionRequestModel request) {
    // Validate request
    if (request == null) {
      throw new BadRequestException(
          ErrorMessages.QaErrorMessages.TEST_EXECUTION_REQUEST_CANNOT_BE_NULL);
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
        throw new BadRequestException(ErrorMessages.QaErrorMessages.TEST_CLASS_NAME_REQUIRED);
      }

      // Surefire cannot select an individual parameterized invocation (e.g.
      // method(String)[1]).
      // If UI passes a parameterized case name, normalize it to the base method name
      // so the tests actually run.
      List<String> executableTestNames = request.getTestNames().stream()
          .filter(Objects::nonNull)
          .map(this::stripParameterizedSuffix)
          .distinct()
          .collect(Collectors.toCollection(ArrayList::new));

      // If caller passed the OUTER test class (e.g. AddressServiceTest) but the
      // method lives in a @Nested class,
      // resolve to the declaring nested class so surefire can actually find the test.
      if (request.getTestNames().size() == 1 && !testClassName.contains("$")) {
        String resolvedClass = resolveDeclaringTestClassForTestMethod(testClassName, executableTestNames.getFirst());
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
        if (!normalizedServiceName.endsWith(SERVICE_SUFFIX)) {
          normalizedServiceName = normalizedServiceName + SERVICE_SUFFIX;
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
            ErrorMessages.QaErrorMessages.MUST_SPECIFY_SERVICE_NAME_OR_TEST_CLASS_NAME);
      }

      String methodName = request.getMethodName();

      // 2. Resolve associated unit tests
      List<TestMethodInfo> allTestMethods = readTestMethodsFromFile(testClassName);
      List<QaResponseModel.TestInfo> associatedTests = findAssociatedTests(methodName, allTestMethods,
          Collections.emptyMap());

      if (associatedTests.isEmpty()) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.QaErrorMessages.NO_TESTS_FOUND_FOR_METHOD_FORMAT,
                methodName,
                testClassName));
      }

      // 3. Construct Surefire Filter
      // Strip parameterized suffixes and get unique test names
      List<String> executableTestNames = associatedTests.stream()
          .map(QaResponseModel.TestInfo::getTestMethodName)
          .map(this::stripParameterizedSuffix)
          .distinct()
          .collect(Collectors.toCollection(ArrayList::new));

      // If all tests are in a nested class, resolve to that nested class
      // Check if we have a single nested class for all tests
      String declaringClass = associatedTests.stream()
          .map(QaResponseModel.TestInfo::getDeclaringTestClassName)
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
        String resolvedClass = associatedTests.getFirst().getDeclaringTestClassName();
        if (resolvedClass != null && resolvedClass.contains("$")) {
          testClassName = resolvedClass;
        }
      }

      testMethodFilter = String.join("+", executableTestNames);
    } else {
      throw new BadRequestException(
          ErrorMessages.QaErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD);
    }

    // Calculate expected test count for progress tracking
    int expectedTestCount = calculateExpectedTestCount(serviceName, request.getMethodName(), request.getTestNames());

    // Create initial status
    TestExecutionStatusModel status = new TestExecutionStatusModel(
        executionId, serviceName, request.getMethodName(), expectedTestCount);

    // Store status
    storeStatus(executionId, status);

    // Start async execution (returns immediately)
    executeTestsAsync(executionId, testClassName, testMethodFilter, serviceName);

    return status;
  }

  /**
   * Resolves the declaring test class selector for a specific test method name.
   * For @Nested tests,.
   * this returns "OuterTest$NestedClass".
   */
  private String resolveDeclaringTestClassForTestMethod(
      String outerTestClassName, String testMethodName) {
    if (outerTestClassName == null
        || outerTestClassName.trim().isEmpty()
        || testMethodName == null
        || testMethodName.trim().isEmpty()) {
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
   * Strips the Surefire parameterized suffix from a test case name. Example:
   * "foo(String)[2]" ->.
   * "foo"
   */
  private String stripParameterizedSuffix(String testName) {
    if (testName == null) {
      return null;
    }
    return testName.replaceAll("\\([^)]*\\)\\[\\d+]$", "");
  }

  /** Calculates the expected number of tests based on the execution scope. */
  private int calculateExpectedTestCount(
      String serviceName, String methodName, List<String> testNames) {
    // If running specific tests, we know the exact count
    if (testNames != null && !testNames.isEmpty()) {
      return testNames.size();
    }

    // Try to get from dashboard data (which scans test files)
    try {
      QaDashboardResponseModel dashboard = getDashboardData();

      if ("ALL".equals(serviceName)) {
        // Count all tests across all services
        return dashboard.getServices().stream()
            .flatMap(s -> s.getMethods().stream())
            .mapToInt(QaResponseModel.MethodInfo::getTestCount)
            .sum();
      }

      // Find the specific service
      QaResponseModel service = dashboard.getServices().stream()
          .filter(s -> s.getServiceName().equals(serviceName))
          .findFirst()
          .orElse(null);

      if (service != null) {
        if (methodName != null && !methodName.isEmpty()) {
          // Count tests for specific method
          return service.getMethods().stream()
              .filter(m -> m.getMethodName().equals(methodName))
              .mapToInt(QaResponseModel.MethodInfo::getTestCount)
              .sum();
        } else {
          // Count all tests for the service
          return service.getMethods().stream()
              .mapToInt(QaResponseModel.MethodInfo::getTestCount)
              .sum();
        }
      }
    } catch (Exception e) {
      // If we can't determine, return 0 (will be updated during execution)
      logger.error(e);
    }

    return 0;
  }

  /** Gets the current status and progress of a test execution. */
  @Override
  public TestExecutionStatusModel getTestExecutionStatus(String executionId) {
    if (executionId == null) {
      throw new NotFoundException(
          String.format(
              ErrorMessages.QaErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId));
    }
    TestExecutionStatusModel status = getStatus(executionId);
    if (status == null) {
      throw new NotFoundException(
          String.format(
              ErrorMessages.QaErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId));
    }
    return status;
  }

  // ==================== TEST EXECUTOR METHODS (moved from TestExecutorService)
  // ====================

  /** Stores or overwrites the status for an execution. */
  private void storeStatus(String executionId, TestExecutionStatusModel status) {
    activeExecutions.put(executionId, status);
  }

  /**
   * Gets the current status for an execution. For RUNNING status, returns a
   * snapshot with.
   * time-based progress estimation.
   */
  private TestExecutionStatusModel getStatus(String executionId) {
    TestExecutionStatusModel status = activeExecutions.get(executionId);
    if (status == null) {
      return null;
    }

    if (STATUS_RUNNING.equals(status.getStatus())
        && status.getStartedAt() != null
        && status.getTotalTests() > 0
        && status.getCompletedTests() < status.getTotalTests()) {
      long elapsedMs = java.time.Duration.between(status.getStartedAt(), LocalDateTime.now()).toMillis();
      int totalTests = status.getTotalTests();
      int maxWhileRunning = Math.max(0, totalTests - 1);
      int actualCompletedClamped = clampInt(status.getCompletedTests(), 0, maxWhileRunning);
      long expectedDurationMs = 2000L + totalTests * 400L;
      double ratio = expectedDurationMs > 0
          ? Math.min(0.95d, (double) elapsedMs / (double) expectedDurationMs)
          : 0.0d;
      int estimatedCompleted = (int) Math.floor(ratio * totalTests);
      int estimatedClamped = clampInt(estimatedCompleted, 0, maxWhileRunning);
      int smoothedCompleted = actualCompletedClamped >= estimatedClamped ? actualCompletedClamped : estimatedClamped;

      return TestExecutionStatusModel.createProgressSnapshot(
          status, totalTests, smoothedCompleted, elapsedMs);
    }

    return status;
  }

  /**
   * Starts asynchronous test execution via Maven. Returns immediately; tests run
   * in background.
   */
  @Async("asyncExecutor")
  protected void executeTestsAsync(
      String executionId, String testClassName, String testMethodFilter, String serviceName) {
    TestExecutionStatusModel status = activeExecutions.get(executionId);
    if (status == null) {
      return;
    }

    status.setStatus(STATUS_RUNNING);
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
            status.setStatus(STATUS_RUNNING);
          }
        }
      }

      final int exitCode = process.waitFor();
      runningProcesses.remove(executionId);

      parseSurefireReports(status, projectDir, testClassName);

      status.setDurationMs(System.currentTimeMillis() - startTime);
      status.setCompletedAt(LocalDateTime.now());
      status.setStatus(exitCode == 0 ? "COMPLETED" : "COMPLETED_WITH_FAILURES");

      if (exitCode != 0 && status.getResults().isEmpty()) {
        status.setErrorMessage(
            String.format(
                ErrorMessages.TestExecutorErrorMessages.TESTS_FAILED_EXIT_CODE_FORMAT, exitCode));
      }

    } catch (IOException e) {
      markExecutionFailed(
          executionId,
          status,
          startTime,
          String.format(
              ErrorMessages.TestExecutorErrorMessages.IO_ERROR_DURING_EXECUTION_FORMAT,
              e.getMessage()));
      throw new springapi.exceptions.ApplicationException(
          ErrorMessages.TestExecutorErrorMessages.IO_FAILED, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      markExecutionFailed(
          executionId,
          status,
          startTime,
          String.format(
              ErrorMessages.TestExecutorErrorMessages.INTERRUPTED_FORMAT, e.getMessage()));
      throw new springapi.exceptions.ApplicationException(
          ErrorMessages.TestExecutorErrorMessages.INTERRUPTED, e);
    } catch (Exception e) {
      markExecutionFailed(
          executionId,
          status,
          startTime,
          String.format(
              ErrorMessages.TestExecutorErrorMessages.EXECUTION_FAILED_FORMAT, e.getMessage()));
      throw new springapi.exceptions.ApplicationException(
          ErrorMessages.TestExecutorErrorMessages.EXECUTION_FAILED, e);
    }
  }

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

  private void markExecutionFailed(
      String executionId, TestExecutionStatusModel status, long startTime, String message) {
    status.setStatus("FAILED");
    status.setErrorMessage(message);
    status.setCompletedAt(LocalDateTime.now());
    status.setDurationMs(System.currentTimeMillis() - startTime);
    runningProcesses.remove(executionId);
  }

  /**
   * Locates the Spring API project directory (containing pom.xml and
   * src/test/java). Checks
   * current. dir, SpringApi subdir, and parent directories.
   */
  private Path findProjectDirectory() {
    Path currentDir = Paths.get(System.getProperty(USER_DIR_PROPERTY));

    if (Files.exists(currentDir.resolve(POM_XML))
        && Files.exists(currentDir.resolve("src/test/java"))) {
      return currentDir;
    }

    Path springApiDir = currentDir.resolve(SPRING_API_DIR);
    if (Files.exists(springApiDir.resolve(POM_XML))) {
      return springApiDir;
    }

    Path parent = currentDir.getParent();
    if (parent != null) {
      Path parentSpringApi = parent.resolve(SPRING_API_DIR);
      if (Files.exists(parentSpringApi.resolve(POM_XML))) {
        return parentSpringApi;
      }
    }

    return currentDir;
  }

  /**
   * Parses a single Maven output line for "Tests run: X, Failures: Y..." and
   * updates status.
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
      status.setStatus(STATUS_RUNNING);
    }
  }

  /**
   * Parses Surefire XML reports in target/surefire-reports for detailed test
   * results.
   */
  private void parseSurefireReports(
      TestExecutionStatusModel status, Path projectDir, String testClassName) {
    Path surefireDir = projectDir.resolve("target/surefire-reports");
    if (!Files.exists(surefireDir)) {
      return;
    }

    try (java.util.stream.Stream<Path> reportFiles = Files.list(surefireDir)) {
      reportFiles
          .filter(p -> p.toString().endsWith(".xml"))
          .filter(p -> testClassName == null || p.getFileName().toString().contains(testClassName))
          .forEach(xmlFile -> parseXmlReport(xmlFile, status));
    } catch (IOException e) {
      throw new springapi.exceptions.ApplicationException(
          ErrorMessages.TestExecutorErrorMessages.FAILED_TO_LIST_SUREFIRE_REPORTS, e);
    }
  }

  /** Parses a single Surefire XML report file and populates test results. */
  private void parseXmlReport(Path xmlFile, TestExecutionStatusModel status) {
    try {
      String content = Files.readString(xmlFile);

      Pattern testcasePattern = Pattern.compile(
          "<testcase\\s+name=\"([^\"]+)\"[^>]*classname=\"([^\"]+)\"[^>"
              + "]*time=\"([^\"]+)\"[^>]*(?:/>|>([\\s\\S]*?)</testcase>)",
          Pattern.MULTILINE);

      Matcher matcher = testcasePattern.matcher(content);
      while (matcher.find()) {
        String testName = matcher.group(1);
        double timeSeconds = Double.parseDouble(matcher.group(3));
        final String innerContent = matcher.group(4);

        TestExecutionStatusModel.TestResultInfo result = new TestExecutionStatusModel.TestResultInfo();
        result.setTestMethodName(testName);
        result.setDurationMs((long) (timeSeconds * 1000));

        if (testName.contains("_")) {
          result.setMethodName(testName.substring(0, testName.indexOf("_")));
        }

        if (innerContent != null
            && (innerContent.contains("<failure") || innerContent.contains("<error"))) {
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

        boolean exists = status.getResults().stream().anyMatch(r -> r.getTestMethodName().equals(testName));
        if (!exists) {
          status.getResults().add(result);
        }
      }

      status.updateTotalsFromResults();

    } catch (IOException e) {
      throw new springapi.exceptions.ApplicationException(
          String.format(
              ErrorMessages.TestExecutorErrorMessages.FAILED_TO_PARSE_SUREFIRE_REPORT_FORMAT,
              xmlFile.getFileName()),
          e);
    }
  }

  // ==================== PRIVATE HELPER METHODS ====================

  private QaDashboardResponseModel.AutomatedApiTestsData getAutomatedApiTests() {
    Path apiTestsDir = resolveAutomatedApiTestsPath();
    if (apiTestsDir == null || !Files.isDirectory(apiTestsDir)) {
      return new QaDashboardResponseModel.AutomatedApiTestsData(
          PLAYWRIGHT_RELATIVE_ROOT + AUTOMATED_API_TESTS_PATH, 0, new ArrayList<>());
    }

    List<QaDashboardResponseModel.AutomatedApiTestCategory> categories = new ArrayList<>();
    int totalTests = 0;

    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(apiTestsDir)) {
      List<Path> entries = new ArrayList<>();
      dirStream.forEach(entries::add);
      entries.sort(Comparator.comparing(p -> p.getFileName().toString()));

      for (Path entry : entries) {
        if (Files.isDirectory(entry)) {
          String categoryName = entry.getFileName().toString();
          if (!categoryName.startsWith(".")) {
            List<QaDashboardResponseModel.AutomatedApiTestInfo> tests = new ArrayList<>();
            try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(entry, "*.java")) {
              for (Path file : fileStream) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(JAVA_EXTENSION)) {
                  String testClass = fileName.substring(0, fileName.length() - 5);
                  String relativePath = Path.of(categoryName, fileName).toString();
                  tests.add(
                      new QaDashboardResponseModel.AutomatedApiTestInfo(testClass, relativePath));
                  totalTests++;
                }
              }
            }
            tests.sort(
                Comparator.comparing(QaDashboardResponseModel.AutomatedApiTestInfo::getTestClass));
            categories.add(
                new QaDashboardResponseModel.AutomatedApiTestCategory(
                    categoryName, categoryName, tests));
          }
        }
      }
    } catch (IOException e) {
      logger.error(e);
      return new QaDashboardResponseModel.AutomatedApiTestsData(
          PLAYWRIGHT_RELATIVE_ROOT + AUTOMATED_API_TESTS_PATH, 0, new ArrayList<>());
    }

    return new QaDashboardResponseModel.AutomatedApiTestsData(
        PLAYWRIGHT_RELATIVE_ROOT + AUTOMATED_API_TESTS_PATH, totalTests, categories);
  }

  private Path resolveAutomatedApiTestsPath() {
    Path currentDir = Paths.get(System.getProperty(USER_DIR_PROPERTY));
    Path playwrightRoot = Paths.get(PLAYWRIGHT_PROJECT_DIR);
    List<Path> possiblePaths = new ArrayList<>(
        Arrays.asList(
            currentDir.resolve("..").resolve(playwrightRoot).resolve(AUTOMATED_API_TESTS_PATH),
            currentDir
                .resolve("..")
                .resolve("..")
                .resolve(playwrightRoot)
                .resolve(AUTOMATED_API_TESTS_PATH),
            currentDir.resolve(playwrightRoot).resolve(AUTOMATED_API_TESTS_PATH)));

    Path parent = currentDir.getParent();
    if (parent != null) {
      possiblePaths.add(parent.resolve(PLAYWRIGHT_PROJECT_DIR).resolve(AUTOMATED_API_TESTS_PATH));
      if (parent.getParent() != null) {
        possiblePaths.add(
            parent.getParent().resolve(PLAYWRIGHT_PROJECT_DIR).resolve(AUTOMATED_API_TESTS_PATH));
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

  private static int clampInt(int value, int min, int max) {
    if (value < min) {
      return min;
    }
    if (value > max) {
      return max;
    }
    return value;
  }
}
