package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserLogService Unit Tests")
class UserLogServiceTest {

    @Mock
    private UserLogRepository userLogRepository;

    @Mock
    private com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder userLogFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserLogService userLogService;

    private UserLog testUserLog;
    private UserLogsRequestModel testUserLogsRequest;
    private static final Long TEST_USER_ID = 1L;
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
        testUserLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, TEST_CHANGE, TEST_OLD_VALUE, TEST_NEW_VALUE, "admin");
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());

        // Initialize test user logs request model
        testUserLogsRequest = new UserLogsRequestModel();
        testUserLogsRequest.setUserId(TEST_USER_ID);
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

        // Mock UserLogService directly without authentication issues
        // Note: We avoid mocking getUserId() to prevent type conflicts
    }

    // ==================== Log Data with String User and Change/Old/New Values
    // Tests ====================

    /**
     * Test successful logging with string user ID and all values.
     * Verifies that user log is created and saved with proper validation.
     */
    @Test
    @DisplayName("Log Data - Success - With long user and all values")
    void logData_Success_LongUserWithAllValues() {
        // Arrange
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_CHANGE, TEST_OLD_VALUE, TEST_NEW_VALUE);

        // Assert
        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    // ==================== Log Data with Long User and Endpoint Tests
    // ====================

    /**
     * Test successful logging with long user ID and endpoint.
     * Verifies that user log is created with audit user ID when current user is
     * available.
     */
    @Test
    @DisplayName("Log Data - Success - With long user and endpoint")
    void logData_Success_LongUserWithEndpoint() {
        // Arrange
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        // Act
        Boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

        // Assert
        assertTrue(result);
        verify(userLogRepository, times(1)).save(any(UserLog.class));
    }

    /**
     * Test logging with long user ID and endpoint when no current user.
     * Verifies that audit user ID is set to the log user ID when current user is
     * null.
     */
    @Test
    @DisplayName("Log Data - Success - Long user ID with no authentication uses default user")
    void logData_Success_LongUserWithEndpoint_NoAuthentication() {
        // Arrange - Mock no authentication context and no Authorization header
        try (MockedStatic<SecurityContextHolder> mockStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockSecurityContext = mock(SecurityContext.class);
            when(mockSecurityContext.getAuthentication()).thenReturn(null);
            mockStatic.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            // Remove Authorization header to trigger default user scenario
            lenient().when(request.getHeader("Authorization")).thenReturn(null);

            // Act - This should now succeed with default user "admin"
            boolean result = userLogService.logData(TEST_USER_ID, TEST_NEW_VALUE, TEST_ENDPOINT);

            // Assert
            assertTrue(result);
            verify(userLogRepository, times(1)).save(any(UserLog.class));
        }
    }

    // ==================== Fetch User Logs In Batches Tests ====================

    /**
     * Test successful retrieval of user logs with pagination.
     * Verifies that paginated logs are returned with proper filtering.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - With pagination and filtering")
    void fetchUserLogsInBatches_Success() {
        // Arrange
        List<UserLog> userLogs = Arrays.asList(testUserLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);

        when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
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
        assertEquals("admin", result.getData().get(0).getCreatedUser());

        verify(userLogFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                anyList(),
                any(PageRequest.class));
    }

    /**
     * Test fetch user logs with invalid column name.
     * Verifies that IllegalArgumentException is thrown for invalid column names.
     */
    @Test
    @DisplayName("Fetch User Logs - Failure - Invalid column name")
    void fetchUserLogsInBatches_InvalidColumnName_ThrowsException() {
        // Arrange
        // Set up filters with invalid column
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("equals");
        invalidFilter.setValue("User Login");
        testUserLogsRequest.setFilters(List.of(invalidFilter));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException exception = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

        assertTrue(exception.getMessage().contains("Invalid column name: invalidColumn"));
        verify(userLogFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(anyLong(), anyLong(),
                anyString(), anyList(), any(PageRequest.class));
    }

    /**
     * Test fetch user logs with valid column names.
     * Verifies that each valid column name is accepted.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Valid column names")
    void fetchUserLogsInBatches_ValidColumnNames_Success() {
        // Arrange
        List<UserLog> userLogs = Arrays.asList(testUserLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);

        // Mock column types for all columns used in tests
        when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
        when(userLogFilterQueryBuilder.getColumnType("description")).thenReturn("string");
        when(userLogFilterQueryBuilder.getColumnType("logLevel")).thenReturn("string");

        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), anyLong(), anyString(),
                anyList(), any(PageRequest.class)))
                .thenReturn(page);

        String[] validColumns = { "action", "description", "logLevel" };

        for (String column : validColumns) {
            // Arrange
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn(column);
            filter.setOperator("equals");
            filter.setValue("User Login");
            testUserLogsRequest.setFilters(List.of(filter));

            // Act
            PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                    .fetchUserLogsInBatches(testUserLogsRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }

        verify(userLogFilterQueryBuilder, times(validColumns.length)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class));
    }

    /**
     * Test fetch user logs without column name filtering.
     * Verifies that logs are retrieved when no column filtering is applied.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Without column filtering")
    void fetchUserLogsInBatches_Success_WithoutColumnFiltering() {
        // Arrange
        // Clear filters for no column filtering
        testUserLogsRequest.setFilters(null);

        List<UserLog> userLogs = Arrays.asList(testUserLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);

        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                isNull(),
                any(PageRequest.class))).thenReturn(page);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                .fetchUserLogsInBatches(testUserLogsRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());

        verify(userLogFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                isNull(),
                any(PageRequest.class));
    }

    /**
     * Test fetch user logs with empty result set.
     * Verifies that empty pagination response is returned when no logs found.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Empty result set")
    void fetchUserLogsInBatches_EmptyResult_ReturnsEmptyPagination() {
        // Arrange
        Page<UserLog> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
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

        verify(userLogFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                anyList(),
                any(PageRequest.class));
    }

    /**
     * Test fetch user logs with multiple results.
     * Verifies that multiple logs are properly converted to response models.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Multiple results")
    void fetchUserLogsInBatches_MultipleResults_Success() {
        // Arrange
        UserLog secondLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, "User Logout", null, "logout_success", "admin");
        secondLog.setLogId(2L);

        List<UserLog> userLogs = Arrays.asList(testUserLog, secondLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 2);

        when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                anyList(),
                any(PageRequest.class))).thenReturn(page);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                .fetchUserLogsInBatches(testUserLogsRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(2L, result.getTotalDataCount());
        assertEquals(1L, result.getData().get(0).getLogId());
        assertEquals(2L, result.getData().get(1).getLogId());
        assertEquals(TEST_CHANGE, result.getData().get(0).getAction());
        assertEquals("User Logout", result.getData().get(1).getAction());

        verify(userLogFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                anyList(),
                any(PageRequest.class));
    }

    // ==================== Additional Validation Tests ====================

    /**
     * Test fetch user logs with invalid pagination parameters.
     * Verifies that BadRequestException is thrown when end <= start.
     */
    @Test
    @DisplayName("Fetch User Logs - Failure - Invalid pagination (end <= start)")
    void fetchUserLogsInBatches_InvalidPagination_ThrowsBadRequestException() {
        // Arrange
        testUserLogsRequest.setStart(10);
        testUserLogsRequest.setEnd(5);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

        assertNotNull(exception.getMessage());
    }

    /**
     * Test fetch user logs with invalid logic operator.
     * Verifies that BadRequestException is thrown for invalid logic operator.
     */
    @Test
    @DisplayName("Fetch User Logs - Failure - Invalid logic operator")
    void fetchUserLogsInBatches_InvalidLogicOperator_ThrowsBadRequestException() {
        // Arrange
        testUserLogsRequest.setLogicOperator("INVALID");
        when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest));

        assertTrue(exception.getMessage().contains("Invalid logic operator"));
    }

    /**
     * Test log data with null endpoint.
     * Verifies that logging succeeds with null endpoint.
     */
    @Test
    @DisplayName("Log Data - Success - Null endpoint")
    void logData_Success_NullEndpoint() {
        // Arrange
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, TEST_CHANGE, null);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Test log data with empty action string.
     * Verifies that logging succeeds with empty action.
     */
    @Test
    @DisplayName("Log Data - Success - Empty action")
    void logData_Success_EmptyAction() {
        // Arrange
        when(userLogRepository.save(any(UserLog.class))).thenReturn(testUserLog);

        // Act
        boolean result = userLogService.logData(TEST_USER_ID, "", TEST_ENDPOINT);

        // Assert
        assertTrue(result);
        verify(userLogRepository).save(any(UserLog.class));
    }

    /**
     * Test fetch user logs with null filters list.
     * Verifies that logging succeeds without filters.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Null filters")
    void fetchUserLogsInBatches_NullFilters_Success() {
        // Arrange
        testUserLogsRequest.setFilters(null);
        List<UserLog> userLogs = Arrays.asList(testUserLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);

        when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_USER_ID),
                eq(TEST_CARRIER_ID),
                eq("AND"),
                isNull(),
                any(PageRequest.class))).thenReturn(page);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                .fetchUserLogsInBatches(testUserLogsRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    // ==================== Additional LogData Tests ====================

    @Test
    @DisplayName("Log Data - Null User ID - Throws BadRequestException")
    void logData_NullUserId_ThrowsBadRequestException() {
        testLogDataRequest.setUserId(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidUserId, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Negative User ID - Throws BadRequestException")
    void logData_NegativeUserId_ThrowsBadRequestException() {
        testLogDataRequest.setUserId(-1L);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidUserId, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Zero User ID - Throws BadRequestException")
    void logData_ZeroUserId_ThrowsBadRequestException() {
        testLogDataRequest.setUserId(0L);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidUserId, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Null Action - Throws BadRequestException")
    void logData_NullAction_ThrowsBadRequestException() {
        testLogDataRequest.setAction(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidAction, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Empty Action - Throws BadRequestException")
    void logData_EmptyAction_ThrowsBadRequestException() {
        testLogDataRequest.setAction("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidAction, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Null Details - Throws BadRequestException")
    void logData_NullDetails_ThrowsBadRequestException() {
        testLogDataRequest.setDetails(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(testLogDataRequest));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidDetails, ex.getMessage());
    }

    @Test
    @DisplayName("Log Data - Null Request Object - Throws BadRequestException")
    void logData_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.logData(null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidRequest, ex.getMessage());
    }

    // ==================== Additional FetchUserLogs Tests ====================

    @Test
    @DisplayName("Fetch User Logs - Negative User ID - Throws BadRequestException")
    void fetchUserLogs_NegativeUserId_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(-1L, 0, 10, null, null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidUserId, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Zero User ID - Throws BadRequestException")
    void fetchUserLogs_ZeroUserId_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(0L, 0, 10, null, null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidUserId, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Negative Start Index - Throws BadRequestException")
    void fetchUserLogs_NegativeStartIndex_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(TEST_USER_ID, -1, 10, null, null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidStartIndex, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Negative Page Size - Throws BadRequestException")
    void fetchUserLogs_NegativePageSize_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(TEST_USER_ID, 0, -1, null, null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidPageSize, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Invalid Sort Column - Throws BadRequestException")
    void fetchUserLogs_InvalidSortColumn_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(TEST_USER_ID, 0, 10, "invalidColumn", null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.InvalidSortColumn, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Large Page Size (1000) - Throws BadRequestException")
    void fetchUserLogs_LargePageSize_ThrowsBadRequestException() {
        when(userLogRepository.findByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userLogService.fetchUserLogs(TEST_USER_ID, 0, 1000, null, null, null));
        assertEquals(ErrorMessages.UserLogErrorMessages.PageSizeTooLarge, ex.getMessage());
    }

    @Test
    @DisplayName("Fetch User Logs - Empty User Logs - Returns Empty List")
    void fetchUserLogs_EmptyUserLogs_ReturnsEmptyList() {
        when(userLogRepository.findByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        List<UserLog> logs = userLogService.fetchUserLogs(TEST_USER_ID, 0, 10, null, null, null);
        assertTrue(logs.isEmpty());
    }

    // ==================== Additional GetUserLog Tests ====================

    @Test
    @DisplayName("Get User Log - Negative Log ID - Not Found")
    void getUserLog_NegativeLogId_ThrowsNotFoundException() {
        when(userLogRepository.findById(-1L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userLogService.getUserLog(-1L));
        assertEquals(ErrorMessages.UserLogErrorMessages.LogNotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get User Log - Zero Log ID - Not Found")
    void getUserLog_ZeroLogId_ThrowsNotFoundException() {
        when(userLogRepository.findById(0L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userLogService.getUserLog(0L));
        assertEquals(ErrorMessages.UserLogErrorMessages.LogNotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get User Log - Long.MAX_VALUE Log ID - Not Found")
    void getUserLog_MaxLongLogId_ThrowsNotFoundException() {
        when(userLogRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userLogService.getUserLog(Long.MAX_VALUE));
        assertEquals(ErrorMessages.UserLogErrorMessages.LogNotFound, ex.getMessage());
    }
}
