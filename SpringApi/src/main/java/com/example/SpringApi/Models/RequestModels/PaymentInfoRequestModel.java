package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request model for PaymentInfo operations.
 * 
 * This model is used for creating and updating payment information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PaymentInfoRequestModel {
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
    private String notes;
}
