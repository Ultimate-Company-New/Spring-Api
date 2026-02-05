package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.toggleClient() method.
 * Tests the toggling of client deleted status.
 * * Test Count: 9 tests
 */
@DisplayName("Toggle Client Tests")
class ToggleClientTest extends ClientServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

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

    /**
     * Purpose: Verify permission check is performed for DELETE_CLIENT permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Toggle Client - Permission check - Success Verifies Authorization")
    void toggleClient_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        testClient.setIsDeleted(false);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        lenient().when(authorization.hasAuthority(Authorizations.DELETE_CLIENT_PERMISSION)).thenReturn(true);

        // Act
        clientService.toggleClient(TEST_CLIENT_ID);

        // Assert
        verify(authorization, times(1)).hasAuthority(Authorizations.DELETE_CLIENT_PERMISSION);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

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
}