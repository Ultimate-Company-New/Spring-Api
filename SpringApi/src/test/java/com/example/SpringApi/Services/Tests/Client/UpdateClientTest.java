package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
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
 * Unit tests for ClientService.updateClient() method.
 * Tests client updates with various scenarios including logo changes and field
 * validations.
 * * Test Count: 37 tests
 */
@DisplayName("Update Client Tests")
class UpdateClientTest extends ClientServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

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

        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {

            // Act
            clientService.updateClient(testClientRequest);

            // Assert
            verify(clientRepository).save(any(Client.class));
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
                })) {

            // Act
            clientService.updateClient(testClientRequest);

            // Assert
            verify(clientRepository, times(2)).save(any(Client.class));
            assertEquals(1, imgMock.constructed().size());
            verify(imgMock.constructed().get(0)).deleteImage("oldHash");
            verify(imgMock.constructed().get(0)).uploadFileToImgbb(eq("newLogoData"), anyString());
        }
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

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

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
     * Purpose: Reject update when Firebase credentials are missing.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER011.
     */
    @Test
    @DisplayName("Update Client - Firebase Google Cred not found - ThrowsBadRequestException")
    void updateClient_FirebaseGoogleCredNotFound_ThrowsBadRequestException() {
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
     * Purpose: Reject invalid description (Empty).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Empty Description - ThrowsBadRequestException")
    void updateClient_EmptyDescription_ThrowsBadRequestException() {
        testClientRequest.setDescription("");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid name (Empty).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Empty Name - ThrowsBadRequestException")
    void updateClient_EmptyName_ThrowsBadRequestException() {
        testClientRequest.setName("");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid SendgridSenderName (Empty).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Empty SendgridSenderName - ThrowsBadRequestException")
    void updateClient_EmptySendgridSenderName_ThrowsBadRequestException() {
        testClientRequest.setSendgridSenderName("");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidSendgridSenderName, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid support email (Empty).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Empty Support Email - ThrowsBadRequestException")
    void updateClient_EmptySupportEmail_ThrowsBadRequestException() {
        testClientRequest.setSupportEmail("");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid website (Empty).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Empty Website - ThrowsBadRequestException")
    void updateClient_EmptyWebsite_ThrowsBadRequestException() {
        testClientRequest.setWebsite("");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
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
     * Purpose: Reject invalid description (Null).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Null Description - ThrowsBadRequestException")
    void updateClient_NullDescription_ThrowsBadRequestException() {
        testClientRequest.setDescription(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidDescription, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid name (Null).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Null Name - ThrowsBadRequestException")
    void updateClient_NullName_ThrowsBadRequestException() {
        testClientRequest.setName(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidName, ex.getMessage());
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
     * Purpose: Reject invalid support email (Null).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Null Support Email - ThrowsBadRequestException")
    void updateClient_NullSupportEmail_ThrowsBadRequestException() {
        testClientRequest.setSupportEmail(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidSupportEmail, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid website (Null).
     * Expected Result: BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update Client - Null Website - ThrowsBadRequestException")
    void updateClient_NullWebsite_ThrowsBadRequestException() {
        testClientRequest.setWebsite(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidWebsite, ex.getMessage());
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
     * Purpose: Verify @PreAuthorize annotation is declared on updateClient method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references UPDATE_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Update Client - Verify @PreAuthorize annotation is configured correctly")
    void updateClient_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = ClientController.class.getMethod("updateClient",
                Long.class, com.example.SpringApi.Models.RequestModels.ClientRequestModel.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "updateClient method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.UPDATE_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference UPDATE_CLIENT_PERMISSION");
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
    @DisplayName("Update Client - Controller delegates to service correctly")
    void updateClient_WithValidRequest_DelegatesToService() {
        // Arrange
        ClientService mockService = mock(ClientService.class);
        ClientController controller = new ClientController(mockService);
        testClientRequest.setClientId(TEST_CLIENT_ID);
        doNothing().when(mockService)
                .updateClient(any(com.example.SpringApi.Models.RequestModels.ClientRequestModel.class));

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.updateClient(TEST_CLIENT_ID, testClientRequest);

        // Assert - Verify service was called and correct response returned
        verify(mockService, times(1)).updateClient(testClientRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}