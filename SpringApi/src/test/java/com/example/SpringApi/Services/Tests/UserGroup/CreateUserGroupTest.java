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
 * Unit tests for UserGroupService.createUserGroup method.
 * 
 */
@DisplayName("UserGroupService - CreateUserGroup Tests")
class CreateUserGroupTest extends UserGroupServiceTestBase {

    // Total Tests: 19
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify long description is allowed.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Long Description")
    void createUserGroup_longDescription_success() {
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

    /**
     * Purpose: Verify many users can be added to group.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Many Users")
    void createUserGroup_manyUsers_success() {
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

    /**
     * Purpose: Verify single user group creation.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Single User")
    void createUserGroup_singleUser_success() {
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

    /**
     * Purpose: Verify special characters in group name are allowed.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Special Chars In Name")
    void createUserGroup_specialCharsInName_success() {
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

    /**
     * Purpose: Verify successful create operation.
     * Expected Result: No exception thrown, repos save called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Basic Validation")
    void createUserGroup_success_basicValidation() {
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

    /**
     * Purpose: Verify unicode characters in group name are allowed.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Unicode Chars In Name")
    void createUserGroup_unicodeCharsInName_success() {
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

    /**
     * Purpose: Verify user-group mappings are saved.
     * Expected Result: saveAll is called on userGroupUserMapRepository.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Verify Mappings Saved")
    void createUserGroup_verifyMappingsSaved_success() {
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

    /**
     * Purpose: Verify repository save method is called.
     * Expected Result: save is called on userGroupRepository.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUserGroup - Success - Verify Repository Save")
    void createUserGroup_verifyRepositorySaveCalled_success() {
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

    /**
     * Purpose: Verify empty description throws BadRequestException.
     * Expected Result: BadRequestException with ER003 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Empty Description")
    void createUserGroup_emptyDescription_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setDescription("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Verify empty group name throws BadRequestException.
     * Expected Result: BadRequestException with ER002 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Empty Group Name")
    void createUserGroup_emptyGroupName_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Verify duplicate group name throws BadRequestException.
     * Expected Result: BadRequestException with GroupNameExists message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Group Name Exists")
    void createUserGroup_groupNameExists_throwsBadRequestException() {
        // Arrange
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, testUserGroup);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.GROUP_NAME_EXISTS, ex.getMessage());
    }

    /**
     * Purpose: Verify no users throws BadRequestException.
     * Expected Result: BadRequestException with ER004 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - No Users")
    void createUserGroup_noUsers_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Verify null description throws BadRequestException.
     * Expected Result: BadRequestException with ER003 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Null Description")
    void createUserGroup_nullDescription_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setDescription(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Verify null group name throws BadRequestException.
     * Expected Result: BadRequestException with ER002 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Null Group Name")
    void createUserGroup_nullGroupName_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Verify null request throws NullPointerException.
     * Expected Result: NullPointerException thrown.
     * Assertions: assertThrows
     */
    @Test
    @DisplayName("createUserGroup - Failure - Null Request")
    void createUserGroup_nullRequest_throwsNullPointerException() {
        // Arrange

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> userGroupService.createUserGroup(null));

        // Assert
        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains(
                com.example.SpringApi.Models.RequestModels.UserGroupRequestModel.class.getSimpleName()));
    }

    /**
     * Purpose: Verify null user list throws BadRequestException.
     * Expected Result: BadRequestException with ER004 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Null User List")
    void createUserGroup_nullUserList_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace-only group name throws BadRequestException.
     * Expected Result: BadRequestException with ER002 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUserGroup - Failure - Whitespace Group Name")
    void createUserGroup_whitespaceGroupName_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName("   ");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.createUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned and @PreAuthorize
     * verified.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
     * assertNotNull, assertTrue
     */
    @Test
    @DisplayName("createUserGroup - Controller permission forbidden")
    void createUserGroup_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        stubServiceThrowsUnauthorizedException();
        Method method = UserGroupController.class.getMethod("createUserGroup", UserGroupRequestModel.class);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.createUserGroup(testUserGroupRequest);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called and HTTP 201 is returned.
     * Assertions: verify, HttpStatus.CREATED
     */
    @Test
    @DisplayName("createUserGroup - Controller delegates to service")
    void createUserGroup_withValidRequest_delegatesToService() {
        // Arrange
        stubMockUserGroupServiceCreateUserGroup(testUserGroupRequest);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.createUserGroup(testUserGroupRequest);

        // Assert
        verify(mockUserGroupService, times(1)).createUserGroup(testUserGroupRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
