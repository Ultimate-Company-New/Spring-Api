package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Repositories.TodoRepository;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.TodoService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

/**
 * Base test class for TodoService tests.
 * Provides common mocks, test data, and setup for all Todo service tests.
 */
@ExtendWith(MockitoExtension.class)
public abstract class TodoServiceTestBase extends BaseTest {

    @Mock
    protected TodoRepository todoRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected TodoService todoService;

    protected Todo testTodo;
    protected TodoRequestModel validRequest;
    
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
    }
}
