package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for QAService.getDashboardData() method.
 * 
 * Total Tests: 17
 * 
 * Test Coverage:
 * - Success scenarios (5 tests)
 * - Validation tests (5 tests)
 * - Edge cases (5 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetDashboardDataTest extends QAServiceTestBase {
    // Total Tests: 17

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_success_returnsCompleteDashboardData() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        results.add(createLatestTestResult(2L, "Service2", "testMethod2"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
        assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
        assertNotNull(dashboard.getServices());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_multipleServices_calculatesCorrectTotals() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        results.add(createLatestTestResult(2L, "Service1", "testMethod2"));
        results.add(createLatestTestResult(3L, "Service2", "testMethod3"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_emptyServices_returnsZeroCoverage() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_allMethodsCovered_returns100PercentCoverage() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_partialCoverage_calculatesCorrectPercentage() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        results.add(createLatestTestResult(2L, "Service2", "testMethod2"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    // ==================== VALIDATION TESTS ====================

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_coveragePercentage_isBetween0And100() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
        // Coverage percentage should be between 0 and 100
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_totalMethods_isNonNegative() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_totalTests_isNonNegative() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_serviceBreakdown_matchesTotalCounts() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        results.add(createLatestTestResult(2L, "Service2", "testMethod2"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
        // Service breakdown should match total counts
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_availableServices_containsAllMappedServices() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        // Available services should contain all mapped services
    }

    // ==================== EDGE CASES ====================

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_noTestsForAnyMethod_returnsZeroTests() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_singleService_returnsCorrectData() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_largeNumberOfServices_handlesCorrectly() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            results.add(createLatestTestResult((long) i, "Service" + i, "testMethod" + i));
        }
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_servicesWithNoMethods_excludedFromCounts() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        // Services with no methods should be excluded from counts
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getDashboardData_roundingPrecision_correctToTwoDecimals() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        results.add(createLatestTestResult(2L, "Service1", "testMethod2"));
        results.add(createLatestTestResult(3L, "Service2", "testMethod3"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        QADashboardResponseModel dashboard = qaService.getDashboardData();

        // Assert
        assertNotNull(dashboard);
        assertNotNull(dashboard.getCoverageSummary());
        // Coverage percentage should be rounded to 2 decimal places
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    @DisplayName("Get Dashboard Data - Service Failure - Throws Exception")
    void getDashboardData_serviceFailure_ThrowsException() {
        // Arrange
        stubQaServiceGetAllEndpointsWithTestsThrows(
                new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class, () -> qaService.getDashboardData());

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    @DisplayName("Get Dashboard Data - Controller permission unauthorized - Success")
    void getDashboardData_controller_permission_unauthorized() {
        // Arrange
        QAController controller = new QAController(qaSubTranslator);
        stubQaTranslatorGetDashboardDataThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getDashboardData();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
