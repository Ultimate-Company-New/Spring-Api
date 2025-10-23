package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;
import com.example.SpringApi.Models.DatabaseModels.Package;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
