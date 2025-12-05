package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.BulkUserInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Models.ResponseModels.PermissionResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import java.util.List;

/**
 * Interface for User-related business operations.
 * 
 * This interface defines the contract for user management operations including
 * CRUD operations, user retrieval, and user status management.
 * All implementations should handle proper validation, error handling, and logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IUserSubTranslator {
    
    /**
     * Toggles the deletion status of a user by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the user is currently active (isDeleted = false), it will be marked as deleted.
     * If the user is currently deleted (isDeleted = true), it will be restored.
     * 
     * @param id The unique identifier of the user to toggle
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    void toggleUser(long id);

    /**
     * Retrieves a single user by its unique identifier.
     * 
     * This method fetches a user from the database using the provided ID.
     * The returned UserResponseModel contains all user details including
     * login name, personal information, role, and metadata.
     * 
     * @param id The unique identifier of the user to retrieve
     * @return UserResponseModel containing the user information
     * @throws NotFoundException if no user exists with the given ID
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    UserResponseModel getUserById(long id);

    /**
     * Retrieves a single user by email address.
     * 
     * This method fetches a user from the database using the provided email.
     * The returned UserResponseModel contains all user details.
     * 
     * @param email The email address of the user to retrieve
     * @return UserResponseModel containing the user information
     * @throws NotFoundException if no user exists with the given email
     * @throws IllegalArgumentException if the provided email is null or invalid
     */
    UserResponseModel getUserByEmail(String email);

    /**
     * Creates a new user in the system.
     * 
     * This method validates the provided user data, creates a new User entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps.
     * 
     * @param userRequestModel The UserRequestModel containing the user data to create
     * @throws BadRequestException if the user data is invalid or incomplete
     * @throws IllegalArgumentException if the userRequestModel parameter is null
     */
    void createUser(UserRequestModel userRequestModel);

    /**
     * Updates an existing user with new information.
     * 
     * This method retrieves the existing user by ID, validates the new data,
     * and updates the user while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * 
     * @param user The UserRequestModel containing the updated user data
     * @throws NotFoundException if no user exists with the given ID
     * @throws BadRequestException if the user data is invalid or incomplete
     * @throws IllegalArgumentException if the user parameter is null
     */
    void updateUser(UserRequestModel user);

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
    PaginationBaseResponseModel<UserResponseModel> fetchUsersInCarrierInBatches(UserRequestModel userRequestModel);

    /**
     * Retrieves all permissions available in the system.
     * 
     * This method fetches all permissions from the database including their
     * permission ID, name, code, description, and category. This is useful
     * for populating permission selection dropdowns in user management interfaces.
     * 
     * @return List of PermissionResponseModel containing all permissions
     */
    List<PermissionResponseModel> getAllPermissions();

    /**
     * Confirms a user's email address using the verification token.
     * This is a public endpoint (no authentication required) as users haven't logged in yet.
     * 
     * The method:
     * 1. Validates that the user exists
     * 2. Verifies the provided token matches the user's stored token
     * 3. Sets emailConfirmed to true
     * 
     * @param userId The unique identifier of the user
     * @param token The verification token from the email link
     * @throws NotFoundException if no user exists with the given ID
     * @throws BadRequestException if the token is invalid or expired
     */
    void confirmEmail(Long userId, String token);

    /**
     * Creates multiple users asynchronously - triggers background processing and returns immediately.
     * Results are sent to the user via message notification.
     * 
     * @param users List of UserRequestModel containing the user data to create
     */
    void bulkCreateUsersAsync(List<UserRequestModel> users, Long requestingUserId, String requestingUserLoginName, Long requestingClientId);
}
