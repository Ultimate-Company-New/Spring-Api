package springapi.models.responsemodels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import springapi.models.databasemodels.ProductReview;

/**
 * Response model for ProductReview operations.
 *
 * <p>This model contains all the fields returned when retrieving product review information.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ProductReviewResponseModel {

  private Long reviewId;
  private BigDecimal ratings;
  private Integer score;
  private Boolean isDeleted;
  private String review;
  private Long userId;
  private Long productId;
  private Long parentId;
  private String createdUser;
  private String modifiedUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /** Default constructor. */
  public ProductReviewResponseModel() {}

  /**
   * Constructor to create response model from database model.
   *
   * @param productReview The database model
   */
  public ProductReviewResponseModel(ProductReview productReview) {
    if (productReview != null) {
      this.reviewId = productReview.getReviewId();
      this.ratings = productReview.getRatings();
      this.score = productReview.getScore();
      this.isDeleted = productReview.getIsDeleted();
      this.review = productReview.getReview();
      this.userId = productReview.getUserId();
      this.productId = productReview.getProductId();
      this.parentId = productReview.getParentId();
      this.createdUser = productReview.getCreatedUser();
      this.modifiedUser = productReview.getModifiedUser();
      this.createdAt = productReview.getCreatedAt();
      this.updatedAt = productReview.getUpdatedAt();
    }
  }
}
