package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PaymentService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base class for PaymentService tests.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | CreateOrderTests                        | 12              |
 * | CreateOrderFollowUpTests                | 10              |
 * | VerifyPaymentTests                      | 10              |
 * | RecordCashPaymentTests                  | 16              |
 * | VerifyPaymentFollowUpTests              | 10              |
 * | RecordCashPaymentFollowUpTests          | 12              |
 * | GetPaymentsForPurchaseOrderTests        | 12              |
 * | GetPaymentByIdTests                     | 14              |
 * | IsPurchaseOrderPaidTests                | 10              |
 * | InitiateRefundTests                     | 10              |
 * | **Total**                               | **116**         |
 *
 * Note: PaymentService extends BaseService. Methods have different flows:
 * - createOrder, createOrderFollowUp, verifyPayment, verifyPaymentFollowUp, initiateRefund:
 *   First call getClientWithRazorpayCredentials() which throws NotFoundException(ClientErrorMessages.InvalidId)
 *   when getClientId() returns null.
 * - recordCashPayment, recordCashPaymentFollowUp, getPaymentsForPurchaseOrder:
 *   First lookup PO, then check client access.
 * - getPaymentById: First lookup Payment, then check client access.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class PaymentServiceTestBase extends BaseTest {

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    protected OrderSummaryRepository orderSummaryRepository;

    @Mock
    protected PaymentRepository paymentRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected Environment environment;

    @InjectMocks
    protected PaymentService paymentService;

    protected Client testClient;
    protected PurchaseOrder testPurchaseOrder;
    protected OrderSummary testOrderSummary;
    protected Payment testPayment;
    protected RazorpayOrderRequestModel testOrderRequest;
    protected CashPaymentRequestModel testCashPaymentRequest;
    protected RazorpayVerifyRequestModel testVerifyRequest;

    protected static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
    protected static final Long TEST_PO_ID = DEFAULT_PURCHASE_ORDER_ID;
    protected static final Long TEST_ORDER_SUMMARY_ID = 1L;
    protected static final Long TEST_PAYMENT_ID = 1L;

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

        // Initialize test payment
        testPayment = new Payment();
        testPayment.setPaymentId(TEST_PAYMENT_ID);
        testPayment.setEntityType("PURCHASE_ORDER");
        testPayment.setEntityId(TEST_PO_ID);
        testPayment.setClientId(TEST_CLIENT_ID);
        testPayment.setPaymentStatus("CAPTURED");
        testPayment.setOrderAmountPaise(100000L);
        testPayment.setAmountPaidPaise(100000L);

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
}
