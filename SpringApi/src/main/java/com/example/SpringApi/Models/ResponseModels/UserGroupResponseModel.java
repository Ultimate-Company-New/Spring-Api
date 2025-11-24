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
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // List of user IDs in this group
    private List<Long> userIds;
    
    // Default constructor
    public UserGroupResponseModel() {
        this.userIds = new ArrayList<>();
    }
    
    // Constructor that takes UserGroups entity and populates response fields
    public UserGroupResponseModel(UserGroup userGroup) {
        this(userGroup, true);
    }
    
    // Constructor that takes UserGroups entity and optionally populates user IDs
    public UserGroupResponseModel(UserGroup userGroup, boolean includeUserIds) {
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
            this.notes = userGroup.getNotes();
            
            // Auto-populate user IDs from mappings if available and requested
            if (includeUserIds && userGroup.getUserMappings() != null && !userGroup.getUserMappings().isEmpty()) {
                this.userIds = new ArrayList<>();
                for (var mapping : userGroup.getUserMappings()) {
                    if (mapping.getUserId() != null) {
                        this.userIds.add(mapping.getUserId());
                    }
                }
                // Sort user IDs in descending order to ensure consistent ordering
                this.userIds.sort((id1, id2) -> Long.compare(id2, id1));
            }
        }
    }
}