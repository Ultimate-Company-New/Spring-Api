package springapi.controllers;

import java.util.List;
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
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.PackageRequestModel;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.responsemodels.ErrorResponseModel;
import springapi.models.responsemodels.PackageResponseModel;
import springapi.services.PackageService;
import springapi.services.interfaces.PackageSubTranslator;

/**
 * REST Controller for Package management operations. Handles all package-related HTTP requests.
 * including CRUD operations, batch processing, and specialized queries for package management.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PACKAGE)
public class PackageController {
  private static final ContextualLogger logger =
      ContextualLogger.getLogger(PackageController.class);
  private final PackageSubTranslator packageService;
  private final PackageService concreteService;

  @Autowired
  public PackageController(PackageSubTranslator packageService, PackageService concreteService) {
    this.packageService = packageService;
    this.concreteService = concreteService;
  }

  /**
   * Retrieves packages in paginated batches with optional filtering and sorting. Supports.
   * pagination, sorting by multiple fields, and filtering capabilities.
   *
   * @param paginationBaseRequestModel The request model containing pagination and filter parameters
   * @return ResponseEntity containing paginated package data
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PACKAGES_PERMISSION + "')")
  @PostMapping(ApiRoutes.PackageSubRoute.GET_PACKAGES_IN_BATCHES)
  public ResponseEntity<?> getPackagesInBatches(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(packageService.getPackagesInBatches(paginationBaseRequestModel));
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Retrieves detailed information for a specific package by ID.
   *
   * @param id The unique identifier of the package
   * @return ResponseEntity containing the package response data
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PACKAGES_PERMISSION + "')")
  @GetMapping(ApiRoutes.PackageSubRoute.GET_PACKAGE_BY_ID + "/{id}")
  public ResponseEntity<?> getPackageById(@PathVariable long id) {
    try {
      PackageResponseModel packageResponse = packageService.getPackageById(id);
      return ResponseEntity.ok(packageResponse);
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Retrieves all packages available at a specific pickup location.
   *
   * @param pickupLocationId The unique identifier of the pickup location
   * @return ResponseEntity containing list of packages available at the pickup location
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PACKAGES_PERMISSION + "')")
  @GetMapping(ApiRoutes.PackageSubRoute.GET_PACKAGES_BY_PICKUP_LOCATION_ID + "/{pickupLocationId}")
  public ResponseEntity<?> getPackagesByPickupLocationId(@PathVariable Long pickupLocationId) {
    try {
      List<PackageResponseModel> packages =
          packageService.getPackagesByPickupLocationId(pickupLocationId);
      return ResponseEntity.ok(packages);
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Toggles the active status of a package.
   *
   * @param id The unique identifier of the package to toggle
   * @return ResponseEntity containing success status
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.TOGGLE_PACKAGES_PERMISSION + "')")
  @DeleteMapping(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE + "/{id}")
  public ResponseEntity<?> togglePackage(@PathVariable long id) {
    try {
      packageService.togglePackage(id);
      return ResponseEntity.ok("Package status toggled successfully");
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Updates an existing package with new information.
   *
   * @param packageRequest The updated package data
   * @return ResponseEntity containing success message
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.UPDATE_PACKAGES_PERMISSION + "')")
  @PostMapping(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE)
  public ResponseEntity<?> updatePackage(@RequestBody PackageRequestModel packageRequest) {
    try {
      packageService.updatePackage(packageRequest);
      return ResponseEntity.ok("Package updated successfully");
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Creates a new package in the system.
   *
   * @param packageRequest The package data to create
   * @return ResponseEntity containing success message
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PACKAGES_PERMISSION + "')")
  @PutMapping(ApiRoutes.PackageSubRoute.CREATE_PACKAGE)
  public ResponseEntity<?> createPackage(@RequestBody PackageRequestModel packageRequest) {
    try {
      packageService.createPackage(packageRequest);
      return ResponseEntity.ok("Package created successfully");
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
   * Creates multiple packages asynchronously in a single operation. Results will be sent via.
   * message notification after processing completes.
   *
   * @param packages List of PackageRequestModel containing the package data to insert
   * @return ResponseEntity with 200 OK (processing started)
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PACKAGES_PERMISSION + "')")
  @PutMapping(ApiRoutes.PackageSubRoute.BULK_CREATE_PACKAGE)
  public ResponseEntity<?> bulkCreatePackages(
      @RequestBody java.util.List<PackageRequestModel> packages) {
    try {
      // Capture security context before async call
      Long userId = concreteService.getUserId();
      String loginName = concreteService.getUser();
      Long clientId = concreteService.getClientId();

      // Call async method with captured context
      packageService.bulkCreatePackagesAsync(packages, userId, loginName, clientId);

      // Return immediately - results will be sent via message notification
      return ResponseEntity.ok().build();
    } catch (BadRequestException e) {
      logger.error(e);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  e.getMessage(),
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
}
