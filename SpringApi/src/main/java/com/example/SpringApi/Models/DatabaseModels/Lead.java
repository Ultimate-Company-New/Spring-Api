package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.time.LocalDateTime;

/**
 * JPA Entity for the Lead table.
 * 
 * This entity represents leads for sales and marketing customer acquisition.
 * It includes contact information, company details, and assignment tracking.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Lead`")
public class Lead {
    // Lead Status Constants
    public static final String STATUS_ATTEMPTED_TO_CONTACT = "Attempted To Contact";
    public static final String STATUS_LOST_LEAD = "Lost Lead";
    public static final String STATUS_NOT_CONTACTED = "Not Contacted";
    public static final String STATUS_CONTACT_IN_FUTURE = "Contact In Future";
    public static final String STATUS_CONTACTED = "Contacted";
    public static final String STATUS_RE_QUALIFIED = "Re Qualified";
    public static final String STATUS_JUNK_LEAD = "Junk Lead";
    public static final String STATUS_NOT_QUALIFIED = "Not Qualified";
    
    // Validation Constants
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
    
    // Valid Lead Statuses Set
    public static final java.util.Set<String> VALID_LEAD_STATUSES = java.util.Set.of(
        STATUS_ATTEMPTED_TO_CONTACT,
        STATUS_LOST_LEAD,
        STATUS_NOT_CONTACTED,
        STATUS_CONTACT_IN_FUTURE,
        STATUS_CONTACTED,
        STATUS_RE_QUALIFIED,
        STATUS_JUNK_LEAD,
        STATUS_NOT_QUALIFIED
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leadId", nullable = false)
    private Long leadId;
    
    @Column(name = "annualRevenue", length = 100)
    private String annualRevenue;
    
    @Column(name = "company", length = 255)
    private String company;
    
    @Column(name = "companySize")
    private Integer companySize;
    
    @Column(name = "email", nullable = false, length = 255)
    private String email;
    
    @Column(name = "firstName", nullable = false, length = 255)
    private String firstName;
    
    @Column(name = "fax", length = 50)
    private String fax;
    
    @Column(name = "lastName", nullable = false, length = 255)
    private String lastName;
    
    @Column(name = "leadStatus", nullable = false, length = 50)
    private String leadStatus;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "title", length = 100)
    private String title;
    
    @Column(name = "website", length = 500)
    private String website;
    
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    @Column(name = "addressId", nullable = false)
    private Long addressId;
    
    @Column(name = "createdById", nullable = false)
    private Long createdById;
    
    @Column(name = "assignedAgentId")
    private Long assignedAgentId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "createdUser", nullable = false, length = 255)
    private String createdUser;
    
    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "modifiedUser", nullable = false, length = 255)
    private String modifiedUser;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressId", insertable = false, updatable = false)
    private Address address;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdById", insertable = false, updatable = false)
    private User createdByUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignedAgentId", insertable = false, updatable = false)
    private User assignedAgent;
    
    public Lead() {}
    
    /**
     * Constructor for creating a new lead.
     * 
     * @param request The LeadRequestModel containing lead data
     * @param createdUser The username of the user creating this record
     */
    public Lead(LeadRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
    }
    
    /**
     * Constructor for updating an existing lead.
     * 
     * @param request The LeadRequestModel containing updated lead data
     * @param modifiedUser The username of the user modifying this record
     * @param existingLead The existing lead entity to be updated
     */
    public Lead(LeadRequestModel request, String modifiedUser, Lead existingLead) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        // Copy existing fields
        this.leadId = existingLead.getLeadId();
        this.createdAt = existingLead.getCreatedAt();
        this.createdUser = existingLead.getCreatedUser();
        
        // Update with new values
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;
    }
    
    /**
     * Validates the request model for required fields and constraints based on database schema.
     * 
     * @param request The LeadRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(LeadRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER009);
        }
        
        // Email validation (NOT NULL + REGEX constraint)
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER001);
        }
        if (!isValidEmail(request.getEmail())) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER010);
        }
        
        // First name validation (NOT NULL + LENGTH > 0 constraint)
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER002);
        }
        
        // Last name validation (NOT NULL + LENGTH > 0 constraint)
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER003);
        }
        
        // Phone validation (NOT NULL + REGEX constraint)
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER004);
        }
        if (!isValidPhone(request.getPhone())) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER011);
        }
        
        // Lead status validation (NOT NULL + IN constraint)
        if (request.getLeadStatus() == null || request.getLeadStatus().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER008);
        }
        if (!VALID_LEAD_STATUSES.contains(request.getLeadStatus().trim())) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER007 + VALID_LEAD_STATUSES);
        }
        
        // Client ID validation (NOT NULL constraint)
        if (request.getClientId() == null) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER012);
        }
        
        // Address validation - either address object or addressId must be provided
        if (request.getAddress() == null && request.getAddressId() == null) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER013);
        }
        
        // If address object is provided, validate it using Address entity validation
        if (request.getAddress() != null) {
            validateAddressRequest(request.getAddress());
        }
        
        // Created by ID validation (NOT NULL constraint)
        if (request.getCreatedById() == null) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER015);
        }
        
        // Company size validation (NULL OR > 0 constraint)
        if (request.getCompanySize() != null && request.getCompanySize() <= 0) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER016);
        }
        
        // Assigned agent ID validation (optional, but if provided must be valid)
        if (request.getAssignedAgentId() != null && request.getAssignedAgentId() <= 0) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER017);
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
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER018);
        }
    }
    
    /**
     * Validates the address request model using Address entity validation.
     * 
     * @param addressRequest The AddressRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateAddressRequest(AddressRequestModel addressRequest) {
        // Create a temporary Address instance just for validation
        try {
            new Address(addressRequest, "temp_user");
        } catch (BadRequestException e) {
            throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER014 + " " + e.getMessage());
        }
    }
    
    /**
     * Sets fields from the request model.
     * 
     * @param request The LeadRequestModel to extract fields from
     */
    private void setFieldsFromRequest(LeadRequestModel request) {
        this.annualRevenue = request.getAnnualRevenue() != null ? request.getAnnualRevenue().trim() : null;
        this.company = request.getCompany() != null ? request.getCompany().trim() : null;
        this.companySize = request.getCompanySize();
        this.email = request.getEmail().trim().toLowerCase();
        this.firstName = request.getFirstName().trim();
        this.fax = request.getFax() != null ? request.getFax().trim() : null;
        this.lastName = request.getLastName().trim();
        this.leadStatus = request.getLeadStatus().trim();
        this.phone = request.getPhone().trim();
        this.title = request.getTitle() != null ? request.getTitle().trim() : null;
        this.website = request.getWebsite() != null ? request.getWebsite().trim() : null;
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : false;
        this.clientId = request.getClientId();
        this.addressId = request.getAddressId();
        this.createdById = request.getCreatedById();
        this.assignedAgentId = request.getAssignedAgentId();
        this.notes = request.getNotes() != null ? request.getNotes().trim() : "Created From Spring API";
    }
    
    /**
     * Validates email format using the database constraint regex.
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }
    
    /**
     * Validates phone format using the database constraint regex.
     * 
     * @param phone The phone to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidPhone(String phone) {
        return phone.replaceAll("[\\s\\-\\(\\)]", "").matches(PHONE_REGEX);
    }
    
    /**
     * Public static method to validate lead status.
     * 
     * @param leadStatus The lead status to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLeadStatus(String leadStatus) {
        return leadStatus != null && VALID_LEAD_STATUSES.contains(leadStatus.trim());
    }
    
    /**
     * Public static method to validate email format.
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmailFormat(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }
    
    /**
     * Public static method to validate phone format.
     * 
     * @param phone The phone to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneFormat(String phone) {
        return phone != null && phone.replaceAll("[\\s\\-\\(\\)]", "").matches(PHONE_REGEX);
    }
    
    /**
     * Public static method to validate company size.
     * 
     * @param companySize The company size to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidCompanySize(Integer companySize) {
        return companySize == null || companySize > 0;
    }
    
    /**
     * Gets the full name of the lead.
     * 
     * @return Full name string
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}