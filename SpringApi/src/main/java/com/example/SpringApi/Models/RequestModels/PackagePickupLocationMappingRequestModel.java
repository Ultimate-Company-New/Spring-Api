package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Request model for Package Pickup Location Mapping operations.
 * 
 * This model is used for creating and updating package inventory at pickup locations.
 * Contains only the fields that can be set by the client during create/update operations.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PackagePickupLocationMappingRequestModel {
    
    /**
     * Available quantity of packages at this location.
     * Maps to the availableQuantity column in PackagePickupLocationMapping table.
     */
    private Integer quantity;
    
    /**
     * When to reorder packages - alert threshold.
     * Maps to the reorderLevel column (default 10).
     */
    private Integer reorderLevel;
    
    /**
     * Maximum stock level to maintain at this location.
     * Maps to the maxStockLevel column (default 1000).
     */
    private Integer maxStockLevel;
    
    /**
     * When packages were last restocked at this location.
     * Maps to the lastRestockDate column (nullable).
     */
    private LocalDateTime lastRestockDate;
    
    /**
     * Optional notes about this mapping.
     * Maps to the notes column.
     */
    private String notes;
    
    /**
     * Default constructor.
     */
    public PackagePickupLocationMappingRequestModel() {}
    
    /**
     * Constructor with all fields.
     * 
     * @param quantity Available quantity of packages
     * @param reorderLevel Threshold for reorder alerts
     * @param maxStockLevel Maximum stock level to maintain
     * @param lastRestockDate When packages were last restocked
     * @param notes Optional notes
     */
    public PackagePickupLocationMappingRequestModel(Integer quantity, Integer reorderLevel, 
            Integer maxStockLevel, LocalDateTime lastRestockDate, String notes) {
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.maxStockLevel = maxStockLevel;
        this.lastRestockDate = lastRestockDate;
        this.notes = notes;
    }
    
    /**
     * Constructor with essential fields (without notes).
     * 
     * @param quantity Available quantity of packages
     * @param reorderLevel Threshold for reorder alerts
     * @param maxStockLevel Maximum stock level to maintain
     * @param lastRestockDate When packages were last restocked
     */
    public PackagePickupLocationMappingRequestModel(Integer quantity, Integer reorderLevel, 
            Integer maxStockLevel, LocalDateTime lastRestockDate) {
        this(quantity, reorderLevel, maxStockLevel, lastRestockDate, null);
    }
}

