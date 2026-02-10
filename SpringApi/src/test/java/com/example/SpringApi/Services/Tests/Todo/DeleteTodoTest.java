package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - DeleteTodo operation.
 * Contains 10 tests: 5 SUCCESS + 5 FAILURE/EXCEPTION cases.
 */
@DisplayName("TodoService - DeleteTodo Tests")
public class DeleteTodoTest extends TodoServiceTestBase {

    // Total Tests: 10

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify repository existsById is called.
     * Expected Result: existsById is called once.
     * Assertions: verify(todoRepository).existsById(TEST_TODO_ID);
     */
    @Test
    @DisplayName("Delete Todo - Checks existence before delete")
    void deleteTodo_ChecksExistenceBeforeDelete() {
        // Arrange
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(todoRepository, times(1)).existsById(TEST_TODO_ID);
    }

    /**
     * Purpose: Verify deleteById is called with correct ID.
     * Expected Result: deleteById called with exact ID.
     * Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
     */
    @Test
    @DisplayName("Delete Todo - DeleteById called with correct ID")
    void deleteTodo_DeleteByIdCalledWithCorrectId() {
        // Arrange
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(todoRepository).deleteById(TEST_TODO_ID);
    }

    /**
     * Purpose: Verify multiple deletes work independently.
     * Expected Result: Each delete works correctly.
     * Assertions: verify deleteById called correct times.
     */
    @Test
    @DisplayName("Delete Todo - Multiple deletes work independently")
    void deleteTodo_MultipleDeletes_WorkIndependently() {
        // Arrange
        when(todoRepository.existsById(anyLong())).thenReturn(true);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.deleteTodo(1L);
        todoService.deleteTodo(2L);
        todoService.deleteTodo(3L);

        // Assert
        verify(todoRepository, times(3)).deleteById(anyLong());
    }

    /**
     * Purpose: Verify successful todo deletion.
     * Expected Result: Todo is deleted and logged.
     * Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
     */
    @Test
    @DisplayName("Delete Todo - Success")
    void deleteTodo_Success() {
        // Arrange
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> todoService.deleteTodo(TEST_TODO_ID));

        // Assert
        verify(todoRepository).existsById(TEST_TODO_ID);
        verify(todoRepository).deleteById(TEST_TODO_ID);
    }

    /**
     * Purpose: Verify logging is called after successful delete.
     * Expected Result: userLogService.logData is called.
     * Assertions: verify(userLogService).logData(...);
     */
    @Test
    @DisplayName("Delete Todo - Success - Logs the operation")
    void deleteTodo_Success_LogsOperation() {
        // Arrange
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(true);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.DeleteTodo),
                eq(ApiRoutes.TodoSubRoute.DELETE_ITEM));
    }

    // ========================================
    // FAILURE/EXCEPTION TESTS
    // ========================================

    /**
     * Purpose: Verify max long id throws NotFoundException when not found.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Delete Todo - Max Long id - Throws NotFoundException")
    void deleteTodo_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        when(todoRepository.existsById(Long.MAX_VALUE)).thenReturn(false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify min long id throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Delete Todo - Min Long id - Throws NotFoundException")
    void deleteTodo_MinLongId_ThrowsNotFoundException() {
        // Arrange
        when(todoRepository.existsById(Long.MIN_VALUE)).thenReturn(false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(Long.MIN_VALUE));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative id throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Delete Todo - Negative id - Throws NotFoundException")
    void deleteTodo_NegativeId_ThrowsNotFoundException() {
        // Arrange
        when(todoRepository.existsById(-1L)).thenReturn(false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(-1L));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify non-existent todo throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Delete Todo - Not found - Throws NotFoundException")
    void deleteTodo_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(todoRepository.existsById(TEST_TODO_ID)).thenReturn(false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(TEST_TODO_ID));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        verify(todoRepository, never()).deleteById(anyLong());
    }

    /**
     * Purpose: Verify zero id throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Delete Todo - Zero id - Throws NotFoundException")
    void deleteTodo_ZeroId_ThrowsNotFoundException() {
        // Arrange
        when(todoRepository.existsById(0L)).thenReturn(false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(0L));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }
}
