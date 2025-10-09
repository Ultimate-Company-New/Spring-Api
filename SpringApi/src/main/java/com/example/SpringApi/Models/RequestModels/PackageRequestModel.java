package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request model for Package operations.
 * 
 * This model is used for creating and updating package information.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PackageRequestModel {
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
    private AddressRequestModel address;
}
