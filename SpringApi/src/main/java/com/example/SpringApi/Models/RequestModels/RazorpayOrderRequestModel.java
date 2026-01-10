package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request model for creating a Razorpay order.
 * Used when initiating a payment for a purchase order.
 */
@Getter
@Setter
public class RazorpayOrderRequestModel {
    /**
     * The purchase order ID to create payment for
     */
    private Long purchaseOrderId;
    
    /**
     * Amount to be paid in INR (will be converted to paise for Razorpay)
     */
    private BigDecimal amount;
    
    /**
     * Optional: Customer name for Razorpay prefill
     */
    private String customerName;
    
    /**
     * Optional: Customer email for Razorpay prefill
     */
    private String customerEmail;
    
    /**
     * Optional: Customer phone for Razorpay prefill
     */
    private String customerPhone;
}

