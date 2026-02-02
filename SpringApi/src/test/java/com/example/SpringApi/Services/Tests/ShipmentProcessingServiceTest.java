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
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | ProcessShipmentsCashPaymentTests        | 22              |
 * | ProcessShipmentsOnlinePaymentTests      | 20              |
 * | **Total**                               | **42**          |
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
    private RazorpayVerifyRequestModel testRazorpayRequest;

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

        // Initialize test Razorpay request
        testRazorpayRequest = new RazorpayVerifyRequestModel();
        testRazorpayRequest.setRazorpayOrderId("order_123456");
        testRazorpayRequest.setRazorpayPaymentId("pay_123456");
        testRazorpayRequest.setRazorpaySignature("signature_123456");
    }

    @Nested
    @DisplayName("ProcessShipmentsCashPaymentTests")
    class ProcessShipmentsCashPaymentTests {

        /**
         * Purpose: Verify that processing shipments with null PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Null PO ID - Throws NotFoundException")
        void processShipmentsCash_NullPOId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with non-existent PO throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - PO Not Found - Throws NotFoundException")
        void processShipmentsCash_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with negative PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Negative PO ID - Throws NotFoundException")
        void processShipmentsCash_NegativePOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(-1L, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with zero PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Zero PO ID - Throws NotFoundException")
        void processShipmentsCash_ZeroPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(0L, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with max long PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Max Long PO ID - Throws NotFoundException")
        void processShipmentsCash_MaxLongPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(Long.MAX_VALUE, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments when order summary not found throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Order Summary Not Found - Throws NotFoundException")
        void processShipmentsCash_OrderSummaryNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments when no shipments exist throws BadRequestException.
         * Expected Result: BadRequestException with NoShipmentsFound message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - No Shipments Found - Throws BadRequestException")
        void processShipmentsCash_NoShipmentsFound_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Collections.emptyList());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing shipments with null shipments list throws BadRequestException.
         * Expected Result: BadRequestException with NoShipmentsFound message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Null Shipments List - Throws BadRequestException")
        void processShipmentsCash_NullShipmentsList_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that null cash payment request throws NullPointerException or BadRequestException.
         * Expected Result: Exception is thrown.
         * Assertions: assertThrows(...);
         */
        @Test
        @DisplayName("Process Shipments Cash - Null Cash Payment Request - Throws Exception")
        void processShipmentsCash_NullCashPaymentRequest_ThrowsException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            Shipment shipment = new Shipment();
            shipment.setShipmentId(1L);
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Arrays.asList(shipment));
            when(shipmentProductRepository.findByShipmentId(anyLong()))
                    .thenReturn(Collections.emptyList());
            when(shipmentPackageRepository.findByShipmentId(anyLong()))
                    .thenReturn(Collections.emptyList());

            assertThrows(Exception.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, (CashPaymentRequestModel) null));
        }

        /**
         * Purpose: Verify repository findById is called with correct PO ID.
         * Expected Result: Repository method is called with correct parameter.
         * Assertions: verify(purchaseOrderRepository).findById(TEST_PO_ID);
         */
        @Test
        @DisplayName("Process Shipments Cash - Verify Repository Called")
        void processShipmentsCash_VerifyRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            verify(purchaseOrderRepository).findById(TEST_PO_ID);
        }

        /**
         * Purpose: Verify that multiple sequential calls work independently.
         * Expected Result: Each call throws NotFoundException.
         * Assertions: verify repository called multiple times.
         */
        @Test
        @DisplayName("Process Shipments Cash - Multiple Calls Work Independently")
        void processShipmentsCash_MultipleCallsWorkIndependently() {
            when(purchaseOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(1L, testCashPaymentRequest));
            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(2L, testCashPaymentRequest));

            verify(purchaseOrderRepository, times(2)).findById(anyLong());
        }

        /**
         * Purpose: Verify that different PO IDs are handled independently.
         * Expected Result: Each call checks the correct PO ID.
         * Assertions: verify repository called with each ID.
         */
        @Test
        @DisplayName("Process Shipments Cash - Different PO IDs")
        void processShipmentsCash_DifferentPOIds() {
            when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.empty());
            when(purchaseOrderRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(1L, testCashPaymentRequest));
            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(2L, testCashPaymentRequest));

            verify(purchaseOrderRepository).findById(1L);
            verify(purchaseOrderRepository).findById(2L);
        }

        /**
         * Purpose: Verify order summary repository is called after PO is found.
         * Expected Result: Order summary repository is called.
         * Assertions: verify(orderSummaryRepository).findByEntityTypeAndEntityId(...);
         */
        @Test
        @DisplayName("Process Shipments Cash - Order Summary Repository Called")
        void processShipmentsCash_OrderSummaryRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            verify(orderSummaryRepository).findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID));
        }

        /**
         * Purpose: Verify shipment repository is called after order summary is found.
         * Expected Result: Shipment repository is called.
         * Assertions: verify(shipmentRepository).findByOrderSummaryId(...);
         */
        @Test
        @DisplayName("Process Shipments Cash - Shipment Repository Called")
        void processShipmentsCash_ShipmentRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Collections.emptyList());

            assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));

            verify(shipmentRepository).findByOrderSummaryId(testOrderSummary.getOrderSummaryId());
        }

        /**
         * Purpose: Verify min long PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Min Long PO ID - Throws NotFoundException")
        void processShipmentsCash_MinLongPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(Long.MIN_VALUE, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that different cash amounts don't affect PO lookup.
         * Expected Result: NotFoundException for PO not found regardless of amount.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Different Amount Still Checks PO First")
        void processShipmentsCash_DifferentAmountStillChecksPOFirst() {
            testCashPaymentRequest.setAmount(new BigDecimal("9999999.99"));
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that null amount in request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Null Amount Still Checks PO First")
        void processShipmentsCash_NullAmountStillChecksPOFirst() {
            testCashPaymentRequest.setAmount(null);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that zero amount in request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Zero Amount Still Checks PO First")
        void processShipmentsCash_ZeroAmountStillChecksPOFirst() {
            testCashPaymentRequest.setAmount(BigDecimal.ZERO);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that negative amount in request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Negative Amount Still Checks PO First")
        void processShipmentsCash_NegativeAmountStillChecksPOFirst() {
            testCashPaymentRequest.setAmount(new BigDecimal("-100.00"));
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that null payment date in request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Null Payment Date Still Checks PO First")
        void processShipmentsCash_NullPaymentDateStillChecksPOFirst() {
            testCashPaymentRequest.setPaymentDate(null);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that future payment date still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Future Payment Date Still Checks PO First")
        void processShipmentsCash_FuturePaymentDateStillChecksPOFirst() {
            testCashPaymentRequest.setPaymentDate(LocalDate.now().plusYears(1));
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that past payment date still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Cash - Past Payment Date Still Checks PO First")
        void processShipmentsCash_PastPaymentDateStillChecksPOFirst() {
            testCashPaymentRequest.setPaymentDate(LocalDate.now().minusYears(1));
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("ProcessShipmentsOnlinePaymentTests")
    class ProcessShipmentsOnlinePaymentTests {

        /**
         * Purpose: Verify that processing with null PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Null PO ID - Throws NotFoundException")
        void processShipmentsOnline_NullPOId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(null, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing with non-existent PO throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - PO Not Found - Throws NotFoundException")
        void processShipmentsOnline_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing with negative PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Negative PO ID - Throws NotFoundException")
        void processShipmentsOnline_NegativePOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(-1L, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing with zero PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Zero PO ID - Throws NotFoundException")
        void processShipmentsOnline_ZeroPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(0L, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing with max long PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Max Long PO ID - Throws NotFoundException")
        void processShipmentsOnline_MaxLongPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(Long.MAX_VALUE, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing when order summary not found throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Order Summary Not Found - Throws NotFoundException")
        void processShipmentsOnline_OrderSummaryNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.OrderSummaryNotFoundMessage.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that processing when no shipments exist throws BadRequestException.
         * Expected Result: BadRequestException with NoShipmentsFound message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - No Shipments Found - Throws BadRequestException")
        void processShipmentsOnline_NoShipmentsFound_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Collections.emptyList());

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipmentsFound, ex.getMessage());
        }

        /**
         * Purpose: Verify null Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Null Request Still Checks PO First")
        void processShipmentsOnline_NullRequestStillChecksPOFirst() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, (RazorpayVerifyRequestModel) null));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null order ID in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Null Order ID Still Checks PO First")
        void processShipmentsOnline_NullOrderIdStillChecksPOFirst() {
            testRazorpayRequest.setRazorpayOrderId(null);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify empty order ID in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Empty Order ID Still Checks PO First")
        void processShipmentsOnline_EmptyOrderIdStillChecksPOFirst() {
            testRazorpayRequest.setRazorpayOrderId("");
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null payment ID in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Null Payment ID Still Checks PO First")
        void processShipmentsOnline_NullPaymentIdStillChecksPOFirst() {
            testRazorpayRequest.setRazorpayPaymentId(null);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify empty payment ID in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Empty Payment ID Still Checks PO First")
        void processShipmentsOnline_EmptyPaymentIdStillChecksPOFirst() {
            testRazorpayRequest.setRazorpayPaymentId("");
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null signature in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Null Signature Still Checks PO First")
        void processShipmentsOnline_NullSignatureStillChecksPOFirst() {
            testRazorpayRequest.setRazorpaySignature(null);
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify empty signature in Razorpay request still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Empty Signature Still Checks PO First")
        void processShipmentsOnline_EmptySignatureStillChecksPOFirst() {
            testRazorpayRequest.setRazorpaySignature("");
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify repository findById is called with correct PO ID.
         * Expected Result: Repository method is called with correct parameter.
         * Assertions: verify(purchaseOrderRepository).findById(TEST_PO_ID);
         */
        @Test
        @DisplayName("Process Shipments Online - Verify Repository Called")
        void processShipmentsOnline_VerifyRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));

            verify(purchaseOrderRepository).findById(TEST_PO_ID);
        }

        /**
         * Purpose: Verify min long PO ID throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Min Long PO ID - Throws NotFoundException")
        void processShipmentsOnline_MinLongPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(Long.MIN_VALUE, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify multiple sequential calls work independently.
         * Expected Result: Each call throws NotFoundException.
         * Assertions: verify repository called multiple times.
         */
        @Test
        @DisplayName("Process Shipments Online - Multiple Calls Work Independently")
        void processShipmentsOnline_MultipleCallsWorkIndependently() {
            when(purchaseOrderRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(1L, testRazorpayRequest));
            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(2L, testRazorpayRequest));

            verify(purchaseOrderRepository, times(2)).findById(anyLong());
        }

        /**
         * Purpose: Verify order summary repository is called after PO is found.
         * Expected Result: Order summary repository is called.
         * Assertions: verify(orderSummaryRepository).findByEntityTypeAndEntityId(...);
         */
        @Test
        @DisplayName("Process Shipments Online - Order Summary Repository Called")
        void processShipmentsOnline_OrderSummaryRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));

            verify(orderSummaryRepository).findByEntityTypeAndEntityId(anyString(), eq(TEST_PO_ID));
        }

        /**
         * Purpose: Verify shipment repository is called after order summary is found.
         * Expected Result: Shipment repository is called.
         * Assertions: verify(shipmentRepository).findByOrderSummaryId(...);
         */
        @Test
        @DisplayName("Process Shipments Online - Shipment Repository Called")
        void processShipmentsOnline_ShipmentRepositoryCalled() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));
            when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong()))
                    .thenReturn(Collections.emptyList());

            assertThrows(BadRequestException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));

            verify(shipmentRepository).findByOrderSummaryId(testOrderSummary.getOrderSummaryId());
        }

        /**
         * Purpose: Verify whitespace-only order ID still checks PO first.
         * Expected Result: NotFoundException for PO not found.
         * Assertions: assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Process Shipments Online - Whitespace Order ID Still Checks PO First")
        void processShipmentsOnline_WhitespaceOrderIdStillChecksPOFirst() {
            testRazorpayRequest.setRazorpayOrderId("   ");
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentProcessingService.processShipmentsAfterPaymentApproval(TEST_PO_ID, testRazorpayRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }
    }
}
