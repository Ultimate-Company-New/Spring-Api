package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for Payment entity.
 *
 * <p>This model is used for returning payment information in API responses. Includes all payment
 * details including Razorpay transaction info, status, amounts, and refunds.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-07
 */
@Getter
@Setter
public class PaymentResponseModel {
  private Long paymentId;
  private String entityType;
  private Long entityId;
  private String razorpayOrderId;
  private String razorpayReceipt;
  private Long orderAmountPaise;
  private String currency;
  private LocalDateTime orderCreatedAt;
  private LocalDateTime orderExpiresAt;
  private String razorpayPaymentId;
  private String razorpaySignature;
  private String paymentGateway;
  private String paymentMethod;
  private String paymentStatus;
  private Long amountPaidPaise;
  private BigDecimal amountPaid;
  private Long razorpayFeePaise;
  private BigDecimal razorpayFee;
  private Long razorpayTaxPaise;
  private BigDecimal razorpayTax;
  private LocalDateTime paymentDate;
  private LocalDateTime capturedAt;
  private String cardLast4;
  private String cardNetwork;
  private String cardType;
  private String cardIssuer;
  private Boolean cardInternational;
  private Integer emiTenure;
  private String upiVpa;
  private String upiTransactionId;
  private String bankCode;
  private String bankName;
  private String walletName;
  private Long amountRefundedPaise;
  private BigDecimal amountRefunded;
  private Integer refundCount;
  private String lastRefundId;
  private LocalDateTime lastRefundAt;
  private String refundStatus;
  private String settlementId;
  private String settlementStatus;
  private LocalDateTime settledAt;
  private String errorCode;
  private String errorDescription;
  private String errorSource;
  private String errorReason;
  private String customerEmail;
  private String customerPhone;
  private String customerName;
  private String razorpayCustomerId;
  private String invoiceId;
  private String description;
  private String notes;
  private String payerIpAddress;
  private String payerUserAgent;
  private Boolean isTestPayment;
  private Long clientId;
  private String createdUser;
  private String modifiedUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public PaymentResponseModel(Payment payment) {
    if (payment != null) {
      this.paymentId = payment.getPaymentId();
      this.entityType = payment.getEntityType();
      this.entityId = payment.getEntityId();
      this.razorpayOrderId = payment.getRazorpayOrderId();
      this.razorpayReceipt = payment.getRazorpayReceipt();
      this.orderAmountPaise = payment.getOrderAmountPaise();
      this.currency = payment.getCurrency();
      this.orderCreatedAt = payment.getOrderCreatedAt();
      this.orderExpiresAt = payment.getOrderExpiresAt();
      this.razorpayPaymentId = payment.getRazorpayPaymentId();
      this.razorpaySignature = payment.getRazorpaySignature();
      this.paymentGateway = payment.getPaymentGateway();
      this.paymentMethod = payment.getPaymentMethod();
      this.paymentStatus = payment.getPaymentStatus();
      this.amountPaidPaise = payment.getAmountPaidPaise();
      this.amountPaid = payment.getAmountPaid();
      this.razorpayFeePaise = payment.getRazorpayFeePaise();
      this.razorpayFee = payment.getRazorpayFee();
      this.razorpayTaxPaise = payment.getRazorpayTaxPaise();
      this.razorpayTax = payment.getRazorpayTax();
      this.paymentDate = payment.getPaymentDate();
      this.capturedAt = payment.getCapturedAt();
      this.cardLast4 = payment.getCardLast4();
      this.cardNetwork = payment.getCardNetwork();
      this.cardType = payment.getCardType();
      this.cardIssuer = payment.getCardIssuer();
      this.cardInternational = payment.getCardInternational();
      this.emiTenure = payment.getEmiTenure();
      this.upiVpa = payment.getUpiVpa();
      this.upiTransactionId = payment.getUpiTransactionId();
      this.bankCode = payment.getBankCode();
      this.bankName = payment.getBankName();
      this.walletName = payment.getWalletName();
      this.amountRefundedPaise = payment.getAmountRefundedPaise();
      this.amountRefunded = payment.getAmountRefunded();
      this.refundCount = payment.getRefundCount();
      this.lastRefundId = payment.getLastRefundId();
      this.lastRefundAt = payment.getLastRefundAt();
      this.refundStatus = payment.getRefundStatus();
      this.settlementId = payment.getSettlementId();
      this.settlementStatus = payment.getSettlementStatus();
      this.settledAt = payment.getSettledAt();
      this.errorCode = payment.getErrorCode();
      this.errorDescription = payment.getErrorDescription();
      this.errorSource = payment.getErrorSource();
      this.errorReason = payment.getErrorReason();
      this.customerEmail = payment.getCustomerEmail();
      this.customerPhone = payment.getCustomerPhone();
      this.customerName = payment.getCustomerName();
      this.razorpayCustomerId = payment.getRazorpayCustomerId();
      this.invoiceId = payment.getInvoiceId();
      this.description = payment.getDescription();
      this.notes = payment.getNotes();
      this.payerIpAddress = payment.getPayerIpAddress();
      this.payerUserAgent = payment.getPayerUserAgent();
      this.isTestPayment = payment.getIsTestPayment();
      this.clientId = payment.getClientId();
      this.createdUser = payment.getCreatedUser();
      this.modifiedUser = payment.getModifiedUser();
      this.createdAt = payment.getCreatedAt();
      this.updatedAt = payment.getUpdatedAt();
    }
  }
}

