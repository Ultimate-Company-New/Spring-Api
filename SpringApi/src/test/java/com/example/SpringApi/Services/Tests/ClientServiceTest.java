package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | ToggleClientTests                       | 8               |
 * | GetClientByIdTests                      | 8               |
 * | CreateClientTests                       | 24              |
 * | UpdateClientTests                       | 31              |
 * | GetClientsByUserTests                   | 7               |
 * | **Total**                               | **78**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Unit Tests")
class ClientServiceTest extends BaseTest {

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
    private static final String TEST_LOGO_BASE64 = "base64EncodedString";

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

    @Nested
    @DisplayName("toggleClient Tests")
    class ToggleClientTests {

        /**
         * Purpose: Verify toggling an active client marks it deleted.
         * Expected Result: Client is saved with deleted flag set.
         * Assertions: Deleted flag is true and save/log calls occur.
         */
        @Test
        @DisplayName("Toggle Client - Client found and active - Success toggles to deleted")
        void toggleClient_ActiveClient_Success() {
            // Arrange
            testClient.setIsDeleted(false);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act
            assertDoesNotThrow(() -> clientService.toggleClient(TEST_CLIENT_ID));

            // Assert
            assertTrue(testClient.getIsDeleted());
            verify(clientRepository).save(testClient);
            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify toggling a deleted client restores it.
         * Expected Result: Client is saved with deleted flag cleared.
         * Assertions: Deleted flag is false and save is called.
         */
        @Test
        @DisplayName("Toggle Client - Client found and deleted - Success toggles to active")
        void toggleClient_DeletedClient_Success() {
            // Arrange
            testClient.setIsDeleted(true);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act
            assertDoesNotThrow(() -> clientService.toggleClient(TEST_CLIENT_ID));

            // Assert
            assertFalse(testClient.getIsDeleted());
            verify(clientRepository).save(testClient);
        }

        /**
         * Purpose: Ensure toggling a missing client fails.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId and save is not called.
         */
        @Test
        @DisplayName("Toggle Client - Client not found - ThrowsNotFoundException")
        void toggleClient_ClientNotFound_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(TEST_CLIENT_ID));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
            verify(clientRepository, never()).save(any());
        }

        /**
         * Purpose: Validate negative ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Toggle Client - Negative ID - ThrowsNotFoundException")
        void toggleClient_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(clientRepository.findById(negativeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(negativeId));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate zero ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Toggle Client - Zero ID - ThrowsNotFoundException")
        void toggleClient_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(clientRepository.findById(zeroId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(zeroId));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate max long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Toggle Client - Max Long ID - ThrowsNotFoundException")
        void toggleClient_MaxLongId_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(Long.MAX_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate min long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Toggle Client - Min Long ID - ThrowsNotFoundException")
        void toggleClient_MinLongId_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(Long.MIN_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify multiple toggles switch state each time.
         * Expected Result: State flips on each call.
         * Assertions: Deleted flag toggles and repository calls are counted.
         */
        @Test
        @DisplayName("Toggle Client - Multiple toggles in sequence - Success")
        void toggleClient_MultipleToggles_Success() {
            // Arrange
            testClient.setIsDeleted(false);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act - First toggle
            assertDoesNotThrow(() -> clientService.toggleClient(TEST_CLIENT_ID));
            assertTrue(testClient.getIsDeleted());

            // Act - Second toggle
            assertDoesNotThrow(() -> clientService.toggleClient(TEST_CLIENT_ID));
            assertFalse(testClient.getIsDeleted());

            // Assert
            verify(clientRepository, times(2)).findById(TEST_CLIENT_ID);
            verify(clientRepository, times(2)).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("getClientById Tests")
    class GetClientByIdTests {

        /**
         * Purpose: Verify client details are returned for a valid ID.
         * Expected Result: Response contains client fields.
         * Assertions: Response fields match the test client.
         */
        @Test
        @DisplayName("Get Client By ID - Client found - Returns details")
        void getClientById_Success() {
            // Arrange
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            // Act
            ClientResponseModel result = clientService.getClientById(TEST_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_CLIENT_ID, result.getClientId());
            assertEquals(testClient.getName(), result.getName());
        }

        /**
         * Purpose: Validate missing client ID returns not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Client By ID - Client not found - ThrowsNotFoundException")
        void getClientById_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(TEST_CLIENT_ID));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate negative ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Client By ID - Negative ID - ThrowsNotFoundException")
        void getClientById_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(clientRepository.findById(negativeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(negativeId));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate zero ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Client By ID - Zero ID - ThrowsNotFoundException")
        void getClientById_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(clientRepository.findById(zeroId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(zeroId));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate max long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Client By ID - Max Long ID - ThrowsNotFoundException")
        void getClientById_MaxLongId_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Validate min long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Client By ID - Min Long ID - ThrowsNotFoundException")
        void getClientById_MinLongId_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify deleted clients can be returned by ID.
         * Expected Result: Response indicates deleted status.
         * Assertions: Response has isDeleted=true.
         */
        @Test
        @DisplayName("Get Client By ID - Deleted client - Returns details")
        void getClientById_DeletedClient_Success() {
            // Arrange
            testClient.setIsDeleted(true);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            // Act
            ClientResponseModel result = clientService.getClientById(TEST_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsDeleted());
            assertEquals(TEST_CLIENT_ID, result.getClientId());
        }

        /**
         * Purpose: Verify all client fields are mapped into the response.
         * Expected Result: Response includes name, description, email, website, logo.
         * Assertions: Response fields match populated test client values.
         */
        @Test
        @DisplayName("Get Client By ID - Verify all fields populated - Success")
        void getClientById_AllFieldsPopulated_Success() {
            // Arrange
            testClient.setName("Complete Client");
            testClient.setDescription("Full description");
            testClient.setSupportEmail("support@test.com");
            testClient.setWebsite("https://test.com");
            testClient.setLogoUrl("https://logo.url/image.png");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            // Act
            ClientResponseModel result = clientService.getClientById(TEST_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals("Complete Client", result.getName());
            assertEquals("Full description", result.getDescription());
            assertEquals("support@test.com", result.getSupportEmail());
            assertEquals("https://test.com", result.getWebsite());
            assertEquals("https://logo.url/image.png", result.getLogoUrl());
        }
    }

    @Nested
    @DisplayName("createClient Tests")
    class CreateClientTests {

        /**
         * Purpose: Verify client creation succeeds without logo upload.
         * Expected Result: Client is saved and log is recorded.
         * Assertions: Save and log calls occur.
         */
        @Test
        @DisplayName("Create Client - Valid request without logo - Success")
        void createClient_NoLogo_Success() {
            // Arrange
            testClientRequest.setLogoBase64(null);
            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

            // Assert
            verify(clientRepository).save(any(Client.class));
            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Reject duplicate client names.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message indicates name already exists.
         */
        @Test
        @DisplayName("Create Client - Duplicate name - ThrowsBadRequestException")
        void createClient_DuplicateName_ThrowsBadRequestException() {
            // Arrange
            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(true);

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("already exists"));
            verify(clientRepository, never()).save(any());
        }

        /**
         * Purpose: Default isDeleted when null on create.
         * Expected Result: Client is saved with isDeleted=false.
         * Assertions: Saved client has isDeleted false.
         */
        @Test
        @DisplayName("Create Client - Null isDeleted - Defaults to false")
        void createClient_NullIsDeleted_DefaultsFalse() {
            testClientRequest.setIsDeleted(null);
            testClientRequest.setLogoBase64(null);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertFalse(captor.getValue().getIsDeleted());
        }

        /**
         * Purpose: Reject null create request.
         * Expected Result: NullPointerException is thrown.
         * Assertions: Exception type is NullPointerException.
         */
        @Test
        @DisplayName("Create Client - Null request - ThrowsBadRequestException")
        void createClient_NullRequest_ThrowsBadRequestException() {
            assertThrows(NullPointerException.class, () -> clientService.createClient(null));
        }

        /**
         * Purpose: Allow empty or null logo and skip upload.
         * Expected Result: Client is created without image upload.
         * Assertions: Image repository is not accessed.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Empty or Null Logo - Success (skips upload)")
        void createClient_EmptyLogo_Success(String logoDetails) {
            // Arrange
            testClientRequest.setLogoBase64(logoDetails);
            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

            // Assert
            // ImgBB/Firebase helpers should NOT be called
            verify(googleCredRepository, never()).findById(any());
        }

        /**
         * Purpose: Verify Firebase logo upload path succeeds.
         * Expected Result: Client is saved with logo URL.
         * Assertions: Firebase helper is invoked and save occurs.
         */
        @Test
        @DisplayName("Create Client - With Firebase Logo - Success")
        void createClient_WithFirebaseLogo_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient); // First save
            when(googleCredRepository.findById(DEFAULT_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class,
                    (mock, context) -> {
                        when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(true);
                    })) {

                // Act
                clientService.createClient(testClientRequest);

                // Assert
                verify(clientRepository, atLeastOnce()).save(any(Client.class));
                // We expect upload to have happened
                assertEquals(1, fbMock.constructed().size());
                verify(fbMock.constructed().get(0)).uploadFileToFirebase(eq(TEST_LOGO_BASE64), anyString());
            }
        }

        /**
         * Purpose: Verify ImgBB logo upload path succeeds.
         * Expected Result: Client is saved with ImgBB logo URL.
         * Assertions: ImgBB helper is invoked and save occurs.
         */
        @Test
        @DisplayName("Create Client - With ImgBB Logo - Success")
        void createClient_WithImgbbLogo_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);
            testClient.setImgbbApiKey("some-api-key");

            // Mock created client having an API key
            Client savedClientWithKey = new Client(testClientRequest, DEFAULT_CREATED_USER);
            savedClientWithKey.setImgbbApiKey("test-api-key");

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            // Return client that has API key for validation
            when(clientRepository.save(any(Client.class))).thenReturn(savedClientWithKey);

            ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                    "http://imgbb.com/image.png",
                    "deleteHash123");

            try (MockedConstruction<ImgbbHelper> imgMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    })) {

                // Act
                clientService.createClient(testClientRequest);

                // Assert
                // Verify repository saved the updated logo URL
                verify(clientRepository, times(2)).save(any(Client.class)); // Initial save + update with logo
            }
        }

        /**
         * Purpose: Reject ImgBB upload when API key is missing.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER020.
         */
        @Test
        @DisplayName("Create Client - ImgBB missing API Key - ThrowsBadRequestException")
        void createClient_ImgbbMissingKey_ThrowsBadRequestException() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);

            // Saved client has null API Key
            testClient.setImgbbApiKey(null);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ConfigurationErrorMessages.ImgbbApiKeyNotConfigured, ex.getMessage());
        }

        /**
         * Purpose: Reject invalid image location configuration.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER019.
         */
        @Test
        @DisplayName("Create Client - Invalid Image Location Config - ThrowsBadRequestException")
        void createClient_InvalidImageConfig_ThrowsBadRequestException() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "invalid-provider");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("Invalid imageLocation configuration"));
        }

        /**
         * Purpose: Handle Firebase upload failures.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER021.
         */
        @Test
        @DisplayName("Create Client - Firebase Upload Fail - ThrowsBadRequestException")
        void createClient_FirebaseUploadFail_ThrowsBadRequestException() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(DEFAULT_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class,
                    (mock, context) -> {
                        when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(false);
                    })) {

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> clientService.createClient(testClientRequest));
                assertEquals(ErrorMessages.ClientErrorMessages.InvalidLogoUpload, ex.getMessage());
            }
        }

        /**
         * Purpose: Reject Firebase upload when Google credential is missing.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER011.
         */
        @Test
        @DisplayName("Create Client - Firebase GoogleCred missing - ThrowsBadRequestException")
        void createClient_FirebaseGoogleCredMissing_ThrowsBadRequestException() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(DEFAULT_GOOGLE_CRED_ID)).thenReturn(Optional.empty());

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.UserErrorMessages.ER011, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidName.
         */
        @Test
        @DisplayName("Create Client - Whitespace only name - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyName_ThrowsBadRequestException() {
            testClientRequest.setName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only description.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidDescription.
         */
        @Test
        @DisplayName("Create Client - Whitespace only description - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only support email.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSupportEmail.
         */
        @Test
        @DisplayName("Create Client - Whitespace only email - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyEmail_ThrowsBadRequestException() {
            testClientRequest.setSupportEmail("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only website.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidWebsite.
         */
        @Test
        @DisplayName("Create Client - Whitespace only website - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyWebsite_ThrowsBadRequestException() {
            testClientRequest.setWebsite("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
        }

        /**
         * Purpose: Verify creation succeeds with all fields populated.
         * Expected Result: Client is saved with full details.
         * Assertions: Save is called and no exception occurs.
         */
        @Test
        @DisplayName("Create Client - Valid request with all fields - Success")
        void createClient_AllFieldsValid_Success() {
            // Arrange
            testClientRequest.setName("Valid Client");
            testClientRequest.setDescription("Valid Description");
            testClientRequest.setSupportEmail("valid@email.com");
            testClientRequest.setWebsite("https://valid.com");
            testClientRequest.setSendgridSenderName("Sender Name");
            testClientRequest.setLogoBase64(null);

            when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
            verify(clientRepository).save(any(Client.class));
            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Validate very long name is accepted.
         * Expected Result: Client creation succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Create Client - Very long name - Success")
        void createClient_VeryLongName_Success() {
            // Arrange
            String longName = "A".repeat(500);
            testClientRequest.setName(longName);
            when(clientRepository.existsByName(longName)).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Validate special characters in name are accepted.
         * Expected Result: Client creation succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Create Client - Special characters in name - Success")
        void createClient_SpecialCharactersInName_Success() {
            // Arrange
            String specialName = "Client & Co. #123";
            testClientRequest.setName(specialName);
            when(clientRepository.existsByName(specialName)).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Validate unicode description is accepted.
         * Expected Result: Client creation succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Create Client - Unicode characters in description - Success")
        void createClient_UnicodeCharacters_Success() {
            // Arrange
            testClientRequest.setDescription("Description with émojis and ñ characters");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Validate max long Google credential ID is accepted.
         * Expected Result: Client creation succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Create Client - Max Long Google Cred ID - Success")
        void createClient_MaxLongGoogleCredId_Success() {
            // Arrange
            testClientRequest.setGoogleCredId(Long.MAX_VALUE);
            testClientRequest.setLogoBase64(null);
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            assertDoesNotThrow(() -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid name values.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidName.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Invalid Name - ThrowsBadRequestException")
        void createClient_InvalidName_ThrowsBadRequestException(String name) {
            testClientRequest.setName(name);
            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidName,
                    () -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid description values.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidDescription.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Invalid Description - ThrowsBadRequestException")
        void createClient_InvalidDescription_ThrowsBadRequestException(String description) {
            testClientRequest.setDescription(description);
            // Ensure name is valid to bypass duplicate check (mock assumption)
            when(clientRepository.existsByName(anyString())).thenReturn(false);

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidDescription,
                    () -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid support email values.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSupportEmail.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Invalid Support Email - ThrowsBadRequestException")
        void createClient_InvalidSupportEmail_ThrowsBadRequestException(String email) {
            testClientRequest.setSupportEmail(email);
            when(clientRepository.existsByName(anyString())).thenReturn(false);

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidSupportEmail,
                    () -> clientService.createClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid website values.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidWebsite.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Invalid Website - ThrowsBadRequestException")
        void createClient_InvalidWebsite_ThrowsBadRequestException(String website) {
            testClientRequest.setWebsite(website);
            when(clientRepository.existsByName(anyString())).thenReturn(false);

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidWebsite,
                    () -> clientService.createClient(testClientRequest));
        }

        private void AssertThrowsBadRequest(String expectedMessage,
                org.junit.jupiter.api.function.Executable executable) {
            BadRequestException ex = assertThrows(BadRequestException.class, executable);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("updateClient Tests")
    class UpdateClientTests {

        /**
         * Purpose: Verify update succeeds without logo changes.
         * Expected Result: Client is updated and saved.
         * Assertions: Save is called and no exception occurs.
         */
        @Test
        @DisplayName("Update Client - Success without logo change")
        void updateClient_Success_NoLogoChange() {
            // Arrange
            testClientRequest.setLogoBase64(null);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty()); // No duplicate
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class,
                    (mock, context) -> {
                        // Expect delete if cleaning up, but here we just update fields
                    })) {

                // Act
                clientService.updateClient(testClientRequest);

                // Assert
                verify(clientRepository).save(any(Client.class));
            }
        }

        /**
         * Purpose: Reject duplicate name on a different client.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches NameAlreadyExists.
         */
        @Test
        @DisplayName("Update Client - Duplicate Name (Different Client) - ThrowsBadRequestException")
        void updateClient_DuplicateName_ThrowsBadRequestException() {
            // Arrange
            Client otherClient = new Client();
            otherClient.setClientId(999L); // Different ID
            otherClient.setName(testClientRequest.getName());

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.of(otherClient));

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertTrue(ex.getMessage().contains("already exists"));
        }

        /**
         * Purpose: Allow same name when updating the same client.
         * Expected Result: Update succeeds.
         * Assertions: Save is called without error.
         */
        @Test
        @DisplayName("Update Client - Duplicate Name (Same Client) - Success")
        void updateClient_DuplicateNameSameClient_Success() {
            // Arrange
            testClientRequest.setLogoBase64(null);
            // Same ID as request
            Client sameClient = new Client();
            sameClient.setClientId(TEST_CLIENT_ID);
            sameClient.setName(testClientRequest.getName());

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.of(sameClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
                // Act
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            }
        }

        /**
         * Purpose: Reject updates for missing client IDs.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Client - Client not found - ThrowsNotFoundException")
        void updateClient_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Reject negative ID updates.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Client - Negative ID - ThrowsNotFoundException")
        void updateClient_NegativeId_ThrowsNotFoundException() {
            // Arrange
            testClientRequest.setClientId(-1L);
            when(clientRepository.findById(-1L)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Reject zero ID updates.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Client - Zero ID - ThrowsNotFoundException")
        void updateClient_ZeroId_ThrowsNotFoundException() {
            // Arrange
            testClientRequest.setClientId(0L);
            when(clientRepository.findById(0L)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Reject null update request.
         * Expected Result: NullPointerException is thrown.
         * Assertions: Exception type is NullPointerException.
         */
        @Test
        @DisplayName("Update Client - Null request - ThrowsBadRequestException")
        void updateClient_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> clientService.updateClient(null));
        }

        /**
         * Purpose: Reject whitespace-only name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidName.
         */
        @Test
        @DisplayName("Update Client - Whitespace only name - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyName_ThrowsBadRequestException() {
            testClientRequest.setName("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only description.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidDescription.
         */
        @Test
        @DisplayName("Update Client - Whitespace only description - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only support email.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSupportEmail.
         */
        @Test
        @DisplayName("Update Client - Whitespace only email - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyEmail_ThrowsBadRequestException() {
            testClientRequest.setSupportEmail("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only website.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidWebsite.
         */
        @Test
        @DisplayName("Update Client - Whitespace only website - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyWebsite_ThrowsBadRequestException() {
            testClientRequest.setWebsite("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only sendgrid sender name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSendgridSenderName.
         */
        @Test
        @DisplayName("Update Client - Whitespace only sendgrid sender name - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlySendgridSenderName_ThrowsBadRequestException() {
            testClientRequest.setSendgridSenderName("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName, ex.getMessage());
        }

        /**
         * Purpose: Allow null sendgrid sender name (optional).
         * Expected Result: Update succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Update Client - Null sendgrid sender name - Success (optional)")
        void updateClient_NullSendgridSenderName_Success() {
            // Arrange
            testClientRequest.setSendgridSenderName(null);
            testClientRequest.setLogoBase64(null);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            }
        }

        /**
         * Purpose: Verify update succeeds with all fields set.
         * Expected Result: Client is saved with updated fields.
         * Assertions: Save is called and no exception occurs.
         */
        @Test
        @DisplayName("Update Client - Valid all fields updated - Success")
        void updateClient_AllFieldsUpdated_Success() {
            // Arrange
            testClientRequest.setName("Updated Name");
            testClientRequest.setDescription("Updated Description");
            testClientRequest.setSupportEmail("newemail@test.com");
            testClientRequest.setWebsite("https://updated.com");
            testClientRequest.setSendgridSenderName("Updated Sender");
            testClientRequest.setLogoBase64(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            }
            verify(clientRepository).save(any(Client.class));
        }

        /**
         * Purpose: Reject update when Firebase credentials are missing.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER011.
         */
        @Test
        @DisplayName("Update Client - Firebase Google Cred not found - ThrowsBadRequestException")
        void updateClient_FirebaseGoogleCredNotFound_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64(null);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(anyLong())).thenReturn(Optional.empty());

                // Act & Assert
                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
                assertEquals(ErrorMessages.UserErrorMessages.ER011, ex.getMessage());
        }

        /**
         * Purpose: Validate very long name updates succeed.
         * Expected Result: Update succeeds.
         * Assertions: Save is called without errors.
         */
        @Test
        @DisplayName("Update Client - Very long updated name - Success")
        void updateClient_VeryLongName_Success() {
            try (MockedConstruction<FirebaseHelper> ignored = mockConstruction(FirebaseHelper.class)) {
                // Arrange
                String longName = "B".repeat(500);
                testClientRequest.setName(longName);
                testClientRequest.setLogoBase64(null);
                when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                when(clientRepository.findByName(longName)).thenReturn(Optional.empty());
                when(clientRepository.save(any(Client.class))).thenReturn(testClient);

                // Act & Assert
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            }
        }

        /**
         * Purpose: Verify multiple field changes are persisted.
         * Expected Result: Update succeeds with all changes.
         * Assertions: Save is called and no exception occurs.
         */
        @Test
        @DisplayName("Update Client - Multiple field changes - Success")
        void updateClient_MultipleFieldChanges_Success() {
            try (MockedConstruction<FirebaseHelper> ignored = mockConstruction(FirebaseHelper.class)) {
                // Arrange
                testClientRequest.setName("New Name");
                testClientRequest.setDescription("New Description");
                testClientRequest.setSupportEmail("newemail@domain.com");
                testClientRequest.setWebsite("https://newdomain.com");
                testClientRequest.setLogoBase64(null);

                when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
                when(clientRepository.save(any(Client.class))).thenReturn(testClient);

                // Act & Assert
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
                verify(clientRepository, times(1)).save(any(Client.class));
            }
        }

        /**
         * Purpose: Verify logo update via ImgBB.
         * Expected Result: Client is saved with new logo URL.
         * Assertions: ImgBB helper is invoked and save occurs.
         */
        @Test
        @DisplayName("Update Client - Update Logo ImgBB - Success")
        void updateClient_UpdateLogoImgBB_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64("newLogoData");

            // Existing client has API key
            testClient.setImgbbApiKey("valid-key");
            testClient.setLogoDeleteHash("oldHash");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient); // returns updated client

            ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                    "http://imgbb.com/new.png",
                    "newHash");

            try (MockedConstruction<ImgbbHelper> imgMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                        // Expect delete of old logo
                    })) {

                // Act
                clientService.updateClient(testClientRequest);

                // Assert
                verify(clientRepository, times(2)).save(any(Client.class));
                // Verify mock interaction if needed (hard with MockedConstruction unless
                // capturing instance)
                // But successful execution implies proper flow
                assertEquals(1, imgMock.constructed().size());
                verify(imgMock.constructed().get(0)).deleteImage("oldHash");
                verify(imgMock.constructed().get(0)).uploadFileToImgbb(eq("newLogoData"), anyString());
            }
        }

        /**
         * Purpose: Reject ImgBB upload when response is null.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidLogoUpload.
         */
        @Test
        @DisplayName("Update Client - ImgBB upload null - ThrowsBadRequestException")
        void updateClient_ImgbbUploadNull_ThrowsBadRequestException() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64("newLogoData");
            testClient.setImgbbApiKey("valid-key");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            try (MockedConstruction<ImgbbHelper> imgMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null))) {
                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
                assertEquals(ErrorMessages.ClientErrorMessages.InvalidLogoUpload, ex.getMessage());
            }
        }

        /**
         * Purpose: Verify logo removal via ImgBB path.
         * Expected Result: Client is saved without logo URL.
         * Assertions: Save is called with cleared logo URL.
         */
        @Test
        @DisplayName("Update Client - Remove Logo ImgBB - Success")
        void updateClient_RemoveLogoImgBB_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64(null); // No new logo
            testClient.setImgbbApiKey("valid-key");
            testClient.setLogoDeleteHash("oldHash");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            try (MockedConstruction<ImgbbHelper> imgMock = mockConstruction(ImgbbHelper.class)) {

                // Act
                clientService.updateClient(testClientRequest);

                // Assert
                verify(imgMock.constructed().get(0)).deleteImage("oldHash");
            }
        }

        /**
         * Purpose: Reject ImgBB update when API key is missing.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ImgbbApiKeyNotConfigured.
         */
        @Test
        @DisplayName("Update Client - ImgBB missing API Key - ThrowsBadRequestException")
        void updateClient_ImgbbMissingApiKey_ThrowsBadRequestException() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClientRequest.setLogoBase64(TEST_LOGO_BASE64);
            testClient.setImgbbApiKey(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ConfigurationErrorMessages.ImgbbApiKeyNotConfigured, ex.getMessage());
        }

        /**
         * Purpose: Reject invalid name updates.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidName.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid Name - ThrowsBadRequestException")
        void updateClient_InvalidName_ThrowsBadRequestException(String name) {
            testClientRequest.setName(name);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidName,
                    () -> clientService.updateClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid description updates.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidDescription.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid Description - ThrowsBadRequestException")
        void updateClient_InvalidDescription_ThrowsBadRequestException(String description) {
            testClientRequest.setDescription(description);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidDescription,
                    () -> clientService.updateClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid support email updates.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSupportEmail.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid Support Email - ThrowsBadRequestException")
        void updateClient_InvalidSupportEmail_ThrowsBadRequestException(String email) {
            testClientRequest.setSupportEmail(email);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidSupportEmail,
                    () -> clientService.updateClient(testClientRequest));
        }

        /**
         * Purpose: Reject invalid website updates.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidWebsite.
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid Website - ThrowsBadRequestException")
        void updateClient_InvalidWebsite_ThrowsBadRequestException(String website) {
            testClientRequest.setWebsite(website);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidWebsite,
                    () -> clientService.updateClient(testClientRequest));
        }

        /**
         * Purpose: Reject empty sendgrid sender names.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidSendgridSenderName.
         */
        @ParameterizedTest
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid SendgridSenderName (Empty) - ThrowsBadRequestException")
        void updateClient_InvalidSendgridSenderName_ThrowsBadRequestException(String senderName) {
            testClientRequest.setSendgridSenderName(senderName);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName,
                    () -> clientService.updateClient(testClientRequest));
        }

        /**
         * Purpose: Preserve sendgrid sender name when request value is null.
         * Expected Result: Update succeeds and existing sender name is retained.
         * Assertions: Saved client keeps original sendgrid sender name.
         */
        @Test
        @DisplayName("Update Client - Preserve sendgrid sender name when null - Success")
        void updateClient_PreserveSendgridSenderName_WhenNull_Success() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClient.setSendgridSenderName("Existing Sender");
            testClient.setImgbbApiKey("imgbb-key");
            testClient.setLogoDeleteHash(null);

            testClientRequest.setSendgridSenderName(null);
            testClientRequest.setLogoBase64(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository, atLeastOnce()).save(captor.capture());
            assertEquals("Existing Sender", captor.getValue().getSendgridSenderName());
        }

        /**
         * Purpose: Preserve googleCredId when request value is null.
         * Expected Result: Update succeeds and existing googleCredId is retained.
         * Assertions: Saved client keeps original googleCredId.
         */
        @Test
        @DisplayName("Update Client - Preserve googleCredId when null - Success")
        void updateClient_PreserveGoogleCredId_WhenNull_Success() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClient.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);
            testClientRequest.setGoogleCredId(null);
            testClientRequest.setLogoBase64(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(googleCredRepository.findById(DEFAULT_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
                assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            }

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertEquals(DEFAULT_GOOGLE_CRED_ID, captor.getValue().getGoogleCredId());
        }

        /**
         * Purpose: Reject Firebase logo upload failures.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidLogoUpload.
         */
        @Test
        @DisplayName("Update Client - Firebase upload fail - ThrowsBadRequestException")
        void updateClient_FirebaseUploadFail_ThrowsBadRequestException() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64("newLogoData");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(DEFAULT_GOOGLE_CRED_ID)).thenReturn(Optional.of(testGoogleCred));

            try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class,
                    (mock, context) -> when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(false))) {
                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
                assertEquals(ErrorMessages.ClientErrorMessages.InvalidLogoUpload, ex.getMessage());
            }
        }

        /**
         * Purpose: Default isDeleted when null on update.
         * Expected Result: isDeleted defaults to false.
         * Assertions: Saved client has isDeleted false.
         */
        @Test
        @DisplayName("Update Client - Null isDeleted - Defaults to false")
        void updateClient_NullIsDeleted_DefaultsFalse() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
            testClient.setIsDeleted(true);
            testClient.setImgbbApiKey("imgbb-key");
            testClient.setLogoDeleteHash(null);
            testClientRequest.setIsDeleted(null);
            testClientRequest.setLogoBase64(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository, atLeastOnce()).save(captor.capture());
            assertTrue(captor.getAllValues().stream()
                .anyMatch(client -> Boolean.FALSE.equals(client.getIsDeleted())));
        }

        /**
         * Purpose: Reject invalid imageLocation configuration on update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message contains invalid configuration text.
         */
        @Test
        @DisplayName("Update Client - Invalid imageLocation config - ThrowsBadRequestException")
        void updateClient_InvalidImageLocation_ThrowsBadRequestException() {
            ReflectionTestUtils.setField(clientService, "imageLocation", "invalid");
            testClientRequest.setLogoBase64(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
            assertTrue(ex.getMessage().contains("Invalid imageLocation configuration"));
        }

        private void AssertThrowsBadRequest(String expectedMessage,
                org.junit.jupiter.api.function.Executable executable) {
            BadRequestException ex = assertThrows(BadRequestException.class, executable);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getClientsByUser Tests")
    class GetClientsByUserTests {

        /**
         * Purpose: Verify clients are returned for a valid user.
         * Expected Result: List of clients is returned.
         * Assertions: Response list size and fields are validated.
         */
        @Test
        @DisplayName("Get Clients By User - Returns list of clients")
        void getClientsByUser_Success() {
            // Arrange
            // BaseService.getUserId() relies on the mocked 'Authorization' header in the
            // request

            when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(TEST_CLIENT_ID, results.get(0).getClientId());
        }

        /**
         * Purpose: Handle empty client list for a user.
         * Expected Result: Empty list is returned.
         * Assertions: Result size is zero.
         */
        @Test
        @DisplayName("Get Clients By User - No clients - Returns empty list")
        void getClientsByUser_Empty() {
            // Arrange
            when(clientRepository.findByUserId(anyLong())).thenReturn(Collections.emptyList());

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        /**
         * Purpose: Verify multiple clients are returned.
         * Expected Result: All clients are included in response.
         * Assertions: Result size matches expected count.
         */
        @Test
        @DisplayName("Get Clients By User - Multiple clients - Returns all")
        void getClientsByUser_MultipleClients_Success() {
            // Arrange
            Client client1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client1.setClientId(1L);
            client1.setName("Client 1");

            Client client2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client2.setClientId(2L);
            client2.setName("Client 2");

            Client client3 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client3.setClientId(3L);
            client3.setName("Client 3");

            when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(client1, client2, client3));

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertEquals(3, results.size());
            assertEquals(1L, results.get(0).getClientId());
            assertEquals(2L, results.get(1).getClientId());
            assertEquals(3L, results.get(2).getClientId());
            verify(clientRepository).findByUserId(anyLong());
        }

        /**
         * Purpose: Validate large result sets are handled.
         * Expected Result: Large list is returned.
         * Assertions: Result size matches large dataset.
         */
        @Test
        @DisplayName("Get Clients By User - Large result set - Success")
        void getClientsByUser_LargeResultSet_Success() {
            // Arrange
            java.util.List<Client> clients = new java.util.ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                Client client = new Client(testClientRequest, DEFAULT_CREATED_USER);
                client.setClientId((long) i);
                client.setName("Client " + i);
                clients.add(client);
            }

            when(clientRepository.findByUserId(anyLong())).thenReturn(clients);

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertEquals(100, results.size());
            assertEquals(1L, results.get(0).getClientId());
            assertEquals(100L, results.get(99).getClientId());
        }

        /**
         * Purpose: Verify deleted clients are still returned.
         * Expected Result: Deleted clients appear in response.
         * Assertions: All returned clients are marked deleted.
         */
        @Test
        @DisplayName("Get Clients By User - All clients deleted - Returns deleted clients")
        void getClientsByUser_AllClientsDeleted_Success() {
            // Arrange
            Client deletedClient1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            deletedClient1.setClientId(1L);
            deletedClient1.setIsDeleted(true);

            Client deletedClient2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            deletedClient2.setClientId(2L);
            deletedClient2.setIsDeleted(true);

            when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(deletedClient1, deletedClient2));

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.get(0).getIsDeleted());
            assertTrue(results.get(1).getIsDeleted());
        }

        /**
         * Purpose: Verify mixed active and deleted clients are returned.
         * Expected Result: Both active and deleted clients are included.
         * Assertions: Result size matches all clients.
         */
        @Test
        @DisplayName("Get Clients By User - Mixed deleted and active clients - Returns all")
        void getClientsByUser_MixedDeletedAndActive_Success() {
            // Arrange
            Client activeClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
            activeClient.setClientId(1L);
            activeClient.setIsDeleted(false);

            Client deletedClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
            deletedClient.setClientId(2L);
            deletedClient.setIsDeleted(true);

            when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(activeClient, deletedClient));

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());
            assertFalse(results.get(0).getIsDeleted());
            assertTrue(results.get(1).getIsDeleted());
        }

        /**
         * Purpose: Validate all fields are mapped in responses.
         * Expected Result: Response fields match client entities.
         * Assertions: Field-by-field assertions succeed.
         */
        @Test
        @DisplayName("Get Clients By User - Verify all fields in response - Success")
        void getClientsByUser_AllFieldsInResponse_Success() {
            // Arrange
            Client fullClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
            fullClient.setClientId(1L);
            fullClient.setName("Full Client");
            fullClient.setDescription("Complete Description");
            fullClient.setSupportEmail("support@full.com");
            fullClient.setWebsite("https://full.com");
            fullClient.setLogoUrl("https://logo.url");
            fullClient.setIsDeleted(false);

            when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(fullClient));

            // Act
            List<ClientResponseModel> results = clientService.getClientsByUser();

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            ClientResponseModel result = results.get(0);
            assertEquals(1L, result.getClientId());
            assertEquals("Full Client", result.getName());
            assertEquals("Complete Description", result.getDescription());
            assertEquals("support@full.com", result.getSupportEmail());
            assertEquals("https://full.com", result.getWebsite());
            assertEquals("https://logo.url", result.getLogoUrl());
            assertFalse(result.getIsDeleted());
        }

    }
}
