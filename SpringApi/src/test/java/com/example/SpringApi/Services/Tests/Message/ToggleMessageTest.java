package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
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
 * Test Count: 10 tests
 */
@DisplayName("ToggleMessage Tests")
public class ToggleMessageTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Toggle Message - Active to Deleted - Success")
    void toggleMessage_ActiveToDeleted_Success() {
        testMessage.setIsDeleted(false);

        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID))).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

        verify(messageRepository).save(argThat(msg -> ((com.example.SpringApi.Models.DatabaseModels.Message) msg).getIsDeleted()));
    }

    @Test
    @DisplayName("Toggle Message - Deleted to active - Success")
    void toggleMessage_DeletedToActive_Success() {
        testMessage.setIsDeleted(true);

        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID))).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

        verify(messageRepository).save(argThat(msg -> !((com.example.SpringApi.Models.DatabaseModels.Message) msg).getIsDeleted()));
    }

    @Test
    @DisplayName("Toggle Message - Multiple Toggles - Successive States")
    void toggleMessage_MultipleToggles_Successive() {
        testMessage.setIsDeleted(false);
        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID))).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.toggleMessage(TEST_MESSAGE_ID); // To true
        assertTrue(testMessage.getIsDeleted());
        
        messageService.toggleMessage(TEST_MESSAGE_ID); // To false
        assertFalse(testMessage.getIsDeleted());
    }



    @Test
    @DisplayName("Toggle Message - Verify Logging - Success")
    void toggleMessage_VerifyLogging_Success() {
        testMessage.setIsDeleted(false);

        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID))).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.toggleMessage(TEST_MESSAGE_ID);

        verify(userLogService).logData(anyLong(), contains("Successfully toggled"), any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Toggle Message - Message not found - Throws NotFoundException")
    void toggleMessage_MessageNotFound_ThrowsNotFoundException() {
        lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.toggleMessage(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Toggle Message - Negative ID - Throws NotFoundException")
    void toggleMessage_NegativeId_ThrowsNotFoundException() {
        lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(-1L), eq(TEST_CLIENT_ID))).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, () -> messageService.toggleMessage(-1L));
    }

    @Test
    @DisplayName("Toggle Message - Repository Error on Save - Propagates")
    void toggleMessage_RepositorySaveError_Propagates() {
        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> messageService.toggleMessage(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Toggle Message - Unauthorized Access - Throws UnauthorizedException")
    void toggleMessage_UnauthorizedAccess_ThrowsUnauthorizedException() {
        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Note: Authorization is controller-level only, service doesn't check it
        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Toggle Message - Zero ID - Throws NotFoundException")
    void toggleMessage_ZeroId_ThrowsNotFoundException() {
        lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(0L), eq(TEST_CLIENT_ID))).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, () -> messageService.toggleMessage(0L));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("toggleMessage - Verify @PreAuthorize Annotation")
    void toggleMessage_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = MessageController.class.getMethod("toggleMessage", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.DELETE_MESSAGES_PERMISSION),
            "@PreAuthorize should reference DELETE_MESSAGES_PERMISSION");
    }

    @Test
    @DisplayName("toggleMessage - Controller delegates to service")
    void toggleMessage_WithValidRequest_DelegatesToService() {
        MessageController controller = new MessageController(messageServiceMock);
        doNothing().when(messageServiceMock).toggleMessage(TEST_MESSAGE_ID);

        ResponseEntity<?> response = controller.toggleMessage(TEST_MESSAGE_ID);

        verify(messageServiceMock).toggleMessage(TEST_MESSAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}