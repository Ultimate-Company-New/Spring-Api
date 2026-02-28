package com.example.springapi.ServiceTests.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.UserController;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.databasemodels.GoogleCred;
import com.example.springapi.models.databasemodels.User;
import com.example.springapi.models.requestmodels.AddressRequestModel;
import com.example.springapi.models.requestmodels.UserRequestModel;
import com.example.springapi.models.responsemodels.ClientResponseModel;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserService.updateUser method. */
@DisplayName("UserService - UpdateUser Tests")
class UpdateUserTest extends UserServiceTestBase {

  // Total Tests: 22
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify creating new address when user had no address. Expected Result: New address is
   * created and linked to user. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Creates New Address")
  void updateUser_createsNewAddressWhenNoneExists_Success() {
    // Arrange
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setStreetAddress("123 New St");
    addressRequest.setCity("New City");
    addressRequest.setState("NC");
    addressRequest.setPostalCode("11111");
    addressRequest.setCountry("New Country");
    addressRequest.setAddressType("HOME");
    testUserRequest.setAddress(addressRequest);
    testUser.setAddressId(null);

    Address newAddress = new Address();
    newAddress.setAddressId(2L);

    setupUpdateUserMocks();
    stubAddressRepositorySave(newAddress);

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(addressRepository, times(1)).save(any(Address.class));
  }

  /**
   * Purpose: Verify that loginName (email) from the request is preserved in update. Expected
   * Result: User is successfully updated. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Login Name Change Allowed")
  void updateUser_loginNameChange_allowed() {
    // Arrange
    UserRequestModel changeEmailRequest = new UserRequestModel();
    changeEmailRequest.setUserId(TEST_USER_ID);
    changeEmailRequest.setLoginName("newemail@example.com");
    changeEmailRequest.setFirstName("Test");
    changeEmailRequest.setLastName("User");
    changeEmailRequest.setPhone("1234567890");
    changeEmailRequest.setRole("Admin");
    changeEmailRequest.setDob(LocalDate.of(1990, 1, 1));
    changeEmailRequest.setPermissionIds(Arrays.asList(1L, 2L, 3L));
    changeEmailRequest.setIsDeleted(false);

    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserClientPermissionMappingRepositoryFindByUserIdAndClientId(new ArrayList<>());
    stubUserClientPermissionMappingRepositorySaveAll(new ArrayList<>());
    stubUserGroupUserMapRepositoryFindByUserId(new ArrayList<>());
    stubGoogleCredRepositoryFindByIdAny(Optional.of(new GoogleCred()));
    stubClientRepositoryFindByIdAny(Optional.of(createTestClient()));
    stubClientServiceGetClientById(new ClientResponseModel());
    stubUserLogServiceLogData(true);
    stubEnvironmentActiveProfiles(new String[] {"test"});
    stubImgbbHelper();

    // Act
    assertDoesNotThrow(() -> userService.updateUser(changeEmailRequest));

    // Assert
    verify(userRepository, atLeastOnce()).save(any(User.class));
  }

  /**
   * Purpose: Verify single permission update is allowed. Expected Result: User is updated with
   * single permission. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Single Permission")
  void updateUser_singlePermission_success() {
    // Arrange
    testUserRequest.setPermissionIds(Arrays.asList(1L));
    setupUpdateUserMocks();

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify user log is called after successful update. Expected Result:
   * userLogService.logData is called. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Logs Operation")
  void updateUser_success_logsOperation() {
    // Arrange
    setupUpdateUserMocks();

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify successful user update. Expected Result: User is updated and saved. Assertions:
   * verify
   */
  @Test
  @DisplayName("updateUser - Success - Updates User Details")
  void updateUser_success_updatesUserDetails() {
    // Arrange
    testUserRequest.setFirstName("Updated");
    testUserRequest.setLastName("Name");
    setupUpdateUserMocks();
    stubEnvironmentImageLocation("firebase");

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(userRepository, atLeastOnce()).save(any(User.class));
  }

  /**
   * Purpose: Verify permissions are updated. Expected Result: Old permissions deleted, new ones
   * saved. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Updates Permissions")
  void updateUser_updatesPermissions_Success() {
    // Arrange
    testUserRequest.setPermissionIds(Arrays.asList(4L, 5L));
    setupUpdateUserMocks();

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify user groups are updated. Expected Result: Old group mappings deleted, new ones
   * saved. Assertions: verify
   */
  @Test
  @DisplayName("updateUser - Success - Updates User Groups")
  void updateUser_updatesUserGroups_Success() {
    // Arrange
    testUserRequest.setSelectedGroupIds(Arrays.asList(3L, 4L));
    setupUpdateUserMocks();
    stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify address update is successful. Expected Result: Address is updated. Assertions:
   * verify
   */
  @Test
  @DisplayName("updateUser - Success - With Address")
  void updateUser_withAddress_updatesAddress() {
    // Arrange
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setStreetAddress("123 Updated St");
    addressRequest.setCity("Updated City");
    addressRequest.setState("US");
    addressRequest.setPostalCode("54321");
    addressRequest.setCountry("Updated Country");
    addressRequest.setAddressType("HOME");
    testUserRequest.setAddress(addressRequest);

    Address existingAddress = new Address();
    existingAddress.setAddressId(1L);
    testUser.setAddressId(1L);

    setupUpdateUserMocks();
    stubAddressRepositoryFindById(1L, Optional.of(existingAddress));
    stubAddressRepositorySave(existingAddress);

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(addressRepository, times(1)).save(any(Address.class));
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify empty permissions throws BadRequestException. Expected Result:
   * BadRequestException with AtLeastOnePermissionRequired message. Assertions: assertThrows,
   * assertEquals
   */
  @Test
  @DisplayName("updateUser - Failure - Empty Permissions")
  void updateUser_emptyPermissions_throwsBadRequestException() {
    // Arrange
    testUserRequest.setPermissionIds(new ArrayList<>());
    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify max long ID throws NotFoundException when not found. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("updateUser - Failure - Max Long ID")
  void updateUser_maxLongId_throwsNotFoundException() {
    // Arrange
    testUserRequest.setUserId(Long.MAX_VALUE);
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative ID throws NotFoundException. Expected Result: NotFoundException with
   * InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("updateUser - Failure - Negative ID")
  void updateUser_negativeId_throwsNotFoundException() {
    // Arrange
    testUserRequest.setUserId(-1L);
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify null permissions throws BadRequestException. Expected Result:
   * BadRequestException with AtLeastOnePermissionRequired message. Assertions: assertThrows,
   * assertEquals
   */
  @Test
  @DisplayName("updateUser - Failure - Null Permissions")
  void updateUser_nullPermissions_throwsBadRequestException() {
    // Arrange
    testUserRequest.setPermissionIds(null);
    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify update of non-existent user throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals, verify
   */
  @Test
  @DisplayName("updateUser - Failure - User Not Found")
  void updateUser_userNotFound_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  /**
   * Purpose: Verify updateUser creates a new address when user has addressId but the address record
   * is missing. Expected Result: New address saved and user updated with new addressId. Assertions:
   * Address save and user save invocations.
   */
  @Test
  @DisplayName("updateUser - FailurePathCoverage - Missing Existing Address Creates New Address")
  void updateUser_missingExistingAddress_createsNewAddress() {
    // Arrange
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setStreetAddress("123 New St");
    addressRequest.setCity("New City");
    addressRequest.setState("NS");
    addressRequest.setPostalCode("400001");
    addressRequest.setCountry("India");
    addressRequest.setAddressType("HOME");
    testUserRequest.setAddress(addressRequest);
    testUser.setAddressId(1L);

    Address createdAddress = new Address();
    createdAddress.setAddressId(99L);

    setupUpdateUserMocks();
    stubAddressRepositoryFindById(1L, Optional.empty());
    stubAddressRepositorySave(createdAddress);

    // Act
    assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

    // Assert
    verify(addressRepository, times(1)).save(any(Address.class));
    verify(userRepository, atLeastOnce()).save(any(User.class));
  }

  /**
   * Purpose: Verify ImgBB profile-picture update fails when API key is missing. Expected Result:
   * BadRequestException with ImgBB configuration message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("updateUser - Failure - Imgbb Missing Api Key")
  void updateUser_imgbbMissingApiKey_throwsBadRequestException() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupUpdateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "imgbb");

    com.example.springapi.models.databasemodels.Client clientWithoutKey = createTestClient();
    clientWithoutKey.setImgbbApiKey(" ");
    stubClientRepositoryFindByIdAny(Optional.of(clientWithoutKey));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify ImgBB profile-picture update fails when upload returns null response. Expected
   * Result: BadRequestException with ER010 message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("updateUser - Failure - Imgbb Upload Returns Null")
  void updateUser_imgbbUploadReturnsNull_throwsBadRequestException() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupUpdateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "imgbb");
    stubClientRepositoryFindByIdAny(Optional.of(createTestClient()));
    stubClientServiceGetClientById(createTestClientResponse());

    if (mockedImgbbHelper != null) {
      mockedImgbbHelper.close();
    }
    mockedImgbbHelper =
        org.mockito.Mockito.mockConstruction(
            com.example.springapi.helpers.ImgbbHelper.class,
            (mock, context) ->
                when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.ER010, ex.getMessage());
  }

  /**
   * Purpose: Verify Firebase profile-picture update fails when Google credentials are missing.
   * Expected Result: BadRequestException with ER011 message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("updateUser - Failure - Firebase Missing GoogleCred")
  void updateUser_firebaseMissingGoogleCred_throwsBadRequestException() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupUpdateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "firebase");
    stubGoogleCredRepositoryFindByIdAny(Optional.empty());

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.ER011, ex.getMessage());
  }

  /**
   * Purpose: Verify Firebase profile-picture update fails when upload API returns false. Expected
   * Result: BadRequestException with ER010 message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("updateUser - Failure - Firebase Upload Returns False")
  void updateUser_firebaseUploadReturnsFalse_throwsBadRequestException() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupUpdateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "firebase");
    stubGoogleCredRepositoryFindByIdAny(Optional.of(new GoogleCred()));

    if (mockedFirebaseHelper != null) {
      mockedFirebaseHelper.close();
    }
    mockedFirebaseHelper =
        org.mockito.Mockito.mockConstruction(
            com.example.springapi.helpers.FirebaseHelper.class,
            (mock, context) -> {
              when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(false);
              doNothing().when(mock).deleteFile(anyString());
            });

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.ER010, ex.getMessage());
  }

  /**
   * Purpose: Verify invalid imageLocation setting is rejected during profile update. Expected
   * Result: BadRequestException with invalid configuration message. Assertions: Exception message
   * contains invalid imageLocation marker.
   */
  @Test
  @DisplayName("updateUser - Failure - Invalid ImageLocation")
  void updateUser_invalidImageLocation_throwsBadRequestException() {
    // Arrange
    setupUpdateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "unsupported-store");

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertTrue(ex.getMessage().contains("Invalid imageLocation configuration"));
  }

  /**
   * Purpose: Verify zero ID throws NotFoundException. Expected Result: NotFoundException with
   * InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("updateUser - Failure - Zero ID")
  void updateUser_zeroId_throwsNotFoundException() {
    // Arrange
    testUserRequest.setUserId(0L);
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.updateUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify controller handles unauthorized access via HTTP status. Expected Result: HTTP
   * UNAUTHORIZED status returned and @PreAuthorize verified. Assertions:
   * assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()), assertNotNull, assertTrue
   */
  @Test
  @DisplayName("updateUser - Controller permission forbidden")
  void updateUser_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    stubMockUserServiceUpdateUserThrowsUnauthorized(null);
    Method method =
        UserController.class.getMethod("updateUser", Long.class, UserRequestModel.class);

    // Act
    ResponseEntity<?> response =
        userControllerWithMock.updateUser(TEST_USER_ID, new UserRequestModel());
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(annotation, "updateUser method should have @PreAuthorize annotation");
    assertTrue(
        annotation.value().contains(Authorizations.UPDATE_USER_PERMISSION),
        "@PreAuthorize annotation should check for UPDATE_USER_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called.
   * Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("updateUser - Controller delegates to service")
  void updateUser_withValidRequest_delegatesToService() {
    // Arrange
    Long userId = 1L;
    UserRequestModel userRequest = new UserRequestModel();
    userRequest.setId(userId);
    stubMockUserServiceUpdateUser(userRequest);

    // Act
    ResponseEntity<?> response = userControllerWithMock.updateUser(userId, userRequest);

    // Assert
    verify(mockUserService, times(1)).updateUser(userRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
