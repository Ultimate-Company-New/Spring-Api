package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Request model for order-level shipping optimization.
 * Takes a list of products with quantities and delivery postcode,
 * then calculates the optimal pickup location allocation to minimize
 * total cost (shipping + packaging).
 */
@Getter
@Setter
public class OrderOptimizationRequestModel {
    
    /**
     * Map of Product ID to requested quantity.
     * Example: { 123: 50, 456: 30, 789: 100 }
     */
    private Map<Long, Integer> productQuantities;
    
    /**
     * Delivery address postal code (destination).
     */
    private String deliveryPostcode;
    
    /**
     * Whether the order is Cash on Delivery.
     * Affects shipping rates from couriers.
     */
    private Boolean isCod;
    
    /**
     * Optional: User-defined product allocations.
     * Map of productId -> (Map of pickupLocationId -> quantity)
     * If provided, skips auto-optimization and uses these allocations directly.
     * Example: { 123: { 1: 30, 2: 20 }, 456: { 1: 50 } }
     * means product 123 gets 30 from location 1 and 20 from location 2.
     */
    private Map<Long, Map<Long, Integer>> customAllocations;
}

