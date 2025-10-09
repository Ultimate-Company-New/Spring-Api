package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the ShipmentPackageMapping table.
 * 
 * This entity tracks which packages and quantities are used for each shipment.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ShipmentPackageMapping")
public class ShipmentPackageMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipmentPackageMappingId", nullable = false)
    private Long shipmentPackageMappingId;

    @Column(name = "packageQuantityUsed", nullable = false)
    private Integer packageQuantityUsed;

    @Column(name = "itemsPackedInPackage", nullable = false)
    private Integer itemsPackedInPackage;

    @Column(name = "packageCostPerUnit", nullable = false, precision = 10, scale = 2)
    private BigDecimal packageCostPerUnit;

    @Column(name = "orderShipmentId", nullable = false)
    private Long orderShipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderShipmentId", insertable = false, updatable = false)
    private OrderShipment orderShipment;

    @Column(name = "packageId", nullable = false)
    private Long packageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packageId", insertable = false, updatable = false)
    private Package packageEntity;

    @Column(name = "pickupLocationId", nullable = false)
    private Long pickupLocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickupLocationId", insertable = false, updatable = false)
    private PickupLocation pickupLocation;

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
