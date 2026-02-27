package com.example.SpringApi.ServiceTests.Login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for SignIn functionality in LoginService. Test Count: 21 tests */
@DisplayName("SignIn Tests")
class SignInTest extends LoginServiceTestBase {

  // Total Tests: 22
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify that a client mapping is skipped if the client entity is not found. Expected
   * Result: Sign-in succeeds but returns an empty list. Assertions: Result list is empty.
   */
  @Test
  @DisplayName("Sign In - Success - Client not found in mapping is skipped")
  void signIn_ClientNotFoundInMapping_SkipsClient() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of(testUserClientMapping));
    stubClientRepositoryFindById(TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      List<ClientResponseModel> result = loginService.signIn(testLoginRequest);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  /**
   * Purpose: Verify accessible clients are returned sorted alphabetically by name. Expected Result:
   * Client list is ordered A-Z. Assertions: First item is "Alpha", second is "Beta".
   */
  @Test
  @DisplayName("Sign In - Success - Clients sorted by name")
  void signIn_ClientsSortedByName_Success() {
    // Arrange
    Client clientB = new Client();
    clientB.setClientId(2L);
    clientB.setName("Beta");

    Client clientA = new Client();
    clientA.setClientId(1L);
    clientA.setName("Alpha");

    UserClientMapping mappingA = new UserClientMapping();
    mappingA.setUserId(TEST_USER_ID);
    mappingA.setClientId(1L);
    mappingA.setApiKey("api-key-a");

    UserClientMapping mappingB = new UserClientMapping();
    mappingB.setUserId(TEST_USER_ID);
    mappingB.setClientId(2L);
    mappingB.setApiKey("api-key-b");

    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of(mappingB, mappingA));
    stubClientRepositoryFindById(1L, Optional.of(clientA));
    stubClientRepositoryFindById(2L, Optional.of(clientB));

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      List<ClientResponseModel> result = loginService.signIn(testLoginRequest);

      assertEquals("Alpha", result.get(0).getName());
      assertEquals("Beta", result.get(1).getName());
    }
  }

  /**
   * Purpose: Verify sign-in returns multiple mapped clients for the user. Expected Result: List
   * contains both mock clients. Assertions: Result size is 2.
   */
  @Test
  @DisplayName("Sign In - Success - Multiple accessible clients")
  void signIn_MultipleAccessibleClients_Success() {
    // Arrange
    Client client2 = new Client();
    client2.setClientId(2L);
    client2.setName("Client 2");

    UserClientMapping mapping2 = new UserClientMapping();
    mapping2.setUserId(TEST_USER_ID);
    mapping2.setClientId(2L);
    mapping2.setApiKey("api-key-2");

    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(
        TEST_USER_ID, List.of(testUserClientMapping, mapping2));
    stubClientRepositoryFindById(TEST_CLIENT_ID, Optional.of(testClient));
    stubClientRepositoryFindById(2L, Optional.of(client2));

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      List<ClientResponseModel> result = loginService.signIn(testLoginRequest);

      assertNotNull(result);
      assertEquals(2, result.size());
    }
  }

  /**
   * Purpose: Verify sign-in returns empty list when no client mappings exist. Expected Result:
   * Authentication succeeds but returns no clients. Assertions: List is empty.
   */
  @Test
  @DisplayName("Sign In - Success - No client mappings returns empty list")
  void signIn_NoClientMappings_ReturnsEmptyList() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of());

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      List<ClientResponseModel> result = loginService.signIn(testLoginRequest);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  /**
   * Purpose: Verify successful sign-in resets login attempts and saves user. Expected Result: User
   * attempts are reset to default and saved. Assertions: userRepository.save is called once.
   */
  @Test
  @DisplayName("Sign In - Success - Resets login attempts and saves user")
  void signIn_Success_ResetsLoginAttemptsAndSavesUser() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of(testUserClientMapping));
    stubClientRepositoryFindById(TEST_CLIENT_ID, Optional.of(testClient));

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      loginService.signIn(testLoginRequest);

      verify(userRepository, times(1)).save(any(User.class));
    }
  }

  /**
   * Purpose: Verify lastLoginAt timestamp is set on successful sign-in. Expected Result:
   * lastLoginAt is populated. Assertions: testUser.getLastLoginAt() is not null.
   */
  @Test
  @DisplayName("Sign In - Success - Sets lastLoginAt")
  void signIn_Success_SetsLastLoginAt() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of(testUserClientMapping));
    stubClientRepositoryFindById(TEST_CLIENT_ID, Optional.of(testClient));

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      loginService.signIn(testLoginRequest);

      assertNotNull(testUser.getLastLoginAt());
    }
  }

  /**
   * Purpose: Verify successful sign-in returns accessible clients. Expected Result: Sign-in
   * succeeds and clients are returned. Assertions: Response list size is 1 and fields match.
   */
  @Test
  @DisplayName("Sign In - Success - Should return list of clients")
  void signIn_Success_Success() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByUserId(TEST_USER_ID, List.of(testUserClientMapping));
    stubClientRepositoryFindById(TEST_CLIENT_ID, Optional.of(testClient));

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(true)) {
      List<ClientResponseModel> result = loginService.signIn(testLoginRequest);

      assertNotNull(result);
      assertEquals(1, result.size());
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject sign-in for accounts already marked as locked. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches ER006.
   */
  @Test
  @DisplayName("Sign In - Failure - Account locked")
  void signIn_AccountLocked_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setLocked(true);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when account is locked even if attempts are remaining. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches ER006 and save is not
   * called.
   */
  @Test
  @DisplayName("Sign In - Failure - Account locked from previous attempts")
  void signIn_AccountLockedFromPreviousAttempts_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setLocked(true);
    testUser.setLoginAttempts(0);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER006, exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  /**
   * Purpose: Validate sign-in fails for non-existent users. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidEmail.
   */
  @Test
  @DisplayName("Sign In - Failure - Different user not found")
  void signIn_DifferentUserNotFound_ThrowsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByLoginName("unknownuser", null);
    testLoginRequest.setLoginName("unknownuser");

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.INVALID_EMAIL,
        exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when the user's email has not been confirmed. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches ER005.
   */
  @Test
  @DisplayName("Sign In - Failure - Email not confirmed")
  void signIn_EmailNotConfirmed_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setEmailConfirmed(false);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER005, exception.getMessage());
  }

  /**
   * Purpose: Validate invalid password decrements remaining attempts and denies access. Expected
   * Result: UnauthorizedException is thrown. Assertions: Exception message matches ER007 and save
   * is called.
   */
  @Test
  @DisplayName("Sign In - Failure - Invalid password")
  void signIn_InvalidPassword_DecrementsAttemptsAndThrowsUnauthorized() {
    // Arrange
    testUser.setLoginAttempts(1);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserRepositorySave(testUser);

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(false)) {
      UnauthorizedException exception =
          assertThrows(UnauthorizedException.class, () -> loginService.signIn(testLoginRequest));

      assertEquals(
          com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER007, exception.getMessage());
      verify(userRepository, times(1)).save(any(User.class));
    }
  }

  /**
   * Purpose: Validate invalid password with multiple attempts still available. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches InvalidCredentials.
   */
  @Test
  @DisplayName("Sign In - Failure - Invalid password with multiple attempts remaining")
  void signIn_InvalidPasswordMultipleAttemptsRemaining_ThrowsUnauthorized() {
    // Arrange
    testUser.setLoginAttempts(3);
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserRepositorySave(testUser);

    // Act & Assert
    try (MockedStatic<PasswordHelper> mockedPasswordHelper =
        stubPasswordHelperCheckPassword(false)) {
      UnauthorizedException exception =
          assertThrows(UnauthorizedException.class, () -> loginService.signIn(testLoginRequest));

      assertEquals(
          com.example.SpringApi.ErrorMessages.LoginErrorMessages.INVALID_CREDENTIALS,
          exception.getMessage());
    }
  }

  /**
   * Purpose: Reject sign-in when login name is empty. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ER012.
   */
  @Test
  @DisplayName("Sign In - Failure - Missing login name")
  void signIn_MissingLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when password is empty. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ER012.
   */
  @Test
  @DisplayName("Sign In - Failure - Missing password")
  void signIn_MissingPassword_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setPassword("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when the user has no password set in database. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER016.
   */
  @Test
  @DisplayName("Sign In - Failure - No password set")
  void signIn_NoPasswordSet_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setPassword("");
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER016, exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when login name is null. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ER012.
   */
  @Test
  @DisplayName("Sign In - Failure - Null login name")
  void signIn_NullLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when password is null. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ER012.
   */
  @Test
  @DisplayName("Sign In - Failure - Null password")
  void signIn_NullPassword_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setPassword(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
  }

  /**
   * Purpose: Validate null request handling. Expected Result: NullPointerException is thrown.
   * Assertions: Exception type is NullPointerException.
   */
  @Test
  @DisplayName("Sign In - Failure - Null request")
  void signIn_NullRequest_ThrowsNullPointerException() {
    // Arrange

    // Act & Assert
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> loginService.signIn(null));
    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.NULL_REQUEST,
        exception.getMessage());
  }

  /**
   * Purpose: Validate sign-in fails when user login name is not found. Expected Result:
   * NotFoundException is thrown. Assertions: Exception message matches InvalidEmail.
   */
  @Test
  @DisplayName("Sign In - Failure - User not found")
  void signIn_UserNotFound_ThrowsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.INVALID_EMAIL,
        exception.getMessage());
  }

  /**
   * Purpose: Reject sign-in when login name consists only of whitespace. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER012.
   */
  @Test
  @DisplayName("Sign In - Failure - Whitespace only login name")
  void signIn_WhitespaceLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.signIn(testLoginRequest));

    assertEquals(
        com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
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
  @DisplayName("Sign In - Controller permission unauthorized - Success")
  void signIn_controller_permission_unauthorized() {
    // Arrange
    stubLoginServiceThrowsUnauthorizedOnSignIn();

    // Act
    ResponseEntity<?> response = loginController.signIn(testLoginRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}

