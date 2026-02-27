package com.example.springapi.models.requestmodels;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for ProductReview operations.
 *
 * <p>This model contains all the fields required when creating or updating a product review.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductReviewRequestModel {

  private Long reviewId;
  private BigDecimal ratings;
  private String review;
  private Long userId;
  private Long productId;
  private Long parentId;

  /** Default constructor. */
  public ProductReviewRequestModel() {
    // Required for JSON deserialization.
  }
}
