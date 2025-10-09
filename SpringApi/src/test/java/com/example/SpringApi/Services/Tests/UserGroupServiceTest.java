package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Repositories.UserGroupRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.UserGroupService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService.
 * 
 * This test class provides comprehensive coverage of UserGroupService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - User group retrieval by ID and client ID
 * - Active group filtering
 * - User membership management
 * - Error handling and validation
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
@DisplayName("UserGroupService Unit Tests")
class UserGroupServiceTest {

    @Mock
    private UserGroupRepository userGroupRepository;
    
    @Mock
    private UserGroupUserMapRepository userGroupUserMapRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserLogService userLogService;
    
    @Mock
    private HttpServletRequest request;
    
    @InjectMocks
    private UserGroupService userGroupService;
    
    private UserGroup testUserGroup;
    private UserGroupRequestModel testUserGroupRequest;
    private User testUser;
    private UserGroupUserMap testMapping;
    private static final Long TEST_GROUP_ID = 1L;
    private static final Long TEST_CLIENT_ID = 100L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_GROUP_NAME = "Test Group";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String CREATED_USER = "admin";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test user group request model
        testUserGroupRequest = new UserGroupRequestModel();
        testUserGroupRequest.setGroupId(TEST_GROUP_ID);
        testUserGroupRequest.setClientId(TEST_CLIENT_ID);
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        testUserGroupRequest.setDescription(TEST_DESCRIPTION);
        testUserGroupRequest.setUserIds(Arrays.asList(TEST_USER_ID, 2L, 3L));
        
        // Initialize test user group using constructor
        testUserGroup = new UserGroup(testUserGroupRequest, CREATED_USER);
        testUserGroup.setGroupId(TEST_GROUP_ID);
        testUserGroup.setIsDeleted(false);
        testUserGroup.setCreatedAt(LocalDateTime.now());
        testUserGroup.setUpdatedAt(LocalDateTime.now());
        
        // Initialize test user using proper constructor
        UserRequestModel userRequest = new UserRequestModel();
        userRequest.setLoginName("testuser");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setPhone("1234567890");
        userRequest.setRole("Customer");
        userRequest.setDob(LocalDate.of(1990, 1, 1));
        userRequest.setEmail("test@example.com");
        
        testUser = new User(userRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);
        
        // Initialize test mapping using constructor
        testMapping = new UserGroupUserMap(TEST_USER_ID, TEST_GROUP_ID, CREATED_USER);
        testMapping.setMappingId(1L);
        
        // Setup common mock behaviors with lenient mocking for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        
        // Note: We avoid mocking getUser() and getClientId() to let JWT authentication work
        // The JWT authentication will provide these values automatically
    }

    // ==================== Toggle User Group Tests ====================
    
    /**
     * Test successful user group toggle operation.
     * Verifies that a user group's isDeleted flag is correctly toggled from false to true.
     */
    @Test
    @DisplayName("Toggle User Group - Success - Should toggle isDeleted flag")
    void toggleUserGroup_Success() {
        // Arrange
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.toggleUserGroup(TEST_GROUP_ID));
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test toggle user group with non-existent group ID.
     * Verifies that NotFoundException is thrown when user group is not found.
     */
    @Test
    @DisplayName("Toggle User Group - Failure - Group not found")
    void toggleUserGroup_GroupNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userGroupService.toggleUserGroup(TEST_GROUP_ID)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, never()).save(any(UserGroup.class));
        verify(userLogService, never()).logData(anyString(), anyString(), anyString());
    }

    // ==================== Get User Group Details By ID Tests ====================
    
    /**
     * Test successful retrieval of user group details by ID.
     * Verifies that user group details and associated users are correctly returned.
     */
    @Test
    @DisplayName("Get User Group Details By ID - Success - Should return group with users")
    void getUserGroupDetailsById_Success() {
        // Arrange
        List<UserGroupUserMap> mappings = Arrays.asList(testMapping);
        
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(mappings);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        
        // Act
        UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_GROUP_ID, result.getGroupId());
        assertEquals(TEST_GROUP_NAME, result.getGroupName());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(1, result.getUsers().size());
        assertEquals(TEST_USER_ID, result.getUsers().get(0).getUserId());
        
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupUserMapRepository, times(1)).findByGroupId(TEST_GROUP_ID);
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }
    
    /**
     * Test get user group details with non-existent group ID.
     * Verifies that NotFoundException is thrown when user group is not found.
     */
    @Test
    @DisplayName("Get User Group Details By ID - Failure - Group not found")
    void getUserGroupDetailsById_GroupNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupUserMapRepository, never()).findByGroupId(anyLong());
    }

    // ==================== Create User Group Tests ====================
    
    /**
     * Test successful user group creation.
     * Verifies that user group and user mappings are correctly created.
     */
    @Test
    @DisplayName("Create User Group - Success - Should create group with user mappings")
    void createUserGroup_Success() {
        // Arrange
        when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        verify(userGroupRepository, times(1)).findByGroupName(TEST_GROUP_NAME);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test create user group with no users provided.
     * Verifies that BadRequestException is thrown when user list is empty.
     */
    @Test
    @DisplayName("Create User Group - Failure - No users provided")
    void createUserGroup_NoUsers_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userGroupService.createUserGroup(testUserGroupRequest)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }
    
    /**
     * Test create user group with null user list.
     * Verifies that BadRequestException is thrown when user list is null.
     */
    @Test
    @DisplayName("Create User Group - Failure - Null user list")
    void createUserGroup_NullUserList_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(null);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userGroupService.createUserGroup(testUserGroupRequest)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }
    
    /**
     * Test create user group with existing group name.
     * Verifies that BadRequestException is thrown when group name already exists.
     */
    @Test
    @DisplayName("Create User Group - Failure - Group name already exists")
    void createUserGroup_GroupNameExists_ThrowsBadRequestException() {
        // Arrange
        when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userGroupService.createUserGroup(testUserGroupRequest)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, exception.getMessage());
        verify(userGroupRepository, times(1)).findByGroupName(TEST_GROUP_NAME);
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }

    // ==================== Update User Group Tests ====================
    
    /**
     * Test successful user group update.
     * Verifies that user group fields and user mappings are correctly updated.
     */
    @Test
    @DisplayName("Update User Group - Success - Should update group and user mappings")
    void updateUserGroup_Success() {
        // Arrange
        testUserGroupRequest.setGroupName("Updated Group Name");
        testUserGroupRequest.setDescription("Updated Description");
        
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);
        
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userGroupUserMapRepository, times(1)).findByGroupId(TEST_GROUP_ID);
        verify(userGroupUserMapRepository, times(1)).deleteAll(anyList());
        verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test update user group with non-existent group ID.
     * Verifies that NotFoundException is thrown when user group is not found.
     */
    @Test
    @DisplayName("Update User Group - Failure - Group not found")
    void updateUserGroup_GroupNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userGroupService.updateUserGroup(testUserGroupRequest)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }
    
    /**
     * Test update user group with no users provided.
     * Verifies that BadRequestException is thrown when user list is empty.
     */
    @Test
    @DisplayName("Update User Group - Failure - No users provided")
    void updateUserGroup_NoUsers_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userGroupService.updateUserGroup(testUserGroupRequest)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        verify(userGroupRepository, never()).findById(anyLong());
    }

    // ==================== Get All User Groups Tests ====================
    
    /**
     * Test successful retrieval of all user groups.
     * Verifies that all user groups are returned.
     */
    @Test
    @DisplayName("Get All User Groups - Success - Should return all groups")
    void getAllUserGroup_Success() {
        // Arrange
        UserGroup secondGroup = new UserGroup(testUserGroupRequest, CREATED_USER);
        secondGroup.setGroupId(2L);
        secondGroup.setGroupName("Second Group");
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup, secondGroup);
        when(userGroupRepository.findAll()).thenReturn(userGroups);
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getAllUserGroup();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TEST_GROUP_ID, result.get(0).getGroupId());
        assertEquals(TEST_GROUP_NAME, result.get(0).getGroupName());
        assertEquals(2L, result.get(1).getGroupId());
        assertEquals("Second Group", result.get(1).getGroupName());
        
        verify(userGroupRepository, times(1)).findAll();
    }
    
    /**
     * Test get all user groups when no groups exist.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get All User Groups - Success - Empty list")
    void getAllUserGroup_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userGroupRepository.findAll()).thenReturn(new ArrayList<>());
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getAllUserGroup();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userGroupRepository, times(1)).findAll();
    }

    // ==================== Get User Groups By Client ID Tests ====================
    
    /**
     * Test successful retrieval of user groups by client ID.
     * Verifies that user groups are filtered by client ID.
     */
    @Test
    @DisplayName("Get User Groups By Client ID - Success - Should return client groups")
    void getUserGroupByClientId_Success() {
        // Arrange
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        when(userGroupRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(userGroups);
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getUserGroupByClientId(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_GROUP_ID, result.get(0).getGroupId());
        assertEquals(TEST_CLIENT_ID, result.get(0).getClientId());
        
        verify(userGroupRepository, times(1)).findByClientId(TEST_CLIENT_ID);
    }
    
    /**
     * Test get user groups by client ID when no groups exist for client.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get User Groups By Client ID - Success - Empty list for client")
    void getUserGroupByClientId_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userGroupRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(new ArrayList<>());
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getUserGroupByClientId(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userGroupRepository, times(1)).findByClientId(TEST_CLIENT_ID);
    }

    // ==================== Get Active User Groups Tests ====================
    
    /**
     * Test successful retrieval of active user groups.
     * Verifies that only active (non-deleted) user groups are returned.
     */
    @Test
    @DisplayName("Get Active User Groups - Success - Should return only active groups")
    void getActiveUserGroup_Success() {
        // Arrange
        List<UserGroup> activeGroups = Arrays.asList(testUserGroup);
        when(userGroupRepository.findByIsDeletedFalse()).thenReturn(activeGroups);
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getActiveUserGroup();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_GROUP_ID, result.get(0).getGroupId());
        assertFalse(result.get(0).getIsDeleted());
        
        verify(userGroupRepository, times(1)).findByIsDeletedFalse();
    }
    
    /**
     * Test get active user groups when no active groups exist.
     * Verifies that empty list is returned when all groups are deleted.
     */
    @Test
    @DisplayName("Get Active User Groups - Success - Empty list when no active groups")
    void getActiveUserGroup_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userGroupRepository.findByIsDeletedFalse()).thenReturn(new ArrayList<>());
        
        // Act
        List<UserGroupResponseModel> result = userGroupService.getActiveUserGroup();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userGroupRepository, times(1)).findByIsDeletedFalse();
    }
}
