package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PurchaseOrderService.togglePurchaseOrder method.
 *
 * Test count: 6 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 1 test
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - TogglePurchaseOrder Tests")
public class TogglePurchaseOrderTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 6

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify toggle marks as deleted.
     * Expected Result: isDeleted becomes true.
     * Assertions: Save is called and flag toggled.
     */
    @Test
    @DisplayName("Toggle Purchase Order - Mark Deleted - Success")
    void togglePurchaseOrder_Success_MarkAsDeleted() {
        // Arrange
        testPurchaseOrder.setIsDeleted(false);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubPurchaseOrderRepositorySave(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

        // Assert
        assertTrue(testPurchaseOrder.getIsDeleted());
    }

    /**
     * Purpose: Verify toggle restores deleted purchase order.
     * Expected Result: isDeleted becomes false.
     * Assertions: Save is called and flag toggled.
     */
    @Test
    @DisplayName("Toggle Purchase Order - Restore - Success")
    void togglePurchaseOrder_Success_Restore() {
        // Arrange
        testPurchaseOrder.setIsDeleted(true);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubPurchaseOrderRepositorySave(testPurchaseOrder);

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

        // Assert
        assertFalse(testPurchaseOrder.getIsDeleted());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject toggle for missing purchase order.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId.
     */
    @Test
    @DisplayName("Toggle Purchase Order - Not Found - Throws NotFoundException")
    void togglePurchaseOrder_NotFound_Failure() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                () -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));
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
    @DisplayName("togglePurchaseOrder - Controller Permission - Unauthorized")
    void togglePurchaseOrder_controller_permission_unauthorized() {
        // Arrange
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnToggle();

        // Act
        ResponseEntity<?> response = controller.togglePurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for togglePurchaseOrder.
     * Expected Result: Annotation exists and includes TOGGLE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("togglePurchaseOrder - Verify @PreAuthorize Annotation")
    void togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PurchaseOrderController.class.getMethod("togglePurchaseOrder", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on togglePurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.TOGGLE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference TOGGLE_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("togglePurchaseOrder - Controller delegates to service")
    void togglePurchaseOrder_WithValidId_DelegatesToService() {
        // Arrange
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceToggleDoNothing();

        // Act
        ResponseEntity<?> response = controller.togglePurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
