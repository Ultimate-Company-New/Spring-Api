package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.SuccessMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService.addTodo method.
 * 
 * Test count: 18 tests
 * - SUCCESS: 13 tests
 * - FAILURE / EXCEPTION: 5 tests
 */
@DisplayName("TodoService - AddTodo Tests")
public class AddTodoTest extends TodoServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Add Todo - Success")
    void addTodo_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Add Todo - Task at max length - Success")
    void addTodo_TaskAtMaxLength_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("A".repeat(500));
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - With isDone true - Success")
    void addTodo_WithIsDoneTrue_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(true);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("Add Todo - Special characters in task - Success")
    void addTodo_SpecialCharactersInTask_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test @#$%^&*() Special!!");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Unicode characters in task - Success")
    void addTodo_UnicodeCharactersInTask_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test 你好 🎉 Привет");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Success - Logs the operation")
    void addTodo_Success_LogsOperation() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        todoService.addTodo(request);

        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.InsertTodo),
                eq(ApiRoutes.TodoSubRoute.ADD_ITEM)
        );
    }

    @Test
    @DisplayName("Add Todo - Numeric task - Success")
    void addTodo_NumericTask_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("12345678901234567890");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Single character task - Success")
    void addTodo_SingleCharacterTask_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("X");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Repository save called once")
    void addTodo_RepositorySaveCalledOnce() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        todoService.addTodo(request);

        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    @DisplayName("Add Todo - Task with newlines - Success")
    void addTodo_TaskWithNewlines_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Line 1\nLine 2\nLine 3");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Task with tabs - Success")
    void addTodo_TaskWithTabs_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Item\t:\tDescription");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Task with HTML tags - Success")
    void addTodo_TaskWithHtmlTags_Success() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("<script>alert('test')</script>");
        request.setIsDone(false);

        Todo savedTodo = new Todo(request, CREATED_USER, TEST_USER_ID);
        savedTodo.setTodoId(1L);

        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Multiple adds work independently")
    void addTodo_MultipleAdds_WorkIndependently() {
        Todo savedTodo = new Todo();
        savedTodo.setTodoId(1L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        for (int i = 0; i < 3; i++) {
            TodoRequestModel request = new TodoRequestModel();
            request.setTask("Task " + i);
            request.setIsDone(false);
            todoService.addTodo(request);
        }

        verify(todoRepository, times(3)).save(any(Todo.class));
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Add Todo - Null request - Throws BadRequestException")
    void addTodo_NullRequest_ThrowsBadRequestException() {
        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.InvalidRequest,
                () -> todoService.addTodo(null));
    }

    @Test
    @DisplayName("Add Todo - Null task - Throws BadRequestException")
    void addTodo_NullTask_ThrowsBadRequestException() {
        TodoRequestModel request = createValidTodoRequest();
        request.setTask(null);

        assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.InvalidTask,
                () -> todoService.addTodo(request));
    }

    @Test
    @DisplayName("Add Todo - Empty task - Throws BadRequestException")
    void addTodo_EmptyTask_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("");
        request.setIsDone(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    @Test
    @DisplayName("Add Todo - Whitespace task - Throws BadRequestException")
    void addTodo_WhitespaceTask_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("   ");
        request.setIsDone(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
    }

    @Test
    @DisplayName("Add Todo - Task too long - Throws BadRequestException")
    void addTodo_TaskTooLong_ThrowsBadRequestException() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask(LONG_TASK);
        request.setIsDone(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> todoService.addTodo(request));
        assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
    }
}
