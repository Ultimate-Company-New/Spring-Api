package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - UpdateTodo operation.
 */
@DisplayName("TodoService - UpdateTodo Tests")
class UpdateTodoTest extends TodoServiceTestBase {

    // Total Tests: 18

    // ========================================
    // Section 1: Success Tests
    // ========================================

    // Purpose: Verify repository findById is called before save.
    // Expected Result: findById called before save.
    // Assertions: verify order of calls.
    @Test
    @DisplayName("updateTodo - Verify FindById Called Before Save - Success")
    void updateTodo_verifyFindByIdCalled_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated");
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(testTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.updateTodo(request);

        // Assert
        var inOrder = inOrder(todoRepository);
        inOrder.verify(todoRepository).findById(TEST_TODO_ID);
        inOrder.verify(todoRepository).save(any(Todo.class));
    }

    // Purpose: Verify update with max length task.
    // Expected Result: Todo is updated.
    // Assertions: assertDoesNotThrow();
    @Test
    @DisplayName("updateTodo - Max Length Task - Success")
    void updateTodo_maxLengthTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("A".repeat(500));
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(testTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    // Purpose: Verify update with special characters in task.
    // Expected Result: Todo is updated.
    // Assertions: assertDoesNotThrow();
    @Test
    @DisplayName("updateTodo - Special Chars In Task - Success")
    void updateTodo_specialCharsInTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated @#$%^&*()!");
        request.setIsDone(true);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(testTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    // Purpose: Verify logging is called after successful update.
    // Expected Result: userLogService.logData is called.
    // Assertions: verify(userLogService).logData(...);
    @Test
    @DisplayName("updateTodo - Logs Operation - Success")
    void updateTodo_success_logsOperation() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);
        updatedTodo.setTodoId(TEST_TODO_ID);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(updatedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.updateTodo(request);

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.UpdateTodo),
                eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM));
    }

    // Purpose: Verify update toggling isDone to false.
    // Expected Result: Todo is updated with isDone=false.
    // Assertions: assertDoesNotThrow();
    @Test
    @DisplayName("updateTodo - Toggle isDone To False - Success")
    void updateTodo_toggleIsDoneToFalse_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Task");
        request.setIsDone(false);
        testTodo.setIsDone(true);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(testTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    // Purpose: Verify successful todo update.
    // Expected Result: Todo is updated and logged.
    // Assertions: assertDoesNotThrow(); verify(todoRepository).save(any(Todo.class));
    @Test
    @DisplayName("updateTodo - Valid Request - Success")
    void updateTodo_validRequest_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(updatedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository).save(any(Todo.class));
    }

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    // Purpose: Verify empty task throws BadRequestException.
    // Expected Result: BadRequestException with InvalidTask message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Empty Task - Throws BadRequestException")
    void updateTodo_emptyTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("");
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    // Purpose: Verify max long todoId throws NotFoundException when not found.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Max Long TodoId - Throws NotFoundException")
    void updateTodo_maxLongTodoId_notFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MAX_VALUE);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        stubTodoRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify min long todoId throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Min Long TodoId - Throws NotFoundException")
    void updateTodo_minLongTodoId_notFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MIN_VALUE);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        stubTodoRepositoryFindById(Long.MIN_VALUE, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify negative todoId throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Negative TodoId - Throws NotFoundException")
    void updateTodo_negativeTodoId_notFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(-1L);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        stubTodoRepositoryFindById(-1L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify non-existent todo throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Not Found - Throws NotFoundException")
    void updateTodo_notFound_notFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        verify(todoRepository, never()).save(any());
    }

    // Purpose: Verify null request throws BadRequestException.
    // Expected Result: BadRequestException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Null Request - Throws BadRequestException")
    void updateTodo_nullRequest_badRequestException() {
        // Arrange
        TodoRequestModel request = null;

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify null task throws BadRequestException.
    // Expected Result: BadRequestException with InvalidTask message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Null Task - Throws BadRequestException")
    void updateTodo_nullTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(null);
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    // Purpose: Verify null todoId throws BadRequestException.
    // Expected Result: BadRequestException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Null TodoId - Throws BadRequestException")
    void updateTodo_nullTodoId_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(null);
        request.setTask(TEST_TASK);
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify task too long throws BadRequestException.
    // Expected Result: BadRequestException with TaskTooLong message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Task Too Long - Throws BadRequestException")
    void updateTodo_taskTooLong_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(LONG_TASK);
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
    }

    // Purpose: Verify whitespace task throws BadRequestException.
    // Expected Result: BadRequestException with InvalidTask message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Whitespace Task - Throws BadRequestException")
    void updateTodo_whitespaceTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("   ");
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    // Purpose: Verify zero todoId throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("updateTodo - Zero TodoId - Throws NotFoundException")
    void updateTodo_zeroTodoId_notFoundException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(0L);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        stubTodoRepositoryFindById(0L, Optional.empty());

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // ========================================
    // Section 3: Controller Permission/Auth Tests
    // ========================================

    // Purpose: Verify @PreAuthorize Annotation on controller
    // Expected Result: Annotation exists and has correct value
    // Assertions: assertNotNull, assertEquals
    @Test
    @DisplayName("updateItem - Verify @PreAuthorize Annotation")
    void updateItem_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = TodoController.class.getMethod("updateItem", TodoRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "updateItem method should have @PreAuthorize annotation");
        assertEquals("@customAuthorization.hasAuthority(null)", annotation.value());
    }
}