package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Services.Interface.ILeadSubTranslator;
import com.example.SpringApi.Services.LeadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Lead management operations.
 * Handles all lead-related HTTP requests including CRUD operations,
 * batch processing, and specialized queries for sales and marketing.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.LEAD)
public class LeadController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(LeadController.class);
    private final ILeadSubTranslator leadService;

    @Autowired
    public LeadController(ILeadSubTranslator leadService) {
        this.leadService = leadService;
    }

    /**
     * Retrieves leads in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     * 
     * @param leadRequestModel The request model containing pagination and filter
     *                         parameters
     * @return ResponseEntity containing paginated lead data
     */
    @PostMapping("/" + ApiRoutes.LeadsSubRoute.GET_LEADS_IN_BATCHES)
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_LEADS_PERMISSION + "')")
    public ResponseEntity<?> getLeadsInBatches(
            @RequestBody LeadRequestModel leadRequestModel) {
        try {
            return ResponseEntity.ok(leadService.getLeadsInBatches(leadRequestModel));
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, e.getMessage(),
                            HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information for a specific lead by ID.
     * Returns complete lead information including all associated data.
     * 
     * @param leadId The unique identifier of the lead
     * @return ResponseEntity containing the lead details
     */
    @GetMapping("/" + ApiRoutes.LeadsSubRoute.GET_LEAD_DETAILS_BY_ID + "/{leadId}")
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_LEADS_PERMISSION + "')")
    public ResponseEntity<?> getLeadDetailsById(@PathVariable Long leadId) {
        try {
            return ResponseEntity.ok(leadService.getLeadDetailsById(leadId));
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, e.getMessage(),
                            HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information for a specific lead by email address.
     * Returns complete lead information for the specified email.
     * 
     * @param email The email address of the lead
     * @return ResponseEntity containing the lead details
     */
    @GetMapping("/" + ApiRoutes.LeadsSubRoute.GET_LEAD_DETAILS_BY_EMAIL + "/{email}")
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_LEADS_PERMISSION + "')")
    public ResponseEntity<?> getLeadDetailsByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(leadService.getLeadDetailsByEmail(email));
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, e.getMessage(),
                            HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new lead in the system.
     * Validates lead data and creates a new lead record.
     * 
     * @param leadRequestModel The lead data to create
     * @return ResponseEntity containing the created lead
     */
    @PutMapping("/" + ApiRoutes.LeadsSubRoute.CREATE_LEAD)
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.INSERT_LEADS_PERMISSION + "')")
    public ResponseEntity<?> createLead(@RequestBody LeadRequestModel leadRequestModel) {
        try {
            leadService.createLead(leadRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates multiple leads asynchronously in a single operation.
     * Processing happens in background thread; results sent via message
     * notification.
     * 
     * @param leads List of LeadRequestModel containing the lead data to insert
     * @return ResponseEntity with 200 OK status indicating job has been queued
     */
    @PutMapping("/" + ApiRoutes.LeadsSubRoute.BULK_CREATE_LEAD)
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.INSERT_LEADS_PERMISSION + "')")
    public ResponseEntity<?> bulkCreateLeads(@RequestBody java.util.List<LeadRequestModel> leads) {
        try {
            // Cast to LeadService to access BaseService methods (security context not
            // available in async thread)
            LeadService service = (LeadService) leadService;
            Long userId = service.getUserId();
            String loginName = service.getUser();
            Long clientId = service.getClientId();

            // Trigger async processing - returns immediately
            leadService.bulkCreateLeadsAsync(leads, userId, loginName, clientId);

            // Return 200 OK - processing will continue in background
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing lead with new information.
     * Validates updated data and modifies the lead record.
     * 
     * @param leadId           The unique identifier of the lead to update
     * @param leadRequestModel The updated lead data
     * @return ResponseEntity containing the updated lead
     */
    @PostMapping("/" + ApiRoutes.LeadsSubRoute.UPDATE_LEAD + "/{leadId}")
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.UPDATE_LEADS_PERMISSION + "')")
    public ResponseEntity<?> updateLead(@PathVariable Long leadId,
            @RequestBody LeadRequestModel leadRequestModel) {
        try {
            leadService.updateLead(leadId, leadRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, e.getMessage(),
                            HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (com.example.SpringApi.Exceptions.PermissionException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseModel("Forbidden", e.getMessage(), HttpStatus.FORBIDDEN.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the active status of a lead.
     * Switches between active and inactive states for the specified lead.
     * 
     * @param leadId The unique identifier of the lead to toggle
     * @return ResponseEntity containing the updated lead
     */
    @DeleteMapping("/" + ApiRoutes.LeadsSubRoute.TOGGLE_LEAD + "/{leadId}")
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.TOGGLE_LEADS_PERMISSION + "')")
    public ResponseEntity<?> toggleLead(@PathVariable Long leadId) {
        try {
            leadService.toggleLead(leadId);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, e.getMessage(),
                            HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage(),
                            HttpStatus.UNAUTHORIZED.value()));
        } catch (com.example.SpringApi.Exceptions.PermissionException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponseModel("Forbidden", e.getMessage(), HttpStatus.FORBIDDEN.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}