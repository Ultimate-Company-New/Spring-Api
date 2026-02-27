package com.example.springapi.ServiceTests.Login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.helpers.EmailTemplates;
import com.example.springapi.helpers.PasswordHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for ResetPassword functionality in LoginService. Test Count: 16 tests */
@DisplayName("Reset Password Tests")
class ResetPasswordTest extends LoginServiceTestBase {

  // Total Tests: 15
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful password reset and email trigger. Expected Result: Password is reset
   * and email is sent. Assertions: UserRepository and ClientRepository are queried, and returns
   * true.
   */
  @Test
  @DisplayName("Reset Password - Success - Should reset password and send email")
  void resetPassword_Success_Success() {
    // Arrange
    testUser.setPassword("oldHashedPassword");
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentSendGridProperties("test@example.com", "Test Sender", "test-api-key");

    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
            stubPasswordHelperRandomPassword(
                "newPassword123", new String[] {"newSalt", "newHashedPassword"});
        MockedConstruction<EmailTemplates> mockedEmailTemplates =
            stubEmailTemplatesSendResetPasswordEmail(true)) {

      // Act
      Boolean result = loginService.resetPassword(testLoginRequest);

      // Assert
      assertTrue(result);
      verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Handle failure when reset password email cannot be sent via the helper. Expected
   * Result: RuntimeException is thrown. Assertions: Error message matches "Failed to send reset
   * password email".
   */
  @Test
  @DisplayName("Reset Password - Email send failure - Throws RuntimeException")
  void resetPassword_EmailSendFailure_ThrowsRuntimeException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentSendGridProperties("test@example.com", "Sender", "key");

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
            stubPasswordHelperRandomPassword("newpass", new String[] {"salt", "hash"});
        MockedConstruction<EmailTemplates> emailTemplatesMock =
            stubEmailTemplatesSendResetPasswordEmail(false)) {

      RuntimeException exception =
          assertThrows(RuntimeException.class, () -> loginService.resetPassword(testLoginRequest));

      assertEquals(
          ErrorMessages.LoginErrorMessages.RESET_PASSWORD_EMAIL_FAILED, exception.getMessage());
    }
  }

  /**
   * Purpose: Reject reset when user password in database is empty. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER003.
   */
  @Test
  @DisplayName("Reset Password - Failure - User has empty password")
  void resetPassword_EmptyPasswordSet_ThrowsBadRequestException() {
    // Arrange
    testUser.setPassword("");
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
  }

  /**
   * Purpose: Reject missing Brevo API key when email service is configured to brevo. Expected
   * Result: BadRequestException is thrown. Assertions: Error message matches
   * BrevoApiKeyNotConfigured.
   */
  @Test
  @DisplayName("Reset Password - Missing Brevo API key - Throws BadRequestException")
  void resetPassword_MissingBrevoApiKey_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentBrevoProperties("test@example.com", "Sender", "");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.BREVO_API_KEY_NOT_CONFIGURED,
        exception.getMessage());
  }

  /**
   * Purpose: Reject reset when login name is an empty string. Expected Result: BadRequestException
   * is thrown. Assertions: Exception message matches ER014.
   */
  @Test
  @DisplayName("Reset Password - Failure - Missing login name")
  void resetPassword_MissingLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
  }

  /**
   * Purpose: Reject missing sender email configuration in environment. Expected Result:
   * BadRequestException is thrown. Assertions: Error message matches SendGridEmailNotConfigured.
   */
  @Test
  @DisplayName("Reset Password - Missing sender email - Throws BadRequestException")
  void resetPassword_MissingSenderEmail_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentSendGridProperties(null, "Sender", "key");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.SEND_GRID_EMAIL_NOT_CONFIGURED,
        exception.getMessage());
  }

  /**
   * Purpose: Reject missing sender name configuration in environment. Expected Result:
   * BadRequestException is thrown. Assertions: Error message matches SendGridNameNotConfigured.
   */
  @Test
  @DisplayName("Reset Password - Missing sender name - Throws BadRequestException")
  void resetPassword_MissingSenderName_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentSendGridProperties("test@example.com", " ", "key");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.SEND_GRID_NAME_NOT_CONFIGURED,
        exception.getMessage());
  }

  /**
   * Purpose: Reject missing SendGrid API key configuration. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches SendGridApiKeyNotConfigured.
   */
  @Test
  @DisplayName("Reset Password - Missing SendGrid API key - Throws BadRequestException")
  void resetPassword_MissingSendGridApiKey_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(testClient);
    stubEnvironmentSendGridProperties("test@example.com", "Sender", "");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.SEND_GRID_API_KEY_NOT_CONFIGURED,
        exception.getMessage());
  }

  /**
   * Purpose: Reject reset attempts when no client configuration is found. Expected Result:
   * RuntimeException is thrown. Assertions: Error message matches NoClientConfigurationFound.
   */
  @Test
  @DisplayName("Reset Password - Missing client configuration - Throws RuntimeException")
  void resetPassword_NoClientConfiguration_ThrowsRuntimeException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubClientRepositoryFindFirstByOrderByClientIdAsc(null);

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.NO_CLIENT_CONFIGURATION_FOUND,
        exception.getMessage());
  }

  /**
   * Purpose: Reject reset when user has no password set (null). Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER003.
   */
  @Test
  @DisplayName("Reset Password - Failure - User has no password set")
  void resetPassword_NoPasswordSet_ThrowsBadRequestException() {
    // Arrange
    testUser.setPassword(null);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
  }

  /**
   * Purpose: Reject reset when the login name in request is null. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER014.
   */
  @Test
  @DisplayName("Reset Password - Failure - Null login name")
  void resetPassword_NullLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
  }

  /**
   * Purpose: Validate null request handling for reset. Expected Result: NullPointerException is
   * thrown. Assertions: Exception type is NullPointerException.
   */
  @Test
  @DisplayName("Reset Password - Failure - Null request")
  void resetPassword_NullRequest_ThrowsNullPointerException() {
    // Arrange

    // Act & Assert
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> loginService.resetPassword(null));
    assertEquals(ErrorMessages.LoginErrorMessages.NULL_REQUEST, exception.getMessage());
  }

  /**
   * Purpose: Validate reset fails for non-existent users. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidEmail.
   */
  @Test
  @DisplayName("Reset Password - Failure - User not found")
  void resetPassword_UserNotFound_ThrowsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.INVALID_EMAIL,
        exception.getMessage());
  }

  /**
   * Purpose: Reject reset when login name consists only of whitespace. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER014.
   */
  @Test
  @DisplayName("Reset Password - Failure - Whitespace only login name")
  void resetPassword_WhitespaceLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.resetPassword(testLoginRequest));

    assertEquals(
        com.example.springapi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("Reset Password - Controller permission unauthorized - Success")
  void resetPassword_controller_permission_unauthorized() {
    // Arrange
    stubLoginServiceThrowsUnauthorizedOnResetPassword();

    // Act
    ResponseEntity<?> response = loginController.resetPassword(testLoginRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
