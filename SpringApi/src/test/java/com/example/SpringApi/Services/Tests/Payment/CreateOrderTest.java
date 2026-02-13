package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.RazorpayOrderResponseModel;
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
 * Tests for PaymentService.createOrder().
 */
@DisplayName("CreateOrder Tests")
class CreateOrderTest extends PaymentServiceTestBase {

    // Total Tests: 10
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository is called for createOrder flow.
     * Expected Result: clientRepository.findById is called exactly once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("createOrder - Verify Client Repository Interaction - Success")
    void createOrder_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify createOrder throws NotFoundException when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrder - Client Not Found - Failure")
    void createOrder_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify createOrder handles negative purchase order id while client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrder - Negative Purchase Order Id - Failure")
    void createOrder_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify createOrder handles zero purchase order id while client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrder - Zero Purchase Order Id - Failure")
    void createOrder_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify createOrder handles max long purchase order id while client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrder - Max Long Purchase Order Id - Failure")
    void createOrder_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify createOrder handles min long purchase order id while client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrder - Min Long Purchase Order Id - Failure")
    void createOrder_f05_minLongPurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(Long.MIN_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs all resolve to the same client-not-found path.
     * Expected Result: Each id throws NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("createOrder - Additional Invalid Purchase Order Ids - Failure")
    void createOrder_f06_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L, 7L };

        // Act
        for (Long invalidId : invalidIds) {
            testOrderRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
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
    @DisplayName("createOrder - Controller Permission Forbidden")
    void createOrder_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceCreateOrderThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.createOrder(testOrderRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("createOrder - Controller PreAuthorize Annotation - Success")
    void createOrder_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("createOrder", RazorpayOrderRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates createOrder request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("createOrder - Controller Delegates To Service - Success")
    void createOrder_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceCreateOrder(new RazorpayOrderResponseModel());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.createOrder(testOrderRequest);

        // Assert
        verify(paymentServiceMock, times(1)).createOrder(testOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
