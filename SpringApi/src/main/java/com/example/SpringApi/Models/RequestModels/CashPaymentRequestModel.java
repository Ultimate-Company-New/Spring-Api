package com.example.SpringApi.Models.RequestModels;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for recording a cash payment. Used when recording a manual/cash payment for a
 * purchase order.
 */
@Getter
@Setter
public class CashPaymentRequestModel {
  /** The purchase order ID to record payment for */
  private Long purchaseOrderId;

  /** Payment date when cash was received (required) */
  private LocalDate paymentDate;

  /** Amount received in INR (required) */
  private BigDecimal amount;

  /** Optional: Additional notes about the payment */
  private String notes;

  /** Optional: UPI transaction ID if payment was made via UPI */
  private String upiTransactionId;
}

