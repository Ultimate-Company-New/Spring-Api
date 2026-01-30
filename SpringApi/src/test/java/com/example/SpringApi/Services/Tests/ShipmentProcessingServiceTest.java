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
 * Unit tests for ShipmentProcessingService.
 * 
 * Test Group Summary:
 * | Group Name | Number of Tests |
 * | :--- | :--- |
 * | ProcessShipmentsCashPaymentTests | 13 |
 * | ProcessShipmentsOnlinePaymentTests | 15 |
 * | **Total** | **28** |
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

    @Nested
    @DisplayName("ProcessShipmentsCashPaymentTests")
    class ProcessShipmentsCashPaymentTests {

        /**
         * Purpose: Verify that processing shipments with null purchase order ID throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
        @Test
        @DisplayName("Process Shipments - Null Purchase Order ID - Throws BadRequestException")
        void processShipments_NullPurchaseOrderId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testCashPaymentRequest));

            assertNotNull(exception.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with a non-existent purchase order throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message is thrown.
         * Assertions: Exception message equals ErrorMessages.PurchaseOrderErrorMessages.InvalidId.
         */
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

        /**
         * Purpose: Verify that processing shipments when order summary is not found throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message is thrown.
         * Assertions: Exception message equals ErrorMessages.OrderSummaryNotFoundMessage.NotFound.
         */
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

        /**
         * Purpose: Verify that processing shipments when no shipments exist throws BadRequestException.
         * Expected Result: BadRequestException with NoShipmentsFound message is thrown.
         * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.NoShipmentsFound.
         */
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

        /**
         * Purpose: Verify that processing shipments with null cash payment request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
        @Test
        @DisplayName("Process Shipments - Null Cash Payment Request - Throws BadRequestException")
        void processShipments_NullCashPaymentRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            (CashPaymentRequestModel) null));

            assertNotNull(exception.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null payment amount throws BadRequestException.
         * Expected Result: BadRequestException with ValidPaymentAmountRequired message is thrown.
         * Assertions: Exception message equals ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired.
         */
        @Test
        @DisplayName("Process Shipments - Invalid Payment Amount - Throws BadRequestException")
        void processShipments_InvalidPaymentAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null payment date throws BadRequestException.
         * Expected Result: BadRequestException with PaymentDateRequired message is thrown.
         * Assertions: Exception message equals ErrorMessages.PaymentErrorMessages.PaymentDateRequired.
         */
        @Test
        @DisplayName("Process Shipments - Missing Payment Date - Throws BadRequestException")
        void processShipments_MissingPaymentDate_ThrowsBadRequestException() {
            testCashPaymentRequest.setPaymentDate(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.PaymentDateRequired, exception.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null PO ID in request throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message is thrown.
         * Assertions: Exception message equals ErrorMessages.PurchaseOrderErrorMessages.InvalidId.
         */
        @Test
        @DisplayName("Process Shipments Cash - Null PO ID - Throws BadRequestException")
        void processShipmentsCash_NullPOId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with negative PO ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message is thrown.
         * Assertions: Exception message equals ErrorMessages.PurchaseOrderErrorMessages.InvalidId.
         */
        @Test
        @DisplayName("Process Shipments Cash - Negative PO ID - Throws BadRequestException")
        void processShipmentsCash_NegativePOId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(-1L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(-1L, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with non-existent PO throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message is thrown.
         * Assertions: Exception message equals ErrorMessages.PurchaseOrderErrorMessages.InvalidId.
         */
        @Test
        @DisplayName("Process Shipments Cash - PO Not Found - Throws NotFoundException")
        void processShipmentsCash_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null amount throws BadRequestException.
         * Expected Result: BadRequestException with ValidPaymentAmountRequired message is thrown.
         * Assertions: Exception message equals ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired.
         */
        @Test
        @DisplayName("Process Shipments Cash - Null Amount - Throws BadRequestException")
        void processShipmentsCash_NullAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with negative amount throws BadRequestException.
         * Expected Result: BadRequestException with ValidPaymentAmountRequired message is thrown.
         * Assertions: Exception message equals ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired.
         */
        @Test
        @DisplayName("Process Shipments Cash - Negative Amount - Throws BadRequestException")
        void processShipmentsCash_NegativeAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(new BigDecimal("-100.00"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with zero amount throws BadRequestException.
         * Expected Result: BadRequestException with ValidPaymentAmountRequired message is thrown.
         * Assertions: Exception message equals ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired.
         */
        @Test
        @DisplayName("Process Shipments Cash - Zero Amount - Throws BadRequestException")
        void processShipmentsCash_ZeroAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(BigDecimal.ZERO);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("ProcessShipmentsOnlinePaymentTests")
    class ProcessShipmentsOnlinePaymentTests {

        /**
         * Purpose: Verify that processing shipments with null Razorpay request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
        @Test
        @DisplayName("Process Shipments Online - Null Razorpay Request - Throws BadRequestException")
        void processShipmentsOnline_NullRazorpayRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID,
                            (RazorpayVerifyRequestModel) null));

            assertNotNull(exception.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null Razorpay order ID throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with null Razorpay payment ID throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with null Razorpay signature throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with empty Razorpay order ID throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with empty Razorpay payment ID throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with empty Razorpay signature throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with negative PO ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with zero PO ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with Long.MAX_VALUE PO ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with null request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
        @Test
        @DisplayName("Process Shipments - Null Request - Throws BadRequestException")
        void processShipmentsAfterPaymentApproval_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, (RazorpayVerifyRequestModel) null));
            assertNotNull(ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null order ID in request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with empty order ID in request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with null payment ID in request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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

        /**
         * Purpose: Verify that processing shipments with null signature in request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is not null.
         */
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
    }
}
