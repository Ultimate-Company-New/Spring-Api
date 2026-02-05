package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.getClientById() method.
 * Tests retrieval of client details by ID.
 * * Test Count: 9 tests
 */
@DisplayName("Get Client By ID Tests")
class GetClientByIdTest extends ClientServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

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
     * Purpose: Verify permission check is performed for VIEW_CLIENT permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Client By ID - Permission check - Success Verifies Authorization")
    void getClientById_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_CLIENT_PERMISSION)).thenReturn(true);

        // Act
        clientService.getClientById(TEST_CLIENT_ID);

        // Assert
        verify(authorization, times(1)).hasAuthority(Authorizations.VIEW_CLIENT_PERMISSION);
    }

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

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

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
}