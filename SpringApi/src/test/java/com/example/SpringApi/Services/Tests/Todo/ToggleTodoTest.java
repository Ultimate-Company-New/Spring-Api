package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.DatabaseModels.Todo;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - ToggleTodo operation.
 * Contains 12 tests: 7 SUCCESS + 5 FAILURE/EXCEPTION cases.
 */
@DisplayName("TodoService - ToggleTodo Tests")
public class ToggleTodoTest extends TodoServiceTestBase {

    // ========================================
    // SUCCESS TESTS
    // ========================================

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

    // ========================================
    // FAILURE/EXCEPTION TESTS
    // ========================================

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
}
