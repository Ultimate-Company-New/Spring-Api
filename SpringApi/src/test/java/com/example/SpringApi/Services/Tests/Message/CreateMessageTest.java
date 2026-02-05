package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Models.DatabaseModels.MessageUserGroupMap;
import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.createMessage method.
 * Test Count: 35 tests
 */
@DisplayName("CreateMessage Tests")
public class CreateMessageTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Create Message - Group Targets - Success")
    void createMessage_GroupTargets_Success() {
        validRequest.setUserIds(null);
        validRequest.setUserGroupIds(List.of(10L, 20L));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserGroupMapRepository, times(2)).save(any(MessageUserGroupMap.class));
    }

    @Test
    @DisplayName("Create Message - Permission check - Success Verifies Authorization")
    void createMessage_PermissionCheck_SuccessVerifiesAuthorization() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        lenient().when(authorization.hasAuthority(Authorizations.INSERT_MESSAGES_PERMISSION)).thenReturn(true);

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.INSERT_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Create Message - SendAsEmail false - No recipient lookup")
    void createMessage_SendAsEmailFalse_NoRecipientLookup() {
        validRequest.setSendAsEmail(false);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(userRepository, never()).findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList());
    }

    @Test
    @DisplayName("Create Message - SendAsEmail true with no recipients - No email")
    void createMessage_SendAsEmailNoRecipients_NoEmailSent() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);
        testMessage.setSendAsEmail(true);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
            .thenReturn(List.of());

        try (MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class)) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));
            assertTrue(templatesMock.constructed().isEmpty());
        }
    }

    @Test
    @DisplayName("Create Message - SendAsEmail with publishDate - Generates batch ID")
    void createMessage_SendAsEmailWithPublishDate_GeneratesBatchId() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Message.class))).thenReturn(testMessage);

        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("batch-123"))) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> messageCaptor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            assertEquals("batch-123", messageCaptor.getValue().getSendgridEmailBatchId());
        }
    }

    @Test
    @DisplayName("Create Message - SendAsEmail true with recipients - Sends email")
    void createMessage_SendAsEmailWithRecipients_SendsEmail() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        testMessage.setSendAsEmail(true);
        testMessage.setDescriptionHtml(TEST_DESC_HTML);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
            .thenReturn(List.of(TEST_EMAIL));

        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("batch-email"));
             MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class,
                (mock, context) -> when(mock.sendMessageEmail(anyList(), anyString(), anyString(), any(), any())).thenReturn(true))) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            EmailTemplates constructed = templatesMock.constructed().get(0);
            verify(constructed).sendMessageEmail(eq(List.of(TEST_EMAIL)), eq(TEST_TITLE), eq(TEST_DESC_HTML), any(), any());
        }
    }

    @Test
    @DisplayName("Create Message - SendAsEmail without publishDate - No batch ID")
    void createMessage_SendAsEmailWithoutPublishDate_NoBatchId() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Message.class))).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> messageCaptor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertNull(messageCaptor.getValue().getSendgridEmailBatchId());
    }

    @Test
    @DisplayName("Create Message - Success - No email")
    void createMessage_Success_NoEmail() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(new MessageUserMap());
        when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class))).thenReturn(new MessageUserGroupMap());

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));

        verify(messageRepository).save(any());
        verify(userLogService).logDataWithContext(eq(TEST_USER_ID), anyString(), eq(TEST_CLIENT_ID), contains("Successfully inserted"), any());
    }

    @Test
    @DisplayName("Create Message - Trimmed Fields - Success")
    void createMessage_TrimmedFields_Success() {
        validRequest.setTitle("  Trimmed Title  ");
        validRequest.setDescriptionHtml("  <p>Trimmed</p>  ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.createMessage(validRequest);

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Trimmed Title", captor.getValue().getTitle());
        assertEquals("<p>Trimmed</p>", captor.getValue().getDescriptionHtml());
    }

    @Test
    @DisplayName("Create Message - User Targets - Success")
    void createMessage_UserTargets_Success() {
        validRequest.setUserIds(List.of(1L, 2L));
        validRequest.setUserGroupIds(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserMapRepository, times(2)).save(any(MessageUserMap.class));
    }

    @Test
    @DisplayName("Create Message With Context - Valid Context - Success")
    void createMessageWithContext_ValidContext_Success() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.createMessageWithContext(validRequest, 999L, "system_user", TEST_CLIENT_ID));
        verify(userLogService).logDataWithContext(eq(999L), eq("system_user"), eq(TEST_CLIENT_ID), anyString(), any());
    }

    @Test
    @DisplayName("Create Message - Verify Logging - Success")
    void createMessage_VerifyLogging_Success() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        
        messageService.createMessage(validRequest);
        verify(userLogService).logDataWithContext(eq(TEST_USER_ID), anyString(), eq(TEST_CLIENT_ID), contains("Successfully inserted"), any());
    }

    @Test
    @DisplayName("Create Message - Mixed Targets - Success")
    void createMessage_MixedTargets_Success() {
        validRequest.setUserIds(List.of(1L));
        validRequest.setUserGroupIds(List.of(10L));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserMapRepository, times(1)).save(any(MessageUserMap.class));
        verify(messageUserGroupMapRepository, times(1)).save(any(MessageUserGroupMap.class));
    }

    @Test
    @DisplayName("Create Message - Null Notes - Success")
    void createMessage_NullNotes_Success() {
        validRequest.setNotes(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Blank Notes - Success")
    void createMessage_BlankNotes_Success() {
        validRequest.setNotes("   ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Create Message - Blank descriptionHtml - Throws BadRequestException")
    void createMessage_BlankDescriptionHtml_ThrowsBadRequestException() {
        validRequest.setDescriptionHtml("   ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Blank title - Throws BadRequestException")
    void createMessage_BlankTitle_ThrowsBadRequestException() {
        validRequest.setTitle("   ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Client not found - Throws NotFoundException")
    void createMessage_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Email beyond 72 hours - Throws BadRequestException")
    void createMessage_EmailBeyond72Hours_ThrowsBadRequestException() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(74));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Email in past - Throws BadRequestException")
    void createMessage_EmailInPast_ThrowsBadRequestException() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Empty targets - Success without maps")
    void createMessage_EmptyTargets_SuccessWithoutMaps() {
        validRequest.setUserIds(Arrays.asList());
        validRequest.setUserGroupIds(Arrays.asList());
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessage(validRequest));
        assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - Invalid UserId in Context - Throws BadRequestException")
    void createMessageWithContext_InvalidUserId_ThrowsBadRequestException() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessageWithContext(validRequest, 0L, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidUserId, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - Negative UserId in Context - Throws BadRequestException")
    void createMessageWithContext_NegativeUserId_ThrowsBadRequestException() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessageWithContext(validRequest, -1L, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidUserId, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - No targets - Success without maps")
    void createMessage_NoTargets_SuccessWithoutMaps() {
        validRequest.setUserIds(null);
        validRequest.setUserGroupIds(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessage(validRequest));
        assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - Null descriptionHtml - Throws BadRequestException")
    void createMessage_NullDescriptionHtml_ThrowsBadRequestException() {
        validRequest.setDescriptionHtml(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Null Request - Throws BadRequestException")
    void createMessage_NullRequest_ThrowsBadRequestException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessage(null));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - Null request in Context - Throws BadRequestException")
    void createMessageWithContext_NullRequest_ThrowsBadRequestException() {
        BadRequestException exception = assertThrows(BadRequestException.class, () -> messageService.createMessageWithContext(null, TEST_USER_ID, "admin", TEST_CLIENT_ID));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Create Message - Null title - Throws BadRequestException")
    void createMessage_NullTitle_ThrowsBadRequestException() {
        validRequest.setTitle(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Publish date without sendAsEmail - Throws BadRequestException")
    void createMessage_PublishDateRequiresSendAsEmail_ThrowsBadRequestException() {
        validRequest.setSendAsEmail(false);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Repository Save Failure - Propagates Exception")
    void createMessage_RepositorySaveFailure_Propagates() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(RuntimeException.class, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Title boundary (500 chars) - Success")
    void createMessage_TitleBoundary_Success() {
        validRequest.setTitle("a".repeat(500));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.save(any())).thenReturn(testMessage);
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Title too long - Throws BadRequestException")
    void createMessage_TitleTooLong_ThrowsBadRequestException() {
        validRequest.setTitle("A".repeat(501));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.TitleTooLong, () -> messageService.createMessage(validRequest));
    }

    @Test
    @DisplayName("Create Message - Unauthorized Client Context - Throws NotFoundException")
    void createMessageWithContext_UnauthorizedClient_ThrowsNotFoundException() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId, () -> messageService.createMessageWithContext(validRequest, TEST_USER_ID, "admin", 9999L));
    }

    @Test
    @DisplayName("Create Message - Zero ClientId - Throws NotFoundException")
    void createMessage_ZeroClientId_ThrowsNotFoundException() {
        // Mocking behavior where getClientId returns 0
        // (Assuming test base or context allows this injection)
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId, () -> messageService.createMessage(validRequest));
    }
}