package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import java.util.List;

/**
 * Interface for UserGroup-related business operations.
 * 
 * This interface defines the contract for user group management operations including
 * CRUD operations, client-specific group retrieval, active group filtering, and 
 * group status management. All implementations should handle proper validation, 
 * error handling, and logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IUserGroupSubTranslator {
    
    /**
     * Toggles the deletion status of a user group by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the group is currently active (isDeleted = false), it will be marked as deleted.
     * If the group is currently deleted (isDeleted = true), it will be restored.
     * 
     * @param id The unique identifier of the user group to toggle
     * @throws NotFoundException if the user group was not found
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    void toggleUserGroup(long id);

    /**
     * Retrieves a single user group by its unique identifier.
     * 
     * This method fetches a user group from the database using the provided ID.
     * The returned UserGroupResponseModel contains all group details including
     * name, description, client association, metadata, and the list of users
     * in the group.
     * 
     * @param id The unique identifier of the user group to retrieve
     * @return UserGroupResponseModel containing the user group information
     * @throws NotFoundException if no user group exists with the given ID
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    UserGroupResponseModel getUserGroupDetailsById(long id);

    /**
     * Creates a new user group in the system.
     * 
     * This method validates the provided user group data, creates a new UserGroup entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. It also creates mappings
     * for all users specified in the request.
     * 
     * @param userGroup The UserGroupRequestModel containing the group data to insert
     * @throws BadRequestException if the user group data is invalid or incomplete
     * @throws IllegalArgumentException if the userGroup parameter is null
     */
    void createUserGroup(UserGroupRequestModel userGroup);

    /**
     * Updates an existing user group with new information.
     * 
     * This method retrieves the existing user group by ID, validates the new data,
     * and updates the group while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * It also updates the user-group mappings by removing old mappings and
     * creating new ones.
     * 
     * @param userGroup The UserGroupRequestModel containing the updated group data
     * @throws NotFoundException if no user group exists with the given ID
     * @throws BadRequestException if the user group data is invalid or incomplete
     * @throws IllegalArgumentException if the userGroup parameter is null
     */
    void updateUserGroup(UserGroupRequestModel userGroup);

    /**
     * Retrieves all user groups in the system.
     * 
     * This method fetches all user groups from the database regardless of status.
     * The method returns a list of UserGroupResponseModel objects, each containing
     * complete group information. Returns an empty list if no groups are found.
     * 
     * @return List of UserGroupResponseModel objects for all user groups
     */
    List<UserGroupResponseModel> getAllUserGroup();

    /**
     * Retrieves all user groups associated with a specific client.
     * 
     * This method fetches all user groups where the clientId matches the provided parameter.
     * The method returns a list of UserGroupResponseModel objects, each containing
     * complete group information. Returns an empty list if no groups are found.
     * 
     * @param id The unique identifier of the client
     * @return List of UserGroupResponseModel objects for the client
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    List<UserGroupResponseModel> getUserGroupByClientId(long id);

    /**
     * Retrieves all active user groups in the system.
     * 
     * This method fetches all user groups where isDeleted is false.
     * The method returns a list of UserGroupResponseModel objects for active groups only.
     * Returns an empty list if no active groups are found.
     * 
     * @return List of UserGroupResponseModel objects for active user groups
     */
    List<UserGroupResponseModel> getActiveUserGroup();
}