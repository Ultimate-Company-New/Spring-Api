package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.FilterQueryBuilder.UserGroupFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Repositories.UserGroupRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserGroupService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for UserGroupService tests.
 * 
 * Provides common setup, mocks, and test data used across all UserGroupService
 * test classes.
 * All UserGroupService test classes should extend this base class to inherit
 * the common setup.
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
abstract class UserGroupServiceTestBase {

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

    @Spy
    @InjectMocks
    protected UserGroupService userGroupService;

    @Mock
    protected UserGroupService mockUserGroupService;

    protected UserGroupController userGroupController;
    protected UserGroupController userGroupControllerWithMock;

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
    protected static final String[] STRING_COLUMNS = { "groupName", "description", "notes", "createdUser",
            "modifiedUser" };
    protected static final String[] NUMBER_COLUMNS = { "groupId", "clientId" };
    protected static final String[] BOOLEAN_COLUMNS = { "isActive", "isDeleted" };

    // Valid operators
    protected static final String[] STRING_OPERATORS = { "equals", "contains", "startsWith", "endsWith" };
    protected static final String[] NUMBER_OPERATORS = { "equals", ">", ">=", "<", "<=" };
    protected static final String[] BOOLEAN_OPERATORS = { "is" };

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

        userGroupController = new UserGroupController(userGroupService);
        userGroupControllerWithMock = new UserGroupController(mockUserGroupService);

        stubRequestAuthorizationHeader("Bearer test-token");
    }

    // ==========================================
    // STUB METHODS
    // ==========================================

    protected void stubRequestAuthorizationHeader(String value) {
        lenient().when(request.getHeader("Authorization")).thenReturn(value);
    }

    protected void stubUserGroupRepositoryFindById(Long id, Optional<UserGroup> result) {
        lenient().when(userGroupRepository.findById(id)).thenReturn(result);
    }

    protected void stubUserGroupRepositoryFindByGroupName(String groupName, UserGroup result) {
        lenient().when(userGroupRepository.findByGroupName(groupName)).thenReturn(result);
    }

    protected void stubUserGroupRepositoryFindByIdWithUsers(UserGroup result) {
        lenient().when(userGroupRepository.findByIdWithUsers(anyLong())).thenReturn(result);
    }

    protected void stubUserGroupRepositorySave(UserGroup result) {
        lenient().when(userGroupRepository.save(any(UserGroup.class))).thenReturn(result);
    }

    protected void stubUserGroupUserMapRepositoryFindByGroupId(List<UserGroupUserMap> result) {
        lenient().when(userGroupUserMapRepository.findByGroupId(anyLong())).thenReturn(result);
    }

    protected void stubUserGroupUserMapRepositoryDeleteAll() {
        lenient().doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
    }

    protected void stubUserGroupUserMapRepositorySaveAll(List<UserGroupUserMap> result) {
        lenient().when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(result);
    }

    protected void stubUserRepositoryFindAllById(List<User> result) {
        lenient().when(userRepository.findAllById(anyList())).thenReturn(result);
    }

    protected void stubUserGroupFilterQueryBuilderFindPaginatedEntities(Page<UserGroup> page) {
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any()))
                .thenReturn(page);
    }

    protected void stubUserGroupFilterQueryBuilderGetColumnType(String column, String type) {
        lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn(type);
    }

    protected void stubUserLogServiceLogData(boolean result) {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
    }

    protected void stubUserLogServiceLogDataWithContext(boolean result) {
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(result);
    }

    protected void stubUserGroupRepositoryForBulkInsert(java.util.Map<String, UserGroup> storage) {
        lenient().when(userGroupRepository.findByGroupName(anyString()))
                .thenAnswer(inv -> storage.get((String) inv.getArgument(0)));

        lenient().when(userGroupRepository.save(any(UserGroup.class))).thenAnswer(inv -> {
            UserGroup group = inv.getArgument(0);
            if (group.getGroupId() == null) {
                group.setGroupId(Math.abs(new java.util.Random().nextLong()));
                if (group.getGroupId() == 0) {
                    group.setGroupId(1L);
                }
            }
            storage.put(group.getGroupName(), group);
            return group;
        });
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    protected UserGroupRequestModel createBasicPaginationRequest() {
        UserGroupRequestModel userGroupRequest = new UserGroupRequestModel();
        userGroupRequest.setStart(0);
        userGroupRequest.setEnd(10);
        userGroupRequest.setIncludeDeleted(false);
        return userGroupRequest;
    }

    protected void stubFetchUserGroupsInClientInBatches(PaginationBaseResponseModel<UserGroupResponseModel> response) {
        doReturn(response).when(userGroupService).fetchUserGroupsInClientInBatches(any(UserGroupRequestModel.class));
    }

    protected void stubBulkCreateUserGroupsAsync() {
        doNothing().when(userGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    protected void stubUserGroupRepositoryFindByGroupNameForPartialFailure(String validName, String duplicateName) {
        lenient().when(userGroupRepository.findByGroupName(validName)).thenReturn(null);
        lenient().when(userGroupRepository.findByGroupName(duplicateName)).thenReturn(testUserGroup);
    }

    protected void stubUserGroupRepositorySaveWithException() {
        lenient().when(userGroupRepository.save(any())).thenThrow(new RuntimeException("Unexpected error"));
    }

    protected void stubMockUserGroupServiceGetUserId(Long userId) {
        lenient().when(mockUserGroupService.getUserId()).thenReturn(userId);
    }

    protected void stubMockUserGroupServiceGetUser(String user) {
        lenient().when(mockUserGroupService.getUser()).thenReturn(user);
    }

    protected void stubMockUserGroupServiceGetClientId(Long clientId) {
        lenient().when(mockUserGroupService.getClientId()).thenReturn(clientId);
    }

    protected void stubMockUserGroupServiceBulkCreateUserGroupsAsync() {
        lenient().doNothing().when(mockUserGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(),
                anyLong());
    }

    protected void stubMockUserGroupServiceCreateUserGroup(UserGroupRequestModel userGroupRequest) {
        lenient().doNothing().when(mockUserGroupService).createUserGroup(userGroupRequest);
    }

    protected void stubMockUserGroupServiceUpdateUserGroup(UserGroupRequestModel userGroupRequest) {
        lenient().doNothing().when(mockUserGroupService).updateUserGroup(userGroupRequest);
    }

    protected void stubMockUserGroupServiceToggleUserGroup(Long groupId) {
        lenient().doNothing().when(mockUserGroupService).toggleUserGroup(groupId);
    }

    protected void stubMockUserGroupServiceGetUserGroupDetailsById(Long groupId, UserGroupResponseModel result) {
        lenient().when(mockUserGroupService.getUserGroupDetailsById(groupId)).thenReturn(result);
    }

    protected void stubMockUserGroupServiceFetchUserGroupsInClientInBatches(
            PaginationBaseResponseModel<UserGroupResponseModel> result) {
        lenient().when(mockUserGroupService.fetchUserGroupsInClientInBatches(any(UserGroupRequestModel.class)))
                .thenReturn(result);
    }

    protected void stubServiceThrowsUnauthorizedException() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockUserGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockUserGroupService).createUserGroup(any());
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockUserGroupService).updateUserGroup(any());
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockUserGroupService).toggleUserGroup(anyLong());
        lenient().when(mockUserGroupService.getUserGroupDetailsById(anyLong()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                        com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED));
        lenient().when(mockUserGroupService.fetchUserGroupsInClientInBatches(any()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                        com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED));
    }

}
