package com.example.SpringApi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.ResponseModels.RazorpayOrderResponseModel;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Tests for PaymentService.createOrderFollowUp(). */
@DisplayName("CreateOrderFollowUp Tests")
class CreateOrderFollowUpTest extends PaymentServiceTestBase {

  // Total Tests: 17
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify client repository interaction occurs for follow-up order creation. Expected
   * Result: clientRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("createOrderFollowUp - Verify Client Repository Interaction - Success")
  void createOrderFollowUp_verifyClientRepositoryInteraction_success() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    verify(clientRepository, times(1)).findById(any());
  }

  /**
   * Purpose: Verify follow-up order creation computes pending amount from order summary and
   * existing payments. Expected Result: Successful response with pending amount and follow-up note
   * metadata. Assertions: Response values, saved payment, and Razorpay notes payload.
   */
  @Test
  @DisplayName("createOrderFollowUp - Pending Amount Fallback And Notes - Success")
  void createOrderFollowUp_s02_pendingAmountFallbackAndNotes_success() throws Exception {
    // Arrange
    testOrderRequest.setAmount(BigDecimal.ZERO);
    testOrderRequest.setCustomerName("Follow Up User");
    testOrderRequest.setCustomerEmail("followup@example.com");
    testOrderRequest.setCustomerPhone("8888888888");
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubEnvironmentActiveProfiles("localhost");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.of(testOrderSummary));
    stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 25000L);
    stubPaymentRepositorySaveAssignsPaymentId(9002L);
    stubRazorpayClientOrderCreateReturns("order_followup_001");
    try {
      // Act
      RazorpayOrderResponseModel response = paymentService.createOrderFollowUp(testOrderRequest);

      // Assert
      ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Payment> paymentCaptor =
          ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Payment.class);
      verify(paymentRepository, times(1)).save(paymentCaptor.capture());
      com.example.SpringApi.Models.DatabaseModels.Payment savedPayment = paymentCaptor.getValue();
      assertEquals("order_followup_001", response.getOrderId());
      assertEquals(new BigDecimal("750.00"), response.getAmount());
      assertEquals(75000L, response.getAmountInPaise());
      assertEquals("Follow Up User", response.getPrefillName());
      assertEquals("followup@example.com", response.getPrefillEmail());
      assertEquals("8888888888", response.getPrefillPhone());
      assertTrue(
          savedPayment.getDescription().startsWith("Follow-up payment for Purchase Order #"));
      assertNotNull(capturedRazorpayOrderCreateRequest);
      assertEquals(
          "true",
          capturedRazorpayOrderCreateRequest
              .getJSONObject("notes")
              .getString("is_follow_up_payment"));
    } finally {
      closeMockedRazorpayClientConstruction();
    }
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify follow-up order creation fails when client context is unavailable. Expected
   * Result: NotFoundException with Client InvalidId message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Client Not Found - Failure")
  void createOrderFollowUp_f01_clientNotFound_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative purchase order id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Negative Purchase Order Id - Failure")
  void createOrderFollowUp_f02_negativePurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(-1L);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero purchase order id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Zero Purchase Order Id - Failure")
  void createOrderFollowUp_f03_zeroPurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(0L);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify max long purchase order id follows same client-not-found path. Expected Result:
   * NotFoundException with Client InvalidId message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Max Long Purchase Order Id - Failure")
  void createOrderFollowUp_f04_maxLongPurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs follow same client-not-found path. Expected Result: All
   * IDs throw NotFoundException with Client InvalidId. Assertions: Exception type and exact message
   * for each ID.
   */
  @Test
  @DisplayName("createOrderFollowUp - Additional Invalid Purchase Order Ids - Failure")
  void createOrderFollowUp_f05_additionalInvalidPurchaseOrderIds_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L};

    // Act
    for (Long invalidId : invalidIds) {
      testOrderRequest.setPurchaseOrderId(invalidId);
      NotFoundException ex =
          assertThrows(
              NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

      // Assert
      assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  /**
   * Purpose: Verify createOrderFollowUp fails when purchase order belongs to another client.
   * Expected Result: BadRequestException with access denied message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Client Access Denied - Failure")
  void createOrderFollowUp_f06_clientAccessDenied_failure() {
    // Arrange
    testPurchaseOrder.setClientId(TEST_CLIENT_ID + 100);
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order can only be created for APPROVED or partial-payment statuses.
   * Expected Result: BadRequestException with follow-up status validation message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Invalid Status For Follow Up - Failure")
  void createOrderFollowUp_f07_invalidStatusForFollowUp_failure() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.PENDING_APPROVAL.getValue());
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.FOLLOW_UP_PAYMENT_STATUS_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order amount fallback fails when order summary is unavailable.
   * Expected Result: BadRequestException with order summary not found message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Amount Fallback Order Summary Missing - Failure")
  void createOrderFollowUp_f08_amountFallbackOrderSummaryMissing_failure() {
    // Arrange
    testOrderRequest.setAmount(BigDecimal.ZERO);
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.empty());

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order validates Razorpay API key before any further processing.
   * Expected Result: BadRequestException with API key validation message. Assertions: Exception
   * type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Razorpay Api Key Missing - Failure")
  void createOrderFollowUp_f09_razorpayApiKeyMissing_failure() {
    // Arrange
    testClient.setRazorpayApiKey("");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_KEY_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order validates Razorpay API secret before any further processing.
   * Expected Result: BadRequestException with API secret validation message. Assertions: Exception
   * type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Razorpay Api Secret Missing - Failure")
  void createOrderFollowUp_f10_razorpayApiSecretMissing_failure() {
    // Arrange
    testClient.setRazorpayApiSecret("");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_SECRET_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order validates purchase order existence after client validation.
   * Expected Result: NotFoundException with purchase order invalid id message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Purchase Order Missing - Failure")
  void createOrderFollowUp_f11_purchaseOrderMissing_failure() {
    // Arrange
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify follow-up order creation wraps Razorpay order creation exceptions consistently.
   * Expected Result: BadRequestException with formatted create-order failure message. Assertions:
   * Exception type and exact formatted message.
   */
  @Test
  @DisplayName("createOrderFollowUp - Razorpay Order Create Exception - Failure")
  void createOrderFollowUp_f12_razorpayOrderCreateException_failure() {
    // Arrange
    testOrderRequest.setAmount(new BigDecimal("200.00"));
    testPurchaseOrder.setPurchaseOrderStatus(
        PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue());
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubRazorpayClientOrderCreateThrows("follow-up gateway unavailable");
    try {
      // Act
      BadRequestException ex =
          assertThrows(
              BadRequestException.class,
              () -> paymentService.createOrderFollowUp(testOrderRequest));

      // Assert
      assertEquals(
          String.format(
              ErrorMessages.PaymentErrorMessages.FAILED_TO_CREATE_RAZORPAY_ORDER_FORMAT,
              "follow-up gateway unavailable"),
          ex.getMessage());
    } finally {
      closeMockedRazorpayClientConstruction();
    }
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
  @DisplayName("createOrderFollowUp - Controller Permission Forbidden")
  void createOrderFollowUp_p01_controller_permission_forbidden() {
    // Arrange
    stubPaymentServiceCreateOrderFollowUpThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.createOrderFollowUp(testOrderRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates follow-up create order request to service. Expected
   * Result: HTTP 200 OK with delegated service call. Assertions: Service interaction and response
   * code.
   */
  @Test
  @DisplayName("createOrderFollowUp - Controller Delegates To Service - Success")
  void createOrderFollowUp_p03_controllerDelegatesToService_success() {
    // Arrange
    stubPaymentServiceCreateOrderFollowUp(new RazorpayOrderResponseModel());

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.createOrderFollowUp(testOrderRequest);

    // Assert
    verify(paymentServiceMock, times(1)).createOrderFollowUp(testOrderRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

