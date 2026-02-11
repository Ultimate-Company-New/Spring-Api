package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.Authorizations;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PurchaseOrderService.updatePurchaseOrder method.
 * 
 * Test count: 8 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 7 tests (2 tests + 5 dynamic tests)
 */
@DisplayName("PurchaseOrderService - UpdatePurchaseOrder Tests")
public class UpdatePurchaseOrderTest extends PurchaseOrderServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Update PO - Success With Attachments")
    void updatePurchaseOrder_Success() {
        // Arrange
        Map<String, String> attachments = new HashMap<>();
        attachments.put("invoice.pdf", "base64-data-here");
        testPurchaseOrderRequest.setAttachments(attachments);

        lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);

        // Existing resources for cleanup
        Resources existingResource = new Resources();
        existingResource.setResourceId(100L);
        existingResource.setKey("old-receipt.pdf");
        existingResource.setDeleteHashValue("old-delete-hash");
        existingResource.setValue("https://i.ibb.co/old/receipt.pdf");
        lenient().when(resourcesRepository.findByEntityIdAndEntityType(anyLong(), anyString()))
                .thenReturn(Arrays.asList(existingResource));

        AtomicLong shipmentIdSeq = new AtomicLong(1L);
        lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            if (s.getShipmentId() == null) {
                s.setShipmentId(shipmentIdSeq.getAndIncrement());
            }
            return s;
        });
        lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
        lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
            ShipmentPackage p = invocation.getArgument(0);
            if (p.getShipmentPackageId() == null) {
                p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
            }
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
                    when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))
                            .thenReturn(results);
                    when(mock.deleteMultipleImages(anyList())).thenReturn(1);
                })) {

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

            // Assert
            verify(purchaseOrderRepository, atLeastOnce()).save(any(PurchaseOrder.class));
            verify(orderSummaryRepository, atLeastOnce()).save(any(OrderSummary.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Update PO - Not Found")
    void updatePurchaseOrder_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound("Invalid purchase order Id.", () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
    }

    @Test
    @DisplayName("Update PO - Client Missing For Attachment Cleanup")
    void updatePurchaseOrder_ClientMissingForCleanup() {
        // Arrange
        Map<String, String> attachments = new HashMap<>();
        attachments.put("doc.pdf", "base64-data");
        testPurchaseOrderRequest.setAttachments(attachments);

        Resources existingResource = new Resources();
        existingResource.setResourceId(200L);
        existingResource.setDeleteHashValue("hash-to-delete");
        lenient().when(resourcesRepository.findByEntityIdAndEntityType(anyLong(), anyString()))
                .thenReturn(Arrays.asList(existingResource));

        lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                eq(TEST_PO_ID), anyLong())).thenReturn(Optional.of(testPurchaseOrder));
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound("Invalid Client Id.", () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
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
                    lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(
                            eq(id), anyLong())).thenReturn(Optional.empty());

                    assertThrowsNotFound("Invalid purchase order Id.", () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("updatePurchaseOrder - Verify @PreAuthorize Annotation")
    void updatePurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("updatePurchaseOrder", PurchaseOrderRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updatePurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("updatePurchaseOrder - Controller delegates to service")
    void updatePurchaseOrder_WithValidRequest_DelegatesToService() {
        PurchaseOrderService mockService = mock(PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doNothing().when(mockService).updatePurchaseOrder(testPurchaseOrderRequest);

        ResponseEntity<?> response = controller.updatePurchaseOrder(testPurchaseOrderRequest);

        verify(mockService).updatePurchaseOrder(testPurchaseOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
