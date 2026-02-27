package com.example.springapi.models.responsemodels;

import com.example.springapi.models.databasemodels.PickupLocation;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

/**
 * Response model for PickupLocation operations.
 *
 * <p>This model contains all the fields returned when retrieving pickup location information. It
 * includes related entities and calculated fields for the UI.
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
  private AddressResponseModel address;
  private ClientResponseModel client;

  // Additional computed fields
  private Boolean isActive;

  // Count fields for products and packages at this location
  private Integer productCount;
  private Integer packageCount;

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
      if (pickupLocation.getAddress() != null
          && Hibernate.isInitialized(pickupLocation.getAddress())) {
        this.address = new AddressResponseModel(pickupLocation.getAddress());
      }
      if (pickupLocation.getClient() != null
          && Hibernate.isInitialized(pickupLocation.getClient())) {
        this.client = new ClientResponseModel(pickupLocation.getClient());
      }

      // Compute additional fields
      this.isActive = !this.isDeleted;
    }
  }
}
