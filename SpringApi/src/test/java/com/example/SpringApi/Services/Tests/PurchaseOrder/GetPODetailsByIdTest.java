package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
 * 
 * Test count: 6 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 5 tests (1 test + 4 dynamic tests)
 */
@DisplayName("PurchaseOrderService - GetPODetailsById Tests")
public class GetPODetailsByIdTest extends PurchaseOrderServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Get PO Details By ID - Success")
    void getPurchaseOrderDetailsById_Success() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));

        // Act
        PurchaseOrderResponseModel result = assertDoesNotThrow(
                () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PO_ID, result.getPurchaseOrderId());
        assertEquals(TEST_VENDOR_NUMBER, result.getVendorNumber());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Get PO Details By ID - Not Found")
    void getPurchaseOrderDetailsById_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsBadRequest("InvalidId", () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));
    }

    /**
     * Purpose: Get PO details with various invalid ID scenarios.
     * Expected Result: Throws BadRequestException with InvalidId message.
     * Assertions: Correct exception message.
     */
    @TestFactory
    @DisplayName("Get PO Details By ID - Invalid ID variations")
    Stream<DynamicTest> getPurchaseOrderDetailsById_InvalidIds() {
        return Stream.of(0L, -1L, -999L, Long.MAX_VALUE)
                .map(id -> DynamicTest.dynamicTest("ID=" + id, () -> {
                    lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(
                            eq(id), anyLong())).thenReturn(Optional.empty());

                    assertThrowsBadRequest("InvalidId", () -> purchaseOrderService.getPurchaseOrderDetailsById(id));
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPurchaseOrderDetailsById - Verify @PreAuthorize Annotation")
    void getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("getPurchaseOrderDetailsById", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPurchaseOrderDetailsById");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference VIEW_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("getPurchaseOrderDetailsById - Controller delegates to service")
    void getPurchaseOrderDetailsById_WithValidId_DelegatesToService() {
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderService);
        when(purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID))
                .thenReturn(mock(PurchaseOrderResponseModel.class));

        ResponseEntity<?> response = controller.getPurchaseOrderDetailsById(TEST_PO_ID);

        verify(purchaseOrderService).getPurchaseOrderDetailsById(TEST_PO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
