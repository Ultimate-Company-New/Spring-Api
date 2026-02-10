package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Services.UserGroupService;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Create User Group functionality.
 * 
 * Contains 17 tests covering:
 * - Successful user group creation with various inputs
 * - Validation of required fields (group name, description, user IDs)
 * - Edge cases (single user, many users, special characters, unicode)
 * - Repository interaction verification
 */
@DisplayName("UserGroupService - CreateUserGroup Tests")
public class CreateUserGroupTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("createUserGroup - Verify @PreAuthorize Annotation")
    void createUserGroup_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserGroupController.class.getMethod("createUserGroup", UserGroupRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("createUserGroup - Controller delegates to service")
    void createUserGroup_WithValidRequest_DelegatesToService() {
        UserGroupService mockUserGroupService = mock(UserGroupService.class);
        UserGroupController controller = new UserGroupController(mockUserGroupService);
        UserGroupRequestModel request = new UserGroupRequestModel();
        doNothing().when(mockUserGroupService).createUserGroup(request);

        ResponseEntity<?> response = controller.createUserGroup(request);

        verify(mockUserGroupService).createUserGroup(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

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
    @DisplayName("Create User Group - Verify repository save called")
    void createUserGroup_VerifyRepositorySaveCalled() {
        when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        userGroupService.createUserGroup(testUserGroupRequest);

        verify(userGroupRepository).save(any(UserGroup.class));
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("Create User Group - Empty Description - Throws BadRequestException")
    void createUserGroup_EmptyDescription_ThrowsBadRequestException() {
        testUserGroupRequest.setDescription("");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
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
    @DisplayName("Create User Group - Group Name Exists - Throws BadRequestException")
    void createUserGroup_GroupNameExists_ThrowsBadRequestException() {
        when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
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
    @DisplayName("Create User Group - Null Description - Throws BadRequestException")
    void createUserGroup_NullDescription_ThrowsBadRequestException() {
        testUserGroupRequest.setDescription(null);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
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
    @DisplayName("Create User Group - Null Request - Throws NullPointerException")
    void createUserGroup_NullRequest_ThrowsNullPointerException() {
        // Service receives null request, which causes NPE when accessing request
        // properties
        assertThrows(NullPointerException.class,
                () -> userGroupService.createUserGroup(null));
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
    @DisplayName("Create User Group - Whitespace Group Name - Throws BadRequestException")
    void createUserGroup_WhitespaceGroupName_ThrowsBadRequestException() {
        testUserGroupRequest.setGroupName("   ");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }
}
