package springapi.ServiceTests.Login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.UnauthorizedException;
import springapi.models.databasemodels.User;
import springapi.models.databasemodels.UserClientMapping;
import springapi.models.databasemodels.UserClientPermissionMapping;

/** Unit tests for GetToken functionality in LoginService. Test Count: 12 tests */
@DisplayName("Get Token Tests")
class GetTokenTest extends LoginServiceTestBase {

  // Total Tests: 13
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify token generation includes multiple permissions correctly. Expected Result:
   * Token contains all three mock permissions. Assertions: Permission list size in the generator
   * matches the expected count.
   */
  @Test
  @DisplayName("Get Token - Success - Multiple permissions")
  void getToken_MultiplePermissions_Success() {
    // Arrange
    List<UserClientPermissionMapping> permissions = new ArrayList<>();
    Long mappingClientId = testUserClientMapping.getClientId();
    permissions.add(new UserClientPermissionMapping(TEST_USER_ID, mappingClientId, 1L, "admin"));
    permissions.add(new UserClientPermissionMapping(TEST_USER_ID, mappingClientId, 2L, "editor"));
    permissions.add(new UserClientPermissionMapping(TEST_USER_ID, mappingClientId, 3L, "viewer"));

    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.of(testUserClientMapping));
    stubUserClientPermissionMappingByUserId(TEST_USER_ID, permissions);
    stubJwtTokenProviderGenerateToken("jwt-token-123");

    // Act
    String result = loginService.getToken(testLoginRequest);

    // Assert
    assertEquals("jwt-token-123", result);
    verify(jwtTokenProvider, times(1))
        .generateToken(any(User.class), argThat(list -> list.size() == 3), anyLong());
  }

  /**
   * Purpose: Verify token generation when no permissions exist for the client. Expected Result:
   * Token is generated with empty permissions. Assertions: Returned token matches expectations and
   * provider is called with empty list.
   */
  @Test
  @DisplayName("Get Token - Success - No permissions")
  void getToken_NoPermissions_Success() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.of(testUserClientMapping));
    stubUserClientPermissionMappingByUserId(TEST_USER_ID, new ArrayList<>());
    stubJwtTokenProviderGenerateToken("jwt-token-123");

    // Act
    String result = loginService.getToken(testLoginRequest);

    // Assert
    assertEquals("jwt-token-123", result);
    verify(jwtTokenProvider).generateToken(any(User.class), argThat(List::isEmpty), anyLong());
  }

  /**
   * Purpose: Filter out permissions that belong to other clients during token generation. Expected
   * Result: Token generated with only matching client permissions. Assertions: Permission list
   * passed to generator is empty when mappings don't match.
   */
  @Test
  @DisplayName("Get Token - Success - Permissions filtered by client")
  void getToken_PermissionsFilteredByClient_Success() {
    // Arrange
    List<UserClientPermissionMapping> permissions = new ArrayList<>();
    permissions.add(new UserClientPermissionMapping(TEST_USER_ID, 999L, 1L, "admin"));

    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.of(testUserClientMapping));
    stubUserClientPermissionMappingByUserId(TEST_USER_ID, permissions);
    stubJwtTokenProviderGenerateToken("jwt-token-123");

    // Act
    String result = loginService.getToken(testLoginRequest);

    // Assert
    assertEquals("jwt-token-123", result);
    verify(jwtTokenProvider).generateToken(any(User.class), argThat(List::isEmpty), anyLong());
  }

  /**
   * Purpose: Verify token generation succeeds with standard valid inputs. Expected Result: JWT
   * token is generated. Assertions: Returned token matches the provider output.
   */
  @Test
  @DisplayName("Get Token - Success - Should generate JWT token")
  void getToken_Success_Success() {
    // Arrange
    List<UserClientPermissionMapping> permissions = new ArrayList<>();
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.of(testUserClientMapping));
    stubUserClientPermissionMappingByUserId(TEST_USER_ID, permissions);
    stubJwtTokenProviderGenerateToken("jwt-token-123");

    // Act
    String result = loginService.getToken(testLoginRequest);

    // Assert
    assertEquals("jwt-token-123", result);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject token request when API key belongs to another user. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches InvalidCredentials.
   */
  @Test
  @DisplayName("Get Token - Failure - API key belongs to different user")
  void getToken_ApiKeyBelongsToDifferentUser_ThrowsUnauthorizedException() {
    // Arrange
    UserClientMapping differentUserMapping = new UserClientMapping();
    differentUserMapping.setUserId(999L);
    differentUserMapping.setClientId(TEST_CLIENT_ID);
    differentUserMapping.setApiKey(TEST_API_KEY);

    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.of(differentUserMapping));

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(
        springapi.ErrorMessages.LoginErrorMessages.INVALID_CREDENTIALS, exception.getMessage());
  }

  /**
   * Purpose: Reject token request for different unknown user names. Expected Result:
   * UnauthorizedException is thrown. Assertions: Exception message matches InvalidCredentials.
   */
  @Test
  @DisplayName("Get Token - Failure - Different user not found")
  void getToken_DifferentUserNotFound_ThrowsUnauthorizedException() {
    // Arrange
    stubUserRepositoryFindByLoginName("unknownuser", null);
    testLoginRequest.setLoginName("unknownuser");

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(
        springapi.ErrorMessages.LoginErrorMessages.INVALID_CREDENTIALS, exception.getMessage());
  }

  /**
   * Purpose: Reject token request with an invalid API key. Expected Result: UnauthorizedException
   * is thrown. Assertions: Exception message matches InvalidCredentials.
   */
  @Test
  @DisplayName("Get Token - Failure - Invalid API key")
  void getToken_InvalidApiKey_ThrowsUnauthorizedException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, testUser);
    stubUserClientMappingRepositoryFindByApiKey(TEST_API_KEY, Optional.empty());

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(
        springapi.ErrorMessages.LoginErrorMessages.INVALID_CREDENTIALS, exception.getMessage());
  }

  /**
   * Purpose: Reject token request with a missing API key string. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER015.
   */
  @Test
  @DisplayName("Get Token - Failure - Missing API key")
  void getToken_MissingApiKey_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setApiKey("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
  }

  /**
   * Purpose: Reject token request with a missing login name string. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ER015.
   */
  @Test
  @DisplayName("Get Token - Failure - Missing login name")
  void getToken_MissingLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
  }

  /**
   * Purpose: Reject token request with a null API key. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ER015.
   */
  @Test
  @DisplayName("Get Token - Failure - Null API key")
  void getToken_NullApiKey_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setApiKey(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
  }

  /**
   * Purpose: Reject token request with a null login name. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ER015.
   */
  @Test
  @DisplayName("Get Token - Failure - Null login name")
  void getToken_NullLoginName_ThrowsBadRequestException() {
    // Arrange
    testLoginRequest.setLoginName(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(springapi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
  }

  /**
   * Purpose: Reject token request for a non-existent user. Expected Result: UnauthorizedException
   * is thrown. Assertions: Exception message matches InvalidCredentials.
   */
  @Test
  @DisplayName("Get Token - Failure - User not found")
  void getToken_UserNotFound_ThrowsUnauthorizedException() {
    // Arrange
    stubUserRepositoryFindByLoginName(TEST_LOGIN_NAME, null);

    // Act & Assert
    UnauthorizedException exception =
        assertThrows(UnauthorizedException.class, () -> loginService.getToken(testLoginRequest));

    assertEquals(
        springapi.ErrorMessages.LoginErrorMessages.INVALID_CREDENTIALS, exception.getMessage());
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
  @DisplayName("Get Token - Controller permission unauthorized - Success")
  void getToken_controller_permission_unauthorized() {
    // Arrange
    stubLoginServiceThrowsUnauthorizedOnGetToken();

    // Act
    ResponseEntity<?> response = loginController.getToken(testLoginRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
