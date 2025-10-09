package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response model for User operations.
 * 
 * This model contains all the fields returned when retrieving user information.
 * It includes audit fields, metadata, and permissions for comprehensive user data.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class UserResponseModel {
    
    private Long userId;
    private String loginName;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime datePasswordChanges;
    private Integer loginAttempts;
    private String role;
    private Boolean isDeleted;
    private Boolean locked;
    private Boolean emailConfirmed;
    private LocalDate dob;
    private Boolean isGuest;
    private Integer lockedAttempts;
    private String apiKey;
    private String email;
    private Long addressId;
    private String profilePicture;
    private LocalDateTime lastLoginAt;
    private String createdUser;
    private String modifiedUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private List<UserPermissionInfo> permissions;
    
    /**
     * Default constructor.
     */
    public UserResponseModel() {}
    
    /**
     * Constructor that populates fields from a User entity.
     * 
     * @param user The User entity to populate from
     */
    public UserResponseModel(User user) {
        if (user != null) {
            this.userId = user.getUserId();
            this.loginName = user.getLoginName();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.phone = user.getPhone();
            this.datePasswordChanges = user.getDatePasswordChanges();
            this.loginAttempts = user.getLoginAttempts();
            this.role = user.getRole();
            this.isDeleted = user.getIsDeleted();
            this.locked = user.getLocked();
            this.emailConfirmed = user.getEmailConfirmed();
            this.dob = user.getDob();
            this.isGuest = user.getIsGuest();
            this.lockedAttempts = user.getLockedAttempts();
            this.apiKey = user.getApiKey();
            this.email = user.getEmail();
            this.addressId = user.getAddressId();
            this.profilePicture = user.getProfilePicture();
            this.lastLoginAt = user.getLastLoginAt();
            this.createdUser = user.getCreatedUser();
            this.modifiedUser = user.getModifiedUser();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
            this.notes = user.getNotes();
        }
    }
    
    /**
     * Inner class to represent permission information for a user.
     */
    @Getter
    @Setter
    public static class UserPermissionInfo {
        private Long permissionId;
        private String permissionName;
        private String permissionCode;
        private String description;
        private String category;
        
        public UserPermissionInfo() {}
        
        public UserPermissionInfo(Long permissionId, String permissionName, String permissionCode, 
                                String description, String category) {
            this.permissionId = permissionId;
            this.permissionName = permissionName;
            this.permissionCode = permissionCode;
            this.description = description;
            this.category = category;
        }
    }
}
