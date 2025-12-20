package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;

/**
 * Interface for shipping-related operations.
 * 
 * Defines the contract for shipping service implementations including
 * order-level shipping calculations that can combine multiple products
 * from the same pickup location.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IShippingSubTranslator {
    
    /**
     * Calculate shipping options for an order.
     * Groups products by pickup location and returns available couriers for each location.
     * 
     * @param request Contains delivery postcode, COD flag, and list of pickup locations with weights
     * @return Shipping options for each pickup location with available couriers
     */
    ShippingCalculationResponseModel calculateShipping(ShippingCalculationRequestModel request);
    
    /**
     * Optimize order fulfillment across multiple pickup locations.
     * Finds the optimal allocation of products to pickup locations that minimizes
     * total cost (shipping + packaging), considering:
     * - Product availability at each location
     * - Packaging capacity and costs at each location
     * - Shipping rates (tiered/slab-based) from each location to delivery address
     * - Consolidation benefits (multiple products in same shipment)
     * 
     * @param request Contains map of product IDs to quantities and delivery postcode
     * @return Allocation options ranked by total cost with detailed breakdown
     */
    OrderOptimizationResponseModel optimizeOrder(OrderOptimizationRequestModel request);
}
