package com.example.SpringApi.Services.Tests.Todo;

import com.example.SpringApi.Controllers.TodoController;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for TodoService - DeleteTodo operation.
 */
@DisplayName("TodoService - DeleteTodo Tests")
public class DeleteTodoTest extends TodoServiceTestBase {

    // Total Tests: 11

    // ========================================
    // Section 1: Success Tests
    // ========================================

    // Purpose: Verify repository existsById is called.
    // Expected Result: existsById is called once.
    // Assertions: verify(todoRepository).existsById(TEST_TODO_ID);
    @Test
    @DisplayName("deleteTodo - Check Existence Before Delete - Success")
    void deleteTodo_checkExistenceBeforeDelete_success() {
        // Arrange
        stubTodoRepositoryExistsById(TEST_TODO_ID, true);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(todoRepository, times(1)).existsById(TEST_TODO_ID);
    }

    // Purpose: Verify deleteById is called with correct ID.
    // Expected Result: deleteById called with exact ID.
    // Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
    @Test
    @DisplayName("deleteTodo - DeleteById Called With Correct ID - Success")
    void deleteTodo_deleteByIdCalledWithCorrectId_success() {
        // Arrange
        stubTodoRepositoryExistsById(TEST_TODO_ID, true);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(todoRepository).deleteById(TEST_TODO_ID);
    }

    // Purpose: Verify multiple deletes work independently.
    // Expected Result: Each delete works correctly.
    // Assertions: verify deleteById called correct times.
    @Test
    @DisplayName("deleteTodo - Multiple Deletes - Success")
    void deleteTodo_multipleDeletes_success() {
        // Arrange
        stubTodoRepositoryExistsByIdAny(true);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.deleteTodo(1L);
        todoService.deleteTodo(2L);
        todoService.deleteTodo(3L);

        // Assert
        verify(todoRepository, times(3)).deleteById(anyLong());
    }

    // Purpose: Verify logging is called after successful delete.
    // Expected Result: userLogService.logData is called.
    // Assertions: verify(userLogService).logData(...);
    @Test
    @DisplayName("deleteTodo - Logs Operation - Success")
    void deleteTodo_success_logsOperation() {
        // Arrange
        stubTodoRepositoryExistsById(TEST_TODO_ID, true);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        todoService.deleteTodo(TEST_TODO_ID);

        // Assert
        verify(userLogService).logData(
                eq(TEST_USER_ID.longValue()),
                contains(SuccessMessages.TodoSuccessMessages.DeleteTodo),
                eq(ApiRoutes.TodoSubRoute.DELETE_ITEM));
    }

    // Purpose: Verify successful todo deletion.
    // Expected Result: Todo is deleted and logged.
    // Assertions: verify(todoRepository).deleteById(TEST_TODO_ID);
    @Test
    @DisplayName("deleteTodo - Valid ID - Success")
    void deleteTodo_validId_success() {
        // Arrange
        stubTodoRepositoryExistsById(TEST_TODO_ID, true);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        assertDoesNotThrow(() -> todoService.deleteTodo(TEST_TODO_ID));

        // Assert
        verify(todoRepository).existsById(TEST_TODO_ID);
        verify(todoRepository).deleteById(TEST_TODO_ID);
    }

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    // Purpose: Verify max long id throws NotFoundException when not found.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("deleteTodo - Max Long ID - Throws NotFoundException")
    void deleteTodo_maxLongId_notFoundException() {
        // Arrange
        stubTodoRepositoryExistsById(Long.MAX_VALUE, false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify min long id throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("deleteTodo - Min Long ID - Throws NotFoundException")
    void deleteTodo_minLongId_notFoundException() {
        // Arrange
        stubTodoRepositoryExistsById(Long.MIN_VALUE, false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(Long.MIN_VALUE));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify negative id throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("deleteTodo - Negative ID - Throws NotFoundException")
    void deleteTodo_negativeId_notFoundException() {
        // Arrange
        stubTodoRepositoryExistsById(-1L, false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(-1L));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // Purpose: Verify non-existent todo throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("deleteTodo - Not Found - Throws NotFoundException")
    void deleteTodo_notFound_notFoundException() {
        // Arrange
        stubTodoRepositoryExistsById(TEST_TODO_ID, false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(TEST_TODO_ID));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
        verify(todoRepository, never()).deleteById(anyLong());
    }

    // Purpose: Verify zero id throws NotFoundException.
    // Expected Result: NotFoundException with InvalidId message.
    // Assertions: assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    @Test
    @DisplayName("deleteTodo - Zero ID - Throws NotFoundException")
    void deleteTodo_zeroId_notFoundException() {
        // Arrange
        stubTodoRepositoryExistsById(0L, false);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> todoService.deleteTodo(0L));

        // Assert
        assertEquals(ErrorMessages.TodoErrorMessages.InvalidId, ex.getMessage());
    }

    // ========================================
    // Section 3: Controller Permission/Auth Tests
    // ========================================

    // Purpose: Verify @PreAuthorize Annotation on controller
    // Expected Result: Annotation exists and has correct value
    // Assertions: assertNotNull, assertEquals
    @Test
    @DisplayName("deleteItem - Verify @PreAuthorize Annotation")
    void deleteItem_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = TodoController.class.getMethod("deleteItem", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "deleteItem method should have @PreAuthorize annotation");
        assertEquals("@customAuthorization.hasAuthority(null)", annotation.value());
    }
}