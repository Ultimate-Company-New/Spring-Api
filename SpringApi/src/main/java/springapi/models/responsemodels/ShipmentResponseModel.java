package springapi.models.responsemodels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import springapi.models.databasemodels.ReturnShipment;
import springapi.models.databasemodels.Shipment;
import springapi.models.databasemodels.ShipmentPackage;
import springapi.models.databasemodels.ShipmentPackageProduct;
import springapi.models.databasemodels.ShipmentProduct;

/**
 * Response model for Shipment operations.
 *
 * <p>This model is used for returning shipment information in API responses. Includes all related
 * entities: Products, Packages, Pickup Location, Order Summary, and Purchase Order details.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ShipmentResponseModel {

  // Shipment Basic Fields
  private Long shipmentId;
  private Long orderSummaryId;
  private Long pickupLocationId;
  private BigDecimal totalWeightKgs;
  private Integer totalQuantity;
  private LocalDateTime expectedDeliveryDate;
  private LocalDateTime deliveredDate; // Actual delivery date

  // Cost Breakdown
  private BigDecimal packagingCost;
  private BigDecimal shippingCost;
  private BigDecimal totalCost;

  // Courier Selection
  private Long selectedCourierCompanyId;
  private String selectedCourierName;
  private BigDecimal selectedCourierRate;
  private BigDecimal selectedCourierMinWeight;
  private String selectedCourierMetadata;

  // ShipRocket Details
  private String shipRocketOrderId;
  private Long shipRocketShipmentId;
  private String shipRocketAwbCode;
  private String shipRocketTrackingId;
  private String shipRocketStatus;
  private String shipRocketManifestUrl;
  private String shipRocketInvoiceUrl;
  private String shipRocketLabelUrl;
  private String shipRocketFullResponse; // Complete ShipRocket order details as JSON
  private String shipRocketAwbMetadata; // AWB assignment response as JSON
  private String shipRocketPickupMetadata; // Pickup generation response as JSON
  private String shipRocketGeneratedManifestUrl; // Manifest URL from manifest generation API
  private String shipRocketGeneratedLabelUrl; // Label URL from label generation API
  private String shipRocketGeneratedInvoiceUrl; // Invoice URL from invoice generation API
  private String shipRocketTrackingMetadata; // Tracking API response as JSON

  // Audit Fields
  private Long clientId;
  private String createdUser;
  private String modifiedUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Related Entities
  private PickupLocationResponseModel pickupLocation;
  private AddressResponseModel deliveryAddress; // Delivery address from OrderSummary
  private Long purchaseOrderId;
  private UserResponseModel createdByUser;
  private UserResponseModel modifiedByUser;

  // Products in this shipment
  private List<ProductResponseModel> products = new ArrayList<>();

  // Packages in this shipment
  private List<PackageResponseModel> packages = new ArrayList<>();

  // Return shipments for this shipment
  private List<ReturnShipmentResponseModel> returnShipments = new ArrayList<>();

  /** Default constructor. */
  public ShipmentResponseModel() {}

  /**
   * Constructor that creates a response model from a Shipment entity.
   *
   * @param shipment The Shipment entity to convert
   */
  public ShipmentResponseModel(Shipment shipment) {
    if (shipment == null) {
      return;
    }

    // Basic fields
    this.shipmentId = shipment.getShipmentId();
    this.orderSummaryId = shipment.getOrderSummaryId();
    this.pickupLocationId = shipment.getPickupLocationId();
    this.totalWeightKgs = shipment.getTotalWeightKgs();
    this.totalQuantity = shipment.getTotalQuantity();
    this.expectedDeliveryDate = shipment.getExpectedDeliveryDate();
    this.deliveredDate = shipment.getDeliveredDate();

    // Cost breakdown
    this.packagingCost = shipment.getPackagingCost();
    this.shippingCost = shipment.getShippingCost();
    this.totalCost = shipment.getTotalCost();

    // Courier selection
    this.selectedCourierCompanyId = shipment.getSelectedCourierCompanyId();
    this.selectedCourierName = shipment.getSelectedCourierName();
    this.selectedCourierRate = shipment.getSelectedCourierRate();
    this.selectedCourierMinWeight = shipment.getSelectedCourierMinWeight();
    this.selectedCourierMetadata = shipment.getSelectedCourierMetadata();

    // ShipRocket details
    this.shipRocketOrderId = shipment.getShipRocketOrderId();
    this.shipRocketShipmentId = shipment.getShipRocketShipmentId();
    this.shipRocketAwbCode = shipment.getShipRocketAwbCode();
    this.shipRocketTrackingId = shipment.getShipRocketTrackingId();
    this.shipRocketStatus = shipment.getShipRocketStatus();
    this.shipRocketManifestUrl = shipment.getShipRocketManifestUrl();
    this.shipRocketInvoiceUrl = shipment.getShipRocketInvoiceUrl();
    this.shipRocketLabelUrl = shipment.getShipRocketLabelUrl();
    this.shipRocketFullResponse = shipment.getShipRocketFullResponse();
    this.shipRocketAwbMetadata = shipment.getShipRocketAwbMetadata();
    this.shipRocketPickupMetadata = shipment.getShipRocketPickupMetadata();
    this.shipRocketGeneratedManifestUrl = shipment.getShipRocketGeneratedManifestUrl();
    this.shipRocketGeneratedLabelUrl = shipment.getShipRocketGeneratedLabelUrl();
    this.shipRocketGeneratedInvoiceUrl = shipment.getShipRocketGeneratedInvoiceUrl();
    this.shipRocketTrackingMetadata = shipment.getShipRocketTrackingMetadata();

    // Audit fields
    this.clientId = shipment.getClientId();
    this.createdUser = shipment.getCreatedUser();
    this.modifiedUser = shipment.getModifiedUser();
    this.createdAt = shipment.getCreatedAt();
    this.updatedAt = shipment.getUpdatedAt();

    // Extract pickup location info
    if (Hibernate.isInitialized(shipment.getPickupLocation())
        && shipment.getPickupLocation() != null) {
      this.pickupLocation = new PickupLocationResponseModel(shipment.getPickupLocation());
    }

    // Extract created by user info
    if (Hibernate.isInitialized(shipment.getCreatedByUser())
        && shipment.getCreatedByUser() != null) {
      this.createdByUser = new UserResponseModel(shipment.getCreatedByUser());
    }

    // Extract modified by user info
    if (Hibernate.isInitialized(shipment.getModifiedByUser())
        && shipment.getModifiedByUser() != null) {
      this.modifiedByUser = new UserResponseModel(shipment.getModifiedByUser());
    }

    // Extract shipment products
    if (Hibernate.isInitialized(shipment.getShipmentProducts())
        && shipment.getShipmentProducts() != null) {
      for (ShipmentProduct sp : shipment.getShipmentProducts()) {
        if (Hibernate.isInitialized(sp.getProduct()) && sp.getProduct() != null) {
          this.products.add(new ProductResponseModel(sp.getProduct(), sp));
        }
      }
    }

    // Extract shipment packages
    if (Hibernate.isInitialized(shipment.getShipmentPackages())
        && shipment.getShipmentPackages() != null) {
      for (ShipmentPackage pkg : shipment.getShipmentPackages()) {
        if (Hibernate.isInitialized(pkg.getPackageInfo()) && pkg.getPackageInfo() != null) {
          this.packages.add(new PackageResponseModel(pkg.getPackageInfo(), pkg));
        }
      }
    }

    // Extract purchase order ID and delivery address from OrderSummary
    if (Hibernate.isInitialized(shipment.getOrderSummary()) && shipment.getOrderSummary() != null) {
      springapi.models.databasemodels.OrderSummary orderSummary = shipment.getOrderSummary();
      if (orderSummary.getEntityType() != null
          && orderSummary
              .getEntityType()
              .equals(
                  springapi.models.databasemodels.OrderSummary.EntityType.PURCHASE_ORDER.getValue())
          && orderSummary.getEntityId() != null) {
        this.purchaseOrderId = orderSummary.getEntityId();
      }

      // Extract delivery address from OrderSummary
      if (Hibernate.isInitialized(orderSummary.getEntityAddress())
          && orderSummary.getEntityAddress() != null) {
        this.deliveryAddress = new AddressResponseModel(orderSummary.getEntityAddress());
      }
    }

    // Extract return shipments
    if (Hibernate.isInitialized(shipment.getReturnShipments())
        && shipment.getReturnShipments() != null) {
      for (ReturnShipment rs : shipment.getReturnShipments()) {
        if (!rs.getIsDeleted()) {
          this.returnShipments.add(new ReturnShipmentResponseModel(rs));
        }
      }
    }
  }

  /**
   * Sets purchase order ID from the order summary's related purchase order. This should be called.
   * after the entity is fetched with the PO data.
   *
   * @param po The purchase order to extract ID from
   */
  public void setPurchaseOrderFromEntity(springapi.models.databasemodels.PurchaseOrder po) {
    if (po != null) {
      this.purchaseOrderId = po.getPurchaseOrderId();
    }
  }

  /** Product response data within a package. */
  @Getter
  @Setter
  public static class PackageProductResponseData {
    private Long shipmentPackageProductId;
    private Long productId;
    private ProductResponseModel product;
    private Integer quantity;

    public PackageProductResponseData() {}

    /** Executes package product response data. */
    public PackageProductResponseData(ShipmentPackageProduct packageProduct) {
      if (packageProduct != null) {
        this.shipmentPackageProductId = packageProduct.getShipmentPackageProductId();
        this.productId = packageProduct.getProductId();
        this.quantity = packageProduct.getQuantity();

        // Extract Product
        if (Hibernate.isInitialized(packageProduct.getProduct())
            && packageProduct.getProduct() != null) {
          this.product = new ProductResponseModel(packageProduct.getProduct());
        }
      }
    }
  }
}
