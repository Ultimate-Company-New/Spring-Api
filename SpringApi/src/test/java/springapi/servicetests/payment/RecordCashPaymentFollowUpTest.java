package springapi.servicetests.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.models.databasemodels.OrderSummary;
import springapi.models.databasemodels.Payment;
import springapi.models.databasemodels.PurchaseOrder;
import springapi.models.requestmodels.CashPaymentRequestModel;
import springapi.models.responsemodels.PaymentVerificationResponseModel;

/** Tests for PaymentService.recordCashPaymentFollowUp(). */
@DisplayName("RecordCashPaymentFollowUp Tests")
class RecordCashPaymentFollowUpTest extends PaymentServiceTestBase {

  // Total Tests: 17
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify purchase order repository interaction for follow-up cash payment flow. Expected
   * Result: purchaseOrderRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Verify Purchase Order Repository Interaction - Success")
  void recordCashPaymentFollowUp_verifyPurchaseOrderRepositoryInteraction_success() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify follow-up cash payment fails when purchase order is missing. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Purchase Order Not Found - Failure")
  void recordCashPaymentFollowUp_f01_purchaseOrderNotFound_failure() {
    // Arrange
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up cash payment throws NullPointerException for null request. Expected
   * Result: NullPointerException. Assertions: Exception type.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Null Request - Failure")
  void recordCashPaymentFollowUp_f02_nullRequest_failure() {
    // Arrange
    CashPaymentRequestModel request = null;

    // Act
    NullPointerException ex =
        assertThrows(
            NullPointerException.class, () -> paymentService.recordCashPaymentFollowUp(request));

    // Assert
    assertNotNull(ex.getMessage());
  }

  /**
   * Purpose: Verify negative purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Negative Purchase Order Id - Failure")
  void recordCashPaymentFollowUp_f03_negativePurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(-1L);
    stubPurchaseOrderRepositoryFindById(-1L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Zero Purchase Order Id - Failure")
  void recordCashPaymentFollowUp_f04_zeroPurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(0L);
    stubPurchaseOrderRepositoryFindById(0L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify max long purchase order id throws NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Max Long Purchase Order Id - Failure")
  void recordCashPaymentFollowUp_f05_maxLongPurchaseOrderId_failure() {
    // Arrange
    testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
    stubPurchaseOrderRepositoryFindById(Long.MAX_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs throw NotFoundException. Expected Result:
   * NotFoundException with purchase order invalid id for each id. Assertions: Exception type and
   * exact message for each id.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Additional Invalid Purchase Order Ids - Failure")
  void recordCashPaymentFollowUp_f06_additionalInvalidPurchaseOrderIds_failure() {
    // Arrange
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L, -100L};

    // Act
    for (Long invalidId : invalidIds) {
      testCashPaymentRequest.setPurchaseOrderId(invalidId);
      stubPurchaseOrderRepositoryFindById(invalidId, Optional.empty());
      NotFoundException ex =
          assertThrows(
              NotFoundException.class,
              () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

      // Assert
      assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  /**
   * Purpose: Verify follow-up cash payment denies access for purchase orders from other clients.
   * Expected Result: BadRequestException with access denied message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Client Access Denied - Failure")
  void recordCashPaymentFollowUp_f07_clientAccessDenied_failure() {
    // Arrange
    testPurchaseOrder.setClientId(TEST_CLIENT_ID + 30);
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up cash payment enforces allowed statuses. Expected Result:
   * BadRequestException with follow-up status validation message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Invalid Status For Follow Up - Failure")
  void recordCashPaymentFollowUp_f08_invalidStatusForFollowUp_failure() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.PENDING_APPROVAL.getValue());
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.FOLLOW_UP_PAYMENT_STATUS_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify payment date is mandatory for follow-up manual payment. Expected Result:
   * BadRequestException with payment date required message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Missing Payment Date - Failure")
  void recordCashPaymentFollowUp_f09_missingPaymentDate_failure() {
    // Arrange
    testCashPaymentRequest.setPaymentDate(null);
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.PAYMENT_DATE_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify positive amount is mandatory for follow-up manual payment. Expected Result:
   * BadRequestException with valid payment amount required message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Invalid Amount - Failure")
  void recordCashPaymentFollowUp_f10_invalidAmount_failure() {
    // Arrange
    testCashPaymentRequest.setAmount(BigDecimal.ZERO);
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.VALID_PAYMENT_AMOUNT_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up payment amount cannot exceed pending amount. Expected Result:
   * BadRequestException with formatted pending-amount exceeded message. Assertions: Exception type
   * and exact message.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Amount Exceeds Pending Amount - Failure")
  void recordCashPaymentFollowUp_f11_amountExceedsPendingAmount_failure() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(
        PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue());
    testCashPaymentRequest.setAmount(new BigDecimal("700.00"));
    testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
        TEST_PO_ID,
        Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 50000L);

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.PaymentErrorMessages.PAYMENT_AMOUNT_EXCEEDS_PENDING_AMOUNT_FORMAT,
            new BigDecimal("700.00"),
            new BigDecimal("500.00")),
        ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up UPI manual payment updates payment method and order status as fully
   * paid. Expected Result: Success response with APPROVED status and UPI payment method.
   * Assertions: Response status and saved payment details.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Upi Payment Full Approval - Success")
  void recordCashPaymentFollowUp_s02_upiPaymentFullApproval_success() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(
        PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue());
    testCashPaymentRequest.setAmount(new BigDecimal("300.00"));
    testCashPaymentRequest.setUpiTransactionId("upi-followup-01");
    testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
    stubEnvironmentActiveProfiles("dev");
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
        TEST_PO_ID,
        Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrderSequence(TEST_PO_ID, 70000L, 100000L);
    stubPaymentRepositorySaveAssignsPaymentId(92001L);
    stubOrderSummaryRepositorySaveReturnsArgument();
    stubPurchaseOrderRepositorySaveReturnsArgument();

    // Act
    PaymentVerificationResponseModel response =
        paymentService.recordCashPaymentFollowUp(testCashPaymentRequest);

    // Assert
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    assertEquals(Payment.PaymentMethod.UPI.getValue(), savedPayment.getPaymentMethod());
    assertEquals(Boolean.TRUE, savedPayment.getIsTestPayment());
    assertTrue(response.isSuccess());
    assertEquals(PurchaseOrder.Status.APPROVED.getValue(), response.getPurchaseOrderStatus());
  }

  /**
   * Purpose: Verify follow-up manual payment supports CASH method when UPI transaction id is
   * absent/blank. Expected Result: Success response with saved payment method CASH and no
   * order-summary update when summary is absent. Assertions: Saved payment method, test-payment
   * flag, and successful response.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Cash Payment Without Order Summary - Success")
  void recordCashPaymentFollowUp_s03_cashPaymentWithoutOrderSummary_success() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    testCashPaymentRequest.setAmount(new BigDecimal("150.00"));
    testCashPaymentRequest.setUpiTransactionId("   ");
    stubEnvironmentActiveProfiles("prod");
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
        OrderSummary.EntityType.PURCHASE_ORDER.getValue(), TEST_PO_ID, Optional.empty());
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 15000L);
    stubPaymentRepositorySaveAssignsPaymentId(93001L);

    // Act
    PaymentVerificationResponseModel response =
        paymentService.recordCashPaymentFollowUp(testCashPaymentRequest);

    // Assert
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    assertEquals(Payment.PaymentMethod.CASH.getValue(), savedPayment.getPaymentMethod());
    assertEquals(Boolean.FALSE, savedPayment.getIsTestPayment());
    assertTrue(response.isSuccess());
    assertEquals(PurchaseOrder.Status.APPROVED.getValue(), response.getPurchaseOrderStatus());
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
  @DisplayName("recordCashPaymentFollowUp - Controller Permission Forbidden")
  void recordCashPaymentFollowUp_p01_controller_permission_forbidden() {
    // Arrange
    stubPaymentServiceRecordCashPaymentFollowUpThrowsUnauthorized();

    // Act
    ResponseEntity<?> response =
        paymentControllerWithMock.recordCashPaymentFollowUp(testCashPaymentRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates follow-up cash payment request to service. Expected
   * Result: HTTP 200 OK with delegated service call. Assertions: Service interaction and response
   * code.
   */
  @Test
  @DisplayName("recordCashPaymentFollowUp - Controller Delegates To Service - Success")
  void recordCashPaymentFollowUp_p03_controllerDelegatesToService_success() {
    // Arrange
    stubPaymentServiceRecordCashPaymentFollowUp(createSuccessPaymentVerificationResponse());

    // Act
    ResponseEntity<?> response =
        paymentControllerWithMock.recordCashPaymentFollowUp(testCashPaymentRequest);

    // Assert
    verify(paymentServiceMock, times(1)).recordCashPaymentFollowUp(testCashPaymentRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
