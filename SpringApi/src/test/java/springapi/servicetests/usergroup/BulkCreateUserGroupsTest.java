package springapi.ServiceTests.UserGroup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import springapi.ErrorMessages;
import springapi.controllers.UserGroupController;
import springapi.exceptions.BadRequestException;
import springapi.models.Authorizations;
import springapi.models.databasemodels.UserGroup;
import springapi.models.requestmodels.UserGroupRequestModel;
import springapi.models.responsemodels.BulkInsertResponseModel;

/** Unit tests for UserGroupService.bulkCreateUserGroups method. */
@DisplayName("UserGroupService - BulkCreateUserGroups Tests")
class BulkCreateUserGroupsTest extends UserGroupServiceTestBase {

  // Total Tests: 11
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify all valid groups are created successfully. Expected Result: All groups saved,
   * success count matches total. Assertions: assertEquals, verify
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Success - All Valid")
  void bulkCreateUserGroups_allValid_success() {
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

  /**
   * Purpose: Verify many groups can be bulk created. Expected Result: All 10 groups created
   * successfully. Assertions: assertEquals
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Success - Many Groups")
  void bulkCreateUserGroups_manyGroups_success() {
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

  /**
   * Purpose: Verify partial success scenario (some valid, some invalid). Expected Result: Valid
   * groups created, invalid ones fail. Assertions: assertEquals
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Success - Partial Success")
  void bulkCreateUserGroups_partialSuccess_success() {
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

  /**
   * Purpose: Verify single group can be bulk created. Expected Result: Group created successfully.
   * Assertions: assertEquals
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Success - Single Group")
  void bulkCreateUserGroups_singleGroup_success() {
    // Arrange
    List<UserGroupRequestModel> userGroups = new ArrayList<>();
    UserGroupRequestModel groupReq = new UserGroupRequestModel();
    groupReq.setGroupName("SingleGroup");
    groupReq.setDescription("Single description");
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

  /**
   * Purpose: Verify logging service is called. Expected Result: logData is called. Assertions:
   * verify
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Success - Verify Logging")
  void bulkCreateUserGroups_verifyLogging_success() {
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

  /**
   * Purpose: Verify all failures scenario. Expected Result: No groups created, all fail.
   * Assertions: assertEquals
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Failure - All Failures")
  void bulkCreateUserGroups_allFailures_failure() {
    // Arrange
    List<UserGroupRequestModel> userGroups = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      UserGroupRequestModel groupReq = new UserGroupRequestModel();
      groupReq.setGroupName("FailGroup" + i);
      groupReq.setDescription("Description");
      groupReq.setUserIds(null);
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

  /**
   * Purpose: Verify duplicate name handling in bulk create. Expected Result: New group created,
   * duplicate fails. Assertions: assertEquals
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Failure - Duplicate Name")
  void bulkCreateUserGroups_duplicateName_failure() {
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

  /**
   * Purpose: Verify empty list throws BadRequestException. Expected Result: BadRequestException
   * with ListCannotBeNullOrEmpty message. Assertions: assertThrows, assertTrue
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Failure - Empty List")
  void bulkCreateUserGroups_emptyList_throwsBadRequestException() {
    // Arrange
    List<UserGroupRequestModel> emptyList = new ArrayList<>();

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> userGroupService.bulkCreateUserGroups(emptyList));
    assertTrue(
        ex.getMessage()
            .contains(
                String.format(
                    ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "User group")));
  }

  /**
   * Purpose: Verify null list throws BadRequestException. Expected Result: BadRequestException with
   * ListCannotBeNullOrEmpty message. Assertions: assertThrows, assertTrue
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Failure - Null List")
  void bulkCreateUserGroups_nullList_throwsBadRequestException() {
    // Arrange

    // Act & Assert
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> userGroupService.bulkCreateUserGroups(null));
    assertTrue(
        ex.getMessage()
            .contains(
                String.format(
                    ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "User group")));
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify controller handles unauthorized access via HTTP status. Expected Result: HTTP
   * UNAUTHORIZED status returned and @PreAuthorize verified. Assertions:
   * assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()), assertNotNull, assertTrue
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Controller permission forbidden")
  void bulkCreateUserGroups_controller_permission_forbidden() throws NoSuchMethodException {
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
    assertNotNull(
        annotation, "@PreAuthorize annotation should be present on bulkCreateUserGroups method");
    assertTrue(
        annotation.value().contains(Authorizations.INSERT_GROUPS_PERMISSION),
        "@PreAuthorize annotation should check for INSERT_GROUPS_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called and
   * HTTP 201 is returned. Assertions: verify, HttpStatus.CREATED
   */
  @Test
  @DisplayName("bulkCreateUserGroups - Controller delegates to service")
  void bulkCreateUserGroups_withValidRequest_delegatesToService() {
    // Arrange
    List<UserGroupRequestModel> requestList = Collections.singletonList(testUserGroupRequest);
    stubMockUserGroupServiceGetUserId(TEST_USER_ID);
    stubMockUserGroupServiceGetUser(CREATED_USER);
    stubMockUserGroupServiceGetClientId(TEST_CLIENT_ID);
    stubMockUserGroupServiceBulkCreateUserGroupsAsync();

    // Act
    ResponseEntity<?> response = userGroupControllerWithMock.bulkCreateUserGroups(requestList);

    // Assert
    verify(mockUserGroupService, times(1))
        .bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }
}
