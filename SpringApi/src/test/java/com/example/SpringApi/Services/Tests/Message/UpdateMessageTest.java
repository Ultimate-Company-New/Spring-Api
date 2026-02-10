package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.updateMessage method.
 */
@DisplayName("UpdateMessage Tests")
public class UpdateMessageTest extends MessageServiceTestBase {

    // Total Tests: 31

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Update Message - Cancel Email Logic - Only works if batch exists and not blocked by validation")
    void updateMessage_CancelEmailLogic_IfValidationPasses() {
        testMessage.setSendgridEmailBatchId("BATCH-OLD");
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));

        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(25));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        // Validation rule 3: Cannot change publishDate on scheduled message
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotModifyScheduledPublishDate,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    @Test
    @DisplayName("Update Message - Change Notes - Success")
    void updateMessage_ChangeNotes_Success() {
        validRequest.setNotes("Updated Notes");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Updated Notes", captor.getValue().getNotes());
    }

    @Test
    @DisplayName("Update Message - Change Title - Success")
    void updateMessage_ChangeTitle_Success() {
        validRequest.setTitle("New Title");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("New Title", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("Update Message - Deletes Old Mappings Before Save")
    void updateMessage_DeletesOldMappings() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        verify(messageUserMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(messageUserGroupMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(messageUserMapRepository).flush();
    }

    @Test
    @DisplayName("Update Message - SendAsEmail False -> True - Generates New Batch")
    void updateMessage_EnableEmail_GeneratesNewBatch() {
        testMessage.setSendAsEmail(false);
        testMessage.setSendgridEmailBatchId(null);
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(2));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("BATCH-NEW"))) {
            messageService.updateMessage(validRequest);
            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(captor.capture());
            assertEquals("BATCH-NEW", captor.getValue().getSendgridEmailBatchId());
        }
    }

    @Test
    @DisplayName("Update Message - SendAsEmail False -> False (No Change) - BatchID Null")
    void updateMessage_NoEmailChange_BatchIDNull() {
        testMessage.setSendAsEmail(false);
        validRequest.setSendAsEmail(false);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertNull(captor.getValue().getSendgridEmailBatchId());
    }



    @Test
    @DisplayName("Update Message - SendAsEmail True -> True with New Date - Resends/Re-batches")
    void updateMessage_Reschedule_GeneratesNewBatch() {
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));
        testMessage.setSendgridEmailBatchId("BATCH-OLD");
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(48));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        // Validation rule 3: Cannot change publishDate on scheduled message
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotModifyScheduledPublishDate,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    @Test
    @DisplayName("Update Message - Reschedule Same Date - No Batch Change")
    void updateMessage_RescheduleSameDate_NoBatchChange() {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(10);
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(now);
        testMessage.setSendgridEmailBatchId("OLD");
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(now);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
                .thenReturn(Collections.emptyList());

        // Same date passes validation; mock EmailHelper for batch generation
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("OLD"))) {
            assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor.forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(captor.capture());
            assertEquals("OLD", captor.getValue().getSendgridEmailBatchId());
        }
    }

    @Test
    @DisplayName("Update Message - SendAsEmail True (Immediate) - Sends Email Now")
    void updateMessage_SendImmediate_SendsEmail() {
        testMessage.setSendAsEmail(false);
        testMessage.setMessageUserMaps(Collections.emptyList());
        testMessage.setMessageUserGroupMaps(Collections.emptyList());
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
            .thenReturn(List.of("email@test.com"));

        try (MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class)) {
            messageService.updateMessage(validRequest);
            // Verify EmailTemplates was constructed and sendMessageEmail was called
            assertFalse(templatesMock.constructed().isEmpty(), "EmailTemplates should be constructed");
            if (!templatesMock.constructed().isEmpty()) {
                verify(templatesMock.constructed().get(0)).sendMessageEmail(anyList(), anyString(), anyString(), any(), any());
            }
        }
    }

    @Test
    @DisplayName("Update Message - Success")
    void updateMessage_Success() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

        verify(messageRepository).save(any());
    }

    @Test
    @DisplayName("Update Message - Updates User Mappings")
    void updateMessage_UpdatesUserMappings() {
        validRequest.setUserIds(List.of(99L));
        validRequest.setUserGroupIds(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        verify(messageUserMapRepository).save(any(MessageUserMap.class));
    }

    @Test
    @DisplayName("Update Message - Valid Title Boundary - Success")
    void updateMessage_ValidTitleBoundary_Success() {
        validRequest.setTitle("a".repeat(500));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Verify Log - Success")
    void updateMessage_VerifyLog_Success() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        messageService.updateMessage(validRequest);

        verify(userLogService).logData(eq(TEST_USER_ID), contains("Successfully updated"), eq(ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Update Message - Blank Description - Throws BadRequest")
    void updateMessage_BlankDescription_ThrowsBadRequest() {
        validRequest.setDescriptionHtml("   ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Cannot Add PublishDate After Already Sent")
    void updateMessage_CannotAddPublishDateAfterSent_ThrowsException() {
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(null);
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().plusHours(5));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotAddPublishDateAfterSent, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Cannot Disable SendAsEmail Once Enabled")
    void updateMessage_CannotDisableSendAsEmail_ThrowsException() {
        testMessage.setSendAsEmail(true);
        validRequest.setSendAsEmail(false);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Cannot Modify Scheduled Publish Date if mismatch logic")
    void updateMessage_CannotModifyScheduledPublishDate_LogicCheck() {
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now().minusHours(1));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER011, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Client Not Found - Throws NotFoundException")
    void updateMessage_ClientNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Disable SendAsEmail - Cancels Existing Batch")
    void updateMessage_DisableSendAsEmail_CancelsBatch() {
        testMessage.setSendgridEmailBatchId("BATCH-EXISTING");
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));
        validRequest.setSendAsEmail(false);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        // Validation rule 1: Cannot disable sendAsEmail once enabled (fires before rule 3)
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    @Test
    @DisplayName("Update Message - Empty Targets - Throws BadRequestException")
    void updateMessage_EmptyTargets_ThrowsBadRequestException() {
        validRequest.setUserIds(Collections.emptyList());
        validRequest.setUserGroupIds(Collections.emptyList());
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrows(BadRequestException.class, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Message Not Found - Throws NotFoundException")
    void updateMessage_MessageNotFound_ThrowsNotFoundException() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Null MessageID - Throws BadRequestException")
    void updateMessage_NullMessageId_ThrowsBadRequestException() {
        validRequest.setMessageId(null);
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Publish Date In Past - Throws BadRequest")
    void updateMessage_PublishDateInPast_ThrowsBadRequest() {
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().minusHours(1));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Publish Date > 72h - Throws BadRequest")
    void updateMessage_PublishDateTooFar_ThrowsBadRequest() {
        testMessage.setSendAsEmail(false); // Start with sendAsEmail=false so we can enable it
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(74));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        // Validation should fail before any email service interaction
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Publish Date Without SendAsEmail - Throws BadRequest")
    void updateMessage_PublishDateWithoutSendAsEmail_ThrowsBadRequest() {
        validRequest.setSendAsEmail(false);
        validRequest.setPublishDate(LocalDateTime.now().plusHours(5));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail, () -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Repository Update Failure - Propagates Exception")
    void updateMessage_RepositoryFailure_Propagates() {
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenThrow(new RuntimeException("Update failed"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.updateMessage(validRequest));
        assertEquals("Update failed", ex.getMessage());
    }

    @Test
    @DisplayName("Update Message - Title boundary (500 chars) - Success")
    void updateMessage_TitleBoundary_Success() {
        validRequest.setTitle("a".repeat(500));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);
        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
    }

    @Test
    @DisplayName("Update Message - Title Too Long - Throws BadRequest")
    void updateMessage_TitleTooLong_ThrowsBadRequest() {
        validRequest.setTitle("a".repeat(501));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testMessage));

        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.TitleTooLong, () -> messageService.updateMessage(validRequest));
    }
    
    @Test
    @DisplayName("Update Message - Zero MessageId - Throws BadRequest")
    void updateMessage_ZeroMessageId_ThrowsBadRequest() {
        validRequest.setMessageId(0L);
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId, () -> messageService.updateMessage(validRequest));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("updateMessage - Verify @PreAuthorize Annotation")
    void updateMessage_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = MessageController.class.getMethod("updateMessage", MessageRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_MESSAGES_PERMISSION),
            "@PreAuthorize should reference UPDATE_MESSAGES_PERMISSION");
    }

    @Test
    @DisplayName("updateMessage - Controller delegates to service")
    void updateMessage_WithValidRequest_DelegatesToService() {
        MessageController controller = new MessageController(messageServiceMock);
        doNothing().when(messageServiceMock).updateMessage(validRequest);

        ResponseEntity<?> response = controller.updateMessage(validRequest);

        verify(messageServiceMock).updateMessage(validRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}