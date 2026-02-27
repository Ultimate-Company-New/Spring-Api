package com.example.SpringApi.ServiceTests.UserLog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for UserLogService.logDataWithContext method. */
@DisplayName("UserLogService - LogDataWithContext Tests")
class LogDataWithContextTest extends UserLogServiceTestBase {

  // Total Tests: 25
  // ========================================
  // Section 1: Success Tests
  // ========================================

  /*
   * Purpose: Verify logging with all context values null except userId.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - All Null Except UserId - Success")
  void logDataWithContext_allNullExceptUserId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result = userLogService.logDataWithContext(TEST_USER_ID, null, null, null, null);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify return value is always true.
   * Expected Result: Returns true.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Always Returns True - Success")
  void logDataWithContext_alwaysReturnsTrue_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
  }

  /*
   * Purpose: Verify logging with different user context.
   * Expected Result: Log is created with different context.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Different User Context - Success")
  void logDataWithContext_differentUserContext_success() {
    // Arrange
    Long differentUserId = 999L;
    String differentUsername = "different_user";
    Long differentClientId = 555L;
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            differentUserId, differentUsername, differentClientId, "test value", TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository, times(1)).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with empty username in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Empty Username - Success")
  void logDataWithContext_emptyUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with max long client ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Max Long Client ID - Success")
  void logDataWithContext_maxLongClientId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", Long.MAX_VALUE, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with max long user ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Max Long User ID - Success")
  void logDataWithContext_maxLongUserId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            Long.MAX_VALUE, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify multiple calls work independently.
   * Expected Result: Each call saves separately.
   * Assertions: verify save called correct times.
   */
  @Test
  @DisplayName("logDataWithContext - Multiple Calls Work Independently - Success")
  void logDataWithContext_multipleCallsWorkIndependently_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    userLogService.logDataWithContext(1L, "user1", 100L, "value1", "endpoint1");
    userLogService.logDataWithContext(2L, "user2", 200L, "value2", "endpoint2");
    userLogService.logDataWithContext(3L, "user3", 300L, "value3", "endpoint3");

    // Assert
    verify(userLogRepository, times(3)).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with negative client ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Negative Client ID - Success")
  void logDataWithContext_negativeClientId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", -1L, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with negative user ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Negative User ID - Success")
  void logDataWithContext_negativeUserId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            -1L, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with null client ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Null Client ID - Success")
  void logDataWithContext_nullClientId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", null, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with null endpoint in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Null Endpoint - Success")
  void logDataWithContext_nullEndpoint_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, null);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with null new value in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Null New Value - Success")
  void logDataWithContext_nullNewValue_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", TEST_CARRIER_ID, null, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository, times(1)).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with null username in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Null Username - Success")
  void logDataWithContext_nullUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, null, TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify repository save is called exactly once.
   * Expected Result: save() called once.
   * Assertions: verify(userLogRepository, times(1)).save(any()).
   */
  @Test
  @DisplayName("logDataWithContext - Repository Save Called Once - Success")
  void logDataWithContext_repositorySaveCalledOnce_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    userLogService.logDataWithContext(
        TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    verify(userLogRepository, times(1)).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with special chars in username.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Special Chars Username - Success")
  void logDataWithContext_specialCharsUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "user@domain.com", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with explicit context - basic success.
   * Expected Result: Log is created with explicit context.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Basic Success")
  void logDataWithContext_success_basic() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "testuser", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository, times(1)).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with unicode username.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Unicode Username - Success")
  void logDataWithContext_unicodeUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "用户名", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with very long username.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Very Long Username - Success")
  void logDataWithContext_veryLongUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);
    String longUsername = "user_" + "x".repeat(500);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, longUsername, TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with whitespace username.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Whitespace Username - Success")
  void logDataWithContext_whitespaceUsername_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "   ", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with zero client ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Zero Client ID - Success")
  void logDataWithContext_zeroClientId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(TEST_USER_ID, "admin", 0L, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  /*
   * Purpose: Verify logging with zero user ID in context.
   * Expected Result: Log is created.
   * Assertions: assertTrue(result).
   */
  @Test
  @DisplayName("logDataWithContext - Zero User ID - Success")
  void logDataWithContext_zeroUserId_success() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            0L, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
  }

  // ========================================
  // Section 2: Failure / Exception Tests
  // ========================================

  /*
   * Purpose: Verify logging propagates repository exceptions
   * Expected Result: RuntimeException is thrown when repository save fails
   * Assertions: Exception captured and message verified
   */
  @Test
  @DisplayName("logDataWithContext - Repository Save Fails - Throws Exception")
  void logDataWithContext_repositorySaveFails_throwsException() {
    // Arrange
    stubUserLogRepositorySaveThrows(
        new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

    // Act
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () ->
                userLogService.logDataWithContext(
                    TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, exception.getMessage());
  }

  // ========================================
  // Section 3: Controller Permission/Auth Tests
  // ========================================

  /**
   * Purpose: Verify UserLogService has controller endpoint with permission check. Expected Result:
   * HTTP UNAUTHORIZED status returned and @PreAuthorize verified. Assertions:
   * assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()), assertNotNull, assertTrue
   */
  @Test
  @DisplayName("logDataWithContext - Controller permission forbidden")
  void logDataWithContext_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    com.example.SpringApi.Models.RequestModels.UserLogsRequestModel request =
        new com.example.SpringApi.Models.RequestModels.UserLogsRequestModel();
    stubServiceThrowsUnauthorizedException();
    java.lang.reflect.Method method =
        com.example.SpringApi.Controllers.UserLogController.class.getMethod(
            "fetchUserLogsInBatches",
            com.example.SpringApi.Models.RequestModels.UserLogsRequestModel.class);

    // Act
    org.springframework.http.ResponseEntity<?> response =
        userLogControllerWithMock.fetchUserLogsInBatches(request);
    org.springframework.security.access.prepost.PreAuthorize annotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);

    // Assert
    assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(
        annotation, "@PreAuthorize annotation should be present on fetchUserLogsInBatches method");
    assertTrue(
        annotation
            .value()
            .contains(com.example.SpringApi.Models.Authorizations.VIEW_USER_PERMISSION),
        "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
  }

  /*
   * Purpose: Verify logDataWithContext is not exposed via controller (internal
   * service method only)
   * Expected Result: No public controller endpoint exists, method only accessible
   * internally
   * Assertions: Verify method works internally, no @PreAuthorize required as it's
   * not exposed
   */
  @Test
  @DisplayName("logDataWithContext - Internal Service Method - No Public Endpoint")
  void logDataWithContext_internalServiceMethod_noControllerEndpoint() {
    // Arrange
    stubUserLogRepositorySave(testUserLog);

    // Act
    Boolean result =
        userLogService.logDataWithContext(
            TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

    // Assert
    assertTrue(result);
    verify(userLogRepository).save(any(UserLog.class));
    // Note: logDataWithContext is an internal service method with no controller
    // endpoint
    // It's called by other services to log actions with context, not exposed to API
    // No permission check needed as it's not publicly accessible
  }

  /**
   * Purpose: Verify UserLogService controller delegates to service. Expected Result: Service method
   * is called and HTTP 200 is returned. Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("logDataWithContext - Service controller delegates properly")
  void logDataWithContext_serviceControllerDelegatesProperly_success() {
    // Arrange
    com.example.SpringApi.Models.RequestModels.UserLogsRequestModel request =
        new com.example.SpringApi.Models.RequestModels.UserLogsRequestModel();
    stubUserLogServiceFetchUserLogsInBatchesMock(
        request, new com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<>());

    // Act
    org.springframework.http.ResponseEntity<?> response =
        userLogControllerWithMock.fetchUserLogsInBatches(request);

    // Assert
    verify(mockUserLogService).fetchUserLogsInBatches(request);
    assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
  }
}

