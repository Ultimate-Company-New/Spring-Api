package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ProductCategory operations.
 *
 * <p>This model is used for returning product category information in API responses.
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
  private String fullPath; // Full hierarchical path e.g., "Electronics > Computers > Laptops"

  /**
   * Constructor that creates a response model from a ProductCategory entity. The fullPath will be
   * set to just the category name - use setFullPath() to set the complete path.
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
      this.fullPath = category.getName(); // Default to just the name, service will set full path
    }
  }

  /**
   * Constructor that creates a response model with a pre-computed full path.
   *
   * @param category The ProductCategory entity to convert
   * @param fullPath The pre-computed full hierarchical path
   */
  public ProductCategoryResponseModel(ProductCategory category, String fullPath) {
    this(category);
    if (fullPath != null && !fullPath.isEmpty()) {
      this.fullPath = fullPath;
    }
  }
}
