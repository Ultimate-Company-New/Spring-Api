package com.example.SpringApi.Models.ResponseModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response model for payment verification. Indicates whether the payment was successfully verified
 * and processed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationResponseModel {
  /** Whether the payment was successfully verified */
  private boolean success;

  /** Human-readable message */
  private String message;

  /** The Razorpay payment ID */
  private String paymentId;

  /** The purchase order ID that was paid */
  private Long purchaseOrderId;

  /** The new status of the purchase order */
  private String purchaseOrderStatus;

  /** Static factory for success response */
  public static PaymentVerificationResponseModel success(
      String paymentId, Long purchaseOrderId, String status) {
    return new PaymentVerificationResponseModel(
        true,
        "Payment verified successfully. Purchase order approved.",
        paymentId,
        purchaseOrderId,
        status);
  }

  /** Static factory for failure response */
  public static PaymentVerificationResponseModel failure(String message) {
    return new PaymentVerificationResponseModel(false, message, null, null, null);
  }
}

