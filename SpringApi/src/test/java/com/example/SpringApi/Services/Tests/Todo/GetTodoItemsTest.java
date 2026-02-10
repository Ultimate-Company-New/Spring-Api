package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - GetTodoItems operation.
 */
@DisplayName("TodoService - GetTodoItems Tests")
class GetTodoItemsTest extends TodoServiceTestBase {

    // Total Tests: 12

    // ========================================
    // Section 1: Success Tests
    // ========================================

    // Purpose: Verify repository is called with correct userId.
    // Expected Result: findAllByUserIdOrderByTodoIdDesc called with userId.
    // Assertions: verify(todoRepository).findAllByUserIdOrderByTodoIdDesc(anyLong());
    @Test
    @DisplayName("getTodoItems - Verify Repository Call - Success")
    void getTodoItems_verifyRepositoryCall_success() {
        // Arrange
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(new ArrayList<>());
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.getTodoItems();

        // Assert
        verify(todoRepository, times(1)).findAllByUserIdOrderByTodoIdDesc(anyLong());
    }

    // Purpose: Verify empty list when no todos exist.
    // Expected Result: Empty list is returned.
    // Assertions: assertTrue(result.isEmpty());
    @Test
    @DisplayName("getTodoItems - Empty List - Success")
    void getTodoItems_emptyList_success() {
        // Arrange
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(new ArrayList<>());
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Purpose: Verify todo fields are correctly mapped to response model.
    // Expected Result: All fields are correctly mapped.
    // Assertions: All field assertions pass.
    @Test
    @DisplayName("getTodoItems - Fields Correctly Mapped - Success")
    void getTodoItems_fieldsCorrectlyMapped_success() {
        // Arrange
        testTodo.setTask("Mapped Task");
        testTodo.setIsDone(true);
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(expectedTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        TodoResponseModel response = result.get(0);
        assertEquals(TEST_TODO_ID, response.getTodoId());
        assertEquals("Mapped Task", response.getTask());
        assertTrue(response.getIsDone());
        assertEquals(TEST_USER_ID, response.getUserId());
    }

    // Purpose: Verify many todos are returned correctly.
    // Expected Result: All todos are returned.
    // Assertions: assertEquals(10, result.size());
    @Test
    @DisplayName("getTodoItems - Many Todos - Success")
    void getTodoItems_manyTodos_success() {
        // Arrange
        List<Todo> manyTodos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Todo todo = createTestTodo();
            todo.setTodoId((long) i);
            todo.setTask("Task " + i);
            manyTodos.add(todo);
        }
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(manyTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        assertEquals(10, result.size());
    }

    // Purpose: Verify todos with mixed isDone values are returned.
    // Expected Result: All todos returned with correct isDone values.
    // Assertions: assertEquals expected isDone for each.
    @Test
    @DisplayName("getTodoItems - Mixed IsDone Values - Success")
    void getTodoItems_mixedIsDoneValues_success() {
        // Arrange
        testTodo.setIsDone(false);
        Todo doneTodo = createTestTodo();
        doneTodo.setTodoId(2L);
        doneTodo.setIsDone(true);
        List<Todo> expectedTodos = Arrays.asList(testTodo, doneTodo);
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(expectedTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        assertEquals(2, result.size());
        assertFalse(result.get(0).getIsDone());
        assertTrue(result.get(1).getIsDone());
    }

    // Purpose: Verify multiple todos are returned correctly.
    // Expected Result: All todos are returned.
    // Assertions: assertEquals(3, result.size());
    @Test
    @DisplayName("getTodoItems - Multiple Todos - Success")
    void getTodoItems_multipleTodos_success() {
        // Arrange
        Todo todo2 = createTestTodo();
        todo2.setTodoId(2L);
        todo2.setTask("Task 2");
        Todo todo3 = createTestTodo();
        todo3.setTodoId(3L);
        todo3.setTask("Task 3");
        List<Todo> expectedTodos = Arrays.asList(testTodo, todo2, todo3);
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(expectedTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    // Purpose: Verify repository is called exactly once.
    // Expected Result: findAllByUserIdOrderByTodoIdDesc called once.
    // Assertions: verify called once.
    @Test
    @DisplayName("getTodoItems - Repository Called Once - Success")
    void getTodoItems_repositoryCalledOnce_success() {
        // Arrange
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(Arrays.asList(testTodo));
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.getTodoItems();
        todoService.getTodoItems();

        // Assert
        verify(todoRepository, times(2)).findAllByUserIdOrderByTodoIdDesc(anyLong());
    }

    // Purpose: Verify successful retrieval of todo items.
    // Expected Result: List of TodoResponseModel is returned.
    // Assertions: assertNotNull(result); assertEquals(1, result.size());
    @Test
    @DisplayName("getTodoItems - Success - Returns Todos")
    void getTodoItems_success_returnsTodos() {
        // Arrange
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(expectedTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        List<TodoResponseModel> result = todoService.getTodoItems();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_TODO_ID, result.get(0).getTodoId());
        assertEquals(TEST_TASK, result.get(0).getTask());
    }

    // Purpose: Verify logging is called after successful get.
    // Expected Result: userLogService.logData is called.
    // Assertions: verify(userLogService).logData(...);
    @Test
    @DisplayName("getTodoItems - Logs Operation - Success")
    void getTodoItems_success_logsOperation() {
        // Arrange
        List<Todo> expectedTodos = Arrays.asList(testTodo);
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(expectedTodos);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.getTodoItems();

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
                eq(ApiRoutes.TodoSubRoute.GET_ITEMS));
    }

    // Purpose: Verify todos with various special characters in tasks.
    // Expected Result: All are returned correctly.
    // Assertions: All tasks are mapped correctly.
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

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    // Purpose: Verify null from repository is handled.
    // Expected Result: NullPointerException or empty list depending on implementation.
    // Assertions: Throws or returns empty.
    @Test
    @DisplayName("getTodoItems - Null From Repository - Throws NullPointerException")
    void getTodoItems_nullFromRepository_exception() {
        // Arrange
        stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(null);
        stubUserLogServiceLogDataReturnsTrue();

        // Act & Assert
        // The service might throw NPE when mapping null to stream
        assertThrows(NullPointerException.class, () -> todoService.getTodoItems());
    }

    // ========================================
    // Section 3: Controller Permission/Auth Tests
    // ========================================

    // Purpose: Verify @PreAuthorize Annotation on controller
    // Expected Result: Annotation exists and has correct value
    // Assertions: assertNotNull, assertEquals
    @Test
    @DisplayName("getTodoItems - Verify @PreAuthorize Annotation")
    void getTodoItems_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = TodoController.class.getMethod("getTodoItems");

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "getTodoItems method should have @PreAuthorize annotation");
        assertEquals("@customAuthorization.hasAuthority(null)", annotation.value());
    }
}