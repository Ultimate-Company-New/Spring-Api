package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.FilterQueryBuilder.UserGroupFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Repositories.UserGroupRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserGroupService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserGroupService tests.
 * 
 * Provides common setup, mocks, and test data used across all UserGroupService test classes.
 * All UserGroupService test classes should extend this base class to inherit the common setup.
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
public abstract class UserGroupServiceTestBase extends BaseTest {

    @Mock
    protected UserGroupRepository userGroupRepository;

    @Mock
    protected UserGroupUserMapRepository userGroupUserMapRepository;

    @Mock
    protected UserGroupFilterQueryBuilder userGroupFilterQueryBuilder;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected MessageService messageService;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected UserGroupService userGroupService;

    protected UserGroup testUserGroup;
    protected UserGroupRequestModel testUserGroupRequest;
    protected User testUser;
    protected UserGroupUserMap testMapping;
    
    protected static final Long TEST_GROUP_ID = 1L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = 1L;
    protected static final String TEST_GROUP_NAME = "Test Group";
    protected static final String TEST_DESCRIPTION = "Test Description";
    protected static final String CREATED_USER = "admin";

    // Valid columns for batch filtering
    protected static final String[] STRING_COLUMNS = {"groupName", "description", "notes", "createdUser", "modifiedUser"};
    protected static final String[] NUMBER_COLUMNS = {"groupId", "clientId"};
    protected static final String[] BOOLEAN_COLUMNS = {"isActive", "isDeleted"};

    // Valid operators
    protected static final String[] STRING_OPERATORS = {"equals", "contains", "startsWith", "endsWith"};
    protected static final String[] NUMBER_OPERATORS = {"equals", ">", ">=", "<", "<="};
    protected static final String[] BOOLEAN_OPERATORS = {"is"};

    @BeforeEach
    protected void setUp() {
        testUserGroupRequest = new UserGroupRequestModel();
        testUserGroupRequest.setGroupId(TEST_GROUP_ID);
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        testUserGroupRequest.setDescription(TEST_DESCRIPTION);
        testUserGroupRequest.setUserIds(Arrays.asList(TEST_USER_ID, 2L, 3L));

        testUserGroup = new UserGroup(testUserGroupRequest, CREATED_USER, TEST_CLIENT_ID);
        testUserGroup.setGroupId(TEST_GROUP_ID);
        testUserGroup.setIsDeleted(false);
        testUserGroup.setCreatedAt(LocalDateTime.now());
        testUserGroup.setUpdatedAt(LocalDateTime.now());

        UserRequestModel userRequest = new UserRequestModel();
        userRequest.setLoginName("testuser");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setPhone("1234567890");
        userRequest.setRole("Customer");
        userRequest.setDob(LocalDate.of(1990, 1, 1));

        testUser = new User(userRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);

        testMapping = new UserGroupUserMap(TEST_USER_ID, TEST_GROUP_ID, CREATED_USER);
        testMapping.setMappingId(1L);

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }
}
