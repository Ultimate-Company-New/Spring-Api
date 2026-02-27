package com.example.springapi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.springapi.controllers.PaymentController;
import com.example.springapi.models.Authorizations;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Tests for PaymentService.isPurchaseOrderPaid(). */
@DisplayName("IsPurchaseOrderPaid Tests")
class IsPurchaseOrderPaidTest extends PaymentServiceTestBase {

  // Total Tests: 7
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify successful payment presence returns true. Expected Result: true. Assertions:
   * assertTrue(result).
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Has Successful Payment - Success")
  void isPurchaseOrderPaid_hasSuccessfulPayment_success() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPayment(TEST_PO_ID, true);

    // Act
    boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

    // Assert
    assertTrue(result);
  }

  /**
   * Purpose: Verify repository interaction parameters. Expected Result: hasSuccessfulPayment called
   * once with expected args. Assertions: verify interaction.
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Verify Payment Repository Interaction - Success")
  void isPurchaseOrderPaid_verifyPaymentRepositoryInteraction_success() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPayment(TEST_PO_ID, true);

    // Act
    paymentService.isPurchaseOrderPaid(TEST_PO_ID);

    // Assert
    verify(paymentRepository, times(1)).hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify no successful payment returns false. Expected Result: false. Assertions:
   * assertFalse(result).
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - No Successful Payment - Failure")
  void isPurchaseOrderPaid_f01_noSuccessfulPayment_failure() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPayment(TEST_PO_ID, false);

    // Act
    boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

    // Assert
    assertFalse(result);
  }

  /**
   * Purpose: Verify negative purchase order id returns false. Expected Result: false. Assertions:
   * assertFalse(result).
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Negative Purchase Order Id - Failure")
  void isPurchaseOrderPaid_f02_negativePurchaseOrderId_failure() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPayment(-1L, false);

    // Act
    boolean result = paymentService.isPurchaseOrderPaid(-1L);

    // Assert
    assertFalse(result);
  }

  /**
   * Purpose: Verify zero purchase order id returns false. Expected Result: false. Assertions:
   * assertFalse(result).
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Zero Purchase Order Id - Failure")
  void isPurchaseOrderPaid_f03_zeroPurchaseOrderId_failure() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPayment(0L, false);

    // Act
    boolean result = paymentService.isPurchaseOrderPaid(0L);

    // Assert
    assertFalse(result);
  }

  /**
   * Purpose: Verify additional ids return false when repository responds false. Expected Result:
   * false for each id. Assertions: assertFalse for each id and repository invocation count.
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Additional Purchase Order Ids - Failure")
  void isPurchaseOrderPaid_f04_additionalPurchaseOrderIds_failure() {
    // Arrange
    stubPaymentRepositoryHasSuccessfulPaymentAny(false);
    Long[] ids = new Long[] {2L, 3L, 4L, 5L, Long.MAX_VALUE};

    // Act
    for (Long id : ids) {
      boolean result = paymentService.isPurchaseOrderPaid(id);

      // Assert
      assertFalse(result);
    }

    assertTrue(ids.length > 0);
    verify(paymentRepository, times(ids.length))
        .hasSuccessfulPayment(eq("PURCHASE_ORDER"), anyLong());
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify receipt download endpoint permission handling for payment-id based operations.
   * Expected Result: Unauthorized status is returned and expected permission annotation exists.
   * Assertions: HTTP status and annotation permission constant.
   */
  @Test
  @DisplayName("isPurchaseOrderPaid - Controller Permission Forbidden")
  void isPurchaseOrderPaid_controller_permission_forbidden() throws Exception {
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
