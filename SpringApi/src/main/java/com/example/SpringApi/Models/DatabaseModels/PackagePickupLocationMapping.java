package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for the PackagePickupLocationMapping table.
 * 
 * This entity manages package inventory at each pickup location.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "PackagePickupLocationMapping")
public class PackagePickupLocationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packagePickupLocationMappingId", nullable = false)
    private Long packagePickupLocationMappingId;

    @Column(name = "availableQuantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reorderLevel", nullable = false)
    private Integer reorderLevel;

    @Column(name = "maxStockLevel", nullable = false)
    private Integer maxStockLevel;

    @Column(name = "lastRestockDate")
    private LocalDateTime lastRestockDate;

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
