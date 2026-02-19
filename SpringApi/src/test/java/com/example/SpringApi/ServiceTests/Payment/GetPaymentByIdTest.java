package com.example.SpringApi.ServiceTests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for PaymentService.getPaymentById().
 */
@DisplayName("GetPaymentById Tests")
class GetPaymentByIdTest extends PaymentServiceTestBase {

    // Total Tests: 10
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify payment repository is queried with the requested payment id.
     * Expected Result: paymentRepository.findById is called exactly once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("getPaymentById - Verify Payment Repository Interaction - Success")
    void getPaymentById_verifyPaymentRepositoryInteraction_success() {
        // Arrange
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
        verify(paymentRepository, times(1)).findById(TEST_PAYMENT_ID);
    }

    /**
     * Purpose: Verify service returns payment details when payment exists and belongs to current client.
     * Expected Result: Returned payment matches repository entity.
     * Assertions: Payment id and client id.
     */
    @Test
    @DisplayName("getPaymentById - Returns Payment Details - Success")
    void getPaymentById_s02_returnsPaymentDetails_success() {
        // Arrange
        testPayment.setClientId(TEST_CLIENT_ID);
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

        // Act
        Payment result = paymentService.getPaymentById(TEST_PAYMENT_ID);

        // Assert
        assertEquals(TEST_PAYMENT_ID, result.getPaymentId());
        assertEquals(TEST_CLIENT_ID, result.getClientId());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify missing payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Payment Not Found - Failure")
    void getPaymentById_f01_paymentNotFound_failure() {
        // Arrange
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify negative payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Negative Id - Failure")
    void getPaymentById_f02_negativeId_failure() {
        // Arrange
        stubPaymentRepositoryFindById(-1L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(-1L));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify zero payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Zero Id - Failure")
    void getPaymentById_f03_zeroId_failure() {
        // Arrange
        stubPaymentRepositoryFindById(0L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(0L));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify max long payment id throws NotFoundException.
     * Expected Result: NotFoundException with payment not found message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Max Long Id - Failure")
    void getPaymentById_f04_maxLongId_failure() {
        // Arrange
        stubPaymentRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify access-denied scenario when payment belongs to a different client.
     * Expected Result: BadRequestException with payment access denied message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Client Access Denied - Failure")
    void getPaymentById_f05_clientAccessDenied_failure() {
        // Arrange
        testPayment.setClientId(TEST_CLIENT_ID + 1);
        stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class, () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.ACCESS_DENIED, ex.getMessage());
    }

    /**
     * Purpose: Verify multiple calls independently invoke payment repository.
     * Expected Result: NotFoundException for each call and two repository invocations.
     * Assertions: Exception type and repository invocation count.
     */
    @Test
    @DisplayName("getPaymentById - Multiple Calls - Failure")
    void getPaymentById_f06_multipleCalls_failure() {
        // Arrange
        stubPaymentRepositoryFindByIdAny(Optional.empty());

        // Act
        NotFoundException exOne = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(1L));
        NotFoundException exTwo = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(2L));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, exOne.getMessage());
        assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, exTwo.getMessage());
        verify(paymentRepository, times(2)).findById(anyLong());
    }

    /**
     * Purpose: Verify additional invalid ids throw NotFoundException.
     * Expected Result: NotFoundException with payment not found message for each id.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("getPaymentById - Additional Invalid Ids - Failure")
    void getPaymentById_f07_additionalInvalidIds_failure() {
        // Arrange
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L, -100L, Long.MIN_VALUE };

        // Act
        for (Long invalidId : invalidIds) {
            stubPaymentRepositoryFindById(invalidId, Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(invalidId));

            // Assert
            assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
        }
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify receipt download endpoint has expected permission annotation for payment-id based access.
     * Expected Result: Annotation exists and contains VIEW_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("getPaymentById - Controller Permission Forbidden")
    void getPaymentById_controller_permission_forbidden() throws Exception {
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
