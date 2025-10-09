package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.Address;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model for Product operations.
 * 
 * This model contains all the fields returned when retrieving product information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductResponseModel {
    
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
    private Long pickupLocationId;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    // Related entities
    private ProductCategory category;
    private PickupLocation pickupLocation;
    private Address pickupLocationAddress;
    
    /**
     * Default constructor.
     */
    public ProductResponseModel() {}
    
    /**
     * Constructor that populates fields from a Product entity.
     * 
     * @param product The Product entity to populate from
     */
    public ProductResponseModel(Product product) {
        if (product != null) {
            this.productId = product.getProductId();
            this.title = product.getTitle();
            this.descriptionHtml = product.getDescriptionHtml();
            this.length = product.getLength();
            this.brand = product.getBrand();
            this.color = product.getColor();
            this.colorLabel = product.getColorLabel();
            this.isDeleted = product.getIsDeleted();
            this.condition = product.getCondition();
            this.countryOfManufacture = product.getCountryOfManufacture();
            this.model = product.getModel();
            this.itemModified = product.getItemModified();
            this.upc = product.getUpc();
            this.modificationHtml = product.getModificationHtml();
            this.price = product.getPrice();
            this.discount = product.getDiscount();
            this.isDiscountPercent = product.getIsDiscountPercent();
            this.returnsAllowed = product.getReturnsAllowed();
            this.breadth = product.getBreadth();
            this.height = product.getHeight();
            this.weightKgs = product.getWeightKgs();
            this.categoryId = product.getCategoryId();
            this.clientId = product.getClientId();
            this.pickupLocationId = product.getPickupLocationId();
            this.createdUser = product.getCreatedUser();
            this.modifiedUser = product.getModifiedUser();
            this.createdAt = product.getCreatedAt();
            this.updatedAt = product.getUpdatedAt();
            this.notes = product.getNotes();
            
            // Populate related entities
            this.category = product.getCategory();
            this.pickupLocation = product.getPickupLocation();
            if (this.pickupLocation != null) {
                this.pickupLocationAddress = this.pickupLocation.getAddress();
            }
        }
    }
}
