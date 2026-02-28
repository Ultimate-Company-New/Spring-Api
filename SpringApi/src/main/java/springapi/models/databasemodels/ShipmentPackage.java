package com.example.springapi.models.databasemodels;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import jakarta.persistence.CascadeType;
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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA Entity for the ShipmentPackage table.
 *
 * <p>This entity stores which packages are used in each shipment.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ShipmentPackage")
public class ShipmentPackage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "shipmentPackageId", nullable = false)
  private Long shipmentPackageId;

  // Foreign Keys
  @Column(name = "shipmentId", nullable = false)
  private Long shipmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipmentId", insertable = false, updatable = false)
  private Shipment shipment;

  @Column(name = "packageId", nullable = false)
  private Long packageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "packageId", insertable = false, updatable = false)
  private Package packageInfo;

  // Package Usage Details
  @Column(name = "quantityUsed", nullable = false)
  private Integer quantityUsed;

  @Column(name = "totalCost", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalCost;

  // Standard Fields
  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // Relationships
  @OneToMany(
      mappedBy = "shipmentPackage",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<ShipmentPackageProduct> shipmentPackageProducts = new ArrayList<>();

  /** Default constructor. */
  public ShipmentPackage() {}

  /**
   * Constructor for creating a new shipment package from ShipmentPackageData.
   *
   * @param shipmentId The shipment ID (not in ShipmentPackageData)
   * @param packageData The shipment package data from request model
   */
  public ShipmentPackage(
      Long shipmentId,
      com.example.springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentPackageData
          packageData) {
    validateRequest(shipmentId, packageData);

    this.shipmentId = shipmentId;
    this.packageId = packageData.getPackageId();
    this.quantityUsed = packageData.getQuantityUsed();
    this.totalCost = packageData.getTotalCost();
  }

  /**
   * Constructor for updating an existing shipment package from ShipmentPackageData.
   *
   * @param shipmentId The shipment ID (not in ShipmentPackageData)
   * @param packageData The shipment package data from request model
   * @param existingShipmentPackage The existing shipment package entity
   */
  public ShipmentPackage(
      Long shipmentId,
      com.example.springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentPackageData
          packageData,
      ShipmentPackage existingShipmentPackage) {
    validateRequest(shipmentId, packageData);

    this.shipmentPackageId = existingShipmentPackage.getShipmentPackageId();
    this.createdAt = existingShipmentPackage.getCreatedAt();

    this.shipmentId = shipmentId;
    this.packageId = packageData.getPackageId();
    this.quantityUsed = packageData.getQuantityUsed();
    this.totalCost = packageData.getTotalCost();
  }

  /**
   * Validates all request data for ShipmentPackage creation/update.
   *
   * @param shipmentId The shipment ID
   * @param packageData The shipment package data from request model
   */
  private void validateRequest(
      Long shipmentId,
      com.example.springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentPackageData
          packageData) {
    if (packageData == null) {
      throw new BadRequestException(ErrorMessages.ShipmentPackageErrorMessages.INVALID_REQUEST);
    }

    // Validate shipment ID
    if (shipmentId == null || shipmentId <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentPackageErrorMessages.SHIPMENT_ID_REQUIRED);
    }

    // Validate package ID
    if (packageData.getPackageId() == null || packageData.getPackageId() <= 0) {
      throw new BadRequestException(ErrorMessages.PackageErrorMessages.INVALID_ID);
    }

    // Validate quantity used
    if (packageData.getQuantityUsed() == null || packageData.getQuantityUsed() <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentPackageErrorMessages.QUANTITY_USED_REQUIRED);
    }

    // Validate total cost
    if (packageData.getTotalCost() == null) {
      throw new BadRequestException(ErrorMessages.ShipmentPackageErrorMessages.TOTAL_COST_REQUIRED);
    }
    if (packageData.getTotalCost().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.ShipmentPackageErrorMessages.TOTAL_COST_INVALID);
    }
  }
}
