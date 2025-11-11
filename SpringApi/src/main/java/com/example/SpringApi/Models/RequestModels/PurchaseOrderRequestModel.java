package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request model for PurchaseOrder operations.
 * 
 * This model is used for creating and updating purchase order information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PurchaseOrderRequestModel {
    private Long purchaseOrderId; // Required for updates
    private LocalDateTime expectedDeliveryDate;
    private String vendorNumber;
    private Boolean isDeleted;
    private String termsConditionsHtml;
    private String purchaseOrderReceipt;
    private String purchaseOrderStatus;
    private String priority; // Required: LOW, MEDIUM, HIGH, URGENT
    private Long paymentId;
    private Long clientId;
    private Long approvedByUserId;
    private LocalDateTime approvedDate;
    private Long rejectedByUserId;
    private LocalDateTime rejectedDate;
    private Long assignedLeadId;
    private Long purchaseOrderAddressId; // Optional: if provided, use existing address; if null, create from 'address' field
    private AddressRequestModel address; // Optional: address data for creating new address
    private List<PurchaseOrderProductItem> products; // Required: list of products with price and quantity
    private List<Map<String, Object>> attachments; // Optional: max 30 attachments
    private String notes;
    
    // Payment calculation fields (optional - if not provided, defaults will be used)
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal packagingFee;
    private BigDecimal discount;
}
