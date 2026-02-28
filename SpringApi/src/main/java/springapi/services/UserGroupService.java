package springapi.services;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import springapi.ErrorMessages;
import springapi.SuccessMessages;
import springapi.authentication.JwtTokenProvider;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.filterquerybuilder.UserGroupFilterQueryBuilder;
import springapi.helpers.BulkInsertHelper;
import springapi.models.ApiRoutes;
import springapi.models.databasemodels.UserGroup;
import springapi.models.databasemodels.UserGroupUserMap;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;
import springapi.models.requestmodels.UserGroupRequestModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;
import springapi.models.responsemodels.UserGroupResponseModel;
import springapi.repositories.UserGroupRepository;
import springapi.repositories.UserGroupUserMapRepository;
import springapi.repositories.UserRepository;
import springapi.services.interfaces.UserGroupSubTranslator;

/**
 * Service class for managing UserGroup-related business operations.
 *
 * <p>This service implements the IUserGroupervice interface and provides comprehensive user group
 * management functionality including CRUD operations, client-specific group retrieval, active group
 * filtering, and user membership management. The service handles validation, error handling, audit
 * logging, and database persistence.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class UserGroupService extends BaseService implements UserGroupSubTranslator {
  private static final String UNKNOWN_GROUP_NAME = "unknown";

  private final UserGroupRepository userGroupRepository;
  private final UserGroupUserMapRepository userGroupUserMapRepository;
  private final UserLogService userLogService;
  private final UserGroupFilterQueryBuilder userGroupFilterQueryBuilder;
  private final MessageService messageService;

  /** Initializes UserGroupService. */
  @Autowired
  public UserGroupService(
      UserLogService userLogService,
      UserGroupRepository userGroupRepository,
      UserGroupUserMapRepository userGroupUserMapRepository,
      UserRepository userRepository,
      UserGroupFilterQueryBuilder userGroupFilterQueryBuilder,
      MessageService messageService,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.userLogService = userLogService;
    this.userGroupRepository = userGroupRepository;
    this.userGroupUserMapRepository = userGroupUserMapRepository;
    this.userGroupFilterQueryBuilder = userGroupFilterQueryBuilder;
    this.messageService = messageService;
  }

  /**
   * Toggles the deletion status of a user group by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the group is
   * currently active (isDeleted = false), it will be marked as deleted. If the group is currently
   * deleted (isDeleted = true), it will be restored. The operation is logged for audit purposes.
   *
   * @param groupId The unique identifier of the user group to toggle
   * @throws NotFoundException if the user group was not found
   */
  @Override
  public void toggleUserGroup(long groupId) {
    Optional<UserGroup> userGroup = userGroupRepository.findById(groupId);
    if (userGroup.isPresent()) {
      userGroup.get().setIsDeleted(!userGroup.get().getIsDeleted());
      userGroupRepository.save(userGroup.get());
      userLogService.logData(
          getUserId(),
          SuccessMessages.UserGroupSuccessMessages.TOGGLE_GROUP
              + " "
              + userGroup.get().getGroupId(),
          ApiRoutes.UserGroupSubRoute.TOGGLE_USER_GROUP);
    } else {
      throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.INVALID_ID);
    }
  }

  /**
   * Retrieves a single user group by its unique identifier.
   *
   * <p>This method fetches a user group from the database using the provided ID. The returned
   * UserGroupResponseModel contains all group details including name, description, client
   * association, metadata, and the list of users in the group.
   *
   * @param groupId The unique identifier of the user group to retrieve
   * @return UserGroupResponseModel containing the user group information
   * @throws NotFoundException if no user group exists with the given ID
   */
  @Override
  public UserGroupResponseModel getUserGroupDetailsById(long groupId) {
    // Fetch user group with ALL users in ONE query using JOIN FETCH
    UserGroup userGroup = userGroupRepository.findByIdWithUsers(groupId);

    if (userGroup != null) {
      // UserGroupResponseModel constructor auto-populates users from userMappings
      return new UserGroupResponseModel(userGroup);
    } else {
      throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.INVALID_ID);
    }
  }

  /**
   * Creates a new user group in the system.
   *
   * <p>This method validates the provided user group data, creates a new UserGroup entity, and
   * persists it to the database. The method automatically sets audit fields such as createdUser,
   * modifiedUser, and timestamps. It also creates mappings for all users specified in the request.
   * The operation is logged for audit purposes.
   *
   * @param userGroupRequest The UserGroupRequestModel containing the group data to insert
   * @throws BadRequestException if the user group data is invalid or incomplete
   */
  @Override
  public void createUserGroup(UserGroupRequestModel userGroupRequest) {
    createUserGroupInternal(userGroupRequest, getUser(), true);
  }

  /**
   * Updates an existing user group with new information.
   *
   * <p>This method retrieves the existing user group by ID, validates the new data, and updates the
   * group while preserving audit information like createdUser and createdAt. Only the modifiedUser
   * and updatedAt fields are updated. It also updates the user-group mappings by removing old
   * mappings and creating new ones. The operation is logged for audit purposes.
   *
   * @param userGroupRequest The UserGroupRequestModel containing the updated group data
   * @throws NotFoundException if no user group exists with the given ID
   * @throws BadRequestException if the user group data is invalid or incomplete
   */
  @Override
  public void updateUserGroup(UserGroupRequestModel userGroupRequest) {
    // Validate that at least one user is provided
    if (userGroupRequest.getUserIds() == null || userGroupRequest.getUserIds().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER004);
    }

    Optional<UserGroup> existingGroup = userGroupRepository.findById(userGroupRequest.getGroupId());
    if (existingGroup.isPresent()) {
      // Check if the new group name already exists for a different group
      UserGroup groupWithSameName =
          userGroupRepository.findByGroupName(userGroupRequest.getGroupName());
      if (groupWithSameName != null
          && !groupWithSameName.getGroupId().equals(userGroupRequest.getGroupId())) {
        throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GROUP_NAME_EXISTS);
      }

      // Update the user group
      UserGroup userGroup =
          new UserGroup(userGroupRequest, getUser(), existingGroup.get(), getClientId());
      UserGroup updatedGroup = userGroupRepository.save(userGroup);

      // Update user-group mappings
      updateUserGroupMappings(userGroupRequest, updatedGroup);

      userLogService.logData(
          getUserId(),
          SuccessMessages.UserGroupSuccessMessages.UPDATE_GROUP + " " + updatedGroup.getGroupId(),
          ApiRoutes.UserGroupSubRoute.UPDATE_USER_GROUP);
    } else {
      throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.INVALID_ID);
    }
  }

  /**
   * Retrieves user groups for a client with pagination, filtering, and sorting.
   *
   * <p>This method fetches user groups with support for: - Multi-filter support with AND/OR logic -
   * Column-based filtering (groupName, description, etc.) - Various filter conditions (contains,
   * equals, startsWith, endsWith, isEmpty, isNotEmpty) - Pagination with configurable page size -
   * Optional inclusion of deleted groups - Optional filtering by specific group IDs
   *
   * <p>Valid columns for filtering: "groupId", "groupName", "description", "isActive", "isDeleted",
   * "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes", "userCount", "memberCount",
   * "members" (all refer to member count)
   *
   * @param userGroupRequestModel The request model containing filter criteria and pagination
   *     settings
   * @return PaginationBaseResponseModel containing the filtered and paginated user groups
   * @throws BadRequestException if an invalid column name or filter condition is provided
   */
  @Override
  public PaginationBaseResponseModel<UserGroupResponseModel> fetchUserGroupsInClientInBatches(
      UserGroupRequestModel userGroupRequestModel) {
    // Validate page size
    int start = userGroupRequestModel.getStart();
    int end = userGroupRequestModel.getEnd();
    int pageSize = end - start;

    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    // Validate logic operator if provided
    if (userGroupRequestModel.getLogicOperator() != null
        && !userGroupRequestModel.isValidLogicOperator()) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR);
    }

    // Validate filters if provided
    if (userGroupRequestModel.hasMultipleFilters()) {
      Set<String> validColumns =
          new HashSet<>(
              Arrays.asList(
                  "groupId",
                  "clientId",
                  "groupName",
                  "description",
                  "isActive",
                  "isDeleted",
                  "notes",
                  "createdUser",
                  "modifiedUser",
                  "createdAt",
                  "updatedAt",
                  "userCount",
                  "memberCount",
                  "members"));

      for (FilterCondition filter : userGroupRequestModel.getFilters()) {
        // Validate column name
        if (!validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              "Invalid column name: "
                  + filter.getColumn()
                  + ". Valid columns: "
                  + String.join(", ", validColumns));
        }

        // Validate operator
        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              "Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn());
        }

        // Validate operator matches column type
        String columnType = userGroupFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());

        // Validate value presence
        filter.validateValuePresence();
      }
    }

    // Create custom Pageable with exact OFFSET and LIMIT for database-level pagination
    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("groupId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // Use the filter query builder for multi-filter support
    String logicOperator =
        userGroupRequestModel.getLogicOperator() != null
            ? userGroupRequestModel.getLogicOperator()
            : "AND";

    Page<UserGroup> userGroups =
        userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            userGroupRequestModel.getSelectedGroupIds(),
            logicOperator,
            userGroupRequestModel.getFilters(),
            userGroupRequestModel.isIncludeDeleted(),
            pageable);

    PaginationBaseResponseModel<UserGroupResponseModel> paginationBaseResponseModel =
        new PaginationBaseResponseModel<>();
    List<UserGroupResponseModel> userGroupResponseModels = new ArrayList<>();
    for (UserGroup userGroup : userGroups.getContent()) {
      UserGroupResponseModel userGroupResponseModel = new UserGroupResponseModel(userGroup);
      userGroupResponseModels.add(userGroupResponseModel);
    }

    paginationBaseResponseModel.setData(userGroupResponseModels);
    paginationBaseResponseModel.setTotalDataCount(userGroups.getTotalElements());
    return paginationBaseResponseModel;
  }

  /**
   * Creates multiple user groups asynchronously in the system with partial success support.
   *
   * <p>This method processes user groups in a background thread with the following characteristics:
   * - Supports partial success: if some groups fail validation, others still succeed - Sends
   * detailed results to user via message notification after processing completes - NOT_SUPPORTED:
   * Runs without a transaction to avoid rollback-only issues when individual group creations fail
   *
   * @param userGroups List of UserGroupRequestModel containing the group data to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  @Override
  @org.springframework.scheduling.annotation.Async
  @org.springframework.transaction.annotation.Transactional(
      propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  public void bulkCreateUserGroupsAsync(
      List<UserGroupRequestModel> userGroups,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId) {
    try {
      // Validate input
      if (userGroups == null || userGroups.isEmpty()) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "User group"));
      }

      springapi.models.responsemodels.BulkInsertResponseModel<Long> response =
          new springapi.models.responsemodels.BulkInsertResponseModel<>();
      response.setTotalRequested(userGroups.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each group individually
      for (UserGroupRequestModel groupRequest : userGroups) {
        try {
          // Call createUserGroup with explicit createdUser and shouldLog = false (bulk logs
          // collectively)
          createUserGroupInternal(groupRequest, requestingUserLoginName, false);

          // If we get here, group was created successfully
          // Fetch the created group to get the groupId
          UserGroup createdGroup = userGroupRepository.findByGroupName(groupRequest.getGroupName());
          response.addSuccess(groupRequest.getGroupName(), createdGroup.getGroupId());
          successCount++;

        } catch (BadRequestException bre) {
          // Validation or business logic error
          response.addFailure(
              groupRequest.getGroupName() != null
                  ? groupRequest.getGroupName()
                  : UNKNOWN_GROUP_NAME,
              bre.getMessage());
          failureCount++;
        } catch (Exception e) {
          // Unexpected error
          response.addFailure(
              groupRequest.getGroupName() != null
                  ? groupRequest.getGroupName()
                  : UNKNOWN_GROUP_NAME,
              "Error: " + e.getMessage());
          failureCount++;
        }
      }

      // Log bulk user group creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.UserGroupSuccessMessages.INSERT_GROUP
              + " (Bulk: "
              + successCount
              + " succeeded, "
              + failureCount
              + " failed)",
          ApiRoutes.UserGroupSubRoute.BULK_CREATE_USER_GROUP);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the specialized user group helper
      // (using captured context)
      BulkInsertHelper.createBulkUserGroupInsertResultMessage(
          response, messageService, requestingUserId, requestingUserLoginName, requestingClientId);

    } catch (Exception e) {
      // Still send a message to user about the failure (using captured userId)
      springapi.models.responsemodels.BulkInsertResponseModel<Long> errorResponse =
          new springapi.models.responsemodels.BulkInsertResponseModel<>();
      errorResponse.setTotalRequested(userGroups != null ? userGroups.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(userGroups != null ? userGroups.size() : 0);
      errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
      BulkInsertHelper.createBulkUserGroupInsertResultMessage(
          errorResponse,
          messageService,
          requestingUserId,
          requestingUserLoginName,
          requestingClientId);
    }
  }

  /**
   * Creates multiple user groups synchronously in a single operation (for testing). This is a.
   * synchronous wrapper that processes user groups immediately and returns results.
   *
   * @param userGroups List of UserGroupRequestModel containing the user group data to create
   * @return BulkInsertResponseModel containing success/failure details for each user group
   */
  @Override
  @org.springframework.transaction.annotation.Transactional
  public springapi.models.responsemodels.BulkInsertResponseModel<Long> bulkCreateUserGroups(
      List<UserGroupRequestModel> userGroups) {
    // Validate input
    if (userGroups == null || userGroups.isEmpty()) {
      throw new BadRequestException(
          String.format(
              ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "User group"));
    }

    springapi.models.responsemodels.BulkInsertResponseModel<Long> response =
        new springapi.models.responsemodels.BulkInsertResponseModel<>();
    response.setTotalRequested(userGroups.size());

    int successCount = 0;
    int failureCount = 0;

    // Process each group individually
    for (UserGroupRequestModel groupRequest : userGroups) {
      try {
        // Call createUserGroup with current user and shouldLog = false
        createUserGroupInternal(groupRequest, getUser(), false);

        // If we get here, group was created successfully
        // Fetch the created group to get the groupId
        UserGroup createdGroup = userGroupRepository.findByGroupName(groupRequest.getGroupName());
        response.addSuccess(groupRequest.getGroupName(), createdGroup.getGroupId());
        successCount++;

      } catch (BadRequestException bre) {
        // Validation or business logic error
        response.addFailure(
            groupRequest.getGroupName() != null ? groupRequest.getGroupName() : UNKNOWN_GROUP_NAME,
            bre.getMessage());
        failureCount++;
      } catch (Exception e) {
        // Unexpected error
        response.addFailure(
            groupRequest.getGroupName() != null ? groupRequest.getGroupName() : UNKNOWN_GROUP_NAME,
            "Error: " + e.getMessage());
        failureCount++;
      }
    }

    // Log bulk user group creation
    userLogService.logData(
        getUserId(),
        SuccessMessages.UserGroupSuccessMessages.INSERT_GROUP
            + " (Bulk: "
            + successCount
            + " succeeded, "
            + failureCount
            + " failed)",
        ApiRoutes.UserGroupSubRoute.BULK_CREATE_USER_GROUP);

    response.setSuccessCount(successCount);
    response.setFailureCount(failureCount);

    return response;
  }

  // ==================== HELPER METHODS ====================

  /**
   * Creates a new user group in the system with explicit createdUser. This variant is used for.
   * async operations where security context is not available.
   *
   * @param userGroupRequest The UserGroupRequestModel containing the group data to insert
   * @param createdUser The loginName of the user creating this group (for async operations)
   * @param shouldLog Whether to log this individual group creation (false for bulk operations)
   * @throws BadRequestException if the user group data is invalid or incomplete
   */
  @org.springframework.transaction.annotation.Transactional
  private void createUserGroupInternal(
      UserGroupRequestModel userGroupRequest, String createdUser, boolean shouldLog) {
    // Validate that at least one user is provided
    if (userGroupRequest.getUserIds() == null || userGroupRequest.getUserIds().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER004);
    }

    // Create and save the user group
    UserGroup userGroup = new UserGroup(userGroupRequest, createdUser, getClientId());

    // Check if group name already exists
    UserGroup existingGroup = userGroupRepository.findByGroupName(userGroup.getGroupName());
    if (existingGroup != null) {
      throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GROUP_NAME_EXISTS);
    }
    UserGroup savedGroup = userGroupRepository.save(userGroup);

    // Create user-group mappings
    createUserGroupMappings(userGroupRequest, savedGroup, createdUser);

    // Log user group creation (skip for bulk operations as they log collectively)
    if (shouldLog) {
      userLogService.logData(
          getUserId(),
          SuccessMessages.UserGroupSuccessMessages.INSERT_GROUP + " " + savedGroup.getGroupId(),
          ApiRoutes.UserGroupSubRoute.CREATE_USER_GROUP);
    }
  }

  /**
   * Creates user-group mappings for a user group.
   *
   * @param userGroupRequest The user group request containing user IDs
   * @param savedGroup The saved user group entity
   * @param createdUser The loginName of the user creating these mappings
   */
  private void createUserGroupMappings(
      UserGroupRequestModel userGroupRequest, UserGroup savedGroup, String createdUser) {
    if (userGroupRequest.getUserIds() != null && !userGroupRequest.getUserIds().isEmpty()) {
      List<UserGroupUserMap> mappings = new ArrayList<>();
      for (Long userId : userGroupRequest.getUserIds()) {
        UserGroupUserMap mapping =
            new UserGroupUserMap(userId, savedGroup.getGroupId(), createdUser);
        mappings.add(mapping);
      }
      userGroupUserMapRepository.saveAll(mappings);
    }
  }

  /**
   * Updates user-group mappings for an existing user group. Deletes all existing mappings and.
   * creates new ones.
   *
   * @param userGroupRequest The user group request containing user IDs
   * @param updatedGroup The updated user group entity
   */
  private void updateUserGroupMappings(
      UserGroupRequestModel userGroupRequest, UserGroup updatedGroup) {
    // Delete existing user-group mappings
    List<UserGroupUserMap> existingMappings =
        userGroupUserMapRepository.findByGroupId(userGroupRequest.getGroupId());
    if (!existingMappings.isEmpty()) {
      userGroupUserMapRepository.deleteAll(existingMappings);
    }

    // Create new user-group mappings
    if (userGroupRequest.getUserIds() != null && !userGroupRequest.getUserIds().isEmpty()) {
      List<UserGroupUserMap> mappings = new ArrayList<>();
      for (Long userId : userGroupRequest.getUserIds()) {
        UserGroupUserMap mapping =
            new UserGroupUserMap(userId, updatedGroup.getGroupId(), getUser());
        mappings.add(mapping);
      }
      userGroupUserMapRepository.saveAll(mappings);
    }
  }
}
