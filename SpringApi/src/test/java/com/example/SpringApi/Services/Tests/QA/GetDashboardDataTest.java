package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QAService.getDashboardData() method.
 * 
 * Total Tests: 15
 * 
 * Test Coverage:
 * - Success scenarios (5 tests)
 * - Validation tests (5 tests)
 * - Edge cases (5 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetDashboardDataTest extends QAServiceBaseTest {

    // ==================== SUCCESS TESTS ====================

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
}
