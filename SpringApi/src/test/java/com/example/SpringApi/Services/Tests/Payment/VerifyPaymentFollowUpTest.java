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
 * Tests for PaymentService.verifyPaymentFollowUp().
 */
@DisplayName("VerifyPaymentFollowUp Tests")
class VerifyPaymentFollowUpTest extends PaymentServiceTestBase {

    // Total Tests: 9
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify client repository interaction is performed during follow-up verification.
     * Expected Result: clientRepository.findById is called once.
     * Assertions: verify interaction count.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Verify Client Repository Interaction - Success")
    void verifyPaymentFollowUp_verifyClientRepositoryInteraction_success() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        verify(clientRepository, times(1)).findById(any());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify follow-up verification fails when client context is unavailable.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Client Not Found - Failure")
    void verifyPaymentFollowUp_f01_clientNotFound_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Negative Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f02_negativePurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(-1L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Zero Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f03_zeroPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(0L);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify max long purchase order id follows same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId message.
     * Assertions: Exception type and exact message.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Max Long Purchase Order Id - Failure")
    void verifyPaymentFollowUp_f04_maxLongPurchaseOrderId_failure() {
        // Arrange
        testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
        stubClientRepositoryFindByIdNull(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify additional invalid IDs all follow same client-not-found path.
     * Expected Result: NotFoundException with Client InvalidId for each id.
     * Assertions: Exception type and exact message for each id.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Additional Invalid Purchase Order Ids - Failure")
    void verifyPaymentFollowUp_f05_additionalInvalidPurchaseOrderIds_failure() {
        // Arrange
        stubClientRepositoryFindByIdNull(Optional.empty());
        Long[] invalidIds = new Long[] { 2L, 3L, 4L, 5L, 6L };

        // Act
        for (Long invalidId : invalidIds) {
            testVerifyRequest.setPurchaseOrderId(invalidId);
            NotFoundException ex = assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));

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
    @DisplayName("verifyPaymentFollowUp - Controller Permission Forbidden")
    void verifyPaymentFollowUp_p01_controller_permission_forbidden() {
        // Arrange
        stubPaymentServiceVerifyPaymentFollowUpThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller method has expected @PreAuthorize permission constant.
     * Expected Result: Annotation exists and contains UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: annotation not null and value contains expected permission.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Controller PreAuthorize Annotation - Success")
    void verifyPaymentFollowUp_p02_controllerPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("verifyPaymentFollowUp", RazorpayVerifyRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }

    /**
     * Purpose: Verify controller delegates follow-up verification request to service.
     * Expected Result: HTTP 200 OK with delegated service call.
     * Assertions: Service interaction and response code.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Controller Delegates To Service - Success")
    void verifyPaymentFollowUp_p03_controllerDelegatesToService_success() {
        // Arrange
        stubPaymentServiceVerifyPaymentFollowUp(createSuccessPaymentVerificationResponse());

        // Act
        ResponseEntity<?> response = paymentControllerWithMock.verifyPaymentFollowUp(testVerifyRequest);

        // Assert
        verify(paymentServiceMock, times(1)).verifyPaymentFollowUp(testVerifyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
