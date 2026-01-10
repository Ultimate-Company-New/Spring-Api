package com.example.SpringApi.Models.ResponseModels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response model for Razorpay order creation.
 * Contains all data needed by the frontend to open Razorpay checkout.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponseModel {
    /**
     * Razorpay order ID
     */
    private String orderId;
    
    /**
     * Amount in INR (for display)
     */
    private BigDecimal amount;
    
    /**
     * Amount in paise (for Razorpay)
     */
    private Long amountInPaise;
    
    /**
     * Currency code (INR)
     */
    private String currency;
    
    /**
     * Razorpay Key ID (public key for frontend)
     */
    private String razorpayKeyId;
    
    /**
     * Purchase order vendor number (for reference)
     */
    private String vendorNumber;
    
    /**
     * Purchase order ID
     */
    private Long purchaseOrderId;
    
    /**
     * Company/Business name
     */
    private String companyName;
    
    /**
     * Order description
     */
    private String description;
    
    /**
     * Customer name for prefill
     */
    private String prefillName;
    
    /**
     * Customer email for prefill
     */
    private String prefillEmail;
    
    /**
     * Customer phone for prefill
     */
    private String prefillPhone;
}

