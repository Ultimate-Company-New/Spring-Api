package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ProductCategory with full hierarchical path.
 *
 * <p>This model includes the category information along with its complete breadcrumb path from root
 * to leaf, making it easy for frontend to display searchable category selections.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-12-05
 */
@Getter
@Setter
public class ProductCategoryWithPathResponseModel {
  private Long categoryId;
  private String name;
  private String fullPath; // e.g., "Electronics › Computers › Laptops"
  private Long parentId;
  private Boolean isEnd; // true = leaf (selectable), false = has children (drillable)

  /**
   * Constructor for creating response model with full path.
   *
   * @param categoryId The unique identifier of the category
   * @param name The name of the category
   * @param fullPath The complete hierarchical path with separators
   * @param parentId The ID of the parent category
   * @param isEnd Whether this is a leaf category (true) or has children (false)
   */
  public ProductCategoryWithPathResponseModel(
      Long categoryId, String name, String fullPath, Long parentId, Boolean isEnd) {
    this.categoryId = categoryId;
    this.name = name;
    this.fullPath = fullPath;
    this.parentId = parentId;
    this.isEnd = isEnd;
  }
}
