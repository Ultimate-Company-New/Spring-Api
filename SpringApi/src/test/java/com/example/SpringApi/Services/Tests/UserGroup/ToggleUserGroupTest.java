package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService.toggleUserGroup method.
 * 
 * Total Tests: 14
 */
@DisplayName("UserGroupService - ToggleUserGroup Tests")
class ToggleUserGroupTest extends UserGroupServiceTestBase {
    // Total Tests: 14

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Toggle multiple times and verify state persists.
     * Expected Result: State toggles correctly each time.
     * Assertions: assertTrue, assertFalse
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Multiple Toggles")
    void toggleUserGroup_multipleToggles_statePersists() {
        // Arrange
        testUserGroup.setIsDeleted(false);
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act & Assert
        // First Toggle: false -> true
        userGroupService.toggleUserGroup(TEST_GROUP_ID);
        assertTrue(testUserGroup.getIsDeleted());

        // Second Toggle: true -> false
        userGroupService.toggleUserGroup(TEST_GROUP_ID);
        assertFalse(testUserGroup.getIsDeleted());
    }

    /**
     * Purpose: Verify deleted group can be restored.
     * Expected Result: isDeleted changes from true to false.
     * Assertions: assertFalse
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Restore Group")
    void toggleUserGroup_restoresDeletedGroup_success() {
        // Arrange
        testUserGroup.setIsDeleted(true);
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        assertFalse(testUserGroup.getIsDeleted());
    }

    /**
     * Purpose: Verify successful toggle operation.
     * Expected Result: isDeleted flag is toggled.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Basic Validation")
    void toggleUserGroup_success_basicValidation() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        assertDoesNotThrow(() -> userGroupService.toggleUserGroup(TEST_GROUP_ID));

        // Assert
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
    }

    /**
     * Purpose: Verify operation is logged.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Verify Logging")
    void toggleUserGroup_success_verifyLogging() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        verify(userLogService).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify logging is called.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Verify Logging Called")
    void toggleUserGroup_verifyLoggingCalled_success() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        verify(userLogService).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify repository methods are called.
     * Expected Result: findById and save are called once each.
     * Assertions: verify
     */
    @Test
    @DisplayName("toggleUserGroup - Success - Verify Repository")
    void toggleUserGroup_verifyRepositoryCalled_success() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        verify(userGroupRepository, times(1)).findById(TEST_GROUP_ID);
        verify(userGroupRepository, times(1)).save(testUserGroup);
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify group not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("toggleUserGroup - Failure - Group Not Found")
    void toggleUserGroup_groupNotFound_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(TEST_GROUP_ID));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify max long ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("toggleUserGroup - Failure - Max Long ID")
    void toggleUserGroup_maxLongId_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(Long.MAX_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify min long ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("toggleUserGroup - Failure - Min Long ID")
    void toggleUserGroup_minLongId_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(Long.MIN_VALUE, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(Long.MIN_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("toggleUserGroup - Failure - Negative ID")
    void toggleUserGroup_negativeId_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(-1L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(-1L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("toggleUserGroup - Failure - Zero ID")
    void toggleUserGroup_zeroId_throwsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(0L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(0L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode())
     */
    @Test
    @DisplayName("toggleUserGroup - Controller permission forbidden")
    void toggleUserGroup_controller_permission_forbidden() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with
     * DELETE_GROUPS_PERMISSION.
     * Assertions: assertNotNull, assertTrue
     */
    @Test
    @DisplayName("toggleUserGroup - Verify @PreAuthorize Annotation")
    void toggleUserGroup_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("toggleUserGroup", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on toggleUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.DELETE_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for DELETE_GROUPS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called and HTTP 200 is returned.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("toggleUserGroup - Controller delegates to service")
    void toggleUserGroup_withValidId_delegatesToService() {
        // Arrange
        stubMockUserGroupServiceToggleUserGroup(TEST_GROUP_ID);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        verify(mockUserGroupService).toggleUserGroup(TEST_GROUP_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
