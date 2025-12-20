package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Request model for PurchaseOrder operations.
 * 
 * This model is used for creating and updating purchase order information.
 * Includes OrderSummary (financial breakdown and fulfillment details) and Shipment data.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderRequestModel {
    // Purchase Order Basic Fields
    private Long purchaseOrderId; // Required for updates
    private String vendorNumber;
    private Boolean isDeleted;
    private String purchaseOrderReceipt;
    private String purchaseOrderStatus;
    private Long paymentId;
    private Long approvedByUserId;
    private LocalDateTime approvedDate;
    private Long rejectedByUserId;
    private LocalDateTime rejectedDate;
    private Long assignedLeadId;
    private List<PurchaseOrderProductItem> products; // Required: list of products with price and quantity
    private Map<String, String> attachments; // Optional: max 30 attachments (key: fileName, value: base64 data)
    
    // OrderSummary Data (Financial Breakdown and Fulfillment Details)
    private OrderSummaryData orderSummary;
    
    // Shipment Data (List of shipments with products, packages, and courier selections)
    private List<ShipmentData> shipments = new ArrayList<>();
    
    /**
     * OrderSummary data structure containing financial breakdown and fulfillment details.
     */
    @Getter
    @Setter
    public static class OrderSummaryData {
        // Financial Breakdown
        private BigDecimal productsSubtotal; // Required
        private BigDecimal totalDiscount; // Default: 0
        private BigDecimal packagingFee; // Default: 0
        private BigDecimal totalShipping; // Default: 0
        private BigDecimal gstPercentage; // Default: 18.00
        private BigDecimal gstAmount; // Required (calculated from subtotal * gstPercentage)
        private BigDecimal grandTotal; // Required (calculated: subtotal + gstAmount)
        private BigDecimal pendingAmount; // Default: grandTotal (nothing paid yet)
        
        // Fulfillment Details
        private LocalDateTime expectedDeliveryDate; // Optional
        private AddressRequestModel address; // Required: delivery/shipping address data (will check for duplicates before creating)
        private String priority; // Required: LOW, MEDIUM, HIGH, URGENT
        
        // Promotion & Terms (Optional)
        private Long promoId; // Optional: applied promotion/discount code
        private String termsConditionsHtml; // Optional
        private String notes; // Optional
    }
    
    /**
     * Shipment data structure containing shipment details, products, packages, and courier selection.
     */
    @Getter
    @Setter
    public static class ShipmentData {
        // Shipment Basic Info
        private Long pickupLocationId; // Required
        private BigDecimal totalWeightKgs; // Required
        private Integer totalQuantity; // Required
        private LocalDateTime expectedDeliveryDate; // Optional: expected delivery date for this shipment
        
        // Costs
        private BigDecimal packagingCost; // Required
        private BigDecimal shippingCost; // Required
        private BigDecimal totalCost; // Required (packagingCost + shippingCost)
        
        // Courier Selection
        private CourierSelectionData selectedCourier; // Optional: selected courier details
        
        // Products in Shipment
        private List<ShipmentProductData> products = new ArrayList<>();
        
        // Packages Used in Shipment
        private List<ShipmentPackageData> packages = new ArrayList<>();
    }
    
    /**
     * Courier selection data containing selected courier details and metadata.
     */
    @Getter
    @Setter
    public static class CourierSelectionData {
        private Long courierCompanyId; // Required: selected courier company ID
        private String courierName; // Required: selected courier name
        private BigDecimal courierRate; // Required: selected courier rate
        private String courierMetadata; // Optional: Full CourierOption JSON as string
    }
    
    /**
     * Product data within a shipment.
     */
    @Getter
    @Setter
    public static class ShipmentProductData {
        private Long productId; // Required
        private Integer allocatedQuantity; // Required: quantity allocated from this location
        private BigDecimal allocatedPrice; // Required: custom price per unit for this product in this shipment
    }
    
    /**
     * Package data within a shipment.
     */
    @Getter
    @Setter
    public static class ShipmentPackageData {
        private Long packageId; // Required
        private Integer quantityUsed; // Required: number of boxes/units used
        private BigDecimal totalCost; // Required: total cost for this package type
        
        // Products in this package
        private List<PackageProductData> products = new ArrayList<>();
    }
    
    /**
     * Product data within a package.
     */
    @Getter
    @Setter
    public static class PackageProductData {
        private Long productId; // Required
        private Integer quantity; // Required: quantity of this product in this package
    }
}
