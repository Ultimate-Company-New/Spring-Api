package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * JPA Entity representing the User table.
 * 
 * This entity maps to the User table in the UltimateCompany database and includes
 * all user-related fields, relationships, and business logic. It provides constructors
 * for both creation and update scenarios with proper validation and audit handling.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`User`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "loginName", unique = true, nullable = false)
    private String loginName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "datePasswordChanges")
    private LocalDateTime datePasswordChanges;

    @Column(name = "loginAttempts", nullable = false)
    private Integer loginAttempts;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "locked", nullable = false)
    private Boolean locked;

    @Column(name = "emailConfirmed", nullable = false)
    private Boolean emailConfirmed;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "isGuest", nullable = false)
    private Boolean isGuest;

    @Column(name = "apiKey", nullable = false)
    private String apiKey;

    @Column(name = "email")
    private String email;

    @Column(name = "addressId")
    private Long addressId;

    @Column(name = "profilePicture")
    private String profilePicture;

    @Column(name = "lastLoginAt")
    private LocalDateTime lastLoginAt;

    @Column(name = "createdUser")
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @Column(name = "modifiedUser")
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

    @Column(name = "notes")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserClientMapping> userClientMappings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGroupUserMap> userGroupMappings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserClientPermissionMapping> userClientPermissionMappings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGridPreference> userGridPreferences;

    @ManyToMany
    @JoinTable(
        name = "`UserGroupUserMap`",
        joinColumns = @JoinColumn(name = "userId"),
        inverseJoinColumns = @JoinColumn(name = "groupId")
    )
    private List<UserGroup> userGroups;

    /**
     * Constructor for creating a new customer user during signup.
     * 
     * @param request The UserRequestModel containing user data
     */
    public User(UserRequestModel request) {
        validateRequest(request);
        
        setFieldsFromRequest(request);
        this.role = "Customer"; // Ensure customer role
        this.createdUser = "system";
        this.modifiedUser = "system";
    }

    /**
     * Constructor for creating a new user.
     * 
     * @param request The UserRequestModel containing user data
     * @param createdUser The username of the user creating this record
     */
    public User(UserRequestModel request, String createdUser) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
    }

    /**
     * Constructor for updating an existing user.
     * 
     * @param request The UserRequestModel containing updated user data
     * @param modifiedUser The username of the user modifying this record
     * @param existingUser The existing User entity to update
     */
    public User(UserRequestModel request, String modifiedUser, User existingUser) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        // Copy existing values that shouldn't change
        this.userId = existingUser.getUserId();
        this.createdUser = existingUser.getCreatedUser();
        this.createdAt = existingUser.getCreatedAt();
        
        setFieldsFromRequest(request);
        this.modifiedUser = modifiedUser;  // When updating, use the provided modified user
    }

    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The UserRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(UserRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
        }
        
        if (request.getLoginName() == null || request.getLoginName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidLoginName);
        }
        
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidFirstName);
        }
        
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidLastName);
        }
        
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidPhone);
        }
        
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRole);
        }
        
        if (request.getDob() == null) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidDob);
        }
    }

    /**
     * Validates the user parameter for audit fields.
     * 
     * @param user The username to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
        }
    }

    /**
     * Sets entity fields from the request model.
     * 
     * @param request The UserRequestModel containing the data to set
     */
    private void setFieldsFromRequest(UserRequestModel request) {
        this.loginName = request.getLoginName();
        this.password = request.getPassword();
        this.salt = request.getSalt();
        this.firstName = request.getFirstName();
        this.lastName = request.getLastName();
        this.phone = request.getPhone();
        this.datePasswordChanges = request.getDatePasswordChanges();
        this.loginAttempts = request.getLoginAttempts() != null ? request.getLoginAttempts() : 5;
        this.role = request.getRole();
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE;
        this.locked = request.getLocked() != null ? request.getLocked() : Boolean.FALSE;
        this.emailConfirmed = request.getEmailConfirmed() != null ? request.getEmailConfirmed() : Boolean.FALSE;
        this.token = request.getToken();
        this.dob = request.getDob();
        this.isGuest = request.getIsGuest() != null ? request.getIsGuest() : Boolean.FALSE;
        this.apiKey = request.getApiKey();
        this.email = request.getEmail();
        this.addressId = request.getAddressId();
        this.profilePicture = request.getProfilePicture();
        this.lastLoginAt = request.getLastLoginAt();
        this.notes = request.getNotes();
    }
}
