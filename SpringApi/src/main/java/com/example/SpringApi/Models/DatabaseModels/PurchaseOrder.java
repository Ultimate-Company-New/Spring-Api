package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the PurchaseOrder table.
 * 
 * This entity represents purchase orders for vendor procurement management.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PurchaseOrder")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchaseOrderId", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "expectedShipmentDate")
    private LocalDateTime expectedShipmentDate;

    @Column(name = "vendorNumber", length = 100)
    private String vendorNumber;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "termsConditionsHtml", nullable = false, columnDefinition = "TEXT")
    private String termsConditionsHtml;

    @Column(name = "orderReceipt", length = 500)
    private String orderReceipt;

    @Column(name = "orderStatus", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "paymentStatus", nullable = false, length = 50)
    private String paymentStatus;

    @Column(name = "totalAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amountPaid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "clientId", nullable = false)
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;

    @Column(name = "approvedByUserId")
    private Long approvedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedByUserId", insertable = false, updatable = false)
    private User approvedByUser;

    @Column(name = "assignedLeadId", nullable = false)
    private Long assignedLeadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedLeadId", insertable = false, updatable = false)
    private Lead assignedLead;

    @Column(name = "purchaseOrderAddressId", nullable = false)
    private Long purchaseOrderAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseOrderAddressId", insertable = false, updatable = false)
    private Address purchaseOrderAddress;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Default constructor.
     */
    public PurchaseOrder() {}

    /**
     * Constructor for creating a new purchase order.
     * 
     * @param request The PurchaseOrderRequestModel containing the purchase order data
     * @param createdUser The user creating the purchase order
     */
    public PurchaseOrder(PurchaseOrderRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }

    /**
     * Constructor for updating an existing purchase order.
     * 
     * @param request The PurchaseOrderRequestModel containing the updated purchase order data
     * @param modifiedUser The user modifying the purchase order
     * @param existingPurchaseOrder The existing purchase order entity
     */
    public PurchaseOrder(PurchaseOrderRequestModel request, String modifiedUser, PurchaseOrder existingPurchaseOrder) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        this.purchaseOrderId = existingPurchaseOrder.getPurchaseOrderId();
        this.createdUser = existingPurchaseOrder.getCreatedUser();
        this.createdAt = existingPurchaseOrder.getCreatedAt();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;
    }

    /**
     * Validates the request model.
     * 
     * @param request The PurchaseOrderRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(PurchaseOrderRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidRequest);
        }
        if (request.getTermsConditionsHtml() == null || request.getTermsConditionsHtml().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidTermsConditions);
        }
        if (request.getOrderStatus() == null || request.getOrderStatus().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus);
        }
        if (request.getPaymentStatus() == null || request.getPaymentStatus().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidPaymentStatus);
        }
        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidTotalAmount);
        }
        if (request.getAmountPaid() == null || request.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidAmountPaid);
        }
        if (request.getAmountPaid().compareTo(request.getTotalAmount()) > 0) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.AmountPaidExceedsTotal);
        }
        if (request.getClientId() == null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidClientId);
        }
        if (request.getAssignedLeadId() == null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidAssignedLeadId);
        }
        if (request.getPurchaseOrderAddressId() == null) {
            throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.InvalidAddressId);
        }
    }

    /**
     * Validates the user parameter.
     * 
     * @param user The user to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }

    /**
     * Sets fields from the request model.
     * 
     * @param request The PurchaseOrderRequestModel to extract fields from
     */
    private void setFieldsFromRequest(PurchaseOrderRequestModel request) {
        this.expectedShipmentDate = request.getExpectedShipmentDate();
        this.vendorNumber = request.getVendorNumber() != null ? request.getVendorNumber().trim() : null;
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
        this.termsConditionsHtml = request.getTermsConditionsHtml().trim();
        this.orderReceipt = request.getOrderReceipt() != null ? request.getOrderReceipt().trim() : null;
        this.orderStatus = request.getOrderStatus().trim();
        this.paymentStatus = request.getPaymentStatus().trim();
        this.totalAmount = request.getTotalAmount();
        this.amountPaid = request.getAmountPaid();
        this.clientId = request.getClientId();
        this.approvedByUserId = request.getApprovedByUserId();
        this.assignedLeadId = request.getAssignedLeadId();
        this.purchaseOrderAddressId = request.getPurchaseOrderAddressId();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}
