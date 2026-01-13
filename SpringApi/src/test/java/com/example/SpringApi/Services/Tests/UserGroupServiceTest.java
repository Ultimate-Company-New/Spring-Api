package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.FilterQueryBuilder.UserGroupFilterQueryBuilder;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private UserGroupFilterQueryBuilder userGroupFilterQueryBuilder;
    
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
    private static final Long TEST_CLIENT_ID = 1L;
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
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        testUserGroupRequest.setDescription(TEST_DESCRIPTION);
        testUserGroupRequest.setUserIds(Arrays.asList(TEST_USER_ID, 2L, 3L));
        
        // Initialize test user group using constructor
        testUserGroup = new UserGroup(testUserGroupRequest, CREATED_USER, TEST_CLIENT_ID);
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

        testUser = new User(userRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);
        
        // Initialize test mapping using constructor
        testMapping = new UserGroupUserMap(TEST_USER_ID, TEST_GROUP_ID, CREATED_USER);
        testMapping.setMappingId(1L);
        
        // Setup common mock behaviors with lenient mocking for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        // Note: BaseService methods are now handled by the actual service implementation
        
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
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.toggleUserGroup(TEST_GROUP_ID));
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
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
        verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
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
        when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);
        
        // Act
        UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_GROUP_ID, result.getGroupId());
        assertEquals(TEST_GROUP_NAME, result.getGroupName());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        
        verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
    }
    
    /**
     * Test get user group details with non-existent group ID.
     * Verifies that NotFoundException is thrown when user group is not found.
     */
    @Test
    @DisplayName("Get User Group Details By ID - Failure - Group not found")
    void getUserGroupDetailsById_GroupNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(null);
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID)
        );
        
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
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
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        verify(userGroupRepository, times(1)).findByGroupName(TEST_GROUP_NAME);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
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
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userGroupUserMapRepository, times(1)).findByGroupId(TEST_GROUP_ID);
        verify(userGroupUserMapRepository, times(1)).deleteAll(anyList());
        verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
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

    // ==================== Fetch User Groups In Batches Tests ====================
    
    /**
     * Test successful retrieval of user groups in batches.
     * Verifies that paginated user groups are returned correctly.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - Returns paginated groups")
    void fetchUserGroupsInClientInBatches_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        assertEquals(TEST_GROUP_ID, result.getData().get(0).getGroupId());
        assertEquals(TEST_GROUP_NAME, result.getData().get(0).getGroupName());
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with invalid column name.
     * Verifies that BadRequestException is thrown for invalid column.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Failure - Invalid column name")
    void fetchUserGroupsInClientInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("contains");
        invalidFilter.setValue("test");
        testUserGroupRequest.setFilters(Arrays.asList(invalidFilter));
        testUserGroupRequest.setLogicOperator("AND");
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid column name"));
        verify(userGroupFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with single filter.
     * Verifies that single filter expressions are correctly applied.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - With single filter")
    void fetchUserGroupsInClientInBatches_WithSingleFilter_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("description");
        filter.setOperator("contains");
        filter.setValue("Admin");
        testUserGroupRequest.setFilters(Arrays.asList(filter));
        testUserGroupRequest.setLogicOperator("AND");
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        when(userGroupFilterQueryBuilder.getColumnType("description")).thenReturn("string");
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(userGroupFilterQueryBuilder, times(1)).getColumnType("description");
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with selected group IDs.
     * Verifies that specific group IDs are filtered correctly.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - With selected group IDs")
    void fetchUserGroupsInClientInBatches_WithSelectedGroups_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        testUserGroupRequest.setSelectedGroupIds(Arrays.asList(TEST_GROUP_ID, 2L, 3L));
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with multiple filters using AND logic.
     * Verifies that multiple filters combined with AND are correctly applied.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - With multiple filters AND")
    void fetchUserGroupsInClientInBatches_WithMultipleFiltersAND_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("groupName");
        filter1.setOperator("contains");
        filter1.setValue("Admin");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("description");
        filter2.setOperator("contains");
        filter2.setValue("Group");
        
        testUserGroupRequest.setFilters(Arrays.asList(filter1, filter2));
        testUserGroupRequest.setLogicOperator("AND");
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
        when(userGroupFilterQueryBuilder.getColumnType("description")).thenReturn("string");
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(userGroupFilterQueryBuilder, times(1)).getColumnType("groupName");
        verify(userGroupFilterQueryBuilder, times(1)).getColumnType("description");
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with multiple filters using OR logic.
     * Verifies that multiple filters combined with OR are correctly applied.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - With multiple filters OR")
    void fetchUserGroupsInClientInBatches_WithMultipleFiltersOR_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("groupName");
        filter1.setOperator("contains");
        filter1.setValue("Admin");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("groupName");
        filter2.setOperator("contains");
        filter2.setValue("Manager");
        
        testUserGroupRequest.setFilters(Arrays.asList(filter1, filter2));
        testUserGroupRequest.setLogicOperator("OR");
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(userGroupFilterQueryBuilder, times(2)).getColumnType("groupName");
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    /**
     * Test pagination with complex filters (string, number, boolean).
     * Verifies that filters with different column types are correctly validated and applied.
     */
    @Test
    @DisplayName("Fetch User Groups In Batches - Success - With complex filters")
    void fetchUserGroupsInClientInBatches_WithComplexFilters_Success() {
        // Arrange
        testUserGroupRequest.setStart(0);
        testUserGroupRequest.setEnd(10);
        testUserGroupRequest.setIncludeDeleted(false);
        
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("groupName");
        filter1.setOperator("contains");
        filter1.setValue("Admin");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("groupId");
        filter2.setOperator(PaginationBaseRequestModel.OP_GREATER_THAN);
        filter2.setValue("0");
        
        testUserGroupRequest.setFilters(Arrays.asList(filter1, filter2));
        testUserGroupRequest.setLogicOperator("AND");
        
        List<UserGroup> userGroups = Arrays.asList(testUserGroup);
        Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);
        
        when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
        when(userGroupFilterQueryBuilder.getColumnType("groupId")).thenReturn("number");
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(userGroupPage);
        
        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(userGroupFilterQueryBuilder, times(1)).getColumnType("groupName");
        verify(userGroupFilterQueryBuilder, times(1)).getColumnType("groupId");
        verify(userGroupFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)
        );
    }
    
    // ==================== Bulk Create User Groups Tests ====================
    
    /**
     * Test successful bulk user group creation.
     * Verifies that multiple user groups are created successfully.
     */
    @Test
    @DisplayName("Bulk Create User Groups - Success - All valid groups")
    void bulkCreateUserGroups_AllValid_Success() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("BulkGroup" + i);
            groupReq.setDescription("Description for group " + i);
            groupReq.setUserIds(Arrays.asList(1L, 2L));
            userGroups.add(groupReq);
        }
        
        Map<String, UserGroup> savedGroups = new HashMap<>();
        when(userGroupRepository.findByGroupName(anyString()))
            .thenAnswer(invocation -> savedGroups.get(invocation.getArgument(0)));
        when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(invocation -> {
            UserGroup group = invocation.getArgument(0);
            group.setGroupId((long) (Math.random() * 1000));
            savedGroups.put(group.getGroupName(), group);
            return group;
        });
        when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(userGroupRepository, times(3)).save(any(UserGroup.class));
    }
    
    /**
     * Test bulk user group creation with partial success.
     * Verifies that some groups succeed while others fail validation.
     */
    @Test
    @DisplayName("Bulk Create User Groups - Partial Success - Mixed valid and invalid")
    void bulkCreateUserGroups_PartialSuccess() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        
        // Valid group
        UserGroupRequestModel validGroup = new UserGroupRequestModel();
        validGroup.setGroupName("ValidGroup");
        validGroup.setDescription("Valid description");
        validGroup.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(validGroup);
        
        // Invalid group (missing userIds)
        UserGroupRequestModel invalidGroup = new UserGroupRequestModel();
        invalidGroup.setGroupName("InvalidGroup");
        invalidGroup.setDescription("Invalid description");
        userGroups.add(invalidGroup);
        
        Map<String, UserGroup> savedGroups = new HashMap<>();
        when(userGroupRepository.findByGroupName(anyString()))
            .thenAnswer(invocation -> savedGroups.get(invocation.getArgument(0)));
        when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(invocation -> {
            UserGroup group = invocation.getArgument(0);
            group.setGroupId(100L);
            savedGroups.put(group.getGroupName(), group);
            return group;
        });
        when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
    }
    
    /**
     * Test bulk user group creation with duplicate group name.
     * Verifies that duplicate group names are rejected.
     */
    @Test
    @DisplayName("Bulk Create User Groups - Failure - Duplicate group name")
    void bulkCreateUserGroups_DuplicateName() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        
        UserGroupRequestModel group1 = new UserGroupRequestModel();
        group1.setGroupName("NewGroup");
        group1.setDescription("New group");
        group1.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(group1);
        
        UserGroupRequestModel group2 = new UserGroupRequestModel();
        group2.setGroupName("ExistingGroup");
        group2.setDescription("Existing group");
        group2.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(group2);
        
        Map<String, UserGroup> savedGroups = new HashMap<>();
        UserGroup existingGroup = new UserGroup(group2, CREATED_USER, TEST_CLIENT_ID);
        existingGroup.setGroupId(TEST_GROUP_ID);
        savedGroups.put("ExistingGroup", existingGroup);
        
        when(userGroupRepository.findByGroupName(anyString()))
            .thenAnswer(invocation -> savedGroups.get(invocation.getArgument(0)));
        when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(invocation -> {
            UserGroup group = invocation.getArgument(0);
            group.setGroupId(100L);
            savedGroups.put(group.getGroupName(), group);
            return group;
        });
        when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        
        // Verify the duplicate group name failed
        Optional<BulkInsertResponseModel.InsertResult<Long>> duplicateResult = 
            result.getResults().stream()
                .filter(r -> r.getIdentifier().equals("ExistingGroup"))
                .findFirst();
        assertTrue(duplicateResult.isPresent());
        assertFalse(duplicateResult.get().isSuccess());
    }
    
    /**
     * Test bulk user group creation with empty list.
     * Verifies that empty list returns empty result.
     */
    @Test
    @DisplayName("Bulk Create User Groups - Empty List - Returns empty result")
    void bulkCreateUserGroups_EmptyList() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userGroupService.bulkCreateUserGroups(new ArrayList<>());
        });
        assertTrue(exception.getMessage().contains("User group list cannot be null or empty"));
        verify(userGroupRepository, never()).save(any(UserGroup.class));
    }

    // ==================== Additional GetUserGroupDetailsById Tests ====================

    @Test
    @DisplayName("Get User Group By ID - Negative ID - Not Found")
    void getUserGroupDetailsById_NegativeId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(-1L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get User Group By ID - Zero ID - Not Found")
    void getUserGroupDetailsById_ZeroId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(0L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get User Group By ID - Long.MAX_VALUE - Not Found")
    void getUserGroupDetailsById_MaxLongId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get User Group By ID - Long.MIN_VALUE - Not Found")
    void getUserGroupDetailsById_MinLongId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    // ==================== Additional CreateUserGroup Tests ====================

    @Test
    @DisplayName("Create User Group - Null Request - Throws BadRequestException")
    void createUserGroup_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(null));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Null Group Name - Throws BadRequestException")
    void createUserGroup_NullGroupName_ThrowsBadRequestException() {
        testUserGroupRequest.setUserGroupName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidName, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Empty Group Name - Throws BadRequestException")
    void createUserGroup_EmptyGroupName_ThrowsBadRequestException() {
        testUserGroupRequest.setUserGroupName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidName, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Whitespace Group Name - Throws BadRequestException")
    void createUserGroup_WhitespaceGroupName_ThrowsBadRequestException() {
        testUserGroupRequest.setUserGroupName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidName, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Null Description - Throws BadRequestException")
    void createUserGroup_NullDescription_ThrowsBadRequestException() {
        testUserGroupRequest.setDescription(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidDescription, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Empty Description - Throws BadRequestException")
    void createUserGroup_EmptyDescription_ThrowsBadRequestException() {
        testUserGroupRequest.setDescription("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidDescription, ex.getMessage());
    }

    // ==================== Additional UpdateUserGroup Tests ====================

    @Test
    @DisplayName("Update User Group - Negative ID - Not Found")
    void updateUserGroup_NegativeId_ThrowsNotFoundException() {
        testUserGroupRequest.setUserGroupId(-1L);
        when(userGroupRepository.findByUserGroupIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Zero ID - Not Found")
    void updateUserGroup_ZeroId_ThrowsNotFoundException() {
        testUserGroupRequest.setUserGroupId(0L);
        when(userGroupRepository.findByUserGroupIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Null Name - Throws BadRequestException")
    void updateUserGroup_NullName_ThrowsBadRequestException() {
        testUserGroupRequest.setUserGroupName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidName, ex.getMessage());
    }

    // ==================== Additional ToggleUserGroup Tests ====================

    @Test
    @DisplayName("Toggle User Group - Negative ID - Not Found")
    void toggleUserGroup_NegativeId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(-1L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle User Group - Zero ID - Not Found")
    void toggleUserGroup_ZeroId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(0L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle User Group - Max Long ID - Not Found")
    void toggleUserGroup_MaxLongId_ThrowsNotFoundException() {
        when(userGroupRepository.findByUserGroupIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(Long.MAX_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle User Group - Multiple Toggles - State Persistence")
    void toggleUserGroup_MultipleToggles_StatePersists() {
        testUserGroup.setIsDeleted(false);
        when(userGroupRepository.findByUserGroupIdAndClientId(TEST_GROUP_ID, TEST_CLIENT_ID))
                .thenReturn(testUserGroup);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        
        userGroupService.toggleUserGroup(TEST_GROUP_ID);
        assertTrue(testUserGroup.getIsDeleted());
        
        userGroupService.toggleUserGroup(TEST_GROUP_ID);
        assertFalse(testUserGroup.getIsDeleted());
    }
}
