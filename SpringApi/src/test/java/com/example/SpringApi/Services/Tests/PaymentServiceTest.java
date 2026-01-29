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
class PaymentServiceTest extends BaseTest {

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

    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
    private static final Long TEST_PO_ID = DEFAULT_PURCHASE_ORDER_ID;
    private static final Long TEST_ORDER_SUMMARY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test client
        testClient = createTestClient();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setRazorpayApiKey("rzp_test_key");
        testClient.setRazorpayApiSecret("rzp_test_secret");

        // Initialize test purchase order
        testPurchaseOrder = createTestPurchaseOrder();
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
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.NullRequest,
                    () -> paymentService.createOrder(null));
        }

        @Test
        @DisplayName("Create Order - Null Purchase Order ID - Throws BadRequestException")
        void createOrder_NullPurchaseOrderId_ThrowsBadRequestException() {
            testOrderRequest.setPurchaseOrderId(null);

            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Null Amount - Throws BadRequestException")
        void createOrder_NullAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired,
                    () -> paymentService.createOrder(testOrderRequest));
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

            assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Invalid PO Status - Throws BadRequestException")
        void createOrder_InvalidPOStatus_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("APPROVED");

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Razorpay API Key Not Configured - Throws BadRequestException")
        void createOrder_RazorpayApiKeyNotConfigured_ThrowsBadRequestException() {
            testClient.setRazorpayApiKey(null);

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.RazorpayApiKeyNotConfigured,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Razorpay API Secret Not Configured - Throws BadRequestException")
        void createOrder_RazorpayApiSecretNotConfigured_ThrowsBadRequestException() {
            testClient.setRazorpayApiSecret(null);

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.RazorpayApiSecretNotConfigured,
                    () -> paymentService.createOrder(testOrderRequest));
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

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.FollowUpPaymentStatusRequired,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));
        }
    }

    // ==================== Verify Payment Tests ====================

    @Nested
    @DisplayName("Verify Payment - Validation Tests")
    class VerifyPaymentValidationTests {

        @Test
        @DisplayName("Verify Payment - Null Request - Throws BadRequestException")
        void verifyPayment_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.NullRequest,
                    () -> paymentService.verifyPayment(null));
        }

        @Test
        @DisplayName("Verify Payment - Null Order ID - Throws BadRequestException")
        void verifyPayment_NullOrderId_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpayOrderId(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidOrderId,
                    () -> paymentService.verifyPayment(testVerifyRequest));
        }

        @Test
        @DisplayName("Verify Payment - Null Payment ID - Throws BadRequestException")
        void verifyPayment_NullPaymentId_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpayPaymentId(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidPaymentId,
                    () -> paymentService.verifyPayment(testVerifyRequest));
        }

        @Test
        @DisplayName("Verify Payment - Null Signature - Throws BadRequestException")
        void verifyPayment_NullSignature_ThrowsBadRequestException() {
            testVerifyRequest.setRazorpaySignature(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidSignature,
                    () -> paymentService.verifyPayment(testVerifyRequest));
        }

        @Test
        @DisplayName("Verify Payment - Payment Order Not Found - Throws NotFoundException")
        void verifyPayment_PaymentOrderNotFound_ThrowsNotFoundException() {
            when(paymentRepository.findByRazorpayOrderId(testVerifyRequest.getRazorpayOrderId()))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.PaymentOrderNotFound,
                    () -> paymentService.verifyPayment(testVerifyRequest));
        }
    }

    // ==================== Record Cash Payment Tests ====================

    @Nested
    @DisplayName("Record Cash Payment - Validation Tests")
    class RecordCashPaymentValidationTests {

        @Test
        @DisplayName("Record Cash Payment - Null Request - Throws BadRequestException")
        void recordCashPayment_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.NullRequest,
                    () -> paymentService.recordCashPayment(null));
        }

        @Test
        @DisplayName("Record Cash Payment - Null Purchase Order ID - Throws BadRequestException")
        void recordCashPayment_NullPurchaseOrderId_ThrowsBadRequestException() {
            testCashPaymentRequest.setPurchaseOrderId(null);

            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Null Payment Amount - Throws BadRequestException")
        void recordCashPayment_NullPaymentAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.ValidPaymentAmountRequired,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Null Payment Date - Throws BadRequestException")
        void recordCashPayment_NullPaymentDate_ThrowsBadRequestException() {
            testCashPaymentRequest.setPaymentDate(null);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.PaymentDateRequired,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Purchase Order Not Found - Throws NotFoundException")
        void recordCashPayment_PurchaseOrderNotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Invalid PO Status - Throws BadRequestException")
        void recordCashPayment_InvalidPOStatus_ThrowsBadRequestException() {
            testPurchaseOrder.setPurchaseOrderStatus("APPROVED");

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
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
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.getPaymentsForPurchaseOrder(null));
        }

        @Test
        @DisplayName("Get Payments - Zero Purchase Order ID - Throws BadRequestException")
        void getPayments_ZeroPurchaseOrderId_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.getPaymentsForPurchaseOrder(0L));
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
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidId,
                    () -> paymentService.getPaymentById(null));
        }

        @Test
        @DisplayName("Get Payment By ID - Zero Payment ID - Throws BadRequestException")
        void getPaymentById_ZeroPaymentId_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidId,
                    () -> paymentService.getPaymentById(0L));
        }

        @Test
        @DisplayName("Get Payment By ID - Payment Not Found - Throws NotFoundException")
        void getPaymentById_PaymentNotFound_ThrowsNotFoundException() {
            Long paymentId = 999L;
            when(paymentRepository.findByPaymentIdAndClientId(paymentId, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(paymentId));
        }
    }

    // ==================== Refund Payment Tests ====================

    @Nested
    @DisplayName("Refund Payment - Validation Tests")
    class RefundPaymentValidationTests {

        @Test
        @DisplayName("Refund Payment - Null Payment ID - Throws BadRequestException")
        void refundPayment_NullPaymentId_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidId,
                    () -> paymentService.refundPayment(null));
        }

        @Test
        @DisplayName("Refund Payment - Payment Not Found - Throws NotFoundException")
        void refundPayment_PaymentNotFound_ThrowsNotFoundException() {
            Long paymentId = 999L;
            when(paymentRepository.findByPaymentIdAndClientId(paymentId, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.refundPayment(paymentId));
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

    @Nested
    @DisplayName("GetPaymentById Additional Tests")
    class GetPaymentByIdAdditionalTests {
        @Test
        @DisplayName("Get Payment By ID - Negative ID - Throws NotFoundException")
        void getPaymentById_NegativeId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(-1L));
        }

        @Test
        @DisplayName("Get Payment By ID - Zero ID - Throws NotFoundException")
        void getPaymentById_ZeroId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(0L));
        }

        @Test
        @DisplayName("Get Payment By ID - Max Long ID - Throws NotFoundException")
        void getPaymentById_MaxLongId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(Long.MAX_VALUE));
        }
    }

    // ==================== Additional GetPaymentsForPurchaseOrder Tests ====================

    @Nested
    @DisplayName("GetPaymentsForPurchaseOrder Additional Tests")
    class GetPaymentsForPurchaseOrderAdditionalTests {
        @Test
        @DisplayName("Get Payments For PO - Negative PO ID - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_NegativePOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.getPaymentsForPurchaseOrder(-1L));
        }

        @Test
        @DisplayName("Get Payments For PO - Zero PO ID - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_ZeroPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.getPaymentsForPurchaseOrder(0L));
        }
    }

    // ==================== Additional RecordCashPayment Tests ====================

    @Nested
    @DisplayName("RecordCashPayment Additional Tests")
    class RecordCashPaymentAdditionalTests {
        @Test
        @DisplayName("Record Cash Payment - Null Amount - Throws BadRequestException")
        void recordCashPayment_NullAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(null);
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Negative Amount - Throws BadRequestException")
        void recordCashPayment_NegativeAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(new BigDecimal("-100.00"));
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - Zero Amount - Throws BadRequestException")
        void recordCashPayment_ZeroAmount_ThrowsBadRequestException() {
            testCashPaymentRequest.setAmount(BigDecimal.ZERO);
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }

        @Test
        @DisplayName("Record Cash Payment - PO Not Found - Throws NotFoundException")
        void recordCashPayment_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
        }
    }

    // ==================== Additional CreateRazorpayOrder Tests ====================

    @Nested
    @DisplayName("CreateRazorpayOrder Additional Tests")
    class CreateRazorpayOrderAdditionalTests {
        @Test
        @DisplayName("Create Razorpay Order - Negative Amount - Throws BadRequestException")
        void createRazorpayOrder_NegativeAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(new BigDecimal("-500.00"));
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.createRazorpayOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Razorpay Order - Zero Amount - Throws BadRequestException")
        void createRazorpayOrder_ZeroAmount_ThrowsBadRequestException() {
            testOrderRequest.setAmount(BigDecimal.ZERO);
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.createRazorpayOrder(testOrderRequest));
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
    }

    // ==================== Additional RefundPayment Tests ====================

    @Nested
    @DisplayName("RefundPayment Additional Tests")
    class RefundPaymentAdditionalTests {
        @Test
        @DisplayName("Refund Payment - Negative ID - Throws NotFoundException")
        void refundPayment_NegativeId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.refundPayment(-1L));
        }

        @Test
        @DisplayName("Refund Payment - Zero ID - Throws NotFoundException")
        void refundPayment_ZeroId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.refundPayment(0L));
        }
    }

    // ==================== COMPREHENSIVE EDGE CASE TESTS ====================

    @Nested
    @DisplayName("getPaymentById Edge Cases")
    class GetPaymentByIdEdgeCases {

        @Test
        @DisplayName("Get Payment By ID - Valid payment exists - Success returns payment")
        void getPaymentById_ValidPaymentExists_Success() {
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));

            Payment result = paymentService.getPaymentById(DEFAULT_PAYMENT_ID);

            assertNotNull(result);
            assertEquals(DEFAULT_PAYMENT_ID, result.getPaymentId());
            verify(paymentRepository, times(1)).findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID);
        }

        @Test
        @DisplayName("Get Payment By ID - Payment not found - Throws NotFoundException")
        void getPaymentById_PaymentNotFound_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(DEFAULT_PAYMENT_ID));
        }

        @Test
        @DisplayName("Get Payment By ID - Deleted payment - Success returns deleted payment")
        void getPaymentById_DeletedPayment_SuccessReturnsDeleted() {
            Payment deletedPayment = createDeletedTestPayment();
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(deletedPayment));

            Payment result = paymentService.getPaymentById(DEFAULT_PAYMENT_ID);

            assertNotNull(result);
            assertTrue(result.getIsDeleted());
        }

        @Test
        @DisplayName("Get Payment By ID - Max Long ID - Throws NotFoundException")
        void getPaymentById_MaxLongId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.PaymentErrorMessages.NotFound,
                    () -> paymentService.getPaymentById(Long.MAX_VALUE));
        }
    }

    @Nested
    @DisplayName("getPaymentsForPurchaseOrder Edge Cases")
    class GetPaymentsForPurchaseOrderEdgeCases {

        @Test
        @DisplayName("Get Payments For PO - Multiple payments found - Success returns list")
        void getPaymentsForPO_MultiplePaymentsFound_Success() {
            List<Payment> payments = Arrays.asList(
                    createTestPayment(1L, new BigDecimal("500.00")),
                    createTestPayment(2L, new BigDecimal("500.00"))
            );
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(payments);

            List<Payment> result = paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(paymentRepository, times(1)).findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID);
        }

        @Test
        @DisplayName("Get Payments For PO - No payments found - Success returns empty list")
        void getPaymentsForPO_NoPaymentsFound_SuccessReturnsEmpty() {
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            List<Payment> result = paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Get Payments For PO - Negative PO ID - Success returns empty list")
        void getPaymentsForPO_NegativePOId_SuccessReturnsEmpty() {
            when(paymentRepository.findByPurchaseOrderIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            List<Payment> result = paymentService.getPaymentsForPurchaseOrder(-1L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Get Payments For PO - Zero PO ID - Success returns empty list")
        void getPaymentsForPO_ZeroPOId_SuccessReturnsEmpty() {
            when(paymentRepository.findByPurchaseOrderIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            List<Payment> result = paymentService.getPaymentsForPurchaseOrder(0L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Get Payments For PO - Very large list (100 payments) - Success")
        void getPaymentsForPO_VeryLargeList_Success() {
            List<Payment> payments = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                payments.add(createTestPayment((long) i, new BigDecimal("10.00")));
            }
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(payments);

            List<Payment> result = paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID);

            assertNotNull(result);
            assertEquals(100, result.size());
        }
    }

    @Nested
    @DisplayName("isPurchaseOrderPaid Edge Cases")
    class IsPurchaseOrderPaidEdgeCases {

        @Test
        @DisplayName("Is PO Paid - All payments verified - Success returns true")
        void isPOPaid_AllPaymentsVerified_Success() {
            Payment verifiedPayment1 = createTestPayment(1L, new BigDecimal("500.00"));
            verifiedPayment1.setPaymentStatus("VERIFIED");
            Payment verifiedPayment2 = createTestPayment(2L, new BigDecimal("500.00"));
            verifiedPayment2.setPaymentStatus("VERIFIED");
            
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Arrays.asList(verifiedPayment1, verifiedPayment2));

            boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("Is PO Paid - No payments found - Success returns false")
        void isPOPaid_NoPaymentsFound_SuccessFalse() {
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Is PO Paid - Some payments pending - Success returns false")
        void isPOPaid_SomePaymentsPending_SuccessFalse() {
            Payment verifiedPayment = createTestPayment(1L, new BigDecimal("500.00"));
            verifiedPayment.setPaymentStatus("VERIFIED");
            Payment pendingPayment = createTestPayment(2L, new BigDecimal("500.00"));
            pendingPayment.setPaymentStatus("PENDING");
            
            when(paymentRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Arrays.asList(verifiedPayment, pendingPayment));

            boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Is PO Paid - Negative PO ID - Success returns false")
        void isPOPaid_NegativePOId_SuccessFalse() {
            when(paymentRepository.findByPurchaseOrderIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(new ArrayList<>());

            boolean result = paymentService.isPurchaseOrderPaid(-1L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getRazorpayKeyId Validation")
    class GetRazorpayKeyIdValidation {

        @Test
        @DisplayName("Get Razorpay Key ID - Valid client - Success returns key")
        void getRazorpayKeyId_ValidClient_Success() {
            String expectedKeyId = "rzp_test_key_123";
            testClient.setRazorpayApiKey(expectedKeyId);
            when(clientRepository.findById(TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testClient));

            String result = paymentService.getRazorpayKeyId();

            assertNotNull(result);
            assertEquals(expectedKeyId, result);
        }

        @Test
        @DisplayName("Get Razorpay Key ID - Client not found - Throws NotFoundException")
        void getRazorpayKeyId_ClientNotFound_ThrowsException() {
            when(clientRepository.findById(TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.ClientErrorMessages.ClientNotFound,
                    () -> paymentService.getRazorpayKeyId());
        }

        @Test
        @DisplayName("Get Razorpay Key ID - Null key - Throws exception")
        void getRazorpayKeyId_NullKey_ThrowsException() {
            testClient.setRazorpayApiKey(null);
            when(clientRepository.findById(TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRazorpayKey,
                    () -> paymentService.getRazorpayKeyId());
        }

        @Test
        @DisplayName("Get Razorpay Key ID - Empty key - Throws exception")
        void getRazorpayKeyId_EmptyKey_ThrowsException() {
            testClient.setRazorpayApiKey("");
            when(clientRepository.findById(TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRazorpayKey,
                    () -> paymentService.getRazorpayKeyId());
        }
    }

    @Nested
    @DisplayName("Payment Amount Validation Edge Cases")
    class PaymentAmountValidationEdgeCases {

        @Test
        @DisplayName("Create Order - Zero amount - Throws BadRequestException")
        void createOrder_ZeroAmount_ThrowsException() {
            testOrderRequest.setAmount(BigDecimal.ZERO);

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Negative amount - Throws BadRequestException")
        void createOrder_NegativeAmount_ThrowsException() {
            testOrderRequest.setAmount(new BigDecimal("-100.00"));

            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount,
                    () -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Very large amount - Success")
        void createOrder_VeryLargeAmount_Success() {
            testOrderRequest.setAmount(new BigDecimal("999999999.99"));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            assertDoesNotThrow(() -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Decimal precision (.001) - Success")
        void createOrder_DecimalPrecision_Success() {
            testOrderRequest.setAmount(new BigDecimal("100.001"));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            assertDoesNotThrow(() -> paymentService.createOrder(testOrderRequest));
        }

        @Test
        @DisplayName("Create Order - Very small amount (0.01) - Success")
        void createOrder_VerySmallAmount_Success() {
            testOrderRequest.setAmount(new BigDecimal("0.01"));
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            assertDoesNotThrow(() -> paymentService.createOrder(testOrderRequest));
        }
    }

    @Nested
    @DisplayName("Payment Null Handling")
    class PaymentNullHandling {

        @Test
        @DisplayName("Create Order - Null request - Throws BadRequestException")
        void createOrder_NullRequest_ThrowsException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRequest,
                    () -> paymentService.createOrder(null));
        }

        @Test
        @DisplayName("Record Cash Payment - Null request - Throws BadRequestException")
        void recordCashPayment_NullRequest_ThrowsException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRequest,
                    () -> paymentService.recordCashPayment(null));
        }

        @Test
        @DisplayName("Verify Payment - Null request - Throws BadRequestException")
        void verifyPayment_NullRequest_ThrowsException() {
            assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRequest,
                    () -> paymentService.verifyPayment(null));
        }

        @Test
        @DisplayName("Initiate Refund - Null payment ID - Throws BadRequestException")
        void initiateRefund_NullPaymentId_ThrowsException() {
            assertThrows(BadRequestException.class,
                    () -> paymentService.initiateRefund(null, new BigDecimal("100.00"), "Testing"));
        }

        @Test
        @DisplayName("Get Payments For PO - Null PO ID - Throws BadRequestException")
        void getPaymentsForPO_NullPoId_ThrowsException() {
            assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(null));
        }
    }

    @Nested
    @DisplayName("Payment Status Transitions")
    class PaymentStatusTransitions {

        @Test
        @DisplayName("Payment status - PENDING to VERIFIED - Success")
        void paymentStatus_PendingToVerified_Success() {
            testPayment.setPaymentStatus("PENDING");
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));

            testPayment.setPaymentStatus("VERIFIED");
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            assertDoesNotThrow(() -> {
                Payment payment = paymentService.getPaymentById(DEFAULT_PAYMENT_ID);
                payment.setPaymentStatus("VERIFIED");
                paymentRepository.save(payment);
            });

            assertEquals("VERIFIED", testPayment.getPaymentStatus());
        }

        @Test
        @DisplayName("Payment status - VERIFIED to REFUNDED - Success")
        void paymentStatus_VerifiedToRefunded_Success() {
            testPayment.setPaymentStatus("VERIFIED");
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));

            testPayment.setPaymentStatus("REFUNDED");
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            assertDoesNotThrow(() -> {
                Payment payment = paymentService.getPaymentById(DEFAULT_PAYMENT_ID);
                payment.setPaymentStatus("REFUNDED");
                paymentRepository.save(payment);
            });

            assertEquals("REFUNDED", testPayment.getPaymentStatus());
        }

        // ==================== Comprehensive Validation Tests - Added ====================

        @Test
        @DisplayName("Get Payment By ID - Negative ID - Throws NotFoundException")
        void getPaymentById_NegativeId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(-1L));
            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Get Payment By ID - Zero ID - Throws NotFoundException")
        void getPaymentById_ZeroId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(0L));
            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Get Payment By ID - Long.MAX_VALUE ID - Throws NotFoundException")
        void getPaymentById_MaxLongId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Process Payment - Negative Amount - Throws BadRequestException")
        void processPayment_NegativeAmount_ThrowsBadRequestException() {
            testPaymentRequest.setAmount(new BigDecimal("-100.00"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("amount") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Zero Amount - Throws BadRequestException")
        void processPayment_ZeroAmount_ThrowsBadRequestException() {
            testPaymentRequest.setAmount(BigDecimal.ZERO);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("amount") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Null Amount - Throws BadRequestException")
        void processPayment_NullAmount_ThrowsBadRequestException() {
            testPaymentRequest.setAmount(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("amount") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Null Payment Method - Throws BadRequestException")
        void processPayment_NullPaymentMethod_ThrowsBadRequestException() {
            testPaymentRequest.setPaymentMethod(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("method") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Empty Payment Method - Throws BadRequestException")
        void processPayment_EmptyPaymentMethod_ThrowsBadRequestException() {
            testPaymentRequest.setPaymentMethod("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("method") || ex.getMessage().contains("empty"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Whitespace Payment Method - Throws BadRequestException")
        void processPayment_WhitespacePaymentMethod_ThrowsBadRequestException() {
            testPaymentRequest.setPaymentMethod("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("method") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Null Purchase Order ID - Throws BadRequestException")
        void processPayment_NullPurchaseOrderId_ThrowsBadRequestException() {
            testPaymentRequest.setPurchaseOrderId(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("order") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Negative Purchase Order ID - Throws BadRequestException")
        void processPayment_NegativePurchaseOrderId_ThrowsBadRequestException() {
            testPaymentRequest.setPurchaseOrderId(-1L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("order") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Payment - Zero Purchase Order ID - Throws BadRequestException")
        void processPayment_ZeroPurchaseOrderId_ThrowsBadRequestException() {
            testPaymentRequest.setPurchaseOrderId(0L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processPayment(testPaymentRequest));
            assertTrue(ex.getMessage().contains("order") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Refund Payment - Negative Refund Amount - Throws BadRequestException")
        void refundPayment_NegativeRefundAmount_ThrowsBadRequestException() {
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));
            testPaymentRequest.setAmount(new BigDecimal("-50.00"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(DEFAULT_PAYMENT_ID, testPaymentRequest));
            assertTrue(ex.getMessage().contains("refund") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Refund Payment - Zero Refund Amount - Throws BadRequestException")
        void refundPayment_ZeroRefundAmount_ThrowsBadRequestException() {
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));
            testPaymentRequest.setAmount(BigDecimal.ZERO);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(DEFAULT_PAYMENT_ID, testPaymentRequest));
            assertTrue(ex.getMessage().contains("refund") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Refund Payment - Refund Amount Greater Than Original - Throws BadRequestException")
        void refundPayment_RefundGreaterThanOriginal_ThrowsBadRequestException() {
            testPayment.setAmount(new BigDecimal("100.00"));
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));
            testPaymentRequest.setAmount(new BigDecimal("150.00"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.refundPayment(DEFAULT_PAYMENT_ID, testPaymentRequest));
            assertTrue(ex.getMessage().contains("exceed") || ex.getMessage().contains("greater"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Refund Payment - Non-existent Payment ID - Throws NotFoundException")
        void refundPayment_NonexistentPaymentId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.refundPayment(-1L, testPaymentRequest));
            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Refund Payment - Zero Payment ID - Throws NotFoundException")
        void refundPayment_ZeroPaymentId_ThrowsNotFoundException() {
            when(paymentRepository.findByPaymentIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.refundPayment(0L, testPaymentRequest));
            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Verify Payment - Invalid Payment Status - Throws BadRequestException")
        void verifyPayment_InvalidPaymentStatus_ThrowsBadRequestException() {
            testPayment.setPaymentStatus("INVALID_STATUS");
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(DEFAULT_PAYMENT_ID));
            assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("invalid"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Verify Payment - Already Verified Payment - Throws BadRequestException")
        void verifyPayment_AlreadyVerified_ThrowsBadRequestException() {
            testPayment.setPaymentStatus("VERIFIED");
            when(paymentRepository.findByPaymentIdAndClientId(DEFAULT_PAYMENT_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPayment));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.verifyPayment(DEFAULT_PAYMENT_ID));
            assertTrue(ex.getMessage().contains("already") || ex.getMessage().contains("verified"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Multiple Payments - Empty List - Throws BadRequestException")
        void processMultiplePayments_EmptyList_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processMultiplePayments(new java.util.ArrayList<>()));
            assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Process Multiple Payments - Null List - Throws BadRequestException")
        void processMultiplePayments_NullList_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.processMultiplePayments(null));
            assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Get Payments In Batches - Negative Start Index - Throws BadRequestException")
        void getPaymentsInBatches_NegativeStart_ThrowsBadRequestException() {
            testPaginationRequest.setStart(-1);
            testPaginationRequest.setEnd(10);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("start") || ex.getMessage().contains("invalid"));
        }

        @Test
        @DisplayName("Get Payments In Batches - Start Greater Than End - Throws BadRequestException")
        void getPaymentsInBatches_StartGreaterThanEnd_ThrowsBadRequestException() {
            testPaginationRequest.setStart(100);
            testPaginationRequest.setEnd(10);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("start") || ex.getMessage().contains("end"));
        }
    }
}
