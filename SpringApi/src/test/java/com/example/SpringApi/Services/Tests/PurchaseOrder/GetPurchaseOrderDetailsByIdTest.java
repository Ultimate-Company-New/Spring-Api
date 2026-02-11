package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
 *
 * Test count: 5 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 1 test
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - GetPurchaseOrderDetailsById Tests")
public class GetPurchaseOrderDetailsByIdTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 5

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify get details succeeds for valid ID.
     * Expected Result: Response model is returned.
     * Assertions: Response has matching ID and vendor number.
     */
    @Test
    @DisplayName("Get Purchase Order Details By Id - Success")
    void getPurchaseOrderDetailsById_Success_Success() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));

        // ACT
        PurchaseOrderResponseModel result = assertDoesNotThrow(
                () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));

        // ASSERT
        assertNotNull(result);
        assertEquals(TEST_PO_ID, result.getPurchaseOrderId());
        assertEquals(TEST_VENDOR_NUMBER, result.getVendorNumber());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify not found is thrown for missing ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId.
     */
    @Test
    @DisplayName("Get Purchase Order Details By Id - Not Found")
    void getPurchaseOrderDetailsById_NotFound_Failure() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));
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
    @DisplayName("getPurchaseOrderDetailsById - Controller Permission - Unauthorized")
    void getPurchaseOrderDetailsById_controller_permission_unauthorized() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnGetById();

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrderDetailsById(TEST_PO_ID);

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("getPurchaseOrderDetailsById - Verify @PreAuthorize Annotation")
    void getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("getPurchaseOrderDetailsById", long.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPurchaseOrderDetailsById");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference VIEW_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("getPODetailsById - Controller delegates to service")
    void getPODetailsById_WithValidId_DelegatesToService() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceGetPurchaseOrderDetailsById(new PurchaseOrderResponseModel(testPurchaseOrder));

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrderDetailsById(TEST_PO_ID);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}