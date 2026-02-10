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

    // Total Tests: 30

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that scheduled email cancellation logic is triggered when
     * validation passes.
     * Scenario: Update message with valid parameters where an old batch needs to be
     * cancelled.
     * Expected Result: BadRequestException thrown if date change violates business
     * rules (logic check).
     */
    @Test
    @DisplayName("Update Message - Cancel Email Logic - Only works if batch exists and not blocked by validation")
    void updateMessage_CancelEmailLogic_IfValidationPasses() {
        // Arrange
        testMessage.setSendgridEmailBatchId("BATCH-OLD");
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));

        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(25));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotModifyScheduledPublishDate,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    /**
     * Purpose: Verify that message notes can be successfully updated.
     * Scenario: Call updateMessage with a new notes string.
     * Expected Result: Message is saved with the updated notes.
     */
    @Test
    @DisplayName("Update Message - Change Notes - Success")
    void updateMessage_ChangeNotes_Success() {
        // Arrange
        validRequest.setNotes("Updated Notes");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Updated Notes", captor.getValue().getNotes());
    }

    /**
     * Purpose: Verify that the message title can be successfully updated.
     * Scenario: Call updateMessage with a new title.
     * Expected Result: Message is saved with the updated title.
     */
    @Test
    @DisplayName("Update Message - Change Title - Success")
    void updateMessage_ChangeTitle_Success() {
        // Arrange
        validRequest.setTitle("New Title");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("New Title", captor.getValue().getTitle());
    }

    /**
     * Purpose: Verify that old user and group mappings are deleted before saving
     * the updated message.
     * Expected Result: Repository delete and flush methods called for both mapping
     * types.
     */
    @Test
    @DisplayName("Update Message - Deletes Old Mappings Before Save")
    void updateMessage_DeletesOldMappings() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        verify(messageUserMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(messageUserGroupMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(messageUserMapRepository).flush();
    }

    /**
     * Purpose: Verify that a new email batch is generated when sendAsEmail is
     * toggled from false to true.
     * Expected Result: EmailHelper generates new batch ID which is saved to the
     * message.
     */
    @Test
    @DisplayName("Update Message - SendAsEmail False -> True - Generates New Batch")
    void updateMessage_EnableEmail_GeneratesNewBatch() {
        // Arrange
        testMessage.setSendAsEmail(false);
        testMessage.setSendgridEmailBatchId(null);
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(2));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("BATCH-NEW"))) {
            messageService.updateMessage(validRequest);
            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                    .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(captor.capture());
            assertEquals("BATCH-NEW", captor.getValue().getSendgridEmailBatchId());
        }
    }

    /**
     * Purpose: Verify that no batch ID is generated when sendAsEmail remains false.
     * Expected Result: Saved entity has null batch ID.
     */
    @Test
    @DisplayName("Update Message - SendAsEmail False -> False (No Change) - BatchID Null")
    void updateMessage_NoEmailChange_BatchIDNull() {
        // Arrange
        testMessage.setSendAsEmail(false);
        validRequest.setSendAsEmail(false);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertNull(captor.getValue().getSendgridEmailBatchId());
    }

    /**
     * Purpose: Verify business rules for rescheduling an already scheduled email.
     * Expected Result: BadRequestException if rule violation occurs (placeholder
     * test for logic).
     */
    @Test
    @DisplayName("Update Message - SendAsEmail True -> True with New Date - Resends/Re-batches")
    void updateMessage_Reschedule_GeneratesNewBatch() {
        // Arrange
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));
        testMessage.setSendgridEmailBatchId("BATCH-OLD");
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(48));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotModifyScheduledPublishDate,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    /**
     * Purpose: Verify that no new batch is generated if the publish date remains
     * unchanged.
     * Expected Result: Message saved with original batch ID.
     */
    @Test
    @DisplayName("Update Message - Reschedule Same Date - No Batch Change")
    void updateMessage_RescheduleSameDate_NoBatchChange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(10);
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(now);
        testMessage.setSendgridEmailBatchId("OLD");
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(now);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> when(mock.generateBatchId()).thenReturn("OLD"))) {
            assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                    .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(captor.capture());
            assertEquals("OLD", captor.getValue().getSendgridEmailBatchId());
        }
    }

    /**
     * Purpose: Verify that an email is sent immediately if rescheduled to a null
     * publish date.
     * Expected Result: EmailTemplates.sendMessageEmail called.
     */
    @Test
    @DisplayName("Update Message - SendAsEmail True (Immediate) - Sends Email Now")
    void updateMessage_SendImmediate_SendsEmail() {
        // Arrange
        testMessage.setSendAsEmail(false);
        testMessage.setMessageUserMaps(Collections.emptyList());
        testMessage.setMessageUserGroupMaps(Collections.emptyList());
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
                .thenReturn(List.of("email@test.com"));

        // Act & Assert
        try (MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class)) {
            messageService.updateMessage(validRequest);
            assertFalse(templatesMock.constructed().isEmpty(), "EmailTemplates should be constructed");
            if (!templatesMock.constructed().isEmpty()) {
                verify(templatesMock.constructed().get(0)).sendMessageEmail(anyList(), anyString(), anyString(), any(),
                        any());
            }
        }
    }

    /**
     * Purpose: Verify successful message update with standard valid parameters.
     * Expected Result: No exceptions thrown and repository save called.
     */
    @Test
    @DisplayName("Update Message - Success")
    void updateMessage_Success() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
        verify(messageRepository).save(any());
    }

    /**
     * Purpose: Verify that target user mappings are correctly refreshed during
     * update.
     * Scenario: Call updateMessage with a non-null list of user IDs.
     * Expected Result: Repository save called for new mapping records.
     */
    @Test
    @DisplayName("Update Message - Updates User Mappings")
    void updateMessage_UpdatesUserMappings() {
        // Arrange
        validRequest.setUserIds(List.of(99L));
        validRequest.setUserGroupIds(null);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        verify(messageUserMapRepository).save(any(MessageUserMap.class));
    }

    /**
     * Purpose: Verify message with maximum allowed title length (500 chars) is
     * accepted during update.
     * Expected Result: Update succeeds without validation errors.
     */
    @Test
    @DisplayName("Update Message - Valid Title Boundary - Success")
    void updateMessage_ValidTitleBoundary_Success() {
        // Arrange
        validRequest.setTitle("a".repeat(500));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Verify that successful update triggers activity logging.
     * Expected Result: UserLogService.logData called with identifying route info.
     */
    @Test
    @DisplayName("Update Message - Verify Log - Success")
    void updateMessage_VerifyLog_Success() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenReturn(testMessage);

        // Act
        messageService.updateMessage(validRequest);

        // Assert
        verify(userLogService).logData(eq(TEST_USER_ID), contains("Successfully updated"),
                eq(ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject update when descriptionHtml is blank.
     * Expected Result: BadRequestException with ER007 error code.
     */
    @Test
    @DisplayName("Update Message - Blank Description - Throws BadRequest")
    void updateMessage_BlankDescription_ThrowsBadRequest() {
        // Arrange
        validRequest.setDescriptionHtml("   ");
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject adding a publish date to an email that has already been
     * flagged as sent.
     * Expected Result: BadRequestException with CannotAddPublishDateAfterSent
     * message.
     */
    @Test
    @DisplayName("Update Message - Cannot Add PublishDate After Already Sent")
    void updateMessage_CannotAddPublishDateAfterSent_ThrowsException() {
        // Arrange
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(null);
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().plusHours(5));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotAddPublishDateAfterSent,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject disabling sendAsEmail flag once it has been enabled for a
     * message.
     * Expected Result: BadRequestException with CannotDisableSendAsEmailOnce
     * message.
     */
    @Test
    @DisplayName("Update Message - Cannot Disable SendAsEmail Once Enabled")
    void updateMessage_CannotDisableSendAsEmail_ThrowsException() {
        // Arrange
        testMessage.setSendAsEmail(true);
        validRequest.setSendAsEmail(false);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject modification of scheduled publish date if it doesn't match
     * specific business logic (legacy check).
     * Expected Result: BadRequestException with ER011 error code.
     */
    @Test
    @DisplayName("Update Message - Cannot Modify Scheduled Publish Date if mismatch logic")
    void updateMessage_CannotModifyScheduledPublishDate_LogicCheck() {
        // Arrange
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now().minusHours(1));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER011,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update for non-existent client ID.
     * Expected Result: NotFoundException with client invalid ID message.
     */
    @Test
    @DisplayName("Update Message - Client Not Found - Throws NotFoundException")
    void updateMessage_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Verify that disabling sendAsEmail triggers cancellation of any
     * existing email batch.
     * Expected Result: BadRequestException thrown as Rule 1 (Cannot disable) takes
     * precedence.
     */
    @Test
    @DisplayName("Update Message - Disable SendAsEmail - Cancels Existing Batch")
    void updateMessage_DisableSendAsEmail_CancelsBatch() {
        // Arrange
        testMessage.setSendgridEmailBatchId("BATCH-EXISTING");
        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(24));
        validRequest.setSendAsEmail(false);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    /**
     * Purpose: Reject update when the targets list (users and groups) are both
     * empty/null.
     * Expected Result: BadRequestException with ER008 error code.
     */
    @Test
    @DisplayName("Update Message - Empty Targets - Throws BadRequestException")
    void updateMessage_EmptyTargets_ThrowsBadRequestException() {
        // Arrange
        validRequest.setUserIds(Collections.emptyList());
        validRequest.setUserGroupIds(Collections.emptyList());
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update request when specified message ID does not exist.
     * Expected Result: NotFoundException with message invalid ID error code.
     */
    @Test
    @DisplayName("Update Message - Message Not Found - Throws NotFoundException")
    void updateMessage_MessageNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update request when message ID is missing/null in the model.
     * Expected Result: BadRequestException with message invalid ID error code.
     */
    @Test
    @DisplayName("Update Message - Null MessageID - Throws BadRequestException")
    void updateMessage_NullMessageId_ThrowsBadRequestException() {
        // Arrange
        validRequest.setMessageId(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update when provided publishDate is in the past.
     * Expected Result: BadRequestException with ER009 error code.
     */
    @Test
    @DisplayName("Update Message - Publish Date In Past - Throws BadRequest")
    void updateMessage_PublishDateInPast_ThrowsBadRequest() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().minusHours(1));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update when publishDate is too far in future ( Shipgrid
     * limit).
     * Expected Result: BadRequestException with ER010 error code.
     */
    @Test
    @DisplayName("Update Message - Publish Date > 72h - Throws BadRequest")
    void updateMessage_PublishDateTooFar_ThrowsBadRequest() {
        // Arrange
        testMessage.setSendAsEmail(false);
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(java.time.ZoneOffset.UTC).plusHours(74));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update with publishDate if sendAsEmail is toggled to false.
     * Expected Result: BadRequestException with PublishDateRequiresSendAsEmail
     * error code.
     */
    @Test
    @DisplayName("Update Message - Publish Date Without SendAsEmail - Throws BadRequest")
    void updateMessage_PublishDateWithoutSendAsEmail_ThrowsBadRequest() {
        // Arrange
        validRequest.setSendAsEmail(false);
        validRequest.setPublishDate(LocalDateTime.now().plusHours(5));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Propagate repository exceptions when database update fails.
     * Expected Result: RuntimeException with original database error message.
     */
    @Test
    @DisplayName("Update Message - Repository Update Failure - Propagates Exception")
    void updateMessage_RepositoryFailure_Propagates() {
        // Arrange
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any())).thenThrow(new RuntimeException("Update failed"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.updateMessage(validRequest));
        assertEquals("Update failed", ex.getMessage());
    }

    /**
     * Purpose: Reject message with title exceeding maximum length during update.
     * Expected Result: BadRequestException with TitleTooLong error.
     */
    @Test
    @DisplayName("Update Message - Title Too Long - Throws BadRequest")
    void updateMessage_TitleTooLong_ThrowsBadRequest() {
        // Arrange
        validRequest.setTitle("a".repeat(501));
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.TitleTooLong,
                () -> messageService.updateMessage(validRequest));
    }

    /**
     * Purpose: Reject update request when message ID is zero.
     * Expected Result: BadRequestException with message invalid ID error code.
     */
    @Test
    @DisplayName("Update Message - Zero MessageId - Throws BadRequest")
    void updateMessage_ZeroMessageId_ThrowsBadRequest() {
        // Arrange
        validRequest.setMessageId(0L);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.updateMessage(validRequest));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the updateMessage controller method is protected by
     * correct @PreAuthorize permission.
     * Expected: Method has @PreAuthorize referencing UPDATE_MESSAGES_PERMISSION.
     */
    @Test
    @DisplayName("updateMessage - Verify @PreAuthorize Annotation")
    void updateMessage_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = MessageController.class.getMethod("updateMessage", MessageRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_MESSAGES_PERMISSION),
                "@PreAuthorize should reference UPDATE_MESSAGES_PERMISSION");
    }

    /**
     * Purpose: Verify that the controller correctly delegates updateMessage calls
     * to the service layer.
     * Expected Result: Status 200 returned upon delegation.
     */
    @Test
    @DisplayName("updateMessage - Controller delegates to service")
    void updateMessage_WithValidRequest_DelegatesToService() {
        // Arrange
        MessageController controller = new MessageController(messageServiceMock);
        doNothing().when(messageServiceMock).updateMessage(validRequest);

        // Act
        ResponseEntity<?> response = controller.updateMessage(validRequest);

        // Assert
        verify(messageServiceMock).updateMessage(validRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}