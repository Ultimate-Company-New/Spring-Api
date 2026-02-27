package com.example.SpringApi.Models.RequestModels;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for creating a return order in ShipRocket. POST /orders/create/return
 *
 * <p>Note: For returns, the pickup location is where the customer is (delivery address of original
 * order) and the shipping location is where the product should be returned to (original pickup
 * location).
 */
@Getter
@Setter
public class ShipRocketReturnOrderRequestModel {

  @SerializedName("order_id")
  private String orderId;

  @SerializedName("order_date")
  private String orderDate;

  @SerializedName("channel_id")
  private String channelId;

  // Pickup address (customer's location - where to pick up the return)
  @SerializedName("pickup_customer_name")
  private String pickupCustomerName;

  @SerializedName("pickup_last_name")
  private String pickupLastName;

  @SerializedName("company_name")
  private String companyName;

  @SerializedName("pickup_address")
  private String pickupAddress;

  @SerializedName("pickup_address_2")
  private String pickupAddress2;

  @SerializedName("pickup_city")
  private String pickupCity;

  @SerializedName("pickup_state")
  private String pickupState;

  @SerializedName("pickup_country")
  private String pickupCountry;

  @SerializedName("pickup_pincode")
  private String pickupPincode;

  @SerializedName("pickup_email")
  private String pickupEmail;

  @SerializedName("pickup_phone")
  private String pickupPhone;

  @SerializedName("pickup_isd_code")
  private String pickupIsdCode;

  // Shipping address (warehouse/return location - where to deliver the return)
  @SerializedName("shipping_customer_name")
  private String shippingCustomerName;

  @SerializedName("shipping_last_name")
  private String shippingLastName;

  @SerializedName("shipping_address")
  private String shippingAddress;

  @SerializedName("shipping_address_2")
  private String shippingAddress2;

  @SerializedName("shipping_city")
  private String shippingCity;

  @SerializedName("shipping_country")
  private String shippingCountry;

  @SerializedName("shipping_pincode")
  private String shippingPincode;

  @SerializedName("shipping_state")
  private String shippingState;

  @SerializedName("shipping_email")
  private String shippingEmail;

  @SerializedName("shipping_isd_code")
  private String shippingIsdCode;

  @SerializedName("shipping_phone")
  private String shippingPhone;

  // Order items
  @SerializedName("order_items")
  private List<ReturnOrderItem> orderItems;

  // Payment and pricing
  @SerializedName("payment_method")
  private String paymentMethod;

  @SerializedName("total_discount")
  private String totalDiscount;

  @SerializedName("sub_total")
  private BigDecimal subTotal;

  // Package dimensions
  @SerializedName("length")
  private BigDecimal length;

  @SerializedName("breadth")
  private BigDecimal breadth;

  @SerializedName("height")
  private BigDecimal height;

  @SerializedName("weight")
  private BigDecimal weight;

  /** Inner class for order items in return request */
  @Getter
  @Setter
  public static class ReturnOrderItem {
    @SerializedName("name")
    private String name;

    @SerializedName("qc_enable")
    private Boolean qcEnable;

    @SerializedName("qc_product_name")
    private String qcProductName;

    @SerializedName("sku")
    private String sku;

    @SerializedName("units")
    private Integer units;

    @SerializedName("selling_price")
    private BigDecimal sellingPrice;

    @SerializedName("discount")
    private BigDecimal discount;

    @SerializedName("qc_brand")
    private String qcBrand;

    @SerializedName("qc_product_image")
    private String qcProductImage;
  }
}

