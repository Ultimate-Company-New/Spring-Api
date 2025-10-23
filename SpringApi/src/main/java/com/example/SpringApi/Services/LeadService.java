package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Services.Interface.ILeadSubTranslator;

import jakarta.servlet.http.HttpServletRequest;

import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service implementation for Lead operations and business logic.
 * Handles all lead-related business operations including CRUD operations,
 * batch processing, validation, and specialized queries.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class LeadService extends BaseService implements ILeadSubTranslator {    
    private final LeadRepository leadRepository;
    private final AddressRepository addressRepository;
    private final UserLogService userLogService;

    @Autowired
    public LeadService(
        LeadRepository leadRepository, 
        AddressRepository addressRepository,
        UserLogService userLogService,
        HttpServletRequest request) {
        super();
        this.leadRepository = leadRepository;
        this.addressRepository = addressRepository;
        this.userLogService = userLogService;
    }
    
    /**
     * Retrieves leads in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     * 
     * @param leadRequestModel The request model containing pagination and filter parameters
     * @return PaginationBaseResponseModel containing paginated lead data
     */
    @Override
    public PaginationBaseResponseModel<LeadResponseModel> getLeadsInBatches(LeadRequestModel leadRequestModel) {
        // Validate column name - required field
        Set<String> VALID_COLUMN_NAMES = Set.of(
            "leadId", "firstName", "lastName", "email", "address", "website",
            "phone", "companySize", "title", "leadAssignedTo", "leadCreatedBy", "leadStatus"
        );

        if (leadRequestModel.getColumnName() == null || leadRequestModel.getColumnName().trim().isEmpty()) {
            throw new BadRequestException("Column name is required and cannot be empty");
        }
        
        if (!VALID_COLUMN_NAMES.contains(leadRequestModel.getColumnName())) {
            throw new BadRequestException("Invalid column name: " + leadRequestModel.getColumnName() + ". Valid columns are: " + VALID_COLUMN_NAMES);
        }
        
        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(leadRequestModel.getSortDirection() != null ? 
                        leadRequestModel.getSortDirection() : "ASC"), 
                        leadRequestModel.getSortBy() != null ? leadRequestModel.getSortBy() : "leadId");
        Pageable pageable = PageRequest.of(leadRequestModel.getPageNumber() - 1, leadRequestModel.getPageSize(), sort);
        
                // Execute paginated query
        Page<Lead> result = leadRepository.findPaginatedLeads(
            leadRequestModel.getColumnName(),
            leadRequestModel.getCondition(),
            leadRequestModel.getFilterExpr(),
            leadRequestModel.isIncludeDeleted(),
            pageable
        );
        
        // Convert Lead entities to LeadResponseModel (relationships already loaded)
        PaginationBaseResponseModel<LeadResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(result.getContent().stream()
            .map(lead -> {
                LeadResponseModel responseModel = new LeadResponseModel(lead);
                responseModel.setAddress(lead.getAddress() != null ? new AddressResponseModel(lead.getAddress()) : null);
                responseModel.setCreatedByUser(lead.getCreatedByUser() != null ? new UserResponseModel(lead.getCreatedByUser()) : null);
                responseModel.setAssignedAgent(lead.getAssignedAgent() != null ? new UserResponseModel(lead.getAssignedAgent()) : null);
                
                return responseModel;
            })
            .toList());
        response.setTotalDataCount(result.getTotalElements());
        
        return response;
    }    
    
    /**
     * Retrieves detailed information for a specific lead by ID.
     * 
     * @param leadId The unique identifier of the lead
     * @return LeadResponseModel with complete details including address and user information
     */
    @Override
    public LeadResponseModel getLeadDetailsById(Long leadId) {
        Lead lead = leadRepository.findLeadWithDetailsById(leadId);
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }
        
        LeadResponseModel responseModel = new LeadResponseModel(lead);
        responseModel.setAddress(lead.getAddress() != null ? new AddressResponseModel(lead.getAddress()) : null);
        responseModel.setCreatedByUser(lead.getCreatedByUser() != null ? new UserResponseModel(lead.getCreatedByUser()) : null);
        responseModel.setAssignedAgent(lead.getAssignedAgent() != null ? new UserResponseModel(lead.getAssignedAgent()) : null);
        
        return responseModel;
    }
    
    /**
     * Retrieves detailed information for a specific lead by email address.
     * 
     * @param email The email address of the lead
     * @return LeadResponseModel with complete details including address and user information
     */
    @Override
    public LeadResponseModel getLeadDetailsByEmail(String email) {
        Lead lead = leadRepository.findLeadWithDetailsByEmail(email);
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }
        
        LeadResponseModel responseModel = new LeadResponseModel(lead);
        responseModel.setAddress(lead.getAddress() != null ? new AddressResponseModel(lead.getAddress()) : null);
        responseModel.setCreatedByUser(lead.getCreatedByUser() != null ? new UserResponseModel(lead.getCreatedByUser()) : null);
        responseModel.setAssignedAgent(lead.getAssignedAgent() != null ? new UserResponseModel(lead.getAssignedAgent()) : null);
        
        return responseModel;
    }
    
    /**
     * Creates a new lead in the system.
     * 
     * @param leadRequestModel The lead data to create
     * @param createdUser The username of the user creating the lead
     * @return The created Lead entity
     */
    @Override
    public void createLead(LeadRequestModel leadRequestModel) {
        // Get current authenticated user
        String authenticatedUser = getUser();

        // Use the Lead constructor that handles validation and field mapping
        Lead lead = new Lead(leadRequestModel, authenticatedUser);
        Address savedAddress = addressRepository.save(new Address(leadRequestModel.getAddress(), authenticatedUser));
        lead.setAddressId(savedAddress.getAddressId());
        Lead savedLead = leadRepository.save(lead);

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.LeadSuccessMessages.InsertLead + savedLead.getLeadId(),
            ApiRoutes.LeadsSubRoute.CREATE_LEAD);
    }
    
    /**
     * Updates an existing lead with new information.
     * 
     * @param leadId The unique identifier of the lead to update
     * @param leadRequestModel The updated lead data
     * @return The updated Lead entity
     */
    @Override
    public void updateLead(Long leadId, LeadRequestModel leadRequestModel) {
        Lead existingLead = leadRepository.findById(leadId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.LEAD_NOT_FOUND));
        
        // Use the Lead constructor that handles validation and field mapping for updates
        Lead updatedLead = new Lead(leadRequestModel, getUser(), existingLead);
        Address savedAddress = addressRepository.save(new Address(leadRequestModel.getAddress(), getUser(), existingLead.getAddress()));

        updatedLead.setAddressId(savedAddress.getAddressId());
        leadRepository.save(updatedLead);

        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.LeadSuccessMessages.UpdateLead + updatedLead.getLeadId(),
            ApiRoutes.LeadsSubRoute.UPDATE_LEAD);
    }
    
    /**
     * Toggles the active status of a lead.
     * 
     * @param leadId The unique identifier of the lead to toggle
     * @return The updated Lead entity
     */
    @Override
    public void toggleLead(Long leadId) {
        Lead lead = leadRepository.findById(leadId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.LEAD_NOT_FOUND));
        
        // Toggle the deleted status (active/inactive)
        lead.setIsDeleted(!lead.getIsDeleted());
        lead.setModifiedUser(getUser());
        
        leadRepository.save(lead);

         // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.LeadSuccessMessages.ToggleLead + lead.getLeadId(),
            ApiRoutes.LeadsSubRoute.TOGGLE_LEAD);
    }
}