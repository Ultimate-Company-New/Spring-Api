package com.example.springapi.models.shippingresponsemodel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/** Response model for ShipRocket manifest generation API. POST /manifests/generate. */
@Getter
@Setter
public class ShipRocketManifestResponseModel {

  @SerializedName("status")
  private Integer status;

  @SerializedName("manifest_url")
  private String manifestUrl;

  /**
   * Check if manifest was successfully generated.
   *
   * @return true if status is 1
   */
  public boolean isSuccess() {
    return status != null && status == 1;
  }
}
