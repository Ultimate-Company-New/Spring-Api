package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PaymentService;
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
 * Unit tests for {@link PaymentService}.
 * 
 * Tests Razorpay order creation, payment verification, refunds,
 * cash payments, and purchase order status updates.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private OrderSummaryRepository orderSummaryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private Environment environment;

    @InjectMocks
    private PaymentService paymentService;

    private Client testClient;
    private PurchaseOrder testPurchaseOrder;
    private OrderSummary testOrderSummary;
    private RazorpayOrderRequestModel testOrderRequest;
    private CashPaymentRequestModel testCashPaymentRequest;
    private RazorpayVerifyRequestModel testVerifyRequest;

    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_PO_ID = 1L;
    private static final Long TEST_ORDER_SUMMARY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test client
        testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setRazorpayApiKey("rzp_test_key");
        testClient.setRazorpayApiSecret("rzp_test_secret");

        // Initialize test purchase order
        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrder.setClientId(TEST_CLIENT_ID);
        testPurchaseOrder.setPurchaseOrderStatus("PENDING_APPROVAL");

        // Initialize test order summary
        testOrderSummary = new OrderSummary();
        testOrderSummary.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testOrderSummary.setEntityType(OrderSummary.EntityType.PURCHASE_ORDER.getValue());
        testOrderSummary.setEntityId(TEST_PO_ID);
        testOrderSummary.setGrandTotal(new BigDecimal("1000.00"));
        testOrderSummary.setPendingAmount(new BigDecimal("1000.00"));

        // Initialize test order request
        testOrderRequest = new RazorpayOrderRequestModel();
        testOrderRequest.setPurchaseOrderId(TEST_PO_ID);
        testOrderRequest.setAmount(new BigDecimal("1000.00"));

        // Initialize test cash payment request
        testCashPaymentRequest = new CashPaymentRequestModel();
        testCashPaymentRequest.setPurchaseOrderId(TEST_PO_ID);
        testCashPaymentRequest.setAmount(new BigDecimal("1000.00"));
        testCashPaymentRequest.setPaymentDate(LocalDate.now());

        // Initialize test verify request
        testVerifyRequest = new RazorpayVerifyRequestModel();
        testVerifyRequest.setRazorpayOrderId("order_test123");
        testVerifyRequest.setRazorpayPaymentId("pay_test123");
        testVerifyRequest.setRazorpaySignature("sig_test123");
        testVerifyRequest.setPurchaseOrderId(TEST_PO_ID);
    }

    // ==================== Create Razorpay Order Tests ====================

    @Nested
    @DisplayName("Create Razorpay Order - Validation Tests")
    class CreateRazorpayOrderValidationTests {

        @Test
        @DisplayName("Create Order - Null Request - Throws BadRequestException")
        void createOrder_NullRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(null));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Null Purchase Order ID - Throws BadRequestException")
        void createOrder_NullPurchaseOrderId_ThrowsBadRequestException() {
            testOrderRequest.setPurchaseOrderId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Null Amount - Throws BadRequestException")
        void createOrder_NullAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Zero Amount - Throws BadRequestException")
        void createOrder_ZeroAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(BigDecimal.ZERO);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Negative Amount - Throws BadRequestException")
        void createOrder_NegativeAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(new BigDecimal("-100.00"));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Purchase Order Not Found - Throws NotFoundException")
        void createOrder_PurchaseOrderNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Invalid PO Status - Throws BadRequestException")
        void createOrder_InvalidPOStatus_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("APPROVED");

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Razorpay API Key Not Configured - Throws BadRequestException")
        void createOrder_RazorpayApiKeyNotConfigured_ThrowsBadRequestException() {
            testClient.setRazorpayApiKey(null);

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.RazorpayApiKeyNotConfigured, exception.getMessage());
        }

        @Test
        @DisplayName("Create Order - Razorpay API Secret Not Configured - Throws BadRequestException")
        void createOrder_RazorpayApiSecretNotConfigured_ThrowsBadRequestException() {
            testClient.setRazorpayApiSecret(null);

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrder(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.RazorpayApiSecretNotConfigured, exception.getMessage());
        }
    }

    // ==================== Create Follow-Up Order Tests ====================

    @Nested
    @DisplayName("Create Follow-Up Razorpay Order - Validation Tests")
    class CreateFollowUpOrderValidationTests {

        @Test
        @DisplayName("Create Follow-Up Order - Invalid Status for Follow-Up - Throws BadRequestException")
        void createFollowUpOrder_InvalidStatusForFollowUp_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("PENDING_APPROVAL");

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.FollowUpPaymentStatusRequired, exception.getMessage());
        }
    }

    // ==================== Verify Payment Tests ====================

    @Nested
    @DisplayName("Verify Payment - Validation Tests")
    class VerifyPaymentValidationTests {

        @Test
        @DisplayName("Verify Payment - Null Request - Throws BadRequestException")
        void verifyPayment_NullRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(null));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Verify Payment - Null Order ID - Throws BadRequestException")
        void verifyPayment_NullOrderId_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpayOrderId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Verify Payment - Null Payment ID - Throws BadRequestException")
        void verifyPayment_NullPaymentId_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpayPaymentId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Verify Payment - Null Signature - Throws BadRequestException")
        void verifyPayment_NullSignature_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpaySignature(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Verify Payment - Payment Order Not Found - Throws NotFoundException")
        void verifyPayment_PaymentOrderNotFound_ThrowsNotFoundException() {
            when(paymentRepository.findByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId()))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.PaymentOrderNotFound, exception.getMessage());
        }
    }

    // ==================== Record Cash Payment Tests ====================

    @Nested
    @DisplayName("Record Cash Payment - Validation Tests")
    class RecordCashPaymentValidationTests {

        @Test
        @DisplayName("Record Cash Payment - Null Request - Throws BadRequestException")
        void recordCashPayment_NullRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.recordCashPayment(null));

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Record Cash Payment - Null Purchase Order ID - Throws BadRequestException")
        void recordCashPayment_NullPurchaseOrderId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Record Cash Payment - Null Payment Amount - Throws BadRequestException")
        void recordCashPayment_NullPaymentAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Record Cash Payment - Null Payment Date - Throws BadRequestException")
        void recordCashPayment_NullPaymentDate_ThrowsBadRequestException() {
            testCashPaymentRequest.setPaymentDate(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.PaymentDateRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Record Cash Payment - Purchase Order Not Found - Throws NotFoundException")
        void recordCashPayment_PurchaseOrderNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Record Cash Payment - Invalid PO Status - Throws BadRequestException")
        void recordCashPayment_InvalidPOStatus_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("APPROVED");

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));

            assertEquals(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid, exception.getMessage());
        }
    }

    // ==================== Get Payments For Purchase Order Tests
    // ====================

    @Nested
    @DisplayName("Get Payments For Purchase Order - Validation Tests")
    class GetPaymentsForPurchaseOrderValidationTests {

        @Test
        @DisplayName("Get Payments - Null Purchase Order ID - Throws BadRequestException")
        void getPayments_NullPurchaseOrderId_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(null));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get Payments - Zero Purchase Order ID - Throws BadRequestException")
        void getPayments_ZeroPurchaseOrderId_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(0L));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get Payments - Purchase Order Not Found - Throws NotFoundException")
        void getPayments_PurchaseOrderNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== Get Payment By ID Tests ====================

    @Nested
    @DisplayName("Get Payment By ID - Validation Tests")
    class GetPaymentByIdValidationTests {

        @Test
        @DisplayName("Get Payment By ID - Null Payment ID - Throws BadRequestException")
        void getPaymentById_NullPaymentId_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentById(null));

            assertEquals(ErrorMessages.PaymentErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get Payment By ID - Zero Payment ID - Throws BadRequestException")
        void getPaymentById_ZeroPaymentId_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentById(0L));

            assertEquals(ErrorMessages.PaymentErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get Payment By ID - Payment Not Found - Throws NotFoundException")
        void getPaymentById_PaymentNotFound_ThrowsNotFoundException() {
            Long paymentId = 999L;
            when(paymentRepository.findByPaymentIdAndClientId(paymentId, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(paymentId));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }
    }

    // ==================== Refund Payment Tests ====================

    @Nested
    @DisplayName("Refund Payment - Validation Tests")
    class RefundPaymentValidationTests {

        @Test
        @DisplayName("Refund Payment - Null Payment ID - Throws BadRequestException")
        void refundPayment_NullPaymentId_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(null));

            assertEquals(ErrorMessages.PaymentErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Refund Payment - Payment Not Found - Throws NotFoundException")
        void refundPayment_PaymentNotFound_ThrowsNotFoundException() {
            Long paymentId = 999L;
            when(paymentRepository.findByPaymentIdAndClientId(paymentId, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.refundPayment(paymentId));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }

        @Test
        @DisplayName("Refund Payment - Cannot Refund Manual Payment - Throws BadRequestException")
        void refundPayment_CannotRefundManualPayment_ThrowsBadRequestException() {
            Payment manualPayment = new Payment();
            manualPayment.setPaymentId(1L);
            manualPayment.setClientId(TEST_CLIENT_ID);
            manualPayment.setPaymentGateway("MANUAL");
            manualPayment.setRazorpayPaymentId(null);

            when(paymentRepository.findByPaymentIdAndClientId(1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(manualPayment));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(1L));

            assertEquals(ErrorMessages.PaymentErrorMessages.CannotRefund, exception.getMessage());
        }
    }

    // ==================== Additional GetPaymentByID Tests ====================

    @Test
    @DisplayName("Get Payment By ID - Negative ID - Not Found")
    void getPaymentById_NegativeId_ThrowsNotFoundException() {
        when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(-1L));
        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Payment By ID - Zero ID - Not Found")
    void getPaymentById_ZeroId_ThrowsNotFoundException() {
        when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(0L));
        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Payment By ID - Long.MAX_VALUE - Not Found")
    void getPaymentById_MaxLongId_ThrowsNotFoundException() {
        when(paymentRepository.findByPaymentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
    }

    // ==================== Additional GetPaymentsForPurchaseOrder Tests ====================

    @Test
    @DisplayName("Get Payments For PO - Negative PO ID - Not Found")
    void getPaymentsForPurchaseOrder_NegativePOId_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(-1L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(-1L));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Payments For PO - Zero PO ID - Not Found")
    void getPaymentsForPurchaseOrder_ZeroPOId_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(0L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.getPaymentsForPurchaseOrder(0L));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    // ==================== Additional RecordCashPayment Tests ====================

    @Test
    @DisplayName("Record Cash Payment - Null Request - Throws BadRequestException")
    void recordCashPayment_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.recordCashPayment(null));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Record Cash Payment - Null Amount - Throws BadRequestException")
    void recordCashPayment_NullAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Record Cash Payment - Negative Amount - Throws BadRequestException")
    void recordCashPayment_NegativeAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(new BigDecimal("-100.00"));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Record Cash Payment - Zero Amount - Throws BadRequestException")
    void recordCashPayment_ZeroAmount_ThrowsBadRequestException() {
        testCashPaymentRequest.setAmount(BigDecimal.ZERO);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Record Cash Payment - Null PO ID - Throws BadRequestException")
    void recordCashPayment_NullPOId_ThrowsBadRequestException() {
        testCashPaymentRequest.setPurchaseOrderId(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Record Cash Payment - PO Not Found - Throws NotFoundException")
    void recordCashPayment_PONotFound_ThrowsNotFoundException() {
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.recordCashPayment(testCashPaymentRequest));
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
    }

    // ==================== Additional CreateRazorpayOrder Tests ====================

    @Test
    @DisplayName("Create Razorpay Order - Null Request - Throws BadRequestException")
    void createRazorpayOrder_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.createRazorpayOrder(null));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Create Razorpay Order - Negative Amount - Throws BadRequestException")
    void createRazorpayOrder_NegativeAmount_ThrowsBadRequestException() {
        testOrderRequest.setAmount(new BigDecimal("-500.00"));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.createRazorpayOrder(testOrderRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Create Razorpay Order - Zero Amount - Throws BadRequestException")
    void createRazorpayOrder_ZeroAmount_ThrowsBadRequestException() {
        testOrderRequest.setAmount(BigDecimal.ZERO);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> paymentService.createRazorpayOrder(testOrderRequest));
        assertEquals(ErrorMessages.PaymentErrorMessages.InvalidAmount, ex.getMessage());
    }

    @Test
    @DisplayName("Create Razorpay Order - Large Amount - Success")
    void createRazorpayOrder_LargeAmount_Success() {
        testOrderRequest.setAmount(new BigDecimal("999999.99"));
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPurchaseOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());
        
        assertDoesNotThrow(() -> paymentService.createRazorpayOrder(testOrderRequest));
    }

    // ==================== Additional RefundPayment Tests ====================

    @Test
    @DisplayName("Refund Payment - Negative ID - Not Found")
    void refundPayment_NegativeId_ThrowsNotFoundException() {
        when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.refundPayment(-1L));
        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Refund Payment - Zero ID - Not Found")
    void refundPayment_ZeroId_ThrowsNotFoundException() {
        when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> paymentService.refundPayment(0L));
        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
    }
}
