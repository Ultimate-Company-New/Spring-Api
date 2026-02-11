package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Services.PaymentService;
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
 * Tests for PaymentService.recordCashPayment() method.
 * Contains 16 tests covering various scenarios.
 */
@DisplayName("RecordCashPayment Tests")
class RecordCashPaymentTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that recordCashPayment throws NotFoundException when PO not found.
     * Expected Result: NotFoundException is thrown (PO lookup happens first).
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPayment - PO not found - Throws NotFoundException")
    void recordCashPayment_PONotFound_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPayment handles null request.
     * Expected Result: NullPointerException is thrown (accessing null request).
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("recordCashPayment - Null request - Throws NullPointerException")
    void recordCashPayment_NullRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> paymentService.recordCashPayment(null));
    }

    /**
     * Purpose: Verify that recordCashPayment handles negative PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPayment - Negative PO ID - Throws NotFoundException")
    void recordCashPayment_NegativePurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(-1L);
        when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPayment handles zero PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPayment - Zero PO ID - Throws NotFoundException")
    void recordCashPayment_ZeroPurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(0L);
        when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPayment handles Long.MAX_VALUE PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPayment - Long.MAX_VALUE PO ID - Throws NotFoundException")
    void recordCashPayment_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
        when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that recordCashPayment handles Long.MIN_VALUE PO ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId error.
     */
    @Test
    @DisplayName("recordCashPayment - Long.MIN_VALUE PO ID - Throws NotFoundException")
    void recordCashPayment_MinLongPurchaseOrderId_ThrowsNotFoundException() {
        testCashPaymentRequest.setPurchaseOrderId(Long.MIN_VALUE);
        when(purchaseOrderRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify PO repository interaction for recordCashPayment.
     * Expected Result: PO repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("recordCashPayment - Verify PO repository interaction")
    void recordCashPayment_VerifyPORepositoryInteraction() {
        when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> paymentService.recordCashPayment(testCashPaymentRequest));
        verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
    }

    /**
     * Purpose: Additional recordCashPayment invalid ID coverage.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches PurchaseOrder InvalidId.
     */
    @TestFactory
    @DisplayName("recordCashPayment - Additional invalid PO IDs")
    Stream<DynamicTest> recordCashPayment_AdditionalInvalidPoIds() {
        return Stream.of(2L, 3L, 4L, 5L, 6L, 7L, -100L, Long.MAX_VALUE - 1)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    testCashPaymentRequest.setPurchaseOrderId(id);
                    when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.recordCashPayment(testCashPaymentRequest));
                    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("recordCashPayment - Verify @PreAuthorize Annotation")
    void recordCashPayment_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PaymentController.class.getMethod("recordCashPayment", CashPaymentRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
            "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("recordCashPayment - Controller delegates to service")
    void recordCashPayment_WithValidRequest_DelegatesToService() {
        PaymentService paymentServiceMock = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentServiceMock, null);
        when(paymentServiceMock.recordCashPayment(testCashPaymentRequest))
            .thenReturn(com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel
                .success("payment-id", TEST_PO_ID, "APPROVED"));

        ResponseEntity<?> response = controller.recordCashPayment(testCashPaymentRequest);

        verify(paymentServiceMock).recordCashPayment(testCashPaymentRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}