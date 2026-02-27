package com.example.SpringApi.ServiceTests.Shipping;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipment;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageProduct;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Tests for ShippingService.getShipmentById(). */
@DisplayName("GetShipmentById Tests")
class GetShipmentByIdTest extends ShippingServiceTestBase {

  // Total Tests: 16
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify valid shipment ID with different ID returns response. Expected Result:
   * ShipmentResponseModel is returned. Assertions: Response shipmentId matches.
   */
  @Test
  @DisplayName("getShipmentById - Different ID - Success")
  void getShipmentById_DifferentId_Success() {
    // Arrange
    Shipment shipment = createTestShipment(999L);
    shipment.setShipRocketOrderId("SR-999");
    stubShipmentRepositoryFindById(shipment);

    // Act
    ShipmentResponseModel result = shippingService.getShipmentById(999L);

    // Assert
    assertNotNull(result);
    assertEquals(999L, result.getShipmentId());
  }

  /**
   * Purpose: Verify response when shipment has order id. Expected Result: ShipmentResponseModel
   * returned without error. Assertions: Response is not null.
   */
  @Test
  @DisplayName("getShipmentById - ShipRocket Order Id Present - Success")
  void getShipmentById_ShipRocketOrderIdPresent_Success() {
    // Arrange
    testShipment.setShipRocketOrderId("SR-123");
    stubShipmentRepositoryFindById(testShipment);

    // Act
    ShipmentResponseModel result = shippingService.getShipmentById(TEST_SHIPMENT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
  }

  /**
   * Purpose: Verify valid shipment ID returns response. Expected Result: ShipmentResponseModel is
   * returned. Assertions: Response is not null.
   */
  @Test
  @DisplayName("getShipmentById - Valid ID - Success")
  void getShipmentById_ValidId_Success() {
    // Arrange
    stubShipmentRepositoryFindById(testShipment);

    // Act
    ShipmentResponseModel result = shippingService.getShipmentById(TEST_SHIPMENT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
  }

  /**
   * Purpose: Verify nested shipment graph is initialized for response mapping. Expected Result:
   * ShipmentResponseModel returned with nested lists. Assertions: Response contains package and
   * return shipment data.
   */
  @Test
  @DisplayName("getShipmentById - Nested Graph - Success")
  void getShipmentById_NestedGraph_Success() {
    // Arrange
    ShipmentPackageProduct shipmentPackageProduct = new ShipmentPackageProduct();
    shipmentPackageProduct.setProduct(testProduct);
    testShipmentPackage.setShipmentPackageProducts(List.of(shipmentPackageProduct));
    testShipmentPackage.setPackageInfo(testPackage);
    testShipment.setShipmentProducts(List.of(testShipmentProduct));
    testShipment.setShipmentPackages(List.of(testShipmentPackage));

    ReturnShipment returnShipment = new ReturnShipment();
    ReturnShipmentProduct returnShipmentProduct = new ReturnShipmentProduct();
    returnShipment.setReturnProducts(List.of(returnShipmentProduct));
    testShipment.setReturnShipments(List.of(returnShipment));
    stubShipmentRepositoryFindById(testShipment);

    // Act
    ShipmentResponseModel result = shippingService.getShipmentById(TEST_SHIPMENT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
    assertNotNull(result.getPackages());
    assertNotNull(result.getReturnShipments());
  }

  /*
   **********************************************************************************************
   * FAILURE TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify client mismatch throws NotFoundException. Expected Result: NotFoundException
   * with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Client Mismatch - Throws NotFoundException")
  void getShipmentById_ClientMismatch_ThrowsNotFoundException() {
    // Arrange
    Shipment shipment = createTestShipment(TEST_SHIPMENT_ID);
    shipment.setClientId(999L);
    stubShipmentRepositoryFindById(shipment);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify NotFoundException for large ID when shipment not found. Expected Result:
   * NotFoundException with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Large ID Not Found - Throws NotFoundException")
  void getShipmentById_LargeIdNotFound_ThrowsNotFoundException() {
    // Arrange
    stubShipmentRepositoryFindById(null);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(Long.MAX_VALUE));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, Long.MAX_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Verify multiple invalid calls still throw BadRequestException. Expected Result:
   * BadRequestException with InvalidId message. Assertions: Exception type and message for each
   * call.
   */
  @Test
  @DisplayName("getShipmentById - Multiple Invalid IDs - Throws BadRequestException")
  void getShipmentById_MultipleInvalidIds_ThrowsBadRequestException() {
    // Arrange
    Long shipmentId1 = -5L;
    Long shipmentId2 = 0L;

    // Act
    com.example.SpringApi.Exceptions.BadRequestException ex1 =
        assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId1));
    com.example.SpringApi.Exceptions.BadRequestException ex2 =
        assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId2));

    // Assert
    assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex1.getMessage());
    assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex2.getMessage());
  }

  /**
   * Purpose: Verify negative ID throws BadRequestException. Expected Result: BadRequestException
   * with InvalidId message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Negative ID - Throws BadRequestException")
  void getShipmentById_NegativeId_ThrowsBadRequestException() {
    // Arrange
    Long shipmentId = -1L;

    // Act
    com.example.SpringApi.Exceptions.BadRequestException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

    // Assert
    assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify non-existent shipment throws NotFoundException. Expected Result:
   * NotFoundException with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Not Found - Throws NotFoundException")
  void getShipmentById_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubShipmentRepositoryFindById(null);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify null ID throws BadRequestException. Expected Result: BadRequestException with
   * InvalidId message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Null ID - Throws BadRequestException")
  void getShipmentById_NullId_ThrowsBadRequestException() {
    // Arrange
    Long shipmentId = null;

    // Act
    com.example.SpringApi.Exceptions.BadRequestException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

    // Assert
    assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify empty ShipRocket order ID throws NotFoundException. Expected Result:
   * NotFoundException with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - ShipRocket Order ID Empty - Throws NotFoundException")
  void getShipmentById_ShipRocketOrderIdEmpty_ThrowsNotFoundException() {
    // Arrange
    testShipment.setShipRocketOrderId("");
    stubShipmentRepositoryFindById(testShipment);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify missing ShipRocket order ID throws NotFoundException. Expected Result:
   * NotFoundException with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - ShipRocket Order ID Null - Throws NotFoundException")
  void getShipmentById_ShipRocketOrderIdNull_ThrowsNotFoundException() {
    // Arrange
    testShipment.setShipRocketOrderId(null);
    stubShipmentRepositoryFindById(testShipment);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify whitespace ShipRocket order ID throws NotFoundException. Expected Result:
   * NotFoundException with NotFound message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - ShipRocket Order ID Whitespace - Throws NotFoundException")
  void getShipmentById_ShipRocketOrderIdWhitespace_ThrowsNotFoundException() {
    // Arrange
    testShipment.setShipRocketOrderId("   ");
    stubShipmentRepositoryFindById(testShipment);

    // Act
    com.example.SpringApi.Exceptions.NotFoundException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify zero ID throws BadRequestException. Expected Result: BadRequestException with
   * InvalidId message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentById - Zero ID - Throws BadRequestException")
  void getShipmentById_ZeroId_ThrowsBadRequestException() {
    // Arrange
    Long shipmentId = 0L;

    // Act
    com.example.SpringApi.Exceptions.BadRequestException ex =
        assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

    // Assert
    assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
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
  @DisplayName("getShipmentById - Controller Permission - Unauthorized")
  void getShipmentById_controller_permission_unauthorized() {
    // Arrange
    ShippingController controller = new ShippingController(shippingServiceMock);
    stubShippingServiceMockGetShipmentByIdUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getShipmentById(TEST_SHIPMENT_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}

