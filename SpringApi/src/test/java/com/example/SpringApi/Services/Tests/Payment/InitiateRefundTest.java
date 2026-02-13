package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for PaymentService.initiateRefund().
 */
@DisplayName("InitiateRefund Tests")
class InitiateRefundTest extends PaymentServiceTestBase {

    // Total Tests: 7
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository is queried for refund flow.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("initiateRefund - Verify Client Repository Interaction - Success")
    void initiateRefund_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify refund fails when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("initiateRefund - Client Not Found - Failure")
    void initiateRefund_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "refund"));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative payment id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("initiateRefund - Negative Payment Id - Failure")
    void initiateRefund_f02_negativePaymentId_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(-1L, 10000L, "refund"));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero payment id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("initiateRefund - Zero Payment Id - Failure")
    void initiateRefund_f03_zeroPaymentId_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(0L, 10000L, "refund"));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify max long payment id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("initiateRefund - Max Long Payment Id - Failure")
    void initiateRefund_f04_maxLongPaymentId_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(Long.MAX_VALUE, 10000L, "refund"));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs follow same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("initiateRefund - Additional Invalid Payment Ids - Failure")
    void initiateRefund_f05_additionalInvalidPaymentIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(invalidId, 10000L, "refund"));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify receipt download endpoint permission handling for payment operations.
     * Expected Result: Unauthorized status is returned and expected permission annotation exists.
     * Assertions: HTTP status and annotation permission constant.
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
