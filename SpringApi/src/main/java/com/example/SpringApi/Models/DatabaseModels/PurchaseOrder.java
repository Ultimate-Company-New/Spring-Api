package com.example.SpringApi.Models.DatabaseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the PurchaseOrder table.
 *
 * <p>This entity represents purchase orders for vendor procurement management.
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

  /** Enum for Purchase Order Status values. */
  public enum Status {
    DRAFT("DRAFT"),
    PENDING_APPROVAL("PENDING_APPROVAL"),
    APPROVED("APPROVED"),
    APPROVED_WITH_PARTIAL_PAYMENT("APPROVED_WITH_PARTIAL_PAYMENT"),
    REJECTED("REJECTED"),
    SENT_TO_VENDOR("SENT_TO_VENDOR"),
    ACKNOWLEDGED("ACKNOWLEDGED"),
    IN_PRODUCTION("IN_PRODUCTION"),
    SHIPPED("SHIPPED"),
    PARTIALLY_RECEIVED("PARTIALLY_RECEIVED"),
    RECEIVED("RECEIVED"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    ON_HOLD("ON_HOLD");

    private final String value;

    Status(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Check if a string value is a valid status.
     *
     * @param value The string value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
      if (value == null) {
        return false;
      }
      for (Status status : Status.values()) {
        if (status.value.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "purchaseOrderId", nullable = false)
  private Long purchaseOrderId;

  @Column(name = "vendorNumber", length = 100)
  private String vendorNumber;

  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted;

  @Column(name = "purchaseOrderReceipt", length = 500)
  private String purchaseOrderReceipt;

  @Column(name = "purchaseOrderStatus", nullable = false, length = 50)
  private String purchaseOrderStatus;

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

  @Column(name = "approvedDate")
  private LocalDateTime approvedDate;

  @Column(name = "rejectedByUserId")
  private Long rejectedByUserId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rejectedByUserId", insertable = false, updatable = false)
  private User rejectedByUser;

  @Column(name = "rejectedDate")
  private LocalDateTime rejectedDate;

  @Column(name = "assignedLeadId", nullable = false)
  private Long assignedLeadId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignedLeadId", insertable = false, updatable = false)
  private Lead assignedLead;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  // OrderSummary - polymorphic link via entityType+entityId (transient, populated by service)
  // Note: Cannot use @OneToOne with formula due to Hibernate limitations with subqueries
  // This relationship is populated programmatically in the service layer
  @Transient private OrderSummary orderSummary;

  // Relationships - Collections
  @Transient
  private List<Resources> attachments = new ArrayList<>(); // Not persisted, populated manually

  /** Default constructor. */
  public PurchaseOrder() {}

  /**
   * Constructor for creating a new purchase order.
   *
   * @param request The PurchaseOrderRequestModel containing the purchase order data
   * @param createdUser The user creating the purchase order
   * @param clientId The client ID from the authenticated user's token
   */
  public PurchaseOrder(PurchaseOrderRequestModel request, String createdUser, Long clientId) {
    validateRequest(request);
    validateUser(createdUser);

    setFieldsFromRequest(request, clientId);
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
  public PurchaseOrder(
      PurchaseOrderRequestModel request, String modifiedUser, PurchaseOrder existingPurchaseOrder) {
    validateRequest(request);
    validateUser(modifiedUser);

    this.purchaseOrderId = existingPurchaseOrder.getPurchaseOrderId();
    this.createdUser = existingPurchaseOrder.getCreatedUser();
    this.createdAt = existingPurchaseOrder.getCreatedAt();

    // Set clientId from existing purchase order (don't trust client-provided clientId)
    setFieldsFromRequest(request, existingPurchaseOrder.getClientId());

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
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_REQUEST);
    }

    // Validate OrderSummary data is provided
    if (request.getOrderSummary() == null) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.INVALID_REQUEST);
    }

    // Validate max 30 attachments
    if (request.getAttachments() != null && request.getAttachments().size() > 30) {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.MAX_ATTACHMENTS_EXCEEDED);
    }

    // Validate that products list is provided and not empty
    if (request.getProducts() == null || request.getProducts().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.ER004);
    }

    // Validate each product entry has productId, quantity, and pricePerUnit
    for (PurchaseOrderProductItem item : request.getProducts()) {
      if (item == null) {
        continue;
      }

      if (item.getProductId() == null || item.getProductId() <= 0) {
        throw new BadRequestException(ErrorMessages.ProductErrorMessages.INVALID_ID);
      }

      if (item.getQuantity() == null || item.getQuantity() <= 0) {
        throw new BadRequestException(
            "Quantity must be greater than 0 for productId " + item.getProductId());
      }

      if (item.getPricePerUnit() == null) {
        throw new BadRequestException(
            "pricePerUnit is required for productId " + item.getProductId());
      }
      if (item.getPricePerUnit().compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException(
            "pricePerUnit must be greater than or equal to 0 for productId " + item.getProductId());
      }
    }

    // vendorNumber is optional (can be null)
    if (request.getPurchaseOrderStatus() == null
        || request.getPurchaseOrderStatus().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ORDER_STATUS);
    }
    // Validate purchaseOrderStatus values using enum
    if (!Status.isValid(request.getPurchaseOrderStatus().trim())) {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.INVALID_ORDER_STATUS_VALUE);
    }
    if (request.getAssignedLeadId() == null) {
      throw new BadRequestException(
          ErrorMessages.PurchaseOrderErrorMessages.INVALID_ASSIGNED_LEAD_ID);
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
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
    }
  }

  /**
   * Sets fields from the request model.
   *
   * @param request The PurchaseOrderRequestModel to extract fields from
   * @param clientId The client ID from the authenticated user's token or existing purchase order
   */
  private void setFieldsFromRequest(PurchaseOrderRequestModel request, Long clientId) {
    this.vendorNumber = request.getVendorNumber() != null ? request.getVendorNumber().trim() : null;
    this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
    this.purchaseOrderReceipt =
        request.getPurchaseOrderReceipt() != null ? request.getPurchaseOrderReceipt().trim() : null;
    this.purchaseOrderStatus = request.getPurchaseOrderStatus().trim();
    this.clientId = clientId;
    this.approvedByUserId = request.getApprovedByUserId();
    this.approvedDate = request.getApprovedDate();
    this.rejectedByUserId = request.getRejectedByUserId();
    this.rejectedDate = request.getRejectedDate();
    this.assignedLeadId = request.getAssignedLeadId();
  }

  /**
   * Sets the approval fields for this purchase order. Also clears rejection fields since a PO can't
   * be both approved and rejected.
   *
   * @param modifiedByUser The user performing the approval
   * @param approvedByUserId The ID of the user approving the purchase order
   */
  public void setApprovalFields(String modifiedByUser, Long approvedByUserId) {
    this.approvedByUserId = approvedByUserId;
    this.approvedDate = LocalDateTime.now(java.time.ZoneOffset.UTC);

    // Clear rejection fields when approving (a PO can't be both approved and rejected)
    this.rejectedByUserId = null;
    this.rejectedDate = null;

    // Update modified user
    this.modifiedUser = modifiedByUser;
  }

  /**
   * Sets the rejection fields for this purchase order. Also clears approval fields since a PO can't
   * be both approved and rejected.
   *
   * @param modifiedByUser The user performing the rejection
   * @param rejectedByUserId The ID of the user rejecting the purchase order
   */
  public void setRejectionFields(String modifiedByUser, Long rejectedByUserId) {
    this.rejectedByUserId = rejectedByUserId;
    this.rejectedDate = LocalDateTime.now(java.time.ZoneOffset.UTC);

    // Clear approval fields when rejecting (a PO can't be both approved and rejected)
    this.approvedByUserId = null;
    this.approvedDate = null;

    // Update modified user
    this.modifiedUser = modifiedByUser;
  }
}
