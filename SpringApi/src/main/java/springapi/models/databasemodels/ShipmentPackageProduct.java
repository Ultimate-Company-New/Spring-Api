package springapi.models.databasemodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;

/**
 * JPA Entity for the ShipmentPackageProduct table.
 *
 * <p>This entity stores which products and quantities go into each specific package within a
 * shipment. This provides detailed product-to-package mapping within shipments.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ShipmentPackageProduct")
public class ShipmentPackageProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "shipmentPackageProductId", nullable = false)
  private Long shipmentPackageProductId;

  // Foreign Keys
  @Column(name = "shipmentPackageId", nullable = false)
  private Long shipmentPackageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipmentPackageId", insertable = false, updatable = false)
  private ShipmentPackage shipmentPackage;

  @Column(name = "productId", nullable = false)
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productId", insertable = false, updatable = false)
  private Product product;

  // Product Allocation in Package
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  // Standard Fields
  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Default constructor. */
  public ShipmentPackageProduct() {}

  /**
   * Constructor for creating a new shipment package product from PackageProductData.
   *
   * @param shipmentPackageId The shipment package ID (not in PackageProductData)
   * @param productData The package product data from request model
   */
  public ShipmentPackageProduct(
      Long shipmentPackageId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.PackageProductData productData) {
    validateRequest(shipmentPackageId, productData);

    this.shipmentPackageId = shipmentPackageId;
    this.productId = productData.getProductId();
    this.quantity = productData.getQuantity();
  }

  /**
   * Constructor for updating an existing shipment package product from PackageProductData.
   *
   * @param shipmentPackageId The shipment package ID (not in PackageProductData)
   * @param productData The package product data from request model
   * @param existingShipmentPackageProduct The existing shipment package product entity
   */
  public ShipmentPackageProduct(
      Long shipmentPackageId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.PackageProductData productData,
      ShipmentPackageProduct existingShipmentPackageProduct) {
    validateRequest(shipmentPackageId, productData);

    this.shipmentPackageProductId = existingShipmentPackageProduct.getShipmentPackageProductId();
    this.createdAt = existingShipmentPackageProduct.getCreatedAt();

    this.shipmentPackageId = shipmentPackageId;
    this.productId = productData.getProductId();
    this.quantity = productData.getQuantity();
  }

  /**
   * Validates all request data for ShipmentPackageProduct creation/update.
   *
   * @param shipmentPackageId The shipment package ID
   * @param productData The package product data from request model
   */
  private void validateRequest(
      Long shipmentPackageId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.PackageProductData productData) {
    if (productData == null) {
      throw new BadRequestException(
          ErrorMessages.ShipmentPackageProductErrorMessages.INVALID_REQUEST);
    }

    // Validate shipment package ID
    if (shipmentPackageId == null || shipmentPackageId <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentPackageProductErrorMessages.SHIPMENT_PACKAGE_ID_REQUIRED);
    }

    // Validate product ID
    if (productData.getProductId() == null || productData.getProductId() <= 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_ID);
    }

    // Validate quantity
    if (productData.getQuantity() == null || productData.getQuantity() <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentPackageProductErrorMessages.QUANTITY_REQUIRED);
    }
  }
}
