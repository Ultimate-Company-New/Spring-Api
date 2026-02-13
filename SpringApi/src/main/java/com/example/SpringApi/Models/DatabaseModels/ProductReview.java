package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the ProductReview table.
 * 
 * This entity represents product reviews and ratings from customers.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ProductReview")
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewId", nullable = false)
    private Long reviewId;

    @Column(name = "ratings", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratings;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "review", nullable = false, columnDefinition = "TEXT")
    private String review;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @Column(name = "productId", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @Column(name = "parentId")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId", insertable = false, updatable = false)
    private ProductReview parentReview;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public ProductReview() {
        // Default constructor for JPA
    }

    /**
     * Constructor for creating a new product review.
     * 
     * @param request The ProductReviewRequestModel containing review data
     * @param createdUser The username of the user creating this record
     * @throws BadRequestException if validation fails
     */
    public ProductReview(ProductReviewRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
        this.score = 0; // Initialize score to 0
        this.isDeleted = false; // Initialize as not deleted
    }

    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The ProductReviewRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(ProductReviewRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.INVALID_ID);
        }
        
        // Validate ratings
        if (request.getRatings() == null || 
            request.getRatings().compareTo(BigDecimal.ZERO) < 0 || 
            request.getRatings().compareTo(new BigDecimal("5.0")) > 0) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.ER001);
        }
        
        // Validate review text
        if (request.getReview() == null || request.getReview().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.ER002);
        }
        
        // Validate user ID
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.ER003);
        }
        
        // Validate product ID
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.ER004);
        }
    }

    /**
     * Validates the user parameter for audit fields.
     * 
     * @param user The username to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ProductReviewErrorMessages.INVALID_AUDIT_USER);
        }
    }

    /**
     * Sets entity fields from the request model.
     * 
     * @param request The ProductReviewRequestModel containing the data to set
     */
    private void setFieldsFromRequest(ProductReviewRequestModel request) {
        this.ratings = request.getRatings();
        this.review = request.getReview();
        this.userId = request.getUserId();
        this.productId = request.getProductId();
        this.parentId = request.getParentId();
    }
}
