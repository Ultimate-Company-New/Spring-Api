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
 * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
 * 
 * Test count: 6 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 5 tests (2 tests + 3 dynamic tests)
 */
@DisplayName("PurchaseOrderService - ApprovedByPO Tests")
public class ApprovedByPOTest extends PurchaseOrderServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Approved By PO - Success")
    void approvedByPurchaseOrder_Success() {
        // Arrange
        testPurchaseOrder.setApprovedByUserId(null);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));

        // Assert
        verify(purchaseOrderRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Approved By PO - Already Approved")
    void approvedByPurchaseOrder_AlreadyApproved() {
        // Arrange
        testPurchaseOrder.setApprovedByUserId(999L);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));

        // Act & Assert
        assertThrowsBadRequest("AlreadyApproved", () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Approved By PO - Not Found")
    void approvedByPurchaseOrder_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsBadRequest("InvalidId", () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
        verify(purchaseOrderRepository, never()).save(any());
    }

    /**
     * Purpose: Approved by PO with various invalid ID scenarios.
     * Expected Result: Throws BadRequestException with InvalidId message.
     * Assertions: Correct exception message.
     */
    @TestFactory
    @DisplayName("Approved By PO - Invalid ID variations")
    Stream<DynamicTest> approvedByPurchaseOrder_InvalidIds() {
        return Stream.of(0L, -1L, -100L)
                .map(id -> DynamicTest.dynamicTest("ID=" + id, () -> {
                    lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                            eq(id), anyLong())).thenReturn(Optional.empty());

                    assertThrowsBadRequest("InvalidId", () -> purchaseOrderService.approvedByPurchaseOrder(id));
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("approvedByPurchaseOrder - Verify @PreAuthorize Annotation")
    void approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("approvedByPurchaseOrder", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on approvedByPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("approvedByPurchaseOrder - Controller delegates to service")
    void approvedByPurchaseOrder_WithValidId_DelegatesToService() {
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderService);
        doNothing().when(purchaseOrderService).approvedByPurchaseOrder(TEST_PO_ID);

        ResponseEntity<?> response = controller.approvedByPurchaseOrder(TEST_PO_ID);

        verify(purchaseOrderService).approvedByPurchaseOrder(TEST_PO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
