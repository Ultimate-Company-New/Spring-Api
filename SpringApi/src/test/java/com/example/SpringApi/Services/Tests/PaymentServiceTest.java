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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.or;

/**
 * Unit tests for PaymentService.
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
    private Payment testPayment;
    private RazorpayOrderRequestModel testOrderRequest;
    private CashPaymentRequestModel testCashPaymentRequest;
    private RazorpayVerifyRequestModel testVerifyRequest;

    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
    private static final Long TEST_PO_ID = DEFAULT_PURCHASE_ORDER_ID;
    private static final Long TEST_ORDER_SUMMARY_ID = 1L;
    private static final Long TEST_PAYMENT_ID = 1L;

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

    @Nested
    @DisplayName("CreateOrder Tests")
    class CreateOrderTests {

        /**
         * Purpose: Verify that createOrder throws NotFoundException when client credentials not found.
         * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrder - Client not found - Throws NotFoundException")
        void createOrder_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrder handles negative PO ID (client check happens first).
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrder - Negative PO ID - Throws NotFoundException for client")
        void createOrder_NegativePurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(-1L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrder handles zero PO ID (client check happens first).
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrder - Zero PO ID - Throws NotFoundException for client")
        void createOrder_ZeroPurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(0L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrder handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrder - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
        void createOrder_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrder handles Long.MIN_VALUE PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrder - Long.MIN_VALUE PO ID - Throws NotFoundException for client")
        void createOrder_MinLongPurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(Long.MIN_VALUE);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrder(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrder verifies client repository interaction.
         * Expected Result: Client repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("createOrder - Verify client repository interaction")
        void createOrder_VerifyClientRepositoryInteraction() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.createOrder(testOrderRequest));
            verify(clientRepository, times(1)).findById(any());
        }

        /**
         * Purpose: Additional createOrder invalid ID coverage.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId.
         */
        @TestFactory
        @DisplayName("createOrder - Additional invalid PO IDs")
        Stream<DynamicTest> createOrder_AdditionalInvalidPoIds() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            return Stream.of(2L, 3L, 4L, 5L, 6L, 7L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testOrderRequest.setPurchaseOrderId(id);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.createOrder(testOrderRequest));
                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("CreateOrderFollowUp Tests")
    class CreateOrderFollowUpTests {

        /**
         * Purpose: Verify that createOrderFollowUp throws NotFoundException when client not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrderFollowUp - Client not found - Throws NotFoundException")
        void createOrderFollowUp_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrderFollowUp handles negative PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrderFollowUp - Negative PO ID - Throws NotFoundException for client")
        void createOrderFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(-1L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrderFollowUp handles zero PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrderFollowUp - Zero PO ID - Throws NotFoundException for client")
        void createOrderFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(0L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that createOrderFollowUp handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("createOrderFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
        void createOrderFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
            testOrderRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.createOrderFollowUp(testOrderRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify client repository interaction for createOrderFollowUp.
         * Expected Result: Client repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("createOrderFollowUp - Verify client repository interaction")
        void createOrderFollowUp_VerifyClientRepositoryInteraction() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.createOrderFollowUp(testOrderRequest));
            verify(clientRepository, times(1)).findById(any());
        }

        /**
         * Purpose: Additional createOrderFollowUp invalid ID coverage.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId.
         */
        @TestFactory
        @DisplayName("createOrderFollowUp - Additional invalid PO IDs")
        Stream<DynamicTest> createOrderFollowUp_AdditionalInvalidPoIds() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            return Stream.of(2L, 3L, 4L, 5L, 6L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testOrderRequest.setPurchaseOrderId(id);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.createOrderFollowUp(testOrderRequest));
                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("VerifyPayment Tests")
    class VerifyPaymentTests {

        /**
         * Purpose: Verify that verifyPayment throws NotFoundException when client not found.
         * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPayment - Client not found - Throws NotFoundException")
        void verifyPayment_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPayment handles negative PO ID (client check first).
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPayment - Negative PO ID - Throws NotFoundException for client")
        void verifyPayment_NegativePOId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(-1L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPayment handles zero PO ID (client check first).
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPayment - Zero PO ID - Throws NotFoundException for client")
        void verifyPayment_ZeroPOId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(0L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPayment handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPayment - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
        void verifyPayment_MaxLongPOId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPayment(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify client repository interaction for verifyPayment.
         * Expected Result: Client repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("verifyPayment - Verify client repository interaction")
        void verifyPayment_VerifyClientRepositoryInteraction() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.verifyPayment(testVerifyRequest));
            verify(clientRepository, times(1)).findById(any());
        }

        /**
         * Purpose: Additional verifyPayment invalid ID coverage.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId.
         */
        @TestFactory
        @DisplayName("verifyPayment - Additional invalid PO IDs")
        Stream<DynamicTest> verifyPayment_AdditionalInvalidPoIds() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            return Stream.of(2L, 3L, 4L, 5L, 6L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testVerifyRequest.setPurchaseOrderId(id);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.verifyPayment(testVerifyRequest));
                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("RecordCashPayment Tests")
    class RecordCashPaymentTests {

        /**
         * Purpose: Verify that recordCashPayment throws NotFoundException when PO not found.
         * Expected Result: NotFoundException is thrown (PO lookup happens first).
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPayment - PO not found - Throws NotFoundException")
        void recordCashPayment_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPayment handles null request.
         * Expected Result: NullPointerException is thrown (accessing null request).
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("recordCashPayment - Null request - Throws NullPointerException")
        void recordCashPayment_NullRequest_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> paymentService.recordCashPayment(null));
        }

        /**
         * Purpose: Verify that recordCashPayment handles negative PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPayment - Negative PO ID - Throws NotFoundException")
        void recordCashPayment_NegativePurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(-1L);
            when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPayment handles zero PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPayment - Zero PO ID - Throws NotFoundException")
        void recordCashPayment_ZeroPurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(0L);
            when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPayment handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPayment - Long.MAX_VALUE PO ID - Throws NotFoundException")
        void recordCashPayment_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPayment handles Long.MIN_VALUE PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPayment - Long.MIN_VALUE PO ID - Throws NotFoundException")
        void recordCashPayment_MinLongPurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(Long.MIN_VALUE);
            when(purchaseOrderRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPayment(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify PO repository interaction for recordCashPayment.
         * Expected Result: PO repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("recordCashPayment - Verify PO repository interaction")
        void recordCashPayment_VerifyPORepositoryInteraction() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.recordCashPayment(testCashPaymentRequest));
            verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
        }

        /**
         * Purpose: Additional recordCashPayment invalid ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId.
         */
        @TestFactory
        @DisplayName("recordCashPayment - Additional invalid PO IDs")
        Stream<DynamicTest> recordCashPayment_AdditionalInvalidPoIds() {
            return Stream.of(2L, 3L, 4L, 5L, 6L, 7L, -100L, Long.MAX_VALUE - 1)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testCashPaymentRequest.setPurchaseOrderId(id);
                        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.recordCashPayment(testCashPaymentRequest));
                        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("VerifyPaymentFollowUp Tests")
    class VerifyPaymentFollowUpTests {

        /**
         * Purpose: Verify that verifyPaymentFollowUp throws NotFoundException when client not found.
         * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPaymentFollowUp - Client not found - Throws NotFoundException")
        void verifyPaymentFollowUp_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPaymentFollowUp handles negative PO ID (client check first).
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPaymentFollowUp - Negative PO ID - Throws NotFoundException for client")
        void verifyPaymentFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(-1L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPaymentFollowUp handles zero PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPaymentFollowUp - Zero PO ID - Throws NotFoundException for client")
        void verifyPaymentFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(0L);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that verifyPaymentFollowUp handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("verifyPaymentFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException for client")
        void verifyPaymentFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
            testVerifyRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify client repository interaction for verifyPaymentFollowUp.
         * Expected Result: Client repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("verifyPaymentFollowUp - Verify client repository interaction")
        void verifyPaymentFollowUp_VerifyClientRepositoryInteraction() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
            verify(clientRepository, times(1)).findById(any());
        }

        /**
         * Purpose: Additional verifyPaymentFollowUp invalid ID coverage.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId.
         */
        @TestFactory
        @DisplayName("verifyPaymentFollowUp - Additional invalid PO IDs")
        Stream<DynamicTest> verifyPaymentFollowUp_AdditionalInvalidPoIds() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            return Stream.of(2L, 3L, 4L, 5L, 6L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testVerifyRequest.setPurchaseOrderId(id);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.verifyPaymentFollowUp(testVerifyRequest));
                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("RecordCashPaymentFollowUp Tests")
    class RecordCashPaymentFollowUpTests {

        /**
         * Purpose: Verify that recordCashPaymentFollowUp throws NotFoundException when PO not found.
         * Expected Result: NotFoundException is thrown (PO lookup happens first).
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPaymentFollowUp - PO not found - Throws NotFoundException")
        void recordCashPaymentFollowUp_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPaymentFollowUp handles null request.
         * Expected Result: NullPointerException is thrown (accessing null request).
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("recordCashPaymentFollowUp - Null request - Throws NullPointerException")
        void recordCashPaymentFollowUp_NullRequest_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> paymentService.recordCashPaymentFollowUp(null));
        }

        /**
         * Purpose: Verify that recordCashPaymentFollowUp handles negative PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPaymentFollowUp - Negative PO ID - Throws NotFoundException")
        void recordCashPaymentFollowUp_NegativePurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(-1L);
            when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPaymentFollowUp handles zero PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPaymentFollowUp - Zero PO ID - Throws NotFoundException")
        void recordCashPaymentFollowUp_ZeroPurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(0L);
            when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that recordCashPaymentFollowUp handles Long.MAX_VALUE PO ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("recordCashPaymentFollowUp - Long.MAX_VALUE PO ID - Throws NotFoundException")
        void recordCashPaymentFollowUp_MaxLongPurchaseOrderId_ThrowsNotFoundException() {
            testCashPaymentRequest.setPurchaseOrderId(Long.MAX_VALUE);
            when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Additional recordCashPaymentFollowUp invalid ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId.
         */
        @TestFactory
        @DisplayName("recordCashPaymentFollowUp - Additional invalid PO IDs")
        Stream<DynamicTest> recordCashPaymentFollowUp_AdditionalInvalidPoIds() {
            return Stream.of(2L, 3L, 4L, 5L, 6L, -100L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        testCashPaymentRequest.setPurchaseOrderId(id);
                        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.recordCashPaymentFollowUp(testCashPaymentRequest));
                        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("GetPaymentsForPurchaseOrder Tests")
    class GetPaymentsForPurchaseOrderTests {

        /**
         * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException when PO doesn't exist.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - PO not found - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_PONotFound_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - Negative PO ID - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_NegativePOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(-1L));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - Zero PO ID - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_ZeroPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(0L));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentsForPurchaseOrder throws NotFoundException for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId error.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - Long.MAX_VALUE PO ID - Throws NotFoundException")
        void getPaymentsForPurchaseOrder_MaxLongPOId_ThrowsNotFoundException() {
            when(purchaseOrderRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(Long.MAX_VALUE));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentsForPurchaseOrder throws BadRequestException when client access denied.
         * Expected Result: BadRequestException is thrown (after PO found, client check fails).
         * Assertions: Exception message matches AccessDeniedToPurchaseOrder error.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - Client access denied - Throws BadRequestException")
        void getPaymentsForPurchaseOrder_ClientAccessDenied_ThrowsBadRequestException() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.of(testPurchaseOrder));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));
            assertEquals(ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder, ex.getMessage());
        }

        /**
         * Purpose: Verify PO repository interaction for getPaymentsForPurchaseOrder.
         * Expected Result: PO repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("getPaymentsForPurchaseOrder - Verify PO repository interaction")
        void getPaymentsForPurchaseOrder_VerifyPORepositoryInteraction() {
            when(purchaseOrderRepository.findById(TEST_PO_ID)).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class, () -> paymentService.getPaymentsForPurchaseOrder(TEST_PO_ID));
            verify(purchaseOrderRepository, times(1)).findById(TEST_PO_ID);
        }

        /**
         * Purpose: Additional invalid PO ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches PurchaseOrder InvalidId.
         */
        @TestFactory
        @DisplayName("getPaymentsForPurchaseOrder - Additional invalid PO IDs")
        Stream<DynamicTest> getPaymentsForPurchaseOrder_AdditionalInvalidPoIds() {
            return Stream.of(2L, 3L, 4L, 5L, 6L, -100L)
                    .map(id -> DynamicTest.dynamicTest("Invalid PO ID: " + id, () -> {
                        when(purchaseOrderRepository.findById(id)).thenReturn(Optional.empty());
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.getPaymentsForPurchaseOrder(id));
                        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("GetPaymentById Tests")
    class GetPaymentByIdTests {

        /**
         * Purpose: Verify that getPaymentById throws NotFoundException when payment doesn't exist.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Payment NotFound error.
         */
        @Test
        @DisplayName("getPaymentById - Payment not found - Throws NotFoundException")
        void getPaymentById_PaymentNotFound_ThrowsNotFoundException() {
            when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentById throws NotFoundException for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Payment NotFound error.
         */
        @Test
        @DisplayName("getPaymentById - Negative ID - Throws NotFoundException")
        void getPaymentById_NegativeId_ThrowsNotFoundException() {
            when(paymentRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(-1L));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentById throws NotFoundException for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Payment NotFound error.
         */
        @Test
        @DisplayName("getPaymentById - Zero ID - Throws NotFoundException")
        void getPaymentById_ZeroId_ThrowsNotFoundException() {
            when(paymentRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(0L));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentById throws NotFoundException for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Payment NotFound error.
         */
        @Test
        @DisplayName("getPaymentById - Long.MAX_VALUE ID - Throws NotFoundException")
        void getPaymentById_MaxLongId_ThrowsNotFoundException() {
            when(paymentRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(Long.MAX_VALUE));

            assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify that getPaymentById throws BadRequestException when client access denied.
         * Expected Result: BadRequestException is thrown (after payment found, client check fails).
         * Assertions: Exception message matches Payment AccessDenied error.
         */
        @Test
        @DisplayName("getPaymentById - Client access denied - Throws BadRequestException")
        void getPaymentById_ClientAccessDenied_ThrowsBadRequestException() {
            when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPayment));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> paymentService.getPaymentById(TEST_PAYMENT_ID));
            assertEquals(ErrorMessages.PaymentErrorMessages.AccessDenied, ex.getMessage());
        }

        /**
         * Purpose: Verify payment repository is called with correct ID.
         * Expected Result: Repository findById is called.
         * Assertions: verify repository findById is called once.
         */
        @Test
        @DisplayName("getPaymentById - Verify payment repository interaction")
        void getPaymentById_VerifyPaymentRepositoryInteraction() {
            when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> paymentService.getPaymentById(TEST_PAYMENT_ID));

            verify(paymentRepository, times(1)).findById(TEST_PAYMENT_ID);
        }

        /**
         * Purpose: Verify multiple independent calls are handled correctly.
         * Expected Result: Each call is processed independently.
         * Assertions: Both calls throw NotFoundException.
         */
        @Test
        @DisplayName("getPaymentById - Multiple calls with different IDs")
        void getPaymentById_MultipleCalls() {
            when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(1L));
            assertThrows(NotFoundException.class, () -> paymentService.getPaymentById(2L));

            verify(paymentRepository, times(2)).findById(anyLong());
        }

        /**
         * Purpose: Additional invalid payment ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches Payment NotFound.
         */
        @TestFactory
        @DisplayName("getPaymentById - Additional invalid IDs")
        Stream<DynamicTest> getPaymentById_AdditionalInvalidIds() {
            return Stream.of(2L, 3L, 4L, 5L, 6L, -100L, Long.MIN_VALUE)
                    .map(id -> DynamicTest.dynamicTest("Invalid payment ID: " + id, () -> {
                        when(paymentRepository.findById(id)).thenReturn(Optional.empty());
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.getPaymentById(id));
                        assertEquals(ErrorMessages.PaymentErrorMessages.NotFound, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("IsPurchaseOrderPaid Tests")
    class IsPurchaseOrderPaidTests {

        /**
         * Purpose: Verify that isPurchaseOrderPaid returns true when successful payment exists.
         * Expected Result: true is returned.
         * Assertions: Result is true.
         */
        @Test
        @DisplayName("isPurchaseOrderPaid - Has successful payment - Returns true")
        void isPurchaseOrderPaid_HasSuccessfulPayment_ReturnsTrue() {
            when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(true);

            boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            assertTrue(result);
        }

        /**
         * Purpose: Verify that isPurchaseOrderPaid returns false when no successful payment exists.
         * Expected Result: false is returned.
         * Assertions: Result is false.
         */
        @Test
        @DisplayName("isPurchaseOrderPaid - No successful payment - Returns false")
        void isPurchaseOrderPaid_NoSuccessfulPayment_ReturnsFalse() {
            when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(false);

            boolean result = paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            assertFalse(result);
        }

        /**
         * Purpose: Verify that isPurchaseOrderPaid handles negative ID.
         * Expected Result: false is returned for non-existent PO.
         * Assertions: Result is false.
         */
        @Test
        @DisplayName("isPurchaseOrderPaid - Negative ID - Returns false")
        void isPurchaseOrderPaid_NegativeId_ReturnsFalse() {
            when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", -1L)).thenReturn(false);

            boolean result = paymentService.isPurchaseOrderPaid(-1L);

            assertFalse(result);
        }

        /**
         * Purpose: Verify that isPurchaseOrderPaid handles zero ID.
         * Expected Result: false is returned for non-existent PO.
         * Assertions: Result is false.
         */
        @Test
        @DisplayName("isPurchaseOrderPaid - Zero ID - Returns false")
        void isPurchaseOrderPaid_ZeroId_ReturnsFalse() {
            when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", 0L)).thenReturn(false);

            boolean result = paymentService.isPurchaseOrderPaid(0L);

            assertFalse(result);
        }

        /**
         * Purpose: Verify payment repository is called with correct parameters.
         * Expected Result: Repository hasSuccessfulPayment is called.
         * Assertions: verify repository method is called with correct params.
         */
        @Test
        @DisplayName("isPurchaseOrderPaid - Verify payment repository interaction")
        void isPurchaseOrderPaid_VerifyPaymentRepositoryInteraction() {
            when(paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID)).thenReturn(true);

            paymentService.isPurchaseOrderPaid(TEST_PO_ID);

            verify(paymentRepository, times(1)).hasSuccessfulPayment("PURCHASE_ORDER", TEST_PO_ID);
        }

        /**
         * Purpose: Additional purchase order ID coverage for isPurchaseOrderPaid.
         * Expected Result: false when repository returns false.
         * Assertions: Result is false.
         */
        @TestFactory
        @DisplayName("isPurchaseOrderPaid - Additional IDs")
        Stream<DynamicTest> isPurchaseOrderPaid_AdditionalIds() {
            when(paymentRepository.hasSuccessfulPayment(eq("PURCHASE_ORDER"), anyLong())).thenReturn(false);
            return Stream.of(2L, 3L, 4L, 5L, Long.MAX_VALUE)
                    .map(id -> DynamicTest.dynamicTest("PO ID: " + id, () -> {
                        boolean result = paymentService.isPurchaseOrderPaid(id);
                        assertFalse(result);
                    }));
        }
    }

    @Nested
    @DisplayName("InitiateRefund Tests")
    class InitiateRefundTests {

        /**
         * Purpose: Verify that initiateRefund throws NotFoundException when client not found.
         * Expected Result: NotFoundException is thrown (getClientWithRazorpayCredentials fails first).
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("initiateRefund - Client not found - Throws NotFoundException")
        void initiateRefund_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "Test refund"));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that initiateRefund throws NotFoundException for negative payment ID.
         * Expected Result: NotFoundException is thrown for client (checked first).
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("initiateRefund - Negative payment ID - Throws NotFoundException for client")
        void initiateRefund_NegativePaymentId_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(-1L, 10000L, "Test refund"));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that initiateRefund throws NotFoundException for zero payment ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("initiateRefund - Zero payment ID - Throws NotFoundException for client")
        void initiateRefund_ZeroPaymentId_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(0L, 10000L, "Test refund"));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that initiateRefund handles Long.MAX_VALUE payment ID.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId error.
         */
        @Test
        @DisplayName("initiateRefund - Long.MAX_VALUE payment ID - Throws NotFoundException for client")
        void initiateRefund_MaxLongPaymentId_ThrowsNotFoundException() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(Long.MAX_VALUE, 10000L, "Test refund"));
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify client repository interaction for initiateRefund.
         * Expected Result: Client repository findById is called.
         * Assertions: Verify repository interaction.
         */
        @Test
        @DisplayName("initiateRefund - Verify client repository interaction")
        void initiateRefund_VerifyClientRepositoryInteraction() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            
            assertThrows(NotFoundException.class,
                    () -> paymentService.initiateRefund(TEST_PAYMENT_ID, 10000L, "Test refund"));
            verify(clientRepository, times(1)).findById(any());
        }

        /**
         * Purpose: Additional initiateRefund invalid ID coverage.
         * Expected Result: NotFoundException is thrown for client.
         * Assertions: Exception message matches Client InvalidId.
         */
        @TestFactory
        @DisplayName("initiateRefund - Additional invalid payment IDs")
        Stream<DynamicTest> initiateRefund_AdditionalInvalidPaymentIds() {
            when(clientRepository.findById(isNull())).thenReturn(Optional.empty());
            return Stream.of(2L, 3L, 4L, 5L, 6L)
                    .map(id -> DynamicTest.dynamicTest("Payment ID: " + id, () -> {
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> paymentService.initiateRefund(id, 10000L, "Test refund"));
                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }
}
