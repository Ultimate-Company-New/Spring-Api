package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;

/**
 * Interface for UserGroup-related business operations.
 *
 * <p>This interface defines the contract for user group management operations including CRUD
 * operations, client-specific group retrieval, active group filtering, and group status management.
 * All implementations should handle proper validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IUserGroupSubTranslator {

  /**
   * Toggles the deletion status of a user group by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the group is
   * currently active (isDeleted = false), it will be marked as deleted. If the group is currently
   * deleted (isDeleted = true), it will be restored.
   *
   * @param id The unique identifier of the user group to toggle
   * @throws NotFoundException if the user group was not found
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  void toggleUserGroup(long id);

  /**
   * Retrieves a single user group by its unique identifier.
   *
   * <p>This method fetches a user group from the database using the provided ID. The returned
   * UserGroupResponseModel contains all group details including name, description, client
   * association, metadata, and the list of users in the group.
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
   * <p>This method validates the provided user group data, creates a new UserGroup entity, and
   * persists it to the database. The method automatically sets audit fields such as createdUser,
   * modifiedUser, and timestamps. It also creates mappings for all users specified in the request.
   *
   * @param userGroup The UserGroupRequestModel containing the group data to insert
   * @throws BadRequestException if the user group data is invalid or incomplete
   * @throws IllegalArgumentException if the userGroup parameter is null
   */
  void createUserGroup(UserGroupRequestModel userGroup);

  /**
   * Updates an existing user group with new information.
   *
   * <p>This method retrieves the existing user group by ID, validates the new data, and updates the
   * group while preserving audit information like createdUser and createdAt. Only the modifiedUser
   * and updatedAt fields are updated. It also updates the user-group mappings by removing old
   * mappings and creating new ones.
   *
   * @param userGroup The UserGroupRequestModel containing the updated group data
   * @throws NotFoundException if no user group exists with the given ID
   * @throws BadRequestException if the user group data is invalid or incomplete
   * @throws IllegalArgumentException if the userGroup parameter is null
   */
  void updateUserGroup(UserGroupRequestModel userGroup);

  /**
   * Retrieves user groups for a client with pagination, filtering, and sorting.
   *
   * <p>This method fetches user groups with support for: - Column-based filtering (groupName,
   * description) - Various filter conditions (contains, equals, startsWith, endsWith, isEmpty,
   * isNotEmpty) - Pagination with configurable page size - Optional inclusion of deleted groups -
   * Optional filtering by specific group IDs
   *
   * <p>Valid columns for filtering: "userGroupId", "name", "description"
   *
   * @param userGroupRequestModel The request model containing filter criteria and pagination
   *     settings
   * @return PaginationBaseResponseModel containing the filtered and paginated user groups
   * @throws BadRequestException if an invalid column name is provided
   * @throws IllegalArgumentException if the userGroupRequestModel parameter is null
   */
  PaginationBaseResponseModel<UserGroupResponseModel> fetchUserGroupsInClientInBatches(
      UserGroupRequestModel userGroupRequestModel);

  /**
   * Creates multiple user groups asynchronously in the system with partial success support.
   *
   * <p>This method processes user groups in a background thread. Supports partial success: if some
   * groups fail validation, others still succeed. Results are sent to user via message notification
   * after processing completes.
   *
   * @param userGroups List of UserGroupRequestModel containing the group data to insert
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  void bulkCreateUserGroupsAsync(
      java.util.List<UserGroupRequestModel> userGroups,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId);

  /**
   * Creates multiple user groups synchronously in a single operation (for testing). This is a
   * synchronous wrapper that processes user groups immediately and returns results.
   *
   * @param userGroups List of UserGroupRequestModel containing the user group data to create
   * @return BulkInsertResponseModel containing success/failure details for each user group
   */
  com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreateUserGroups(
      java.util.List<UserGroupRequestModel> userGroups);
}

