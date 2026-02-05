package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.getMessageDetailsById method.
 * Test Count: 10 tests
 */
@DisplayName("GetMessageDetailsById Tests")
public class GetMessageDetailsByIdTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Message Details By ID - Mapping Verification - Success")
    void getMessageDetailsById_MappingVerification_Success() {
        testMessage.setDescriptionHtml("<b>Test</b>");
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertEquals("<b>Test</b>", result.getDescriptionHtml());
        assertEquals(TEST_TITLE, result.getTitle());
    }

    @Test
    @DisplayName("Get Message Details By ID - Permission check - Success Verifies Authorization")
    void getMessageDetailsById_PermissionCheck_SuccessVerifiesAuthorization() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(true);

        messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Message Details By ID - Success")
    void getMessageDetailsById_Success() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getMessageId());
        assertEquals(TEST_TITLE, result.getTitle());
    }

    @Test
    @DisplayName("Get Message Details By ID - Verify SendAsEmail flag - Success")
    void getMessageDetailsById_VerifySendAsEmail_Success() {
        testMessage.setSendAsEmail(true);
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);
        assertTrue(result.getSendAsEmail());
    }

    @Test
    @DisplayName("Get Message Details By ID - With Targets - Success")
    void getMessageDetailsById_WithTargets_Success() {
        // Logic check: ensure the query for targets is called
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));
        
        assertDoesNotThrow(() -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        verify(messageRepository).findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Message Details By ID - Message not found - Throws NotFoundException")
    void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Get Message Details By ID - Negative message ID - Throws BadRequestException")
    void getMessageDetailsById_NegativeMessageId_ThrowsBadRequestException() {
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(-1L));
    }

    @Test
    @DisplayName("Get Message Details By ID - Repository Error - Propagates Exception")
    void getMessageDetailsById_RepositoryError_Propagates() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(anyLong(), anyLong()))
            .thenThrow(new RuntimeException("Lookup failed"));
        assertThrows(RuntimeException.class, () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Get Message Details By ID - Unauthorized Access - Throws UnauthorizedException")
    void getMessageDetailsById_UnauthorizedAccess_ThrowsUnauthorizedException() {
        when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));

        verify(authorization).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Message Details By ID - Zero message ID - ThrowsBadRequestException")
    void getMessageDetailsById_ZeroMessageId_ThrowsBadRequestException() {
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(0L));
    }
}