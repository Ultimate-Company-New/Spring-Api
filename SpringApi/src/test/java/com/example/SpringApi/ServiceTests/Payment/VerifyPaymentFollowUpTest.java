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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for PaymentService.verifyPaymentFollowUp().
 */
@DisplayName("VerifyPaymentFollowUp Tests")
class VerifyPaymentFollowUpTest extends PaymentServiceTestBase {

    // Total Tests: 15
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository interaction is performed during follow-up verification.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Verify Client Repository Interaction - Success")
    void verifyPaymentFollowUp_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify follow-up verification fails when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Client Not Found - Failure")
    void verifyPaymentFollowUp_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Negative Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Zero Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Max Long Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs all follow same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Additional Invalid Purchase Order Ids - Failure")
    void verifyPaymentFollowUp_f05_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            testVerifyRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
        }
    }

    /**
     * Purpose: Verify follow-up verification rejects purchase orders from other clients.
     * Expected Result: BadRequestException with access denied message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Client Access Denied - Failure")
    void verifyPaymentFollowUp_f06_clientAccessDenied_failure() {
        // Arrange
        testPurchaseOrder.setClientId(TEST_CLIENT_ID + 33);
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
    }

    /**
     * Purpose: Verify follow-up verification enforces APPROVED/APPROVED_WITH_PARTIAL_PAYMENT status.
     * Expected Result: BadRequestException with follow-up status validation message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Invalid Status For Follow Up - Failure")
    void verifyPaymentFollowUp_f07_invalidStatusForFollowUp_failure() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.PENDING_APPROVAL.getValue());
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.FOLLOW_UP_PAYMENT_STATUS_REQUIRED, ex.getMessage());
    }

    /**
     * Purpose: Verify follow-up verification validates purchase order existence after client lookup.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Purchase Order Missing - Failure")
    void verifyPaymentFollowUp_f08_purchaseOrderMissing_failure() {
        // Arrange
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify follow-up verification fails when payment order lookup fails.
     * Expected Result: BadRequestException with payment order not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Payment Order Not Found - Failure")
    void verifyPaymentFollowUp_f08_paymentOrderNotFound_failure() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.empty());

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.PAYMENT_ORDER_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid follow-up signature marks payment as failed and returns failure response.
     * Expected Result: Response success=false and payment status FAILED.
     * Assertions: Response fields and payment status.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Invalid Signature Marks Failed - Failure")
    void verifyPaymentFollowUp_f09_invalidSignatureMarksFailed_failure() {
        // Arrange
        testPayment.setPaymentStatus(Payment.PaymentStatus.CREATED.getValue());
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();

        // Act
        PaymentVerificationResponseModel response = paymentService.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals(Payment.PaymentStatus.FAILED.getValue(), testPayment.getPaymentStatus());
        assertEquals("Payment verification failed: Invalid signature", response.getMessage());
    }

    /**
     * Purpose: Verify valid follow-up payment updates order summary and keeps follow-up status logic.
     * Expected Result: Success response with APPROVED_WITH_PARTIAL_PAYMENT.
     * Assertions: Response status and pending amount.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Valid Signature Updates Partial Status - Success")
    void verifyPaymentFollowUp_s02_validSignatureUpdatesPartialStatus_success() {
        // Arrange
        String signature = createValidRazorpaySignature(
                testVerifyRequest.getRazorpayOrderId(),
                testVerifyRequest.getRazorpayPaymentId(),
                testClient.getRazorpayApiSecret());
        testVerifyRequest.setRazorpaySignature(signature);
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
        stubPaymentRepositoryFindByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId(), Optional.of(testPayment));
        stubPaymentRepositorySaveReturnsArgument();
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
                OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                TEST_PO_ID,
                Optional.of(testOrderSummary));
        stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(TEST_PO_ID, 50000L);
        stubOrderSummaryRepositorySaveReturnsArgument();
        stubPurchaseOrderRepositorySaveReturnsArgument();

        // Act
        PaymentVerificationResponseModel response = paymentService.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue(), response.getPurchaseOrderStatus());
        assertEquals(new BigDecimal("500.00"), testOrderSummary.getPendingAmount());
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
    @DisplayName("verifyPaymentFollowUp - Controller Permission Forbidden")
    void verifyPaymentFollowUp_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceVerifyPaymentFollowUpThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    /**
     * Purpose: Verify controller delegates follow-up verification request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Controller Delegates To Service - Success")
    void verifyPaymentFollowUp_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceVerifyPaymentFollowUp(createSuccessPaymentVerificationResponse());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        verify(paymentServiceMock, times(1)).verifyPaymentFollowUp(testVerifyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
