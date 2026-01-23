package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PaymentService;
import com.example.SpringApi.Services.ShipmentProcessingService;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShipmentProcessingService}.
 * 
 * Tests the processing of shipments after payment approval, including
 * inventory validation, payment processing, ShipRocket order creation,
 * and inventory updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentProcessingService Tests")
class ShipmentProcessingServiceTest extends BaseTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private OrderSummaryRepository orderSummaryRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentProductRepository shipmentProductRepository;

    @Mock
    private ShipmentPackageRepository shipmentPackageRepository;

    @Mock
    private ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    private PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PickupLocationRepository pickupLocationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserLogService userLogService;

    @InjectMocks
    private ShipmentProcessingService shipmentProcessingService;

    private PurchaseOrder testPurchaseOrder;
    private OrderSummary testOrderSummary;
    private Shipment testShipment;
    private CashPaymentRequestModel testCashPaymentRequest;
    private RazorpayVerifyRequestModel testVerifyRequest;
    private Client testClient;
    private Address testAddress;
    private PickupLocation testPickupLocation;

    private static final Long TEST_PO_ID = 1L;
    private static final Long TEST_ORDER_SUMMARY_ID = 1L;
    private static final Long TEST_SHIPMENT_ID = 1L;
    private static final Long TEST_PICKUP_LOCATION_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1L;
    private static final Long TEST_PACKAGE_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize BaseTest mocks
        lenient().doReturn(DEFAULT_CLIENT_ID).when(shipmentProcessingService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(shipmentProcessingService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(shipmentProcessingService).getUser();

        // Initialize test data
        testClient = createTestClient(DEFAULT_CLIENT_ID);
        testClient.setShipRocketEmail("test@example.com");
        testClient.setShipRocketPassword("password");

        testAddress = createTestAddress();

        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrder.setClientId(DEFAULT_CLIENT_ID);
        testPurchaseOrder.setPurchaseOrderStatus("PENDING_APPROVAL");
        testPurchaseOrder.setVendorNumber("VN123");

        testOrderSummary = new OrderSummary();
        testOrderSummary.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testOrderSummary.setEntityId(TEST_PO_ID);
        testOrderSummary.setEntityType(OrderSummary.EntityType.PURCHASE_ORDER.getValue());
        testOrderSummary.setEntityAddress(testAddress);

        testShipment = new Shipment();
        testShipment.setShipmentId(TEST_SHIPMENT_ID);
        testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testShipment.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testShipment.setTotalWeightKgs(new BigDecimal("1.0"));
        testShipment.setSelectedCourierCompanyId(1L);

        testPickupLocation = new PickupLocation();
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setAddressNickName("Test Location");

        testCashPaymentRequest = new CashPaymentRequestModel();
        testCashPaymentRequest.setPurchaseOrderId(TEST_PO_ID);
        testCashPaymentRequest.setAmount(new BigDecimal("1000.00"));
        testCashPaymentRequest.setPaymentDate(LocalDate.now());

        testVerifyRequest = new RazorpayVerifyRequestModel();
        testVerifyRequest.setPurchaseOrderId(TEST_PO_ID);
        testVerifyRequest.setRazorpayPaymentId("pay_123");
        testVerifyRequest.setRazorpayOrderId("order_123");
        testVerifyRequest.setRazorpaySignature("sig_123");
    }

    // ==================== Cash Payment Flow Tests ====================

    @Nested
    @DisplayName("Process Shipments (Cash) - Validation Tests")
    class ProcessShipmentsCashValidationTests {

        @Test
        @DisplayName("Cash Payment - PO Not Found - Throws NotFoundException")
        void processCash_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Cash Payment - Invalid PO Status - Throws BadRequestException")
        void processCash_InvalidPOStatus_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("DRAFT");
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid, exception.getMessage());
        }

        @Test
        @DisplayName("Cash Payment - Access Denied - Throws BadRequestException")
        void processCash_AccessDenied_ThrowsBadRequestException() {
            testPurchaseOrder.setClientId(999L);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertEquals(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder, exception.getMessage());
        }

        @Test
        @DisplayName("Cash Payment - Order Summary Not Found - Throws NotFoundException")
        void processCash_OrderSummaryNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID)))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, exception.getMessage());
        }

        @Test
        @DisplayName("Cash Payment - No Shipments - Throws BadRequestException")
        void processCash_NoShipments_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(TEST_ORDER_SUMMARY_ID)).thenReturn(new ArrayList<>());

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, exception.getMessage());
        }
    }

    // ==================== Online Payment Flow Tests ====================

    @Nested
    @DisplayName("Process Shipments (Online) - Validation Tests")
    class ProcessShipmentsOnlineValidationTests {

        @Test
        @DisplayName("Online Payment - PO Not Found - Throws NotFoundException")
        void processOnline_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testVerifyRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Online Payment - Payment Failed - Throws BadRequestException")
        void processOnline_PaymentFailed_ThrowsBadRequestException() {
            // Setup valid flow until payment verification
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(TEST_ORDER_SUMMARY_ID))
                    .thenReturn(Collections.singletonList(testShipment));

            // Mock inventory check success
            ProductPickupLocationMapping productMapping = new ProductPickupLocationMapping();
            productMapping.setAvailableStock(100);
            when(productPickupLocationMappingRepository.findByProductIdAndPickupLocationId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(productMapping));

            PackagePickupLocationMapping packageMapping = new PackagePickupLocationMapping();
            packageMapping.setAvailableQuantity(100);
            when(packagePickupLocationMappingRepository.findByPackageIdAndPickupLocationId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(packageMapping));

            // Mock shipment items
            ShipmentProduct sp = new ShipmentProduct();
            sp.setProductId(TEST_PRODUCT_ID);
            sp.setAllocatedQuantity(1);
            when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                    .thenReturn(Collections.singletonList(sp));

            ShipmentPackage pkg = new ShipmentPackage();
            pkg.setPackageId(TEST_PACKAGE_ID);
            pkg.setQuantityUsed(1);
            when(shipmentPackageRepository.findByShipmentId(TEST_SHIPMENT_ID))
                    .thenReturn(Collections.singletonList(pkg));

            // Mock payment failure
            when(paymentService.verifyPayment(testVerifyRequest))
                    .thenReturn(PaymentVerificationResponseModel.failure("Payment failed"));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testVerifyRequest));

            assertTrue(exception.getMessage().contains("Payment failed"));
        }
    }

    // ==================== Inventory Validation Tests ====================

    @Nested
    @DisplayName("Inventory Validation Tests")
    class InventoryValidationTests {

        @Test
        @DisplayName("Insufficient Product Stock - Throws BadRequestException")
        void validateInventory_InsufficientProductStock_ThrowsBadRequestException() {
            // Setup basic flow
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(TEST_ORDER_SUMMARY_ID))
                    .thenReturn(Collections.singletonList(testShipment));

            // Setup shipment product
            ShipmentProduct sp = new ShipmentProduct();
            sp.setProductId(TEST_PRODUCT_ID);
            sp.setAllocatedQuantity(10);
            when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                    .thenReturn(Collections.singletonList(sp));

            // Setup insufficient stock
            ProductPickupLocationMapping mapping = new ProductPickupLocationMapping();
            mapping.setAvailableStock(5); // Less than required 10
            when(productPickupLocationMappingRepository.findByProductIdAndPickupLocationId(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID))
                    .thenReturn(Optional.of(mapping));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertTrue(exception.getMessage().contains("Insufficient stock for product ID"));
        }

        @Test
        @DisplayName("Product Not Available At Location - Throws BadRequestException")
        void validateInventory_ProductNotAtLocation_ThrowsBadRequestException() {
            // Setup basic flow
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(TEST_ORDER_SUMMARY_ID))
                    .thenReturn(Collections.singletonList(testShipment));

            // Setup shipment product
            ShipmentProduct sp = new ShipmentProduct();
            sp.setProductId(TEST_PRODUCT_ID);
            sp.setAllocatedQuantity(10);
            when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                    .thenReturn(Collections.singletonList(sp));

            // Setup missing mapping
            when(productPickupLocationMappingRepository.findByProductIdAndPickupLocationId(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID))
                    .thenReturn(Optional.empty());

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            assertTrue(exception.getMessage().contains("is not available at pickup location ID"));
        }
    }
}
