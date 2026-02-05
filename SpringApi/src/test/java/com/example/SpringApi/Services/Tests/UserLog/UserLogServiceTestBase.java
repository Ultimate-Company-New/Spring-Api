package com.example.SpringApi.Services.Tests.UserLog;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserLogService tests.
 * Contains common mocks, test data, and setup methods.
 */
@ExtendWith(MockitoExtension.class)
public abstract class UserLogServiceTestBase extends BaseTest {

    @Mock
    protected UserLogRepository userLogRepository;

    @Mock
    protected UserLogFilterQueryBuilder userLogFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected UserLogService userLogService;

    protected UserLog testUserLog;
    protected UserLogsRequestModel testUserLogsRequest;
    
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_CARRIER_ID = 100L;
    protected static final String TEST_ACTION = "User Login";
    protected static final String TEST_OLD_VALUE = "old_value";
    protected static final String TEST_NEW_VALUE = "new_value";
    protected static final String TEST_ENDPOINT = "loginUser";

    // Valid columns for filtering
    protected static final String[] STRING_COLUMNS = {"action", "description", "ipAddress", "userAgent", 
            "sessionId", "logLevel", "createdUser", "modifiedUser", "notes", "change", "newValue", "oldValue"};
    protected static final String[] NUMBER_COLUMNS = {"logId", "userId", "clientId", "auditUserId"};
    
    // Valid operators
    protected static final String[] STRING_OPERATORS = {"equals", "contains", "startsWith", "endsWith"};
    protected static final String[] NUMBER_OPERATORS = {"equals", ">", ">=", "<", "<="};

    @BeforeEach
    protected void setUp() {
        testUserLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, TEST_ACTION, TEST_OLD_VALUE, TEST_NEW_VALUE, "admin");
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }
}
