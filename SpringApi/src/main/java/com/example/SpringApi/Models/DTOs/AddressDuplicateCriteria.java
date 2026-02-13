package com.example.SpringApi.Models.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDuplicateCriteria {
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
}
