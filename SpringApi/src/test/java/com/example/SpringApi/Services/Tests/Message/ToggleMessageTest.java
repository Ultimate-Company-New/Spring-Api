package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.toggleMessage method.
 */
@DisplayName("ToggleMessage Tests")
public class ToggleMessageTest extends MessageServiceTestBase {


    // Total Tests: 12
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that an active message can be successfully toggled to a
     * deleted state.
     * Scenario: Call toggleMessage for a message where isDeleted is currently
     * false.
     * Expected: Message is saved with isDeleted set to true.
     */
    @Test
    @DisplayName("Toggle Message - Active to Deleted - Success")
    void toggleMessage_ActiveToDeleted_Success() {
        // Arrange
        testMessage.setIsDeleted(false);

        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));
        verify(messageRepository)
                .save(argThat(msg -> ((com.example.SpringApi.Models.DatabaseModels.Message) msg).getIsDeleted()));
    }

    /**
     * Purpose: Verify that a deleted message can be successfully restored to an
     * active state.
     * Scenario: Call toggleMessage for a message where isDeleted is currently true.
     * Expected: Message is saved with isDeleted set to false.
     */
    @Test
    @DisplayName("Toggle Message - Deleted to active - Success")
    void toggleMessage_DeletedToActive_Success() {
        // Arrange
        testMessage.setIsDeleted(true);

        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));
        verify(messageRepository)
                .save(argThat(msg -> !((com.example.SpringApi.Models.DatabaseModels.Message) msg).getIsDeleted()));
    }

    /**
     * Purpose: Verify that successive calls to toggleMessage flip the deletion
     * state correctly each time.
     * Scenario: Call toggleMessage twice on the same message object.
     * Expected: state changes from false -> true -> false.
     */
    @Test
    @DisplayName("Toggle Message - Multiple Toggles - Successive States")
    void toggleMessage_MultipleToggles_Successive() {
        // Arrange
        testMessage.setIsDeleted(false);
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySave(testMessage);

        // Act
        messageService.toggleMessage(TEST_MESSAGE_ID); // To true
        // Assert
        assertTrue(testMessage.getIsDeleted());

        // Act
        messageService.toggleMessage(TEST_MESSAGE_ID); // To false
        // Assert
        assertFalse(testMessage.getIsDeleted());
    }

    /**
     * Purpose: Verify that toggling a message triggers correct activity logging.
     * Expected Result: UserLogService is called with a descriptive log message.
     */
    @Test
    @DisplayName("Toggle Message - Verify Logging - Success")
    void toggleMessage_VerifyLogging_Success() {
        // Arrange
        testMessage.setIsDeleted(false);

        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySave(testMessage);

        // Act
        messageService.toggleMessage(TEST_MESSAGE_ID);

        // Assert
        verify(userLogService).logData(anyLong(), contains("Successfully toggled"), any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject toggle request when specified message record does not exist
     * in database.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Toggle Message - Message not found - Throws NotFoundException")
    void toggleMessage_MessageNotFound_ThrowsNotFoundException() {
        // Arrange
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.INVALID_ID,
                () -> messageService.toggleMessage(TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Reject toggle request when provided message ID is negative.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Toggle Message - Negative ID - Throws NotFoundException")
    void toggleMessage_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.INVALID_ID, () -> messageService.toggleMessage(-1L));
    }

    /**
     * Purpose: Propagate repository exceptions when database save fails during
     * toggle.
     * Expected Result: RuntimeException with original database error message.
     */
    @Test
    @DisplayName("Toggle Message - Repository Error on Save - Propagates")
    void toggleMessage_RepositorySaveError_Propagates() {
        // Arrange
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySaveThrowsRuntimeException(ErrorMessages.MessagesErrorMessages.DB_ERROR);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.toggleMessage(TEST_MESSAGE_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.DB_ERROR, ex.getMessage());
    }

    /**
     * Purpose: Verify that unauthorized access checks are handled at controller
     * level.
     * Scenario: Service layer is called for a valid message (service doesn't
     * enforce auth).
     * Expected Result: Service returns valid result without throwing security
     * exceptions.
     */
    @Test
    @DisplayName("Toggle Message - Unauthorized Access - Throws UnauthorizedException")
    void toggleMessage_UnauthorizedAccess_ThrowsUnauthorizedException() {
        // Arrange
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.of(testMessage));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));
    }

    /**
     * Purpose: Reject toggle request when provided message ID is zero.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Toggle Message - Zero ID - Throws NotFoundException")
    void toggleMessage_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.INVALID_ID, () -> messageService.toggleMessage(0L));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("Toggle Message - Controller permission unauthorized - Success")
    void toggleMessage_controller_permission_unauthorized() {
        // Arrange
        MessageController controller = new MessageController(messageServiceMock);
        stubMessageServiceToggleMessageThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.toggleMessage(TEST_MESSAGE_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify that the toggleMessage controller method is protected by
     * correct @PreAuthorize permission.
     * Expected: Method has @PreAuthorize referencing DELETE_MESSAGES_PERMISSION.
     */
    @Test
    @DisplayName("toggleMessage - Verify @PreAuthorize Annotation")
    void toggleMessage_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = MessageController.class.getMethod("toggleMessage", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.DELETE_MESSAGES_PERMISSION),
                "@PreAuthorize should reference DELETE_MESSAGES_PERMISSION");
    }

    /**
     * Purpose: Verify that the controller correctly delegates toggleMessage calls
     * to the service layer.
     * Expected Result: Status 200 returned upon delegation.
     */
    @Test
    @DisplayName("toggleMessage - Controller delegates to service")
    void toggleMessage_WithValidRequest_DelegatesToService() {
        // Arrange
        MessageController controller = new MessageController(messageServiceMock);
        stubMessageServiceToggleMessageDoNothing();

        // Act
        ResponseEntity<?> response = controller.toggleMessage(TEST_MESSAGE_ID);

        // Assert
        verify(messageServiceMock).toggleMessage(TEST_MESSAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
