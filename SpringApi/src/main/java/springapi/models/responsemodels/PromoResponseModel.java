package com.example.springapi.models.responsemodels;

import com.example.springapi.models.databasemodels.Promo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for Promo operations.
 *
 * <p>This model contains all the fields returned when retrieving promo information. It includes
 * related entities and calculated fields for the UI.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PromoResponseModel {

  private Long promoId;
  private String description;
  private Boolean isDeleted;
  private Boolean isPercent;
  private BigDecimal discountValue;
  private String promoCode;
  private Long clientId;
  private LocalDateTime createdAt;
  private String createdUser;
  private LocalDateTime updatedAt;
  private String modifiedUser;
  private String notes;
  private LocalDate startDate;
  private LocalDate expiryDate;

  // Additional computed fields
  private String discountDisplay;
  private Boolean isActive;
  private String promoType;

  /**
   * Constructor to create response model from database entity.
   *
   * @param promo The Promo entity
   */
  public PromoResponseModel(Promo promo) {
    if (promo != null) {
      this.promoId = promo.getPromoId();
      this.description = promo.getDescription();
      this.isDeleted = promo.getIsDeleted();
      this.isPercent = promo.getIsPercent();
      this.discountValue = promo.getDiscountValue();
      this.promoCode = promo.getPromoCode();
      this.clientId = promo.getClientId();
      this.createdAt = promo.getCreatedAt();
      this.createdUser = promo.getCreatedUser();
      this.updatedAt = promo.getUpdatedAt();
      this.modifiedUser = promo.getModifiedUser();
      this.notes = promo.getNotes();
      this.startDate = promo.getStartDate();
      this.expiryDate = promo.getExpiryDate();

      // Compute additional fields
      this.isActive = !this.isDeleted;
      this.promoType = this.isPercent ? "Percentage" : "Fixed Amount";
      this.discountDisplay = buildDiscountDisplay();
    }
  }

  /**
   * Builds a formatted discount display string.
   *
   * @return Formatted discount string
   */
  private String buildDiscountDisplay() {
    if (this.discountValue != null) {
      if (this.isPercent) {
        return this.discountValue + "%";
      } else {
        return "$" + this.discountValue;
      }
    }
    return "No discount";
  }
}
