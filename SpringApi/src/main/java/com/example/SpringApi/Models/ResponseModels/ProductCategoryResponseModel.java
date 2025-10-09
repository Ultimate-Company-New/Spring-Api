package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.ProductCategory;

import java.time.LocalDateTime;

/**
 * Response model for ProductCategory operations.
 * 
 * This model is used for returning product category information in API responses.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductCategoryResponseModel {
    private Long categoryId;
    private String name;
    private Long parentId;
    private Boolean isEnd;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    /**
     * Constructor that creates a response model from a ProductCategory entity.
     * 
     * @param category The ProductCategory entity to convert
     */
    public ProductCategoryResponseModel(ProductCategory category) {
        if (category != null) {
            this.categoryId = category.getCategoryId();
            this.name = category.getName();
            this.parentId = category.getParentId();
            this.isEnd = category.getIsEnd();
            this.createdUser = category.getCreatedUser();
            this.modifiedUser = category.getModifiedUser();
            this.createdAt = category.getCreatedAt();
            this.updatedAt = category.getUpdatedAt();
            this.notes = category.getNotes();
        }
    }
}
