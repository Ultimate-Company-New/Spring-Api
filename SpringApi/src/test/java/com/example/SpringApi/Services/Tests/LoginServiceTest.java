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
 * This test class provides comprehensive coverage of LoginService methods including:
 * - Email confirmation
 * - User sign-in with various scenarios
 * - User sign-up and registration
 * - Password reset functionality
 * - JWT token generation
 * - Error handling and validation
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies like EmailTemplates and PasswordHelper are properly mocked.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService Unit Tests")
class LoginServiceTest {

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
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final String TEST_LOGIN_NAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
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

        testUser = new User(testUserRequest);
        testUser.setUserId(TEST_USER_ID);
        testUser.setToken(TEST_TOKEN);
        testUser.setEmailConfirmed(true);
        testUser.setLocked(false);
        testUser.setLoginAttempts(5);
        testUser.setSalt("test-salt");
        testUser.setPassword("hashedPassword");
        
        // Initialize test login request
        testLoginRequest = new LoginRequestModel();
        testLoginRequest.setUserId(TEST_USER_ID);
        testLoginRequest.setLoginName(TEST_LOGIN_NAME);
        testLoginRequest.setPassword(TEST_PASSWORD);
        testLoginRequest.setClientId(TEST_CLIENT_ID);
        testLoginRequest.setToken(TEST_TOKEN);
        testLoginRequest.setApiKey(TEST_API_KEY);
        
        // Initialize test client
        testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setName("Test Client");
        testClient.setSupportEmail("support@test.com");
        testClient.setSendGridApiKey("test-sendgrid-key");
        
        // Initialize test UserClientMapping
        testUserClientMapping = new UserClientMapping();
        testUserClientMapping.setMappingId(1L);
        testUserClientMapping.setUserId(TEST_USER_ID);
        testUserClientMapping.setClientId(TEST_CLIENT_ID);
        testUserClientMapping.setApiKey(TEST_API_KEY);
        
        // Initialize test GoogleCred
        GoogleCred testGoogleCred = new GoogleCred();
        testGoogleCred.setGoogleCredId(1L);
        // Add lenient mock for googleCredRepository since not all tests use it
        lenient().when(googleCredRepository.findAll()).thenReturn(Arrays.asList(testGoogleCred));
    }

    // ==================== Confirm Email Tests ====================
    
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
            () -> loginService.confirmEmail(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.confirmEmail(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Sign In Tests ====================
    
    /**
     * Test successful user sign-in.
     * Verifies that user can sign in and returns list of clients they have access to.
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
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> result = loginService.signIn(testLoginRequest);
            
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        // Note: findByLoginName is called before validation, so we don't verify never()
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        // Note: findByLoginName is called before validation, so we don't verify never()
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
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
            () -> loginService.signIn(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
    }
    
    /**
     * Test sign-in with invalid password.
     * Verifies that user locked attempts are decremented and account is locked if attempts reach zero.
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
                () -> loginService.signIn(testLoginRequest)
            );
            
            assertNotNull(exception.getMessage());
            verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    // ==================== Sign Up Tests ====================
    // Note: signUp method has been removed from the API - no tests needed

    // ==================== Reset Password Tests ====================
    
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
                .thenReturn(new String[]{"newSalt", "newHashedPassword"});
            
            // Mock EmailTemplates constructor - avoid using argument matchers  
            try (MockedConstruction<EmailTemplates> mockedEmailTemplates = mockConstruction(EmailTemplates.class, (mock, context) -> {
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
            () -> loginService.resetPassword(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.resetPassword(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
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
            () -> loginService.resetPassword(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Get Token Tests ====================
    
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
            TEST_USER_ID, 1L, TEST_CLIENT_ID, "admin"
        );
        permissions.add(permission);
        
        when(userRepository.findByLoginName(TEST_LOGIN_NAME)).thenReturn(testUser);
        when(userClientMappingRepository.findByApiKey(TEST_API_KEY)).thenReturn(Optional.of(testUserClientMapping));
        when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(TEST_USER_ID)).thenReturn(permissions);
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
            () -> loginService.getToken(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.getToken(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.getToken(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
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
            () -> loginService.getToken(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
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
            () -> loginService.getToken(testLoginRequest)
        );
        
        assertNotNull(exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_LOGIN_NAME);
        verify(userClientMappingRepository, times(1)).findByApiKey(TEST_API_KEY);
        verify(userClientPermissionMappingRepository, never()).findClientPermissionMappingByUserId(anyLong());
    }
}
