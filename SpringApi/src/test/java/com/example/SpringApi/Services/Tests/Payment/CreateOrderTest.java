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
 * Tests for PaymentService.createOrder() method.
 * Contains 12 tests covering various scenarios.
 */
@DisplayName("CreateOrder Tests")
class CreateOrderTest extends PaymentServiceTestBase {

    /**
     * Purpose: Verify that createOrder throws NotFoundException when client
     * credentials not found.
     * Expected Result: NotFoundException is thrown
     * (getClientWithRazorpayCredentials fails first).
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrder - Client not found - Throws NotFoundException")
    void createOrder_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrder(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrder handles negative PO ID (client check happens
     * first).
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrder - Negative PO ID - Throws NotFoundException for client")
    void createOrder_NegativePurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(-1L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrder(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrder handles zero PO ID (client check happens
     * first).
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrder - Zero PO ID - Throws NotFoundException for client")
    void createOrder_ZeroPurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(0L);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrder(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrder handles Long.MAX_VALUE PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrder - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
    void createOrder_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrder(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrder handles Long.MIN_VALUE PO ID.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId error.
     */
    @Test
    @DisplayName("createOrder - Long.MIN_VALUE PO ID - Throws NotFoundException for client")
    void createOrder_MinLongPurchaseOrderId_ThrowsNotFoundException() {
        testOrderRequest.setPurchaseOrderId(Long.MIN_VALUE);
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.createOrder(testOrderRequest));
        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that createOrder verifies client repository interaction.
     * Expected Result: Client repository findById is called.
     * Assertions: Verify repository interaction.
     */
    @Test
    @DisplayName("createOrder - Verify client repository interaction")
    void createOrder_VerifyClientRepositoryInteraction() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));
        verify(clientRepository, times(1)).findById(any());
    }

    /**
     * Purpose: Additional createOrder invalid ID coverage.
     * Expected Result: NotFoundException is thrown for client.
     * Assertions: Exception message matches Client InvalidId.
     */
    @TestFactory
    @DisplayName("createOrder - Additional invalid PO IDs")
    Stream<DynamicTest> createOrder_AdditionalInvalidPoIds() {
        when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
        return Stream.of(2L, 3L, 4L, 5L, 6L, 7L)
                .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                    testOrderRequest.setPurchaseOrderId(id);
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> paymentService.createOrder(testOrderRequest));
                    assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("createOrder - Verify @PreAuthorize Annotation")
    void createOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PaymentController.class.getMethod("createOrder", RazorpayOrderRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("createOrder - Controller delegates to service")
    void createOrder_WithValidRequest_DelegatesToService() {
        PaymentController controller = new PaymentController(paymentService, null);
        when(paymentService.createOrder(testOrderRequest)).thenReturn("order_123");

        ResponseEntity<?> response = controller.createOrder(testOrderRequest);

        verify(paymentService).createOrder(testOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}