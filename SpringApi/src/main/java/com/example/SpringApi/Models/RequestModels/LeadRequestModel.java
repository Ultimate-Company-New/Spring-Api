package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for Lead operations.
 * 
 * This model contains all the fields required for creating or updating a lead.
 * It includes validation constraints and business logic requirements.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class LeadRequestModel extends PaginationBaseRequestModel {
    
    private Long leadId;
    private String annualRevenue;
    private String company;
    private Integer companySize;
    private String email;
    private String firstName;
    private String fax;
    private String lastName;
    private String leadStatus;
    private String phone;
    private String title;
    private String website;
    private Boolean isDeleted;
    private Long clientId;
    private Long addressId;
    private Long createdById;
    private Long assignedAgentId;
    private String notes;
    
    // Pagination fields
    private int pageNumber;
    private String sortBy;
    private String sortDirection;
    private String filter;
    
    // Additional fields for creation/update
    private AddressRequestModel address;
}