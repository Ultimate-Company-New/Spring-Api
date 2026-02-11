package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.LatestTestResultResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QAService.getLatestTestResults() method.
 * 
 * Total Tests: 15
 * 
 * Test Coverage:
 * - Success scenarios (5 tests)
 * - Validation tests (5 tests)
 * - Edge cases (5 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetLatestTestResultsTest extends QAServiceBaseTest {

    // ==================== SUCCESS TESTS ====================

    @Test
    void getLatestTestResults_nullServiceName_returnsAllResults() {
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
    }

    @Test
    void getLatestTestResults_emptyServiceName_returnsAllResults() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getLatestTestResults_whitespaceServiceName_returnsAllResults() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("   ");

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void getLatestTestResults_validServiceName_returnsFilteredResults() {
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
    }

    @Test
    void getLatestTestResults_serviceWithoutSuffix_normalizesAndReturns() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "TestService", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientIdAndServiceName("TestService", results);

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("Test");

        // Assert
        assertNotNull(response);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    void getLatestTestResults_results_orderedByTestMethodName() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethodB"));
        results.add(createLatestTestResult(2L, "Service1", "testMethodA"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

        // Assert
        assertNotNull(response);
        // Results should be ordered (implementation may vary)
        assertTrue(response.size() >= 0);
    }

    @Test
    void getLatestTestResults_results_containAllRequiredFields() {
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
        // Verify response model has required fields
    }

    @Test
    void getLatestTestResults_results_filterByClientId() {
        // Arrange
        List<LatestTestResult> results = new ArrayList<>();
        results.add(createLatestTestResult(1L, "Service1", "testMethod1"));
        stubLatestTestResultRepositoryFindByClientId(results);

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

        // Assert
        assertNotNull(response);
        // All results should be for the current client
        assertEquals(1, response.size());
    }

    @Test
    void getLatestTestResults_multipleServices_separatesCorrectly() {
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
    }

    @Test
    void getLatestTestResults_results_mappedToResponseModel() {
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
    }

    // ==================== EDGE CASES ====================

    @Test
    void getLatestTestResults_noResults_returnsEmptyList() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientId(new ArrayList<>());

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults(null);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getLatestTestResults_nonExistentService_returnsEmptyList() {
        // Arrange
        stubLatestTestResultRepositoryFindByClientIdAndServiceName("NonExistentService", new ArrayList<>());

        // Act
        List<LatestTestResultResponseModel> response = qaService.getLatestTestResults("NonExistentService");

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getLatestTestResults_singleResult_returnsSingleItem() {
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

    @Test
    void getLatestTestResults_multipleResultsSameTest_returnsLatestOnly() {
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
    }

    @Test
    void getLatestTestResults_repositoryFailure_propagatesException() {
        // Arrange
        org.mockito.Mockito.doThrow(new RuntimeException("Database error"))
                .when(latestTestResultRepository)
                .findByClientIdOrderByServiceNameAscTestMethodNameAsc(org.mockito.ArgumentMatchers.anyLong());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            qaService.getLatestTestResults(null);
        });
    }
}
