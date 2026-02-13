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
 * Unit tests for UserGroupService.updateUserGroup method.
 * 
 */
@DisplayName("UserGroupService - UpdateUserGroup Tests")
class UpdateUserGroupTest extends UserGroupServiceTestBase {

    // Total Tests: 15
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify group can keep same name when updating.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("updateUserGroup - Success - Same Name Allowed")
    void updateUserGroup_sameNameSameGroup_allowed() {
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

    /**
     * Purpose: Verify special characters in group name are allowed.
     * Expected Result: No exception thrown, save is called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("updateUserGroup - Success - Special Chars In Name")
    void updateUserGroup_specialCharsInName_success() {
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

    /**
     * Purpose: Verify successful update operation.
     * Expected Result: No exception thrown, save called.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("updateUserGroup - Success - Basic Validation")
    void updateUserGroup_success_basicValidation() {
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

    /**
     * Purpose: Verify new user-group mappings are created.
     * Expected Result: saveAll is called on userGroupUserMapRepository.
     * Assertions: verify
     */
    @Test
    @DisplayName("updateUserGroup - Success - Verify New Mappings")
    void updateUserGroup_verifyNewMappingsCreated_success() {
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

    /**
     * Purpose: Verify old user-group mappings are deleted.
     * Expected Result: deleteAll is called on userGroupUserMapRepository.
     * Assertions: verify
     */
    @Test
    @DisplayName("updateUserGroup - Success - Verify Old Mappings Deleted")
    void updateUserGroup_verifyOldMappingsDeleted_success() {
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

    /**
     * Purpose: Verify duplicate group name throws BadRequestException.
     * Expected Result: BadRequestException with GroupNameExists message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Duplicate Name")
    void updateUserGroup_duplicateName_throwsBadRequestException() {
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
        assertEquals(ErrorMessages.UserGroupErrorMessages.GROUP_NAME_EXISTS, ex.getMessage());
    }

    /**
     * Purpose: Verify max long ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Max Long ID")
    void updateUserGroup_maxLongId_throwsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(Long.MAX_VALUE);
        stubUserGroupRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Negative ID")
    void updateUserGroup_negativeId_throwsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(-1L);
        stubUserGroupRepositoryFindById(-1L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify that updating a non-existent group throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Not Found")
    void updateUserGroup_notFound_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));

        // Assert
        assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify no users throws BadRequestException.
     * Expected Result: BadRequestException with ER004 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - No Users")
    void updateUserGroup_noUsers_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(new ArrayList<>());

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Verify null name throws BadRequestException.
     * Expected Result: BadRequestException with ER002 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Null Name")
    void updateUserGroup_nullName_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setGroupName(null);
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Verify null users throws BadRequestException.
     * Expected Result: BadRequestException with ER004 message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Null Users")
    void updateUserGroup_nullUsers_throwsBadRequestException() {
        // Arrange
        testUserGroupRequest.setUserIds(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("updateUserGroup - Failure - Zero ID")
    void updateUserGroup_zeroId_throwsNotFoundException() {
        // Arrange
        testUserGroupRequest.setGroupId(0L);
        stubUserGroupRepositoryFindById(0L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
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
    @DisplayName("updateUserGroup - Controller permission forbidden")
    void updateUserGroup_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        stubServiceThrowsUnauthorizedException();
        Method method = UserGroupController.class.getMethod("updateUserGroup", Long.class, UserGroupRequestModel.class);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.updateUserGroup(TEST_GROUP_ID, testUserGroupRequest);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updateUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for UPDATE_GROUPS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called and HTTP 200 is returned.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("updateUserGroup - Controller delegates to service")
    void updateUserGroup_withValidRequest_delegatesToService() {
        // Arrange
        stubMockUserGroupServiceUpdateUserGroup(testUserGroupRequest);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.updateUserGroup(TEST_GROUP_ID, testUserGroupRequest);

        // Assert
        verify(mockUserGroupService, times(1)).updateUserGroup(testUserGroupRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
