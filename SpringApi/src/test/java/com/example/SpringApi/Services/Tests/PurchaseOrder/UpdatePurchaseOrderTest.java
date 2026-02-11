package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Services.PurchaseOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test class for PurchaseOrderService.updatePurchaseOrder method.
 *
 * Test count: 7 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 3 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - UpdatePurchaseOrder Tests")
public class UpdatePurchaseOrderTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 7

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify update succeeds with attachments.
     * Expected Result: Attachments are uploaded, resources saved, and update completes.
     * Assertions: Repository saves and logging are performed.
     */
    @Test
    @DisplayName("Update PO - Success With Attachments")
    void updatePurchaseOrder_WithAttachments_Success() {
        // ARRANGE
        Map<String, String> attachments = new HashMap<>();
        attachments.put("invoice.pdf", "base64-data");
        testPurchaseOrderRequest.setAttachments(attachments);

        Resources existingResource = new Resources();
        existingResource.setResourceId(200L);
        existingResource.setDeleteHashValue("hash-to-delete");
        stubResourcesRepositoryFindByEntityIdAndEntityType(Arrays.asList(existingResource));
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubClientRepositoryFindById(Optional.of(testClient));

        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        lenient().when(addressRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Address.class))).thenReturn(testAddress);
        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        AtomicLong shipmentIdSeq = new AtomicLong(1L);
        lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(shipmentIdSeq.getAndIncrement());
            return s;
        });
        lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
        lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
            ShipmentPackage p = invocation.getArgument(0);
            p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
            return p;
        });
        lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    List<ImgbbHelper.AttachmentUploadResult> results = Arrays.asList(
                            new ImgbbHelper.AttachmentUploadResult(
                                    "https://i.ibb.co/test/invoice.pdf",
                                    "delete-hash-456",
                                    null));
                    lenient().when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))
                            .thenReturn(results);
                    lenient().when(mock.deleteMultipleImages(anyList())).thenReturn(1);
                })) {

            // ACT
            assertDoesNotThrow(() -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

            // ASSERT
            verify(purchaseOrderRepository, atLeastOnce()).save(any(PurchaseOrder.class));
            verify(orderSummaryRepository, atLeastOnce()).save(any(OrderSummary.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }
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
    @DisplayName("Update PO - Not Found")
    void updatePurchaseOrder_NotFound() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindById(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
    }

    /**
     * Purpose: Verify client missing during attachment cleanup is rejected.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId.
     */
    @Test
    @DisplayName("Update PO - Client Missing For Attachment Cleanup")
    void updatePurchaseOrder_ClientMissingForCleanup() {
        // ARRANGE
        Map<String, String> attachments = new HashMap<>();
        attachments.put("doc.pdf", "base64-data");
        testPurchaseOrderRequest.setAttachments(attachments);

        Resources existingResource = new Resources();
        existingResource.setResourceId(200L);
        existingResource.setDeleteHashValue("hash-to-delete");
        stubResourcesRepositoryFindByEntityIdAndEntityType(Arrays.asList(existingResource));
        stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
        stubClientRepositoryFindById(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
    }

    /**
     * Purpose: Update PO with various invalid ID scenarios.
     * Expected Result: Throws NotFoundException with Invalid purchase order Id. message.
     * Assertions: Correct exception message.
     */
    @TestFactory
    @DisplayName("Update PO - Invalid ID variations")
    Stream<DynamicTest> updatePurchaseOrder_InvalidIds() {
        return Stream.of(0L, -1L, -100L, Long.MAX_VALUE, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("ID=" + id, () -> {
                    initializeTestData();
                    lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(eq(id), anyLong()))
                            .thenReturn(Optional.empty());

                    assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                            () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
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
    @DisplayName("updatePurchaseOrder - Controller Permission - Unauthorized")
    void updatePurchaseOrder_controller_permission_unauthorized() {
        // ARRANGE
        PurchaseOrderService mockService = mock(PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockService).updatePurchaseOrder(any(PurchaseOrderRequestModel.class));

        // ACT
        ResponseEntity<?> response = controller.updatePurchaseOrder(testPurchaseOrderRequest);

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("updatePurchaseOrder - Verify @PreAuthorize Annotation")
    void updatePurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("updatePurchaseOrder", PurchaseOrderRequestModel.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updatePurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("updatePurchaseOrder - Controller delegates to service")
    void updatePurchaseOrder_WithValidRequest_DelegatesToService() {
        // ARRANGE
        PurchaseOrderService mockService = mock(PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doNothing().when(mockService).updatePurchaseOrder(testPurchaseOrderRequest);

        // ACT
        ResponseEntity<?> response = controller.updatePurchaseOrder(testPurchaseOrderRequest);

        // ASSERT
        verify(mockService).updatePurchaseOrder(testPurchaseOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
