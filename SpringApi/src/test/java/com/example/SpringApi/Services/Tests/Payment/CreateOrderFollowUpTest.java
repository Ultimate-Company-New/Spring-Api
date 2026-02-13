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
 * Tests for PaymentService.createOrderFollowUp().
 */
@DisplayName("CreateOrderFollowUp Tests")
class CreateOrderFollowUpTest extends PaymentServiceTestBase {

    // Total Tests: 9
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository interaction occurs for follow-up order creation.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("createOrderFollowUp - Verify Client Repository Interaction - Success")
    void createOrderFollowUp_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify follow-up order creation fails when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrderFollowUp - Client Not Found - Failure")
    void createOrderFollowUp_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrderFollowUp - Negative Purchase Order Id - Failure")
    void createOrderFollowUp_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrderFollowUp - Zero Purchase Order Id - Failure")
    void createOrderFollowUp_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("createOrderFollowUp - Max Long Purchase Order Id - Failure")
    void createOrderFollowUp_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs follow same client-not-found path.
     * Expected Result: All IDs throw NotFoundException with Client InvalidId.
     * Assertions: Exception type and exact message for each ID.
     */
    @Test
    @DisplayName("createOrderFollowUp - Additional Invalid Purchase Order Ids - Failure")
    void createOrderFollowUp_f05_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            testOrderRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));

            // Assert
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
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
    @DisplayName("createOrderFollowUp - Controller Permission Forbidden")
    void createOrderFollowUp_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceCreateOrderFollowUpThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.createOrderFollowUp(testOrderRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("createOrderFollowUp - Controller PreAuthorize Annotation - Success")
    void createOrderFollowUp_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("createOrderFollowUp", RazorpayOrderRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates follow-up create order request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("createOrderFollowUp - Controller Delegates To Service - Success")
    void createOrderFollowUp_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceCreateOrderFollowUp(new RazorpayOrderResponseModel());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.createOrderFollowUp(testOrderRequest);

        // Assert
        verify(paymentServiceMock, times(1)).createOrderFollowUp(testOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
