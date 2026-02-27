package com.example.springapi.ServiceTests.UserGroup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.UserGroupController;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.responsemodels.UserGroupResponseModel;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserGroupService.getUserGroupDetailsById method. */
@DisplayName("UserGroupService - GetUserGroupDetailsById Tests")
class GetUserGroupDetailsByIdTest extends UserGroupServiceTestBase {

  // Total Tests: 11
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify repository is called once. Expected Result: findByIdWithUsers is called exactly
   * once. Assertions: verify
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Success - Repository Called Once")
  void getUserGroupDetailsById_repositoryCalledOnce_success() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

    // Act
    userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

    // Assert
    verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
  }

  /**
   * Purpose: Verify correct fields are returned. Expected Result: Response contains all correct
   * fields. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Success - Correct Fields")
  void getUserGroupDetailsById_returnsCorrectFields_success() {
    // Arrange
    testUserGroup.setDescription("Specific Description");
    stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

    // Act
    UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

    // Assert
    assertEquals(TEST_GROUP_ID, result.getGroupId());
    assertEquals(TEST_GROUP_NAME, result.getGroupName());
    assertEquals("Specific Description", result.getDescription());
  }

  /**
   * Purpose: Verify non-null response. Expected Result: Response and its fields are not null.
   * Assertions: assertNotNull
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Success - Non-null Response")
  void getUserGroupDetailsById_returnsNonNullResponse_success() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

    // Act
    UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getGroupId());
    assertNotNull(result.getGroupName());
  }

  /**
   * Purpose: Verify successful retrieval of user group details. Expected Result:
   * UserGroupResponseModel is returned with correct data. Assertions: assertNotNull, assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Success - Basic Validation")
  void getUserGroupDetailsById_success_basicValidation() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

    // Act
    UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_GROUP_ID, result.getGroupId());
    assertEquals(TEST_GROUP_NAME, result.getGroupName());
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify max long ID not found throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Failure - Max Long ID")
  void getUserGroupDetailsById_maxLongId_throwsNotFoundException() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> userGroupService.getUserGroupDetailsById(Long.MAX_VALUE));
    assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify min long ID not found throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Failure - Min Long ID")
  void getUserGroupDetailsById_minLongId_throwsNotFoundException() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> userGroupService.getUserGroupDetailsById(Long.MIN_VALUE));
    assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative ID not found throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Failure - Negative ID")
  void getUserGroupDetailsById_negativeId_throwsNotFoundException() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userGroupService.getUserGroupDetailsById(-1L));
    assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify group not found throws NotFoundException. Expected Result: NotFoundException
   * with InvalidId message. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Failure - Not Found")
  void getUserGroupDetailsById_notFound_throwsNotFoundException() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID));
    assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero ID not found throws NotFoundException. Expected Result: NotFoundException
   * with InvalidId message. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Failure - Zero ID")
  void getUserGroupDetailsById_zeroId_throwsNotFoundException() {
    // Arrange
    stubUserGroupRepositoryFindByIdWithUsers(null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userGroupService.getUserGroupDetailsById(0L));
    assertEquals(ErrorMessages.UserGroupErrorMessages.INVALID_ID, ex.getMessage());
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
  @DisplayName("getUserGroupDetailsById - Controller permission forbidden")
  void getUserGroupDetailsById_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    stubServiceThrowsUnauthorizedException();
    Method method = UserGroupController.class.getMethod("getUserGroupDetailsById", Long.class);

    // Act
    ResponseEntity<?> response = userGroupControllerWithMock.getUserGroupDetailsById(TEST_GROUP_ID);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(
        annotation, "@PreAuthorize annotation should be present on getUserGroupDetailsById method");
    assertTrue(
        annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
        "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called and
   * HTTP 200 is returned. Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("getUserGroupDetailsById - Controller delegates to service")
  void getUserGroupDetailsById_withValidId_delegatesToService() {
    // Arrange
    stubMockUserGroupServiceGetUserGroupDetailsById(TEST_GROUP_ID, new UserGroupResponseModel());

    // Act
    ResponseEntity<?> response = userGroupControllerWithMock.getUserGroupDetailsById(TEST_GROUP_ID);

    // Assert
    verify(mockUserGroupService).getUserGroupDetailsById(TEST_GROUP_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
