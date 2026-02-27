package com.example.springapi.services;

import com.example.springapi.ErrorMessages;
import com.example.springapi.SuccessMessages;
import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.filterquerybuilder.ProductReviewFilterQueryBuilder;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.databasemodels.ProductReview;
import com.example.springapi.models.requestmodels.PaginationBaseRequestModel;
import com.example.springapi.models.requestmodels.ProductReviewRequestModel;
import com.example.springapi.models.responsemodels.PaginationBaseResponseModel;
import com.example.springapi.models.responsemodels.ProductReviewResponseModel;
import com.example.springapi.repositories.ProductReviewRepository;
import com.example.springapi.services.interfaces.ProductReviewSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for ProductReview operations.
 *
 * <p>This service handles all business logic related to product review management including CRUD
 * operations, review management, and score tracking.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ProductReviewService extends BaseService implements ProductReviewSubTranslator {

  private final ProductReviewRepository productReviewRepository;
  private final UserLogService userLogService;
  private final ProductReviewFilterQueryBuilder productReviewFilterQueryBuilder;

  /**
   * Initializes ProductReviewService.
   */
  @Autowired
  public ProductReviewService(
      ProductReviewRepository productReviewRepository,
      UserLogService userLogService,
      ProductReviewFilterQueryBuilder productReviewFilterQueryBuilder,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.productReviewRepository = productReviewRepository;
    this.userLogService = userLogService;
    this.productReviewFilterQueryBuilder = productReviewFilterQueryBuilder;
  }

  /**
   * Inserts a new product review.
   *
   * <p>This method creates a new product review with the provided details including ratings, review
   * text, and associated product and user information.
   *
   * @param productReviewRequestModel The product review to create
   * @throws BadRequestException if validation fails
   */
  @Override
  @Transactional
  public void insertProductReview(ProductReviewRequestModel productReviewRequestModel) {
    // Create new product review using constructor (validations are handled in the constructor)
    ProductReview productReview = new ProductReview(productReviewRequestModel, getUser());

    // Save the review
    productReviewRepository.save(productReview);

    // Log the operation
    userLogService.logData(
        getUserId(),
        SuccessMessages.ProductReviewSuccessMessages.INSERT_PRODUCT_REVIEW
            + " "
            + productReview.getReviewId(),
        ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW);
  }

  /**
   * Retrieves product reviews for a specific product in paginated batches.
   *
   * <p>This method returns a paginated list of reviews for a given product ID with advanced
   * filtering, sorting, and batch processing capabilities.
   *
   * @param paginationBaseRequestModel The pagination and filtering parameters
   * @param id The product ID to retrieve reviews for
   * @return Paginated response containing product review data
   */
  @Override
  @Transactional(readOnly = true)
  public PaginationBaseResponseModel<ProductReviewResponseModel>
      getProductReviewsInBatchesGivenProductId(
          PaginationBaseRequestModel paginationBaseRequestModel, long id) {
    // Valid columns for filtering
    Set<String> validColumns =
        new HashSet<>(
            Arrays.asList(
                "reviewId",
                "ratings",
                "score",
                "isDeleted",
                "review",
                "userId",
                "productId",
                "parentId",
                "createdUser",
                "modifiedUser",
                "createdAt",
                "updatedAt",
                "notes"));

    // Validate filter conditions if provided
    if (paginationBaseRequestModel.getFilters() != null
        && !paginationBaseRequestModel.getFilters().isEmpty()) {
      for (PaginationBaseRequestModel.FilterCondition filter :
          paginationBaseRequestModel.getFilters()) {
        if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.ProductReviewErrorMessages.INVALID_COLUMN_NAME_FORMAT,
                  filter.getColumn()));
        }

        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.ProductReviewErrorMessages.INVALID_OPERATOR_FORMAT,
                  filter.getOperator()));
        }

        String columnType = productReviewFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());

        filter.validateValuePresence();
      }
    }

    // Calculate page size and offset
    int start = paginationBaseRequestModel.getStart();
    int end = paginationBaseRequestModel.getEnd();
    int pageSize = end - start;

    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("reviewId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    Page<ProductReview> reviewPage =
        productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            id,
            paginationBaseRequestModel.getSelectedIds(),
            paginationBaseRequestModel.getLogicOperator() != null
                ? paginationBaseRequestModel.getLogicOperator()
                : "AND",
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable);

    PaginationBaseResponseModel<ProductReviewResponseModel> response =
        new PaginationBaseResponseModel<>();
    response.setData(
        reviewPage.getContent().stream()
            .map(ProductReviewResponseModel::new)
            .collect(Collectors.toCollection(ArrayList::new)));
    response.setTotalDataCount(reviewPage.getTotalElements());

    return response;
  }

  /**
   * Toggles the deleted status of a product review (soft delete/restore).
   *
   * <p>This method toggles the deleted flag of a product review without permanently removing it
   * from the database. Deleted reviews are hidden from standard queries. When a review is marked as
   * deleted, all its child reviews in the hierarchy are also deleted.
   *
   * @param id The ID of the product review to toggle
   * @throws NotFoundException if the product review is not found
   */
  @Override
  @Transactional
  public void toggleProductReview(long id) {
    // Find the review filtered by clientId
    ProductReview review = productReviewRepository.findByReviewIdAndClientId(id, getClientId());
    if (review == null) {
      throw new NotFoundException(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND);
    }

    // Toggle the deleted status
    boolean newDeletedStatus = !review.getIsDeleted();
    review.setIsDeleted(newDeletedStatus);
    review.setModifiedUser(getUser());

    // Save the updated review
    productReviewRepository.save(review);

    // If marking as deleted, also delete all child reviews in the hierarchy
    if (newDeletedStatus) {
      productReviewRepository.markAllDescendantsAsDeleted(review.getReviewId(), getUser());
    }

    // Log the operation
    userLogService.logData(
        getUserId(),
        SuccessMessages.ProductReviewSuccessMessages.TOGGLE_PRODUCT_REVIEW
            + " "
            + review.getReviewId(),
        ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW);
  }

  /**
   * Sets the score of a product review (helpful/not helpful).
   *
   * <p>This method increases or decreases the score of a product review based on user feedback. The
   * score indicates how helpful other users found the review.
   *
   * @param id The ID of the product review
   * @param increaseScore True to increase score, false to decrease
   * @throws NotFoundException if the product review is not found
   */
  @Override
  @Transactional
  public void setProductReviewScore(long id, boolean increaseScore) {
    // Find the review filtered by clientId
    ProductReview review = productReviewRepository.findByReviewIdAndClientId(id, getClientId());
    if (review == null) {
      throw new NotFoundException(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND);
    }

    // Update the score
    int currentScore = review.getScore() != null ? review.getScore() : 0;
    if (increaseScore) {
      review.setScore(currentScore + 1);
    } else {
      review.setScore(Math.max(0, currentScore - 1)); // Don't go below 0
    }
    review.setModifiedUser(getUser());

    // Save the updated review
    productReviewRepository.save(review);

    // Log the operation
    userLogService.logData(
        getUserId(),
        SuccessMessages.ProductReviewSuccessMessages.SCORE_UPDATE + " " + review.getReviewId(),
        ApiRoutes.ProductReviewSubRoute.SET_PRODUCT_REVIEW_SCORE);
  }
}
