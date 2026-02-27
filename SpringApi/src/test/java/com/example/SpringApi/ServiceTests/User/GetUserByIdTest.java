package com.example.SpringApi.ServiceTests.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import java.lang.reflect.Method;
import java.util.HashSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserService.getUserById method. */
@DisplayName("UserService - GetUserById Tests")
class GetUserByIdTest extends UserServiceTestBase {

  // Total Tests: 10
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify user with no permissions returns empty or null permissions. Expected Result:
   * Empty or null permissions list. Assertions: assertTrue
   */
  @Test
  @DisplayName("getUserById - Success - No Permissions")
  void getUserById_noPermissions_returnsEmptyPermissionsList() {
    // Arrange
    testUser.setUserClientPermissionMappings(new HashSet<>());
    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    UserResponseModel result = userService.getUserById(TEST_USER_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.getPermissions() == null || result.getPermissions().isEmpty());
  }

  /**
   * Purpose: Verify repository is called exactly once. Expected Result: findByIdWithAllRelations
   * called once. Assertions: verify
   */
  @Test
  @DisplayName("getUserById - Success - Repository Called Once")
  void getUserById_repositoryCalledOnce_Success() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    userService.getUserById(TEST_USER_ID);

    // Assert
    verify(userRepository, times(1)).findByIdWithAllRelations(anyLong(), anyLong());
  }

  /**
   * Purpose: Verify user permissions are returned. Expected Result: Permissions list is populated.
   * Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserById - Success - Returns Permissions")
  void getUserById_success_returnsPermissions() {
    // Arrange
    com.example.SpringApi.Models.DatabaseModels.Permission p1 =
        new com.example.SpringApi.Models.DatabaseModels.Permission();
    p1.setPermissionId(1L);
    com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping m1 =
        new com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping();
    m1.setPermission(p1);

    com.example.SpringApi.Models.DatabaseModels.Permission p2 =
        new com.example.SpringApi.Models.DatabaseModels.Permission();
    p2.setPermissionId(2L);
    com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping m2 =
        new com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping();
    m2.setPermission(p2);

    testUser.setUserClientPermissionMappings(new HashSet<>());
    testUser.getUserClientPermissionMappings().add(m1);
    testUser.getUserClientPermissionMappings().add(m2);

    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    UserResponseModel result = userService.getUserById(TEST_USER_ID);

    // Assert
    assertNotNull(result.getPermissions());
    assertEquals(2, result.getPermissions().size());
  }

  /**
   * Purpose: Verify successful user retrieval by ID. Expected Result: UserResponseModel is returned
   * with correct data. Assertions: assertEquals
   */
  @Test
  @DisplayName("getUserById - Success - Returns User Details")
  void getUserById_success_returnsUserWithDetails() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(testUser);

    // Act
    UserResponseModel result = userService.getUserById(TEST_USER_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_USER_ID, result.getUserId());
    assertEquals(TEST_EMAIL, result.getEmail());
    assertEquals(TEST_LOGIN_NAME, result.getLoginName());
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify max long ID throws NotFoundException when not found. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("getUserById - Failure - Max Long ID")
  void getUserById_maxLongId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.getUserById(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify negative ID throws NotFoundException. Expected Result: NotFoundException with
   * InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("getUserById - Failure - Negative ID")
  void getUserById_negativeId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.getUserById(-1L));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify that non-existent user throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("getUserById - Failure - User Not Found")
  void getUserById_userNotFound_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.getUserById(TEST_USER_ID));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify zero ID throws NotFoundException. Expected Result: NotFoundException with
   * InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("getUserById - Failure - Zero ID")
  void getUserById_zeroId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.getUserById(0L));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
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
  @DisplayName("getUserById - Controller permission forbidden")
  void getUserById_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    stubMockUserServiceGetUserByIdThrowsUnauthorized(null);
    Method method = UserController.class.getMethod("getUserById", Long.class);

    // Act
    ResponseEntity<?> response = userControllerWithMock.getUserById(TEST_USER_ID);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(annotation, "getUserById method should have @PreAuthorize annotation");
    assertTrue(
        annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
        "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called.
   * Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("getUserById - Controller delegates to service")
  void getUserById_withValidId_delegatesToService() {
    // Arrange
    Long userId = 1L;
    stubMockUserServiceGetUserById(userId, new UserResponseModel());

    // Act
    ResponseEntity<?> response = userControllerWithMock.getUserById(userId);

    // Assert
    verify(mockUserService, times(1)).getUserById(userId);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

