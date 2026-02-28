package com.example.springapi.models.databasemodels;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.models.requestmodels.PackageRequestModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the Package table.
 *
 * <p>This entity represents physical package dimensions and pricing information.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Package`")
public class Package {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "packageId", nullable = false)
  private Long packageId;

  @Column(name = "packageName", nullable = false)
  private String packageName;

  @Column(name = "length", nullable = false)
  private Integer length;

  @Column(name = "breadth", nullable = false)
  private Integer breadth;

  @Column(name = "height", nullable = false)
  private Integer height;

  @Column(name = "maxWeight", nullable = false, precision = 8, scale = 2)
  private BigDecimal maxWeight;

  @Column(name = "standardCapacity", nullable = false)
  private Integer standardCapacity;

  @Column(name = "pricePerUnit", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerUnit;

  @Column(name = "packageType", nullable = false, length = 50)
  private String packageType;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted;

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

  @Column(name = "notes")
  private String notes;

  /** Default constructor. */
  public Package() {}

  /**
   * Constructor for creating a new package.
   *
   * @param request The PackageRequestModel containing the package data
   * @param createdUser The user creating the package
   */
  public Package(PackageRequestModel request, String createdUser, long clientId) {
    validateRequest(request);
    validateUser(createdUser);

    setFieldsFromRequest(request);
    this.createdUser = createdUser;
    this.clientId = clientId;
    this.modifiedUser = createdUser;
  }

  /**
   * Constructor for updating an existing package.
   *
   * @param request The PackageRequestModel containing the updated package data
   * @param modifiedUser The user modifying the package
   * @param existingPackage The existing package entity
   */
  public Package(PackageRequestModel request, String modifiedUser, Package existingPackage) {
    validateRequest(request);
    validateUser(modifiedUser);

    this.packageId = existingPackage.getPackageId();
    this.clientId = existingPackage.getClientId();
    this.createdUser = existingPackage.getCreatedUser();
    this.createdAt = existingPackage.getCreatedAt();

    setFieldsFromRequest(request);
    this.modifiedUser = modifiedUser;
  }

  /**
   * Validates the request model.
   *
   * @param request The PackageRequestModel to validate
   * @throws BadRequestException if validation fails
   */
  private void validateRequest(PackageRequestModel request) {
    if (request == null) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_REQUEST);
    }
    if (request.getPackageName() == null || request.getPackageName().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_PACKAGE_NAME);
    }
    if (request.getPackageName().length() > 255) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_PACKAGE_NAME);
    }
    if (request.getLength() == null || request.getLength() <= 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_LENGTH);
    }
    if (request.getBreadth() == null || request.getBreadth() <= 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_BREADTH);
    }
    if (request.getHeight() == null || request.getHeight() <= 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_HEIGHT);
    }
    if (request.getMaxWeight() == null || request.getMaxWeight().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_MAX_WEIGHT);
    }
    if (request.getStandardCapacity() == null || request.getStandardCapacity() <= 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_STANDARD_CAPACITY);
    }
    if (request.getPricePerUnit() == null
        || request.getPricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_PRICE_PER_UNIT);
    }
    if (request.getPackageType() == null || request.getPackageType().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_PACKAGE_TYPE);
    }
    // Note: clientId is passed as a constructor parameter from security context,
    // not from request body
    // Note: Packages don't require an address - they are stored at pickup locations
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
   * Sets fields from the request model.
   *
   * @param request The PackageRequestModel to extract fields from
   */
  private void setFieldsFromRequest(PackageRequestModel request) {
    this.packageName = request.getPackageName().trim();
    this.length = request.getLength();
    this.breadth = request.getBreadth();
    this.height = request.getHeight();
    this.maxWeight = request.getMaxWeight();
    this.standardCapacity = request.getStandardCapacity();
    this.pricePerUnit = request.getPricePerUnit();
    this.packageType = request.getPackageType().trim();
    this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
    this.notes = request.getNotes();
  }
}
