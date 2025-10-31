package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Services.Interface.IUserGroupSubTranslator;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Logging.ContextualLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;

/**
 * REST Controller for managing UserGroup-related operations.
 * 
 * This controller provides RESTful endpoints for user group management including
 * CRUD operations, client-specific group retrieval, and active group filtering.
 * All endpoints are secured with appropriate authorization checks and include
 * comprehensive error handling with contextual logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USER_GROUP)
public class UserGroupController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(UserGroupController.class);
    private final IUserGroupSubTranslator userGroupService;

    @Autowired
    public UserGroupController(IUserGroupSubTranslator userGroupService) {
        this.userGroupService = userGroupService;
    }

    /**
     * Retrieves a single user group by its unique identifier.
     * 
     * This endpoint fetches a user group from the database using the provided ID.
     * The response contains all group details including name, description, client
     * association, and metadata. Requires VIEW_USER_GROUP_PERMISSION.
     * 
     * @param id The unique identifier of the user group to retrieve
     * @return ResponseEntity containing UserGroupResponseModel or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_GROUPS_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.UserGroupSubRoute.GET_USER_GROUP_DETAILS_BY_ID + "/{id}")
    public ResponseEntity<?> getUserGroupDetailsById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userGroupService.getUserGroupDetailsById(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves user groups in batches with pagination, filtering, and sorting.
     * 
     * This endpoint fetches user groups for the current client with support for:
     * - Column-based filtering (groupName, description)
     * - Various filter conditions (contains, equals, startsWith, endsWith, isEmpty, isNotEmpty)
     * - Pagination with configurable page size
     * - Optional inclusion of deleted groups
     * 
     * Valid columns: "userGroupId", "name", "description"
     * Requires VIEW_GROUPS_PERMISSION.
     * 
     * @param userGroupRequestModel The request model containing filter criteria and pagination settings
     * @return ResponseEntity containing PaginationBaseResponseModel with user groups or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_GROUPS_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.UserGroupSubRoute.GET_USER_GROUPS_IN_BATCHES)
    public ResponseEntity<?> getUserGroupsInBatches(@RequestBody UserGroupRequestModel userGroupRequestModel) {
        try {
            return ResponseEntity.ok(userGroupService.fetchUserGroupsInClientInBatches(userGroupRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new user group in the system.
     * 
     * This endpoint validates the provided user group data, creates a new UserGroup entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. Requires INSERT_USER_GROUP_PERMISSION.
     * 
     * @param userGroupRequest The UserGroupRequestModel containing the group data to create
     * @return ResponseEntity containing the ID of the created user group or ErrorResponseModel
     */
    @PutMapping("/" + ApiRoutes.UserGroupSubRoute.CREATE_USER_GROUP)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_GROUPS_PERMISSION +"')")
    public ResponseEntity<?> createUserGroup(@RequestBody UserGroupRequestModel userGroupRequest) {
        try {
            userGroupService.createUserGroup(userGroupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing user group with new information.
     * 
     * This endpoint retrieves the existing user group by ID, validates the new data,
     * and updates the group while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * Requires UPDATE_USER_GROUP_PERMISSION.
     * 
     * @param id The unique identifier of the user group to update
     * @param userGroupRequest The UserGroupRequestModel containing the updated group data
     * @return ResponseEntity containing the ID of the updated user group or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_GROUPS_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.UserGroupSubRoute.UPDATE_USER_GROUP + "/{id}")
    public ResponseEntity<?> updateUserGroup(@PathVariable Long id, @RequestBody UserGroupRequestModel userGroupRequest) {
        try {
            userGroupRequest.setGroupId(id);
            userGroupService.updateUserGroup(userGroupRequest);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the deletion status of a user group by its ID.
     * 
     * This endpoint performs a soft delete operation by toggling the isDeleted flag.
     * If the group is currently active (isDeleted = false), it will be marked as deleted.
     * If the group is currently deleted (isDeleted = true), it will be restored.
     * Requires DELETE_USER_GROUP_PERMISSION.
     * 
     * @param id The unique identifier of the user group to toggle
     * @return ResponseEntity containing success status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_GROUPS_PERMISSION +"')")
    @DeleteMapping("/" + ApiRoutes.UserGroupSubRoute.TOGGLE_USER_GROUP + "/{id}")
    public ResponseEntity<?> toggleUserGroup(@PathVariable Long id) {
        try {
            userGroupService.toggleUserGroup(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}