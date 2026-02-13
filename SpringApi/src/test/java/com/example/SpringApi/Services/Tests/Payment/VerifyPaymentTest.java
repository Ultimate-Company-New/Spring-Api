package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
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
 * Tests for PaymentService.verifyPayment().
 */
@DisplayName("VerifyPayment Tests")
class VerifyPaymentTest extends PaymentServiceTestBase {

    // Total Tests: 9
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository interaction is performed during verifyPayment.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("verifyPayment - Verify Client Repository Interaction - Success")
    void verifyPayment_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify verifyPayment throws NotFoundException when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Client Not Found - Failure")
    void verifyPayment_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Negative Purchase Order Id - Failure")
    void verifyPayment_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Zero Purchase Order Id - Failure")
    void verifyPayment_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id follows the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPayment - Max Long Purchase Order Id - Failure")
    void verifyPayment_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs all follow the same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("verifyPayment - Additional Invalid Purchase Order Ids - Failure")
    void verifyPayment_f05_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            testVerifyRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));

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
    @DisplayName("verifyPayment - Controller Permission Forbidden")
    void verifyPayment_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceVerifyPaymentThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPayment(testVerifyRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("verifyPayment - Controller PreAuthorize Annotation - Success")
    void verifyPayment_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("verifyPayment", RazorpayVerifyRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates verifyPayment request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("verifyPayment - Controller Delegates To Service - Success")
    void verifyPayment_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceVerifyPayment(createSuccessPaymentVerificationResponse());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPayment(testVerifyRequest);

        // Assert
        verify(paymentServiceMock, times(1)).verifyPayment(testVerifyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
