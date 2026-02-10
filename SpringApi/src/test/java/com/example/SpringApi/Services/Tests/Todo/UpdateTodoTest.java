package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - UpdateTodo operation.
 * Contains 17 tests: 6 SUCCESS + 11 FAILURE/EXCEPTION cases.
 */
@DisplayName("TodoService - UpdateTodo Tests")
public class UpdateTodoTest extends TodoServiceTestBase {

    // Total Tests: 17

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify repository findById is called before save.
     * Expected Result: findById called before save.
     * Assertions: verify order of calls.
     */
    @Test
    @DisplayName("Update Todo - Calls findById before save")
    void updateTodo_CallsFindByIdBeforeSave() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated");
        request.setIsDone(false);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.updateTodo(request);

        // Assert
        var inOrder = inOrder(todoRepository);
        inOrder.verify(todoRepository).findById(TEST_TODO_ID);
        inOrder.verify(todoRepository).save(any(Todo.class));
    }

    /**
     * Purpose: Verify update with max length task.
     * Expected Result: Todo is updated.
     * Assertions: assertDoesNotThrow();
     */
    @Test
    @DisplayName("Update Todo - Max length task - Success")
    void updateTodo_MaxLengthTask_Success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("A".repeat(500));
        request.setIsDone(false);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> todoService.updateTodo(request));
    }

    /**
     * Purpose: Verify update with special characters in task.
     * Expected Result: Todo is updated.
     * Assertions: assertDoesNotThrow();
     */
    @Test
    @DisplayName("Update Todo - Special chars in task - Success")
    void updateTodo_SpecialCharsInTask_Success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated @#$%^&*()!");
        request.setIsDone(true);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> todoService.updateTodo(request));
    }

    /**
     * Purpose: Verify successful todo update.
     * Expected Result: Todo is updated and logged.
     * Assertions: assertDoesNotThrow();
     * verify(todoRepository).save(any(Todo.class));
     */
    @Test
    @DisplayName("Update Todo - Success")
    void updateTodo_Success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository).save(any(Todo.class));
    }

    /**
     * Purpose: Verify logging is called after successful update.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Update Todo - Success - Logs the operation")
    void updateTodo_Success_LogsOperation() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);
        updatedTodo.setTodoId(TEST_TODO_ID);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.updateTodo(request);

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.UpdateTodo),
                eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM));
    }

    /**
     * Purpose: Verify update toggling isDone to false.
     * Expected Result: Todo is updated with isDone=false.
     * Assertions: assertDoesNotThrow();
     */
    @Test
    @DisplayName("Update Todo - Toggle isDone to false")
    void updateTodo_ToggleIsDoneToFalse() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Task");
        request.setIsDone(false);
        testTodo.setIsDone(true);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> todoService.updateTodo(request));
    }

    // ========================================
    // FAILURE/EXCEPTION TESTS
    // ========================================

    /**
     * Purpose: Verify empty task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Empty task - Throws BadRequestException")
    void updateTodo_EmptyTask_ThrowsBadRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("");
        request.setIsDone(false);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    /**
     * Purpose: Verify max long todoId throws NotFoundException when not found.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Max Long todoId - Throws NotFoundException")
    void updateTodo_MaxLongTodoId_ThrowsNotFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MAX_VALUE);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        when(todoRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify min long todoId throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Min Long todoId - Throws NotFoundException")
    void updateTodo_MinLongTodoId_ThrowsNotFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MIN_VALUE);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        when(todoRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative todoId throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Negative todoId - Throws NotFoundException")
    void updateTodo_NegativeTodoId_ThrowsNotFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(-1L);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        when(todoRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify non-existent todo throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Not found - Throws NotFoundException")
    void updateTodo_NotFound_ThrowsNotFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        verify(todoRepository, never()).save(any());
    }

    /**
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Null request - Throws BadRequestException")
    void updateTodo_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(null));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify null task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Null task - Throws BadRequestException")
    void updateTodo_NullTask_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(null);
        request.setIsDone(false);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    /**
     * Purpose: Verify null todoId throws BadRequestException.
     * Expected Result: BadRequestException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Null todoId - Throws BadRequestException")
    void updateTodo_NullTodoId_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(null);
        request.setTask(TEST_TASK);
        request.setIsDone(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify task too long throws BadRequestException.
     * Expected Result: BadRequestException with TaskTooLong message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Task too long - Throws BadRequestException")
    void updateTodo_TaskTooLong_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(LONG_TASK);
        request.setIsDone(false);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Whitespace task - Throws BadRequestException")
    void updateTodo_WhitespaceTask_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("   ");
        request.setIsDone(false);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    /**
     * Purpose: Verify zero todoId throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Update Todo - Zero todoId - Throws NotFoundException")
    void updateTodo_ZeroTodoId_ThrowsNotFoundException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(0L);
        request.setTask(TEST_TASK);
        request.setIsDone(false);

        lenient().when(todoRepository.findById(0L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }
}
