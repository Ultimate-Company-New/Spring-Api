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
 * Unit tests for UserGroupService - ToggleUserGroup functionality.
 *
 * Test Summary:
 * - Controller authorization and delegation
 * - Successful toggle (soft delete/restore) operations
 * - State persistence check
 * - Logging verification
 * - Failure cases (group not found, invalid IDs)
 */
@DisplayName("UserGroupService - ToggleUserGroup Tests")
public class ToggleUserGroupTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("toggleUserGroup - Verify @PreAuthorize Annotation")
    void toggleUserGroup_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("toggleUserGroup", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on toggleUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.DELETE_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for DELETE_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("toggleUserGroup - Controller delegates to service")
    void toggleUserGroup_WithValidId_DelegatesToService() {
        // Arrange
        stubUserGroupRepositoryFindById(TEST_GROUP_ID, Optional.of(testUserGroup));
        stubUserGroupRepositorySave(testUserGroup);
        stubUserLogServiceLogData(true);

        // Act
        ResponseEntity<?> response = userGroupController.toggleUserGroup(TEST_GROUP_ID);

        // Assert
        verify(userGroupService).toggleUserGroup(TEST_GROUP_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Toggle multiple times and verify state persists.
     * Expected Result: State toggles correctly each time.
     * Assertions: assertTrue/assertFalse for each toggle.
     */
    @Test
    @DisplayName("Toggle User Group - Multiple Toggles - State Persistence")
    void toggleUserGroup_MultipleToggles_StatePersists() {
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
     * Assertions: assertFalse(testUserGroup.getIsDeleted());
     */
    @Test
    @DisplayName("Toggle User Group - Restores deleted group")
    void toggleUserGroup_RestoresDeletedGroup() {
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
     * Assertions: assertDoesNotThrow(); verify save called.
     */
    @Test
    @DisplayName("Toggle User Group - Success - Should toggle isDeleted flag")
    void toggleUserGroup_Success() {
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
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Toggle User Group - Success - Logs operation")
    void toggleUserGroup_Success_LogsOperation() {
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
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Toggle User Group - Verify logging called")
    void toggleUserGroup_VerifyLoggingCalled() {
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
     * Assertions: verify calls.
     */
    @Test
    @DisplayName("Toggle User Group - Verify repository called")
    void toggleUserGroup_VerifyRepositoryCalled() {
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
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User Group - Failure - Group not found")
    void toggleUserGroup_GroupNotFound_ThrowsNotFoundException() {
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
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User Group - Max Long ID - Not Found")
    void toggleUserGroup_MaxLongId_ThrowsNotFoundException() {
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
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User Group - Min Long ID - Not Found")
    void toggleUserGroup_MinLongId_ThrowsNotFoundException() {
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
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User Group - Negative ID - Not Found")
    void toggleUserGroup_NegativeId_ThrowsNotFoundException() {
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
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User Group - Zero ID - Not Found")
    void toggleUserGroup_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindById(0L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.toggleUserGroup(0L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }
}
