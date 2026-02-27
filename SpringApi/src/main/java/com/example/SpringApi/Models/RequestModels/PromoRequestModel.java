package com.example.SpringApi.Models.RequestModels;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for Promo operations.
 *
 * <p>This model contains all the fields required for creating or updating a promo. It includes
 * validation constraints and business logic requirements.
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
  private LocalDate startDate;
  private LocalDate expiryDate;
}

