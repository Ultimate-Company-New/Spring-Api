package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Response model for PickupLocation operations.
 * 
 * This model contains all the fields returned when retrieving pickup location information.
 * It includes related entities and calculated fields for the UI.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PickupLocationResponseModel {
    
    private Long pickupLocationId;
    private String addressNickName;
    private Boolean isDeleted;
    private Long clientId;
    private Long pickupLocationAddressId;
    private Long shipRocketPickupLocationId;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String modifiedBy;
    private String notes;
    
    // Related entities
    private Address address;
    private Client client;
    
    // Additional computed fields
    private Boolean isActive;
    
    /**
     * Constructor to create response model from database entity.
     * 
     * @param pickupLocation The PickupLocation entity
     */
    public PickupLocationResponseModel(PickupLocation pickupLocation) {
        if (pickupLocation != null) {
            this.pickupLocationId = pickupLocation.getPickupLocationId();
            this.addressNickName = pickupLocation.getAddressNickName();
            this.isDeleted = pickupLocation.getIsDeleted();
            this.clientId = pickupLocation.getClientId();
            this.pickupLocationAddressId = pickupLocation.getPickupLocationAddressId();
            this.shipRocketPickupLocationId = pickupLocation.getShipRocketPickupLocationId();
            this.createdAt = pickupLocation.getCreatedAt();
            this.createdBy = pickupLocation.getCreatedBy();
            this.updatedAt = pickupLocation.getUpdatedAt();
            this.modifiedBy = pickupLocation.getModifiedBy();
            this.notes = pickupLocation.getNotes();
            
            // Set related entities if loaded
            this.address = pickupLocation.getAddress();
            this.client = pickupLocation.getClient();
            
            // Compute additional fields
            this.isActive = !this.isDeleted;
        }
    }
    
    /**
     * Constructor to create response model from database entity and separate address.
     * 
     * @param pickupLocation The PickupLocation entity
     * @param address The Address entity
     */
    public PickupLocationResponseModel(PickupLocation pickupLocation, Address address) {
       if (pickupLocation != null) {
            this.pickupLocationId = pickupLocation.getPickupLocationId();
            this.addressNickName = pickupLocation.getAddressNickName();
            this.isDeleted = pickupLocation.getIsDeleted();
            this.clientId = pickupLocation.getClientId();
            this.pickupLocationAddressId = pickupLocation.getPickupLocationAddressId();
            this.shipRocketPickupLocationId = pickupLocation.getShipRocketPickupLocationId();
            this.createdAt = pickupLocation.getCreatedAt();
            this.createdBy = pickupLocation.getCreatedBy();
            this.updatedAt = pickupLocation.getUpdatedAt();
            this.modifiedBy = pickupLocation.getModifiedBy();
            this.notes = pickupLocation.getNotes();
            
            // Set related entities if loaded
            this.address = pickupLocation.getAddress();
            this.client = pickupLocation.getClient();
            
            // Compute additional fields
            this.isActive = !this.isDeleted;
        }
        if (address != null) {
            this.address = address; // Override the address with the provided one
        }
    }
}