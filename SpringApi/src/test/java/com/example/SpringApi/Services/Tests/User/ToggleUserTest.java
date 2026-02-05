package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - Toggle User functionality.
 * 
 * Tests: 9
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@DisplayName("UserService - Toggle User Tests")
class ToggleUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("toggleUser - Verify @PreAuthorize Annotation")
    void toggleUser_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("toggleUser", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on toggleUser method");
        assertTrue(annotation.value().contains(Authorizations.DELETE_USER_PERMISSION),
                "@PreAuthorize annotation should check for DELETE_USER_PERMISSION");
    }

    @Test
    @DisplayName("toggleUser - Controller delegates to service")
    void toggleUser_WithValidId_DelegatesToService() {
        UserController controller = new UserController(userService);
        Long userId = 1L;
        doNothing().when(userService).toggleUser(userId);

        ResponseEntity<?> response = controller.toggleUser(userId);

        verify(userService).toggleUser(userId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    // ========================================
    // SUCCESS Tests
    // ========================================

    /**
     * Purpose: Verify that user log is called after successful toggle.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Toggle User - Success - Logs the operation")
    void toggleUser_Success_LogsOperation() {
        testUser.setIsDeleted(false);
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        userService.toggleUser(TEST_USER_ID);

        verify(userLogService, times(1)).logData(anyLong(), contains("deletion status"), anyString());
    }

    /**
     * Purpose: Verify that multiple toggles persist state correctly.
     * Expected Result: User state toggles between deleted and active.
     * Assertions: State alternates correctly.
     */
    @Test
    @DisplayName("Toggle User - Multiple Toggles - State Persists")
    void toggleUser_MultipleToggles_StatePersists() {
        testUser.setIsDeleted(false);
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        userService.toggleUser(TEST_USER_ID);
        assertTrue(testUser.getIsDeleted());

        userService.toggleUser(TEST_USER_ID);
        assertFalse(testUser.getIsDeleted());

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
    void toggleUser_Success_RestoresDeletedUser() {
        testUser.setIsDeleted(true);
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.toggleUser(TEST_USER_ID);

        assertFalse(testUser.getIsDeleted());
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Purpose: Verify that a user can be successfully toggled (soft deleted).
     * Expected Result: The user's isDeleted status is set to true.
     * Assertions: assertTrue(testUser.getIsDeleted());
     */
    @Test
    @DisplayName("Toggle User - Success - Should set isDeleted to true")
    void toggleUser_Success_SetsIsDeletedTrue() {
        testUser.setIsDeleted(false);
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.toggleUser(TEST_USER_ID);

        assertTrue(testUser.getIsDeleted());
        verify(userRepository, times(1)).save(testUser);
    }

    /**
     * Purpose: Verify that modifiedUser is updated on toggle.
     * Expected Result: modifiedUser field is updated.
     * Assertions: verify setModifiedUser is called.
     */
    @Test
    @DisplayName("Toggle User - Success - Updates modifiedUser")
    void toggleUser_Success_UpdatesModifiedUser() {
        testUser.setIsDeleted(false);
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.toggleUser(TEST_USER_ID);

        assertNotNull(testUser.getModifiedUser());
        verify(userRepository, times(1)).save(testUser);
    }

    // ========================================
    // FAILURE Tests
    // ========================================

    /**
     * Purpose: Verify that toggling user with max long ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User - Max Long ID - Throws NotFoundException")
    void toggleUser_MaxLongId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(Long.MAX_VALUE), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(Long.MAX_VALUE));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling user with negative ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User - Negative ID - Throws NotFoundException")
    void toggleUser_NegativeId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(-1L), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(-1L));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling a non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User - User Not Found - Throws NotFoundException")
    void toggleUser_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(TEST_USER_ID));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify that toggling user with zero ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Toggle User - Zero ID - Throws NotFoundException")
    void toggleUser_ZeroId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(0L), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.toggleUser(0L));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }
}
