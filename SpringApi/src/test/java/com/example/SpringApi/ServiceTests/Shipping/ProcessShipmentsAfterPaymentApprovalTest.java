package com.example.SpringApi.ServiceTests.Shipping;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.RequestModels.ShipRocketOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    // Total Tests: 48
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

    /**
     * Purpose: Verify successful cash payment shipment processing.
     * Expected Result: PaymentVerificationResponseModel indicates success.
     * Assertions: Response success is true.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Valid Request - Success")
    void processShipmentsAfterPaymentApproval_CashValidRequest_Success() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("cash", TEST_PURCHASE_ORDER_ID,
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
        PaymentVerificationResponseModel result = shippingService
                .processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest);

        // Assert
        assertTrue(result.isSuccess());
    }

    /**
     * Purpose: Verify order payload includes pricing/tax/receipt fields when configured.
     * Expected Result: Shipment processing succeeds.
     * Assertions: Response success is true.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Pricing And Receipt Fields - Success")
    void processShipmentsAfterPaymentApproval_PricingAndReceiptFields_Success() {
        // Arrange
        testDeliveryAddress.setNameOnAddress("John Doe");
        testOrderSummary.setTotalShipping(new java.math.BigDecimal("40"));
        testOrderSummary.setTotalDiscount(new java.math.BigDecimal("10"));
        testOrderSummary.setGstPercentage(new java.math.BigDecimal("18"));
        testPurchaseOrder.setPurchaseOrderReceipt("INV-001");
        testProduct.setDiscount(new java.math.BigDecimal("10"));
        testProduct.setIsDiscountPercent(true);
        testShipmentProduct.setAllocatedPrice(new java.math.BigDecimal("200"));

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
        stubProductRepositoryFindById(testProduct);
        stubShipRocketHelperCreateCustomOrder(createValidShipRocketOrderResponse());
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

    /**
     * Purpose: Verify cash payment failure throws BadRequestException.
     * Expected Result: BadRequestException with OperationFailedWithMessageFormat.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Payment Failure - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_CashPaymentFailure_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.failure("cash-failed"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentProcessingErrorMessages.OPERATION_FAILED_WITH_MESSAGE_FORMAT,
                ErrorMessages.OPERATION_FAILED, "cash-failed"), ex.getMessage());
    }

    /**
     * Purpose: Verify missing pickup location nickname throws BadRequestException.
     * Expected Result: BadRequestException with PickupLocationNameNotConfigured.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Pickup Name Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_PickupNameMissing_ThrowsBadRequestException() {
        // Arrange
        testPickupLocation.setAddressNickName(" ");
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

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.PICKUP_LOCATION_NAME_NOT_CONFIGURED,
                TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify invalid billing postal code throws BadRequestException.
     * Expected Result: BadRequestException with BillingPostalCodeMustBeNumeric.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Invalid Billing Postal - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_InvalidBillingPostal_ThrowsBadRequestException() {
        // Arrange
        testDeliveryAddress.setPostalCode("40A001");
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

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.BILLING_POSTAL_CODE_MUST_BE_NUMERIC, "40A001"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify invalid billing phone throws BadRequestException.
     * Expected Result: BadRequestException with BillingPhoneMustBe10Digits.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Invalid Billing Phone - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_InvalidBillingPhone_ThrowsBadRequestException() {
        // Arrange
        testDeliveryAddress.setPhoneOnAddress("12345");
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

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.BILLING_PHONE_MUST_BE10_DIGITS, "12345"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify AWB assignment exception throws BadRequestException.
     * Expected Result: BadRequestException with AwbAssignmentFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - AWB Assign Exception - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_AwbAssignException_ThrowsBadRequestException() {
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
        stubShipRocketHelperAssignAwbAsJsonThrows(new RuntimeException("awb-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.AWB_ASSIGNMENT_FAILED,
                shipRocketResponse.getShipmentId(), "awb-error"), ex.getMessage());
    }

    /**
     * Purpose: Verify order-details fallback serialization path still succeeds.
     * Expected Result: Shipment is saved with fallback full response and AWB from response.
     * Assertions: Saved shipment contains full response and awb code.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Order Details Fallback - Success")
    void processShipmentsAfterPaymentApproval_OrderDetailsFallback_Success() {
        // Arrange
        testShipment.setSelectedCourierCompanyId(null);
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
        stubShipRocketHelperGeneratePickupAsJson("{}");
        stubShipRocketHelperGenerateManifest("manifest");
        stubShipRocketHelperGenerateLabel("label");
        stubShipRocketHelperGenerateInvoice("invoice");
        stubShipRocketHelperGetTrackingAsJson("{}");
        stubShipRocketHelperGetOrderDetailsAsJsonThrows(new RuntimeException("order-details-error"));

        // Act
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(captor.capture());
        Shipment savedShipment = captor.getValue();
        assertEquals("AWB-1", savedShipment.getShipRocketAwbCode());
        assertNotNull(savedShipment.getShipRocketFullResponse());
    }

    /**
     * Purpose: Verify cash flow throws when purchase order is not found.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash PO Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_CashPoNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify cash flow rejects purchase orders belonging to another client.
     * Expected Result: BadRequestException with AccessDeniedToPurchaseOrder.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Client Mismatch - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_CashClientMismatch_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setClientId(999L);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.ACCESS_DENIED_TO_PURCHASE_ORDER, ex.getMessage());
    }

    /**
     * Purpose: Verify cash flow rejects non-pending purchase order status.
     * Expected Result: BadRequestException with OnlyPendingApprovalCanBePaid.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Status Not Pending - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_CashStatusNotPending_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.ONLY_PENDING_APPROVAL_CAN_BE_PAID, ex.getMessage());
    }

    /**
     * Purpose: Verify cash flow throws when order summary is missing.
     * Expected Result: NotFoundException with OrderSummary not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Order Summary Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_CashOrderSummaryNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify cash flow throws when no shipments exist for order summary.
     * Expected Result: BadRequestException with NoShipmentsFound.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Cash Shipments Empty - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_CashShipmentsEmpty_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of());

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NO_SHIPMENTS_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify insufficient product stock at pickup location throws BadRequestException.
     * Expected Result: BadRequestException with InsufficientProductStockFormat.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Product Stock Insufficient - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ProductStockInsufficient_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 0);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.INSUFFICIENT_PRODUCT_STOCK_FORMAT,
                TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 0, testShipmentProduct.getAllocatedQuantity()), ex.getMessage());
    }

    /**
     * Purpose: Verify client lookup failure during ShipRocket processing throws NotFoundException.
     * Expected Result: NotFoundException with Client invalid id message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Client Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubClientRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify package lookup failure while building ShipRocket request throws NotFoundException.
     * Expected Result: NotFoundException with package id message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Package Missing - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_PackageMissing_ThrowsNotFoundException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubPackageRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.PackageErrorMessages.INVALID_ID_WITH_ID_FORMAT, TEST_PACKAGE_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify order request uses default dimensions when shipment has no package entries.
     * Expected Result: Shipment processing succeeds with default LxBxH dimensions.
     * Assertions: Captured request dimensions are 10.0 each.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - No Packages Uses Default Dimensions - Success")
    void processShipmentsAfterPaymentApproval_NoPackagesUsesDefaultDimensions_Success() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipmentPackageRepositoryFindByShipmentId(List.of());
        stubProductRepositoryFindById(testProduct);

        // Act
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        ArgumentCaptor<ShipRocketOrderRequestModel> requestCaptor = ArgumentCaptor.forClass(ShipRocketOrderRequestModel.class);
        verify(shipRocketHelper).createCustomOrder(requestCaptor.capture());
        ShipRocketOrderRequestModel captured = requestCaptor.getValue();
        assertEquals(10.0, captured.getLength());
        assertEquals(10.0, captured.getBreadth());
        assertEquals(10.0, captured.getHeight());
    }

    /**
     * Purpose: Verify null billing phone produces explicit validation error.
     * Expected Result: BadRequestException with BillingPhoneMustBe10Digits using "empty".
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Billing Phone Null - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_BillingPhoneNull_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        testDeliveryAddress.setPhoneOnAddress(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.BILLING_PHONE_MUST_BE10_DIGITS, "empty"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify phone numbers longer than 10 digits are normalized to the last 10.
     * Expected Result: Shipment processing succeeds and billing phone is normalized.
     * Assertions: Captured request billing phone equals normalized value.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Billing Phone Normalized - Success")
    void processShipmentsAfterPaymentApproval_BillingPhoneNormalized_Success() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubProductRepositoryFindById(testProduct);
        testDeliveryAddress.setPhoneOnAddress("+91-9876543210");

        // Act
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        ArgumentCaptor<ShipRocketOrderRequestModel> requestCaptor = ArgumentCaptor.forClass(ShipRocketOrderRequestModel.class);
        verify(shipRocketHelper).createCustomOrder(requestCaptor.capture());
        assertEquals(9876543210L, requestCaptor.getValue().getBillingPhone());
    }

    /**
     * Purpose: Verify null/blank address fields use safe fallback values in ShipRocket request.
     * Expected Result: Shipment processing succeeds and request fields are populated with defaults.
     * Assertions: Captured request contains expected fallback values.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Address Fallback Fields - Success")
    void processShipmentsAfterPaymentApproval_AddressFallbackFields_Success() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubProductRepositoryFindById(testProduct);
        testDeliveryAddress.setNameOnAddress(null);
        testDeliveryAddress.setStreetAddress(null);
        testDeliveryAddress.setStreetAddress2(" ");
        testDeliveryAddress.setCity(null);
        testDeliveryAddress.setState(null);
        testDeliveryAddress.setCountry(null);
        testDeliveryAddress.setEmailOnAddress(null);
        testDeliveryAddress.setPhoneOnAddress("9999999999");

        // Act
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest);

        // Assert
        ArgumentCaptor<ShipRocketOrderRequestModel> requestCaptor = ArgumentCaptor.forClass(ShipRocketOrderRequestModel.class);
        verify(shipRocketHelper).createCustomOrder(requestCaptor.capture());
        ShipRocketOrderRequestModel captured = requestCaptor.getValue();
        assertEquals("", captured.getBillingCustomerName());
        assertEquals("", captured.getBillingAddress());
        assertEquals("", captured.getBillingCity());
        assertEquals("", captured.getBillingState());
        assertEquals("", captured.getBillingCountry());
        assertNull(captured.getShippingEmail());
    }

    /**
     * Purpose: Verify pickup-generation API failure is converted to domain exception.
     * Expected Result: BadRequestException with PickupGenerationFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Pickup Generation Error - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_PickupGenerationError_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipRocketHelperGeneratePickupAsJsonThrows(new RuntimeException("pickup-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.PICKUP_GENERATION_FAILED, 2001L, "pickup-error"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify manifest-generation API failure is converted to domain exception.
     * Expected Result: BadRequestException with ManifestGenerationFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Manifest Generation Error - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_ManifestGenerationError_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipRocketHelperGenerateManifestThrows(new RuntimeException("manifest-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.MANIFEST_GENERATION_FAILED, 2001L, "manifest-error"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify label-generation API failure is converted to domain exception.
     * Expected Result: BadRequestException with LabelGenerationFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Label Generation Error - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_LabelGenerationError_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipRocketHelperGenerateLabelThrows(new RuntimeException("label-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.LABEL_GENERATION_FAILED, 2001L, "label-error"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify invoice-generation API failure is converted to domain exception.
     * Expected Result: BadRequestException with InvoiceGenerationFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Invoice Generation Error - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_InvoiceGenerationError_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipRocketHelperGenerateInvoiceThrows(new RuntimeException("invoice-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.INVOICE_GENERATION_FAILED, 2001L, "invoice-error"),
                ex.getMessage());
    }

    /**
     * Purpose: Verify tracking API failure is converted to domain exception.
     * Expected Result: BadRequestException with TrackingFetchFailed.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApproval - Tracking Fetch Error - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_TrackingFetchError_ThrowsBadRequestException() {
        // Arrange
        arrangeOnlineSuccessBaseline();
        stubShipRocketHelperGetTrackingAsJsonThrows(new RuntimeException("tracking-error"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.TRACKING_FETCH_FAILED, "AWB-1", "tracking-error"),
                ex.getMessage());
    }

    private void arrangeOnlineSuccessBaseline() {
        testShipmentProduct.setAllocatedPrice(new java.math.BigDecimal("100"));
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
