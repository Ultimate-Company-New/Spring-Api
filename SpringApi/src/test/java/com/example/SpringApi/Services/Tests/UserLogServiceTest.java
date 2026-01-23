package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.
 * 
 * This test class provides comprehensive coverage of UserLogService methods
 * including:
 * - Data logging operations with different parameter combinations
 * - Pagination-based log retrieval with filtering
 * - Error handling for invalid parameters
 * - Column validation and filtering
 * - Audit user handling
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 * 
 * @author SpringApi Team
 * @version 2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserLogService Unit Tests")
class UserLogServiceTest extends BaseTest {

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
    private static final Long TEST_CARRIER_ID = 100L;
    private static final String TEST_CHANGE = "User Login";
    private static final String TEST_OLD_VALUE = "old_value";
    private static final String TEST_NEW_VALUE = "new_value";
    private static final String TEST_ENDPOINT = "loginUser";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test user log using constructor
        testUserLog = new UserLog(DEFAULT_USER_ID, TEST_CARRIER_ID, TEST_CHANGE, TEST_OLD_VALUE, TEST_NEW_VALUE, DEFAULT_CREATED_USER);
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());

        // Initialize test user logs request model
        testUserLogsRequest = new UserLogsRequestModel();
        testUserLogsRequest.setUserId(DEFAULT_USER_ID);
        testUserLogsRequest.setCarrierId(TEST_CARRIER_ID);
        testUserLogsRequest.setStart(0);
        testUserLogsRequest.setEnd(10);
        // Set up filters using new FilterCondition structure
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("action");
        filter.setOperator("equals");
        filter.setValue("User Login");
        testUserLogsRequest.setFilters(List.of(filter));
        testUserLogsRequest.setLogicOperator("AND");

        // Setup common mock behaviors with lenient mocking for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock BaseService behavior
        lenient().doReturn(DEFAULT_CLIENT_ID).when(userLogService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(userLogService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(userLogService).getUser();
    }

    // ==================== Log Data Tests ====================

    @Nested
    @DisplayName("logData Tests")
    class LogDataTests {

        /**
         * Test successful logging with user ID and all values.
         */
        @Test
        @DisplayName("Log Data - Success - All Values")
        void logData_Success_AllValues() {
            // Arrange
            when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

            // Act
            Boolean result = userLogService.logData(DEFAULT_USER_ID, TEST_CHANGE, TEST_OLD_VALUE, TEST_NEW_VALUE);

            // Assert
            assertTrue(result);
            verify(userLogRepository, times(1)).save(any(UserLog.class));
        }

        /**
         * Test successful logging with user ID and endpoint.
         */
        @Test
        @DisplayName("Log Data - Success - With Endpoint")
        void logData_Success_WithEndpoint() {
            // Arrange
            when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

            // Act
            Boolean result = userLogService.logData(DEFAULT_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

            // Assert
            assertTrue(result);
            verify(userLogRepository, times(1)).save(any(UserLog.class));
        }

        /**
         * Test log data with null endpoint.
         */
        @Test
        @DisplayName("Log Data - Success - Null Endpoint")
        void logData_Success_NullEndpoint() {
            // Arrange
            when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

            // Act
            boolean result = userLogService.logData(DEFAULT_USER_ID, TEST_CHANGE, null);

            // Assert
            assertTrue(result);
            verify(userLogRepository).save(any(UserLog.class));
        }

        /**
         * Test log data with explicit context.
         */
        @Test
        @DisplayName("Log Data - Success - With Explicit Context")
        void logDataWithContext_Success() {
            // Arrange
            when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

            // Act
            boolean result = userLogService.logDataWithContext(DEFAULT_USER_ID, DEFAULT_LOGIN_NAME, DEFAULT_CLIENT_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

            // Assert
            assertTrue(result);
            verify(userLogRepository).save(any(UserLog.class));
        }
    }

    // ==================== Fetch User Logs In Batches Tests ====================

    @Nested
    @DisplayName("fetchUserLogsInBatches Tests")
    class FetchUserLogsInBatchesTests {

        /**
         * Test successful retrieval of user logs with pagination.
         */
        @Test
        @DisplayName("Fetch User Logs - Success - With Pagination")
        void fetchUserLogsInBatches_Success() {
            // Arrange
            List<UserLog> userLogs = Arrays.asList(testUserLog);
            Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);

            when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
            when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    eq(DEFAULT_USER_ID),
                    eq(TEST_CARRIER_ID),
                    eq("AND"),
                    anyList(),
                    any(PageRequest.class))).thenReturn(page);

            // Act
            PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                    .fetchUserLogsInBatches(testUserLogsRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1L, result.getTotalDataCount());
            assertEquals(1L, result.getData().get(0).getLogId());
            assertEquals(TEST_CHANGE, result.getData().get(0).getAction());
            assertEquals(TEST_OLD_VALUE, result.getData().get(0).getDescription());
            assertEquals(DEFAULT_CREATED_USER, result.getData().get(0).getCreatedUser());

            verify(userLogFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                    eq(DEFAULT_USER_ID),
                    eq(TEST_CARRIER_ID),
                    eq("AND"),
                    anyList(),
                    any(PageRequest.class));
        }

        /**
         * Test fetch user logs with invalid column name.
         */
        @Test
        @DisplayName("Fetch User Logs - Failure - Invalid Column Name")
        void fetchUserLogsInBatches_InvalidColumnName_ThrowsBadRequestException() {
            // Arrange
            PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
            invalidFilter.setColumn("invalidColumn");
            invalidFilter.setOperator("equals");
            invalidFilter.setValue("User Login");
            testUserLogsRequest.setFilters(List.of(invalidFilter));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

            assertTrue(exception.getMessage().contains("Invalid column name: invalidColumn"));
            verify(userLogFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(anyLong(), anyLong(),
                    anyString(), anyList(), any(PageRequest.class));
        }

        /**
         * Test fetch user logs with invalid pagination parameters.
         */
        @Test
        @DisplayName("Fetch User Logs - Failure - Invalid Pagination")
        void fetchUserLogsInBatches_InvalidPagination_ThrowsBadRequestException() {
            // Arrange
            testUserLogsRequest.setStart(10);
            testUserLogsRequest.setEnd(5);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, exception.getMessage());
        }

        /**
         * Test fetch user logs with invalid logic operator.
         */
        @Test
        @DisplayName("Fetch User Logs - Failure - Invalid Logic Operator")
        void fetchUserLogsInBatches_InvalidLogicOperator_ThrowsBadRequestException() {
            // Arrange
            testUserLogsRequest.setLogicOperator("INVALID");
            when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

            assertTrue(exception.getMessage().contains("Invalid logic operator"));
        }

        /**
         * Test fetch user logs with empty result set.
         */
        @Test
        @DisplayName("Fetch User Logs - Success - Empty Result")
        void fetchUserLogsInBatches_EmptyResult_ReturnsEmptyPagination() {
            // Arrange
            Page<UserLog> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

            when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
            when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    eq(DEFAULT_USER_ID),
                    eq(TEST_CARRIER_ID),
                    eq("AND"),
                    anyList(),
                    any(PageRequest.class))).thenReturn(emptyPage);

            // Act
            PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                    .fetchUserLogsInBatches(testUserLogsRequest);

            // Assert
            assertNotNull(result);
            assertTrue(result.getData().isEmpty());
            assertEquals(0L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Fetch User Logs - Triple Loop Validation")
        void fetchUserLogsInBatches_TripleLoopValidation() {
            List<String> validColumns = Arrays.asList(
                "logId", "userId", "userName", "module", "action",
                "clientId", "createdAt", "description"
            );

            List<String> invalidColumns = Arrays.asList("invalidCol", "dropTable", "select");

            List<String> validOperators = Arrays.asList(
                "equals", "contains", "startsWith", "endsWith",
                "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
            );

            List<String> invalidOperators = Arrays.asList("invalidOp", "like");

            List<String> values = Arrays.asList("test", "123", "2023-01-01");

            Page<UserLog> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class)))
                    .thenReturn(emptyPage);
            lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            for (String column : validColumns) {
                for (String operator : validOperators) {
                    for (String value : values) {
                        UserLogsRequestModel req = new UserLogsRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        req.setUserId(DEFAULT_USER_ID);
                        req.setCarrierId(TEST_CARRIER_ID);

                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(Collections.singletonList(filter));
                        req.setLogicOperator("AND");

                        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(req));
                    }
                }
            }

            for (String column : invalidColumns) {
                UserLogsRequestModel req = new UserLogsRequestModel();
                req.setStart(0);
                req.setEnd(10);
                req.setUserId(DEFAULT_USER_ID);
                req.setCarrierId(TEST_CARRIER_ID);
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator("equals");
                filter.setValue("val");
                req.setFilters(Collections.singletonList(filter));
                req.setLogicOperator("AND");

                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(req));
                assertTrue(ex.getMessage().contains("Invalid column name"));
            }

            for (String operator : invalidOperators) {
                UserLogsRequestModel req = new UserLogsRequestModel();
                req.setStart(0);
                req.setEnd(10);
                req.setUserId(DEFAULT_USER_ID);
                req.setCarrierId(TEST_CARRIER_ID);
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn("action");
                filter.setOperator(operator);
                filter.setValue("val");
                req.setFilters(Collections.singletonList(filter));
                req.setLogicOperator("AND");

                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(req));
                assertTrue(ex.getMessage().contains("Invalid operator"));
            }
        }
    }
}
