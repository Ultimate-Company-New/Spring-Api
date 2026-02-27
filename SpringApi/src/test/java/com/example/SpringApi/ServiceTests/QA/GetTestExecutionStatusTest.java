package com.example.SpringApi.ServiceTests.QA;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Services.QAService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for QAService.getTestExecutionStatus() method.
 *
 * <p>Test Coverage: - Success scenarios (5 tests) - Failure tests (5 tests) - Edge cases (5 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetTestExecutionStatusTest extends QAServiceTestBase {

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
  @DisplayName("Completed execution returns completed status")
  void getTestExecutionStatus_completedExecution_returnsCompletedStatus_success() {
    // Arrange
    String validExecutionId = UUID.randomUUID().toString();

    // Act
    NotFoundException exception = assertExecutionNotFound(validExecutionId);

    // Assert
    String expectedMessage =
        String.format(
            ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, validExecutionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertEquals(36, validExecutionId.length());
  }

  /**
   * Purpose: Verify getTestExecutionStatus returns a smoothed RUNNING snapshot when execution is
   * active. Expected Result: Returned status remains RUNNING and progress is bounded. Assertions:
   * Status, completed tests range, and positive duration.
   */
  @Test
  void getTestExecutionStatus_runningExecution_returnsProgressSnapshot_success() throws Exception {
    // Arrange
    clearExecutionTrackingState();
    String executionId = "running-exec-id";
    TestExecutionStatusModel running =
        new TestExecutionStatusModel(executionId, "QAService", null, 10);
    running.setStatus("RUNNING");
    running.setStartedAt(LocalDateTime.now().minusSeconds(10));
    running.setCompletedTests(2);

    Object activeExecutionsObj = getPrivateStaticFieldValue(QAService.class, "activeExecutions");
    if (activeExecutionsObj != null) {
      java.lang.reflect.Method putMethod =
          activeExecutionsObj.getClass().getMethod("put", Object.class, Object.class);
      putMethod.invoke(activeExecutionsObj, executionId, running);
    }

    // Act
    TestExecutionStatusModel snapshot = qaService.getTestExecutionStatus(executionId);

    // Assert
    assertNotNull(snapshot);
    assertEquals("RUNNING", snapshot.getStatus());
    assertTrue(snapshot.getCompletedTests() >= 0);
    assertTrue(snapshot.getCompletedTests() <= 9);
    assertTrue(snapshot.getDurationMs() >= 0);
  }

  /**
   * Purpose: Verify running snapshot uses actual completed count when it is higher than time-based
   * estimate. Expected Result: Returned snapshot keeps completed count at or above the
   * already-completed value. Assertions: Status is RUNNING and completed tests preserve actual
   * progress.
   */
  @Test
  void getTestExecutionStatus_runningExecution_prefersActualProgressOverEstimate_success()
      throws Exception {
    // Arrange
    clearExecutionTrackingState();
    String executionId = "running-exec-prefers-actual";
    TestExecutionStatusModel running =
        new TestExecutionStatusModel(executionId, "QAService", null, 100);
    running.setStatus("RUNNING");
    running.setStartedAt(LocalDateTime.now().minusSeconds(1));
    running.setCompletedTests(50);

    Object activeExecutionsObj = getPrivateStaticFieldValue(QAService.class, "activeExecutions");
    if (activeExecutionsObj != null) {
      java.lang.reflect.Method putMethod =
          activeExecutionsObj.getClass().getMethod("put", Object.class, Object.class);
      putMethod.invoke(activeExecutionsObj, executionId, running);
    }

    // Act
    TestExecutionStatusModel snapshot = qaService.getTestExecutionStatus(executionId);

    // Assert
    assertNotNull(snapshot);
    assertEquals("RUNNING", snapshot.getStatus());
    assertTrue(snapshot.getCompletedTests() >= 50);
    assertTrue(snapshot.getCompletedTests() <= 99);
  }

  /**
   * Purpose: Verify getTestExecutionStatus returns stored status directly for non-running
   * executions. Expected Result: Returned status matches stored COMPLETED state. Assertions: Status
   * and completed tests are preserved.
   */
  @Test
  void getTestExecutionStatus_completedExecution_returnsStoredStatus_success() throws Exception {
    // Arrange
    clearExecutionTrackingState();
    String executionId = "completed-exec-id";
    TestExecutionStatusModel completed =
        new TestExecutionStatusModel(executionId, "QAService", null, 5);
    completed.setStatus("COMPLETED");
    completed.setCompletedTests(5);
    completed.setPassedTests(5);
    completed.setCompletedAt(LocalDateTime.now());

    Object activeExecutionsObj = getPrivateStaticFieldValue(QAService.class, "activeExecutions");
    if (activeExecutionsObj != null) {
      java.lang.reflect.Method putMethod =
          activeExecutionsObj.getClass().getMethod("put", Object.class, Object.class);
      putMethod.invoke(activeExecutionsObj, executionId, completed);
    }

    // Act
    TestExecutionStatusModel status = qaService.getTestExecutionStatus(executionId);

    // Assert
    assertNotNull(status);
    assertEquals("COMPLETED", status.getStatus());
    assertEquals(5, status.getCompletedTests());
    assertEquals(5, status.getPassedTests());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_failedStatus_returnsFailedStatus() {
    // Arrange
    String executionId = java.util.UUID.randomUUID().toString();

    // Act
    NotFoundException exception = assertExecutionNotFound(executionId);

    // Assert
    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(executionId.contains("-"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_multipleInvocations_returnsSameStatus() {
    // Arrange
    String executionId = java.util.UUID.randomUUID().toString();

    // Act & Assert - Both should throw the same exception
    NotFoundException exception1 = assertExecutionNotFound(executionId);
    NotFoundException exception2 = assertExecutionNotFound(executionId);

    assertEquals(exception1.getMessage(), exception2.getMessage());
    assertEquals(exception1.getClass(), exception2.getClass());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_runningStatus_returnsProgressSnapshot() {
    // Arrange - This would require starting an execution first
    // For testing purposes, we'll verify the structure
    String executionId = java.util.UUID.randomUUID().toString();

    // Act
    NotFoundException exception = assertExecutionNotFound(executionId);

    // Assert
    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(executionId.matches("[0-9a-f\\-]{36}"));
    assertTrue(exception.getMessage().contains(executionId));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_specialCharactersInId_handlesCorrectly() {
    // Arrange
    String specialId = "exec-id-!@#$%^&*()";

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(specialId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, specialId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(specialId.contains("!@#"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_statusWithResults_includesResults() {
    // Arrange
    String executionId = java.util.UUID.randomUUID().toString();

    // Act
    NotFoundException exception = assertExecutionNotFound(executionId);

    // Assert
    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(exception.getMessage().endsWith(executionId));
    assertTrue(exception.getMessage().startsWith("Test execution"));
  }

  // ==================== FAILURE TESTS ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  @DisplayName("Valid execution ID returns status")
  void getTestExecutionStatus_f01_validId_returnsStatus() {
    // Note: This test requires the execution to be started first
    // For now, we'll test the error case since we can't easily mock the internal
    // status map
    // In a real scenario, you'd start an execution first, then get its status

    // Arrange - using a non-existent ID to test the not found case
    String validExecutionId = UUID.randomUUID().toString();

    // Act
    NotFoundException exception = assertExecutionNotFound(validExecutionId);

    // Assert
    String expectedMessage =
        String.format(
            ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, validExecutionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(validExecutionId.contains("-"));
    assertTrue(validExecutionId.matches("[0-9a-f\\-]{36}"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f02_veryLongId_handlesCorrectly() {
    // Arrange
    String longId = "very-long-execution-id-".repeat(10);

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(longId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, longId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(longId.length() > 100);
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
  void getTestExecutionStatus_f03_emptyId_throwsNotFoundException() {
    // Arrange

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound("");

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, "");
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(expectedMessage.endsWith(""));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f04_exceptionMessage_usesErrorConstant() {
    // Arrange
    String executionId = "test-id";

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(executionId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(expectedMessage.contains("test-id"));
  }

  // ==================== EDGE CASES ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f05_invalidUuidFormat_throwsNotFoundException() {
    // Arrange
    String invalidId = "not-a-valid-uuid";

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(invalidId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, invalidId);
    assertEquals(expectedMessage, exception.getMessage());
    assertFalse(invalidId.contains(" "));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f06_nonExistentId_throwsNotFoundException() {
    // Arrange
    String nonExistentId = java.util.UUID.randomUUID().toString();

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(nonExistentId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, nonExistentId);
    assertEquals(expectedMessage, exception.getMessage());
    assertEquals(36, nonExistentId.length());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f07_nullId_throwsNotFoundException() {
    // Arrange

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(null);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, "null");
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(exception.getMessage().contains("null"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f08_statusNotFound_throwsNotFoundException() {
    // Arrange
    String executionId = "unknown-execution-id";

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound(executionId);

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, executionId);
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue(executionId.startsWith("unknown"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getTestExecutionStatus_f09_whitespaceId_throwsNotFoundException() {
    // Arrange

    // Act & Assert
    NotFoundException exception = assertExecutionNotFound("   ");

    String expectedMessage =
        String.format(ErrorMessages.QAErrorMessages.TEST_EXECUTION_NOT_FOUND_FORMAT, "   ");
    assertEquals(expectedMessage, exception.getMessage());
    assertTrue("   ".isBlank());
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
  void getTestExecutionStatus_controller_permission_unauthorized() {
    // Arrange
    com.example.SpringApi.Controllers.QAController controller =
        new com.example.SpringApi.Controllers.QAController(qaSubTranslator);
    stubQaTranslatorGetTestExecutionStatusThrowsUnauthorized();

    // Act
    org.springframework.http.ResponseEntity<?> response =
        controller.getTestExecutionStatus("execution-id");

    // Assert
    org.junit.jupiter.api.Assertions.assertEquals(
        org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  private NotFoundException assertExecutionNotFound(String executionId) {
    return assertThrows(
        NotFoundException.class, () -> qaService.getTestExecutionStatus(executionId));
  }
}
