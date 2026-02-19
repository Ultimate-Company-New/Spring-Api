package com.example.SpringApi.ServiceTests.UserLog;

import com.example.SpringApi.Controllers.UserLogController;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserLogService tests.
 * Contains common mocks, test data, and setup methods.
 */
@ExtendWith(MockitoExtension.class)
abstract class UserLogServiceTestBase {

    @Mock
    protected UserLogRepository userLogRepository;

    @Mock
    protected UserLogFilterQueryBuilder userLogFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected UserLogService userLogService;

    // For controller delegation tests
    @Mock
    protected UserLogService mockUserLogService;

    protected UserLogController userLogController;
    protected UserLogController userLogControllerWithMock;

    protected UserLog testUserLog;
    protected UserLogsRequestModel testUserLogsRequest;

    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_CARRIER_ID = 100L;
    protected static final String TEST_ACTION = "User Login";
    protected static final String TEST_OLD_VALUE = "old_value";
    protected static final String TEST_NEW_VALUE = "new_value";
    protected static final String TEST_ENDPOINT = "loginUser";

    // Valid columns for filtering
    protected static final String[] STRING_COLUMNS = { "action", "description", "ipAddress", "userAgent",
            "sessionId", "logLevel", "createdUser", "modifiedUser", "notes", "change", "newValue", "oldValue" };
    protected static final String[] NUMBER_COLUMNS = { "logId", "userId", "clientId", "auditUserId" };

    // Valid operators
    protected static final String[] STRING_OPERATORS = { "equals", "contains", "startsWith", "endsWith" };
    protected static final String[] NUMBER_OPERATORS = { "equals", ">", ">=", "<", "<=" };

    @BeforeEach
    protected void setUp() {
        stubAuthorizationHeader();

        userLogController = new UserLogController(userLogService);
        userLogControllerWithMock = new UserLogController(mockUserLogService);

        testUserLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE, "admin");
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());
    }

    protected void stubAuthorizationHeader() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    // ==========================================
    // STUBS
    // ==========================================

    protected void stubUserLogRepositorySave(UserLog returnLog) {
        lenient().when(userLogRepository.save(any(UserLog.class))).thenReturn(returnLog);
    }

    protected void stubUserLogRepositorySaveThrows(RuntimeException exception) {
        lenient().when(userLogRepository.save(any(UserLog.class))).thenThrow(exception);
    }

    protected void stubUserLogFilterQueryBuilderGetColumnType(String column, String returnType) {
        lenient().when(userLogFilterQueryBuilder.getColumnType(column)).thenReturn(returnType);
    }

    protected void stubUserLogFilterQueryBuilderGetColumnTypeAny(String returnType) {
        lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn(returnType);
    }

    protected void stubUserLogFilterQueryBuilderFindPaginatedEntities(Page<UserLog> page) {
        lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                any(), any(), any(), any(), any())).thenReturn(page);
    }

    protected void stubUserLogServiceFetchUserLogsInBatches(UserLogService mockService, UserLogsRequestModel req,
            PaginationBaseResponseModel<UserLogsResponseModel> res) {
        lenient().when(mockService.fetchUserLogsInBatches(req)).thenReturn(res);
    }

    protected void stubUserLogServiceFetchUserLogsInBatchesMock(UserLogsRequestModel req,
            PaginationBaseResponseModel<UserLogsResponseModel> res) {
        lenient().when(mockUserLogService.fetchUserLogsInBatches(req)).thenReturn(res);
    }

    protected void stubServiceThrowsUnauthorizedException() {
        lenient().when(mockUserLogService.fetchUserLogsInBatches(any()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException("Unauthorized"));
    }

    protected void stubUserLogServiceLogDataThrowsUnauthorized() {
        lenient().when(mockUserLogService.logData(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                        com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED));
        lenient().when(mockUserLogService.logData(anyLong(), anyString(), anyString()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                        com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED));
    }
}
