package springapi.ServiceTests.Todo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.SuccessMessages;
import springapi.exceptions.NotFoundException;
import springapi.models.ApiRoutes;

/** Test class for TodoService - ToggleTodo operation. */
@DisplayName("TodoService - ToggleTodo Tests")
class ToggleTodoTest extends TodoServiceTestBase {

  // Total Tests: 15
  // ========================================
  // Section 1: Success Tests
  // ========================================

  /*
   * Purpose: Verify false to true toggle.
   * Expected Result: isDone toggles to true.
   * Assertions: isDone is true.
   */
  @Test
  @DisplayName("toggleTodo - False To True - Success")
  void toggleTodo_falseToTrue_success() {
    // Arrange
    testTodo.setIsDone(false);
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    assertTrue(testTodo.getIsDone());
  }

  /*
   * Purpose: Verify findById called once.
   * Expected Result: findById called once.
   * Assertions: verify call count.
   */
  @Test
  @DisplayName("toggleTodo - FindById Called Once - Success")
  void toggleTodo_findByIdCalledOnce_success() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    verify(todoRepository, times(1)).findById(TEST_TODO_ID);
  }

  /*
   * Purpose: Verify multiple toggles maintain state.
   * Expected Result: state returns to original after even toggles.
   * Assertions: isDone equals original state.
   */
  @Test
  @DisplayName("toggleTodo - Multiple Toggles - State Persists")
  void toggleTodo_multipleToggles_statePersists() {
    // Arrange
    testTodo.setIsDone(false);
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    assertFalse(testTodo.getIsDone());
  }

  /*
   * Purpose: Verify repository save is called.
   * Expected Result: save called once.
   * Assertions: verify save called.
   */
  @Test
  @DisplayName("toggleTodo - Repository Save Called - Success")
  void toggleTodo_repositorySaveCalled_success() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    verify(todoRepository, times(1)).save(testTodo);
  }

  /*
   * Purpose: Verify successful toggle logs operation.
   * Expected Result: userLogService.logData called.
   * Assertions: verify logData call.
   */
  @Test
  @DisplayName("toggleTodo - Success Logs Operation")
  void toggleTodo_success_logsOperation() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    verify(userLogService, times(1))
        .logData(
            eq(TEST_USER_ID.longValue()),
            contains(SuccessMessages.TodoSuccessMessages.TOGGLE_TODO),
            eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE));
  }

  /*
   * Purpose: Verify true to false toggle.
   * Expected Result: isDone toggles to false.
   * Assertions: isDone is false.
   */
  @Test
  @DisplayName("toggleTodo - True To False - Success")
  void toggleTodo_trueToFalse_success() {
    // Arrange
    testTodo.setIsDone(true);
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    assertFalse(testTodo.getIsDone());
  }

  /*
   * Purpose: Verify modifiedUser updated.
   * Expected Result: modifiedUser set.
   * Assertions: modifiedUser is not null.
   */
  @Test
  @DisplayName("toggleTodo - Updates Modified User - Success")
  void toggleTodo_updatesModifiedUser_success() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    assertNotNull(testTodo.getModifiedUser());
  }

  /*
   * Purpose: Verify modifiedUser string contains expected value.
   * Expected Result: modifiedUser equals default created user.
   * Assertions: modifiedUser matches.
   */
  @Test
  @DisplayName("toggleTodo - Verify Specific Modified User String - Success")
  void toggleTodo_verifySpecificModifiedUserString_success() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
    stubTodoRepositorySave(testTodo);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.toggleTodo(TEST_TODO_ID);

    // Assert
    assertEquals(CREATED_USER, testTodo.getModifiedUser());
  }

  // ========================================
  // Section 2: Failure / Exception Tests
  // ========================================

  /*
   * Purpose: Verify max long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_ID
   */
  @Test
  @DisplayName("toggleTodo - Max Long Id - NotFoundException")
  void toggleTodo_maxLongId_notFoundException() {
    // Arrange
    stubTodoRepositoryFindById(Long.MAX_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.toggleTodo(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify min long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_ID
   */
  @Test
  @DisplayName("toggleTodo - Min Long Id - NotFoundException")
  void toggleTodo_minLongId_notFoundException() {
    // Arrange
    stubTodoRepositoryFindById(Long.MIN_VALUE, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.toggleTodo(Long.MIN_VALUE));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify negative ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_ID
   */
  @Test
  @DisplayName("toggleTodo - Negative Id - NotFoundException")
  void toggleTodo_negativeId_notFoundException() {
    // Arrange
    stubTodoRepositoryFindById(-1L, Optional.empty());

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> todoService.toggleTodo(-1L));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_ID
   */
  @Test
  @DisplayName("toggleTodo - Not Found - NotFoundException")
  void toggleTodo_notFound_notFoundException() {
    // Arrange
    stubTodoRepositoryFindById(TEST_TODO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.toggleTodo(TEST_TODO_ID));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify zero ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_ID
   */
  @Test
  @DisplayName("toggleTodo - Zero Id - NotFoundException")
  void toggleTodo_zeroId_notFoundException() {
    // Arrange
    stubTodoRepositoryFindById(0L, Optional.empty());

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> todoService.toggleTodo(0L));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  // ========================================
  // Section 3: Controller Permission/Auth Tests
  // ========================================

  /*
   * Purpose: Verify unauthorized access is blocked at the controller level.
   * Expected Result: Unauthorized status is returned.
   * Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("toggleTodo - Controller Permission - Unauthorized")
  void toggleTodo_controller_permission_unauthorized() {
    // Arrange
    stubTodoServiceToggleTodoThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = todoController.toggleTodo(TEST_TODO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(todoServiceMock, times(1)).toggleTodo(TEST_TODO_ID);
  }
}
