package com.example.SpringApi.ServiceTests.Todo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for TodoService.getTodoItems method. */
@DisplayName("TodoService - GetTodoItems Tests")
class GetTodoItemsTest extends TodoServiceTestBase {

  // Total Tests: 14
  // ========================================
  // Section 1: Success Tests
  // ========================================

  /*
   * Purpose: Verify empty list returns successfully.
   * Expected Result: Empty list.
   * Assertions: result.isEmpty()
   */
  @Test
  @DisplayName("getTodoItems - Empty List - Success")
  void getTodoItems_emptyList_success() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(new ArrayList<>());
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertTrue(result.isEmpty());
  }

  /*
   * Purpose: Verify many todos are returned.
   * Expected Result: All todos returned.
   * Assertions: result size equals input.
   */
  @Test
  @DisplayName("getTodoItems - Many Todos - Success")
  void getTodoItems_manyTodos_success() {
    // Arrange
    List<Todo> todos = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      Todo todo = createTestTodo();
      todo.setTodoId((long) i);
      todos.add(todo);
    }
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(todos);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertEquals(20, result.size());
  }

  /*
   * Purpose: Verify entity fields map into response.
   * Expected Result: Response contains expected values.
   * Assertions: todoId and task match.
   */
  @Test
  @DisplayName("getTodoItems - Maps fields - Success")
  void getTodoItems_mapsFields_success() {
    // Arrange
    Todo todo = createTestTodo();
    todo.setTodoId(TEST_TODO_ID);
    todo.setTask("Mapped Task");
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(todo));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertEquals(1, result.size());
    assertEquals(TEST_TODO_ID, result.get(0).getTodoId());
    assertEquals("Mapped Task", result.get(0).getTask());
  }

  /*
   * Purpose: Verify mixed isDone values are preserved.
   * Expected Result: Responses reflect mixed values.
   * Assertions: isDone values match.
   */
  @Test
  @DisplayName("getTodoItems - Mixed IsDone Values - Success")
  void getTodoItems_mixedIsDoneValues_success() {
    // Arrange
    Todo todo1 = createTestTodo();
    todo1.setTodoId(1L);
    todo1.setIsDone(true);
    Todo todo2 = createTestTodo();
    todo2.setTodoId(2L);
    todo2.setIsDone(false);
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(todo1, todo2));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertTrue(result.get(0).getIsDone());
    assertFalse(result.get(1).getIsDone());
  }

  /*
   * Purpose: Verify multiple todos returned.
   * Expected Result: Result size matches count.
   * Assertions: size equals 2.
   */
  @Test
  @DisplayName("getTodoItems - Multiple Todos - Success")
  void getTodoItems_multipleTodos_success() {
    // Arrange
    Todo todo1 = createTestTodo();
    todo1.setTodoId(1L);
    Todo todo2 = createTestTodo();
    todo2.setTodoId(2L);
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(todo1, todo2));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertEquals(2, result.size());
  }

  /*
   * Purpose: Verify repository called once.
   * Expected Result: findAllByUserIdOrderByTodoIdDesc called once.
   * Assertions: verify call count.
   */
  @Test
  @DisplayName("getTodoItems - Repository Called Once - Success")
  void getTodoItems_repositoryCalledOnce_success() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(testTodo));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.getTodoItems();

    // Assert
    verify(todoRepository, times(1)).findAllByUserIdOrderByTodoIdDesc(TEST_USER_ID);
  }

  /*
   * Purpose: Verify success logs operation.
   * Expected Result: userLogService.logData called.
   * Assertions: verify logData call.
   */
  @Test
  @DisplayName("getTodoItems - Success Logs Operation")
  void getTodoItems_success_logsOperation() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(testTodo));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.getTodoItems();

    // Assert
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /*
   * Purpose: Verify basic success returns todos.
   * Expected Result: Result contains todos.
   * Assertions: size equals 1.
   */
  @Test
  @DisplayName("getTodoItems - Success Returns Todos")
  void getTodoItems_success_returnsTodos() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(testTodo));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertEquals(1, result.size());
  }

  /*
   * Purpose: Verify basic success case returns todos.
   * Expected Result: Result contains todos.
   * Assertions: assertNotNull, size equals 1.
   */
  @Test
  @DisplayName("getTodoItems - Success - returns todos (basic)")
  void getTodoItems_success_returnsTodos_basic() {
    // Arrange
    List<Todo> todos = List.of(testTodo);
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(todos);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    var result = todoService.getTodoItems();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  /*
   * Purpose: Verify special characters are preserved.
   * Expected Result: Special characters in task preserved.
   * Assertions: tasks match.
   */
  @Test
  @DisplayName("getTodoItems - Various Special Characters - Success")
  void getTodoItems_variousSpecialCharacters_success() {
    // Arrange
    List<Todo> specialTodos = new ArrayList<>();
    Todo todo1 = createTestTodo();
    todo1.setTodoId(1L);
    todo1.setTask("@#$%^&*()");
    specialTodos.add(todo1);
    Todo todo2 = createTestTodo();
    todo2.setTodoId(2L);
    todo2.setTask("你好世界");
    specialTodos.add(todo2);
    Todo todo3 = createTestTodo();
    todo3.setTodoId(3L);
    todo3.setTask("<html>");
    specialTodos.add(todo3);
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(specialTodos);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    List<TodoResponseModel> result = todoService.getTodoItems();

    // Assert
    assertEquals(3, result.size());
    assertEquals("@#$%^&*()", result.get(0).getTask());
    assertEquals("你好世界", result.get(1).getTask());
    assertEquals("<html>", result.get(2).getTask());
  }

  /*
   * Purpose: Verify repository call for getTodoItems.
   * Expected Result: repository called with user id.
   * Assertions: verify repository called.
   */
  @Test
  @DisplayName("getTodoItems - Verify Repository Call - Success")
  void getTodoItems_verifyRepositoryCall_success() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List.of(testTodo));
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.getTodoItems();

    // Assert
    verify(todoRepository).findAllByUserIdOrderByTodoIdDesc(TEST_USER_ID);
  }

  // ========================================
  // Section 2: Failure / Exception Tests
  // ========================================

  /*
   * Purpose: Verify null from repository is handled.
   * Expected Result: BadRequestException is thrown.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_REQUEST
   */
  @Test
  @DisplayName("getTodoItems - Null From Repository - Throws BadRequestException")
  void getTodoItems_nullFromRepository_exception() {
    // Arrange
    stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(null);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> todoService.getTodoItems());

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_REQUEST, ex.getMessage());
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
  @DisplayName("getTodoItems - Controller Permission - Unauthorized")
  void getTodoItems_controller_permission_unauthorized() {
    // Arrange
    stubTodoServiceGetTodoItemsThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = todoController.getTodoItems();

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(todoServiceMock, times(1)).getTodoItems();
  }
}

