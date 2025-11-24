package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Repositories.UserGroupRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.IUserGroupSubTranslator;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.FilterQueryBuilder.UserGroupFilterQueryBuilder;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Helpers.BulkInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.SpringApi.Exceptions.NotFoundException;

import java.util.*;

/**
 * Service class for managing UserGroup-related business operations.
 * 
 * This service implements the IUserGroupervice interface and provides
 * comprehensive user group management functionality including CRUD operations,
 * client-specific group retrieval, active group filtering, and user membership
 * management. The service handles validation, error handling, audit logging,
 * and database persistence.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class UserGroupService extends BaseService implements IUserGroupSubTranslator {
    private final UserGroupRepository userGroupRepository;
    private final UserGroupUserMapRepository userGroupUserMapRepository;
    private final UserLogService userLogService;
    private final UserGroupFilterQueryBuilder userGroupFilterQueryBuilder;
    private final MessageService messageService;

    @Autowired
    public UserGroupService(UserLogService userLogService,
                           UserGroupRepository userGroupRepository,
                           UserGroupUserMapRepository userGroupUserMapRepository,
                           UserRepository userRepository,
                           UserGroupFilterQueryBuilder userGroupFilterQueryBuilder,
                           MessageService messageService) {
        super();
        this.userLogService = userLogService;
        this.userGroupRepository = userGroupRepository;
        this.userGroupUserMapRepository = userGroupUserMapRepository;
        this.userRepository = userRepository;
        this.userGroupFilterQueryBuilder = userGroupFilterQueryBuilder;
        this.messageService = messageService;
    }

    /**
     * Toggles the deletion status of a user group by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the group is currently active (isDeleted = false), it will be marked as deleted.
     * If the group is currently deleted (isDeleted = true), it will be restored.
     * The operation is logged for audit purposes.
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
            userLogService.logData(getUserId(), 
                SuccessMessages.UserGroupSuccessMessages.ToggleGroup + " " + userGroup.get().getGroupId(),
                ApiRoutes.UserGroupSubRoute.TOGGLE_USER_GROUP);
        } else {
            throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.InvalidId);
        }
    }

    /**
     * Retrieves a single user group by its unique identifier.
     * 
     * This method fetches a user group from the database using the provided ID.
     * The returned UserGroupResponseModel contains all group details including
     * name, description, client association, metadata, and the list of users
     * in the group.
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
            throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.InvalidId);
        }
    }

    /**
     * Creates a new user group in the system.
     * 
     * This method validates the provided user group data, creates a new UserGroup entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. It also creates mappings
     * for all users specified in the request. The operation is logged for audit purposes.
     * 
     * @param userGroupRequest The UserGroupRequestModel containing the group data to insert
     * @throws BadRequestException if the user group data is invalid or incomplete
     */
    @Override
    public void createUserGroup(UserGroupRequestModel userGroupRequest) {
        // Create and save the user group
        UserGroup userGroup = new UserGroup(userGroupRequest, getUser(), getClientId());

        // Check if group name already exists
        UserGroup existingGroup = userGroupRepository.findByGroupName(userGroup.getGroupName());
        if (existingGroup != null) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GroupNameExists);
        }
        UserGroup savedGroup = userGroupRepository.save(userGroup);
        
        // Create user-group mappings
        List<UserGroupUserMap> mappings = new ArrayList<>();
        for (Long userId : userGroupRequest.getUserIds()) {
            UserGroupUserMap mapping = new UserGroupUserMap(userId, savedGroup.getGroupId(), getUser());
            mappings.add(mapping);
        }
        userGroupUserMapRepository.saveAll(mappings);
        
        userLogService.logData(getUserId(), 
            SuccessMessages.UserGroupSuccessMessages.InsertGroup + " " + savedGroup.getGroupId(),
            ApiRoutes.UserGroupSubRoute.CREATE_USER_GROUP);
    }

    /**
     * Updates an existing user group with new information.
     * 
     * This method retrieves the existing user group by ID, validates the new data,
     * and updates the group while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * It also updates the user-group mappings by removing old mappings and
     * creating new ones. The operation is logged for audit purposes.
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
            UserGroup groupWithSameName = userGroupRepository.findByGroupName(userGroupRequest.getGroupName());
            if (groupWithSameName != null && !groupWithSameName.getGroupId().equals(userGroupRequest.getGroupId())) {
                throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GroupNameExists);
            }
            
            // Update the user group
            UserGroup userGroup = new UserGroup(userGroupRequest, getUser(), existingGroup.get(), getClientId());
            UserGroup updatedGroup = userGroupRepository.save(userGroup);
            
            // Delete existing user-group mappings
            List<UserGroupUserMap> existingMappings = userGroupUserMapRepository.findByGroupId(userGroupRequest.getGroupId());
            if (!existingMappings.isEmpty()) {
                userGroupUserMapRepository.deleteAll(existingMappings);
            }
            
            // Create new user-group mappings
            List<UserGroupUserMap> mappings = new ArrayList<>();
            for (Long userId : userGroupRequest.getUserIds()) {
                UserGroupUserMap mapping = new UserGroupUserMap(userId, updatedGroup.getGroupId(), getUser());
                mappings.add(mapping);
            }
            userGroupUserMapRepository.saveAll(mappings);
            
            userLogService.logData(getUserId(), 
                SuccessMessages.UserGroupSuccessMessages.UpdateGroup + " " + updatedGroup.getGroupId(),
                ApiRoutes.UserGroupSubRoute.UPDATE_USER_GROUP);
        } else {
            throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.InvalidId);
        }
    }

    /**
     * Retrieves user groups for a client with pagination, filtering, and sorting.
     * 
     * This method fetches user groups with support for:
     * - Multi-filter support with AND/OR logic
     * - Column-based filtering (groupName, description, etc.)
     * - Various filter conditions (contains, equals, startsWith, endsWith, isEmpty, isNotEmpty)
     * - Pagination with configurable page size
     * - Optional inclusion of deleted groups
     * - Optional filtering by specific group IDs
     * 
     * Valid columns for filtering: "groupId", "groupName", "description", "isActive", "isDeleted", 
     *                               "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes"
     * 
     * @param userGroupRequestModel The request model containing filter criteria and pagination settings
     * @return PaginationBaseResponseModel containing the filtered and paginated user groups
     * @throws BadRequestException if an invalid column name or filter condition is provided
     */
    @Override
    public PaginationBaseResponseModel<UserGroupResponseModel> fetchUserGroupsInClientInBatches(UserGroupRequestModel userGroupRequestModel) {
        // Validate page size
        int start = userGroupRequestModel.getStart();
        int end = userGroupRequestModel.getEnd();
        int pageSize = end - start;
        
        if (pageSize <= 0) {
            throw new BadRequestException("Invalid pagination: end must be greater than start");
        }

        // Validate logic operator if provided
        if (userGroupRequestModel.getLogicOperator() != null && !userGroupRequestModel.isValidLogicOperator()) {
            throw new BadRequestException("Invalid logic operator. Must be 'AND' or 'OR'");
        }

        // Validate filters if provided
        if (userGroupRequestModel.hasMultipleFilters()) {
            Set<String> validColumns = new HashSet<>(Arrays.asList(
                "groupId", "clientId", "groupName", "description", "isActive", "isDeleted",
                "notes", "createdUser", "modifiedUser", "createdAt", "updatedAt"
            ));

            for (FilterCondition filter : userGroupRequestModel.getFilters()) {
                // Validate column name
                if (!validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException(
                        "Invalid column name: " + filter.getColumn() + 
                        ". Valid columns: " + String.join(", ", validColumns)
                    );
                }

                // Validate operator
                if (!filter.isValidOperator()) {
                    throw new BadRequestException(
                        "Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn()
                    );
                }

                // Validate operator matches column type
                String columnType = userGroupFilterQueryBuilder.getColumnType(filter.getColumn());
                filter.validateOperatorForType(columnType, filter.getColumn());

                // Validate value presence
                filter.validateValuePresence();
            }
        }

        // Create custom Pageable with exact OFFSET and LIMIT for database-level pagination
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("groupId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Use the filter query builder for multi-filter support
        String logicOperator = userGroupRequestModel.getLogicOperator() != null ? 
            userGroupRequestModel.getLogicOperator() : "AND";

        Page<UserGroup> userGroups = userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            userGroupRequestModel.getSelectedGroupIds(),
            logicOperator,
            userGroupRequestModel.getFilters(),
            userGroupRequestModel.isIncludeDeleted(),
            pageable
        );

        PaginationBaseResponseModel<UserGroupResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
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
     * Creates multiple user groups in a single operation.
     * 
     * @param userGroups List of UserGroupRequestModel containing the group data to insert
     * @return BulkInsertResponseModel containing success/failure details for each group
     */
    @Override
    public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreateUserGroups(List<UserGroupRequestModel> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            throw new BadRequestException("User group list cannot be null or empty");
        }

        com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = 
            new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
        response.setTotalRequested(userGroups.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (UserGroupRequestModel groupRequest : userGroups) {
            try {
                createUserGroup(groupRequest);
                
                UserGroup createdGroup = userGroupRepository.findByGroupName(groupRequest.getGroupName());
                response.addSuccess(groupRequest.getGroupName(), createdGroup.getGroupId());
                successCount++;
                
            } catch (BadRequestException bre) {
                response.addFailure(
                    groupRequest.getGroupName() != null ? groupRequest.getGroupName() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                response.addFailure(
                    groupRequest.getGroupName() != null ? groupRequest.getGroupName() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        userLogService.logData(getUserId(), 
            SuccessMessages.UserGroupSuccessMessages.InsertGroup + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.UserGroupSubRoute.BULK_CREATE_USER_GROUP);
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        BulkInsertHelper.createBulkInsertResultMessage(response, "User Group", messageService, getUserId());
        
        return response;
    }
}