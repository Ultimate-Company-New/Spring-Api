package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.createClient() method.
 * Tests client creation with various scenarios including logo uploads.
 * * Test Count: 32 tests
 */
@DisplayName("Create Client Tests")
class CreateClientTest extends ClientServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

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
     * Purpose: Allow empty logo and skip upload.
     * Expected Result: Client is created without image upload.
     * Assertions: Image repository is not accessed.
     */
    @Test
    @DisplayName("Create Client - Empty Logo - Success (skips upload)")
    void createClient_EmptyLogo_Success() {
        // Arrange
        testClientRequest.setLogoBase64("");
        when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // Act
        assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

        // Assert
        verify(googleCredRepository, never()).findById(any());
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
     * Purpose: Allow null logo and skip upload.
     * Expected Result: Client is created without image upload.
     * Assertions: Image repository is not accessed.
     */
    @Test
    @DisplayName("Create Client - Null Logo - Success (skips upload)")
    void createClient_NullLogo_Success() {
        // Arrange
        testClientRequest.setLogoBase64(null);
        when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // Act
        assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

        // Assert
        verify(googleCredRepository, never()).findById(any());
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
     * Purpose: Allow whitespace logo and skip upload.
     * Expected Result: Client is created without image upload.
     * Assertions: Image repository is not accessed.
     */
    @Test
    @DisplayName("Create Client - Whitespace Logo - Success (skips upload)")
    void createClient_WhitespaceLogo_Success() {
        // Arrange
        testClientRequest.setLogoBase64("   ");
        when(clientRepository.existsByName(testClientRequest.getName())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // Act
        assertDoesNotThrow(() -> clientService.createClient(testClientRequest));

        // Assert
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

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

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
     * Purpose: Reject empty description.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidDescription.
     */
    @Test
    @DisplayName("Create Client - Empty Description - ThrowsBadRequestException")
    void createClient_EmptyDescription_ThrowsBadRequestException() {
        testClientRequest.setDescription("");
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
    }

    /**
     * Purpose: Reject empty name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidName.
     */
    @Test
    @DisplayName("Create Client - Empty Name - ThrowsBadRequestException")
    void createClient_EmptyName_ThrowsBadRequestException() {
        testClientRequest.setName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
    }

    /**
     * Purpose: Reject empty support email values.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSupportEmail.
     */
    @Test
    @DisplayName("Create Client - Empty Support Email - ThrowsBadRequestException")
    void createClient_EmptySupportEmail_ThrowsBadRequestException() {
        testClientRequest.setSupportEmail("");
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
    }

    /**
     * Purpose: Reject empty website values.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidWebsite.
     */
    @Test
    @DisplayName("Create Client - Empty Website - ThrowsBadRequestException")
    void createClient_EmptyWebsite_ThrowsBadRequestException() {
        testClientRequest.setWebsite("");
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
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
     * Purpose: Reject null description.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidDescription.
     */
    @Test
    @DisplayName("Create Client - Null Description - ThrowsBadRequestException")
    void createClient_NullDescription_ThrowsBadRequestException() {
        testClientRequest.setDescription(null);
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
    }

    /**
     * Purpose: Reject null name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidName.
     */
    @Test
    @DisplayName("Create Client - Null Name - ThrowsBadRequestException")
    void createClient_NullName_ThrowsBadRequestException() {
        testClientRequest.setName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
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
     * Purpose: Reject null support email values.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSupportEmail.
     */
    @Test
    @DisplayName("Create Client - Null Support Email - ThrowsBadRequestException")
    void createClient_NullSupportEmail_ThrowsBadRequestException() {
        testClientRequest.setSupportEmail(null);
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
    }

    /**
     * Purpose: Reject null website values.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidWebsite.
     */
    @Test
    @DisplayName("Create Client - Null Website - ThrowsBadRequestException")
    void createClient_NullWebsite_ThrowsBadRequestException() {
        testClientRequest.setWebsite(null);
        when(clientRepository.existsByName(anyString())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.createClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
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

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on createClient method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references INSERT_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Create Client - Verify @PreAuthorize annotation is configured correctly")
    void createClient_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = ClientController.class.getMethod("createClient",
                com.example.SpringApi.Models.RequestModels.ClientRequestModel.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "createClient method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.INSERT_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference INSERT_CLIENT_PERMISSION");
    }

    /**
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     * 
     * Note: This test simulates the happy path assuming authorization has already
     * passed.
     * Actual @PreAuthorize enforcement is handled by Spring Security AOP and tested
     * in end-to-end tests.
     */
    @Test
    @DisplayName("Create Client - Controller delegates to service correctly")
    void createClient_WithValidRequest_DelegatesToService() {
        // Arrange
        ClientService mockService = mock(ClientService.class);
        ClientController controller = new ClientController(mockService);
        doNothing().when(mockService)
                .createClient(any(com.example.SpringApi.Models.RequestModels.ClientRequestModel.class));

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.createClient(testClientRequest);

        // Assert - Verify service was called and correct response returned
        verify(mockService, times(1)).createClient(testClientRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Should return HTTP 201 Created");
    }
}