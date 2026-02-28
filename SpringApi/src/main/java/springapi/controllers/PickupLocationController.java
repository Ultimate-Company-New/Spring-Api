package com.example.springapi.controllers;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.requestmodels.PaginationBaseRequestModel;
import com.example.springapi.models.requestmodels.PickupLocationRequestModel;
import com.example.springapi.models.responsemodels.ErrorResponseModel;
import com.example.springapi.services.PickupLocationService;
import com.example.springapi.services.interfaces.PickupLocationSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class PickupLocationController {

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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
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
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
