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
        // Stubs repository lookups by client name.
        // When 'exists' is true, both existsByName(name) and findByName(name)
        // will return a positive result (Optional.of(testClient)). Otherwise
        // they return false/empty to simulate not found.
        lenient().when(clientRepository.existsByName(name)).thenReturn(exists);
        lenient().when(clientRepository.findByName(name))
                .thenReturn(exists ? Optional.of(testClient) : Optional.empty());
    }

    /**
     * Stub repository method findByName.
     * @param name repository lookup name
     * @param client optional client to return
     */
    protected void stubClientFindByName(String name, Optional<Client> client) {
        lenient().when(clientRepository.findByName(name)).thenReturn(client);
    }

    /**
     * Stub repository method findById.
     * @param id repository id to lookup
     * @param client optional client to return
     */
    protected void stubClientFindById(Long id, Optional<Client> client) {
        lenient().when(clientRepository.findById(id)).thenReturn(client);
    }

    /**
     * Stub repository save behavior.
     * Returns the provided client when save(any(Client.class)) is called.
     * @param clientToReturn the client instance to return from save
     */
    protected void stubClientSave(Client clientToReturn) {
        lenient().when(clientRepository.save(any(Client.class))).thenReturn(clientToReturn);
    }

    /**
     * Stub repository method findByUserId.
     * @param userId the user id used for lookup
     * @param clients the list of clients to return
     */
    protected void stubClientFindByUserId(Long userId, java.util.List<Client> clients) {
        lenient().when(clientRepository.findByUserId(userId)).thenReturn(clients);
    }

    /**
     * Stub GoogleCredRepository.findById.
     * @param id the id to lookup
     * @param cred optional GoogleCred to return
     */
    protected void stubGoogleCredFindById(Long id, Optional<GoogleCred> cred) {
        lenient().when(googleCredRepository.findById(id)).thenReturn(cred);
    }

    /**
     * Stub environment active profiles.
     * @param profiles array of profiles to return from environment.getActiveProfiles()
     */
    protected void stubEnvironmentProfiles(String[] profiles) {
        lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
    }

    /**
     * Stub request header retrieval.
     * Returns the provided value when request.getHeader(name) is called.
     * @param name header name
     * @param value header value to return
     */
    protected void stubRequestHeader(String name, String value) {
        lenient().when(request.getHeader(name)).thenReturn(value);
    }

    /**
     * Stub user log service return value for logData calls.
     * Always returns the provided boolean result for any logData invocation.
     * @param result boolean result to return from userLogService.logData(...)
     */
    protected void stubUserLogDataReturn(boolean result) {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
    }

    /**
     * Stub the controller-level createClient call to do nothing (for controller tests).
     */
    protected void stubServiceCreateClientDoNothing() {
        doNothing().when(mockClientService).createClient(any());
    }

    /**
     * Stub the controller-level updateClient call to do nothing (for controller tests).
     */
    protected void stubServiceUpdateClientDoNothing() {
        doNothing().when(mockClientService).updateClient(any());
    }

    /**
     * Stub the controller-level toggleClient call to do nothing (for controller tests).
     */
    protected void stubServiceToggleClientDoNothing() {
        doNothing().when(mockClientService).toggleClient(anyLong());
    }

    /**
     * Stub the controller-level getClientById to return a prepared response.
     * @param response the ClientResponseModel to return
     */
    protected void stubServiceGetClientById(ClientResponseModel response) {
        when(mockClientService.getClientById(anyLong())).thenReturn(response);
    }

    /**
     * Stub the controller-level getClientsByUser to return a prepared list.
     * @param response the list of ClientResponseModel to return
     */
    protected void stubServiceGetClientsByUser(java.util.List<ClientResponseModel> response) {
        when(mockClientService.getClientsByUser()).thenReturn(response);
    }
}
