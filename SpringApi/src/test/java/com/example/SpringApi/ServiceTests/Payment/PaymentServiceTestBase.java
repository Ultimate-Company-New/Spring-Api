package com.example.SpringApi.ServiceTests.Payment;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

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
import com.razorpay.OrderClient;
import com.razorpay.PaymentClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/**
 * Base class for PaymentService tests.
 *
 * <p>Test Group Summary: | Group Name | Number of Tests | | :--------------------------------------
 * | :-------------- | | CreateOrderTests | 12 | | CreateOrderFollowUpTests | 10 | |
 * VerifyPaymentTests | 10 | | RecordCashPaymentTests | 16 | | VerifyPaymentFollowUpTests | 10 | |
 * RecordCashPaymentFollowUpTests | 12 | | GetPaymentsForPurchaseOrderTests | 12 | |
 * GetPaymentByIdTests | 14 | | IsPurchaseOrderPaidTests | 10 | | InitiateRefundTests | 10 | |
 * **Total** | **116** |
 *
 * <p>Note: PaymentService extends BaseService. Methods have different flows: - createOrder,
 * createOrderFollowUp, verifyPayment, verifyPaymentFollowUp, initiateRefund: First call
 * getClientWithRazorpayCredentials() which throws NotFoundException(ClientErrorMessages.INVALID_ID)
 * when getClientId() returns null. - recordCashPayment, recordCashPaymentFollowUp,
 * getPaymentsForPurchaseOrder: First lookup PO, then check client access. - getPaymentById: First
 * lookup Payment, then check client access.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_CLIENT_ID = 1L;
  protected static final Long DEFAULT_PURCHASE_ORDER_ID = 1L;
  protected static final String DEFAULT_CREATED_USER = "admin";

  @Mock protected ClientRepository clientRepository;

  @Mock protected PurchaseOrderRepository purchaseOrderRepository;

  @Mock protected OrderSummaryRepository orderSummaryRepository;

  @Mock protected PaymentRepository paymentRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected Environment environment;

  @InjectMocks protected PaymentService paymentService;

  @Mock protected PaymentService paymentServiceMock;

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
  protected MockedConstruction<RazorpayClient> mockedRazorpayClient;
  protected OrderClient mockedRazorpayOrderClient;
  protected PaymentClient mockedRazorpayPaymentClient;
  protected JSONObject capturedRazorpayOrderCreateRequest;
  protected JSONObject capturedRazorpayRefundRequest;

  @BeforeEach
  void setUp() {
    closeMockedRazorpayClientConstruction();
    capturedRazorpayOrderCreateRequest = null;
    capturedRazorpayRefundRequest = null;

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

  protected void stubClientRepositoryFindByIdDefaultClient(Optional<Client> result) {
    lenient().when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(result);
  }

  protected void stubPurchaseOrderRepositoryFindById(
      Long purchaseOrderId, Optional<PurchaseOrder> result) {
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
    lenient()
        .when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", purchaseOrderId))
        .thenReturn(result);
  }

  protected void stubPaymentRepositoryHasSuccessfulPaymentAny(boolean result) {
    lenient()
        .when(paymentRepository.hasSuccessfulPayment(eq("PURCHASE_ORDER"), anyLong()))
        .thenReturn(result);
  }

  protected void stubPaymentRepositoryFindByRazorpayOrderId(
      String razorpayOrderId, Optional<Payment> result) {
    lenient().when(paymentRepository.findByRazorpayOrderId(razorpayOrderId)).thenReturn(result);
  }

  protected void stubPaymentRepositoryFindByRazorpayOrderIdAny(Optional<Payment> result) {
    lenient().when(paymentRepository.findByRazorpayOrderId(anyString())).thenReturn(result);
  }

  protected void stubPaymentRepositoryFindAllByPurchaseOrderId(
      Long purchaseOrderId, List<Payment> result) {
    lenient().when(paymentRepository.findAllByPurchaseOrderId(purchaseOrderId)).thenReturn(result);
  }

  protected void stubPaymentRepositoryGetTotalNetPaidPaiseForEntity(
      String entityType, Long entityId, Long totalPaidPaise) {
    lenient()
        .when(paymentRepository.getTotalNetPaidPaiseForEntity(entityType, entityId))
        .thenReturn(totalPaidPaise);
  }

  protected void stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrder(
      Long purchaseOrderId, Long totalPaidPaise) {
    lenient()
        .when(
            paymentRepository.getTotalNetPaidPaiseForEntity(
                Payment.EntityType.PURCHASE_ORDER.getValue(), purchaseOrderId))
        .thenReturn(totalPaidPaise);
  }

  protected void stubPaymentRepositoryGetTotalNetPaidPaiseForPurchaseOrderSequence(
      Long purchaseOrderId, Long... totals) {
    if (totals == null || totals.length == 0) {
      return;
    }
    Long[] remaining =
        totals.length > 1 ? java.util.Arrays.copyOfRange(totals, 1, totals.length) : new Long[0];
    lenient()
        .when(
            paymentRepository.getTotalNetPaidPaiseForEntity(
                Payment.EntityType.PURCHASE_ORDER.getValue(), purchaseOrderId))
        .thenReturn(totals[0], remaining);
  }

  protected void stubPaymentRepositorySaveReturnsArgument() {
    lenient()
        .when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubPaymentRepositorySaveAssignsPaymentId(Long paymentId) {
    lenient()
        .when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(
            invocation -> {
              Payment payment = invocation.getArgument(0);
              if (payment.getPaymentId() == null) {
                payment.setPaymentId(paymentId);
              }
              return payment;
            });
  }

  protected void stubOrderSummaryRepositoryFindByPurchaseOrderId(
      Long purchaseOrderId, Optional<OrderSummary> result) {
    lenient()
        .when(orderSummaryRepository.findByPurchaseOrderId(purchaseOrderId))
        .thenReturn(result);
  }

  protected void stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
      String entityType, Long entityId, Optional<OrderSummary> result) {
    lenient()
        .when(orderSummaryRepository.findByEntityTypeAndEntityId(entityType, entityId))
        .thenReturn(result);
  }

  protected void stubOrderSummaryRepositorySaveReturnsArgument() {
    lenient()
        .when(orderSummaryRepository.save(any(OrderSummary.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubPurchaseOrderRepositorySaveReturnsArgument() {
    lenient()
        .when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubEnvironmentActiveProfiles(String... profiles) {
    lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
  }

  protected void stubRazorpayClientOrderCreateReturns(String razorpayOrderId) {
    closeMockedRazorpayClientConstruction();
    mockedRazorpayClient =
        org.mockito.Mockito.mockConstruction(
            RazorpayClient.class,
            (mock, context) -> {
              mockedRazorpayOrderClient = org.mockito.Mockito.mock(OrderClient.class);
              mock.orders = mockedRazorpayOrderClient;
              lenient()
                  .when(mockedRazorpayOrderClient.create(any(JSONObject.class)))
                  .thenAnswer(
                      invocation -> {
                        capturedRazorpayOrderCreateRequest = invocation.getArgument(0);
                        return new com.razorpay.Order(new JSONObject().put("id", razorpayOrderId));
                      });
            });
  }

  protected void stubRazorpayClientOrderCreateThrows(String message) {
    closeMockedRazorpayClientConstruction();
    mockedRazorpayClient =
        org.mockito.Mockito.mockConstruction(
            RazorpayClient.class,
            (mock, context) -> {
              mockedRazorpayOrderClient = org.mockito.Mockito.mock(OrderClient.class);
              mock.orders = mockedRazorpayOrderClient;
              lenient()
                  .when(mockedRazorpayOrderClient.create(any(JSONObject.class)))
                  .thenAnswer(
                      invocation -> {
                        capturedRazorpayOrderCreateRequest = invocation.getArgument(0);
                        throw new RazorpayException(message);
                      });
            });
  }

  protected void stubRazorpayClientPaymentRefundReturns(String refundId) {
    closeMockedRazorpayClientConstruction();
    mockedRazorpayClient =
        org.mockito.Mockito.mockConstruction(
            RazorpayClient.class,
            (mock, context) -> {
              mockedRazorpayPaymentClient = org.mockito.Mockito.mock(PaymentClient.class);
              mock.payments = mockedRazorpayPaymentClient;
              lenient()
                  .when(mockedRazorpayPaymentClient.refund(anyString(), any(JSONObject.class)))
                  .thenAnswer(
                      invocation -> {
                        capturedRazorpayRefundRequest = invocation.getArgument(1);
                        return new com.razorpay.Refund(new JSONObject().put("id", refundId));
                      });
            });
  }

  protected void stubRazorpayClientPaymentRefundThrows(String message) {
    closeMockedRazorpayClientConstruction();
    mockedRazorpayClient =
        org.mockito.Mockito.mockConstruction(
            RazorpayClient.class,
            (mock, context) -> {
              mockedRazorpayPaymentClient = org.mockito.Mockito.mock(PaymentClient.class);
              mock.payments = mockedRazorpayPaymentClient;
              lenient()
                  .when(mockedRazorpayPaymentClient.refund(anyString(), any(JSONObject.class)))
                  .thenAnswer(
                      invocation -> {
                        capturedRazorpayRefundRequest = invocation.getArgument(1);
                        throw new RazorpayException(message);
                      });
            });
  }

  protected void closeMockedRazorpayClientConstruction() {
    if (mockedRazorpayClient != null) {
      mockedRazorpayClient.close();
      mockedRazorpayClient = null;
    }
  }

  protected String createValidRazorpaySignature(
      String razorpayOrderId, String razorpayPaymentId, String secret) {
    try {
      String data = razorpayOrderId + "|" + razorpayPaymentId;
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
      sha256Hmac.init(secretKey);
      byte[] hash = sha256Hmac.doFinal(data.getBytes());
      return java.util.HexFormat.of().formatHex(hash);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to generate test signature", exception);
    }
  }

  protected PaymentVerificationResponseModel createSuccessPaymentVerificationResponse() {
    return PaymentVerificationResponseModel.success("payment-id", TEST_PO_ID, "APPROVED");
  }

  protected void stubPaymentServiceCreateOrder(RazorpayOrderResponseModel response) {
    lenient()
        .when(paymentServiceMock.createOrder(any(RazorpayOrderRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceCreateOrderThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .createOrder(any(RazorpayOrderRequestModel.class));
  }

  protected void stubPaymentServiceCreateOrderFollowUp(RazorpayOrderResponseModel response) {
    lenient()
        .when(paymentServiceMock.createOrderFollowUp(any(RazorpayOrderRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceCreateOrderFollowUpThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .createOrderFollowUp(any(RazorpayOrderRequestModel.class));
  }

  protected void stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel response) {
    lenient()
        .when(paymentServiceMock.verifyPayment(any(RazorpayVerifyRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceVerifyPaymentThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .verifyPayment(any(RazorpayVerifyRequestModel.class));
  }

  protected void stubPaymentServiceVerifyPaymentFollowUp(
      PaymentVerificationResponseModel response) {
    lenient()
        .when(paymentServiceMock.verifyPaymentFollowUp(any(RazorpayVerifyRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceVerifyPaymentFollowUpThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .verifyPaymentFollowUp(any(RazorpayVerifyRequestModel.class));
  }

  protected void stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel response) {
    lenient()
        .when(paymentServiceMock.recordCashPayment(any(CashPaymentRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceRecordCashPaymentThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .recordCashPayment(any(CashPaymentRequestModel.class));
  }

  protected void stubPaymentServiceRecordCashPaymentFollowUp(
      PaymentVerificationResponseModel response) {
    lenient()
        .when(paymentServiceMock.recordCashPaymentFollowUp(any(CashPaymentRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubPaymentServiceRecordCashPaymentFollowUpThrowsUnauthorized() {
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .recordCashPaymentFollowUp(any(CashPaymentRequestModel.class));
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
    lenient()
        .doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
        .when(paymentServiceMock)
        .generatePaymentReceiptPDF(anyLong());
  }
}
