package com.example.SpringApi.Services.Tests.Payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentService.isPurchaseOrderPaid() method.
 * Contains 10 tests covering various scenarios.
 */
@DisplayName("IsPurchaseOrderPaid Tests")
class IsPurchaseOrderPaidTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that isPurchaseOrderPaid returns true when successful payment exists.
     * Expected Result: true is returned.
     * Assertions: Result is true.
     */
    @Test
    @DisplayName("isPurchaseOrderPaid - Has successful payment - Returns true")
    void isPurchaseOrderPaid_HasSuccessfulPayment_ReturnsTrue() {
        when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(true);

        boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

        assertTrue(result);
    }

    /**
     * Purpose: Verify that isPurchaseOrderPaid returns false when no successful payment exists.
     * Expected Result: false is returned.
     * Assertions: Result is false.
     */
    @Test
    @DisplayName("isPurchaseOrderPaid - No successful payment - Returns false")
    void isPurchaseOrderPaid_NoSuccessfulPayment_ReturnsFalse() {
        when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(false);

        boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

        assertFalse(result);
    }

    /**
     * Purpose: Verify that isPurchaseOrderPaid handles negative ID.
     * Expected Result: false is returned for non-existent PO.
     * Assertions: Result is false.
     */
    @Test
    @DisplayName("isPurchaseOrderPaid - Negative ID - Returns false")
    void isPurchaseOrderPaid_NegativeId_ReturnsFalse() {
        when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", -1L)).thenReturn(false);

        boolean result = paymentService.isPurchaseOrderPaid(-1L);

        assertFalse(result);
    }

    /**
     * Purpose: Verify that isPurchaseOrderPaid handles zero ID.
     * Expected Result: false is returned for non-existent PO.
     * Assertions: Result is false.
     */
    @Test
    @DisplayName("isPurchaseOrderPaid - Zero ID - Returns false")
    void isPurchaseOrderPaid_ZeroId_ReturnsFalse() {
        when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", 0L)).thenReturn(false);

        boolean result = paymentService.isPurchaseOrderPaid(0L);

        assertFalse(result);
    }

    /**
     * Purpose: Verify payment repository is called with correct parameters.
     * Expected Result: Repository hasSuccessfulPayment is called.
     * Assertions: verify repository method is called with correct params.
     */
    @Test
    @DisplayName("isPurchaseOrderPaid - Verify payment repository interaction")
    void isPurchaseOrderPaid_VerifyPaymentRepositoryInteraction() {
        when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(true);

        paymentService.isPurchaseOrderPaid(TEST_PO_ID);

        verify(paymentRepository, times(1)).hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID);
    }

    /**
     * Purpose: Additional purchase order ID coverage for isPurchaseOrderPaid.
     * Expected Result: false when repository returns false.
     * Assertions: Result is false.
     */
    @TestFactory
    @DisplayName("isPurchaseOrderPaid - Additional IDs")
    Stream<DynamicTest> isPurchaseOrderPaid_AdditionalIds() {
        when(paymentRepository.hasSuccessfulPayment(eq("PURCHASE_ORDER"), anyLong())).thenReturn(false);
        return Stream.of(2L, 3L, 4L, 5L, Long.MAX_VALUE)
                .map(id -> DynamicTest.dynamicTest("PO ID: " + id, () -> {
                    boolean result = paymentService.isPurchaseOrderPaid(id);
                    assertFalse(result);
                }));
    }
}
