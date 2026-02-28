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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;

/**
 * JPA Entity for the ShipmentProduct table.
 *
 * <p>This entity links products and quantities to each shipment (aggregate level). Tracks total
 * quantity per product in shipment with custom pricing.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ShipmentProduct")
public class ShipmentProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "shipmentProductId", nullable = false)
  private Long shipmentProductId;

  // Foreign Keys
  @Column(name = "shipmentId", nullable = false)
  private Long shipmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipmentId", insertable = false, updatable = false)
  private Shipment shipment;

  @Column(name = "productId", nullable = false)
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productId", insertable = false, updatable = false)
  private Product product;

  // Product Allocation Details
  @Column(name = "allocatedQuantity", nullable = false)
  private Integer allocatedQuantity;

  @Column(name = "allocatedPrice", nullable = false, precision = 15, scale = 2)
  private BigDecimal allocatedPrice;

  // Standard Fields
  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Default constructor. */
  public ShipmentProduct() {}

  /**
   * Constructor for creating a new shipment product from ShipmentProductData.
   *
   * @param shipmentId The shipment ID (not in ShipmentProductData)
   * @param productData The shipment product data from request model
   */
  public ShipmentProduct(
      Long shipmentId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentProductData productData) {
    validateRequest(shipmentId, productData);

    this.shipmentId = shipmentId;
    this.productId = productData.getProductId();
    this.allocatedQuantity = productData.getAllocatedQuantity();
    this.allocatedPrice = productData.getAllocatedPrice();
  }

  /**
   * Constructor for updating an existing shipment product from ShipmentProductData.
   *
   * @param shipmentId The shipment ID (not in ShipmentProductData)
   * @param productData The shipment product data from request model
   * @param existingShipmentProduct The existing shipment product entity
   */
  public ShipmentProduct(
      Long shipmentId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentProductData productData,
      ShipmentProduct existingShipmentProduct) {
    validateRequest(shipmentId, productData);

    this.shipmentProductId = existingShipmentProduct.getShipmentProductId();
    this.createdAt = existingShipmentProduct.getCreatedAt();

    this.shipmentId = shipmentId;
    this.productId = productData.getProductId();
    this.allocatedQuantity = productData.getAllocatedQuantity();
    this.allocatedPrice = productData.getAllocatedPrice();
  }

  /**
   * Validates all request data for ShipmentProduct creation/update.
   *
   * @param shipmentId The shipment ID
   * @param productData The shipment product data from request model
   */
  private void validateRequest(
      Long shipmentId,
      springapi.models.requestmodels.PurchaseOrderRequestModel.ShipmentProductData productData) {
    if (productData == null) {
      throw new BadRequestException(ErrorMessages.ShipmentProductErrorMessages.INVALID_REQUEST);
    }

    // Validate shipment ID
    if (shipmentId == null || shipmentId <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentProductErrorMessages.SHIPMENT_ID_REQUIRED);
    }

    // Validate product ID
    if (productData.getProductId() == null || productData.getProductId() <= 0) {
      throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_ID);
    }

    // Validate quantity
    if (productData.getAllocatedQuantity() == null || productData.getAllocatedQuantity() <= 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentProductErrorMessages.ALLOCATED_QUANTITY_REQUIRED);
    }

    // Validate price
    if (productData.getAllocatedPrice() == null) {
      throw new BadRequestException(
          ErrorMessages.ShipmentProductErrorMessages.ALLOCATED_PRICE_REQUIRED);
    }
    if (productData.getAllocatedPrice().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(
          ErrorMessages.ShipmentProductErrorMessages.ALLOCATED_PRICE_INVALID);
    }
  }
}
