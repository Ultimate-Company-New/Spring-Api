package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.User;
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

@DisplayName("UserService - ConfirmEmail Tests")
class ConfirmEmailTest extends UserServiceTestBase {
    // Total Tests: 13

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify repository is called exactly once to find user.
     * Expected Result: findById is called once.
     * Assertions: verify(userRepository, times(1)).findById(TEST_USER_ID);
     */
    @Test
    @DisplayName("Confirm Email - Repository findById called once - Success")
    void confirmEmail_findByIdCalledOnce_Success() {
        // Arrange
        User userToConfirm = new User(testUserRequest, CREATED_USER);
        userToConfirm.setUserId(TEST_USER_ID);
        userToConfirm.setToken("valid-token");
        userToConfirm.setEmailConfirmed(false);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userToConfirm));
        stubUserRepositorySave(userToConfirm);

        // Act
        userService.confirmEmail(TEST_USER_ID, "valid-token");

        // Assert
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    /**
     * Purpose: Verify null emailConfirmed is treated as false and updated.
     * Expected Result: Email is confirmed successfully.
     * Assertions: assertTrue(user.getEmailConfirmed());
     */
    @Test
    @DisplayName("Confirm Email - Null emailConfirmed - Success")
    void confirmEmail_nullEmailConfirmed_success() {
        // Arrange
        User userWithNullConfirmed = new User(testUserRequest, CREATED_USER);
        userWithNullConfirmed.setUserId(TEST_USER_ID);
        userWithNullConfirmed.setToken("valid-token");
        userWithNullConfirmed.setEmailConfirmed(null);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userWithNullConfirmed));
        stubUserRepositorySave(userWithNullConfirmed);

        // Act
        assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

        // Assert
        assertTrue(userWithNullConfirmed.getEmailConfirmed());
        verify(userRepository, times(1)).save(userWithNullConfirmed);
    }

    /**
     * Purpose: Verify successful email confirmation.
     * Expected Result: emailConfirmed is set to true.
     * Assertions: assertTrue(user.getEmailConfirmed());
     */
    @Test
    @DisplayName("Confirm Email - Success - Sets emailConfirmed to true")
    void confirmEmail_success_setsEmailConfirmedToTrue() {
        // Arrange
        User userToConfirm = new User(testUserRequest, CREATED_USER);
        userToConfirm.setUserId(TEST_USER_ID);
        userToConfirm.setToken("valid-token");
        userToConfirm.setEmailConfirmed(false);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userToConfirm));
        stubUserRepositorySave(userToConfirm);

        // Act
        assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

        // Assert
        assertTrue(userToConfirm.getEmailConfirmed());
        verify(userRepository, times(1)).save(userToConfirm);
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify already confirmed email throws BadRequestException.
     * Expected Result: BadRequestException with "Account has already been
     * confirmed" message.
     */
    @Test
    @DisplayName("Confirm Email - Already Confirmed - Throws BadRequestException")
    void confirmEmail_alreadyConfirmed_throwsBadRequestException() {
        // Arrange
        User alreadyConfirmedUser = new User(testUserRequest, CREATED_USER);
        alreadyConfirmedUser.setUserId(TEST_USER_ID);
        alreadyConfirmedUser.setToken("valid-token");
        alreadyConfirmedUser.setEmailConfirmed(true);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(alreadyConfirmedUser));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

        // Assert
        assertEquals(ErrorMessages.LoginErrorMessages.AccountConfirmed, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify empty token string throws BadRequestException.
     * Expected Result: BadRequestException with "Invalid token" message.
     */
    @Test
    @DisplayName("Confirm Email - Empty Token - Throws BadRequestException")
    void confirmEmail_emptyToken_throwsBadRequestException() {
        // Arrange
        User userWithToken = new User(testUserRequest, CREATED_USER);
        userWithToken.setUserId(TEST_USER_ID);
        userWithToken.setToken("valid-token");
        userWithToken.setEmailConfirmed(false);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userWithToken));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.confirmEmail(TEST_USER_ID, ""));

        // Assert
        assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
    }

    /**
     * Purpose: Verify max long user ID throws NotFoundException when not found.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Confirm Email - Max Long User ID - Throws NotFoundException")
    void confirmEmail_maxLongUserId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.confirmEmail(Long.MAX_VALUE, "valid-token"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative user ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Confirm Email - Negative User ID - Throws NotFoundException")
    void confirmEmail_negativeUserId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindById(-1L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.confirmEmail(-1L, "valid-token"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify null token throws BadRequestException.
     * Expected Result: BadRequestException with "Invalid token" message.
     */
    @Test
    @DisplayName("Confirm Email - Null Token in DB - Throws BadRequestException")
    void confirmEmail_nullToken_throwsBadRequestException() {
        // Arrange
        User userWithNullToken = new User(testUserRequest, CREATED_USER);
        userWithNullToken.setUserId(TEST_USER_ID);
        userWithNullToken.setToken(null);
        userWithNullToken.setEmailConfirmed(false);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userWithNullToken));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.confirmEmail(TEST_USER_ID, "any-token"));

        // Assert
        assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify token mismatch throws BadRequestException.
     * Expected Result: BadRequestException with "Invalid token" message.
     */
    @Test
    @DisplayName("Confirm Email - Token Mismatch - Throws BadRequestException")
    void confirmEmail_tokenMismatch_throwsBadRequestException() {
        // Arrange
        User userWithDifferentToken = new User(testUserRequest, CREATED_USER);
        userWithDifferentToken.setUserId(TEST_USER_ID);
        userWithDifferentToken.setToken("correct-token");
        userWithDifferentToken.setEmailConfirmed(false);

        stubUserRepositoryFindById(TEST_USER_ID, Optional.of(userWithDifferentToken));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.confirmEmail(TEST_USER_ID, "wrong-token"));

        // Assert
        assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Confirm Email - User Not Found - Throws NotFoundException")
    void confirmEmail_userNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindById(TEST_USER_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify zero user ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Confirm Email - Zero User ID - Throws NotFoundException")
    void confirmEmail_zeroUserId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindById(0L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.confirmEmail(0L, "valid-token"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller has @PreAuthorize annotation.
     * Expected Result: Annotation is present and permitAll().
     * Assertions: assertNotNull, assertEquals("permitAll()", annotation.value()),
     * HTTP status OK.
     */
    @Test
    @DisplayName("confirmEmail - Verify @PreAuthorize Annotation")
    void confirmEmail_controller_permission_success() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("confirmEmail", Long.class, String.class);
        Long userId = 1L;
        String token = "valid-token";
        UserController localController = new UserController(mockUserService);
        stubMockUserServiceConfirmEmail(userId, token);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        ResponseEntity<?> response = localController.confirmEmail(userId, token);

        // Assert
        assertNotNull(annotation, "confirmEmail method should have @PreAuthorize annotation");
        assertEquals("permitAll()", annotation.value());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).confirmEmail(userId, token); HTTP status OK.
     */
    @Test
    @DisplayName("confirmEmail - Controller delegates to service")
    void confirmEmail_withValidToken_delegatesToService() {
        // Arrange
        Long userId = 1L;
        String token = "valid-token";
        UserController localController = new UserController(mockUserService);
        stubMockUserServiceConfirmEmail(userId, token);

        // Act
        ResponseEntity<?> response = localController.confirmEmail(userId, token);

        // Assert
        verify(mockUserService, times(1)).confirmEmail(userId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
