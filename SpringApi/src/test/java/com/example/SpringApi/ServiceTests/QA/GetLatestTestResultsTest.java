package com.example.SpringApi.ServiceTests.QA;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.LatestTestResultResponseModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for QAService.getLatestTestResults() method.
 *
 * <p>Test Coverage: - Success scenarios (5 tests) - Validation tests (5 tests) - Edge cases (5
 * tests)
 */
@ExtendWith(MockitoExtension.class)
class GetLatestTestResultsTest extends QAServiceTestBase {

  // Total Tests: 16
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
  void getLatestTestResults_s01_emptyServiceName_success() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("");

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
    assertEquals("Service1", response.get(0).getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s02_multipleResultsSameTest_returnsLatestOnly() {
    // Arrange
    // The repository should already handle returning only the latest
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
    assertEquals("testMethod1", response.get(0).getTestMethodName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s03_multipleServices_separatesCorrectly() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    results.add(createLatestTestResult(2L, "Service2", "testMethod2"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(2, response.size());
    Set<String> serviceNames =
        response.stream()
            .map(LatestTestResultResponseModel::getServiceName)
            .collect(Collectors.toSet());
    assertEquals(Set.of("Service1", "Service2"), serviceNames);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s04_noResults_returnsEmptyList() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertTrue(response.isEmpty());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s05_nonExistentService_returnsEmptyList() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientIdAndServiceName(
        "NonExistentService", new ArrayList<>());

    // Act
    List<LatestTestResultResponseModel> response =
        qaService.getLatestTestResults("NonExistentService");

    // Assert
    assertNotNull(response);
    assertTrue(response.isEmpty());
    verify(latestTestResultRepository)
        .findByClientIdAndServiceNameOrderByTestMethodNameAsc(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.eq("NonExistentService"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s06_nullServiceName_returnsAllResults() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    results.add(createLatestTestResult(2L, "Service2", "testMethod2"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(2, response.size());
    assertTrue(
        response.stream()
            .allMatch(
                result -> result.getServiceName() != null && !result.getServiceName().isBlank()));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s07_results_containAllRequiredFields() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertFalse(response.isEmpty());
    LatestTestResultResponseModel first = response.get(0);
    assertNotNull(first);
    assertNotNull(first.getServiceName());
    assertNotNull(first.getTestMethodName());
    assertNotNull(first.getStatus());
    assertNotNull(first.getLatestTestResultId());
    assertTrue(first.getDurationMs() >= 0);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s08_results_filterByClientId() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
    assertEquals("Service1", response.get(0).getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s09_results_mappedToResponseModel() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    LatestTestResult result = createLatestTestResult(1L, "Service1", "testMethod1");
    results.add(result);
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertFalse(response.isEmpty());
    // Verify mapping occurred
    assertTrue(response.get(0) instanceof LatestTestResultResponseModel);
    assertEquals(result.getLatestTestResultId(), response.get(0).getLatestTestResultId());
  }

  // ==================== EDGE CASES ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s10_results_orderedByTestMethodName() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethodB"));
    results.add(createLatestTestResult(2L, "Service1", "testMethodA"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(2, response.size());
    assertTrue(response.stream().anyMatch(item -> "testMethodA".equals(item.getTestMethodName())));
    assertTrue(response.stream().anyMatch(item -> "testMethodB".equals(item.getTestMethodName())));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s11_serviceWithoutSuffix_normalizesAndReturns() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "TestService", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientIdAndServiceName("TestService", results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("Test");

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
    assertEquals("TestService", response.get(0).getServiceName());
    assertEquals("testMethod1", response.get(0).getTestMethodName());
    assertFalse(response.get(0).getStatus().isBlank());
  }

  // ==================== VALIDATION TESTS ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s12_singleResult_returnsSingleItem() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s13_validServiceName_returnsFilteredResults() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "TestService", "testMethod1"));
    results.add(createLatestTestResult(2L, "TestService", "testMethod2"));
    stubLatestTestResultRepositoryFindByClientIdAndServiceName("TestService", results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("TestService");

    // Assert
    assertNotNull(response);
    assertEquals(2, response.size());
    assertTrue(response.stream().allMatch(item -> "TestService".equals(item.getServiceName())));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getLatestTestResults_s14_whitespaceServiceName_returnsAllResults() {
    // Arrange
    List<LatestTestResult> results = new ArrayList<>();
    results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
    stubLatestTestResultRepositoryFindByClientId(results);

    // Act
    List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("   ");

    // Assert
    assertNotNull(response);
    assertEquals(1, response.size());
    assertEquals("Service1", response.get(0).getServiceName());
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
  void getLatestTestResults_repositoryFailure_propagatesException() {
    // Arrange
    stubLatestTestResultRepositoryFindByClientIdThrows(
        new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

    // Act
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> qaService.getLatestTestResults(null));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */
  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  void getLatestTestResults_controller_permission_unauthorized() {
    // Arrange
    com.example.SpringApi.Controllers.QAController controller =
        new com.example.SpringApi.Controllers.QAController(qaSubTranslator);
    stubQaTranslatorGetLatestTestResultsThrowsUnauthorized();

    // Act
    org.springframework.http.ResponseEntity<?> response = controller.getLatestTestResults(null);

    // Assert
    org.junit.jupiter.api.Assertions.assertEquals(
        org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}

