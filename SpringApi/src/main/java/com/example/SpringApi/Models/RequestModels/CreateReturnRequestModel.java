package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request model for creating a return order from the frontend.
 * Contains the shipment ID and list of products to return with quantities and reasons.
 */
@Getter
@Setter
public class CreateReturnRequestModel {
    
    /**
     * The shipment ID to create return for
     */
    private Long shipmentId;
    
    /**
     * List of products to return
     */
    private List<ReturnProductItem> products;
    
    /**
     * Package dimensions for the return shipment
     */
    private BigDecimal length;
    private BigDecimal breadth;
    private BigDecimal height;
    private BigDecimal weight;
    
    /**
     * Inner class for product return items
     */
    @Getter
    @Setter
    public static class ReturnProductItem {
        /**
         * Product ID to return
         */
        private Long productId;
        
        /**
         * Quantity to return
         */
        private Integer quantity;
        
        /**
         * Return reason (must match ReturnReason enum)
         */
        private String reason;
        
        /**
         * Additional comments
         */
        private String comments;
    }
}
