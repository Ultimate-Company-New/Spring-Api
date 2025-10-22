package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<AddressResponseModel> addresses;
    private List<UserGroupResponseModel> userGroups;
    
    /**
     * Default constructor.
     */
    public UserResponseModel() {}
    
    /**
     * Constructor that populates fields from a User entity.
     * Automatically populates address, permissions, and userGroups if available.
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
            this.email = user.getEmail();
            this.addressId = user.getAddressId();
            this.profilePicture = user.getProfilePicture();
            this.lastLoginAt = user.getLastLoginAt();
            this.createdUser = user.getCreatedUser();
            this.modifiedUser = user.getModifiedUser();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
            this.notes = user.getNotes();
            
            // Auto-populate address from addresses collection
            if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
                // Find primary address or use first non-deleted address
                this.addresses = user.getAddresses().stream()
                    .map(address -> new AddressResponseModel(address))
                    .collect(Collectors.toList());
            }
            
            // Auto-populate permissions if available
            if (user.getUserClientPermissionMappings() != null && !user.getUserClientPermissionMappings().isEmpty()) {
                this.permissions = user.getUserClientPermissionMappings().stream()
                    .map(ucpm -> new UserPermissionInfo(
                        ucpm.getPermission().getPermissionId(),
                        ucpm.getPermission().getPermissionName(),
                        ucpm.getPermission().getPermissionCode(),
                        ucpm.getPermission().getDescription(),
                        ucpm.getPermission().getCategory()
                    ))
                    .collect(Collectors.toList());
            }
            
            // Auto-populate userGroups if available (without users to avoid circular reference)
            if (user.getUserGroupMappings() != null && !user.getUserGroupMappings().isEmpty()) {
                this.userGroups = user.getUserGroupMappings().stream()
                    .filter(ugm -> !ugm.getUserGroup().getIsDeleted())
                    .map(ugm -> new UserGroupResponseModel(ugm.getUserGroup(), false))
                    .collect(Collectors.toList());
            }
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
