package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * Request model for Promo operations.
 * 
 * This model contains all the fields required for creating or updating a promo.
 * It includes validation constraints and business logic requirements.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PromoRequestModel extends PaginationBaseRequestModel {
    
    private Long promoId;
    private String description;
    private Boolean isDeleted;
    private Boolean isPercent;
    private BigDecimal discountValue;
    private String promoCode;
    private Long clientId;
    private String notes;
}