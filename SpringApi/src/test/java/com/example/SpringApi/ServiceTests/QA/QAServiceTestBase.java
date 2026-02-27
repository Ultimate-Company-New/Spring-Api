package com.example.springapi.ServiceTests.QA;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

import com.example.springapi.ErrorMessages;
import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.models.databasemodels.LatestTestResult;
import com.example.springapi.models.databasemodels.TestRun;
import com.example.springapi.models.databasemodels.TestRunResult;
import com.example.springapi.models.requestmodels.TestExecutionRequestModel;
import com.example.springapi.models.requestmodels.TestRunRequestModel;
import com.example.springapi.repositories.LatestTestResultRepository;
import com.example.springapi.repositories.TestRunRepository;
import com.example.springapi.services.QaService;
import com.example.springapi.services.interfaces.QaSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Base test class for QA Service tests providing common helper methods and stub methods.
 *
 * <p>This class provides QA-specific test data creation and repository stubs. All QA service test
 * classes should extend this class to ensure consistent test data and follow the no-inline-mocks
 * rule.
 */
abstract class QAServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_CLIENT_ID = 100L;
  protected static final String DEFAULT_CREATED_USER = "admin";

  @Mock protected TestRunRepository testRunRepository;

  @Mock protected LatestTestResultRepository latestTestResultRepository;

  @Mock protected QaSubTranslator qaSubTranslator;

  @Mock protected JwtTokenProvider jwtTokenProvider;

  @Mock protected HttpServletRequest request;

  protected QaService qaService;

  @BeforeEach
  public void setUp() {
    // Create a subclass that overrides the async execution method to prevent
    // actual Maven process execution during unit tests.
    QaService realService =
        new QaService(testRunRepository, latestTestResultRepository, jwtTokenProvider, request) {
          @Override
          protected void executeTestsAsync(
              String executionId,
              String testClassName,
              String testMethodFilter,
              String serviceName) {
            // Do nothing during unit tests
          }
        };
    qaService = Mockito.spy(realService);
    clearExecutionTrackingState();
  }

  /** Stub testRunRepository.save to throw the provided exception. */
  protected void stubTestRunRepositorySaveThrows(RuntimeException exception) {
    doThrow(exception).when(testRunRepository).save(any(TestRun.class));
  }

  /** Stub QA service getAvailableServices to throw the provided exception. */
  protected void stubQaServiceGetAvailableServicesThrows(RuntimeException exception) {
    doThrow(exception).when(qaService).getAvailableServices();
  }

  /** Stub QA service getAllEndpointsWithTests to throw the provided exception. */
  protected void stubQaServiceGetAllEndpointsWithTestsThrows(RuntimeException exception) {
    doThrow(exception).when(qaService).getAllEndpointsWithTests();
  }

  /** Stub QA service getDashboardData to throw the provided exception. */
  protected void stubQaServiceGetDashboardDataThrows(RuntimeException exception) {
    doThrow(exception).when(qaService).getDashboardData();
  }

  /** Stub latestTestResultRepository findByClientId to throw the provided exception. */
  protected void stubLatestTestResultRepositoryFindByClientIdThrows(RuntimeException exception) {
    doThrow(exception)
        .when(latestTestResultRepository)
        .findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
  }

  /** Access the QaService SERVICE_MAPPINGS map via reflection for test manipulation. */
  protected Map<String, Object> getServiceMappings() {
    try {
      Field field = QaService.class.getDeclaredField("SERVICE_MAPPINGS");
      field.setAccessible(true);
      return (Map<String, Object>) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Creates a ServiceControllerMapping instance via reflection. */
  protected Object createServiceControllerMapping(
      String controllerName, String basePath, String testClassName) {
    return createQaPrivateInnerInstance(
        "ServiceControllerMapping",
        new Class<?>[] {String.class, String.class, String.class},
        controllerName,
        basePath,
        testClassName);
  }

  /** Creates an instance of a private QaService inner class via reflection. */
  protected Object createQaPrivateInnerInstance(
      String innerSimpleName, Class<?>[] parameterTypes, Object... args) {
    try {
      Class<?> innerClass = Class.forName(QaService.class.getName() + "$" + innerSimpleName);
      java.lang.reflect.Constructor<?> ctor = innerClass.getDeclaredConstructor(parameterTypes);
      ctor.setAccessible(true);
      return ctor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Creates a real QaService instance (without overriding executeTestsAsync). */
  protected QaService createRealQAService() {
    return new QaService(testRunRepository, latestTestResultRepository, jwtTokenProvider, request);
  }

  /** Invokes a private/protected method using reflection. */
  protected Object invokePrivateMethod(
      Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
    try {
      Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
      method.setAccessible(true);
      return method.invoke(target, args);
    } catch (NoSuchMethodException e) {
      // Try superclass (needed for spies/subclasses)
      try {
        Method method =
            target.getClass().getSuperclass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Invokes a private static method using reflection. */
  protected Object invokePrivateStaticMethod(
      Class<?> targetClass, String methodName, Class<?>[] parameterTypes, Object... args) {
    try {
      Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Gets a private static field value by name. */
  protected Object getPrivateStaticFieldValue(Class<?> targetClass, String fieldName) {
    try {
      Field field = targetClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Clears QaService execution tracking static maps between tests. */
  protected void clearExecutionTrackingState() {
    Object activeExecutions = getPrivateStaticFieldValue(QaService.class, "activeExecutions");
    if (activeExecutions instanceof Map<?, ?> map) {
      map.clear();
    }

    Object runningProcesses = getPrivateStaticFieldValue(QaService.class, "runningProcesses");
    if (runningProcesses instanceof Map<?, ?> map) {
      map.clear();
    }
  }

  // ==================== TEST DATA FACTORY METHODS ====================

  /** Creates a valid TestRunRequestModel with default values. */
  protected TestRunRequestModel createValidTestRunRequest() {
    return createValidTestRunRequest("TestService", 1);
  }

  /** Creates a valid TestRunRequestModel with specified service name and result count. */
  protected TestRunRequestModel createValidTestRunRequest(String serviceName, int resultCount) {
    TestRunRequestModel testRunRequest = new TestRunRequestModel();
    testRunRequest.setServiceName(serviceName);
    testRunRequest.setEnvironment("test");
    testRunRequest.setRunType("manual");

    List<TestRunRequestModel.TestResultData> results = new ArrayList<>();
    for (int i = 0; i < resultCount; i++) {
      TestRunRequestModel.TestResultData result = new TestRunRequestModel.TestResultData();
      result.setTestMethodName("testMethod" + i);
      result.setMethodName("methodName" + i);
      result.setStatus("PASSED");
      result.setDurationMs(100);
      result.setDisplayName("Test Method " + i);
      results.add(result);
    }
    testRunRequest.setResults(results);

    return testRunRequest;
  }

  /** Creates a valid TestExecutionRequestModel for running all tests. */
  protected TestExecutionRequestModel createValidTestExecutionRequest() {
    TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
    executionRequest.setRunAll(true);
    return executionRequest;
  }

  /** Creates a valid TestExecutionRequestModel for running specific tests. */
  protected TestExecutionRequestModel createTestExecutionRequestWithTestNames(
      String testClassName, List<String> testNames) {
    TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
    executionRequest.setRunAll(false);
    executionRequest.setTestClassName(testClassName);
    executionRequest.setTestNames(testNames);
    return executionRequest;
  }

  /** Creates a valid TestExecutionRequestModel for running tests by method name. */
  protected TestExecutionRequestModel createTestExecutionRequestWithMethodName(
      String serviceName, String methodName) {
    TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
    executionRequest.setRunAll(false);
    executionRequest.setServiceName(serviceName);
    executionRequest.setMethodName(methodName);
    return executionRequest;
  }

  /** Creates a test TestRun entity with default values. */
  protected TestRun createTestRun() {
    return createTestRun(1L, "TestService");
  }

  /** Creates a test TestRun entity with specified ID and service name. */
  protected TestRun createTestRun(Long testRunId, String serviceName) {
    TestRun testRun = new TestRun();
    testRun.setTestRunId(testRunId);
    testRun.setClientId(DEFAULT_CLIENT_ID);
    testRun.setServiceName(serviceName);
    testRun.setEnvironment("test");
    testRun.setRunType("manual");
    testRun.setStartTime(LocalDateTime.now());
    testRun.setEndTime(LocalDateTime.now());
    testRun.setTotalTests(10);
    testRun.setPassedCount(8);
    testRun.setFailedCount(2);
    testRun.setSkippedCount(0);
    testRun.setCreatedDate(LocalDateTime.now());
    return testRun;
  }

  /** Creates a test LatestTestResult entity with default values. */
  protected LatestTestResult createLatestTestResult() {
    return createLatestTestResult(1L, "TestService", "testMethod");
  }

  /** Creates a test LatestTestResult entity with specified values. */
  protected LatestTestResult createLatestTestResult(
      Long id, String serviceName, String testMethodName) {
    LatestTestResult result = new LatestTestResult();
    result.setLatestTestResultId(id);
    result.setClientId(DEFAULT_CLIENT_ID);
    result.setServiceName(serviceName);
    result.setTestClassName("TestClass");
    result.setTestMethodName(testMethodName);
    result.setStatus("PASSED");
    result.setDurationMs(100);
    result.setLastRunId(1L);
    result.setLastRunByUserId(1L);
    result.setLastRunByUserName(DEFAULT_CREATED_USER);
    result.setLastRunAt(LocalDateTime.now());
    return result;
  }

  /** Creates a test TestRunResult entity with default values. */
  protected TestRunResult createTestRunResult() {
    return createTestRunResult(1L, 1L, "testMethod");
  }

  /** Creates a test TestRunResult entity with specified values. */
  protected TestRunResult createTestRunResult(Long id, Long testRunId, String testMethodName) {
    TestRunResult result = new TestRunResult();
    result.setTestRunResultId(id);
    result.setTestRunId(testRunId);
    result.setServiceName("TestService");
    result.setTestMethodName(testMethodName);
    result.setMethodName("methodName");
    result.setTestClassName("TestClass");
    result.setStatus("PASSED");
    result.setDurationMs(100);
    result.setDisplayName("Test Method");
    result.setExecutedAt(LocalDateTime.now());
    result.setClientId(DEFAULT_CLIENT_ID);
    return result;
  }

  // ==================== STUB METHODS ====================

  /** Stubs TestRunRepository.save() to return the provided test run. */
  protected void stubTestRunRepositorySave(TestRun testRun) {
    doReturn(testRun).when(testRunRepository).save(any(TestRun.class));
  }

  /** Stubs TestRunRepository.findById() to return the provided test run. */
  protected void stubTestRunRepositoryFindById(Long testRunId, TestRun testRun) {
    doReturn(Optional.of(testRun)).when(testRunRepository).findById(testRunId);
  }

  /** Stubs TestRunRepository.findById() to return empty. */
  protected void stubTestRunRepositoryFindByIdNotFound(Long testRunId) {
    doReturn(Optional.empty()).when(testRunRepository).findById(testRunId);
  }

  /** Stubs LatestTestResultRepository.findByClientId() to return the provided results. */
  protected void stubLatestTestResultRepositoryFindByClientId(List<LatestTestResult> results) {
    lenient()
        .doReturn(results)
        .when(latestTestResultRepository)
        .findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
  }

  /**
   * Stubs LatestTestResultRepository.findByClientIdAndServiceName() to return the provided results.
   */
  protected void stubLatestTestResultRepositoryFindByClientIdAndServiceName(
      String serviceName, List<LatestTestResult> results) {
    lenient()
        .doReturn(results)
        .when(latestTestResultRepository)
        .findByClientIdAndServiceNameOrderByTestMethodNameAsc(anyLong(), eq(serviceName));
  }

  /** Stubs LatestTestResultRepository.save() to return the provided result. */
  protected void stubLatestTestResultRepositorySave(LatestTestResult result) {
    lenient().doReturn(result).when(latestTestResultRepository).save(any(LatestTestResult.class));
  }

  /** Stubs LatestTestResultRepository.saveAll() to return the provided results. */
  protected void stubLatestTestResultRepositorySaveAll(List<LatestTestResult> results) {
    lenient().doReturn(results).when(latestTestResultRepository).saveAll(anyList());
  }

  /**
   * Stubs LatestTestResultRepository.findByClientIdAndServiceNameAndTestMethodName() to return the
   * provided result.
   */
  protected void stubLatestTestResultRepositoryFindByClientIdAndServiceNameAndTestMethodName(
      String serviceName, String testClassName, String testMethodName, LatestTestResult result) {
    lenient()
        .doReturn(Optional.ofNullable(result))
        .when(latestTestResultRepository)
        .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
            anyLong(), eq(serviceName), eq(testClassName), eq(testMethodName));
  }

  /**
   * Stubs LatestTestResultRepository.findByClientIdAndServiceNameAndTestMethodName() to return
   * empty.
   */
  protected void
      stubLatestTestResultRepositoryFindByClientIdAndServiceNameAndTestMethodNameNotFound(
          String serviceName, String testClassName, String testMethodName) {
    lenient()
        .doReturn(Optional.empty())
        .when(latestTestResultRepository)
        .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
            anyLong(), eq(serviceName), eq(testClassName), eq(testMethodName));
  }

  /** Stub QA translator getDashboardData to throw UnauthorizedException. */
  protected void stubQaTranslatorGetDashboardDataThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getDashboardData();
  }

  /** Stub QA translator getAllEndpointsWithTests to throw UnauthorizedException. */
  protected void stubQaTranslatorGetAllEndpointsWithTestsThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getAllEndpointsWithTests();
  }

  /** Stub QA translator getEndpointsWithTestsByService to throw UnauthorizedException. */
  protected void stubQaTranslatorGetEndpointsWithTestsByServiceThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getEndpointsWithTestsByService(anyString());
  }

  /** Stub QA translator getCoverageSummary to throw UnauthorizedException. */
  protected void stubQaTranslatorGetCoverageSummaryThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getCoverageSummary();
  }

  /** Stub QA translator getAvailableServices to throw UnauthorizedException. */
  protected void stubQaTranslatorGetAvailableServicesThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getAvailableServices();
  }

  /** Stub QA translator saveTestRun to throw UnauthorizedException. */
  protected void stubQaTranslatorSaveTestRunThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .saveTestRun(any(TestRunRequestModel.class));
  }

  /** Stub QA translator getLatestTestResults to throw UnauthorizedException. */
  protected void stubQaTranslatorGetLatestTestResultsThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getLatestTestResults(any());
  }

  /** Stub QA translator startTestExecution to throw UnauthorizedException. */
  protected void stubQaTranslatorStartTestExecutionThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .startTestExecution(any(TestExecutionRequestModel.class));
  }

  /** Stub QA translator getTestExecutionStatus to throw UnauthorizedException. */
  protected void stubQaTranslatorGetTestExecutionStatusThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(qaSubTranslator)
        .getTestExecutionStatus(anyString());
  }
}
