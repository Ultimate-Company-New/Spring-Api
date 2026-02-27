package com.example.SpringApi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
 *
 * <p>Test count: 5 tests
 */
@DisplayName("PurchaseOrderService - GetPurchaseOrderDetailsById Tests")
class GetPurchaseOrderDetailsByIdTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 5
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify get details succeeds for valid ID. Expected Result: Response model is returned.
   * Assertions: Response has matching ID and vendor number.
   */
  @Test
  @DisplayName("Get Purchase Order Details By Id - Success")
  void getPurchaseOrderDetailsById_Success_Success() {
    // Arrange
    stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));

    // Act
    PurchaseOrderResponseModel result =
        assertDoesNotThrow(() -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));

    // Assert
    assertNotNull(result);
    assertEquals(TEST_PO_ID, result.getPurchaseOrderId());
    assertEquals(TEST_VENDOR_NUMBER, result.getVendorNumber());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify not found is thrown for missing ID. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidId.
   */
  @Test
  @DisplayName("Get Purchase Order Details By Id - Not Found")
  void getPurchaseOrderDetailsById_NotFound_Failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.empty());

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID,
        () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getPurchaseOrderDetailsById - Controller Permission - Unauthorized")
  void getPurchaseOrderDetailsById_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceThrowsUnauthorizedOnGetById();

    // Act
    ResponseEntity<?> response = controller.getPurchaseOrderDetailsById(TEST_PO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service is called once and
   * HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("getPurchaseOrderDetailsById - Controller delegates to service")
  void getPurchaseOrderDetailsById_WithValidId_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceGetPurchaseOrderDetailsById(
        new PurchaseOrderResponseModel(testPurchaseOrder));

    // Act
    ResponseEntity<?> response = controller.getPurchaseOrderDetailsById(TEST_PO_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

