package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.LeadRequestModel;
import springapi.services.LeadService;
import springapi.services.interfaces.LeadSubTranslator;

/**
 * REST Controller for Lead management operations. Handles all lead-related HTTP requests including.
 * CRUD operations, batch processing, and specialized queries for sales and marketing.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.LEAD)
public class LeadController extends BaseController {
  private static final ContextualLogger logger = ContextualLogger.getLogger(LeadController.class);
  private final LeadSubTranslator leadService;

  @Autowired
  public LeadController(LeadSubTranslator leadService) {
    this.leadService = leadService;
  }

  /**
   * Retrieves leads in paginated batches with optional filtering and sorting. Supports pagination,.
   * sorting by multiple fields, and filtering capabilities.
   *
   * @param leadRequestModel The request model containing pagination and filter parameters
   * @return ResponseEntity containing paginated lead data
   */
  @PostMapping("/" + ApiRoutes.LeadsSubRoute.GET_LEADS_IN_BATCHES)
  @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_LEADS_PERMISSION + "')")
  public ResponseEntity<?> getLeadsInBatches(@RequestBody LeadRequestModel leadRequestModel) {
    try {
      return ResponseEntity.ok(leadService.getLeadsInBatches(leadRequestModel));
    } catch (BadRequestException e) {
      return badRequest(logger, e);
    } catch (NotFoundException e) {
      return notFound(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves detailed information for a specific lead by ID. Returns complete lead information.
   * including all associated data.
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
      return badRequest(logger, e);
    } catch (NotFoundException e) {
      return notFound(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves detailed information for a specific lead by email address. Returns complete lead.
   * information for the specified email.
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
      return badRequest(logger, e);
    } catch (NotFoundException e) {
      return notFound(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates a new lead in the system. Validates lead data and creates a new lead record.
   *
   * @param leadRequestModel The lead data to create
   * @return ResponseEntity containing the created lead
   */
  @PutMapping("/" + ApiRoutes.LeadsSubRoute.CREATE_LEAD)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_LEADS_PERMISSION + "')")
  public ResponseEntity<?> createLead(@RequestBody LeadRequestModel leadRequestModel) {
    try {
      leadService.createLead(leadRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException e) {
      return badRequest(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates multiple leads asynchronously in a single operation. Processing happens in background.
   * thread; results sent via message notification.
   *
   * @param leads List of LeadRequestModel containing the lead data to insert
   * @return ResponseEntity with 200 OK status indicating job has been queued
   */
  @PutMapping("/" + ApiRoutes.LeadsSubRoute.BULK_CREATE_LEAD)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_LEADS_PERMISSION + "')")
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
      return badRequest(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Updates an existing lead with new information. Validates updated data and modifies the lead.
   * record.
   *
   * @param leadId The unique identifier of the lead to update
   * @param leadRequestModel The updated lead data
   * @return ResponseEntity containing the updated lead
   */
  @PostMapping("/" + ApiRoutes.LeadsSubRoute.UPDATE_LEAD + "/{leadId}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.UPDATE_LEADS_PERMISSION + "')")
  public ResponseEntity<?> updateLead(
      @PathVariable Long leadId, @RequestBody LeadRequestModel leadRequestModel) {
    try {
      leadService.updateLead(leadId, leadRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException e) {
      return badRequest(logger, e);
    } catch (NotFoundException e) {
      return notFound(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (springapi.exceptions.PermissionException e) {
      return forbidden(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Toggles the active status of a lead. Switches between active and inactive states for the.
   * specified lead.
   *
   * @param leadId The unique identifier of the lead to toggle
   * @return ResponseEntity containing the updated lead
   */
  @DeleteMapping("/" + ApiRoutes.LeadsSubRoute.TOGGLE_LEAD + "/{leadId}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.TOGGLE_LEADS_PERMISSION + "')")
  public ResponseEntity<?> toggleLead(@PathVariable Long leadId) {
    try {
      leadService.toggleLead(leadId);
      return ResponseEntity.ok().build();
    } catch (BadRequestException e) {
      return badRequest(logger, e);
    } catch (NotFoundException e) {
      return notFound(logger, e);
    } catch (UnauthorizedException e) {
      return unauthorized(logger, e);
    } catch (springapi.exceptions.PermissionException e) {
      return forbidden(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
