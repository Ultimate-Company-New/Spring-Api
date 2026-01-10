package com.example.SpringApi.Models.ShippingResponseModel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Response model for ShipRocket invoice generation API.
 * POST /orders/print/invoice
 */
@Getter
@Setter
public class ShipRocketInvoiceResponseModel {
    
    @SerializedName("is_invoice_created")
    private Boolean isInvoiceCreated;
    
    @SerializedName("invoice_url")
    private String invoiceUrl;
    
    @SerializedName("not_created")
    private List<Object> notCreated;
    
    /**
     * Check if invoice was successfully created.
     * @return true if is_invoice_created is true
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(isInvoiceCreated);
    }
}
