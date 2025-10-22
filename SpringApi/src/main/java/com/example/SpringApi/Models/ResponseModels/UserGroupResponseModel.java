package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public class UserGroupResponseModel {
    
    private Long groupId;
    private Long clientId;
    private String groupName;
    private String description;
    private Boolean isDeleted;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // List of users in this group
    private List<UserResponseModel> users;
    
    // Default constructor
    public UserGroupResponseModel() {
        this.users = new ArrayList<>();
    }
    
    // Constructor that takes UserGroups entity and populates response fields
    public UserGroupResponseModel(UserGroup userGroup) {
        this(userGroup, true);
    }
    
    // Constructor that takes UserGroups entity and optionally populates users
    public UserGroupResponseModel(UserGroup userGroup, boolean includeUsers) {
        this();
        if (userGroup != null) {
            this.groupId = userGroup.getGroupId();
            this.clientId = userGroup.getClientId();
            this.groupName = userGroup.getGroupName();
            this.description = userGroup.getDescription();
            this.isDeleted = userGroup.getIsDeleted();
            this.createdUser = userGroup.getCreatedUser();
            this.modifiedUser = userGroup.getModifiedUser();
            this.createdAt = userGroup.getCreatedAt();
            this.updatedAt = userGroup.getUpdatedAt();
            
            // Auto-populate users from mappings if available and requested
            if (includeUsers && userGroup.getUserMappings() != null && !userGroup.getUserMappings().isEmpty()) {
                this.users = new ArrayList<>();
                for (var mapping : userGroup.getUserMappings()) {
                    if (mapping.getUser() != null) {
                        this.users.add(new UserResponseModel(mapping.getUser()));
                    }
                }
            }
        }
    }
    
    // Constructor with user list
    public UserGroupResponseModel(UserGroup userGroup, List<UserResponseModel> users) {
        this(userGroup);
        this.users = users != null ? users : new ArrayList<>();
    }
}