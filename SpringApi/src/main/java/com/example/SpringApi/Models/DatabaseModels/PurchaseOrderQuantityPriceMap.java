package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for the PurchaseOrderQuantityPriceMap table.
 * 
 * This entity manages the many-to-many relationship between purchase orders and product pickup locations with quantities and prices.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PurchaseOrderQuantityPriceMap")
public class PurchaseOrderQuantityPriceMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchaseOrderQuantityPriceMapId", nullable = false)
    private Long purchaseOrderQuantityPriceMapId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "pricePerQuantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerQuantity;

    @Column(name = "productId", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @Column(name = "purchaseOrderId", nullable = false)
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseOrderId", insertable = false, updatable = false)
    private PurchaseOrder purchaseOrder;
    
    /**
     * Creates a list of PurchaseOrderQuantityPriceMap entries from a PurchaseOrderRequestModel.
     * 
     * @param request The PurchaseOrderRequestModel containing products list with price and quantity
     * @param purchaseOrderId The purchase order ID
     * @return List of PurchaseOrderQuantityPriceMap entries
     */
    public static List<PurchaseOrderQuantityPriceMap> createFromRequest(
            com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel request,
            Long purchaseOrderId) {
        
        List<PurchaseOrderQuantityPriceMap> quantityPriceMaps = new ArrayList<>();
        
        for (com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem productItem : request.getProducts()) {
            Long productId = productItem.getProductId();
            BigDecimal pricePerUnit = productItem.getPricePerUnit();
            Integer quantity = productItem.getQuantity();
            
            // Create PurchaseOrderQuantityPriceMap entry
            PurchaseOrderQuantityPriceMap quantityPriceMap = new PurchaseOrderQuantityPriceMap();
            quantityPriceMap.setPurchaseOrderId(purchaseOrderId);
            quantityPriceMap.setProductId(productId);
            quantityPriceMap.setQuantity(quantity);
            quantityPriceMap.setPricePerQuantity(pricePerUnit);
            
            quantityPriceMaps.add(quantityPriceMap);
        }
        
        return quantityPriceMaps;
    }
}

