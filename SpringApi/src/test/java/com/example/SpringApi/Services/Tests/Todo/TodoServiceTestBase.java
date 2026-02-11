package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Repositories.TodoRepository;
import com.example.SpringApi.Services.TodoService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Interface.ITodoSubTranslator;
import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base test class for TodoService tests.
 * Provides common mocks, test data, and setup for all Todo service tests.
 */
@ExtendWith(MockitoExtension.class)
public abstract class TodoServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_TODO_ID = 1L;
    protected static final String DEFAULT_TODO_TITLE = "Test Todo";
    protected static final Boolean DEFAULT_TODO_COMPLETED = false;
    protected static final Long DEFAULT_USER_ID = 1L;
    protected static final String DEFAULT_CREATED_USER = "admin";

    @Mock
    protected TodoRepository todoRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected ITodoSubTranslator todoServiceMock;

    @InjectMocks
    protected TodoService todoService;

    protected Todo testTodo;
    protected TodoRequestModel validRequest;

    protected TodoController todoController;

    protected static final Long TEST_TODO_ID = DEFAULT_TODO_ID;
    protected static final Long TEST_USER_ID = DEFAULT_USER_ID;
    protected static final String TEST_TASK = DEFAULT_TODO_TITLE;
    protected static final String CREATED_USER = DEFAULT_CREATED_USER;
    protected static final String LONG_TASK = "A".repeat(501);

    @BeforeEach
    protected void setUp() {
        validRequest = createValidTodoRequest();
        validRequest.setTodoId(TEST_TODO_ID);
        validRequest.setTask(TEST_TASK);
        validRequest.setIsDone(false);

        testTodo = createTestTodo();
        testTodo.setTodoId(TEST_TODO_ID);
        testTodo.setUserId(TEST_USER_ID);
        testTodo.setCreatedAt(LocalDateTime.now());
        testTodo.setUpdatedAt(LocalDateTime.now());

        todoController = new TodoController(todoServiceMock);
    }

    // ==========================================
    // STUBS
    // ==========================================

    protected void stubTodoRepositoryExistsById(Long id, boolean exists) {
        lenient().when(todoRepository.existsById(id)).thenReturn(exists);
    }

    protected void stubTodoRepositoryExistsByIdAny(boolean exists) {
        lenient().when(todoRepository.existsById(anyLong())).thenReturn(exists);
    }

    protected void stubTodoRepositorySave(Todo returnTodo) {
        lenient().when(todoRepository.save(any(Todo.class))).thenReturn(returnTodo);
    }

    protected void stubTodoRepositoryFindById(Long id, Optional<Todo> result) {
        lenient().when(todoRepository.findById(id)).thenReturn(result);
    }

    protected void stubTodoRepositoryFindByIdAny(Optional<Todo> result) {
        lenient().when(todoRepository.findById(anyLong())).thenReturn(result);
    }

    protected void stubTodoRepositoryDeleteById(Long id) {
        lenient().doNothing().when(todoRepository).deleteById(id);
    }

    protected void stubTodoRepositoryFindAllByUserIdOrderByTodoIdDesc(List<Todo> result) {
        lenient().when(todoRepository.findAllByUserIdOrderByTodoIdDesc(anyLong())).thenReturn(result);
    }

    protected void stubUserLogServiceLogDataReturnsTrue() {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
    }

    protected void stubTodoServiceAddTodoThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(todoServiceMock).addTodo(any(TodoRequestModel.class));
    }

    protected void stubTodoServiceUpdateTodoThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(todoServiceMock).updateTodo(any(TodoRequestModel.class));
    }

    protected void stubTodoServiceGetTodoItemsThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(todoServiceMock).getTodoItems();
    }

    protected void stubTodoServiceDeleteTodoThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(todoServiceMock).deleteTodo(anyLong());
    }

    protected void stubTodoServiceToggleTodoThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(todoServiceMock).toggleTodo(anyLong());
    }

    // ==================== FACTORY METHODS ====================

    protected TodoRequestModel createValidTodoRequest() {
        return createValidTodoRequest(DEFAULT_TODO_ID);
    }

    protected TodoRequestModel createValidTodoRequest(Long todoId) {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(todoId);
        request.setTask(DEFAULT_TODO_TITLE);
        request.setIsDone(DEFAULT_TODO_COMPLETED);
        return request;
    }

    protected Todo createTestTodo() {
        return createTestTodo(createValidTodoRequest(), DEFAULT_CREATED_USER);
    }

    protected Todo createTestTodo(TodoRequestModel request, String createdUser) {
        Todo todo = new Todo();
        todo.setTodoId(request.getTodoId());
        todo.setTask(request.getTask());
        todo.setIsDone(request.getIsDone());
        todo.setCreatedUser(createdUser);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todo;
    }

    protected void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }
}
