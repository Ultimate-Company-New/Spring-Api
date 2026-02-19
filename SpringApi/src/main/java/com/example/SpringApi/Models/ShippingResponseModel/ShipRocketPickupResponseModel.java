package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/** Response model for ShipRocket pickup generation API. POST /courier/generate/pickup */
@Getter
@Setter
public class ShipRocketPickupResponseModel {

  @SerializedName("pickup_status")
  private Integer pickupStatus;

  @SerializedName("response")
  private PickupResponse response;

  /**
   * Check if pickup was successfully scheduled.
   *
   * @return true if pickup_status is 1
   */
  public boolean isSuccess() {
    return pickupStatus != null && pickupStatus == 1;
  }

  @Getter
  @Setter
  public static class PickupResponse {
    @SerializedName("pickup_scheduled_date")
    private String pickupScheduledDate;

    @SerializedName("pickup_token_number")
    private String pickupTokenNumber;

    @SerializedName("status")
    private Integer status;

    @SerializedName("others")
    private String others;

    @SerializedName("pickup_generated_date")
    private PickupGeneratedDate pickupGeneratedDate;

    @SerializedName("data")
    private String data;
  }

  @Getter
  @Setter
  public static class PickupGeneratedDate {
    @SerializedName("date")
    private String date;

    @SerializedName("timezone_type")
    private Integer timezoneType;

    @SerializedName("timezone")
    private String timezone;
  }
}
