package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService.addTodo method.
 */
@DisplayName("TodoService - AddTodo Tests")
public class AddTodoTest extends TodoServiceTestBase {


    // Total Tests: 20
    // ========================================
    // Section 1: Success Tests
    // ========================================

    /*
     * Purpose: Verify multiple adds work independently.
     * Expected Result: Each add is saved successfully.
     * Assertions: verify(todoRepository, times(3)).save(any(Todo.class))
     */
    @Test
    @DisplayName("addTodo - Multiple Adds - Work Independently")
    void addTodo_multipleAdds_success() {
        // Arrange
        Todo savedTodo = new Todo();
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        for (int i = 0; i < 3; i++) {
            TodoRequestModel request = new TodoRequestModel();
            request.setTask("Task " + i);
            request.setIsDone(false);
            todoService.addTodo(request);
        }

        // Assert
        verify(todoRepository, times(3)).save(any(Todo.class));
    }

    /*
     * Purpose: Verify numeric task is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Numeric Task - Success")
    void addTodo_numericTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("12345678901234567890");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify repository save is called exactly once.
     * Expected Result: save() called once.
     * Assertions: verify(todoRepository, times(1)).save(any(Todo.class))
     */
    @Test
    @DisplayName("addTodo - Repository Save Called Once - Success")
    void addTodo_repositorySaveCalledOnce_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.addTodo(request);

        // Assert
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    /*
     * Purpose: Verify single character task is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Single Character Task - Success")
    void addTodo_singleCharacterTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("X");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify special characters in task are accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Special Characters In Task - Success")
    void addTodo_specialCharactersInTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test @#$%^&*() Special!!");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify successful addition logs the operation.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...)
     */
    @Test
    @DisplayName("addTodo - Log Operation - Success")
    void addTodo_success_logsOperation() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.addTodo(request);

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.InsertTodo),
                eq(ApiRoutes.TodoSubRoute.ADD_ITEM));
    }

    /*
     * Purpose: Verify task at max length is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Task At Max Length - Success")
    void addTodo_taskAtMaxLength_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("A".repeat(500));
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify task with HTML tags is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Task With Html Tags - Success")
    void addTodo_taskWithHtmlTags_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("<script>alert('test')</script>");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify task with newlines is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Task With Newlines - Success")
    void addTodo_taskWithNewlines_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Line 1\nLine 2\nLine 3");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify task with tabs is accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Task With Tabs - Success")
    void addTodo_taskWithTabs_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Item\t:\tDescription");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify unicode characters in task are accepted.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Unicode Characters In Task - Success")
    void addTodo_unicodeCharactersInTask_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test 你好 🎉 Привет");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify standard add todo succeeds.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - Valid Request - Success")
    void addTodo_validRequest_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    /*
     * Purpose: Verify add todo with isDone=true.
     * Expected Result: Success.
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("addTodo - With isDone True - Success")
    void addTodo_withIsDoneTrue_success() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(true);
        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);
        stubTodoRepositorySave(savedTodo);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.addTodo(request));

        // Assert
        verify(todoRepository).save(any(Todo.class));
    }

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    /*
     * Purpose: Verify empty task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage())
     */
    @Test
    @DisplayName("addTodo - Empty Task - Throws BadRequestException")
    void addTodo_emptyTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("");
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage());
    }

    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException with InvalidRequest message.
     * Assertions: assertThrowsBadRequest
     */
    @Test
    @DisplayName("addTodo - Null Request - Throws BadRequestException")
    void addTodo_nullRequest_badRequestException() {
        // Arrange
        TodoRequestModel request = null;

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.INVALID_REQUEST,
                () -> todoService.addTodo(request));
    }

    /*
     * Purpose: Verify null task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertThrowsBadRequest
     */
    @Test
    @DisplayName("addTodo - Null Task - Throws BadRequestException")
    void addTodo_nullTask_badRequestException() {
        // Arrange
        TodoRequestModel request = createValidTodoRequest();
        request.setTask(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.INVALID_TASK,
                () -> todoService.addTodo(request));
    }

    /*
     * Purpose: Verify task too long throws BadRequestException.
     * Expected Result: BadRequestException with TaskTooLong message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.TASK_TOO_LONG, ex.getMessage())
     */
    @Test
    @DisplayName("addTodo - Task Too Long - Throws BadRequestException")
    void addTodo_taskTooLong_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask(LONG_TASK);
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.TASK_TOO_LONG, ex.getMessage());
    }

    /*
     * Purpose: Verify whitespace task throws BadRequestException.
     * Expected Result: BadRequestException with InvalidTask message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage())
     */
    @Test
    @DisplayName("addTodo - Whitespace Task - Throws BadRequestException")
    void addTodo_whitespaceTask_badRequestException() {
        // Arrange
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("   ");
        request.setIsDone(false);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.INVALID_TASK, ex.getMessage());
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
    @DisplayName("addTodo - Controller Permission - Unauthorized")
    void addTodo_controller_permission_unauthorized() {
        // Arrange
        stubTodoServiceAddTodoThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = todoController.addItem(createValidTodoRequest());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(todoServiceMock, times(1)).addTodo(any(TodoRequestModel.class));
    }

    /*
     * Purpose: Verify @PreAuthorize annotation on controller.
     * Expected Result: Annotation exists and has correct value.
     * Assertions: assertNotNull, assertEquals
     */
    @Test
    @DisplayName("addTodo - Verify @PreAuthorize Annotation")
    void addTodo_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = TodoController.class.getMethod("addItem", TodoRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "addItem method should have @PreAuthorize annotation");
        assertEquals("@customAuthorization.hasAuthority(null)", annotation.value());
    }
}