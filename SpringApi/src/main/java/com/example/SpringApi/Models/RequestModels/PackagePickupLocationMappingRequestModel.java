package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for PackagePickupLocationMapping. Used when creating/updating pickup locations with
 * package inventory.
 */
@Getter
@Setter
public class PackagePickupLocationMappingRequestModel {
  private Long packageId;
  private Integer quantity;
  private Integer reorderLevel;
  private Integer maxStockLevel;

  public PackagePickupLocationMappingRequestModel() {}

  public PackagePickupLocationMappingRequestModel(
      Long packageId, Integer quantity, Integer reorderLevel, Integer maxStockLevel) {
    this.packageId = packageId;
    this.quantity = quantity;
    this.reorderLevel = reorderLevel;
    this.maxStockLevel = maxStockLevel;
  }
}

