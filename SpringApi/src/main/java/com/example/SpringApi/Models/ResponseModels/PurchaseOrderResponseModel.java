package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private OrderSummaryResponseModel orderSummary;
    
    // Address (top-level for grid display convenience - extracted from OrderSummary)
    private AddressResponseModel address;
    
    // Shipment Data (List of shipments with products, packages, and courier selections)
    private List<ShipmentResponseModel> shipments = new ArrayList<>();
    
    /**
     * Products extracted from shipments for backward compatibility.
     *
     * @deprecated Use `shipments[].products[]` instead.
     */
    @Deprecated(since = "2026-02", forRemoval = false)
    private List<PurchaseOrderProductItem> products;
    
    // Attachments (List of resource details - contains both fileName (key) and URL/base64 (value))
    private List<ResourceResponseModel> attachments;
    
    // Payments (List of all payments made for this purchase order)
    private List<PaymentResponseModel> payments = new ArrayList<>();

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
            this.approvedDate = purchaseOrder.getApprovedDate();
            this.rejectedDate = purchaseOrder.getRejectedDate();
            this.assignedLeadId = purchaseOrder.getAssignedLeadId();
            this.createdAt = purchaseOrder.getCreatedAt();
            this.updatedAt = purchaseOrder.getUpdatedAt();
            
            // Extract Assigned Lead - minimal fields only (leadId, firstName, lastName, email)
            if (purchaseOrder.getAssignedLead() != null) {
                this.lead = new LeadResponseModel(purchaseOrder.getAssignedLead(), true);
            }
            
            // Extract Created By User - exclude permissions, addresses, and userGroups
            if (purchaseOrder.getCreatedByUser() != null) {
                this.createdByUser = new UserResponseModel(purchaseOrder.getCreatedByUser(), true);
            }
            
            // Extract Modified By User - exclude permissions, addresses, and userGroups
            if (purchaseOrder.getModifiedByUser() != null) {
                this.modifiedByUser = new UserResponseModel(purchaseOrder.getModifiedByUser(), true);
            }
            
            // Extract Approved By User - exclude permissions, addresses, and userGroups
            if (purchaseOrder.getApprovedByUser() != null) {
                this.approvedByUser = new UserResponseModel(purchaseOrder.getApprovedByUser(), true);
            }
            
            // Extract Rejected By User - exclude permissions, addresses, and userGroups
            if (purchaseOrder.getRejectedByUser() != null) {
                this.rejectedByUser = new UserResponseModel(purchaseOrder.getRejectedByUser(), true);
            }
            
            // Extract OrderSummary
            if (orderSummary != null) {
                this.orderSummary = new OrderSummaryResponseModel(orderSummary);
                
                // Extract Address from OrderSummary (for top-level access in grid)
                if (orderSummary.getEntityAddress() != null) {
                    this.address = new AddressResponseModel(orderSummary.getEntityAddress());
                }
                
                // Extract Shipments from OrderSummary
                if (orderSummary.getShipments() != null) {
                    Hibernate.initialize(orderSummary.getShipments());
                    for (Shipment shipment : orderSummary.getShipments()) {
                        this.shipments.add(new ShipmentResponseModel(shipment));
                    }
                }
            }
            
            // Initialize collections
            this.attachments = new ArrayList<>();
            
            // Note: Products are available in shipments, no need for separate products field
            // Frontend should extract products from shipments.shipments[].products[]
            
            // Extract Resources (attachments)
            if (purchaseOrder.getAttachments() != null && !purchaseOrder.getAttachments().isEmpty()) {
                for (Resources resource : purchaseOrder.getAttachments()) {
                    // Add to list - contains both fileName (key) and URL/base64 (value)
                    this.attachments.add(new ResourceResponseModel(resource));
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
            // Note: products field is deprecated - products are available in shipments
            this.attachments = new ArrayList<>();
            
            // Extract Resources (attachments)
            if (purchaseOrder.getAttachments() != null && !purchaseOrder.getAttachments().isEmpty()) {
                for (Resources resource : purchaseOrder.getAttachments()) {
                    this.attachments.add(new ResourceResponseModel(resource));
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
        private BigDecimal courierMinWeight; // Courier minimum weight in kg
        private String courierMetadata; // Full CourierOption JSON as string
        
        public CourierSelectionResponseData(Shipment shipment) {
            if (shipment != null) {
                this.courierCompanyId = shipment.getSelectedCourierCompanyId();
                this.courierName = shipment.getSelectedCourierName();
                this.courierRate = shipment.getSelectedCourierRate();
                this.courierMinWeight = shipment.getSelectedCourierMinWeight();
                this.courierMetadata = shipment.getSelectedCourierMetadata();
            }
        }
    }
    
}
