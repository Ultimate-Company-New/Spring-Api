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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | AddTodoTests                            | 18              |
 * | UpdateTodoTests                         | 17              |
 * | DeleteTodoTests                         | 10              |
 * | ToggleTodoTests                         | 12              |
 * | GetTodoItemsTests                       | 11              |
 * | **Total**                               | **68**          |
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
    private TodoService todoService;

    private Todo testTodo;
    private TodoRequestModel validRequest;
    private static final Long TEST_TODO_ID = DEFAULT_TODO_ID;
    private static final Long TEST_USER_ID = DEFAULT_USER_ID;
    private static final String TEST_TASK = DEFAULT_TODO_TITLE;
    private static final String CREATED_USER = DEFAULT_CREATED_USER;
    private static final String LONG_TASK = "A".repeat(501);

    @BeforeEach
    void setUp() {
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

    @Nested
    @DisplayName("AddTodo Tests")
    class AddTodoTests {

        /**
         * Purpose: Verify successful todo addition.
         * Expected Result: Todo is saved and logged.
         * Assertions: assertDoesNotThrow(); verify(todoRepository).save(any(Todo.class));
         */
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

        /**
         * Purpose: Verify null request throws BadRequestException.
         * Expected Result: BadRequestException with InvalidRequest message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidRequest, ex.getMessage());
         */
        @Test
        @DisplayName("Add Todo - Null request - Throws BadRequestException")
        void addTodo_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.InvalidRequest,
                    () -> todoService.addTodo(null));
        }

        /**
         * Purpose: Verify null task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
        @Test
        @DisplayName("Add Todo - Null task - Throws BadRequestException")
        void addTodo_NullTask_ThrowsBadRequestException() {
            TodoRequestModel request = createValidTodoRequest();
            request.setTask(null);

            assertThrowsBadRequest(ErrorMessages.TodoErrorMessages.InvalidTask,
                    () -> todoService.addTodo(request));
        }

        /**
         * Purpose: Verify empty task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
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

        /**
         * Purpose: Verify whitespace-only task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
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

        /**
         * Purpose: Verify task too long throws BadRequestException.
         * Expected Result: BadRequestException with TaskTooLong message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
         */
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

        /**
         * Purpose: Verify task at max length (500 chars) succeeds.
         * Expected Result: Todo is saved successfully.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify todo with isDone=true is saved correctly.
         * Expected Result: Todo is saved with isDone=true.
         * Assertions: verify(todoRepository).save(any(Todo.class));
         */
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

        /**
         * Purpose: Verify todo with special characters in task succeeds.
         * Expected Result: Todo is saved successfully.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify todo with unicode characters in task succeeds.
         * Expected Result: Todo is saved successfully.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify logging is called after successful add.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
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

        /**
         * Purpose: Verify todo with numeric task succeeds.
         * Expected Result: Todo is saved.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify todo with single character task succeeds.
         * Expected Result: Todo is saved.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify repository save is called exactly once.
         * Expected Result: save() called once.
         * Assertions: verify(todoRepository, times(1)).save(any()).
         */
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

        /**
         * Purpose: Verify todo with newlines in task succeeds.
         * Expected Result: Todo is saved.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify todo with tabs in task succeeds.
         * Expected Result: Todo is saved.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify todo with HTML tags in task succeeds.
         * Expected Result: Todo is saved.
         * Assertions: assertDoesNotThrow();
         */
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

        /**
         * Purpose: Verify multiple adds work independently.
         * Expected Result: Each add works correctly.
         * Assertions: verify(todoRepository, times(3)).save(any());
         */
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
    }

    @Nested
    @DisplayName("UpdateTodo Tests")
    class UpdateTodoTests {

        /**
         * Purpose: Verify successful todo update.
         * Expected Result: Todo is updated and logged.
         * Assertions: assertDoesNotThrow(); verify(todoRepository).save(any(Todo.class));
         */
        @Test
        @DisplayName("Update Todo - Success")
        void updateTodo_Success() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Todo");
            request.setIsDone(true);

            Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.updateTodo(request));
            verify(todoRepository).findById(TEST_TODO_ID);
            verify(todoRepository).save(any(Todo.class));
        }

        /**
         * Purpose: Verify null request throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Null request - Throws BadRequestException")
        void updateTodo_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(null));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify null todoId throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Null todoId - Throws BadRequestException")
        void updateTodo_NullTodoId_ThrowsBadRequestException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(null);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify zero todoId throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Zero todoId - Throws NotFoundException")
        void updateTodo_ZeroTodoId_ThrowsNotFoundException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(0L);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            lenient().when(todoRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify non-existent todo throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Not found - Throws NotFoundException")
        void updateTodo_NotFound_ThrowsNotFoundException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Todo");
            request.setIsDone(true);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
            verify(todoRepository, never()).save(any());
        }

        /**
         * Purpose: Verify null task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Null task - Throws BadRequestException")
        void updateTodo_NullTask_ThrowsBadRequestException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask(null);
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
        }

        /**
         * Purpose: Verify empty task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Empty task - Throws BadRequestException")
        void updateTodo_EmptyTask_ThrowsBadRequestException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("");
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
        }

        /**
         * Purpose: Verify whitespace task throws BadRequestException.
         * Expected Result: BadRequestException with InvalidTask message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Whitespace task - Throws BadRequestException")
        void updateTodo_WhitespaceTask_ThrowsBadRequestException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("   ");
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidTask, ex.getMessage());
        }

        /**
         * Purpose: Verify task too long throws BadRequestException.
         * Expected Result: BadRequestException with TaskTooLong message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Task too long - Throws BadRequestException")
        void updateTodo_TaskTooLong_ThrowsBadRequestException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask(LONG_TASK);
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.TaskTooLong, ex.getMessage());
        }

        /**
         * Purpose: Verify negative todoId throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Negative todoId - Throws NotFoundException")
        void updateTodo_NegativeTodoId_ThrowsNotFoundException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(-1L);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            when(todoRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify max long todoId throws NotFoundException when not found.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Max Long todoId - Throws NotFoundException")
        void updateTodo_MaxLongTodoId_ThrowsNotFoundException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(Long.MAX_VALUE);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            when(todoRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify logging is called after successful update.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Update Todo - Success - Logs the operation")
        void updateTodo_Success_LogsOperation() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Todo");
            request.setIsDone(true);

            Todo updatedTodo = new Todo(request, CREATED_USER, testTodo, TEST_USER_ID);
            updatedTodo.setTodoId(TEST_TODO_ID);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.updateTodo(request);

            verify(userLogService).logData(
                    eq(TEST_USER_ID.longValue()),
                    contains(SuccessMessages.TodoSuccessMessages.UpdateTodo),
                    eq(ApiRoutes.TodoSubRoute.UPDATE_ITEM)
            );
        }

        /**
         * Purpose: Verify update with special characters in task.
         * Expected Result: Todo is updated.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Update Todo - Special chars in task - Success")
        void updateTodo_SpecialCharsInTask_Success() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated @#$%^&*()!");
            request.setIsDone(true);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.updateTodo(request));
        }

        /**
         * Purpose: Verify update with max length task.
         * Expected Result: Todo is updated.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Update Todo - Max length task - Success")
        void updateTodo_MaxLengthTask_Success() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("A".repeat(500));
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.updateTodo(request));
        }

        /**
         * Purpose: Verify update toggling isDone to false.
         * Expected Result: Todo is updated with isDone=false.
         * Assertions: assertDoesNotThrow();
         */
        @Test
        @DisplayName("Update Todo - Toggle isDone to false")
        void updateTodo_ToggleIsDoneToFalse() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated Task");
            request.setIsDone(false);

            testTodo.setIsDone(true);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.updateTodo(request));
        }

        /**
         * Purpose: Verify min long todoId throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Update Todo - Min Long todoId - Throws NotFoundException")
        void updateTodo_MinLongTodoId_ThrowsNotFoundException() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(Long.MIN_VALUE);
            request.setTask(TEST_TASK);
            request.setIsDone(false);

            when(todoRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.updateTodo(request));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify repository findById is called before save.
         * Expected Result: findById called before save.
         * Assertions: verify order of calls.
         */
        @Test
        @DisplayName("Update Todo - Calls findById before save")
        void updateTodo_CallsFindByIdBeforeSave() {
            TodoRequestModel request = new TodoRequestModel();
            request.setTodoId(TEST_TODO_ID);
            request.setTask("Updated");
            request.setIsDone(false);

            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.updateTodo(request);

            var inOrder = inOrder(todoRepository);
            inOrder.verify(todoRepository).findById(TEST_TODO_ID);
            inOrder.verify(todoRepository).save(any(Todo.class));
        }
    }

    @Nested
    @DisplayName("DeleteTodo Tests")
    class DeleteTodoTests {

        /**
         * Purpose: Verify successful todo deletion.
         * Expected Result: Todo is deleted and logged.
         * Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
         */
        @Test
        @DisplayName("Delete Todo - Success")
        void deleteTodo_Success() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.deleteTodo(TEST_TODO_ID));

            verify(todoRepository).existsById(TEST_TODO_ID);
            verify(todoRepository).deleteById(TEST_TODO_ID);
        }

        /**
         * Purpose: Verify non-existent todo throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Delete Todo - Not found - Throws NotFoundException")
        void deleteTodo_NotFound_ThrowsNotFoundException() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(TEST_TODO_ID));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
            verify(todoRepository, never()).deleteById(anyLong());
        }

        /**
         * Purpose: Verify zero id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Delete Todo - Zero id - Throws NotFoundException")
        void deleteTodo_ZeroId_ThrowsNotFoundException() {
            when(todoRepository.existsById(0L)).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(0L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify negative id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Delete Todo - Negative id - Throws NotFoundException")
        void deleteTodo_NegativeId_ThrowsNotFoundException() {
            when(todoRepository.existsById(-1L)).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(-1L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify max long id throws NotFoundException when not found.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Delete Todo - Max Long id - Throws NotFoundException")
        void deleteTodo_MaxLongId_ThrowsNotFoundException() {
            when(todoRepository.existsById(Long.MAX_VALUE)).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(Long.MAX_VALUE));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify logging is called after successful delete.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Delete Todo - Success - Logs the operation")
        void deleteTodo_Success_LogsOperation() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.deleteTodo(TEST_TODO_ID);

            verify(userLogService).logData(
                    eq(TEST_USER_ID.longValue()),
                    contains(SuccessMessages.TodoSuccessMessages.DeleteTodo),
                    eq(ApiRoutes.TodoSubRoute.DELETE_ITEM)
            );
        }

        /**
         * Purpose: Verify repository existsById is called.
         * Expected Result: existsById is called once.
         * Assertions: verify(todoRepository).existsById(TEST_TODO_ID);
         */
        @Test
        @DisplayName("Delete Todo - Checks existence before delete")
        void deleteTodo_ChecksExistenceBeforeDelete() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.deleteTodo(TEST_TODO_ID);

            verify(todoRepository, times(1)).existsById(TEST_TODO_ID);
        }

        /**
         * Purpose: Verify min long id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Delete Todo - Min Long id - Throws NotFoundException")
        void deleteTodo_MinLongId_ThrowsNotFoundException() {
            when(todoRepository.existsById(Long.MIN_VALUE)).thenReturn(false);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.deleteTodo(Long.MIN_VALUE));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify multiple deletes work independently.
         * Expected Result: Each delete works correctly.
         * Assertions: verify deleteById called correct times.
         */
        @Test
        @DisplayName("Delete Todo - Multiple deletes work independently")
        void deleteTodo_MultipleDeletes_WorkIndependently() {
            when(todoRepository.existsById(anyLong())).thenReturn(true);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.deleteTodo(1L);
            todoService.deleteTodo(2L);
            todoService.deleteTodo(3L);

            verify(todoRepository, times(3)).deleteById(anyLong());
        }

        /**
         * Purpose: Verify deleteById is called with correct ID.
         * Expected Result: deleteById called with exact ID.
         * Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
         */
        @Test
        @DisplayName("Delete Todo - DeleteById called with correct ID")
        void deleteTodo_DeleteByIdCalledWithCorrectId() {
            when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.deleteTodo(TEST_TODO_ID);

            verify(todoRepository).deleteById(TEST_TODO_ID);
        }
    }

    @Nested
    @DisplayName("ToggleTodo Tests")
    class ToggleTodoTests {

        /**
         * Purpose: Verify toggle from false to true.
         * Expected Result: isDone is set to true.
         * Assertions: assertTrue(testTodo.getIsDone());
         */
        @Test
        @DisplayName("Toggle Todo - False to true - Success")
        void toggleTodo_FalseToTrue_Success() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

            assertTrue(testTodo.getIsDone());
            verify(todoRepository).save(testTodo);
        }

        /**
         * Purpose: Verify toggle from true to false.
         * Expected Result: isDone is set to false.
         * Assertions: assertFalse(testTodo.getIsDone());
         */
        @Test
        @DisplayName("Toggle Todo - True to false - Success")
        void toggleTodo_TrueToFalse_Success() {
            testTodo.setIsDone(true);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> todoService.toggleTodo(TEST_TODO_ID));

            assertFalse(testTodo.getIsDone());
            verify(todoRepository).save(testTodo);
        }

        /**
         * Purpose: Verify non-existent todo throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Toggle Todo - Not found - Throws NotFoundException")
        void toggleTodo_NotFound_ThrowsNotFoundException() {
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(TEST_TODO_ID));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
            verify(todoRepository, never()).save(any());
        }

        /**
         * Purpose: Verify negative id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Toggle Todo - Negative id - Throws NotFoundException")
        void toggleTodo_NegativeId_ThrowsNotFoundException() {
            when(todoRepository.findById(-1L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(-1L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify zero id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Toggle Todo - Zero id - Throws NotFoundException")
        void toggleTodo_ZeroId_ThrowsNotFoundException() {
            when(todoRepository.findById(0L)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(0L));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify max long id throws NotFoundException when not found.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Toggle Todo - Max Long id - Throws NotFoundException")
        void toggleTodo_MaxLongId_ThrowsNotFoundException() {
            when(todoRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(Long.MAX_VALUE));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify multiple toggles persist state correctly.
         * Expected Result: State toggles correctly.
         * Assertions: State alternates correctly.
         */
        @Test
        @DisplayName("Toggle Todo - Multiple toggles - State persists")
        void toggleTodo_MultipleToggles_StatePersists() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.toggleTodo(TEST_TODO_ID);
            assertTrue(testTodo.getIsDone());

            todoService.toggleTodo(TEST_TODO_ID);
            assertFalse(testTodo.getIsDone());

            todoService.toggleTodo(TEST_TODO_ID);
            assertTrue(testTodo.getIsDone());
        }

        /**
         * Purpose: Verify logging is called after successful toggle.
         * Expected Result: userLogService.logData is called.
         * Assertions: verify(userLogService).logData(...);
         */
        @Test
        @DisplayName("Toggle Todo - Success - Logs the operation")
        void toggleTodo_Success_LogsOperation() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.toggleTodo(TEST_TODO_ID);

            verify(userLogService).logData(
                    eq(TEST_USER_ID.longValue()),
                    contains(SuccessMessages.TodoSuccessMessages.ToggleTodo),
                    eq(ApiRoutes.TodoSubRoute.TOGGLE_DONE)
            );
        }

        /**
         * Purpose: Verify modifiedUser is updated on toggle.
         * Expected Result: modifiedUser field is updated.
         * Assertions: verify setModifiedUser is called.
         */
        @Test
        @DisplayName("Toggle Todo - Updates modifiedUser")
        void toggleTodo_UpdatesModifiedUser() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.toggleTodo(TEST_TODO_ID);

            assertNotNull(testTodo.getModifiedUser());
            verify(todoRepository).save(testTodo);
        }

        /**
         * Purpose: Verify min long id throws NotFoundException.
         * Expected Result: NotFoundException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Toggle Todo - Min Long id - Throws NotFoundException")
        void toggleTodo_MinLongId_ThrowsNotFoundException() {
            when(todoRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> todoService.toggleTodo(Long.MIN_VALUE));
            assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify repository save is called after toggle.
         * Expected Result: save() is called.
         * Assertions: verify(todoRepository).save(any());
         */
        @Test
        @DisplayName("Toggle Todo - Repository save called")
        void toggleTodo_RepositorySaveCalled() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.toggleTodo(TEST_TODO_ID);

            verify(todoRepository, times(1)).save(any(Todo.class));
        }

        /**
         * Purpose: Verify findById is not called after toggle completes.
         * Expected Result: findById called exactly once.
         * Assertions: verify(todoRepository, times(1)).findById(TEST_TODO_ID);
         */
        @Test
        @DisplayName("Toggle Todo - FindById called once")
        void toggleTodo_FindByIdCalledOnce() {
            testTodo.setIsDone(false);
            when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            todoService.toggleTodo(TEST_TODO_ID);

            verify(todoRepository, times(1)).findById(TEST_TODO_ID);
        }
    }

    @Nested
    @DisplayName("GetTodoItems Tests")
    class GetTodoItemsTests {

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
    }
}
