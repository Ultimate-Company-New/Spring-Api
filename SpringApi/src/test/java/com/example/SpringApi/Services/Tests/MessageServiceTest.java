package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetMessagesInBatchesTests               | 1               |
 * | CreateMessageTests                      | 20              |
 * | UpdateMessageTests                      | 18              |
 * | ToggleMessageTests                      | 3               |
 * | GetMessageDetailsByIdTests              | 2               |
 * | GetMessagesByUserIdTests                | 4               |
 * | SetMessageReadTests                     | 4               |
 * | GetUnreadMessageCountTests              | 3               |
 * | **Total**                               | **55**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Unit Tests")
class MessageServiceTest extends BaseTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageUserReadMapRepository messageUserReadMapRepository;

    @Mock
    private MessageUserMapRepository messageUserMapRepository;

    @Mock
    private MessageUserGroupMapRepository messageUserGroupMapRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private MessageRequestModel validRequest;
    private Client testClient;
    private User testUser;
    
    private static final Long TEST_MESSAGE_ID = DEFAULT_MESSAGE_ID;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = DEFAULT_USER_ID;
    private static final String TEST_TITLE = "Test Message Title";
    private static final String TEST_DESC_HTML = "<p>Test message description</p>";
    private static final String CREATED_USER = DEFAULT_CREATED_USER;
    private static final String TEST_EMAIL = "test@example.com";
    
    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize valid request
        validRequest = createValidMessageRequest();
        validRequest.setMessageId(TEST_MESSAGE_ID);
        validRequest.setTitle(TEST_TITLE);
        validRequest.setDescriptionHtml(TEST_DESC_HTML);
        validRequest.setPublishDate(null);
        validRequest.setSendAsEmail(false);
        validRequest.setIsDeleted(false);
        validRequest.setUserIds(Arrays.asList(TEST_USER_ID));
        validRequest.setUserGroupIds(Arrays.asList(1L, 2L));

        // Initialize test message
        testMessage = createTestMessage();
        testMessage.setMessageId(TEST_MESSAGE_ID);
        testMessage.setTitle(TEST_TITLE);
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());

        // Initialize test client
        testClient = createTestClient();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setSendGridApiKey("test-api-key");
        testClient.setSendGridEmailAddress("test@sendgrid.com");
        testClient.setSendgridSenderName("Test Sender");

        // Initialize test user
        testUser = createTestUser();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setIsDeleted(false);
    }


    @Nested
    @DisplayName("GetMessagesInBatches Tests")
    class GetMessagesInBatchesTests {
        /**
         * Purpose: Validate pagination, filter column validation, and success retrieval in one flow.
         * Expected Result: Invalid inputs throw expected errors and valid inputs return a populated response.
         * Assertions: Errors are thrown for invalid pagination/columns; response size/count match on success.
         */
        @Test
        @DisplayName("Get Messages In Batches - Invalid pagination, success no filters, and column validation")
        void getMessagesInBatches_SingleComprehensiveTest() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            
            // (1) Invalid pagination: end <= start
            paginationRequest.setStart(10);
            paginationRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> messageService.getMessagesInBatches(paginationRequest));

            // (2) Success: simple retrieval without filters
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            paginationRequest.setFilters(null);
            paginationRequest.setIncludeDeleted(false);

            List<Message> messages = Arrays.asList(testMessage);
            Page<Message> messagePage = new PageImpl<>(messages);

            lenient().when(messageRepository.findPaginatedMessages(
                anyLong(), isNull(), isNull(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(messagePage);

            PaginationBaseResponseModel<MessageResponseModel> result = 
                messageService.getMessagesInBatches(paginationRequest);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getTotalDataCount());
            assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());

            // (3) Invalid column name validation
            String[] validColumns = {
                "messageId", "title", "publishDate", "descriptionHtml", "sendAsEmail",
                "isDeleted", "createdByUserId", "sendgridEmailBatchId", "createdAt",
                "updatedAt", "notes", "createdUser", "modifiedUser"
            };
            String[] invalidColumns = {"invalidColumn", "nonExistentField", "wrongField"};

            // Test invalid columns
            for (String invalidCol : invalidColumns) {
                paginationRequest.setFilters(List.of(createFilterCondition(invalidCol, "equals", "test")));
                assertThrowsBadRequest("Invalid column name: " + invalidCol,
                        () -> messageService.getMessagesInBatches(paginationRequest));
            }

            // Test valid columns (should not throw for column validation)
            for (String validCol : validColumns) {
                paginationRequest.setFilters(List.of(createFilterCondition(validCol, "equals", "test")));
                // Note: MessageService only validates column names, not operators/types
                // So valid columns should pass column validation (may fail later in repository if operator/value invalid)
                lenient().when(messageRepository.findPaginatedMessages(
                    anyLong(), isNull(), isNull(), isNull(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Arrays.asList()));
                assertDoesNotThrow(() -> messageService.getMessagesInBatches(paginationRequest));
            }
        }
    }


    @Nested
    @DisplayName("CreateMessage Tests")
    class CreateMessageTests {
        /**
         * Purpose: Verify message creation without email delivery.
         * Expected Result: Message is saved and user log is written.
         * Assertions: Repository saves and log call are invoked.
         */
        @Test
        @DisplayName("Create Message - Success - No email")
        void createMessage_Success_NoEmail() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(new MessageUserMap());
            when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class))).thenReturn(new MessageUserGroupMap());

            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            verify(messageRepository).save(any(Message.class));
            verify(messageUserMapRepository, times(1)).save(any(MessageUserMap.class));
            verify(messageUserGroupMapRepository, times(2)).save(any(MessageUserGroupMap.class));
            verify(userLogService).logDataWithContext(
                eq(TEST_USER_ID),
                eq("admin"),
                eq(TEST_CLIENT_ID),
                contains("Successfully inserted message"),
                eq(ApiRoutes.MessagesSubRoute.CREATE_MESSAGE)
            );
        }

        /**
         * Purpose: Ensure missing client results in not found error.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches expected client invalid ID.
         */
        @Test
        @DisplayName("Create Message - Client not found - Throws NotFoundException")
        void createMessage_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                    () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Validate email publish date cannot be in the past.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER009.
         */
        @Test
        @DisplayName("Create Message - Email in past - Throws BadRequestException")
        void createMessage_EmailInPast_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009,
                    () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Validate email publish date cannot exceed 72 hours.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message indicates batch ID generation failure.
         */
        @Test
        @DisplayName("Create Message - Email beyond 72 hours - Throws BadRequestException")
        void createMessage_EmailBeyond72Hours_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(74));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010,
                    () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Validate create requires at least one user/group target.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER008.
         */
        @Test
        @DisplayName("Create Message - No targets - Success without maps")
        void createMessage_NoTargets_SuccessWithoutMaps() {
            validRequest.setUserIds(null);
            validRequest.setUserGroupIds(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(validRequest));

            assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
        }

        /**
         * Purpose: Validate create rejects empty target lists.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER008.
         */
        @Test
        @DisplayName("Create Message - Empty targets - Success without maps")
        void createMessage_EmptyTargets_SuccessWithoutMaps() {
            validRequest.setUserIds(Arrays.asList());
            validRequest.setUserGroupIds(Arrays.asList());

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(validRequest));

            assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
        }

        /**
         * Purpose: Verify sendAsEmail false does not query recipients.
         * Expected Result: Message is created without email lookup.
         * Assertions: userRepository recipient query is never called.
         */
        @Test
        @DisplayName("Create Message - SendAsEmail false - No recipient lookup")
        void createMessage_SendAsEmailFalse_NoRecipientLookup() {
            validRequest.setSendAsEmail(false);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            verify(userRepository, never()).findAllUserEmailsByClientAndUserIdsAndGroupIds(
                anyLong(), anyList(), anyList());
        }

        /**
         * Purpose: Reject null message request.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Create Message - Null request - Throws BadRequestException")
        void createMessage_NullRequest_ThrowsBadRequestException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessage(null));

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject null title on create.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Create Message - Null title - Throws BadRequestException")
        void createMessage_NullTitle_ThrowsBadRequestException() {
            validRequest.setTitle(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject blank title on create.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Create Message - Blank title - Throws BadRequestException")
        void createMessage_BlankTitle_ThrowsBadRequestException() {
            validRequest.setTitle("   ");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER003,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject title longer than 500 chars.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches TitleTooLong.
         */
        @Test
        @DisplayName("Create Message - Title too long - Throws BadRequestException")
        void createMessage_TitleTooLong_ThrowsBadRequestException() {
            validRequest.setTitle("A".repeat(501));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.TitleTooLong,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject null descriptionHtml on create.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @Test
        @DisplayName("Create Message - Null descriptionHtml - Throws BadRequestException")
        void createMessage_NullDescriptionHtml_ThrowsBadRequestException() {
            validRequest.setDescriptionHtml(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject blank descriptionHtml on create.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @Test
        @DisplayName("Create Message - Blank descriptionHtml - Throws BadRequestException")
        void createMessage_BlankDescriptionHtml_ThrowsBadRequestException() {
            validRequest.setDescriptionHtml("   ");

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER007,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject publishDate when sendAsEmail is false.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches PublishDateRequiresSendAsEmail.
         */
        @Test
        @DisplayName("Create Message - Publish date without sendAsEmail - Throws BadRequestException")
        void createMessage_PublishDateRequiresSendAsEmail_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(false);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Reject publishDate beyond 72 hours.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER010.
         */
        @Test
        @DisplayName("Create Message - Publish date beyond 72 hours - Throws BadRequestException")
        void createMessage_PublishDateBeyond72Hours_ThrowsBadRequestExceptionWithER010() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(74));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010,
                () -> messageService.createMessage(validRequest));
        }

        /**
         * Purpose: Ensure sendAsEmail true with publishDate generates batch ID.
         * Expected Result: Message saved with non-null batch ID.
         * Assertions: Saved message has sendgridEmailBatchId set.
         */
        @Test
        @DisplayName("Create Message - SendAsEmail with publishDate - Generates batch ID")
        void createMessage_SendAsEmailWithPublishDate_GeneratesBatchId() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                    (mock, context) -> when(mock.generateBatchId()).thenReturn("batch-123"))) {

                assertDoesNotThrow(() -> messageService.createMessage(validRequest));

                ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
                verify(messageRepository).save(messageCaptor.capture());
                assertEquals("batch-123", messageCaptor.getValue().getSendgridEmailBatchId());
            }
        }

        /**
         * Purpose: Ensure sendAsEmail true with no publishDate does not generate batch ID.
         * Expected Result: Message saved with null batch ID.
         * Assertions: Saved message has null sendgridEmailBatchId.
         */
        @Test
        @DisplayName("Create Message - SendAsEmail without publishDate - No batch ID")
        void createMessage_SendAsEmailWithoutPublishDate_NoBatchId() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            assertNull(messageCaptor.getValue().getSendgridEmailBatchId());
        }

        /**
         * Purpose: Ensure email template is used when recipients exist.
         * Expected Result: EmailTemplates.sendMessageEmail is invoked.
         * Assertions: sendMessageEmail is called with expected values.
         */
        @Test
        @DisplayName("Create Message - SendAsEmail true with recipients - Sends email")
        void createMessage_SendAsEmailWithRecipients_SendsEmail() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(validRequest.getPublishDate());
            testMessage.setDescriptionHtml(TEST_DESC_HTML);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
                .thenReturn(List.of(TEST_EMAIL));

            try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                    (mock, context) -> when(mock.generateBatchId()).thenReturn("batch-email"));
                 MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class,
                    (mock, context) -> when(mock.sendMessageEmail(anyList(), anyString(), anyString(), any(), any())).thenReturn(true))) {
                assertDoesNotThrow(() -> messageService.createMessage(validRequest));

                EmailTemplates constructed = templatesMock.constructed().get(0);
                verify(constructed).sendMessageEmail(
                    eq(List.of(TEST_EMAIL)),
                    eq(TEST_TITLE),
                    eq(TEST_DESC_HTML),
                    eq(validRequest.getPublishDate()),
                    any()
                );
            }
        }

        /**
         * Purpose: Ensure no email is sent when recipients list is empty.
         * Expected Result: EmailTemplates is never constructed.
         * Assertions: No EmailTemplates instances are created.
         */
        @Test
        @DisplayName("Create Message - SendAsEmail true with no recipients - No email")
        void createMessage_SendAsEmailNoRecipients_NoEmailSent() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(null);

            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(anyLong(), anyList(), anyList()))
                .thenReturn(List.of());

            try (MockedConstruction<EmailTemplates> templatesMock = mockConstruction(EmailTemplates.class)) {
                assertDoesNotThrow(() -> messageService.createMessage(validRequest));
                assertTrue(templatesMock.constructed().isEmpty());
            }
        }

        /**
         * Purpose: Validate invalid requesting user ID is rejected in context method.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidUserId.
         */
        @Test
        @DisplayName("Create Message With Context - Invalid userId - Throws BadRequestException")
        void createMessageWithContext_InvalidUserId_ThrowsBadRequestException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.createMessageWithContext(validRequest, 0L, "admin", TEST_CLIENT_ID));

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidUserId, exception.getMessage());
        }
    }


    @Nested
    @DisplayName("UpdateMessage Tests")
    class UpdateMessageTests {
        /**
         * Purpose: Verify update succeeds for a valid existing message.
         * Expected Result: Message is saved and related mappings are refreshed.
         * Assertions: Save/delete calls occur and user log is written.
         */
        @Test
        @DisplayName("Update Message - Success")
        void updateMessage_Success() {
        // Note: BaseService methods are now handled by the actual service implementation
        // Note: BaseService methods are now handled by the actual service implementation
        // Note: BaseService methods are now handled by the actual service implementation

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(new MessageUserMap());
        when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class))).thenReturn(new MessageUserGroupMap());

        assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

        verify(messageRepository).save(any(Message.class));
        verify(messageUserMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(messageUserGroupMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
        verify(userLogService).logData(
            eq(TEST_USER_ID),
            contains("Successfully updated message"),
            eq(ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE)
        );
    }

        /**
         * Purpose: Validate update requires a message ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Message - Null message ID - Throws BadRequestException")
        void updateMessage_NullMessageId_ThrowsBadRequestException() {
            validRequest.setMessageId(null);

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Ensure updating a non-existent message fails.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Message - Message not found - Throws NotFoundException")
        void updateMessage_MessageNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Prevent updates to messages already sent.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER011.
         */
        @Test
        @DisplayName("Update Message - Message already sent - Throws BadRequestException")
        void updateMessage_MessageAlreadySent_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER011,
                    () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Validate update rejects empty target lists.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER008.
         */
        @Test
        @DisplayName("Update Message - Empty targets - No map inserts")
        void updateMessage_EmptyTargets_NoMapInserts() {
            validRequest.setUserIds(Arrays.asList());
            validRequest.setUserGroupIds(Arrays.asList());

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.updateMessage(validRequest));

            assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
        }

        /**
         * Purpose: Validate update requires a client to exist.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Message - Client not found - Throws NotFoundException")
        void updateMessage_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Validate update rejects null targets.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER008.
         */
        @Test
        @DisplayName("Update Message - Null targets - Throws BadRequestException")
        void updateMessage_NullTargets_ThrowsBadRequestException() {
            validRequest.setUserIds(null);
            validRequest.setUserGroupIds(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            BadRequestException exception = assertThrows(BadRequestException.class,
                () -> messageService.updateMessage(validRequest));

            assertEquals(ErrorMessages.MessagesErrorMessages.ER008, exception.getMessage());
        }

        /**
         * Purpose: Reject publishDate when sendAsEmail is false during update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches PublishDateRequiresSendAsEmail.
         */
        @Test
        @DisplayName("Update Message - Publish date without sendAsEmail - Throws BadRequestException")
        void updateMessage_PublishDateRequiresSendAsEmail_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(false);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.PublishDateRequiresSendAsEmail,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Prevent disabling sendAsEmail after it was enabled.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches CannotDisableSendAsEmailOnce.
         */
        @Test
        @DisplayName("Update Message - Cannot disable sendAsEmail once enabled - Throws BadRequestException")
        void updateMessage_CannotDisableSendAsEmailOnce_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(null);
            validRequest.setSendAsEmail(false);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Prevent adding publishDate after sendAsEmail true with no prior date.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches CannotAddPublishDateAfterSent.
         */
        @Test
        @DisplayName("Update Message - Cannot add publishDate after sendAsEmail without date - Throws BadRequestException")
        void updateMessage_CannotAddPublishDateAfterSent_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(null);
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotAddPublishDateAfterSent,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Prevent modifying scheduled publishDate.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches CannotModifyScheduledPublishDate.
         */
        @Test
        @DisplayName("Update Message - Cannot modify scheduled publishDate - Throws BadRequestException")
        void updateMessage_CannotModifyScheduledPublishDate_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(25));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotModifyScheduledPublishDate,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Prevent disabling sendAsEmail for scheduled messages.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches CannotDisableSendAsEmailScheduled.
         */
        @Test
        @DisplayName("Update Message - Cannot disable sendAsEmail for scheduled - Throws BadRequestException")
        void updateMessage_CannotDisableSendAsEmailScheduled_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
            validRequest.setSendAsEmail(false);
            validRequest.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.CannotDisableSendAsEmailOnce,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Reject publishDate in the past during update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER009.
         */
        @Test
        @DisplayName("Update Message - Publish date in past - Throws BadRequestException")
        void updateMessage_PublishDateInPast_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).minusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Reject publishDate beyond 72 hours during update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER010.
         */
        @Test
        @DisplayName("Update Message - Publish date beyond 72 hours - Throws BadRequestException")
        void updateMessage_PublishDateBeyond72Hours_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(74));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER010,
                () -> messageService.updateMessage(validRequest));
        }

        /**
         * Purpose: Cancel scheduled email when sendAsEmail is disabled.
         * Expected Result: EmailHelper.cancelEmail is called.
         * Assertions: cancelEmail is invoked with existing batch ID.
         */
        @Test
        @DisplayName("Update Message - Disable sendAsEmail cancels batch")
        void updateMessage_DisableSendAsEmail_CancelsBatch() {
            testMessage.setSendgridEmailBatchId("batch-999");
            testMessage.setSendAsEmail(false);
            validRequest.setSendAsEmail(false);
            validRequest.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                    (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
                assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

                EmailHelper helper = emailHelperMock.constructed().get(0);
                verify(helper).cancelEmail("batch-999");
            }
        }

        /**
         * Purpose: Cancel scheduled email when publishDate changes.
         * Expected Result: EmailHelper.cancelEmail is called.
         * Assertions: cancelEmail is invoked with existing batch ID.
         */
        @Test
        @DisplayName("Update Message - Publish date change cancels batch")
        void updateMessage_PublishDateChange_CancelsBatch() {
            testMessage.setSendgridEmailBatchId("batch-888");
            testMessage.setSendAsEmail(false);
            testMessage.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(24));
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(25));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                    (mock, context) -> doNothing().when(mock).cancelEmail(anyString()))) {
                assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

                EmailHelper helper = emailHelperMock.constructed().get(0);
                verify(helper).cancelEmail("batch-888");
            }
        }

        /**
         * Purpose: Generate new batch ID for scheduled email update.
         * Expected Result: Updated message saved with new batch ID.
         * Assertions: sendgridEmailBatchId is set to new value.
         */
        @Test
        @DisplayName("Update Message - SendAsEmail with publishDate - Generates new batch ID")
        void updateMessage_SendAsEmailWithPublishDate_GeneratesNewBatchId() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

            testMessage.setSendAsEmail(false);
            testMessage.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            try (MockedConstruction<EmailHelper> emailHelperMock = mockConstruction(EmailHelper.class,
                    (mock, context) -> when(mock.generateBatchId()).thenReturn("batch-new"))) {
                assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

                ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
                verify(messageRepository).save(messageCaptor.capture());
                assertEquals("batch-new", messageCaptor.getValue().getSendgridEmailBatchId());
            }
        }

        /**
         * Purpose: Clear batch ID when sendAsEmail is false.
         * Expected Result: Updated message saved with null batch ID.
         * Assertions: sendgridEmailBatchId is null.
         */
        @Test
        @DisplayName("Update Message - SendAsEmail false clears batch ID")
        void updateMessage_SendAsEmailFalse_ClearsBatchId() {
            validRequest.setSendAsEmail(false);
            validRequest.setPublishDate(null);

            testMessage.setSendAsEmail(false);
            testMessage.setPublishDate(null);

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

            assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            assertNull(messageCaptor.getValue().getSendgridEmailBatchId());
        }
    }

    @Nested
    @DisplayName("ToggleMessage Tests")
    class ToggleMessageTests {

        /**
         * Purpose: Verify toggling updates the deleted flag.
         * Expected Result: Message is toggled and log entry recorded.
         * Assertions: Saved entity has deleted flag set and log is called.
         */
        @Test
        @DisplayName("Toggle Message - Success")
        void toggleMessage_Success() {
        testMessage.setIsDeleted(false);

        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID)))
            .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

        verify(messageRepository).save(argThat(msg -> msg.getIsDeleted() == true));
        verify(userLogService).logData(
            anyLong(),
            contains("Successfully toggled message"),
            eq(ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE)
        );
    }

        /**
         * Purpose: Ensure toggle fails when message does not exist.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Toggle Message - Message not found - Throws NotFoundException")
        void toggleMessage_MessageNotFound_ThrowsNotFoundException() {
            lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.toggleMessage(TEST_MESSAGE_ID));
        }

        /**
         * Purpose: Verify toggling a deleted message restores it.
         * Expected Result: isDeleted becomes false.
         * Assertions: Saved entity has isDeleted false.
         */
        @Test
        @DisplayName("Toggle Message - Deleted to active - Success")
        void toggleMessage_DeletedToActive_Success() {
            testMessage.setIsDeleted(true);

            when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID)))
                .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

            verify(messageRepository).save(argThat(msg -> msg.getIsDeleted() == false));
        }
    }


    @Nested
    @DisplayName("GetMessageDetailsById Tests")
    class GetMessageDetailsByIdTests {

        /**
         * Purpose: Validate message detail retrieval by ID.
         * Expected Result: Response contains message details.
         * Assertions: Response ID and title match test message.
         */
        @Test
        @DisplayName("Get Message Details By ID - Success")
        void getMessageDetailsById_Success() {
        // Note: BaseService methods are now handled by the actual service implementation

        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getMessageId());
        assertEquals(TEST_TITLE, result.getTitle());
    }

        /**
         * Purpose: Ensure not found is thrown for missing message details.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Message Details By ID - Message not found - Throws NotFoundException")
        void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
            when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        }
    }

    @Nested
    @DisplayName("GetMessagesByUserId Tests")
    class GetMessagesByUserIdTests {

        /**
         * Purpose: Verify messages are returned for a valid user with pagination.
         * Expected Result: Response contains messages and read status.
         * Assertions: Response size, ID, and read flag are validated.
         */
        @Test
        @DisplayName("Get Messages By User ID - Success")
        void getMessagesByUserId_Success() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            paginationRequest.setId(TEST_USER_ID);

        List<Message> messages = Arrays.asList(testMessage);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(
            eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
            .thenReturn(messagePage);
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
            .thenReturn(null);

        PaginationBaseResponseModel<MessageResponseModel> result = 
            messageService.getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
        assertFalse(result.getData().get(0).getIsRead());
    }

        /**
         * Purpose: Ensure user validation occurs before fetching messages.
         * Expected Result: NotFoundException is thrown for missing user.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Get Messages By User ID - User not found - Throws NotFoundException")
        void getMessagesByUserId_UserNotFound_ThrowsNotFoundException() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            paginationRequest.setId(TEST_USER_ID);

            when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                    () -> messageService.getMessagesByUserId(paginationRequest));
        }

        /**
         * Purpose: Verify read status is true when read map exists.
         * Expected Result: Response marks message as read.
         * Assertions: isRead is true in response.
         */
        @Test
        @DisplayName("Get Messages By User ID - Read map exists - isRead true")
        void getMessagesByUserId_ReadMapExists_IsReadTrue() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            paginationRequest.setId(TEST_USER_ID);

            List<Message> messages = Arrays.asList(testMessage);
            Page<Message> messagePage = new PageImpl<>(messages);

            when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testUser));
            when(messageRepository.findMessagesByUserIdPaginated(
                eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(messagePage);
            when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
                .thenReturn(new MessageUserReadMap());

            PaginationBaseResponseModel<MessageResponseModel> result =
                messageService.getMessagesByUserId(paginationRequest);

            assertNotNull(result);
            assertTrue(result.getData().get(0).getIsRead());
        }

        /**
         * Purpose: Verify default page size is used when end-start is non-positive.
         * Expected Result: Request succeeds and returns results.
         * Assertions: Response contains expected message.
         */
        @Test
        @DisplayName("Get Messages By User ID - Non-positive range - Uses default size")
        void getMessagesByUserId_DefaultPageSize_WhenRangeNonPositive() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            paginationRequest.setId(TEST_USER_ID);
            paginationRequest.setStart(0);
            paginationRequest.setEnd(0);

            Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));

            when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testUser));
            when(messageRepository.findMessagesByUserIdPaginated(
                eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(messagePage);
            when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
                .thenReturn(null);

            PaginationBaseResponseModel<MessageResponseModel> result =
                messageService.getMessagesByUserId(paginationRequest);

            assertNotNull(result);
            assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
        }
    }


    @Nested
    @DisplayName("SetMessageRead Tests")
    class SetMessageReadTests {

        /**
         * Purpose: Verify a message is marked read for a valid user/message.
         * Expected Result: Read map is created and log is written.
         * Assertions: Save call and log call are executed.
         */
        @Test
        @DisplayName("Set Message Read - Success")
        void setMessageReadByUserIdAndMessageId_Success() {
        // Note: BaseService methods are now handled by the actual service implementation
        // Note: BaseService methods are now handled by the actual service implementation
        // Note: BaseService methods are now handled by the actual service implementation

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
            .thenReturn(null);
        when(messageUserReadMapRepository.save(any(MessageUserReadMap.class)))
            .thenReturn(new MessageUserReadMap());

        assertDoesNotThrow(() -> 
            messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));

        verify(messageUserReadMapRepository).save(any(MessageUserReadMap.class));
        verify(userLogService).logData(
            eq(TEST_USER_ID),
            contains("Successfully marked message as read"),
            eq(ApiRoutes.MessagesSubRoute.SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID)
        );
    }

        /**
         * Purpose: Ensure no duplicate read map is created if already read.
         * Expected Result: No new read map is saved.
         * Assertions: Save is never called.
         */
        @Test
        @DisplayName("Set Message Read - Already read - No duplicate")
        void setMessageReadByUserIdAndMessageId_AlreadyRead_NoDuplicate() {
        // Note: BaseService methods are now handled by the actual service implementation

        MessageUserReadMap existingRead = new MessageUserReadMap();

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
            .thenReturn(existingRead);

        assertDoesNotThrow(() -> 
            messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID));

        verify(messageUserReadMapRepository, never()).save(any(MessageUserReadMap.class));
    }

        /**
         * Purpose: Validate error when user is missing while setting read.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Set Message Read - User not found - Throws NotFoundException")
        void setMessageReadByUserIdAndMessageId_UserNotFound_ThrowsNotFoundException() {
        // Note: BaseService methods are now handled by the actual service implementation

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);
        });

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

        /**
         * Purpose: Validate error when message is missing while setting read.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Set Message Read - Message not found - Throws NotFoundException")
        void setMessageReadByUserIdAndMessageId_MessageNotFound_ThrowsNotFoundException() {
        // Note: BaseService methods are now handled by the actual service implementation

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testUser));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }
    }


    @Nested
    @DisplayName("GetUnreadMessageCount Tests")
    class GetUnreadMessageCountTests {

        /**
         * Purpose: Verify unread count returns correct non-zero value.
         * Expected Result: Count matches repository result.
         * Assertions: Returned value equals repository count.
         */
        @Test
        @DisplayName("Get Unread Message Count - Success")
        void getUnreadMessageCount_Success() {
        // Note: BaseService methods are now handled by the actual service implementation

        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
            .thenReturn(5L);

        int result = messageService.getUnreadMessageCount();

        assertEquals(5, result);
        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

        /**
         * Purpose: Verify zero unread messages is handled.
         * Expected Result: Zero count is returned.
         * Assertions: Returned value equals zero.
         */
        @Test
        @DisplayName("Get Unread Message Count - No unread messages - Returns zero")
        void getUnreadMessageCount_NoUnreadMessages_ReturnsZero() {
        // Note: BaseService methods are now handled by the actual service implementation

        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
            .thenReturn(0L);

        int result = messageService.getUnreadMessageCount();

        assertEquals(0, result);
        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

        /**
         * Purpose: Validate large unread counts are returned correctly.
         * Expected Result: Large count is returned.
         * Assertions: Returned value equals 1000.
         */
        @Test
        @DisplayName("Get Unread Message Count - Large count - Success")
        void getUnreadMessageCount_LargeCount_Success() {
            when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                .thenReturn(1000L);

            int result = messageService.getUnreadMessageCount();

            assertEquals(1000, result);
            verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
        }
    }
}
