package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.FilterQueryBuilder.UserFilterQueryBuilder;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserService tests.
 * 
 * Provides common setup, mocks, and test data used across all UserService test classes.
 * All UserService test classes should extend this base class to inherit the common setup.
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
public abstract class UserServiceTestBase extends BaseTest {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected UserClientPermissionMappingRepository userClientPermissionMappingRepository;

    @Mock
    protected UserGroupUserMapRepository userGroupUserMapRepository;

    @Mock
    protected UserClientMappingRepository userClientMappingRepository;

    @Mock
    protected GoogleCredRepository googleCredRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected Environment environment;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected ClientService clientService;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected UserFilterQueryBuilder userFilterQueryBuilder;

    @Mock
    protected FirebaseHelper firebaseHelper;

    @Mock
    protected EmailHelper emailHelper;

    @InjectMocks
    protected UserService userService;

    protected User testUser;
    protected UserRequestModel testUserRequest;
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_LOGIN_NAME = "test@example.com"; // loginName is the email
    protected static final String CREATED_USER = "admin";

    // Valid string operators (matching FilterCondition.isValidOperator)
    protected static final String[] STRING_OPERATORS = { "equals", "contains", "startsWith", "endsWith" };

    // Valid number operators
    protected static final String[] NUMBER_OPERATORS = { "equals", ">", ">=", "<", "<=" };

    // Valid boolean operators
    protected static final String[] BOOLEAN_OPERATORS = { "is" };

    // Invalid operators
    protected static final String[] INVALID_OPERATORS = { "invalid", "xyz", "!@#", "", null };

    // Invalid columns
    protected static final String[] INVALID_COLUMNS = { "invalidColumn", "xyz", "!@#$", "", "nonExistent" };

    @BeforeEach
    protected void setUp() {
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
}
