package com.example.SpringApi.ServiceTests.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.User;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserService.toggleUser method. */
@DisplayName("UserService - ToggleUser Tests")
class ToggleUserTest extends UserServiceTestBase {

  // Total Tests: 11
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify that multiple toggles persist state correctly. Expected Result: User state
   * toggles between deleted and active. Assertions: State alternates correctly.
   */
  @Test
  @DisplayName("toggleUser - Success - Multiple Toggles")
  void toggleUser_multipleToggles_statePersists() {
    // Arrange
    testUser.setIsDeleted(false);
    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserLogServiceLogData(true);

    // Act & Assert 1
    userService.toggleUser(TEST_USER_ID);
    assertTrue(testUser.getIsDeleted());

    // Act & Assert 2
    userService.toggleUser(TEST_USER_ID);
    assertFalse(testUser.getIsDeleted());

    // Act & Assert 3
    userService.toggleUser(TEST_USER_ID);
    assertTrue(testUser.getIsDeleted());
  }

  /**
   * Purpose: Verify that user log is called after successful toggle. Expected Result:
   * userLogService.logData is called. Assertions: verify
   */
  @Test
  @DisplayName("toggleUser - Success - Logs Operation")
  void toggleUser_success_logsOperation() {
    // Arrange
    testUser.setIsDeleted(false);
    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserLogServiceLogData(true);

    // Act
    userService.toggleUser(TEST_USER_ID);

    // Assert
    verify(userLogService, times(1)).logData(anyLong(), contains("deletion status"), anyString());
  }

  /**
   * Purpose: Verify that a deleted user can be restored. Expected Result: The user's isDeleted
   * status is set to false. Assertions: assertFalse
   */
  @Test
  @DisplayName("toggleUser - Success - Restores Deleted User")
  void toggleUser_success_restoresDeletedUser() {
    // Arrange
    testUser.setIsDeleted(true);
    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserLogServiceLogData(true);

    // Act
    userService.toggleUser(TEST_USER_ID);

    // Assert
    assertFalse(testUser.getIsDeleted());
    verify(userRepository, atLeastOnce()).save(testUser);
  }

  /**
   * Purpose: Verify that a user can be successfully toggled (soft deleted). Expected Result: The
   * user's isDeleted status is set to true. Assertions: assertTrue
   */
  @Test
  @DisplayName("toggleUser - Success - Soft Delete")
  void toggleUser_success_setsIsDeletedTrue() {
    // Arrange
    testUser.setIsDeleted(false);
    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserLogServiceLogData(true);

    // Act
    userService.toggleUser(TEST_USER_ID);

    // Assert
    assertTrue(testUser.getIsDeleted());
    verify(userRepository, atLeastOnce()).save(testUser);
  }

  /**
   * Purpose: Verify that modifiedUser is updated on toggle. Expected Result: modifiedUser field is
   * updated. Assertions: assertNotNull
   */
  @Test
  @DisplayName("toggleUser - Success - Updates Modified User")
  void toggleUser_success_updatesModifiedUser() {
    // Arrange
    testUser.setIsDeleted(false);
    stubUserRepositoryFindByIdWithAllRelations(testUser);
    stubUserRepositorySave(testUser);
    stubUserLogServiceLogData(true);

    // Act
    userService.toggleUser(TEST_USER_ID);

    // Assert
    assertNotNull(testUser.getModifiedUser());
    verify(userRepository, atLeastOnce()).save(testUser);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify that toggling user with max long ID throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("toggleUser - Failure - Max Long ID")
  void toggleUser_maxLongId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.toggleUser(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify that toggling user with negative ID throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("toggleUser - Failure - Negative ID")
  void toggleUser_negativeId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.toggleUser(-1L));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify that toggling a non-existent user throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("toggleUser - Failure - User Not Found")
  void toggleUser_userNotFound_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> userService.toggleUser(TEST_USER_ID));

    // Assert
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  /**
   * Purpose: Verify that toggling user with zero ID throws NotFoundException. Expected Result:
   * NotFoundException with InvalidId message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("toggleUser - Failure - Zero ID")
  void toggleUser_zeroId_throwsNotFoundException() {
    // Arrange
    stubUserRepositoryFindByIdWithAllRelations(null);

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.toggleUser(0L));

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
  @DisplayName("toggleUser - Controller permission forbidden")
  void toggleUser_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    stubMockUserServiceToggleUserThrowsUnauthorized(null);
    Method method = UserController.class.getMethod("toggleUser", Long.class);

    // Act
    ResponseEntity<?> response = userControllerWithMock.toggleUser(TEST_USER_ID);
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(annotation, "toggleUser method should have @PreAuthorize annotation");
    assertTrue(
        annotation.value().contains(Authorizations.DELETE_USER_PERMISSION),
        "@PreAuthorize annotation should check for DELETE_USER_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called.
   * Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("toggleUser - Controller delegates to service")
  void toggleUser_withValidId_delegatesToService() {
    // Arrange
    Long userId = 1L;
    stubMockUserServiceToggleUser(userId);

    // Act
    ResponseEntity<?> response = userControllerWithMock.toggleUser(userId);

    // Assert
    verify(mockUserService, times(1)).toggleUser(userId);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

