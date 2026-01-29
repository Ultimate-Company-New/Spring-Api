package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PaymentService;
import com.example.SpringApi.Services.ShipmentProcessingService;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShipmentProcessingService}.
 * 
 * Tests shipment processing after payment approval including
 * inventory validation, payment processing, ShipRocket order creation,
 * and inventory updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentProcessingService Tests")
class ShipmentProcessingServiceTest {

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
    private ShipmentPackageProductRepository shipmentPackageProductRepository;

    @Mock
    private ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    private PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    private PickupLocationRepository pickupLocationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserLogService userLogService;

    @Mock
    private Environment environment;

    @InjectMocks
    private ShipmentProcessingService shipmentProcessingService;

    private PurchaseOrder testPurchaseOrder;
    private OrderSummary testOrderSummary;
    private CashPaymentRequestModel testCashPaymentRequest;

    private static final Long TEST_PO_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test purchase order
        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrder.setClientId(TEST_CLIENT_ID);
        testPurchaseOrder.setPurchaseOrderStatus("PENDING_APPROVAL");

        // Initialize test order summary
        testOrderSummary = new OrderSummary();
        testOrderSummary.setOrderSummaryId(1L);
        testOrderSummary.setEntityType(OrderSummary.EntityType.PURCHASE_ORDER.getValue());
        testOrderSummary.setEntityId(TEST_PO_ID);
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        testOrderSummary.setPendingAmount(new BigDecimal("1000.00"));

        // Initialize test cash payment request
        testCashPaymentRequest = new CashPaymentRequestModel();
        testCashPaymentRequest.setPurchaseOrderId(TEST_PO_ID);
        testCashPaymentRequest.setAmount(new BigDecimal("1000.00"));
        testCashPaymentRequest.setPaymentDate(LocalDate.now());
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Process Shipments After Payment Approval - Validation Tests")
    class ProcessShipmentsValidationTests {

        @Test
        @DisplayName("Process Shipments - Null Purchase Order ID - Throws BadRequestException")
        void processShipments_NullPurchaseOrderId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testCashPaymentRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - Purchase Order Not Found - Throws NotFoundException")
        void processShipments_PurchaseOrderNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - Order Summary Not Found - Throws NotFoundException")
        void processShipments_OrderSummaryNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - No Shipments Found - Throws BadRequestException")
        void processShipments_NoShipmentsFound_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Collections.emptyList());

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - Null Cash Payment Request - Throws BadRequestException")
        void processShipments_NullCashPaymentRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            (CashPaymentRequestModel) null));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - Invalid Payment Amount - Throws BadRequestException")
        void processShipments_InvalidPaymentAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments - Missing Payment Date - Throws BadRequestException")
        void processShipments_MissingPaymentDate_ThrowsBadRequestException() {
            testCashPaymentRequest.setPaymentDate(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.PaymentDateRequired, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Process Shipments After Online Payment - Validation Tests")
    class ProcessShipmentsOnlinePaymentTests {

        @Test
        @DisplayName("Process Shipments Online - Null Razorpay Request - Throws BadRequestException")
        void processShipmentsOnline_NullRazorpayRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            (RazorpayVerifyRequestModel) null));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments Online - Missing Order ID - Throws BadRequestException")
        void processShipmentsOnline_MissingOrderId_ThrowsBadRequestException() {
            RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
            razorpayRequest.setRazorpayOrderId(null);
            razorpayRequest.setRazorpayPaymentId("pay_123");
            razorpayRequest.setRazorpaySignature("sig_123");

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments Online - Missing Payment ID - Throws BadRequestException")
        void processShipmentsOnline_MissingPaymentId_ThrowsBadRequestException() {
            RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
            razorpayRequest.setRazorpayOrderId("order_123");
            razorpayRequest.setRazorpayPaymentId(null);
            razorpayRequest.setRazorpaySignature("sig_123");

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Process Shipments Online - Missing Signature - Throws BadRequestException")
        void processShipmentsOnline_MissingSignature_ThrowsBadRequestException() {
            RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
            razorpayRequest.setRazorpayOrderId("order_123");
            razorpayRequest.setRazorpayPaymentId("pay_123");
            razorpayRequest.setRazorpaySignature(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));

            assertNotNull(exception.getMessage());
        }
    }

    // ==================== Additional ProcessShipments Cash Payment Tests ====================

    @Test
    @DisplayName("Process Shipments Cash - Null PO ID - Throws BadRequestException")
    void processShipmentsCash_NullPOId_ThrowsBadRequestException() {
        testCashPaymentRequest.setPurchaseOrderId(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Cash - Negative PO ID - Throws BadRequestException")
    void processShipmentsCash_NegativePOId_ThrowsBadRequestException() {
        testCashPaymentRequest.setPurchaseOrderId(-1L);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(-1L, testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Cash - PO Not Found - Throws NotFoundException")
    void processShipmentsCash_PONotFound_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Cash - Null Amount - Throws BadRequestException")
    void processShipmentsCash_NullAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Cash - Negative Amount - Throws BadRequestException")
    void processShipmentsCash_NegativeAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(new BigDecimal("-100.00"));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Cash - Zero Amount - Throws BadRequestException")
    void processShipmentsCash_ZeroAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(BigDecimal.ZERO);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    // ==================== Additional ProcessShipments Online Payment Tests ====================

    @Test
    @DisplayName("Process Shipments Online - Null Razorpay Request - Throws BadRequestException")
    void processShipmentsOnline_NullRazorpayRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, (RazorpayVerifyRequestModel) null));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Online - Empty Order ID - Throws BadRequestException")
    void processShipmentsOnline_EmptyOrderId_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Online - Empty Payment ID - Throws BadRequestException")
    void processShipmentsOnline_EmptyPaymentId_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("");
        razorpayRequest.setRazorpaySignature("sig_123");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Process Shipments Online - Empty Signature - Throws BadRequestException")
    void processShipmentsOnline_EmptySignature_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(exception.getMessage());
    }

    // ==================== Comprehensive Validation Tests - Added ====================

    @Test
    @DisplayName("Process Shipments - Negative PO ID - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_NegativePOId_ThrowsNotFoundException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(-1L, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Zero PO ID - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_ZeroPOId_ThrowsNotFoundException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(0L, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Long.MAX_VALUE PO ID - Throws NotFoundException")
    void processShipmentsAfterPaymentApproval_MaxPOId_ThrowsNotFoundException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(Long.MAX_VALUE, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Null Request - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, null));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Null Order ID - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_NullOrderId_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId(null);
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Empty Order ID - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_EmptyOrderId_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature("sig_123");
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Null Payment ID - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_NullPaymentId_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId(null);
        razorpayRequest.setRazorpaySignature("sig_123");
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Process Shipments - Null Signature - Throws BadRequestException")
    void processShipmentsAfterPaymentApproval_NullSignature_ThrowsBadRequestException() {
        RazorpayVerifyRequestModel razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_123");
        razorpayRequest.setRazorpayPaymentId("pay_123");
        razorpayRequest.setRazorpaySignature(null);
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, razorpayRequest));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Update Shipment Status - Negative Shipment ID - Throws NotFoundException")
    void updateShipmentStatus_NegativeShipmentId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.updateShipmentStatus(-1L, "DELIVERED"));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Update Shipment Status - Zero Shipment ID - Throws NotFoundException")
    void updateShipmentStatus_ZeroShipmentId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.updateShipmentStatus(0L, "DELIVERED"));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Update Shipment Status - Null Status - Throws BadRequestException")
    void updateShipmentStatus_NullStatus_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.updateShipmentStatus(TEST_SHIPMENT_ID, null));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Update Shipment Status - Empty Status - Throws BadRequestException")
    void updateShipmentStatus_EmptyStatus_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.updateShipmentStatus(TEST_SHIPMENT_ID, ""));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Cancel Shipment - Negative Shipment ID - Throws NotFoundException")
    void cancelShipment_NegativeShipmentId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.cancelShipment(-1L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Cancel Shipment - Zero Shipment ID - Throws NotFoundException")
    void cancelShipment_ZeroShipmentId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.cancelShipment(0L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Get Shipment By ID - Negative ID - Throws NotFoundException")
    void getShipmentById_NegativeId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.getShipmentById(-1L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Get Shipment By ID - Zero ID - Throws NotFoundException")
    void getShipmentById_ZeroId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.getShipmentById(0L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Get Shipments By PO ID - Negative PO ID - Throws NotFoundException")
    void getShipmentsByPOId_NegativePOId_ThrowsNotFoundException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.getShipmentsByPOId(-1L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Get Shipments By Status - Null Status - Throws BadRequestException")
    void getShipmentsByStatus_NullStatus_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.getShipmentsByStatus(null));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Get Shipments By Status - Empty Status - Throws BadRequestException")
    void getShipmentsByStatus_EmptyStatus_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> shipmentProcessingService.getShipmentsByStatus(""));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Track Shipment - Negative Tracking ID - Throws NotFoundException")
    void trackShipment_NegativeTrackingId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.trackShipment(-1L));
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Track Shipment - Zero Tracking ID - Throws NotFoundException")
    void trackShipment_ZeroTrackingId_ThrowsNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> shipmentProcessingService.trackShipment(0L));
        assertNotNull(ex.getMessage());
    }
}
