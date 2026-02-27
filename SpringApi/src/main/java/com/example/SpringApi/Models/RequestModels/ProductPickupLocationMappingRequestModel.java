package com.example.springapi.models.requestmodels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for ProductPickupLocationMapping. Used when creating/updating pickup locations
 * with. product inventory.
 */
@Getter
@Setter
public class ProductPickupLocationMappingRequestModel {
  private Long productId;
  private Integer quantity;

  public ProductPickupLocationMappingRequestModel() {}

  public ProductPickupLocationMappingRequestModel(Long productId, Integer quantity) {
    this.productId = productId;
    this.quantity = quantity;
  }
}
