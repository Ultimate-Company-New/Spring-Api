package com.example.SpringApi.Models.DatabaseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
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
 * JPA Entity for the OrderSummary table.
 *
 * <p>This entity stores the financial breakdown and fulfillment details for any order entity
 * (PurchaseOrder or Order). Uses polymorphic association pattern via entityType + entityId to link
 * to PurchaseOrder or Order.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "OrderSummary")
public class OrderSummary {

  /** Enum for Entity Type values. */
  public enum EntityType {
    PURCHASE_ORDER("PURCHASE_ORDER"),
    ORDER("ORDER");

    private final String value;

    EntityType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Check if a string value is a valid entity type.
     *
     * @param value The string value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
      if (value == null) {
        return false;
      }
      for (EntityType type : EntityType.values()) {
        if (type.value.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  /** Enum for Priority values. */
  public enum Priority {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    URGENT("URGENT");

    private final String value;

    Priority(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Check if a string value is a valid priority.
     *
     * @param value The string value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
      if (value == null) {
        return false;
      }
      for (Priority priority : Priority.values()) {
        if (priority.value.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "orderSummaryId", nullable = false)
  private Long orderSummaryId;

  // Polymorphic Entity Reference
  @Column(name = "entityType", nullable = false, length = 50)
  private String entityType;

  @Column(name = "entityId", nullable = false)
  private Long entityId;

  // Financial Breakdown
  @Column(name = "productsSubtotal", nullable = false, precision = 15, scale = 2)
  private BigDecimal productsSubtotal;

  @Column(name = "totalDiscount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalDiscount;

  @Column(name = "packagingFee", nullable = false, precision = 15, scale = 2)
  private BigDecimal packagingFee;

  @Column(name = "totalShipping", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalShipping;

  @Column(name = "serviceFee", nullable = false, precision = 15, scale = 2)
  private BigDecimal serviceFee;

  @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "gstPercentage", nullable = false, precision = 5, scale = 2)
  private BigDecimal gstPercentage;

  @Column(name = "gstAmount", nullable = false, precision = 15, scale = 2)
  private BigDecimal gstAmount;

  @Column(name = "grandTotal", nullable = false, precision = 15, scale = 2)
  private BigDecimal grandTotal;

  @Column(name = "pendingAmount", nullable = false, precision = 15, scale = 2)
  private BigDecimal pendingAmount;

  // Fulfillment Details
  @Column(name = "expectedDeliveryDate")
  private LocalDateTime expectedDeliveryDate;

  @Column(name = "entityAddressId", nullable = false)
  private Long entityAddressId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "entityAddressId", insertable = false, updatable = false)
  private Address entityAddress;

  @Column(name = "priority", nullable = false, length = 20)
  private String priority;

  // Promotion & Terms
  @Column(name = "promoId")
  private Long promoId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "promoId", insertable = false, updatable = false)
  private Promo promo;

  @Column(name = "termsConditionsHtml", columnDefinition = "TEXT")
  private String termsConditionsHtml;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  // Standard Fields
  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

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

  // Relationships
  @OneToMany(mappedBy = "orderSummary", fetch = FetchType.LAZY)
  private List<Shipment> shipments = new ArrayList<>();

  /** Default constructor. */
  public OrderSummary() {}

  /**
   * Constructor for creating a new order summary from OrderSummaryData.
   *
   * @param entityType The entity type ('PURCHASE_ORDER' or 'ORDER')
   * @param entityId The ID of the parent entity
   * @param orderSummaryData The order summary data from request model (contains all financial and
   *     fulfillment data)
   * @param entityAddressId The delivery/shipping address ID (computed separately from address)
   * @param clientId The client ID
   * @param createdUser The user creating the order summary
   */
  public OrderSummary(
      String entityType,
      Long entityId,
      com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.OrderSummaryData
          orderSummaryData,
      Long entityAddressId,
      Long clientId,
      String createdUser) {
    validateRequest(entityType, entityId, orderSummaryData, entityAddressId, clientId, createdUser);

    this.entityType = entityType.trim();
    this.entityId = entityId;
    this.productsSubtotal = orderSummaryData.getProductsSubtotal();
    this.totalDiscount =
        orderSummaryData.getTotalDiscount() != null
            ? orderSummaryData.getTotalDiscount()
            : BigDecimal.ZERO;
    this.packagingFee =
        orderSummaryData.getPackagingFee() != null
            ? orderSummaryData.getPackagingFee()
            : BigDecimal.ZERO;
    this.totalShipping =
        orderSummaryData.getTotalShipping() != null
            ? orderSummaryData.getTotalShipping()
            : BigDecimal.ZERO;
    this.serviceFee =
        orderSummaryData.getServiceFee() != null
            ? orderSummaryData.getServiceFee()
            : BigDecimal.ZERO;
    this.gstPercentage =
        orderSummaryData.getGstPercentage() != null
            ? orderSummaryData.getGstPercentage()
            : new BigDecimal("18.00");

    // Calculate subtotal: productsSubtotal - totalDiscount + packagingFee + totalShipping +
    // serviceFee
    this.subtotal =
        this.productsSubtotal
            .subtract(this.totalDiscount)
            .add(this.packagingFee)
            .add(this.totalShipping)
            .add(this.serviceFee);

    // Calculate GST amount
    this.gstAmount =
        this.subtotal
            .multiply(this.gstPercentage.divide(new BigDecimal("100")))
            .setScale(2, java.math.RoundingMode.HALF_UP);

    // Calculate grand total: subtotal + gstAmount
    this.grandTotal = this.subtotal.add(this.gstAmount);

    this.pendingAmount = this.grandTotal; // Initially, nothing is paid
    this.entityAddressId = entityAddressId;
    this.priority = orderSummaryData.getPriority().trim();
    this.clientId = clientId;
    this.createdUser = createdUser;
    this.modifiedUser = createdUser;

    // Set optional fields
    setOptionalFields(
        orderSummaryData.getExpectedDeliveryDate(),
        orderSummaryData.getPromoId(),
        orderSummaryData.getTermsConditionsHtml(),
        orderSummaryData.getNotes());
  }

  /**
   * Constructor for updating an existing order summary from OrderSummaryData.
   *
   * @param entityType The entity type ('PURCHASE_ORDER' or 'ORDER')
   * @param entityId The ID of the parent entity
   * @param orderSummaryData The order summary data from request model (contains all financial and
   *     fulfillment data)
   * @param entityAddressId The delivery/shipping address ID (computed separately from address)
   * @param modifiedUser The user modifying the order summary
   * @param existingOrderSummary The existing order summary entity
   */
  public OrderSummary(
      String entityType,
      Long entityId,
      com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.OrderSummaryData
          orderSummaryData,
      Long entityAddressId,
      String modifiedUser,
      OrderSummary existingOrderSummary) {
    validateRequest(entityType, entityId, orderSummaryData, entityAddressId, null, modifiedUser);

    this.orderSummaryId = existingOrderSummary.getOrderSummaryId();
    this.createdUser = existingOrderSummary.getCreatedUser();
    this.createdAt = existingOrderSummary.getCreatedAt();
    this.clientId = existingOrderSummary.getClientId();
    this.pendingAmount = existingOrderSummary.getPendingAmount(); // Preserve pending amount

    this.entityType = entityType.trim();
    this.entityId = entityId;
    this.productsSubtotal = orderSummaryData.getProductsSubtotal();
    this.totalDiscount =
        orderSummaryData.getTotalDiscount() != null
            ? orderSummaryData.getTotalDiscount()
            : BigDecimal.ZERO;
    this.packagingFee =
        orderSummaryData.getPackagingFee() != null
            ? orderSummaryData.getPackagingFee()
            : BigDecimal.ZERO;
    this.totalShipping =
        orderSummaryData.getTotalShipping() != null
            ? orderSummaryData.getTotalShipping()
            : BigDecimal.ZERO;
    this.serviceFee =
        orderSummaryData.getServiceFee() != null
            ? orderSummaryData.getServiceFee()
            : BigDecimal.ZERO;
    this.gstPercentage =
        orderSummaryData.getGstPercentage() != null
            ? orderSummaryData.getGstPercentage()
            : new BigDecimal("18.00");

    // Recalculate subtotal, GST, and grand total
    this.subtotal =
        this.productsSubtotal
            .subtract(this.totalDiscount)
            .add(this.packagingFee)
            .add(this.totalShipping)
            .add(this.serviceFee);

    this.gstAmount =
        this.subtotal
            .multiply(this.gstPercentage.divide(new BigDecimal("100")))
            .setScale(2, java.math.RoundingMode.HALF_UP);

    this.grandTotal = this.subtotal.add(this.gstAmount);

    this.entityAddressId = entityAddressId;
    this.priority = orderSummaryData.getPriority().trim();
    this.modifiedUser = modifiedUser;

    // Set optional fields (overwrites preserved values from existingOrderSummary)
    setOptionalFields(
        orderSummaryData.getExpectedDeliveryDate(),
        orderSummaryData.getPromoId(),
        orderSummaryData.getTermsConditionsHtml(),
        orderSummaryData.getNotes());
  }

  /**
   * Validates all request data for OrderSummary creation/update.
   *
   * @param entityType The entity type ('PURCHASE_ORDER' or 'ORDER')
   * @param entityId The ID of the parent entity
   * @param orderSummaryData The order summary data from request model
   * @param entityAddressId The delivery/shipping address ID
   * @param clientId The client ID (required for create, null for update)
   * @param user The user (createdUser for create, modifiedUser for update)
   */
  private void validateRequest(
      String entityType,
      Long entityId,
      com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.OrderSummaryData
          orderSummaryData,
      Long entityAddressId,
      Long clientId,
      String user) {
    if (orderSummaryData == null) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.INVALID_REQUEST);
    }

    // Validate entity type
    if (entityType == null || entityType.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.ENTITY_TYPE_REQUIRED);
    }
    if (!EntityType.isValid(entityType.trim())) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.INVALID_ENTITY_TYPE);
    }

    // Validate entity ID
    if (entityId == null || entityId <= 0) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.ENTITY_ID_REQUIRED);
    }

    // Validate financial amounts
    if (orderSummaryData.getProductsSubtotal() == null) {
      throw new BadRequestException(
          ErrorMessages.OrderSummaryErrorMessages.PRODUCTS_SUBTOTAL_REQUIRED);
    }
    if (orderSummaryData.getProductsSubtotal().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(
          ErrorMessages.OrderSummaryErrorMessages.PRODUCTS_SUBTOTAL_INVALID);
    }

    if (orderSummaryData.getTotalDiscount() != null
        && orderSummaryData.getTotalDiscount().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.TOTAL_DISCOUNT_INVALID);
    }

    if (orderSummaryData.getPackagingFee() != null
        && orderSummaryData.getPackagingFee().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.PACKAGING_FEE_INVALID);
    }

    if (orderSummaryData.getTotalShipping() != null
        && orderSummaryData.getTotalShipping().compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.TOTAL_SHIPPING_INVALID);
    }

    // Validate GST percentage
    if (orderSummaryData.getGstPercentage() != null
        && (orderSummaryData.getGstPercentage().compareTo(BigDecimal.ZERO) < 0
            || orderSummaryData.getGstPercentage().compareTo(new BigDecimal("100")) > 0)) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.INVALID_GST_PERCENTAGE);
    }

    // Validate address ID
    if (entityAddressId == null || entityAddressId <= 0) {
      throw new BadRequestException(
          ErrorMessages.OrderSummaryErrorMessages.ENTITY_ADDRESS_ID_REQUIRED);
    }

    // Validate priority
    if (orderSummaryData.getPriority() == null || orderSummaryData.getPriority().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.PRIORITY_REQUIRED);
    }
    if (!Priority.isValid(orderSummaryData.getPriority().trim())) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.INVALID_PRIORITY);
    }

    // Validate client ID (only for create)
    if (clientId != null && (clientId <= 0)) {
      throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_ID);
    }

    // Validate user
    if (user == null || user.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
    }
  }

  /**
   * Sets optional fields.
   *
   * @param expectedDeliveryDate The expected delivery date
   * @param promoId The promo ID
   * @param termsConditionsHtml The terms and conditions HTML
   * @param notes Additional notes
   */
  public void setOptionalFields(
      LocalDateTime expectedDeliveryDate, Long promoId, String termsConditionsHtml, String notes) {
    this.expectedDeliveryDate = expectedDeliveryDate;
    this.promoId = promoId;
    this.termsConditionsHtml = termsConditionsHtml != null ? termsConditionsHtml.trim() : null;
    this.notes = notes != null ? notes.trim() : null;
  }

  /** Updates pending amount (for payment tracking). */
  public void updatePendingAmount(BigDecimal paidAmount) {
    if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException(ErrorMessages.OrderSummaryErrorMessages.PAID_AMOUNT_INVALID);
    }
    if (paidAmount.compareTo(this.grandTotal) > 0) {
      throw new BadRequestException(
          ErrorMessages.OrderSummaryErrorMessages.PAID_AMOUNT_EXCEEDS_GRAND_TOTAL);
    }
    this.pendingAmount = this.grandTotal.subtract(paidAmount);
  }
}

