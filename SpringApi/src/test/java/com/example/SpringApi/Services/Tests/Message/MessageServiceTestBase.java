package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Message;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Interface.IMessageSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for MessageService tests.
 * Contains common mocks, dependencies, and setup logic shared across all MessageService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class MessageServiceTestBase extends BaseTest {

    @Mock
    protected MessageRepository messageRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected MessageUserReadMapRepository messageUserReadMapRepository;

    @Mock
    protected MessageUserMapRepository messageUserMapRepository;

    @Mock
    protected MessageUserGroupMapRepository messageUserGroupMapRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Authorization authorization;

    @Mock
    protected Environment environment;

    @Mock
    IMessageSubTranslator messageServiceMock;

    @InjectMocks
    protected MessageService messageService;

    protected Message testMessage;
    protected MessageRequestModel validRequest;
    protected Client testClient;
    protected User testUser;
    
    protected static final Long TEST_MESSAGE_ID = DEFAULT_MESSAGE_ID;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = DEFAULT_USER_ID;
    protected static final String TEST_TITLE = "Test Message Title";
    protected static final String TEST_DESC_HTML = "<p>Test message description</p>";
    protected static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        // Initialize valid request
        validRequest = stubValidMessageRequest();
        validRequest.setMessageId(TEST_MESSAGE_ID);
        validRequest.setTitle(TEST_TITLE);
        validRequest.setDescriptionHtml(TEST_DESC_HTML);
        validRequest.setPublishDate(null);
        validRequest.setSendAsEmail(false);
        validRequest.setIsDeleted(false);
        validRequest.setUserIds(Arrays.asList(TEST_USER_ID));
        validRequest.setUserGroupIds(Arrays.asList(1L, 2L));

        // Initialize test message
        testMessage = stubTestMessage();
        testMessage.setMessageId(TEST_MESSAGE_ID);
        testMessage.setTitle(TEST_TITLE);
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());

        // Initialize test client
        testClient = stubTestClient();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setSendGridApiKey("test-api-key");
        testClient.setSendGridEmailAddress("test@sendgrid.com");
        testClient.setSendgridSenderName("Test Sender");

        // Initialize test user
        testUser = stubTestUser();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setIsDeleted(false);

        stubUserLogServiceSuccess();
        stubRequestHeaderSuccess();
        stubEnvironmentPropertySuccess();
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    /**
     * Stub for valid message request initialization.
     */
    protected MessageRequestModel stubValidMessageRequest() {
        return createValidMessageRequest();
    }

    /**
     * Stub for test message initialization.
     */
    protected Message stubTestMessage() {
        return createTestMessage();
    }

    /**
     * Stub for test client initialization.
     */
    protected Client stubTestClient() {
        return createTestClient();
    }

    /**
     * Stub for test user initialization.
     */
    protected User stubTestUser() {
        return createTestUser();
    }

    /**
     * Stub for UserLogService.logData mocking.
     */
    protected void stubUserLogServiceSuccess() {
        lenient().when(userLogService.logData(eq(DEFAULT_USER_ID), eq("Message"), anyString())).thenReturn(true);
    }

    /**
     * Stub for HttpServletRequest header mocking.
     */
    protected void stubRequestHeaderSuccess() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    /**
     * Stub for Environment property mocking.
     */
    protected void stubEnvironmentPropertySuccess() {
        lenient().when(environment.getProperty(eq("email.service"), anyString())).thenReturn("sendgrid");
    }
}
