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
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.PickupLocationRequestModel;
import springapi.services.PickupLocationService;
import springapi.services.interfaces.PickupLocationSubTranslator;

/**
 * REST Controller for PickupLocation management operations.
 *
 * <p>This controller handles HTTP requests for pickup location management including CRUD
 * operations, batch retrieval, status management, and permission validation. All endpoints require
 * proper authorization and include comprehensive error handling.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PICKUP_LOCATION)
public class PickupLocationController extends BaseController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(PickupLocationController.class);

  private final PickupLocationSubTranslator pickupLocationService;

  @Autowired
  public PickupLocationController(PickupLocationSubTranslator pickupLocationService) {
    this.pickupLocationService = pickupLocationService;
  }

  /**
   * Retrieves pickup locations in paginated batches based on filter criteria.
   *
   * <p>Supports filtering, sorting, and pagination of pickup locations. Requires
   * VIEW_PICKUP_LOCATIONS_PERMISSION to access.
   *
   * @param paginationBaseRequestModel The pagination and filter parameters
   * @return ResponseEntity containing paginated pickup location data or error
   */
  @PostMapping("/" + ApiRoutes.PickupLocationsSubRoute.GET_PICKUP_LOCATIONS_IN_BATCHES)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> getPickupLocationsInBatches(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(
          pickupLocationService.getPickupLocationsInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves a specific pickup location by its unique identifier.
   *
   * <p>Fetches pickup location along with associated address information. Requires
   * VIEW_PICKUP_LOCATIONS_PERMISSION to access.
   *
   * @param id The unique identifier of the pickup location
   * @return ResponseEntity containing pickup location details or error
   */
  @GetMapping("/" + ApiRoutes.PickupLocationsSubRoute.GET_PICKUP_LOCATION_BY_ID + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> getPickupLocationById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(pickupLocationService.getPickupLocationById(id));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates a new pickup location with associated address information.
   *
   * <p>Validates pickup location data, creates address record, and establishes relationships.
   * Requires CREATE_PICKUP_LOCATIONS_PERMISSION to access.
   *
   * @param pickupLocationRequestModel The pickup location data to create
   * @return ResponseEntity containing the ID of newly created pickup location or error
   */
  @PutMapping("/" + ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> createPickupLocation(
      @RequestBody PickupLocationRequestModel pickupLocationRequestModel) {
    try {
      pickupLocationService.createPickupLocation(pickupLocationRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Updates an existing pickup location with new information.
   *
   * <p>Validates updated data and modifies pickup location record while maintaining audit trail.
   * Requires UPDATE_PICKUP_LOCATIONS_PERMISSION to access.
   *
   * @param id The unique identifier of the pickup location to update
   * @param pickupLocationRequestModel The updated pickup location data
   * @return ResponseEntity containing the ID of updated pickup location or error
   */
  @PostMapping("/" + ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> updatePickupLocation(
      @PathVariable Long id, @RequestBody PickupLocationRequestModel pickupLocationRequestModel) {
    try {
      pickupLocationRequestModel.setPickupLocationId(id);
      pickupLocationService.updatePickupLocation(pickupLocationRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Toggles the deletion status of a pickup location by its ID.
   *
   * <p>Performs soft delete operation by toggling the isDeleted flag. Requires
   * DELETE_PICKUP_LOCATIONS_PERMISSION to access.
   *
   * @param id The unique identifier of the pickup location to toggle
   * @return ResponseEntity containing success status or error
   */
  @DeleteMapping("/" + ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.DELETE_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> togglePickupLocation(@PathVariable Long id) {
    try {
      pickupLocationService.togglePickupLocation(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates multiple pickup locations asynchronously in a single operation. Processing happens in.
   * background thread; results sent via message notification.
   *
   * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data
   *     to insert
   * @return ResponseEntity with 200 OK status indicating job has been queued
   */
  @PutMapping("/" + ApiRoutes.PickupLocationsSubRoute.BULK_CREATE_PICKUP_LOCATION)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION
          + "')")
  public ResponseEntity<?> bulkCreatePickupLocations(
      @RequestBody java.util.List<PickupLocationRequestModel> pickupLocations) {
    try {
      // Cast to PickupLocationService to access BaseService methods (security context not available
      // in async thread)
      PickupLocationService service = (PickupLocationService) pickupLocationService;
      Long userId = service.getUserId();
      String loginName = service.getUser();
      Long clientId = service.getClientId();

      // Trigger async processing - returns immediately
      pickupLocationService.bulkCreatePickupLocationsAsync(
          pickupLocations, userId, loginName, clientId);

      // Return 200 OK - processing will continue in background
      return ResponseEntity.ok().build();
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (BadRequestException e) {
      return badRequest(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
