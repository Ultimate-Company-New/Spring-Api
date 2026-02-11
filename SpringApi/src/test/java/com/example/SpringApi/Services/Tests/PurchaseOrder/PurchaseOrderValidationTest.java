package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;

/**
 * Test class for PurchaseOrderService validation tests.
 * 
 * Test count: 22 tests (16 tests + 11 dynamic tests in @TestFactory = 27 total dynamic executions)
 * - All tests are FAILURE / EXCEPTION scenarios testing validation
 */
@DisplayName("PurchaseOrderService - Validation Tests")
public class PurchaseOrderValidationTest extends PurchaseOrderServiceTestBase {

    @Nested
    @DisplayName("Create Purchase Order - Validation Tests")
    class CreatePOValidationTests {

        @Test
        @DisplayName("Create PO - Null Request - Throws BadRequestException")
        void createPurchaseOrder_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidRequest,
                    () -> purchaseOrderService.createPurchaseOrder(null));
        }

        @Test
        @DisplayName("Create PO - Null Order Summary - Throws BadRequestException")
        void createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setOrderSummary(null);
            assertThrowsBadRequest(ErrorMessages.OrderSummaryErrorMessages.InvalidRequest,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Max Attachments Exceeded - Throws BadRequestException")
        void createPurchaseOrder_MaxAttachmentsExceeded_ThrowsBadRequestException() {
            Map<String, String> attachments = new HashMap<>();
            for (int i = 0; i < 31; i++) {
                attachments.put("file" + i + ".txt", "data");
            }
            testPurchaseOrderRequest.setAttachments(attachments);

                assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.MaxAttachmentsExceeded,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Null Products List - Throws BadRequestException")
        void createPurchaseOrder_NullProducts_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setProducts(null);
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.ER004,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Empty Products List - Throws BadRequestException")
        void createPurchaseOrder_EmptyProducts_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setProducts(new ArrayList<>());
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.ER004,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Invalid Product ID - Throws BadRequestException")
        void createPurchaseOrder_InvalidProductId_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setProductId(0L);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidId,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Invalid Quantity - Throws BadRequestException")
        void createPurchaseOrder_InvalidQuantity_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setQuantity(0);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));
        }

        @Test
        @DisplayName("Create PO - Null Price Per Unit - Throws BadRequestException")
        void createPurchaseOrder_NullPricePerUnit_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("pricePerUnit is required"));
        }

        @Test
        @DisplayName("Create PO - Negative Price Per Unit - Throws BadRequestException")
        void createPurchaseOrder_NegativePricePerUnit_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(new BigDecimal("-1.00"));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("pricePerUnit must be greater than or equal to 0"));
        }

        @Test
        @DisplayName("Create PO - Null Order Status - Throws BadRequestException")
        void createPurchaseOrder_NullOrderStatus_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus(null);
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Empty Order Status - Throws BadRequestException")
        void createPurchaseOrder_EmptyOrderStatus_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus("");
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Invalid Order Status Value - Throws BadRequestException")
        void createPurchaseOrder_InvalidOrderStatusValue_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus("INVALID_STATUS");
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatusValue,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Null Assigned Lead ID - Throws BadRequestException")
        void createPurchaseOrder_NullAssignedLeadId_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setAssignedLeadId(null);
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidAssignedLeadId,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - No Shipments - Throws BadRequestException")
        void createPurchaseOrder_NoShipments_ThrowsBadRequestException() {
            lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);

            testPurchaseOrderRequest.setShipments(null);
                assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.AtLeastOneShipmentRequired,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Shipment Missing Courier - Throws BadRequestException")
        void createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException() {
            lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);

            testPurchaseOrderRequest.getShipments().get(0).setSelectedCourier(null);
                assertThrowsBadRequest(ErrorMessages.ShipmentErrorMessages.CourierSelectionRequired,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Shipment Missing Packages - Throws BadRequestException")
        void createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException() {
            lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            Shipment savedShipment = new Shipment();
            savedShipment.setShipmentId(1L);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);

            testPurchaseOrderRequest.getShipments().get(0).setPackages(null);
                assertThrowsBadRequest(ErrorMessages.ShipmentPackageErrorMessages.AtLeastOnePackageRequired,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        @Test
        @DisplayName("Create PO - Package Missing Products - Throws BadRequestException")
        void createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException() {
            lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            Shipment savedShipment = new Shipment();
            savedShipment.setShipmentId(1L);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);
            lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class)))
                    .thenReturn(new ShipmentPackage());

            testPurchaseOrderRequest.getShipments().get(0).getPackages().get(0).setProducts(null);
                assertThrowsBadRequest(ErrorMessages.ShipmentPackageProductErrorMessages.AtLeastOneProductRequired,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
        }

        /**
         * Purpose: Additional validation coverage for createPurchaseOrder.
         * Expected Result: Invalid inputs throw BadRequestException.
         * Assertions: Exception messages match expected errors.
         */
        @TestFactory
        @DisplayName("Create PO - Additional validation cases")
        Stream<DynamicTest> createPurchaseOrder_AdditionalValidationCases() {
            lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment shipment = invocation.getArgument(0);
                shipment.setShipmentId(1L);
                return shipment;
            });
            lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
                ShipmentPackage shipmentPackage = invocation.getArgument(0);
                shipmentPackage.setShipmentPackageId(1L);
                return shipmentPackage;
            });
            lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            return Stream.of(
                    DynamicTest.dynamicTest("Null vendor number", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.setVendorNumber(null);
                        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Empty vendor number", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.setVendorNumber(" ");
                        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Negative product ID", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.getProducts().get(0).setProductId(-1L);
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidId,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Negative quantity", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.getProducts().get(0).setQuantity(-1);
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                        assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));
                    }),
                    DynamicTest.dynamicTest("Price per unit negative", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(new BigDecimal("-10.00"));
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                        assertTrue(ex.getMessage().contains("pricePerUnit must be greater than or equal to 0"));
                    }),
                    DynamicTest.dynamicTest("Order status whitespace", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.setPurchaseOrderStatus(" ");
                        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Assigned lead ID zero", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.setAssignedLeadId(0L);
                        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Shipments empty list", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.setShipments(new ArrayList<>());
                        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.AtLeastOneShipmentRequired,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Shipment packages empty list", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.getShipments().get(0).setPackages(new ArrayList<>());
                        assertThrowsBadRequest(ErrorMessages.ShipmentPackageErrorMessages.AtLeastOnePackageRequired,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Package products empty list", () -> {
                        initializeTestData();
                        testPurchaseOrderRequest.getShipments().get(0).getPackages().get(0)
                                .setProducts(new ArrayList<>());
                        assertThrowsBadRequest(ErrorMessages.ShipmentPackageProductErrorMessages.AtLeastOneProductRequired,
                            () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    }),
                    DynamicTest.dynamicTest("Max attachments exceeded (100)", () -> {
                        initializeTestData();
                        Map<String, String> attachments = new HashMap<>();
                        for (int i = 0; i < 100; i++) {
                            attachments.put("file" + i + ".txt", "data");
                        }
                        testPurchaseOrderRequest.setAttachments(attachments);
                        assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.MaxAttachmentsExceeded,
                                () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
                    })
            );
        }
    }
}
