package com.example.springapi.ServiceTests.Shipping;

import static org.junit.jupiter.api.Assertions.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.ShippingController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Tests for ShippingService.getWalletBalance(). */
@DisplayName("GetWalletBalance Tests")
class GetWalletBalanceTest extends ShippingServiceTestBase {

  // Total Tests: 4
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify wallet balance returns value. Expected Result: Balance returned. Assertions:
   * Balance equals expected.
   */
  @Test
  @DisplayName("getWalletBalance - Success")
  void getWalletBalance_Success_Success() {
    // Arrange
    stubClientServiceGetClientById(testClientResponse);
    stubShipRocketHelperGetWalletBalance(123.45);

    // Act
    Double result = shippingService.getWalletBalance();

    // Assert
    assertEquals(123.45, result);
  }

  /*
   **********************************************************************************************
   * FAILURE TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify missing credentials throws BadRequestException. Expected Result:
   * BadRequestException with ShipRocketCredentialsNotConfigured message. Assertions: Exception type
   * and message.
   */
  @Test
  @DisplayName("getWalletBalance - Credentials Missing - Throws BadRequestException")
  void getWalletBalance_CredentialsMissing_ThrowsBadRequestException() {
    // Arrange
    testClientResponse.setShipRocketPassword(null);
    stubClientServiceGetClientById(testClientResponse);

    // Act
    com.example.springapi.exceptions.BadRequestException ex =
        assertThrows(
            com.example.springapi.exceptions.BadRequestException.class,
            () -> shippingService.getWalletBalance());

    // Assert
    assertEquals(
        ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED,
        ex.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getWalletBalance - Controller Permission - Unauthorized")
  void getWalletBalance_controller_permission_unauthorized() {
    // Arrange
    ShippingController controller = new ShippingController(shippingServiceMock);
    stubShippingServiceMockGetWalletBalanceUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getWalletBalance();

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
