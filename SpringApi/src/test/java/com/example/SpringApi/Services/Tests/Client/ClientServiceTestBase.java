package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
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

import com.example.SpringApi.Controllers.ClientController;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for ClientService tests.
 * Contains common mocks, dependencies, and setup logic shared across all
 * ClientService test classes.
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

    @InjectMocks
    protected ClientService clientService;

    protected Client testClient;
    protected ClientRequestModel testClientRequest;
    protected GoogleCred testGoogleCred;

    protected ClientService mockClientService;
    protected ClientController clientController;

    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final String TEST_LOGO_BASE64 = "base64EncodedString";

    @BeforeEach
    void setUp() {
        mockClientService = mock(ClientService.class);
        clientController = new ClientController(mockClientService);

        testClientRequest = createValidClientRequest(TEST_CLIENT_ID, DEFAULT_CLIENT_NAME);
        testClient = createTestClient(TEST_CLIENT_ID, DEFAULT_CLIENT_NAME);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        testClient.setCreatedUser(DEFAULT_CREATED_USER);
        testGoogleCred = createTestGoogleCred(DEFAULT_GOOGLE_CRED_ID);
        testClientRequest.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);
        testClient.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);

        stubRequestHeader("Authorization", "Bearer token");
        stubUserLogDataReturn(true);
        stubEnvironmentProfiles(new String[] { "test" });
        stubGoogleCredFindById(DEFAULT_GOOGLE_CRED_ID, Optional.of(testGoogleCred));
        ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
    }

    protected void stubClientExistsByName(String name, boolean exists) {
        lenient().when(clientRepository.existsByName(name)).thenReturn(exists);
        lenient().when(clientRepository.findByName(name))
                .thenReturn(exists ? Optional.of(testClient) : Optional.empty());
    }

    protected void stubClientFindByName(String name, Optional<Client> client) {
        lenient().when(clientRepository.findByName(name)).thenReturn(client);
    }

    protected void stubClientFindById(Long id, Optional<Client> client) {
        lenient().when(clientRepository.findById(id)).thenReturn(client);
    }

    protected void stubClientSave(Client clientToReturn) {
        lenient().when(clientRepository.save(any(Client.class))).thenReturn(clientToReturn);
    }

    protected void stubClientFindByUserId(Long userId, java.util.List<Client> clients) {
        lenient().when(clientRepository.findByUserId(userId)).thenReturn(clients);
    }

    protected void stubGoogleCredFindById(Long id, Optional<GoogleCred> cred) {
        lenient().when(googleCredRepository.findById(id)).thenReturn(cred);
    }

    protected void stubEnvironmentProfiles(String[] profiles) {
        lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
    }

    /**
     * Centralized stub for request headers.
     */
    protected void stubRequestHeader(String name, String value) {
        lenient().when(request.getHeader(name)).thenReturn(value);
    }

    /**
     * Centralized stub for user log data results.
     */
    protected void stubUserLogDataReturn(boolean result) {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
    }

    /**
     * Stubs the client service createClient method for controller tests.
     */
    protected void stubServiceCreateClientDoNothing() {
        doNothing().when(mockClientService).createClient(any());
    }

    /**
     * Stubs the client service updateClient method for controller tests.
     */
    protected void stubServiceUpdateClientDoNothing() {
        doNothing().when(mockClientService).updateClient(any());
    }

    /**
     * Stubs the client service toggleClient method for controller tests.
     */
    protected void stubServiceToggleClientDoNothing() {
        doNothing().when(mockClientService).toggleClient(anyLong());
    }

    /**
     * Stubs the client service getClientById method for controller tests.
     */
    protected void stubServiceGetClientById(ClientResponseModel response) {
        when(mockClientService.getClientById(anyLong())).thenReturn(response);
    }

    /**
     * Stubs the client service getClientsByUser method for controller tests.
     */
    protected void stubServiceGetClientsByUser(java.util.List<ClientResponseModel> response) {
        when(mockClientService.getClientsByUser()).thenReturn(response);
    }
}
