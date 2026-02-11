package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Total Tests: 8
@DisplayName("UserGroupService - Bulk Create User Groups Async Tests")
class BulkCreateUserGroupsAsyncTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("bulkCreateUserGroups - Verify @PreAuthorize Annotation")
    void bulkCreateUserGroups_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("bulkCreateUserGroups", List.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreateUserGroups method");
        assertTrue(annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("bulkCreateUserGroups - Controller delegates to service")
    void bulkCreateUserGroups_WithValidRequest_DelegatesToService() {
        // Arrange
        UserGroupController localController = new UserGroupController(userGroupService);
        List<UserGroupRequestModel> request = Collections.singletonList(testUserGroupRequest);

        doNothing().when(userGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());

        // Act
        localController.bulkCreateUserGroups(request);

        // Assert
        verify(userGroupService, times(1)).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Success - All Valid")
    void bulkCreateUserGroupsAsync_success_allValid() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(null, null); // No existing group
        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(userGroupRepository, times(1)).save(any());
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Success - Partial Failures")
    void bulkCreateUserGroupsAsync_success_partialFailures() {
        // Arrange
        UserGroupRequestModel validRequest = testUserGroupRequest;
        UserGroupRequestModel duplicateRequest = new UserGroupRequestModel();
        duplicateRequest.setGroupName("Duplicate Group");
        duplicateRequest.setDescription("Desc");

        List<UserGroupRequestModel> requests = Arrays.asList(validRequest, duplicateRequest);

        // First call valid, second call duplicate
        lenient().when(userGroupRepository.findByGroupName(validRequest.getGroupName())).thenReturn(null);
        lenient().when(userGroupRepository.findByGroupName(duplicateRequest.getGroupName())).thenReturn(testUserGroup);

        stubUserGroupRepositorySave(testUserGroup);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(userGroupRepository, times(1)).save(any()); // Only 1 saved
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Empty List - Sends Error Message")
    void bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage() {
        // Arrange
        List<UserGroupRequestModel> emptyList = new ArrayList<>();

        // Act
        userGroupService.bulkCreateUserGroupsAsync(emptyList, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Null List - Sends Error Message")
    void bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage() {
        // Act
        userGroupService.bulkCreateUserGroupsAsync(null, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Duplicate Name - Records Failure")
    void bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(testUserGroupRequest.getGroupName(), testUserGroup); // Exists
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(userGroupRepository, never()).save(any());
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Unexpected Exception - Records Failure")
    void bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(null, null);
        lenient().when(userGroupRepository.save(any())).thenThrow(new RuntimeException("Unexpected error"));
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }
}
