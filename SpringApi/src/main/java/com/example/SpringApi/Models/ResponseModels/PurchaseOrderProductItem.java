package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Model to represent a product item in a purchase order response.
 * Contains product details, price per unit, and quantity.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderProductItem {
    private ProductResponseModel product;
    private BigDecimal pricePerUnit;
    private Integer quantity;
    
    /**
     * Default constructor.
     */
    public PurchaseOrderProductItem() {}
    
    /**
     * Constructor with all fields.
     * 
     * @param product The product response model
     * @param pricePerUnit The price per unit
     * @param quantity The quantity
     */
    public PurchaseOrderProductItem(ProductResponseModel product, BigDecimal pricePerUnit, Integer quantity) {
        this.product = product;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }
}

