package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageProduct;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response model for PurchaseOrder operations.
 * 
 * This model is used for returning purchase order information in API responses.
 * Includes all related entities: OrderSummary (financial breakdown and fulfillment details),
 * Shipments (with products, packages, and courier selections), Address, Products, 
 * Pickup Locations, and Attachments.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderResponseModel {
    // Purchase Order Basic Fields
    private Long purchaseOrderId;
    private String vendorNumber;
    private Boolean isDeleted;
    private String purchaseOrderReceipt;
    private String purchaseOrderStatus;
    private Long paymentId;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private Long assignedLeadId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Nested response models for related entities
    private LeadResponseModel lead;
    private UserResponseModel createdByUser;
    private UserResponseModel modifiedByUser;
    private UserResponseModel approvedByUser;
    private UserResponseModel rejectedByUser;
    
    // OrderSummary Data (Financial Breakdown and Fulfillment Details)
    private OrderSummaryResponseData orderSummary;
    
    // Shipment Data (List of shipments with products, packages, and courier selections)
    private List<ShipmentResponseData> shipments = new ArrayList<>();
    
    // Products (for backward compatibility - extracted from shipments)
    private List<PurchaseOrderProductItem> products;
    
    // Attachments (Map format: fileName -> base64 data for edit form, or List format for display)
    private Map<String, String> attachments; // fileName -> base64 data (for edit form)
    private List<ResourceResponseModel> attachmentsList; // Full resource details (for display)

    /**
     * Constructor that creates a response model from a PurchaseOrder entity.
     * Extracts and maps all related entities including:
     * - OrderSummary (financial breakdown and fulfillment details)
     * - Shipments (with products, packages, and courier selections)
     * - Address (delivery/shipping address from OrderSummary)
     * - Created By User (user who created the purchase order)
     * - Modified By User (user who last modified the purchase order)
     * - Products (extracted from shipments for backward compatibility)
     * - Attachments (resources/files)
     * 
     * @param purchaseOrder The PurchaseOrder entity to convert
     * @param orderSummary The OrderSummary entity (must be loaded with shipments)
     */
    public PurchaseOrderResponseModel(PurchaseOrder purchaseOrder, OrderSummary orderSummary) {
        if (purchaseOrder != null) {
            // Basic purchase order fields
            this.purchaseOrderId = purchaseOrder.getPurchaseOrderId();
            this.vendorNumber = purchaseOrder.getVendorNumber();
            this.isDeleted = purchaseOrder.getIsDeleted();
            this.purchaseOrderReceipt = purchaseOrder.getPurchaseOrderReceipt();
            this.purchaseOrderStatus = purchaseOrder.getPurchaseOrderStatus();
            this.paymentId = purchaseOrder.getPaymentId();
            this.approvedDate = purchaseOrder.getApprovedDate();
            this.rejectedDate = purchaseOrder.getRejectedDate();
            this.assignedLeadId = purchaseOrder.getAssignedLeadId();
            this.createdAt = purchaseOrder.getCreatedAt();
            this.updatedAt = purchaseOrder.getUpdatedAt();
            
            // Extract Assigned Lead
            if (purchaseOrder.getAssignedLead() != null) {
                this.lead = new LeadResponseModel(purchaseOrder.getAssignedLead());
            }
            
            // Extract Created By User
            if (purchaseOrder.getCreatedByUser() != null) {
                this.createdByUser = new UserResponseModel(purchaseOrder.getCreatedByUser());
            }
            
            // Extract Modified By User
            if (purchaseOrder.getModifiedByUser() != null) {
                this.modifiedByUser = new UserResponseModel(purchaseOrder.getModifiedByUser());
            }
            
            // Extract Approved By User
            if (purchaseOrder.getApprovedByUser() != null) {
                this.approvedByUser = new UserResponseModel(purchaseOrder.getApprovedByUser());
            }
            
            // Extract Rejected By User
            if (purchaseOrder.getRejectedByUser() != null) {
                this.rejectedByUser = new UserResponseModel(purchaseOrder.getRejectedByUser());
            }
            
            // Extract OrderSummary
            if (orderSummary != null) {
                this.orderSummary = new OrderSummaryResponseData(orderSummary);
                
                // Extract Shipments from OrderSummary
                if (orderSummary.getShipments() != null) {
                    Hibernate.initialize(orderSummary.getShipments());
                    for (Shipment shipment : orderSummary.getShipments()) {
                        this.shipments.add(new ShipmentResponseData(shipment));
                    }
                }
            }
            
            // Initialize collections
            this.products = new ArrayList<>();
            this.attachments = new HashMap<>();
            this.attachmentsList = new ArrayList<>();
            
            // Extract Products from Shipments (for backward compatibility)
            // Aggregate products from all shipments
            Map<Long, PurchaseOrderProductItem> productMap = new HashMap<>();
            for (ShipmentResponseData shipmentData : this.shipments) {
                for (ShipmentProductResponseData productData : shipmentData.getProducts()) {
                    Long productId = productData.getProductId();
                    if (productMap.containsKey(productId)) {
                        // Aggregate quantity and price
                        PurchaseOrderProductItem existingItem = productMap.get(productId);
                        existingItem.setQuantity(existingItem.getQuantity() + productData.getAllocatedQuantity());
                        // Use the latest allocated price (or average if needed)
                        existingItem.setPricePerUnit(productData.getAllocatedPrice());
                    } else {
                        // Create new product item
                        ProductResponseModel productResponse = productData.getProduct();
                        if (productResponse == null) {
                            // Skip if product is not loaded
                            continue;
                        }
                        PurchaseOrderProductItem productItem = new PurchaseOrderProductItem(
                            productResponse,
                            productData.getAllocatedPrice(),
                            productData.getAllocatedQuantity()
                        );
                        productMap.put(productId, productItem);
                    }
                }
            }
            this.products.addAll(productMap.values());
            
            // Extract Resources (attachments)
            if (purchaseOrder.getAttachments() != null && !purchaseOrder.getAttachments().isEmpty()) {
                for (Resources resource : purchaseOrder.getAttachments()) {
                    // Add to list for full details
                    this.attachmentsList.add(new ResourceResponseModel(resource));
                    
                    // Add to map for edit form (fileName -> URL)
                    // key field stores fileName, value field stores URL
                    // Frontend can use URLs for existing attachments and base64 for new uploads
                    this.attachments.put(resource.getKey(), resource.getValue());
                }
            }
        }
    }
    
    /**
     * Constructor that creates a response model from a PurchaseOrder entity (backward compatibility).
     * This constructor will attempt to load OrderSummary, but it's recommended to use the
     * constructor with OrderSummary parameter for better performance.
     * 
     * @param purchaseOrder The PurchaseOrder entity to convert
     */
    public PurchaseOrderResponseModel(PurchaseOrder purchaseOrder) {
        // This constructor is kept for backward compatibility but should not be used
        // when OrderSummary data is needed. It will create a response model without
        // OrderSummary and Shipments data.
        if (purchaseOrder != null) {
            // Basic purchase order fields
            this.purchaseOrderId = purchaseOrder.getPurchaseOrderId();
            this.vendorNumber = purchaseOrder.getVendorNumber();
            this.isDeleted = purchaseOrder.getIsDeleted();
            this.purchaseOrderReceipt = purchaseOrder.getPurchaseOrderReceipt();
            this.purchaseOrderStatus = purchaseOrder.getPurchaseOrderStatus();
            this.paymentId = purchaseOrder.getPaymentId();
            this.approvedDate = purchaseOrder.getApprovedDate();
            this.rejectedDate = purchaseOrder.getRejectedDate();
            this.assignedLeadId = purchaseOrder.getAssignedLeadId();
            this.createdAt = purchaseOrder.getCreatedAt();
            this.updatedAt = purchaseOrder.getUpdatedAt();
            
            // Extract related entities
            if (purchaseOrder.getAssignedLead() != null) {
                this.lead = new LeadResponseModel(purchaseOrder.getAssignedLead());
            }
            if (purchaseOrder.getCreatedByUser() != null) {
                this.createdByUser = new UserResponseModel(purchaseOrder.getCreatedByUser());
            }
            if (purchaseOrder.getModifiedByUser() != null) {
                this.modifiedByUser = new UserResponseModel(purchaseOrder.getModifiedByUser());
                    }
            if (purchaseOrder.getApprovedByUser() != null) {
                this.approvedByUser = new UserResponseModel(purchaseOrder.getApprovedByUser());
            }
            if (purchaseOrder.getRejectedByUser() != null) {
                this.rejectedByUser = new UserResponseModel(purchaseOrder.getRejectedByUser());
            }
            
            // Initialize collections
            this.products = new ArrayList<>();
            this.attachments = new HashMap<>();
            this.attachmentsList = new ArrayList<>();
            
            // Extract Resources (attachments)
            if (purchaseOrder.getAttachments() != null && !purchaseOrder.getAttachments().isEmpty()) {
                for (Resources resource : purchaseOrder.getAttachments()) {
                    this.attachmentsList.add(new ResourceResponseModel(resource));
                    this.attachments.put(resource.getKey(), resource.getValue());
                }
            }
        }
    }
    
    /**
     * OrderSummary response data structure containing financial breakdown and fulfillment details.
     */
    @Getter
    @Setter
    public static class OrderSummaryResponseData {
        private Long orderSummaryId;
        
        // Financial Breakdown
        private BigDecimal productsSubtotal;
        private BigDecimal totalDiscount;
        private BigDecimal packagingFee;
        private BigDecimal totalShipping;
        private BigDecimal subtotal;
        private BigDecimal gstPercentage;
        private BigDecimal gstAmount;
        private BigDecimal grandTotal;
        private BigDecimal pendingAmount;
        
        // Fulfillment Details
        private LocalDateTime expectedDeliveryDate;
        private AddressResponseModel address; // Delivery/shipping address
        private String priority; // LOW, MEDIUM, HIGH, URGENT
        
        // Promotion & Terms (Optional)
        private Long promoId;
        private PromoResponseModel promo;
        private String termsConditionsHtml;
        private String notes;
        
        public OrderSummaryResponseData(OrderSummary orderSummary) {
            if (orderSummary != null) {
                this.orderSummaryId = orderSummary.getOrderSummaryId();
                this.productsSubtotal = orderSummary.getProductsSubtotal();
                this.totalDiscount = orderSummary.getTotalDiscount();
                this.packagingFee = orderSummary.getPackagingFee();
                this.totalShipping = orderSummary.getTotalShipping();
                this.subtotal = orderSummary.getSubtotal();
                this.gstPercentage = orderSummary.getGstPercentage();
                this.gstAmount = orderSummary.getGstAmount();
                this.grandTotal = orderSummary.getGrandTotal();
                this.pendingAmount = orderSummary.getPendingAmount();
                this.expectedDeliveryDate = orderSummary.getExpectedDeliveryDate();
                this.priority = orderSummary.getPriority();
                this.promoId = orderSummary.getPromoId();
                this.termsConditionsHtml = orderSummary.getTermsConditionsHtml();
                this.notes = orderSummary.getNotes();
                
                // Extract Address
                if (orderSummary.getEntityAddress() != null) {
                    this.address = new AddressResponseModel(orderSummary.getEntityAddress());
                }
                
                // Extract Promo
                if (orderSummary.getPromo() != null) {
                    this.promo = new PromoResponseModel(orderSummary.getPromo());
                }
            }
        }
    }
    
    /**
     * Shipment response data structure containing shipment details, products, packages, and courier selection.
     */
    @Getter
    @Setter
    public static class ShipmentResponseData {
        private Long shipmentId;
        
        // Shipment Basic Info
        private Long pickupLocationId;
        private PickupLocationResponseModel pickupLocation;
        private BigDecimal totalWeightKgs;
        private Integer totalQuantity;
        private LocalDateTime expectedDeliveryDate;
        
        // Costs
        private BigDecimal packagingCost;
        private BigDecimal shippingCost;
        private BigDecimal totalCost;
        
        // Courier Selection
        private CourierSelectionResponseData selectedCourier;
        
        // Products in Shipment
        private List<ShipmentProductResponseData> products = new ArrayList<>();
        
        // Packages Used in Shipment
        private List<ShipmentPackageResponseData> packages = new ArrayList<>();
        
        public ShipmentResponseData(Shipment shipment) {
            if (shipment != null) {
                this.shipmentId = shipment.getShipmentId();
                this.pickupLocationId = shipment.getPickupLocationId();
                this.totalWeightKgs = shipment.getTotalWeightKgs();
                this.totalQuantity = shipment.getTotalQuantity();
                this.expectedDeliveryDate = shipment.getExpectedDeliveryDate();
                this.packagingCost = shipment.getPackagingCost();
                this.shippingCost = shipment.getShippingCost();
                this.totalCost = shipment.getTotalCost();
                
                // Extract PickupLocation
                if (shipment.getPickupLocation() != null) {
                    this.pickupLocation = new PickupLocationResponseModel(shipment.getPickupLocation());
                }
                
                // Extract Courier Selection
                if (shipment.getSelectedCourierCompanyId() != null) {
                    this.selectedCourier = new CourierSelectionResponseData(shipment);
                }
                
                // Extract ShipmentProducts
                if (shipment.getShipmentProducts() != null) {
                    Hibernate.initialize(shipment.getShipmentProducts());
                    for (ShipmentProduct shipmentProduct : shipment.getShipmentProducts()) {
                        this.products.add(new ShipmentProductResponseData(shipmentProduct));
                    }
                }
                
                // Extract ShipmentPackages
                if (shipment.getShipmentPackages() != null) {
                    Hibernate.initialize(shipment.getShipmentPackages());
                    for (ShipmentPackage shipmentPackage : shipment.getShipmentPackages()) {
                        this.packages.add(new ShipmentPackageResponseData(shipmentPackage));
                    }
                }
            }
        }
    }
    
    /**
     * Courier selection response data containing selected courier details and metadata.
     */
    @Getter
    @Setter
    public static class CourierSelectionResponseData {
        private Long courierCompanyId;
        private String courierName;
        private BigDecimal courierRate;
        private String courierMetadata; // Full CourierOption JSON as string
        
        public CourierSelectionResponseData(Shipment shipment) {
            if (shipment != null) {
                this.courierCompanyId = shipment.getSelectedCourierCompanyId();
                this.courierName = shipment.getSelectedCourierName();
                this.courierRate = shipment.getSelectedCourierRate();
                this.courierMetadata = shipment.getSelectedCourierMetadata();
            }
        }
    }
    
    /**
     * Product response data within a shipment.
     */
    @Getter
    @Setter
    public static class ShipmentProductResponseData {
        private Long shipmentProductId;
        private Long productId;
        private ProductResponseModel product;
        private Integer allocatedQuantity;
        private BigDecimal allocatedPrice;
        
        public ShipmentProductResponseData(ShipmentProduct shipmentProduct) {
            if (shipmentProduct != null) {
                this.shipmentProductId = shipmentProduct.getShipmentProductId();
                this.productId = shipmentProduct.getProductId();
                this.allocatedQuantity = shipmentProduct.getAllocatedQuantity();
                this.allocatedPrice = shipmentProduct.getAllocatedPrice();
                
                // Extract Product
                if (shipmentProduct.getProduct() != null) {
                    this.product = new ProductResponseModel(shipmentProduct.getProduct());
                }
            }
        }
    }
    
    /**
     * Package response data within a shipment.
     */
    @Getter
    @Setter
    public static class ShipmentPackageResponseData {
        private Long shipmentPackageId;
        private Long packageId;
        private PackageResponseModel packageInfo;
        private Integer quantityUsed;
        private BigDecimal totalCost;
        
        // Products in this package
        private List<PackageProductResponseData> products = new ArrayList<>();
        
        public ShipmentPackageResponseData(ShipmentPackage shipmentPackage) {
            if (shipmentPackage != null) {
                this.shipmentPackageId = shipmentPackage.getShipmentPackageId();
                this.packageId = shipmentPackage.getPackageId();
                this.quantityUsed = shipmentPackage.getQuantityUsed();
                this.totalCost = shipmentPackage.getTotalCost();
                
                // Extract Package
                if (shipmentPackage.getPackageInfo() != null) {
                    this.packageInfo = new PackageResponseModel(shipmentPackage.getPackageInfo());
                }
                
                // Extract ShipmentPackageProducts
                if (shipmentPackage.getShipmentPackageProducts() != null) {
                    Hibernate.initialize(shipmentPackage.getShipmentPackageProducts());
                    for (ShipmentPackageProduct packageProduct : shipmentPackage.getShipmentPackageProducts()) {
                        this.products.add(new PackageProductResponseData(packageProduct));
                    }
                }
            }
        }
    }
    
    /**
     * Product response data within a package.
     */
    @Getter
    @Setter
    public static class PackageProductResponseData {
        private Long shipmentPackageProductId;
        private Long productId;
        private ProductResponseModel product;
        private Integer quantity;
        
        public PackageProductResponseData(ShipmentPackageProduct packageProduct) {
            if (packageProduct != null) {
                this.shipmentPackageProductId = packageProduct.getShipmentPackageProductId();
                this.productId = packageProduct.getProductId();
                this.quantity = packageProduct.getQuantity();
                
                // Extract Product
                if (packageProduct.getProduct() != null) {
                    this.product = new ProductResponseModel(packageProduct.getProduct());
                }
            }
        }
    }
}
