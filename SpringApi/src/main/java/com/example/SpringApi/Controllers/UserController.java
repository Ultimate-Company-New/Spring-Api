package com.example.SpringApi.Controllers;

import com.example.SpringApi.Services.UserService;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User-related operations.
 * 
 * This controller provides comprehensive user management endpoints including
 * CRUD operations, user retrieval, and user status management. It follows the
 * established pattern from AddressController and ClientController with proper
 * error handling, security annotations, and comprehensive documentation.
 * 
 * All endpoints are secured with appropriate permission checks and include
 * comprehensive error handling with structured error responses.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USER)
public class UserController {
    
    private final UserService userService;
    private final ContextualLogger logger;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        this.logger = ContextualLogger.getLogger(UserController.class);
    }

    /**
     * Retrieves a user by their unique identifier.
     * 
     * This endpoint fetches a user from the database using the provided ID.
     * The response includes all user details including login name, personal
     * information, role, and metadata.
     * 
     * @param id The unique identifier of the user to retrieve
     * @return ResponseEntity containing UserResponseModel or ErrorResponseModel
     */
    @GetMapping("/" + ApiRoutes.UsersSubRoute.GET_USER_BY_ID + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves all users in the system.
     * 
     * This endpoint fetches all users from the database. The response includes
     * a list of UserResponseModel objects, each containing complete user
     * information.
     * 
     * @return ResponseEntity containing List of UserResponseModel or ErrorResponseModel
     */
    @GetMapping("/" + ApiRoutes.UsersSubRoute.FETCH_ALL_USERS_IN_SYSTEM)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves a user by their email address.
     * 
     * This endpoint fetches a user from the database using the provided email.
     * The response includes all user details.
     * 
     * @param email The email address of the user to retrieve
     * @return ResponseEntity containing UserResponseModel or ErrorResponseModel
     */
    @GetMapping("/" + ApiRoutes.UsersSubRoute.GET_USER_BY_EMAIL + "/{email}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(userService.getUserByEmail(email));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new user in the system.
     * 
     * This endpoint validates the provided user data, creates a new User entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps.
     * 
     * @param user The UserRequestModel containing the user data to insert
     * @return ResponseEntity containing true if user was created successfully or ErrorResponseModel
     */
    @PutMapping("/" + ApiRoutes.UsersSubRoute.CREATE_USER)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.CREATE_USER_PERMISSION +"')")
    public ResponseEntity<?> createUser(@RequestBody UserRequestModel user) {
        try {
            userService.createUser(user);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing user with new information.
     * 
     * This endpoint retrieves the existing user by ID, validates the new data,
     * and updates the user while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * 
     * @param id The unique identifier of the user to update
     * @param user The UserRequestModel containing the updated user data
     * @return ResponseEntity containing the updated user ID or ErrorResponseModel
     */
    @PostMapping("/" + ApiRoutes.UsersSubRoute.UPDATE_USER + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_USER_PERMISSION +"')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserRequestModel user) {
        try {
            userService.updateUser(user);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the deletion status of a user by its ID.
     * 
     * This endpoint performs a soft delete operation by toggling the isDeleted flag.
     * If the user is currently active (isDeleted = false), it will be marked as deleted.
     * If the user is currently deleted (isDeleted = true), it will be restored.
     * 
     * @param id The unique identifier of the user to toggle
     * @return ResponseEntity containing the toggle result or ErrorResponseModel
     */
    @DeleteMapping("/" + ApiRoutes.UsersSubRoute.TOGGLE_USER + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_USER_PERMISSION +"')")
    public ResponseEntity<?> toggleUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.toggleUser(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves all users mapped to the current carrier.
     * Fetches all users associated with the current carrier (tenant/client) context. Optionally includes
     * users marked as deleted, depending on the {@code includeDeleted} parameter. Returns user details such as
     * user ID, name, role, and other metadata. Used for administrative or management purposes.
     *
     * @param includeDeleted If true, includes users marked as deleted; otherwise, only active users are returned.
     * @return List of {@link UserResponseModel} for all users in the carrier.
     */
    @GetMapping("/" + ApiRoutes.UsersSubRoute.FETCH_USERS_IN_CARRIER)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    public ResponseEntity<?> fetchUsersInCarrier(@RequestParam boolean includeDeleted) {
        try {
            return ResponseEntity.ok(userService.fetchUsersInCarrier(includeDeleted));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves users in the carrier in paginated batches with advanced filtering and sorting.
     * Fetches users associated with the current carrier in a paginated, filterable, and sortable manner.
     * Accepts a {@link UserRequestModel} containing pagination parameters (start, end), filter expressions,
     * column names for sorting, and other options. Returns a {@link PaginationBaseResponseModel} with user data
     * and total count, enabling efficient client-side pagination and search. Ideal for large user lists in admin panels.
     *
     * @param userRequestModel The request model containing pagination, filter, and sort options.
     * @return {@link PaginationBaseResponseModel} of {@link UserResponseModel} for the requested batch.
     */
    @PostMapping("/" + ApiRoutes.UsersSubRoute.GET_USERS_IN_CARRIER_IN_BATCHES)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_USER_PERMISSION +"')")
    public ResponseEntity<?> fetchUsersInCarrierInBatches(@RequestBody UserRequestModel userRequestModel) {
        try {
            return ResponseEntity.ok(userService.fetchUsersInCarrierInBatches(userRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}