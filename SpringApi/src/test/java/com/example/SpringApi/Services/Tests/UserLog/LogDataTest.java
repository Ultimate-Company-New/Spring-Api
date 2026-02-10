package com.example.SpringApi.Services.Tests.UserLog;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.logData methods.
 * Tests: 29
 */
@DisplayName("UserLogService - LogData Tests")
public class LogDataTest extends UserLogServiceTestBase {

    // Total Tests: 29

    // ========================================
    // SUCCESS Tests
    // ========================================

    /**
     * Purpose: Verify logging with all values provided.
     * Expected Result: Log is created with proper description combining old/new values.
     * Assertions: assertTrue(result); verify repository save called.
     */
    @Test
    @DisplayName("Log Data (4-param) - Success - All values provided")
    void logData_4Param_Success_AllValuesProvided() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with endpoint - basic success.
     * Expected Result: Log is created with endpoint as action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Success - Basic")
    void logData_3Param_Success_Basic() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify return value is always true.
     * Expected Result: Returns true.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Always returns true")
    void logData_AlwaysReturnsTrue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result1 = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);
        Boolean result2 = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result1);
        assertTrue(result2);
    }

    /**
     * Purpose: Verify logging with both values null.
     * Expected Result: Description says "Cleared value".
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Both values null")
    void logData_4Param_BothValuesNull() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, null, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with both null.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Both null")
    void logData_3Param_BothNull() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, null, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with empty action string.
     * Expected Result: Log created with empty action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Empty action")
    void logData_4Param_EmptyAction() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "", TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with empty endpoint.
     * Expected Result: Log created with empty endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Empty endpoint")
    void logData_3Param_EmptyEndpoint() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, "");

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with empty new value.
     * Expected Result: Log created with empty new value.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Empty new value")
    void logData_3Param_EmptyNewValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, "", TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify JSON-like values can be logged.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - JSON-like values")
    void logData_JsonLikeValues() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "Update Config", 
                "{\"key\":\"old\"}", "{\"key\":\"new\"}");

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with long endpoint path.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Long endpoint path")
    void logData_3Param_LongEndpointPath() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);
        String longEndpoint = "/api/v1/" + "segment/".repeat(100);

        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, longEndpoint);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with max long user ID.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Max Long user ID")
    void logData_MaxLongUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(Long.MAX_VALUE, TEST_ACTION, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with min long user ID.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Min Long user ID")
    void logData_MinLongUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(Long.MIN_VALUE, TEST_ACTION, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify multiple log calls work independently.
     * Expected Result: Each call saves separately.
     * Assertions: verify save called correct times.
     */
    @Test
    @DisplayName("Log Data - Multiple calls work independently")
    void logData_MultipleCallsWorkIndependently() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        userLogService.logData(1L, "Action 1", "old1", "new1");
        userLogService.logData(2L, "Action 2", "old2", "new2");
        userLogService.logData(3L, "Action 3", "old3", "new3");

        verify(userLogRepository, times(3)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with negative user ID.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Negative user ID")
    void logData_NegativeUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(-1L, TEST_ACTION, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null endpoint.
     * Expected Result: Log created with null endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Null endpoint")
    void logData_3Param_NullEndpoint() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null new value generates correct description.
     * Expected Result: Description says "Cleared value".
     * Assertions: assertTrue(result); verify save called.
     */
    @Test
    @DisplayName("Log Data (4-param) - Null new value - Clears value")
    void logData_4Param_NullNewValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null new value.
     * Expected Result: Log created with description about endpoint.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Null new value")
    void logData_3Param_NullNewValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, null, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null old value generates correct description.
     * Expected Result: Description says "Set to 'newValue'".
     * Assertions: assertTrue(result); verify save called.
     */
    @Test
    @DisplayName("Log Data (4-param) - Null old value - Sets description")
    void logData_4Param_NullOldValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, null, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify numeric values can be logged.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Numeric string values")
    void logData_NumericStringValues() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "12345", "67890", "11111");

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify repository save is called exactly once.
     * Expected Result: save() called once.
     * Assertions: verify(userLogRepository, times(1)).save(any()).
     */
    @Test
    @DisplayName("Log Data - Repository save called once")
    void logData_RepositorySaveCalledOnce() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE);

        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with special chars in endpoint.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (3-param) - Special chars in endpoint")
    void logData_3Param_SpecialCharsEndpoint() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, "/api/v1/users?id=123&name=test");

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with special characters in action.
     * Expected Result: Log created with special chars.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Special characters in action")
    void logData_4Param_SpecialCharsInAction() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "Test @#$%^&*()!<>?", TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify SQL-like values can be logged (injection test).
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - SQL-like values (injection test)")
    void logData_SqlLikeValues() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "Update", 
                "'; DROP TABLE users; --", "normal_value");

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with unicode characters in action.
     * Expected Result: Log created with unicode.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Unicode characters in action")
    void logData_4Param_UnicodeInAction() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "Test 你好 🎉 Привет العربية", TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with very long action string.
     * Expected Result: Log created with long action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Very long action string")
    void logData_4Param_VeryLongAction() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);
        String longAction = "A".repeat(1000);

        Boolean result = userLogService.logData(TEST_USER_ID, longAction, TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with very long new value.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Very long new value")
    void logData_4Param_VeryLongNewValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);
        String longValue = "C".repeat(5000);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, TEST_OLD_VALUE, longValue);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with very long old value.
     * Expected Result: Log created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Very long old value")
    void logData_4Param_VeryLongOldValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);
        String longValue = "B".repeat(5000);

        Boolean result = userLogService.logData(TEST_USER_ID, TEST_ACTION, longValue, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with whitespace action.
     * Expected Result: Log created with whitespace action.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data (4-param) - Whitespace action")
    void logData_4Param_WhitespaceAction() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(TEST_USER_ID, "   ", TEST_OLD_VALUE, TEST_NEW_VALUE);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with zero user ID.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data - Zero user ID")
    void logData_ZeroUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logData(0L, TEST_ACTION, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }
}
