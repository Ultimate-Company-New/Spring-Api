package com.example.springapi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.databasemodels.PurchaseOrder;
import com.example.springapi.models.responsemodels.RazorpayOrderResponseModel;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Tests for PaymentService.createOrder(). */
@DisplayName("CreateOrder Tests")
class CreateOrderTest extends PaymentServiceTestBase {

  // Total Tests: 18
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify client repository is called for createOrder flow. Expected Result:
   * clientRepository.findById is called exactly once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("createOrder - Verify Client Repository Interaction - Success")
  void createOrder_verifyClientRepositoryInteraction_success() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    verify(clientRepository, times(1)).findById(any());
  }

  /**
   * Purpose: Verify createOrder completes end-to-end with amount fallback, customer prefill, and
   * saved payment metadata. Expected Result: Successful response with Razorpay order details and
   * prefill values. Assertions: Response values, saved payment data, and Razorpay payload notes.
   */
  @Test
  @DisplayName("createOrder - Summary Fallback And Prefill - Success")
  void createOrder_s02_summaryFallbackAndPrefill_success() throws Exception {
    // Arrange
    testOrderRequest.setAmount(BigDecimal.ZERO);
    testOrderRequest.setCustomerName("Alice Vendor");
    testOrderRequest.setCustomerEmail("alice@example.com");
    testOrderRequest.setCustomerPhone("9999999999");
    testClient.setName(null);
    stubEnvironmentActiveProfiles("development");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.of(testOrderSummary));
    stubPaymentRepositorySaveAssignsPaymentId(9001L);
    stubRazorpayClientOrderCreateReturns("order_success_001");
    try {
      // Act
      RazorpayOrderResponseModel response = paymentService.createOrder(testOrderRequest);

      // Assert
      ArgumentCaptor<com.example.springapi.models.databasemodels.Payment> paymentCaptor =
          ArgumentCaptor.forClass(com.example.springapi.models.databasemodels.Payment.class);
      verify(paymentRepository, times(1)).save(paymentCaptor.capture());
      com.example.springapi.models.databasemodels.Payment savedPayment = paymentCaptor.getValue();
      assertEquals("order_success_001", response.getOrderId());
      assertEquals(new BigDecimal("1000.00"), response.getAmount());
      assertEquals(100000L, response.getAmountInPaise());
      assertEquals("Ultimate Company", response.getCompanyName());
      assertEquals("Alice Vendor", response.getPrefillName());
      assertEquals("alice@example.com", response.getPrefillEmail());
      assertEquals("9999999999", response.getPrefillPhone());
      assertEquals("Alice Vendor", savedPayment.getCustomerName());
      assertEquals("alice@example.com", savedPayment.getCustomerEmail());
      assertEquals("9999999999", savedPayment.getCustomerPhone());
      assertTrue(Boolean.TRUE.equals(savedPayment.getIsTestPayment()));
      assertNotNull(capturedRazorpayOrderCreateRequest);
      assertEquals(
          TEST_PO_ID.toString(),
          capturedRazorpayOrderCreateRequest.getJSONObject("notes").getString("purchase_order_id"));
    } finally {
      closeMockedRazorpayClientConstruction();
    }
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify createOrder throws NotFoundException when client context is unavailable.
   * Expected Result: NotFoundException with Client InvalidId message. Assertions: Exception type
   * and exact message.
   */
  @Test
  @DisplayName("createOrder - Client Not Found - Failure")
  void createOrder_f01_clientNotFound_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder handles negative purchase order id while client context is
   * unavailable. Expected Result: NotFoundException with Client InvalidId message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrder - Negative Purchase Order Id - Failure")
  void createOrder_f02_negativePurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(-1L);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder handles zero purchase order id while client context is unavailable.
   * Expected Result: NotFoundException with Client InvalidId message. Assertions: Exception type
   * and exact message.
   */
  @Test
  @DisplayName("createOrder - Zero Purchase Order Id - Failure")
  void createOrder_f03_zeroPurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(0L);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder handles max long purchase order id while client context is
   * unavailable. Expected Result: NotFoundException with Client InvalidId message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrder - Max Long Purchase Order Id - Failure")
  void createOrder_f04_maxLongPurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder handles min long purchase order id while client context is
   * unavailable. Expected Result: NotFoundException with Client InvalidId message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrder - Min Long Purchase Order Id - Failure")
  void createOrder_f05_minLongPurchaseOrderId_failure() {
    // Arrange
    testOrderRequest.setPurchaseOrderId(Long.MIN_VALUE);
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify additional invalid IDs all resolve to the same client-not-found path. Expected
   * Result: Each id throws NotFoundException with Client InvalidId. Assertions: Exception type and
   * exact message for each id.
   */
  @Test
  @DisplayName("createOrder - Additional Invalid Purchase Order Ids - Failure")
  void createOrder_f06_additionalInvalidPurchaseOrderIds_failure() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());
    Long[] invalidIds = new Long[] {2L, 3L, 4L, 5L, 6L, 7L};

    // Act
    for (Long invalidId : invalidIds) {
      testOrderRequest.setPurchaseOrderId(invalidId);
      NotFoundException ex =
          assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

      // Assert
      assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }
  }

  /**
   * Purpose: Verify createOrder fails when purchase order does not belong to current client.
   * Expected Result: BadRequestException with access denied message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("createOrder - Client Access Denied - Failure")
  void createOrder_f07_clientAccessDenied_failure() {
    // Arrange
    testPurchaseOrder.setClientId(TEST_CLIENT_ID + 10);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder allows payment only for PENDING_APPROVAL status. Expected Result:
   * BadRequestException with status validation message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("createOrder - Invalid Status For Payment - Failure")
  void createOrder_f08_invalidStatusForPayment_failure() {
    // Arrange
    testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder falls back to order summary amount and fails when summary is
   * missing. Expected Result: BadRequestException with order summary not found message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrder - Amount Fallback Order Summary Missing - Failure")
  void createOrder_f09_amountFallbackOrderSummaryMissing_failure() {
    // Arrange
    testOrderRequest.setAmount(BigDecimal.ZERO);
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubOrderSummaryRepositoryFindByPurchaseOrderId(TEST_PO_ID, Optional.empty());

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder validates client Razorpay API key before proceeding. Expected
   * Result: BadRequestException with API key not configured message. Assertions: Exception type and
   * exact message.
   */
  @Test
  @DisplayName("createOrder - Razorpay Api Key Missing - Failure")
  void createOrder_f10_razorpayApiKeyMissing_failure() {
    // Arrange
    testClient.setRazorpayApiKey(" ");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_KEY_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder validates client Razorpay API secret before proceeding. Expected
   * Result: BadRequestException with API secret not configured message. Assertions: Exception type
   * and exact message.
   */
  @Test
  @DisplayName("createOrder - Razorpay Api Secret Missing - Failure")
  void createOrder_f11_razorpayApiSecretMissing_failure() {
    // Arrange
    testClient.setRazorpayApiSecret(" ");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_SECRET_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder validates purchase order existence after client validation.
   * Expected Result: NotFoundException with purchase order invalid id message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("createOrder - Purchase Order Missing - Failure")
  void createOrder_f12_purchaseOrderMissing_failure() {
    // Arrange
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify createOrder wraps Razorpay SDK errors using the standardized failure format.
   * Expected Result: BadRequestException with formatted create-order failure message. Assertions:
   * Exception type and exact formatted message.
   */
  @Test
  @DisplayName("createOrder - Razorpay Order Create Exception - Failure")
  void createOrder_f13_razorpayOrderCreateException_failure() {
    // Arrange
    testOrderRequest.setAmount(new BigDecimal("250.00"));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubRazorpayClientOrderCreateThrows("gateway temporarily unavailable");
    try {
      // Act
      BadRequestException ex =
          assertThrows(
              BadRequestException.class, () -> paymentService.createOrder(testOrderRequest));

      // Assert
      assertEquals(
          String.format(
              ErrorMessages.PaymentErrorMessages.FAILED_TO_CREATE_RAZORPAY_ORDER_FORMAT,
              "gateway temporarily unavailable"),
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
  @DisplayName("createOrder - Controller Permission Forbidden")
  void createOrder_p01_controller_permission_forbidden() {
    // Arrange
    stubPaymentServiceCreateOrderThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.createOrder(testOrderRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates createOrder request to service. Expected Result: HTTP 200
   * OK with delegated service call. Assertions: Service interaction and response code.
   */
  @Test
  @DisplayName("createOrder - Controller Delegates To Service - Success")
  void createOrder_p03_controllerDelegatesToService_success() {
    // Arrange
    stubPaymentServiceCreateOrder(new RazorpayOrderResponseModel());

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.createOrder(testOrderRequest);

    // Assert
    verify(paymentServiceMock, times(1)).createOrder(testOrderRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
