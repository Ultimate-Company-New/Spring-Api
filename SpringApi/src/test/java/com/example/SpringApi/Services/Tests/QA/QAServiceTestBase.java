package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.DatabaseModels.TestRun;
import com.example.SpringApi.Models.DatabaseModels.TestRunResult;
import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Repositories.LatestTestResultRepository;
import com.example.SpringApi.Repositories.TestRunRepository;
import com.example.SpringApi.Services.Interface.IQASubTranslator;
import com.example.SpringApi.Services.QAService;
import jakarta.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Field;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;

/**
 * Base test class for QA Service tests providing common helper methods and stub
 * methods.
 * 
 * This class provides QA-specific test data creation and repository stubs.
 * All QA service test classes should extend this class to ensure consistent
 * test data and
 * follow the no-inline-mocks rule.
 */
public abstract class QAServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_CLIENT_ID = 100L;
    protected static final String DEFAULT_CREATED_USER = "admin";

    @Mock
    protected TestRunRepository testRunRepository;

    @Mock
    protected LatestTestResultRepository latestTestResultRepository;

    @Mock
    protected IQASubTranslator qaSubTranslator;

    @Mock
    protected JwtTokenProvider jwtTokenProvider;

    @Mock
    protected HttpServletRequest request;

    protected QAService qaService;

    @BeforeEach
    public void setUp() {
        // Create a subclass that overrides the async execution method to prevent
        // actual Maven process execution during unit tests.
        QAService realService = new QAService(testRunRepository, latestTestResultRepository, jwtTokenProvider, request) {
            @Override
            protected void executeTestsAsync(String executionId, String testClassName, String testMethodFilter,
                    String serviceName) {
                // Do nothing during unit tests
            }
        };
        qaService = Mockito.spy(realService);
    }

    /**
     * Stub testRunRepository.save to throw the provided exception.
     */
    protected void stubTestRunRepositorySaveThrows(RuntimeException exception) {
        doThrow(exception).when(testRunRepository).save(any(TestRun.class));
    }

    /**
     * Stub QA service getAvailableServices to throw the provided exception.
     */
    protected void stubQaServiceGetAvailableServicesThrows(RuntimeException exception) {
        doThrow(exception).when(qaService).getAvailableServices();
    }

    /**
     * Stub QA service getAllEndpointsWithTests to throw the provided exception.
     */
    protected void stubQaServiceGetAllEndpointsWithTestsThrows(RuntimeException exception) {
        doThrow(exception).when(qaService).getAllEndpointsWithTests();
    }

    /**
     * Stub latestTestResultRepository findByClientId to throw the provided exception.
     */
    protected void stubLatestTestResultRepositoryFindByClientIdThrows(RuntimeException exception) {
        doThrow(exception).when(latestTestResultRepository)
                .findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
    }

    /**
     * Access the QAService SERVICE_MAPPINGS map via reflection for test manipulation.
     */
    protected Map<String, Object> getServiceMappings() {
        try {
            Field field = QAService.class.getDeclaredField("SERVICE_MAPPINGS");
            field.setAccessible(true);
            return (Map<String, Object>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== TEST DATA FACTORY METHODS ====================

    /**
     * Creates a valid TestRunRequestModel with default values.
     */
    protected TestRunRequestModel createValidTestRunRequest() {
        return createValidTestRunRequest("TestService", 1);
    }

    /**
     * Creates a valid TestRunRequestModel with specified service name and result
     * count.
     */
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

    /**
     * Creates a valid TestExecutionRequestModel for running all tests.
     */
    protected TestExecutionRequestModel createValidTestExecutionRequest() {
        TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
        executionRequest.setRunAll(true);
        return executionRequest;
    }

    /**
     * Creates a valid TestExecutionRequestModel for running specific tests.
     */
    protected TestExecutionRequestModel createTestExecutionRequestWithTestNames(String testClassName,
            List<String> testNames) {
        TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
        executionRequest.setRunAll(false);
        executionRequest.setTestClassName(testClassName);
        executionRequest.setTestNames(testNames);
        return executionRequest;
    }

    /**
     * Creates a valid TestExecutionRequestModel for running tests by method name.
     */
    protected TestExecutionRequestModel createTestExecutionRequestWithMethodName(String serviceName,
            String methodName) {
        TestExecutionRequestModel executionRequest = new TestExecutionRequestModel();
        executionRequest.setRunAll(false);
        executionRequest.setServiceName(serviceName);
        executionRequest.setMethodName(methodName);
        return executionRequest;
    }

    /**
     * Creates a test TestRun entity with default values.
     */
    protected TestRun createTestRun() {
        return createTestRun(1L, "TestService");
    }

    /**
     * Creates a test TestRun entity with specified ID and service name.
     */
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

    /**
     * Creates a test LatestTestResult entity with default values.
     */
    protected LatestTestResult createLatestTestResult() {
        return createLatestTestResult(1L, "TestService", "testMethod");
    }

    /**
     * Creates a test LatestTestResult entity with specified values.
     */
    protected LatestTestResult createLatestTestResult(Long id, String serviceName, String testMethodName) {
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

    /**
     * Creates a test TestRunResult entity with default values.
     */
    protected TestRunResult createTestRunResult() {
        return createTestRunResult(1L, 1L, "testMethod");
    }

    /**
     * Creates a test TestRunResult entity with specified values.
     */
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

    /**
     * Stubs TestRunRepository.save() to return the provided test run.
     */
    protected void stubTestRunRepositorySave(TestRun testRun) {
        doReturn(testRun).when(testRunRepository).save(any(TestRun.class));
    }

    /**
     * Stubs TestRunRepository.findById() to return the provided test run.
     */
    protected void stubTestRunRepositoryFindById(Long testRunId, TestRun testRun) {
        doReturn(Optional.of(testRun)).when(testRunRepository).findById(testRunId);
    }

    /**
     * Stubs TestRunRepository.findById() to return empty.
     */
    protected void stubTestRunRepositoryFindByIdNotFound(Long testRunId) {
        doReturn(Optional.empty()).when(testRunRepository).findById(testRunId);
    }

    /**
     * Stubs LatestTestResultRepository.findByClientId() to return the provided
     * results.
     */
    protected void stubLatestTestResultRepositoryFindByClientId(List<LatestTestResult> results) {
        lenient().doReturn(results).when(latestTestResultRepository)
                .findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
    }

    /**
     * Stubs LatestTestResultRepository.findByClientIdAndServiceName() to return the
     * provided results.
     */
    protected void stubLatestTestResultRepositoryFindByClientIdAndServiceName(String serviceName,
            List<LatestTestResult> results) {
        lenient().doReturn(results).when(latestTestResultRepository)
                .findByClientIdAndServiceNameOrderByTestMethodNameAsc(anyLong(), eq(serviceName));
    }

    /**
     * Stubs LatestTestResultRepository.save() to return the provided result.
     */
    protected void stubLatestTestResultRepositorySave(LatestTestResult result) {
        lenient().doReturn(result).when(latestTestResultRepository).save(any(LatestTestResult.class));
    }

    /**
     * Stubs LatestTestResultRepository.saveAll() to return the provided results.
     */
    protected void stubLatestTestResultRepositorySaveAll(List<LatestTestResult> results) {
        lenient().doReturn(results).when(latestTestResultRepository).saveAll(anyList());
    }

    /**
     * Stubs
     * LatestTestResultRepository.findByClientIdAndServiceNameAndTestMethodName() to
     * return the provided result.
     */
    protected void stubLatestTestResultRepositoryFindByClientIdAndServiceNameAndTestMethodName(
            String serviceName, String testClassName, String testMethodName, LatestTestResult result) {
            lenient().doReturn(Optional.ofNullable(result)).when(latestTestResultRepository)
                .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
                        anyLong(), eq(serviceName), eq(testClassName), eq(testMethodName));
    }

    /**
     * Stubs
     * LatestTestResultRepository.findByClientIdAndServiceNameAndTestMethodName() to
     * return empty.
     */
    protected void stubLatestTestResultRepositoryFindByClientIdAndServiceNameAndTestMethodNameNotFound(
            String serviceName, String testClassName, String testMethodName) {
            lenient().doReturn(Optional.empty()).when(latestTestResultRepository)
                .findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
                        anyLong(), eq(serviceName), eq(testClassName), eq(testMethodName));
    }

    /**
     * Stub QA translator getDashboardData to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetDashboardDataThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getDashboardData();
    }

    /**
     * Stub QA translator getAllEndpointsWithTests to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetAllEndpointsWithTestsThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getAllEndpointsWithTests();
    }

    /**
     * Stub QA translator getEndpointsWithTestsByService to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetEndpointsWithTestsByServiceThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getEndpointsWithTestsByService(anyString());
    }

    /**
     * Stub QA translator getCoverageSummary to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetCoverageSummaryThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getCoverageSummary();
    }

    /**
     * Stub QA translator getAvailableServices to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetAvailableServicesThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getAvailableServices();
    }

    /**
     * Stub QA translator saveTestRun to throw UnauthorizedException.
     */
    protected void stubQaTranslatorSaveTestRunThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).saveTestRun(any(TestRunRequestModel.class));
    }

    /**
     * Stub QA translator getLatestTestResults to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetLatestTestResultsThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getLatestTestResults(any());
    }

    /**
     * Stub QA translator startTestExecution to throw UnauthorizedException.
     */
    protected void stubQaTranslatorStartTestExecutionThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).startTestExecution(any(TestExecutionRequestModel.class));
    }

    /**
     * Stub QA translator getTestExecutionStatus to throw UnauthorizedException.
     */
    protected void stubQaTranslatorGetTestExecutionStatusThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(qaSubTranslator).getTestExecutionStatus(anyString());
    }
}
