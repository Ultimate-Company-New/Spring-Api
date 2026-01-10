package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model for OrderSummary operations.
 * 
 * This model contains financial breakdown and fulfillment details for an order summary.
 * Used in PurchaseOrderResponseModel and can be used independently.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class OrderSummaryResponseModel {
    private Long orderSummaryId;
    
    // Financial Breakdown
    private BigDecimal productsSubtotal;
    private BigDecimal totalDiscount;
    private BigDecimal packagingFee;
    private BigDecimal totalShipping;
    private BigDecimal serviceFee;
    private BigDecimal subtotal;
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal grandTotal;
    private BigDecimal pendingAmount;
    
    // Fulfillment Details
    private LocalDateTime expectedDeliveryDate;
    private AddressResponseModel address; // Delivery/shipping address
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    // Promotion & Terms (Optional)
    private Long promoId;
    private PromoResponseModel promo;
    private String termsConditionsHtml;
    private String notes;
    
    /**
     * Default constructor.
     */
    public OrderSummaryResponseModel() {}
    
    /**
     * Constructor that creates a response model from an OrderSummary entity.
     * 
     * @param orderSummary The OrderSummary entity to convert
     */
    public OrderSummaryResponseModel(OrderSummary orderSummary) {
        if (orderSummary != null) {
            this.orderSummaryId = orderSummary.getOrderSummaryId();
            this.productsSubtotal = orderSummary.getProductsSubtotal();
            this.totalDiscount = orderSummary.getTotalDiscount();
            this.packagingFee = orderSummary.getPackagingFee();
            this.totalShipping = orderSummary.getTotalShipping();
            this.serviceFee = orderSummary.getServiceFee();
            this.subtotal = orderSummary.getSubtotal();
            this.gstPercentage = orderSummary.getGstPercentage();
            this.gstAmount = orderSummary.getGstAmount();
            this.grandTotal = orderSummary.getGrandTotal();
            this.pendingAmount = orderSummary.getPendingAmount();
            this.expectedDeliveryDate = orderSummary.getExpectedDeliveryDate();
            this.priority = orderSummary.getPriority();
            this.promoId = orderSummary.getPromoId();
            this.termsConditionsHtml = orderSummary.getTermsConditionsHtml();
            this.notes = orderSummary.getNotes();
            
            // Extract Address
            if (orderSummary.getEntityAddress() != null) {
                this.address = new AddressResponseModel(orderSummary.getEntityAddress());
            }
            
            // Extract Promo
            if (orderSummary.getPromo() != null) {
                this.promo = new PromoResponseModel(orderSummary.getPromo());
            }
        }
    }
}
