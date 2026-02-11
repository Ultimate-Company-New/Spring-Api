package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Bulk Create User Groups functionality.
 * 
 * Contains 9 tests covering:
 * - Successful bulk creation with varying numbers of groups
 * - Partial success scenarios (some valid, some invalid)
 * - Complete failure scenarios (all groups invalid)
 * - Validation for null/empty lists and duplicate group names
 * - Verification of logging service integration
 */
@DisplayName("UserGroupService - BulkCreateUserGroups Tests")
class BulkCreateUserGroupsTest extends UserGroupServiceTestBase {

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

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("Bulk Create User Groups - All Valid - Success")
    void bulkCreateUserGroups_AllValid_Success() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("BulkGroup" + i);
            groupReq.setDescription("Description for group " + i);
            groupReq.setUserIds(Arrays.asList(1L, 2L));
            userGroups.add(groupReq);
        }

        Map<String, UserGroup> savedGroups = new HashMap<>();
        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        verify(userGroupRepository, times(3)).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Bulk Create User Groups - Many groups - Success")
    void bulkCreateUserGroups_ManyGroups_Success() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("BulkGroup" + i);
            groupReq.setDescription("Description for group " + i);
            groupReq.setUserIds(Arrays.asList(1L, 2L));
            userGroups.add(groupReq);
        }

        Map<String, UserGroup> savedGroups = new HashMap<>();
        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getTotalRequested());
        assertEquals(10, result.getSuccessCount());
    }

    @Test
    @DisplayName("Bulk Create User Groups - Partial Success")
    void bulkCreateUserGroups_PartialSuccess() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();

        UserGroupRequestModel validGroup = new UserGroupRequestModel();
        validGroup.setGroupName("ValidGroup");
        validGroup.setDescription("Valid description");
        validGroup.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(validGroup);

        UserGroupRequestModel invalidGroup = new UserGroupRequestModel();
        invalidGroup.setGroupName("InvalidGroup");
        invalidGroup.setDescription("Invalid description");
        invalidGroup.setUserIds(null); // No users - will fail
        userGroups.add(invalidGroup);

        Map<String, UserGroup> savedGroups = new HashMap<>();
        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create User Groups - Single Group - Success")
    void bulkCreateUserGroups_SingleGroup_Success() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        UserGroupRequestModel groupReq = new UserGroupRequestModel();
        groupReq.setGroupName("SingleGroup");
        groupReq.setDescription("Single group description");
        groupReq.setUserIds(Collections.singletonList(1L));
        userGroups.add(groupReq);

        Map<String, UserGroup> savedGroups = new HashMap<>();
        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create User Groups - Verify logging called")
    void bulkCreateUserGroups_VerifyLoggingCalled() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();
        UserGroupRequestModel groupReq = new UserGroupRequestModel();
        groupReq.setGroupName("TestGroup");
        groupReq.setDescription("Test description");
        groupReq.setUserIds(Collections.singletonList(1L));
        userGroups.add(groupReq);

        Map<String, UserGroup> savedGroups = new HashMap<>();
        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        verify(userLogService).logData(anyLong(), anyString(), anyString());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("Bulk Create User Groups - All failures")
    void bulkCreateUserGroups_AllFailures() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();

        // All groups without userIds - will fail
        for (int i = 0; i < 3; i++) {
            UserGroupRequestModel groupReq = new UserGroupRequestModel();
            groupReq.setGroupName("FailGroup" + i);
            groupReq.setDescription("Description");
            groupReq.setUserIds(null); // No users
            userGroups.add(groupReq);
        }

        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getSuccessCount());
        assertEquals(3, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create User Groups - Duplicate Name")
    void bulkCreateUserGroups_DuplicateName() {
        // Arrange
        List<UserGroupRequestModel> userGroups = new ArrayList<>();

        UserGroupRequestModel group1 = new UserGroupRequestModel();
        group1.setGroupName("NewGroup");
        group1.setDescription("New group");
        group1.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(group1);

        UserGroupRequestModel group2 = new UserGroupRequestModel();
        group2.setGroupName("ExistingGroup");
        group2.setDescription("Existing group");
        group2.setUserIds(Arrays.asList(1L, 2L));
        userGroups.add(group2);

        Map<String, UserGroup> savedGroups = new HashMap<>();
        UserGroup existingGroup = new UserGroup(group2, CREATED_USER, TEST_CLIENT_ID);
        existingGroup.setGroupId(TEST_GROUP_ID);
        savedGroups.put("ExistingGroup", existingGroup);

        stubUserGroupRepositoryForBulkInsert(savedGroups);
        stubUserGroupUserMapRepositorySaveAll(new ArrayList<>());
        stubUserLogServiceLogData(true);

        // Act
        BulkInsertResponseModel<Long> result = userGroupService.bulkCreateUserGroups(userGroups);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create User Groups - Empty List - Throws BadRequestException")
    void bulkCreateUserGroups_EmptyList() {
        // Arrange
        List<UserGroupRequestModel> emptyList = new ArrayList<>();

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.bulkCreateUserGroups(emptyList));
        assertTrue(ex.getMessage()
                .contains(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User group")));
    }

    @Test
    @DisplayName("Bulk Create User Groups - Null List - Throws BadRequestException")
    void bulkCreateUserGroups_NullList() {
        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.bulkCreateUserGroups(null));
        assertTrue(ex.getMessage()
                .contains(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User group")));
    }
}
