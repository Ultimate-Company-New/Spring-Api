package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequestModel {

  private Long id;
  private Long userId;
  private Long clientId;
  private boolean includeDeleted;

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
}
