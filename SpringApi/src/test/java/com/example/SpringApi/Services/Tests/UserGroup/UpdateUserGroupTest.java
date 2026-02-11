package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Update User Group functionality.
 * 
 * Total Tests: 15
 *
 * Tests cover:
 * - Successful user group updates with various inputs
 * - Validation for non-existent groups, invalid IDs, and duplicate names
 * - Edge cases (special characters, same name updates, max long ID)
 * - Repository interaction verification (delete old mappings, save new ones)
 */
@DisplayName("UserGroupService - UpdateUserGroup Tests")
public class UpdateUserGroupTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("updateUserGroup - Verify @PreAuthorize Annotation")
    void updateUserGroup_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("updateUserGroup", Long.class, UserGroupRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updateUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for UPDATE_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("updateUserGroup - Controller delegates to service")
    void updateUserGroup_WithValidRequest_DelegatesToService() {
        // Arrange
        testUserGroupRequest.setId(TEST_GROUP_ID);
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(Collections.singletonList(testMapping));
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        ResponseEntity<?> response = userGroupController.updateUserGroup(TEST_GROUP_ID, testUserGroupRequest);

        // Assert
        verify(userGroupService).updateUserGroup(testUserGroupRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("Update User Group - Same Name Different Group - Allowed")
    void updateUserGroup_SameNameSameGroup_Allowed() {
        // Arrange
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        List<UserGroupUserMap> existingMappings = Collections.singletonList(testMapping);

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, testUserGroup);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(existingMappings);
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Update User Group - Special chars in name - Success")
    void updateUserGroup_SpecialCharsInName_Success() {
        // Arrange
        String specialName = "Updated @#$ Group";
        testUserGroupRequest.setGroupName(specialName);
        List<UserGroupUserMap> existingMappings = Collections.singletonList(testMapping);

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName(specialName, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(existingMappings);
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Update User Group - Success")
    void updateUserGroup_Success() {
        // Arrange
        testUserGroupRequest.setGroupName("Updated Group Name");
        testUserGroupRequest.setDescription("Updated Description");
        List<UserGroupUserMap> existingMappings = Collections.singletonList(testMapping);

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName("Updated Group Name", null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(existingMappings);
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));

        // Assert
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Update User Group - Verify new mappings created")
    void updateUserGroup_VerifyNewMappingsCreated() {
        // Arrange
        List<UserGroupUserMap> existingMappings = Collections.singletonList(testMapping);

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(existingMappings);
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.updateUserGroup(testUserGroupRequest);

        // Assert
        verify(userGroupUserMapRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Update User Group - Verify old mappings deleted")
    void updateUserGroup_VerifyOldMappingsDeleted() {
        // Arrange
        List<UserGroupUserMap> existingMappings = Collections.singletonList(testMapping);

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName(TEST_GROUP_NAME, null);
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositoryFindByGroupId(existingMappings);
        stubUserGroupUserMapRepositoryDeleteAll();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.updateUserGroup(testUserGroupRequest);

        // Assert
        verify(userGroupUserMapRepository).deleteAll(anyList());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("Update User Group - Duplicate Name - Throws BadRequestException")
    void updateUserGroup_DuplicateName_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName("Existing Group");
        UserGroup existingGroupWithSameName = new UserGroup();
        existingGroupWithSameName.setGroupId(999L); // Different ID
        existingGroupWithSameName.setGroupName("Existing Group");

        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositoryFindByGroupName("Existing Group", existingGroupWithSameName);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Max Long ID - Throws NotFoundException")
    void updateUserGroup_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(Long.MAX_VALUE);
        stubUserGroupRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Negative ID - Throws NotFoundException")
    void updateUserGroup_NegativeId_ThrowsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(-1L);
        stubUserGroupRepositoryFindById(-1L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - No Users - Throws BadRequestException")
    void updateUserGroup_NoUsers_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Not Found - Throws NotFoundException")
    void updateUserGroup_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Null Name - Throws BadRequestException")
    void updateUserGroup_NullName_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName(null);
        // Need to mock findById to return the existing group so validation proceeds to
        // the name check
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Null Users - Throws BadRequestException")
    void updateUserGroup_NullUsers_ThrowsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Zero ID - Throws NotFoundException")
    void updateUserGroup_ZeroId_ThrowsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(0L);
        stubUserGroupRepositoryFindById(0L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }
}
