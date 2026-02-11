package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Create User Group functionality.
 * 
 * Contains tests covering:
 * - Successful user group creation with various inputs
 * - Validation of required fields (group name, description, user IDs)
 * - Edge cases (single user, many users, special characters, unicode)
 * - Repository interaction verification
 * - Controller authorization and delegation
 */
@DisplayName("UserGroupService - CreateUserGroup Tests")
public class CreateUserGroupTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("createUserGroup - Verify @PreAuthorize Annotation")
    void createUserGroup_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("createUserGroup", UserGroupRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("createUserGroup - Controller delegates to service")
    void createUserGroup_WithValidRequest_DelegatesToService() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        ResponseEntity<?> response = userGroupController.createUserGroup(testUserGroupRequest);

        // Assert
        verify(userGroupService).createUserGroup(testUserGroupRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("Create User Group - Long description - Success")
    void createUserGroup_LongDescription_Success() {
        // Arrange
        testUserGroupRequest.setDescription("D".repeat(1000));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Create User Group - Many Users - Success")
    void createUserGroup_ManyUsers_Success() {
        // Arrange
        testUserGroupRequest.setUserIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Create User Group - Single User - Success")
    void createUserGroup_SingleUser_Success() {
        // Arrange
        testUserGroupRequest.setUserIds(Collections.singletonList(TEST_USER_ID));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Create User Group - Special chars in name - Success")
    void createUserGroup_SpecialCharsInName_Success() {
        // Arrange
        String specialName = "Test @#$% Group!";
        testUserGroupRequest.setGroupName(specialName);
        stubUserGroupRepositoryFindByGroupName(specialName, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Create User Group - Success")
    void createUserGroup_Success() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
        verify(userGroupUserMapRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Create User Group - Unicode chars in name - Success")
    void createUserGroup_UnicodeCharsInName_Success() {
        // Arrange
        String unicodeName = "测试组 Test";
        testUserGroupRequest.setGroupName(unicodeName);
        stubUserGroupRepositoryFindByGroupName(unicodeName, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.createUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Create User Group - Verify mappings saved")
    void createUserGroup_VerifyMappingsSaved() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.createUserGroup(testUserGroupRequest);

        // Assert
        verify(userGroupUserMapRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Create User Group - Verify repository save called")
    void createUserGroup_VerifyRepositorySaveCalled() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.createUserGroup(testUserGroupRequest);

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("Create User Group - Empty Description - Throws BadRequestException")
    void createUserGroup_EmptyDescription_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setDescription("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Empty Group Name - Throws BadRequestException")
    void createUserGroup_EmptyGroupName_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Group Name Exists - Throws BadRequestException")
    void createUserGroup_GroupNameExists_ThrowsBadRequestException() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, testUserGroup);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - No Users - Throws BadRequestException")
    void createUserGroup_NoUsers_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Null Description - Throws BadRequestException")
    void createUserGroup_NullDescription_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setDescription(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Null Group Name - Throws BadRequestException")
    void createUserGroup_NullGroupName_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Null Request - Throws NullPointerException")
    void createUserGroup_NullRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> userGroupService.createUserGroup(null));
    }

    @Test
    @DisplayName("Create User Group - Null User List - Throws BadRequestException")
    void createUserGroup_NullUserList_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Create User Group - Whitespace Group Name - Throws BadRequestException")
    void createUserGroup_WhitespaceGroupName_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName("   ");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }
}
