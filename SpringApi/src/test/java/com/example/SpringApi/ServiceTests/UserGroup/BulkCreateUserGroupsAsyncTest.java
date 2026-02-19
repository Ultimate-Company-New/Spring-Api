package com.example.SpringApi.ServiceTests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService.bulkCreateUserGroupsAsync method.
 * 
 */
@DisplayName("UserGroupService - BulkCreateUserGroupsAsync Tests")
class BulkCreateUserGroupsAsyncTest extends UserGroupServiceTestBase {

    // Total Tests: 8
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify all valid groups are created async successfully.
     * Expected Result: Groups saved, message notification created.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Success - All Valid")
    void bulkCreateUserGroupsAsync_success_allValid() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(null, null);
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

    /**
     * Purpose: Verify partial failure handles valid groups and duplicates.
     * Expected Result: Valid group saved, message notification created.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Success - Partial Failures")
    void bulkCreateUserGroupsAsync_success_partialFailures() {
        // Arrange
        UserGroupRequestModel validRequest = testUserGroupRequest;
        UserGroupRequestModel duplicateRequest = new UserGroupRequestModel();
        duplicateRequest.setGroupName("Duplicate Group");
        duplicateRequest.setDescription("Desc");

        List<UserGroupRequestModel> requests = Arrays.asList(validRequest, duplicateRequest);

        stubUserGroupRepositoryFindByGroupNameForPartialFailure(validRequest.getGroupName(),
                duplicateRequest.getGroupName());
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

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify duplicate name does not save but notifies user.
     * Expected Result: message notification created, repo.save never called.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Failure - Duplicate Name")
    void bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(testUserGroupRequest.getGroupName(), testUserGroup);
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(userGroupRepository, never()).save(any());
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify empty list handles gracefully with notification.
     * Expected Result: message notification created.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Failure - Empty List")
    void bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage() {
        // Arrange
        List<UserGroupRequestModel> emptyList = new ArrayList<>();

        // Act
        userGroupService.bulkCreateUserGroupsAsync(emptyList, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify null list handles gracefully with notification.
     * Expected Result: message notification created.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Failure - Null List")
    void bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage() {
        // Arrange

        // Act
        userGroupService.bulkCreateUserGroupsAsync(null, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify unexpected exception during async processing records failure.
     * Expected Result: message notification created.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Failure - Unexpected Exception")
    void bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure() {
        // Arrange
        List<UserGroupRequestModel> requests = Collections.singletonList(testUserGroupRequest);
        stubUserGroupRepositoryFindByGroupName(null, null);
        stubUserGroupRepositorySaveWithException();
        stubUserLogServiceLogDataWithContext(true);

        // Act
        userGroupService.bulkCreateUserGroupsAsync(requests, TEST_USER_ID, "testUser", TEST_CLIENT_ID);

        // Assert
        verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq("testUser"),
                eq(TEST_CLIENT_ID));
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned and @PreAuthorize
     * verified.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
     * assertNotNull, assertTrue
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Controller permission forbidden")
    void bulkCreateUserGroupsAsync_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        List<UserGroupRequestModel> request = Collections.singletonList(testUserGroupRequest);
        stubMockUserGroupServiceGetUserId(TEST_USER_ID);
        stubMockUserGroupServiceGetUser(CREATED_USER);
        stubMockUserGroupServiceGetClientId(TEST_CLIENT_ID);
        stubServiceThrowsUnauthorizedException();
        Method method = UserGroupController.class.getMethod("bulkCreateUserGroups", List.class);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.bulkCreateUserGroups(request);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreateUserGroups method");
        assertTrue(annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called and HTTP 201 is returned.
     * Assertions: verify, HttpStatus.CREATED
     */
    @Test
    @DisplayName("bulkCreateUserGroupsAsync - Controller delegates to service")
    void bulkCreateUserGroupsAsync_withValidRequest_delegatesToService() {
        // Arrange
        List<UserGroupRequestModel> requestList = Collections.singletonList(testUserGroupRequest);
        stubMockUserGroupServiceGetUserId(TEST_USER_ID);
        stubMockUserGroupServiceGetUser(CREATED_USER);
        stubMockUserGroupServiceGetClientId(TEST_CLIENT_ID);
        stubMockUserGroupServiceBulkCreateUserGroupsAsync();

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.bulkCreateUserGroups(requestList);

        // Assert
        verify(mockUserGroupService, times(1)).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
