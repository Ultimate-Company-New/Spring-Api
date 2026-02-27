package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for User operations.
 *
 * <p>This model contains all the fields returned when retrieving user information. It includes
 * audit fields, metadata, and permissions for comprehensive user data.
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
  private List<PermissionResponseModel> permissions;
  private List<AddressResponseModel> addresses;
  private List<UserGroupResponseModel> userGroups;

  /** Default constructor. */
  public UserResponseModel() {}

  /**
   * Minimal constructor for Purchase Order responses. Excludes permissions, addresses, and
   * userGroups.
   *
   * @param user The User entity to populate from
   * @param minimal If true, exclude permissions, addresses, and userGroups
   */
  public UserResponseModel(User user, boolean minimal) {
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

      // Only populate permissions, addresses, and userGroups if minimal is false
      if (!minimal) {
        // Auto-populate address from addresses collection
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
          // Map addresses and sort by addressId descending to match AddressRepository ordering
          this.addresses =
              user.getAddresses().stream()
                  .map(AddressResponseModel::new)
                  .sorted((a1, a2) -> Long.compare(a2.getAddressId(), a1.getAddressId()))
                  .toList();
        }

        // Auto-populate permissions if available
        if (user.getUserClientPermissionMappings() != null
            && !user.getUserClientPermissionMappings().isEmpty()) {
          this.permissions =
              user.getUserClientPermissionMappings().stream()
                  .map(ucpm -> new PermissionResponseModel(ucpm.getPermission()))
                  .sorted((p1, p2) -> Long.compare(p1.getPermissionId(), p2.getPermissionId()))
                  .toList();
        }

        // Auto-populate userGroups if available (without users to avoid circular reference)
        if (user.getUserGroupMappings() != null && !user.getUserGroupMappings().isEmpty()) {
          this.userGroups =
              user.getUserGroupMappings().stream()
                  .filter(ugm -> !ugm.getUserGroup().getIsDeleted())
                  .map(ugm -> new UserGroupResponseModel(ugm.getUserGroup(), false))
                  .sorted(
                      (g1, g2) ->
                          Long.compare(
                              g1.getGroupId(), g2.getGroupId())) // Sort by groupId ascending
                  .toList();
        }
      }
      // If minimal is true, permissions, addresses, and userGroups remain null
    }
  }

  /**
   * Constructor that populates fields from a User entity. Automatically populates address,
   * permissions, and userGroups if available.
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
        // Map addresses and sort by addressId descending to match AddressRepository ordering
        this.addresses =
            user.getAddresses().stream()
                .map(AddressResponseModel::new)
                .sorted((a1, a2) -> Long.compare(a2.getAddressId(), a1.getAddressId()))
                .toList();
      }

      // Auto-populate permissions if available
      if (user.getUserClientPermissionMappings() != null
          && !user.getUserClientPermissionMappings().isEmpty()) {
        this.permissions =
            user.getUserClientPermissionMappings().stream()
                .map(ucpm -> new PermissionResponseModel(ucpm.getPermission()))
                .sorted((p1, p2) -> Long.compare(p1.getPermissionId(), p2.getPermissionId()))
                .toList();
      }

      // Auto-populate userGroups if available (without users to avoid circular reference)
      if (user.getUserGroupMappings() != null && !user.getUserGroupMappings().isEmpty()) {
        this.userGroups =
            user.getUserGroupMappings().stream()
                .filter(ugm -> !ugm.getUserGroup().getIsDeleted())
                .map(ugm -> new UserGroupResponseModel(ugm.getUserGroup(), false))
                .sorted(
                    (g1, g2) ->
                        Long.compare(g1.getGroupId(), g2.getGroupId())) // Sort by groupId ascending
                .toList();
      }
    }
  }
}
