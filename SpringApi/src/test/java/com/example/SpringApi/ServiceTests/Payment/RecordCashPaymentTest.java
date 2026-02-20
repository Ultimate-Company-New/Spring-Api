package com.example.SpringApi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Tests for PaymentService.recordCashPayment(). */
@DisplayName("RecordCashPayment Tests")
class RecordCashPaymentTest extends PaymentServiceTestBase {

  // Total Tests: 17
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify purchase order repository interaction for cash payment flow. Expected Result:
   * purchaseOrderRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("recordCashPayment - Verify Purchase Order Repository Interaction - Success")
  void recordCashPayment_verifyPurchaseOrderRepositoryInteraction_success() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify recordCashPayment throws NotFoundException when purchase order is missing.
   * Expected Result: NotFoundException with purchase order invalid id message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("recordCashPayment - Purchase Order Not Found - Failure")
  void recordCashPayment_f01_purchaseOrderNotFound_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify recordCashPayment throws NullPointerException for null request. Expected
   * Result: NullPointerException. Assertions: Exception type.
   */
  @Test
  @DisplayName("recordCashPayment - Null Request - Failure")
  void recordCashPayment_f02_nullRequest_failure() {
    // Arrange
    CashPaymentRequestModel request = null;

    // Act
    NullPointerException ex =
        assertThrows(NullPointerException.class, () -> paymentService.recordCashPayment(request));

    // Assert
    assertNotNull(ex.getMessage());
  }

  /**
   * Purpose: Verify negative purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Negative Purchase Order Id - Failure")
  void recordCashPayment_f03_negativePurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(-1L);
    stubPurchaseOrderRepositoryFindById(-1L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Zero Purchase Order Id - Failure")
  void recordCashPayment_f04_zeroPurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(0L);
    stubPurchaseOrderRepositoryFindById(0L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify max long purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Max Long Purchase Order Id - Failure")
  void recordCashPayment_f05_maxLongPurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
    stubPurchaseOrderRepositoryFindById(Long.MAX_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify min long purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Min Long Purchase Order Id - Failure")
  void recordCashPayment_f06_minLongPurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(Long.MIN_VALUE);
    stubPurchaseOrderRepositoryFindById(Long.MIN_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs throw NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id for each id. Assertions: Exception type and
   * exact message for each id.
   */
  @Test
  @DisplayName("recordCashPayment - Additional Invalid Purchase Order Ids - Failure")
  void recordCashPayment_f07_additionalInvalidPurchaseOrderIds_failure() {
    // Arrange
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L, 7L, -100L, Long.MAX_VALUE - 1};

    // Act
    for (Long invalidId : invalidIds) {
      testCashPaymentRequest.setPurchaseOrderId(invalidId);
      stubPurchaseOrderRepositoryFindById(invalidId, Optional.empty());
      NotFoundException ex =
          assertThrows(
              NotFoundException.class,
              () -> paymentService.recordCashPayment(testCashPaymentRequest));

      // Assert
      assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  /**
   * Purpose: Verify recordCashPayment denies access when purchase order belongs to another client.
   * Expected Result: BadRequestException with access denied message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("recordCashPayment - Client Access Denied - Failure")
  void recordCashPayment_f08_clientAccessDenied_failure() {
    // Arrange
    testPurchaseOrder.setClientId(TEST_CLIENT_ID + 7);
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
  }

  /**
   * Purpose: Verify recordCashPayment allows only PENDING_APPROVAL status. Expected Result:
   * BadRequestException with pending-approval status message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Invalid Status For Cash Payment - Failure")
  void recordCashPayment_f09_invalidStatusForCashPayment_failure() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID, ex.getMessage());
  }

  /**
   * Purpose: Verify payment date is mandatory for manual cash payment recording. Expected Result:
   * BadRequestException with payment date required message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPayment - Missing Payment Date - Failure")
  void recordCashPayment_f10_missingPaymentDate_failure() {
    // Arrange
    testCashPaymentRequest.setPaymentDate(null);
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.PAYMENT_DATE_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify positive amount is mandatory for manual cash payment recording. Expected
   * Result: BadRequestException with valid payment amount required message. Assertions: Exception
   * type and exact message.
   */
  @Test
  @DisplayName("recordCashPayment - Invalid Amount - Failure")
  void recordCashPayment_f11_invalidAmount_failure() {
    // Arrange
    testCashPaymentRequest.setAmount(BigDecimal.ZERO);
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.VALID_PAYMENT_AMOUNT_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify payment amount cannot exceed pending amount from order summary. Expected
   * Result: BadRequestException with pending-amount exceeded message. Assertions: Exception type
   * and exact formatted message.
   */
  @Test
  @DisplayName("recordCashPayment - Amount Exceeds Pending Amount - Failure")
  void recordCashPayment_f12_amountExceedsPendingAmount_failure() {
    // Arrange
    testCashPaymentRequest.setAmount(new BigDecimal("900.00"));
    testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
        TEST_PO_ID,
        Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 20000L);

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPayment(testCashPaymentRequest));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.PaymentErrorMessages.PAYMENT_AMOUNT_EXCEEDS_PENDING_AMOUNT_FORMAT,
            new BigDecimal("900.00"),
            new BigDecimal("800.00")),
        ex.getMessage());
  }

  /**
   * Purpose: Verify UPI transaction id triggers UPI payment method and full-payment approval status
   * update. Expected Result: Success response with APPROVED status and captured payment saved as
   * UPI. Assertions: Response fields and saved payment method/test mode.
   */
  @Test
  @DisplayName("recordCashPayment - Upi Payment Full Approval - Success")
  void recordCashPayment_s02_upiPaymentFullApproval_success() {
    // Arrange
    testCashPaymentRequest.setAmount(new BigDecimal("500.00"));
    testCashPaymentRequest.setUpiTransactionId("upi-ref-123");
    testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
    stubEnvironmentActiveProfiles("development");
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
        TEST_PO_ID,
        Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrderSequence(TEST_PO_ID, 50000L, 100000L);
    stubPaymentRepositorySaveAssignsPaymentId(91001L);
    stubOrderSummaryRepositorySaveReturnsArgument();
    stubPurchaseOrderRepositorySaveReturnsArgument();

    // Act
    PaymentVerificationResponseModel response =
        paymentService.recordCashPayment(testCashPaymentRequest);

    // Assert
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    assertEquals(Payment.PaymentMethod.UPI.getValue(), savedPayment.getPaymentMethod());
    assertTrue(Boolean.TRUE.equals(savedPayment.getIsTestPayment()));
    assertTrue(response.isSuccess());
    assertEquals(PurchaseOrder.Status.APPROVED.getValue(), response.getPurchaseOrderStatus());
    assertEquals(new BigDecimal("0.00"), testOrderSummary.getPendingAmount());
  }

  /**
   * Purpose: Verify blank UPI transaction id falls back to CASH method and partial status update.
   * Expected Result: Success response with APPROVED_WITH_PARTIAL_PAYMENT and CASH method.
   * Assertions: Response status and saved payment method.
   */
  @Test
  @DisplayName("recordCashPayment - Cash Method Partial Approval - Success")
  void recordCashPayment_s03_cashMethodPartialApproval_success() {
    // Arrange
    testCashPaymentRequest.setAmount(new BigDecimal("200.00"));
    testCashPaymentRequest.setUpiTransactionId("   ");
    testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
    stubEnvironmentActiveProfiles("production");
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
        TEST_PO_ID,
        Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrderSequence(TEST_PO_ID, 20000L, 40000L);
    stubPaymentRepositorySaveAssignsPaymentId(91002L);
    stubOrderSummaryRepositorySaveReturnsArgument();
    stubPurchaseOrderRepositorySaveReturnsArgument();

    // Act
    PaymentVerificationResponseModel response =
        paymentService.recordCashPayment(testCashPaymentRequest);

    // Assert
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    assertEquals(Payment.PaymentMethod.CASH.getValue(), savedPayment.getPaymentMethod());
    assertFalse(Boolean.TRUE.equals(savedPayment.getIsTestPayment()));
    assertTrue(response.isSuccess());
    assertEquals(
        PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue(),
        response.getPurchaseOrderStatus());
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify controller returns unauthorized status when service throws
   * UnauthorizedException. Expected Result: HTTP 401 UNAUTHORIZED. Assertions: HTTP status is
   * UNAUTHORIZED.
   */
  @Test
  @DisplayName("recordCashPayment - Controller Permission Forbidden")
  void recordCashPayment_p01_controller_permission_forbidden() {
    // Arrange
    stubPaymentServiceRecordCashPaymentThrowsUnauthorized();

    // Act
    ResponseEntity<?> response =
        paymentControllerWithMock.recordCashPayment(testCashPaymentRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates cash payment request to service. Expected Result: HTTP 200
   * OK with delegated service call. Assertions: Service interaction and response code.
   */
  @Test
  @DisplayName("recordCashPayment - Controller Delegates To Service - Success")
  void recordCashPayment_p03_controllerDelegatesToService_success() {
    // Arrange
    stubPaymentServiceRecordCashPayment(createSuccessPaymentVerificationResponse());

    // Act
    ResponseEntity<?> response =
        paymentControllerWithMock.recordCashPayment(testCashPaymentRequest);

    // Assert
    verify(paymentServiceMock, times(1)).recordCashPayment(testCashPaymentRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
