package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.DatabaseModels.TestRun;
import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Models.ResponseModels.LatestTestResultResponseModel;
import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
import com.example.SpringApi.Models.ResponseModels.QAResponseModel;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Models.ResponseModels.TestRunResponseModel;
import com.example.SpringApi.Repositories.LatestTestResultRepository;
import com.example.SpringApi.Repositories.TestRunRepository;
import com.example.SpringApi.Services.QAService;
import com.example.SpringApi.Services.Interface.ITestExecutorService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QAService.
 * Tests all QA-related operations including dashboard data, endpoint mapping,
 * test run tracking, and test execution.
 */
@DisplayName("QAService Tests")
class QAServiceTest extends BaseTest {

    @Mock
    private TestRunRepository testRunRepository;

    @Mock
    private LatestTestResultRepository latestTestResultRepository;

    @Mock
    private ITestExecutorService testExecutorService;

    @InjectMocks
    private QAService qaService;

    private TestRunRequestModel validTestRunRequest;
    private TestRun mockTestRun;

    @BeforeEach
    void setUp() {
        super.setUp();
        
        // Setup valid test run request
        validTestRunRequest = new TestRunRequestModel();
        validTestRunRequest.setServiceName("AddressService");
        validTestRunRequest.setRunType("SERVICE");
        validTestRunRequest.setEnvironment("localhost");
        
        List<TestRunRequestModel.TestResultData> results = new ArrayList<>();
        TestRunRequestModel.TestResultData result = new TestRunRequestModel.TestResultData();
        result.setTestMethodName("toggleAddress_AddressFound_Success");
        result.setMethodName("toggleAddress");
        result.setPassed(true);
        result.setDurationMs(150L);
        results.add(result);
        
        validTestRunRequest.setResults(results);
        
        // Setup mock test run
        mockTestRun = new TestRun("AddressService", "SERVICE", 1L, "testuser", 1L);
        mockTestRun.setTestRunId(1L);
    }

    // ==================== GET DASHBOARD DATA TESTS ====================

    @Test
    @DisplayName("getDashboardData - Success - Returns Complete Dashboard Data")
    void getDashboardData_Success_ReturnsCompleteDashboardData() {
        // Act
        QADashboardResponseModel result = qaService.getDashboardData();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getServices());
        assertNotNull(result.getCoverageSummary());
        assertNotNull(result.getAvailableServices());
        assertNotNull(result.getAutomatedApiTests());
        assertTrue(result.getServices().size() > 0);
        assertTrue(result.getCoverageSummary().getTotalServices() > 0);
    }

    @Test
    @DisplayName("getDashboardData - Coverage Summary - Calculates Correctly")
    void getDashboardData_CoverageSummary_CalculatesCorrectly() {
        // Act
        QADashboardResponseModel result = qaService.getDashboardData();

        // Assert
        QADashboardResponseModel.CoverageSummaryData summary = result.getCoverageSummary();
        assertNotNull(summary);
        assertTrue(summary.getTotalMethods() >= 0);
        assertTrue(summary.getTotalTests() >= 0);
        assertTrue(summary.getOverallCoveragePercentage() >= 0.0);
        assertTrue(summary.getOverallCoveragePercentage() <= 100.0);
    }

    // ==================== GET ALL ENDPOINTS WITH TESTS TESTS ====================

    @Test
    @DisplayName("getAllEndpointsWithTests - Success - Returns All Services")
    void getAllEndpointsWithTests_Success_ReturnsAllServices() {
        // Act
        List<QAResponseModel> result = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
        // Verify at least one service has methods
        boolean hasMethodsInAtLeastOneService = result.stream()
            .anyMatch(service -> service.getMethods() != null && !service.getMethods().isEmpty());
        assertTrue(hasMethodsInAtLeastOneService);
    }

    @Test
    @DisplayName("getAllEndpointsWithTests - Service Structure - Contains Required Fields")
    void getAllEndpointsWithTests_ServiceStructure_ContainsRequiredFields() {
        // Act
        List<QAResponseModel> result = qaService.getAllEndpointsWithTests();

        // Assert
        assertFalse(result.isEmpty());
        QAResponseModel firstService = result.get(0);
        
        assertNotNull(firstService.getServiceName());
        assertNotNull(firstService.getControllerName());
        assertNotNull(firstService.getBasePath());
        assertNotNull(firstService.getMethods());
    }

    // ==================== GET ENDPOINTS BY SERVICE TESTS ====================

    @Test
    @DisplayName("getEndpointsByService - Valid Service - Returns Service Info")
    void getEndpointsByService_ValidService_ReturnsServiceInfo() {
        // Act
        QAResponseModel result = qaService.getEndpointsWithTestsByService("AddressService");

        // Assert
        assertNotNull(result);
        assertEquals("AddressService", result.getServiceName());
        assertNotNull(result.getMethods());
    }

    @Test
    @DisplayName("getEndpointsByService - Service Without Suffix - Normalizes Name")
    void getEndpointsByService_ServiceWithoutSuffix_NormalizesName() {
        // Act
        QAResponseModel result = qaService.getEndpointsWithTestsByService("Address");

        // Assert
        assertNotNull(result);
        assertEquals("AddressService", result.getServiceName());
    }

    @Test
    @DisplayName("getEndpointsByService - Invalid Service - Throws NotFoundException")
    void getEndpointsByService_InvalidService_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            qaService.getEndpointsWithTestsByService("NonExistentService");
        });
    }

    @Test
    @DisplayName("getEndpointsByService - Null Service Name - Throws NotFoundException")
    void getEndpointsByService_NullServiceName_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            qaService.getEndpointsWithTestsByService(null);
        });
    }

    // ==================== GET COVERAGE SUMMARY TESTS ====================

    @Test
    @DisplayName("getCoverageSummary - Success - Returns Summary Statistics")
    void getCoverageSummary_Success_ReturnsSummaryStatistics() {
        // Act
        Map<String, Object> result = qaService.getCoverageSummary();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("totalServices"));
        assertTrue(result.containsKey("totalMethods"));
        assertTrue(result.containsKey("totalTests"));
        assertTrue(result.containsKey("overallCoveragePercentage"));
        assertTrue(result.containsKey("serviceBreakdown"));
    }

    @Test
    @DisplayName("getCoverageSummary - Service Breakdown - Contains All Services")
    void getCoverageSummary_ServiceBreakdown_ContainsAllServices() {
        // Act
        Map<String, Object> result = qaService.getCoverageSummary();

        // Assert
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("serviceBreakdown");
        assertNotNull(breakdown);
        assertTrue(breakdown.size() > 0);
        
        // Verify breakdown structure
        Map<String, Object> firstService = breakdown.get(0);
        assertTrue(firstService.containsKey("serviceName"));
        assertTrue(firstService.containsKey("methodCount"));
        assertTrue(firstService.containsKey("testCount"));
        assertTrue(firstService.containsKey("coveragePercentage"));
    }

    // ==================== GET AVAILABLE SERVICES TESTS ====================

    @Test
    @DisplayName("getAvailableServices - Success - Returns Service Names")
    void getAvailableServices_Success_ReturnsServiceNames() {
        // Act
        List<String> result = qaService.getAvailableServices();

        // Assert
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.contains("AddressService"));
        assertTrue(result.contains("UserService"));
    }

    @Test
    @DisplayName("getAvailableServices - All Services - End With Service Suffix")
    void getAvailableServices_AllServices_EndWithServiceSuffix() {
        // Act
        List<String> result = qaService.getAvailableServices();

        // Assert
        for (String serviceName : result) {
            assertTrue(serviceName.endsWith("Service"));
        }
    }

    // ==================== SAVE TEST RUN TESTS ====================

    @Test
    @DisplayName("saveTestRun - Valid Request - Saves Successfully")
    void saveTestRun_ValidRequest_SavesSuccessfully() {
        // Arrange
        when(testRunRepository.save(any(TestRun.class))).thenReturn(mockTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(validTestRunRequest);

        // Assert
        assertNotNull(result);
        verify(testRunRepository, atLeastOnce()).save(any(TestRun.class));
    }

    @Test
    @DisplayName("saveTestRun - Null Request - Throws BadRequestException")
    void saveTestRun_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(null);
        });
    }

    @Test
    @DisplayName("saveTestRun - Null Service Name - Throws BadRequestException")
    void saveTestRun_NullServiceName_ThrowsBadRequestException() {
        // Arrange
        validTestRunRequest.setServiceName(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(validTestRunRequest);
        });
    }

    @Test
    @DisplayName("saveTestRun - Empty Service Name - Throws BadRequestException")
    void saveTestRun_EmptyServiceName_ThrowsBadRequestException() {
        // Arrange
        validTestRunRequest.setServiceName("  ");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(validTestRunRequest);
        });
    }

    @Test
    @DisplayName("saveTestRun - Null Results - Throws BadRequestException")
    void saveTestRun_NullResults_ThrowsBadRequestException() {
        // Arrange
        validTestRunRequest.setResults(null);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(validTestRunRequest);
        });
    }

    @Test
    @DisplayName("saveTestRun - Empty Results - Throws BadRequestException")
    void saveTestRun_EmptyResults_ThrowsBadRequestException() {
        // Arrange
        validTestRunRequest.setResults(new ArrayList<>());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(validTestRunRequest);
        });
    }

    @Test
    @DisplayName("saveTestRun - Multiple Results - Saves All Results")
    void saveTestRun_MultipleResults_SavesAllResults() {
        // Arrange
        TestRunRequestModel.TestResultData result2 = new TestRunRequestModel.TestResultData();
        result2.setTestMethodName("toggleAddress_NotFound_ThrowsException");
        result2.setMethodName("toggleAddress");
        result2.setPassed(false);
        result2.setDurationMs(100L);
        result2.setErrorMessage("Address not found");
        
        validTestRunRequest.getResults().add(result2);
        
        when(testRunRepository.save(any(TestRun.class))).thenReturn(mockTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(validTestRunRequest);

        // Assert
        assertNotNull(result);
        verify(testRunRepository, atLeastOnce()).save(any(TestRun.class));
    }

    // ==================== GET LATEST TEST RESULTS TESTS ====================

    @Test
    @DisplayName("getLatestTestResults - All Services - Returns All Results")
    void getLatestTestResults_AllServices_ReturnsAllResults() {
        // Arrange
        List<LatestTestResult> mockResults = new ArrayList<>();
        LatestTestResult result1 = new LatestTestResult();
        result1.setServiceName("AddressService");
        result1.setTestMethodName("toggleAddress_Success");
        mockResults.add(result1);
        
        when(latestTestResultRepository.findByClientId(anyLong())).thenReturn(mockResults);

        // Act
        List<LatestTestResultResponseModel> results = qaService.getLatestTestResults(null);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(latestTestResultRepository).findByClientId(anyLong());
    }

    @Test
    @DisplayName("getLatestTestResults - Specific Service - Returns Filtered Results")
    void getLatestTestResults_SpecificService_ReturnsFilteredResults() {
        // Arrange
        List<LatestTestResult> mockResults = new ArrayList<>();
        LatestTestResult result1 = new LatestTestResult();
        result1.setServiceName("AddressService");
        mockResults.add(result1);
        
        when(latestTestResultRepository.findByClientIdAndServiceName(anyLong(), eq("AddressService")))
            .thenReturn(mockResults);

        // Act
        List<LatestTestResultResponseModel> results = qaService.getLatestTestResults("AddressService");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(latestTestResultRepository).findByClientIdAndServiceName(anyLong(), eq("AddressService"));
    }

    @Test
    @DisplayName("getLatestTestResults - Empty Service Name - Returns All Results")
    void getLatestTestResults_EmptyServiceName_ReturnsAllResults() {
        // Arrange
        when(latestTestResultRepository.findByClientId(anyLong())).thenReturn(new ArrayList<>());

        // Act
        List<LatestTestResultResponseModel> results = qaService.getLatestTestResults("  ");

        // Assert
        assertNotNull(results);
        verify(latestTestResultRepository).findByClientId(anyLong());
    }

    // ==================== START TEST EXECUTION TESTS ====================

    @Test
    @DisplayName("startTestExecution - Valid Request - Returns Status")
    void startTestExecution_ValidRequest_ReturnsStatus() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setServiceName("AddressService");
        request.setRunAll(false);
        
        doNothing().when(testExecutorService).storeStatus(anyString(), any());
        doNothing().when(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());

        // Act
        TestExecutionStatusModel result = qaService.startTestExecution(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getExecutionId());
        verify(testExecutorService).storeStatus(anyString(), any());
        verify(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("startTestExecution - Null Request - Throws BadRequestException")
    void startTestExecution_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(null);
        });
    }

    @Test
    @DisplayName("startTestExecution - Run All - Executes All Tests")
    void startTestExecution_RunAll_ExecutesAllTests() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(true);
        
        doNothing().when(testExecutorService).storeStatus(anyString(), any());
        doNothing().when(testExecutorService).executeTestsAsync(anyString(), isNull(), isNull(), isNull());

        // Act
        TestExecutionStatusModel result = qaService.startTestExecution(request);

        // Assert
        assertNotNull(result);
        verify(testExecutorService).executeTestsAsync(anyString(), isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("startTestExecution - Specific Method - Executes Method Tests")
    void startTestExecution_SpecificMethod_ExecutesMethodTests() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setServiceName("AddressService");
        request.setMethodName("toggleAddress");
        request.setRunAll(false);
        
        doNothing().when(testExecutorService).storeStatus(anyString(), any());
        doNothing().when(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());

        // Act
        TestExecutionStatusModel result = qaService.startTestExecution(request);

        // Assert
        assertNotNull(result);
        assertEquals("AddressService", result.getServiceName());
        assertEquals("toggleAddress", result.getMethodName());
    }

    // ==================== GET TEST EXECUTION STATUS TESTS ====================

    @Test
    @DisplayName("getTestExecutionStatus - Valid ID - Returns Status")
    void getTestExecutionStatus_ValidId_ReturnsStatus() {
        // Arrange
        String executionId = "test-123";
        TestExecutionStatusModel mockStatus = new TestExecutionStatusModel();
        mockStatus.setExecutionId(executionId);
        mockStatus.setStatus("RUNNING");
        
        when(testExecutorService.getStatus(executionId)).thenReturn(mockStatus);

        // Act
        TestExecutionStatusModel result = qaService.getTestExecutionStatus(executionId);

        // Assert
        assertNotNull(result);
        assertEquals(executionId, result.getExecutionId());
        assertEquals("RUNNING", result.getStatus());
        verify(testExecutorService).getStatus(executionId);
    }

    @Test
    @DisplayName("getTestExecutionStatus - Invalid ID - Throws NotFoundException")
    void getTestExecutionStatus_InvalidId_ThrowsNotFoundException() {
        // Arrange
        when(testExecutorService.getStatus(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            qaService.getTestExecutionStatus("invalid-id");
        });
    }

    @Test
    @DisplayName("getTestExecutionStatus - Null ID - Throws NotFoundException")
    void getTestExecutionStatus_NullId_ThrowsNotFoundException() {
        // Arrange
        when(testExecutorService.getStatus(null)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            qaService.getTestExecutionStatus(null);
        });
    }
}
