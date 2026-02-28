package com.example.springapi.models.requestmodels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for verifying a Razorpay payment. Contains the payment details returned by
 * Razorpay. after successful payment.
 */
@Getter
@Setter
public class RazorpayVerifyRequestModel {
  /** The purchase order ID the payment was made for. */
  private Long purchaseOrderId;

  /** Razorpay order ID (created by our backend). */
  private String razorpayOrderId;

  /** Razorpay payment ID (generated after successful payment). */
  private String razorpayPaymentId;

  /** Razorpay signature for verification. */
  private String razorpaySignature;
}
