package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentService.recordCashPaymentFollowUp() method.
 * Contains 12 tests covering various scenarios.
 */
@DisplayName("RecordCashPaymentFollowUp Tests")
class RecordCashPaymentFollowUpTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that recordCashPaymentFollowUp throws NotFoundException when PO not found.
     * Expected Result: NotFoundException is thrown (PO lookup happens first).
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - PO not found - Throws NotFoundException")
    void recordCashPaymentFollowUp_PONotFound_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPaymentFollowUp handles null request.
     * Expected Result: NullPointerException is thrown (accessing null request).
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Null request - Throws NullPointerException")
    void recordCashPaymentFollowUp_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> paymentService.recordCashPaymentFollowUp(null));
    }

    /**
     * Purpose: Verify that recordCashPaymentFollowUp handles negative PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Negative PO ID - Throws NotFoundException")
    void recordCashPaymentFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(-1L);
        when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPaymentFollowUp handles zero PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Zero PO ID - Throws NotFoundException")
    void recordCashPaymentFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(0L);
        when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPaymentFollowUp handles Long.MAX_VALUE PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException")
    void recordCashPaymentFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
        when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Additional recordCashPaymentFollowUp invalid ID coverage.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId.
     */
    @TestFactory
    @DisplayName("recordCashPaymentFollowUp - Additional invalid PO IDs")
    Stream<DynamicTest> recordCashPaymentFollowUp_AdditionalInvalidPoIds() {
        return Stream.of(2L, 3L, 4L, 5L, 6L, -100L)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    testCashPaymentRequest.setPurchaseOrderId(id);
                    when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
                    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("recordCashPaymentFollowUp - Verify @PreAuthorize Annotation")
    void recordCashPaymentFollowUp_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PaymentController.class.getMethod("recordCashPaymentFollowUp", CashPaymentRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
            "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("recordCashPaymentFollowUp - Controller delegates to service")
    void recordCashPaymentFollowUp_WithValidRequest_DelegatesToService() {
        PaymentController controller = new PaymentController(paymentService, null);
        doNothing().when(paymentService).recordCashPaymentFollowUp(testCashPaymentRequest);

        ResponseEntity<?> response = controller.recordCashPaymentFollowUp(testCashPaymentRequest);

        verify(paymentService).recordCashPaymentFollowUp(testCashPaymentRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}