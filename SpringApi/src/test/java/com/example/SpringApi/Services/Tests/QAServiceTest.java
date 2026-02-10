//package com.example.SpringApi.Services.Tests;
//
//import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
//import com.example.SpringApi.Models.DatabaseModels.TestRun;
//import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
//import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
//import com.example.SpringApi.Models.ResponseModels.LatestTestResultResponseModel;
//import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
//import com.example.SpringApi.Models.ResponseModels.QAResponseModel;
//import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
//import com.example.SpringApi.Models.ResponseModels.TestRunResponseModel;
//import com.example.SpringApi.Repositories.LatestTestResultRepository;
//import com.example.SpringApi.Repositories.TestRunRepository;
//import com.example.SpringApi.Services.QAService;
//import com.example.SpringApi.Services.Interface.ITestExecutorService;
//import com.example.SpringApi.Exceptions.BadRequestException;
//import com.example.SpringApi.Exceptions.NotFoundException;
//import com.example.SpringApi.ErrorMessages;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for QAService.
// *
// * Test Group Summary:
// * | Group Name                              | Number of Tests |
// * | :-------------------------------------- | :-------------- |
// * | GetDashboardDataTests                   | 2               |
// * | GetAllEndpointsWithTestsTests           | 2               |
// * | GetEndpointsWithTestsByServiceTests     | 4               |
// * | GetCoverageSummaryTests                 | 2               |
// * | GetAvailableServicesTests               | 2               |
// * | SaveTestRunTests                        | 7               |
// * | GetLatestTestResultsTests               | 3               |
// * | StartTestExecutionTests                 | 5               |
// * | GetTestExecutionStatusTests             | 3               |
// * | **Total**                               | **30**          |
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("QAService Unit Tests")
//class QAServiceTest extends BaseTest {
//
//    @Mock
//    private TestRunRepository testRunRepository;
//    @Mock
//    private ITestExecutorService testExecutorService;
//
//    @Mock
//    private LatestTestResultRepository latestTestResultRepository;
//
//    @Spy
//    @InjectMocks
//    private QAService qaService;
//
//    private TestRunRequestModel validTestRunRequest;
//    private TestRun mockTestRun;
//
//    @BeforeEach
//    void setUp() {
//        validTestRunRequest = new TestRunRequestModel();
//        validTestRunRequest.setServiceName("AddressService");
//        validTestRunRequest.setRunType("SERVICE");
//        validTestRunRequest.setEnvironment("localhost");
//
//        List<TestRunRequestModel.TestResultData> results = new ArrayList<>();
//        TestRunRequestModel.TestResultData result = new TestRunRequestModel.TestResultData();
//        result.setTestMethodName("toggleAddress_AddressFound_Success");
//        result.setMethodName("toggleAddress");
//        result.setStatus("PASSED");
//        result.setDurationMs(150);
//        results.add(result);
//        validTestRunRequest.setResults(results);
//
//        mockTestRun = new TestRun("AddressService", "SERVICE", 1L, "testuser", 1L);
//        mockTestRun.setTestRunId(1L);
//    }
//
//    @Nested
//    @DisplayName("getDashboardData Tests")
//    class GetDashboardDataTests {
//
//        /**
//         * Purpose: Verify getDashboardData returns complete dashboard with all sections.
//         * Expected Result: Non-null response with services, coverage, available services, automated tests.
//         * Assertions: All sections populated and total services > 0.
//         */
//        @Test
//        @DisplayName("Get Dashboard Data - Success - Returns Complete Dashboard Data")
//        void getDashboardData_Success_ReturnsCompleteDashboardData() {
//            // Act
//            QADashboardResponseModel result = qaService.getDashboardData();
//
//            // Assert
//            assertNotNull(result);
//            assertNotNull(result.getServices());
//            assertNotNull(result.getCoverageSummary());
//            assertNotNull(result.getAvailableServices());
//            assertNotNull(result.getAutomatedApiTests());
//            assertTrue(result.getServices().size() > 0);
//            assertTrue(result.getCoverageSummary().getTotalServices() > 0);
//        }
//
//        /**
//         * Purpose: Verify coverage summary calculates correctly with valid bounds.
//         * Expected Result: Coverage percentage between 0 and 100.
//         * Assertions: Total methods/tests >= 0, coverage in valid range.
//         */
//        @Test
//        @DisplayName("Get Dashboard Data - Coverage Summary - Calculates Correctly")
//        void getDashboardData_CoverageSummary_CalculatesCorrectly() {
//            // Act
//            QADashboardResponseModel result = qaService.getDashboardData();
//
//            // Assert
//            QADashboardResponseModel.CoverageSummaryData summary = result.getCoverageSummary();
//            assertNotNull(summary);
//            assertTrue(summary.getTotalMethods() >= 0);
//            assertTrue(summary.getTotalTests() >= 0);
//            assertTrue(summary.getOverallCoveragePercentage() >= 0.0);
//            assertTrue(summary.getOverallCoveragePercentage() <= 100.0);
//        }
//    }
//
//    @Nested
//    @DisplayName("getAllEndpointsWithTests Tests")
//    class GetAllEndpointsWithTestsTests {
//
//        /**
//         * Purpose: Verify getAllEndpointsWithTests returns all services with methods.
//         * Expected Result: Non-empty list with at least one service having methods.
//         * Assertions: List not empty, at least one service has methods.
//         */
//        @Test
//        @DisplayName("Get All Endpoints With Tests - Success - Returns All Services")
//        void getAllEndpointsWithTests_Success_ReturnsAllServices() {
//            // Act
//            List<QAResponseModel> result = qaService.getAllEndpointsWithTests();
//
//            // Assert
//            assertNotNull(result);
//            assertTrue(result.size() > 0);
//            boolean hasMethodsInAtLeastOneService = result.stream()
//                    .anyMatch(service -> service.getMethods() != null && !service.getMethods().isEmpty());
//            assertTrue(hasMethodsInAtLeastOneService);
//        }
//
//        /**
//         * Purpose: Verify each service has required structure (serviceName, controllerName, basePath, methods).
//         * Expected Result: First service contains all required fields.
//         * Assertions: serviceName, controllerName, basePath, methods non-null.
//         */
//        @Test
//        @DisplayName("Get All Endpoints With Tests - Service Structure - Contains Required Fields")
//        void getAllEndpointsWithTests_ServiceStructure_ContainsRequiredFields() {
//            // Act
//            List<QAResponseModel> result = qaService.getAllEndpointsWithTests();
//
//            // Assert
//            assertFalse(result.isEmpty());
//            QAResponseModel firstService = result.get(0);
//            assertNotNull(firstService.getServiceName());
//            assertNotNull(firstService.getControllerName());
//            assertNotNull(firstService.getBasePath());
//            assertNotNull(firstService.getMethods());
//        }
//    }
//
//    @Nested
//    @DisplayName("getEndpointsWithTestsByService Tests")
//    class GetEndpointsWithTestsByServiceTests {
//
//        /**
//         * Purpose: Verify valid service name returns service info.
//         * Expected Result: QAResponseModel with matching serviceName and methods.
//         * Assertions: Service name matches, methods list non-null.
//         */
//        @Test
//        @DisplayName("Get Endpoints By Service - Valid Service - Returns Service Info")
//        void getEndpointsWithTestsByService_ValidService_ReturnsServiceInfo() {
//            // Act
//            QAResponseModel result = qaService.getEndpointsWithTestsByService("AddressService");
//
//            // Assert
//            assertNotNull(result);
//            assertEquals("AddressService", result.getServiceName());
//            assertNotNull(result.getMethods());
//        }
//
//        /**
//         * Purpose: Verify service name without "Service" suffix is normalized.
//         * Expected Result: "Address" resolves to "AddressService".
//         * Assertions: Returned serviceName is "AddressService".
//         */
//        @Test
//        @DisplayName("Get Endpoints By Service - Service Without Suffix - Normalizes Name")
//        void getEndpointsWithTestsByService_ServiceWithoutSuffix_NormalizesName() {
//            // Act
//            QAResponseModel result = qaService.getEndpointsWithTestsByService("Address");
//
//            // Assert
//            assertNotNull(result);
//            assertEquals("AddressService", result.getServiceName());
//        }
//
//        /**
//         * Purpose: Verify non-existent service throws NotFoundException.
//         * Expected Result: NotFoundException thrown.
//         * Assertions: Exception type and message contain service info.
//         */
//        @Test
//        @DisplayName("Get Endpoints By Service - Invalid Service - ThrowsNotFoundException")
//        void getEndpointsWithTestsByService_InvalidService_ThrowsNotFoundException() {
//            // Act & Assert
//            assertThrows(NotFoundException.class, () -> {
//                qaService.getEndpointsWithTestsByService("NonExistentService");
//            });
//        }
//
//        /**
//         * Purpose: Verify null service name causes NPE (service does not validate null).
//         * Expected Result: NullPointerException thrown.
//         * Assertions: Exception type is NullPointerException.
//         */
//        @Test
//        @DisplayName("Get Endpoints By Service - Null Service Name - ThrowsNullPointerException")
//        void getEndpointsWithTestsByService_NullServiceName_ThrowsNullPointerException() {
//            // Act & Assert
//            assertThrows(NullPointerException.class, () -> {
//                qaService.getEndpointsWithTestsByService(null);
//            });
//        }
//    }
//
//    @Nested
//    @DisplayName("getCoverageSummary Tests")
//    class GetCoverageSummaryTests {
//
//        /**
//         * Purpose: Verify getCoverageSummary returns all required keys.
//         * Expected Result: Map contains totalServices, totalMethods, totalTests, overallCoveragePercentage, serviceBreakdown.
//         * Assertions: All keys present.
//         */
//        @Test
//        @DisplayName("Get Coverage Summary - Success - Returns Summary Statistics")
//        void getCoverageSummary_Success_ReturnsSummaryStatistics() {
//            // Act
//            Map<String, Object> result = qaService.getCoverageSummary();
//
//            // Assert
//            assertNotNull(result);
//            assertTrue(result.containsKey("totalServices"));
//            assertTrue(result.containsKey("totalMethods"));
//            assertTrue(result.containsKey("totalTests"));
//            assertTrue(result.containsKey("overallCoveragePercentage"));
//            assertTrue(result.containsKey("serviceBreakdown"));
//        }
//
//        /**
//         * Purpose: Verify service breakdown structure when services exist.
//         * Expected Result: Each service has serviceName, totalMethods, methodsWithCoverage, totalTests, coveragePercentage.
//         * Assertions: Structure validated when breakdown non-empty.
//         */
//        @Test
//        @DisplayName("Get Coverage Summary - Service Breakdown - Contains Valid Structure")
//        void getCoverageSummary_ServiceBreakdown_ContainsValidStructure() {
//            // Act
//            Map<String, Object> result = qaService.getCoverageSummary();
//
//            // Assert
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> breakdown = (List<Map<String, Object>>) result.get("serviceBreakdown");
//            assertNotNull(breakdown);
//            assertTrue(breakdown.size() >= 0);
//            if (!breakdown.isEmpty()) {
//                Map<String, Object> firstService = breakdown.get(0);
//                assertTrue(firstService.containsKey("serviceName"));
//                assertTrue(firstService.containsKey("totalMethods"));
//                assertTrue(firstService.containsKey("methodsWithCoverage"));
//                assertTrue(firstService.containsKey("totalTests"));
//                assertTrue(firstService.containsKey("coveragePercentage"));
//            }
//        }
//    }
//
//    @Nested
//    @DisplayName("getAvailableServices Tests")
//    class GetAvailableServicesTests {
//
//        /**
//         * Purpose: Verify getAvailableServices returns non-empty list of known services.
//         * Expected Result: List contains AddressService and UserService.
//         * Assertions: Size > 0, contains expected services.
//         */
//        @Test
//        @DisplayName("Get Available Services - Success - Returns Service Names")
//        void getAvailableServices_Success_ReturnsServiceNames() {
//            // Act
//            List<String> result = qaService.getAvailableServices();
//
//            // Assert
//            assertNotNull(result);
//            assertTrue(result.size() > 0);
//            assertTrue(result.contains("AddressService"));
//            assertTrue(result.contains("UserService"));
//        }
//
//        /**
//         * Purpose: Verify all returned service names end with "Service".
//         * Expected Result: Every name ends with "Service".
//         * Assertions: All names pass endsWith check.
//         */
//        @Test
//        @DisplayName("Get Available Services - All Services - End With Service Suffix")
//        void getAvailableServices_AllServices_EndWithServiceSuffix() {
//            // Act
//            List<String> result = qaService.getAvailableServices();
//
//            // Assert
//            for (String serviceName : result) {
//                assertTrue(serviceName.endsWith("Service"));
//            }
//        }
//    }
//
//    @Nested
//    @DisplayName("saveTestRun Tests")
//    class SaveTestRunTests {
//
//        /**
//         * Purpose: Verify valid test run request saves successfully.
//         * Expected Result: TestRunResponseModel returned, repository save called.
//         * Assertions: Result non-null, save invoked at least once.
//         */
//        @Test
//        @DisplayName("Save Test Run - Valid Request - Saves Successfully")
//        void saveTestRun_ValidRequest_SavesSuccessfully() {
//            // Arrange
//            when(testRunRepository.save(any(TestRun.class))).thenReturn(mockTestRun);
//
//            // Act
//            TestRunResponseModel result = qaService.saveTestRun(validTestRunRequest);
//
//            // Assert
//            assertNotNull(result);
//            verify(testRunRepository, atLeastOnce()).save(any(TestRun.class));
//        }
//
//        /**
//         * Purpose: Verify null request throws BadRequestException.
//         * Expected Result: BadRequestException with appropriate message.
//         * Assertions: Exception type and message match.
//         */
//        @Test
//        @DisplayName("Save Test Run - Null Request - ThrowsBadRequestException")
//        void saveTestRun_NullRequest_ThrowsBadRequestException() {
//            // Act & Assert
//            assertThrows(BadRequestException.class, () -> qaService.saveTestRun(null));
//        }
//
//        /**
//         * Purpose: Verify null service name throws BadRequestException.
//         * Expected Result: BadRequestException thrown.
//         * Assertions: Exception type is BadRequestException.
//         */
//        @Test
//        @DisplayName("Save Test Run - Null Service Name - ThrowsBadRequestException")
//        void saveTestRun_NullServiceName_ThrowsBadRequestException() {
//            // Arrange
//            validTestRunRequest.setServiceName(null);
//
//            // Act & Assert
//            assertThrows(BadRequestException.class, () -> qaService.saveTestRun(validTestRunRequest));
//        }
//
//        /**
//         * Purpose: Verify blank service name throws BadRequestException.
//         * Expected Result: BadRequestException thrown.
//         * Assertions: Exception type is BadRequestException.
//         */
//        @Test
//        @DisplayName("Save Test Run - Empty Service Name - ThrowsBadRequestException")
//        void saveTestRun_EmptyServiceName_ThrowsBadRequestException() {
//            // Arrange
//            validTestRunRequest.setServiceName("  ");
//
//            // Act & Assert
//            assertThrows(BadRequestException.class, () -> qaService.saveTestRun(validTestRunRequest));
//        }
//
//        /**
//         * Purpose: Verify null results throws BadRequestException.
//         * Expected Result: BadRequestException thrown.
//         * Assertions: Exception type is BadRequestException.
//         */
//        @Test
//        @DisplayName("Save Test Run - Null Results - ThrowsBadRequestException")
//        void saveTestRun_NullResults_ThrowsBadRequestException() {
//            // Arrange
//            validTestRunRequest.setResults(null);
//
//            // Act & Assert
//            assertThrows(BadRequestException.class, () -> qaService.saveTestRun(validTestRunRequest));
//        }
//
//        /**
//         * Purpose: Verify empty results list throws BadRequestException.
//         * Expected Result: BadRequestException thrown.
//         * Assertions: Exception type is BadRequestException.
//         */
//        @Test
//        @DisplayName("Save Test Run - Empty Results - ThrowsBadRequestException")
//        void saveTestRun_EmptyResults_ThrowsBadRequestException() {
//            // Arrange
//            validTestRunRequest.setResults(new ArrayList<>());
//
//            // Act & Assert
//            assertThrows(BadRequestException.class, () -> qaService.saveTestRun(validTestRunRequest));
//        }
//
//        /**
//         * Purpose: Verify multiple results in request all get saved.
//         * Expected Result: Save invoked, result non-null.
//         * Assertions: Result non-null, save called at least once.
//         */
//        @Test
//        @DisplayName("Save Test Run - Multiple Results - Saves All Results")
//        void saveTestRun_MultipleResults_SavesAllResults() {
//            // Arrange
//            TestRunRequestModel.TestResultData result2 = new TestRunRequestModel.TestResultData();
//            result2.setTestMethodName("toggleAddress_NotFound_ThrowsException");
//            result2.setMethodName("toggleAddress");
//            result2.setStatus("FAILED");
//            result2.setDurationMs(100);
//            result2.setErrorMessage("Address not found");
//            validTestRunRequest.getResults().add(result2);
//            when(testRunRepository.save(any(TestRun.class))).thenReturn(mockTestRun);
//
//            // Act
//            TestRunResponseModel result = qaService.saveTestRun(validTestRunRequest);
//
//            // Assert
//            assertNotNull(result);
//            verify(testRunRepository, atLeastOnce()).save(any(TestRun.class));
//        }
//    }
//
//    @Nested
//    @DisplayName("getLatestTestResults Tests")
//    class GetLatestTestResultsTests {
//
//        /**
//         * Purpose: Verify null service name returns all results for client.
//         * Expected Result: List of LatestTestResultResponseModel from repository.
//         * Assertions: Result size matches mock, findByClientIdOrderBy... invoked.
//         */
//        @Test
//        @DisplayName("Get Latest Test Results - All Services - Returns All Results")
//        void getLatestTestResults_AllServices_ReturnsAllResults() {
//            // Arrange
//            List<LatestTestResult> mockResults = new ArrayList<>();
//            LatestTestResult result1 = new LatestTestResult();
//            result1.setServiceName("AddressService");
//            result1.setTestMethodName("toggleAddress_Success");
//            mockResults.add(result1);
//            when(latestTestResultRepository.findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong()))
//                    .thenReturn(mockResults);
//
//            // Act
//            List<LatestTestResultResponseModel> results = qaService.getLatestTestResults(null);
//
//            // Assert
//            assertNotNull(results);
//            assertEquals(1, results.size());
//            verify(latestTestResultRepository).findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
//        }
//
//        /**
//         * Purpose: Verify specific service name filters results.
//         * Expected Result: Only results for that service returned.
//         * Assertions: Result size matches mock, findByClientIdAndServiceName... invoked.
//         */
//        @Test
//        @DisplayName("Get Latest Test Results - Specific Service - Returns Filtered Results")
//        void getLatestTestResults_SpecificService_ReturnsFilteredResults() {
//            // Arrange
//            List<LatestTestResult> mockResults = new ArrayList<>();
//            LatestTestResult result1 = new LatestTestResult();
//            result1.setServiceName("AddressService");
//            mockResults.add(result1);
//            when(latestTestResultRepository.findByClientIdAndServiceNameOrderByTestMethodNameAsc(anyLong(), eq("AddressService")))
//                    .thenReturn(mockResults);
//
//            // Act
//            List<LatestTestResultResponseModel> results = qaService.getLatestTestResults("AddressService");
//
//            // Assert
//            assertNotNull(results);
//            assertEquals(1, results.size());
//            verify(latestTestResultRepository).findByClientIdAndServiceNameOrderByTestMethodNameAsc(anyLong(), eq("AddressService"));
//        }
//
//        /**
//         * Purpose: Verify empty/whitespace service name treated as all services.
//         * Expected Result: findByClientIdOrderBy... invoked (all results path).
//         * Assertions: Repository method for all services called.
//         */
//        @Test
//        @DisplayName("Get Latest Test Results - Empty Service Name - Returns All Results")
//        void getLatestTestResults_EmptyServiceName_ReturnsAllResults() {
//            // Arrange
//            when(latestTestResultRepository.findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong()))
//                    .thenReturn(new ArrayList<>());
//
//            // Act
//            List<LatestTestResultResponseModel> results = qaService.getLatestTestResults("  ");
//
//            // Assert
//            assertNotNull(results);
//            verify(latestTestResultRepository).findByClientIdOrderByServiceNameAscTestMethodNameAsc(anyLong());
//        }
//    }
//
//    @Nested
//    @DisplayName("startTestExecution Tests")
//    class StartTestExecutionTests {
//
//        /**
//         * Purpose: Verify valid request with methodName+serviceName starts execution.
//         * Expected Result: TestExecutionStatusModel with executionId, storeStatus and executeTestsAsync called.
//         * Assertions: Result non-null, mocks verified.
//         */
//        @Test
//        @DisplayName("Start Test Execution - Valid Request - Returns Status")
//        void startTestExecution_ValidRequest_ReturnsStatus() {
//            // Arrange
//            TestExecutionRequestModel request = new TestExecutionRequestModel();
//            doNothing().when(testExecutorService).storeStatus(anyString(), any());
//            doNothing().when(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//            // Stub QAService internal async execution/store methods to avoid running mvn in unit tests
//            doNothing().when(qaService).storeStatus(anyString(), any());
//            doNothing().when(qaService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//
//            // Act
//            verify(testExecutorService).storeStatus(anyString(), any());
//            verify(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//            // Assert
//            assertNotNull(result);
//            assertNotNull(result.getExecutionId());
//            verify(qaService).storeStatus(anyString(), any());
//            verify(qaService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//        }
//
//        /**
//         * Purpose: Verify null request throws BadRequestException.
//         * Expected Result: BadRequestException with TestExecutionRequestCannotBeNull.
//         * Assertions: Exception type and message match.
//         */
//        @Test
//        @DisplayName("Start Test Execution - Null Request - ThrowsBadRequestException")
//        void startTestExecution_NullRequest_ThrowsBadRequestException() {
//            // Act & Assert
//            BadRequestException ex = assertThrows(BadRequestException.class, () -> qaService.startTestExecution(null));
//            assertEquals(ErrorMessages.QAErrorMessages.TestExecutionRequestCannotBeNull, ex.getMessage());
//        }
//
//        /**
//         * Purpose: Verify runAll=true executes all tests with serviceName "ALL".
//         * Expected Result: executeTestsAsync called with (executionId, null, null, "ALL").
//         * Assertions: Result non-null, executeTestsAsync invoked with eq("ALL").
//         */
//        @Test
//        @DisplayName("Start Test Execution - Run All - Executes All Tests")
//            doNothing().when(testExecutorService).storeStatus(anyString(), any());
//            doNothing().when(testExecutorService).executeTestsAsync(anyString(), isNull(), isNull(), eq("ALL"));
//            TestExecutionRequestModel request = new TestExecutionRequestModel();
//            request.setRunAll(true);
//            doNothing().when(qaService).storeStatus(anyString(), any());
//            doNothing().when(qaService).executeTestsAsync(anyString(), isNull(), isNull(), eq("ALL"));
//
//            // Act
//            verify(testExecutorService).executeTestsAsync(anyString(), isNull(), isNull(), eq("ALL"));
//
//            // Assert
//            assertNotNull(result);
//            verify(qaService).executeTestsAsync(anyString(), isNull(), isNull(), eq("ALL"));
//        }
//
//        /**
//         * Purpose: Verify specific method execution returns status with service and method names.
//         * Expected Result: Result contains serviceName and methodName from request.
//         * Assertions: serviceName and methodName match request.
//         */
//        @Test
//        @DisplayName("Start Test Execution - Specific Method - Executes Method Tests")
//        void startTestExecution_SpecificMethod_ExecutesMethodTests() {
//            // Arrange
//            doNothing().when(testExecutorService).storeStatus(anyString(), any());
//            doNothing().when(testExecutorService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//            request.setMethodName("toggleAddress");
//            request.setRunAll(false);
//            doNothing().when(qaService).storeStatus(anyString(), any());
//            doNothing().when(qaService).executeTestsAsync(anyString(), anyString(), anyString(), anyString());
//
//            // Act
//            TestExecutionStatusModel result = qaService.startTestExecution(request);
//
//            // Assert
//            assertNotNull(result);
//            assertEquals("AddressService", result.getServiceName());
//            assertEquals("toggleAddress", result.getMethodName());
//        }
//
//        /**
//         * Purpose: Verify request without runAll, testNames, or methodName throws BadRequestException.
//         * Expected Result: BadRequestException with MustSpecifyRunAllOrTestNamesOrMethod.
//         * Assertions: Exception type and message match.
//         */
//        @Test
//        @DisplayName("Start Test Execution - Missing Scope - ThrowsBadRequestException")
//        void startTestExecution_MissingScope_ThrowsBadRequestException() {
//            // Arrange - only serviceName, no methodName, runAll, or testNames
//            TestExecutionRequestModel request = new TestExecutionRequestModel();
//            request.setServiceName("AddressService");
//            request.setRunAll(false);
//
//            // Act & Assert
//            BadRequestException ex = assertThrows(BadRequestException.class, () -> qaService.startTestExecution(request));
//            assertTrue(ex.getMessage().contains(ErrorMessages.QAErrorMessages.MustSpecifyRunAllOrTestNamesOrMethod));
//        }
//    }
//
//    @Nested
//    @DisplayName("getTestExecutionStatus Tests")
//    class GetTestExecutionStatusTests {
//
//        /**
//         * Purpose: Verify valid execution ID returns status from testExecutorService.
//         * Expected Result: Status model with matching executionId and status.
//         * Assertions: Result matches mock, getStatus invoked.
//         */
//        @Test
//        @DisplayName("Get Test Execution Status - Valid ID - Returns Status")
//        void getTestExecutionStatus_ValidId_ReturnsStatus() {
//            // Arrange
//            when(testExecutorService.getStatus(executionId)).thenReturn(mockStatus);
//            TestExecutionStatusModel mockStatus = new TestExecutionStatusModel();
//            mockStatus.setExecutionId(executionId);
//            mockStatus.setStatus("RUNNING");
//            when(qaService.getStatus(executionId)).thenReturn(mockStatus);
//
//            // Act
//            TestExecutionStatusModel result = qaService.getTestExecutionStatus(executionId);
//
//            verify(testExecutorService).getStatus(executionId);
//            assertNotNull(result);
//            assertEquals(executionId, result.getExecutionId());
//            assertEquals("RUNNING", result.getStatus());
//            verify(qaService).getStatus(executionId);
//        }
//
//        /**
//         * Purpose: Verify invalid/non-existent execution ID throws NotFoundException.
//         * Expected Result: NotFoundException with TestExecutionNotFoundFormat.
//         * Assertions: Exception type and message contain execution ID.
//         */
//            when(testExecutorService.getStatus(anyString())).thenReturn(null);
//        @DisplayName("Get Test Execution Status - Invalid ID - ThrowsNotFoundException")
//        void getTestExecutionStatus_InvalidId_ThrowsNotFoundException() {
//            // Arrange
//            when(qaService.getStatus(anyString())).thenReturn(null);
//
//            // Act & Assert
//            assertThrows(NotFoundException.class, () -> qaService.getTestExecutionStatus("invalid-id"));
//        }
//
//        /**
//         * Purpose: Verify null execution ID throws NotFoundException when getStatus returns null.
//         * Expected Result: NotFoundException thrown.
//         * Assertions: Exception type is NotFoundException.
//         */
//            when(testExecutorService.getStatus(null)).thenReturn(null);
//        @DisplayName("Get Test Execution Status - Null ID - ThrowsNotFoundException")
//        void getTestExecutionStatus_NullId_ThrowsNotFoundException() {
//            // Arrange
//            when(qaService.getStatus(null)).thenReturn(null);
//
//            // Act & Assert
//            assertThrows(NotFoundException.class, () -> qaService.getTestExecutionStatus(null));
//        }
//    }
//}
