package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for PickupLocation operations.
 * 
 * This model contains all the fields required for creating or updating a pickup location.
 * It includes validation constraints and business logic requirements.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PickupLocationRequestModel extends PaginationBaseRequestModel {
    
    private Long pickupLocationId;
    private String addressNickName;
    private Boolean isDeleted;
    private Long clientId;
    private Long pickupLocationAddressId;
    private Long shipRocketPickupLocationId;
    private String notes;
    
    // Additional fields for creation/update
    private AddressRequestModel address;
}