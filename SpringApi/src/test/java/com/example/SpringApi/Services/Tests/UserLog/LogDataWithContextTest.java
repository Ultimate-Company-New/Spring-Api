package com.example.SpringApi.Services.Tests.UserLog;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.logDataWithContext method.
 * Tests: 21
 */
@DisplayName("UserLogService - LogDataWithContext Tests")
public class LogDataWithContextTest extends UserLogServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    /**
     * Purpose: Verify return value is always true.
     * Expected Result: Returns true.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Always returns true")
    void logDataWithContext_AlwaysReturnsTrue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
    }

    /**
     * Purpose: Verify logging with all context values null except userId.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - All null except userId")
    void logDataWithContext_AllNullExceptUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, null, null, null, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with different user context.
     * Expected Result: Log is created with different context.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Different user context")
    void logDataWithContext_DifferentUserContext() {
        Long differentUserId = 999L;
        String differentUsername = "different_user";
        Long differentClientId = 555L;
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(differentUserId, differentUsername, differentClientId, "test value", TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with empty username in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Empty username")
    void logDataWithContext_EmptyUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with max long client ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Max Long client ID")
    void logDataWithContext_MaxLongClientId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", Long.MAX_VALUE, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with max long user ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Max Long user ID")
    void logDataWithContext_MaxLongUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(Long.MAX_VALUE, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify multiple calls work independently.
     * Expected Result: Each call saves separately.
     * Assertions: verify save called correct times.
     */
    @Test
    @DisplayName("Log Data With Context - Multiple calls work independently")
    void logDataWithContext_MultipleCallsWorkIndependently() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        userLogService.logDataWithContext(1L, "user1", 100L, "value1", "endpoint1");
        userLogService.logDataWithContext(2L, "user2", 200L, "value2", "endpoint2");
        userLogService.logDataWithContext(3L, "user3", 300L, "value3", "endpoint3");

        verify(userLogRepository, times(3)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with negative client ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Negative client ID")
    void logDataWithContext_NegativeClientId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", -1L, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with negative user ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Negative user ID")
    void logDataWithContext_NegativeUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(-1L, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null client ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Null client ID")
    void logDataWithContext_NullClientId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", null, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null endpoint in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Null endpoint")
    void logDataWithContext_NullEndpoint() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, null);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null new value in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Null new value")
    void logDataWithContext_NullNewValue() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", TEST_CARRIER_ID, null, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with null username in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Null username")
    void logDataWithContext_NullUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, null, TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify repository save is called exactly once.
     * Expected Result: save() called once.
     * Assertions: verify(userLogRepository, times(1)).save(any()).
     */
    @Test
    @DisplayName("Log Data With Context - Repository save called once")
    void logDataWithContext_RepositorySaveCalledOnce() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        userLogService.logDataWithContext(TEST_USER_ID, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with special chars in username.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Special chars in username")
    void logDataWithContext_SpecialCharsUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "user@domain.com", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with explicit context - basic success.
     * Expected Result: Log is created with explicit context.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Success - Basic")
    void logDataWithContext_Success_Basic() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "testuser", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with unicode username.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Unicode username")
    void logDataWithContext_UnicodeUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "用户名", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with very long username.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Very long username")
    void logDataWithContext_VeryLongUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);
        String longUsername = "user_" + "x".repeat(500);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, longUsername, TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with whitespace username.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Whitespace username")
    void logDataWithContext_WhitespaceUsername() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "   ", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with zero client ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Zero client ID")
    void logDataWithContext_ZeroClientId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(TEST_USER_ID, "admin", 0L, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Purpose: Verify logging with zero user ID in context.
     * Expected Result: Log is created.
     * Assertions: assertTrue(result).
     */
    @Test
    @DisplayName("Log Data With Context - Zero user ID")
    void logDataWithContext_ZeroUserId() {
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        Boolean result = userLogService.logDataWithContext(0L, "admin", TEST_CARRIER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }
}
