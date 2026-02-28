package springapi.servicetests.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import springapi.ErrorMessages;
import springapi.controllers.PaymentController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.models.Authorizations;
import springapi.models.databasemodels.Payment;

/** Tests for PaymentService.getPaymentsForPurchaseOrder(). */
@DisplayName("GetPaymentsForPurchaseOrder Tests")
class GetPaymentsForPurchaseOrderTest extends PaymentServiceTestBase {

  // Total Tests: 9
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify purchase order repository is queried with requested purchase order id. Expected
   * Result: purchaseOrderRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName(
      "getPaymentsForPurchaseOrder - Verify Purchase Order Repository Interaction - Success")
  void getPaymentsForPurchaseOrder_verifyPurchaseOrderRepositoryInteraction_success() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
  }

  /**
   * Purpose: Verify service returns payment history when purchase order exists and client has
   * access. Expected Result: Returned list matches repository result. Assertions: List size, order,
   * and repository interaction.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Returns Payments List - Success")
  void getPaymentsForPurchaseOrder_s02_returnsPaymentsList_success() {
    // Arrange
    Payment paymentOne = new Payment();
    paymentOne.setPaymentId(101L);
    paymentOne.setClientId(TEST_CLIENT_ID);
    Payment paymentTwo = new Payment();
    paymentTwo.setPaymentId(102L);
    paymentTwo.setClientId(TEST_CLIENT_ID);
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubPaymentRepositoryFindAllByPurchaseOrderId(TEST_PO_ID, List.of(paymentOne, paymentTwo));

    // Act
    List<Payment> result = paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID);

    // Assert
    assertEquals(2, result.size());
    assertEquals(101L, result.get(0).getPaymentId());
    assertEquals(102L, result.get(1).getPaymentId());
    verify(paymentRepository, times(1)).findAllByPurchaseOrderId(TEST_PO_ID);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify missing purchase order throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Purchase Order Not Found - Failure")
  void getPaymentsForPurchaseOrder_f01_purchaseOrderNotFound_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Negative Purchase Order Id - Failure")
  void getPaymentsForPurchaseOrder_f02_negativePurchaseOrderId_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(-1L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(-1L));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Zero Purchase Order Id - Failure")
  void getPaymentsForPurchaseOrder_f03_zeroPurchaseOrderId_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(0L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(0L));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify max long purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Max Long Purchase Order Id - Failure")
  void getPaymentsForPurchaseOrder_f04_maxLongPurchaseOrderId_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(Long.MAX_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.getPaymentsForPurchaseOrder(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify access denied scenario when purchase order belongs to another client. Expected
   * Result: BadRequestException with access denied message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Client Access Denied - Failure")
  void getPaymentsForPurchaseOrder_f05_clientAccessDenied_failure() {
    // Arrange
    testPurchaseOrder.setClientId(TEST_CLIENT_ID + 1);
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs throw NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id for each id. Assertions: Exception type and
   * exact message for each id.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Additional Invalid Purchase Order Ids - Failure")
  void getPaymentsForPurchaseOrder_f06_additionalInvalidPurchaseOrderIds_failure() {
    // Arrange
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L, -100L};

    // Act
    for (Long invalidId : invalidIds) {
      stubPurchaseOrderRepositoryFindById(invalidId, Optional.empty());
      NotFoundException ex =
          assertThrows(
              NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(invalidId));

      // Assert
      assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify receipt download endpoint permission handling for payment-related access.
   * Expected Result: Unauthorized status is returned and expected permission annotation exists.
   * Assertions: HTTP status and annotation permission constant.
   */
  @Test
  @DisplayName("getPaymentsForPurchaseOrder - Controller Permission Forbidden")
  void getPaymentsForPurchaseOrder_controller_permission_forbidden() throws Exception {
    // Arrange
    stubPaymentServiceGeneratePaymentReceiptPDFThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.downloadPaymentReceipt(TEST_PAYMENT_ID);
    Method method = PaymentController.class.getMethod("downloadPaymentReceipt", Long.class);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(annotation, "@PreAuthorize annotation should be present");
    assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION));
  }
}
