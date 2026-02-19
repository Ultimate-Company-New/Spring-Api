package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/** Response model for ShipRocket return order creation API. POST /orders/create/return */
@Getter
@Setter
public class ShipRocketReturnOrderResponseModel {

  @SerializedName("order_id")
  private Long orderId;

  @SerializedName("shipment_id")
  private Long shipmentId;

  @SerializedName("status")
  private String status;

  @SerializedName("status_code")
  private Integer statusCode;

  @SerializedName("company_name")
  private String companyName;

  @SerializedName("message")
  private String message;

  /**
   * Check if return order was successfully created.
   *
   * @return true if order_id is present
   */
  public boolean isSuccess() {
    return orderId != null && orderId > 0;
  }

  /** Get order ID as string. */
  public String getOrderIdAsString() {
    return orderId != null ? String.valueOf(orderId) : null;
  }
}
