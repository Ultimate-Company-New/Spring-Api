package com.example.SpringApi.Models.RequestModels;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request model for ShipRocket create custom order API.
 * Uses @SerializedName to map Java field names to ShipRocket JSON field names (Gson serialization).
 */
@Getter
@Setter
public class ShipRocketOrderRequestModel {
    
    @SerializedName("order_id")
    private String orderId;
    
    @SerializedName("order_date")
    private String orderDate;
    
    @SerializedName("pickup_location")
    private String pickupLocation;
    
    @SerializedName("channel_id")
    private String channelId;
    
    @SerializedName("company_name")
    private String companyName;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("reseller_name")
    private String resellerName;
    
    // Billing address
    @SerializedName("billing_customer_name")
    private String billingCustomerName;
    
    @SerializedName("billing_last_name")
    private String billingLastName;
    
    @SerializedName("billing_address")
    private String billingAddress;
    
    @SerializedName("billing_address_2")
    private String billingAddress2;
    
    @SerializedName("billing_city")
    private String billingCity;
    
    @SerializedName("billing_pincode")
    private Integer billingPincode;
    
    @SerializedName("billing_state")
    private String billingState;
    
    @SerializedName("billing_country")
    private String billingCountry;
    
    @SerializedName("billing_email")
    private String billingEmail;
    
    @SerializedName("billing_phone")
    private Long billingPhone;
    
    @SerializedName("billing_isd_code")
    private String billingIsdCode;
    
    // Shipping address
    @SerializedName("shipping_is_billing")
    private Boolean shippingIsBilling;
    
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
    
    @SerializedName("shipping_pincode")
    private Integer shippingPincode;
    
    @SerializedName("shipping_state")
    private String shippingState;
    
    @SerializedName("shipping_country")
    private String shippingCountry;
    
    @SerializedName("shipping_email")
    private String shippingEmail;
    
    @SerializedName("shipping_phone")
    private Long shippingPhone;
    
    // Order items
    @SerializedName("order_items")
    private List<OrderItem> orderItems;
    
    // Payment and charges
    @SerializedName("payment_method")
    private String paymentMethod;
    
    @SerializedName("shipping_charges")
    private Integer shippingCharges;
    
    @SerializedName("total_discount")
    private Integer totalDiscount;
    
    @SerializedName("sub_total")
    private Integer subTotal;
    
    // Package dimensions
    @SerializedName("length")
    private Double length;
    
    @SerializedName("breadth")
    private Double breadth;
    
    @SerializedName("height")
    private Double height;
    
    @SerializedName("weight")
    private Double weight;
    
    @SerializedName("cod_amount")
    private Double codAmount;
    
    @SerializedName("courier_id")
    private Long courierId;
    
    @SerializedName("invoice_number")
    private String invoiceNumber;
    
    @SerializedName("is_insurance_opt")
    private Boolean isInsuranceOpt;
    
    @SerializedName("is_document")
    private Integer isDocument;
    
    @SerializedName("order_tag")
    private String orderTag;
    
    /**
     * Order item model for ShipRocket order.
     */
    @Getter
    @Setter
    public static class OrderItem {
        @SerializedName("name")
        private String name;
        
        @SerializedName("sku")
        private String sku;
        
        @SerializedName("units")
        private Integer units;
        
        @SerializedName("selling_price")
        private Integer sellingPrice;
        
        @SerializedName("discount")
        private Integer discount;
        
        @SerializedName("tax")
        private Integer tax;

        public OrderItem() {
        }

        /**
         * Constructor with all order item fields.
         */
        public OrderItem(String name, String sku, Integer units, Integer sellingPrice, Integer discount, Integer tax) {
            this.name = name;
            this.sku = sku;
            this.units = units;
            this.sellingPrice = sellingPrice;
            this.discount = discount;
            this.tax = tax;
        }
    }
}

