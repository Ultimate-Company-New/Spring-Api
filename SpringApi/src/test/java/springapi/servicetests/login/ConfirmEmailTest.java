package springapi.ServiceTests.Login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.models.databasemodels.User;

/** Unit tests for ConfirmEmail functionality in LoginService. */
@DisplayName("Confirm Email Tests")
class ConfirmEmailTest extends LoginServiceTestBase {

  // Total Tests: 8
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify confirmation clears token on success. Expected Result: Token is set to null and
   * saved. Assertions: Saved user has null token.
   */
  @Test
  @DisplayName("Confirm Email - Success - Clears token")
  void confirmEmail_Success_ClearsToken() {
    // Arrange
    testUser.setToken(TEST_TOKEN);
    stubUserRepositoryFindById(TEST_USER_ID, Optional.of(testUser));
    stubUserRepositorySave(testUser);

    // Act
    assertDoesNotThrow(() -> loginService.confirmEmail(testLoginRequest));

    // Assert
    assertNull(testUser.getToken());
    verify(userRepository, times(1)).save(any(User.class));
  }

  /**
   * Purpose: Validate successful email confirmation with a valid token. Expected Result: User email
   * is confirmed and persisted. Assertions: Repository save is invoked and no exception is thrown.
   */
  @Test
  @DisplayName("Confirm Email - Success - Should confirm user email")
  void confirmEmail_Success_Success() {
    // Arrange
    testUser.setEmailConfirmed(false);
    stubUserRepositoryFindById(TEST_USER_ID, Optional.of(testUser));
    stubUserRepositorySave(testUser);

    // Act
    assertDoesNotThrow(() -> loginService.confirmEmail(testLoginRequest));

    // Assert
    verify(userRepository, times(1)).findById(TEST_USER_ID);
    verify(userRepository, times(1)).save(any(User.class));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject confirmation when stored token is blank. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidToken.
   */
  @Test
  @DisplayName("Confirm Email - Failure - Blank stored token")
  void confirmEmail_BlankStoredToken_ThrowsNotFoundException() {
    // Arrange
    testUser.setToken("  ");
    stubUserRepositoryFindById(TEST_USER_ID, Optional.of(testUser));

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.confirmEmail(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.INVALID_TOKEN, exception.getMessage());
  }

  /**
   * Purpose: Reject email confirmation when token does not match. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches InvalidToken and save is
   * not called.
   */
  @Test
  @DisplayName("Confirm Email - Failure - Invalid token")
  void confirmEmail_InvalidToken_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setToken("different-token");
    stubUserRepositoryFindById(TEST_USER_ID, Optional.of(testUser));

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(
            UnauthorizedException.class, () -> loginService.confirmEmail(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.INVALID_TOKEN, exception.getMessage());
    verify(userRepository, times(1)).findById(TEST_USER_ID);
    verify(userRepository, never()).save(any(User.class));
  }

  /**
   * Purpose: Reject confirmation when stored token is null. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidToken.
   */
  @Test
  @DisplayName("Confirm Email - Failure - Null stored token")
  void confirmEmail_NullStoredToken_ThrowsNotFoundException() {
    // Arrange
    testUser.setToken(null);
    stubUserRepositoryFindById(TEST_USER_ID, Optional.of(testUser));

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.confirmEmail(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.INVALID_TOKEN, exception.getMessage());
  }

  /**
   * Purpose: Reject confirmation when userId is null. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches InvalidId.
   */
  @Test
  @DisplayName("Confirm Email - Failure - Null userId")
  void confirmEmail_NullUserId_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setUserId(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.confirmEmail(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.INVALID_ID, exception.getMessage());
  }

  /**
   * Purpose: Handle confirmation for a non-existent user. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidId and save is not called.
   */
  @Test
  @DisplayName("Confirm Email - Failure - User not found")
  void confirmEmail_UserNotFound_ThrowsNotFoundException() {
    // Arrange
    stubUserRepositoryFindById(TEST_USER_ID, Optional.empty());

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.confirmEmail(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.INVALID_ID, exception.getMessage());
    verify(userRepository, times(1)).findById(TEST_USER_ID);
    verify(userRepository, never()).save(any(User.class));
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
  @DisplayName("Confirm Email - Controller permission unauthorized - Success")
  void confirmEmail_controller_permission_unauthorized() {
    // Arrange
    stubLoginServiceThrowsUnauthorizedOnConfirmEmail();

    // Act
    ResponseEntity<?> response = loginController.confirmEmail(testLoginRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
