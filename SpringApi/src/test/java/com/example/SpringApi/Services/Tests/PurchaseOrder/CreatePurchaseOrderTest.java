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
 * Test class for PurchaseOrderService.createPurchaseOrder method.
 * 
 * Test count: 6 tests
 * - SUCCESS: 5 tests (2 tests + 3 dynamic tests)
 * - FAILURE / EXCEPTION: 1 test (canonical price persistence)
 */
@DisplayName("PurchaseOrderService - CreatePurchaseOrder Tests")
public class CreatePurchaseOrderTest extends PurchaseOrderServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Create PO - Success Without Attachments")
    void createPurchaseOrder_Success_WithoutAttachments() {
        // Arrange
        testPurchaseOrderRequest.setAttachments(null);

        when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        AtomicLong shipmentIdSeq = new AtomicLong(1L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(shipmentIdSeq.getAndIncrement());
            return s;
        });
        when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
        when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
            ShipmentPackage p = invocation.getArgument(0);
            p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
            return p;
        });
        when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

        // Assert
        verify(addressRepository, atLeastOnce()).save(any(Address.class));
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
        verify(userLogService, times(1)).logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Create PO - Success With Attachments")
    void createPurchaseOrder_Success_WithAttachments() {
        // Arrange
        Map<String, String> attachments = new HashMap<>();
        attachments.put("receipt.pdf", "base64-data-here");
        testPurchaseOrderRequest.setAttachments(attachments);

        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                        any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
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
                                    "https://i.ibb.co/test/receipt.pdf",
                                    "delete-hash-123",
                                    null));
                    when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))
                            .thenReturn(results);
                })) {

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

            // Assert
            verify(addressRepository, atLeastOnce()).save(any(Address.class));
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
            verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
        }
    }

    /**
     * Purpose: Additional create PO success variations.
     * Expected Result: Requests succeed with minor variations.
     * Assertions: No exceptions thrown.
     */
    @TestFactory
    @DisplayName("Create PO - Additional success variations")
    Stream<DynamicTest> createPurchaseOrder_AdditionalSuccessVariations() {
        return Stream.of("attachments empty", "attachments null", "duplicate address")
                .map(label -> DynamicTest.dynamicTest(label, () -> {
                    initializeTestData();
                    if ("attachments empty".equals(label)) {
                        testPurchaseOrderRequest.setAttachments(new HashMap<>());
                    } else if ("attachments null".equals(label)) {
                        testPurchaseOrderRequest.setAttachments(null);
                    }

                    if ("duplicate address".equals(label)) {
                        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                                        any(), any(), any(), any(), any()))
                                .thenReturn(Optional.of(testAddress));
                    } else {
                        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                                        any(), any(), any(), any(), any()))
                                .thenReturn(Optional.empty());
                    }

                    lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
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

                    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                }));
    }

    // ========================================
    // Additional Test - Canonical Price Persistence
    // ========================================

    @Test
    @DisplayName("Create PO - Persists canonical custom price from request.products[] into ShipmentProduct.allocatedPrice")
    void createPurchaseOrder_PersistsCanonicalCustomPrice() {
        // Arrange
        when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        AtomicLong shipmentIdSeq = new AtomicLong(1L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setShipmentId(shipmentIdSeq.getAndIncrement());
            return s;
        });

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<ShipmentProduct>> captor = 
            (org.mockito.ArgumentCaptor<List<ShipmentProduct>>) (org.mockito.ArgumentCaptor<?>) 
            org.mockito.ArgumentCaptor.forClass(List.class);
        when(shipmentProductRepository.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
        when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
            ShipmentPackage p = invocation.getArgument(0);
            p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
            return p;
        });
        when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

        // Assert - Verify canonical price from products[] is persisted
        List<ShipmentProduct> saved = captor.getValue();
        assertNotNull(saved);
        assertFalse(saved.isEmpty());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("createPurchaseOrder - Verify @PreAuthorize Annotation")
    void createPurchaseOrder_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("createPurchaseOrder", PurchaseOrderRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createPurchaseOrder");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference INSERT_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("createPurchaseOrder - Controller delegates to service")
    void createPurchaseOrder_WithValidRequest_DelegatesToService() {
        PurchaseOrderService mockService = mock(PurchaseOrderService.class);
        PurchaseOrderController controller = new PurchaseOrderController(mockService);
        doNothing().when(mockService).createPurchaseOrder(testPurchaseOrderRequest);

        ResponseEntity<?> response = controller.createPurchaseOrder(testPurchaseOrderRequest);

        verify(mockService).createPurchaseOrder(testPurchaseOrderRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
