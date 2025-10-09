package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the PurchaseOrderQuantityMap table.
 * 
 * This entity manages the many-to-many relationship between purchase orders and product pickup locations with quantities.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PurchaseOrderQuantityMap")
public class PurchaseOrderQuantityMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchaseOrderQuantityMapId", nullable = false)
    private Long purchaseOrderQuantityMapId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "priceAtPurchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(name = "discountAtPurchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAtPurchase;

    @Column(name = "totalAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "productPickupLocationMappingId", nullable = false)
    private Long productPickupLocationMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productPickupLocationMappingId", insertable = false, updatable = false)
    private ProductPickupLocationMapping productPickupLocationMapping;

    @Column(name = "purchaseOrderId", nullable = false)
    private Long purchaseOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseOrderId", insertable = false, updatable = false)
    private PurchaseOrder purchaseOrder;

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
