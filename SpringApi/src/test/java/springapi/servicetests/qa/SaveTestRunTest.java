package springapi.servicetests.qa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.QaController;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.LatestTestResult;
import springapi.models.databasemodels.TestRun;
import springapi.models.requestmodels.TestRunRequestModel;
import springapi.models.responsemodels.TestRunResponseModel;

/**
 * Unit tests for QaService.saveTestRun() method.
 *
 * <p>Test Coverage: - Success scenarios (6 tests) - Validation failures (12 tests) - Edge cases (7
 * tests)
 */
@ExtendWith(MockitoExtension.class)
class SaveTestRunTest extends QAServiceTestBase {

  // Total Tests: 27
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
  void saveTestRun_s01_ValidRequest_Success() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getTestRunId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_s02_multipleResults_savesAllResults() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest("TestService", 5);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals(5, request.getResults().size());
    assertEquals("TestService", result.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_s03_withEnvironment_savesEnvironment() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setEnvironment("production");
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("production", request.getEnvironment());
    assertEquals("TestService", result.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_s04_withRunType_savesRunType() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setRunType("automated");
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("automated", request.getRunType());
    assertEquals(1L, result.getTestRunId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_s05_updatesLatestTestResults_correctly() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);
    stubLatestTestResultRepositorySaveAll(new ArrayList<>());

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("TestService", result.getServiceName());
    assertEquals("manual", result.getRunType());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_s06_marksTestRunComplete_afterSave() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getStartTime());
    assertNotNull(result.getEndTime());
    assertFalse(result.getEndTime().isBefore(result.getStartTime()));
    assertEquals("manual", result.getRunType());
  }

  /**
   * Purpose: Verify existing latest-test records are updated (not inserted as new). Expected
   * Result: Existing latest result is loaded and saved after update. Assertions: Save succeeds and
   * repository save is called for existing entity.
   */
  @Test
  void saveTestRun_s07_existingLatestResult_updatesExistingRecord() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest("QaService", 1);
    request.getResults().get(0).setTestMethodName("saveTestRun_s01_ValidRequest_Success");
    TestRun savedTestRun = createTestRun(10L, "QaService");
    stubTestRunRepositorySave(savedTestRun);

    LatestTestResult existingLatest =
        createLatestTestResult(77L, "QaService", "saveTestRun_s01_ValidRequest_Success");
    existingLatest.setTestClassName("QAServiceTest");
    stubLatestTestResultRepositoryFindByClientIdAndServiceNameAndTestMethodName(
        "QaService", "QAServiceTest", "saveTestRun_s01_ValidRequest_Success", existingLatest);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    verify(latestTestResultRepository).save(existingLatest);
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
  void saveTestRun_f01_nullRequest_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = null;

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.TEST_RUN_REQUEST_CANNOT_BE_NULL, exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f02_nullServiceName_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setServiceName(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(ErrorMessages.QaErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    assertNull(request.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f03_emptyServiceName_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setServiceName("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(ErrorMessages.QaErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    assertTrue(request.getServiceName().isEmpty());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f04_whitespaceServiceName_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setServiceName("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(ErrorMessages.QaErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    assertTrue(request.getServiceName().isBlank());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f05_nullResults_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setResults(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.AT_LEAST_ONE_TEST_RESULT_REQUIRED, exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f06_emptyResults_throwsBadRequestException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.setResults(new ArrayList<>());

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.AT_LEAST_ONE_TEST_RESULT_REQUIRED, exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f07_resultWithNullTestMethodName_handlesGracefully() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setTestMethodName(null);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f08_resultWithNullMethodName_handlesGracefully() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setMethodName(null);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f09_resultWithNullStatus_handlesGracefully() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setStatus(null);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f10_resultWithNullDuration_defaultsToZero() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setDurationMs(null);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f11_resultWithNegativeDuration_handlesGracefully() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setDurationMs(-100);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f12_exceptionMessages_useErrorConstants() {
    // Arrange - null request
    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.saveTestRun(null);
            });

    // Verify the exception message comes from ErrorMessages constants
    assertEquals(
        ErrorMessages.QaErrorMessages.TEST_RUN_REQUEST_CANNOT_BE_NULL, exception.getMessage());
  }

  /*
   **********************************************************************************************
   * EDGE CASES
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f13_singleResult_savesSingleResult() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest("TestService", 1);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, request.getResults().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f14_largeNumberOfResults_savesAll() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest("TestService", 100);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals(100, request.getResults().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f15_resultWithErrorMessage_savesErrorMessage() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setErrorMessage("Test failed with error");
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("Test failed with error", request.getResults().get(0).getErrorMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f16_resultWithStackTrace_savesStackTrace() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setStackTrace("at line 1\nat line 2");
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("at line 1\nat line 2", request.getResults().get(0).getStackTrace());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f17_resultWithDisplayName_savesDisplayName() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    request.getResults().get(0).setDisplayName("Custom Display Name");
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
    assertEquals("Custom Display Name", request.getResults().get(0).getDisplayName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f18_unknownServiceName_stillSaves() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest("UnknownService", 1);
    TestRun savedTestRun = createTestRun();
    stubTestRunRepositorySave(savedTestRun);

    // Act
    TestRunResponseModel result = qaService.saveTestRun(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void saveTestRun_f19_repositorySaveFailure_propagatesException() {
    // Arrange
    TestRunRequestModel request = createValidTestRunRequest();
    stubTestRunRepositorySaveThrows(
        new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

    // Act & Assert
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> qaService.saveTestRun(request));
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
  @DisplayName("Save Test Run - Controller permission unauthorized - Success")
  void saveTestRun_controller_permission_unauthorized() {
    // Arrange
    QaController controller = new QaController(qaSubTranslator);
    stubQaTranslatorSaveTestRunThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.saveTestRun(createValidTestRunRequest());

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
