package com.example.SpringApi.Services;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.RequestModels.CashPaymentRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.RazorpayVerifyRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaymentVerificationResponseModel;
import com.example.SpringApi.Models.ResponseModels.RazorpayOrderResponseModel;
import com.example.SpringApi.Helpers.HTMLHelper;
import com.example.SpringApi.Helpers.PDFHelper;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.OrderSummaryRepository;
import com.example.SpringApi.Repositories.PaymentRepository;
import com.example.SpringApi.Repositories.PurchaseOrderRepository;
import com.itextpdf.text.DocumentException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling Razorpay payment operations.
 * Manages order creation, payment verification, refunds, and purchase order status updates.
 * 
 * Each client has their own Razorpay API credentials stored in the Client table.
 * All payment transactions are tracked in the Payment table.
 */
@Service
public class PaymentService extends BaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderSummaryRepository orderSummaryRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    private final Environment environment;

    @Autowired
    public PaymentService(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderSummaryRepository orderSummaryRepository,
            PaymentRepository paymentRepository,
            ClientRepository clientRepository,
            UserLogService userLogService,
            Environment environment) {
        super();
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderSummaryRepository = orderSummaryRepository;
        this.paymentRepository = paymentRepository;
        this.clientRepository = clientRepository;
        this.userLogService = userLogService;
        this.environment = environment;
    }

    // ========================================================================
    // CLIENT & RAZORPAY CLIENT HELPERS
    // ========================================================================

    /**
     * Gets the Razorpay credentials for the current client.
     */
    private Client getClientWithRazorpayCredentials() {
        Long clientId = getClientId();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        
        if (client.getRazorpayApiKey() == null || client.getRazorpayApiKey().trim().isEmpty()) {
            throw new BadRequestException("Razorpay API Key not configured for this client. Please configure in Client Settings.");
        }
        if (client.getRazorpayApiSecret() == null || client.getRazorpayApiSecret().trim().isEmpty()) {
            throw new BadRequestException("Razorpay API Secret not configured for this client. Please configure in Client Settings.");
        }
        
        return client;
    }

    /**
     * Creates a RazorpayClient instance for the current client.
     */
    private RazorpayClient createRazorpayClient(Client client) throws RazorpayException {
        return new RazorpayClient(client.getRazorpayApiKey(), client.getRazorpayApiSecret());
    }

    /**
     * Checks if we're running in test/development mode
     */
    private boolean isTestMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("localhost".equalsIgnoreCase(profile) || 
                "development".equalsIgnoreCase(profile) ||
                "dev".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    // ========================================================================
    // ORDER CREATION
    // ========================================================================

    /**
     * Creates a Razorpay order for a purchase order.
     * Also creates a Payment record to track the transaction.
     * 
     * @param request The order request containing purchaseOrderId and amount
     * @return RazorpayOrderResponseModel containing order details for frontend
     */
    @Transactional
    public RazorpayOrderResponseModel createOrder(RazorpayOrderRequestModel request) {
        // Get client with Razorpay credentials
        Client client = getClientWithRazorpayCredentials();
        
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Validate status - only PENDING_APPROVAL orders can be paid
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException("Only orders with PENDING_APPROVAL status can be paid");
        }

        // Get amount from order summary if not provided
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            OrderSummary orderSummary = orderSummaryRepository
                    .findByPurchaseOrderId(purchaseOrder.getPurchaseOrderId())
                    .orElseThrow(() -> new BadRequestException("Order summary not found"));
            amount = orderSummary.getGrandTotal();
        }

        // Convert to paise (Razorpay uses smallest currency unit)
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String receipt = "PO_" + purchaseOrder.getPurchaseOrderId() + "_" + System.currentTimeMillis();

        try {
            // Create Razorpay client for this client
            RazorpayClient razorpayClient = createRazorpayClient(client);
            
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1); // Auto-capture payment

            // Add notes for reference
            JSONObject notes = new JSONObject();
            notes.put("purchase_order_id", purchaseOrder.getPurchaseOrderId().toString());
            notes.put("vendor_number", purchaseOrder.getVendorNumber());
            notes.put("client_id", client.getClientId().toString());
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // Create Payment record to track this transaction
            Payment payment = new Payment(
                    Payment.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId(),
                    razorpayOrderId,
                    receipt,
                    amountInPaise,
                    "INR",
                    Payment.PaymentGateway.RAZORPAY.getValue(),
                    getClientId(),
                    getUser()
            );
            
            // Set customer details if provided
            if (request.getCustomerName() != null) {
                payment.setCustomerName(request.getCustomerName());
            }
            if (request.getCustomerEmail() != null) {
                payment.setCustomerEmail(request.getCustomerEmail());
            }
            if (request.getCustomerPhone() != null) {
                payment.setCustomerPhone(request.getCustomerPhone());
            }
            
            // Set description
            payment.setDescription("Payment for Purchase Order #" + purchaseOrder.getPurchaseOrderId() + 
                    " (" + purchaseOrder.getVendorNumber() + ")");
            
            // Mark as test payment if in development mode
            payment.setIsTestPayment(isTestMode());
            
            // Save payment record
            paymentRepository.save(payment);

            // Build response
            RazorpayOrderResponseModel response = new RazorpayOrderResponseModel();
            response.setOrderId(razorpayOrderId);
            response.setAmount(amount);
            response.setAmountInPaise(amountInPaise);
            response.setCurrency("INR");
            response.setRazorpayKeyId(client.getRazorpayApiKey());
            response.setVendorNumber(purchaseOrder.getVendorNumber());
            response.setPurchaseOrderId(purchaseOrder.getPurchaseOrderId());
            response.setCompanyName(client.getName() != null ? client.getName() : "Ultimate Company");
            response.setDescription(payment.getDescription());

            // Prefill customer details if provided
            if (request.getCustomerName() != null) {
                response.setPrefillName(request.getCustomerName());
            }
            if (request.getCustomerEmail() != null) {
                response.setPrefillEmail(request.getCustomerEmail());
            }
            if (request.getCustomerPhone() != null) {
                response.setPrefillPhone(request.getCustomerPhone());
            }

            return response;

        } catch (RazorpayException e) {
            throw new BadRequestException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Creates a Razorpay order for a follow-up payment (order already approved/partially paid).
     * This method allows orders with APPROVED or APPROVED_WITH_PARTIAL_PAYMENT status.
     * 
     * @param request The order request containing purchaseOrderId and amount
     * @return RazorpayOrderResponseModel containing order details for frontend
     */
    @Transactional
    public RazorpayOrderResponseModel createOrderFollowUp(RazorpayOrderRequestModel request) {
        // Get client with Razorpay credentials
        Client client = getClientWithRazorpayCredentials();
        
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Validate status - allow APPROVED or APPROVED_WITH_PARTIAL_PAYMENT for follow-up payments
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.APPROVED.getValue().equals(status) && 
            !PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue().equals(status)) {
            throw new BadRequestException("Follow-up payments can only be made for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders");
        }

        // Get amount from order summary if not provided
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            OrderSummary orderSummary = orderSummaryRepository
                    .findByPurchaseOrderId(purchaseOrder.getPurchaseOrderId())
                    .orElseThrow(() -> new BadRequestException("Order summary not found"));
            // For follow-up payments, use pending amount instead of grand total
            Long totalPaidPaise = paymentRepository.getTotalNetPaidPaiseForEntity(
                    Payment.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId());
            BigDecimal totalPaid = BigDecimal.valueOf(totalPaidPaise).divide(BigDecimal.valueOf(100));
            BigDecimal pendingAmount = orderSummary.getGrandTotal().subtract(totalPaid);
            amount = pendingAmount;
        }

        // Convert to paise (Razorpay uses smallest currency unit)
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String receipt = "PO_" + purchaseOrder.getPurchaseOrderId() + "_" + System.currentTimeMillis();

        try {
            // Create Razorpay client for this client
            RazorpayClient razorpayClient = createRazorpayClient(client);
            
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1); // Auto-capture payment

            // Add notes for reference
            JSONObject notes = new JSONObject();
            notes.put("purchase_order_id", purchaseOrder.getPurchaseOrderId().toString());
            notes.put("vendor_number", purchaseOrder.getVendorNumber());
            notes.put("client_id", client.getClientId().toString());
            notes.put("is_follow_up_payment", "true");
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // Create Payment record to track this transaction
            Payment payment = new Payment(
                    Payment.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId(),
                    razorpayOrderId,
                    receipt,
                    amountInPaise,
                    "INR",
                    Payment.PaymentGateway.RAZORPAY.getValue(),
                    getClientId(),
                    getUser()
            );
            
            // Set customer details if provided
            if (request.getCustomerName() != null) {
                payment.setCustomerName(request.getCustomerName());
            }
            if (request.getCustomerEmail() != null) {
                payment.setCustomerEmail(request.getCustomerEmail());
            }
            if (request.getCustomerPhone() != null) {
                payment.setCustomerPhone(request.getCustomerPhone());
            }
            
            // Set description
            payment.setDescription("Follow-up payment for Purchase Order #" + purchaseOrder.getPurchaseOrderId() + 
                    " (" + purchaseOrder.getVendorNumber() + ")");
            
            // Mark as test payment if in development mode
            payment.setIsTestPayment(isTestMode());
            
            // Save payment record
            paymentRepository.save(payment);

            // Build response
            RazorpayOrderResponseModel response = new RazorpayOrderResponseModel();
            response.setOrderId(razorpayOrderId);
            response.setAmount(amount);
            response.setAmountInPaise(amountInPaise);
            response.setCurrency("INR");
            response.setRazorpayKeyId(client.getRazorpayApiKey());
            response.setVendorNumber(purchaseOrder.getVendorNumber());
            response.setPurchaseOrderId(purchaseOrder.getPurchaseOrderId());
            response.setCompanyName(client.getName() != null ? client.getName() : "Ultimate Company");
            response.setDescription(payment.getDescription());

            // Prefill customer details if provided
            if (request.getCustomerName() != null) {
                response.setPrefillName(request.getCustomerName());
            }
            if (request.getCustomerEmail() != null) {
                response.setPrefillEmail(request.getCustomerEmail());
            }
            if (request.getCustomerPhone() != null) {
                response.setPrefillPhone(request.getCustomerPhone());
            }

            return response;
        } catch (RazorpayException e) {
            throw new BadRequestException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    // ========================================================================
    // PAYMENT VERIFICATION
    // ========================================================================

    /**
     * Verifies a Razorpay payment and updates the purchase order status.
     * Updates the Payment record with transaction details.
     * 
     * @param request The verification request with payment details
     * @return PaymentVerificationResponseModel indicating success/failure
     */
    @Transactional
    public PaymentVerificationResponseModel verifyPayment(RazorpayVerifyRequestModel request) {
        // Get client with Razorpay credentials (for signature verification)
        Client client = getClientWithRazorpayCredentials();
        
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Find the payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new BadRequestException("Payment order not found. Please try again."));

        // Verify signature using client's secret
        boolean isValidSignature = verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                client.getRazorpayApiSecret()
        );

        if (!isValidSignature) {
            // Mark payment as failed
            payment.markAsFailed(
                    "SIGNATURE_VERIFICATION_FAILED",
                    "Payment signature verification failed",
                    "gateway",
                    "Invalid signature",
                    getUser()
            );
            paymentRepository.save(payment);
            
            return PaymentVerificationResponseModel.failure("Payment verification failed: Invalid signature");
        }

        // Mark payment as captured
        payment.markAsCaptured(
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                null, // Payment method will be updated from webhook or API call
                payment.getOrderAmountPaise(),
                getUser()
        );
        paymentRepository.save(payment);

        // Calculate total paid and update order summary
        updatePurchaseOrderStatusBasedOnPayment(purchaseOrder);

        // Log the payment
        userLogService.logData(
                getUserId(),
                "Payment verified for PO #" + purchaseOrder.getPurchaseOrderId() + 
                " (" + purchaseOrder.getVendorNumber() + "). Payment ID: " + request.getRazorpayPaymentId(),
                "Payment/verifyPayment"
        );

        // Get updated status
        purchaseOrder = purchaseOrderRepository.findById(purchaseOrder.getPurchaseOrderId()).orElse(purchaseOrder);

        return PaymentVerificationResponseModel.success(
                request.getRazorpayPaymentId(),
                purchaseOrder.getPurchaseOrderId(),
                purchaseOrder.getPurchaseOrderStatus()
        );
    }

    /**
     * Records a cash/manual payment for a purchase order.
     * Creates a Payment record with CASH payment method and MANUAL gateway.
     * Updates the purchase order status to APPROVED.
     * 
     * @param request The cash payment request containing payment details
     * @return PaymentVerificationResponseModel indicating success/failure
     */
    @Transactional
    public PaymentVerificationResponseModel recordCashPayment(CashPaymentRequestModel request) {
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Validate status - only PENDING_APPROVAL orders can be paid
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.PENDING_APPROVAL.getValue().equals(status)) {
            throw new BadRequestException("Only orders with PENDING_APPROVAL status can be paid");
        }

        // Validate required fields
        if (request.getPaymentDate() == null) {
            throw new BadRequestException("Payment date is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valid payment amount is required");
        }
        
        // Get order summary to validate payment amount
        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                    OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId())
                .orElse(null);
        
        if (orderSummary != null) {
            // Calculate total paid so far
            Long totalPaidPaise = paymentRepository.getTotalNetPaidPaiseForEntity(
                    Payment.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId());
            BigDecimal totalPaid = BigDecimal.valueOf(totalPaidPaise).divide(BigDecimal.valueOf(100));
            BigDecimal pendingAmount = orderSummary.getGrandTotal().subtract(totalPaid);
            
            // Validate new payment doesn't exceed pending amount
            if (request.getAmount().compareTo(pendingAmount) > 0) {
                throw new BadRequestException("Payment amount (₹" + request.getAmount() + 
                        ") exceeds pending amount (₹" + pendingAmount + ")");
            }
        }

        // Convert amount to paise
        long amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        
        // Determine payment method (UPI if transaction ID provided, otherwise CASH)
        String paymentMethod = (request.getUpiTransactionId() != null && 
                               !request.getUpiTransactionId().trim().isEmpty()) 
                               ? Payment.PaymentMethod.UPI.getValue() 
                               : Payment.PaymentMethod.CASH.getValue();
        
        // Convert LocalDate to LocalDateTime (at start of day)
        LocalDateTime paymentDateTime = request.getPaymentDate().atStartOfDay();
        
        // Create Payment record for cash payment using constructor
        Payment payment = new Payment(
                Payment.EntityType.PURCHASE_ORDER.getValue(),
                purchaseOrder.getPurchaseOrderId(),
                amountInPaise,
                request.getAmount(),
                "INR",
                paymentMethod,
                paymentDateTime,
                request.getNotes(),
                request.getUpiTransactionId(),
                "Cash payment for Purchase Order #" + purchaseOrder.getPurchaseOrderId() + 
                        " (" + purchaseOrder.getVendorNumber() + ")",
                isTestMode(),
                getClientId(),
                getUser()
        );
        
        paymentRepository.save(payment);

        // Calculate total paid and update order summary
        updatePurchaseOrderStatusBasedOnPayment(purchaseOrder);

        // Log the payment
        userLogService.logData(
                getUserId(),
                "Cash payment recorded for PO #" + purchaseOrder.getPurchaseOrderId() + 
                " (" + purchaseOrder.getVendorNumber() + "). Amount: ₹" + request.getAmount() + 
                ". Payment ID: " + payment.getPaymentId(),
                "Payment/recordCashPayment"
        );

        // Get updated status
        purchaseOrder = purchaseOrderRepository.findById(purchaseOrder.getPurchaseOrderId()).orElse(purchaseOrder);
        
        return PaymentVerificationResponseModel.success(
                payment.getPaymentId().toString(),
                purchaseOrder.getPurchaseOrderId(),
                purchaseOrder.getPurchaseOrderStatus()
        );
    }

    /**
     * Verifies a Razorpay payment for a follow-up payment (order already approved/partially paid).
     * This method allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT status.
     * Does NOT trigger shipment processing (shipments already created).
     * 
     * @param request The verification request with payment details
     * @return PaymentVerificationResponseModel indicating success/failure
     */
    @Transactional
    public PaymentVerificationResponseModel verifyPaymentFollowUp(RazorpayVerifyRequestModel request) {
        // Get client with Razorpay credentials (for signature verification)
        Client client = getClientWithRazorpayCredentials();
        
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Validate status - allow APPROVED or APPROVED_WITH_PARTIAL_PAYMENT for follow-up payments
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.APPROVED.getValue().equals(status) && 
            !PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue().equals(status)) {
            throw new BadRequestException("Follow-up payments can only be made for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders");
        }

        // Find the payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new BadRequestException("Payment order not found. Please try again."));

        // Verify signature using client's secret
        boolean isValidSignature = verifyRazorpaySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                client.getRazorpayApiSecret()
        );

        if (!isValidSignature) {
            // Mark payment as failed
            payment.markAsFailed(
                    "SIGNATURE_VERIFICATION_FAILED",
                    "Payment signature verification failed",
                    "gateway",
                    "Invalid signature",
                    getUser()
            );
            paymentRepository.save(payment);
            
            return PaymentVerificationResponseModel.failure("Payment verification failed: Invalid signature");
        }

        // Mark payment as captured
        payment.markAsCaptured(
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                null, // Payment method will be updated from webhook or API call
                payment.getOrderAmountPaise(),
                getUser()
        );
        paymentRepository.save(payment);

        // Calculate total paid and update order summary (no shipment processing)
        updatePurchaseOrderStatusBasedOnPayment(purchaseOrder);

        // Log the payment
        userLogService.logData(
                getUserId(),
                "Follow-up payment verified for PO #" + purchaseOrder.getPurchaseOrderId() + 
                " (" + purchaseOrder.getVendorNumber() + "). Payment ID: " + request.getRazorpayPaymentId(),
                "Payment/verifyPaymentFollowUp"
        );

        // Get updated status
        purchaseOrder = purchaseOrderRepository.findById(purchaseOrder.getPurchaseOrderId()).orElse(purchaseOrder);

        return PaymentVerificationResponseModel.success(
                request.getRazorpayPaymentId(),
                purchaseOrder.getPurchaseOrderId(),
                purchaseOrder.getPurchaseOrderStatus()
        );
    }

    /**
     * Records a cash/manual payment for a follow-up payment (order already approved/partially paid).
     * This method allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT status.
     * Does NOT trigger shipment processing (shipments already created).
     * 
     * @param request The cash payment request containing payment details
     * @return PaymentVerificationResponseModel indicating success/failure
     */
    @Transactional
    public PaymentVerificationResponseModel recordCashPaymentFollowUp(CashPaymentRequestModel request) {
        // Validate purchase order exists
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(request.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Validate client access
        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        // Validate status - allow APPROVED or APPROVED_WITH_PARTIAL_PAYMENT for follow-up payments
        String status = purchaseOrder.getPurchaseOrderStatus();
        if (!PurchaseOrder.Status.APPROVED.getValue().equals(status) && 
            !PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue().equals(status)) {
            throw new BadRequestException("Follow-up payments can only be made for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders");
        }

        // Validate required fields
        if (request.getPaymentDate() == null) {
            throw new BadRequestException("Payment date is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valid payment amount is required");
        }
        
        // Get order summary to validate payment amount
        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                    OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId())
                .orElse(null);
        
        if (orderSummary != null) {
            // Calculate total paid so far
            Long totalPaidPaise = paymentRepository.getTotalNetPaidPaiseForEntity(
                    Payment.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId());
            BigDecimal totalPaid = BigDecimal.valueOf(totalPaidPaise).divide(BigDecimal.valueOf(100));
            BigDecimal pendingAmount = orderSummary.getGrandTotal().subtract(totalPaid);
            
            // Validate new payment doesn't exceed pending amount
            if (request.getAmount().compareTo(pendingAmount) > 0) {
                throw new BadRequestException("Payment amount (₹" + request.getAmount() + 
                        ") exceeds pending amount (₹" + pendingAmount + ")");
            }
        }

        // Convert amount to paise
        long amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        
        // Determine payment method (UPI if transaction ID provided, otherwise CASH)
        String paymentMethod = (request.getUpiTransactionId() != null && 
                               !request.getUpiTransactionId().trim().isEmpty()) 
                               ? Payment.PaymentMethod.UPI.getValue() 
                               : Payment.PaymentMethod.CASH.getValue();
        
        // Convert LocalDate to LocalDateTime (at start of day)
        LocalDateTime paymentDateTime = request.getPaymentDate().atStartOfDay();
        
        // Create Payment record for cash payment using constructor
        Payment payment = new Payment(
                Payment.EntityType.PURCHASE_ORDER.getValue(),
                purchaseOrder.getPurchaseOrderId(),
                amountInPaise,
                request.getAmount(),
                "INR",
                paymentMethod,
                paymentDateTime,
                request.getNotes(),
                request.getUpiTransactionId(),
                "Follow-up cash payment for Purchase Order #" + purchaseOrder.getPurchaseOrderId() + 
                        " (" + purchaseOrder.getVendorNumber() + ")",
                isTestMode(),
                getClientId(),
                getUser()
        );
        
        paymentRepository.save(payment);

        // Calculate total paid and update order summary (no shipment processing)
        updatePurchaseOrderStatusBasedOnPayment(purchaseOrder);

        // Log the payment
        userLogService.logData(
                getUserId(),
                "Follow-up cash payment recorded for PO #" + purchaseOrder.getPurchaseOrderId() + 
                " (" + purchaseOrder.getVendorNumber() + "). Amount: ₹" + request.getAmount() + 
                ". Payment ID: " + payment.getPaymentId(),
                "Payment/recordCashPaymentFollowUp"
        );

        // Get updated status
        purchaseOrder = purchaseOrderRepository.findById(purchaseOrder.getPurchaseOrderId()).orElse(purchaseOrder);
        
        return PaymentVerificationResponseModel.success(
                payment.getPaymentId().toString(),
                purchaseOrder.getPurchaseOrderId(),
                purchaseOrder.getPurchaseOrderStatus()
        );
    }

    /**
     * Calculates total paid amount for a purchase order and updates status accordingly.
     * Sets APPROVED if fully paid, APPROVED_WITH_PARTIAL_PAYMENT if partially paid.
     */
    private void updatePurchaseOrderStatusBasedOnPayment(PurchaseOrder purchaseOrder) {
        // Calculate total paid so far
        Long totalPaidPaise = paymentRepository.getTotalNetPaidPaiseForEntity(
                Payment.EntityType.PURCHASE_ORDER.getValue(),
                purchaseOrder.getPurchaseOrderId());
        
        BigDecimal totalPaid = BigDecimal.valueOf(totalPaidPaise).divide(BigDecimal.valueOf(100));
        
        // Get order summary
        OrderSummary orderSummary = orderSummaryRepository
                .findByEntityTypeAndEntityId(
                    OrderSummary.EntityType.PURCHASE_ORDER.getValue(),
                    purchaseOrder.getPurchaseOrderId())
                .orElse(null);
        
        if (orderSummary == null) {
            return; // No order summary, can't update
        }
        
        // Calculate pending amount
        BigDecimal pendingAmount = orderSummary.getGrandTotal().subtract(totalPaid);
        
        // Validate payment doesn't exceed grand total
        if (pendingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Payment amount exceeds grand total. Total paid: ₹" + 
                    totalPaid + ", Grand total: ₹" + orderSummary.getGrandTotal());
        }
        
        // Update pending amount
        orderSummary.setPendingAmount(pendingAmount);
        orderSummary.setModifiedUser(getUser());
        orderSummaryRepository.save(orderSummary);
        
        // Update PO status
        if (pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            purchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED.getValue());
        } else {
            purchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.APPROVED_WITH_PARTIAL_PAYMENT.getValue());
        }
        
        purchaseOrder.setApprovedDate(LocalDateTime.now());
        purchaseOrder.setApprovedByUserId(getUserId());
        purchaseOrder.setModifiedUser(getUser());
        purchaseOrderRepository.save(purchaseOrder);
    }

    // ========================================================================
    // PAYMENT QUERIES
    // ========================================================================

    /**
     * Gets all payments for a purchase order.
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForPurchaseOrder(Long purchaseOrderId) {
        // Validate access
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(purchaseOrderId)
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        if (!purchaseOrder.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this purchase order");
        }

        return paymentRepository.findAllByPurchaseOrderId(purchaseOrderId);
    }

    /**
     * Gets payment by ID.
     */
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this payment");
        }

        return payment;
    }

    /**
     * Checks if a purchase order has been paid.
     */
    @Transactional(readOnly = true)
    public boolean isPurchaseOrderPaid(Long purchaseOrderId) {
        return paymentRepository.hasSuccessfulPayment("PURCHASE_ORDER", purchaseOrderId);
    }

    // ========================================================================
    // REFUND OPERATIONS
    // ========================================================================

    /**
     * Initiates a refund for a payment.
     * 
     * @param paymentId The payment ID to refund
     * @param amountInPaise Amount to refund in paise (null for full refund)
     * @param reason Reason for refund
     * @return Updated Payment entity
     */
    @Transactional
    public Payment initiateRefund(Long paymentId, Long amountInPaise, String reason) {
        Client client = getClientWithRazorpayCredentials();
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this payment");
        }

        if (!payment.canBeRefunded()) {
            throw new BadRequestException("This payment cannot be refunded");
        }

        // If no amount specified, refund the remaining amount
        if (amountInPaise == null || amountInPaise <= 0) {
            amountInPaise = payment.getRefundableAmountPaise();
        }

        // Validate refund amount
        if (amountInPaise > payment.getRefundableAmountPaise()) {
            throw new BadRequestException("Refund amount exceeds refundable amount. Maximum: " + 
                    payment.getRefundableAmountPaise() + " paise");
        }

        try {
            RazorpayClient razorpayClient = createRazorpayClient(client);
            
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaise);
            refundRequest.put("speed", "normal"); // or "optimum" for instant refunds
            
            JSONObject notes = new JSONObject();
            notes.put("reason", reason != null ? reason : "Customer requested refund");
            notes.put("payment_id", payment.getPaymentId().toString());
            refundRequest.put("notes", notes);

            com.razorpay.Refund refund = razorpayClient.payments.refund(
                    payment.getRazorpayPaymentId(), 
                    refundRequest
            );

            // Update payment record
            payment.recordRefund(refund.get("id"), amountInPaise, getUser());
            paymentRepository.save(payment);

            // Update order summary if this is a purchase order
            if ("PURCHASE_ORDER".equals(payment.getEntityType())) {
                OrderSummary orderSummary = orderSummaryRepository
                        .findByPurchaseOrderId(payment.getEntityId())
                        .orElse(null);

                if (orderSummary != null) {
                    // Add back the refunded amount to pending
                    BigDecimal refundedAmount = BigDecimal.valueOf(amountInPaise).divide(BigDecimal.valueOf(100));
                    orderSummary.setPendingAmount(orderSummary.getPendingAmount().add(refundedAmount));
                    orderSummary.setModifiedUser(getUser());
                    orderSummaryRepository.save(orderSummary);
                }
            }

            // Log the refund
            userLogService.logData(
                    getUserId(),
                    "Refund of " + amountInPaise + " paise processed for Payment #" + payment.getPaymentId() +
                    ". Refund ID: " + refund.get("id"),
                    "Payment/refund"
            );

            return payment;

        } catch (RazorpayException e) {
            throw new BadRequestException("Failed to process refund: " + e.getMessage());
        }
    }

    // ========================================================================
    // SIGNATURE VERIFICATION
    // ========================================================================

    /**
     * Verifies the Razorpay signature using HMAC-SHA256.
     */
    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature, String apiSecret) {
        try {
            String data = orderId + "|" + paymentId;
            
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            
            byte[] hash = sha256_HMAC.doFinal(data.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            String generatedSignature = sb.toString();
            
            return generatedSignature.equals(signature);
            
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // PUBLIC HELPERS
    // ========================================================================

    /**
     * Gets the Razorpay Key ID for the current client.
     * Safe to expose publicly as this is the public key.
     */
    public String getRazorpayKeyId() {
        Client client = getClientWithRazorpayCredentials();
        return client.getRazorpayApiKey();
    }

    // ========================================================================
    // PDF GENERATION
    // ========================================================================

    /**
     * Generates a PDF receipt for a payment.
     * 
     * @param paymentId The ID of the payment to generate receipt for
     * @return The PDF as a byte array
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the payment is not found
     * @throws TemplateException if PDF template processing fails
     * @throws IOException if PDF generation fails
     * @throws DocumentException if PDF document creation fails
     */
    @Transactional
    public byte[] generatePaymentReceiptPDF(Long paymentId) throws TemplateException, IOException, DocumentException {
        // Fetch payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        // Validate client access
        if (!payment.getClientId().equals(getClientId())) {
            throw new BadRequestException("Access denied to this payment");
        }

        // Fetch purchase order (basic info only)
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findById(payment.getEntityId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found"));

        // Fetch client information
        Client client = clientRepository.findById(getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Generate HTML from template
        String htmlContent = formPaymentReceiptHtml(client, payment, purchaseOrder);

        // Replace br tags for PDF compatibility
        htmlContent = HTMLHelper.replaceBrTags(htmlContent);

        // Convert HTML to PDF
        byte[] pdfBytes = PDFHelper.convertHtmlToPdf(htmlContent);

        // Log the PDF generation
        userLogService.logData(
                getUserId(),
                "Payment receipt PDF generated for Payment ID: " + paymentId,
                "Payment/generatePaymentReceiptPDF"
        );

        return pdfBytes;
    }

    /**
     * Forms the HTML content for the payment receipt PDF using FreeMarker template.
     * 
     * @param client The client (company) information
     * @param payment The payment entity
     * @param purchaseOrder The purchase order entity (basic info only)
     * @return The HTML content as a string
     * @throws IOException if template loading fails
     * @throws TemplateException if template processing fails
     */
    private String formPaymentReceiptHtml(
            Client client,
            Payment payment,
            PurchaseOrder purchaseOrder) throws IOException, TemplateException {
        
        // Configure FreeMarker
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassLoaderForTemplateLoading(
            Thread.currentThread().getContextClassLoader(),
            "InvoiceTemplates"
        );
        
        // Load template
        Template template = cfg.getTemplate("PaymentReceipt.ftl");
        
        // Prepare template data
        Map<String, Object> templateData = new HashMap<>();
        
        // Company information
        templateData.put("companyName", client.getName());
        templateData.put("website", client.getWebsite() != null ? client.getWebsite() : "");
        templateData.put("supportEmail", client.getSupportEmail() != null ? client.getSupportEmail() : "");
        templateData.put("companyLogo", client.getLogoUrl() != null ? client.getLogoUrl() : "");
        templateData.put("currentYear", java.time.Year.now().getValue());
        
        // Payment information
        templateData.put("payment", payment);
        
        // Format payment date
        LocalDateTime paymentDateTime = payment.getCapturedAt() != null 
            ? payment.getCapturedAt() 
            : (payment.getPaymentDate() != null ? payment.getPaymentDate() : payment.getOrderCreatedAt());
        if (paymentDateTime != null) {
            templateData.put("paymentDate", paymentDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        } else {
            templateData.put("paymentDate", "N/A");
        }
        
        // Payment status (lowercase for CSS class)
        templateData.put("paymentStatusLower", payment.getPaymentStatus() != null 
            ? payment.getPaymentStatus().toLowerCase().replace("_", "-") 
            : "unknown");
        
        // Payment method and gateway
        templateData.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A");
        templateData.put("paymentGateway", payment.getPaymentGateway() != null ? payment.getPaymentGateway() : "N/A");
        
        // Format amount
        BigDecimal amountPaid = payment.getAmountPaid() != null 
            ? payment.getAmountPaid() 
            : (payment.getAmountPaidPaise() != null 
                ? BigDecimal.valueOf(payment.getAmountPaidPaise()).divide(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        templateData.put("amountPaid", amountPaid.toString());
        
        // Format currency
        templateData.put("currency", payment.getCurrency() != null ? payment.getCurrency() : "INR");
        
        // Format Razorpay fee and tax
        if (payment.getRazorpayFee() != null && payment.getRazorpayFee().compareTo(BigDecimal.ZERO) > 0) {
            templateData.put("razorpayFee", payment.getRazorpayFee().toString());
        }
        if (payment.getRazorpayTax() != null && payment.getRazorpayTax().compareTo(BigDecimal.ZERO) > 0) {
            templateData.put("razorpayTax", payment.getRazorpayTax().toString());
        }
        
        // Format refunded amount
        if (payment.getAmountRefunded() != null && payment.getAmountRefunded().compareTo(BigDecimal.ZERO) > 0) {
            templateData.put("amountRefunded", payment.getAmountRefunded().toString());
        }
        
        // Purchase order (basic info only)
        templateData.put("purchaseOrder", purchaseOrder);
        
        // Process template
        StringWriter writer = new StringWriter();
        template.process(templateData, writer);
        return writer.toString();
    }
}
