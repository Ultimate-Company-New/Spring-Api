package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PaymentInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model for PaymentInfo operations.
 * 
 * This model is used for returning payment information in API responses.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PaymentInfoResponseModel {
    private Long paymentId;
    private BigDecimal total;
    private BigDecimal tax;
    private BigDecimal serviceFee;
    private BigDecimal packagingFee;
    private BigDecimal discount;
    private BigDecimal subTotal;
    private BigDecimal deliveryFee;
    private BigDecimal pendingAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentGateway;
    private String cardLast4;
    private String cardBrand;
    private LocalDateTime paymentDate;
    private LocalDateTime processedDate;
    private String razorpayTransactionId;
    private String razorpayReceipt;
    private String razorpayOrderId;
    private String razorpayPaymentNotes;
    private String razorpaySignature;
    private Long promoId;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    /**
     * Constructor that creates a response model from a PaymentInfo entity.
     * 
     * @param paymentInfo The PaymentInfo entity to convert
     */
    public PaymentInfoResponseModel(PaymentInfo paymentInfo) {
        if (paymentInfo != null) {
            this.paymentId = paymentInfo.getPaymentId();
            this.total = paymentInfo.getTotal();
            this.tax = paymentInfo.getTax();
            this.serviceFee = paymentInfo.getServiceFee();
            this.packagingFee = paymentInfo.getPackagingFee();
            this.discount = paymentInfo.getDiscount();
            this.subTotal = paymentInfo.getSubTotal();
            this.deliveryFee = paymentInfo.getDeliveryFee();
            this.pendingAmount = paymentInfo.getPendingAmount();
            this.paymentMethod = paymentInfo.getPaymentMethod();
            this.paymentStatus = paymentInfo.getPaymentStatus();
            this.paymentGateway = paymentInfo.getPaymentGateway();
            this.cardLast4 = paymentInfo.getCardLast4();
            this.cardBrand = paymentInfo.getCardBrand();
            this.paymentDate = paymentInfo.getPaymentDate();
            this.processedDate = paymentInfo.getProcessedDate();
            this.razorpayTransactionId = paymentInfo.getRazorpayTransactionId();
            this.razorpayReceipt = paymentInfo.getRazorpayReceipt();
            this.razorpayOrderId = paymentInfo.getRazorpayOrderId();
            this.razorpayPaymentNotes = paymentInfo.getRazorpayPaymentNotes();
            this.razorpaySignature = paymentInfo.getRazorpaySignature();
            this.promoId = paymentInfo.getPromoId();
            this.createdUser = paymentInfo.getCreatedUser();
            this.modifiedUser = paymentInfo.getModifiedUser();
            this.createdAt = paymentInfo.getCreatedAt();
            this.updatedAt = paymentInfo.getUpdatedAt();
            this.notes = paymentInfo.getNotes();
        }
    }
}
