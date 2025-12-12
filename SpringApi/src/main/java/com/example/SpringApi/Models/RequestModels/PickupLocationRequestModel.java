package com.example.SpringApi.Models.RequestModels;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request model for PickupLocation operations.
 * 
 * This model contains all the fields required for creating or updating a pickup location.
 * It includes validation constraints and business logic requirements.
 * 
 * Note: addressNickName has a max length of 36 characters due to Shiprocket API limitation.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PickupLocationRequestModel extends PaginationBaseRequestModel {
    
    private Long pickupLocationId;
    
    @Size(max = 36, message = "Location name must be 36 characters or less (Shiprocket limit)")
    private String addressNickName;
    private Boolean isDeleted;
    private Long pickupLocationAddressId;
    private Long shipRocketPickupLocationId;
    private String notes;
    
    // Additional fields for creation/update
    private AddressRequestModel address;
    
    // Product and Package inventory mappings
    private List<ProductPickupLocationMappingRequestModel> productMappings;
    private List<PackagePickupLocationMappingRequestModel> packageMappings;
}