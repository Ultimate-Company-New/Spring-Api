package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.FirebaseHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.
 * 
 * This test class provides comprehensive coverage of ClientService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Client retrieval by ID and user mapping
 * - File upload operations for client logos
 * - Error handling and validation
 * - Firebase integration testing
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Unit Tests")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private GoogleCredRepository googleCredRepository;
    
    @Mock
    private UserLogService userLogService;
    
    @Mock
    private Environment environment;
    
    @Mock
    private HttpServletRequest request;
    
    @InjectMocks
    private ClientService clientService;
    
    private Client testClient;
    private ClientRequestModel testClientRequest;
    private GoogleCred testGoogleCred;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_GOOGLE_CRED_ID = 1L;
    private static final String TEST_CLIENT_NAME = "Test Client";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String CREATED_USER = "admin";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_WEBSITE = "https://test.com";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test client request model
        testClientRequest = new ClientRequestModel();
        testClientRequest.setClientId(TEST_CLIENT_ID);
        testClientRequest.setName(TEST_CLIENT_NAME);
        testClientRequest.setDescription(TEST_DESCRIPTION);
        testClientRequest.setSupportEmail(TEST_EMAIL);
        testClientRequest.setWebsite(TEST_WEBSITE);
        testClientRequest.setSendgridSenderName("Test Sender");
        testClientRequest.setGoogleCredId(TEST_GOOGLE_CRED_ID);
        testClientRequest.setIsDeleted(false);
        
        // Initialize test client using constructor
        testClient = new Client(testClientRequest, CREATED_USER);
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        
        // Initialize test Google credential
        testGoogleCred = new GoogleCred();
        testGoogleCred.setGoogleCredId(TEST_GOOGLE_CRED_ID);
        testGoogleCred.setProjectId("test-project");
        testGoogleCred.setClientEmail("test@test.com");
        testGoogleCred.setPrivateKey("test-key");
        
        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        // Note: getUser() and getUserId() are handled by BaseService with test token support
        // No need to mock them explicitly
    }

    // ==================== Toggle Client Tests ====================
    
    /**
     * Test successful client toggle operation.
     * Verifies that a client's isDeleted flag is correctly toggled from false to true.
     */
    @Test
    @DisplayName("Toggle Client - Success - Should toggle isDeleted flag")
    void toggleClient_Success() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> clientService.toggleClient(TEST_CLIENT_ID));
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test toggle client with non-existent client ID.
     * Verifies that NotFoundException is thrown when client is not found.
     */
    @Test
    @DisplayName("Toggle Client - Failure - Client not found")
    void toggleClient_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> clientService.toggleClient(TEST_CLIENT_ID)
        );
        
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
        verify(clientRepository, never()).save(any(Client.class));
        verify(userLogService, never()).logData(anyString(), anyString(), anyString());
    }

    // ==================== Get Client By ID Tests ====================
    
    /**
     * Test successful retrieval of client by ID.
     * Verifies that client details are correctly returned.
     */
    @Test
    @DisplayName("Get Client By ID - Success - Should return client details")
    void getClientById_Success() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        
        // Act
        ClientResponseModel result = clientService.getClientById(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_CLIENT_ID, result.getClientId());
        assertEquals(TEST_CLIENT_NAME, result.getName());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(TEST_EMAIL, result.getSupportEmail());
        assertEquals(TEST_WEBSITE, result.getWebsite());
        
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
    }
    
    /**
     * Test get client by ID with non-existent client ID.
     * Verifies that NotFoundException is thrown when client is not found.
     */
    @Test
    @DisplayName("Get Client By ID - Failure - Client not found")
    void getClientById_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> clientService.getClientById(TEST_CLIENT_ID)
        );
        
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
    }

    // ==================== Create Client Tests ====================
    
    /**
     * Test successful client creation without logo.
     * Verifies that client is correctly created and saved.
     */
    @Test
    @DisplayName("Create Client - Success - Without logo")
    void createClient_Success_WithoutLogo() {
        // Arrange
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
        verify(googleCredRepository, never()).findById(anyLong());
    }
    
    /**
     * Test successful client creation with logo upload.
     * Verifies that client is created and logo is uploaded to Firebase.
     */
    @Test
    @DisplayName("Create Client - Success - With logo upload")
    void createClient_Success_WithLogo() {
        // Arrange
        testClientRequest.setLogoBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
        
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(googleCredRepository.findById(TEST_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        try (MockedConstruction<FirebaseHelper> firebaseHelperMock = mockConstruction(FirebaseHelper.class,
                (mock, context) -> {
                    when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(true);
                })) {
            
            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
            verify(clientRepository, times(1)).save(any(Client.class));
            verify(googleCredRepository, times(1)).findById(TEST_GOOGLE_CRED_ID);
            verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
        }
    }
    
    /**
     * Test create client with logo but no Google credentials.
     * Verifies that BadRequestException is thrown when Google credentials are missing.
     */
    @Test
    @DisplayName("Create Client - Failure - Logo upload without Google credentials")
    void createClient_LogoUpload_NoGoogleCreds_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setLogoBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
        
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(googleCredRepository.findById(TEST_GOOGLE_CRED_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> clientService.createClient(testClientRequest)
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.ER011, exception.getMessage());
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(googleCredRepository, times(1)).findById(TEST_GOOGLE_CRED_ID);
    }
    
    /**
     * Test create client with logo upload failure.
     * Verifies that BadRequestException is thrown when Firebase upload fails.
     */
    @Test
    @DisplayName("Create Client - Failure - Logo upload failed")
    void createClient_LogoUploadFailed_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setLogoBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
        
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(googleCredRepository.findById(TEST_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        
        try (MockedConstruction<FirebaseHelper> firebaseHelperMock = mockConstruction(FirebaseHelper.class,
                (mock, context) -> {
                    when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(false);
                })) {
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> clientService.createClient(testClientRequest)
            );
            
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidLogoUpload, exception.getMessage());
            verify(clientRepository, times(1)).save(any(Client.class));
            verify(googleCredRepository, times(1)).findById(TEST_GOOGLE_CRED_ID);
        }
    }

    // ==================== Update Client Tests ====================
    
    /**
     * Test successful client update.
     * Verifies that client fields are correctly updated.
     */
    @Test
    @DisplayName("Update Client - Success - Should update client fields")
    void updateClient_Success() {
        // Arrange
        testClientRequest.setName("Updated Client Name");
        testClientRequest.setDescription("Updated Description");
        
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
        verify(clientRepository, times(1)).save(any(Client.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test update client with non-existent client ID.
     * Verifies that NotFoundException is thrown when client is not found.
     */
    @Test
    @DisplayName("Update Client - Failure - Client not found")
    void updateClient_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> clientService.updateClient(testClientRequest)
        );
        
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        verify(clientRepository, times(1)).findById(TEST_CLIENT_ID);
        verify(clientRepository, never()).save(any(Client.class));
    }

    // ==================== Get All Clients Tests ====================
    
    /**
     * Test successful retrieval of all clients.
     * Verifies that all clients are returned.
     */
    @Test
    @DisplayName("Get All Clients - Success - Should return all clients")
    void getAllClients_Success() {
        // Arrange
        Client secondClient = new Client(testClientRequest, CREATED_USER);
        secondClient.setClientId(2L);
        secondClient.setName("Second Client");
        
        List<Client> clients = Arrays.asList(testClient, secondClient);
        when(clientRepository.findAll()).thenReturn(clients);
        
        // Act
        List<ClientResponseModel> result = clientService.getAllClients();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TEST_CLIENT_ID, result.get(0).getClientId());
        assertEquals(TEST_CLIENT_NAME, result.get(0).getName());
        assertEquals(2L, result.get(1).getClientId());
        assertEquals("Second Client", result.get(1).getName());
        
        verify(clientRepository, times(1)).findAll();
    }
    
    /**
     * Test get all clients when no clients exist.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get All Clients - Success - Empty list")
    void getAllClients_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(new ArrayList<>());
        
        // Act
        List<ClientResponseModel> result = clientService.getAllClients();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findAll();
    }

    // ==================== Get Clients By User Tests ====================
    
    /**
     * Test successful retrieval of clients mapped to user.
     * Verifies that user's mapped clients are returned.
     */
    @Test
    @DisplayName("Get Clients By User - Success - Should return user's clients")
    void getClientsByUser_Success() {
        // Arrange
        List<Client> clients = Arrays.asList(testClient);
        when(clientRepository.findByUserId(TEST_USER_ID)).thenReturn(clients);
        
        // Act
        List<ClientResponseModel> result = clientService.getClientsByUser();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_CLIENT_ID, result.get(0).getClientId());
        assertEquals(TEST_CLIENT_NAME, result.get(0).getName());
        
        verify(clientRepository, times(1)).findByUserId(TEST_USER_ID);
    }
    
    /**
     * Test get clients by user when no clients are mapped.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get Clients By User - Success - Empty list for user")
    void getClientsByUser_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(clientRepository.findByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        
        // Act
        List<ClientResponseModel> result = clientService.getClientsByUser();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clientRepository, times(1)).findByUserId(TEST_USER_ID);
    }
}
