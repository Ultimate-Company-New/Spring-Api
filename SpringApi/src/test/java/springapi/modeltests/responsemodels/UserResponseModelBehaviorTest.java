package springapi.modeltests.responsemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.databasemodels.Address;
import springapi.models.databasemodels.Permission;
import springapi.models.databasemodels.User;
import springapi.models.databasemodels.UserClientPermissionMapping;
import springapi.models.databasemodels.UserGroup;
import springapi.models.databasemodels.UserGroupUserMap;
import springapi.models.responsemodels.UserResponseModel;

@DisplayName("User Response Model Behavior Tests")
class UserResponseModelBehaviorTest {

  // Total Tests: 2

  /**
   * Purpose: Verify full constructor maps nested addresses, permissions, and user groups with
   * sorting/filtering rules. Expected Result: Addresses sort desc by addressId, permissions sort
   * asc by permissionId, and deleted groups are excluded. Assertions: Collection sizes and sorted
   * IDs match expected order.
   */
  @Test
  @DisplayName("userResponseModel - FullConstructor MapsAndSortsNestedCollections - Success")
  void userResponseModel_s01_fullConstructorMapsAndSortsNestedCollections_success() {
    // Arrange
    User user = new User();
    user.setUserId(10L);
    user.setLoginName("john.doe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setPhone("1234567890");
    user.setDatePasswordChanges(LocalDateTime.now().minusDays(2));
    user.setLoginAttempts(1);
    user.setRole("Admin");
    user.setIsDeleted(false);
    user.setLocked(false);
    user.setEmailConfirmed(true);
    user.setDob(LocalDate.of(1990, 1, 1));
    user.setIsGuest(false);
    user.setEmail("john@example.com");
    user.setAddressId(200L);
    user.setProfilePicture("img.png");
    user.setLastLoginAt(LocalDateTime.now().minusHours(5));
    user.setCreatedUser("system");
    user.setModifiedUser("system");
    user.setCreatedAt(LocalDateTime.now().minusDays(30));
    user.setUpdatedAt(LocalDateTime.now().minusDays(1));
    user.setNotes("notes");

    Address addressOne = new Address();
    addressOne.setAddressId(1L);
    addressOne.setCity("A");
    Address addressTwo = new Address();
    addressTwo.setAddressId(5L);
    addressTwo.setCity("B");
    user.setAddresses(new HashSet<>(Set.of(addressOne, addressTwo)));

    Permission permissionOne = new Permission();
    permissionOne.setPermissionId(9L);
    permissionOne.setPermissionName("Edit");
    Permission permissionTwo = new Permission();
    permissionTwo.setPermissionId(4L);
    permissionTwo.setPermissionName("View");

    UserClientPermissionMapping mappingOne = new UserClientPermissionMapping();
    mappingOne.setPermission(permissionOne);
    UserClientPermissionMapping mappingTwo = new UserClientPermissionMapping();
    mappingTwo.setPermission(permissionTwo);
    user.setUserClientPermissionMappings(new HashSet<>(Set.of(mappingOne, mappingTwo)));

    UserGroup activeGroup = new UserGroup();
    activeGroup.setGroupId(3L);
    activeGroup.setGroupName("Ops");
    activeGroup.setIsDeleted(false);

    UserGroup deletedGroup = new UserGroup();
    deletedGroup.setGroupId(2L);
    deletedGroup.setGroupName("Old");
    deletedGroup.setIsDeleted(true);

    UserGroupUserMap groupMapOne = new UserGroupUserMap();
    groupMapOne.setUserGroup(activeGroup);
    UserGroupUserMap groupMapTwo = new UserGroupUserMap();
    groupMapTwo.setUserGroup(deletedGroup);
    user.setUserGroupMappings(new HashSet<>(Set.of(groupMapOne, groupMapTwo)));

    // Act
    UserResponseModel responseModel = new UserResponseModel(user);

    // Assert
    assertNotNull(responseModel.getAddresses());
    assertEquals(2, responseModel.getAddresses().size());
    assertEquals(5L, responseModel.getAddresses().get(0).getAddressId());
    assertEquals(1L, responseModel.getAddresses().get(1).getAddressId());

    assertNotNull(responseModel.getPermissions());
    assertEquals(2, responseModel.getPermissions().size());
    assertEquals(4L, responseModel.getPermissions().get(0).getPermissionId());
    assertEquals(9L, responseModel.getPermissions().get(1).getPermissionId());

    assertNotNull(responseModel.getUserGroups());
    assertEquals(1, responseModel.getUserGroups().size());
    assertEquals(3L, responseModel.getUserGroups().get(0).getGroupId());
  }

  /**
   * Purpose: Verify minimal constructor suppresses nested collection mapping. Expected Result: Core
   * fields are mapped while permissions/addresses/userGroups remain null. Assertions: Core scalar
   * fields are populated and nested collections are null.
   */
  @Test
  @DisplayName("userResponseModel - MinimalConstructor ExcludesNestedCollections - Success")
  void userResponseModel_s02_minimalConstructorExcludesNestedCollections_success() {
    // Arrange
    User user = new User();
    user.setUserId(77L);
    user.setLoginName("minimal.user");
    user.setFirstName("Minimal");
    user.setLastName("User");
    user.setEmail("minimal@example.com");

    // Act
    UserResponseModel responseModel = new UserResponseModel(user, true);

    // Assert
    assertEquals(77L, responseModel.getUserId());
    assertEquals("minimal.user", responseModel.getLoginName());
    assertEquals("minimal@example.com", responseModel.getEmail());
    assertNull(responseModel.getPermissions());
    assertNull(responseModel.getAddresses());
    assertNull(responseModel.getUserGroups());
  }
}
