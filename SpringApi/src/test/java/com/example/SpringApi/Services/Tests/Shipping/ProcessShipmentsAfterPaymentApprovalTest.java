package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * Tests for ShippingService.processShipmentsAfterPaymentApproval.
 */
@DisplayName("ProcessShipmentsAfterPaymentApproval Tests")
class ProcessShipmentsAfterPaymentApprovalTest extends ShippingServiceTestBase {

    // Total Tests: 23
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */
/**
         * Purpose: Verify user log is recorded on success.
         * Expected Result: User log service is called.
         * Assertions: Verify logData called.
         */
        @Test
        @DisplayName("processShipmentsAfterPaymentApproval - Logs User Action - Success")
        void processShipmentsAfterPaymentApproval_LogsUserAction_Success() {
                // Arrange
                stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
                stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
                stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
                stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
                stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
                ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
                stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
                PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
                stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
                stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                                PurchaseOrder.Status.APPROVED.getValue()));
                stubClientRepositoryFindById(testClient);
                stubPickupLocationRepositoryFindById(testPickupLocation);
                stubShipmentRepositorySave(testShipment);
                stubShipRocketHelperCreateCustomOrder(createValidShipRocketOrderResponse());
                stubShipRocketHelperAssignAwbAsJson(createValidAwbJson());
                stubShipRocketHelperGeneratePickupAsJson("{}");
                stubShipRocketHelperGenerateManifest("manifest");
                stubShipRocketHelperGenerateLabel("label");
                stubShipRocketHelperGenerateInvoice("invoice");
                stubShipRocketHelperGetTrackingAsJson("{}");
                stubShipRocketHelperGetOrderDetailsAsJson("{}");

                // Act
                shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

                // Assert
                verify(userLogService).logData(anyLong(), anyString(), anyString());
        }
/**
     * Purpose: Verify shipment save is invoked on online payment success.
     * Expected Result: Shipment repository save is called.
     * Assertions: Verify save called.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Saves Shipment - Success")
    void processShipmentsAfterPaymentApproval_SavesShipment_Success() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        stubShipmentRepositorySave(testShipment);
        stubShipRocketHelperCreateCustomOrder(createValidShipRocketOrderResponse());
        stubShipRocketHelperAssignAwbAsJson(createValidAwbJson());
        stubShipRocketHelperGeneratePickupAsJson("{}");
        stubShipRocketHelperGenerateManifest("manifest");
        stubShipRocketHelperGenerateLabel("label");
        stubShipRocketHelperGenerateInvoice("invoice");
        stubShipRocketHelperGetTrackingAsJson("{}");
        stubShipRocketHelperGetOrderDetailsAsJson("{}");

        // Act
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        verify(shipmentRepository).save(any(Shipment.class));
    }
/**
     * Purpose: Verify successful online payment processing.
     * Expected Result: PaymentVerificationResponseModel indicates success.
     * Assertions: Response success is true.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Valid Request - Success")
    void processShipmentsAfterPaymentApproval_ValidRequest_Success() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        stubShipmentRepositorySave(testShipment);
        ShipRocketOrderResponseModel shipRocketResponse = createValidShipRocketOrderResponse();
        stubShipRocketHelperCreateCustomOrder(shipRocketResponse);
        stubShipRocketHelperAssignAwbAsJson(createValidAwbJson());
        stubShipRocketHelperGeneratePickupAsJson("{}");
        stubShipRocketHelperGenerateManifest("manifest");
        stubShipRocketHelperGenerateLabel("label");
        stubShipRocketHelperGenerateInvoice("invoice");
        stubShipRocketHelperGetTrackingAsJson("{}");
        stubShipRocketHelperGetOrderDetailsAsJson("{}");

        // Act
        PaymentVerificationResponseModel result = shippingService
                .processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        assertTrue(result.isSuccess());
    }
/*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */
/**
     * Purpose: Verify client mismatch throws BadRequestException.
     * Expected Result: BadRequestException with AccessDenied message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Client Mismatch - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ClientMismatch_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setClientId(999L);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
    }
/**
     * Purpose: Verify credentials missing throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Credentials Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testClient.setShipRocketPassword(null);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED, ex.getMessage());
    }
/**
     * Purpose: Verify delivery address missing throws BadRequestException.
     * Expected Result: BadRequestException with DeliveryAddressNotFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Delivery Address Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_DeliveryAddressMissing_ThrowsBadRequestException() {
        // Arrange
        testOrderSummary.setEntityAddress(null);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.DELIVERY_ADDRESS_NOT_FOUND, ex.getMessage());
    }
/**
     * Purpose: Verify order summary not found throws NotFoundException.
     * Expected Result: NotFoundException with OrderSummary not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Order Summary Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_OrderSummaryNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND, ex.getMessage());
    }
/**
     * Purpose: Verify package not available throws BadRequestException.
     * Expected Result: BadRequestException with PackageNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Package Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_PackageNotAvailable_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.PACKAGE_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }
/**
     * Purpose: Verify package stock insufficient throws BadRequestException.
     * Expected Result: BadRequestException with InsufficientPackageStockFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Package Stock Insufficient - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_PackageStockInsufficient_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 0);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.INSUFFICIENT_PACKAGE_STOCK_FORMAT,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 0, testShipmentPackage.getQuantityUsed()), ex.getMessage());
    }
/**
     * Purpose: Verify payment failure throws BadRequestException.
     * Expected Result: BadRequestException with OperationFailedWithMessageFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Payment Failure - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_PaymentFailure_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.failure("fail"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentProcessingErrorMessages.OPERATION_FAILED_WITH_MESSAGE_FORMAT,
                ErrorMessages.OPERATION_FAILED, "fail"), ex.getMessage());
    }
/**
     * Purpose: Verify pickup location not found throws NotFoundException.
     * Expected Result: NotFoundException with PickupLocation not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Pickup Location Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_PickupLocationNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, TEST_PICKUP_LOCATION_ID),
                ex.getMessage());
    }
/**
     * Purpose: Verify purchase order not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - PO Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_PoNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }
/**
     * Purpose: Verify product not available throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Product Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ProductNotAvailable_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.PRODUCT_NOT_AVAILABLE_AT_PICKUP_LOCATION_FORMAT,
                TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }
/**
     * Purpose: Verify empty shipments list throws BadRequestException.
     * Expected Result: BadRequestException with NoShipmentsFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Shipments Empty - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipmentsEmpty_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of());

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NO_SHIPMENTS_FOUND, ex.getMessage());
    }
/**
     * Purpose: Verify invalid status throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Invalid Status - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketInvalidStatus_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.setStatus("INVALID");
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        String validStatuses = String.join(", ",
                java.util.Arrays.stream(Shipment.ShipRocketStatus.values())
                        .map(Shipment.ShipRocketStatus::getValue)
                        .toArray(String[]::new));
        String expectedDetail = String.format(
                ErrorMessages.ShippingErrorMessages.INVALID_SHIP_ROCKET_STATUS_FORMAT,
                "INVALID", validStatuses);
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED,
                TEST_SHIPMENT_ID, expectedDetail), ex.getMessage());
    }
/**
     * Purpose: Verify missing order_id throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Missing Order Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketMissingOrderId_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.setOrderId(null);
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_ID_MISSING), ex.getMessage());
    }
/**
     * Purpose: Verify missing shipment_id throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Missing Shipment Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketMissingShipmentId_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.setShipmentId(null);
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_SHIPMENT_ID_MISSING), ex.getMessage());
    }
/**
     * Purpose: Verify missing status throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Missing Status - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketMissingStatus_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.setStatus("");
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_STATUS_MISSING), ex.getMessage());
    }
/**
     * Purpose: Verify ShipRocket response message throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Response Message - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketResponseMessage_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.setMessage(ErrorMessages.OPERATION_FAILED);
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_ORDER_CREATION_FAILED,
                TEST_SHIPMENT_ID, ErrorMessages.OPERATION_FAILED), ex.getMessage());
    }
/**
     * Purpose: Verify null ShipRocket response throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketApiNullResponse message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - ShipRocket Response Null - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ShipRocketResponseNull_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        stubShipRocketHelperCreateCustomOrder(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_API_NULL_RESPONSE, TEST_SHIPMENT_ID),
                ex.getMessage());
    }
/**
     * Purpose: Verify status not pending throws BadRequestException.
     * Expected Result: BadRequestException with OnlyPendingApprovalCanBePaid message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Status Not Pending - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_StatusNotPending_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID, ex.getMessage());
    }
/*
         **********************************************************************************************
         * PERMISSION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify unauthorized access is blocked at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("processShipmentsAfterPaymentApproval - Controller Permission - Unauthorized")
        void processShipmentsAfterPaymentApproval_controller_permission_unauthorized() {
                // Arrange
                razorpayRequest.setPurchaseOrderId(1L); // Set purchase order ID so stub can match
                com.example.SpringApi.Models.RequestModels.ProcessPaymentAndShipmentRequestModel request =
                        new com.example.SpringApi.Models.RequestModels.ProcessPaymentAndShipmentRequestModel();
                request.setIsCashPayment(false);
                request.setOnlinePaymentRequest(razorpayRequest);
                com.example.SpringApi.Controllers.PaymentController controller =
                        new com.example.SpringApi.Controllers.PaymentController(paymentService, shippingServiceControllerMock);
                stubShippingServiceProcessShipmentsAfterPaymentApprovalUnauthorizedOnline();

                // Act
                ResponseEntity<?> response = controller.processPaymentAndShipments(request);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        /**
         * Purpose: Verify controller has @PreAuthorize for processPaymentAndShipments.
         * Expected Result: Annotation exists and includes UPDATE_PURCHASE_ORDERS_PERMISSION.
         * Assertions: Annotation is present and contains permission.
         */
        @Test
        @DisplayName("processShipmentsAfterPaymentApproval - Verify @PreAuthorize Annotation")
        void processShipmentsAfterPaymentApproval_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
                // Arrange
                Method method = com.example.SpringApi.Controllers.PaymentController.class
                        .getMethod("processPaymentAndShipments", com.example.SpringApi.Models.RequestModels.ProcessPaymentAndShipmentRequestModel.class);

                // Act
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

                // Assert
                assertNotNull(annotation);
                assertTrue(annotation.value().contains(com.example.SpringApi.Models.Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
        }
}
