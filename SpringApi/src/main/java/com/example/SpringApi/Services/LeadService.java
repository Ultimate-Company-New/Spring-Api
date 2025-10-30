package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
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
        // Valid columns for filtering
        Set<String> validColumns = Set.of(
            "leadId", "firstName", "lastName", "email", "address", "website",
            "phone", "companySize", "title", "leadStatus", "company", "annualRevenue",
            "fax", "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes"
        );

        // Validate column name if provided
        if (leadRequestModel.getColumnName() != null 
            && !validColumns.contains(leadRequestModel.getColumnName())) {
            throw new BadRequestException(
                "Invalid column name: " + leadRequestModel.getColumnName());
        }
        
        // Calculate page size and offset
        int start = leadRequestModel.getStart();
        int end = leadRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException("Invalid pagination: end must be greater than start");
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("leadId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };
        
        // Execute paginated query with clientId filter
        Page<Lead> page = leadRepository.findPaginatedLeads(
            getClientId(),
            leadRequestModel.getColumnName(),
            leadRequestModel.getCondition(),
            leadRequestModel.getFilterExpr(),
            leadRequestModel.isIncludeDeleted(),
            pageable
        );
        
        // Convert Lead entities to LeadResponseModel (relationships already loaded)
        PaginationBaseResponseModel<LeadResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(page.getContent().stream()
            .map(LeadResponseModel::new)
            .toList());
        response.setTotalDataCount(page.getTotalElements());
        
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
        Lead lead = leadRepository.findLeadWithDetailsById(leadId, getClientId());
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }
        
        return new LeadResponseModel(lead);
    }
    
    /**
     * Retrieves detailed information for a specific lead by email address.
     * 
     * @param email The email address of the lead
     * @return LeadResponseModel with complete details including address and user information
     */
    @Override
    public LeadResponseModel getLeadDetailsByEmail(String email) {
        Lead lead = leadRepository.findLeadWithDetailsByEmail(email, getClientId());
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }
        
        return new LeadResponseModel(lead);
    }
    
    /**
     * Creates a new lead in the system.
     * 
     * @param leadRequestModel The lead data to create
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
        Lead existingLead = leadRepository.findLeadWithDetailsById(leadId, getClientId());
        if (existingLead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }

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
        Lead lead = leadRepository.findLeadWithDetailsById(leadId, getClientId());
        if (lead == null) {
            throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
        }
        
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