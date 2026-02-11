package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 */
@DisplayName("CreateMessage Tests")
class CreateMessageTest extends MessageServiceTestBase {

    // Total Tests: 30

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful message creation when notes are blank.
     * Scenario: CreateMessage with a title, description, targets and blank notes.
     * Expected: Message created successfully.
     */
    @Test
    @DisplayName("Create Message - Blank Notes - Success")
    void createMessage_BlankNotes_Success() {
        // Arrange
        validRequest.setNotes("   ");
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Verify successful message creation with group targets.
     * Scenario: CreateMessage with group target IDs provided.
     * Expected: Message saved and group mappings created.
     */
    @Test
    @DisplayName("Create Message - Group Targets - Success")
    void createMessage_GroupTargets_Success() {
        // Arrange
        validRequest.setUserIds(null);
        validRequest.setUserGroupIds(List.of(10L, 20L));
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserGroupMapRepository, times(2)).save(any(MessageUserGroupMap.class));
    }

    /**
     * Purpose: Verify successful message creation with mixed (user and group)
     * targets.
     * Scenario: CreateMessage with both user and group target IDs.
     * Expected: Message saved and both types of mappings created.
     */
    @Test
    @DisplayName("Create Message - Mixed Targets - Success")
    void createMessage_MixedTargets_Success() {
        // Arrange
        validRequest.setUserIds(List.of(1L));
        validRequest.setUserGroupIds(List.of(10L));
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserMapRepository, times(1)).save(any(MessageUserMap.class));
        verify(messageUserGroupMapRepository, times(1)).save(any(MessageUserGroupMap.class));
    }

    /**
     * Purpose: Verify successful message creation when notes are null.
     * Scenario: CreateMessage with a title, description, targets and null notes.
     * Expected: Message created successfully.
     */
    @Test
    @DisplayName("Create Message - Null Notes - Success")
    void createMessage_NullNotes_Success() {
        // Arrange
        validRequest.setNotes(null);
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Verify that no recipient lookup occurs when sendAsEmail is false.
     * Scenario: CreateMessage with sendAsEmail set to false.
     * Expected: Recipient lookup repository method never called.
     */
    @Test
    @DisplayName("Create Message - SendAsEmail false - No recipient lookup")
    void createMessage_SendAsEmailFalse_NoRecipientLookup() {
        // Arrange
        validRequest.setSendAsEmail(false);
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(userRepository, never()).findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList());
    }

    /**
     * Purpose: Verify that no email is sent when sendAsEmail is true but no
     * recipient emails are found.
     * Scenario: CreateMessage with sendAsEmail=true but repository returns empty
     * email list.
     * Expected: EmailTemplates classes not instantiated/called.
     */
    @Test
    @DisplayName("Create Message - SendAsEmail true with no recipients - No email")
    void createMessage_SendAsEmailNoRecipients_NoEmailSent() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);
        testMessage.setSendAsEmail(true);

        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);
        stubUserRepositoryFindAllUserEmails(List.of());

        // Act & Assert
        try (MockedConstruction<EmailTemplates> templatesMock = stubEmailTemplatesSendEmail(true)) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));
            assertTrue(templatesMock.constructed().isEmpty());
        }
    }

    /**
     * Purpose: Verify that a batch ID is generated when a scheduled email is
     * created.
     * Scenario: CreateMessage with sendAsEmail=true and a future publishDate.
     * Expected: EmailHelper used to generate batch ID which is saved to the
     * message.
     */
    @Test
    @DisplayName("Create Message - SendAsEmail with publishDate - Generates batch ID")
    void createMessage_SendAsEmailWithPublishDate_GeneratesBatchId() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = stubEmailHelperGenerateBatchId("batch-123")) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> messageCaptor = ArgumentCaptor
                    .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            assertEquals("batch-123", messageCaptor.getValue().getSendgridEmailBatchId());
        }
    }

    /**
     * Purpose: Verify that an email is sent to recipients when sendAsEmail is true
     * and recipients are found.
     * Scenario: CreateMessage with sendAsEmail=true and repository returns a list
     * of recipient emails.
     * Expected: EmailTemplates.sendMessageEmail is called with correct parameters.
     */
    @Test
    @DisplayName("Create Message - SendAsEmail true with recipients - Sends email")
    void createMessage_SendAsEmailWithRecipients_SendsEmail() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        testMessage.setSendAsEmail(true);
        testMessage.setDescriptionHtml(TEST_DESC_HTML);

        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);
        stubUserRepositoryFindAllUserEmails(List.of(TEST_EMAIL));

        // Act & Assert
        try (MockedConstruction<EmailHelper> emailHelperMock = stubEmailHelperGenerateBatchId("batch-email");
            MockedConstruction<EmailTemplates> templatesMock = stubEmailTemplatesSendEmail(true)) {
            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            EmailTemplates constructed = templatesMock.constructed().get(0);
            verify(constructed).sendMessageEmail(eq(List.of(TEST_EMAIL)), eq(TEST_TITLE), eq(TEST_DESC_HTML), any(),
                    any());
        }
    }

    /**
     * Purpose: Verify that no batch ID is generated when an immediate email (no
     * publishDate) is created.
     * Scenario: CreateMessage with sendAsEmail=true but null publishDate.
     * Expected: Message saved without generating a batch ID.
     */
    @Test
    @DisplayName("Create Message - SendAsEmail without publishDate - No batch ID")
    void createMessage_SendAsEmailWithoutPublishDate_NoBatchId() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(null);

        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));

        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> messageCaptor = ArgumentCaptor
                .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertNull(messageCaptor.getValue().getSendgridEmailBatchId());
    }

    /**
     * Purpose: Verify successful message creation when email sending is not
     * requested.
     * Scenario: CreateMessage with sendAsEmail=false.
     * Expected: Message saved and user activity logged, but no email operations
     * performed.
     */
    @Test
    @DisplayName("Create Message - Success - No email")
    void createMessage_Success_NoEmail() {
        // Arrange
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);
        stubMessageUserMapRepositorySave(new MessageUserMap());
        stubMessageUserGroupMapRepositorySave(new MessageUserGroupMap());

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));

        verify(messageRepository).save(any());
        verify(userLogService).logDataWithContext(eq(TEST_USER_ID), anyString(), eq(TEST_CLIENT_ID),
                contains("Successfully inserted"), any());
    }

    /**
     * Purpose: Verify message with maximum allowed title length (500 chars) is
     * accepted.
     * Scenario: CreateMessage with 500-character title.
     * Expected: Message created successfully without validation errors.
     */
    @Test
    @DisplayName("Create Message - TitleBoundary (500 chars) - Success")
    void createMessage_TitleBoundary_Success() {
        // Arrange
        validRequest.setTitle("a".repeat(500));
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Verify that title and description fields are trimmed during message
     * creation.
     * Scenario: CreateMessage with leading and trailing whitespace in fields.
     * Expected: Saved entity contains trimmed strings.
     */
    @Test
    @DisplayName("Create Message - Trimmed Fields - Success")
    void createMessage_TrimmedFields_Success() {
        // Arrange
        validRequest.setTitle("  Trimmed Title  ");
        validRequest.setDescriptionHtml("  <p>Trimmed</p>  ");
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act
        messageService.createMessage(validRequest);

        // Assert
        ArgumentCaptor<com.example.SpringApi.Models.DatabaseModels.Message> captor = ArgumentCaptor
                .forClass(com.example.SpringApi.Models.DatabaseModels.Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Trimmed Title", captor.getValue().getTitle());
        assertEquals("<p>Trimmed</p>", captor.getValue().getDescriptionHtml());
    }

    /**
     * Purpose: Verify successful message creation with individual user targets.
     * Scenario: CreateMessage with a list of user IDs.
     * Expected: Message saved and user mappings created for each user ID.
     */
    @Test
    @DisplayName("Create Message - User Targets - Success")
    void createMessage_UserTargets_Success() {
        // Arrange
        validRequest.setUserIds(List.of(1L, 2L));
        validRequest.setUserGroupIds(null);
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act & Assert
        assertDoesNotThrow(() -> messageService.createMessage(validRequest));
        verify(messageUserMapRepository, times(2)).save(any(MessageUserMap.class));
    }

    /**
     * Purpose: Verify that successful message creation triggers activity logging.
     * Scenario: CreateMessage with valid request.
     * Expected: UserLogService.logDataWithContext called with correct details.
     */
    @Test
    @DisplayName("Create Message - Verify Logging - Success")
    void createMessage_VerifyLogging_Success() {
        // Arrange
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySave(testMessage);

        // Act
        messageService.createMessage(validRequest);

        // Assert
        verify(userLogService).logDataWithContext(eq(TEST_USER_ID), anyString(), eq(TEST_CLIENT_ID),
                contains("Successfully inserted"), any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject message creation when descriptionHtml is blank.
     * Expected Result: BadRequestException with ER007 error code.
     */
    @Test
    @DisplayName("Create Message - Blank descriptionHtml - Throws BadRequestException")
    void createMessage_BlankDescriptionHtml_ThrowsBadRequestException() {
        // Arrange
        validRequest.setDescriptionHtml("   ");
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation when title is blank.
     * Expected Result: BadRequestException with ER003 error code.
     */
    @Test
    @DisplayName("Create Message - Blank title - Throws BadRequestException")
    void createMessage_BlankTitle_ThrowsBadRequestException() {
        // Arrange
        validRequest.setTitle("   ");
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation for non-existent client ID.
     * Expected Result: NotFoundException with client invalid ID error message.
     */
    @Test
    @DisplayName("Create Message - Client not found - Throws NotFoundException")
    void createMessage_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        stubClientRepositoryFindById(Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject scheduled email when publishDate is too far in future (
     * Shipgrid limit).
     * Scenario: CreateMessage with publishDate 74 hours away.
     * Expected Result: BadRequestException with ER010 error code.
     */
    @Test
    @DisplayName("Create Message - Email beyond 72 hours - Throws BadRequestException")
    void createMessage_EmailBeyond72Hours_ThrowsBadRequestException() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(74));
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject scheduled email when publishDate is in the past.
     * Scenario: CreateMessage with publishDate 1 hour past.
     * Expected Result: BadRequestException with ER009 error code.
     */
    @Test
    @DisplayName("Create Message - Email in past - Throws BadRequestException")
    void createMessage_EmailInPast_ThrowsBadRequestException() {
        // Arrange
        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation when targets list is empty.
     * Expected Result: BadRequestException with ER008 error code.
     */
    @Test
    @DisplayName("Create Message - Empty targets - Throws BadRequestException")
    void createMessage_EmptyTargets_ThrowsBadRequestException() {
        // Arrange
        validRequest.setUserIds(Arrays.asList());
        validRequest.setUserGroupIds(Arrays.asList());
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(validRequest));
        assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
    }

    /**
     * Purpose: Reject message creation when targets list is null.
     * Expected Result: BadRequestException with ER008 error code.
     */
    @Test
    @DisplayName("Create Message - No targets - Throws BadRequestException")
    void createMessage_NoTargets_ThrowsBadRequestException() {
        // Arrange
        validRequest.setUserIds(null);
        validRequest.setUserGroupIds(null);
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(validRequest));
        assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
    }

    /**
     * Purpose: Reject message creation when descriptionHtml is null.
     * Expected Result: BadRequestException with ER007 error code.
     */
    @Test
    @DisplayName("Create Message - Null descriptionHtml - Throws BadRequestException")
    void createMessage_NullDescriptionHtml_ThrowsBadRequestException() {
        // Arrange
        validRequest.setDescriptionHtml(null);
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation when request model is null.
     * Expected Result: BadRequestException with InvalidId error code (legacy
     * exception code).
     */
    @Test
    @DisplayName("Create Message - Null Request - Throws BadRequestException")
    void createMessage_NullRequest_ThrowsBadRequestException() {
        // Arrange
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(null));
        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject message creation when title is null.
     * Expected Result: BadRequestException with ER003 error code.
     */
    @Test
    @DisplayName("Create Message - Null title - Throws BadRequestException")
    void createMessage_NullTitle_ThrowsBadRequestException() {
        // Arrange
        validRequest.setTitle(null);
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation with publishDate if sendAsEmail is false.
     * Expected Result: BadRequestException with PublishDateRequiresSendAsEmail
     * error code.
     */
    @Test
    @DisplayName("Create Message - Publish date without sendAsEmail - Throws BadRequestException")
    void createMessage_PublishDateRequiresSendAsEmail_ThrowsBadRequestException() {
        // Arrange
        validRequest.setSendAsEmail(false);
        validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Propagate repository exceptions when database save fails.
     * Expected Result: RuntimeException with original database error message.
     */
    @Test
    @DisplayName("Create Message - Repository Save Failure - Propagates Exception")
    void createMessage_RepositorySaveFailure_Propagates() {
        // Arrange
        stubClientRepositoryFindById(Optional.of(testClient));
        stubMessageRepositorySaveThrowsRuntimeException("DB Error");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.createMessage(validRequest));
        assertEquals("DB Error", ex.getMessage());
    }

    /**
     * Purpose: Reject message with title exceeding maximum length is rejected.
     * Scenario: CreateMessage with 501-character title.
     * Expected Result: BadRequestException with TitleTooLong error.
     */
    @Test
    @DisplayName("Create Message - Title too long - Throws BadRequestException")
    void createMessage_TitleTooLong_ThrowsBadRequestException() {
        // Arrange
        validRequest.setTitle("A".repeat(501));
        stubClientRepositoryFindById(Optional.of(testClient));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.TitleTooLong,
                () -> messageService.createMessage(validRequest));
    }

    /**
     * Purpose: Reject message creation when client ID is missing/zero.
     * Expected Result: NotFoundException with client invalid ID message.
     */
    @Test
    @DisplayName("Create Message - Zero ClientId - Throws NotFoundException")
    void createMessage_ZeroClientId_ThrowsNotFoundException() {
        // Arrange & Act & Assert
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> messageService.createMessage(validRequest));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */

    /**
     * Purpose: Verify that the createMessage controller method is protected by
     * correct @PreAuthorize permission.
     * Expected: Method has @PreAuthorize referencing INSERT_MESSAGES_PERMISSION.
     */
    @Test
    @DisplayName("Create Message - Verify @PreAuthorize annotation is configured correctly")
    void createMessage_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        var method = MessageController.class.getMethod("createMessage",
                com.example.SpringApi.Models.RequestModels.MessageRequestModel.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "createMessage method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.INSERT_MESSAGES_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference INSERT_MESSAGES_PERMISSION");
    }

    /**
     * Purpose: Verify that the controller correctly delegates createMessage calls
     * to the service layer.
     * Expected: Service method called with correct request and HTTP 200 returned.
     */
    @Test
    @DisplayName("Create Message - Controller delegates to service correctly")
    void createMessage_WithValidRequest_DelegatesToService() {
        // Arrange
        MessageController controller = new MessageController(messageServiceMock);
        stubMessageServiceCreateMessageDoNothing();

        // Act
        ResponseEntity<?> response = controller.createMessage(validRequest);

        // Assert
        verify(messageServiceMock, times(1)).createMessage(validRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
    }
}