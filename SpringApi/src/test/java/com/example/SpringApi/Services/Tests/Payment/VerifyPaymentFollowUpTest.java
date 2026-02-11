package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PaymentService.verifyPaymentFollowUp() method.
 * Contains 10 tests covering various scenarios.
 */
@DisplayName("VerifyPaymentFollowUp Tests")
class VerifyPaymentFollowUpTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that verifyPaymentFollowUp throws NotFoundException when client not found.
     * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Client not found - Throws NotFoundException")
    void verifyPaymentFollowUp_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that verifyPaymentFollowUp handles negative PO ID (client check first).
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Negative PO ID - Throws NotFoundException for client")
    void verifyPaymentFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
        testVerifyRequest.setPurchaseOrderId(-1L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that verifyPaymentFollowUp handles zero PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Zero PO ID - Throws NotFoundException for client")
    void verifyPaymentFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
        testVerifyRequest.setPurchaseOrderId(0L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that verifyPaymentFollowUp handles Long.MAX_VALUE PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
    void verifyPaymentFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
        testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify client repository interaction for verifyPaymentFollowUp.
     * Expected Result: Client repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("verifyPaymentFollowUp - Verify client repository interaction")
    void verifyPaymentFollowUp_VerifyClientRepositoryInteraction() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
        verify(clientRepository, times(1)).findById(any());
    }

    /**
     * Purpose: Additional verifyPaymentFollowUp invalid ID coverage.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId.
     */
    @TestFactory
    @DisplayName("verifyPaymentFollowUp - Additional invalid PO IDs")
    Stream<DynamicTest> verifyPaymentFollowUp_AdditionalInvalidPoIds() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        return Stream.of(2L, 3L, 4L, 5L, 6L)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    testVerifyRequest.setPurchaseOrderId(id);
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
                    assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("verifyPaymentFollowUp - Verify @PreAuthorize Annotation")
    void verifyPaymentFollowUp_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PaymentController.class.getMethod("verifyPaymentFollowUp", RazorpayVerifyRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
            "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("verifyPaymentFollowUp - Controller delegates to service")
    void verifyPaymentFollowUp_WithValidRequest_DelegatesToService() {
        PaymentService paymentServiceMock = mock(PaymentService.class);
        PaymentController controller = new PaymentController(paymentServiceMock, null);
        when(paymentServiceMock.verifyPaymentFollowUp(testVerifyRequest))
            .thenReturn(com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel
                .success("payment-id", TEST_PO_ID, "APPROVED"));

        ResponseEntity<?> response = controller.verifyPaymentFollowUp(testVerifyRequest);

        verify(paymentServiceMock).verifyPaymentFollowUp(testVerifyRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}