package com.example.SpringApi.ServiceTests.Client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for ClientService.getClientById() method. */
@DisplayName("Get Client By ID Tests")
class GetClientByIdTest extends ClientServiceTestBase {

  // Total Tests: 11
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
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
    stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

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

  /*
   * Purpose: Verify deleted clients can be returned by ID.
   * Expected Result: Response indicates deleted status.
   * Assertions: Response has isDeleted=true.
   */
  @Test
  @DisplayName("Get Client By ID - Deleted client - Success")
  void getClientById_DeletedClient_Success() {
    // Arrange
    testClient.setIsDeleted(true);
    stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

    // Act
    ClientResponseModel result = clientService.getClientById(TEST_CLIENT_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.getIsDeleted());
    assertEquals(TEST_CLIENT_ID, result.getClientId());
  }

  /*
   * Purpose: Verify client details are returned for a valid ID.
   * Expected Result: Response contains client fields.
   * Assertions: Response fields match the test client.
   */
  @Test
  @DisplayName("Get Client By ID - Client found - Success")
  void getClientById_Success_Success() {
    // Arrange
    stubClientFindById(TEST_CLIENT_ID, Optional.of(testClient));

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

  /*
   * Purpose: Validate max long ID is rejected when not found.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches InvalidId.
   */
  @Test
  @DisplayName("Get Client By ID - Max Long ID - ThrowsNotFoundException")
  void getClientById_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubClientFindById(Long.MAX_VALUE, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> clientService.getClientById(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Validate min long ID is rejected when not found.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches InvalidId.
   */
  @Test
  @DisplayName("Get Client By ID - Min Long ID - ThrowsNotFoundException")
  void getClientById_MinLongId_ThrowsNotFoundException() {
    // Arrange
    stubClientFindById(Long.MIN_VALUE, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> clientService.getClientById(Long.MIN_VALUE));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Validate negative ID is rejected.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches InvalidId.
   */
  @Test
  @DisplayName("Get Client By ID - Negative ID - ThrowsNotFoundException")
  void getClientById_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long negativeId = -1L;
    stubClientFindById(negativeId, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> clientService.getClientById(negativeId));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Validate missing client ID returns not found.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches InvalidId.
   */
  @Test
  @DisplayName("Get Client By ID - Client not found - ThrowsNotFoundException")
  void getClientById_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubClientFindById(TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> clientService.getClientById(TEST_CLIENT_ID));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Validate zero ID is rejected.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches InvalidId.
   */
  @Test
  @DisplayName("Get Client By ID - Zero ID - ThrowsNotFoundException")
  void getClientById_ZeroId_ThrowsNotFoundException() {
    // Arrange
    long zeroId = 0L;
    stubClientFindById(zeroId, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> clientService.getClientById(zeroId));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify controller calls service when authorization passes
   * (simulated).
   * Expected Result: Service method is called and correct HTTP status is
   * returned.
   * Assertions: Service called once, HTTP status is correct.
   */
  @Test
  @DisplayName("Get Client By ID - Controller delegates to service correctly")
  void getClientById_p02_WithValidRequest_DelegatesToService_Success() {
    // Arrange
    stubServiceGetClientById(new ClientResponseModel());

    // Act
    ResponseEntity<?> response = clientController.getClientById(TEST_CLIENT_ID);

    // Assert
    verify(mockClientService, times(1)).getClientById(TEST_CLIENT_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }

  /*
   * Purpose: Verify controller has correct @PreAuthorize permission.
   * Expected Result: Annotation exists and contains VIEW_CLIENT_PERMISSION.
   * Assertions: Annotation is present and permission matches.
   */
  @Test
  @DisplayName("Get Client By ID - Controller permission forbidden - Success")
  void getClientById_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method = ClientController.class.getMethod("getClientById", Long.class);
    stubServiceGetClientById(new ClientResponseModel());

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = clientController.getClientById(TEST_CLIENT_ID);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "getClientById method should have @PreAuthorize annotation");
    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.VIEW_CLIENT_PERMISSION + "')";
    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference VIEW_CLIENT_PERMISSION");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
