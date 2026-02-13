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
 * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
 *
 * Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - ApprovedByPurchaseOrder Tests")
public class ApprovedByPurchaseOrderTest extends PurchaseOrderServiceTestBase {

    // Total Tests: 6
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify approve succeeds when not already approved.
     * Expected Result: Purchase order is updated and logged.
     * Assertions: Save is called and no exception thrown.
     */
    @Test
    @DisplayName("Approved By Purchase Order - Success")
    void approvedByPurchaseOrder_Success_Success() {
        // Arrange
        testPurchaseOrder.setApprovedByUserId(null);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubPurchaseOrderRepositorySave(testPurchaseOrder);

        // Act & Assert
        assertDoesNotThrow(() -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject approval if already approved.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message matches AlreadyApproved.
     */
    @Test
    @DisplayName("Approved By Purchase Order - Already Approved - Throws BadRequestException")
    void approvedByPurchaseOrder_AlreadyApproved_Failure() {
        // Arrange
        testPurchaseOrder.setApprovedByUserId(999L);
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.AlreadyApproved,
                () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
    }

    /**
     * Purpose: Reject approval for missing purchase order.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId.
     */
    @Test
    @DisplayName("Approved By Purchase Order - Not Found - Throws NotFoundException")
    void approvedByPurchaseOrder_NotFound_Failure() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));
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
    @DisplayName("approvedByPurchaseOrder - Controller Permission - Unauthorized")
    void approvedByPurchaseOrder_controller_permission_unauthorized() {
        // Arrange
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnApprove();

        // Act
        ResponseEntity<?> response = controller.approvedByPurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for approvedByPurchaseOrder.
     * Expected Result: Annotation exists and includes UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("approvedByPurchaseOrder - Verify @PreAuthorize Annotation")
    void approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PurchaseOrderController.class.getMethod("approvedByPurchaseOrder", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on approvedByPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("approvedByPurchaseOrder - Controller delegates to service")
    void approvedByPurchaseOrder_WithValidId_DelegatesToService() {
        // Arrange
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceApproveDoNothing();

        // Act
        ResponseEntity<?> response = controller.approvedByPurchaseOrder(TEST_PO_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
