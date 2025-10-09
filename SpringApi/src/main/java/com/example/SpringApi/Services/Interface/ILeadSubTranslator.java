package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;

/**
 * Interface for Lead operations and business logic.
 * Provides methods for managing leads including CRUD operations,
 * batch processing, and specialized queries.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface ILeadSubTranslator {
    
        /**
     * Retrieves leads in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     * 
     * @param leadRequestModel The request model containing pagination and filter parameters
     * @return PaginationBaseResponseModel containing paginated lead data
     */
    PaginationBaseResponseModel<LeadResponseModel> getLeadsInBatches(LeadRequestModel leadRequestModel);
    
    /**
     * Retrieves detailed information for a specific lead by ID.
     * 
     * @param leadId The unique identifier of the lead
     * @return LeadResponseModel with complete details including address and user information
     */
    LeadResponseModel getLeadDetailsById(Long leadId);
    
    /**
     * Retrieves detailed information for a specific lead by email address.
     * 
     * @param email The email address of the lead
     * @return LeadResponseModel with complete details including address and user information
     */
    LeadResponseModel getLeadDetailsByEmail(String email);
    
    /**
     * Creates a new lead in the system.
     * 
     * @param leadRequestModel The lead data to create
     * @return The created Lead entity
     */
    void createLead(LeadRequestModel leadRequestModel);
    
    /**
     * Updates an existing lead with new information.
     * 
     * @param leadId The unique identifier of the lead to update
     * @param leadRequestModel The updated lead data
     * @return The updated Lead entity
     */
    void updateLead(Long leadId, LeadRequestModel leadRequestModel);
    
    /**
     * Toggles the active status of a lead.
     * 
     * @param leadId The unique identifier of the lead to toggle
     * @return The updated Lead entity
     */
    void toggleLead(Long leadId);
}