package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for order-level shipping optimization.
 * Returns the cheapest allocation option with detailed breakdown 
 * of products, packaging, and shipping per location.
 * 
 * Reuses existing response models (ProductResponseModel, PickupLocationResponseModel, 
 * PackageResponseModel, ShippingCalculationResponseModel.CourierOption) so frontend 
 * can extract fields from familiar models.
 */
@Getter
@Setter
public class OrderOptimizationResponseModel {
    
    /**
     * Human-readable description of the allocation.
     * Example: "All from Bangalore Warehouse" or "Split: Mumbai + Bangalore"
     */
    private String description;
    
    /**
     * Total cost = packaging + shipping.
     */
    private BigDecimal totalCost = BigDecimal.ZERO;
    
    /**
     * Total packaging cost across all locations.
     */
    private BigDecimal totalPackagingCost = BigDecimal.ZERO;
    
    /**
     * Total shipping cost across all locations.
     */
    private BigDecimal totalShippingCost = BigDecimal.ZERO;
    
    /**
     * Number of separate shipments (one per location used).
     */
    private Integer shipmentCount;
    
    /**
     * List of shipments, one per pickup location used.
     */
    private List<Shipment> shipments = new ArrayList<>();
    
    /**
     * Whether this allocation can fulfill the entire order.
     */
    private Boolean canFulfillOrder = true;
    
    /**
     * If cannot fulfill, the shortfall amount.
     */
    private Integer shortfall;
    
    /**
     * Whether all shipments have available couriers.
     */
    private Boolean allCouriersAvailable = true;
    
    /**
     * Reason why allocation is not available (e.g., "No couriers for Mumbai route").
     */
    private String unavailabilityReason;
    
    /**
     * Total number of products in the request.
     */
    private Integer totalProductCount;
    
    /**
     * Total quantity of items across all products.
     */
    private Integer totalQuantity;
    
    /**
     * Error message if optimization failed (e.g., insufficient stock).
     */
    private String errorMessage;
    
    /**
     * Whether the optimization was successful.
     */
    private Boolean success = true;
    
    /**
     * Represents a shipment from one pickup location.
     */
    @Getter
    @Setter
    public static class Shipment {
        
        /**
         * Full pickup location details using existing PickupLocationResponseModel.
         * Frontend can access: pickupLocationId, addressNickName, address (with all address fields), etc.
         */
        private PickupLocationResponseModel pickupLocation;
        
        /**
         * Products being shipped from this location with their quantities.
         */
        private List<ProductAllocation> products = new ArrayList<>();
        
        /**
         * Total weight of this shipment in kg.
         */
        private BigDecimal totalWeightKgs = BigDecimal.ZERO;
        
        /**
         * Total quantity of items in this shipment.
         */
        private Integer totalQuantity = 0;
        
        /**
         * Packages used for this shipment, using existing PackageResponseModel.
         */
        private List<PackageUsage> packagesUsed = new ArrayList<>();
        
        /**
         * Total packaging cost for this location.
         */
        private BigDecimal packagingCost = BigDecimal.ZERO;
        
        /**
         * Shipping cost for this shipment (based on cheapest available courier).
         */
        private BigDecimal shippingCost = BigDecimal.ZERO;
        
        /**
         * Total cost for this shipment (packagingCost + shippingCost).
         */
        private BigDecimal totalCost = BigDecimal.ZERO;
        
        /**
         * Available courier options for this location (sorted by rate, cheapest first).
         * Frontend should select from this list.
         * Uses existing ShippingCalculationResponseModel.CourierOption.
         * Note: hasCouriersAvailable can be derived from availableCouriers.isEmpty()
         */
        private List<ShippingCalculationResponseModel.CourierOption> availableCouriers = new ArrayList<>();
    }
    
    /**
     * Represents a product allocation to a pickup location.
     */
    @Getter
    @Setter
    public static class ProductAllocation {
        
        /**
         * Full product details using existing ProductResponseModel.
         * Frontend can access: productId, title, weightKgs, length, breadth, height, 
         * mainImageUrl, price, discount, category, etc.
         */
        private ProductResponseModel product;
        
        /**
         * Quantity allocated from this location for this product.
         */
        private Integer allocatedQuantity;
        
        /**
         * Total weight for this product allocation (quantity * weightKgs).
         */
        private BigDecimal totalWeight;
    }
    
    /**
     * Represents package usage in a shipment.
     */
    @Getter
    @Setter
    public static class PackageUsage {
        
        /**
         * Full package details using existing PackageResponseModel.
         * Frontend can access: packageId, packageName, packageType, length, breadth, 
         * height, maxWeight, pricePerUnit, etc.
         */
        private PackageResponseModel packageInfo;
        
        /**
         * Number of this package type used.
         */
        private Integer quantityUsed;
        
        /**
         * Total cost for this package type (quantityUsed * pricePerUnit).
         */
        private BigDecimal totalCost;
        
        /**
         * List of product IDs packed in these packages.
         */
        private List<Long> productIds = new ArrayList<>();
        
        /**
         * Detailed breakdown of products and quantities in these packages.
         * Maps productId -> quantity packed in this package type.
         */
        private List<PackageProductDetail> productDetails = new ArrayList<>();
    }
    
    /**
     * Detail of a product packed in a package.
     * Only includes productId - full product details are in Shipment.products list.
     */
    @Getter
    @Setter
    public static class PackageProductDetail {
        
        /**
         * The product ID. Full product details can be found in Shipment.products.
         */
        private Long productId;
        
        /**
         * Quantity of this product in the package(s).
         */
        private Integer quantity;
    }
}
