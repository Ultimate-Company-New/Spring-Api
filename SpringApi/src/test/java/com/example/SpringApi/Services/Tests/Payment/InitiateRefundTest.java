package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.ErrorMessages;
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
 * Tests for PaymentService.initiateRefund() method.
 * Contains 10 tests covering various scenarios.
 */
@DisplayName("InitiateRefund Tests")
class InitiateRefundTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that initiateRefund throws NotFoundException when client not found.
     * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("initiateRefund - Client not found - Throws NotFoundException")
    void initiateRefund_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "Test refund"));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that initiateRefund throws NotFoundException for negative payment ID.
     * Expected Result: NotFoundException is thrown for client (checked first).
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("initiateRefund - Negative payment ID - Throws NotFoundException for client")
    void initiateRefund_NegativePaymentId_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(-1L, 10000L, "Test refund"));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that initiateRefund throws NotFoundException for zero payment ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("initiateRefund - Zero payment ID - Throws NotFoundException for client")
    void initiateRefund_ZeroPaymentId_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(0L, 10000L, "Test refund"));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that initiateRefund handles Long.MAX_VALUE payment ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("initiateRefund - Long.MAX_VALUE payment ID - Throws NotFoundException for client")
    void initiateRefund_MaxLongPaymentId_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(Long.MAX_VALUE, 10000L, "Test refund"));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify client repository interaction for initiateRefund.
     * Expected Result: Client repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("initiateRefund - Verify client repository interaction")
    void initiateRefund_VerifyClientRepositoryInteraction() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class,
                () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "Test refund"));
        verify(clientRepository, times(1)).findById(any());
    }

    /**
     * Purpose: Additional initiateRefund invalid ID coverage.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId.
     */
    @TestFactory
    @DisplayName("initiateRefund - Additional invalid payment IDs")
    Stream<DynamicTest> initiateRefund_AdditionalInvalidPaymentIds() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        return Stream.of(2L, 3L, 4L, 5L, 6L)
                .map(id -> DynamicTest.dynamicTest("Payment ID: " + id, () -> {
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.initiateRefund(id, 10000L, "Test refund"));
                    assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                }));
    }
}
