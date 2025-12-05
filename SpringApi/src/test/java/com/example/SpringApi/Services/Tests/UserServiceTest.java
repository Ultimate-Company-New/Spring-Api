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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
 * This test class provides comprehensive coverage of UserService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - User retrieval by ID and email
 * - Permission management
 * - Address management
 * - Profile picture handling
 * - Pagination and filtering
 * - Error handling and validation
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * 
 * @author SpringApi Team
 * @version 1.0
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
    
    // Mock helper classes to prevent external service calls during testing
    
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
    private static final String TEST_LOGIN_NAME = "testuser";
    private static final String CREATED_USER = "admin";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test user request model first
        testUserRequest = new UserRequestModel();
        testUserRequest.setUserId(TEST_USER_ID);
        testUserRequest.setLoginName(TEST_LOGIN_NAME);
        testUserRequest.setEmail(TEST_EMAIL);
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
        
        // Use constructor to create test user from request model
        testUser = new User(testUserRequest, CREATED_USER);
        
        // Set ID manually since it's generated by database
        testUser.setUserId(TEST_USER_ID);
        
        // Initialize required collections for eager loading tests
        Set<UserClientPermissionMapping> permissionMappings = new HashSet<>();
        
        // Create test permissions
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
        
        // Create permission mappings
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
        
        // Mock Authorization header for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        
        // Mock environment for profile checks
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"localhost"});
        lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");
        
        // Manually inject the mocked environment into userService since @InjectMocks may not work reliably with many constructor parameters
        try {
            java.lang.reflect.Field envField = UserService.class.getDeclaredField("environment");
            envField.setAccessible(true);
            envField.set(userService, environment);
        } catch (Exception e) {
            // Ignore reflection exceptions in test
        }
        
        // Set the @Value field using reflection since @Value doesn't work with mocked environments
        ReflectionTestUtils.setField(userService, "imageLocation", "imgbb");
        
        // Note: getClientId() is now handled by the actual service implementation
    }

    // ==================== Toggle User Tests ====================
    
    /**
     * Test successful user toggle operation.
     * Verifies that a user's isDeleted flag is correctly toggled from false to true.
     */
    @Test
    @DisplayName("Toggle User - Success - Should toggle isDeleted flag")
    void toggleUser_Success() {
        // Arrange
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        userService.toggleUser(TEST_USER_ID);
        
        // Assert
        assertTrue(testUser.getIsDeleted());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        verify(userRepository, times(1)).save(testUser);
    }
    
    /**
     * Test toggle user with non-existent user ID.
     * Verifies that NotFoundException is thrown when user is not found.
     */
    @Test
    @DisplayName("Toggle User - Failure - User not found")
    void toggleUser_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userService.toggleUser(TEST_USER_ID)
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Get User By ID Tests ====================
    
    /**
     * Test successful retrieval of user by ID.
     * Verifies that user details and permissions are correctly returned.
     */
    @Test
    @DisplayName("Get User By ID - Success - Should return user with permissions")
    void getUserById_Success() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        
        // Act
        UserResponseModel result = userService.getUserById(TEST_USER_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_LOGIN_NAME, result.getLoginName());
        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());
        assertEquals("View Users", result.getPermissions().get(0).getPermissionName());
        
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        // Permissions are now loaded eagerly with findByIdWithAllRelations, no separate call needed
    }
    
    /**
     * Test get user by ID with non-existent user.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Get User By ID - Failure - User not found")
    void getUserById_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userService.getUserById(TEST_USER_ID)
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
    }

    // ==================== Get User By Email Tests ====================
    
    /**
     * Test successful retrieval of user by email.
     * Verifies that user is found and returned with correct details.
     */
    @Test
    @DisplayName("Get User By Email - Success - Should return user")
    void getUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);
        
        // Act
        UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USER_ID, result.getUserId());
        
        verify(userRepository, times(1)).findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong());
        // Permissions are now loaded eagerly with findByEmailWithAllRelations, no separate call needed
    }
    
    /**
     * Test get user by email with non-existent email.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Get User By Email - Failure - Email not found")
    void getUserByEmail_EmailNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(null);
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userService.getUserByEmail(TEST_EMAIL)
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, exception.getMessage());
        verify(userRepository, times(1)).findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong());
    }

    // ==================== Create User Tests ====================
    
    /**
     * Test successful user creation with all required fields.
     * Verifies that user, permissions, and mappings are correctly created.
     */
    @Test
    @DisplayName("Create User - Success - Should create user with permissions")
    void createUser_Success() {
        // Arrange
        User savedUser = new User(testUserRequest, CREATED_USER);
        savedUser.setUserId(TEST_USER_ID);
        
        UserClientMapping userClientMapping = new UserClientMapping(
            TEST_USER_ID,
            TEST_CLIENT_ID,
            CREATED_USER,
            CREATED_USER
        );
        
        when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(userClientMappingRepository.save(any(UserClientMapping.class))).thenReturn(userClientMapping);
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(new Client()));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        clientResponse.setSendgridSenderName("Test Sender");
        clientResponse.setSendGridEmailAddress("sender@test.com");
        clientResponse.setSendGridApiKey("test-key");
        when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Mock static PasswordHelper methods to prevent actual password generation
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[]{"salt123", "hashedPassword123"});
            mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
            
            // Mock EmailTemplates constructor and method
            try (MockedConstruction<EmailTemplates> emailTemplatesMock = mockConstruction(EmailTemplates.class,
                    (mock, context) -> {
                        when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                            .thenReturn(true);
                    })) {
                
                // Mock FirebaseHelper constructor and method
                try (MockedConstruction<FirebaseHelper> firebaseHelperMock = mockConstruction(FirebaseHelper.class,
                        (mock, context) -> {
                            when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(true);
                        })) {
                    
                    // Act & Assert
                    assertDoesNotThrow(() -> userService.createUser(testUserRequest));
                    verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
                    verify(userRepository, times(1)).save(any(User.class));
                    verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
                    verify(userClientMappingRepository, times(1)).save(any(UserClientMapping.class));
                }
            }
        }
    }
    
    /**
     * Test create user with duplicate email.
     * Verifies that BadRequestException is thrown when email already exists.
     */
    @Test
    @DisplayName("Create User - Failure - Duplicate email")
    void createUser_DuplicateEmail_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(testUser);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.createUser(testUserRequest)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }
    
    /**
     * Test create user without permission IDs.
     * Verifies that BadRequestException is thrown when no permissions are provided.
     */
    @Test
    @DisplayName("Create User - Failure - No permissions provided")
    void createUser_NoPermissions_ThrowsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(null);
        User savedUser = new User(testUserRequest, CREATED_USER);
        savedUser.setUserId(TEST_USER_ID);
        
        when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.createUser(testUserRequest)
        );
        
        assertEquals("At least one permission mapping is required for the user.", exception.getMessage());
        verify(userRepository, times(1)).findByLoginName(TEST_EMAIL);
    }

    /**
     * Test create user with empty permission list.
     * Verifies that BadRequestException is thrown.
     */
    @Test
    @DisplayName("Create User - Failure - Empty permissions list")
    void createUser_EmptyPermissions_ThrowsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(new ArrayList<>());
        User savedUser = new User(testUserRequest, CREATED_USER);
        savedUser.setUserId(TEST_USER_ID);
        
        when(userRepository.findByLoginName(TEST_EMAIL)).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.createUser(testUserRequest)
        );
        
        assertEquals("At least one permission mapping is required for the user.", exception.getMessage());
    }

    // ==================== Update User Tests ====================
    
    /**
     * Test successful user update.
     * Verifies that user fields are correctly updated while preserving audit fields.
     */
    @Test
    @DisplayName("Update User - Success - Should update user details")
    void updateUser_Success() {
        // Arrange
        testUserRequest.setFirstName("Updated");
        testUserRequest.setLastName("Name");
        
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
            .thenReturn(new ArrayList<>());
        when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
        
        // Mock client for ImgBB API key
        Client updateClient = new Client();
        updateClient.setClientId(TEST_CLIENT_ID);
        updateClient.setImgbbApiKey("test-imgbb-api-key");
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(updateClient));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        
        // Mock ImgbbHelper constructor to prevent external calls
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {
            
            // Act & Assert
            assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
            verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
            verify(userRepository, times(2)).save(any(User.class)); // Called twice: once for profile pic cleanup, once for main update
            verify(userClientPermissionMappingRepository, times(1)).saveAll(anyList());
        }
    }
    
    /**
     * Test update user with non-existent user ID.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Update User - Failure - User not found")
    void updateUser_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userService.updateUser(testUserRequest)
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }
    
    /**
     * Test update user with changed email.
     * Verifies that BadRequestException is thrown as email cannot be changed.
     */
    @Test
    @DisplayName("Update User - Failure - Email change not allowed")
    void updateUser_EmailChange_ThrowsBadRequestException() {
        // Arrange
        testUserRequest.setEmail("newemail@example.com");
        
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.updateUser(testUserRequest)
        );
        
        assertEquals("User email cannot be changed.", exception.getMessage());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
        verify(userRepository, never()).save(any(User.class));
    }
    
    /**
     * Test update user without permissions.
     * Verifies that BadRequestException is thrown.
     */
    @Test
    @DisplayName("Update User - Failure - No permissions provided")
    void updateUser_NoPermissions_ThrowsBadRequestException() {
        // Arrange
        testUserRequest.setPermissionIds(null);
        
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.updateUser(testUserRequest)
        );
        
        assertEquals("At least one permission mapping is required for the user.", exception.getMessage());
        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
    }
    
    /**
     * Test update user with address.
     * Verifies that address is correctly created or updated.
     */
    @Test
    @DisplayName("Update User - Success - With address update")
    void updateUser_WithAddress_Success() {
        // Arrange
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setStreetAddress("123 Test St");
        addressRequest.setCity("Test City");
        addressRequest.setState("TS");
        addressRequest.setPostalCode("12345");
        addressRequest.setCountry("Test Country");
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
        
        // Mock client for ImgBB API key
        Client testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setImgbbApiKey("test-imgbb-api-key");
        Client testClientPartial = new Client();
        testClientPartial.setClientId(TEST_CLIENT_ID);
        testClientPartial.setImgbbApiKey("test-imgbb-api-key");
        when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClientPartial));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getProperty("imageLocation")).thenReturn("firebase");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        
        // Mock ImgbbHelper constructor to prevent external calls
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {
            
            // Act & Assert
            assertDoesNotThrow(() -> userService.updateUser(testUserRequest));
            verify(addressRepository, times(1)).findById(1L);
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(userRepository, times(2)).save(any(User.class)); // Called twice: once for profile pic cleanup, once for main update
        }
    }

    // ==================== Get All Users Tests ====================
    
    // ==================== Fetch Users In Carrier Tests ====================
    
    // ==================== Fetch Users In Batches Tests ====================
    
    /**
     * Test successful pagination of users.
     * Verifies that users are correctly paginated with proper metadata.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Success - Should return paginated users")
    void fetchUsersInCarrierInBatches_Success() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        testUserRequest.setIncludeDeleted(false);
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), isNull(), eq(false), any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);
        
        // Act
        var result = userService.fetchUsersInCarrierInBatches(testUserRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), isNull(), eq(false), any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    /**
     * Test pagination with invalid column name.
     * Verifies that BadRequestException is thrown for invalid column.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Failure - Invalid column name")
    void fetchUsersInCarrierInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("contains");
        invalidFilter.setValue("test");
        testUserRequest.setFilters(Arrays.asList(invalidFilter));
        testUserRequest.setLogicOperator("AND");
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.fetchUsersInCarrierInBatches(testUserRequest)
        );
        
        assertTrue(exception.getMessage().contains("Invalid column"));
        verify(userFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), anyList(), anyString(), anyList(), anyBoolean(), any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    /**
     * Test pagination with single filter.
     * Verifies that filter expressions are correctly applied.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Success - With single filter")
    void fetchUsersInCarrierInBatches_WithSingleFilter_Success() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        testUserRequest.setIncludeDeleted(false);
        
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("firstName");
        filter.setOperator("contains");
        filter.setValue("Test");
        testUserRequest.setFilters(Arrays.asList(filter));
        testUserRequest.setLogicOperator("AND");
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
        when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);
        
        // Act
        var result = userService.fetchUsersInCarrierInBatches(testUserRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    /**
     * Test pagination with multiple filters using AND logic.
     * Verifies that multiple filter conditions are correctly combined with AND.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Success - With multiple filters AND logic")
    void fetchUsersInCarrierInBatches_WithMultipleFiltersAND_Success() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        testUserRequest.setIncludeDeleted(false);
        
        // Create multiple filters
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("firstName");
        filter1.setOperator("contains");
        filter1.setValue("Test");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("role");
        filter2.setOperator("equals");
        filter2.setValue("Admin");
        
        testUserRequest.setFilters(Arrays.asList(filter1, filter2));
        testUserRequest.setLogicOperator("AND");
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
        when(userFilterQueryBuilder.getColumnType("role")).thenReturn("string");
        when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);
        
        // Act
        var result = userService.fetchUsersInCarrierInBatches(testUserRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    /**
     * Test pagination with multiple filters using OR logic.
     * Verifies that multiple filter conditions are correctly combined with OR.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Success - With multiple filters OR logic")
    void fetchUsersInCarrierInBatches_WithMultipleFiltersOR_Success() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        testUserRequest.setIncludeDeleted(false);
        
        // Create multiple filters
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("firstName");
        filter1.setOperator("contains");
        filter1.setValue("John");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("firstName");
        filter2.setOperator("contains");
        filter2.setValue("Jane");
        
        PaginationBaseRequestModel.FilterCondition filter3 = new PaginationBaseRequestModel.FilterCondition();
        filter3.setColumn("email");
        filter3.setOperator("contains");
        filter3.setValue("@admin.com");
        
        testUserRequest.setFilters(Arrays.asList(filter1, filter2, filter3));
        testUserRequest.setLogicOperator("OR");
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
        when(userFilterQueryBuilder.getColumnType("email")).thenReturn("string");
        when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("OR"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);
        
        // Act
        var result = userService.fetchUsersInCarrierInBatches(testUserRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("OR"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        );
    }
    
    /**
     * Test pagination with complex filters (number, date, boolean operators).
     * Verifies that different column types and operators work correctly.
     */
    @Test
    @DisplayName("Fetch Users In Batches - Success - With complex filters")
    void fetchUsersInCarrierInBatches_WithComplexFilters_Success() {
        // Arrange
        testUserRequest.setStart(0);
        testUserRequest.setEnd(10);
        testUserRequest.setIncludeDeleted(false);
        
        // Create filters with different types
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("loginAttempts");
        filter1.setOperator(">");
        filter1.setValue("3");
        
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("emailConfirmed");
        filter2.setOperator("is");
        filter2.setValue("true");
        
        PaginationBaseRequestModel.FilterCondition filter3 = new PaginationBaseRequestModel.FilterCondition();
        filter3.setColumn("firstName");
        filter3.setOperator("startsWith");
        filter3.setValue("A");
        
        testUserRequest.setFilters(Arrays.asList(filter1, filter2, filter3));
        testUserRequest.setLogicOperator("AND");
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);
        
        when(userFilterQueryBuilder.getColumnType("loginAttempts")).thenReturn("number");
        when(userFilterQueryBuilder.getColumnType("emailConfirmed")).thenReturn("boolean");
        when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
        when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);
        
        // Act
        var result = userService.fetchUsersInCarrierInBatches(testUserRequest);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(userFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), anyList(), eq(false), any(org.springframework.data.domain.Pageable.class)
        );
    }

    // ==================== Bulk Create Users Tests ====================
    
    /**
     * Test successful bulk user creation.
     * Verifies that multiple users are created successfully.
     */
    @Test
    @DisplayName("Bulk Create Users - Success - All valid users")
    void bulkCreateUsers_AllValid_Success() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserRequestModel userReq = new UserRequestModel();
            userReq.setLoginName("bulkuser" + i + "@test.com");
            userReq.setEmail("bulkuser" + i + "@test.com");
            userReq.setFirstName("Bulk");
            userReq.setLastName("User" + i);
            userReq.setPhone("123456789" + i);
            userReq.setRole("User");
            userReq.setDob(LocalDate.of(1990, 1, 1));
            userReq.setPermissionIds(Arrays.asList(1L));
            users.add(userReq);
        }
        
        Map<String, User> savedUsers = new HashMap<>();
        when(userRepository.findByLoginName(anyString())).thenAnswer(invocation -> {
            String loginName = invocation.getArgument(0);
            return savedUsers.get(loginName);
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getUserId() == null) {
                user.setUserId((long) (Math.random() * 1000));
            }
            savedUsers.put(user.getLoginName(), user);
            return user;
        });
        when(userClientMappingRepository.save(any(UserClientMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
        Client bulkClient = new Client();
        bulkClient.setClientId(TEST_CLIENT_ID);
        bulkClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(bulkClient));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        // Mock additional services needed by createUser
        lenient().when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            address.setAddressId((long) (Math.random() * 1000));
            return address;
        });
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(emailHelper.sendEmail(any(com.example.SpringApi.Models.RequestModels.SendEmailRequest.class))).thenReturn(true);
        lenient().when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        
        // Mock static PasswordHelper
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[]{"salt123", "hashedPassword123"});
            mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
            
            // Act
            // Note: bulkCreateUsersAsync is now void and async, so we can't test the return value directly
            // This test would need to be refactored to test the async behavior properly
            assertDoesNotThrow(() -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            
            // Assert
            // Verify that save was called for each user
            verify(userRepository, times(3)).save(any(User.class));
        }
    }
    
    /**
     * Test bulk user creation with partial success.
     * Verifies that some users succeed while others fail validation.
     */
    @Test
    @DisplayName("Bulk Create Users - Partial Success - Mixed valid and invalid")
    void bulkCreateUsers_PartialSuccess() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();
        
        // Valid user
        UserRequestModel validUser = new UserRequestModel();
        validUser.setLoginName("valid@test.com");
        validUser.setEmail("valid@test.com");
        validUser.setFirstName("Valid");
        validUser.setLastName("User");
        validUser.setPhone("1234567890");
        validUser.setRole("User");
        validUser.setDob(LocalDate.of(1990, 1, 1));
        validUser.setPermissionIds(Arrays.asList(1L));
        users.add(validUser);
        
        // Invalid user (missing required fields)
        UserRequestModel invalidUser = new UserRequestModel();
        invalidUser.setEmail("invalid@test.com");
        users.add(invalidUser);
        
        Map<String, User> savedUsers = new HashMap<>();
        when(userRepository.findByLoginName(anyString())).thenAnswer(invocation ->
            savedUsers.get(invocation.getArgument(0))
        );
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getUserId() == null) {
                user.setUserId(100L);
            }
            savedUsers.put(user.getLoginName(), user);
            return user;
        });
        when(userClientMappingRepository.save(any(UserClientMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
        Client partialClient = new Client();
        partialClient.setClientId(TEST_CLIENT_ID);
        partialClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(partialClient));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[]{"salt123", "hashedPassword123"});
            mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
            
            // Act
            // Note: bulkCreateUsersAsync is now void and async, so we can't test the return value directly
            assertDoesNotThrow(() -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            
            // Assert
            // Verify that save was called for the valid user
            verify(userRepository, times(1)).save(any(User.class));
        }
    }
    
    /**
     * Test bulk user creation with duplicate email.
     * Verifies that duplicate emails are rejected.
     */
    @Test
    @DisplayName("Bulk Create Users - Failure - Duplicate email")
    void bulkCreateUsers_DuplicateEmail() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();
        
        UserRequestModel user1 = new UserRequestModel();
        user1.setLoginName("new@test.com");
        user1.setEmail("new@test.com");
        user1.setFirstName("New");
        user1.setLastName("User");
        user1.setPhone("1234567890");
        user1.setRole("User");
        user1.setDob(LocalDate.of(1990, 1, 1));
        user1.setPermissionIds(Arrays.asList(1L));
        users.add(user1);
        
        UserRequestModel user2 = new UserRequestModel();
        user2.setLoginName("existing@test.com");
        user2.setEmail("existing@test.com");
        user2.setFirstName("Existing");
        user2.setLastName("User");
        user2.setPhone("1234567890");
        user2.setRole("User");
        user2.setDob(LocalDate.of(1990, 1, 1));
        user2.setPermissionIds(Arrays.asList(1L));
        users.add(user2);
        
        Map<String, User> savedUsersForDuplicate = new HashMap<>();
        savedUsersForDuplicate.put("existing@test.com", testUser);
        when(userRepository.findByLoginName(anyString())).thenAnswer(invocation ->
            savedUsersForDuplicate.get(invocation.getArgument(0))
        );
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getUserId() == null) {
                user.setUserId(100L);
            }
            savedUsersForDuplicate.put(user.getLoginName(), user);
            return user;
        });
        when(userClientMappingRepository.save(any(UserClientMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(Optional.of(new GoogleCred()));
        Client duplicateClient = new Client();
        duplicateClient.setClientId(TEST_CLIENT_ID);
        duplicateClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(duplicateClient));
        
        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);
        
        try (MockedStatic<PasswordHelper> mockedPasswordHelper = mockStatic(PasswordHelper.class)) {
            mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
            mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[]{"salt123", "hashedPassword123"});
            mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
            
            // Act
            // Note: bulkCreateUsersAsync is now void and async, so we can't test the return value directly
            assertDoesNotThrow(() -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
            
            // Assert
            // Verify that save was called for the new user only (duplicate should be rejected)
            verify(userRepository, times(1)).save(any(User.class));
        }
    }
    
    /**
     * Test bulk user creation with empty list.
     * Verifies that empty list returns empty result.
     */
    @Test
    @DisplayName("Bulk Create Users - Empty List - Throws BadRequestException")
    void bulkCreateUsers_EmptyList() {
        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException exception = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> userService.bulkCreateUsersAsync(new ArrayList<>(), TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID)
        );
        
        assertTrue(exception.getMessage().contains("User list cannot be null or empty"));
    }

    // ==================== Confirm Email Tests ====================
    
    /**
     * Test successful email confirmation.
     * Verifies that user's email is confirmed when valid userId and token are provided.
     */
    @Test
    @DisplayName("Confirm Email - Success - Should confirm email with valid token")
    void confirmEmail_Success() {
        // Arrange
        User userToConfirm = new User(testUserRequest, CREATED_USER);
        userToConfirm.setUserId(TEST_USER_ID);
        userToConfirm.setToken("valid-token");
        userToConfirm.setEmailConfirmed(false);
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userToConfirm));
        when(userRepository.save(any(User.class))).thenReturn(userToConfirm);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act
        assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));
        
        // Assert
        assertTrue(userToConfirm.getEmailConfirmed());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, times(1)).save(userToConfirm);
    }
    
    /**
     * Test email confirmation with non-existent user.
     * Verifies that NotFoundException is thrown when user is not found.
     */
    @Test
    @DisplayName("Confirm Email - Failure - User not found")
    void confirmEmail_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> userService.confirmEmail(TEST_USER_ID, "valid-token")
        );
        
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
    }
    
    /**
     * Test email confirmation with null token on user.
     * Verifies that BadRequestException is thrown when user's token is null.
     */
    @Test
    @DisplayName("Confirm Email - Failure - User token is null")
    void confirmEmail_NullToken_ThrowsBadRequestException() {
        // Arrange
        User userWithNullToken = new User(testUserRequest, CREATED_USER);
        userWithNullToken.setUserId(TEST_USER_ID);
        userWithNullToken.setToken(null);
        userWithNullToken.setEmailConfirmed(false);
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullToken));
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.confirmEmail(TEST_USER_ID, "any-token")
        );
        
        assertEquals("Invalid or expired verification token", exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
    }
    
    /**
     * Test email confirmation with mismatched token.
     * Verifies that BadRequestException is thrown when token doesn't match.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Token mismatch")
    void confirmEmail_TokenMismatch_ThrowsBadRequestException() {
        // Arrange
        User userWithDifferentToken = new User(testUserRequest, CREATED_USER);
        userWithDifferentToken.setUserId(TEST_USER_ID);
        userWithDifferentToken.setToken("correct-token");
        userWithDifferentToken.setEmailConfirmed(false);
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithDifferentToken));
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.confirmEmail(TEST_USER_ID, "wrong-token")
        );
        
        assertEquals("Invalid or expired verification token", exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
    }
    
    /**
     * Test email confirmation when email is already confirmed.
     * Verifies that BadRequestException is thrown when email is already confirmed.
     */
    @Test
    @DisplayName("Confirm Email - Failure - Email already confirmed")
    void confirmEmail_AlreadyConfirmed_ThrowsBadRequestException() {
        // Arrange
        User alreadyConfirmedUser = new User(testUserRequest, CREATED_USER);
        alreadyConfirmedUser.setUserId(TEST_USER_ID);
        alreadyConfirmedUser.setToken("valid-token");
        alreadyConfirmedUser.setEmailConfirmed(true);
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(alreadyConfirmedUser));
        
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> userService.confirmEmail(TEST_USER_ID, "valid-token")
        );
        
        assertEquals("Email is already confirmed", exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
        verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
    }
    
    /**
     * Test email confirmation when emailConfirmed is null (treated as false).
     * Verifies that email confirmation succeeds when emailConfirmed is null.
     */
    @Test
    @DisplayName("Confirm Email - Success - Email confirmed is null (treated as false)")
    void confirmEmail_NullEmailConfirmed_Success() {
        // Arrange
        User userWithNullConfirmed = new User(testUserRequest, CREATED_USER);
        userWithNullConfirmed.setUserId(TEST_USER_ID);
        userWithNullConfirmed.setToken("valid-token");
        userWithNullConfirmed.setEmailConfirmed(null); // Null means not confirmed
        
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithNullConfirmed));
        when(userRepository.save(any(User.class))).thenReturn(userWithNullConfirmed);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        // Act
        assertDoesNotThrow(() -> userService.confirmEmail(TEST_USER_ID, "valid-token"));
        
        // Assert
        assertTrue(userWithNullConfirmed.getEmailConfirmed());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, times(1)).save(userWithNullConfirmed);
    }
}