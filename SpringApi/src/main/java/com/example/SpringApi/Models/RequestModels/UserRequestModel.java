package com.example.SpringApi.Models.RequestModels;

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
    private LocalDate dob;
    private Boolean isGuest;
    private Integer lockedAttempts;
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
}
