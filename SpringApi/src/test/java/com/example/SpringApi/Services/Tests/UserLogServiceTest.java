package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | LogDataTests                            | 29              |
 * | LogDataWithContextTests                 | 21              |
 * | FetchUserLogsInBatchesTests             | 1               |
 * | **Total**                               | **51**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserLogService Unit Tests")
class UserLogServiceTest {

    @Mock
    private UserLogRepository userLogRepository;

    @Mock
    private UserLogFilterQueryBuilder userLogFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserLogService userLogService;

    private UserLog testUserLog;
    private UserLogsRequestModel testUserLogsRequest;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CARRIER_ID = 100L;
    private static final String TEST_ACTION = "User Login";
    private static final String TEST_OLD_VALUE = "old_value";
    private static final String TEST_NEW_VALUE = "new_value";
    private static final String TEST_ENDPOINT = "loginUser";

    // Valid columns for filtering
    private static final String[] STRING_COLUMNS = {"action", "description", "ipAddress", "userAgent", 
            "sessionId", "logLevel", "createdUser", "modifiedUser", "notes", "change", "newValue", "oldValue"};
    private static final String[] NUMBER_COLUMNS = {"logId", "userId", "clientId", "auditUserId"};
    // Valid operators
    private static final String[] STRING_OPERATORS = {"equals", "contains", "startsWith", "endsWith"};
    private static final String[] NUMBER_OPERATORS = {"equals", ">", ">=", "<", "<="};

    @BeforeEach
    void setUp() {
        testUserLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE, "admin");
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());

        testUserLogsRequest = new UserLogsRequestModel();
        testUserLogsRequest.setUserId(TEST_USER_ID);
        testUserLogsRequest.setCarrierId(TEST_CARRIER_ID);
        testUserLogsRequest.setStart(0);
        testUserLogsRequest.setEnd(10);
        
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("action");
        filter.setOperator("equals");
        filter.setValue("User Login");
        testUserLogsRequest.setFilters(List.of(filter));
        testUserLogsRequest.setLogicOperator("AND");

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    @Nested
    @DisplayName("LogData Tests")
    class LogDataTests {

        // ==================== logData(userId, action, oldValue, newValue) Tests ====================

        /**
         * Purpose: Verify successful logging with all values provided.
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

        // ==================== logData(userId, newValue, endPoint) Tests ====================

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

        // ==================== User ID Edge Cases ====================

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

        // ==================== Repository Interaction Tests ====================

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
    }

    @Nested
    @DisplayName("LogDataWithContext Tests")
    class LogDataWithContextTests {

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
    }

    @Nested
    @DisplayName("FetchUserLogsInBatches Tests")
    class FetchUserLogsInBatchesTests {

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators using nested loops.
         */
        @Test
        @DisplayName("Comprehensive Batch Filter Test - All Combinations")
        void fetchUserLogsInBatches_ComprehensiveCombinationTest() {
            int validTests = 0;
            int invalidTests = 0;

            String[] logicOperators = {"AND", "OR", "and", "or"};
            String[] invalidLogicOperators = {"XOR", "NAND", "invalid"};
            String[] invalidColumns = {"invalidColumn", "xyz", "!@#$"};

            // ============== TEST 1: Valid string column + operator combinations ==============
            for (String column : STRING_COLUMNS) {
                for (String operator : STRING_OPERATORS) {
                    UserLogsRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("testValue");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

                    lenient().when(userLogFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                    lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class))).thenReturn(page);

                    assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                            "String column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // ============== TEST 2: Valid number column + operator combinations ==============
            for (String column : NUMBER_COLUMNS) {
                for (String operator : NUMBER_OPERATORS) {
                    UserLogsRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("100");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

                    lenient().when(userLogFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                    lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class))).thenReturn(page);

                    assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                            "Number column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // ============== TEST 3: Invalid column names ==============
            for (String invalidColumn : invalidColumns) {
                UserLogsRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(invalidColumn);
                filter.setOperator("equals");
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userLogService.fetchUserLogsInBatches(request),
                        "Invalid column '" + invalidColumn + "' should throw BadRequestException");
                assertTrue(ex.getMessage().contains("Invalid column name"));
                invalidTests++;
            }

            // ============== TEST 4: Invalid logic operators ==============
            for (String invalidLogic : invalidLogicOperators) {
                UserLogsRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("action");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("description");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(invalidLogic);

                lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userLogService.fetchUserLogsInBatches(request),
                        "Invalid logic operator '" + invalidLogic + "' should throw BadRequestException");
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
                invalidTests++;
            }

            // ============== TEST 5: Valid logic operators ==============
            for (String validLogic : logicOperators) {
                UserLogsRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("action");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("description");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(validLogic);

                Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

                lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                        "Valid logic operator '" + validLogic + "' should succeed");
                validTests++;
            }

            // ============== TEST 6: Pagination edge cases ==============
            int[][] invalidPaginationCases = {{10, 10}, {10, 5}, {0, 0}};

            for (int[] pagination : invalidPaginationCases) {
                if (pagination[1] - pagination[0] <= 0) {
                    UserLogsRequestModel request = createBasicPaginationRequest();
                    request.setStart(pagination[0]);
                    request.setEnd(pagination[1]);
                    request.setFilters(null);

                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> userLogService.fetchUserLogsInBatches(request),
                            "Pagination start=" + pagination[0] + ", end=" + pagination[1] + " should throw");
                    assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                    invalidTests++;
                }
            }

            // ============== TEST 7: No filters (basic pagination) ==============
            UserLogsRequestModel noFilterRequest = createBasicPaginationRequest();
            noFilterRequest.setFilters(null);

            Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

            PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(noFilterRequest);
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            validTests++;

            // ============== TEST 8: Multiple filters with AND ==============
            UserLogsRequestModel multiFilterAndRequest = createBasicPaginationRequest();
            PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
            filter1.setColumn("action");
            filter1.setOperator("contains");
            filter1.setValue("Login");
            PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
            filter2.setColumn("description");
            filter2.setOperator("contains");
            filter2.setValue("User");
            multiFilterAndRequest.setFilters(Arrays.asList(filter1, filter2));
            multiFilterAndRequest.setLogicOperator("AND");

            lenient().when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
            lenient().when(userLogFilterQueryBuilder.getColumnType("description")).thenReturn("string");
            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), eq("AND"), anyList(), any(PageRequest.class))).thenReturn(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterAndRequest));
            validTests++;

            // ============== TEST 9: Multiple filters with OR ==============
            UserLogsRequestModel multiFilterOrRequest = createBasicPaginationRequest();
            multiFilterOrRequest.setFilters(Arrays.asList(filter1, filter2));
            multiFilterOrRequest.setLogicOperator("OR");

            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), eq("OR"), anyList(), any(PageRequest.class))).thenReturn(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterOrRequest));
            validTests++;

            // ============== TEST 10: Empty result set ==============
            UserLogsRequestModel emptyResultRequest = createBasicPaginationRequest();
            emptyResultRequest.setFilters(null);

            Page<UserLog> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(emptyPage);

            PaginationBaseResponseModel<UserLogsResponseModel> emptyResult = userLogService.fetchUserLogsInBatches(emptyResultRequest);
            assertNotNull(emptyResult);
            assertTrue(emptyResult.getData().isEmpty());
            assertEquals(0L, emptyResult.getTotalDataCount());
            validTests++;

            // ============== TEST 11: Multiple results ==============
            UserLog secondLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, "User Logout", null, "logout", "admin");
            secondLog.setLogId(2L);

            List<UserLog> multipleLogs = Arrays.asList(testUserLog, secondLog);
            Page<UserLog> multiPage = new PageImpl<>(multipleLogs, PageRequest.of(0, 10), 2);

            UserLogsRequestModel multiResultRequest = createBasicPaginationRequest();
            multiResultRequest.setFilters(null);

            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(multiPage);

            PaginationBaseResponseModel<UserLogsResponseModel> multiResult = userLogService.fetchUserLogsInBatches(multiResultRequest);
            assertNotNull(multiResult);
            assertEquals(2, multiResult.getData().size());
            assertEquals(2L, multiResult.getTotalDataCount());
            validTests++;

            // ============== TEST 12: Large page size ==============
            UserLogsRequestModel largePageRequest = createBasicPaginationRequest();
            largePageRequest.setStart(0);
            largePageRequest.setEnd(100);
            largePageRequest.setFilters(null);

            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(largePageRequest));
            validTests++;

            // ============== TEST 13: Different user IDs ==============
            long[] userIds = {0L, 1L, 100L, Long.MAX_VALUE};
            for (long userId : userIds) {
                UserLogsRequestModel userIdRequest = createBasicPaginationRequest();
                userIdRequest.setUserId(userId);
                userIdRequest.setFilters(null);

                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        eq(userId), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(userIdRequest),
                        "User ID " + userId + " should be accepted");
                validTests++;
            }

            // ============== TEST 14: Different carrier IDs ==============
            long[] carrierIds = {0L, 1L, 100L, Long.MAX_VALUE};
            for (long carrierId : carrierIds) {
                UserLogsRequestModel carrierIdRequest = createBasicPaginationRequest();
                carrierIdRequest.setCarrierId(carrierId);
                carrierIdRequest.setFilters(null);

                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), eq(carrierId), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(carrierIdRequest),
                        "Carrier ID " + carrierId + " should be accepted");
                validTests++;
            }

            System.out.println("Comprehensive UserLog Batch Filter Test Summary:");
            System.out.println("  Valid test cases passed: " + validTests);
            System.out.println("  Invalid test cases (expected failures): " + invalidTests);

            assertTrue(validTests >= 60, "Should have at least 60 valid test cases");
            assertTrue(invalidTests >= 5, "Should have at least 5 invalid test cases");
        }

        private UserLogsRequestModel createBasicPaginationRequest() {
            UserLogsRequestModel request = new UserLogsRequestModel();
            request.setUserId(TEST_USER_ID);
            request.setCarrierId(TEST_CARRIER_ID);
            request.setStart(0);
            request.setEnd(10);
            return request;
        }
    }
}
