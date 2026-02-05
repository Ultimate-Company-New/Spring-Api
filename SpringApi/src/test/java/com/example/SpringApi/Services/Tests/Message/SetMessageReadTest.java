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
 * Test Count: 12 tests
 */
@DisplayName("SetMessageRead Tests")
public class SetMessageReadTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Set Message Read - Already read - No duplicate")
    void setMessageReadByUserIdAndMessageId_AlreadyRead_NoDuplicate() {
        MessageUserReadMap existingRead = new MessageUserReadMap();

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(existingRead);

        assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));

        verify(messageUserReadMapRepository, never()).save(any(MessageUserReadMap.class));
    }



    @Test
    @DisplayName("Set Message Read - Success")
    void setMessageReadByUserIdAndMessageId_Success() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(null);
        when(messageUserReadMapRepository.save(any(MessageUserReadMap.class))).thenReturn(new MessageUserReadMap());

        assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));

        verify(messageUserReadMapRepository).save(any(MessageUserReadMap.class));
    }

    @Test
    @DisplayName("Set Message Read - Verify Log Content - Success")
    void setMessageReadByUserIdAndMessageId_VerifyLogContent_Success() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(null);

        messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        verify(userLogService).logData(eq(TEST_USER_ID), contains("marking message as read"), any());
    }

    @Test
    @DisplayName("Set Message Read - Verify Mapping Data - Success")
    void setMessageReadByUserIdAndMessageId_VerifyMappingData_Success() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(null);

        messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

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

    @Test
    @DisplayName("Set Message Read - Message ID Negative - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageIdNegative_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, -1L));
    }

    @Test
    @DisplayName("Set Message Read - Message ID Zero - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageIdZero_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, 0L));
    }

    @Test
    @DisplayName("Set Message Read - Message not found - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_MessageNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Set Message Read - User ID Negative - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserIdNegative_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(-1L, TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Set Message Read - User ID Zero - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserIdZero_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(0L, TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Set Message Read - User not found - Throws NotFoundException")
    void setMessageReadByUserIdAndMessageId_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId, 
            () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Set Message Read - User Repository Exception - Propagates Exception")
    void setMessageReadByUserIdAndMessageId_UserRepositoryException_Propagates() {
        when(userRepository.findByUserIdAndClientId(anyLong(), anyLong())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("setMessageReadByUserIdAndMessageId - Verify @PreAuthorize Annotation")
    void setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = MessageController.class.getMethod("setMessageReadByUserIdAndMessageId", Long.class, Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_MESSAGES_PERMISSION),
            "@PreAuthorize should reference VIEW_MESSAGES_PERMISSION");
    }

    @Test
    @DisplayName("setMessageReadByUserIdAndMessageId - Controller delegates to service")
    void setMessageReadByUserIdAndMessageId_WithValidRequest_DelegatesToService() {
        MessageController controller = new MessageController(messageService);
        doNothing().when(messageService).setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        ResponseEntity<?> response = controller.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);

        verify(messageService).setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}