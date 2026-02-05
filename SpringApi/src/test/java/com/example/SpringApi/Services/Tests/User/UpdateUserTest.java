package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - UpdateUser functionality.
 *
 * Test Summary:
 * | Test Group          | Number of Tests |
 * | :------------------ | :-------------- |
 * | SUCCESS Tests       | 6               |
 * | FAILURE Tests       | 8               |
 * | **Total**           | **14**          |
 */
@DisplayName("UserService - UpdateUser Tests")
class UpdateUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("updateUser - Verify @PreAuthorize Annotation")
    void updateUser_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("updateUser", Long.class, UserRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updateUser method");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for UPDATE_USER_PERMISSION");
    }

    @Test
    @DisplayName("updateUser - Controller delegates to service")
    void updateUser_WithValidRequest_DelegatesToService() {
        UserController controller = new UserController(userService);
        Long userId = 1L;
        UserRequestModel request = new UserRequestModel();
        request.setId(userId);
        doNothing().when(userService).updateUser(request);

        ResponseEntity<?> response = controller.updateUser(userId, request);

        verify(userService).updateUser(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Nested
    @DisplayName("SUCCESS Tests")
    class SuccessTests {

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
        void updateUser_SinglePermission_Success() {
            testUserRequest.setPermissionIds(Arrays.asList(1L));

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
            }
        }

        /**
         * Purpose: Verify successful user update.
         * Expected Result: User is updated and saved.
         * Assertions: assertDoesNotThrow(); verify(userRepository).save(any(User.class));
         */
        @Test
        @DisplayName("Update User - Success - Updates user details")
        void updateUser_Success() {
            testUserRequest.setFirstName("Updated");
            testUserRequest.setLastName("Name");

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(userRepository, times(2)).save(any(User.class));
            }
        }

        /**
         * Purpose: Verify user log is called after successful update.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Update User - Success - Logs the operation")
        void updateUser_Success_LogsOperation() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
            }
        }

        /**
         * Purpose: Verify permissions are updated.
         * Expected Result: Old permissions deleted, new ones saved.
         * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
         */
        @Test
        @DisplayName("Update User - Updates permissions")
        void updateUser_UpdatesPermissions() {
            testUserRequest.setPermissionIds(Arrays.asList(4L, 5L));

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
            }
        }

        /**
         * Purpose: Verify user groups are updated.
         * Expected Result: Old group mappings deleted, new ones saved.
         * Assertions: verify(userGroupUserMapRepository).saveAll(anyList());
         */
        @Test
        @DisplayName("Update User - Updates user groups")
        void updateUser_UpdatesUserGroups() {
            testUserRequest.setSelectedGroupIds(Arrays.asList(3L, 4L));

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
            }
        }

        /**
         * Purpose: Verify address update is successful.
         * Expected Result: Address is updated.
         * Assertions: verify(addressRepository).save(any(Address.class));
         */
        @Test
        @DisplayName("Update User - With Address - Updates address")
        void updateUser_WithAddress_UpdatesAddress() {
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

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(addressRepository.findById(1L)).thenReturn(Optional.of(existingAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(existingAddress);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client testClient = new Client();
            testClient.setClientId(TEST_CLIENT_ID);
            testClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(addressRepository, times(1)).save(any(Address.class));
            }
        }
    }

    @Nested
    @DisplayName("FAILURE Tests")
    class FailureTests {

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
        void updateUser_CreatesNewAddress() {
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

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(addressRepository.save(any(Address.class))).thenReturn(newAddress);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
                verify(addressRepository, times(1)).save(any(Address.class));
            }
        }

        /**
         * Purpose: Verify empty permissions throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - Empty Permissions - Throws BadRequestException")
        void updateUser_EmptyPermissions_ThrowsBadRequestException() {
            testUserRequest.setPermissionIds(new ArrayList<>());

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
        }

        /**
         * Purpose: Verify that loginName (email) from the request is preserved in update.
         * Expected Result: User is successfully updated (loginName/email change is allowed in current implementation).
         * Assertions: assertDoesNotThrow();
         * Note: The current service implementation does not prevent email changes.
         */
        @Test
        @DisplayName("Update User - Login Name Change - Allowed in current implementation")
        void updateUser_LoginNameChange_Allowed() {
            // Create a fresh user request with different login name
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

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client updateClient = new Client();
            updateClient.setClientId(TEST_CLIENT_ID);
            updateClient.setImgbbApiKey("test-imgbb-api-key");
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        lenient().when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                assertDoesNotThrow(() -> userService.updateUser(changeEmailRequest));
                verify(userRepository, atLeastOnce()).save(any(User.class));
            }
        }

        /**
         * Purpose: Verify max long ID throws NotFoundException when not found.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - Max Long ID - Throws NotFoundException")
        void updateUser_MaxLongId_ThrowsNotFoundException() {
            testUserRequest.setUserId(Long.MAX_VALUE);
            when(userRepository.findByIdWithAllRelations(eq(Long.MAX_VALUE), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify negative ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - Negative ID - Throws NotFoundException")
        void updateUser_NegativeId_ThrowsNotFoundException() {
            testUserRequest.setUserId(-1L);
            when(userRepository.findByIdWithAllRelations(eq(-1L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null permissions throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - Null Permissions - Throws BadRequestException")
        void updateUser_NullPermissions_ThrowsBadRequestException() {
            testUserRequest.setPermissionIds(null);

            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
        }

        /**
         * Purpose: Verify update of non-existent user throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - User Not Found - Throws NotFoundException")
        void updateUser_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify zero ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Update User - Zero ID - Throws NotFoundException")
        void updateUser_ZeroId_ThrowsNotFoundException() {
            testUserRequest.setUserId(0L);
            when(userRepository.findByIdWithAllRelations(eq(0L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.updateUser(testUserRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }
    }
}
