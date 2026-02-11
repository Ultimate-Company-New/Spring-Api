package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.setMessageReadByUserIdAndMessageId method.
 */
@DisplayName("SetMessageRead Tests")
public class SetMessageReadTest extends MessageServiceTestBase {

    // Total Tests: 13

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that marking a message as read multiple times does not result
     * in duplicate mapping records.
     * Scenario: MessageUserReadMap already exists for the given user and message.
     * Expected Result: Method executes without error and doesn't call save() again.
     */
    @Test
    @DisplayName("Set Message Read - Already read - No duplicate")
    void setMessageReadByUserIdAndMessageId_AlreadyRead_NoDuplicate() {
        // Arrange
        MessageUserReadMap existingRead = new MessageUserReadMap();

        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.of(testMessage));
        stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(existingRead);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
        verify(messageUserReadMapRepository, never()).save(any(MessageUserReadMap.class));
    }

    /**
     * Purpose: Verify that a message can be successfully marked as read for a given
     * user.
     * Scenario: Call setMessageReadByUserIdAndMessageId for a user and message that
     * haven't been marked read yet.
     * Expected Result: A new MessageUserReadMap record is saved to the database.
     */
    @Test
    @DisplayName("Set Message Read - Success")
    void setMessageReadByUserIdAndMessageId_Success() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.of(testMessage));
        stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(null);
        stubMessageUserReadMapRepositorySave(new MessageUserReadMap());

        // Act & Assert
        assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
        verify(messageUserReadMapRepository).save(any(MessageUserReadMap.class));
    }

    /**
     * Purpose: Verify that marking a message as read triggers correct activity
     * logging.
     * Expected Result: UserLogService is called with a descriptive log message.
     */
    @Test
    @DisplayName("Set Message Read - Verify Log Content - Success")
    void setMessageReadByUserIdAndMessageId_VerifyLogContent_Success() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.of(testMessage));
        stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(null);

        // Act
        messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        // Assert
        verify(userLogService).logData(eq(TEST_USER_ID), contains("marked message as read"), any());
    }

    /**
     * Purpose: Verify that the correct user and message IDs are persisted in the
     * read mapping record.
     * Scenario: Capture the save() argument and inspect field values.
     * Expected Result: Persisted object should contain provided user ID and message
     * ID.
     */
    @Test
    @DisplayName("Set Message Read - Verify Mapping Data - Success")
    void setMessageReadByUserIdAndMessageId_VerifyMappingData_Success() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.of(testMessage));
        stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(null);

        // Act
        messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        // Assert
        ArgumentCaptor<MessageUserReadMap> captor = ArgumentCaptor.forClass(MessageUserReadMap.class);
        verify(messageUserReadMapRepository).save(captor.capture());
        assertEquals(TEST_MESSAGE_ID, captor.getValue().getMessageId());
        assertEquals(TEST_USER_ID, captor.getValue().getUserId());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject request when provided message ID is negative.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - Message ID Negative - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageIdNegative_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, -1L));
    }

    /**
     * Purpose: Reject request when provided message ID is zero.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - Message ID Zero - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageIdZero_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, 0L));
    }

    /**
     * Purpose: Reject request when specified message record does not exist in
     * database.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - Message not found - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageNotFound_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubMessageRepositoryFindByMessageIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Reject request when provided user ID is negative.
     * Expected Result: NotFoundException with user invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - User ID Negative - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserIdNegative_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(-1L, TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Reject request when provided user ID is zero.
     * Expected Result: NotFoundException with user invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - User ID Zero - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserIdZero_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(0L, TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Reject request when specified user record does not exist in
     * database.
     * Expected Result: NotFoundException with user invalid ID error code.
     */
    @Test
    @DisplayName("Set Message Read - User not found - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientId(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Propagate repository exceptions when user lookup fails.
     * Expected Result: RuntimeException with original database error message.
     */
    @Test
    @DisplayName("Set Message Read - User Repository Exception - Propagates Exception")
    void setMessageReadByUserIdAndMessageId_UserRepositoryException_Propagates() {
        // Arrange
        stubUserRepositoryFindByUserIdAndClientIdThrows("DB Error");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
        assertEquals("DB Error", ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the setMessageReadByUserIdAndMessageId controller method
     * is protected by correct @PreAuthorize permission.
     * Expected: Method has @PreAuthorize referencing VIEW_MESSAGES_PERMISSION.
     */
    @Test
    @DisplayName("setMessageReadByUserIdAndMessageId - Verify @PreAuthorize Annotation")
    void setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = MessageController.class.getMethod("setMessageReadByUserIdAndMessageId", Long.class, Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_MESSAGES_PERMISSION),
                "@PreAuthorize should reference VIEW_MESSAGES_PERMISSION");
    }

    /**
     * Purpose: Verify that the controller correctly delegates
     * setMessageReadByUserIdAndMessageId calls to the service layer.
     * Expected Result: Status 200 returned upon delegation.
     */
    @Test
    @DisplayName("setMessageReadByUserIdAndMessageId - Controller delegates to service")
    void setMessageReadByUserIdAndMessageId_WithValidRequest_DelegatesToService() {
        // Arrange
        MessageController controller = new MessageController(messageServiceMock);
        stubMessageServiceSetMessageReadByUserIdAndMessageIdDoNothing();

        // Act
        ResponseEntity<?> response = controller.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        // Assert
        verify(messageServiceMock).setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}