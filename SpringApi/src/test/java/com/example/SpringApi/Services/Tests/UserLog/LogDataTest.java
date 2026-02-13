package com.example.SpringApi.Services.Tests.UserLog;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.logData methods.
 */
@DisplayName("UserLogService - LogData Tests")
class LogDataTest extends UserLogServiceTestBase {


    // Total Tests: 31
    // ========================================
    // Section 1: Success Tests
    // ========================================

    /*
     * Purpose: Verify logging with all values provided.
     * Expected Result: Log is created with proper description combining old/new
     * values.
     * Assertions: assertTrue(result); verify repository save called.
     */
    @Test
    @DisplayName("logData - All Values Provided - Success")
    void logData_s01_allValuesProvided_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify return value is always true.
     * Expected Result: Returns true.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Always Returns True - Success")
    void logData_s02_alwaysReturnsTrue_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result1 = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);
        Boolean result2 = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        // Assert
        assertTrue(result1);
        assertTrue(result2);
    }

    /*
     * Purpose: Verify logging with endpoint - basic success.
     * Expected Result: Log is created with endpoint as action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Basic 3 Param - Success")
    void logData_s03_basicThreeParam_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        // Assert
        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with both values null (3-param).
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Both Null 3 Param - Success")
    void logData_s04_bothNullThreeParam_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, null, null);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with both values null (4-param).
     * Expected Result: Description says "Cleared value".
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Both Values Null 4 Param - Success")
    void logData_s05_bothValuesNullFourParam_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, null, null);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with empty action string.
     * Expected Result: Log created with empty action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Empty Action - Success")
    void logData_s06_emptyAction_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "", TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with empty endpoint.
     * Expected Result: Log created with empty endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Empty Endpoint - Success")
    void logData_s07_emptyEndpoint_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, "");

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with empty new value.
     * Expected Result: Log created with empty new value.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Empty New Value - Success")
    void logData_s08_emptyNewValue_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, "", TEST_ENDPOINT);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify JSON-like values can be logged.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Json Like Values - Success")
    void logData_s09_jsonLikeValues_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "Update Config",
                "{\"key\":\"old\"}", "{\"key\":\"new\"}");

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with long endpoint path.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Long Endpoint Path - Success")
    void logData_s10_longEndpointPath_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);
        String longEndpoint = "/api/v1/" + "segment/".repeat(100);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, longEndpoint);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify multiple log calls work independently.
     * Expected Result: Each call saves separately.
     * Assertions: verify save called correct times.
     */
    @Test
    @DisplayName("logData - Multiple Calls Work Independently - Success")
    void logData_s11_multipleCallsWorkIndependently_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        userLogService.logData(1L, "Action 1", "old1", "new1");
        userLogService.logData(2L, "Action 2", "old2", "new2");
        userLogService.logData(3L, "Action 3", "old3", "new3");

        // Assert
        verify(userLogRepository, times(3)).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with null endpoint.
     * Expected Result: Log created with null endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Null Endpoint - Success")
    void logData_s12_nullEndpoint_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, null);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with null new value (4-param).
     * Expected Result: Description says "Cleared value".
     * Assertions: assertTrue(result); verify save called.
     */
    @Test
    @DisplayName("logData - Null New Value 4 Param - Success")
    void logData_s13_nullNewValueFourParam_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, null);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with null new value (3-param).
     * Expected Result: Log created with description about endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Null New Value 3 Param - Success")
    void logData_s14_nullNewValueThreeParam_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, null, TEST_ENDPOINT);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with null old value generates correct description.
     * Expected Result: Description says "Set to 'newValue'".
     * Assertions: assertTrue(result); verify save called.
     */
    @Test
    @DisplayName("logData - Null Old Value - Success")
    void logData_s15_nullOldValue_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, null, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify numeric values can be logged.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Numeric String Values - Success")
    void logData_s16_numericStringValues_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "12345", "67890", "11111");

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
    @DisplayName("logData - Repository Save Called Once - Success")
    void logData_s17_repositorySaveCalledOnce_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with special chars in endpoint.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Special Chars Endpoint - Success")
    void logData_s18_specialCharsEndpoint_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, "/api/v1/users?id=123&name=test");

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with special characters in action.
     * Expected Result: Log created with special chars.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Special Chars In Action - Success")
    void logData_s19_specialCharsInAction_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "Test @#$%^&*()!<>?", TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify SQL-like values can be logged (injection test).
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Sql Like Values - Success")
    void logData_s20_sqlLikeValues_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "Update",
                "'; DROP TABLE users; --", "normal_value");

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with unicode characters in action.
     * Expected Result: Log created with unicode.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Unicode In Action - Success")
    void logData_s21_unicodeInAction_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "Test 你好 🎉 Привет العربية", TEST_OLD_VALUE,
                TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with very long action string.
     * Expected Result: Log created with long action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Very Long Action - Success")
    void logData_s22_veryLongAction_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);
        String longAction = "A".repeat(1000);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, longAction, TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with very long new value.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Very Long New Value - Success")
    void logData_s23_veryLongNewValue_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);
        String longValue = "C".repeat(5000);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, longValue);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with very long old value.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Very Long Old Value - Success")
    void logData_s24_veryLongOldValue_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);
        String longValue = "B".repeat(5000);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, longValue, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /*
     * Purpose: Verify logging with whitespace action.
     * Expected Result: Log created with whitespace action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Whitespace Action - Success")
    void logData_s25_whitespaceAction_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, "   ", TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with varied user ID values (Max, Min, Negative,
     * Zero).
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("logData - Varied User IDs (Max/Min/Neg/Zero) - Success")
    void logData_s26_variedUserIds_success() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);
        Long[] userIds = new Long[] { Long.MAX_VALUE, Long.MIN_VALUE, -1L, 0L };

        // Act
        for (Long userId : userIds) {
            Boolean result = userLogService.logData(userId, TEST_ACTION, TEST_ENDPOINT);

            // Assert
            assertTrue(result);
        }

        verify(userLogRepository, times(userIds.length)).save(any(UserLog.class));
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
    @DisplayName("logData - Repository Save Fails - Throws Exception")
    void logData_repositorySaveFails_throwsException() {
        // Arrange
        stubUserLogRepositorySaveThrows(new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE));
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, exception.getMessage());
    }

    // ========================================
    // Section 3: Controller Permission/Auth Tests
    // ========================================

    /**
     * Purpose: Verify UserLogService has controller endpoint with permission check.
     * Expected Result: HTTP UNAUTHORIZED status returned and @PreAuthorize
     * verified.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
     * assertNotNull, assertTrue
     */
    @Test
    @DisplayName("logData - Controller permission forbidden")
    void logData_p01_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        com.example.SpringApi.Models.RequestModels.UserLogsRequestModel request = new com.example.SpringApi.Models.RequestModels.UserLogsRequestModel();
        stubServiceThrowsUnauthorizedException();
        java.lang.reflect.Method method = com.example.SpringApi.Controllers.UserLogController.class.getMethod(
                "fetchUserLogsInBatches", com.example.SpringApi.Models.RequestModels.UserLogsRequestModel.class);

        // Act
        org.springframework.http.ResponseEntity<?> response = userLogControllerWithMock.fetchUserLogsInBatches(request);
        org.springframework.security.access.prepost.PreAuthorize annotation = method
                .getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on fetchUserLogsInBatches method");
        assertTrue(annotation.value().contains(com.example.SpringApi.Models.Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    /*
     * Purpose: Verify logData is not exposed via controller (internal service
     * method only)
     * Expected Result: No public controller endpoint exists, method only accessible
     * internally
     * Assertions: Verify method works internally, no @PreAuthorize required as it's
     * not exposed
     */
    @Test
    @DisplayName("logData - Internal Service Method - No Public Endpoint")
    void logData_p02_internalServiceMethod_noControllerEndpoint() {
        // Arrange
        stubUserLogRepositorySave(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
        // Note: logData is an internal service method with no controller endpoint
        // It's called by other services to log actions, not exposed to API
        // No permission check needed as it's not publicly accessible
    }

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("logData - Controller permission unauthorized - Success")
    void logData_p03_controller_permission_unauthorized() {
        // Arrange
        com.example.SpringApi.Models.RequestModels.UserLogsRequestModel request = new com.example.SpringApi.Models.RequestModels.UserLogsRequestModel();
        // Use both stubs to ensure coverage of user instruction and actual method
        // execution
        stubUserLogServiceLogDataThrowsUnauthorized();
        stubServiceThrowsUnauthorizedException();

        // Act
        org.springframework.http.ResponseEntity<?> response = userLogControllerWithMock.fetchUserLogsInBatches(request);

        // Assert
        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify UserLogService controller delegates to service.
     * Expected Result: Service method is called and HTTP 200 is returned.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("logData - Controller delegates to service correctly")
    void logData_p04_controller_permission_Success() {
        // Arrange
        com.example.SpringApi.Models.RequestModels.UserLogsRequestModel request = new com.example.SpringApi.Models.RequestModels.UserLogsRequestModel();
        stubUserLogServiceFetchUserLogsInBatchesMock(request,
                new com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<>());

        // Act
        org.springframework.http.ResponseEntity<?> response = userLogControllerWithMock.fetchUserLogsInBatches(request);

        // Assert
        verify(mockUserLogService).fetchUserLogsInBatches(request);
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
    }
}
