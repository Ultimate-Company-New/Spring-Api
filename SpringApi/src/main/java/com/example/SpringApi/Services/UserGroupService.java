package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Repositories.UserGroupRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.IUserGroupSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SpringApi.Exceptions.NotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

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
    private final UserRepository userRepository;
    private final UserLogService userLogService;

    @Autowired
    public UserGroupService(HttpServletRequest request,
                           UserLogService userLogService,
                           UserGroupRepository userGroupRepository,
                           UserGroupUserMapRepository userGroupUserMapRepository,
                           UserRepository userRepository) {
        super();
        this.userLogService = userLogService;
        this.userGroupRepository = userGroupRepository;
        this.userGroupUserMapRepository = userGroupUserMapRepository;
        this.userRepository = userRepository;
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
            userLogService.logData(getUser(), 
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
        Optional<UserGroup> userGroup = userGroupRepository.findById(groupId);
        if (userGroup.isPresent()) {
            UserGroupResponseModel responseModel = new UserGroupResponseModel(userGroup.get());
            
            // Get all users in this group
            List<UserGroupUserMap> userMappings = userGroupUserMapRepository.findByGroupId(groupId);
            List<UserResponseModel> users = new ArrayList<>();
            
            for (UserGroupUserMap mapping : userMappings) {
                Optional<User> user = userRepository.findById(mapping.getUserId());
                if (user.isPresent()) {
                    users.add(new UserResponseModel(user.get()));
                }
            }
            
            responseModel.setUsers(users);
            return responseModel;
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
        // Validate that at least one user is provided
        if (userGroupRequest.getUserIds() == null || userGroupRequest.getUserIds().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER004);
        }
        
        // Check if group name already exists
        UserGroup existingGroup = userGroupRepository.findByGroupName(userGroupRequest.getGroupName().trim());
        if (existingGroup != null) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GroupNameExists);
        }
        
        // Create and save the user group
        UserGroup userGroup = new UserGroup(userGroupRequest, getUser());
        UserGroup savedGroup = userGroupRepository.save(userGroup);
        
        // Create user-group mappings
        List<UserGroupUserMap> mappings = new ArrayList<>();
        for (Long userId : userGroupRequest.getUserIds()) {
            UserGroupUserMap mapping = new UserGroupUserMap(userId, savedGroup.getGroupId(), getUser());
            mappings.add(mapping);
        }
        userGroupUserMapRepository.saveAll(mappings);
        
        userLogService.logData(getUser(), 
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
            // Update the user group
            UserGroup userGroup = new UserGroup(userGroupRequest, getUser(), existingGroup.get());
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
            
            userLogService.logData(getUser(), 
                SuccessMessages.UserGroupSuccessMessages.UpdateGroup + " " + updatedGroup.getGroupId(),
                ApiRoutes.UserGroupSubRoute.UPDATE_USER_GROUP);
        } else {
            throw new NotFoundException(ErrorMessages.UserGroupErrorMessages.InvalidId);
        }
    }

    /**
     * Retrieves all user groups in the system.
     * 
     * This method fetches all user groups from the database regardless of status.
     * The method returns a list of UserGroupResponseModel objects, each containing
     * complete group information. Returns an empty list if no groups are found.
     * 
     * @return List of UserGroupResponseModel objects for all user groups
     */
    @Override
    public List<UserGroupResponseModel> getAllUserGroup() {
        List<UserGroup> UserGroup = userGroupRepository.findAll();
        List<UserGroupResponseModel> responseModels = new ArrayList<>();
        
        for (UserGroup userGroup : UserGroup) {
            responseModels.add(new UserGroupResponseModel(userGroup));
        }
        
        return responseModels;
    }

    /**
     * Retrieves all user groups associated with a specific client.
     * 
     * This method fetches all user groups where the clientId matches the provided parameter.
     * The method returns a list of UserGroupResponseModel objects, each containing
     * complete group information. Returns an empty list if no groups are found.
     * 
     * @param clientId The unique identifier of the client
     * @return List of UserGroupResponseModel objects for the client
     */
    @Override
    public List<UserGroupResponseModel> getUserGroupByClientId(long clientId) {
        List<UserGroup> UserGroup = userGroupRepository.findByClientId(clientId);
        List<UserGroupResponseModel> responseModels = new ArrayList<>();
        
        for (UserGroup userGroup : UserGroup) {
            responseModels.add(new UserGroupResponseModel(userGroup));
        }
        
        return responseModels;
    }

    /**
     * Retrieves all active user groups in the system.
     * 
     * This method fetches all user groups where isDeleted is false.
     * The method returns a list of UserGroupResponseModel objects for active groups only.
     * Returns an empty list if no active groups are found.
     * 
     * @return List of UserGroupResponseModel objects for active user groups
     */
    @Override
    public List<UserGroupResponseModel> getActiveUserGroup() {
        List<UserGroup> UserGroup = userGroupRepository.findByIsDeletedFalse();
        List<UserGroupResponseModel> responseModels = new ArrayList<>();
        
        for (UserGroup userGroup : UserGroup) {
            responseModels.add(new UserGroupResponseModel(userGroup));
        }
        
        return responseModels;
    }
}