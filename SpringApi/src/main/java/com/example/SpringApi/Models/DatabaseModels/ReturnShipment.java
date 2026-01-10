package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for the ReturnShipment table.
 * 
 * This entity stores return orders initiated for a shipment.
 * A single shipment can have multiple return orders (partial returns).
 * 
 * @author SpringApi Team
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "ReturnShipment")
public class ReturnShipment {
    
    /**
     * Enum for return type
     */
    public enum ReturnType {
        FULL_RETURN("FULL_RETURN"),
        PARTIAL_RETURN("PARTIAL_RETURN");
        
        private final String value;
        
        ReturnType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * Enum for return shipment status values.
     */
    public enum ReturnStatus {
        RETURN_PENDING("RETURN_PENDING"),
        RETURN_PICKUP_SCHEDULED("RETURN_PICKUP_SCHEDULED"),
        RETURN_PICKED_UP("RETURN_PICKED_UP"),
        RETURN_IN_TRANSIT("RETURN_IN_TRANSIT"),
        RETURN_OUT_FOR_DELIVERY("RETURN_OUT_FOR_DELIVERY"),
        RETURN_DELIVERED("RETURN_DELIVERED"),
        RETURN_CANCELLED("RETURN_CANCELLED"),
        RETURN_FAILED("RETURN_FAILED");
        
        private final String value;
        
        ReturnStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static boolean isValid(String status) {
            if (status == null) return true;
            for (ReturnStatus s : values()) {
                if (s.value.equalsIgnoreCase(status)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "returnShipmentId")
    private Long returnShipmentId;
    
    // Foreign Key to Shipment
    @Column(name = "shipmentId", nullable = false)
    private Long shipmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipmentId", insertable = false, updatable = false)
    private Shipment shipment;
    
    // Return Type
    @Enumerated(EnumType.STRING)
    @Column(name = "returnType", nullable = false)
    private ReturnType returnType;
    
    // ShipRocket Return Order Details
    @Column(name = "shipRocketReturnOrderId", length = 100)
    private String shipRocketReturnOrderId;
    
    @Column(name = "shipRocketReturnShipmentId")
    private Long shipRocketReturnShipmentId;
    
    @Column(name = "shipRocketReturnStatus", length = 50)
    private String shipRocketReturnStatus;
    
    @Column(name = "shipRocketReturnStatusCode")
    private Integer shipRocketReturnStatusCode;
    
    // AWB Details for Return
    @Column(name = "shipRocketReturnAwbCode", length = 100)
    private String shipRocketReturnAwbCode;
    
    @Column(name = "shipRocketReturnAwbMetadata", columnDefinition = "JSON")
    private String shipRocketReturnAwbMetadata;
    
    // Return Order Metadata
    @Column(name = "shipRocketReturnOrderMetadata", columnDefinition = "JSON")
    private String shipRocketReturnOrderMetadata;
    
    // Dimensions and Weight
    @Column(name = "returnWeightKgs", precision = 10, scale = 3, nullable = false)
    private BigDecimal returnWeightKgs = BigDecimal.ZERO;
    
    @Column(name = "returnLength", precision = 10, scale = 2)
    private BigDecimal returnLength;
    
    @Column(name = "returnBreadth", precision = 10, scale = 2)
    private BigDecimal returnBreadth;
    
    @Column(name = "returnHeight", precision = 10, scale = 2)
    private BigDecimal returnHeight;
    
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
    
    // One-to-Many relationship with ReturnShipmentProduct
    @OneToMany(mappedBy = "returnShipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnShipmentProduct> returnProducts = new ArrayList<>();
}
