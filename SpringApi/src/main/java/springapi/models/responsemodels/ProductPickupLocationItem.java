package com.example.springapi.models.responsemodels;

import lombok.Getter;
import lombok.Setter;

/**
 * Model to represent a pickup location with its available stock for a product. Contains pickup.
 * location details and available stock quantity.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductPickupLocationItem {
  private PickupLocationResponseModel pickupLocation;
  private Integer availableStock;

  /** Default constructor. */
  public ProductPickupLocationItem() {}

  /**
   * Constructor with all fields.
   *
   * @param pickupLocation The pickup location response model
   * @param availableStock The available stock at this pickup location
   */
  public ProductPickupLocationItem(
      PickupLocationResponseModel pickupLocation, Integer availableStock) {
    this.pickupLocation = pickupLocation;
    this.availableStock = availableStock;
  }
}
