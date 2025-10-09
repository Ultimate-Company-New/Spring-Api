package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String notes;
}
