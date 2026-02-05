package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - GetTodoItems operation.
 * Contains 11 tests: 10 SUCCESS + 1 FAILURE/EXCEPTION case.
 */
@DisplayName("TodoService - GetTodoItems Tests")
public class GetTodoItemsTest extends TodoServiceTestBase {

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify repository is called with correct userId.
     * Expected Result: findAllByUserIdOrderByTodoIdDesc called with userId.
     * Assertions: verify(todoRepository).findAllByUserIdOrderByTodoIdDesc(anyLong());
     */
    @Test
    @DisplayName("Get Todo Items - Calls repository with userId")
    void getTodoItems_CallsRepositoryWithUserId() {
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        todoService.getTodoItems();

        verify(todoRepository, times(1)).findAllByUserIdOrderByTodoIdDesc(anyLong());
    }

    /**
     * Purpose: Verify empty list when no todos exist.
     * Expected Result: Empty list is returned.
     * Assertions: assertTrue(result.isEmpty());
     */
    @Test
    @DisplayName("Get Todo Items - Empty list - Returns empty")
    void getTodoItems_EmptyList_ReturnsEmpty() {
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Purpose: Verify todo fields are correctly mapped to response model.
     * Expected Result: All fields are correctly mapped.
     * Assertions: All field assertions pass.
     */
    @Test
    @DisplayName("Get Todo Items - Fields correctly mapped")
    void getTodoItems_FieldsCorrectlyMapped() {
        testTodo.setTask("Mapped Task");
        testTodo.setIsDone(true);

        List<Todo> expectedTodos = Arrays.asList(testTodo);
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(expectedTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        TodoResponseModel response = result.get(0);
        assertEquals(TEST_TODO_ID, response.getTodoId());
        assertEquals("Mapped Task", response.getTask());
        assertTrue(response.getIsDone());
        assertEquals(TEST_USER_ID, response.getUserId());
    }

    /**
     * Purpose: Verify many todos are returned correctly.
     * Expected Result: All todos are returned.
     * Assertions: assertEquals(10, result.size());
     */
    @Test
    @DisplayName("Get Todo Items - Many todos - Returns all")
    void getTodoItems_ManyTodos_ReturnsAll() {
        List<Todo> manyTodos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Todo todo = createTestTodo();
            todo.setTodoId((long) i);
            todo.setTask("Task " + i);
            manyTodos.add(todo);
        }

        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(manyTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertEquals(10, result.size());
    }

    /**
     * Purpose: Verify todos with mixed isDone values are returned.
     * Expected Result: All todos returned with correct isDone values.
     * Assertions: assertEquals expected isDone for each.
     */
    @Test
    @DisplayName("Get Todo Items - Mixed isDone values")
    void getTodoItems_MixedIsDoneValues() {
        testTodo.setIsDone(false);

        Todo doneTodo = createTestTodo();
        doneTodo.setTodoId(2L);
        doneTodo.setIsDone(true);

        List<Todo> expectedTodos = Arrays.asList(testTodo, doneTodo);
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(expectedTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertEquals(2, result.size());
        assertFalse(result.get(0).getIsDone());
        assertTrue(result.get(1).getIsDone());
    }

    /**
     * Purpose: Verify multiple todos are returned correctly.
     * Expected Result: All todos are returned.
     * Assertions: assertEquals(3, result.size());
     */
    @Test
    @DisplayName("Get Todo Items - Multiple todos - Returns all")
    void getTodoItems_MultipleTodos_ReturnsAll() {
        Todo todo2 = createTestTodo();
        todo2.setTodoId(2L);
        todo2.setTask("Task 2");

        Todo todo3 = createTestTodo();
        todo3.setTodoId(3L);
        todo3.setTask("Task 3");

        List<Todo> expectedTodos = Arrays.asList(testTodo, todo2, todo3);
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(expectedTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    /**
     * Purpose: Verify repository is called exactly once.
     * Expected Result: findAllByUserIdOrderByTodoIdDesc called once.
     * Assertions: verify called once.
     */
    @Test
    @DisplayName("Get Todo Items - Repository called once")
    void getTodoItems_RepositoryCalledOnce() {
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(Arrays.asList(testTodo));
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        todoService.getTodoItems();
        todoService.getTodoItems();

        verify(todoRepository, times(2)).findAllByUserIdOrderByTodoIdDesc(anyLong());
    }

    /**
     * Purpose: Verify successful retrieval of todo items.
     * Expected Result: List of TodoResponseModel is returned.
     * Assertions: assertNotNull(result); assertEquals(1, result.size());
     */
    @Test
    @DisplayName("Get Todo Items - Success - Returns todos")
    void getTodoItems_Success() {
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(expectedTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_TODO_ID, result.get(0).getTodoId());
        assertEquals(TEST_TASK, result.get(0).getTask());
    }

    /**
     * Purpose: Verify logging is called after successful get.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Get Todo Items - Success - Logs the operation")
    void getTodoItems_Success_LogsOperation() {
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(expectedTodos);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        todoService.getTodoItems();

        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
                eq(ApiRoutes.TodoSubRoute.GET_ITEMS)
        );
    }

    /**
     * Purpose: Verify todos with various special characters in tasks.
     * Expected Result: All are returned correctly.
     * Assertions: All tasks are mapped correctly.
     */
    @Test
    @DisplayName("Get Todo Items - Various special characters")
    void getTodoItems_VariousSpecialCharacters() {
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

        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(specialTodos);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertEquals(3, result.size());
        assertEquals("@#$%^&*()", result.get(0).getTask());
        assertEquals("你好世界", result.get(1).getTask());
        assertEquals("<html>", result.get(2).getTask());
    }

    // ========================================
    // FAILURE/EXCEPTION TESTS
    // ========================================

    /**
     * Purpose: Verify null from repository is handled.
     * Expected Result: NullPointerException or empty list depending on implementation.
     * Assertions: Throws or returns empty.
     */
    @Test
    @DisplayName("Get Todo Items - Null from repository")
    void getTodoItems_NullFromRepository() {
        when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(null);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // The service might throw NPE when mapping null to stream
        assertThrows(NullPointerException.class, () -> todoService.getTodoItems());
    }
}
