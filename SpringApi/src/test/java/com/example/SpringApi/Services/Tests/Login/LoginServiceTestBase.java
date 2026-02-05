package com.example.SpringApi.Services.Tests.Login;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
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
import com.example.SpringApi.Services.Tests.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.lenient;

/**
 * Base class for LoginService test classes.
 * 
 * Provides shared mock objects and common test data initialization for all Login-related tests.
 * This class should be extended by all LoginService test classes to ensure consistent
 * test setup and reduce code duplication.
 */
@ExtendWith(MockitoExtension.class)
public abstract class LoginServiceTestBase extends BaseTest {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserClientMappingRepository userClientMappingRepository;

    @Mock
    protected UserClientPermissionMappingRepository userClientPermissionMappingRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected GoogleCredRepository googleCredRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected JwtTokenProvider jwtTokenProvider;

    @Mock
    protected Environment environment;

    @Mock
    protected Authorization authorization;

    @Mock
    protected GoogleCred googleCred;

    // Mock helper classes to prevent external service calls during testing
    @Mock
    protected EmailTemplates emailTemplates;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected LoginService loginService;

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
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }
}
