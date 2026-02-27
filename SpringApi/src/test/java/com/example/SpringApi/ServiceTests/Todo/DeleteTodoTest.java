package com.example.springapi.ServiceTests.Todo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.SuccessMessages;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.ApiRoutes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for TodoService - DeleteTodo operation. */
@DisplayName("TodoService - DeleteTodo Tests")
class DeleteTodoTest extends TodoServiceTestBase {

  // Total Tests: 12
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify existence check occurs before delete.
   * Expected Result: existsById called, deleteById called.
   * Assertions: verify calls.
   */
  @Test
  @DisplayName("deleteTodo - Check Existence Before Delete - Success")
  void deleteTodo_checkExistenceBeforeDelete_success() {
    // Arrange
    stubTodoRepositoryExistsById(TEST_TODO_ID, true);
    stubTodoRepositoryDeleteById(TEST_TODO_ID);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.deleteTodo(TEST_TODO_ID);

    // Assert
    verify(todoRepository).existsById(TEST_TODO_ID);
    verify(todoRepository).deleteById(TEST_TODO_ID);
  }

  /*
   * Purpose: Verify deleteById is called with correct id.
   * Expected Result: deleteById called once with ID.
   * Assertions: verify deleteById called.
   */
  @Test
  @DisplayName("deleteTodo - Delete By Id Called With Correct Id - Success")
  void deleteTodo_deleteByIdCalledWithCorrectId_success() {
    // Arrange
    stubTodoRepositoryExistsById(TEST_TODO_ID, true);
    stubTodoRepositoryDeleteById(TEST_TODO_ID);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.deleteTodo(TEST_TODO_ID);

    // Assert
    verify(todoRepository).deleteById(TEST_TODO_ID);
  }

  /*
   * Purpose: Verify multiple deletes succeed.
   * Expected Result: deleteById called expected times.
   * Assertions: verify deleteById times.
   */
  @Test
  @DisplayName("deleteTodo - Multiple Deletes - Success")
  void deleteTodo_multipleDeletes_success() {
    // Arrange
    stubTodoRepositoryExistsByIdAny(true);
    stubTodoRepositoryDeleteById(TEST_TODO_ID);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.deleteTodo(TEST_TODO_ID);
    todoService.deleteTodo(TEST_TODO_ID + 1);

    // Assert
    verify(todoRepository, times(2)).deleteById(anyLong());
  }

  /*
   * Purpose: Verify successful delete logs operation.
   * Expected Result: userLogService.logData is called.
   * Assertions: verify userLogService call.
   */
  @Test
  @DisplayName("deleteTodo - Success Logs Operation")
  void deleteTodo_success_logsOperation() {
    // Arrange
    stubTodoRepositoryExistsById(TEST_TODO_ID, true);
    stubTodoRepositoryDeleteById(TEST_TODO_ID);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.deleteTodo(TEST_TODO_ID);

    // Assert
    verify(userLogService)
        .logData(
            eq(TEST_USER_ID.longValue()),
            contains(SuccessMessages.TodoSuccessMessages.DELETE_TODO),
            eq(ApiRoutes.TodoSubRoute.DELETE_ITEM));
  }

  /*
   * Purpose: Verify valid ID deletes successfully.
   * Expected Result: deleteById called.
   * Assertions: verify deleteById called.
   */
  @Test
  @DisplayName("deleteTodo - Valid Id - Success")
  void deleteTodo_validId_success() {
    // Arrange
    long validTodoId = TEST_TODO_ID;
    stubTodoRepositoryExistsById(TEST_TODO_ID, true);
    stubTodoRepositoryDeleteById(TEST_TODO_ID);
    stubUserLogServiceLogDataReturnsTrue();

    // Act
    todoService.deleteTodo(validTodoId);

    // Assert
    verify(todoRepository).existsById(validTodoId);
    verify(todoRepository).deleteById(validTodoId);
    verify(userLogService, atLeastOnce())
        .logData(
            anyLong(),
            org.mockito.ArgumentMatchers.contains("deleted"),
            org.mockito.ArgumentMatchers.anyString());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify max long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
   */
  @Test
  @DisplayName("deleteTodo - Max Long Id - NotFoundException")
  void deleteTodo_maxLongId_notFoundException() {
    // Arrange
    stubTodoRepositoryExistsById(Long.MAX_VALUE, false);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.deleteTodo(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify min long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
   */
  @Test
  @DisplayName("deleteTodo - Min Long Id - NotFoundException")
  void deleteTodo_minLongId_notFoundException() {
    // Arrange
    stubTodoRepositoryExistsById(Long.MIN_VALUE, false);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.deleteTodo(Long.MIN_VALUE));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify negative ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
   */
  @Test
  @DisplayName("deleteTodo - Negative Id - NotFoundException")
  void deleteTodo_negativeId_notFoundException() {
    // Arrange
    stubTodoRepositoryExistsById(-1L, false);

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> todoService.deleteTodo(-1L));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
   */
  @Test
  @DisplayName("deleteTodo - Not Found - NotFoundException")
  void deleteTodo_notFound_notFoundException() {
    // Arrange
    stubTodoRepositoryExistsById(TEST_TODO_ID, false);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> todoService.deleteTodo(TEST_TODO_ID));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify zero ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
   */
  @Test
  @DisplayName("deleteTodo - Zero Id - NotFoundException")
  void deleteTodo_zeroId_notFoundException() {
    // Arrange
    stubTodoRepositoryExistsById(0L, false);

    // Act
    NotFoundException ex = assertThrows(NotFoundException.class, () -> todoService.deleteTodo(0L));

    // Assert
    assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify unauthorized access is blocked at the controller level.
   * Expected Result: Unauthorized status is returned.
   * Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("deleteTodo - Controller Permission - Unauthorized")
  void deleteTodo_controller_permission_unauthorized() {
    // Arrange
    stubTodoServiceDeleteTodoThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = todoController.deleteItem(TEST_TODO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(todoServiceMock, times(1)).deleteTodo(TEST_TODO_ID);
  }
}
