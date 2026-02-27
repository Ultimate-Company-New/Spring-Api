package com.example.SpringApi.Models.DatabaseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "`Address`")
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "addressId", nullable = false)
  private Long addressId;

  @Column(name = "userId")
  private Long userId;

  @Column(name = "clientId")
  private Long clientId;

  @Column(name = "addressType", nullable = false)
  private String addressType;

  @Column(name = "streetAddress", nullable = false)
  private String streetAddress;

  @Column(name = "streetAddress2")
  private String streetAddress2;

  @Column(name = "streetAddress3")
  private String streetAddress3;

  @Column(name = "city", nullable = false)
  private String city;

  @Column(name = "state", nullable = false)
  private String state;

  @Column(name = "postalCode", nullable = false)
  private String postalCode;

  @Column(name = "nameOnAddress")
  private String nameOnAddress;

  @Column(name = "emailOnAddress")
  private String emailOnAddress;

  @Column(name = "phoneOnAddress")
  private String phoneOnAddress;

  @Column(name = "country", nullable = false)
  private String country;

  @Column(name = "isPrimary", nullable = false)
  private Boolean isPrimary;

  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", insertable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

  private static final Set<String> VALID_ADDRESS_TYPES =
      new HashSet<>(Set.of("HOME", "WORK", "BILLING", "SHIPPING", "OFFICE", "WAREHOUSE"));

  public Address() {}

  // Constructor for creating new address
  public Address(AddressRequestModel request, String createdUser) {
    validateRequest(request);
    validateUser(createdUser);

    setFieldsFromRequest(request);
    this.createdUser = createdUser;
    this.modifiedUser = createdUser; // When creating, modified user is same as created user
  }

  // Constructor for updating existing address
  public Address(AddressRequestModel request, String modifiedUser, Address existingAddress) {
    validateRequest(request);
    validateUser(modifiedUser);

    // Copy existing values that shouldn't change
    this.addressId = existingAddress.getAddressId();
    this.createdUser = existingAddress.getCreatedUser();
    this.createdAt = existingAddress.getCreatedAt();

    setFieldsFromRequest(request);
    this.modifiedUser = modifiedUser; // When updating, use the provided modified user
  }

  public void validateRequest(AddressRequestModel request) {
    if (request == null) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }

    // Validate street address (required, length > 0)
    if (request.getStreetAddress() == null || request.getStreetAddress().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }

    // Validate city (required, length > 0)
    if (request.getCity() == null || request.getCity().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER002);
    }

    // Validate state (required, length > 0)
    if (request.getState() == null || request.getState().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER003);
    }

    // Validate postal code (required, length > 0)
    if (request.getPostalCode() == null || request.getPostalCode().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER004);
    }
    if (!request.getPostalCode().matches("\\d{5,6}")) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER007);
    }

    // Validate country (required, length > 0)
    if (request.getCountry() == null || request.getCountry().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER005);
    }

    // Validate address type (must be one of the allowed values)
    if (request.getAddressType() == null
        || !VALID_ADDRESS_TYPES.contains(request.getAddressType().toUpperCase())) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER006);
    }

    // Validate user ID (if provided, must be positive)
    if (request.getUserId() != null && request.getUserId() <= 0) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    // Validate client ID (if provided, must be positive)
    if (request.getClientId() != null && request.getClientId() <= 0) {
      throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_ID);
    }
  }

  private void validateUser(String user) {
    if (user == null || user.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
    }
  }

  private void setFieldsFromRequest(AddressRequestModel request) {
    this.userId = request.getUserId();
    this.clientId = request.getClientId();
    this.addressType = request.getAddressType().toUpperCase(); // Normalize to uppercase
    this.streetAddress = request.getStreetAddress().trim();
    this.streetAddress2 =
        request.getStreetAddress2() != null ? request.getStreetAddress2().trim() : null;
    this.streetAddress3 =
        request.getStreetAddress3() != null ? request.getStreetAddress3().trim() : null;
    this.city = request.getCity().trim();
    this.state = request.getState().trim();
    this.postalCode = request.getPostalCode().trim();
    this.nameOnAddress =
        request.getNameOnAddress() != null ? request.getNameOnAddress().trim() : null;
    this.emailOnAddress =
        request.getEmailOnAddress() != null ? request.getEmailOnAddress().trim() : null;
    this.phoneOnAddress =
        request.getPhoneOnAddress() != null ? request.getPhoneOnAddress().trim() : null;
    this.country = request.getCountry() != null ? request.getCountry().trim() : "India";
    this.isPrimary =
        request.getIsPrimary() != null && request.getIsPrimary(); // Default false if null
    this.isDeleted =
        request.getIsDeleted() != null && request.getIsDeleted(); // Default false if null
  }
}
