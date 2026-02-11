package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.createUser method.
 * 
 * Total Tests: 17
 */
@DisplayName("UserService - CreateUser Tests")
class CreateUserTest extends UserServiceTestBase {
    // Total Tests: 17

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify user with many permissions is created successfully.
     * Expected Result: User is saved with all permissions.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - Many Permissions")
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
     * Purpose: Verify user with single permission is created successfully.
     * Expected Result: User is saved with one permission.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - Single Permission")
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
     * Purpose: Verify user client mapping is created.
     * Expected Result: UserClientMapping is saved.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - User Client Mapping")
    void createUser_success_createsUserClientMapping() {
        // Arrange
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userClientMappingRepository, times(1)).save(any(UserClientMapping.class));
    }

    /**
     * Purpose: Verify successful user creation with all required fields.
     * Expected Result: User is saved to repository.
     * Assertions: assertDoesNotThrow, verify
     */
    @Test
    @DisplayName("createUser - Success - User With Permissions")
    void createUser_success_createsUserWithPermissions() {
        // Arrange
        setupCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(() -> userService.createUser(testUserRequest));
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user creation logs the operation.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - Logs Operation")
    void createUser_success_logsOperation() {
        // Arrange
        setupCreateUserMocks();

        // Act
        userService.createUser(testUserRequest);

        // Assert
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify user creation with address.
     * Expected Result: Address is saved along with user.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - With Address")
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
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - With User Groups")
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
     * Purpose: Verify user without address is created successfully.
     * Expected Result: User is saved without address.
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - Without Address")
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
     * Assertions: verify
     */
    @Test
    @DisplayName("createUser - Success - Without User Groups")
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
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify checking for duplicate email during creation.
     * Expected Result: BadRequestException with email exists message.
     * Assertions: assertThrows, assertTrue, verify
     */
    @Test
    @DisplayName("createUser - Failure - Checks Duplicate Email")
    void createUser_checksDuplicateEmail_success() {
        // Arrange
        stubUserRepositoryFindByLoginNameAny(testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.InvalidEmail));
        assertTrue(ex.getMessage().contains("Login name (email) already exists"));
        verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
    }

    /**
     * Purpose: Verify duplicate email throws BadRequestException.
     * Expected Result: BadRequestException with email exists message.
     * Assertions: assertThrows, assertTrue, verify
     */
    @Test
    @DisplayName("createUser - Failure - Duplicate Email")
    void createUser_duplicateEmail_throwsBadRequestException() {
        // Arrange
        stubUserRepositoryFindByLoginNameAny(testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.InvalidEmail));
        assertTrue(ex.getMessage().contains("Login name (email) already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify duplicate login name throws BadRequestException.
     * Expected Result: BadRequestException with email exists message.
     * Assertions: assertThrows, assertTrue
     */
    @Test
    @DisplayName("createUser - Failure - Duplicate Login Name")
    void createUser_duplicateLoginName_throwsBadRequestException() {
        // Arrange
        stubUserRepositoryFindByLoginName(testUserRequest.getLoginName(), testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertTrue(ex.getMessage().contains(ErrorMessages.UserErrorMessages.InvalidEmail));
        assertTrue(ex.getMessage().contains("Login name (email) already exists"));
    }

    /**
     * Purpose: Verify empty permissions list throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUser - Failure - Empty Permissions")
    void createUser_emptyPermissions_throwsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(new ArrayList<>());
        setupCreateUserMocks();

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.createUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify null permissions throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("createUser - Failure - Null Permissions")
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

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode())
     */
    @Test
    @DisplayName("createUser - Controller permission forbidden")
    void createUser_controller_permission_forbidden() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = userControllerWithMock.createUser(new UserRequestModel());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with CREATE_USER_PERMISSION.
     * Assertions: assertNotNull, assertTrue
     */
    @Test
    @DisplayName("createUser - Verify @PreAuthorize Annotation")
    void createUser_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
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
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("createUser - Controller delegates to service")
    void createUser_withValidRequest_delegatesToService() {
        // Arrange
        UserRequestModel userRequest = new UserRequestModel();
        stubMockUserServiceCreateUser(userRequest);

        // Act
        ResponseEntity<?> response = userControllerWithMock.createUser(userRequest);

        // Assert
        verify(mockUserService, times(1)).createUser(userRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
