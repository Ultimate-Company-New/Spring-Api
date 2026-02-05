package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Tests.BaseTest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for ClientService tests.
 * Contains common mocks, dependencies, and setup logic shared across all ClientService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ClientServiceTestBase extends BaseTest {

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected GoogleCredRepository googleCredRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected Environment environment;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Authorization authorization;

    @InjectMocks
    protected ClientService clientService;

    protected Client testClient;
    protected ClientRequestModel testClientRequest;
    protected GoogleCred testGoogleCred;

    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final String TEST_LOGO_BASE64 = "base64EncodedString";

    @BeforeEach
    void setUp() {
        testClientRequest = createValidClientRequest(TEST_CLIENT_ID, DEFAULT_CLIENT_NAME);
        testClient = createTestClient(TEST_CLIENT_ID, DEFAULT_CLIENT_NAME);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        testClient.setCreatedUser(DEFAULT_CREATED_USER);
        testGoogleCred = createTestGoogleCred(DEFAULT_GOOGLE_CRED_ID);
        testClientRequest.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);
        testClient.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer token");
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(testGoogleCred));
        ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
    }
}
