package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
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
 * Tests for PaymentService.createOrderFollowUp() method.
 * Contains 10 tests covering various scenarios.
 */
@DisplayName("CreateOrderFollowUp Tests")
class CreateOrderFollowUpTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that createOrderFollowUp throws NotFoundException when client not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrderFollowUp - Client not found - Throws NotFoundException")
    void createOrderFollowUp_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrderFollowUp handles negative PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrderFollowUp - Negative PO ID - Throws NotFoundException for client")
    void createOrderFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(-1L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrderFollowUp handles zero PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrderFollowUp - Zero PO ID - Throws NotFoundException for client")
    void createOrderFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(0L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrderFollowUp handles Long.MAX_VALUE PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrderFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
    void createOrderFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrderFollowUp(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify client repository interaction for createOrderFollowUp.
     * Expected Result: Client repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("createOrderFollowUp - Verify client repository interaction")
    void createOrderFollowUp_VerifyClientRepositoryInteraction() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        
        assertThrows(NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));
        verify(clientRepository, times(1)).findById(any());
    }

    /**
     * Purpose: Additional createOrderFollowUp invalid ID coverage.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId.
     */
    @TestFactory
    @DisplayName("createOrderFollowUp - Additional invalid PO IDs")
    Stream<DynamicTest> createOrderFollowUp_AdditionalInvalidPoIds() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        return Stream.of(2L, 3L, 4L, 5L, 6L)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    testOrderRequest.setPurchaseOrderId(id);
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.createOrderFollowUp(testOrderRequest));
                    assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("createOrderFollowUp - Verify @PreAuthorize Annotation")
    void createOrderFollowUp_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PaymentController.class.getMethod("createOrderFollowUp", RazorpayOrderRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
            "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("createOrderFollowUp - Controller delegates to service")
    void createOrderFollowUp_WithValidRequest_DelegatesToService() {
        PaymentController controller = new PaymentController(paymentService, null);
        when(paymentService.createOrderFollowUp(testOrderRequest)).thenReturn("order_123");

        ResponseEntity<?> response = controller.createOrderFollowUp(testOrderRequest);

        verify(paymentService).createOrderFollowUp(testOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}