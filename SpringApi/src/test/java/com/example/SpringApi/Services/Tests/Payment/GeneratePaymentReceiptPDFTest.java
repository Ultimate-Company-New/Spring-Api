package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for PaymentService.generatePaymentReceiptPDF().
 */
@DisplayName("GeneratePaymentReceiptPDF Tests")
class GeneratePaymentReceiptPDFTest extends PaymentServiceTestBase {

    // Total Tests: 7
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify payment repository interaction for receipt generation.
     * Expected Result: paymentRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Verify Payment Repository Interaction - Success")
    void generatePaymentReceiptPDF_s01_verifyPaymentRepositoryInteraction_success() {
        // Arrange
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
        verify(paymentRepository, times(1)).findById(TEST_PAYMENT_ID);
    }

    /**
     * Purpose: Verify controller delegates receipt generation endpoint to service.
     * Expected Result: HTTP 200 OK with PDF byte[] response.
     * Assertions: Service interaction and response status/body.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Controller Delegates To Service - Success")
    void generatePaymentReceiptPDF_s02_controllerDelegatesToService_success() throws Exception {
        // Arrange
        byte[] bytes = new byte[] { 1, 2, 3 };
        stubPaymentServiceGeneratePaymentReceiptPDF(bytes);

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.downloadPaymentReceipt(TEST_PAYMENT_ID);

        // Assert
        verify(paymentServiceMock, times(1)).generatePaymentReceiptPDF(TEST_PAYMENT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(bytes, (byte[]) response.getBody());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify missing payment throws NotFoundException in receipt generation.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Payment Not Found - Failure")
    void generatePaymentReceiptPDF_f01_paymentNotFound_failure() {
        // Arrange
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify negative payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Negative Payment Id - Failure")
    void generatePaymentReceiptPDF_f02_negativePaymentId_failure() {
        // Arrange
        stubPaymentRepositoryFindById(-1L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.generatePaymentReceiptPDF(-1L));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify zero payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Zero Payment Id - Failure")
    void generatePaymentReceiptPDF_f03_zeroPaymentId_failure() {
        // Arrange
        stubPaymentRepositoryFindById(0L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.generatePaymentReceiptPDF(0L));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify access denied when payment belongs to a different client.
     * Expected Result: BadRequestException with payment access denied message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Client Access Denied - Failure")
    void generatePaymentReceiptPDF_f04_clientAccessDenied_failure() {
        // Arrange
        testPayment.setClientId(TEST_CLIENT_ID + 1);
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.ACCESS_DENIED, ex.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify receipt download endpoint requires view purchase orders permission.
     * Expected Result: @PreAuthorize is present with VIEW_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and contains permission constant.
     */
    @Test
    @DisplayName("generatePaymentReceiptPDF - Controller Permission Forbidden")
    void generatePaymentReceiptPDF_controller_permission_forbidden() throws Exception {
        // Arrange
        Method method = PaymentController.class.getMethod("downloadPaymentReceipt", Long.class);
        stubPaymentServiceGeneratePaymentReceiptPDFThrowsUnauthorized();

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        ResponseEntity<?> response = paymentControllerWithMock.downloadPaymentReceipt(TEST_PAYMENT_ID);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
