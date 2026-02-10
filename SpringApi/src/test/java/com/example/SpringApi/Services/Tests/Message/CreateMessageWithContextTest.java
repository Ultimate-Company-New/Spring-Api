package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.createMessageWithContext method.
 */
@DisplayName("CreateMessageWithContext Tests")
class CreateMessageWithContextTest extends MessageServiceTestBase {

    // Total Tests: 5

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful message creation with explicit user and client
     * context.
     * Scenario: CreateMessageWithContext with valid request and context parameters.
     * Expected: Message created and logged with provided context.
     */
    @Test
    @DisplayName("Create Message With Context - Valid Context - Success")
    void createMessageWithContext_ValidContext_Success() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act & Assert
        assertDoesNotThrow(
                () -> messageService.createMessageWithContext(validRequest, 999L, "system_user", TEST_CLIENT_ID));
        verify(userLogService).logDataWithContext(eq(999L), eq("system_user"), eq(TEST_CLIENT_ID), anyString(), any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject context message creation when requesting user ID is invalid.
     * Expected Result: BadRequestException with InvalidUserId message.
     */
    @Test
    @DisplayName("Create Message With Context - Invalid UserId - Throws BadRequestException")
    void createMessageWithContext_InvalidUserId_ThrowsBadRequestException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessageWithContext(validRequest, 0L, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidUserId, exception.getMessage());
    }

    /**
     * Purpose: Reject context message creation when requesting user ID is negative.
     * Expected Result: BadRequestException with InvalidUserId message.
     */
    @Test
    @DisplayName("Create Message With Context - Negative UserId - Throws BadRequestException")
    void createMessageWithContext_NegativeUserId_ThrowsBadRequestException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessageWithContext(validRequest, -1L, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidUserId, exception.getMessage());
    }

    /**
     * Purpose: Reject context message creation when request model is null.
     * Expected Result: BadRequestException with InvalidId error code.
     */
    @Test
    @DisplayName("Create Message With Context - Null Request - Throws BadRequestException")
    void createMessageWithContext_NullRequest_ThrowsBadRequestException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessageWithContext(null, TEST_USER_ID, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject context message creation for unauthorized/non-existent client
     * ID.
     * Expected Result: NotFoundException with client invalid ID message.
     */
    @Test
    @DisplayName("Create Message With Context - Unauthorized Client - Throws NotFoundException")
    void createMessageWithContext_UnauthorizedClient_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> messageService.createMessageWithContext(validRequest, TEST_USER_ID, "admin", 9999L));
    }
}
