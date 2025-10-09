package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for ProductCategory operations.
 * 
 * This model is used for creating and updating product category information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductCategoryRequestModel {
    private String name;
    private Long parentId;
    private Boolean isEnd;
    private String notes;
}
