package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.toggleClient() method.
 * Tests the toggling of client deleted status.
 * * Test Count: 10 tests
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
     * Purpose: Verify @PreAuthorize annotation is declared on toggleClient method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references DELETE_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Toggle Client - Verify @PreAuthorize annotation is configured correctly")
    void toggleClient_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = ClientController.class.getMethod("toggleClient",
                Long.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "toggleClient method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.DELETE_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference DELETE_CLIENT_PERMISSION");
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
    @DisplayName("Toggle Client - Controller delegates to service correctly")
    void toggleClient_WithValidRequest_DelegatesToService() {
        // Arrange
        ClientService mockService = mock(ClientService.class);
        ClientController controller = new ClientController(mockService);
        doNothing().when(mockService).toggleClient(TEST_CLIENT_ID);

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.toggleClient(TEST_CLIENT_ID);

        // Assert - Verify service was called and correct response returned
        verify(mockService, times(1)).toggleClient(TEST_CLIENT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}