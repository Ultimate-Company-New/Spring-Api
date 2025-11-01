package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.PaymentRefund;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response model for PaymentRefund operations.
 * 
 * This model is used for returning payment refund information in API responses.
 * Includes product IDs, reasons, and pricing information for each refunded product.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PaymentRefundResponseModel {
    private Long paymentRefundId;
    private String refundId;
    private BigDecimal amount;
    private String refundType;
    private String refundStatus;
    private String refundMethod;
    private String speed;
    private LocalDateTime approvedDate;
    private LocalDateTime processedDate;
    private LocalDateTime completedDate;
    private Long paymentId;
    private String razorpayId;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    // Nested collections with product and reason information
    private List<Long> productIds;
    private Map<Long, String> productIdToReasonMap; // productId -> reason
    private Map<Long, BigDecimal> productIdToPricingMap; // productId -> pricing
    private Map<Long, Integer> productIdToQuantityMap; // productId -> quantity

    /**
     * Constructor that creates a response model from a PaymentRefund entity.
     * Extracts product IDs, reasons, pricing, and quantities from the refund mappings.
     * 
     * @param paymentRefund The PaymentRefund entity to convert
     */
    public PaymentRefundResponseModel(PaymentRefund paymentRefund) {
        if (paymentRefund != null) {
            this.paymentRefundId = paymentRefund.getPaymentRefundId();
            this.refundId = paymentRefund.getRefundId();
            this.amount = paymentRefund.getAmount();
            this.refundType = paymentRefund.getRefundType();
            this.refundStatus = paymentRefund.getRefundStatus();
            this.refundMethod = paymentRefund.getRefundMethod();
            this.speed = paymentRefund.getSpeed();
            this.approvedDate = paymentRefund.getApprovedDate();
            this.processedDate = paymentRefund.getProcessedDate();
            this.completedDate = paymentRefund.getCompletedDate();
            this.paymentId = paymentRefund.getPaymentId();
            this.razorpayId = paymentRefund.getRazorpayId();
            this.createdUser = paymentRefund.getCreatedUser();
            this.modifiedUser = paymentRefund.getModifiedUser();
            this.createdAt = paymentRefund.getCreatedAt();
            this.updatedAt = paymentRefund.getUpdatedAt();
            this.notes = paymentRefund.getNotes();
            
            // Initialize collections
            this.productIds = new ArrayList<>();
            this.productIdToReasonMap = new HashMap<>();
            this.productIdToPricingMap = new HashMap<>();
            this.productIdToQuantityMap = new HashMap<>();
            
            // Extract product IDs and pricing from PaymentRefundProductMapping
            if (paymentRefund.getPaymentRefundProductMappings() != null && 
                !paymentRefund.getPaymentRefundProductMappings().isEmpty()) {
                paymentRefund.getPaymentRefundProductMappings().forEach(mapping -> {
                    Long productId = mapping.getProductId();
                    if (!this.productIds.contains(productId)) {
                        this.productIds.add(productId);
                    }
                    this.productIdToPricingMap.put(productId, mapping.getPricing());
                    this.productIdToQuantityMap.put(productId, mapping.getQuantity());
                });
            }
            
            // Extract reasons from PaymentRefundProductReasonMapping
            if (paymentRefund.getPaymentRefundProductReasonMappings() != null && 
                !paymentRefund.getPaymentRefundProductReasonMappings().isEmpty()) {
                paymentRefund.getPaymentRefundProductReasonMappings().forEach(mapping -> {
                    Long productId = mapping.getProductId();
                    if (!this.productIds.contains(productId)) {
                        this.productIds.add(productId);
                    }
                    this.productIdToReasonMap.put(productId, mapping.getReason());
                });
            }
        }
    }
}

