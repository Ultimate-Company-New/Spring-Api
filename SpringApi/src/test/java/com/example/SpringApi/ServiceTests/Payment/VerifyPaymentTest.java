package com.example.SpringApi.ServiceTests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for PaymentService.verifyPayment().
 */
@DisplayName("VerifyPayment Tests")
class VerifyPaymentTest extends PaymentServiceTestBase {

    // Total Tests: 17
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository interaction is performed during verifyPayment.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("verifyPayment - Verify Client Repository Interaction - Success")
    void verifyPayment_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify verifyPayment throws NotFoundException when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Client Not Found - Failure")
    void verifyPayment_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Negative Purchase Order Id - Failure")
    void verifyPayment_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Zero Purchase Order Id - Failure")
    void verifyPayment_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Max Long Purchase Order Id - Failure")
    void verifyPayment_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs all follow the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("verifyPayment - Additional Invalid Purchase Order Ids - Failure")
    void verifyPayment_f05_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            testVerifyRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
        }
    }

    /**
     * Purpose: Verify verifyPayment fails when purchase order belongs to another client.
     * Expected Result: BadRequestException with access denied message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Client Access Denied - Failure")
    void verifyPayment_f06_clientAccessDenied_failure() {
        // Arrange
        testPurchaseOrder.setClientId(TEST_CLIENT_ID + 50);
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
    }

    /**
     * Purpose: Verify verifyPayment validates purchase order existence after resolving client credentials.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Purchase Order Missing - Failure")
    void verifyPayment_f07_purchaseOrderMissing_failure() {
        // Arrange
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify verifyPayment fails when tracked payment order is not found.
     * Expected Result: BadRequestException with payment order not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Payment Order Not Found - Failure")
    void verifyPayment_f07_paymentOrderNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.empty());

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.PAYMENT_ORDER_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid signature marks payment as failed and returns failure response.
     * Expected Result: Response success=false and payment status FAILED.
     * Assertions: Response fields and updated payment state.
     */
    @Test
    @DisplayName("verifyPayment - Invalid Signature Marks Failed - Failure")
    void verifyPayment_f08_invalidSignatureMarksFailed_failure() {
        // Arrange
        testPayment.setPaymentStatus(Payment.PaymentStatus.CREATED.getValue());
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();

        // Act
        PaymentVerificationResponseModel response = paymentService.verifyPayment(testVerifyRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Payment verification failed: Invalid signature", response.getMessage());
        assertEquals(Payment.PaymentStatus.FAILED.getValue(), testPayment.getPaymentStatus());
        verify(paymentRepository, times(1)).save(testPayment);
    }

    /**
     * Purpose: Verify valid signature captures payment and marks purchase order as APPROVED when fully paid.
     * Expected Result: Success response with APPROVED status and zero pending amount.
     * Assertions: Response fields and updated entities.
     */
    @Test
    @DisplayName("verifyPayment - Valid Signature Full Payment Updates Approved - Success")
    void verifyPayment_s02_validSignatureFullPaymentUpdatesApproved_success() {
        // Arrange
        String signature = createValidRazorpaySignature(
                testVerifyRequest.getRazorpayOrderId(),
                testVerifyRequest.getRazorpayPaymentId(),
                testClient.getRazorpayApiSecret());
        testVerifyRequest.setRazorpaySignature(signature);
        testPayment.setPaymentStatus(Payment.PaymentStatus.CREATED.getValue());
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
                OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                TEST_PO_ID,
                Optional.of(testOrderSummary));
        stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 100000L);
        stubOrderSummaryRepositorySaveReturnsArgument();
        stubPurchaseOrderRepositorySaveReturnsArgument();

        // Act
        PaymentVerificationResponseModel response = paymentService.verifyPayment(testVerifyRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(PurchaseOrder.Status.APPROVED.getValue(), response.getPurchaseOrderStatus());
        assertEquals(Payment.PaymentStatus.CAPTURED.getValue(), testPayment.getPaymentStatus());
        assertEquals(new BigDecimal("0.00"), testOrderSummary.getPendingAmount());
        verify(orderSummaryRepository, times(1)).save(testOrderSummary);
        verify(purchaseOrderRepository, times(1)).save(testPurchaseOrder);
    }

    /**
     * Purpose: Verify partial net paid amount marks purchase order as APPROVED_WITH_PARTIAL_PAYMENT.
     * Expected Result: Success response with partial-payment status and non-zero pending amount.
     * Assertions: Response status and pending amount.
     */
    @Test
    @DisplayName("verifyPayment - Valid Signature Partial Payment Updates Partial Status - Success")
    void verifyPayment_s03_validSignaturePartialPaymentUpdatesPartialStatus_success() {
        // Arrange
        String signature = createValidRazorpaySignature(
                testVerifyRequest.getRazorpayOrderId(),
                testVerifyRequest.getRazorpayPaymentId(),
                testClient.getRazorpayApiSecret());
        testVerifyRequest.setRazorpaySignature(signature);
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
                OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                TEST_PO_ID,
                Optional.of(testOrderSummary));
        stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 60000L);
        stubOrderSummaryRepositorySaveReturnsArgument();
        stubPurchaseOrderRepositorySaveReturnsArgument();

        // Act
        PaymentVerificationResponseModel response = paymentService.verifyPayment(testVerifyRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue(), response.getPurchaseOrderStatus());
        assertEquals(new BigDecimal("400.00"), testOrderSummary.getPendingAmount());
    }

    /**
     * Purpose: Verify overpayment condition throws exception when total paid exceeds grand total.
     * Expected Result: BadRequestException with formatted overpayment message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Total Paid Exceeds Grand Total - Failure")
    void verifyPayment_f09_totalPaidExceedsGrandTotal_failure() {
        // Arrange
        String signature = createValidRazorpaySignature(
                testVerifyRequest.getRazorpayOrderId(),
                testVerifyRequest.getRazorpayPaymentId(),
                testClient.getRazorpayApiSecret());
        testVerifyRequest.setRazorpaySignature(signature);
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
                OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                TEST_PO_ID,
                Optional.of(testOrderSummary));
        stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 120000L);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.PaymentErrorMessages.PAYMENT_AMOUNT_EXCEEDS_GRAND_TOTAL_FORMAT,
                new BigDecimal("1200"),
                new BigDecimal("1000.00")), ex.getMessage());
    }

    /**
     * Purpose: Verify signature helper safely returns false when secret is invalid/null.
     * Expected Result: Signature verification returns false without propagating exception.
     * Assertions: Return value is false.
     */
    @Test
    @DisplayName("verifyPayment - Signature Helper Handles Null Secret - Failure")
    void verifyPayment_f10_signatureHelperHandlesNullSecret_failure() {
        // Arrange

        // Act
        Boolean result = ReflectionTestUtils.invokeMethod(
                paymentService,
                "verifyRazorpaySignature",
                "order_abc",
                "pay_abc",
                "signature",
                null);

        // Assert
        assertNotNull(result);
        assertFalse(result);
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller returns unauthorized status when service throws UnauthorizedException.
     * Expected Result: HTTP 401 UNAUTHORIZED.
     * Assertions: HTTP status is UNAUTHORIZED.
     */
    @Test
    @DisplayName("verifyPayment - Controller Permission Forbidden")
    void verifyPayment_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceVerifyPaymentThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPayment(testVerifyRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("verifyPayment - Controller PreAuthorize Annotation - Success")
    void verifyPayment_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("verifyPayment", RazorpayVerifyRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates verifyPayment request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("verifyPayment - Controller Delegates To Service - Success")
    void verifyPayment_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceVerifyPayment(createSuccessPaymentVerificationResponse());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPayment(testVerifyRequest);

        // Assert
        verify(paymentServiceMock, times(1)).verifyPayment(testVerifyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
