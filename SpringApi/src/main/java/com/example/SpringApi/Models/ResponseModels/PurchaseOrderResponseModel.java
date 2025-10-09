package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model for PurchaseOrder operations.
 * 
 * This model is used for returning purchase order information in API responses.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderResponseModel {
    private Long purchaseOrderId;
    private LocalDateTime expectedShipmentDate;
    private String vendorNumber;
    private Boolean isDeleted;
    private String termsConditionsHtml;
    private String orderReceipt;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private Long clientId;
    private Long approvedByUserId;
    private Long assignedLeadId;
    private Long purchaseOrderAddressId;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    /**
     * Constructor that creates a response model from a PurchaseOrder entity.
     * 
     * @param purchaseOrder The PurchaseOrder entity to convert
     */
    public PurchaseOrderResponseModel(PurchaseOrder purchaseOrder) {
        if (purchaseOrder != null) {
            this.purchaseOrderId = purchaseOrder.getPurchaseOrderId();
            this.expectedShipmentDate = purchaseOrder.getExpectedShipmentDate();
            this.vendorNumber = purchaseOrder.getVendorNumber();
            this.isDeleted = purchaseOrder.getIsDeleted();
            this.termsConditionsHtml = purchaseOrder.getTermsConditionsHtml();
            this.orderReceipt = purchaseOrder.getOrderReceipt();
            this.orderStatus = purchaseOrder.getOrderStatus();
            this.paymentStatus = purchaseOrder.getPaymentStatus();
            this.totalAmount = purchaseOrder.getTotalAmount();
            this.amountPaid = purchaseOrder.getAmountPaid();
            this.clientId = purchaseOrder.getClientId();
            this.approvedByUserId = purchaseOrder.getApprovedByUserId();
            this.assignedLeadId = purchaseOrder.getAssignedLeadId();
            this.purchaseOrderAddressId = purchaseOrder.getPurchaseOrderAddressId();
            this.createdUser = purchaseOrder.getCreatedUser();
            this.modifiedUser = purchaseOrder.getModifiedUser();
            this.createdAt = purchaseOrder.getCreatedAt();
            this.updatedAt = purchaseOrder.getUpdatedAt();
            this.notes = purchaseOrder.getNotes();
        }
    }
}
