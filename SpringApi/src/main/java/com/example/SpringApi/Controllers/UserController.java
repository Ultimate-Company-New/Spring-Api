package com.example.springapi.controllers;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.exceptions.PermissionException;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.requestmodels.UserRequestModel;
import com.example.springapi.models.responsemodels.ErrorResponseModel;
import com.example.springapi.models.responsemodels.PaginationBaseResponseModel;
import com.example.springapi.models.responsemodels.UserResponseModel;
import com.example.springapi.services.UserService;
import com.example.springapi.services.interfaces.UserSubTranslator;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for User-related operations.
 *
 * <p>This controller provides comprehensive user management endpoints including CRUD operations,
 * user retrieval, and user status management. It follows the established pattern from
 * AddressController and ClientController with proper error handling, security annotations, and
 * comprehensive documentation.
 *
 * <p>All endpoints are secured with appropriate permission checks and include comprehensive error
 * handling with structured error responses.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USER)
public class UserController {

  private final UserSubTranslator userService;
  private final ContextualLogger logger;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
    this.logger = ContextualLogger.getLogger(UserController.class);
  }

  /**
   * Retrieves a user by their unique identifier.
   *
   * <p>This endpoint fetches a user from the database using the provided ID. The response includes
   * all user details including login name, personal information, role, and metadata.
   *
   * @param id The unique identifier of the user to retrieve
   * @return ResponseEntity containing UserResponseModel or ErrorResponseModel
   */
  @GetMapping("/" + ApiRoutes.UsersSubRoute.GET_USER_BY_ID + "/{id}")
  @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_USER_PERMISSION + "')")
  public ResponseEntity<?> getUserById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(userService.getUserById(id));
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
   * Retrieves a user by their email address.
   *
   * <p>This endpoint fetches a user from the database using the provided email. The response
   * includes all user details.
   *
   * @param email The email address of the user to retrieve
   * @return ResponseEntity containing UserResponseModel or ErrorResponseModel
   */
  @GetMapping("/" + ApiRoutes.UsersSubRoute.GET_USER_BY_EMAIL + "/{email}")
  @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_USER_PERMISSION + "')")
  public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
    try {
      return ResponseEntity.ok(userService.getUserByEmail(email));
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
   * Creates a new user in the system.
   *
   * <p>This endpoint validates the provided user data, creates a new User entity, and persists it
   * to the database. The method automatically sets audit fields such as createdUser, modifiedUser,
   * and timestamps.
   *
   * @param user The UserRequestModel containing the user data to insert
   * @return ResponseEntity containing true if user was created successfully or ErrorResponseModel
   */
  @PutMapping("/" + ApiRoutes.UsersSubRoute.CREATE_USER)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.CREATE_USER_PERMISSION + "')")
  public ResponseEntity<?> createUser(@RequestBody UserRequestModel user) {
    try {
      // Always send email for single user creation
      userService.createUser(user);
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
    } catch (PermissionException pe) {
      logger.error(pe);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ErrorResponseModel("Forbidden", pe.getMessage(), HttpStatus.FORBIDDEN.value()));
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
   * Updates an existing user with new information.
   *
   * <p>This endpoint retrieves the existing user by ID, validates the new data, and updates the
   * user while preserving audit information like createdUser and createdAt. Only the modifiedUser
   * and updatedAt fields are updated.
   *
   * @param id The unique identifier of the user to update
   * @param user The UserRequestModel containing the updated user data
   * @return ResponseEntity containing the updated user ID or ErrorResponseModel
   */
  @PostMapping("/" + ApiRoutes.UsersSubRoute.UPDATE_USER + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.UPDATE_USER_PERMISSION + "')")
  public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequestModel user) {
    try {
      userService.updateUser(user);
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
   * Toggles the deletion status of a user by its ID.
   *
   * <p>This endpoint performs a soft delete operation by toggling the isDeleted flag. If the user
   * is currently active (isDeleted = false), it will be marked as deleted. If the user is currently
   * deleted (isDeleted = true), it will be restored.
   *
   * @param id The unique identifier of the user to toggle
   * @return ResponseEntity with no content on success or ErrorResponseModel
   */
  @DeleteMapping("/" + ApiRoutes.UsersSubRoute.TOGGLE_USER + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.DELETE_USER_PERMISSION + "')")
  public ResponseEntity<?> toggleUser(@PathVariable Long id) {
    try {
      userService.toggleUser(id);
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
   * Retrieves users in the carrier in paginated batches with advanced filtering and sorting.
   * Fetches users associated with the current carrier in a paginated, filterable, and sortable
   * manner. Accepts a {@link UserRequestModel} containing pagination parameters (start, end),
   * filter expressions, column names for sorting, and other options. Returns a {@link
   * PaginationBaseResponseModel} with user data and total count, enabling efficient client-side
   * pagination and search. Ideal for large user lists in admin panels.
   *
   * @param userRequestModel The request model containing pagination, filter, and sort options.
   * @return {@link PaginationBaseResponseModel} of {@link UserResponseModel} for the requested
   *     batch.
   */
  @PostMapping("/" + ApiRoutes.UsersSubRoute.GET_USERS_IN_CARRIER_IN_BATCHES)
  @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_USER_PERMISSION + "')")
  public ResponseEntity<?> fetchUsersInCarrierInBatches(
      @RequestBody UserRequestModel userRequestModel) {
    try {
      return ResponseEntity.ok(userService.fetchUsersInCarrierInBatches(userRequestModel));
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
   * Confirms user email using userId and token from email link. This is a PUBLIC endpoint (no.
   * authentication required) as users haven't logged in yet.
   *
   * @param userId User ID from the email link (path variable)
   * @param token Verification token from the email link (query parameter to avoid URL encoding
   *     issues with bcrypt)
   * @return Success message or error response
   */
  @PostMapping(ApiRoutes.UserSubRoute.CONFIRM_EMAIL + "/{userId}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<?> confirmEmail(@PathVariable Long userId, @RequestParam String token) {
    try {
      userService.confirmEmail(userId, token);
      return ResponseEntity.ok("Email confirmed successfully");
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
   * Retrieves all permissions available in the system.
   *
   * <p>This endpoint fetches all permissions from the database including their permission ID, name,
   * code, description, and category. This is useful for populating permission selection dropdowns
   * in user management interfaces. Only non-deleted permissions are returned.
   *
   * @return ResponseEntity containing List of PermissionResponseModel or ErrorResponseModel
   */
  @GetMapping("/" + ApiRoutes.UsersSubRoute.GET_ALL_PERMISSIONS)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.CREATE_USER_PERMISSION + "')")
  public ResponseEntity<?> getAllPermissions() {
    try {
      return ResponseEntity.ok(userService.getAllPermissions());
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
   * Creates multiple users in the system efficiently with partial success support.
   *
   * <p>This endpoint performs bulk user insertion ASYNCHRONOUSLY with the following
   * characteristics: - Immediately returns 202 Accepted to the client - Processes users in
   * background thread - Uses batch database operations for maximum efficiency - Supports partial
   * success: if some users fail validation, others still succeed - Does NOT send email
   * confirmations (unlike createUser endpoint) - Sends results to the user via message notification
   * after processing completes
   *
   * <p>This is ideal for importing large numbers of users from external systems or bulk user
   * provisioning scenarios. The endpoint can handle millions of records efficiently through batched
   * database operations without blocking the client.
   *
   * @param users List of UserRequestModel containing the user data to insert
   * @return ResponseEntity with 202 Accepted status indicating job has been queued
   */
  @PutMapping("/" + ApiRoutes.UsersSubRoute.BULK_CREATE_USER)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.CREATE_USER_PERMISSION + "')")
  public ResponseEntity<?> bulkCreateUsers(@RequestBody List<UserRequestModel> users) {
    try {
      // Cast to UserService to access BaseService methods (security context not
      // available in async thread)
      UserService service = (UserService) userService;
      Long userId = service.getUserId();
      String loginName = service.getUser();
      Long clientId = service.getClientId();

      // Trigger async processing - returns immediately
      userService.bulkCreateUsersAsync(users, userId, loginName, clientId);

      // Return 200 OK - processing will continue in background
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
}
