package com.example.SpringApi.Models.RequestModels;

import com.example.SpringApi.Models.Enums.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserRequestModel extends PaginationBaseRequestModel {
    
    private Long userId;
    private String loginName;
    private String password;
    private String salt;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime datePasswordChanges;
    private Integer loginAttempts;
    private String role;
    private Boolean isDeleted;
    private Boolean locked;
    private Boolean emailConfirmed;
    private String token;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private Boolean isGuest;
    private String apiKey;
    private String email;
    private Long addressId;
    private String profilePicture;
    private LocalDateTime lastLoginAt;
    private String notes;
    
    // Additional fields for user creation
    private List<Long> permissionIds;
    private List<Long> selectedGroupIds;
    private AddressRequestModel address;
    private String profilePictureBase64;
    private List<Long> selectedUserIds;
    
    /**
     * Get the role as a UserRole enum
     * @return UserRole enum or null if role is invalid
     */
    @JsonIgnore
    public UserRole getRoleEnum() {
        return UserRole.fromString(role);
    }
    
    /**
     * Validate if the role string is valid
     * @return true if valid, false otherwise
     */
    @JsonIgnore
    public boolean isRoleValid() {
        return UserRole.isValid(role);
    }
}
