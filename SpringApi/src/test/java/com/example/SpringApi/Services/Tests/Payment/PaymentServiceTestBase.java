package com.example.SpringApi.Services.Tests.Payment;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ResponseModels.RazorpayOrderResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PaymentService;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

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
public class PaymentServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_CLIENT_ID = 1L;
    protected static final Long DEFAULT_PURCHASE_ORDER_ID = 1L;
    protected static final String DEFAULT_CREATED_USER = "admin";

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

    @Mock
    protected PaymentService paymentServiceMock;

    protected PaymentController paymentControllerWithMock;

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

        paymentControllerWithMock = new PaymentController(paymentServiceMock, null);
    }

    // ==================== FACTORY METHODS ====================

    protected Client createTestClient() {
        Client client = new Client();
        client.setClientId(DEFAULT_CLIENT_ID);
        client.setName("Test Client");
        client.setCreatedUser(DEFAULT_CREATED_USER);
        client.setModifiedUser(DEFAULT_CREATED_USER);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    protected PurchaseOrder createTestPurchaseOrder() {
        return createTestPurchaseOrder(DEFAULT_PURCHASE_ORDER_ID);
    }

    protected PurchaseOrder createTestPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseOrderId(purchaseOrderId);
        po.setClientId(DEFAULT_CLIENT_ID);
        po.setVendorNumber("VENDOR-001");
        po.setPurchaseOrderStatus("DRAFT");
        po.setAssignedLeadId(1L);
        po.setIsDeleted(false);
        po.setCreatedUser(DEFAULT_CREATED_USER);
        po.setModifiedUser(DEFAULT_CREATED_USER);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        return po;
    }

    // ==================== STUB HELPERS ====================

    protected void stubClientRepositoryFindByIdNull(Optional<Client> result) {
        lenient().when(clientRepository.findById(isNull())).thenReturn(result);
    }

    protected void stubClientRepositoryFindById(Long clientId, Optional<Client> result) {
        lenient().when(clientRepository.findById(clientId)).thenReturn(result);
    }

    protected void stubClientRepositoryFindByIdAny(Optional<Client> result) {
        lenient().when(clientRepository.findById(any())).thenReturn(result);
    }

    protected void stubPurchaseOrderRepositoryFindById(Long purchaseOrderId, Optional<PurchaseOrder> result) {
        lenient().when(purchaseOrderRepository.findById(purchaseOrderId)).thenReturn(result);
    }

    protected void stubPurchaseOrderRepositoryFindByIdAny(Optional<PurchaseOrder> result) {
        lenient().when(purchaseOrderRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubPaymentRepositoryFindById(Long paymentId, Optional<Payment> result) {
        lenient().when(paymentRepository.findById(paymentId)).thenReturn(result);
    }

    protected void stubPaymentRepositoryFindByIdAny(Optional<Payment> result) {
        lenient().when(paymentRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubPaymentRepositoryHasSuccessfulPayment(Long purchaseOrderId, boolean result) {
        lenient().when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", purchaseOrderId)).thenReturn(result);
    }

    protected void stubPaymentRepositoryHasSuccessfulPaymentAny(boolean result) {
        lenient().when(paymentRepository.hasSuccessfulPayment(eq("PURCHASE_ORDER"), anyLong())).thenReturn(result);
    }

    protected PaymentVerificationResponseModel createSuccessPaymentVerificationResponse() {
        return PaymentVerificationResponseModel.success("payment-id", TEST_PO_ID, "APPROVED");
    }

    protected void stubPaymentServiceCreateOrder(RazorpayOrderResponseModel response) {
        lenient().when(paymentServiceMock.createOrder(any(RazorpayOrderRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceCreateOrderThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).createOrder(any(RazorpayOrderRequestModel.class));
    }

    protected void stubPaymentServiceCreateOrderFollowUp(RazorpayOrderResponseModel response) {
        lenient().when(paymentServiceMock.createOrderFollowUp(any(RazorpayOrderRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceCreateOrderFollowUpThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).createOrderFollowUp(any(RazorpayOrderRequestModel.class));
    }

    protected void stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel response) {
        lenient().when(paymentServiceMock.verifyPayment(any(RazorpayVerifyRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceVerifyPaymentThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).verifyPayment(any(RazorpayVerifyRequestModel.class));
    }

    protected void stubPaymentServiceVerifyPaymentFollowUp(PaymentVerificationResponseModel response) {
        lenient().when(paymentServiceMock.verifyPaymentFollowUp(any(RazorpayVerifyRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceVerifyPaymentFollowUpThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).verifyPaymentFollowUp(any(RazorpayVerifyRequestModel.class));
    }

    protected void stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel response) {
        lenient().when(paymentServiceMock.recordCashPayment(any(CashPaymentRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceRecordCashPaymentThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).recordCashPayment(any(CashPaymentRequestModel.class));
    }

    protected void stubPaymentServiceRecordCashPaymentFollowUp(PaymentVerificationResponseModel response) {
        lenient().when(paymentServiceMock.recordCashPaymentFollowUp(any(CashPaymentRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceRecordCashPaymentFollowUpThrowsUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).recordCashPaymentFollowUp(any(CashPaymentRequestModel.class));
    }

    protected void stubPaymentServiceGetRazorpayKeyId(String keyId) {
        lenient().when(paymentServiceMock.getRazorpayKeyId()).thenReturn(keyId);
    }

    protected void stubPaymentServiceGetRazorpayKeyIdThrows(RuntimeException exception) {
        lenient().doThrow(exception).when(paymentServiceMock).getRazorpayKeyId();
    }

    protected void stubPaymentServiceGeneratePaymentReceiptPDF(byte[] bytes) throws Exception {
        lenient().when(paymentServiceMock.generatePaymentReceiptPDF(anyLong())).thenReturn(bytes);
    }

    protected void stubPaymentServiceGeneratePaymentReceiptPDFThrowsUnauthorized() throws Exception {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(paymentServiceMock).generatePaymentReceiptPDF(anyLong());
    }
}
