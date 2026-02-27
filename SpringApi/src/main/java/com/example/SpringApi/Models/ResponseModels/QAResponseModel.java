package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Comprehensive response model for QA endpoint-to-test mapping.
 *
 * <p>This model provides complete information about a service including all its public endpoints
 * and the associated unit tests for each method. This enables QA teams to understand test coverage
 * at a service level.
 *
 * <p>Test naming convention: methodName_Result_Outcome Example:
 * toggleAddress_AddressNotFound_ThrowsNotFoundException
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class QAResponseModel {

  /** The name of the service (e.g., "AddressService", "UserService") */
  private String serviceName;

  /** The controller that exposes this service's endpoints (e.g., "AddressController") */
  private String controllerName;

  /** The base API path for this service (e.g., "/api/Address") */
  private String basePath;

  /** The name of the unit test class for this service (e.g., "AddressServiceTest") */
  private String testClassName;

  /** List of all public methods in this service with their test mappings */
  private List<MethodInfo> methods;

  /** Total number of public methods in this service */
  private int totalMethods;

  /** Number of methods that have at least one unit test */
  private int methodsWithCoverage;

  /** Total number of unit tests across all methods */
  private int totalTests;

  /** Coverage percentage (methodsWithCoverage / totalMethods * 100) */
  private double coveragePercentage;

  /** Default constructor. */
  public QAResponseModel() {
    this.methods = new ArrayList<>();
    this.totalMethods = 0;
    this.methodsWithCoverage = 0;
    this.totalTests = 0;
    this.coveragePercentage = 0.0;
  }

  /**
   * Constructor with service details.
   *
   * @param serviceName The name of the service
   * @param controllerName The name of the controller
   * @param basePath The base API path
   * @param testClassName The name of the test class
   */
  public QAResponseModel(
      String serviceName, String controllerName, String basePath, String testClassName) {
    this.serviceName = serviceName;
    this.controllerName = controllerName;
    this.basePath = basePath;
    this.testClassName = testClassName;
    this.methods = new ArrayList<>();
    this.totalMethods = 0;
    this.methodsWithCoverage = 0;
    this.totalTests = 0;
    this.coveragePercentage = 0.0;
  }

  /**
   * Adds a method to this service and updates statistics.
   *
   * @param method The method info to add
   */
  public void addMethod(MethodInfo method) {
    if (method != null) {
      this.methods.add(method);
      this.totalMethods = this.methods.size();

      if (method.isHasCoverage()) {
        this.methodsWithCoverage++;
      }

      this.totalTests += method.getTestCount();

      // Recalculate coverage percentage
      if (this.totalMethods > 0) {
        this.coveragePercentage = ((double) this.methodsWithCoverage / this.totalMethods) * 100.0;
      }
    }
  }

  // ==================== INNER CLASSES ====================

  /** Represents a service method and its associated unit tests. */
  @Getter
  @Setter
  public static class MethodInfo {

    /** The name of the service method (e.g., "toggleAddress", "getAddressById") */
    private String methodName;

    /** The HTTP method used by this endpoint (GET, POST, PUT, DELETE) */
    private String httpMethod;

    /** The API route/path for this endpoint (e.g., "/api/Address/toggleAddress/{id}") */
    private String apiRoute;

    /** Brief description of what this method does */
    private String description;

    /**
     * List of unit tests associated with this service method. Test names follow the pattern:
     * methodName_Result_Outcome
     */
    private List<TestInfo> associatedUnitTests;

    /** The number of unit tests associated with this method */
    private int testCount;

    /** Whether this method has at least one unit test */
    private boolean hasCoverage;

    /** Default constructor. */
    public MethodInfo() {
      this.associatedUnitTests = new ArrayList<>();
      this.testCount = 0;
      this.hasCoverage = false;
    }

    /** Constructor with method details. */
    public MethodInfo(String methodName, String httpMethod, String apiRoute, String description) {
      this.methodName = methodName;
      this.httpMethod = httpMethod;
      this.apiRoute = apiRoute;
      this.description = description;
      this.associatedUnitTests = new ArrayList<>();
      this.testCount = 0;
      this.hasCoverage = false;
    }

    /** Constructor with method name, api route, and description (httpMethod omitted). */
    public MethodInfo(String methodName, String apiRoute, String description) {
      this(methodName, null, apiRoute, description);
    }

    /** Adds a unit test to the list of associated tests. */
    public void addUnitTest(TestInfo testInfo) {
      if (testInfo != null
          && testInfo.getTestMethodName() != null
          && !testInfo.getTestMethodName().trim().isEmpty()) {
        this.associatedUnitTests.add(testInfo);
        this.testCount = this.associatedUnitTests.size();
        this.hasCoverage = true;
      }
    }

    /** Adds a unit test with method name and display name. */
    public void addUnitTest(String testMethodName, String displayName) {
      addUnitTest(new TestInfo(testMethodName, displayName));
    }

    /** Adds multiple unit tests to the list. */
    public void addUnitTests(List<TestInfo> testInfos) {
      if (testInfos != null) {
        for (TestInfo testInfo : testInfos) {
          addUnitTest(testInfo);
        }
      }
    }
  }

  /** Represents a unit test method with its display name and last run information. */
  @Getter
  @Setter
  public static class TestInfo {

    /**
     * The name of the test method (e.g., "toggleAddress_AddressNotFound_ThrowsNotFoundException")
     * Format: serviceMethodName_Result_Outcome
     */
    private String testMethodName;

    /**
     * The declaring test class name that contains this test method.
     *
     * <p>This is used for Maven/Surefire test selection. For @Nested tests, this will include the
     * nested class using '$' (e.g., "AddressServiceTest$GetAddressByIdTests"). For non-nested
     * tests, this will typically be the outer test class name (e.g., "AddressServiceTest").
     */
    private String declaringTestClassName;

    /**
     * The human-readable display name from @DisplayName annotation (e.g., "Toggle Address - Address
     * not found - ThrowsNotFoundException")
     */
    private String displayName;

    // ==================== LAST RUN INFORMATION ====================

    /** Whether this test has been run before */
    private boolean hasBeenRun;

    /** The status of the last run (PASSED, FAILED, SKIPPED, ERROR) */
    private String lastRunStatus;

    /** When the test was last run */
    private LocalDateTime lastRunAt;

    /** Who ran the test last */
    private String lastRunByUserName;

    /** User ID of who ran the test last */
    private Long lastRunByUserId;

    /** Duration of the last run in milliseconds */
    private Integer lastRunDurationMs;

    /** Error message from the last run (if failed) */
    private String lastRunErrorMessage;

    /** Stack trace from the last run (if failed) */
    private String lastRunStackTrace;

    /** Default constructor. */
    public TestInfo() {
      this.hasBeenRun = false;
    }

    /** Constructor with test method name and display name. */
    public TestInfo(String testMethodName, String displayName) {
      this.testMethodName = testMethodName;
      this.displayName = displayName;
      this.hasBeenRun = false;
    }

    /** Constructor with just test method name. */
    public TestInfo(String testMethodName) {
      this.testMethodName = testMethodName;
      this.displayName = null;
      this.hasBeenRun = false;
    }

    /** Constructor with test method name, display name, and declaring test class. */
    public TestInfo(String testMethodName, String displayName, String declaringTestClassName) {
      this.testMethodName = testMethodName;
      this.displayName = displayName;
      this.declaringTestClassName = declaringTestClassName;
      this.hasBeenRun = false;
    }

    /** Populates last run information from a LatestTestResult entity. */
    public void populateFromLatestResult(LatestTestResult latestResult) {
      if (latestResult != null) {
        this.hasBeenRun = true;
        this.lastRunStatus = latestResult.getStatus();
        this.lastRunAt = latestResult.getLastRunAt();
        this.lastRunByUserName = latestResult.getLastRunByUserName();
        this.lastRunByUserId = latestResult.getLastRunByUserId();
        this.lastRunDurationMs = latestResult.getDurationMs();
        this.lastRunErrorMessage = latestResult.getErrorMessage();
        this.lastRunStackTrace = latestResult.getStackTrace();
      }
    }
  }
}

