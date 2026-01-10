package com.example.SpringApi.Models.ShippingResponseModel;

import lombok.Getter;
import lombok.Setter;

/**
 * Response model for ShipRocket create custom order API.
 * ShipRocket returns a flat structure with order fields at root level.
 */
@Getter
@Setter
public class ShipRocketOrderResponseModel {
    public Long order_id;
    public String channel_order_id;
    public Long shipment_id;
    public String status; // "NEW", etc.
    public Integer status_code; // 1 for success
    public String message; // Error message if any
    public String awb_code;
    public String tracking_id;
    public String courier_company_id;
    public String courier_name;
    public String manifest_url;
    public String invoice_url;
    public String label_url;
    
    /**
     * Gets order_id as string (for storage in Shipment.shipRocketOrderId).
     */
    public String getOrderIdAsString() {
        return order_id != null ? order_id.toString() : null;
    }
    
    /**
     * Gets courier_id as Long (parsed from courier_company_id string).
     */
    public Long getCourierId() {
        if (courier_company_id == null || courier_company_id.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(courier_company_id.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

