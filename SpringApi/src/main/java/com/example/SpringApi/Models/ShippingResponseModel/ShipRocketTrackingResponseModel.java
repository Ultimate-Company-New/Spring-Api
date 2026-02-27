package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Response model for ShipRocket tracking API. GET /courier/track/awb/{awb_code} */
@Getter
@Setter
public class ShipRocketTrackingResponseModel {

  @SerializedName("tracking_data")
  private TrackingData trackingData;

  /**
   * Check if tracking data is available.
   *
   * @return true if tracking_data exists and track_status is 1
   */
  public boolean isSuccess() {
    return trackingData != null
        && trackingData.getTrackStatus() != null
        && trackingData.getTrackStatus() == 1;
  }

  @Getter
  @Setter
  public static class TrackingData {
    @SerializedName("track_status")
    private Integer trackStatus;

    @SerializedName("shipment_status")
    private Integer shipmentStatus;

    @SerializedName("shipment_track")
    private List<ShipmentTrack> shipmentTrack;

    @SerializedName("shipment_track_activities")
    private List<TrackActivity> shipmentTrackActivities;

    @SerializedName("track_url")
    private String trackUrl;

    @SerializedName("etd")
    private String etd;

    @SerializedName("qc_response")
    private QcResponse qcResponse;
  }

  @Getter
  @Setter
  public static class ShipmentTrack {
    @SerializedName("id")
    private Long id;

    @SerializedName("awb_code")
    private String awbCode;

    @SerializedName("courier_company_id")
    private Integer courierCompanyId;

    @SerializedName("shipment_id")
    private Long shipmentId;

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("pickup_date")
    private String pickupDate;

    @SerializedName("delivered_date")
    private String deliveredDate;

    @SerializedName("weight")
    private String weight;

    @SerializedName("packages")
    private Integer packages;

    @SerializedName("current_status")
    private String currentStatus;

    @SerializedName("delivered_to")
    private String deliveredTo;

    @SerializedName("destination")
    private String destination;

    @SerializedName("consignee_name")
    private String consigneeName;

    @SerializedName("origin")
    private String origin;

    @SerializedName("courier_agent_details")
    private String courierAgentDetails;

    @SerializedName("courier_name")
    private String courierName;

    @SerializedName("edd")
    private String edd;

    @SerializedName("pod")
    private String pod;

    @SerializedName("pod_status")
    private String podStatus;
  }

  @Getter
  @Setter
  public static class TrackActivity {
    @SerializedName("date")
    private String date;

    @SerializedName("status")
    private String status;

    @SerializedName("activity")
    private String activity;

    @SerializedName("location")
    private String location;

    @SerializedName("sr-status")
    private String srStatus;

    @SerializedName("sr-status-label")
    private String srStatusLabel;
  }

  @Getter
  @Setter
  public static class QcResponse {
    @SerializedName("qc_image")
    private String qcImage;

    @SerializedName("qc_failed_reason")
    private String qcFailedReason;
  }
}

