package com.example.SpringApi.ServiceTests.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserService.createUser method. */
@DisplayName("UserService - CreateUser Tests")
class CreateUserTest extends UserServiceTestBase {

  // Total Tests: 23
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify user with many permissions is created successfully. Expected Result: User is
   * saved with all permissions. Assertions: verify
   */
  @Test
  @DisplayName("Create User - Many Permissions - Success")
  void createUser_ManyPermissions_Success() {
    // Arrange
    testUserRequest.setPermissionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify user with single permission is created successfully. Expected Result: User is
   * saved with one permission. Assertions: verify
   */
  @Test
  @DisplayName("Create User - Single Permission - Success")
  void createUser_SinglePermission_Success() {
    // Arrange
    testUserRequest.setPermissionIds(Collections.singletonList(1L));
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify user client mapping is created. Expected Result: UserClientMapping is saved.
   * Assertions: verify
   */
  @Test
  @DisplayName("Create User - User Client Mapping - Success")
  void createUser_Success_CreatesUserClientMapping() {
    // Arrange
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userClientMappingRepository, times(1)).save(any(UserClientMapping.class));
  }

  /**
   * Purpose: Verify successful user creation with all required fields. Expected Result: User is
   * saved to repository. Assertions: assertDoesNotThrow, verify
   */
  @Test
  @DisplayName("Create User - User With Permissions - Success")
  void createUser_Success_CreatesUserWithPermissions() {
    // Arrange
    setupCreateUserMocks();

    // Act & Assert
    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
    verify(userRepository, atLeastOnce()).save(any(User.class));
    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
  }

  /**
   * Purpose: Verify user creation logs the operation. Expected Result: userLogService.logData is
   * called. Assertions: verify
   */
  @Test
  @DisplayName("Create User - Logs Operation - Success")
  void createUser_Success_LogsOperation() {
    // Arrange
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify user creation with address. Expected Result: Address is saved along with user.
   * Assertions: verify
   */
  @Test
  @DisplayName("Create User - With Address - Success")
  void createUser_WithAddress_SavesAddress() {
    // Arrange
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setStreetAddress("123 Test St");
    addressRequest.setCity("Test City");
    addressRequest.setState("TS");
    addressRequest.setPostalCode("12345");
    addressRequest.setCountry("Test Country");
    addressRequest.setAddressType("HOME");
    testUserRequest.setAddress(addressRequest);

    setupCreateUserMocks();
    stubAddressRepositorySave(new Address());

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(addressRepository, times(1)).save(any(Address.class));
  }

  /**
   * Purpose: Verify user without address is created successfully. Expected Result: User is saved
   * and address repository is not called. Assertions: verify
   */
  @Test
  @DisplayName("Create User - Without Address - Success")
  void createUser_WithoutAddress_Success() {
    // Arrange
    testUserRequest.setAddress(null);
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(addressRepository, never()).save(any(Address.class));
  }

  /**
   * Purpose: Verify user without user groups is created successfully. Expected Result: User is
   * saved without group mappings. Assertions: verify
   */
  @Test
  @DisplayName("Create User - Without User Groups - Success")
  void createUser_WithoutUserGroups_Success() {
    // Arrange
    testUserRequest.setSelectedGroupIds(null);
    setupCreateUserMocks();

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userGroupUserMapRepository, never()).saveAll(anyList());
  }

  /**
   * Purpose: Verify user creation with user groups. Expected Result: User group mappings are saved.
   * Assertions: verify
   */
  @Test
  @DisplayName("Create User - With User Groups - Success")
  void createUser_WithUserGroups_SavesGroupMappings() {
    // Arrange
    testUserRequest.setSelectedGroupIds(Arrays.asList(1L, 2L));
    setupCreateUserMocks();
    stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());

    // Act
    userService.createUser(testUserRequest);

    // Assert
    verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
  }

  /*
   **********************************************************************************************
   * FAILURE TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify checking for duplicate email during creation. Expected Result:
   * BadRequestException with email exists message. Assertions: assertThrows, assertTrue, verify
   */
  @Test
  @DisplayName("Create User - Checks Duplicate Email - Failure")
  void createUser_ChecksDuplicateEmail_Failure() {
    // Arrange
    stubUserRepositoryFindByLoginNameAny(testUser);

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.INVALID_EMAIL));
    assertTrue(ex.getMessage().contains("Login name (email) already exists"));
    verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
  }

  /**
   * Purpose: Verify duplicate email throws BadRequestException. Expected Result:
   * BadRequestException with email exists message. Assertions: assertThrows, assertTrue, verify
   */
  @Test
  @DisplayName("Create User - Duplicate Email - ThrowsBadRequestException")
  void createUser_DuplicateEmail_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginNameAny(testUser);

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.INVALID_EMAIL));
    assertTrue(ex.getMessage().contains("Login name (email) already exists"));
    verify(userRepository, never()).save(any(User.class));
  }

  /**
   * Purpose: Verify duplicate login name throws BadRequestException. Expected Result:
   * BadRequestException with email exists message. Assertions: assertThrows, assertTrue
   */
  @Test
  @DisplayName("Create User - Duplicate Login Name - ThrowsBadRequestException")
  void createUser_DuplicateLoginName_ThrowsBadRequestException() {
    // Arrange
    stubUserRepositoryFindByLoginName(testUserRequest.getLoginName(), testUser);

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.INVALID_EMAIL));
    assertTrue(ex.getMessage().contains("Login name (email) already exists"));
  }

  /**
   * Purpose: Verify empty permissions list throws BadRequestException. Expected Result:
   * BadRequestException with permission required message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("Create User - Empty Permissions - ThrowsBadRequestException")
  void createUser_EmptyPermissions_ThrowsBadRequestException() {
    // Arrange
    testUserRequest.setPermissionIds(new ArrayList<>());
    setupCreateUserMocks();

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify null permissions throws BadRequestException. Expected Result:
   * BadRequestException with permission required message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("Create User - Null Permissions - ThrowsBadRequestException")
  void createUser_NullPermissions_ThrowsBadRequestException() {
    // Arrange
    testUserRequest.setPermissionIds(null);
    setupCreateUserMocks();

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED, ex.getMessage());
  }

  /**
   * Purpose: Verify ImgBB flow fails when API key is missing for profile picture upload. Expected
   * Result: BadRequestException with configuration message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("Create User - Imgbb Missing Api Key For Profile Picture - Failure")
  void createUser_ImgbbMissingApiKeyForProfilePicture_Failure() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupCreateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "imgbb");

    com.example.SpringApi.Models.DatabaseModels.Client clientWithoutKey = createTestClient();
    clientWithoutKey.setImgbbApiKey(" ");
    stubClientRepositoryFindByIdAny(Optional.of(clientWithoutKey));

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals(
        ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED,
        exception.getMessage());
  }

  /**
   * Purpose: Verify ImgBB flow fails when upload response is null. Expected Result:
   * BadRequestException with ER010 message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("Create User - Imgbb Upload Failure Throws Er010 - Failure")
  void createUser_ImgbbUploadFailureThrowsEr010_Failure() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupCreateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "imgbb");
    stubClientRepositoryFindByIdAny(Optional.of(createTestClient()));
    stubClientServiceGetClientById(createTestClientResponse());

    if (mockedImgbbHelper != null) {
      mockedImgbbHelper.close();
    }
    mockedImgbbHelper =
        org.mockito.Mockito.mockConstruction(
            com.example.SpringApi.Helpers.ImgbbHelper.class,
            (mock, context) ->
                when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null));

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.ER010, exception.getMessage());
  }

  /**
   * Purpose: Verify Firebase flow fails when Google credentials are missing. Expected Result:
   * BadRequestException with ER011 message. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("Create User - Firebase Missing GoogleCred Throws Er011 - Failure")
  void createUser_FirebaseMissingGoogleCredThrowsEr011_Failure() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupCreateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "firebase");
    stubGoogleCredRepositoryFindByIdAny(Optional.empty());

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.ER011, exception.getMessage());
  }

  /**
   * Purpose: Verify invalid imageLocation setting is rejected. Expected Result: BadRequestException
   * with invalid configuration message. Assertions: Exception message contains invalid
   * imageLocation marker.
   */
  @Test
  @DisplayName("Create User - Invalid ImageLocation Configuration - Failure")
  void createUser_InvalidImageLocationConfiguration_Failure() {
    // Arrange
    testUserRequest.setProfilePictureBase64("base64-image");
    setupCreateUserMocks();
    org.springframework.test.util.ReflectionTestUtils.setField(
        userService, "imageLocation", "invalid-store");

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertTrue(exception.getMessage().contains("Invalid imageLocation configuration"));
  }

  /**
   * Purpose: Verify createUser wraps false confirmation email response into BadRequestException.
   * Expected Result: BadRequestException with failed confirmation message. Assertions: Exception
   * message contains failed confirmation constant.
   */
  @Test
  @DisplayName("Create User - Confirmation Email False Throws - Failure")
  void createUser_ConfirmationEmailFalseThrows_Failure() {
    // Arrange
    setupCreateUserMocks();

    if (mockedEmailTemplates != null) {
      mockedEmailTemplates.close();
    }
    mockedEmailTemplates =
        org.mockito.Mockito.mockConstruction(
            com.example.SpringApi.Helpers.EmailTemplates.class,
            (mock, context) ->
                when(mock.sendNewUserAccountConfirmation(
                        anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(false));

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertTrue(
        exception
            .getMessage()
            .contains(ErrorMessages.CommonErrorMessages.FAILED_TO_SEND_CONFIRMATION_EMAIL));
  }

  /**
   * Purpose: Verify createUser wraps thrown confirmation email errors. Expected Result:
   * BadRequestException with wrapped email exception reason. Assertions: Exact exception message.
   */
  @Test
  @DisplayName("Create User - Confirmation Email Exception Throws - Failure")
  void createUser_ConfirmationEmailExceptionThrows_Failure() {
    // Arrange
    setupCreateUserMocks();

    if (mockedEmailTemplates != null) {
      mockedEmailTemplates.close();
    }
    mockedEmailTemplates =
        org.mockito.Mockito.mockConstruction(
            com.example.SpringApi.Helpers.EmailTemplates.class,
            (mock, context) ->
                when(mock.sendNewUserAccountConfirmation(
                        anyLong(), anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("smtp-down")));

    // Act
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

    // Assert
    assertEquals("Failed to send confirmation email: smtp-down", exception.getMessage());
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called.
   * Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("Create User - Controller delegates to service")
  void createUser_p01_WithValidRequest_DelegatesToService() {
    // Arrange
    UserRequestModel userRequest = new UserRequestModel();
    stubMockUserServiceCreateUser(userRequest);

    // Act
    ResponseEntity<?> response = userControllerWithMock.createUser(userRequest);

    // Assert
    verify(mockUserService, times(1)).createUser(userRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller handles unauthorized access via HTTP status. Expected Result: HTTP
   * UNAUTHORIZED status returned and @PreAuthorize verified. Assertions:
   * assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()), assertNotNull, assertTrue
   */
  @Test
  @DisplayName("Create User - Controller permission unauthorized")
  void createUser_p02_controller_permission_unauthorized() throws NoSuchMethodException {
    // Arrange
    stubMockUserServiceCreateUserThrowsUnauthorized(null);
    Method method = UserController.class.getMethod("createUser", UserRequestModel.class);

    // Act
    ResponseEntity<?> response = userControllerWithMock.createUser(new UserRequestModel());
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(annotation, "createUser method should have @PreAuthorize annotation");
    assertTrue(
        annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
        "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
  }

  /**
   * Purpose: Verify controller handles forbidden access via HTTP status. Expected Result: HTTP
   * FORBIDDEN status returned. Assertions: assertEquals(HttpStatus.FORBIDDEN,
   * response.getStatusCode())
   */
  @Test
  @DisplayName("Create User - Controller permission forbidden")
  void createUser_p03_controller_permission_forbidden() {
    // Arrange
    stubMockUserServiceCreateUserThrowsForbidden(null);

    // Act
    ResponseEntity<?> response = userControllerWithMock.createUser(new UserRequestModel());

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
