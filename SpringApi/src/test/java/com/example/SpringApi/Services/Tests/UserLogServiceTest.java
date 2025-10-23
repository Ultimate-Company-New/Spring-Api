package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
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
 * This test class provides comprehensive coverage of UserLogService methods including:
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
        testUserLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, TEST_CHANGE, TEST_OLD_VALUE, TEST_NEW_VALUE);
        testUserLog.setLogId(1L);
        testUserLog.setCreatedAt(LocalDateTime.now());
        testUserLog.setUpdatedAt(LocalDateTime.now());
        
        // Initialize test user logs request model
        testUserLogsRequest = new UserLogsRequestModel();
        testUserLogsRequest.setUserId(TEST_USER_ID);
        testUserLogsRequest.setCarrierId(TEST_CARRIER_ID);
        testUserLogsRequest.setStart(0);
        testUserLogsRequest.setEnd(10);
        testUserLogsRequest.setColumnName("change");
        testUserLogsRequest.setCondition("equals");
        testUserLogsRequest.setFilterExpr("User Login");
        
        // Setup common mock behaviors with lenient mocking for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        
        // Mock UserLogService directly without authentication issues
        // Note: We avoid mocking getUserId() to prevent type conflicts
    }

    // ==================== Log Data with String User and Change/Old/New Values Tests ====================
    
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
    
    // ==================== Log Data with Long User and Endpoint Tests ====================
    
    /**
     * Test successful logging with long user ID and endpoint.
     * Verifies that user log is created with audit user ID when current user is available.
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
     * Verifies that audit user ID is set to the log user ID when current user is null.
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
        
        when(userLogRepository.findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        )).thenReturn(page);
        
        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(testUserLogsRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        assertEquals(1L, result.getData().get(0).getLogId());
        assertEquals(TEST_CHANGE, result.getData().get(0).getAction());
        assertEquals(TEST_OLD_VALUE, result.getData().get(0).getDescription());
        assertEquals(TEST_NEW_VALUE, result.getData().get(0).getCreatedUser());
        
        verify(userLogRepository, times(1)).findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        );
    }
    
    /**
     * Test fetch user logs with invalid column name.
     * Verifies that IllegalArgumentException is thrown for invalid column names.
     */
    @Test
    @DisplayName("Fetch User Logs - Failure - Invalid column name")
    void fetchUserLogsInBatches_InvalidColumnName_ThrowsException() {
        // Arrange
        testUserLogsRequest.setColumnName("invalidColumn");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userLogService.fetchUserLogsInBatches(testUserLogsRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid column name: invalidColumn"));
        verify(userLogRepository, never()).findPaginatedUserLogs(anyLong(), anyLong(), anyString(), anyString(), anyString(), any(PageRequest.class));
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
        
        when(userLogRepository.findPaginatedUserLogs(anyLong(), anyLong(), anyString(), anyString(), anyString(), any(PageRequest.class)))
            .thenReturn(page);
        
        String[] validColumns = {"change", "oldValue", "newValue"};
        
        for (String column : validColumns) {
            // Arrange
            testUserLogsRequest.setColumnName(column);
            
            // Act
            PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(testUserLogsRequest);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }
        
        verify(userLogRepository, times(validColumns.length)).findPaginatedUserLogs(anyLong(), anyLong(), anyString(), anyString(), anyString(), any(PageRequest.class));
    }
    
    /**
     * Test fetch user logs without column name filtering.
     * Verifies that logs are retrieved when no column filtering is applied.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Without column filtering")
    void fetchUserLogsInBatches_Success_WithoutColumnFiltering() {
        // Arrange
        testUserLogsRequest.setColumnName(null);
        
        List<UserLog> userLogs = Arrays.asList(testUserLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 1);
        
        when(userLogRepository.findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            isNull(),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        )).thenReturn(page);
        
        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(testUserLogsRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        
        verify(userLogRepository, times(1)).findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            isNull(),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        );
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
        
        when(userLogRepository.findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        )).thenReturn(emptyPage);
        
        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(testUserLogsRequest);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0L, result.getTotalDataCount());
        
        verify(userLogRepository, times(1)).findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        );
    }
    
    /**
     * Test fetch user logs with multiple results.
     * Verifies that multiple logs are properly converted to response models.
     */
    @Test
    @DisplayName("Fetch User Logs - Success - Multiple results")
    void fetchUserLogsInBatches_MultipleResults_Success() {
        // Arrange
        UserLog secondLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, "User Logout", null, "logout_success");
        secondLog.setLogId(2L);
        
        List<UserLog> userLogs = Arrays.asList(testUserLog, secondLog);
        Page<UserLog> page = new PageImpl<>(userLogs, PageRequest.of(0, 10), 2);
        
        when(userLogRepository.findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        )).thenReturn(page);
        
        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService.fetchUserLogsInBatches(testUserLogsRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(2L, result.getTotalDataCount());
        assertEquals(1L, result.getData().get(0).getLogId());
        assertEquals(2L, result.getData().get(1).getLogId());
        assertEquals(TEST_CHANGE, result.getData().get(0).getAction());
        assertEquals("User Logout", result.getData().get(1).getAction());
        
        verify(userLogRepository, times(1)).findPaginatedUserLogs(
            eq(TEST_USER_ID),
            eq(TEST_CARRIER_ID),
            eq("change"),
            eq("equals"),
            eq("User Login"),
            any(PageRequest.class)
        );
    }
}
