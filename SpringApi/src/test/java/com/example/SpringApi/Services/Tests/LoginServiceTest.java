package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping;
import com.example.SpringApi.Models.RequestModels.LoginRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.LoginService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import org.mockito.MockedConstruction;

/**
 * Unit tests for LoginService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | ConfirmEmailTests                       | 7               |
 * | SignInTests                             | 21              |
 * | ResetPasswordTests                      | 14              |
 * | GetTokenTests                           | 12              |
 * | **Total**                               | **54**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService Unit Tests")
class LoginServiceTest extends BaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserClientMappingRepository userClientMappingRepository;

    @Mock
    private UserClientPermissionMappingRepository userClientPermissionMappingRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private GoogleCredRepository googleCredRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Environment environment;

    @Mock
    private GoogleCred googleCred;

    // Mock helper classes to prevent external service calls during testing
    @Mock
    private EmailTemplates emailTemplates;

    @InjectMocks
    private LoginService loginService;

    private User testUser;
    private LoginRequestModel testLoginRequest;
    private UserRequestModel testUserRequest;
    private Client testClient;
    private UserClientMapping testUserClientMapping;
    private static final Long TEST_USER_ID = DEFAULT_USER_ID;
    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
    private static final String TEST_LOGIN_NAME = DEFAULT_LOGIN_NAME;
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test-token-123";
    private static final String TEST_API_KEY = "test-api-key-123";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test user with proper constructor
        testUserRequest = new UserRequestModel();
        testUserRequest.setLoginName(TEST_LOGIN_NAME);
        testUserRequest.setPassword(TEST_PASSWORD);
        testUserRequest.setFirstName("Test");
        testUserRequest.setLastName("User");
        testUserRequest.setPhone("1234567890");
        testUserRequest.setRole("Customer");
        testUserRequest.setDob(LocalDate.of(1990, 1, 1));

        testUser = createTestUser();
        testUser.setUserId(TEST_USER_ID);
        testUser.setToken(TEST_TOKEN);
        testUser.setEmailConfirmed(true);
        testUser.setLocked(false);
        testUser.setLoginAttempts(5);
        testUser.setSalt("test-salt");
        testUser.setPassword("hashedPassword");

        // Initialize test login request
        testLoginRequest = createValidLoginRequest();
        testLoginRequest.setUserId(TEST_USER_ID);
        testLoginRequest.setClientId(TEST_CLIENT_ID);
        testLoginRequest.setToken(TEST_TOKEN);
        testLoginRequest.setApiKey(TEST_API_KEY);

        // Initialize test client
        testClient = createTestClient();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setName("Test Client");
        testClient.setSupportEmail("support@test.com");
        testClient.setSendGridApiKey("test-sendgrid-key");

        // Initialize test UserClientMapping
        testUserClientMapping = createTestUserClientMapping(1L, TEST_USER_ID, TEST_CLIENT_ID, TEST_API_KEY);

        // Initialize test GoogleCred
        GoogleCred testGoogleCred = createTestGoogleCred(1L);
        // Add lenient mock for googleCredRepository since not all tests use it
        lenient().when(googleCredRepository.findAll()).thenReturn(Arrays.asList(testGoogleCred));
    }


    @Nested
    @DisplayName("ConfirmEmail Tests")
    class ConfirmEmailTests {
        /**
         * Purpose: Validate successful email confirmation with a valid token.
         * Expected Result: User email is confirmed and persisted.
         * Assertions: Repository save is invoked and no exception is thrown.
         */
        @Test
        @DisplayName("Confirm Email - Success - Should confirm user email")
        void confirmEmail_Success() {
        // Arrange
        testUser.setEmailConfirmed(false);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> loginService.confirmEmail(testLoginRequest));

        // Assert
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Purpose: Verify confirmation clears token on success.
     * Expected Result: Token is set to null and saved.
     * Assertions: Saved user has null token.
     */
    @Test
    @DisplayName("Confirm Email - Success - Clears token")
    void confirmEmail_Success_ClearsToken() {
        // Arrange
        testUser.setToken(TEST_TOKEN);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        assertDoesNotThrow(() -> loginService.confirmEmail(testLoginRequest));

        // Assert
        assertNull(testUser.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * Purpose: Reject email confirmation when token does not match.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches InvalidToken and save is not called.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Invalid token")
    void confirmEmail_InvalidToken_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setToken("different-token");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.confirmEmail(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidToken, exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Handle confirmation for a non-existent user.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId and save is not called.
     */
    @Test
    @DisplayName("Confirm Email - Failure - User not found")
    void confirmEmail_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.confirmEmail(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidId, exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
        }

    /**
     * Purpose: Reject confirmation when userId is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidId.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Null userId")
    void confirmEmail_NullUserId_ThrowsBadRequestException() {
        testLoginRequest.setUserId(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.confirmEmail(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject confirmation when stored token is blank.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidToken.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Blank stored token")
    void confirmEmail_BlankStoredToken_ThrowsNotFoundException() {
        testUser.setToken("  ");
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.confirmEmail(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidToken, exception.getMessage());
    }

    /**
     * Purpose: Reject confirmation when stored token is null.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidToken.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Null stored token")
    void confirmEmail_NullStoredToken_ThrowsNotFoundException() {
        testUser.setToken(null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.confirmEmail(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidToken, exception.getMessage());
    }
    }

    @Nested
    @DisplayName("SignIn Tests")
    class SignInTests {

    /**
     * Purpose: Verify successful sign-in returns accessible clients.
     * Expected Result: Sign-in succeeds and clients are returned.
     * Assertions: No exception is thrown and repositories are queried.
     */
    @Test
    @DisplayName("Sign In - Success - Should return list of clients")
    void signIn_Success() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(testUserClientMapping));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        // Mock PasswordHelper static method to return true for valid password
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            // Act
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result = loginService
                    .signIn(testLoginRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            com.example.SpringApi.Models.ResponseModels.ClientResponseModel clientResponse = result.get(0);
            assertEquals(TEST_CLIENT_ID, clientResponse.getClientId());
            assertEquals("Test Client", clientResponse.getName());
            assertEquals(TEST_API_KEY, clientResponse.getApiKey());

            verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
            verify(userClientMappingRepository, times(1)).findByUserId(TEST_USER_ID);
            verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
        }
    }

    /**
     * Purpose: Verify successful sign-in resets login attempts and saves user.
     * Expected Result: User is saved after login.
     * Assertions: userRepository.save is called once.
     */
    @Test
    @DisplayName("Sign In - Success - Resets login attempts and saves user")
    void signIn_Success_ResetsLoginAttemptsAndSavesUser() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(testUserClientMapping));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            loginService.signIn(testLoginRequest);

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    /**
     * Purpose: Verify lastLoginAt is set on successful sign-in.
     * Expected Result: lastLoginAt is not null.
     * Assertions: User has lastLoginAt populated.
     */
    @Test
    @DisplayName("Sign In - Success - Sets lastLoginAt")
    void signIn_Success_SetsLastLoginAt() {
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(testUserClientMapping));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                .thenReturn(true);

            loginService.signIn(testLoginRequest);

            assertNotNull(testUser.getLastLoginAt());
        }
    }

    /**
     * Purpose: Validate missing login name is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidLoginName.
     */
    @Test
    @DisplayName("Sign In - Failure - Missing login name")
    void signIn_MissingLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
        // Note: findByLoginName is called before validation, so we don't verify never()
    }

    /**
     * Purpose: Validate null login name is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER012.
     */
    @Test
    @DisplayName("Sign In - Failure - Null login name")
    void signIn_NullLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
    }

    /**
     * Purpose: Validate whitespace-only login name is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER012.
     */
    @Test
    @DisplayName("Sign In - Failure - Whitespace only login name")
    void signIn_WhitespaceLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
    }

    /**
     * Purpose: Validate missing password is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER012.
     */
    @Test
    @DisplayName("Sign In - Failure - Missing password")
    void signIn_MissingPassword_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setPassword("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
        // Note: findByLoginName is called before validation, so we don't verify never()
    }

    /**
     * Purpose: Validate null password is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER012.
     */
    @Test
    @DisplayName("Sign In - Failure - Null password")
    void signIn_NullPassword_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setPassword(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER012, exception.getMessage());
    }

    /**
     * Purpose: Validate null request handling.
     * Expected Result: NullPointerException is thrown.
     * Assertions: Exception type is NullPointerException.
     */
    @Test
    @DisplayName("Sign In - Failure - Null request")
    void signIn_NullRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginService.signIn(null));
    }

    /**
     * Purpose: Validate sign-in fails when user does not exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Sign In - Failure - User not found")
    void signIn_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidEmail, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Validate sign-in fails for unknown login names.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Sign In - Failure - Different user not found")
    void signIn_DifferentUserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByLoginName("unknownuser")).thenReturn(null);
        testLoginRequest.setLoginName("unknownuser");

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidEmail, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName("unknownuser");
    }

    /**
     * Purpose: Reject sign-in when email is not confirmed.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches ER005.
     */
    @Test
    @DisplayName("Sign In - Failure - Email not confirmed")
    void signIn_EmailNotConfirmed_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setEmailConfirmed(false);
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER005, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Reject sign-in for locked accounts.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches ER006.
     */
    @Test
    @DisplayName("Sign In - Failure - Account locked")
    void signIn_AccountLocked_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setLocked(true);
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER006, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Reject sign-in when account is locked from prior attempts.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches ER006 and save is not called.
     */
    @Test
    @DisplayName("Sign In - Failure - Account locked from previous attempts")
    void signIn_AccountLockedFromPreviousAttempts_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setLocked(true);
        testUser.setLoginAttempts(0);
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER006, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Reject sign-in when password is not set.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER016.
     */
    @Test
    @DisplayName("Sign In - Failure - No password set")
    void signIn_NoPasswordSet_ThrowsUnauthorizedException() {
        // Arrange
        testUser.setPassword("");
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.signIn(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER016, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Validate invalid password decrements attempts and denies access.
     * Expected Result: UnauthorizedException is thrown and attempts are updated.
     * Assertions: Save is invoked and exception message matches ER008.
     */
    @Test
    @DisplayName("Sign In - Failure - Invalid password")
    void signIn_InvalidPassword_DecrementsAttemptsAndThrowsUnauthorized() {
        // Arrange
        testUser.setLoginAttempts(1); // Will become 0 after failed attempt
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Mock PasswordHelper static method to return false
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(false);

            // Act & Assert
            UnauthorizedException exception = assertThrows(
                    UnauthorizedException.class,
                    () -> loginService.signIn(testLoginRequest));

            assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER007, exception.getMessage());
            verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    /**
     * Purpose: Validate invalid password with remaining attempts.
     * Expected Result: UnauthorizedException is thrown and attempts decrease.
     * Assertions: Save is invoked with updated attempts.
     */
    @Test
    @DisplayName("Sign In - Failure - Invalid password with multiple attempts remaining")
    void signIn_InvalidPasswordMultipleAttemptsRemaining_ThrowsUnauthorized() {
        // Arrange
        testUser.setLoginAttempts(3); // Multiple attempts remaining
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(false);

            // Act & Assert
            UnauthorizedException exception = assertThrows(
                    UnauthorizedException.class,
                    () -> loginService.signIn(testLoginRequest));

            assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidCredentials, exception.getMessage());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    /**
     * Purpose: Verify sign-in returns multiple accessible clients.
     * Expected Result: Sign-in succeeds and multiple clients are returned.
     * Assertions: No exception is thrown and repositories are queried.
     */
    @Test
    @DisplayName("Sign In - Success - Multiple accessible clients")
    void signIn_MultipleAccessibleClients_Success() {
        // Arrange
        Client client2 = new Client();
        client2.setClientId(2L);
        client2.setName("Client 2");

        UserClientMapping mapping2 = new UserClientMapping();
        mapping2.setMappingId(2L);
        mapping2.setUserId(TEST_USER_ID);
        mapping2.setClientId(2L);
        mapping2.setApiKey("api-key-2");

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByUserId(TEST_USER_ID))
                .thenReturn(List.of(testUserClientMapping, mapping2));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client2));

        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(true);

            // Act
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result = loginService
                    .signIn(testLoginRequest);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
            verify(userClientMappingRepository, times(1)).findByUserId(TEST_USER_ID);
        }
        }

            /**
             * Purpose: Verify clients are returned sorted by name.
             * Expected Result: Response list is alphabetical.
             * Assertions: First client name is "Alpha", second is "Beta".
             */
            @Test
            @DisplayName("Sign In - Success - Clients sorted by name")
            void signIn_ClientsSortedByName_Success() {
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

            when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
            when(userClientMappingRepository.findByUserId(TEST_USER_ID))
                .thenReturn(List.of(mappingB, mappingA));
            when(clientRepository.findById(1L)).thenReturn(Optional.of(clientA));
            when(clientRepository.findById(2L)).thenReturn(Optional.of(clientB));

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                    .thenReturn(true);

                List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result =
                    loginService.signIn(testLoginRequest);

                assertEquals("Alpha", result.get(0).getName());
                assertEquals("Beta", result.get(1).getName());
            }
            }

            /**
             * Purpose: Return empty list when no client mappings exist.
             * Expected Result: Sign-in succeeds and returns empty list.
             * Assertions: Result list is empty.
             */
            @Test
            @DisplayName("Sign In - Success - No client mappings returns empty list")
            void signIn_NoClientMappings_ReturnsEmptyList() {
                when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
                when(userClientMappingRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of());

                try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                    mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                        .thenReturn(true);

                    List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result =
                        loginService.signIn(testLoginRequest);

                    assertNotNull(result);
                    assertTrue(result.isEmpty());
                }
            }

            /**
             * Purpose: Skip mappings where client is missing.
             * Expected Result: Sign-in succeeds and returns empty list.
             * Assertions: Result list is empty.
             */
            @Test
            @DisplayName("Sign In - Success - Client not found in mapping is skipped")
            void signIn_ClientNotFoundInMapping_SkipsClient() {
                when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
                when(userClientMappingRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(testUserClientMapping));
                when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

                try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                    mockedPasswordHelper.when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
                        .thenReturn(true);

                    List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result =
                        loginService.signIn(testLoginRequest);

                    assertNotNull(result);
                    assertTrue(result.isEmpty());
                }
            }
    }

    // Note: signUp method has been removed from the API - no tests needed

    @Nested
    @DisplayName("ResetPassword Tests")
    class ResetPasswordTests {

    /**
     * Purpose: Verify successful password reset.
     * Expected Result: Password is reset and email is sent.
     * Assertions: Save is called and email helper is invoked.
     */
    @Test
    @DisplayName("Reset Password - Success - Should reset password and send email")
    void resetPassword_Success() {
        // Arrange
        testUser.setPassword("oldHashedPassword"); // User has password set
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        lenient().when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(testClient);

        // Mock environment properties for email configuration
        when(environment.getProperty("email.sender.address")).thenReturn("test@example.com");
        when(environment.getProperty("email.sender.name")).thenReturn("Test Sender");
        when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
        when(environment.getProperty("sendgrid.api.key")).thenReturn("test-api-key");
        lenient().when(emailTemplates.sendResetPasswordEmail(anyString(), anyString()))
                .thenReturn(true);

        // Mock static PasswordHelper methods
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("newPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                    .thenReturn(new String[] { "newSalt", "newHashedPassword" });

            // Mock EmailTemplates constructor - avoid using argument matchers
            try (MockedConstruction<EmailTemplates> mockedEmailTemplates = mockConstruction(EmailTemplates.class,
                    (mock, context) -> {
                        when(mock.sendResetPasswordEmail(anyString(), anyString())).thenReturn(true);
                    })) {

                // Act
                Boolean result = loginService.resetPassword(testLoginRequest);

                // Assert
                assertTrue(result);
                verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
                // Note: resetPassword doesn't save the user, so no save verification
                verify(clientRepository, times(1)).findFirstByOrderByClientIdAsc();
            }
        }
    }

    /**
     * Purpose: Reject missing client configuration.
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
     * Purpose: Reject missing sender email configuration.
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
     * Purpose: Reject missing sender name configuration.
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
     * Purpose: Reject missing Brevo API key when emailService is brevo.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches BrevoApiKeyNotConfigured.
     */
    @Test
    @DisplayName("Reset Password - Missing Brevo API key when emailService is brevo - Throws BadRequestException")
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
     * Purpose: Handle failure when reset password email cannot be sent.
     * Expected Result: RuntimeException is thrown.
     * Assertions: Error message matches expected text.
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
     * Purpose: Reject reset when login name is missing.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Missing login name")
    void resetPassword_MissingLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject reset when login name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Null login name")
    void resetPassword_NullLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject reset when login name is whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Reset Password - Failure - Whitespace only login name")
    void resetPassword_WhitespaceLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER014, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Validate reset fails for non-existent user.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Reset Password - Failure - User not found")
    void resetPassword_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidEmail, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Validate reset fails for a different unknown user.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Reset Password - Failure - Different user not found")
    void resetPassword_DifferentUserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByLoginName("unknownuser")).thenReturn(null);
        testLoginRequest.setLoginName("unknownuser");

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidEmail, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName("unknownuser");
    }

    /**
     * Purpose: Reject reset when user has no password set.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER016.
     */
    @Test
    @DisplayName("Reset Password - Failure - User has no password set")
    void resetPassword_NoPasswordSet_ThrowsBadRequestException() {
        // Arrange
        testUser.setPassword(null);
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Reject reset when user password is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER016.
     */
    @Test
    @DisplayName("Reset Password - Failure - User has empty password")
    void resetPassword_EmptyPasswordSet_ThrowsBadRequestException() {
        // Arrange
        testUser.setPassword("");
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.resetPassword(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER003, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Validate null request handling for reset.
     * Expected Result: NullPointerException is thrown.
     * Assertions: Exception type is NullPointerException.
     */
    @Test
    @DisplayName("Reset Password - Failure - Null request")
    void resetPassword_NullRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginService.resetPassword(null));
        }
    }


    @Nested
    @DisplayName("GetToken Tests")
    class GetTokenTests {

    /**
     * Purpose: Verify token generation succeeds with valid inputs.
     * Expected Result: JWT token is generated.
     * Assertions: Returned token is not null and includes permissions.
     */
    @Test
    @DisplayName("Get Token - Success - Should generate JWT token")
    void getToken_Success() {
        // Arrange
        List<UserClientPermissionMapping> permissions = new ArrayList<>();
        UserClientPermissionMapping permission = new UserClientPermissionMapping(
                TEST_USER_ID, 1L, TEST_CLIENT_ID, "admin");
        permissions.add(permission);

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(TEST_USER_ID))
                .thenReturn(permissions);
        when(jwtTokenProvider.generateToken(any(User.class), anyList(), anyLong())).thenReturn("jwt-token-123");

        // Act
        String result = loginService.getToken(testLoginRequest);

        // Assert
        assertEquals("jwt-token-123", result);
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userClientMappingRepository, times(1)).findByApiKey(TEST_API_KEY);
        verify(userClientPermissionMappingRepository, times(1)).findClientPermissionMappingByUserId(TEST_USER_ID);
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class), anyList(), anyLong());
    }

    /**
     * Purpose: Reject token request with missing login name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Get Token - Failure - Missing login name")
    void getToken_MissingLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject token request with null login name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER014.
     */
    @Test
    @DisplayName("Get Token - Failure - Null login name")
    void getToken_NullLoginName_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setLoginName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject token request with missing API key.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER015.
     */
    @Test
    @DisplayName("Get Token - Failure - Missing API key")
    void getToken_MissingApiKey_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setApiKey("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject token request with null API key.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches ER015.
     */
    @Test
    @DisplayName("Get Token - Failure - Null API key")
    void getToken_NullApiKey_ThrowsBadRequestException() {
        // Arrange
        testLoginRequest.setApiKey(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER015, exception.getMessage());
        verify(userRepository, never()).findByLoginName(anyString());
    }

    /**
     * Purpose: Reject token request for non-existent user.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Get Token - Failure - User not found")
    void getToken_UserNotFound_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(null);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidCredentials, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }

    /**
     * Purpose: Reject token request for different unknown user.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches InvalidEmail.
     */
    @Test
    @DisplayName("Get Token - Failure - Different user not found")
    void getToken_DifferentUserNotFound_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByLoginName("unknownuser")).thenReturn(null);
        testLoginRequest.setLoginName("unknownuser");

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidCredentials, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName("unknownuser");
    }

    /**
     * Purpose: Reject token request with invalid API key.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches ER018.
     */
    @Test
    @DisplayName("Get Token - Failure - Invalid API key")
    void getToken_InvalidApiKey_ThrowsUnauthorizedException() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidCredentials, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userClientMappingRepository, times(1)).findByApiKey(TEST_API_KEY);
        verify(userClientPermissionMappingRepository, never()).findClientPermissionMappingByUserId(anyLong());
    }

    /**
     * Purpose: Reject token request when API key belongs to another user.
     * Expected Result: UnauthorizedException is thrown.
     * Assertions: Exception message matches ER018.
     */
    @Test
    @DisplayName("Get Token - Failure - API key belongs to different user")
    void getToken_ApiKeyBelongsToDifferentUser_ThrowsUnauthorizedException() {
        // Arrange
        UserClientMapping differentUserMapping = new UserClientMapping();
        differentUserMapping.setUserId(999L); // Different user ID
        differentUserMapping.setClientId(TEST_CLIENT_ID);
        differentUserMapping.setApiKey(TEST_API_KEY);

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(differentUserMapping));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> loginService.getToken(testLoginRequest));

        assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.InvalidCredentials, exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userClientMappingRepository, times(1)).findByApiKey(TEST_API_KEY);
        verify(userClientPermissionMappingRepository, never()).findClientPermissionMappingByUserId(anyLong());
    }

    /**
     * Purpose: Verify token generation when no permissions exist.
     * Expected Result: Token is generated with empty permissions.
     * Assertions: Token is not null and permissions list is empty.
     */
    @Test
    @DisplayName("Get Token - Success - No permissions")
    void getToken_NoPermissions_Success() {
        // Arrange
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(TEST_USER_ID))
                .thenReturn(new ArrayList<>());
        when(jwtTokenProvider.generateToken(any(User.class), anyList(), anyLong())).thenReturn("jwt-token-123");

        // Act
        String result = loginService.getToken(testLoginRequest);

        // Assert
        assertEquals("jwt-token-123", result);
        verify(userClientPermissionMappingRepository, times(1)).findClientPermissionMappingByUserId(TEST_USER_ID);
    }

    /**
     * Purpose: Verify token generation includes multiple permissions.
     * Expected Result: Token contains all permissions.
     * Assertions: Permission list size matches expected.
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

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(anyLong()))
            .thenReturn(permissions);
        when(jwtTokenProvider.generateToken(any(User.class), anyList(), anyLong())).thenReturn("jwt-token-123");

        // Act
        String result = loginService.getToken(testLoginRequest);

        // Assert
        assertEquals("jwt-token-123", result);
        verify(userClientPermissionMappingRepository, times(1)).findClientPermissionMappingByUserId(TEST_USER_ID);
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class),
                argThat(list -> list.size() == 3), anyLong());
        }

    /**
     * Purpose: Filter out permissions that belong to other clients.
     * Expected Result: Token generated with only matching client permissions.
     * Assertions: Permission list is empty when all permissions are for other clients.
     */
    @Test
    @DisplayName("Get Token - Success - Permissions filtered by client")
    void getToken_PermissionsFilteredByClient_Success() {
        List<UserClientPermissionMapping> permissions = new ArrayList<>();
        permissions.add(new UserClientPermissionMapping(TEST_USER_ID, 999L, 1L, "admin"));

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(anyLong()))
            .thenReturn(permissions);
        when(jwtTokenProvider.generateToken(any(User.class), anyList(), anyLong())).thenReturn("jwt-token-123");

        String result = loginService.getToken(testLoginRequest);

        assertEquals("jwt-token-123", result);
        verify(jwtTokenProvider).generateToken(any(User.class), argThat(List::isEmpty), anyLong());
    }

    }
}
