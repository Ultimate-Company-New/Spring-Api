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
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentService.getPaymentsForPurchaseOrder() method.
 * Contains 12 tests covering various scenarios.
 */
@DisplayName("GetPaymentsForPurchaseOrder Tests")
class GetPaymentsForPurchaseOrderTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException when PO doesn't exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - PO not found - Throws NotFoundException")
    void getPaymentsForPurchaseOrder_PONotFound_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for negative ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - Negative PO ID - Throws NotFoundException")
    void getPaymentsForPurchaseOrder_NegativePOId_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(-1L));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - Zero PO ID - Throws NotFoundException")
    void getPaymentsForPurchaseOrder_ZeroPOId_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(0L));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for Long.MAX_VALUE ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - Long.MAX_VALUE PO ID - Throws NotFoundException")
    void getPaymentsForPurchaseOrder_MaxLongPOId_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(Long.MAX_VALUE));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Verify that getPaymentsForPurchaseOrder throws BadRequestException when client access denied.
     * Expected Result: BadRequestException is thrown (after PO found, client check fails).
     * Assertions: Exception message matches AccessDeniedToPurchaseOrder error.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - Client access denied - Throws BadRequestException")
    void getPaymentsForPurchaseOrder_ClientAccessDenied_ThrowsBadRequestException() {
        testPurchaseOrder.setClientId(TEST_CLIENT_ID + 1);
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));
        assertEquals(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder, ex.getMessage());
    }

    /**
     * Purpose: Verify PO repository interaction for getPaymentsForPurchaseOrder.
     * Expected Result: PO repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("getPaymentsForPurchaseOrder - Verify PO repository interaction")
    void getPaymentsForPurchaseOrder_VerifyPORepositoryInteraction() {
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
    }

    /**
     * Purpose: Additional invalid PO ID coverage.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId.
     */
    @TestFactory
    @DisplayName("getPaymentsForPurchaseOrder - Additional invalid PO IDs")
    Stream<DynamicTest> getPaymentsForPurchaseOrder_AdditionalInvalidPoIds() {
        return Stream.of(2L, 3L, 4L, 5L, 6L, -100L)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.getPaymentsForPurchaseOrder(id));
                    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                }));
    }
}
