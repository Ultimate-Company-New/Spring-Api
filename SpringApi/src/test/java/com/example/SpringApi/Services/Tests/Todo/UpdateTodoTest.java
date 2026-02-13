package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.SuccessMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    // Total Tests: 19
    // ========================================
    // Section 1: Success Tests
    // ========================================

    /*
     * Purpose: Verify update with max length task.
     * Expected Result: Todo is updated.
     * Assertions: method call completes without exception.
     */
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

    /*
     * Purpose: Verify update with special characters in task.
     * Expected Result: Todo is updated.
     * Assertions: method call completes without exception.
     */
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

    /*
     * Purpose: Verify logging is called after successful update.
     * Expected Result: userLogService.logData is called.
     * Assertions: logging call is executed with expected route and message.
     */
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
                contains(SuccessMessages.TodoSuccessMessages.UPDATE_TODO),
                eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM));
    }

    /*
     * Purpose: Verify toggle isDone from true to false.
     * Expected Result: Todo is updated.
     * Assertions: verify save is called.
     */
    @Test
    @DisplayName("updateTodo - Toggle IsDone To False - Success")
    void updateTodo_toggleIsDoneToFalse_success() {
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
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify update with valid request.
     * Expected Result: Todo is updated.
     * Assertions: method call completes without exception.
     */
    @Test
    @DisplayName("updateTodo - Valid Request - Success")
    void updateTodo_validRequest_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated");
        request.setIsDone(false);
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.of(testTodo));
        stubTodoRepositorySave(testTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.updateTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify repository findById is called before save.
     * Expected Result: findById called before save.
     * Assertions: verify order of calls.
     */
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

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    /*
     * Purpose: Verify empty task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK,
     * ex.getMessage())
     */
    @Test
    @DisplayName("updateTodo - Empty Task - BadRequestException")
    void updateTodo_emptyTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("");
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage());
    }

    /*
     * Purpose: Verify max long ID not found.
     * Expected Result: NotFoundException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
     */
    @Test
    @DisplayName("updateTodo - Max Long TodoId - NotFoundException")
    void updateTodo_maxLongTodoId_notFoundException() {
        // Arrange
        stubTodoRepositoryFindById(Long.MAX_VALUE, Optional.empty());

        // Act
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MAX_VALUE);
        request.setTask("task");
        request.setIsDone(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Verify min long ID not found.
     * Expected Result: NotFoundException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
     */
    @Test
    @DisplayName("updateTodo - Min Long TodoId - NotFoundException")
    void updateTodo_minLongTodoId_notFoundException() {
        // Arrange
        stubTodoRepositoryFindById(Long.MIN_VALUE, Optional.empty());

        // Act
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(Long.MIN_VALUE);
        request.setTask("task");
        request.setIsDone(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Verify negative ID not found.
     * Expected Result: NotFoundException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
     */
    @Test
    @DisplayName("updateTodo - Negative TodoId - NotFoundException")
    void updateTodo_negativeTodoId_notFoundException() {
        // Arrange
        stubTodoRepositoryFindById(-1L, Optional.empty());

        // Act
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(-1L);
        request.setTask("task");
        request.setIsDone(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Verify not found throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
     */
    @Test
    @DisplayName("updateTodo - Not Found - NotFoundException")
    void updateTodo_notFound_notFoundException() {
        // Arrange
        stubTodoRepositoryFindById(TEST_TODO_ID, Optional.empty());

        // Act
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("task");
        request.setIsDone(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_ID, ex.getMessage());
    }

    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: assertThrowsBadRequest
     */
    @Test
    @DisplayName("updateTodo - Null Request - BadRequestException")
    void updateTodo_nullRequest_badRequestException() {
        // Arrange
        TodoRequestModel request = null;

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.INVALID_REQUEST,
                () -> todoService.updateTodo(request));
    }

    /*
     * Purpose: Verify null task throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: assertThrowsBadRequest
     */
    @Test
    @DisplayName("updateTodo - Null Task - BadRequestException")
    void updateTodo_nullTask_badRequestException() {
        // Arrange
        TodoRequestModel request = createValidTodoRequest();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.INVALID_TASK,
                () -> todoService.updateTodo(request));
    }

    /*
     * Purpose: Verify null todoId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: assertThrowsBadRequest
     */
    @Test
    @DisplayName("updateTodo - Null TodoId - BadRequestException")
    void updateTodo_nullTodoId_badRequestException() {
        // Arrange
        TodoRequestModel request = createValidTodoRequest();
        request.setTodoId(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.INVALID_REQUEST,
                () -> todoService.updateTodo(request));
    }

    /*
     * Purpose: Verify task too long throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.TASK_TOO_LONG
     */
    @Test
    @DisplayName("updateTodo - Task Too Long - BadRequestException")
    void updateTodo_taskTooLong_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(LONG_TASK);
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.TASK_TOO_LONG, ex.getMessage());
    }

    /*
     * Purpose: Verify whitespace task throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.INVALID_TASK
     */
    @Test
    @DisplayName("updateTodo - Whitespace Task - BadRequestException")
    void updateTodo_whitespaceTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("   ");
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.updateTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage());
    }

    /*
     * Purpose: Verify zero ID not found.
     * Expected Result: NotFoundException.
     * Assertions: message equals ErrorMessages.TodoErrorMessages.NOT_FOUND
     */
    @Test
    @DisplayName("updateTodo - Zero TodoId - NotFoundException")
    void updateTodo_zeroTodoId_notFoundException() {
        // Arrange
        stubTodoRepositoryFindById(0L, Optional.empty());

        // Act
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(0L);
        request.setTask("task");
        request.setIsDone(false);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.updateTodo(request));

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
    @DisplayName("updateTodo - Controller Permission - Unauthorized")
    void updateTodo_controller_permission_unauthorized() {
        // Arrange
        stubTodoServiceUpdateTodoThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = todoController.updateItem(createValidTodoRequest());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(todoServiceMock, times(1)).updateTodo(any(TodoRequestModel.class));
    }

    /*
     * Purpose: Verify @PreAuthorize annotation on controller.
     * Expected Result: Annotation exists and has correct value.
     * Assertions: assertNotNull, assertEquals
     */
    @Test
    @DisplayName("updateTodo - Verify @PreAuthorize Annotation")
    void updateTodo_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = TodoController.class.getMethod("updateItem", TodoRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "updateItem method should have @PreAuthorize annotation");
        assertEquals("@customAuthorization.hasAuthority(null)", annotation.value());
    }
}
