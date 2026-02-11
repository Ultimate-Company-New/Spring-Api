package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.*;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Total Tests: 16
@DisplayName("UserService - CreateUser Tests")
class CreateUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with CREATE_USER_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("Create User - Controller permission forbidden - Success")
    void createUser_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("createUser", UserRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "createUser method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).createUser(request);
     */
    @Test
    @DisplayName("Create User - Controller delegates to service")
    void createUser_WithValidRequest_DelegatesToService() {
        // Arrange
        UserRequestModel request = new UserRequestModel();
        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        doNothing().when(mockUserService).createUser(request);

        // Act
        ResponseEntity<?> response = localController.createUser(request);

        // Assert
        verify(mockUserService, times(1)).createUser(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify successful user creation with all required fields.
     * Expected Result: User is saved to repository.
     * Assertions: assertDoesNotThrow(); verify repositories.
     */
    @Test
    @DisplayName("Create User - Success - Creates user with permissions")
    void createUser_success_createsUserWithPermissions() {
        // Arrange
        setupCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(() -> userService.createUser(testUserRequest));
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user creation with address.
     * Expected Result: Address is saved along with user.
     * Assertions: verify(addressRepository).save(any(Address.class));
     */
    @Test
    @DisplayName("Create User - With Address - Saves address")
    void createUser_withAddress_savesAddress() {
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
     * Purpose: Verify user creation with user groups.
     * Expected Result: User group mappings are saved.
     * Assertions: verify(userGroupUserMapRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Create User - With User Groups - Saves group mappings")
    void createUser_withUserGroups_savesGroupMappings() {
        // Arrange
        testUserRequest.setSelectedGroupIds(Arrays.asList(1L, 2L));
        setupCreateUserMocks();
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user creation logs the operation.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Create User - Success - Logs the operation")
    void createUser_success_logsOperation() {
        // Arrange
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify user client mapping is created.
     * Expected Result: UserClientMapping is saved.
     * Assertions:
     * verify(userClientMappingRepository).save(any(UserClientMapping.class));
     */
    @Test
    @DisplayName("Create User - Success - Creates user client mapping")
    void createUser_success_createsUserClientMapping() {
        // Arrange
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userClientMappingRepository, times(1)).save(any(UserClientMapping.class));
    }

    /**
     * Purpose: Verify user with single permission is created successfully.
     * Expected Result: User is saved with one permission.
     * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Create User - Single Permission - Success")
    void createUser_singlePermission_success() {
        // Arrange
        testUserRequest.setPermissionIds(Collections.singletonList(1L));
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user with many permissions is created successfully.
     * Expected Result: User is saved with all permissions.
     * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Create User - Many Permissions - Success")
    void createUser_manyPermissions_success() {
        // Arrange
        testUserRequest.setPermissionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user without address is created successfully.
     * Expected Result: User is saved without address.
     * Assertions: verify(addressRepository, never()).save(any(Address.class));
     */
    @Test
    @DisplayName("Create User - Without Address - Success")
    void createUser_withoutAddress_success() {
        // Arrange
        testUserRequest.setAddress(null);
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(addressRepository, never()).save(any(Address.class));
    }

    /**
     * Purpose: Verify user without user groups is created successfully.
     * Expected Result: User is saved without group mappings.
     * Assertions: verify(userGroupUserMapRepository, never()).saveAll(anyList());
     */
    @Test
    @DisplayName("Create User - Without User Groups - Success")
    void createUser_withoutUserGroups_success() {
        // Arrange
        testUserRequest.setSelectedGroupIds(null);
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userGroupUserMapRepository, never()).saveAll(anyList());
    }

    // ========================================
    // DUPLICATE CHECK TESTS
    // ========================================

    @Test
    @DisplayName("createUser - Duplicate Login Name - Throws BadRequestException")
    void createUser_duplicateLoginName_throwsBadRequestException() {
        // Arrange
        stubUserRepositoryFindByLoginName(testUserRequest.getLoginName(), testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertTrue(ex.getMessage().contains("Login name (email) already exists"));
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify repository findByLoginName is called to check duplicates.
     * Expected Result: findByLoginName is called exactly once.
     * Assertions: verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
     */
    @Test
    @DisplayName("Create User - Checks for duplicate email")
    void createUser_checksDuplicateEmail() {
        // Arrange
        stubUserRepositoryFindByLoginNameAny(testUser); // Returns existing user

        // Act & Assert
        assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));
        verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
    }

    /**
     * Purpose: Verify duplicate email throws BadRequestException.
     * Expected Result: BadRequestException with email exists message.
     * Assertions: exception message contains "Login name (email) already exists".
     */
    @Test
    @DisplayName("Create User - Duplicate Email - Throws BadRequestException")
    void createUser_duplicateEmail_throwsBadRequestException() {
        // Arrange
        stubUserRepositoryFindByLoginNameAny(testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertTrue(ex.getMessage().contains("Login name (email) already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify empty permissions list throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     */
    @Test
    @DisplayName("Create User - Empty Permissions - Throws BadRequestException")
    void createUser_emptyPermissions_throwsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(new ArrayList<>());
        setupCreateUserMocks(); // Needed for password helper stubs even if fail

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify null permissions throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     */
    @Test
    @DisplayName("Create User - Null Permissions - Throws BadRequestException")
    void createUser_nullPermissions_throwsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(null);
        setupCreateUserMocks();

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
    }

    private void setupCreateUserMocks() {
        User savedUser = new User(testUserRequest, CREATED_USER);
        savedUser.setUserId(TEST_USER_ID);

        stubUserRepositoryFindByLoginNameAny(null); // No duplicate by default
        savedUser.setToken("token123");
        savedUser.setSalt("salt123");
        savedUser.setPassword("hashedPassword123");
        stubUserRepositorySave(savedUser);
        stubUserClientPermissionMappingRepositorySaveAll(new ArrayList<>());
        stubUserClientMappingRepositorySave(null);
        stubGoogleCredRepositoryFindByIdAny(Optional.of(new GoogleCred()));
        stubClientRepositoryFindByIdAny(Optional.of(new Client()));
        stubClientServiceGetClientById(new ClientResponseModel());
        stubUserLogServiceLogData(true);
        stubPasswordHelper();
        stubEmailTemplates();
        stubFirebaseHelper();
    }
}
