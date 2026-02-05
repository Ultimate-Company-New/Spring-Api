package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - ConfirmEmail functionality.
 *
 * Test Summary:
 * | Test Group          | Number of Tests |
 * | :------------------ | :-------------- |
 * | SUCCESS Tests       | 3               |
 * | FAILURE Tests       | 8               |
 * | **Total**           | **11**          |
 */
@DisplayName("UserService - ConfirmEmail Tests")
class ConfirmEmailTest extends UserServiceTestBase {

    @Nested
    @DisplayName("SUCCESS Tests")
    class SuccessTests {

        // ========================================
        // SUCCESS TESTS
        // ========================================

        /**
         * Purpose: Verify null emailConfirmed is treated as false.
         * Expected Result: Email is confirmed successfully.
         * Assertions: assertTrue(user.getEmailConfirmed());
         */
        @Test
        @DisplayName("Confirm Email - Null emailConfirmed - Success")
        void confirmEmail_NullEmailConfirmed_Success() {
            User userWithNullConfirmed = new User(testUserRequest, CREATED_USER);
            userWithNullConfirmed.setUserId(TEST_USER_ID);
            userWithNullConfirmed.setToken("valid-token");
            userWithNullConfirmed.setEmailConfirmed(null);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullConfirmed));
            when(userRepository.save(any(User.class))).thenReturn(userWithNullConfirmed);

            assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertTrue(userWithNullConfirmed.getEmailConfirmed());
            verify(userRepository, times(1)).save(userWithNullConfirmed);
        }

        /**
         * Purpose: Verify repository is called exactly once to find user.
         * Expected Result: findById is called once.
         * Assertions: verify(userRepository, times(1)).findById(TEST_USER_ID);
         */
        @Test
        @DisplayName("Confirm Email - Repository findById called once")
        void confirmEmail_FindByIdCalledOnce() {
            User userToConfirm = new User(testUserRequest, CREATED_USER);
            userToConfirm.setUserId(TEST_USER_ID);
            userToConfirm.setToken("valid-token");
            userToConfirm.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userToConfirm));
            when(userRepository.save(any(User.class))).thenReturn(userToConfirm);

            userService.confirmEmail(TEST_USER_ID, "valid-token");

            verify(userRepository, times(1)).findById(TEST_USER_ID);
        }

        /**
         * Purpose: Verify successful email confirmation.
         * Expected Result: emailConfirmed is set to true.
         * Assertions: assertTrue(user.getEmailConfirmed());
         */
        @Test
        @DisplayName("Confirm Email - Success - Sets emailConfirmed to true")
        void confirmEmail_Success() {
            User userToConfirm = new User(testUserRequest, CREATED_USER);
            userToConfirm.setUserId(TEST_USER_ID);
            userToConfirm.setToken("valid-token");
            userToConfirm.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userToConfirm));
            when(userRepository.save(any(User.class))).thenReturn(userToConfirm);

            assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertTrue(userToConfirm.getEmailConfirmed());
            verify(userRepository, times(1)).save(userToConfirm);
        }
    }

    @Nested
    @DisplayName("FAILURE Tests")
    class FailureTests {

        // ========================================
        // FAILURE TESTS
        // ========================================

        /**
         * Purpose: Verify already confirmed email throws BadRequestException.
         * Expected Result: BadRequestException with "Account has already been confirmed"
         * message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.AccountConfirmed,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Already Confirmed - Throws BadRequestException")
        void confirmEmail_AlreadyConfirmed_ThrowsBadRequestException() {
            User alreadyConfirmedUser = new User(testUserRequest, CREATED_USER);
            alreadyConfirmedUser.setUserId(TEST_USER_ID);
            alreadyConfirmedUser.setToken("valid-token");
            alreadyConfirmedUser.setEmailConfirmed(true);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(alreadyConfirmedUser));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.AccountConfirmed, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify empty token string throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Empty Token - Throws BadRequestException")
        void confirmEmail_EmptyToken_ThrowsBadRequestException() {
            User userWithToken = new User(testUserRequest, CREATED_USER);
            userWithToken.setUserId(TEST_USER_ID);
            userWithToken.setToken("valid-token");
            userWithToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, ""));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
        }

        /**
         * Purpose: Verify max long user ID throws NotFoundException when not found.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Max Long User ID - Throws NotFoundException")
        void confirmEmail_MaxLongUserId_ThrowsNotFoundException() {
            when(userRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(Long.MAX_VALUE, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify negative user ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Negative User ID - Throws NotFoundException")
        void confirmEmail_NegativeUserId_ThrowsNotFoundException() {
            when(userRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(-1L, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null token throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Null Token in DB - Throws BadRequestException")
        void confirmEmail_NullToken_ThrowsBadRequestException() {
            User userWithNullToken = new User(testUserRequest, CREATED_USER);
            userWithNullToken.setUserId(TEST_USER_ID);
            userWithNullToken.setToken(null);
            userWithNullToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "any-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify token mismatch throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Token Mismatch - Throws BadRequestException")
        void confirmEmail_TokenMismatch_ThrowsBadRequestException() {
            User userWithDifferentToken = new User(testUserRequest, CREATED_USER);
            userWithDifferentToken.setUserId(TEST_USER_ID);
            userWithDifferentToken.setToken("correct-token");
            userWithDifferentToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithDifferentToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "wrong-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify non-existent user throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - User Not Found - Throws NotFoundException")
        void confirmEmail_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify zero user ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Zero User ID - Throws NotFoundException")
        void confirmEmail_ZeroUserId_ThrowsNotFoundException() {
            when(userRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(0L, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }
    }
}
