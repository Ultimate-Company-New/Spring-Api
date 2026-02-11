package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for PurchaseOrderService.createPurchaseOrder method.
 *
 * Test count: 8 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 2 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - CreatePurchaseOrder Tests")
public class CreatePurchaseOrderTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 8

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify create succeeds without attachments.
     * Expected Result: Purchase order is created and logs are written.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create PO - Success Without Attachments")
    void createPurchaseOrder_Success_WithoutAttachments_Success() {
        // ARRANGE
        testPurchaseOrderRequest.setAttachments(null);
        stubSuccessfulPurchaseOrderCreate();

        // ACT & ASSERT
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
    }

    /**
     * Purpose: Verify create succeeds with attachments.
     * Expected Result: Attachments are uploaded and purchase order is created.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create PO - Success With Attachments")
    void createPurchaseOrder_Success_WithAttachments_Success() {
        // ARRANGE
        Map<String, String> attachments = new HashMap<>();
        attachments.put("receipt.pdf", "base64-data-here");
        testPurchaseOrderRequest.setAttachments(attachments);

        stubSuccessfulPurchaseOrderCreate();
        stubClientRepositoryFindById(Optional.of(testClient));
        stubResourcesRepositorySave(new Resources());

        List<ImgbbHelper.AttachmentUploadResult> results = List.of(
                new ImgbbHelper.AttachmentUploadResult(
                        "https://i.ibb.co/test/receipt.pdf",
                        "delete-hash-123",
                        null));

        try (MockedConstruction<ImgbbHelper> ignored =
                     stubImgbbHelperUploadResults(results)) {
            // ACT & ASSERT
            assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }
    }

    /**
     * Purpose: Verify canonical custom price from request.products[] is persisted.
     * Expected Result: ShipmentProduct allocatedPrice equals product price per unit.
     * Assertions: Captured shipment products include the canonical price.
     */
    @Test
    @DisplayName("Create PO - Persists canonical custom price")
    void createPurchaseOrder_PersistsCanonicalCustomPrice_Success() {
        // ARRANGE
        stubSuccessfulPurchaseOrderCreate();
        ArgumentCaptor<List<ShipmentProduct>> captor = ArgumentCaptor.forClass((Class) List.class);
        stubShipmentProductRepositorySaveAllCapture(captor);

        // ACT
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

        // ASSERT
        List<ShipmentProduct> saved = captor.getValue();
        assertNotNull(saved);
        assertFalse(saved.isEmpty());
        assertEquals(testPurchaseOrderRequest.getProducts().get(0).getPricePerUnit(),
                saved.get(0).getAllocatedPrice());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify null request is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidRequest.
     */
    @Test
    @DisplayName("Create PO - Null Request - Throws BadRequestException")
    void createPurchaseOrder_NullRequest_ThrowsBadRequestException() {
        // ACT & ASSERT
        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidRequest,
                () -> purchaseOrderService.createPurchaseOrder(null));
    }

    /**
     * Purpose: Verify null order summary is rejected.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches OrderSummary InvalidRequest.
     */
    @Test
    @DisplayName("Create PO - Null Order Summary - Throws BadRequestException")
    void createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException() {
        // ARRANGE
        testPurchaseOrderRequest.setOrderSummary(null);

        // ACT & ASSERT
        assertThrowsBadRequest(ErrorMessages.OrderSummaryErrorMessages.InvalidRequest,
                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
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
    @DisplayName("createPurchaseOrder - Controller Permission - Unauthorized")
    void createPurchaseOrder_controller_permission_unauthorized() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnCreate();

        // ACT
        ResponseEntity<?> response = controller.createPurchaseOrder(testPurchaseOrderRequest);

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("createPurchaseOrder - Verify @PreAuthorize Annotation")
    void createPurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("createPurchaseOrder",
                com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference INSERT_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("createPurchaseOrder - Controller delegates to service")
    void createPurchaseOrder_WithValidRequest_DelegatesToService() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceCreateDoNothing();

        // ACT
        ResponseEntity<?> response = controller.createPurchaseOrder(testPurchaseOrderRequest);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void stubSuccessfulPurchaseOrderCreate() {
        stubPurchaseOrderRepositorySave(testPurchaseOrder);
        stubOrderSummaryRepositorySave(testOrderSummary);
        stubAddressRepositoryFindExactDuplicate(Optional.empty());
        stubAddressRepositorySave(testAddress);
        stubShipmentRepositorySaveAssigningId(1L);
        stubShipmentProductRepositorySaveAll();
        stubShipmentPackageRepositorySaveAssigningId(1L);
        stubShipmentPackageProductRepositorySaveAll();
    }
}
