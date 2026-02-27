package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ShipRocket AWB (Air Waybill) assignment API.
 *
 * <p>Based on ShipRocket API: POST /courier/assign/awb
 *
 * <p>This response contains the AWB assignment details including the AWB code, courier information,
 * and shipping details.
 */
@Getter
@Setter
public class ShipRocketAwbResponseModel {

  @SerializedName("awb_assign_status")
  private Integer awbAssignStatus;

  @SerializedName("response")
  private AwbResponse response;

  /**
   * Checks if AWB assignment was successful.
   *
   * @return true if awb_assign_status is 1 (success)
   */
  public boolean isSuccess() {
    return awbAssignStatus != null && awbAssignStatus == 1;
  }

  /**
   * Gets the AWB code from the response.
   *
   * @return AWB code string or null if not available
   */
  public String getAwbCode() {
    if (response != null && response.getData() != null) {
      return response.getData().getAwbCode();
    }
    return null;
  }

  /**
   * Gets the shipment ID from the response.
   *
   * @return Shipment ID or null if not available
   */
  public Long getShipmentId() {
    if (response != null && response.getData() != null) {
      return response.getData().getShipmentId();
    }
    return null;
  }

  @Getter
  @Setter
  public static class AwbResponse {
    @SerializedName("data")
    private AwbData data;
  }

  @Getter
  @Setter
  public static class AwbData {
    @SerializedName("courier_company_id")
    private Long courierCompanyId;

    @SerializedName("awb_code")
    private String awbCode;

    @SerializedName("cod")
    private Integer cod;

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("shipment_id")
    private Long shipmentId;

    @SerializedName("awb_code_status")
    private Integer awbCodeStatus;

    @SerializedName("assigned_date_time")
    private AssignedDateTime assignedDateTime;

    @SerializedName("applied_weight")
    private Double appliedWeight;

    @SerializedName("company_id")
    private Long companyId;

    @SerializedName("courier_name")
    private String courierName;

    @SerializedName("child_courier_name")
    private String childCourierName;

    @SerializedName("pickup_scheduled_date")
    private String pickupScheduledDate;

    @SerializedName("routing_code")
    private String routingCode;

    @SerializedName("rto_routing_code")
    private String rtoRoutingCode;

    @SerializedName("invoice_no")
    private String invoiceNo;

    @SerializedName("transporter_id")
    private String transporterId;

    @SerializedName("transporter_name")
    private String transporterName;

    @SerializedName("shipped_by")
    private ShippedBy shippedBy;
  }

  @Getter
  @Setter
  public static class AssignedDateTime {
    @SerializedName("date")
    private String date;

    @SerializedName("timezone_type")
    private Integer timezoneType;

    @SerializedName("timezone")
    private String timezone;
  }

  @Getter
  @Setter
  public static class ShippedBy {
    @SerializedName("shipper_company_name")
    private String shipperCompanyName;

    @SerializedName("shipper_address_1")
    private String shipperAddress1;

    @SerializedName("shipper_address_2")
    private String shipperAddress2;

    @SerializedName("shipper_city")
    private String shipperCity;

    @SerializedName("shipper_state")
    private String shipperState;

    @SerializedName("shipper_country")
    private String shipperCountry;

    @SerializedName("shipper_postcode")
    private String shipperPostcode;

    @SerializedName("shipper_first_mile_activated")
    private Integer shipperFirstMileActivated;

    @SerializedName("shipper_phone")
    private String shipperPhone;

    @SerializedName("lat")
    private String lat;

    @SerializedName("long")
    private String lng;

    @SerializedName("shipper_email")
    private String shipperEmail;

    @SerializedName("rto_company_name")
    private String rtoCompanyName;

    @SerializedName("rto_address_1")
    private String rtoAddress1;

    @SerializedName("rto_address_2")
    private String rtoAddress2;

    @SerializedName("rto_city")
    private String rtoCity;

    @SerializedName("rto_state")
    private String rtoState;

    @SerializedName("rto_country")
    private String rtoCountry;

    @SerializedName("rto_postcode")
    private String rtoPostcode;

    @SerializedName("rto_phone")
    private String rtoPhone;

    @SerializedName("rto_email")
    private String rtoEmail;
  }
}
