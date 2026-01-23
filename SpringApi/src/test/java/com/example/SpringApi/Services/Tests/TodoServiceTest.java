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
import org.junit.jupiter.api.Nested;
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
 * @version 2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Unit Tests")
class TodoServiceTest extends BaseTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    @Spy
    private TodoService todoService;

    private Todo testTodo;
    private TodoRequestModel validRequest;
    private static final Long TEST_TODO_ID = 1L;
    private static final String TEST_TASK = "Test todo task";
    private static final String LONG_TASK = "A".repeat(501); // 501 characters

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize valid request
        validRequest = new TodoRequestModel();
        validRequest.setTodoId(TEST_TODO_ID);
        validRequest.setTask(TEST_TASK);
        validRequest.setIsDone(false);

        // Initialize test todo using constructor
        testTodo = new Todo(validRequest, DEFAULT_LOGIN_NAME, DEFAULT_USER_ID);
        testTodo.setTodoId(TEST_TODO_ID);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());
        
        // Mock BaseService behavior
        lenient().doReturn(DEFAULT_CLIENT_ID).when(todoService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(todoService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(todoService).getUser();
    }

    // ==================== ADD TODO TESTS ====================

    @Nested
    @DisplayName("addTodo Tests")
    class AddTodoTests {

        /**
         * Test successful addition of a new todo item.
         */
        @Test
        @DisplayName("Add Todo - Success")
        void testAddTodo_Success() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTask("Test Todo");
            request.setIsDone(false);

            Todo savedTodo = new Todo(request, DEFAULT_LOGIN_NAME, DEFAULT_USER_ID);
            savedTodo.setTodoId(1L);

            when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

            assertDoesNotThrow(() -> todoService.addTodo(request));

            verify(todoRepository).save(any(Todo.class));
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                eq(SuccessMessages.TodoSuccessMessages.InsertTodo + " " + savedTodo.getTodoId()),
                eq(ApiRoutes.TodoSubRoute.ADD_ITEM)
            );
        }

        /**
         * Test addition with null request.
         */
        @Test
        @DisplayName("Add Todo - Null Request - Throws BadRequestException")
        void testAddTodo_NullRequest() {
            BadRequestException ex = assertThrows(BadRequestException.class, () -> {
                todoService.addTodo(null);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidRequest, ex.getMessage());
        }

        @Test
        @DisplayName("Add Todo - Null Task - Throws BadRequestException")
        void testAddTodo_NullTask() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTask(null);
            request.setIsDone(false);

            BadRequestException ex = assertThrows(BadRequestException.class, () -> {
                todoService.addTodo(request);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
        }

        // Additional edge case tests
        @Test
        @DisplayName("Add Todo - Task at Max Length (500) - Success")
        void addTodo_TaskMaxLength_Success() {
            validRequest.setTask("T".repeat(500));
            Todo savedTodo = new Todo(validRequest, DEFAULT_LOGIN_NAME, DEFAULT_USER_ID);
            savedTodo.setTodoId(1L);
            when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

            assertDoesNotThrow(() -> todoService.addTodo(validRequest));
            verify(todoRepository, times(1)).save(any(Todo.class));
        }

        @Test
        @DisplayName("Add Todo - Task with Special Characters - Success")
        void addTodo_SpecialCharacters_Success() {
            validRequest.setTask("Test!@#$%^&*()_+-=[]{}|;':,.<>?");
            Todo savedTodo = new Todo(validRequest, DEFAULT_LOGIN_NAME, DEFAULT_USER_ID);
            savedTodo.setTodoId(1L);
            when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

            assertDoesNotThrow(() -> todoService.addTodo(validRequest));
            verify(todoRepository, times(1)).save(any(Todo.class));
        }
    }

    // ==================== UPDATE TODO TESTS ====================

    @Nested
    @DisplayName("updateTodo Tests")
    class UpdateTodoTests {

        /**
         * Test successful update of an existing todo item.
         */
        @Test
        @DisplayName("Update Todo - Success")
        void testUpdateTodo_Success() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Todo");
            request.setIsDone(true);

            Todo updatedTodo = new Todo(request, DEFAULT_LOGIN_NAME, testTodo, DEFAULT_USER_ID);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

            assertDoesNotThrow(() -> todoService.updateTodo(request));

            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository).save(any(Todo.class));
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                eq(SuccessMessages.TodoSuccessMessages.UpdateTodo + " " + TEST_TODO_ID),
                eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM)
            );
        }

        /**
         * Test update with null request.
         */
        @Test
        @DisplayName("Update Todo - Null Request - Throws BadRequestException")
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
        @DisplayName("Update Todo - Null TodoId - Throws BadRequestException")
        void testUpdateTodo_NullTodoId() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(null);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                todoService.updateTodo(request);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Test update of non-existent todo item.
         */
        @Test
        @DisplayName("Update Todo - Not Found - Throws NotFoundException")
        void testUpdateTodo_NotFound() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Todo");
            request.setIsDone(true);

            lenient().when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                todoService.updateTodo(request);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository, never()).save(any());
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }
    }

    // ==================== DELETE TODO TESTS ====================

    @Nested
    @DisplayName("deleteTodo Tests")
    class DeleteTodoTests {

        /**
         * Test successful deletion of a todo item.
         */
        @Test
        @DisplayName("Delete Todo - Success")
        void testDeleteTodo_Success() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);

            assertDoesNotThrow(() -> todoService.deleteTodo(TEST_TODO_ID));

            verify(todoRepository).existsById(TEST_TODO_ID);
            verify(todoRepository).deleteById(TEST_TODO_ID);
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                contains(SuccessMessages.TodoSuccessMessages.DeleteTodo),
                eq(ApiRoutes.TodoSubRoute.DELETE_ITEM)
            );
        }

        /**
         * Test deletion of non-existent todo item.
         */
        @Test
        @DisplayName("Delete Todo - Not Found - Throws NotFoundException")
        void testDeleteTodo_NotFound() {
            lenient().when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                todoService.deleteTodo(TEST_TODO_ID);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
            verify(todoRepository).existsById(TEST_TODO_ID);
            verify(todoRepository, never()).deleteById(anyLong());
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }

        @Test
        @DisplayName("Delete Todo - Negative ID - Not Found")
        void deleteTodo_NegativeId_ThrowsNotFoundException() {
            when(todoRepository.existsById(-1L)).thenReturn(false);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(-1L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        @Test
        @DisplayName("Delete Todo - Zero ID - Not Found")
        void deleteTodo_ZeroId_ThrowsNotFoundException() {
            when(todoRepository.existsById(0L)).thenReturn(false);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(0L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }
    }

    // ==================== TOGGLE TODO TESTS ====================

    @Nested
    @DisplayName("toggleTodo Tests")
    class ToggleTodoTests {

        /**
         * Test successful toggling from false to true.
         */
        @Test
        @DisplayName("Toggle Todo - Success - False to True")
        void testToggleTodo_Success_FalseToTrue() {
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository).save(testTodo);
            assertTrue(testTodo.getIsDone());
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                contains(SuccessMessages.TodoSuccessMessages.ToggleTodo),
                eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
            );
        }

        /**
         * Test successful toggling from true to false.
         */
        @Test
        @DisplayName("Toggle Todo - Success - True to False")
        void testToggleTodo_Success_TrueToFalse() {
            testTodo.setIsDone(true); // Start as true
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository).save(testTodo);
            assertFalse(testTodo.getIsDone());
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                contains(SuccessMessages.TodoSuccessMessages.ToggleTodo),
                eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
            );
        }

        /**
         * Test toggling of non-existent todo item.
         */
        @Test
        @DisplayName("Toggle Todo - Not Found - Throws NotFoundException")
        void testToggleTodo_NotFound() {
            lenient().when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                todoService.toggleTodo(TEST_TODO_ID);
            });
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, exception.getMessage());
            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository, never()).save(any());
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }
        
        @Test
        @DisplayName("Toggle Todo - Negative ID - Not Found")
        void toggleTodo_NegativeId_ThrowsNotFoundException() {
            when(todoRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(-1L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }
    }

    // ==================== GET TODO ITEMS TESTS ====================

    @Nested
    @DisplayName("getTodoItems Tests")
    class GetTodoItemsTests {

        /**
         * Test successful retrieval of todo items for user.
         */
        @Test
        @DisplayName("Get Todo Items - Success")
        void testGetTodoItems_Success() {
            List<Todo> expectedTodos = Arrays.asList(testTodo);
            lenient().when(todoRepository.findAllByUserIdOrderByTodoIdDesc(DEFAULT_USER_ID)).thenReturn(expectedTodos);

            List<TodoResponseModel> result = todoService.getTodoItems();

            assertNotNull(result);
            assertEquals(1, result.size());
            TodoResponseModel responseModel = result.get(0);
            assertEquals(TEST_TODO_ID, responseModel.getTodoId());
            assertEquals(TEST_TASK, responseModel.getTask());
            assertEquals(false, responseModel.getIsDone());
            assertEquals(DEFAULT_USER_ID, responseModel.getUserId());

            verify(todoRepository).findAllByUserIdOrderByTodoIdDesc(DEFAULT_USER_ID);
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
                eq(ApiRoutes.TodoSubRoute.GET_ITEMS)
            );
        }

        /**
         * Test retrieval of todo items when no todos exist for user.
         */
        @Test
        @DisplayName("Get Todo Items - Empty List")
        void testGetTodoItems_EmptyList() {
            List<Todo> emptyList = Arrays.asList();
            lenient().when(todoRepository.findAllByUserIdOrderByTodoIdDesc(DEFAULT_USER_ID)).thenReturn(emptyList);

            List<TodoResponseModel> result = todoService.getTodoItems();

            assertTrue(result.isEmpty());
            verify(todoRepository).findAllByUserIdOrderByTodoIdDesc(DEFAULT_USER_ID);
            verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                eq(SuccessMessages.TodoSuccessMessages.GetTodoItems),
                eq(ApiRoutes.TodoSubRoute.GET_ITEMS)
            );
        }
    }
}
