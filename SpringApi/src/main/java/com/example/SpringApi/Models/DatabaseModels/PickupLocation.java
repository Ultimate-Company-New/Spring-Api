package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.time.LocalDateTime;

/**
 * JPA Entity for the PickupLocation table.
 * 
 * This entity represents pickup locations where packages can be collected.
 * It includes address information, client association, and ShipRocket integration.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`PickupLocation`")
public class PickupLocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pickupLocationId", nullable = false)
    private Long pickupLocationId;
    
    @Column(name = "addressNickName", nullable = false, length = 36)
    private String addressNickName;
    
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    @Column(name = "pickupLocationAddressId", nullable = false)
    private Long pickupLocationAddressId;
    
    @Column(name = "shipRocketPickupLocationId")
    private Long shipRocketPickupLocationId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "createdBy", nullable = false, length = 255)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "modifiedBy", nullable = false, length = 255)
    private String modifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedBy", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;    
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickupLocationAddressId", insertable = false, updatable = false)
    private Address address;
    
    public PickupLocation() {}
    
    /**
     * Constructor for creating a new pickup location.
     * 
     * @param request The PickupLocationRequestModel containing pickup location data
     * @param createdBy The username of the user creating this record
     */
    public PickupLocation(PickupLocationRequestModel request, String createdBy, Long clientId) {
        validateRequest(request);
        validateUser(createdBy);
        
        setFieldsFromRequest(request);
        this.createdBy = createdBy;
        this.modifiedBy = createdBy;  // When creating, modified user is same as created user
        this.clientId = clientId;
    }
    
    /**
     * Constructor for updating an existing pickup location.
     * 
     * @param request The PickupLocationRequestModel containing updated pickup location data
     * @param modifiedBy The username of the user modifying this record
     * @param existingPickupLocation The existing pickup location entity to be updated
     */
    public PickupLocation(PickupLocationRequestModel request, String modifiedBy, PickupLocation existingPickupLocation) {
        validateRequest(request);
        validateUser(modifiedBy);
        
        // Copy existing fields
        this.pickupLocationId = existingPickupLocation.getPickupLocationId();
        this.createdAt = existingPickupLocation.getCreatedAt();
        this.createdBy = existingPickupLocation.getCreatedBy();
        this.clientId = existingPickupLocation.getClientId();
        
        // Update with new values
        setFieldsFromRequest(request);
        this.modifiedBy = modifiedBy;
    }
    
    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The PickupLocationRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(PickupLocationRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.InvalidRequest);
        }
        
        // Validate address nickname (required, length > 0, max 36 chars - Shiprocket API limit)
        if (request.getAddressNickName() == null || request.getAddressNickName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName);
        }
        if (request.getAddressNickName().trim().length() > 36) {
            throw new BadRequestException("Location name must be 36 characters or less (Shiprocket limit)");
        }
        
        // Validate pickup location address ID (required, > 0)
        if (request.getPickupLocationAddressId() == null || request.getPickupLocationAddressId() <= 0) {
            throw new BadRequestException(ErrorMessages.AddressErrorMessages.InvalidId);
        }
        
        // Validate ShipRocket pickup location ID (if provided, > 0)
        if (request.getShipRocketPickupLocationId() != null && request.getShipRocketPickupLocationId() <= 0) {
            throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId);
        }
    }
    
    /**
     * Validates the user parameter for audit fields.
     * 
     * @param user The username to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }
    
    /**
     * Sets fields from the request model.
     * 
     * @param request The PickupLocationRequestModel to extract fields from
     */
    private void setFieldsFromRequest(PickupLocationRequestModel request) {
        this.addressNickName = request.getAddressNickName().trim();
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : false;
        this.pickupLocationAddressId = request.getPickupLocationAddressId();
        this.shipRocketPickupLocationId = request.getShipRocketPickupLocationId();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}