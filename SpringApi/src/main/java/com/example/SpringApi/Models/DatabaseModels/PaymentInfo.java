package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.PaymentInfoRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the PaymentInfo table.
 * 
 * This entity represents payment information including totals, fees, taxes, and payment gateway details.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PaymentInfo")
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paymentId", nullable = false)
    private Long paymentId;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "tax", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "serviceFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "packagingFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal packagingFee;

    @Column(name = "discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "subTotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "deliveryFee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "pendingAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal pendingAmount;

    @Column(name = "paymentMethod", nullable = true, length = 50)
    private String paymentMethod;

    @Column(name = "paymentStatus", nullable = true, length = 50)
    private String paymentStatus;

    @Column(name = "paymentGateway", length = 100)
    private String paymentGateway;

    @Column(name = "cardLast4", length = 4)
    private String cardLast4;

    @Column(name = "cardBrand", length = 50)
    private String cardBrand;

    @Column(name = "paymentDate", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "processedDate")
    private LocalDateTime processedDate;

    @Column(name = "razorpayTransactionId")
    private String razorpayTransactionId;

    @Column(name = "razorpayReceipt")
    private String razorpayReceipt;

    @Column(name = "razorpayOrderId")
    private String razorpayOrderId;

    @Column(name = "razorpayPaymentNotes", columnDefinition = "TEXT")
    private String razorpayPaymentNotes;

    @Column(name = "razorpaySignature", length = 512)
    private String razorpaySignature;

    @Column(name = "promoId")
    private Long promoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoId", insertable = false, updatable = false)
    private Promo promo;

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
    public PaymentInfo() {}

    /**
     * Constructor for creating a new payment info.
     * 
     * @param request The PaymentInfoRequestModel containing the payment data
     * @param createdUser The user creating the payment info
     */
    public PaymentInfo(PaymentInfoRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }

    /**
     * Constructor for updating an existing payment info.
     * 
     * @param request The PaymentInfoRequestModel containing the updated payment data
     * @param modifiedUser The user modifying the payment info
     * @param existingPaymentInfo The existing payment info entity
     */
    public PaymentInfo(PaymentInfoRequestModel request, String modifiedUser, PaymentInfo existingPaymentInfo) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        this.paymentId = existingPaymentInfo.getPaymentId();
        this.createdUser = existingPaymentInfo.getCreatedUser();
        this.createdAt = existingPaymentInfo.getCreatedAt();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;
    }

    /**
     * Constructor for creating a new Payment Quotation from PurchaseOrderRequestModel.
     * This calculates payment info from the products list in the request model.
     * 
     * Calculation logic:
     * - subTotal: Sum of (pricePerUnit * quantity) for all products in request
     * - tax: 18.5% of subTotal
     * - deliveryFee: From request or default $50
     * - packagingFee: From request or estimated based on total items
     * - serviceFee: From request or default $25
     * - discount: From request or default $0
     * - total: subTotal + tax + deliveryFee + packagingFee + serviceFee - discount
     * - pendingAmount: Same as total (nothing paid yet)
     * 
     * @param request The PurchaseOrderRequestModel containing product items and optional fee information
     * @param createdUser The user creating the payment quotation
     */
    public PaymentInfo(com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel request, String createdUser) {
        validateUser(createdUser);
        
        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException("Purchase order must have at least one product for payment quotation");
        }
        
        // Calculate payment fields from request
        calculatePaymentFromRequest(request);
        
        // Set audit fields for new creation
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }

    /**
     * Constructor for updating an existing Payment Quotation from PurchaseOrderRequestModel.
     * This recalculates payment info from the products list in the request model.
     * 
     * @param request The PurchaseOrderRequestModel containing product items and optional fee information
     * @param modifiedUser The user updating the payment quotation
     * @param existingPaymentInfo The existing payment info to update
     */
    public PaymentInfo(com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel request, 
                      String modifiedUser, 
                      PaymentInfo existingPaymentInfo) {
        validateUser(modifiedUser);
        
        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException("Purchase order must have at least one product for payment quotation");
        }
        
        // Calculate payment fields from request
        calculatePaymentFromRequest(request);
        
        // Preserve existing audit fields and update modified user
        this.paymentId = existingPaymentInfo.getPaymentId();
        this.createdUser = existingPaymentInfo.getCreatedUser();
        this.createdAt = existingPaymentInfo.getCreatedAt();
        this.modifiedUser = modifiedUser;
    }

    /**
     * Helper method to calculate payment fields from PurchaseOrderRequestModel.
     * 
     * @param request The PurchaseOrderRequestModel containing product items and fee information
     */
    private void calculatePaymentFromRequest(com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel request) {
        // Calculate subTotal from product items in request
        BigDecimal subTotal = BigDecimal.ZERO;
        int totalItems = 0;
        
        for (com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem productItem : request.getProducts()) {
            int quantity = productItem.getQuantity();
            BigDecimal pricePerUnit = productItem.getPricePerUnit();
            
            totalItems += quantity;
            subTotal = subTotal.add(pricePerUnit.multiply(new BigDecimal(quantity)));
        }
        
        // Calculate tax (18.5% of subTotal)
        BigDecimal tax = subTotal.multiply(new BigDecimal("0.185")).setScale(2, java.math.RoundingMode.HALF_UP);
        
        // Use fees from request if provided, otherwise use defaults
        BigDecimal deliveryFee;
        if (request.getDeliveryFee() != null) {
            deliveryFee = request.getDeliveryFee();
        } else {
            deliveryFee = new BigDecimal("50.00"); // Default delivery fee
        }
        
        BigDecimal serviceFee;
        if (request.getServiceFee() != null) {
            serviceFee = request.getServiceFee();
        } else {
            serviceFee = new BigDecimal("25.00"); // Default service fee
        }
        
        BigDecimal packagingFee;
        if (request.getPackagingFee() != null) {
            packagingFee = request.getPackagingFee();
        } else {
            // Estimate packaging fee: 3 items per package, $2 per package
            int estimatedPackages = (int) Math.ceil(totalItems / 3.0);
            packagingFee = new BigDecimal(estimatedPackages * 2).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        BigDecimal discount;
        if (request.getDiscount() != null) {
            discount = request.getDiscount();
        } else {
            discount = BigDecimal.ZERO; // Default no discount
        }
        
        // Calculate total
        BigDecimal total = subTotal.add(tax).add(deliveryFee).add(packagingFee).add(serviceFee).subtract(discount);
        
        // Set all fields
        this.total = total;
        this.tax = tax;
        this.serviceFee = serviceFee;
        this.packagingFee = packagingFee;
        this.discount = discount;
        this.subTotal = subTotal;
        this.deliveryFee = deliveryFee;
        this.pendingAmount = total; // Nothing paid yet
        this.paymentMethod = null; // No payment method selected yet for quotation
        this.paymentStatus = null; // No payment status yet for quotation
        this.paymentDate = LocalDateTime.now();
        this.promoId = null; // No promo for quotations
        this.notes = "Payment quotation auto-generated for Purchase Order";
    }

    /**
     * Validates the request model.
     * 
     * @param request The PaymentInfoRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(PaymentInfoRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidRequest);
        }
        if (request.getTotal() == null || request.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidTotal);
        }
        if (request.getTax() == null || request.getTax().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidTax);
        }
        if (request.getServiceFee() == null || request.getServiceFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidServiceFee);
        }
        if (request.getPackagingFee() == null || request.getPackagingFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidPackagingFee);
        }
        if (request.getDiscount() == null || request.getDiscount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidDiscount);
        }
        if (request.getSubTotal() == null || request.getSubTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidSubTotal);
        }
        if (request.getDeliveryFee() == null || request.getDeliveryFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidDeliveryFee);
        }
        if (request.getPendingAmount() == null || request.getPendingAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidPendingAmount);
        }
        // paymentMethod and paymentStatus are now nullable (for quotations)
        if (request.getPaymentMethod() != null && request.getPaymentMethod().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidPaymentMethod);
        }
        if (request.getPaymentStatus() != null && request.getPaymentStatus().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidPaymentStatus);
        }
        if (request.getPaymentDate() == null) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidPaymentDate);
        }
        if (request.getCardLast4() != null && request.getCardLast4().length() != 4) {
            throw new BadRequestException(ErrorMessages.PaymentInfoErrorMessages.InvalidCardLast4);
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
     * @param request The PaymentInfoRequestModel to extract fields from
     */
    private void setFieldsFromRequest(PaymentInfoRequestModel request) {
        this.total = request.getTotal();
        this.tax = request.getTax();
        this.serviceFee = request.getServiceFee();
        this.packagingFee = request.getPackagingFee();
        this.discount = request.getDiscount();
        this.subTotal = request.getSubTotal();
        this.deliveryFee = request.getDeliveryFee();
        this.pendingAmount = request.getPendingAmount();
        this.paymentMethod = request.getPaymentMethod().trim();
        this.paymentStatus = request.getPaymentStatus().trim();
        this.paymentGateway = request.getPaymentGateway() != null ? request.getPaymentGateway().trim() : null;
        this.cardLast4 = request.getCardLast4() != null ? request.getCardLast4().trim() : null;
        this.cardBrand = request.getCardBrand() != null ? request.getCardBrand().trim() : null;
        this.paymentDate = request.getPaymentDate();
        this.processedDate = request.getProcessedDate();
        this.razorpayTransactionId = request.getRazorpayTransactionId() != null ? request.getRazorpayTransactionId().trim() : null;
        this.razorpayReceipt = request.getRazorpayReceipt() != null ? request.getRazorpayReceipt().trim() : null;
        this.razorpayOrderId = request.getRazorpayOrderId() != null ? request.getRazorpayOrderId().trim() : null;
        this.razorpayPaymentNotes = request.getRazorpayPaymentNotes() != null ? request.getRazorpayPaymentNotes().trim() : null;
        this.razorpaySignature = request.getRazorpaySignature() != null ? request.getRazorpaySignature().trim() : null;
        this.promoId = request.getPromoId();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}
