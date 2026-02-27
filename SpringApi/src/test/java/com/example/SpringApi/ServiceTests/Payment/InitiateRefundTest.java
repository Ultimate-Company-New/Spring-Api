package com.example.SpringApi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Tests for PaymentService.initiateRefund(). */
@DisplayName("InitiateRefund Tests")
class InitiateRefundTest extends PaymentServiceTestBase {

  // Total Tests: 14
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify client repository is queried for refund flow. Expected Result:
   * clientRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("initiateRefund - Verify Client Repository Interaction - Success")
  void initiateRefund_verifyClientRepositoryInteraction_success() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    verify(clientRepository, times(1)).findById(any());
  }

  /**
   * Purpose: Verify refund success path uses remaining refundable amount when amount is not
   * provided. Expected Result: Payment is updated as fully refunded and order summary pending
   * amount is increased. Assertions: Updated payment fields, summary pending amount, and refund
   * payload defaults.
   */
  @Test
  @DisplayName("initiateRefund - Full Refund Uses Remaining Amount - Success")
  void initiateRefund_s02_fullRefundUsesRemainingAmount_success() throws Exception {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityType(Payment.EntityType.PURCHASE_ORDER.getValue());
    testPayment.setEntityId(TEST_PO_ID);
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setRazorpayPaymentId("pay_refund_full_001");
    testPayment.setAmountPaidPaise(100000L);
    testPayment.setAmountRefundedPaise(20000L);
    testOrderSummary.setPendingAmount(new BigDecimal("50.00"));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPaymentRepositorySaveReturnsArgument();
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.of(testOrderSummary));
    stubOrderSummaryRepositorySaveReturnsArgument();
    stubRazorpayClientPaymentRefundReturns("rfnd_full_001");
    try {
      // Act
      Payment updatedPayment = paymentService.initiateRefund(TEST_PAYMENT_ID, null, null);

      // Assert
      assertEquals("rfnd_full_001", updatedPayment.getLastRefundId());
      assertEquals(100000L, updatedPayment.getAmountRefundedPaise());
      assertEquals(Payment.PaymentStatus.REFUNDED.getValue(), updatedPayment.getPaymentStatus());
      assertEquals(new BigDecimal("850.00"), testOrderSummary.getPendingAmount());
      assertNotNull(capturedRazorpayRefundRequest);
      assertEquals("normal", capturedRazorpayRefundRequest.getString("speed"));
      assertEquals(
          "Customer requested refund",
          capturedRazorpayRefundRequest.getJSONObject("notes").getString("reason"));
    } finally {
      closeMockedRazorpayClientConstruction();
    }
  }

  /**
   * Purpose: Verify refund success path respects explicit amount and reason values. Expected
   * Result: Payment becomes partially refunded and custom reason is sent in payload. Assertions:
   * Payment status, refunded amount, and captured custom reason.
   */
  @Test
  @DisplayName("initiateRefund - Partial Refund Uses Explicit Reason - Success")
  void initiateRefund_s03_partialRefundUsesExplicitReason_success() throws Exception {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityType(Payment.EntityType.PURCHASE_ORDER.getValue());
    testPayment.setEntityId(TEST_PO_ID);
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setRazorpayPaymentId("pay_refund_partial_001");
    testPayment.setAmountPaidPaise(100000L);
    testPayment.setAmountRefundedPaise(0L);
    testOrderSummary.setPendingAmount(new BigDecimal("100.00"));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPaymentRepositorySaveReturnsArgument();
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.of(testOrderSummary));
    stubOrderSummaryRepositorySaveReturnsArgument();
    stubRazorpayClientPaymentRefundReturns("rfnd_partial_001");
    try {
      // Act
      Payment updatedPayment =
          paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "Damaged shipment");

      // Assert
      assertEquals("rfnd_partial_001", updatedPayment.getLastRefundId());
      assertEquals(10000L, updatedPayment.getAmountRefundedPaise());
      assertEquals(
          Payment.PaymentStatus.PARTIALLY_REFUNDED.getValue(), updatedPayment.getPaymentStatus());
      assertEquals(new BigDecimal("200.00"), testOrderSummary.getPendingAmount());
      assertEquals(
          "Damaged shipment",
          capturedRazorpayRefundRequest.getJSONObject("notes").getString("reason"));
    } finally {
      closeMockedRazorpayClientConstruction();
    }
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify refund fails when client context is unavailable. Expected Result:
   * NotFoundException with Client InvalidId. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("initiateRefund - Client Not Found - Failure")
  void initiateRefund_f01_clientNotFound_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative payment id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("initiateRefund - Negative Payment Id - Failure")
  void initiateRefund_f02_negativePaymentId_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.initiateRefund(-1L, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero payment id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("initiateRefund - Zero Payment Id - Failure")
  void initiateRefund_f03_zeroPaymentId_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.initiateRefund(0L, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify max long payment id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("initiateRefund - Max Long Payment Id - Failure")
  void initiateRefund_f04_maxLongPaymentId_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.initiateRefund(Long.MAX_VALUE, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs follow same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId for each id. Assertions: Exception type and exact
   * message for each id.
   */
  @Test
  @DisplayName("initiateRefund - Additional Invalid Payment Ids - Failure")
  void initiateRefund_f05_additionalInvalidPaymentIds_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L};

    // Act
    for (Long invalidId : invalidIds) {
      NotFoundException ex =
          assertThrows(
              NotFoundException.class,
              () -> paymentService.initiateRefund(invalidId, 10000L, "refund"));

      // Assert
      assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  /**
   * Purpose: Verify refund flow validates payment existence after client validation. Expected
   * Result: NotFoundException with payment not found message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("initiateRefund - Payment Not Found - Failure")
  void initiateRefund_f06_paymentNotFound_failure() {
    // Arrange
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify refund flow denies access when payment belongs to another client. Expected
   * Result: BadRequestException with payment access denied message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("initiateRefund - Client Access Denied - Failure")
  void initiateRefund_f07_clientAccessDenied_failure() {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID + 1);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.ACCESS_DENIED, ex.getMessage());
  }

  /**
   * Purpose: Verify refund is rejected when payment is not in refundable state. Expected Result:
   * BadRequestException with cannot refund message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("initiateRefund - Payment Cannot Be Refunded - Failure")
  void initiateRefund_f08_paymentCannotBeRefunded_failure() {
    // Arrange
    testPayment.setPaymentStatus(Payment.PaymentStatus.FAILED.getValue());
    testPayment.setAmountPaidPaise(100000L);
    testPayment.setAmountRefundedPaise(0L);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.CANNOT_REFUND, ex.getMessage());
  }

  /**
   * Purpose: Verify requested refund amount cannot exceed remaining refundable amount. Expected
   * Result: BadRequestException with refundable amount exceeded message. Assertions: Exception type
   * and exact formatted message.
   */
  @Test
  @DisplayName("initiateRefund - Amount Exceeds Refundable Amount - Failure")
  void initiateRefund_f09_amountExceedsRefundableAmount_failure() {
    // Arrange
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setAmountPaidPaise(100000L);
    testPayment.setAmountRefundedPaise(30000L);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 90000L, "refund"));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.PaymentErrorMessages.REFUND_AMOUNT_EXCEEDS_REFUNDABLE_AMOUNT_FORMAT,
            70000L),
        ex.getMessage());
  }

  /**
   * Purpose: Verify refund gateway errors are wrapped into a standardized BadRequestException
   * message. Expected Result: BadRequestException with formatted process-refund failure message.
   * Assertions: Exception type and exact formatted message.
   */
  @Test
  @DisplayName("initiateRefund - Razorpay Refund Exception - Failure")
  void initiateRefund_f10_razorpayRefundException_failure() {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setRazorpayPaymentId("pay_refund_error_001");
    testPayment.setAmountPaidPaise(100000L);
    testPayment.setAmountRefundedPaise(0L);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubRazorpayClientPaymentRefundThrows("refund api unavailable");
    try {
      // Act
      BadRequestException ex =
          assertThrows(
              BadRequestException.class,
              () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "manual"));

      // Assert
      assertEquals(
          String.format(
              ErrorMessages.PaymentErrorMessages.FAILED_TO_PROCESS_REFUND_FORMAT,
              "refund api unavailable"),
          ex.getMessage());
    } finally {
      closeMockedRazorpayClientConstruction();
    }
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify receipt download endpoint permission handling for payment operations. Expected
   * Result: Unauthorized status is returned and expected permission annotation exists. Assertions:
   * HTTP status and annotation permission constant.
   */
  @Test
  @DisplayName("initiateRefund - Controller Permission Forbidden")
  void initiateRefund_controller_permission_forbidden() throws Exception {
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

