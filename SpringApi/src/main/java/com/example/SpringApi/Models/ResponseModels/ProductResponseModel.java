package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
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
    
    // Related entities
    private ProductCategoryResponseModel category;
    
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
            else{
                throw new NotFoundException(ErrorMessages.ProductErrorMessages.NoPickupLocationsFound);
            }
        }
    }
}
