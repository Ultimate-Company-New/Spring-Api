package com.example.springapi.models.databasemodels;

import com.example.springapi.ErrorMessages;
import com.example.springapi.constants.ProductConditionConstants;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.models.requestmodels.ProductRequestModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the Product table.
 *
 * <p>This entity represents a product in the system with detailed information including pricing,
 * dimensions, and category associations.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Product`")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "productId", nullable = false)
  private Long productId;

  @Column(name = "title", nullable = false, length = 500)
  private String title;

  @Column(name = "descriptionHtml", nullable = false, columnDefinition = "TEXT")
  private String descriptionHtml;

  @Column(name = "length", precision = 8, scale = 2)
  private BigDecimal length;

  @Column(name = "brand", nullable = false)
  private String brand;

  @Column(name = "color", length = 100)
  private String color;

  @Column(name = "colorLabel", nullable = false, length = 100)
  private String colorLabel;

  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted;

  @Column(name = "`condition`", nullable = false, length = 100)
  private String condition;

  @Column(name = "countryOfManufacture", nullable = false, length = 100)
  private String countryOfManufacture;

  @Column(name = "model")
  private String model;

  @Column(name = "itemModified", nullable = false)
  private Boolean itemModified;

  @Column(name = "upc", length = 50)
  private String upc;

  @Column(name = "modificationHtml", columnDefinition = "TEXT")
  private String modificationHtml;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(name = "discount", nullable = false, precision = 10, scale = 2)
  private BigDecimal discount;

  @Column(name = "isDiscountPercent", nullable = false)
  private Boolean isDiscountPercent;

  @Column(name = "returnWindowDays", nullable = false)
  private Integer
      returnWindowDays; // Number of days from delivery within which returns are allowed (0 = no

  // returns)

  @Column(name = "breadth", precision = 8, scale = 2)
  private BigDecimal breadth;

  @Column(name = "height", precision = 8, scale = 2)
  private BigDecimal height;

  @Column(name = "weightKgs", precision = 8, scale = 3)
  private BigDecimal weightKgs;

  @Column(name = "categoryId", nullable = false)
  private Long categoryId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoryId", insertable = false, updatable = false)
  private ProductCategory category;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

  // Product Images (Required - stored on ImgBB)
  @Column(name = "mainImageUrl", nullable = false, length = 500)
  private String mainImageUrl;

  @Column(name = "mainImageDeleteHash", nullable = false, length = 500)
  private String mainImageDeleteHash;

  @Column(name = "topImageUrl", nullable = false, length = 500)
  private String topImageUrl;

  @Column(name = "topImageDeleteHash", nullable = false, length = 500)
  private String topImageDeleteHash;

  @Column(name = "bottomImageUrl", nullable = false, length = 500)
  private String bottomImageUrl;

  @Column(name = "bottomImageDeleteHash", nullable = false, length = 500)
  private String bottomImageDeleteHash;

  @Column(name = "frontImageUrl", nullable = false, length = 500)
  private String frontImageUrl;

  @Column(name = "frontImageDeleteHash", nullable = false, length = 500)
  private String frontImageDeleteHash;

  @Column(name = "backImageUrl", nullable = false, length = 500)
  private String backImageUrl;

  @Column(name = "backImageDeleteHash", nullable = false, length = 500)
  private String backImageDeleteHash;

  @Column(name = "rightImageUrl", nullable = false, length = 500)
  private String rightImageUrl;

  @Column(name = "rightImageDeleteHash", nullable = false, length = 500)
  private String rightImageDeleteHash;

  @Column(name = "leftImageUrl", nullable = false, length = 500)
  private String leftImageUrl;

  @Column(name = "leftImageDeleteHash", nullable = false, length = 500)
  private String leftImageDeleteHash;

  @Column(name = "detailsImageUrl", nullable = false, length = 500)
  private String detailsImageUrl;

  @Column(name = "detailsImageDeleteHash", nullable = false, length = 500)
  private String detailsImageDeleteHash;

  // Product Images (Optional - stored on ImgBB)
  @Column(name = "defectImageUrl", length = 500)
  private String defectImageUrl;

  @Column(name = "defectImageDeleteHash", length = 500)
  private String defectImageDeleteHash;

  @Column(name = "additionalImage1Url", length = 500)
  private String additionalImage1Url;

  @Column(name = "additionalImage1DeleteHash", length = 500)
  private String additionalImage1DeleteHash;

  @Column(name = "additionalImage2Url", length = 500)
  private String additionalImage2Url;

  @Column(name = "additionalImage2DeleteHash", length = 500)
  private String additionalImage2DeleteHash;

  @Column(name = "additionalImage3Url", length = 500)
  private String additionalImage3Url;

  @Column(name = "additionalImage3DeleteHash", length = 500)
  private String additionalImage3DeleteHash;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "itemAvailableFrom", nullable = false)
  private LocalDateTime itemAvailableFrom;

  @Column(name = "itemAvailableFromTimezone", nullable = false, length = 100)
  private String itemAvailableFromTimezone;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
  private Set<ProductPickupLocationMapping> productPickupLocationMappings = new HashSet<>();

  /** Default constructor. */
  public Product() {}

  /**
   * Constructor for creating a new product.
   *
   * @param request The ProductRequestModel containing the product data
   * @param createdUser The user creating the product
   * @param clientId The client ID from the security context (not from request)
   */
  public Product(ProductRequestModel request, String createdUser, Long clientId) {
    validateRequest(request);
    validateUser(createdUser);
    validateClientId(clientId);

    setFieldsFromRequest(request);
    this.clientId = clientId;
    this.createdUser = createdUser;
    this.modifiedUser = createdUser;

    // Initialize required image fields with placeholder values
    // These will be updated when images are uploaded in
    // processAndUploadProductImages
    this.mainImageUrl = "";
    this.mainImageDeleteHash = "";
    this.topImageUrl = "";
    this.topImageDeleteHash = "";
    this.bottomImageUrl = "";
    this.bottomImageDeleteHash = "";
    this.frontImageUrl = "";
    this.frontImageDeleteHash = "";
    this.backImageUrl = "";
    this.backImageDeleteHash = "";
    this.rightImageUrl = "";
    this.rightImageDeleteHash = "";
    this.leftImageUrl = "";
    this.leftImageDeleteHash = "";
    this.detailsImageUrl = "";
    this.detailsImageDeleteHash = "";
  }

  /**
   * Constructor for updating an existing product.
   *
   * @param request The ProductRequestModel containing the updated product data
   * @param modifiedUser The user modifying the product
   * @param existingProduct The existing product entity (clientId is preserved from existing)
   */
  public Product(ProductRequestModel request, String modifiedUser, Product existingProduct) {
    validateRequest(request);
    validateUser(modifiedUser);

    this.productId = existingProduct.getProductId();
    this.createdUser = existingProduct.getCreatedUser();
    this.createdAt = existingProduct.getCreatedAt();
    this.clientId = existingProduct.getClientId(); // Preserve clientId from existing product

    setFieldsFromRequest(request);
    this.modifiedUser = modifiedUser;

    // Preserve existing image URLs and delete hashes so update workflows can detect
    // unchanged images
    this.mainImageUrl = existingProduct.getMainImageUrl();
    this.mainImageDeleteHash = existingProduct.getMainImageDeleteHash();
    this.topImageUrl = existingProduct.getTopImageUrl();
    this.topImageDeleteHash = existingProduct.getTopImageDeleteHash();
    this.bottomImageUrl = existingProduct.getBottomImageUrl();
    this.bottomImageDeleteHash = existingProduct.getBottomImageDeleteHash();
    this.frontImageUrl = existingProduct.getFrontImageUrl();
    this.frontImageDeleteHash = existingProduct.getFrontImageDeleteHash();
    this.backImageUrl = existingProduct.getBackImageUrl();
    this.backImageDeleteHash = existingProduct.getBackImageDeleteHash();
    this.rightImageUrl = existingProduct.getRightImageUrl();
    this.rightImageDeleteHash = existingProduct.getRightImageDeleteHash();
    this.leftImageUrl = existingProduct.getLeftImageUrl();
    this.leftImageDeleteHash = existingProduct.getLeftImageDeleteHash();
    this.detailsImageUrl = existingProduct.getDetailsImageUrl();
    this.detailsImageDeleteHash = existingProduct.getDetailsImageDeleteHash();
    this.defectImageUrl = existingProduct.getDefectImageUrl();
    this.defectImageDeleteHash = existingProduct.getDefectImageDeleteHash();
    this.additionalImage1Url = existingProduct.getAdditionalImage1Url();
    this.additionalImage1DeleteHash = existingProduct.getAdditionalImage1DeleteHash();
    this.additionalImage2Url = existingProduct.getAdditionalImage2Url();
    this.additionalImage2DeleteHash = existingProduct.getAdditionalImage2DeleteHash();
    this.additionalImage3Url = existingProduct.getAdditionalImage3Url();
    this.additionalImage3DeleteHash = existingProduct.getAdditionalImage3DeleteHash();
  }

  /**
   * Validates the request model.
   *
   * @param request The ProductRequestModel to validate
   * @throws BadRequestException if validation fails
   */
  private void validateRequest(ProductRequestModel request) {
    if (request == null) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_REQUEST);
    }
    if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_TITLE);
    }
    if (request.getDescriptionHtml() == null || request.getDescriptionHtml().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_DESCRIPTION);
    }
    if (request.getBrand() == null || request.getBrand().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_BRAND);
    }
    if (request.getColorLabel() == null || request.getColorLabel().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_COLOR_LABEL);
    }
    if (request.getCondition() == null || request.getCondition().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_CONDITION);
    }
    if (!ProductConditionConstants.isValidCondition(request.getCondition())) {
      throw new BadRequestException(
          String.format(
              ErrorMessages.ProductErrorMessages.INVALID_CONDITION_VALUE_FORMAT,
              ProductConditionConstants.getValidConditionsList()));
    }
    if (request.getCountryOfManufacture() == null
        || request.getCountryOfManufacture().trim().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.ProductErrorMessages.INVALID_COUNTRY_OF_MANUFACTURE);
    }
    if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_PRICE);
    }
    if (request.getCategoryId() == null) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_CATEGORY_ID);
    }
    if (request.getWeightKgs() != null && request.getWeightKgs().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_WEIGHT);
    }
    if (request.getLength() != null && request.getLength().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_LENGTH);
    }
    if (request.getBreadth() != null && request.getBreadth().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_BREADTH);
    }
    if (request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_HEIGHT);
    }
    // Note: clientId is not validated from request - it comes from security context
    // via constructor parameter
    if (request.getPickupLocationQuantities() == null
        || request.getPickupLocationQuantities().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.ProductErrorMessages.AT_LEAST_ONE_PICKUP_LOCATION_REQUIRED);
    }
  }

  /**
   * Validates the user parameter.
   *
   * @param user The user to validate
   * @throws BadRequestException if validation fails
   */
  private void validateUser(String user) {
    if (user == null || user.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
    }
  }

  /**
   * Validates the clientId parameter.
   *
   * @param clientId The client ID to validate
   * @throws BadRequestException if validation fails
   */
  private void validateClientId(Long clientId) {
    if (clientId == null) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_CLIENT_ID);
    }
  }

  /**
   * Sets fields from the request model.
   *
   * @param request The ProductRequestModel to extract fields from
   */
  private void setFieldsFromRequest(ProductRequestModel request) {
    this.title = request.getTitle().trim();
    this.descriptionHtml = request.getDescriptionHtml().trim();
    this.length = request.getLength();
    this.brand = request.getBrand().trim();
    this.color = request.getColor() != null ? request.getColor().trim() : null;
    this.colorLabel = request.getColorLabel().trim();
    this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
    this.condition = ProductConditionConstants.normalizeCondition(request.getCondition());
    this.countryOfManufacture = request.getCountryOfManufacture().trim();
    this.model = request.getModel() != null ? request.getModel().trim() : null;
    this.itemModified =
        request.getItemModified() != null ? request.getItemModified() : Boolean.FALSE;
    this.upc = request.getUpc() != null ? request.getUpc().trim() : null;
    this.modificationHtml =
        request.getModificationHtml() != null ? request.getModificationHtml().trim() : null;
    this.price = request.getPrice();
    this.discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
    this.isDiscountPercent =
        request.getIsDiscountPercent() != null ? request.getIsDiscountPercent() : Boolean.FALSE;
    this.returnWindowDays = request.getReturnWindowDays();
    this.breadth = request.getBreadth();
    this.height = request.getHeight();
    this.weightKgs = request.getWeightKgs();
    this.categoryId = request.getCategoryId();
    // Note: clientId is set in constructor from security context, not from request
    this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    this.itemAvailableFrom =
        request.getItemAvailableFrom() != null
            ? request.getItemAvailableFrom()
            : LocalDateTime.now();
    this.itemAvailableFromTimezone =
        request.getItemAvailableFromTimezone() != null
            ? request.getItemAvailableFromTimezone().trim()
            : "UTC";
  }
}
