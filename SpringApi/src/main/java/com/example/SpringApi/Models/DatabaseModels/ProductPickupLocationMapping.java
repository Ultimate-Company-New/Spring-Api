package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for the ProductPickupLocationMapping table.
 * 
 * This entity manages the many-to-many relationship between products and pickup locations.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ProductPickupLocationMapping")
public class ProductPickupLocationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productPickupLocationMappingId", nullable = false)
    private Long productPickupLocationMappingId;

    @Column(name = "productId", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @Column(name = "pickupLocationId", nullable = false)
    private Long pickupLocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickupLocationId", insertable = false, updatable = false)
    private PickupLocation pickupLocation;

    @Column(name = "availableStock", nullable = false)
    private Integer availableStock;

    @Column(name = "itemAvailableFrom", nullable = false)
    private LocalDateTime itemAvailableFrom;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

    @Column(name = "lastStockUpdate", nullable = false)
    private LocalDateTime lastStockUpdate;

    @Column(name = "minStockLevel", nullable = false)
    private Integer minStockLevel;

    @Column(name = "maxStockLevel", nullable = false)
    private Integer maxStockLevel;

    @Column(name = "reorderLevel", nullable = false)
    private Integer reorderLevel;

    @Column(name = "stockNotes", columnDefinition = "TEXT")
    private String stockNotes;

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
