package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductReviewResponseModel;

/**
 * Interface for ProductReview operations and data access.
 *
 * <p>This interface defines the contract for all product review-related business operations
 * including CRUD operations, review management, and score tracking.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IProductReviewSubTranslator {

  /**
   * Inserts a new product review.
   *
   * <p>This method creates a new product review with the provided details including ratings, review
   * text, and associated product and user information.
   *
   * @param productReviewRequestModel The product review to create
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  void insertProductReview(ProductReviewRequestModel productReviewRequestModel);

  /**
   * Retrieves product reviews for a specific product in paginated batches.
   *
   * <p>This method returns a paginated list of reviews for a given product ID with advanced
   * filtering, sorting, and batch processing capabilities.
   *
   * @param paginationBaseRequestModel The pagination and filtering parameters
   * @param id The product ID to retrieve reviews for
   * @return Paginated response containing product review data
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  PaginationBaseResponseModel<ProductReviewResponseModel> getProductReviewsInBatchesGivenProductId(
      PaginationBaseRequestModel paginationBaseRequestModel, long id);

  /**
   * Toggles the deleted status of a product review (soft delete/restore).
   *
   * <p>This method toggles the deleted flag of a product review without permanently removing it
   * from the database. Deleted reviews are hidden from standard queries.
   *
   * @param id The ID of the product review to toggle
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the product review is not found
   * @throws UnauthorizedException if user is not authorized
   */
  void toggleProductReview(long id);

  /**
   * Sets the score of a product review (helpful/not helpful).
   *
   * <p>This method increases or decreases the score of a product review based on user feedback. The
   * score indicates how helpful other users found the review.
   *
   * @param id The ID of the product review
   * @param increaseScore True to increase score, false to decrease
   * @throws NotFoundException if the product review is not found
   * @throws UnauthorizedException if user is not authorized
   */
  void setProductReviewScore(long id, boolean increaseScore);
}
