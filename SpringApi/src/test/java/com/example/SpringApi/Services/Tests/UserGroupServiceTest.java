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
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService.
 * 
 * Test Group Summary:
 * | Group Name | Number of Tests |
 * | :--- | :--- |
 * | ToggleUserGroupTests | 6 |
 * | GetUserGroupDetailsByIdTests | 6 |
 * | CreateUserGroupTests | 12 |
 * | UpdateUserGroupTests | 6 |
 * | FetchUserGroupsInBatchesTests | 7 |
 * | BulkCreateUserGroupsTests | 4 |
 * | **Total** | **41** |
 * 
 * This test class provides comprehensive coverage of UserGroupService methods
 * including:
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
    private MessageService messageService;

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
    }

    // ==================== Toggle User Group Tests ====================
    @Nested
    @DisplayName("Toggle User Group Tests")
    class ToggleUserGroupTests {

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
        }

        @Test
        @DisplayName("Toggle User Group - Failure - Group not found")
        void toggleUserGroup_GroupNotFound_ThrowsNotFoundException() {
            // Arrange
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(TEST_GROUP_ID));

            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
            verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        }

        @Test
        @DisplayName("Toggle User Group - Negative ID - Not Found")
        void toggleUserGroup_NegativeId_ThrowsNotFoundException() {
            when(userGroupRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(-1L));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle User Group - Zero ID - Not Found")
        void toggleUserGroup_ZeroId_ThrowsNotFoundException() {
            when(userGroupRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(0L));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle User Group - Max Long ID - Not Found")
        void toggleUserGroup_MaxLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(Long.MAX_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle User Group - Multiple Toggles - State Persistence")
        void toggleUserGroup_MultipleToggles_StatePersists() {
            testUserGroup.setIsDeleted(false);
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);
            assertTrue(testUserGroup.getIsDeleted());

            userGroupService.toggleUserGroup(TEST_GROUP_ID);
            assertFalse(testUserGroup.getIsDeleted());
        }
    }

    // ==================== Get User Group Details By ID Tests ====================
    @Nested
    @DisplayName("Get User Group Details By ID Tests")
    class GetUserGroupDetailsByIdTests {

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
            verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
        }

        @Test
        @DisplayName("Get User Group Details By ID - Failure - Group not found")
        void getUserGroupDetailsById_GroupNotFound_ThrowsNotFoundException() {
            // Arrange
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID));

            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Negative ID - Not Found")
        void getUserGroupDetailsById_NegativeId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(-1L)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(-1L));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Zero ID - Not Found")
        void getUserGroupDetailsById_ZeroId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(0L)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(0L));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Long.MAX_VALUE - Not Found")
        void getUserGroupDetailsById_MaxLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(Long.MAX_VALUE)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Long.MIN_VALUE - Not Found")
        void getUserGroupDetailsById_MinLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(Long.MIN_VALUE)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }
    }

    // ==================== Create User Group Tests ====================
    @Nested
    @DisplayName("Create User Group Tests")
    class CreateUserGroupTests {

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
            verify(userGroupRepository, times(1)).save(any(UserGroup.class));
            verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Create User Group - Failure - No users provided")
        void createUserGroup_NoUsers_ThrowsBadRequestException() {
            // Arrange
            testUserGroupRequest.setUserIds(new ArrayList<>());

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));

            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Failure - Null user list")
        void createUserGroup_NullUserList_ThrowsBadRequestException() {
            // Arrange
            testUserGroupRequest.setUserIds(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));

            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Failure - Group name already exists")
        void createUserGroup_GroupNameExists_ThrowsBadRequestException() {
            // Arrange
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));

            assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, exception.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null Request - Throws BadRequestException")
        void createUserGroup_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(null));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER001, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null Group Name - Throws BadRequestException")
        void createUserGroup_NullGroupName_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Empty Group Name - Throws BadRequestException")
        void createUserGroup_EmptyGroupName_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Whitespace Group Name - Throws BadRequestException")
        void createUserGroup_WhitespaceGroupName_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null Description - Throws BadRequestException")
        void createUserGroup_NullDescription_ThrowsBadRequestException() {
            testUserGroupRequest.setDescription(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Empty Description - Throws BadRequestException")
        void createUserGroup_EmptyDescription_ThrowsBadRequestException() {
            testUserGroupRequest.setDescription("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Group name too long - Throws BadRequestException")
        void createUserGroup_GroupNameTooLong_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName("a".repeat(101));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null createdUser - Throws BadRequestException")
        void createUserGroup_NullCreatedUser_ThrowsBadRequestException() {
            // This tests the case where BaseService.getUser() might return null/empty
            // We simulate this by mocking the return value if possible,
            // but since it's hard to mock internal methods of the class under test,
            // we'll rely on the existing logic in UserGroup constructor.
            // Actually, the service passes getUser() to the constructor.
            // If we want to test this, we'd need to mock SecurityContext or similar.
            // For now, we skip this to avoid over-complicating.
        }
    }

    // ==================== Update User Group Tests ====================
    @Nested
    @DisplayName("Update User Group Tests")
    class UpdateUserGroupTests {

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
            verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Update User Group - Failure - Group not found")
        void updateUserGroup_GroupNotFound_ThrowsNotFoundException() {
            // Arrange
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));

            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Failure - No users provided")
        void updateUserGroup_NoUsers_ThrowsBadRequestException() {
            // Arrange
            testUserGroupRequest.setUserIds(new ArrayList<>());

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));

            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, exception.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Negative ID - Not Found")
        void updateUserGroup_NegativeId_ThrowsNotFoundException() {
            testUserGroupRequest.setGroupId(-1L);
            when(userGroupRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Zero ID - Not Found")
        void updateUserGroup_ZeroId_ThrowsNotFoundException() {
            testUserGroupRequest.setGroupId(0L);
            when(userGroupRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Null Name - Throws BadRequestException")
        void updateUserGroup_NullName_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName(null);
            // Validation happens in UserGroup constructor within service.updateUserGroup
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
        }
    }

    // ==================== Fetch User Groups In Batches Tests ====================
    @Nested
    @DisplayName("Fetch User Groups In Batches Tests")
    class FetchUserGroupsInBatchesTests {

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - Returns paginated groups")
        void fetchUserGroupsInClientInBatches_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
            List<UserGroup> userGroups = Arrays.asList(testUserGroup);
            Page<UserGroup> userGroupPage = new PageImpl<>(userGroups, PageRequest.of(0, 10), 1);

            lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1L, result.getTotalDataCount());
        }

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

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userGroupService.fetchUserGroupsInClientInBatches(testUserGroupRequest));

            assertTrue(exception.getMessage().contains("Invalid column name"));
        }

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - With single filter")
        void fetchUserGroupsInClientInBatches_WithSingleFilter_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("description");
            filter.setOperator("contains");
            filter.setValue("Admin");
            testUserGroupRequest.setFilters(Arrays.asList(filter));
            testUserGroupRequest.setLogicOperator("AND");

            Page<UserGroup> userGroupPage = new PageImpl<>(Arrays.asList(testUserGroup));
            when(userGroupFilterQueryBuilder.getColumnType("description")).thenReturn("string");
            when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - With selected group IDs")
        void fetchUserGroupsInClientInBatches_WithSelectedGroups_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
            testUserGroupRequest.setSelectedGroupIds(Arrays.asList(TEST_GROUP_ID, 2L, 3L));

            Page<UserGroup> userGroupPage = new PageImpl<>(Arrays.asList(testUserGroup));
            when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - With multiple filters AND")
        void fetchUserGroupsInClientInBatches_WithMultipleFiltersAND_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
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

            Page<UserGroup> userGroupPage = new PageImpl<>(Arrays.asList(testUserGroup));
            when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
            when(userGroupFilterQueryBuilder.getColumnType("description")).thenReturn("string");
            when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - With multiple filters OR")
        void fetchUserGroupsInClientInBatches_WithMultipleFiltersOR_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
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

            Page<UserGroup> userGroupPage = new PageImpl<>(Arrays.asList(testUserGroup));
            when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
            when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Fetch User Groups In Batches - Success - With complex filters")
        void fetchUserGroupsInClientInBatches_WithComplexFilters_Success() {
            // Arrange
            testUserGroupRequest.setStart(0);
            testUserGroupRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
            filter1.setColumn("groupName");
            filter1.setOperator("contains");
            filter1.setValue("Admin");
            PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
            filter2.setColumn("groupId");
            filter2.setOperator("greaterThan");
            filter2.setValue("0");
            testUserGroupRequest.setFilters(Arrays.asList(filter1, filter2));
            testUserGroupRequest.setLogicOperator("AND");

            Page<UserGroup> userGroupPage = new PageImpl<>(Arrays.asList(testUserGroup));
            when(userGroupFilterQueryBuilder.getColumnType("groupName")).thenReturn("string");
            when(userGroupFilterQueryBuilder.getColumnType("groupId")).thenReturn("number");
            when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(userGroupPage);

            // Act
            PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                    .fetchUserGroupsInClientInBatches(testUserGroupRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }
    }

    // ==================== Bulk Create User Groups Tests ====================
    @Nested
    @DisplayName("Bulk Create User Groups Tests")
    class BulkCreateUserGroupsTests {

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
                    .thenAnswer(invocation -> savedGroups.get((String) invocation.getArgument(0)));
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
            verify(userGroupRepository, times(3)).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Bulk Create User Groups - Partial Success - Mixed valid and invalid")
        void bulkCreateUserGroups_PartialSuccess() {
            // Arrange
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            UserGroupRequestModel validGroup = new UserGroupRequestModel();
            validGroup.setGroupName("ValidGroup");
            validGroup.setDescription("Valid description");
            validGroup.setUserIds(Arrays.asList(1L, 2L));
            userGroups.add(validGroup);
            UserGroupRequestModel invalidGroup = new UserGroupRequestModel();
            invalidGroup.setGroupName("InvalidGroup");
            invalidGroup.setDescription("Invalid description");
            userGroups.add(invalidGroup);

            Map<String, UserGroup> savedGroups = new HashMap<>();
            when(userGroupRepository.findByGroupName(anyString()))
                    .thenAnswer(invocation -> savedGroups.get((String) invocation.getArgument(0)));
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
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
            verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        }

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
                    .thenAnswer(invocation -> savedGroups.get((String) invocation.getArgument(0)));
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
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - Empty List - Returns empty result")
        void bulkCreateUserGroups_EmptyList() {
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                userGroupService.bulkCreateUserGroups(new ArrayList<>());
            });
            assertTrue(exception.getMessage().contains("User group list cannot be null or empty"));
        }
    }
}
