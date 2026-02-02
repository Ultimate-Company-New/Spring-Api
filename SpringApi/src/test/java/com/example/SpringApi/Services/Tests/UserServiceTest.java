package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import java.util.ArrayList;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.FilterQueryBuilder.UserFilterQueryBuilder;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.UserService;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * Test Group Summary:
 * | Group Name                    | Number of Tests |
 * | :---------------------------- | :-------------- |
 * | ToggleUserTests               | 9               |
 * | GetUserByIdTests              | 8               |
 * | GetUserByEmailTests           | 9               |
 * | CreateUserTests               | 14              |
 * | UpdateUserTests               | 14              |
 * | FetchUsersInBatchesTests      | 1               |
 * | BulkCreateUsersTests          | 8               |
 * | ConfirmEmailTests             | 11              |
 * | **Total**                     | **74**          |
 * 
 * This test class provides comprehensive coverage of UserService methods
 * including:
 * - CRUD operations (create, read, update, toggle)
 * - User retrieval by ID and email
 * - Permission management
 * - Address management
 * - Profile picture handling
 * - Pagination and filtering with nested loop combinations
 * - Error handling and validation
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserClientPermissionMappingRepository userClientPermissionMappingRepository;

    @Mock
    private UserGroupUserMapRepository userGroupUserMapRepository;

    @Mock
    private UserClientMappingRepository userClientMappingRepository;

    @Mock
    private GoogleCredRepository googleCredRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Environment environment;

    @Mock
    private UserLogService userLogService;

    @Mock
    private ClientService clientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserFilterQueryBuilder userFilterQueryBuilder;

    @Mock
    private FirebaseHelper firebaseHelper;

    @Mock
    private EmailHelper emailHelper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestModel testUserRequest;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_LOGIN_NAME = "test@example.com"; // loginName is the email
    private static final String CREATED_USER = "admin";

    // Valid string operators (matching FilterCondition.isValidOperator)
    private static final String[] STRING_OPERATORS = { "equals", "contains", "startsWith", "endsWith" };

    // Valid number operators
    private static final String[] NUMBER_OPERATORS = { "equals", ">", ">=", "<", "<=" };

    // Valid boolean operators
    private static final String[] BOOLEAN_OPERATORS = { "is" };

    // Invalid operators
    private static final String[] INVALID_OPERATORS = { "invalid", "xyz", "!@#", "", null };

    // Invalid columns
    private static final String[] INVALID_COLUMNS = { "invalidColumn", "xyz", "!@#$", "", "nonExistent" };

    @BeforeEach
    void setUp() {
        testUserRequest = new UserRequestModel();
        testUserRequest.setUserId(TEST_USER_ID);
        testUserRequest.setLoginName(TEST_LOGIN_NAME);
        testUserRequest.setFirstName("Test");
        testUserRequest.setLastName("User");
        testUserRequest.setPhone("1234567890");
        testUserRequest.setRole("Admin");
        testUserRequest.setDob(LocalDate.of(1990, 1, 1));
        testUserRequest.setPermissionIds(Arrays.asList(1L, 2L, 3L));
        testUserRequest.setPassword("hashed-password");
        testUserRequest.setSalt("test-salt");
        testUserRequest.setApiKey("test-api-key");
        testUserRequest.setToken("test-token");
        testUserRequest.setIsDeleted(false);
        testUserRequest.setLocked(false);
        testUserRequest.setEmailConfirmed(true);
        testUserRequest.setIsGuest(false);
        testUserRequest.setLoginAttempts(5);

        testUser = new User(testUserRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);

        Set<UserClientPermissionMapping> permissionMappings = new HashSet<>();
        Permission permission1 = new Permission();
        permission1.setPermissionId(1L);
        permission1.setPermissionName("View Users");
        permission1.setPermissionCode("VIEW_USERS");
        permission1.setDescription("Permission to view users");
        permission1.setCategory("User Management");

        Permission permission2 = new Permission();
        permission2.setPermissionId(2L);
        permission2.setPermissionName("Edit Users");
        permission2.setPermissionCode("EDIT_USERS");
        permission2.setDescription("Permission to edit users");
        permission2.setCategory("User Management");

        UserClientPermissionMapping mapping1 = new UserClientPermissionMapping();
        mapping1.setUserId(TEST_USER_ID);
        mapping1.setClientId(1L);
        mapping1.setPermissionId(1L);
        mapping1.setPermission(permission1);

        UserClientPermissionMapping mapping2 = new UserClientPermissionMapping();
        mapping2.setUserId(TEST_USER_ID);
        mapping2.setClientId(1L);
        mapping2.setPermissionId(2L);
        mapping2.setPermission(permission2);

        permissionMappings.add(mapping1);
        permissionMappings.add(mapping2);

        testUser.setUserClientPermissionMappings(permissionMappings);
        testUser.setUserGroupMappings(new HashSet<>());

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "localhost" });
        lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");

        try {
            java.lang.reflect.Field envField = UserService.class.getDeclaredField("environment");
            envField.setAccessible(true);
            envField.set(userService, environment);
        } catch (Exception e) {
            // Ignore reflection exceptions in test
        }

        ReflectionTestUtils.setField(userService, "imageLocation", "imgbb");
    }

    @Nested
    @DisplayName("Toggle User Tests")
    class ToggleUserTests {

        /**
         * Purpose: Verify that a user can be successfully toggled (soft deleted).
         * Expected Result: The user's isDeleted status is set to true.
         * Assertions: assertTrue(testUser.getIsDeleted());
         */
        @Test
        @DisplayName("Toggle User - Success - Should set isDeleted to true")
        void toggleUser_Success_SetsIsDeletedTrue() {
            testUser.setIsDeleted(false);
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.toggleUser(TEST_USER_ID);

            assertTrue(testUser.getIsDeleted());
            verify(userRepository, times(1)).save(testUser);
        }

        /**
         * Purpose: Verify that a deleted user can be restored.
         * Expected Result: The user's isDeleted status is set to false.
         * Assertions: assertFalse(testUser.getIsDeleted());
         */
        @Test
        @DisplayName("Toggle User - Success - Should restore deleted user")
        void toggleUser_Success_RestoresDeletedUser() {
            testUser.setIsDeleted(true);
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.toggleUser(TEST_USER_ID);

            assertFalse(testUser.getIsDeleted());
            verify(userRepository, times(1)).save(testUser);
        }

        /**
         * Purpose: Verify that toggling a non-existent user throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Toggle User - User Not Found - Throws NotFoundException")
        void toggleUser_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.toggleUser(TEST_USER_ID));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify that toggling user with negative ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Toggle User - Negative ID - Throws NotFoundException")
        void toggleUser_NegativeId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(-1L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.toggleUser(-1L));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that toggling user with zero ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Toggle User - Zero ID - Throws NotFoundException")
        void toggleUser_ZeroId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(0L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.toggleUser(0L));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that toggling user with max long ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Toggle User - Max Long ID - Throws NotFoundException")
        void toggleUser_MaxLongId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(Long.MAX_VALUE), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.toggleUser(Long.MAX_VALUE));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that multiple toggles persist state correctly.
         * Expected Result: User state toggles between deleted and active.
         * Assertions: State alternates correctly.
         */
        @Test
        @DisplayName("Toggle User - Multiple Toggles - State Persists")
        void toggleUser_MultipleToggles_StatePersists() {
            testUser.setIsDeleted(false);
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userService.toggleUser(TEST_USER_ID);
            assertTrue(testUser.getIsDeleted());

            userService.toggleUser(TEST_USER_ID);
            assertFalse(testUser.getIsDeleted());

            userService.toggleUser(TEST_USER_ID);
            assertTrue(testUser.getIsDeleted());
        }

        /**
         * Purpose: Verify that modifiedUser is updated on toggle.
         * Expected Result: modifiedUser field is updated.
         * Assertions: verify setModifiedUser is called.
         */
        @Test
        @DisplayName("Toggle User - Success - Updates modifiedUser")
        void toggleUser_Success_UpdatesModifiedUser() {
            testUser.setIsDeleted(false);
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.toggleUser(TEST_USER_ID);

            assertNotNull(testUser.getModifiedUser());
            verify(userRepository, times(1)).save(testUser);
        }

        /**
         * Purpose: Verify that user log is called after successful toggle.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Toggle User - Success - Logs the operation")
        void toggleUser_Success_LogsOperation() {
            testUser.setIsDeleted(false);
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            userService.toggleUser(TEST_USER_ID);

            verify(userLogService, times(1)).logData(anyLong(), contains("deletion status"), anyString());
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        /**
         * Purpose: Verify successful user retrieval by ID.
         * Expected Result: UserResponseModel is returned with correct data.
         * Assertions: assertNotNull(result); assertEquals(TEST_USER_ID,
         * result.getUserId());
         */
        @Test
        @DisplayName("Get User By ID - Success - Returns user with all details")
        void getUserById_Success_ReturnsUserWithDetails() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserById(TEST_USER_ID);

            assertNotNull(result);
            assertEquals(TEST_USER_ID, result.getUserId());
            assertEquals(TEST_EMAIL, result.getEmail());
            assertEquals(TEST_LOGIN_NAME, result.getLoginName());
        }

        /**
         * Purpose: Verify user permissions are returned.
         * Expected Result: Permissions list is populated.
         * Assertions: assertNotNull(result.getPermissions());
         * assertEquals(2, result.getPermissions().size());
         */
        @Test
        @DisplayName("Get User By ID - Success - Returns permissions")
        void getUserById_Success_ReturnsPermissions() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserById(TEST_USER_ID);

            assertNotNull(result.getPermissions());
            assertEquals(2, result.getPermissions().size());
        }

        /**
         * Purpose: Verify that non-existent user throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By ID - User Not Found - Throws NotFoundException")
        void getUserById_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(TEST_USER_ID));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify negative ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By ID - Negative ID - Throws NotFoundException")
        void getUserById_NegativeId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(-1L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(-1L));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify zero ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By ID - Zero ID - Throws NotFoundException")
        void getUserById_ZeroId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(0L), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(0L));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify max long ID throws NotFoundException when not found.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By ID - Max Long ID - Throws NotFoundException")
        void getUserById_MaxLongId_ThrowsNotFoundException() {
            when(userRepository.findByIdWithAllRelations(eq(Long.MAX_VALUE), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(Long.MAX_VALUE));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify user with no permissions returns empty or null permissions.
         * Expected Result: Empty or null permissions list.
         * Assertions: Permissions is null or empty.
         */
        @Test
        @DisplayName("Get User By ID - No Permissions - Returns empty or null permissions")
        void getUserById_NoPermissions_ReturnsEmptyPermissionsList() {
            testUser.setUserClientPermissionMappings(new HashSet<>());
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserById(TEST_USER_ID);

            assertNotNull(result);
            // Permissions can be null or empty depending on implementation
            assertTrue(result.getPermissions() == null || result.getPermissions().isEmpty());
        }

        /**
         * Purpose: Verify repository is called exactly once.
         * Expected Result: findByIdWithAllRelations called once.
         * Assertions: verify(userRepository, times(1)).findByIdWithAllRelations(...);
         */
        @Test
        @DisplayName("Get User By ID - Repository called once")
        void getUserById_RepositoryCalledOnce() {
            when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

            userService.getUserById(TEST_USER_ID);

            verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        }
    }

    @Nested
    @DisplayName("Get User By Email Tests")
    class GetUserByEmailTests {

        /**
         * Purpose: Verify successful user retrieval by email.
         * Expected Result: UserResponseModel is returned with correct data.
         * Assertions: assertNotNull(result); assertEquals(TEST_EMAIL, result.getEmail());
         */
        @Test
        @DisplayName("Get User By Email - Success - Returns user")
        void getUserByEmail_Success_ReturnsUser() {
            when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

            assertNotNull(result);
            assertEquals(TEST_EMAIL, result.getEmail());
            assertEquals(TEST_USER_ID, result.getUserId());
        }

        /**
         * Purpose: Verify non-existent email throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid Email" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By Email - Email Not Found - Throws NotFoundException")
        void getUserByEmail_EmailNotFound_ThrowsNotFoundException() {
            when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserByEmail(TEST_EMAIL));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
        }

        /**
         * Purpose: Verify null email throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid Email" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By Email - Null Email - Throws NotFoundException")
        void getUserByEmail_NullEmail_ThrowsNotFoundException() {
            when(userRepository.findByEmailWithAllRelations(isNull(), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserByEmail(null));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
        }

        /**
         * Purpose: Verify empty email throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid Email" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By Email - Empty Email - Throws NotFoundException")
        void getUserByEmail_EmptyEmail_ThrowsNotFoundException() {
            when(userRepository.findByEmailWithAllRelations(eq(""), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserByEmail(""));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
        }

        /**
         * Purpose: Verify whitespace email throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid Email" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By Email - Whitespace Email - Throws NotFoundException")
        void getUserByEmail_WhitespaceEmail_ThrowsNotFoundException() {
            when(userRepository.findByEmailWithAllRelations(eq("   "), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserByEmail("   "));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
        }

        /**
         * Purpose: Verify invalid email format throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid Email" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Get User By Email - Invalid Format - Throws NotFoundException")
        void getUserByEmail_InvalidFormat_ThrowsNotFoundException() {
            when(userRepository.findByEmailWithAllRelations(eq("not-an-email"), anyLong())).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.getUserByEmail("not-an-email"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
        }

        /**
         * Purpose: Verify case sensitivity in email lookup.
         * Expected Result: Returns user when email matches.
         * Assertions: assertNotNull(result);
         */
        @Test
        @DisplayName("Get User By Email - Case Sensitive - Returns user")
        void getUserByEmail_CaseSensitive_ReturnsUser() {
            when(userRepository.findByEmailWithAllRelations(eq("TEST@EXAMPLE.COM"), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserByEmail("TEST@EXAMPLE.COM");

            assertNotNull(result);
        }

        /**
         * Purpose: Verify permissions are returned with email lookup.
         * Expected Result: Permissions list is populated.
         * Assertions: assertNotNull(result.getPermissions());
         */
        @Test
        @DisplayName("Get User By Email - Success - Returns permissions")
        void getUserByEmail_Success_ReturnsPermissions() {
            when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

            UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

            assertNotNull(result.getPermissions());
            assertEquals(2, result.getPermissions().size());
        }

        /**
         * Purpose: Verify repository is called exactly once.
         * Expected Result: findByEmailWithAllRelations called once.
         * Assertions: verify(userRepository, times(1)).findByEmailWithAllRelations(...);
         */
        @Test
        @DisplayName("Get User By Email - Repository called once")
        void getUserByEmail_RepositoryCalledOnce() {
            when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

            userService.getUserByEmail(TEST_EMAIL);

            verify(userRepository, times(1)).findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong());
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        /**
         * Purpose: Verify successful user creation with all required fields.
         * Expected Result: User is saved to repository.
         * Assertions: assertDoesNotThrow(); verify(userRepository).save(any(User.class));
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
                            lenient().when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
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
         * Purpose: Verify null permissions throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
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
         * Purpose: Verify empty permissions list throws BadRequestException.
         * Expected Result: BadRequestException with permission required message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.AtLeastOnePermissionRequired,
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
         * Assertions: verify(userClientMappingRepository).save(any(UserClientMapping.class));
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
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

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
    }

    @Nested
    @DisplayName("Fetch Users In Batches Tests")
    class FetchUsersInBatchesTests {

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators using nested loops.
         * 
         * This single test systematically verifies:
         * - Valid and invalid column names
         * - Valid and invalid operators for each column type (string, number, boolean)
         * - AND/OR logic operators
         * - Pagination edge cases (start=0, end=10, negative, zero, same values)
         * - Multiple filters combined
         * 
         * Expected Result: Valid combinations succeed, invalid combinations throw
         * BadRequestException.
         * Assertions: Multiple assertions for each combination.
         */
        @Test
        @DisplayName("Comprehensive Batch Filter Test - All Combinations")
        void fetchUsersInBatches_ComprehensiveCombinationTest() {
            // Test counters
            int validTests = 0;
            int invalidTests = 0;

            // String columns to test
            String[] stringColumns = { "firstName", "lastName", "loginName", "role", "email", "phone" };
            // Number columns to test
            String[] numberColumns = { "userId", "loginAttempts", "addressId" };
            // Boolean columns to test
            String[] booleanColumns = { "isDeleted", "locked", "emailConfirmed", "isGuest" };

            // Logic operators to test
            String[] logicOperators = { "AND", "OR", "and", "or" };
            String[] invalidLogicOperators = { "XOR", "NAND", "", null, "invalid" };

            // ============== TEST 1: Valid column + valid operator combinations ==============
            // Test string columns with string operators
            for (String column : stringColumns) {
                for (String operator : STRING_OPERATORS) {
                    UserRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("testValue");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    List<User> users = Arrays.asList(testUser);
                    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                    lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                    lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                            any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                    assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                            "String column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // Test number columns with number operators
            for (String column : numberColumns) {
                for (String operator : NUMBER_OPERATORS) {
                    UserRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("100");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    List<User> users = Arrays.asList(testUser);
                    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                    lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                    lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                            any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                    assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                            "Number column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // Test boolean columns with boolean operators
            for (String column : booleanColumns) {
                for (String operator : BOOLEAN_OPERATORS) {
                    UserRequestModel request = createBasicPaginationRequest();

                    PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                    filter.setColumn(column);
                    filter.setOperator(operator);
                    filter.setValue("true");
                    request.setFilters(Arrays.asList(filter));
                    request.setLogicOperator("AND");

                    List<User> users = Arrays.asList(testUser);
                    Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                    lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("boolean");
                    lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                            anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                            any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                    assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                            "Boolean column '" + column + "' with operator '" + operator + "' should succeed");
                    validTests++;
                }
            }

            // ============== TEST 2: Invalid column names ==============
            for (String invalidColumn : INVALID_COLUMNS) {
                UserRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(invalidColumn);
                filter.setOperator("equals");
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userService.fetchUsersInCarrierInBatches(request),
                        "Invalid column '" + invalidColumn + "' should throw BadRequestException");
                assertTrue(ex.getMessage().contains("Invalid column"),
                        "Error message should contain 'Invalid column' for column: " + invalidColumn);
                invalidTests++;
            }

            // ============== TEST 3: Invalid operators for each column type ==============
            // String column with invalid operators
            for (String invalidOp : INVALID_OPERATORS) {
                if (invalidOp == null)
                    continue; // Skip null as it causes different exception

                UserRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn("firstName");
                filter.setOperator(invalidOp);
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                lenient().when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userService.fetchUsersInCarrierInBatches(request),
                        "Invalid operator '" + invalidOp + "' should throw BadRequestException");
                assertTrue(ex.getMessage().contains("Invalid operator"),
                        "Error message should contain 'Invalid operator' for: " + invalidOp);
                invalidTests++;
            }

            // ============== TEST 4: Invalid logic operators ==============
            for (String invalidLogic : invalidLogicOperators) {
                if (invalidLogic == null || invalidLogic.isEmpty())
                    continue;

                UserRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("firstName");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("lastName");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(invalidLogic);

                lenient().when(userFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userService.fetchUsersInCarrierInBatches(request),
                        "Invalid logic operator '" + invalidLogic + "' should throw BadRequestException");
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
                invalidTests++;
            }

            // ============== TEST 5: Valid logic operators ==============
            for (String validLogic : logicOperators) {
                UserRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("firstName");
                filter1.setOperator("equals");
                filter1.setValue("test");

                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("lastName");
                filter2.setOperator("equals");
                filter2.setValue("test");

                request.setFilters(Arrays.asList(filter1, filter2));
                request.setLogicOperator(validLogic);

                List<User> users = Arrays.asList(testUser);
                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                lenient().when(userFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                        any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                        "Valid logic operator '" + validLogic + "' should succeed");
                validTests++;
            }

            // ============== TEST 6: Pagination edge cases ==============
            // Invalid pagination: end <= start
            int[][] invalidPaginationCases = {
                    { 10, 10 }, // start == end
                    { 10, 5 }, // start > end
                    { 0, 0 }, // both zero
                    { -1, 10 } // negative start (this actually works as offset, but limit becomes 11)
            };

            for (int[] pagination : invalidPaginationCases) {
                if (pagination[1] - pagination[0] <= 0) {
                    UserRequestModel request = new UserRequestModel();
                    request.setStart(pagination[0]);
                    request.setEnd(pagination[1]);
                    request.setIncludeDeleted(false);

                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> userService.fetchUsersInCarrierInBatches(request),
                            "Pagination start=" + pagination[0] + ", end=" + pagination[1]
                                    + " should throw BadRequestException");
                    assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                    invalidTests++;
                }
            }

            // Valid pagination cases
            int[][] validPaginationCases = {
                    { 0, 10 },
                    { 0, 1 },
                    { 5, 15 },
                    { 0, 100 },
                    { 100, 200 }
            };

            for (int[] pagination : validPaginationCases) {
                UserRequestModel request = new UserRequestModel();
                request.setStart(pagination[0]);
                request.setEnd(pagination[1]);
                request.setIncludeDeleted(false);

                List<User> users = Arrays.asList(testUser);
                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, pagination[1] - pagination[0]), 1);

                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), isNull(), anyString(), isNull(), anyBoolean(),
                        any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                        "Pagination start=" + pagination[0] + ", end=" + pagination[1] + " should succeed");
                validTests++;
            }

            // ============== TEST 7: Multiple filters combined ==============
            // Test with 3 filters of different types
            UserRequestModel multiFilterRequest = createBasicPaginationRequest();

            PaginationBaseRequestModel.FilterCondition stringFilter = new PaginationBaseRequestModel.FilterCondition();
            stringFilter.setColumn("firstName");
            stringFilter.setOperator("contains");
            stringFilter.setValue("John");

            PaginationBaseRequestModel.FilterCondition numberFilter = new PaginationBaseRequestModel.FilterCondition();
            numberFilter.setColumn("loginAttempts");
            numberFilter.setOperator(">");
            numberFilter.setValue("0");

            PaginationBaseRequestModel.FilterCondition boolFilter = new PaginationBaseRequestModel.FilterCondition();
            boolFilter.setColumn("isDeleted");
            boolFilter.setOperator("is");
            boolFilter.setValue("false");

            multiFilterRequest.setFilters(Arrays.asList(stringFilter, numberFilter, boolFilter));
            multiFilterRequest.setLogicOperator("AND");

            List<User> users = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

            lenient().when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
            lenient().when(userFilterQueryBuilder.getColumnType("loginAttempts")).thenReturn("number");
            lenient().when(userFilterQueryBuilder.getColumnType("isDeleted")).thenReturn("boolean");
            lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                    any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

            assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(multiFilterRequest),
                    "Multiple filters of different types should succeed");
            validTests++;

            // ============== TEST 8: No filters (basic pagination) ==============
            UserRequestModel noFilterRequest = createBasicPaginationRequest();
            noFilterRequest.setFilters(null);

            lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), isNull(), anyBoolean(),
                    any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

            assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(noFilterRequest),
                    "Request without filters should succeed");
            validTests++;

            // ============== TEST 9: Include deleted users ==============
            UserRequestModel includeDeletedRequest = createBasicPaginationRequest();
            includeDeletedRequest.setIncludeDeleted(true);

            lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), isNull(), eq(true),
                    any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

            assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(includeDeletedRequest),
                    "Request with includeDeleted=true should succeed");
            validTests++;

            // Print test summary
            System.out.println("Comprehensive Batch Filter Test Summary:");
            System.out.println("  Valid test cases passed: " + validTests);
            System.out.println("  Invalid test cases (expected failures): " + invalidTests);
            System.out.println("  Total test cases: " + (validTests + invalidTests));

            // Ensure we tested a reasonable number of cases
            assertTrue(validTests >= 50, "Should have at least 50 valid test cases");
            assertTrue(invalidTests >= 10, "Should have at least 10 invalid test cases");
        }

        private UserRequestModel createBasicPaginationRequest() {
            UserRequestModel request = new UserRequestModel();
            request.setStart(0);
            request.setEnd(10);
            request.setIncludeDeleted(false);
            return request;
        }
    }

    @Nested
    @DisplayName("Bulk Create Users Tests")
    class BulkCreateUsersTests {

        /**
         * Purpose: Verify bulk creation with all valid users does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - All Valid - No Exception")
        void bulkCreateUsersAsync_AllValid_Success() {
            List<UserRequestModel> users = createValidUserList(3);
            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify bulk creation handles partial success without throwing.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Partial Success - No Exception")
        void bulkCreateUsersAsync_PartialSuccess() {
            List<UserRequestModel> users = new ArrayList<>();
            users.add(createValidUserRequest("valid@test.com"));
            users.add(new UserRequestModel()); // Invalid - missing fields

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify bulk creation with duplicate emails does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Duplicate Email - No Exception")
        void bulkCreateUsersAsync_DuplicateEmail() {
            List<UserRequestModel> users = new ArrayList<>();
            users.add(createValidUserRequest("new@test.com"));
            users.add(createValidUserRequest("existing@test.com"));

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            savedUsers.put("existing@test.com", testUser);

            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify empty list does not throw unexpected exception.
         * Expected Result: No exception is thrown.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Bulk Create Users - Empty List - No exception")
        void bulkCreateUsersAsync_EmptyList() {
            assertDoesNotThrow(
                    () -> userService.bulkCreateUsersAsync(new ArrayList<>(), TEST_USER_ID, TEST_LOGIN_NAME,
                            TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify null list does not throw unexpected exception.
         * Expected Result: No exception is thrown.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Bulk Create Users - Null List - No exception")
        void bulkCreateUsersAsync_NullList() {
            assertDoesNotThrow(
                    () -> userService.bulkCreateUsersAsync(null, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify single user bulk creation does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Single User - No Exception")
        void bulkCreateUsersAsync_SingleUser_Success() {
            List<UserRequestModel> users = createValidUserList(1);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId(100L);
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify large batch bulk creation does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Large Batch - No Exception")
        void bulkCreateUsersAsync_LargeBatch_Success() {
            List<UserRequestModel> users = createValidUserList(10);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        /**
         * Purpose: Verify bulk creation with valid list does not throw.
         * Expected Result: Method completes without exception.
         * Assertions: assertDoesNotThrow();
         * Note: This is an @Async method, so verifications happen in separate thread.
         */
        @Test
        @DisplayName("Bulk Create Users - Multiple Users - No Exception")
        void bulkCreateUsersAsync_LogsOperation() {
            List<UserRequestModel> users = createValidUserList(2);

            setupBulkCreateMocks();

            Map<String, User> savedUsers = new HashMap<>();
            lenient().when(userRepository.findByLoginName(anyString()))
                    .thenAnswer(inv -> savedUsers.get(inv.getArgument(0)));
            lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                if (user.getUserId() == null)
                    user.setUserId((long) (Math.random() * 1000));
                savedUsers.put(user.getLoginName(), user);
                return user;
            });

            try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
                setupPasswordHelperMocks(mockedPasswordHelper);

                // @Async method - just verify it doesn't throw synchronously
                assertDoesNotThrow(
                        () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            }
        }

        // Helper methods for bulk create tests
        private List<UserRequestModel> createValidUserList(int count) {
            List<UserRequestModel> users = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                users.add(createValidUserRequest("bulkuser" + i + "@test.com"));
            }
            return users;
        }

        private UserRequestModel createValidUserRequest(String email) {
            UserRequestModel user = new UserRequestModel();
            user.setLoginName(email);
            user.setFirstName("Bulk");
            user.setLastName("User");
            user.setPhone("1234567890");
            user.setRole("User");
            user.setDob(LocalDate.of(1990, 1, 1));
            user.setPermissionIds(Arrays.asList(1L));
            return user;
        }

        private void setupBulkCreateMocks() {
            lenient().when(userClientMappingRepository.save(any(UserClientMapping.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            lenient().when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
            lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));

            Client client = new Client();
            client.setClientId(TEST_CLIENT_ID);
            client.setImgbbApiKey("test-key");
            lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(client));

            ClientResponseModel clientResponse = new ClientResponseModel();
            clientResponse.setName("Test Client");
            lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
            lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                    .thenReturn(true);
        }

        private void setupPasswordHelperMocks(MockedStatic<PasswordHelper> mockedPasswordHelper) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                    .thenReturn(new String[] { "salt123", "hashedPassword123" });
            mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
        }
    }

    @Nested
    @DisplayName("Confirm Email Tests")
    class ConfirmEmailTests {

        /**
         * Purpose: Verify successful email confirmation.
         * Expected Result: emailConfirmed is set to true.
         * Assertions: assertTrue(user.getEmailConfirmed());
         */
        @Test
        @DisplayName("Confirm Email - Success - Sets emailConfirmed to true")
        void confirmEmail_Success() {
            User userToConfirm = new User(testUserRequest, CREATED_USER);
            userToConfirm.setUserId(TEST_USER_ID);
            userToConfirm.setToken("valid-token");
            userToConfirm.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userToConfirm));
            when(userRepository.save(any(User.class))).thenReturn(userToConfirm);

            assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertTrue(userToConfirm.getEmailConfirmed());
            verify(userRepository, times(1)).save(userToConfirm);
        }

        /**
         * Purpose: Verify non-existent user throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - User Not Found - Throws NotFoundException")
        void confirmEmail_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify null token throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Null Token in DB - Throws BadRequestException")
        void confirmEmail_NullToken_ThrowsBadRequestException() {
            User userWithNullToken = new User(testUserRequest, CREATED_USER);
            userWithNullToken.setUserId(TEST_USER_ID);
            userWithNullToken.setToken(null);
            userWithNullToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "any-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify token mismatch throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Token Mismatch - Throws BadRequestException")
        void confirmEmail_TokenMismatch_ThrowsBadRequestException() {
            User userWithDifferentToken = new User(testUserRequest, CREATED_USER);
            userWithDifferentToken.setUserId(TEST_USER_ID);
            userWithDifferentToken.setToken("correct-token");
            userWithDifferentToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithDifferentToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "wrong-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify already confirmed email throws BadRequestException.
         * Expected Result: BadRequestException with "Account has already been confirmed"
         * message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.AccountConfirmed,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Already Confirmed - Throws BadRequestException")
        void confirmEmail_AlreadyConfirmed_ThrowsBadRequestException() {
            User alreadyConfirmedUser = new User(testUserRequest, CREATED_USER);
            alreadyConfirmedUser.setUserId(TEST_USER_ID);
            alreadyConfirmedUser.setToken("valid-token");
            alreadyConfirmedUser.setEmailConfirmed(true);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(alreadyConfirmedUser));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertEquals(ErrorMessages.LoginErrorMessages.AccountConfirmed, ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        /**
         * Purpose: Verify null emailConfirmed is treated as false.
         * Expected Result: Email is confirmed successfully.
         * Assertions: assertTrue(user.getEmailConfirmed());
         */
        @Test
        @DisplayName("Confirm Email - Null emailConfirmed - Success")
        void confirmEmail_NullEmailConfirmed_Success() {
            User userWithNullConfirmed = new User(testUserRequest, CREATED_USER);
            userWithNullConfirmed.setUserId(TEST_USER_ID);
            userWithNullConfirmed.setToken("valid-token");
            userWithNullConfirmed.setEmailConfirmed(null);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullConfirmed));
            when(userRepository.save(any(User.class))).thenReturn(userWithNullConfirmed);

            assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));

            assertTrue(userWithNullConfirmed.getEmailConfirmed());
            verify(userRepository, times(1)).save(userWithNullConfirmed);
        }

        /**
         * Purpose: Verify negative user ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Negative User ID - Throws NotFoundException")
        void confirmEmail_NegativeUserId_ThrowsNotFoundException() {
            when(userRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(-1L, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify zero user ID throws NotFoundException.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Zero User ID - Throws NotFoundException")
        void confirmEmail_ZeroUserId_ThrowsNotFoundException() {
            when(userRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(0L, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify max long user ID throws NotFoundException when not found.
         * Expected Result: NotFoundException with "Invalid User Id" message.
         * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Max Long User ID - Throws NotFoundException")
        void confirmEmail_MaxLongUserId_ThrowsNotFoundException() {
            when(userRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userService.confirmEmail(Long.MAX_VALUE, "valid-token"));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify empty token string throws BadRequestException.
         * Expected Result: BadRequestException with "Invalid token" message.
         * Assertions: assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken,
         * ex.getMessage());
         */
        @Test
        @DisplayName("Confirm Email - Empty Token - Throws BadRequestException")
        void confirmEmail_EmptyToken_ThrowsBadRequestException() {
            User userWithToken = new User(testUserRequest, CREATED_USER);
            userWithToken.setUserId(TEST_USER_ID);
            userWithToken.setToken("valid-token");
            userWithToken.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithToken));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userService.confirmEmail(TEST_USER_ID, ""));

            assertEquals(ErrorMessages.LoginErrorMessages.InvalidToken, ex.getMessage());
        }

        /**
         * Purpose: Verify repository is called exactly once to find user.
         * Expected Result: findById is called once.
         * Assertions: verify(userRepository, times(1)).findById(TEST_USER_ID);
         */
        @Test
        @DisplayName("Confirm Email - Repository findById called once")
        void confirmEmail_FindByIdCalledOnce() {
            User userToConfirm = new User(testUserRequest, CREATED_USER);
            userToConfirm.setUserId(TEST_USER_ID);
            userToConfirm.setToken("valid-token");
            userToConfirm.setEmailConfirmed(false);

            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userToConfirm));
            when(userRepository.save(any(User.class))).thenReturn(userToConfirm);

            userService.confirmEmail(TEST_USER_ID, "valid-token");

            verify(userRepository, times(1)).findById(TEST_USER_ID);
        }
    }
}
