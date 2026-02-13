package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAllPickupLocationsResponseModel {
    private Data data;

    @Getter
    @Setter
    public static class Data {
        @SerializedName("shipping_address")
        private List<ShippingAddress> shippingAddress;
        @SerializedName("allow_more")
        private String allowMore;
        @SerializedName("is_blackbox_seller")
        private boolean blackboxSeller;
        @SerializedName("company_name")
        private String companyName;
        @SerializedName("recent_addresses")
        private List<Object> recentAddresses;
    }

    @Getter
    @Setter
    public static class ShippingAddress {
        private long id;
        @SerializedName("pickup_location")
        private String pickupLocation;
        @SerializedName("address_type")
        private Object addressType;
        private String address;
        @SerializedName("address_2")
        private String address2;
        @SerializedName("updated_address")
        private boolean updatedAddress;
        @SerializedName("old_address")
        private String oldAddress;
        @SerializedName("old_address2")
        private String oldAddress2;
        private String tag;
        @SerializedName("tag_value")
        private String tagValue;
        private String instruction;
        private String city;
        private String state;
        private String country;
        @SerializedName("pin_code")
        private String pinCode;
        private String email;
        @SerializedName("is_first_mile_pickup")
        private int firstMilePickup;
        private String phone;
        private String name;
        @SerializedName("company_id")
        private int companyId;
        private Object gstin;
        @SerializedName("vendor_name")
        private Object vendorName;
        private int status;
        @SerializedName("phone_verified")
        private int phoneVerified;
        private String lat;
        @SerializedName("long")
        private String longitude;
        @SerializedName("open_time")
        private Object openTime;
        @SerializedName("close_time")
        private Object closeTime;
        @SerializedName("warehouse_code")
        private Object warehouseCode;
        @SerializedName("alternate_phone")
        private String alternatePhone;
        @SerializedName("rto_address_id")
        private int rtoAddressId;
        @SerializedName("lat_long_status")
        private int latLongStatus;
        @SerializedName("new")
        private int newField;
        @SerializedName("associated_rto_address")
        private Object associatedRtoAddress;
        @SerializedName("is_primary_location")
        private int primaryLocation;
    }
}
