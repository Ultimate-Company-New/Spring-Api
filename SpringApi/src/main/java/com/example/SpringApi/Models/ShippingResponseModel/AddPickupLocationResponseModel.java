package com.example.SpringApi.Models.ShippingResponseModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPickupLocationResponseModel {
    private boolean success;
    private Address address;
    private long pickup_id;
    private String company_name;
    private String full_name;

    @Getter
    @Setter
    public static class Address {
        private int company_id;
        private String pickup_code;
        private String address;
        private String address_2;
        private Object address_type;
        private String city;
        private String state;
        private String country;
        private Object gstin;
        private String pin_code;
        private String phone;
        private String email;
        private String name;
        private Object alternate_phone;
        private Object lat;

        @JsonProperty("long")
        private Object lng;
        private int status;
        private int phone_verified;
        private long rto_address_id;
        private String extra_info;
        private String updated_at;
        private String created_at;
        private long id;
    }
}