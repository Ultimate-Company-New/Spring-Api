package com.example.SpringApi.Models.ShippingResponseModel;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ShipRocket Get Order Details API.
 *
 * <p>API: GET https://apiv2.shiprocket.in/v1/external/orders/show/{order_id}
 *
 * <p>This model captures the complete order details returned by ShipRocket after an order is
 * created, including shipment details, AWB data, and more.
 *
 * @author SpringApi Team
 * @version 1.0
 */
@Getter
@Setter
public class ShipRocketOrderDetailsResponseModel {

  @SerializedName("data")
  private OrderDetailsData data;

  /** Main order details data wrapper. */
  @Getter
  @Setter
  public static class OrderDetailsData {
    @SerializedName("id")
    private Long id;

    @SerializedName("channel_id")
    private Long channelId;

    @SerializedName("channel_name")
    private String channelName;

    @SerializedName("base_channel_code")
    private String baseChannelCode;

    @SerializedName("is_international")
    private Integer isInternational;

    @SerializedName("is_document")
    private Integer isDocument;

    @SerializedName("channel_order_id")
    private String channelOrderId;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_email")
    private String customerEmail;

    @SerializedName("customer_phone")
    private String customerPhone;

    @SerializedName("customer_address")
    private String customerAddress;

    @SerializedName("customer_address_2")
    private String customerAddress2;

    @SerializedName("customer_city")
    private String customerCity;

    @SerializedName("customer_state")
    private String customerState;

    @SerializedName("customer_pincode")
    private String customerPincode;

    @SerializedName("customer_country")
    private String customerCountry;

    @SerializedName("pickup_code")
    private String pickupCode;

    @SerializedName("pickup_location")
    private String pickupLocation;

    @SerializedName("pickup_location_id")
    private String pickupLocationId;

    @SerializedName("pickup_id")
    private String pickupId;

    @SerializedName("ship_type")
    private String shipType;

    @SerializedName("courier_mode")
    private String courierMode;

    @SerializedName("currency")
    private String currency;

    @SerializedName("country_code")
    private Integer countryCode;

    @SerializedName("payment_status")
    private String paymentStatus;

    @SerializedName("delivery_code")
    private String deliveryCode;

    @SerializedName("total")
    private Double total;

    @SerializedName("net_total")
    private String netTotal;

    @SerializedName("other_charges")
    private String otherCharges;

    @SerializedName("other_discounts")
    private String otherDiscounts;

    @SerializedName("giftwrap_charges")
    private String giftwrapCharges;

    @SerializedName("sla")
    private String sla;

    @SerializedName("cod")
    private Integer cod;

    @SerializedName("tax")
    private Double tax;

    @SerializedName("discount")
    private Double discount;

    @SerializedName("status")
    private String status;

    @SerializedName("sub_status")
    private String subStatus;

    @SerializedName("status_code")
    private Integer statusCode;

    @SerializedName("master_status")
    private String masterStatus;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("channel_created_at")
    private String channelCreatedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("order_date")
    private String orderDate;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("products")
    private List<OrderProduct> products;

    @SerializedName("shipments")
    private ShipmentDetails shipments;

    @SerializedName("awb_data")
    private AwbData awbData;

    @SerializedName("order_insurance")
    private OrderInsurance orderInsurance;

    @SerializedName("return_pickup_data")
    private ReturnPickupData returnPickupData;

    @SerializedName("company_logo")
    private String companyLogo;

    @SerializedName("allow_return")
    private Integer allowReturn;

    @SerializedName("is_return")
    private Integer isReturn;

    @SerializedName("is_incomplete")
    private Integer isIncomplete;

    @SerializedName("errors")
    private Object errors;

    @SerializedName("billing_city")
    private String billingCity;

    @SerializedName("billing_name")
    private String billingName;

    @SerializedName("billing_email")
    private String billingEmail;

    @SerializedName("billing_phone")
    private String billingPhone;

    @SerializedName("billing_state_name")
    private String billingStateName;

    @SerializedName("billing_address")
    private String billingAddress;

    @SerializedName("billing_country_name")
    private String billingCountryName;

    @SerializedName("billing_pincode")
    private String billingPincode;

    @SerializedName("billing_address_2")
    private String billingAddress2;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("shipping_method")
    private String shippingMethod;

    @SerializedName("eway_bill_number")
    private String ewayBillNumber;

    @SerializedName("eway_bill_url")
    private String ewayBillUrl;

    @SerializedName("eway_required")
    private Boolean ewayRequired;

    @SerializedName("irn_no")
    private String irnNo;

    @SerializedName("etd_date")
    private String etdDate;

    @SerializedName("out_for_delivery_date")
    private String outForDeliveryDate;

    @SerializedName("delivered_date")
    private String deliveredDate;

    @SerializedName("remittance_date")
    private String remittanceDate;

    @SerializedName("remittance_utr")
    private String remittanceUtr;

    @SerializedName("remittance_status")
    private String remittanceStatus;
  }

  /** Product details within the order. */
  @Getter
  @Setter
  public static class OrderProduct {
    @SerializedName("id")
    private Long id;

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("product_id")
    private Long productId;

    @SerializedName("name")
    private String name;

    @SerializedName("sku")
    private String sku;

    @SerializedName("description")
    private String description;

    @SerializedName("hsn")
    private String hsn;

    @SerializedName("brand")
    private String brand;

    @SerializedName("color")
    private String color;

    @SerializedName("size")
    private String size;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("dimensions")
    private String dimensions;

    @SerializedName("price")
    private Double price;

    @SerializedName("cost")
    private Double cost;

    @SerializedName("mrp")
    private Double mrp;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("tax")
    private Double tax;

    @SerializedName("status")
    private Integer status;

    @SerializedName("net_total")
    private Double netTotal;

    @SerializedName("discount")
    private Double discount;

    @SerializedName("selling_price")
    private Double sellingPrice;

    @SerializedName("tax_percentage")
    private Double taxPercentage;
  }

  /** Shipment details within the order. */
  @Getter
  @Setter
  public static class ShipmentDetails {
    @SerializedName("id")
    private Long id;

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("awb")
    private String awb;

    @SerializedName("rto_awb")
    private String rtoAwb;

    @SerializedName("awb_assign_date")
    private String awbAssignDate;

    @SerializedName("etd")
    private String etd;

    @SerializedName("delivered_date")
    private String deliveredDate;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("cod_charges")
    private String codCharges;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("volumetric_weight")
    private Double volumetricWeight;

    @SerializedName("dimensions")
    private String dimensions;

    @SerializedName("courier")
    private String courier;

    @SerializedName("courier_id")
    private String courierId;

    @SerializedName("manifest_id")
    private String manifestId;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("length")
    private Integer length;

    @SerializedName("breadth")
    private Integer breadth;

    @SerializedName("height")
    private Integer height;

    @SerializedName("rto_initiated_date")
    private String rtoInitiatedDate;

    @SerializedName("rto_delivered_date")
    private String rtoDeliveredDate;

    @SerializedName("shipped_date")
    private String shippedDate;

    @SerializedName("invoice_link")
    private String invoiceLink;

    @SerializedName("is_rto")
    private Boolean isRto;
  }

  /** AWB (Air Waybill) data. */
  @Getter
  @Setter
  public static class AwbData {
    @SerializedName("awb")
    private String awb;

    @SerializedName("applied_weight")
    private String appliedWeight;

    @SerializedName("charged_weight")
    private String chargedWeight;

    @SerializedName("billed_weight")
    private String billedWeight;

    @SerializedName("routing_code")
    private String routingCode;

    @SerializedName("rto_routing_code")
    private String rtoRoutingCode;

    @SerializedName("charges")
    private AwbCharges charges;
  }

  /** AWB charges breakdown. */
  @Getter
  @Setter
  public static class AwbCharges {
    @SerializedName("zone")
    private String zone;

    @SerializedName("cod_charges")
    private String codCharges;

    @SerializedName("applied_weight_amount")
    private String appliedWeightAmount;

    @SerializedName("freight_charges")
    private String freightCharges;

    @SerializedName("applied_weight")
    private String appliedWeight;

    @SerializedName("charged_weight")
    private String chargedWeight;

    @SerializedName("charged_weight_amount")
    private String chargedWeightAmount;
  }

  /** Order insurance details. */
  @Getter
  @Setter
  public static class OrderInsurance {
    @SerializedName("insurance_status")
    private String insuranceStatus;

    @SerializedName("policy_no")
    private String policyNo;

    @SerializedName("claim_enable")
    private Boolean claimEnable;
  }

  /** Return pickup data. */
  @Getter
  @Setter
  public static class ReturnPickupData {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("address")
    private String address;

    @SerializedName("address_2")
    private String address2;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    @SerializedName("country")
    private String country;

    @SerializedName("pin_code")
    private String pinCode;

    @SerializedName("phone")
    private String phone;
  }
}
