package com.example.springapi.models.responsemodels;

import com.example.springapi.models.databasemodels.Address;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the address response model component.
 */
@Getter
@Setter
public class AddressResponseModel {

  private Long addressId;
  private Long userId;
  private Long clientId;
  private String addressType;
  private String streetAddress;
  private String streetAddress2;
  private String streetAddress3;
  private String city;
  private String state;
  private String postalCode;
  private String nameOnAddress;
  private String emailOnAddress;
  private String phoneOnAddress;
  private String country;
  private Boolean isPrimary;
  private Boolean isDeleted;
  private LocalDateTime createdAt;
  private String createdUser;
  private LocalDateTime updatedAt;
  private String modifiedUser;
  private String notes;

  // Default constructor
  public AddressResponseModel() {}

  // Constructor that takes Address entity and populates response fields
  /**
   * Executes address response model.
   */
  public AddressResponseModel(Address address) {
    if (address != null) {
      this.addressId = address.getAddressId();
      this.userId = address.getUserId();
      this.clientId = address.getClientId();
      this.addressType = address.getAddressType();
      this.streetAddress = address.getStreetAddress();
      this.streetAddress2 = address.getStreetAddress2();
      this.streetAddress3 = address.getStreetAddress3();
      this.city = address.getCity();
      this.state = address.getState();
      this.postalCode = address.getPostalCode();
      this.nameOnAddress = address.getNameOnAddress();
      this.emailOnAddress = address.getEmailOnAddress();
      this.phoneOnAddress = address.getPhoneOnAddress();
      this.country = address.getCountry();
      this.isPrimary = address.getIsPrimary();
      this.isDeleted = address.getIsDeleted();
      this.createdAt = address.getCreatedAt();
      this.createdUser = address.getCreatedUser();
      this.updatedAt = address.getUpdatedAt();
      this.modifiedUser = address.getModifiedUser();
      this.notes = address.getNotes();
    }
  }
}
