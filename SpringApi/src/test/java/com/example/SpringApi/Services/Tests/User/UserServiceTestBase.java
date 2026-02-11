package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.FilterQueryBuilder.UserFilterQueryBuilder;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserService tests.
 * Contains common mocks, test data, and centralized stubbing methods.
 */
@ExtendWith(MockitoExtension.class)
public abstract class UserServiceTestBase {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected UserClientMappingRepository userClientMappingRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected PermissionRepository permissionRepository;

    @Mock
    protected UserClientPermissionMappingRepository userClientPermissionMappingRepository;

    @Mock
    protected UserFilterQueryBuilder userFilterQueryBuilder;

    @Mock
    protected UserGroupUserMapRepository userGroupUserMapRepository;

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected ClientService clientService;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Environment environment;

    @Mock
    protected GoogleCredRepository googleCredRepository;

    @InjectMocks
    protected UserService userService;

    @Mock
    protected UserService mockUserService;

    protected com.example.SpringApi.Controllers.UserController userController;
    protected com.example.SpringApi.Controllers.UserController userControllerWithMock;

    protected User testUser;
    protected UserRequestModel testUserRequest;
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_CLIENT_ID = 100L;
    protected static final String[] STRING_OPERATORS = { "equals", "contains", "startsWith", "endsWith" };
    protected static final String[] NUMBER_OPERATORS = { "equals", ">", "<", ">=", "<=" };
    protected static final String[] BOOLEAN_OPERATORS = { "is" };
    protected static final String[] INVALID_COLUMNS = { "invalidColumn", "123" };
    protected static final String[] INVALID_OPERATORS = { "invalidOp", "xyz" };
    protected static final String TEST_LOGIN_NAME = "test@example.com";
    protected static final String CREATED_USER = "system_user";
    protected static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        testUserRequest = new UserRequestModel();
        testUserRequest.setFirstName("John");
        testUserRequest.setLastName("Doe");
        testUserRequest.setLoginName(TEST_LOGIN_NAME);
        testUserRequest.setPhone("1234567890");
        testUserRequest.setRole("CUSTOMER");
        testUserRequest.setUserId(TEST_USER_ID);
        testUserRequest.setDob(LocalDate.of(1990, 1, 1));
        testUserRequest.setPermissionIds(Arrays.asList(1L, 2L));

        testUser = new User(testUserRequest, CREATED_USER);
        testUser.setUserId(TEST_USER_ID);
        testUser.setUserClientPermissionMappings(new HashSet<>());
        testUser.setUserGroupMappings(new HashSet<>());

        userController = new com.example.SpringApi.Controllers.UserController(userService);
        userControllerWithMock = new com.example.SpringApi.Controllers.UserController(mockUserService);

        stubEnvironmentActiveProfiles(new String[] { "localhost" });
        stubEnvironmentImageLocation("firebase");
        ReflectionTestUtils.setField(userService, "imageLocation", "imgbb");
    }

    // ==========================================
    // HELPER METHODS (Data Creation)
    // ==========================================

    protected List<UserRequestModel> createValidUserList(int count) {
        List<UserRequestModel> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(createValidUserRequest("bulkuser" + i + "@test.com"));
        }
        return users;
    }

    protected UserRequestModel createValidUserRequest(String email) {
        UserRequestModel user = new UserRequestModel();
        user.setLoginName(email);
        user.setFirstName("Bulk");
        user.setLastName("User");
        user.setPhone("1234567890");
        user.setRole("CUSTOMER");
        user.setDob(LocalDate.of(1990, 1, 1));
        user.setPermissionIds(List.of(1L));
        return user;
    }

    protected Client createTestClient() {
        Client client = new Client();
        client.setClientId(TEST_CLIENT_ID);
        client.setImgbbApiKey("test-key");
        client.setSendGridApiKey("key");
        client.setSendGridEmailAddress("sender@test.com");
        client.setSendgridSenderName("Sender");
        client.setName("Test Client");
        return client;
    }

    protected ClientResponseModel createTestClientResponse() {
        ClientResponseModel response = new ClientResponseModel();
        response.setName("Test Client");
        response.setSendgridSenderName("Sender");
        response.setSendGridEmailAddress("sender@test.com");
        response.setSendGridApiKey("key");
        return response;
    }

    // ==========================================
    // STUB CONFIGURATION HELPERS
    // ==========================================

    protected void stubConfigurePasswordHelperMock(MockedStatic<PasswordHelper> mockedPasswordHelper) {
        mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
        mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[] { "salt123", "hashedPassword123" });
        mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
    }

    protected void stubStandardCreateUserMocks() {
        stubUserClientMappingRepositorySave(new UserClientMapping());
        stubUserClientPermissionMappingRepositorySaveAll(new ArrayList<>());
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubGoogleCredRepositoryFindById(anyLong(), Optional.of(new GoogleCred()));
        stubClientRepositoryFindById(anyLong(), Optional.of(createTestClient()));
        stubClientServiceGetClientById(createTestClientResponse());
        stubUserLogServiceLogDataWithContext(true);
        stubUserLogServiceLogData(true);
        stubUserRepositoryFindByLoginNameAny(null);
        stubUserRepositorySave(testUser);
        stubPermissionRepositoryFindAllById(new ArrayList<>());
    }

    // ==========================================
    // CENTRALIZED STUBS (Using lenient)
    // ==========================================

    protected void stubUserRepositorySave(User returnUser) {
        lenient().when(userRepository.save(any(User.class))).thenReturn(returnUser);
    }

    protected void stubUserRepositoryFindById(Long id, Optional<User> result) {
        lenient().when(userRepository.findById(id)).thenReturn(result);
    }

    protected void stubUserRepositoryFindByIdAny(Optional<User> result) {
        lenient().when(userRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubUserRepositoryFindByLoginName(String loginName, User result) {
        lenient().when(userRepository.findByLoginName(loginName)).thenReturn(result);
    }

    protected void stubUserRepositoryFindByLoginNameAny(User result) {
        lenient().when(userRepository.findByLoginName(anyString())).thenReturn(result);
    }

    protected void stubUserRepositoryFindByEmailWithAllRelations(User result) {
        lenient().when(userRepository.findByEmailWithAllRelations(anyString(), anyLong())).thenReturn(result);
    }

    protected void stubUserRepositoryFindByIdWithAllRelations(User result) {
        lenient().when(userRepository.findByIdWithAllRelations(anyLong(), anyLong())).thenReturn(result);
    }

    protected void stubUserClientMappingRepositorySave(UserClientMapping result) {
        lenient().when(userClientMappingRepository.save(any(UserClientMapping.class))).thenReturn(result);
    }

    protected void stubUserClientPermissionMappingRepositorySaveAll(List<UserClientPermissionMapping> result) {
        lenient().when(userClientPermissionMappingRepository.saveAll(anyList())).thenReturn(result);
    }

    protected void stubUserGroupUserMapRepositorySaveAll(List<UserGroupUserMap> result) {
        lenient().when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(result);
    }

    protected void stubUserGroupUserMapRepositoryDeleteAll() {
        lenient().doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
    }

    protected void stubGoogleCredRepositoryFindById(Long id, Optional<GoogleCred> result) {
        lenient().when(googleCredRepository.findById(id)).thenReturn(result);
    }

    protected void stubGoogleCredRepositoryFindByIdAny(Optional<GoogleCred> result) {
        lenient().when(googleCredRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubClientRepositoryFindById(Long id, Optional<Client> result) {
        lenient().when(clientRepository.findById(id)).thenReturn(result);
    }

    protected void stubClientRepositoryFindByIdAny(Optional<Client> result) {
        lenient().when(clientRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubClientServiceGetClientById(ClientResponseModel result) {
        lenient().when(clientService.getClientById(anyLong())).thenReturn(result);
    }

    protected void stubUserLogServiceLogData(boolean result) {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
    }

    protected void stubUserLogServiceLogDataWithContext(boolean result) {
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(result);
    }

    protected void stubUserFilterQueryBuilderFindPaginatedEntities(Page<User> page) {
        lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any()))
                .thenReturn(page);
    }

    protected void stubUserFilterQueryBuilderGetColumnType(String column, String type) {
        lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn(type);
    }

    protected void stubPermissionRepositoryFindAllById(List<Permission> permissions) {
        lenient().when(permissionRepository.findAllById(any())).thenReturn(permissions);
    }

    protected void stubAddressRepositoryFindById(Long id, Optional<Address> result) {
        lenient().when(addressRepository.findById(id)).thenReturn(result);
    }

    protected void stubAddressRepositorySave(Address result) {
        lenient().when(addressRepository.save(any(Address.class))).thenReturn(result);
    }

    protected void stubPermissionRepositoryFindAll(List<Permission> permissions) {
        lenient().when(permissionRepository.findAll()).thenReturn(permissions);
    }

    protected void stubUserClientPermissionMappingRepositoryDeleteByUserIdAndClientId() {
        lenient().doNothing().when(userClientPermissionMappingRepository).deleteByUserIdAndClientId(anyLong(),
                anyLong());
    }

    protected void stubUserGroupUserMapRepositoryDeleteByUserId() {
        lenient().doNothing().when(userGroupUserMapRepository).deleteByUserId(anyLong());
    }

    // ==========================================
    // MOCK USER SERVICE STUBS (For Controller Tests)
    // ==========================================

    protected void stubMockUserServiceBulkCreateUsersAsync() {
        lenient().doNothing().when(mockUserService).bulkCreateUsersAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    protected void stubMockUserServiceConfirmEmail(Long userId, String token) {
        lenient().doNothing().when(mockUserService).confirmEmail(userId, token);
    }

    protected void stubMockUserServiceCreateUser(UserRequestModel userRequest) {
        lenient().doNothing().when(mockUserService).createUser(userRequest);
    }

    protected void stubMockUserServiceGetAllPermissions(
            List<com.example.SpringApi.Models.ResponseModels.PermissionResponseModel> result) {
        lenient().when(mockUserService.getAllPermissions()).thenReturn(result);
    }

    protected void stubMockUserServiceGetUserByEmail(String email,
            com.example.SpringApi.Models.ResponseModels.UserResponseModel result) {
        lenient().when(mockUserService.getUserByEmail(email)).thenReturn(result);
    }

    protected void stubMockUserServiceGetUserById(Long userId,
            com.example.SpringApi.Models.ResponseModels.UserResponseModel result) {
        lenient().when(mockUserService.getUserById(userId)).thenReturn(result);
    }

    protected void stubMockUserServiceToggleUser(Long userId) {
        lenient().doNothing().when(mockUserService).toggleUser(userId);
    }

    protected void stubMockUserServiceUpdateUser(UserRequestModel userRequest) {
        lenient().doNothing().when(mockUserService).updateUser(userRequest);
    }

    protected void stubMockUserServiceFetchUsersInCarrierInBatches(
            com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<com.example.SpringApi.Models.ResponseModels.UserResponseModel> result) {
        lenient().when(mockUserService.fetchUsersInCarrierInBatches(any(UserRequestModel.class))).thenReturn(result);
    }

    protected void stubServiceThrowsUnauthorizedException() {
        com.example.SpringApi.Exceptions.UnauthorizedException ex = new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED);
        lenient().doThrow(ex).when(mockUserService).bulkCreateUsersAsync(anyList(), anyLong(), anyString(), anyLong());
        lenient().doThrow(ex).when(mockUserService).confirmEmail(anyLong(), anyString());
        lenient().doThrow(ex).when(mockUserService).createUser(any());
        lenient().when(mockUserService.getAllPermissions()).thenThrow(ex);
        lenient().when(mockUserService.getUserByEmail(anyString())).thenThrow(ex);
        lenient().when(mockUserService.getUserById(anyLong())).thenThrow(ex);
        lenient().doThrow(ex).when(mockUserService).toggleUser(anyLong());
        lenient().doThrow(ex).when(mockUserService).updateUser(any());
        lenient().when(mockUserService.fetchUsersInCarrierInBatches(any())).thenThrow(ex);
    }

    // Static and Construction Mocks
    protected MockedStatic<PasswordHelper> mockedPasswordHelper;
    protected MockedConstruction<com.example.SpringApi.Helpers.EmailTemplates> mockedEmailTemplates;
    protected MockedConstruction<com.example.SpringApi.Helpers.FirebaseHelper> mockedFirebaseHelper;
    protected MockedConstruction<com.example.SpringApi.Helpers.ImgbbHelper> mockedImgbbHelper;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedPasswordHelper != null) {
            mockedPasswordHelper.close();
        }
        if (mockedEmailTemplates != null) {
            mockedEmailTemplates.close();
        }
        if (mockedFirebaseHelper != null) {
            mockedFirebaseHelper.close();
        }
        if (mockedImgbbHelper != null) {
            mockedImgbbHelper.close();
        }
    }

    protected void stubEnvironmentActiveProfiles(String[] profiles) {
        lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
    }

    protected void stubEnvironmentImageLocation(String location) {
        lenient().when(environment.getProperty("imageLocation")).thenReturn(location);
    }

    // ==========================================
    // HELPER MOCK STUBS
    // ==========================================

    protected void stubPasswordHelper() {
        if (mockedPasswordHelper != null) {
            mockedPasswordHelper.close();
        }
        mockedPasswordHelper = org.mockito.Mockito.mockStatic(PasswordHelper.class);
        mockedPasswordHelper.when(PasswordHelper::getRandomPassword).thenReturn("randomPassword123");
        mockedPasswordHelper.when(() -> PasswordHelper.getHashedPasswordAndSalt(anyString()))
                .thenReturn(new String[] { "salt123", "hashedPassword123" });
        mockedPasswordHelper.when(() -> PasswordHelper.getToken(anyString())).thenReturn("token123");
    }

    protected void stubEmailTemplates() {
        if (mockedEmailTemplates != null) {
            mockedEmailTemplates.close();
        }
        mockedEmailTemplates = org.mockito.Mockito.mockConstruction(com.example.SpringApi.Helpers.EmailTemplates.class,
                (mock, context) -> {
                    lenient()
                            .when(mock.sendNewUserAccountConfirmation(anyLong(), anyString(), anyString(), anyString()))
                            .thenReturn(true);
                });
    }

    protected void stubFirebaseHelper() {
        if (mockedFirebaseHelper != null) {
            mockedFirebaseHelper.close();
        }
        mockedFirebaseHelper = org.mockito.Mockito.mockConstruction(com.example.SpringApi.Helpers.FirebaseHelper.class,
                (mock, context) -> {
                    lenient().when(mock.uploadFileToFirebase(anyString(), anyString())).thenReturn(true);
                });
    }

    protected void stubImgbbHelper() {
        if (mockedImgbbHelper != null) {
            mockedImgbbHelper.close();
        }
        mockedImgbbHelper = org.mockito.Mockito.mockConstruction(com.example.SpringApi.Helpers.ImgbbHelper.class,
                (mock, context) -> {
                    lenient().when(mock.deleteImage(anyString())).thenReturn(true);
                });
    }

    protected void stubUserClientPermissionMappingRepositoryFindByUserIdAndClientId(
            List<UserClientPermissionMapping> result) {
        lenient().when(userClientPermissionMappingRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                .thenReturn(result);
    }

    protected void stubUserGroupUserMapRepositoryFindByUserId(List<UserGroupUserMap> result) {
        lenient().when(userGroupUserMapRepository.findByUserId(anyLong())).thenReturn(result);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    protected Permission createPermission(Long id, String name, String desc, boolean isDeleted) {
        Permission p = new Permission();
        p.setPermissionId(id);
        p.setPermissionName(name);
        p.setPermissionCode(name.toUpperCase());
        p.setDescription(desc);
        p.setIsDeleted(isDeleted);
        return p;
    }

    protected void setupCreateUserMocks() {
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

    protected void setupUpdateUserMocks() {
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

    protected void testStringFilter(String column, String operator, String value) {
        UserRequestModel paginationRequest = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue(value);
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        stubUserFilterQueryBuilderGetColumnType(column, "string");
        stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(paginationRequest));
    }

    protected void testNumberFilter(String column, String operator, String value) {
        UserRequestModel paginationRequest = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue(value);
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        stubUserFilterQueryBuilderGetColumnType(column, "number");
        stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(paginationRequest));
    }

    protected void testBooleanFilter(String column, String operator, String value) {
        UserRequestModel paginationRequest = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue(value);
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        stubUserFilterQueryBuilderGetColumnType(column, "boolean");
        stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(paginationRequest));
    }

    protected void testLogicOperator(String logicOperator) {
        UserRequestModel paginationRequest = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("firstName");
        filter1.setOperator("equals");
        filter1.setValue("test");
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("lastName");
        filter2.setOperator("equals");
        filter2.setValue("test");
        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator(logicOperator);

        stubUserFilterQueryBuilderGetColumnType("firstName", "string");
        stubUserFilterQueryBuilderGetColumnType("lastName", "string");
        stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(paginationRequest));
    }

    protected UserRequestModel createBasicPaginationRequest() {
        UserRequestModel request = new UserRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setIncludeDeleted(false);
        return request;
    }
}