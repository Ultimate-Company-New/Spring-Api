package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Response model for ShipRocket label generation API. POST /courier/generate/label */
@Getter
@Setter
public class ShipRocketLabelResponseModel {

  @SerializedName("label_created")
  private Integer labelCreated;

  @SerializedName("label_url")
  private String labelUrl;

  @SerializedName("response")
  private String response;

  @SerializedName("not_created")
  private List<Object> notCreated;

  /**
   * Check if label was successfully created.
   *
   * @return true if label_created is 1
   */
  public boolean isSuccess() {
    return labelCreated != null && labelCreated == 1;
  }
}
