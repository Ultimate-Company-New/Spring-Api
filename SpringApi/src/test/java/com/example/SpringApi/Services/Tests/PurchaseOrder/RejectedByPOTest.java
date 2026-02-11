package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PurchaseOrderService.rejectedByPurchaseOrder method.
 * 
 * Test count: 9 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 5 tests (2 tests + 3 dynamic tests)
 * - CONTROLLER: 3 tests (permission, annotation, delegation)
 */
@DisplayName("PurchaseOrderService - RejectedByPO Tests")
public class RejectedByPOTest extends PurchaseOrderServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Rejected By PO - Success")
    void rejectedByPurchaseOrder_Success() {
        // Arrange
        testPurchaseOrder.setRejectedByUserId(null);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));

        // Assert
        verify(purchaseOrderRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Rejected By PO - Already Rejected")
    void rejectedByPurchaseOrder_AlreadyRejected() {
        // Arrange
        testPurchaseOrder.setRejectedByUserId(888L);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));

        // Act & Assert
        assertThrowsBadRequest(com.example.SpringApi.ErrorMessages.PurchaseOrderErrorMessages.AlreadyRejected,
            () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejected By PO - Not Found")
    void rejectedByPurchaseOrder_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(com.example.SpringApi.ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
            () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, never()).save(any());
    }

    /**
     * Purpose: Rejected by PO with various invalid ID scenarios.
     * Expected Result: Throws BadRequestException with InvalidId message.
     * Assertions: Correct exception message.
     */
    @TestFactory
    @DisplayName("Rejected By PO - Invalid ID variations")
    Stream<DynamicTest> rejectedByPurchaseOrder_InvalidIds() {
        return Stream.of(0L, -1L, -100L)
                .map(id -> DynamicTest.dynamicTest("ID=" + id, () -> {
                    lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                            eq(id), anyLong())).thenReturn(Optional.empty());

                assertThrowsNotFound(com.example.SpringApi.ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> purchaseOrderService.rejectedByPurchaseOrder(id));
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

        /**
         * Purpose: Verify unauthorized access is blocked at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("rejectedByPurchaseOrder - Controller Permission - Unauthorized")
        void rejectedByPurchaseOrder_controller_permission_unauthorized() {
        // Arrange
        com.example.SpringApi.Services.PurchaseOrderService mockService =
            mock(com.example.SpringApi.Services.PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(mockService).rejectedByPurchaseOrder(TEST_PO_ID);

        // Act
        ResponseEntity<?> response = controller.rejectedByPurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

    @Test
    @DisplayName("rejectedByPurchaseOrder - Verify @PreAuthorize Annotation")
    void rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("rejectedByPurchaseOrder", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on rejectedByPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("rejectedByPurchaseOrder - Controller delegates to service")
    void rejectedByPurchaseOrder_WithValidId_DelegatesToService() {
        com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doNothing().when(mockService).rejectedByPurchaseOrder(TEST_PO_ID);

        ResponseEntity<?> response = controller.rejectedByPurchaseOrder(TEST_PO_ID);

        verify(mockService).rejectedByPurchaseOrder(TEST_PO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
