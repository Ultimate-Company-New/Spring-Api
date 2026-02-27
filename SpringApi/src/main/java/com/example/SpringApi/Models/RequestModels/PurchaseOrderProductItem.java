package com.example.SpringApi.Models.RequestModels;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Model to represent a product item in a purchase order request. Contains product ID, price per
 * unit, and quantity.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderProductItem {
  private Long productId;
  private BigDecimal pricePerUnit;
  private Integer quantity;

  /** Default constructor. */
  public PurchaseOrderProductItem() {}

  /**
   * Constructor with all fields.
   *
   * @param productId The product ID
   * @param pricePerUnit The price per unit
   * @param quantity The quantity
   */
  public PurchaseOrderProductItem(Long productId, BigDecimal pricePerUnit, Integer quantity) {
    this.productId = productId;
    this.pricePerUnit = pricePerUnit;
    this.quantity = quantity;
  }
}

