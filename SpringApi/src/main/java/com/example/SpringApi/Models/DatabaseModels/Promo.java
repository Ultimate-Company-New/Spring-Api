package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * JPA Entity for the Promo table.
 * 
 * This entity represents promotional codes and discount offers.
 * It includes client association and discount calculation logic.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Promo`")
public class Promo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promoId", nullable = false)
    private Long promoId;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "isPercent", nullable = false)
    private Boolean isPercent = false;
    
    @Column(name = "discountValue", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "promoCode", nullable = false, unique = true, length = 100)
    private String promoCode;
    
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "createdUser", nullable = false, length = 255)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "modifiedUser", nullable = false, length = 255)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;
    
    public Promo() {}
    
    /**
     * Constructor for creating a new promo.
     * 
     * @param request The PromoRequestModel containing promo data
     * @param createdUser The username of the user creating this record
     */
    public Promo(PromoRequestModel request, String createdUser, long clientId) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
        this.notes = "Created Via SpringApi";
        this.clientId = clientId;
    }

    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The PromoRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(PromoRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidRequest);
        }
        
        // Validate description (required, length > 0, max 500 chars)
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidDescription);
        }
        if (request.getDescription().trim().length() > 500) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.DescriptionTooLong);
        }
        
        // Validate discount value (required, >= 0)
        if (request.getDiscountValue() == null || request.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidDiscountValue);
        }
        
        // Validate percentage constraint (if percentage, must be <= 100)
        if (request.getIsPercent() != null && request.getIsPercent() && 
            request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidPercentageValue);
        }
        
        // Validate promo code (required, length > 0, max 100 chars)
        if (request.getPromoCode() == null || request.getPromoCode().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidPromoCode);
        }
        if (request.getPromoCode().trim().length() > 100) {
            throw new BadRequestException(ErrorMessages.PromoErrorMessages.PromoCodeTooLong);
        }
        
        // Validate client ID (required, > 0)
        if (request.getClientId() == null || request.getClientId() <= 0) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidId);
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
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }
    
    /**
     * Sets fields from the request model.
     * 
     * @param request The PromoRequestModel to extract fields from
     */
    private void setFieldsFromRequest(PromoRequestModel request) {
        this.description = request.getDescription().trim();
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : false;
        this.isPercent = request.getIsPercent() != null ? request.getIsPercent() : false;
        this.discountValue = request.getDiscountValue();
        this.promoCode = request.getPromoCode().trim().toUpperCase(); // Standardize to uppercase
        this.clientId = request.getClientId();
    }
    
    /**
     * Calculates the discount amount for a given total.
     * 
     * @param total The total amount to apply discount to
     * @return The discount amount
     */
    public BigDecimal calculateDiscount(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (this.isPercent) {
            return total.multiply(this.discountValue).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            return this.discountValue.min(total); // Can't discount more than the total
        }
    }
}