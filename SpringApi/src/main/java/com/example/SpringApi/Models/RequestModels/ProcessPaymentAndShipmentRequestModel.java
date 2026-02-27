package com.example.SpringApi.Models.RequestModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for processing payment and shipments. Can contain either online payment
 * (RazorpayVerifyRequestModel) or cash payment (CashPaymentRequestModel).
 */
@Getter
@Setter
public class ProcessPaymentAndShipmentRequestModel {
  /**
   * Whether this is a cash payment (true) or online payment (false) Using @JsonProperty to ensure
   * correct JSON mapping since boolean fields starting with "is" can cause issues Excluded from
   * Lombok getter/setter to use custom methods
   */
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  @JsonProperty("isCashPayment")
  private boolean cashPayment;

  /** Cash payment request (required if cashPayment is true) */
  private CashPaymentRequestModel cashPaymentRequest;

  /** Online payment request (required if cashPayment is false) */
  private RazorpayVerifyRequestModel onlinePaymentRequest;

  /** Getter for isCashPayment (for backward compatibility with existing code) */
  public boolean isCashPayment() {
    return cashPayment;
  }

  /** Setter for isCashPayment (maps from JSON field "isCashPayment") */
  @JsonProperty("isCashPayment")
  public void setIsCashPayment(boolean cashPayment) {
    this.cashPayment = cashPayment;
  }
}

