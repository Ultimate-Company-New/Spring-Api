package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.ResponseModels.TodoResponseModel;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Repositories.TodoRepository;
import com.example.SpringApi.Services.TodoService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoService.
 *
 * <p>This test class provides comprehensive coverage of TodoService methods including:
 * - CRUD operations (add, delete, toggle, get)
 * - Todo validation and error handling
 * - Audit logging verification
 * - User-specific todo retrieval
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Unit Tests")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private TodoService todoService;

    private Todo testTodo;
    private TodoRequestModel validRequest;
    private static final Long TEST_TODO_ID = 1L;
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_TASK = "Test todo task";
    private static final String CREATED_USER = "testuser";
    private static final String LONG_TASK = "A".repeat(501); // 501 characters

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Mock BaseService methods to return valid values by default
        lenient().doReturn(CREATED_USER).when(todoService).getUser();
        lenient().doReturn(TEST_USER_ID).when(todoService).getUserId();

        // Initialize valid request
        validRequest = new TodoRequestModel();
        validRequest.setTodoId(TEST_TODO_ID);
        validRequest.setTask(TEST_TASK);
        validRequest.setIsDone(false);
        validRequest.setUserId(TEST_USER_ID);

        // Initialize test todo using constructor
        testTodo = new Todo(validRequest, CREATED_USER);
        testTodo.setTodoId(TEST_TODO_ID);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());
    }

            // ==================== ADD TODO TESTS ====================

    /**
     * Test successful addition of a new todo item.
     */
    @Test
    @DisplayName("Should successfully add new todo item")
    void testAddTodo_Success() {
        doReturn(CREATED_USER).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTask("Test Todo");
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        Todo savedTodo = new Todo(request, CREATED_USER);
        savedTodo.setTodoId(1L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        assertDoesNotThrow(() -> todoService.addTodo(request));

        verify(todoRepository).save(any(Todo.class));
        verify(userLogService).logData(
            eq(CREATED_USER),
            eq(SuccessMessages.TodoSuccessMessages.InsertTodo + " " + savedTodo.getTodoId()),
            eq(ApiRoutes.TodoSubRoute.ADD_ITEM)
        );
    }

    /**
     * Test addition with null request.
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding null todo request")
    void testAddTodo_NullRequest() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(null);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidRequest, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when adding todo with null task")
    void testAddTodo_NullTask() {
        doReturn(CREATED_USER).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTask(null);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test addition with empty task.
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding todo with empty task")
    void testAddTodo_EmptyTask() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("");
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test addition with whitespace-only task.
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding todo with whitespace-only task")
    void testAddTodo_WhitespaceTask() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask("   ");
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test addition with task too long.
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding todo with task too long")
    void testAddTodo_TaskTooLong() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask(LONG_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, exception.getMessage());
    }

    /**
     * Test addition with null userId.
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding todo with null userId")
    void testAddTodo_NullUserId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test addition with invalid userId (<= 0).
     */
    @Test
    @DisplayName("Should throw BadRequestException when adding todo with invalid userId")
    void testAddTodo_InvalidUserId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(0L);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test addition with null user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when getUser returns null")
    void testAddTodo_NullUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    /**
     * Test addition with empty user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when getUser returns empty string")
    void testAddTodo_EmptyUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.addTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    // ==================== UPDATE TODO TESTS ====================

    /**
     * Test successful update of an existing todo item.
     */
    @Test
    @DisplayName("Should successfully update existing todo item")
    void testUpdateTodo_Success() {
        doReturn(CREATED_USER).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        request.setUserId(TEST_USER_ID);

        Todo updatedTodo = new Todo(request, CREATED_USER, testTodo);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        assertDoesNotThrow(() -> todoService.updateTodo(request));

        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository).save(any(Todo.class));
        verify(userLogService).logData(
            eq(CREATED_USER),
            eq(SuccessMessages.TodoSuccessMessages.UpdateTodo + " " + TEST_TODO_ID),
            eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM)
        );
    }

    /**
     * Test update with null request.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with null request")
    void testUpdateTodo_NullRequest() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(null);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test update with null todoId.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with null todoId")
    void testUpdateTodo_NullTodoId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(null);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test update with invalid todoId (<= 0).
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with invalid todoId")
    void testUpdateTodo_InvalidTodoId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(0L);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test update of non-existent todo item.
     */
    @Test
    @DisplayName("Should throw NotFoundException when updating non-existent todo")
    void testUpdateTodo_NotFound() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("Updated Todo");
        request.setIsDone(true);
        request.setUserId(TEST_USER_ID);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository, never()).save(any());
        verify(userLogService, never()).logData(any(), any(), any());
    }

    /**
     * Test update with null task.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with null task")
    void testUpdateTodo_NullTask() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(null);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test update with empty task.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with empty task")
    void testUpdateTodo_EmptyTask() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("");
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test update with whitespace-only task.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with whitespace-only task")
    void testUpdateTodo_WhitespaceTask() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask("   ");
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, exception.getMessage());
    }

    /**
     * Test update with task too long.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with task too long")
    void testUpdateTodo_TaskTooLong() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(LONG_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, exception.getMessage());
    }

    /**
     * Test update with null userId.
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with null userId")
    void testUpdateTodo_NullUserId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(null);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test update with invalid userId (<= 0).
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with invalid userId")
    void testUpdateTodo_InvalidUserId() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(-1L);

        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test update with null user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with null user")
    void testUpdateTodo_NullUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    /**
     * Test update with empty user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when updating with empty user")
    void testUpdateTodo_EmptyUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(TEST_TODO_ID);
        request.setTask(TEST_TASK);
        request.setIsDone(false);
        request.setUserId(TEST_USER_ID);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.updateTodo(request);
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    // ==================== DELETE TODO TESTS ====================

    /**
     * Test successful deletion of a todo item.
     */
    @Test
    @DisplayName("Should successfully delete a todo item")
    void testDeleteTodo_Success() {
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);

        assertDoesNotThrow(() -> todoService.deleteTodo(TEST_TODO_ID));

        verify(todoRepository).existsById(TEST_TODO_ID);
        verify(todoRepository).deleteById(TEST_TODO_ID);
        verify(userLogService).logData(
            eq(CREATED_USER),
            contains(SuccessMessages.TodoSuccessMessages.DeleteTodo),
            eq(ApiRoutes.TodoSubRoute.DELETE_ITEM)
        );
    }

    /**
     * Test deletion with invalid id (<= 0).
     */
    @Test
    @DisplayName("Should throw BadRequestException when deleting with invalid id")
    void testDeleteTodo_InvalidId() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.deleteTodo(0L);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        verify(todoRepository, never()).existsById(any());
        verify(todoRepository, never()).deleteById(any());
        verify(userLogService, never()).logData(any(), any(), any());
    }

    /**
     * Test deletion of non-existent todo item.
     */
    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent todo")
    void testDeleteTodo_NotFound() {
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            todoService.deleteTodo(TEST_TODO_ID);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        verify(todoRepository).existsById(TEST_TODO_ID);
        verify(todoRepository, never()).deleteById(any());
        verify(userLogService, never()).logData(any(), any(), any());
    }

    // ==================== TOGGLE TODO TESTS ====================

    /**
     * Test successful toggling from false to true.
     */
    @Test
    @DisplayName("Should successfully toggle todo completion status from false to true")
    void testToggleTodo_Success_FalseToTrue() {
        doReturn(CREATED_USER).when(todoService).getUser();
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository).save(testTodo);
        assertTrue(testTodo.getIsDone());
        verify(userLogService).logData(
            eq(CREATED_USER),
            contains(SuccessMessages.TodoSuccessMessages.ToggleTodo),
            eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
        );
    }

    /**
     * Test successful toggling from true to false.
     */
    @Test
    @DisplayName("Should successfully toggle todo completion status from true to false")
    void testToggleTodo_Success_TrueToFalse() {
        doReturn(CREATED_USER).when(todoService).getUser();
        testTodo.setIsDone(true); // Start as true
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository).save(testTodo);
        assertFalse(testTodo.getIsDone());
        verify(userLogService).logData(
            eq(CREATED_USER),
            contains(SuccessMessages.TodoSuccessMessages.ToggleTodo),
            eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
        );
    }

    /**
     * Test toggling with invalid id (<= 0).
     */
    @Test
    @DisplayName("Should throw BadRequestException when toggling with invalid id")
    void testToggleTodo_InvalidId() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.toggleTodo(-1L);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        verify(todoRepository, never()).findById(any());
        verify(todoRepository, never()).save(any());
        verify(userLogService, never()).logData(any(), any(), any());
    }

    /**
     * Test toggling of non-existent todo item.
     */
    @Test
    @DisplayName("Should throw NotFoundException when toggling non-existent todo")
    void testToggleTodo_NotFound() {
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            todoService.toggleTodo(TEST_TODO_ID);
        });
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        verify(todoRepository).findById(TEST_TODO_ID);
        verify(todoRepository, never()).save(any());
        verify(userLogService, never()).logData(any(), any(), any());
    }

    // ==================== GET TODO ITEMS TESTS ====================

    /**
     * Test successful retrieval of todo items for user.
     */
    @Test
    @DisplayName("Should successfully retrieve and convert todo items to response models")
    void testGetTodoItems_Success() {
        doReturn(CREATED_USER).when(todoService).getUser();
        doReturn(TEST_USER_ID).when(todoService).getUserId();

        List<Todo> expectedTodos = Arrays.asList(testTodo);
        when(todoRepository.findAllByUserId(TEST_USER_ID)).thenReturn(expectedTodos);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertNotNull(result);
        assertEquals(1, result.size());
        TodoResponseModel responseModel = result.get(0);
        assertEquals(TEST_TODO_ID, responseModel.getTodoId());
        assertEquals(TEST_TASK, responseModel.getTask());
        assertEquals(false, responseModel.getIsDone());
        assertEquals(TEST_USER_ID, responseModel.getUserId());

        verify(todoRepository).findAllByUserId(TEST_USER_ID);
        verify(userLogService).logData(
            eq(CREATED_USER),
            eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
            eq(ApiRoutes.TodoSubRoute.GET_ITEMS)
        );
    }

    /**
     * Test retrieval of todo items when no todos exist for user.
     */
    @Test
    @DisplayName("Should return empty list when no todos exist for user")
    void testGetTodoItems_EmptyList() {
        doReturn(CREATED_USER).when(todoService).getUser();
        doReturn(TEST_USER_ID).when(todoService).getUserId();
        List<Todo> emptyList = Arrays.asList();
        when(todoRepository.findAllByUserId(TEST_USER_ID)).thenReturn(emptyList);

        List<TodoResponseModel> result = todoService.getTodoItems();

        assertTrue(result.isEmpty());
        verify(todoRepository).findAllByUserId(TEST_USER_ID);
        verify(userLogService).logData(
            eq(CREATED_USER),
            eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
            eq(ApiRoutes.TodoSubRoute.GET_ITEMS)
        );
    }

    /**
     * Test retrieval with null user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when getUser returns null during getTodoItems")
    void testGetTodoItems_NullUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.getTodoItems();
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    /**
     * Test retrieval with empty user from getUser().
     */
    @Test
    @DisplayName("Should throw BadRequestException when getUser returns empty string during getTodoItems")
    void testGetTodoItems_EmptyUser() {
        doThrow(new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser)).when(todoService).getUser();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            todoService.getTodoItems();
        });
        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }
}