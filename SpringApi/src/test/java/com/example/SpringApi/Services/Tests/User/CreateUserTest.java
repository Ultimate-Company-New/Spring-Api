package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Services.UserService;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - CreateUser functionality.
 *
 * Test Summary:
 * | Test Group | Number of Tests |
 * | :------------------ | :-------------- |
 * | SUCCESS Tests | 7 |
 * | FAILURE Tests | 7 |
 * | **Total** | **14** |
 */
@DisplayName("UserService - CreateUser Tests")
class CreateUserTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("createUser - Verify @PreAuthorize Annotation")
    void createUser_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("createUser", UserRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on createUser method");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    @Test
    @DisplayName("createUser - Controller delegates to service")
    void createUser_WithValidRequest_DelegatesToService() {
        UserService mockUserService = mock(UserService.class);
        UserController controller = new UserController(mockUserService);
        UserRequestModel request = new UserRequestModel();
        doNothing().when(mockUserService).createUser(request);

        ResponseEntity<?> response = controller.createUser(request);

        verify(mockUserService).createUser(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Nested
    @DisplayName("SUCCESS Tests")
    class SuccessTests {

        // ========================================
        // SUCCESS TESTS
        // ========================================

        /**
         * Purpose: Verify successful user creation with all required fields.
         * Expected Result: User is saved to repository.
         * Assertions: assertDoesNotThrow();
         * verify(userRepository).save(any(User.class));
         */
        @Test
        @DisplayName("Create User - Success - Creates user with permissions")
        void createUser_Success() {
            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            lenient().when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            lenient().when(userRepository.save(any(User.class))).thenReturn(savedUser);
            lenient().when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            lenient()
                                    .when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(),
                                            anyString()))
                                    .thenReturn(true);
                        })) {

                    try (MockedConstruction<FirebaseHelper> firebaseHelperMock = mockConstruction(FirebaseHelper.class,
                            (mock, context) -> {
                                lenient().when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(true);
                            })) {

                        assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                        verify(userRepository, atLeastOnce()).save(any(User.class));
                        verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
                    }
                }
            }
        }

        /**
         * Purpose: Verify user creation with address.
         * Expected Result: Address is saved along with user.
         * Assertions: verify(addressRepository).save(any(Address.class));
         */
        @Test
        @DisplayName("Create User - With Address - Saves address")
        void createUser_WithAddress_SavesAddress() {
            AddressRequestModel addressRequest = new AddressRequestModel();
            addressRequest.setStreetAddress("123 Test St");
            addressRequest.setCity("Test City");
            addressRequest.setState("TS");
            addressRequest.setPostalCode("12345");
            addressRequest.setCountry("Test Country");
            addressRequest.setAddressType("HOME");
            testUserRequest.setAddress(addressRequest);

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            Address savedAddress = new Address();
            savedAddress.setAddressId(1L);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(addressRepository, times(1)).save(any(Address.class));
                }
            }
        }

        /**
         * Purpose: Verify user creation with user groups.
         * Expected Result: User group mappings are saved.
         * Assertions: verify(userGroupUserMapRepository).saveAll(anyList());
         */
        @Test
        @DisplayName("Create User - With User Groups - Saves group mappings")
        void createUser_WithUserGroups_SavesGroupMappings() {
            testUserRequest.setSelectedGroupIds(Arrays.asList(1L, 2L));

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userGroupUserMapRepository, times(1)).saveAll(anyList());
                }
            }
        }

        /**
         * Purpose: Verify user creation logs the operation.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Create User - Success - Logs the operation")
        void createUser_Success_LogsOperation() {
            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
                }
            }
        }

        /**
         * Purpose: Verify user client mapping is created.
         * Expected Result: UserClientMapping is saved.
         * Assertions:
         * verify(userClientMappingRepository).save(any(UserClientMapping.class));
         */
        @Test
        @DisplayName("Create User - Success - Creates user client mapping")
        void createUser_Success_CreatesUserClientMapping() {
            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userClientMappingRepository, times(1)).save(any(UserClientMapping.class));
                }
            }
        }

        /**
         * Purpose: Verify user with single permission is created successfully.
         * Expected Result: User is saved with one permission.
         * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
         */
        @Test
        @DisplayName("Create User - Single Permission - Success")
        void createUser_SinglePermission_Success() {
            testUserRequest.setPermissionIds(Arrays.asList(1L));

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
                }
            }
        }

        /**
         * Purpose: Verify user with many permissions is created successfully.
         * Expected Result: User is saved with all permissions.
         * Assertions: verify(userClientPermissionMappingRepository).saveAll(anyList());
         */
        @Test
        @DisplayName("Create User - Many Permissions - Success")
        void createUser_ManyPermissions_Success() {
            testUserRequest.setPermissionIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
                }
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
         * Purpose: Verify repository findByLoginName is called to check duplicates.
         * Expected Result: findByLoginName is called exactly once.
         * Assertions: verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
         */
        @Test
        @DisplayName("Create User - Checks for duplicate email")
        void createUser_ChecksDuplicateEmail() {
            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(testUser);

            assertThrows(BadRequestException.class, () -> userService.createUser(testUserRequest));

            verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
        }

        /**
         * Purpose: Verify duplicate email throws BadRequestException.
         * Expected Result: BadRequestException with email exists message.
         * Assertions: assertEquals(...InvalidEmail + " - Login name (email) already
         * exists", ex.getMessage());
         */
        @Test
        @DisplayName("Create User - Duplicate Email - Throws BadRequestException")
        void createUser_DuplicateEmail_ThrowsBadRequestException() {
            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(testUser);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.createUser(testUserRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail + " - Login name (email) already exists",
                    ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify empty permissions list throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions:
         * assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Create User - Empty Permissions - Throws BadRequestException")
        void createUser_EmptyPermissions_ThrowsBadRequestException() {
            testUserRequest.setPermissionIds(new ArrayList<>());
            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userService.createUser(testUserRequest));

                assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
            }
        }

        /**
         * Purpose: Verify empty user groups list is handled.
         * Expected Result: User is saved without group mappings.
         * Assertions: verify(userGroupUserMapRepository, never()).saveAll(anyList());
         */
        @Test
        @DisplayName("Create User - Empty User Groups - Success")
        void createUser_EmptyUserGroups_Success() {
            testUserRequest.setSelectedGroupIds(new ArrayList<>());

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userGroupUserMapRepository, never()).saveAll(anyList());
                }
            }
        }

        /**
         * Purpose: Verify null permissions throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions:
         * assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Create User - Null Permissions - Throws BadRequestException")
        void createUser_NullPermissions_ThrowsBadRequestException() {
            testUserRequest.setPermissionIds(null);
            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            lenient().when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            lenient().when(userRepository.save(any(User.class))).thenReturn(savedUser);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userService.createUser(testUserRequest));

                assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired, ex.getMessage());
            }
        }

        /**
         * Purpose: Verify user without address is created successfully.
         * Expected Result: User is saved without address.
         * Assertions: verify(addressRepository, never()).save(any(Address.class));
         */
        @Test
        @DisplayName("Create User - Without Address - Success")
        void createUser_WithoutAddress_Success() {
            testUserRequest.setAddress(null);

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(addressRepository, never()).save(any(Address.class));
                }
            }
        }

        /**
         * Purpose: Verify user without user groups is created successfully.
         * Expected Result: User is saved without group mappings.
         * Assertions: verify(userGroupUserMapRepository, never()).saveAll(anyList());
         */
        @Test
        @DisplayName("Create User - Without User Groups - Success")
        void createUser_WithoutUserGroups_Success() {
            testUserRequest.setSelectedGroupIds(null);

            User savedUser = new User(testUserRequest, CREATED_USER);
            savedUser.setUserId(TEST_USER_ID);

            when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenReturn(new UserClientMapping(TEST_USER_ID, TEST_CLIENT_ID, CREATED_USER, CREATED_USER));
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
            when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            clientResponse.setSendgridSenderName("Test Sender");
            clientResponse.setSendGridEmailAddress("sender@test.com");
            clientResponse.setSendGridApiKey("test-key");
            when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
                mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                        .thenReturn(new String[] { "salt123", "hashedPassword123" });
                mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");

                try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                        (mock, context) -> {
                            when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                                    .thenReturn(true);
                        })) {

                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userGroupUserMapRepository, never()).saveAll(anyList());
                }
            }
        }
    }
}
