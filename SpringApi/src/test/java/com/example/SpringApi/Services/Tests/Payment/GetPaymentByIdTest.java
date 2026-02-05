package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentService.getPaymentById() method.
 * Contains 14 tests covering various scenarios.
 */
@DisplayName("GetPaymentById Tests")
class GetPaymentByIdTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that getPaymentById throws NotFoundException when payment doesn't exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Payment NotFound error.
     */
    @Test
    @DisplayName("getPaymentById - Payment not found - Throws NotFoundException")
    void getPaymentById_PaymentNotFound_ThrowsNotFoundException() {
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentById throws NotFoundException for negative ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Payment NotFound error.
     */
    @Test
    @DisplayName("getPaymentById - Negative ID - Throws NotFoundException")
    void getPaymentById_NegativeId_ThrowsNotFoundException() {
        when(paymentRepository.findById(-1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(-1L));

        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentById throws NotFoundException for zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Payment NotFound error.
     */
    @Test
    @DisplayName("getPaymentById - Zero ID - Throws NotFoundException")
    void getPaymentById_ZeroId_ThrowsNotFoundException() {
        when(paymentRepository.findById(0L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(0L));

        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentById throws NotFoundException for Long.MAX_VALUE ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Payment NotFound error.
     */
    @Test
    @DisplayName("getPaymentById - Long.MAX_VALUE ID - Throws NotFoundException")
    void getPaymentById_MaxLongId_ThrowsNotFoundException() {
        when(paymentRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(Long.MAX_VALUE));

        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentById throws BadRequestException when client access denied.
     * Expected Result: BadRequestException is thrown (after payment found, client check fails).
     * Assertions: Exception message matches Payment AccessDenied error.
     */
    @Test
    @DisplayName("getPaymentById - Client access denied - Throws BadRequestException")
    void getPaymentById_ClientAccessDenied_ThrowsBadRequestException() {
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.getPaymentById(TEST_PAYMENT_ID));
        assertEquals(ErrorMessages.PaymentErrorMessages.AccessDenied, ex.getMessage());
    }

    /**
     * Purpose: Verify payment repository is called with correct ID.
     * Expected Result: Repository findById is called.
     * Assertions: verify repository findById is called once.
     */
    @Test
    @DisplayName("getPaymentById - Verify payment repository interaction")
    void getPaymentById_VerifyPaymentRepositoryInteraction() {
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

        verify(paymentRepository, times(1)).findById(TEST_PAYMENT_ID);
    }

    /**
     * Purpose: Verify multiple independent calls are handled correctly.
     * Expected Result: Each call is processed independently.
     * Assertions: Both calls throw NotFoundException.
     */
    @Test
    @DisplayName("getPaymentById - Multiple calls with different IDs")
    void getPaymentById_MultipleCalls() {
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(1L));
        assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(2L));

        verify(paymentRepository, times(2)).findById(anyLong());
    }

    /**
     * Purpose: Additional invalid payment ID coverage.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Payment NotFound.
     */
    @TestFactory
    @DisplayName("getPaymentById - Additional invalid IDs")
    Stream<DynamicTest> getPaymentById_AdditionalInvalidIds() {
        return Stream.of(2L, 3L, 4L, 5L, 6L, -100L, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("Invalid payment ID: " + id, () -> {
                    when(paymentRepository.findById(id)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.getPaymentById(id));
                    assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
                }));
    }
}
