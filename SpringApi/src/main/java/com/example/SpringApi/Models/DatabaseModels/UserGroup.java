package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "`UserGroup`")
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "groupId", nullable = false)
    private Long groupId;

    @Column(name = "clientId", nullable = false)
    private Long clientId;

    @Column(name = "groupName", nullable = false, length = 100)
    private String groupName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifiedUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User modifiedByUser;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientId", insertable = false, updatable = false)
    private Client client;

    public UserGroup() {}

    // Constructor for creating new user group
    public UserGroup(UserGroupRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
        this.isActive = true;  // New groups are always active
        this.isDeleted = false;  // New groups are not deleted
    }

    // Constructor for updating existing user group
    public UserGroup(UserGroupRequestModel request, String modifiedUser, UserGroup existingUserGroup) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        // Copy existing values that shouldn't change
        this.groupId = existingUserGroup.getGroupId();
        this.createdUser = existingUserGroup.getCreatedUser();
        this.createdAt = existingUserGroup.getCreatedAt();
        this.isActive = existingUserGroup.getIsActive();
        this.isDeleted = existingUserGroup.getIsDeleted();
        this.notes = existingUserGroup.getNotes();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;  // When updating, use the provided modified user
    }

    private void validateRequest(UserGroupRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER001);
        }
        
        // Validate client ID (required, must be positive)
        if (request.getClientId() == null || request.getClientId() <= 0) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
        
        // Validate group name (required, length > 0)
        if (request.getGroupName() == null || request.getGroupName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER002);
        }
        
        // Validate description (required, length > 0)
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.ER003);
        }
        
        // Validate group name length (max 100 characters)
        if (request.getGroupName().trim().length() > 100) {
            throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.GroupNameExists);
        }
    }

    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }

    private void setFieldsFromRequest(UserGroupRequestModel request) {
        this.clientId = request.getClientId();
        this.groupName = request.getGroupName().trim();
        this.description = request.getDescription() != null ? request.getDescription().trim() : null;
    }
}