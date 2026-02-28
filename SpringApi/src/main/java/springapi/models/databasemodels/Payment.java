package springapi.models.databasemodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the Payment table.
 *
 * <p>Stores comprehensive payment information for orders including: - Razorpay transaction details
 * - Payment status and method - Refund information - Fee breakdown
 *
 * <p>Supports polymorphic association via entityType + entityId to link to PurchaseOrder, Order, or
 * any other payable entity.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-07
 */
@Getter
@Setter
@Entity
@Table(
    name = "Payment",
    indexes = {
      @Index(name = "idx_payment_entity", columnList = "entityType, entityId"),
      @Index(name = "idx_payment_razorpay_order_id", columnList = "razorpayOrderId"),
      @Index(name = "idx_payment_razorpay_payment_id", columnList = "razorpayPaymentId"),
      @Index(name = "idx_payment_status", columnList = "paymentStatus"),
      @Index(name = "idx_payment_client_id", columnList = "clientId"),
      @Index(name = "idx_payment_created_at", columnList = "createdAt")
    })
public class Payment {
  /** Represents the payment order data component. */
  @Getter
  @Setter
  public static class PaymentOrderData {
    private String entityType;
    private Long entityId;
    private String razorpayOrderId;
    private String razorpayReceipt;
    private Long orderAmountPaise;
    private String currency;
    private String paymentGateway;
    private Long clientId;
    private String createdUser;
  }

  /** Represents the manual payment data component. */
  @Getter
  @Setter
  public static class ManualPaymentData {
    private String entityType;
    private Long entityId;
    private Long amountPaidPaise;
    private BigDecimal amountPaid;
    private String currency;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String notes;
    private String upiTransactionId;
    private String description;
    private boolean testPayment;
    private Long clientId;
    private String createdUser;
  }

  // ========================================================================
  // ENUMS
  // ========================================================================

  /** Payment status enum matching Razorpay statuses. */
  public enum PaymentStatus {
    CREATED("CREATED"), // Order created, awaiting payment
    AUTHORIZED("AUTHORIZED"), // Payment authorized but not captured
    CAPTURED("CAPTURED"), // Payment captured successfully
    FAILED("FAILED"), // Payment failed
    REFUNDED("REFUNDED"), // Fully refunded
    PARTIALLY_REFUNDED("PARTIALLY_REFUNDED"), // Partially refunded
    PENDING("PENDING"), // Payment pending (for async methods)
    EXPIRED("EXPIRED"); // Order expired without payment

    private final String value;

    PaymentStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /** Checks whether valid. */
    public static boolean isValid(String value) {
      if (value == null) {
        return false;
      }
      for (PaymentStatus status : values()) {
        if (status.value.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  /** Payment method enum. */
  public enum PaymentMethod {
    CARD("CARD"),
    UPI("UPI"),
    NETBANKING("NETBANKING"),
    WALLET("WALLET"),
    EMI("EMI"),
    PAY_LATER("PAY_LATER"),
    BANK_TRANSFER("BANK_TRANSFER"),
    CASH("CASH"),
    OTHER("OTHER");

    private final String value;

    PaymentMethod(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** Payment gateway enum. */
  public enum PaymentGateway {
    RAZORPAY("RAZORPAY"),
    STRIPE("STRIPE"),
    PAYTM("PAYTM"),
    PHONEPE("PHONEPE"),
    MANUAL("MANUAL"),
    OTHER("OTHER");

    private final String value;

    PaymentGateway(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** Entity type for polymorphic association. */
  public enum EntityType {
    PURCHASE_ORDER("PURCHASE_ORDER"),
    SALES_ORDER("SALES_ORDER"),
    INVOICE("INVOICE"),
    SUBSCRIPTION("SUBSCRIPTION");

    private final String value;

    EntityType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  // ========================================================================
  // PRIMARY KEY
  // ========================================================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "paymentId", nullable = false)
  private Long paymentId;

  // ========================================================================
  // POLYMORPHIC ENTITY REFERENCE
  // ========================================================================

  @Column(name = "entityType", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entityId", nullable = false)
  private Long entityId;

  // ========================================================================
  // RAZORPAY ORDER DETAILS (Pre-payment)
  // ========================================================================

  /** Razorpay Order ID (created before payment). */
  @Column(name = "razorpayOrderId", length = 100)
  private String razorpayOrderId;

  /** Receipt/reference number sent to Razorpay. */
  @Column(name = "razorpayReceipt", length = 200)
  private String razorpayReceipt;

  /** Order amount in paise (smallest currency unit). */
  @Column(name = "orderAmountPaise")
  private Long orderAmountPaise;

  /** Order currency (INR, USD, etc.). */
  @Column(name = "currency", length = 10)
  private String currency;

  /** Order creation timestamp from Razorpay. */
  @Column(name = "orderCreatedAt")
  private LocalDateTime orderCreatedAt;

  /** Order expiry timestamp. */
  @Column(name = "orderExpiresAt")
  private LocalDateTime orderExpiresAt;

  // ========================================================================
  // RAZORPAY PAYMENT DETAILS (Post-payment)
  // ========================================================================

  /** Razorpay Payment ID (after successful payment). */
  @Column(name = "razorpayPaymentId", length = 100)
  private String razorpayPaymentId;

  /** Razorpay signature for verification. */
  @Column(name = "razorpaySignature", length = 500)
  private String razorpaySignature;

  /** Payment method used (card, upi, netbanking, wallet, etc.). */
  @Column(name = "paymentMethod", length = 50)
  private String paymentMethod;

  /** Payment gateway used. */
  @Column(name = "paymentGateway", nullable = false, length = 50)
  private String paymentGateway;

  /** Current payment status. */
  @Column(name = "paymentStatus", nullable = false, length = 50)
  private String paymentStatus;

  /** Amount paid in paise. */
  @Column(name = "amountPaidPaise")
  private Long amountPaidPaise;

  /** Amount paid in INR (for display). */
  @Column(name = "amountPaid", precision = 15, scale = 2)
  private BigDecimal amountPaid;

  /** Razorpay fee charged in paise. */
  @Column(name = "razorpayFeePaise")
  private Long razorpayFeePaise;

  /** Razorpay fee in INR. */
  @Column(name = "razorpayFee", precision = 15, scale = 2)
  private BigDecimal razorpayFee;

  /** GST on Razorpay fee in paise. */
  @Column(name = "razorpayTaxPaise")
  private Long razorpayTaxPaise;

  /** GST on Razorpay fee in INR. */
  @Column(name = "razorpayTax", precision = 15, scale = 2)
  private BigDecimal razorpayTax;

  /** Payment timestamp. */
  @Column(name = "paymentDate")
  private LocalDateTime paymentDate;

  /** Payment captured timestamp (for auth + capture flow). */
  @Column(name = "capturedAt")
  private LocalDateTime capturedAt;

  // ========================================================================
  // CARD DETAILS (if paid by card)
  // ========================================================================

  /** Card last 4 digits. */
  @Column(name = "cardLast4", length = 4)
  private String cardLast4;

  /** Card network (Visa, Mastercard, etc.). */
  @Column(name = "cardNetwork", length = 50)
  private String cardNetwork;

  /** Card type (credit, debit, prepaid). */
  @Column(name = "cardType", length = 20)
  private String cardType;

  /** Card issuing bank. */
  @Column(name = "cardIssuer", length = 100)
  private String cardIssuer;

  /** Whether card is international. */
  @Column(name = "cardInternational")
  private Boolean cardInternational;

  /** EMI tenure in months (if EMI). */
  @Column(name = "emiTenure")
  private Integer emiTenure;

  // ========================================================================
  // UPI DETAILS (if paid by UPI)
  // ========================================================================

  /** UPI VPA (Virtual Payment Address). */
  @Column(name = "upiVpa", length = 100)
  private String upiVpa;

  /** UPI transaction reference. */
  @Column(name = "upiTransactionId", length = 100)
  private String upiTransactionId;

  // ========================================================================
  // NET BANKING DETAILS (if paid by netbanking)
  // ========================================================================

  /** Bank code. */
  @Column(name = "bankCode", length = 20)
  private String bankCode;

  /** Bank name. */
  @Column(name = "bankName", length = 100)
  private String bankName;

  // ========================================================================
  // WALLET DETAILS (if paid by wallet)
  // ========================================================================

  /** Wallet name (Paytm, PhonePe, etc.). */
  @Column(name = "walletName", length = 50)
  private String walletName;

  // ========================================================================
  // REFUND DETAILS
  // ========================================================================

  /** Total amount refunded in paise. */
  @Column(name = "amountRefundedPaise")
  private Long amountRefundedPaise;

  /** Total amount refunded in INR. */
  @Column(name = "amountRefunded", precision = 15, scale = 2)
  private BigDecimal amountRefunded;

  /** Number of refunds processed. */
  @Column(name = "refundCount")
  private Integer refundCount;

  /** Last refund ID. */
  @Column(name = "lastRefundId", length = 100)
  private String lastRefundId;

  /** Last refund timestamp. */
  @Column(name = "lastRefundAt")
  private LocalDateTime lastRefundAt;

  /** Refund status (none, partial, full). */
  @Column(name = "refundStatus", length = 20)
  private String refundStatus;

  // ========================================================================
  // SETTLEMENT DETAILS
  // ========================================================================

  /** Settlement ID from Razorpay. */
  @Column(name = "settlementId", length = 100)
  private String settlementId;

  /** Settlement status. */
  @Column(name = "settlementStatus", length = 50)
  private String settlementStatus;

  /** Settlement timestamp. */
  @Column(name = "settledAt")
  private LocalDateTime settledAt;

  // ========================================================================
  // ERROR DETAILS (if failed)
  // ========================================================================

  /** Error code from Razorpay. */
  @Column(name = "errorCode", length = 100)
  private String errorCode;

  /** Error description. */
  @Column(name = "errorDescription", length = 500)
  private String errorDescription;

  /** Error source (gateway, bank, customer). */
  @Column(name = "errorSource", length = 50)
  private String errorSource;

  /** Error reason. */
  @Column(name = "errorReason", length = 200)
  private String errorReason;

  // ========================================================================
  // CUSTOMER DETAILS
  // ========================================================================

  /** Customer email. */
  @Column(name = "customerEmail", length = 255)
  private String customerEmail;

  /** Customer phone. */
  @Column(name = "customerPhone", length = 20)
  private String customerPhone;

  /** Customer name. */
  @Column(name = "customerName", length = 200)
  private String customerName;

  /** Razorpay customer ID (if saved). */
  @Column(name = "razorpayCustomerId", length = 100)
  private String razorpayCustomerId;

  // ========================================================================
  // ADDITIONAL DETAILS
  // ========================================================================

  /** Invoice ID (if linked to invoice). */
  @Column(name = "invoiceId", length = 100)
  private String invoiceId;

  /** Payment description. */
  @Column(name = "description", length = 500)
  private String description;

  /** Additional notes (JSON). */
  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  /** IP address of payer. */
  @Column(name = "payerIpAddress", length = 50)
  private String payerIpAddress;

  /** User agent of payer. */
  @Column(name = "payerUserAgent", length = 500)
  private String payerUserAgent;

  /** Whether this is a test payment. */
  @Column(name = "isTestPayment")
  private Boolean isTestPayment;

  // ========================================================================
  // STANDARD AUDIT FIELDS
  // ========================================================================

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  // ========================================================================
  // CONSTRUCTORS
  // ========================================================================

  /** Default constructor. */
  public Payment() {}

  /** Constructor for creating a new payment order (before payment). */
  public Payment(PaymentOrderData data) {
    this.entityType = data.entityType;
    this.entityId = data.entityId;
    this.razorpayOrderId = data.razorpayOrderId;
    this.razorpayReceipt = data.razorpayReceipt;
    this.orderAmountPaise = data.orderAmountPaise;
    this.currency = data.currency;
    this.paymentGateway = data.paymentGateway;
    this.paymentStatus = PaymentStatus.CREATED.getValue();
    this.clientId = data.clientId;
    this.createdUser = data.createdUser;
    this.modifiedUser = data.createdUser;
    this.orderCreatedAt = LocalDateTime.now();
    this.refundCount = 0;
    this.amountRefundedPaise = 0L;
    this.amountRefunded = BigDecimal.ZERO;
  }

  /** Constructor for creating a cash/manual payment (immediately captured). */
  public Payment(ManualPaymentData data) {
    this.entityType = data.entityType;
    this.entityId = data.entityId;
    this.razorpayOrderId = null; // No Razorpay order for cash payment
    this.razorpayReceipt = "CASH_" + data.entityId + "_" + System.currentTimeMillis();
    this.orderAmountPaise = data.amountPaidPaise;
    this.currency = data.currency;
    this.paymentGateway = PaymentGateway.MANUAL.getValue();
    this.paymentMethod = data.paymentMethod;
    this.paymentStatus = PaymentStatus.CAPTURED.getValue();
    this.amountPaidPaise = data.amountPaidPaise;
    this.amountPaid = data.amountPaid;
    this.paymentDate = data.paymentDate;
    this.capturedAt = LocalDateTime.now();
    this.notes = data.notes;
    this.upiTransactionId = data.upiTransactionId;
    this.description = data.description;
    this.isTestPayment = data.testPayment;
    this.clientId = data.clientId;
    this.createdUser = data.createdUser;
    this.modifiedUser = data.createdUser;
    this.orderCreatedAt = LocalDateTime.now();
    this.refundCount = 0;
    this.amountRefundedPaise = 0L;
    this.amountRefunded = BigDecimal.ZERO;
  }

  // ========================================================================
  // HELPER METHODS
  // ========================================================================

  /** Marks payment as captured/successful. */
  public void markAsCaptured(
      String razorpayPaymentId,
      String razorpaySignature,
      String paymentMethod,
      Long amountPaidPaise,
      String modifiedUser) {
    this.razorpayPaymentId = razorpayPaymentId;
    this.razorpaySignature = razorpaySignature;
    this.paymentMethod = paymentMethod;
    this.amountPaidPaise = amountPaidPaise;
    this.amountPaid = BigDecimal.valueOf(amountPaidPaise).divide(BigDecimal.valueOf(100));
    this.paymentStatus = PaymentStatus.CAPTURED.getValue();
    this.paymentDate = LocalDateTime.now();
    this.capturedAt = LocalDateTime.now();
    this.modifiedUser = modifiedUser;
  }

  /** Marks payment as failed. */
  public void markAsFailed(
      String errorCode,
      String errorDescription,
      String errorSource,
      String errorReason,
      String modifiedUser) {
    this.paymentStatus = PaymentStatus.FAILED.getValue();
    this.errorCode = errorCode;
    this.errorDescription = errorDescription;
    this.errorSource = errorSource;
    this.errorReason = errorReason;
    this.modifiedUser = modifiedUser;
  }

  /** Records a refund. */
  public void recordRefund(String refundId, Long refundAmountPaise, String modifiedUser) {
    this.lastRefundId = refundId;
    this.lastRefundAt = LocalDateTime.now();
    this.refundCount = (this.refundCount != null ? this.refundCount : 0) + 1;

    long totalRefunded =
        (this.amountRefundedPaise != null ? this.amountRefundedPaise : 0L) + refundAmountPaise;
    this.amountRefundedPaise = totalRefunded;
    this.amountRefunded = BigDecimal.valueOf(totalRefunded).divide(BigDecimal.valueOf(100));

    // Update refund status
    if (totalRefunded >= (this.amountPaidPaise != null ? this.amountPaidPaise : 0L)) {
      this.paymentStatus = PaymentStatus.REFUNDED.getValue();
      this.refundStatus = "FULL";
    } else {
      this.paymentStatus = PaymentStatus.PARTIALLY_REFUNDED.getValue();
      this.refundStatus = "PARTIAL";
    }

    this.modifiedUser = modifiedUser;
  }

  /** Sets card details. */
  public void setCardDetails(
      String last4,
      String network,
      String type,
      String issuer,
      Boolean international,
      Integer emiTenure) {
    this.cardLast4 = last4;
    this.cardNetwork = network;
    this.cardType = type;
    this.cardIssuer = issuer;
    this.cardInternational = international;
    this.emiTenure = emiTenure;
  }

  /** Sets UPI details. */
  public void setUpiDetails(String vpa, String transactionId) {
    this.upiVpa = vpa;
    this.upiTransactionId = transactionId;
  }

  /** Sets net banking details. */
  public void setNetBankingDetails(String bankCode, String bankName) {
    this.bankCode = bankCode;
    this.bankName = bankName;
  }

  /** Sets Razorpay fee details. */
  public void setRazorpayFeeDetails(Long feePaise, Long taxPaise) {
    this.razorpayFeePaise = feePaise;
    this.razorpayFee = BigDecimal.valueOf(feePaise).divide(BigDecimal.valueOf(100));
    this.razorpayTaxPaise = taxPaise;
    this.razorpayTax = BigDecimal.valueOf(taxPaise).divide(BigDecimal.valueOf(100));
  }

  /** Check if payment is successful. */
  public boolean isSuccessful() {
    return PaymentStatus.CAPTURED.getValue().equals(this.paymentStatus)
        || PaymentStatus.PARTIALLY_REFUNDED.getValue().equals(this.paymentStatus);
  }

  /** Check if payment can be refunded. */
  public boolean canBeRefunded() {
    if (!isSuccessful()) {
      return false;
    }
    long paid = this.amountPaidPaise != null ? this.amountPaidPaise : 0L;
    long refunded = this.amountRefundedPaise != null ? this.amountRefundedPaise : 0L;
    return paid > refunded;
  }

  /** Get remaining refundable amount in paise. */
  public long getRefundableAmountPaise() {
    if (!canBeRefunded()) {
      return 0L;
    }
    long paid = this.amountPaidPaise != null ? this.amountPaidPaise : 0L;
    long refunded = this.amountRefundedPaise != null ? this.amountRefundedPaise : 0L;
    return paid - refunded;
  }
}
