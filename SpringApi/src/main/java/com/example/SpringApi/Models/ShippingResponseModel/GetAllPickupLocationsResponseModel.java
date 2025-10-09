package com.example.SpringApi.Models.ShippingResponseModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAllPickupLocationsResponseModel{
    public Data data;

    @Getter
    @Setter
    public static class Data{
        public List<ShippingAddress> shipping_address;
        public String allow_more;
        public boolean is_blackbox_seller;
        public String company_name;
        public List<Object> recent_addresses;
    }

    @Getter
    @Setter
    public static class ShippingAddress{
        public long id;
        public String pickup_location;
        public Object address_type;
        public String address;
        public String address_2;
        public boolean updated_address;
        public String old_address;
        public String old_address2;
        public String tag;
        public String tag_value;
        public String instruction;
        public String city;
        public String state;
        public String country;
        public String pin_code;
        public String email;
        public int is_first_mile_pickup;
        public String phone;
        public String name;
        public int company_id;
        public Object gstin;
        public Object vendor_name;
        public int status;
        public int phone_verified;
        public String lat;
        @JsonProperty("long")
        public String mylong;
        public Object open_time;
        public Object close_time;
        public Object warehouse_code;
        public String alternate_phone;
        public int rto_address_id;
        public int lat_long_status;
        @JsonProperty("new")
        public int mynew;
        public Object associated_rto_address;
        public int is_primary_location;
    }
}