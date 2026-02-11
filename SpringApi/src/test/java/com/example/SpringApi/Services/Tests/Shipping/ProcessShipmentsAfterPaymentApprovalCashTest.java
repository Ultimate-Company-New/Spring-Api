package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.RequestModels.ProcessPaymentAndShipmentRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * Tests for ShippingService.processShipmentsAfterPaymentApproval (cash).
 */
@DisplayName("ProcessShipmentsAfterPaymentApproval Cash Tests")
class ProcessShipmentsAfterPaymentApprovalCashTest extends ShippingServiceTestBase {

        // Total Tests: 25

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful cash payment processing updates shipments.
     * Expected Result: PaymentVerificationResponseModel indicates success.
     * Assertions: Response success is true.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Valid Request - Success")
    void processShipmentsAfterPaymentApprovalCash_ValidRequest_Success() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
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
                .processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest);

        // Assert
        assertTrue(result.isSuccess());
    }

    /**
     * Purpose: Verify shipment repository save is invoked on success.
     * Expected Result: Shipment save is called.
     * Assertions: Verify save called.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Saves Shipment - Success")
    void processShipmentsAfterPaymentApprovalCash_SavesShipment_Success() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
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
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest);

        // Assert
        verify(shipmentRepository).save(any(Shipment.class));
    }

    /**
     * Purpose: Verify user log is recorded on success.
     * Expected Result: User log service is called.
     * Assertions: Verify logData called.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Logs User Action - Success")
    void processShipmentsAfterPaymentApprovalCash_LogsUserAction_Success() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
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
        shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest);

        // Assert
        verify(userLogService).logData(anyLong(), anyString(), anyString());
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
    @DisplayName("processShipmentsAfterPaymentApprovalCash - PO Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalCash_PoNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify client mismatch throws BadRequestException.
     * Expected Result: BadRequestException with AccessDenied message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Client Mismatch - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ClientMismatch_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setClientId(999L);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder, ex.getMessage());
    }

    /**
     * Purpose: Verify non-pending status throws BadRequestException.
     * Expected Result: BadRequestException with OnlyPendingApprovalCanBePaid message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Status Not Pending - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_StatusNotPending_ThrowsBadRequestException() {
        // Arrange
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid, ex.getMessage());
    }

    /**
     * Purpose: Verify missing order summary throws NotFoundException.
     * Expected Result: NotFoundException with OrderSummary not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Order Summary Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalCash_OrderSummaryNotFound_ThrowsNotFoundException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
    }

    /**
     * Purpose: Verify null shipments list throws BadRequestException.
     * Expected Result: BadRequestException with NoShipmentsFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Shipments Null - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipmentsNull_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
    }

    /**
     * Purpose: Verify empty shipments list throws BadRequestException.
     * Expected Result: BadRequestException with NoShipmentsFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Shipments Empty - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipmentsEmpty_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of());

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
    }

    /**
     * Purpose: Verify product not available at pickup location throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Product Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ProductNotAvailable_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.ProductNotAvailableAtPickupLocationFormat,
                TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify insufficient product stock throws BadRequestException.
     * Expected Result: BadRequestException with InsufficientProductStockFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Product Stock Insufficient - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ProductStockInsufficient_ThrowsBadRequestException() {
        // Arrange
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 1);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.InsufficientProductStockFormat,
                TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 1, testShipmentProduct.getAllocatedQuantity()), ex.getMessage());
    }

    /**
     * Purpose: Verify package not available at pickup location throws BadRequestException.
     * Expected Result: BadRequestException with PackageNotAvailableAtPickupLocationFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Package Not Available - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_PackageNotAvailable_ThrowsBadRequestException() {
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
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.PackageNotAvailableAtPickupLocationFormat,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID), ex.getMessage());
    }

    /**
     * Purpose: Verify insufficient package stock throws BadRequestException.
     * Expected Result: BadRequestException with InsufficientPackageStockFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Package Stock Insufficient - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_PackageStockInsufficient_ThrowsBadRequestException() {
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
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(
                ErrorMessages.ShipmentProcessingErrorMessages.InsufficientPackageStockFormat,
                TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 0, testShipmentPackage.getQuantityUsed()), ex.getMessage());
    }

    /**
     * Purpose: Verify payment failure throws BadRequestException.
     * Expected Result: BadRequestException with OperationFailedWithMessageFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Payment Failure - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_PaymentFailure_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.failure("fail"));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentProcessingErrorMessages.OperationFailedWithMessageFormat,
                ErrorMessages.OPERATION_FAILED, "fail"), ex.getMessage());
    }

    /**
     * Purpose: Verify missing ShipRocket credentials throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Credentials Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testClient.setShipRocketEmail(null);
        stubPurchaseOrderRepositoryFindById(testPurchaseOrder);
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(testOrderSummary);
        stubShipmentRepositoryFindByOrderSummaryId(List.of(testShipment));
        stubShipmentProductRepositoryFindByShipmentId(List.of(testShipmentProduct));
        stubShipmentPackageRepositoryFindByShipmentId(List.of(testShipmentPackage));
        ProductPickupLocationMapping productMapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(productMapping);
        PackagePickupLocationMapping packageMapping = createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(packageMapping);
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
    }

    /**
     * Purpose: Verify missing delivery address throws BadRequestException.
     * Expected Result: BadRequestException with DeliveryAddressNotFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Delivery Address Missing - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_DeliveryAddressMissing_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.DeliveryAddressNotFound, ex.getMessage());
    }

    /**
     * Purpose: Verify pickup location not found throws NotFoundException.
     * Expected Result: NotFoundException with PickupLocation not found message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Pickup Location Not Found - Throws NotFoundException")
    void processShipmentsAfterPaymentApprovalCash_PickupLocationNotFound_ThrowsNotFoundException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

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
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Response Null - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketResponseNull_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        stubShipRocketHelperCreateCustomOrder(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketApiNullResponse, TEST_SHIPMENT_ID),
                ex.getMessage());
    }

    /**
     * Purpose: Verify ShipRocket response with message throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Response Message - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketResponseMessage_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.message = ErrorMessages.OPERATION_FAILED;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

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
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Missing Order Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketMissingOrderId_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.order_id = null;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

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
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Missing Shipment Id - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketMissingShipmentId_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.shipment_id = null;
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

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
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Missing Status - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketMissingStatus_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.status = "";
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, ErrorMessages.ShippingErrorMessages.ShipRocketStatusMissing), ex.getMessage());
    }

    /**
     * Purpose: Verify invalid status throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketOrderCreationFailed message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - ShipRocket Invalid Status - Throws BadRequestException")
    void processShipmentsAfterPaymentApprovalCash_ShipRocketInvalidStatus_ThrowsBadRequestException() {
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
        stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel.success("pay", TEST_PURCHASE_ORDER_ID,
                PurchaseOrder.Status.APPROVED.getValue()));
        stubClientRepositoryFindById(testClient);
        stubPickupLocationRepositoryFindById(testPickupLocation);
        ShipRocketOrderResponseModel response = createValidShipRocketOrderResponse();
        response.status = "INVALID";
        stubShipRocketHelperCreateCustomOrder(response);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.processShipmentsAfterPaymentApproval(TEST_PURCHASE_ORDER_ID, cashPaymentRequest));

        // Assert
        String validStatuses = String.join(", ",
                java.util.Arrays.stream(Shipment.ShipRocketStatus.values())
                        .map(Shipment.ShipRocketStatus::getValue)
                        .toArray(String[]::new));
        String expectedDetail = String.format(
                ErrorMessages.ShippingErrorMessages.InvalidShipRocketStatusFormat,
                "INVALID", validStatuses);
        assertEquals(String.format(ErrorMessages.ShippingErrorMessages.ShipRocketOrderCreationFailed,
                TEST_SHIPMENT_ID, expectedDetail), ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify controller has @PreAuthorize for processPaymentAndShipments.
     * Expected Result: Annotation exists and includes UPDATE_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("processShipmentsAfterPaymentApprovalCash - Verify @PreAuthorize Annotation")
    void processShipmentsAfterPaymentApprovalCash_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = PaymentController.class.getMethod("processPaymentAndShipments", ProcessPaymentAndShipmentRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION));
    }
}
