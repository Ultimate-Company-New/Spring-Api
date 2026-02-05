package com.example.SpringApi.Services.Tests.Login;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResetPassword functionality in LoginService.
 * Test Count: 16 tests
 */
@DisplayName("Reset Password Tests")
public class ResetPasswordTest extends LoginServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful password reset and email trigger.
     * Expected Result: Password is reset and email is sent.
     * Assertions: UserRepository and ClientRepository are queried, and returns true.
     */
    @Test
    @DisplayName("Reset Password - Success - Should reset password and send email")
    void resetPassword_Success() {
        // Arrange
        testUser.setPassword("oldHashedPassword");
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);

        when(environment.getProperty("email.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("email.sender.name")).thenReturn("Test Sender");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("test-api-key");

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("newPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                    .thenReturn(new String[] { "newSalt", "newHashedPassword" });

            try (MockedConstruction<EmailTemplates> mockedEmailTemplates = mockConstruction(EmailTemplates.class,
                    (mock, context) -> {
                        when(mock.sendResetPasswordEmail(anyString(), anyString())).thenReturn(true);
                    })) {

                // Act
                Boolean result = loginService.resetPassword(testLoginRequest);

                // Assert
                assertTrue(result);
                verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
            }
        }
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Handle failure when reset password email cannot be sent via the helper.
     * Expected Result: RuntimeException is thrown.
     * Assertions: Error message matches "Failed to send reset password email".
     */
    @Test
    @DisplayName("Reset Password - Email send failure - Throws RuntimeException")
    void resetPassword_EmailSendFailure_ThrowsRuntimeException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);
        when(environment.getProperty("email.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("email.sender.name")).thenReturn("Sender");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("key");

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class);
             MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                (mock, context) -> when(mock.sendResetPasswordEmail(anyString(), anyString())).thenReturn(false))) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("newpass");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[] {"salt", "hash"});

            RuntimeException exception = assertThrows(RuntimeException.class,
                () -> loginService.resetPassword(testLoginRequest));

            assertEquals("Failed to send reset password email", exception.getMessage());
        }
    }

    /**
     * Purpose: Reject reset when user password in database is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER003.
     */
    @Test
    @DisplayName("Reset Password - Failure - User has empty password")
    void resetPassword_EmptyPasswordSet_ThrowsBadRequestException() {
        testUser.setPassword("");
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
    }

    /**
     * Purpose: Reject missing Brevo API key when email service is configured to brevo.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches BrevoApiKeyNotConfigured.
     */
    @Test
    @DisplayName("Reset Password - Missing Brevo API key - Throws BadRequestException")
    void resetPassword_MissingBrevoApiKey_ThrowsBadRequestException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("brevo");
        when(environment.getProperty("brevo.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("brevo.sender.name")).thenReturn("Sender");
        when(environment.getProperty("brevo.api.key")).thenReturn("");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> loginService.resetPassword(testLoginRequest));

        assertEquals(ErrorMessages.ConfigurationErrorMessages.BrevoApiKeyNotConfigured, exception.getMessage());
    }

    /**
     * Purpose: Reject reset when login name is an empty string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Missing login name")
    void resetPassword_MissingLoginName_ThrowsBadRequestException() {
        testLoginRequest.setLoginName("");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
    }

    /**
     * Purpose: Reject missing SendGrid API key configuration.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches SendGridApiKeyNotConfigured.
     */
    @Test
    @DisplayName("Reset Password - Missing SendGrid API key - Throws BadRequestException")
    void resetPassword_MissingSendGridApiKey_ThrowsBadRequestException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);
        when(environment.getProperty("email.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("email.sender.name")).thenReturn("Sender");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> loginService.resetPassword(testLoginRequest));

        assertEquals(ErrorMessages.ConfigurationErrorMessages.SendGridApiKeyNotConfigured, exception.getMessage());
    }

    /**
     * Purpose: Reject missing sender email configuration in environment.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches SendGridEmailNotConfigured.
     */
    @Test
    @DisplayName("Reset Password - Missing sender email - Throws BadRequestException")
    void resetPassword_MissingSenderEmail_ThrowsBadRequestException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);
        when(environment.getProperty("email.sender.address")).thenReturn(null);
        when(environment.getProperty("email.sender.name")).thenReturn("Sender");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("key");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> loginService.resetPassword(testLoginRequest));

        assertEquals(ErrorMessages.ConfigurationErrorMessages.SendGridEmailNotConfigured, exception.getMessage());
    }

    /**
     * Purpose: Reject missing sender name configuration in environment.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches SendGridNameNotConfigured.
     */
    @Test
    @DisplayName("Reset Password - Missing sender name - Throws BadRequestException")
    void resetPassword_MissingSenderName_ThrowsBadRequestException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);
        when(environment.getProperty("email.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("email.sender.name")).thenReturn(" ");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("key");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> loginService.resetPassword(testLoginRequest));

        assertEquals(ErrorMessages.ConfigurationErrorMessages.SendGridNameNotConfigured, exception.getMessage());
    }

    /**
     * Purpose: Reject reset attempts when no client configuration is found.
     * Expected Result: RuntimeException is thrown.
     * Assertions: Error message matches NoClientConfigurationFound.
     */
    @Test
    @DisplayName("Reset Password - Missing client configuration - Throws RuntimeException")
    void resetPassword_NoClientConfiguration_ThrowsRuntimeException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> loginService.resetPassword(testLoginRequest));

        assertEquals(ErrorMessages.ConfigurationErrorMessages.NoClientConfigurationFound, exception.getMessage());
    }

    /**
     * Purpose: Reject reset when user has no password set (null).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER003.
     */
    @Test
    @DisplayName("Reset Password - Failure - User has no password set")
    void resetPassword_NoPasswordSet_ThrowsBadRequestException() {
        testUser.setPassword(null);
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
    }

    /**
     * Purpose: Reject reset when the login name in request is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Null login name")
    void resetPassword_NullLoginName_ThrowsBadRequestException() {
        testLoginRequest.setLoginName(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
    }

    /**
     * Purpose: Validate null request handling for reset.
     * Expected Result: NullPointerException is thrown.
     * Assertions: Exception type is NullPointerException.
     */
    @Test
    @DisplayName("Reset Password - Failure - Null request")
    void resetPassword_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> loginService.resetPassword(null));
    }

    /**
     * Purpose: Validate reset fails for non-existent users.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Reset Password - Failure - User not found")
    void resetPassword_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidEmail, exception.getMessage());
    }

    /**
     * Purpose: Reject reset when login name consists only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Whitespace only login name")
    void resetPassword_WhitespaceLoginName_ThrowsBadRequestException() {
        testLoginRequest.setLoginName("   ");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
    }
}