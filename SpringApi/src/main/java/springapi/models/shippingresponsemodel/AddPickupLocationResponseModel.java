package springapi.models.shippingresponsemodel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/** Represents the add pickup location response model component. */
@Getter
@Setter
public class AddPickupLocationResponseModel {
  private boolean success;

  @SerializedName("address")
  private Address pickupAddress;

  @SerializedName("pickup_id")
  private long pickupId;

  @SerializedName("company_name")
  private String companyName;

  @SerializedName("full_name")
  private String fullName;

  // Backward-compatible accessors for existing call sites using getAddress/setAddress.
  public Address getAddress() {
    return pickupAddress;
  }

  public void setAddress(Address address) {
    this.pickupAddress = address;
  }

  /** Represents the address component. */
  @Getter
  @Setter
  public static class Address {
    @SerializedName("company_id")
    private int companyId;

    @SerializedName("pickup_code")
    private String pickupCode;

    @SerializedName("address")
    private String addressLine1;

    @SerializedName("address_2")
    private String address2;

    @SerializedName("address_type")
    private Object addressType;

    private String city;
    private String state;
    private String country;
    private Object gstin;

    @SerializedName("pin_code")
    private String pinCode;

    private String phone;
    private String email;
    private String name;

    @SerializedName("alternate_phone")
    private Object alternatePhone;

    private Object lat;

    @SerializedName("long")
    private Object longitude;

    private int status;

    @SerializedName("phone_verified")
    private int phoneVerified;

    @SerializedName("rto_address_id")
    private long rtoAddressId;

    @SerializedName("extra_info")
    private String extraInfo;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("created_at")
    private String createdAt;

    private long id;
  }
}
