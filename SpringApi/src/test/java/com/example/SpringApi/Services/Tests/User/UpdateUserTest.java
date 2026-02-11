package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Total Tests: 14
@DisplayName("UserService - UpdateUser Tests")
class UpdateUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with UPDATE_USER_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("Update User - Controller permission forbidden - Success")
    void updateUser_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("updateUser", Long.class, UserRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "updateUser method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for UPDATE_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).updateUser(request);
     */
    @Test
    @DisplayName("Update User - Controller delegates to service")
    void updateUser_WithValidRequest_DelegatesToService() {
        // Arrange
        Long userId = 1L;
        UserRequestModel request = new UserRequestModel();
        request.setId(userId);

        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        doNothing().when(mockUserService).updateUser(request);

        // Act
        localController.updateUser(userId, request);

        // Assert
        verify(mockUserService, times(1)).updateUser(request);
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify single permission update is allowed.
     * Expected Result: User is updated with single permission.
     * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Update User - Single Permission - Success")
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
     * Purpose: Verify successful user update.
     * Expected Result: User is updated and saved.
     * Assertions: verify(userRepository).save(any(User.class));
     */
    @Test
    @DisplayName("Update User - Success - Updates user details")
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
     * Purpose: Verify user log is called after successful update.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Update User - Success - Logs the operation")
    void updateUser_success_logsOperation() {
        // Arrange
        setupUpdateUserMocks();

        // Act
        assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

        // Assert
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify permissions are updated.
     * Expected Result: Old permissions deleted, new ones saved.
     * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Update User - Updates permissions")
    void updateUser_updatesPermissions() {
        // Arrange
        testUserRequest.setPermissionIds(Arrays.asList(4L, 5L));
        setupUpdateUserMocks();

        // Act
        assertDoesNotThrow(() -> userService.updateUser(testUserRequest));

        // Assert
        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
    }

    /**
     * Purpose: Verify user groups are updated.
     * Expected Result: Old group mappings deleted, new ones saved.
     * Assertions: verify(userGroupUserMapRepository).saveAll(anyList());
     */
    @Test
    @DisplayName("Update User - Updates user groups")
    void updateUser_updatesUserGroups() {
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
     * Purpose: Verify address update is successful.
     * Expected Result: Address is updated.
     * Assertions: verify(addressRepository).save(any(Address.class));
     */
    @Test
    @DisplayName("Update User - With Address - Updates address")
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
     * Purpose: Verify creating new address when user had no address.
     * Expected Result: New address is created and linked to user.
     * Assertions: verify(addressRepository).save(any(Address.class));
     */
    @Test
    @DisplayName("Update User - Creates new address when none exists")
    void updateUser_createsNewAddressWhenNoneExists() {
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
     * Purpose: Verify empty permissions throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     */
    @Test
    @DisplayName("Update User - Empty Permissions - Throws BadRequestException")
    void updateUser_emptyPermissions_throwsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(new ArrayList<>());
        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify that loginName (email) from the request is preserved in
     * update.
     * Expected Result: User is successfully updated.
     */
    @Test
    @DisplayName("Update User - Login Name Change - Allowed")
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

        // Setup mocks for this specific request
        // Since setupUpdateUserMocks uses testUserRequest, we need to adapt or just set
        // mocks manually
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserClientPermissionMappingRepositoryFindByUserIdAndClientId(new ArrayList<>());
        stubUserClientPermissionMappingRepositorySaveAll(new ArrayList<>());
        stubUserGroupUserMapRepositoryFindByUserId(new ArrayList<>());
        stubGoogleCredRepositoryFindByIdAny(Optional.of(new GoogleCred()));
        stubClientRepositoryFindByIdAny(Optional.of(createTestClient()));
        stubClientServiceGetClientById(new ClientResponseModel());
        stubUserLogServiceLogData(true);
        stubEnvironmentActiveProfiles(new String[] { "test" });
        stubImgbbHelper();

        // Act
        assertDoesNotThrow(() -> userService.updateUser(changeEmailRequest));

        // Assert
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    /**
     * Purpose: Verify max long ID throws NotFoundException when not found.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Update User - Max Long ID - Throws NotFoundException")
    void updateUser_maxLongId_throwsNotFoundException() {
        // Arrange
        testUserRequest.setUserId(Long.MAX_VALUE);
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Update User - Negative ID - Throws NotFoundException")
    void updateUser_negativeId_throwsNotFoundException() {
        // Arrange
        testUserRequest.setUserId(-1L);
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify null permissions throws BadRequestException.
     * Expected Result: BadRequestException with permission required message.
     */
    @Test
    @DisplayName("Update User - Null Permissions - Throws BadRequestException")
    void updateUser_nullPermissions_throwsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(null);
        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify update of non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Update User - User Not Found - Throws NotFoundException")
    void updateUser_userNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("Update User - Zero ID - Throws NotFoundException")
    void updateUser_zeroId_throwsNotFoundException() {
        // Arrange
        testUserRequest.setUserId(0L);
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUser(testUserRequest));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    private void setupUpdateUserMocks() {
        stubUserRepositoryFindByIdWithAllRelations(testUser);
        stubUserRepositorySave(testUser);
        stubUserClientPermissionMappingRepositoryFindByUserIdAndClientId(new ArrayList<>());
        stubUserClientPermissionMappingRepositorySaveAll(new ArrayList<>());
        stubUserGroupUserMapRepositoryFindByUserId(new ArrayList<>());
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubGoogleCredRepositoryFindByIdAny(Optional.of(new GoogleCred()));
        stubClientRepositoryFindByIdAny(Optional.of(createTestClient()));
        stubClientServiceGetClientById(new ClientResponseModel());
        stubUserLogServiceLogData(true);
        stubEnvironmentActiveProfiles(new String[] { "test" });
        stubImgbbHelper();
    }

    // Helper stubs are provided by UserServiceTestBase.
}
