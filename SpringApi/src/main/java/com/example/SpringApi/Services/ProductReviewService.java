package com.example.SpringApi.Services;

import com.example.SpringApi.Services.Interface.IProductReviewSubTranslator;
import com.example.SpringApi.Models.ResponseModels.ProductReviewResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Repositories.ProductReviewRepository;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for ProductReview operations.
 * 
 * This service handles all business logic related to product review management
 * including CRUD operations, review management, and score tracking.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ProductReviewService extends BaseService implements IProductReviewSubTranslator {
    
    private final ProductReviewRepository productReviewRepository;
    private final UserLogService userLogService;
    
    @Autowired
    public ProductReviewService(ProductReviewRepository productReviewRepository,
                               UserLogService userLogService,
                               HttpServletRequest request) {
        super();
        this.productReviewRepository = productReviewRepository;
        this.userLogService = userLogService;
    }
    
    /**
     * Inserts a new product review.
     * 
     * This method creates a new product review with the provided details including
     * ratings, review text, and associated product and user information.
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
        userLogService.logData(getUserId(), SuccessMessages.ProductReviewSuccessMessages.InsertProductReview + " " + productReview.getReviewId(), ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW);
    }
    
    /**
     * Retrieves product reviews for a specific product in paginated batches.
     * 
     * This method returns a paginated list of reviews for a given product ID with
     * advanced filtering, sorting, and batch processing capabilities.
     * 
     * @param paginationBaseRequestModel The pagination and filtering parameters
     * @param id The product ID to retrieve reviews for
     * @return Paginated response containing product review data
     */
    @Override
    public PaginationBaseResponseModel<ProductReviewResponseModel> getProductReviewsInBatchesGivenProductId(PaginationBaseRequestModel paginationBaseRequestModel, long id) { 
        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException("Invalid pagination: end must be greater than start");
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("reviewId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Get paginated reviews for the product (without filtering for now)
        Page<ProductReview> reviewPage = productReviewRepository.findPaginatedProductReviews(
            getClientId(),
            null,
            null,
            null,
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Convert to response models
        PaginationBaseResponseModel<ProductReviewResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(reviewPage.getContent().stream()
            .map(ProductReviewResponseModel::new)
            .collect(Collectors.toList()));
        response.setTotalDataCount(reviewPage.getTotalElements());

        return response;
    }
    
    /**
     * Toggles the deleted status of a product review (soft delete/restore).
     * 
     * This method toggles the deleted flag of a product review without permanently
     * removing it from the database. Deleted reviews are hidden from standard queries.
     * When a review is marked as deleted, all its child reviews in the hierarchy are also deleted.
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
            throw new NotFoundException(ErrorMessages.ProductReviewErrorMessages.NotFound);
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
        userLogService.logData(getUserId(), SuccessMessages.ProductReviewSuccessMessages.ToggleProductReview + " " + review.getReviewId(), ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW);
    }
    
    /**
     * Sets the score of a product review (helpful/not helpful).
     * 
     * This method increases or decreases the score of a product review based on
     * user feedback. The score indicates how helpful other users found the review.
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
            throw new NotFoundException(ErrorMessages.ProductReviewErrorMessages.NotFound);
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
        userLogService.logData(getUserId(), SuccessMessages.ProductReviewSuccessMessages.ScoreUpdate + " " + review.getReviewId(), ApiRoutes.ProductReviewSubRoute.SET_PRODUCT_REVIEW_SCORE);
    }
}
