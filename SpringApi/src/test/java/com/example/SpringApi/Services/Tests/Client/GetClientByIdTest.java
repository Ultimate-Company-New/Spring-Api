package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.getClientById() method.
 * Tests retrieval of client details by ID.
 * * Test Count: 10 tests
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
     * Purpose: Verify @PreAuthorize annotation is declared on getClientById method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Get Client By ID - Verify @PreAuthorize annotation is configured correctly")
    void getClientById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = ClientController.class.getMethod("getClientById",
                Long.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getClientById method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_CLIENT_PERMISSION");
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
    @DisplayName("Get Client By ID - Controller delegates to service correctly")
    void getClientById_WithValidRequest_DelegatesToService() {
        // Arrange
        ClientService mockService = mock(ClientService.class);
        ClientController controller = new ClientController(mockService);
        when(mockService.getClientById(TEST_CLIENT_ID)).thenReturn(new ClientResponseModel());

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.getClientById(TEST_CLIENT_ID);

        // Assert - Verify service was called and correct response returned
        verify(mockService, times(1)).getClientById(TEST_CLIENT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}