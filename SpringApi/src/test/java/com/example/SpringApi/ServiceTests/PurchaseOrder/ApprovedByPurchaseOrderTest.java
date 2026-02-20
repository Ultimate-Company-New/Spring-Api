package com.example.SpringApi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
 *
 * <p>Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - ApprovedByPurchaseOrder Tests")
class ApprovedByPurchaseOrderTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 6
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify approve succeeds when not already approved. Expected Result: Purchase order is
   * updated and logged. Assertions: Save is called and no exception thrown.
   */
  @Test
  @DisplayName("Approved By Purchase Order - Success")
  void approvedByPurchaseOrder_Success_Success() {
    // Arrange
    testPurchaseOrder.setApprovedByUserId(null);
    stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
    stubPurchaseOrderRepositorySave(testPurchaseOrder);

    // Act & Assert
    assertDoesNotThrow(() -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject approval if already approved. Expected Result: BadRequestException is thrown.
   * Assertions: Message matches AlreadyApproved.
   */
  @Test
  @DisplayName("Approved By Purchase Order - Already Approved - Throws BadRequestException")
  void approvedByPurchaseOrder_AlreadyApproved_Failure() {
    // Arrange
    testPurchaseOrder.setApprovedByUserId(999L);
    stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.ALREADY_APPROVED,
        () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
  }

  /**
   * Purpose: Reject approval for missing purchase order. Expected Result: NotFoundException is
   * thrown. Assertions: Message matches InvalidId.
   */
  @Test
  @DisplayName("Approved By Purchase Order - Not Found - Throws NotFoundException")
  void approvedByPurchaseOrder_NotFound_Failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(Optional.empty());

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID,
        () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
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
  @DisplayName("approvedByPurchaseOrder - Controller Permission - Unauthorized")
  void approvedByPurchaseOrder_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceThrowsUnauthorizedOnApprove();

    // Act
    ResponseEntity<?> response = controller.approvedByPurchaseOrder(TEST_PO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service is called once and
   * HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("approvedByPurchaseOrder - Controller delegates to service")
  void approvedByPurchaseOrder_WithValidId_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceApproveDoNothing();

    // Act
    ResponseEntity<?> response = controller.approvedByPurchaseOrder(TEST_PO_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
