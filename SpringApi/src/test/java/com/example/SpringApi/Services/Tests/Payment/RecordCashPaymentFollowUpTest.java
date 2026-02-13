package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
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
 * Tests for PaymentService.recordCashPaymentFollowUp().
 */
@DisplayName("RecordCashPaymentFollowUp Tests")
class RecordCashPaymentFollowUpTest extends PaymentServiceTestBase {

    // Total Tests: 10
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify purchase order repository interaction for follow-up cash payment flow.
     * Expected Result: purchaseOrderRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Verify Purchase Order Repository Interaction - Success")
    void recordCashPaymentFollowUp_verifyPurchaseOrderRepositoryInteraction_success() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
        verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify follow-up cash payment fails when purchase order is missing.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Purchase Order Not Found - Failure")
    void recordCashPaymentFollowUp_f01_purchaseOrderNotFound_failure() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify follow-up cash payment throws NullPointerException for null request.
     * Expected Result: NullPointerException.
     * Assertions: Exception type.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Null Request - Failure")
    void recordCashPaymentFollowUp_f02_nullRequest_failure() {
        // Arrange
        CashPaymentRequestModel request = null;

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> paymentService.recordCashPaymentFollowUp(request));

        // Assert
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id throws NotFoundException.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Negative Purchase Order Id - Failure")
    void recordCashPaymentFollowUp_f03_negativePurchaseOrderId_failure() {
        // Arrange
        testCashPaymentRequest.setPurchaseOrderId(-1L);
        stubPurchaseOrderRepositoryFindById(-1L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id throws NotFoundException.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Zero Purchase Order Id - Failure")
    void recordCashPaymentFollowUp_f04_zeroPurchaseOrderId_failure() {
        // Arrange
        testCashPaymentRequest.setPurchaseOrderId(0L);
        stubPurchaseOrderRepositoryFindById(0L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id throws NotFoundException.
     * Expected Result: NotFoundException with purchase order invalid id message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Max Long Purchase Order Id - Failure")
    void recordCashPaymentFollowUp_f05_maxLongPurchaseOrderId_failure() {
        // Arrange
        testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubPurchaseOrderRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs throw NotFoundException.
     * Expected Result: NotFoundException with purchase order invalid id for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Additional Invalid Purchase Order Ids - Failure")
    void recordCashPaymentFollowUp_f06_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L, -100L };

        // Act
        for (Long invalidId : invalidIds) {
            testCashPaymentRequest.setPurchaseOrderId(invalidId);
            stubPurchaseOrderRepositoryFindById(invalidId, Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));

            // Assert
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
        }
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
    @DisplayName("recordCashPaymentFollowUp - Controller Permission Forbidden")
    void recordCashPaymentFollowUp_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceRecordCashPaymentFollowUpThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.recordCashPaymentFollowUp(testCashPaymentRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Controller PreAuthorize Annotation - Success")
    void recordCashPaymentFollowUp_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("recordCashPaymentFollowUp", CashPaymentRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates follow-up cash payment request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("recordCashPaymentFollowUp - Controller Delegates To Service - Success")
    void recordCashPaymentFollowUp_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceRecordCashPaymentFollowUp(createSuccessPaymentVerificationResponse());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.recordCashPaymentFollowUp(testCashPaymentRequest);

        // Assert
        verify(paymentServiceMock, times(1)).recordCashPaymentFollowUp(testCashPaymentRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
