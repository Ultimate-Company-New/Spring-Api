package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ShipRocket create custom order API. ShipRocket returns a flat structure with
 * order fields at root level.
 */
@Getter
@Setter
public class ShipRocketOrderResponseModel {
  @SerializedName("order_id")
  private Long orderId;

  @SerializedName("channel_order_id")
  private String channelOrderId;

  @SerializedName("shipment_id")
  private Long shipmentId;

  @SerializedName("status")
  private String status;

  @SerializedName("status_code")
  private Integer statusCode;

  @SerializedName("message")
  private String message;

  @SerializedName("awb_code")
  private String awbCode;

  @SerializedName("tracking_id")
  private String trackingId;

  @SerializedName("courier_company_id")
  private String courierCompanyId;

  @SerializedName("courier_name")
  private String courierName;

  @SerializedName("manifest_url")
  private String manifestUrl;

  @SerializedName("invoice_url")
  private String invoiceUrl;

  @SerializedName("label_url")
  private String labelUrl;

  /** Gets order_id as string (for storage in Shipment.shipRocketOrderId). */
  public String getOrderIdAsString() {
    return orderId != null ? orderId.toString() : null;
  }

  /** Gets courier_id as Long (parsed from courier_company_id string). */
  public Long getCourierId() {
    if (courierCompanyId == null || courierCompanyId.trim().isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(courierCompanyId.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}

