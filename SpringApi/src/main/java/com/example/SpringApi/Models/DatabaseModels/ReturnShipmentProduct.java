package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for the ReturnShipmentProduct table.
 * 
 * This entity stores individual products being returned in a return shipment.
 * Each return shipment can have multiple products with different quantities and reasons.
 * 
 * @author SpringApi Team
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "ReturnShipmentProduct")
public class ReturnShipmentProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "returnShipmentProductId")
    private Long returnShipmentProductId;
    
    // Foreign Key to ReturnShipment
    @Column(name = "returnShipmentId", nullable = false)
    private Long returnShipmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returnShipmentId", insertable = false, updatable = false)
    private ReturnShipment returnShipment;
    
    // Foreign Key to Product
    @Column(name = "productId", nullable = false)
    private Long productId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;
    
    // Return Details
    @Column(name = "returnQuantity", nullable = false)
    private Integer returnQuantity;
    
    @Column(name = "returnReason", nullable = false, length = 255)
    private String returnReason;
    
    @Column(name = "returnComments", columnDefinition = "TEXT")
    private String returnComments;
    
    // Product Details at time of return (denormalized for historical record)
    @Column(name = "productName", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "productSku", nullable = false, length = 100)
    private String productSku;
    
    @Column(name = "productSellingPrice", precision = 15, scale = 2, nullable = false)
    private BigDecimal productSellingPrice;
    
    // Soft Delete
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;
    
    // Standard Audit Fields
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "createdUser", nullable = false, length = 255)
    private String createdUser;
    
    @Column(name = "modifiedUser", nullable = false, length = 255)
    private String modifiedUser;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
