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
 * | ConfirmEmailTests                       | 6               |
 * | SignInTests                             | 27              |
 * | ResetPasswordTests                      | 14              |
 * | GetTokenTests                           | 19              |
 * | **Total**                               | **66**          |
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
         * Test successful email confirmation.
         * Verifies that user's email is confirmed when valid token is provided.
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
     * Test confirm email with invalid token.
     * Verifies that UnauthorizedException is thrown when token doesn't match.
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
     * Test confirm email with non-existent user.
     * Verifies that NotFoundException is thrown when user is not found.
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
    }

    // ==================== Sign In Tests ====================

    @Nested
    @DisplayName("SignIn Tests")
    class SignInTests {

    /**
     * Test successful user sign-in.
     * Verifies that user can sign in and returns list of clients they have access
     * to.
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
     * Test sign-in with missing login name.
     * Verifies that BadRequestException is thrown.
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

        assertEquals(com.example.SpringApi.ErrorMessages.UserErrorMessages.InvalidLoginName, exception.getMessage());
        // Note: findByLoginName is called before validation, so we don't verify never()
    }

    /**
     * Test sign-in with null login name.
     * Verifies that BadRequestException is thrown.
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
     * Test sign-in with whitespace-only login name.
     * Verifies that BadRequestException is thrown.
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
     * Test sign-in with missing password.
     * Verifies that BadRequestException is thrown.
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
     * Test sign-in with null password.
     * Verifies that BadRequestException is thrown.
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
     * Test sign-in with null request.
     * Verifies that appropriate exception is thrown.
     */
    @Test
    @DisplayName("Sign In - Failure - Null request")
    void signIn_NullRequest_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> loginService.signIn(null));
    }

    /**
     * Test sign-in with non-existent user.
     * Verifies that NotFoundException is thrown.
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
     * Test sign-in with non-existent user by different login name.
     * Verifies that NotFoundException is thrown consistently.
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
     * Test sign-in with unconfirmed email.
     * Verifies that UnauthorizedException is thrown.
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
     * Test sign-in with locked account.
     * Verifies that UnauthorizedException is thrown.
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
     * Test sign-in with account locked after multiple failed attempts.
     * Verifies that account state is checked properly.
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
     * Test sign-in with no password set.
     * Verifies that UnauthorizedException is thrown.
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
     * Test sign-in with invalid password.
     * Verifies that user locked attempts are decremented and account is locked if
     * attempts reach zero.
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
     * Test sign-in with invalid password multiple times.
     * Verifies that attempts are decremented correctly.
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

            assertEquals(com.example.SpringApi.ErrorMessages.LoginErrorMessages.ER007, exception.getMessage());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    /**
     * Test sign-in with valid password and multiple clients.
     * Verifies that all accessible clients are returned.
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
    }

    // Note: signUp method has been removed from the API - no tests needed

    // ==================== Reset Password Tests ====================

    @Nested
    @DisplayName("ResetPassword Tests")
    class ResetPasswordTests {

    /**
     * Test successful password reset.
     * Verifies that password is reset and user is notified.
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
     * Test reset password with missing login name.
     * Verifies that BadRequestException is thrown.
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
     * Test reset password with null login name.
     * Verifies that BadRequestException is thrown.
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
     * Test reset password with whitespace-only login name.
     * Verifies that BadRequestException is thrown.
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
     * Test reset password with non-existent user.
     * Verifies that NotFoundException is thrown.
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
     * Test reset password with different non-existent user.
     * Verifies consistent error handling.
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
     * Test reset password for user without password set.
     * Verifies that BadRequestException is thrown.
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
     * Test reset password for user with empty password.
     * Verifies that BadRequestException is thrown.
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
     * Test reset password with null request.
     * Verifies that exception is thrown.
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
     * Test successful token generation.
     * Verifies that JWT token is generated with user permissions.
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
     * Test get token with missing login name.
     * Verifies that BadRequestException is thrown.
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
     * Test get token with null login name.
     * Verifies that BadRequestException is thrown.
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
     * Test get token with missing API key.
     * Verifies that BadRequestException is thrown.
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
     * Test get token with null API key.
     * Verifies that BadRequestException is thrown.
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
     * Test get token with non-existent user.
     * Verifies that UnauthorizedException is thrown.
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
     * Test get token with different non-existent user.
     * Verifies consistent error handling.
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
     * Test get token with invalid API key.
     * Verifies that UnauthorizedException is thrown.
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
     * Test get token with API key belonging to different user.
     * Verifies that UnauthorizedException is thrown.
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
     * Test get token with no permissions.
     * Verifies that token is generated with empty permissions list.
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
     * Test get token with multiple permissions.
     * Verifies that all permissions are included in token generation.
     */
    @Test
    @DisplayName("Get Token - Success - Multiple permissions")
    void getToken_MultiplePermissions_Success() {
        // Arrange
        List<UserClientPermissionMapping> permissions = new ArrayList<>();
        permissions.add(new UserClientPermissionMapping(TEST_USER_ID, 1L, TEST_CLIENT_ID, "admin"));
        permissions.add(new UserClientPermissionMapping(TEST_USER_ID, 2L, TEST_CLIENT_ID, "editor"));
        permissions.add(new UserClientPermissionMapping(TEST_USER_ID, 3L, TEST_CLIENT_ID, "viewer"));

        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(TEST_USER_ID))
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

    }
}
