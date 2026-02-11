package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Total Tests: 9
@DisplayName("UserService - Toggle User Tests")
class ToggleUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with DELETE_USER_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("toggleUser - Verify @PreAuthorize Annotation")
    void toggleUser_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("toggleUser", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "toggleUser method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.DELETE_USER_PERMISSION),
                "@PreAuthorize annotation should check for DELETE_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).toggleUser(userId);
     */
    @Test
    @DisplayName("toggleUser - Controller delegates to service")
    void toggleUser_WithValidId_DelegatesToService() {
        // Arrange
        Long userId = 1L;
        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        doNothing().when(mockUserService).toggleUser(userId);

        // Act
        localController.toggleUser(userId);

        // Assert
        verify(mockUserService, times(1)).toggleUser(userId);
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify that user log is called after successful toggle.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Toggle User - Success - Logs the operation")
    void toggleUser_success_logsOperation() {
        // Arrange
        testUser.setIsDeleted(false);
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserLogServiceLogData(true);

        // Act
        userService.toggleUser(TEST_USER_ID);

        // Assert
        verify(userLogService, times(1)).logData(anyLong(), contains("deletion status"), anyString());
    }

    /**
     * Purpose: Verify that multiple toggles persist state correctly.
     * Expected Result: User state toggles between deleted and active.
     * Assertions: State alternates correctly.
     */
    @Test
    @DisplayName("Toggle User - Multiple Toggles - State Persists")
    void toggleUser_multipleToggles_statePersists() {
        // Arrange
        testUser.setIsDeleted(false);
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserLogServiceLogData(true);

        // Act & Assert 1
        userService.toggleUser(TEST_USER_ID);
        assertTrue(testUser.getIsDeleted());

        // Act & Assert 2
        userService.toggleUser(TEST_USER_ID);
        assertFalse(testUser.getIsDeleted());

        // Act & Assert 3
        userService.toggleUser(TEST_USER_ID);
        assertTrue(testUser.getIsDeleted());
    }

    /**
     * Purpose: Verify that a deleted user can be restored.
     * Expected Result: The user's isDeleted status is set to false.
     * Assertions: assertFalse(testUser.getIsDeleted());
     */
    @Test
    @DisplayName("Toggle User - Success - Should restore deleted user")
    void toggleUser_success_restoresDeletedUser() {
        // Arrange
        testUser.setIsDeleted(true);
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserLogServiceLogData(true);

        // Act
        userService.toggleUser(TEST_USER_ID);

        // Assert
        assertFalse(testUser.getIsDeleted());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    /**
     * Purpose: Verify that a user can be successfully toggled (soft deleted).
     * Expected Result: The user's isDeleted status is set to true.
     * Assertions: assertTrue(testUser.getIsDeleted());
     */
    @Test
    @DisplayName("Toggle User - Success - Should set isDeleted to true")
    void toggleUser_success_setsIsDeletedTrue() {
        // Arrange
        testUser.setIsDeleted(false);
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserLogServiceLogData(true);

        // Act
        userService.toggleUser(TEST_USER_ID);

        // Assert
        assertTrue(testUser.getIsDeleted());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    /**
     * Purpose: Verify that modifiedUser is updated on toggle.
     * Expected Result: modifiedUser field is updated.
     * Assertions: verify setModifiedUser is called.
     */
    @Test
    @DisplayName("Toggle User - Success - Updates modifiedUser")
    void toggleUser_success_updatesModifiedUser() {
        // Arrange
        testUser.setIsDeleted(false);
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserLogServiceLogData(true);

        // Act
        userService.toggleUser(TEST_USER_ID);

        // Assert
        assertNotNull(testUser.getModifiedUser());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify that toggling user with max long ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Toggle User - Max Long ID - Throws NotFoundException")
    void toggleUser_maxLongId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling user with negative ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Toggle User - Negative ID - Throws NotFoundException")
    void toggleUser_negativeId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(-1L));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling a non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Toggle User - User Not Found - Throws NotFoundException")
    void toggleUser_userNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(TEST_USER_ID));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify that toggling user with zero ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Toggle User - Zero ID - Throws NotFoundException")
    void toggleUser_zeroId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(0L));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }
}
