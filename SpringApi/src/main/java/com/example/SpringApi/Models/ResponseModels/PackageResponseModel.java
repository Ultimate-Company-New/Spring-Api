package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageProduct;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Response model for Package operations.
 * 
 * This model is used for returning package information in API responses.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PackageResponseModel {
    private Long packageId;
    private String packageName;
    private Integer length;
    private Integer breadth;
    private Integer height;
    private BigDecimal maxWeight;
    private Integer standardCapacity;
    private BigDecimal pricePerUnit;
    private String packageType;
    private Long clientId;
    private Boolean isDeleted;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    
    /**
     * Map of pickup location ID to pickup location inventory data.
     * Shows package inventory at each pickup location with full audit information.
     * Uses PackagePickupLocationMappingResponseModel for complete mapping details.
     */
    private Map<Long, PackagePickupLocationMappingResponseModel> pickupLocationQuantities = new HashMap<>();
    
    // Shipment-specific fields (only populated when package is part of a shipment)
    private Integer quantityUsed;
    private BigDecimal totalCost;
    
    // Products in this package (only populated when package is part of a shipment)
    private List<ShipmentResponseModel.PackageProductResponseData> products = new ArrayList<>();

    /**
     * Constructor that creates a response model from a Package entity.
     * 
     * @param packageEntity The Package entity to convert
     */
    public PackageResponseModel(Package packageEntity) {
        if (packageEntity != null) {
            this.packageId = packageEntity.getPackageId();
            this.packageName = packageEntity.getPackageName();
            this.length = packageEntity.getLength();
            this.breadth = packageEntity.getBreadth();
            this.height = packageEntity.getHeight();
            this.maxWeight = packageEntity.getMaxWeight();
            this.standardCapacity = packageEntity.getStandardCapacity();
            this.pricePerUnit = packageEntity.getPricePerUnit();
            this.packageType = packageEntity.getPackageType();
            this.clientId = packageEntity.getClientId();
            this.isDeleted = packageEntity.getIsDeleted();
            this.createdUser = packageEntity.getCreatedUser();
            this.modifiedUser = packageEntity.getModifiedUser();
            this.createdAt = packageEntity.getCreatedAt();
            this.updatedAt = packageEntity.getUpdatedAt();
            this.notes = packageEntity.getNotes();
        }
    }
    
    /**
     * Constructor that creates a response model from a Package entity and ShipmentPackage entity.
     * Used when package is part of a shipment to include quantity used and total cost.
     * 
     * @param packageEntity The Package entity to populate from
     * @param shipmentPackage The ShipmentPackage entity to get usage details from
     */
    public PackageResponseModel(Package packageEntity, com.example.SpringApi.Models.DatabaseModels.ShipmentPackage shipmentPackage) {
        // First populate from Package
        this(packageEntity);
        
        // Then add shipment-specific fields
        if (shipmentPackage != null) {
            this.quantityUsed = shipmentPackage.getQuantityUsed();
            this.totalCost = shipmentPackage.getTotalCost();
            
            // Extract products in this package
            if (Hibernate.isInitialized(shipmentPackage.getShipmentPackageProducts()) && 
                shipmentPackage.getShipmentPackageProducts() != null) {
                for (ShipmentPackageProduct spp : shipmentPackage.getShipmentPackageProducts()) {
                    this.products.add(new ShipmentResponseModel.PackageProductResponseData(spp));
                }
            }
        }
    }
}
