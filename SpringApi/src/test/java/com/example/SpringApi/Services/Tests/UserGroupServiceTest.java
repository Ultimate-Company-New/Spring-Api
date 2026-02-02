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
 * | Group Name                         | Number of Tests |
 * | :--------------------------------- | :-------------- |
 * | ToggleUserGroupTests               | 11              |
 * | GetUserGroupDetailsByIdTests       | 9               |
 * | CreateUserGroupTests               | 17              |
 * | UpdateUserGroupTests               | 13              |
 * | FetchUserGroupsInBatchesTests      | 1               |
 * | BulkCreateUserGroupsTests          | 9               |
 * | **Total**                          | **60**          |
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

    // Valid columns for batch filtering
    private static final String[] STRING_COLUMNS = {"groupName", "description", "notes", "createdUser", "modifiedUser"};
    private static final String[] NUMBER_COLUMNS = {"groupId", "clientId"};
    private static final String[] BOOLEAN_COLUMNS = {"isActive", "isDeleted"};
    private static final String[] DATE_COLUMNS = {"createdAt", "updatedAt"};

    // Valid operators
    private static final String[] STRING_OPERATORS = {"equals", "contains", "startsWith", "endsWith"};
    private static final String[] NUMBER_OPERATORS = {"equals", ">", ">=", "<", "<="};
    private static final String[] BOOLEAN_OPERATORS = {"is"};

    @BeforeEach
    void setUp() {
        testUserGroupRequest = new UserGroupRequestModel();
        testUserGroupRequest.setGroupId(TEST_GROUP_ID);
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        testUserGroupRequest.setDescription(TEST_DESCRIPTION);
        testUserGroupRequest.setUserIds(Arrays.asList(TEST_USER_ID, 2L, 3L));

        testUserGroup = new UserGroup(testUserGroupRequest, CREATED_USER, TEST_CLIENT_ID);
        testUserGroup.setGroupId(TEST_GROUP_ID);
        testUserGroup.setIsDeleted(false);
        testUserGroup.setCreatedAt(LocalDateTime.now());
        testUserGroup.setUpdatedAt(LocalDateTime.now());

        UserRequestModel userRequest = new UserRequestModel();
        userRequest.setLoginName("testuser");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setPhone("1234567890");
        userRequest.setRole("Customer");
        userRequest.setDob(LocalDate.of(1990, 1, 1));

        testUser = new User(userRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);

        testMapping = new UserGroupUserMap(TEST_USER_ID, TEST_GROUP_ID, CREATED_USER);
        testMapping.setMappingId(1L);

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    @Nested
    @DisplayName("Toggle User Group Tests")
    class ToggleUserGroupTests {

        @Test
        @DisplayName("Toggle User Group - Success - Should toggle isDeleted flag")
        void toggleUserGroup_Success() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.toggleUserGroup(TEST_GROUP_ID));
            verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
            verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Toggle User Group - Failure - Group not found")
        void toggleUserGroup_GroupNotFound_ThrowsNotFoundException() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(TEST_GROUP_ID));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
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
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);
            assertTrue(testUserGroup.getIsDeleted());

            userGroupService.toggleUserGroup(TEST_GROUP_ID);
            assertFalse(testUserGroup.getIsDeleted());
        }

        @Test
        @DisplayName("Toggle User Group - Success - Logs operation")
        void toggleUserGroup_Success_LogsOperation() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);

            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Toggle User Group - Restores deleted group")
        void toggleUserGroup_RestoresDeletedGroup() {
            testUserGroup.setIsDeleted(true);
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);

            assertFalse(testUserGroup.getIsDeleted());
        }

        @Test
        @DisplayName("Toggle User Group - Min Long ID - Not Found")
        void toggleUserGroup_MinLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.toggleUserGroup(Long.MIN_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle User Group - Verify repository called")
        void toggleUserGroup_VerifyRepositoryCalled() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);

            verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
            verify(userGroupRepository, times(1)).save(testUserGroup);
        }

        @Test
        @DisplayName("Toggle User Group - Verify logging called")
        void toggleUserGroup_VerifyLoggingCalled() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.toggleUserGroup(TEST_GROUP_ID);

            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Get User Group Details By ID Tests")
    class GetUserGroupDetailsByIdTests {

        @Test
        @DisplayName("Get User Group Details By ID - Success")
        void getUserGroupDetailsById_Success() {
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

            assertNotNull(result);
            assertEquals(TEST_GROUP_ID, result.getGroupId());
            assertEquals(TEST_GROUP_NAME, result.getGroupName());
        }

        @Test
        @DisplayName("Get User Group Details By ID - Not Found")
        void getUserGroupDetailsById_NotFound_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
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
        @DisplayName("Get User Group By ID - Max Long ID - Not Found")
        void getUserGroupDetailsById_MaxLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(Long.MAX_VALUE)).thenReturn(null);
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Min Long ID - Not Found")
        void getUserGroupDetailsById_MinLongId_ThrowsNotFoundException() {
            when(userGroupRepository.findByIdWithUsers(Long.MIN_VALUE)).thenReturn(null);
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Get User Group By ID - Repository called once")
        void getUserGroupDetailsById_RepositoryCalledOnce() {
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

            verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
        }

        @Test
        @DisplayName("Get User Group By ID - Returns correct fields")
        void getUserGroupDetailsById_ReturnsCorrectFields() {
            testUserGroup.setDescription("Specific Description");
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

            assertEquals(TEST_GROUP_ID, result.getGroupId());
            assertEquals(TEST_GROUP_NAME, result.getGroupName());
            assertEquals("Specific Description", result.getDescription());
        }

        @Test
        @DisplayName("Get User Group By ID - Returns non-null response")
        void getUserGroupDetailsById_ReturnsNonNullResponse() {
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

            assertNotNull(result);
            assertNotNull(result.getGroupId());
            assertNotNull(result.getGroupName());
        }
    }

    @Nested
    @DisplayName("Create User Group Tests")
    class CreateUserGroupTests {

        @Test
        @DisplayName("Create User Group - Success")
        void createUserGroup_Success() {
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
            verify(userGroupRepository).save(any(UserGroup.class));
            verify(userGroupUserMapRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Create User Group - No Users - Throws BadRequestException")
        void createUserGroup_NoUsers_ThrowsBadRequestException() {
            testUserGroupRequest.setUserIds(new ArrayList<>());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null User List - Throws BadRequestException")
        void createUserGroup_NullUserList_ThrowsBadRequestException() {
            testUserGroupRequest.setUserIds(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Group Name Exists - Throws BadRequestException")
        void createUserGroup_GroupNameExists_ThrowsBadRequestException() {
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.createUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
        }

        @Test
        @DisplayName("Create User Group - Null Request - Throws NullPointerException")
        void createUserGroup_NullRequest_ThrowsNullPointerException() {
            // Service receives null request, which causes NPE when accessing request properties
            assertThrows(NullPointerException.class,
                    () -> userGroupService.createUserGroup(null));
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
        @DisplayName("Create User Group - Single User - Success")
        void createUserGroup_SingleUser_Success() {
            testUserGroupRequest.setUserIds(Arrays.asList(TEST_USER_ID));
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        }

        @Test
        @DisplayName("Create User Group - Many Users - Success")
        void createUserGroup_ManyUsers_Success() {
            testUserGroupRequest.setUserIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        }

        @Test
        @DisplayName("Create User Group - Special chars in name - Success")
        void createUserGroup_SpecialCharsInName_Success() {
            testUserGroupRequest.setGroupName("Test @#$% Group!");
            when(userGroupRepository.findByGroupName("Test @#$% Group!")).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        }

        @Test
        @DisplayName("Create User Group - Unicode chars in name - Success")
        void createUserGroup_UnicodeCharsInName_Success() {
            testUserGroupRequest.setGroupName("测试组 Test");
            when(userGroupRepository.findByGroupName("测试组 Test")).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        }

        @Test
        @DisplayName("Create User Group - Verify repository save called")
        void createUserGroup_VerifyRepositorySaveCalled() {
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.createUserGroup(testUserGroupRequest);

            verify(userGroupRepository).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Create User Group - Verify mappings saved")
        void createUserGroup_VerifyMappingsSaved() {
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.createUserGroup(testUserGroupRequest);

            verify(userGroupUserMapRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Create User Group - Long description - Success")
        void createUserGroup_LongDescription_Success() {
            testUserGroupRequest.setDescription("D".repeat(1000));
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));
        }
    }

    @Nested
    @DisplayName("Update User Group Tests")
    class UpdateUserGroupTests {

        @Test
        @DisplayName("Update User Group - Success")
        void updateUserGroup_Success() {
            testUserGroupRequest.setGroupName("Updated Group Name");
            testUserGroupRequest.setDescription("Updated Description");
            List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            lenient().when(userGroupRepository.findByGroupName("Updated Group Name")).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
            doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
            verify(userGroupRepository).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Update User Group - Not Found - Throws NotFoundException")
        void updateUserGroup_NotFound_ThrowsNotFoundException() {
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - No Users - Throws BadRequestException")
        void updateUserGroup_NoUsers_ThrowsBadRequestException() {
            testUserGroupRequest.setUserIds(new ArrayList<>());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Negative ID - Throws NotFoundException")
        void updateUserGroup_NegativeId_ThrowsNotFoundException() {
            testUserGroupRequest.setGroupId(-1L);
            when(userGroupRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Zero ID - Throws NotFoundException")
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
            // Need to mock findById to return the existing group so validation proceeds to the name check
            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Null Users - Throws BadRequestException")
        void updateUserGroup_NullUsers_ThrowsBadRequestException() {
            testUserGroupRequest.setUserIds(null);
            
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Duplicate Name - Throws BadRequestException")
        void updateUserGroup_DuplicateName_ThrowsBadRequestException() {
            testUserGroupRequest.setGroupName("Existing Group");
            UserGroup existingGroupWithSameName = new UserGroup();
            existingGroupWithSameName.setGroupId(999L); // Different ID
            existingGroupWithSameName.setGroupName("Existing Group");

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.findByGroupName("Existing Group")).thenReturn(existingGroupWithSameName);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Same Name Different Group - Allowed")
        void updateUserGroup_SameNameSameGroup_Allowed() {
            testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
            List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
            doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
        }

        @Test
        @DisplayName("Update User Group - Verify old mappings deleted")
        void updateUserGroup_VerifyOldMappingsDeleted() {
            List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            lenient().when(userGroupRepository.findByGroupName(anyString())).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
            doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.updateUserGroup(testUserGroupRequest);

            verify(userGroupUserMapRepository).deleteAll(anyList());
        }

        @Test
        @DisplayName("Update User Group - Verify new mappings created")
        void updateUserGroup_VerifyNewMappingsCreated() {
            List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            lenient().when(userGroupRepository.findByGroupName(anyString())).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
            doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.updateUserGroup(testUserGroupRequest);

            verify(userGroupUserMapRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Update User Group - Max Long ID - Throws NotFoundException")
        void updateUserGroup_MaxLongId_ThrowsNotFoundException() {
            testUserGroupRequest.setGroupId(Long.MAX_VALUE);
            when(userGroupRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.updateUserGroup(testUserGroupRequest));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Update User Group - Special chars in name - Success")
        void updateUserGroup_SpecialCharsInName_Success() {
            testUserGroupRequest.setGroupName("Updated @#$ Group");
            List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

            when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
            when(userGroupRepository.findByGroupName("Updated @#$ Group")).thenReturn(null);
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
            when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
            doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
        }
    }

    @Nested
    @DisplayName("Fetch User Groups In Batches Tests")
    class FetchUserGroupsInBatchesTests {

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators using nested loops.
         */
        @Test
        @DisplayName("Comprehensive Batch Filter Test - All Combinations")
        void fetchUserGroupsInBatches_ComprehensiveCombinationTest() {
            int validTests = 0;
            int invalidTests = 0;

            String[] logicOperators = {"AND", "OR", "and", "or"};
            String[] invalidLogicOperators = {"XOR", "NAND", "invalid"};
            String[] invalidColumns = {"invalidColumn", "xyz", "!@#$"};

            // ============== TEST 1: Valid column + valid operator combinations ==============
            for (String column : STRING_COLUMNS) {
                for (String operator : STRING_OPERATORS) {
                    UserGroupRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("testValue");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                    lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                    lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                    assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                            "String column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // Test number columns
            for (String column : NUMBER_COLUMNS) {
                for (String operator : NUMBER_OPERATORS) {
                    UserGroupRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("100");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                    lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                    lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                    assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                            "Number column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // Test boolean columns
            for (String column : BOOLEAN_COLUMNS) {
                for (String operator : BOOLEAN_OPERATORS) {
                    UserGroupRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("true");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                    lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("boolean");
                    lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                    assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                            "Boolean column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // ============== TEST 2: Invalid column names ==============
            for (String invalidColumn : invalidColumns) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(invalidColumn);
                filter.setOperator("equals");
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Invalid column '" + invalidColumn + "' should throw BadRequestException");
                assertTrue(ex.getMessage().contains("Invalid column name"));
                invalidTests++;
            }

            // ============== TEST 3: Invalid logic operators ==============
            for (String invalidLogic : invalidLogicOperators) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("groupName");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("description");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(invalidLogic);

                lenient().when(userGroupFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Invalid logic operator '" + invalidLogic + "' should throw BadRequestException");
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
                invalidTests++;
            }

            // ============== TEST 4: Valid logic operators ==============
            for (String validLogic : logicOperators) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("groupName");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("description");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(validLogic);

                Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                lenient().when(userGroupFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Valid logic operator '" + validLogic + "' should succeed");
                validTests++;
            }

            // ============== TEST 5: Pagination edge cases ==============
            int[][] invalidPaginationCases = {{10, 10}, {10, 5}, {0, 0}};

            for (int[] pagination : invalidPaginationCases) {
                if (pagination[1] - pagination[0] <= 0) {
                    UserGroupRequestModel request = new UserGroupRequestModel();
                    request.setStart(pagination[0]);
                    request.setEnd(pagination[1]);
                    request.setIncludeDeleted(false);

                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                            "Pagination start=" + pagination[0] + ", end=" + pagination[1] + " should throw");
                    assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                    invalidTests++;
                }
            }

            // ============== TEST 6: No filters (basic pagination) ==============
            UserGroupRequestModel noFilterRequest = createBasicPaginationRequest();
            noFilterRequest.setFilters(null);

            Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);
            lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), isNull(), anyBoolean(), any(Pageable.class))).thenReturn(page);

            assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(noFilterRequest));
            validTests++;

            System.out.println("Comprehensive UserGroup Batch Filter Test Summary:");
            System.out.println("  Valid test cases passed: " + validTests);
            System.out.println("  Invalid test cases (expected failures): " + invalidTests);

            assertTrue(validTests >= 30, "Should have at least 30 valid test cases");
            assertTrue(invalidTests >= 5, "Should have at least 5 invalid test cases");
        }

        private UserGroupRequestModel createBasicPaginationRequest() {
            UserGroupRequestModel request = new UserGroupRequestModel();
            request.setStart(0);
            request.setEnd(10);
            request.setIncludeDeleted(false);
            return request;
        }
    }

    @Nested
    @DisplayName("Bulk Create User Groups Tests")
    class BulkCreateUserGroupsTests {

        @Test
        @DisplayName("Bulk Create User Groups - All Valid - Success")
        void bulkCreateUserGroups_AllValid_Success() {
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
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId((long) (Math.random() * 1000));
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(3, result.getSuccessCount());
            verify(userGroupRepository, times(3)).save(any(UserGroup.class));
        }

        @Test
        @DisplayName("Bulk Create User Groups - Partial Success")
        void bulkCreateUserGroups_PartialSuccess() {
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            
            UserGroupRequestModel validGroup = new UserGroupRequestModel();
            validGroup.setGroupName("ValidGroup");
            validGroup.setDescription("Valid description");
            validGroup.setUserIds(Arrays.asList(1L, 2L));
            userGroups.add(validGroup);
            
            UserGroupRequestModel invalidGroup = new UserGroupRequestModel();
            invalidGroup.setGroupName("InvalidGroup");
            invalidGroup.setDescription("Invalid description");
            invalidGroup.setUserIds(null); // No users - will fail
            userGroups.add(invalidGroup);

            Map<String, UserGroup> savedGroups = new HashMap<>();
            when(userGroupRepository.findByGroupName(anyString()))
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId(100L);
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - Duplicate Name")
        void bulkCreateUserGroups_DuplicateName() {
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
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId(100L);
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - Empty List - Throws BadRequestException")
        void bulkCreateUserGroups_EmptyList() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.bulkCreateUserGroups(new ArrayList<>()));
            assertTrue(ex.getMessage().contains("User group list cannot be null or empty"));
        }

        @Test
        @DisplayName("Bulk Create User Groups - Null List - Throws BadRequestException")
        void bulkCreateUserGroups_NullList() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.bulkCreateUserGroups(null));
            assertTrue(ex.getMessage().contains("User group list cannot be null or empty"));
        }

        @Test
        @DisplayName("Bulk Create User Groups - Single Group - Success")
        void bulkCreateUserGroups_SingleGroup_Success() {
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("SingleGroup");
            groupReq.setDescription("Single group description");
            groupReq.setUserIds(Arrays.asList(1L));
            userGroups.add(groupReq);

            Map<String, UserGroup> savedGroups = new HashMap<>();
            when(userGroupRepository.findByGroupName(anyString()))
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId(100L);
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(1, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - All failures")
        void bulkCreateUserGroups_AllFailures() {
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            
            // All groups without userIds - will fail
            for (int i = 0; i < 3; i++) {
                UserGroupRequestModel groupReq = new UserGroupRequestModel();
                groupReq.setGroupName("FailGroup" + i);
                groupReq.setDescription("Description");
                groupReq.setUserIds(null); // No users
                userGroups.add(groupReq);
            }

            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(0, result.getSuccessCount());
            assertEquals(3, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - Many groups - Success")
        void bulkCreateUserGroups_ManyGroups_Success() {
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                UserGroupRequestModel groupReq = new UserGroupRequestModel();
                groupReq.setGroupName("BulkGroup" + i);
                groupReq.setDescription("Description for group " + i);
                groupReq.setUserIds(Arrays.asList(1L, 2L));
                userGroups.add(groupReq);
            }

            Map<String, UserGroup> savedGroups = new HashMap<>();
            when(userGroupRepository.findByGroupName(anyString()))
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId((long) (Math.random() * 1000));
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

            assertNotNull(result);
            assertEquals(10, result.getTotalRequested());
            assertEquals(10, result.getSuccessCount());
        }

        @Test
        @DisplayName("Bulk Create User Groups - Verify logging called")
        void bulkCreateUserGroups_VerifyLoggingCalled() {
            List<UserGroupRequestModel> userGroups = new ArrayList<>();
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("TestGroup");
            groupReq.setDescription("Test description");
            groupReq.setUserIds(Arrays.asList(1L));
            userGroups.add(groupReq);

            Map<String, UserGroup> savedGroups = new HashMap<>();
            when(userGroupRepository.findByGroupName(anyString()))
                    .thenAnswer(inv -> savedGroups.get((String) inv.getArgument(0)));
            when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
                UserGroup group = inv.getArgument(0);
                group.setGroupId(100L);
                savedGroups.put(group.getGroupName(), group);
                return group;
            });
            when(userGroupUserMapRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userGroupService.bulkCreateUserGroups(userGroups);

            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }
    }
}
