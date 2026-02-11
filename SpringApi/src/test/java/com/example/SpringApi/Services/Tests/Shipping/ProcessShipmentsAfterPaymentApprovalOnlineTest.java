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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * Tests for ShippingService.processShipmentsAfterPaymentApproval (online).
 */
@DisplayName("ProcessShipmentsAfterPaymentApproval Online Tests")
class ProcessShipmentsAfterPaymentApprovalOnlineTest extends ShippingServiceTestBase {

        // Total Tests: 23

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful online payment processing.
     * Expected Result: PaymentVerificationResponseModel indicates success.
     * Assertions: Response success is true.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Valid Request - Success")
    void processShipmentsAfterPaymentApprovalOnline_ValidRequest_Success() {
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

    /**
     * Purpose: Verify shipment save is invoked on online payment success.
     * Expected Result: Shipment repository save is called.
     * Assertions: Verify save called.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Saves Shipment - Success")
    void processShipmentsAfterPaymentApprovalOnline_SavesShipment_Success() {
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

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify purchase order not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - PO Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalOnline_PoNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify client mismatch throws BadRequestException.
     * Expected Result: BadRequestException with AccessDenied message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Client Mismatch - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ClientMismatch_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setClientId(999L);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder, ex.getMessage());
    }

    /**
     * Purpose: Verify status not pending throws BadRequestException.
     * Expected Result: BadRequestException with OnlyPendingApprovalCanBePaid message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Status Not Pending - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_StatusNotPending_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid, ex.getMessage());
    }

    /**
     * Purpose: Verify order summary not found throws NotFoundException.
     * Expected Result: NotFoundException with OrderSummary not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Order Summary Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalOnline_OrderSummaryNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
    }

    /**
     * Purpose: Verify empty shipments list throws BadRequestException.
     * Expected Result: BadRequestException with NoShipmentsFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Shipments Empty - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipmentsEmpty_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of());

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
    }

    /**
     * Purpose: Verify product not available throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Product Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ProductNotAvailable_ThrowsBadRequestException() {
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
                ErrorMessages.ShipmentProcessingErrorMessages.ProductNotAvailableAtPickupLocationFormat,
                TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify payment failure throws BadRequestException.
     * Expected Result: BadRequestException with OperationFailedWithMessageFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Payment Failure - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_PaymentFailure_ThrowsBadRequestException() {
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
        assertEquals(String.format(ErrorMessages.ShipmentProcessingErrorMessages.OperationFailedWithMessageFormat,
                ErrorMessages.OPERATION_FAILED, "fail"), ex.getMessage());
    }

    /**
     * Purpose: Verify credentials missing throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Credentials Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_CredentialsMissing_ThrowsBadRequestException() {
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
        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
    }

    /**
     * Purpose: Verify delivery address missing throws BadRequestException.
     * Expected Result: BadRequestException with DeliveryAddressNotFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Delivery Address Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_DeliveryAddressMissing_ThrowsBadRequestException() {
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
        assertEquals(ErrorMessages.ShippingErrorMessages.DeliveryAddressNotFound, ex.getMessage());
    }

    /**
     * Purpose: Verify package not available throws BadRequestException.
     * Expected Result: BadRequestException with PackageNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Package Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_PackageNotAvailable_ThrowsBadRequestException() {
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
                ErrorMessages.ShipmentProcessingErrorMessages.PackageNotAvailableAtPickupLocationFormat,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify package stock insufficient throws BadRequestException.
     * Expected Result: BadRequestException with InsufficientPackageStockFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Package Stock Insufficient - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_PackageStockInsufficient_ThrowsBadRequestException() {
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
                ErrorMessages.ShipmentProcessingErrorMessages.InsufficientPackageStockFormat,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 0, testShipmentPackage.getQuantityUsed()), ex.getMessage());
    }

    /**
     * Purpose: Verify pickup location not found throws NotFoundException.
     * Expected Result: NotFoundException with PickupLocation not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - Pickup Location Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalOnline_PickupLocationNotFound_ThrowsNotFoundException() {
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
        assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, TEST_PICKUP_LOCATION_ID),
                ex.getMessage());
    }

    /**
     * Purpose: Verify null ShipRocket response throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketApiNullResponse message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - ShipRocket Response Null - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseNull_ThrowsBadRequestException() {
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
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketApiNullResponse, TEST_SHIPMENT_ID),
                ex.getMessage());
    }

    /**
     * Purpose: Verify ShipRocket response message throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - ShipRocket Response Message - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseMessage_ThrowsBadRequestException() {
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
        response.message = ErrorMessages.OPERATION_FAILED;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, ErrorMessages.OPERATION_FAILED), ex.getMessage());
    }

    /**
     * Purpose: Verify missing order_id throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - ShipRocket Missing Order Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingOrderId_ThrowsBadRequestException() {
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
        response.order_id = null;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.ShipRocketOrderIdMissing), ex.getMessage());
    }

    /**
     * Purpose: Verify missing shipment_id throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - ShipRocket Missing Shipment Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingShipmentId_ThrowsBadRequestException() {
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
        response.shipment_id = null;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.ShipRocketShipmentIdMissing), ex.getMessage());
    }

    /**
     * Purpose: Verify missing status throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalOnline - ShipRocket Missing Status - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingStatus_ThrowsBadRequestException() {
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
        response.status = "";
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, razorpayRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.ShipRocketStatusMissing), ex.getMessage());
    }
}
