package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for the Shipment table.
 * 
 * This entity stores each shipment from a pickup location with courier details and costs.
 * Connected directly to OrderSummary via orderSummaryId foreign key.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "Shipment")
public class Shipment {
    
    /**
     * Enum for ShipRocket order status values.
     * These values must match the database CHECK constraint: chk_shipment_shiprocket_status
     */
    public enum ShipRocketStatus {
        NEW("NEW"),
        READY_TO_SHIP("READY_TO_SHIP"),
        PICKUP_SCHEDULED("PICKUP_SCHEDULED"),
        PICKED_UP("PICKED_UP"),
        IN_TRANSIT("IN_TRANSIT"),
        OUT_FOR_DELIVERY("OUT_FOR_DELIVERY"),
        DELIVERED("DELIVERED"),
        RTO_INITIATED("RTO_INITIATED"),
        RTO_DELIVERED("RTO_DELIVERED"),
        CANCELLED("CANCELLED"),
        PENDING("PENDING"),
        FAILED("FAILED");
        
        private final String value;
        
        ShipRocketStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        /**
         * Check if a string value is a valid ShipRocket status.
         * 
         * @param value The string value to check
         * @return true if valid, false otherwise
         */
        public static boolean isValid(String value) {
            if (value == null) {
                return false;
            }
            String normalizedValue = value.toUpperCase().trim().replace(" ", "_");
            for (ShipRocketStatus status : ShipRocketStatus.values()) {
                if (status.value.equals(normalizedValue)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Convert string to ShipRocketStatus enum (case-insensitive, handles spaces).
         * 
         * @param value The string value to convert
         * @return ShipRocketStatus enum or null if invalid
         */
        public static ShipRocketStatus fromString(String value) {
            if (value == null) {
                return null;
            }
            String normalizedValue = value.toUpperCase().trim().replace(" ", "_");
            for (ShipRocketStatus status : ShipRocketStatus.values()) {
                if (status.value.equals(normalizedValue)) {
                    return status;
                }
            }
            return null;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipmentId", nullable = false)
    private Long shipmentId;
    
    // Foreign Key to OrderSummary
    @Column(name = "orderSummaryId", nullable = false)
    private Long orderSummaryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderSummaryId", insertable = false, updatable = false)
    private OrderSummary orderSummary;
    
    // Pickup Location
    @Column(name = "pickupLocationId", nullable = false)
    private Long pickupLocationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickupLocationId", insertable = false, updatable = false)
    private PickupLocation pickupLocation;
    
    // Shipment Details
    @Column(name = "totalWeightKgs", nullable = false, precision = 10, scale = 3)
    private BigDecimal totalWeightKgs;
    
    @Column(name = "totalQuantity", nullable = false)
    private Integer totalQuantity;
    
    @Column(name = "expectedDeliveryDate", nullable = false)
    private LocalDateTime expectedDeliveryDate;
    
    // Cost Breakdown
    @Column(name = "packagingCost", nullable = false, precision = 15, scale = 2)
    private BigDecimal packagingCost;
    
    @Column(name = "shippingCost", nullable = false, precision = 15, scale = 2)
    private BigDecimal shippingCost;
    
    @Column(name = "totalCost", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCost;
    
    // Courier Selection (all required)
    @Column(name = "selectedCourierCompanyId", nullable = false)
    private Long selectedCourierCompanyId;
    
    @Column(name = "selectedCourierName", length = 255, nullable = false)
    private String selectedCourierName;
    
    @Column(name = "selectedCourierRate", precision = 15, scale = 2, nullable = false)
    private BigDecimal selectedCourierRate;
    
    @Column(name = "selectedCourierMinWeight", precision = 10, scale = 3, nullable = false)
    private BigDecimal selectedCourierMinWeight;
    
    @Column(name = "selectedCourierMetadata", columnDefinition = "JSON", nullable = false)
    private String selectedCourierMetadata; // JSON string (required)
    
    // ShipRocket Order Details (populated after order creation)
    @Column(name = "shipRocketOrderId", length = 100)
    private String shipRocketOrderId;
    
    @Column(name = "shipRocketShipmentId")
    private Long shipRocketShipmentId;
    
    @Column(name = "shipRocketAwbCode", length = 100)
    private String shipRocketAwbCode;
    
    @Column(name = "shipRocketTrackingId", length = 100)
    private String shipRocketTrackingId;
    
    @Column(name = "shipRocketStatus", length = 50)
    private String shipRocketStatus;
    
    /**
     * Sets the ShipRocket status with validation.
     * Validates that the status is one of the allowed ShipRocket status values.
     * 
     * @param status The ShipRocket status string to set
     * @throws BadRequestException if the status is invalid
     */
    public void setShipRocketStatus(String status) {
        if (status != null && !status.trim().isEmpty()) {
            if (!ShipRocketStatus.isValid(status)) {
                throw new BadRequestException(
                    String.format("Invalid ShipRocket status: '%s'. Valid statuses are: %s",
                        status,
                        String.join(", ", 
                            java.util.Arrays.stream(ShipRocketStatus.values())
                                .map(ShipRocketStatus::getValue)
                                .toArray(String[]::new)
                        )
                    )
                );
            }
            // Normalize the status value (uppercase, replace spaces with underscores)
            this.shipRocketStatus = status.toUpperCase().trim().replace(" ", "_");
        } else {
            this.shipRocketStatus = null;
        }
    }
    
    @Column(name = "shipRocketManifestUrl", length = 500)
    private String shipRocketManifestUrl;
    
    @Column(name = "shipRocketInvoiceUrl", length = 500)
    private String shipRocketInvoiceUrl;
    
    @Column(name = "shipRocketLabelUrl", length = 500)
    private String shipRocketLabelUrl;
    
    @Column(name = "shipRocketFullResponse", columnDefinition = "JSON")
    private String shipRocketFullResponse; // Complete ShipRocket API response as JSON string
    
    // Standard Fields
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;
    
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
    
    // Relationships
    @OneToMany(mappedBy = "shipment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentProduct> shipmentProducts = new ArrayList<>();
    
    @OneToMany(mappedBy = "shipment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentPackage> shipmentPackages = new ArrayList<>();
    
    /**
     * Default constructor.
     */
    public Shipment() {}
    
    /**
     * Constructor for creating a new shipment from ShipmentData.
     * 
     * @param orderSummaryId The order summary ID (not in ShipmentData)
     * @param shipmentData The shipment data from request model (contains all shipment details)
     * @param clientId The client ID (not in ShipmentData)
     * @param createdUser The user creating the shipment (not in ShipmentData)
     */
    public Shipment(Long orderSummaryId,
                   com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.ShipmentData shipmentData,
                   Long clientId, String createdUser) {
        validateRequest(orderSummaryId, shipmentData, clientId, createdUser);
        
        this.orderSummaryId = orderSummaryId;
        this.pickupLocationId = shipmentData.getPickupLocationId();
        this.totalWeightKgs = shipmentData.getTotalWeightKgs();
        this.totalQuantity = shipmentData.getTotalQuantity();
        this.packagingCost = shipmentData.getPackagingCost() != null ? shipmentData.getPackagingCost() : BigDecimal.ZERO;
        this.shippingCost = shipmentData.getShippingCost() != null ? shipmentData.getShippingCost() : BigDecimal.ZERO;
        this.totalCost = this.packagingCost.add(this.shippingCost);
        this.expectedDeliveryDate = shipmentData.getExpectedDeliveryDate();
        this.clientId = clientId;
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }
    
    /**
     * Constructor for updating an existing shipment from ShipmentData.
     * 
     * @param orderSummaryId The order summary ID (not in ShipmentData)
     * @param shipmentData The shipment data from request model (contains all shipment details)
     * @param modifiedUser The user modifying the shipment (not in ShipmentData)
     * @param existingShipment The existing shipment entity
     */
    public Shipment(Long orderSummaryId,
                   com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.ShipmentData shipmentData,
                   String modifiedUser, Shipment existingShipment) {
        validateRequest(orderSummaryId, shipmentData, null, modifiedUser);
        
        this.shipmentId = existingShipment.getShipmentId();
        this.createdUser = existingShipment.getCreatedUser();
        this.createdAt = existingShipment.getCreatedAt();
        this.clientId = existingShipment.getClientId();
        
        this.orderSummaryId = orderSummaryId;
        this.pickupLocationId = shipmentData.getPickupLocationId();
        this.totalWeightKgs = shipmentData.getTotalWeightKgs();
        this.totalQuantity = shipmentData.getTotalQuantity();
        this.packagingCost = shipmentData.getPackagingCost() != null ? shipmentData.getPackagingCost() : BigDecimal.ZERO;
        this.shippingCost = shipmentData.getShippingCost() != null ? shipmentData.getShippingCost() : BigDecimal.ZERO;
        this.totalCost = this.packagingCost.add(this.shippingCost);
        this.expectedDeliveryDate = shipmentData.getExpectedDeliveryDate();
        this.modifiedUser = modifiedUser;
        
        // Preserve courier selection if it exists
        this.selectedCourierCompanyId = existingShipment.getSelectedCourierCompanyId();
        this.selectedCourierName = existingShipment.getSelectedCourierName();
        this.selectedCourierRate = existingShipment.getSelectedCourierRate();
        this.selectedCourierMetadata = existingShipment.getSelectedCourierMetadata();
    }
    
    /**
     * Validates all request data for Shipment creation/update.
     * 
     * @param orderSummaryId The order summary ID
     * @param shipmentData The shipment data from request model
     * @param clientId The client ID (required for create, null for update)
     * @param user The user (createdUser for create, modifiedUser for update)
     */
    private void validateRequest(Long orderSummaryId,
                                com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.ShipmentData shipmentData,
                                Long clientId, String user) {
        if (shipmentData == null) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.InvalidRequest);
        }
        
        // Validate order summary ID
        if (orderSummaryId == null || orderSummaryId <= 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.OrderSummaryIdRequired);
        }
        
        // Validate pickup location ID
        if (shipmentData.getPickupLocationId() == null || shipmentData.getPickupLocationId() <= 0) {
            throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.InvalidId);
        }
        
        // Validate weight
        if (shipmentData.getTotalWeightKgs() == null) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.TotalWeightRequired);
        }
        if (shipmentData.getTotalWeightKgs().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.TotalWeightInvalid);
        }
        
        // Validate quantity
        if (shipmentData.getTotalQuantity() == null || shipmentData.getTotalQuantity() <= 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.TotalQuantityRequired);
        }
        
        // Validate costs
        if (shipmentData.getPackagingCost() == null) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.PackagingCostRequired);
        }
        if (shipmentData.getPackagingCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.PackagingCostInvalid);
        }
        
        if (shipmentData.getShippingCost() == null) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.ShippingCostRequired);
        }
        if (shipmentData.getShippingCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.ShippingCostInvalid);
        }
        
        // Validate client ID (only for create)
        if (clientId != null && (clientId <= 0)) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
        
        // Validate user
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }
    
    /**
     * Sets courier selection details from CourierSelectionData.
     * All courier fields are required.
     */
    public void setCourierSelection(com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel.CourierSelectionData courierData) {
        if (courierData == null) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.CourierSelectionRequired);
        }
        
        // Validate required fields
        if (courierData.getCourierCompanyId() == null) {
            throw new BadRequestException("Courier company ID is required");
        }
        if (courierData.getCourierName() == null || courierData.getCourierName().trim().isEmpty()) {
            throw new BadRequestException("Courier name is required");
        }
        if (courierData.getCourierRate() == null) {
            throw new BadRequestException("Courier rate is required");
        }
        if (courierData.getCourierMetadata() == null || courierData.getCourierMetadata().trim().isEmpty()) {
            throw new BadRequestException("Courier metadata is required");
        }
        
        this.selectedCourierCompanyId = courierData.getCourierCompanyId();
        this.selectedCourierName = courierData.getCourierName().trim();
        this.selectedCourierRate = courierData.getCourierRate();
        this.selectedCourierMinWeight = courierData.getCourierMinWeight() != null 
            ? courierData.getCourierMinWeight() 
            : BigDecimal.ZERO;
        this.selectedCourierMetadata = courierData.getCourierMetadata();
        
        // Recalculate shipping cost if courier rate is provided
        if (courierData.getCourierRate().compareTo(BigDecimal.ZERO) >= 0) {
            this.shippingCost = courierData.getCourierRate();
            this.totalCost = this.packagingCost.add(this.shippingCost);
        }
    }
}
