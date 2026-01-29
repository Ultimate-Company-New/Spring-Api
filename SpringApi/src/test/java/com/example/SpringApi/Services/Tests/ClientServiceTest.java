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
 * Comprehensive unit tests for ClientService.
 *
 * This test class provides exhaustive coverage of ClientService methods
 * including:
 * - CRUD operations (create, read, update, toggle)
 * - Validation error handling
 * - Integration with Firebase and ImgBB helpers
 * - Edge cases and boundary conditions
 *
 * Test naming convention: methodName_Scenario_ExpectedOutcome
 * Example: toggleClient_ClientNotFound_ThrowsNotFoundException
 *
 * @author SpringApi Team
 * @version 2.0
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

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer token");
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
    }

    // ==================== TOGGLE CLIENT TESTS ====================

    @Nested
    @DisplayName("toggleClient Tests")
    class ToggleClientTests {

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

    // ==================== GET CLIENT BY ID TESTS ====================

    @Nested
    @DisplayName("getClientById Tests")
    class GetClientByIdTests {

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

    // ==================== CREATE CLIENT TESTS ====================

    @Nested
    @DisplayName("createClient Tests")
    class CreateClientTests {

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

        @Test
        @DisplayName("Create Client - Null request - ThrowsBadRequestException")
        void createClient_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidRequest,
                    () -> clientService.createClient(null));
        }

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

        @Test
        @DisplayName("Create Client - Whitespace only name - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyName_ThrowsBadRequestException() {
            testClientRequest.setName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
        }

        @Test
        @DisplayName("Create Client - Whitespace only description - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
        }

        @Test
        @DisplayName("Create Client - Whitespace only email - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyEmail_ThrowsBadRequestException() {
            testClientRequest.setSupportEmail("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
        }

        @Test
        @DisplayName("Create Client - Whitespace only website - ThrowsBadRequestException")
        void createClient_WhitespaceOnlyWebsite_ThrowsBadRequestException() {
            testClientRequest.setWebsite("   ");
            when(clientRepository.existsByName(anyString())).thenReturn(false);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
        }

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

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Client - Invalid Name - ThrowsBadRequestException")
        void createClient_InvalidName_ThrowsBadRequestException(String name) {
            testClientRequest.setName(name);
            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidName,
                    () -> clientService.createClient(testClientRequest));
        }

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

    // ==================== UPDATE CLIENT TESTS ====================

    @Nested
    @DisplayName("updateClient Tests")
    class UpdateClientTests {

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

        @Test
        @DisplayName("Update Client - Null request - ThrowsBadRequestException")
        void updateClient_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(null));
            assertTrue(ex.getMessage() != null);
        }

        @Test
        @DisplayName("Update Client - Whitespace only name - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyName_ThrowsBadRequestException() {
            testClientRequest.setName("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
        }

        @Test
        @DisplayName("Update Client - Whitespace only description - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
        }

        @Test
        @DisplayName("Update Client - Whitespace only email - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyEmail_ThrowsBadRequestException() {
            testClientRequest.setSupportEmail("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
        }

        @Test
        @DisplayName("Update Client - Whitespace only website - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlyWebsite_ThrowsBadRequestException() {
            testClientRequest.setWebsite("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
        }

        @Test
        @DisplayName("Update Client - Whitespace only sendgrid sender name - ThrowsBadRequestException")
        void updateClient_WhitespaceOnlySendgridSenderName_ThrowsBadRequestException() {
            testClientRequest.setSendgridSenderName("   ");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName, ex.getMessage());
        }

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
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

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
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Firebase Google Cred not found - Graceful handling")
        void updateClient_FirebaseGoogleCredNotFound_Success() {
            // Arrange
            ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
            testClientRequest.setLogoBase64(null);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(Optional.empty());
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);
            when(googleCredRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert - should still succeed even if Google Cred not found
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

        @Test
        @DisplayName("Update Client - Very long updated name - Success")
        void updateClient_VeryLongName_Success() {
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

        @Test
        @DisplayName("Update Client - Multiple field changes - Success")
        void updateClient_MultipleFieldChanges_Success() {
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

        @ParameterizedTest
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Client - Invalid SendgridSenderName (Empty) - ThrowsBadRequestException")
        void updateClient_InvalidSendgridSenderName_ThrowsBadRequestException(String senderName) {
            testClientRequest.setSendgridSenderName(senderName);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            AssertThrowsBadRequest(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName,
                    () -> clientService.updateClient(testClientRequest));
        }

        private void AssertThrowsBadRequest(String expectedMessage,
                org.junit.jupiter.api.function.Executable executable) {
            BadRequestException ex = assertThrows(BadRequestException.class, executable);
            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    // ==================== GET CLIENTS BY USER TESTS ====================

    @Nested
    @DisplayName("getClientsByUser Tests")
    class GetClientsByUserTests {

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

        // ==================== Comprehensive Validation Tests - Added ====================

        @Test
        @DisplayName("Get Client By ID - Negative ID - Throws NotFoundException")
        void getClientById_NegativeId_ThrowsNotFoundException() {
            when(clientRepository.findByClientIdAndIsDeletedFalse(-1L)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(-1L));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Get Client By ID - Zero ID - Throws NotFoundException")
        void getClientById_ZeroId_ThrowsNotFoundException() {
            when(clientRepository.findByClientIdAndIsDeletedFalse(0L)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(0L));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Get Client By ID - Long.MAX_VALUE ID - Throws NotFoundException")
        void getClientById_MaxLongId_ThrowsNotFoundException() {
            when(clientRepository.findByClientIdAndIsDeletedFalse(Long.MAX_VALUE)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.getClientById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Create Client - Null Request - Throws BadRequestException")
        void createClient_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(null));
            assertTrue(ex.getMessage().contains("request") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Null Client Name - Throws BadRequestException")
        void createClient_NullClientName_ThrowsBadRequestException() {
            testClientRequest.setName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Empty Client Name - Throws BadRequestException")
        void createClient_EmptyClientName_ThrowsBadRequestException() {
            testClientRequest.setName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("empty"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Whitespace Client Name - Throws BadRequestException")
        void createClient_WhitespaceClientName_ThrowsBadRequestException() {
            testClientRequest.setName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Duplicate Client Name - Throws BadRequestException")
        void createClient_DuplicateClientName_ThrowsBadRequestException() {
            when(clientRepository.findByName(testClientRequest.getName())).thenReturn(testClient);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("exist") || ex.getMessage().contains("duplicate"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Null Description - Throws BadRequestException")
        void createClient_NullDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("description") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Empty Description - Throws BadRequestException")
        void createClient_EmptyDescription_ThrowsBadRequestException() {
            testClientRequest.setDescription("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("description") || ex.getMessage().contains("empty"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Invalid Email Format - Throws BadRequestException")
        void createClient_InvalidEmailFormat_ThrowsBadRequestException() {
            testClientRequest.setSupportEmail("invalid-email");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("email") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Create Client - Invalid Website URL Format - Throws BadRequestException")
        void createClient_InvalidWebsiteURLFormat_ThrowsBadRequestException() {
            testClientRequest.setWebsite("not-a-valid-url");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.createClient(testClientRequest));
            assertTrue(ex.getMessage().contains("website") || ex.getMessage().contains("url"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Negative ID - Throws NotFoundException")
        void updateClient_NegativeId_ThrowsNotFoundException() {
            testClientRequest.setClientId(-1L);
            when(clientRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Zero ID - Throws NotFoundException")
        void updateClient_ZeroId_ThrowsNotFoundException() {
            testClientRequest.setClientId(0L);
            when(clientRepository.findById(0L)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Long.MAX_VALUE ID - Throws NotFoundException")
        void updateClient_MaxLongId_ThrowsNotFoundException() {
            testClientRequest.setClientId(Long.MAX_VALUE);
            when(clientRepository.findById(Long.MAX_VALUE)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Null Name - Throws BadRequestException")
        void updateClient_NullName_ThrowsBadRequestException() {
            testClientRequest.setName(null);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(java.util.Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Update Client - Empty Name - Throws BadRequestException")
        void updateClient_EmptyName_ThrowsBadRequestException() {
            testClientRequest.setName("");
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(java.util.Optional.of(testClient));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));
            assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("empty"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Toggle Client - Negative ID - Throws NotFoundException")
        void toggleClient_NegativeId_ThrowsNotFoundException() {
            when(clientRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(-1L));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Toggle Client - Zero ID - Throws NotFoundException")
        void toggleClient_ZeroId_ThrowsNotFoundException() {
            when(clientRepository.findById(0L)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(0L));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Toggle Client - Long.MAX_VALUE ID - Throws NotFoundException")
        void toggleClient_MaxLongId_ThrowsNotFoundException() {
            when(clientRepository.findById(Long.MAX_VALUE)).thenReturn(java.util.Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> clientService.toggleClient(Long.MAX_VALUE));
            assertEquals(ErrorMessages.ClientErrorMessages.NotFound, ex.getMessage());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Toggle Client - Multiple Toggles - State Transitions")
        void toggleClient_MultipleToggles_StateTransitions() {
            testClient.setIsDeleted(false);
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(java.util.Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenReturn(testClient);

            // First toggle: false -> true
            clientService.toggleClient(TEST_CLIENT_ID);
            assertTrue(testClient.getIsDeleted());

            // Second toggle: true -> false
            testClient.setIsDeleted(true);
            clientService.toggleClient(TEST_CLIENT_ID);
            assertFalse(testClient.getIsDeleted());

            verify(clientRepository, times(2)).save(any(Client.class));
        }

        @Test
        @DisplayName("Get Clients By User - Null User ID - Throws BadRequestException")
        void getClientsByUser_NullUserId_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.getClientsByUser());
            assertNotNull(ex.getMessage());
            verify(clientRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Get Clients By User - No Results - Returns Empty List")
        void getClientsByUser_NoResults_ReturnsEmptyList() {
            when(clientRepository.findByUserId(anyLong())).thenReturn(new java.util.ArrayList<>());
            
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> results = clientService.getClientsByUser();
            
            assertNotNull(results);
            assertEquals(0, results.size());
            verify(clientRepository, times(1)).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Get Clients By User - Multiple Clients - Returns All")
        void getClientsByUser_MultipleClients_ReturnsAll() {
            Client client1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client1.setClientId(1L);
            client1.setName("Client 1");
            
            Client client2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client2.setClientId(2L);
            client2.setName("Client 2");
            
            when(clientRepository.findByUserId(anyLong())).thenReturn(java.util.Arrays.asList(client1, client2));
            
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> results = clientService.getClientsByUser();
            
            assertNotNull(results);
            assertEquals(2, results.size());
            verify(clientRepository, times(1)).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Bulk Create Clients - Empty List - Throws BadRequestException")
        void bulkCreateClients_EmptyList_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.bulkCreateClients(new java.util.ArrayList<>()));
            assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Bulk Create Clients - Null List - Throws BadRequestException")
        void bulkCreateClients_NullList_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.bulkCreateClients(null));
            assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Search Clients - Null Search Term - Throws BadRequestException")
        void searchClients_NullSearchTerm_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.searchClients(null));
            assertTrue(ex.getMessage().contains("search") || ex.getMessage().contains("invalid"));
            verify(clientRepository, never()).findAll();
        }

        @Test
        @DisplayName("Search Clients - Empty Search Term - Throws BadRequestException")
        void searchClients_EmptySearchTerm_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.searchClients(""));
            assertTrue(ex.getMessage().contains("search") || ex.getMessage().contains("empty"));
            verify(clientRepository, never()).findAll();
        }
    }
}
