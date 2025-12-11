package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.User;
import org.hibernate.Hibernate;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    // Product Images (Required - stored on ImgBB)
    private String mainImageUrl;
    private String topImageUrl;
    private String bottomImageUrl;
    private String frontImageUrl;
    private String backImageUrl;
    private String rightImageUrl;
    private String leftImageUrl;
    private String detailsImageUrl;
    
    // Product Images (Optional - stored on ImgBB)
    private String defectImageUrl;
    private String additionalImage1Url;
    private String additionalImage2Url;
    private String additionalImage3Url;
    
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    // Item availability
    private LocalDateTime itemAvailableFrom;
    private String itemAvailableFromTimezone;
    
    // Created by user details (firstName, lastName, email)
    private CreatedByUserInfo createdByUserInfo;
    
    // Related entities
    private ProductCategoryResponseModel category;
    
    /**
     * Inner class to hold created by user information.
     * Contains essential user details for display purposes.
     */
    @Getter
    @Setter
    public static class CreatedByUserInfo {
        private Long userId;
        private String firstName;
        private String lastName;
        private String loginName;
        
        public CreatedByUserInfo() {}
        
        public CreatedByUserInfo(User user) {
            if (user != null) {
                this.userId = user.getUserId();
                this.firstName = user.getFirstName();
                this.lastName = user.getLastName();
                this.loginName = user.getLoginName();
            }
        }
        
        /**
         * Returns the full name of the user.
         */
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return loginName;
        }
    }
    
    // List of pickup locations with their available stock quantities
    private List<ProductPickupLocationItem> pickupLocations;
    
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
            
            // Populate image URLs
            this.mainImageUrl = product.getMainImageUrl();
            this.topImageUrl = product.getTopImageUrl();
            this.bottomImageUrl = product.getBottomImageUrl();
            this.frontImageUrl = product.getFrontImageUrl();
            this.backImageUrl = product.getBackImageUrl();
            this.rightImageUrl = product.getRightImageUrl();
            this.leftImageUrl = product.getLeftImageUrl();
            this.detailsImageUrl = product.getDetailsImageUrl();
            this.defectImageUrl = product.getDefectImageUrl();
            this.additionalImage1Url = product.getAdditionalImage1Url();
            this.additionalImage2Url = product.getAdditionalImage2Url();
            this.additionalImage3Url = product.getAdditionalImage3Url();
            
            this.createdUser = product.getCreatedUser();
            this.modifiedUser = product.getModifiedUser();
            this.createdAt = product.getCreatedAt();
            this.updatedAt = product.getUpdatedAt();
            this.notes = product.getNotes();
            this.itemAvailableFrom = product.getItemAvailableFrom();
            this.itemAvailableFromTimezone = product.getItemAvailableFromTimezone();
            
            // Populate created by user info if available
            if (product.getCreatedByUser() != null && Hibernate.isInitialized(product.getCreatedByUser())) {
                this.createdByUserInfo = new CreatedByUserInfo(product.getCreatedByUser());
            }
            
            // Populate related entities with safe DTOs
            if (product.getCategory() != null && Hibernate.isInitialized(product.getCategory())) {
                this.category = new ProductCategoryResponseModel(product.getCategory());
            }

            // Populate pickup locations with their available stock from ProductPickupLocationMapping
            this.pickupLocations = new ArrayList<>();
            if (product.getProductPickupLocationMappings() != null && 
                Hibernate.isInitialized(product.getProductPickupLocationMappings()) &&
                !product.getProductPickupLocationMappings().isEmpty()) {
                
                for (ProductPickupLocationMapping mapping : product.getProductPickupLocationMappings()) {
                    if (mapping.getPickupLocation() != null && 
                        Hibernate.isInitialized(mapping.getPickupLocation())) {
                        
                        PickupLocationResponseModel pickupLocationResponse = 
                            new PickupLocationResponseModel(mapping.getPickupLocation());
                        
                        // Create ProductPickupLocationItem with pickup location and available stock
                        ProductPickupLocationItem pickupLocationItem = new ProductPickupLocationItem(
                            pickupLocationResponse,
                            mapping.getAvailableStock()
                        );
                        
                        this.pickupLocations.add(pickupLocationItem);
                    }
                }
            }
            // Note: Empty pickupLocations list is valid - products might not be assigned to locations yet
        }
    }

    /**
     * Sets the full path on the category response model.
     * Should be called after construction with the pre-computed full path.
     * 
     * @param fullPath The full hierarchical path (e.g., "Electronics > Computers > Laptops")
     */
    public void setCategoryFullPath(String fullPath) {
        if (this.category != null && fullPath != null) {
            this.category.setFullPath(fullPath);
        }
    }
}
