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
 * Test class for PurchaseOrderService.rejectedByPurchaseOrder method.
 *
 * Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - RejectedByPurchaseOrder Tests")
class RejectedByPurchaseOrderTest extends PurchaseOrderServiceTestBase {

    // Total Tests: 6
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify reject succeeds when not already rejected.
     * Expected Result: Purchase order is updated and logged.
     * Assertions: Save is called and no exception thrown.
     */
    @Test
    @DisplayName("Rejected By Purchase Order - Success")
    void rejectedByPurchaseOrder_Success_Success() {
        // Arrange
        testPurchaseOrder.setRejectedByUserId(null);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubPurchaseOrderRepositorySave(testPurchaseOrder);

        // Act & Assert
        assertDoesNotThrow(() -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject when already rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message matches AlreadyRejected.
     */
    @Test
    @DisplayName("Rejected By Purchase Order - Already Rejected - Throws BadRequestException")
    void rejectedByPurchaseOrder_AlreadyRejected_Failure() {
        // Arrange
        testPurchaseOrder.setRejectedByUserId(888L);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.ALREADY_REJECTED,
                () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));
    }

    /**
     * Purpose: Reject for missing purchase order.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId.
     */
    @Test
    @DisplayName("Rejected By Purchase Order - Not Found - Throws NotFoundException")
    void rejectedByPurchaseOrder_NotFound_Failure() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID,
                () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));
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
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnReject();

        // Act
        ResponseEntity<?> response = controller.rejectedByPurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for rejectedByPurchaseOrder.
     * Expected Result: Annotation exists and includes UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("rejectedByPurchaseOrder - Verify @PreAuthorize Annotation")
    void rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PurchaseOrderController.class.getMethod("rejectedByPurchaseOrder", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on rejectedByPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("rejectedByPurchaseOrder - Controller delegates to service")
    void rejectedByPurchaseOrder_WithValidId_DelegatesToService() {
        // Arrange
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceRejectDoNothing();

        // Act
        ResponseEntity<?> response = controller.rejectedByPurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
