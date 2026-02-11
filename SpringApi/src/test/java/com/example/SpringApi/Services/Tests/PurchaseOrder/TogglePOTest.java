package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
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
 * Test class for PurchaseOrderService.togglePurchaseOrder method.
 *
 * Test count: 8 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 6 tests (1 test + 5 dynamic tests)
 */
@DisplayName("PurchaseOrderService - TogglePO Tests")
public class TogglePOTest extends PurchaseOrderServiceTestBase {

    // ========================================
    //     SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Toggle PO - Success Mark As Deleted")
    void togglePurchaseOrder_Success_MarkAsDeleted() {
        // Arrange
        testPurchaseOrder.setIsDeleted(false);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
            eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

        // Assert
        verify(purchaseOrderRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        assertTrue(testPurchaseOrder.getIsDeleted());
    }

    @Test
    @DisplayName("Toggle PO - Success Restore")
    void togglePurchaseOrder_Success_Restore() {
        // Arrange
        testPurchaseOrder.setIsDeleted(true);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
            eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

        // Assert
        verify(purchaseOrderRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        assertFalse(testPurchaseOrder.getIsDeleted());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Toggle PO - Not Found")
    void togglePurchaseOrder_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
            eq(TEST_PO_ID), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(com.example.SpringApi.ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
            () -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, never()).save(any());
    }

    /**
     * Purpose: Toggle PO with various invalid ID scenarios.
     * Expected Result: Throws BadRequestException with InvalidId message.
     * Assertions: Correct exception message.
     */
    @TestFactory
    @DisplayName("Toggle PO - Invalid ID variations")
    Stream<DynamicTest> togglePurchaseOrder_InvalidIds() {
        return Stream.of(0L, -1L, -999L, Long.MAX_VALUE, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("ID=" + id, () -> {
                lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                    eq(id), anyLong())).thenReturn(Optional.empty());

                assertThrowsNotFound(com.example.SpringApi.ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> purchaseOrderService.togglePurchaseOrder(id));
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("togglePurchaseOrder - Verify @PreAuthorize Annotation")
    void togglePurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("togglePurchaseOrder", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on togglePurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.TOGGLE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference TOGGLE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("togglePurchaseOrder - Controller delegates to service")
    void togglePurchaseOrder_WithValidId_DelegatesToService() {
        com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doNothing().when(mockService).togglePurchaseOrder(TEST_PO_ID);

        ResponseEntity<?> response = controller.togglePurchaseOrder(TEST_PO_ID);

        verify(mockService).togglePurchaseOrder(TEST_PO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
