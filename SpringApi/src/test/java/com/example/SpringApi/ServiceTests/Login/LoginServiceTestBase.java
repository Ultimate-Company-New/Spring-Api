package com.example.SpringApi.ServiceTests.Login;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Controllers.LoginController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.RequestModels.LoginRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.ILoginSubTranslator;
import com.example.SpringApi.Services.LoginService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base class for LoginService test classes.
 *
 * <p>Provides shared mock objects and common test data initialization for all Login-related tests.
 * This class should be extended by all LoginService test classes to ensure consistent test setup
 * and reduce code duplication.
 */
@ExtendWith(MockitoExtension.class)
abstract class LoginServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_USER_ID = 1L;
  protected static final Long DEFAULT_CLIENT_ID = 100L;
  protected static final String DEFAULT_LOGIN_NAME = "testuser";
  protected static final String DEFAULT_EMAIL = "test@example.com";
  protected static final String DEFAULT_FIRST_NAME = "Test";
  protected static final String DEFAULT_LAST_NAME = "User";
  protected static final String DEFAULT_CREATED_USER = "admin";
  protected static final Long DEFAULT_GOOGLE_CRED_ID = 100L;
  protected static final String DEFAULT_CLIENT_NAME = "Test Client";
  protected static final String DEFAULT_CLIENT_DESCRIPTION = "Test Client Description";
  protected static final String DEFAULT_SUPPORT_EMAIL = "support@testclient.com";
  protected static final String DEFAULT_WEBSITE = "https://testclient.com";

  @Mock protected UserRepository userRepository;

  @Mock protected UserClientMappingRepository userClientMappingRepository;

  @Mock protected UserClientPermissionMappingRepository userClientPermissionMappingRepository;

  @Mock protected ClientRepository clientRepository;

  @Mock protected GoogleCredRepository googleCredRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected JwtTokenProvider jwtTokenProvider;

  @Mock protected Environment environment;

  @Mock protected Authorization authorization;

  @Mock protected GoogleCred googleCred;

  // Mock helper classes to prevent external service calls during testing
  @Mock protected EmailTemplates emailTemplates;

  @Mock protected HttpServletRequest request;

  @Mock protected ILoginSubTranslator loginServiceMock;

  @InjectMocks protected LoginService loginService;

  protected LoginController loginController;

  protected User testUser;
  protected LoginRequestModel testLoginRequest;
  protected UserRequestModel testUserRequest;
  protected Client testClient;
  protected UserClientMapping testUserClientMapping;
  protected static final Long TEST_USER_ID = DEFAULT_USER_ID;
  protected static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
  protected static final String TEST_LOGIN_NAME = DEFAULT_LOGIN_NAME;
  protected static final String TEST_PASSWORD = "password123";
  protected static final String TEST_TOKEN = "test-token-123";
  protected static final String TEST_API_KEY = "test-api-key-123";

  /**
   * Sets up test data before each test execution. Initializes common test objects and configures
   * mock behaviors.
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
    testUserClientMapping =
        createTestUserClientMapping(1L, TEST_USER_ID, TEST_CLIENT_ID, TEST_API_KEY);

    // Initialize test GoogleCred
    GoogleCred testGoogleCred = createTestGoogleCred(1L);

    // Default stubs
    stubGoogleCredRepositoryFindAll(Arrays.asList(testGoogleCred));
    stubRequestHeaderAuthorization("Bearer test-token");

    // Controller with mocked service for permission tests
    loginController = new LoginController(loginServiceMock);
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addHeader("Authorization", "Bearer test-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
  }

  // ==================== FACTORY METHODS ====================

  protected User createTestUser() {
    return createTestUser(DEFAULT_USER_ID, DEFAULT_LOGIN_NAME, DEFAULT_EMAIL);
  }

  protected User createTestUser(Long userId, String loginName, String email) {
    User user = new User();
    user.setUserId(userId);
    user.setLoginName(loginName);
    user.setFirstName(DEFAULT_FIRST_NAME);
    user.setLastName(DEFAULT_LAST_NAME);
    user.setEmail(email);
    user.setIsDeleted(false);
    user.setCreatedUser(DEFAULT_CREATED_USER);
    user.setModifiedUser(DEFAULT_CREATED_USER);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return user;
  }

  protected Client createTestClient() {
    Client client = new Client();
    client.setClientId(DEFAULT_CLIENT_ID);
    client.setName(DEFAULT_CLIENT_NAME);
    client.setDescription(DEFAULT_CLIENT_DESCRIPTION);
    client.setSupportEmail(DEFAULT_SUPPORT_EMAIL);
    client.setWebsite(DEFAULT_WEBSITE);
    client.setIsDeleted(false);
    client.setCreatedUser(DEFAULT_CREATED_USER);
    client.setModifiedUser(DEFAULT_CREATED_USER);
    client.setCreatedAt(LocalDateTime.now());
    client.setUpdatedAt(LocalDateTime.now());
    return client;
  }

  protected UserClientMapping createTestUserClientMapping(
      Long mappingId, Long userId, Long clientId, String apiKey) {
    UserClientMapping mapping = new UserClientMapping();
    mapping.setMappingId(mappingId);
    mapping.setUserId(userId);
    mapping.setClientId(clientId);
    mapping.setApiKey(apiKey);
    mapping.setCreatedUser(DEFAULT_CREATED_USER);
    mapping.setModifiedUser(DEFAULT_CREATED_USER);
    return mapping;
  }

  protected LoginRequestModel createValidLoginRequest() {
    LoginRequestModel loginRequest = new LoginRequestModel();
    loginRequest.setUserId(DEFAULT_USER_ID);
    loginRequest.setLoginName(DEFAULT_LOGIN_NAME);
    loginRequest.setPassword("testPassword123");
    loginRequest.setClientId(DEFAULT_CLIENT_ID);
    return loginRequest;
  }

  protected GoogleCred createTestGoogleCred(Long googleCredId) {
    GoogleCred cred = new GoogleCred();
    cred.setGoogleCredId(googleCredId);
    cred.setProjectId("test-project");
    return cred;
  }

  /** Stub googleCredRepository.findAll(). */
  protected void stubGoogleCredRepositoryFindAll(List<GoogleCred> creds) {
    lenient().when(googleCredRepository.findAll()).thenReturn(creds);
  }

  /** Stub request header retrieval. */
  protected void stubRequestHeaderAuthorization(String value) {
    lenient().when(request.getHeader("Authorization")).thenReturn(value);
  }

  /** Stub userRepository.findByLoginName. */
  protected void stubUserRepositoryFindByLoginName(String loginName, User user) {
    lenient().when(userRepository.findByLoginName(loginName)).thenReturn(user);
  }

  /** Stub userRepository.findById. */
  protected void stubUserRepositoryFindById(Long userId, Optional<User> user) {
    lenient().when(userRepository.findById(userId)).thenReturn(user);
  }

  /** Stub userRepository.save. */
  protected void stubUserRepositorySave(User user) {
    lenient().when(userRepository.save(any(User.class))).thenReturn(user);
  }

  /** Stub clientRepository.findFirstByOrderByClientIdAsc. */
  protected void stubClientRepositoryFindFirstByOrderByClientIdAsc(Client client) {
    lenient().when(clientRepository.findFirstByOrderByClientIdAsc()).thenReturn(client);
  }

  /** Stub clientRepository.findById. */
  protected void stubClientRepositoryFindById(Long clientId, Optional<Client> client) {
    lenient().when(clientRepository.findById(clientId)).thenReturn(client);
  }

  /** Stub environment email properties for SendGrid. */
  protected void stubEnvironmentSendGridProperties(
      String senderEmail, String senderName, String apiKey) {
    lenient().when(environment.getProperty("email.sender.address")).thenReturn(senderEmail);
    lenient().when(environment.getProperty("email.sender.name")).thenReturn(senderName);
    lenient().when(environment.getProperty("email.service", "sendgrid")).thenReturn("sendgrid");
    lenient().when(environment.getProperty("sendgrid.api.key")).thenReturn(apiKey);
  }

  /** Stub environment email properties for Brevo. */
  protected void stubEnvironmentBrevoProperties(
      String senderEmail, String senderName, String apiKey) {
    lenient().when(environment.getProperty("email.service", "sendgrid")).thenReturn("brevo");
    lenient().when(environment.getProperty("brevo.sender.address")).thenReturn(senderEmail);
    lenient().when(environment.getProperty("brevo.sender.name")).thenReturn(senderName);
    lenient().when(environment.getProperty("brevo.api.key")).thenReturn(apiKey);
  }

  /** Stub userClientMappingRepository.findByApiKey. */
  protected void stubUserClientMappingRepositoryFindByApiKey(
      String apiKey, Optional<UserClientMapping> mapping) {
    lenient().when(userClientMappingRepository.findByApiKey(apiKey)).thenReturn(mapping);
  }

  /** Stub userClientMappingRepository.findByUserId. */
  protected void stubUserClientMappingRepositoryFindByUserId(
      Long userId, List<UserClientMapping> mappings) {
    lenient().when(userClientMappingRepository.findByUserId(userId)).thenReturn(mappings);
  }

  /** Stub userClientPermissionMappingRepository.findClientPermissionMappingByUserId. */
  protected void stubUserClientPermissionMappingByUserId(
      Long userId,
      List<com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping> mappings) {
    lenient()
        .when(userClientPermissionMappingRepository.findClientPermissionMappingByUserId(userId))
        .thenReturn(mappings);
  }

  /** Stub jwtTokenProvider.generateToken. */
  protected void stubJwtTokenProviderGenerateToken(String token) {
    lenient()
        .when(jwtTokenProvider.generateToken(any(User.class), anyList(), anyLong()))
        .thenReturn(token);
  }

  /** Stub PasswordHelper random password and hash/salt. */
  protected org.mockito.MockedStatic<PasswordHelper> stubPasswordHelperRandomPassword(
      String randomPassword, String[] saltAndHash) {
    org.mockito.MockedStatic<PasswordHelper> mocked = mockStatic(PasswordHelper.class);
    mocked.when(PasswordHelper::getRandomPassword).thenReturn(randomPassword);
    mocked.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString())).thenReturn(saltAndHash);
    return mocked;
  }

  /** Stub PasswordHelper.checkPassword. */
  protected org.mockito.MockedStatic<PasswordHelper> stubPasswordHelperCheckPassword(
      boolean result) {
    org.mockito.MockedStatic<PasswordHelper> mocked = mockStatic(PasswordHelper.class);
    mocked
        .when(() -> PasswordHelper.checkPassword(anyString(), anyString(), anyString()))
        .thenReturn(result);
    return mocked;
  }

  /** Stub EmailTemplates.sendResetPasswordEmail. */
  protected org.mockito.MockedConstruction<EmailTemplates> stubEmailTemplatesSendResetPasswordEmail(
      boolean result) {
    return mockConstruction(
        EmailTemplates.class,
        (mock, context) ->
            lenient()
                .when(mock.sendResetPasswordEmail(anyString(), anyString()))
                .thenReturn(result));
  }

  /** Stub controller service to throw UnauthorizedException for permission tests. */
  protected void stubLoginServiceThrowsUnauthorizedOnResetPassword() {
    lenient()
        .when(loginServiceMock.resetPassword(any()))
        .thenThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED));
  }

  protected void stubLoginServiceThrowsUnauthorizedOnConfirmEmail() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(loginServiceMock)
        .confirmEmail(any());
  }

  protected void stubLoginServiceThrowsUnauthorizedOnGetToken() {
    lenient()
        .when(loginServiceMock.getToken(any()))
        .thenThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED));
  }

  protected void stubLoginServiceThrowsUnauthorizedOnSignIn() {
    lenient()
        .when(loginServiceMock.signIn(any()))
        .thenThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED));
  }
}

