package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the OrderShipment table.
 * 
 * This entity manages multiple shipments per purchase order for partial deliveries.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "OrderShipment")
public class OrderShipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderShipmentId", nullable = false)
    private Long orderShipmentId;

    @Column(name = "shipmentNumber", nullable = false, length = 100)
    private String shipmentNumber;

    @Column(name = "trackingNumber")
    private String trackingNumber;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "shippingMethod", length = 100)
    private String shippingMethod;

    @Column(name = "shipmentStatus", nullable = false, length = 50)
    private String shipmentStatus;

    @Column(name = "shippedDate")
    private LocalDateTime shippedDate;

    @Column(name = "estimatedDeliveryDate")
    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "actualDeliveryDate")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "shippingCost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "purchaseOrderId", nullable = false)
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseOrderId", insertable = false, updatable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "shippingAddressId", nullable = false)
    private Long shippingAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shippingAddressId", insertable = false, updatable = false)
    private Address shippingAddress;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
