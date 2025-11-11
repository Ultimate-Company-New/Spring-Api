package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderQuantityPriceMap;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for PurchaseOrder operations.
 * 
 * This model is used for returning purchase order information in API responses.
 * Includes all related entities: Address, Products, Pickup Locations, Payments, and Refunds.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderResponseModel {
    private Long purchaseOrderId;
    private LocalDateTime expectedDeliveryDate;
    private String vendorNumber;
    private Boolean isDeleted;
    private String termsConditionsHtml;
    private String purchaseOrderReceipt;
    private String purchaseOrderStatus;
    private String priority;
    private Long paymentId;
    private LocalDateTime approvedDate;
    private LocalDateTime rejectedDate;
    private Long assignedLeadId;
    private Long purchaseOrderAddressId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    // Nested response models for related entities
    private AddressResponseModel address;
    private PaymentInfoResponseModel paymentInfo;
    private LeadResponseModel lead;
    private UserResponseModel createdByUser;
    private UserResponseModel modifiedByUser;
    private UserResponseModel approvedByUser;
    private UserResponseModel rejectedByUser;
    private List<PurchaseOrderProductItem> products;
    private List<ResourceResponseModel> attachments;

    /**
     * Constructor that creates a response model from a PurchaseOrder entity.
     * Extracts and maps all related entities including:
     * - Address (delivery/billing address)
     * - PaymentInfo (payment details and quotation)
     * - Created By User (user who created the purchase order)
     * - Modified By User (user who last modified the purchase order)
     * - Products from Quantity Maps with Pickup Locations
     * 
     * @param purchaseOrder The PurchaseOrder entity to convert
     */
    public PurchaseOrderResponseModel(PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null) {
            // Basic purchase order fields
            this.purchaseOrderId = purchaseOrder.getPurchaseOrderId();
            this.expectedDeliveryDate = purchaseOrder.getExpectedDeliveryDate();
            this.vendorNumber = purchaseOrder.getVendorNumber();
            this.isDeleted = purchaseOrder.getIsDeleted();
            this.termsConditionsHtml = purchaseOrder.getTermsConditionsHtml();
            this.purchaseOrderReceipt = purchaseOrder.getPurchaseOrderReceipt();
            this.purchaseOrderStatus = purchaseOrder.getPurchaseOrderStatus();
            this.priority = purchaseOrder.getPriority();
            this.paymentId = purchaseOrder.getPaymentId();
            this.approvedDate = purchaseOrder.getApprovedDate();
            this.rejectedDate = purchaseOrder.getRejectedDate();
            this.assignedLeadId = purchaseOrder.getAssignedLeadId();
            this.purchaseOrderAddressId = purchaseOrder.getPurchaseOrderAddressId();
            this.createdAt = purchaseOrder.getCreatedAt();
            this.updatedAt = purchaseOrder.getUpdatedAt();
            this.notes = purchaseOrder.getNotes();
            
            // Extract Address
            if (purchaseOrder.getPurchaseOrderAddress() != null) {
                this.address = new AddressResponseModel(purchaseOrder.getPurchaseOrderAddress());
            }
            
            // Extract PaymentInfo
            if (purchaseOrder.getPaymentInfo() != null) {
                this.paymentInfo = new PaymentInfoResponseModel(purchaseOrder.getPaymentInfo());
            }
            
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
            
            // Initialize collections
            this.products = new ArrayList<>();
            this.attachments = new ArrayList<>();
            
            // Extract Products with Quantities from PurchaseOrderQuantityPriceMaps
            // ProductResponseModel constructor will automatically load pickup locations with their stock quantities
            if (purchaseOrder.getPurchaseOrderQuantityPriceMaps() != null && 
                !purchaseOrder.getPurchaseOrderQuantityPriceMaps().isEmpty()) {
                
                for (PurchaseOrderQuantityPriceMap quantityMap : purchaseOrder.getPurchaseOrderQuantityPriceMaps()) {
                    if (quantityMap.getProduct() != null) {
                        
                        // Create ProductResponseModel which will include pickup location quantities
                        ProductResponseModel productResponse = new ProductResponseModel(
                            quantityMap.getProduct()
                        );
                        
                        // Create PurchaseOrderProductItem with product, price, and quantity
                        PurchaseOrderProductItem productItem = new PurchaseOrderProductItem(
                            productResponse,
                            quantityMap.getPricePerQuantity(),
                            quantityMap.getQuantity()
                        );
                        
                        this.products.add(productItem);
                    }
                }
            }
            
            // Extract Resources (attachments)
            if (purchaseOrder.getAttachments() != null && !purchaseOrder.getAttachments().isEmpty()) {
                for (Resources resource : purchaseOrder.getAttachments()) {
                    this.attachments.add(new ResourceResponseModel(resource));
                }
            }
        }
    }
}
