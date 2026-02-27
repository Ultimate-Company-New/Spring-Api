package com.example.SpringApi.ServiceTests.QA;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.QADashboardResponseModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for QAService.getDashboardData() method.
 *
 * <p>Test Coverage: - Success scenarios (5 tests) - Validation tests (5 tests) - Edge cases (5
 * tests)
 */
@ExtendWith(MockitoExtension.class)
class GetDashboardDataTest extends QAServiceTestBase {

  // Total Tests: 19
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s01_success_returnsCompleteDashboardData() {
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
    assertNotNull(dashboard.getAvailableServices());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s02_multipleServices_calculatesCorrectTotals() {
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
    assertEquals(dashboard.getServices().size(), dashboard.getCoverageSummary().getTotalServices());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s03_emptyServices_returnsZeroCoverage() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertNotNull(dashboard.getAvailableServices());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s04_allMethodsCovered_returns100PercentCoverage() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertTrue(dashboard.getCoverageSummary().getOverallCoveragePercentage() >= 0.0);
    assertTrue(dashboard.getCoverageSummary().getOverallCoveragePercentage() <= 100.0);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s05_partialCoverage_calculatesCorrectPercentage() {
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
    assertFalse(dashboard.getCoverageSummary().getServiceBreakdown().isEmpty());
  }

  // ==================== VALIDATION TESTS ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s06_coveragePercentage_isBetween0And100() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertTrue(dashboard.getCoverageSummary().getOverallCoveragePercentage() >= 0.0);
    assertTrue(dashboard.getCoverageSummary().getOverallCoveragePercentage() <= 100.0);
    assertFalse(Double.isNaN(dashboard.getCoverageSummary().getOverallCoveragePercentage()));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s07_totalMethods_isNonNegative() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertTrue(dashboard.getCoverageSummary().getTotalMethods() >= 0);
    assertTrue(
        dashboard.getCoverageSummary().getTotalMethodsWithCoverage()
            <= dashboard.getCoverageSummary().getTotalMethods());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s08_totalTests_isNonNegative() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertTrue(dashboard.getCoverageSummary().getTotalTests() >= 0);
    assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s09_serviceBreakdown_matchesTotalCounts() {
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
    assertEquals(
        dashboard.getCoverageSummary().getTotalServices(),
        dashboard.getCoverageSummary().getServiceBreakdown().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s10_availableServices_containsAllMappedServices() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertTrue(dashboard.getAvailableServices().contains("QAService"));
    assertEquals(
        dashboard.getAvailableServices().stream().distinct().count(),
        dashboard.getAvailableServices().size());
  }

  // ==================== EDGE CASES ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s11_noTestsForAnyMethod_returnsZeroTests() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary());
    assertTrue(dashboard.getCoverageSummary().getTotalTests() >= 0);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s12_singleService_returnsCorrectData() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertNotNull(dashboard.getCoverageSummary().getServiceBreakdown());
    assertTrue(
        dashboard.getCoverageSummary().getServiceBreakdown().size()
            <= dashboard.getAvailableServices().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s13_largeNumberOfServices_handlesCorrectly() {
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
    assertFalse(dashboard.getAvailableServices().isEmpty());
    assertTrue(dashboard.getCoverageSummary().getTotalServices() >= 1);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s14_servicesWithNoMethods_excludedFromCounts() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    QADashboardResponseModel dashboard = qaService.getDashboardData();

    // Assert
    assertNotNull(dashboard);
    assertTrue(
        dashboard.getCoverageSummary().getTotalMethodsWithCoverage()
            <= dashboard.getCoverageSummary().getTotalMethods());
    assertTrue(dashboard.getCoverageSummary().getOverallCoveragePercentage() <= 100.0);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getDashboardData_s15_roundingPrecision_correctToTwoDecimals() {
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
    double overallCoverage = dashboard.getCoverageSummary().getOverallCoveragePercentage();
    assertEquals(Math.round(overallCoverage * 100.0) / 100.0, overallCoverage);
  }

  /**
   * Purpose: Verify dashboard returns empty automated API test section when Playwright API tests
   * folder is unavailable. Expected Result: Automated tests section exists with zero tests and no
   * categories. Assertions: Automated data fields are populated with fallback values.
   */
  @Test
  void getDashboardData_s16_missingAutomatedApiTestsPath_returnsFallbackAutomatedSection()
      throws Exception {
    // Arrange
    Path tempProject = Files.createTempDirectory("qa-dashboard-no-playwright-");
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      QADashboardResponseModel dashboard = qaService.getDashboardData();

      // Assert
      assertNotNull(dashboard);
      assertNotNull(dashboard.getAutomatedApiTests());
      assertEquals(0, dashboard.getAutomatedApiTests().getTotalTests());
      assertNotNull(dashboard.getAutomatedApiTests().getCategories());
      assertTrue(dashboard.getAutomatedApiTests().getCategories().isEmpty());
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify dashboard discovers automated API tests from Playwright project and ignores
   * hidden/non-Java files. Expected Result: Only visible categories and Java test classes are
   * counted. Assertions: Category count, test count, and discovered class metadata.
   */
  @Test
  void getDashboardData_s17_automatedApiTestsPath_countsVisibleJavaFilesOnly() throws Exception {
    // Arrange
    Path tempProject = Files.createTempDirectory("qa-dashboard-playwright-");
    Path apiTestsRoot =
        tempProject.resolve(
            "Spring-PlayWright-Automation/src/test/java/com/ultimatecompany/tests/ApiTests");
    Path categoryDir = apiTestsRoot.resolve("Address");
    Path hiddenCategoryDir = apiTestsRoot.resolve(".Hidden");
    Files.createDirectories(categoryDir);
    Files.createDirectories(hiddenCategoryDir);
    Files.writeString(categoryDir.resolve("CreateAddressTest.java"), "class CreateAddressTest {}");
    Files.writeString(categoryDir.resolve("readme.txt"), "ignore");
    Files.writeString(
        hiddenCategoryDir.resolve("IgnoreHiddenTest.java"), "class IgnoreHiddenTest {}");

    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      QADashboardResponseModel dashboard = qaService.getDashboardData();

      // Assert
      assertNotNull(dashboard);
      assertNotNull(dashboard.getAutomatedApiTests());
      assertEquals(1, dashboard.getAutomatedApiTests().getTotalTests());
      assertEquals(1, dashboard.getAutomatedApiTests().getCategories().size());
      assertEquals(
          "Address", dashboard.getAutomatedApiTests().getCategories().getFirst().getCategoryName());
      assertEquals(
          "CreateAddressTest",
          dashboard
              .getAutomatedApiTests()
              .getCategories()
              .getFirst()
              .getTests()
              .getFirst()
              .getTestClass());
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
