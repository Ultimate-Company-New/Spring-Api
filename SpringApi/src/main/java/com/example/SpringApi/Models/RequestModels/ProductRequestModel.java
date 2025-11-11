package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Request model for Product operations.
 * 
 * This model contains all the fields required for creating or updating a product.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductRequestModel {
    
    private Long productId;
    private String title;
    private String descriptionHtml;
    private BigDecimal length;
    private String brand;
    private String color;
    private String colorLabel;
    private Boolean isDeleted;
    private String condition;
    private String countryOfManufacture;
    private String model;
    private Boolean itemModified;
    private String upc;
    private String modificationHtml;
    private BigDecimal price;
    private BigDecimal discount;
    private Boolean isDiscountPercent;
    private Boolean returnsAllowed;
    private BigDecimal breadth;
    private BigDecimal height;
    private BigDecimal weightKgs;
    private Long categoryId;
    private Long clientId;
    
    /**
     * Map of pickup location ID to available quantity at that location.
     * Key: pickupLocationId
     * Value: availableStock (quantity available at that pickup location)
     * 
     * A product can be available at multiple pickup locations with different quantities.
     */
    private Map<Long, Integer> pickupLocationQuantities;
    
    private String notes;
    
    // Image fields
    private String mainImage;
    private String topImage;
    private String bottomImage;
    private String frontImage;
    private String backImage;
    private String rightImage;
    private String leftImage;
    private String detailsImage;
    private String defectImage;
    private String additionalImage1;
    private String additionalImage2;
    private String additionalImage3;
}
