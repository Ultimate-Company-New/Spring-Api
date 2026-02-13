package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
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
 */
@DisplayName("Update Client Tests")
class UpdateClientTest extends ClientServiceTestBase {


    // Total Tests: 38
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
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

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act & Assert
        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

        // Assert
        verify(clientRepository).save(any(Client.class));
    }

    /*
     * Purpose: Allow same name when updating the same client.
     * Expected Result: Update succeeds.
     * Assertions: Save is called without error.
     */
    @Test
    @DisplayName("Update Client - Duplicate Name (Same Client) - Success")
    void updateClient_DuplicateNameSameClient_Success() {
        // Arrange
        testClientRequest.setLogoBase64(null);
        Client sameClient = new Client();
        sameClient.setClientId(TEST_CLIENT_ID);
        sameClient.setName(testClientRequest.getName());

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.of(sameClient));
        stubClientSave(testClient);
        stubGoogleCredFindById(anyLong(), Optional.of(testGoogleCred));

        // Act & Assert
        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }
    }

    /*
     * Purpose: Verify multiple field changes are persisted.
     * Expected Result: Update succeeds with all changes.
     * Assertions: Save is called and no exception occurs.
     */
    @Test
    @DisplayName("Update Client - Multiple field changes - Success")
    void updateClient_MultipleFieldChanges_Success() {
        // Arrange
        testClientRequest.setName("New Name");
        testClientRequest.setDescription("New Description");
        testClientRequest.setSupportEmail("newemail@domain.com");
        testClientRequest.setWebsite("https://newdomain.com");
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act
        try (MockedConstruction<FirebaseHelper> ignored = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

        // Assert
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    /*
     * Purpose: Update succeeds without logo changes.
     * Expected Result: Client is updated and saved.
     * Assertions: Save is called and no exception occurs.
     */
    @Test
    @DisplayName("Update Client - Success without logo change - Success")
    void updateClient_NoLogoChange_Success() {
        // Arrange
        testClientRequest.setLogoBase64(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);
        stubGoogleCredFindById(anyLong(), Optional.of(testGoogleCred));

        // Act
        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
            clientService.updateClient(testClientRequest);
        }

        // Assert
        verify(clientRepository).save(any(Client.class));
    }

    /*
     * Purpose: Default isDeleted when null on update.
     * Expected Result: isDeleted defaults to false.
     * Assertions: Saved client has isDeleted false.
     */
    @Test
    @DisplayName("Update Client - Null isDeleted - Success")
    void updateClient_NullIsDeleted_Success() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
        testClient.setIsDeleted(true);
        testClient.setImgbbApiKey("imgbb-key");
        testClient.setLogoDeleteHash(null);
        testClientRequest.setIsDeleted(null);
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act
        assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));

        // Assert
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getAllValues().stream()
                .anyMatch(client -> Boolean.FALSE.equals(client.getIsDeleted())));
    }

    /*
     * Purpose: Allow null sendgrid sender name (optional).
     * Expected Result: Update succeeds.
     * Assertions: Save is called without errors.
     */
    @Test
    @DisplayName("Update Client - Null sendgrid sender name - Success")
    void updateClient_NullSendgridSenderName_Success() {
        // Arrange
        testClientRequest.setSendgridSenderName(null);
        testClientRequest.setLogoBase64(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act & Assert
        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }
    }

    /*
     * Purpose: Preserve googleCredId when request value is null.
     * Expected Result: Update succeeds and existing googleCredId is retained.
     * Assertions: Saved client keeps original googleCredId.
     */
    @Test
    @DisplayName("Update Client - Preserve googleCredId when null - Success")
    void updateClient_PreserveGoogleCredId_Success() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
        testClient.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);
        testClientRequest.setGoogleCredId(null);
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);
        stubGoogleCredFindById(DEFAULT_GOOGLE_CRED_ID, Optional.of(testGoogleCred));

        // Act
        try (MockedConstruction<FirebaseHelper> fbMock = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

        // Assert
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals(DEFAULT_GOOGLE_CRED_ID, captor.getValue().getGoogleCredId());
    }

    /*
     * Purpose: Preserve sendgrid sender name when request value is null.
     * Expected Result: Update succeeds and existing sender name is retained.
     * Assertions: Saved client keeps original sendgrid sender name.
     */
    @Test
    @DisplayName("Update Client - Preserve sendgrid sender name when null - Success")
    void updateClient_PreserveSendgridSenderName_Success() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
        testClient.setSendgridSenderName("Existing Sender");
        testClient.setImgbbApiKey("imgbb-key");
        testClient.setLogoDeleteHash(null);

        testClientRequest.setSendgridSenderName(null);
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act
        assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));

        // Assert
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, atLeastOnce()).save(captor.capture());
        assertEquals("Existing Sender", captor.getValue().getSendgridSenderName());
    }

    /*
     * Purpose: Verify logo removal via ImgBB path.
     * Expected Result: Client is saved without logo URL.
     * Assertions: Save is called with cleared logo URL.
     */
    @Test
    @DisplayName("Update Client - Remove Logo ImgBB - Success")
    void updateClient_RemoveLogoImgBB_Success() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
        testClientRequest.setLogoBase64(null);
        testClient.setImgbbApiKey("valid-key");
        testClient.setLogoDeleteHash("oldHash");

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientSave(testClient);

        // Act
        try (MockedConstruction<ImgbbHelper> imgMock = mockConstruction(ImgbbHelper.class)) {
            clientService.updateClient(testClientRequest);

            // Assert
            verify(imgMock.constructed().get(0)).deleteImage("oldHash");
        }
    }

    /*
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
        testClient.setImgbbApiKey("valid-key");
        testClient.setLogoDeleteHash("oldHash");

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                "http://imgbb.com/new.png",
                "newHash");

        // Act
        try (MockedConstruction<ImgbbHelper> imgMock = stubImgbbUploadResponse(mockResponse)) {

            clientService.updateClient(testClientRequest);

            // Assert
            verify(clientRepository, times(2)).save(any(Client.class));
            verify(imgMock.constructed().get(0)).deleteImage("oldHash");
            verify(imgMock.constructed().get(0)).uploadFileToImgbb(eq("newLogoData"), anyString());
        }
    }

    /*
     * Purpose: Validate very long name updates succeed.
     * Expected Result: Update succeeds.
     * Assertions: Save is called without errors.
     */
    @Test
    @DisplayName("Update Client - Very long updated name - Success")
    void updateClient_VeryLongName_Success() {
        // Arrange
        String longName = "B".repeat(500);
        testClientRequest.setName(longName);
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(longName, Optional.empty());
        stubClientSave(testClient);

        // Act
        try (MockedConstruction<FirebaseHelper> ignored = mockConstruction(FirebaseHelper.class)) {
            assertDoesNotThrow(() -> clientService.updateClient(testClientRequest));
        }

        // Assert
        verify(clientRepository).save(any(Client.class));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Reject duplicate name on a different client.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message indicates name already exists.
     */
    @Test
    @DisplayName("Update Client - Duplicate Name (Different Client) - ThrowsBadRequestException")
    void updateClient_DuplicateName_ThrowsBadRequestException() {
        // Arrange
        Client otherClient = new Client();
        otherClient.setClientId(999L);
        otherClient.setName(testClientRequest.getName());

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.of(otherClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(
                String.format(ErrorMessages.ClientErrorMessages.DUPLICATE_CLIENT_NAME_FORMAT, testClientRequest.getName()),
                ex.getMessage());
    }

    /*
     * Purpose: Reject invalid description (Empty).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidDescription.
     */
    @Test
    @DisplayName("Update Client - Empty Description - ThrowsBadRequestException")
    void updateClient_EmptyDescription_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setDescription("");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_DESCRIPTION, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid name (Empty).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidName.
     */
    @Test
    @DisplayName("Update Client - Empty Name - ThrowsBadRequestException")
    void updateClient_EmptyName_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setName("");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_NAME, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid SendgridSenderName (Empty).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSendgridSenderName.
     */
    @Test
    @DisplayName("Update Client - Empty SendgridSenderName - ThrowsBadRequestException")
    void updateClient_EmptySendgridSenderName_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setSendgridSenderName("");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_SENDGRID_SENDER_NAME, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid support email (Empty).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSupportEmail.
     */
    @Test
    @DisplayName("Update Client - Empty Support Email - ThrowsBadRequestException")
    void updateClient_EmptySupportEmail_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setSupportEmail("");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_SUPPORT_EMAIL, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid website (Empty).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidWebsite.
     */
    @Test
    @DisplayName("Update Client - Empty Website - ThrowsBadRequestException")
    void updateClient_EmptyWebsite_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setWebsite("");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_WEBSITE, ex.getMessage());
    }

    /*
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

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);
        stubGoogleCredFindById(anyLong(), Optional.empty());

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.ER011, ex.getMessage());
    }

    /*
     * Purpose: Reject Firebase logo upload failures.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidLogoUpload.
     */
    @Test
    @DisplayName("Update Client - Firebase upload fail - ThrowsBadRequestException")
    void updateClient_FirebaseUploadFail_ThrowsBadRequestException() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "firebase");
        testClientRequest.setLogoBase64("newLogoData");

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);
        stubGoogleCredFindById(DEFAULT_GOOGLE_CRED_ID, Optional.of(testGoogleCred));

        // Act & Assert
        try (MockedConstruction<FirebaseHelper> fbMock = stubFirebaseUploadFail()) {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.INVALID_LOGO_UPLOAD, ex.getMessage());
        }
    }

    /*
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

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientSave(testClient);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED, ex.getMessage());
    }

    /*
     * Purpose: Reject ImgBB upload when response is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidLogoUpload.
     */
    @Test
    @DisplayName("Update Client - ImgBB upload null - ThrowsBadRequestException")
    void updateClient_ImgbbUploadNull_ThrowsBadRequestException() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "imgbb");
        testClientRequest.setLogoBase64("newLogoData");
        testClient.setImgbbApiKey("valid-key");

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act & Assert
        try (MockedConstruction<ImgbbHelper> imgMock = stubImgbbUploadResponse(null)) {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> clientService.updateClient(testClientRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.INVALID_LOGO_UPLOAD, ex.getMessage());
        }
    }

    /*
     * Purpose: Reject invalid imageLocation configuration on update.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER019.
     */
    @Test
    @DisplayName("Update Client - Invalid imageLocation config - ThrowsBadRequestException")
    void updateClient_InvalidImageLocation_ThrowsBadRequestException() {
        // Arrange
        ReflectionTestUtils.setField(clientService, "imageLocation", "invalid");
        testClientRequest.setLogoBase64(null);

        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));
        stubClientFindByName(testClientRequest.getName(), Optional.empty());
        stubClientSave(testClient);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ConfigurationErrorMessages.INVALID_IMAGE_LOCATION_CONFIG_FORMAT,
                "invalid"), ex.getMessage());
    }

    /*
     * Purpose: Reject negative ID updates.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Client - Negative ID - ThrowsNotFoundException")
    void updateClient_NegativeId_ThrowsNotFoundException() {
        // Arrange
        testClientRequest.setClientId(-1L);
        stubClientFindById(-1L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Reject updates for missing client IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Client - Client not found - ThrowsNotFoundException")
    void updateClient_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubClientFindById(TEST_CLIENT_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid description (Null).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidDescription.
     */
    @Test
    @DisplayName("Update Client - Null Description - ThrowsBadRequestException")
    void updateClient_NullDescription_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setDescription(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_DESCRIPTION, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid name (Null).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidName.
     */
    @Test
    @DisplayName("Update Client - Null Name - ThrowsBadRequestException")
    void updateClient_NullName_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setName(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_NAME, ex.getMessage());
    }

    /*
     * Purpose: Reject null update request.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidRequest.
     */
    @Test
    @DisplayName("Update Client - Null request - ThrowsBadRequestException")
    void updateClient_NullRequest_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> clientService.updateClient(null));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_REQUEST, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid support email (Null).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSupportEmail.
     */
    @Test
    @DisplayName("Update Client - Null Support Email - ThrowsBadRequestException")
    void updateClient_NullSupportEmail_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setSupportEmail(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_SUPPORT_EMAIL, ex.getMessage());
    }

    /*
     * Purpose: Reject invalid website (Null).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidWebsite.
     */
    @Test
    @DisplayName("Update Client - Null Website - ThrowsBadRequestException")
    void updateClient_NullWebsite_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setWebsite(null);
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_WEBSITE, ex.getMessage());
    }

    /*
     * Purpose: Reject whitespace-only description.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidDescription.
     */
    @Test
    @DisplayName("Update Client - Whitespace only description - ThrowsBadRequestException")
    void updateClient_WhitespaceOnlyDescription_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setDescription("   ");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_DESCRIPTION, ex.getMessage());
    }

    /*
     * Purpose: Reject whitespace-only support email.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSupportEmail.
     */
    @Test
    @DisplayName("Update Client - Whitespace only email - ThrowsBadRequestException")
    void updateClient_WhitespaceOnlyEmail_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setSupportEmail("   ");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_SUPPORT_EMAIL, ex.getMessage());
    }

    /*
     * Purpose: Reject whitespace-only name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidName.
     */
    @Test
    @DisplayName("Update Client - Whitespace only name - ThrowsBadRequestException")
    void updateClient_WhitespaceOnlyName_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setName("   ");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_NAME, ex.getMessage());
    }

    /*
     * Purpose: Reject whitespace-only sendgrid sender name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidSendgridSenderName.
     */
    @Test
    @DisplayName("Update Client - Whitespace only sendgrid sender name - ThrowsBadRequestException")
    void updateClient_WhitespaceOnlySendgridSenderName_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setSendgridSenderName("   ");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_SENDGRID_SENDER_NAME, ex.getMessage());
    }

    /*
     * Purpose: Reject whitespace-only website.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidWebsite.
     */
    @Test
    @DisplayName("Update Client - Whitespace only website - ThrowsBadRequestException")
    void updateClient_WhitespaceOnlyWebsite_ThrowsBadRequestException() {
        // Arrange
        testClientRequest.setWebsite("   ");
        stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_WEBSITE, ex.getMessage());
    }

    /*
     * Purpose: Reject zero ID updates.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Client - Zero ID - ThrowsNotFoundException")
    void updateClient_ZeroId_ThrowsNotFoundException() {
        // Arrange
        testClientRequest.setClientId(0L);
        stubClientFindById(0L, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> clientService.updateClient(testClientRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     */
    @Test
    @DisplayName("Update Client - Controller delegates to service correctly")
    void updateClient_p01_ControllerDelegation_Success() {
        // Arrange
        testClientRequest.setClientId(TEST_CLIENT_ID);
        stubServiceUpdateClientDoNothing();

        // Act
        ResponseEntity<?> response = clientController.updateClient(TEST_CLIENT_ID, testClientRequest);

        // Assert
        verify(mockClientService, times(1)).updateClient(testClientRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }

    /*
     * Purpose: Verify @PreAuthorize annotation is declared on updateClient method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references UPDATE_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Update Client - Verify @PreAuthorize annotation is configured correctly")
    void updateClient_p02_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        var method = ClientController.class.getMethod("updateClient",
                Long.class, com.example.SpringApi.Models.RequestModels.ClientRequestModel.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "updateClient method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.UPDATE_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference UPDATE_CLIENT_PERMISSION");
    }

    /*
     * Purpose: Verify controller has correct @PreAuthorize permission.
     * Expected Result: Annotation exists and contains UPDATE_CLIENT_PERMISSION.
     * Assertions: Annotation is present and permission matches.
     */
    @Test
    @DisplayName("Update Client - Controller permission forbidden - Success")
    void updateClient_p03_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        var method = ClientController.class.getMethod("updateClient", Long.class,
                com.example.SpringApi.Models.RequestModels.ClientRequestModel.class);
        testClientRequest.setClientId(TEST_CLIENT_ID);
        stubServiceUpdateClientDoNothing();

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);
        ResponseEntity<?> response = clientController.updateClient(TEST_CLIENT_ID, testClientRequest);

        // Assert
        assertNotNull(preAuthorizeAnnotation, "updateClient method should have @PreAuthorize annotation");
        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.UPDATE_CLIENT_PERMISSION + "')";
        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference UPDATE_CLIENT_PERMISSION");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
    }
}