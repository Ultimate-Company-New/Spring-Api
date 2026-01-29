package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserLogService;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 *
 * <p>This test class provides comprehensive coverage of MessageService methods including:
 * - CRUD operations (create, update, toggle, get)
 * - Message validation and error handling
 * - Email scheduling and SendGrid integration
 * - Pagination and filtering
 * - User targeting and read status
 * - Audit logging verification
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
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
    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
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

    // ==================== GET MESSAGES IN BATCHES TESTS ====================

    @Nested
    @DisplayName("GetMessagesInBatches Tests")
    class GetMessagesInBatchesTests {
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

    // ==================== CREATE MESSAGE TESTS ====================

    @Nested
    @DisplayName("CreateMessage Tests")
    class CreateMessageTests {
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
            verify(userLogService).logData(
                eq(TEST_USER_ID),
                contains("Successfully inserted message"),
                eq(ApiRoutes.MessagesSubRoute.CREATE_MESSAGE)
            );
        }

        @Test
        @DisplayName("Create Message - Client not found - Throws NotFoundException")
        void createMessage_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                    () -> messageService.createMessage(validRequest));
        }

        @Test
        @DisplayName("Create Message - Email in past - Throws BadRequestException")
        void createMessage_EmailInPast_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now().minusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER009,
                    () -> messageService.createMessage(validRequest));
        }

        @Test
        @DisplayName("Create Message - Email beyond 72 hours - Throws BadRequestException")
        void createMessage_EmailBeyond72Hours_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now().plusHours(73));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.createMessage(validRequest);
            });

            assertTrue(exception.getMessage().contains("Failed to generate batch ID"));
        }
    }

    // ==================== UPDATE MESSAGE TESTS ====================

    @Nested
    @DisplayName("UpdateMessage Tests")
    class UpdateMessageTests {
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

        @Test
        @DisplayName("Update Message - Null message ID - Throws BadRequestException")
        void updateMessage_NullMessageId_ThrowsBadRequestException() {
            validRequest.setMessageId(null);

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.updateMessage(validRequest));
        }

        @Test
        @DisplayName("Update Message - Message not found - Throws NotFoundException")
        void updateMessage_MessageNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.updateMessage(validRequest));
        }

        @Test
        @DisplayName("Update Message - Message already sent - Throws BadRequestException")
        void updateMessage_MessageAlreadySent_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(LocalDateTime.now().minusHours(1));

            when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

            assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.ER011,
                    () -> messageService.updateMessage(validRequest));
        }
    }

    // ==================== TOGGLE MESSAGE TESTS ====================

    @Nested
    @DisplayName("ToggleMessage Tests")
    class ToggleMessageTests {

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

        @Test
        @DisplayName("Toggle Message - Message not found - Throws NotFoundException")
        void toggleMessage_MessageNotFound_ThrowsNotFoundException() {
            lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.toggleMessage(TEST_MESSAGE_ID));
        }
    }

    // ==================== GET MESSAGE DETAILS BY ID TESTS ====================

    @Nested
    @DisplayName("GetMessageDetailsById Tests")
    class GetMessageDetailsByIdTests {

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

        @Test
        @DisplayName("Get Message Details By ID - Message not found - Throws NotFoundException")
        void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
            when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

            assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                    () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        }
    }

    // ==================== GET MESSAGES BY USER ID TESTS ====================

    @Nested
    @DisplayName("GetMessagesByUserId Tests")
    class GetMessagesByUserIdTests {

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
    }

    // ==================== SET MESSAGE READ TESTS ====================

    @Nested
    @DisplayName("SetMessageRead Tests")
    class SetMessageReadTests {

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

    // ==================== GET UNREAD MESSAGE COUNT TESTS ====================

    @Nested
    @DisplayName("GetUnreadMessageCount Tests")
    class GetUnreadMessageCountTests {

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

    @Nested
    @DisplayName("Message Service - Comprehensive Validation Tests")
    class MessageValidationTests {

        @Nested
        @DisplayName("Get Messages In Batches - Pagination Validation")
        class GetMessagesInBatchesPaginationTests {

            @Test
            @DisplayName("Get Messages - Null pagination request - Throws BadRequestException")
            void getMessagesInBatches_NullRequest_ThrowsBadRequest() {
                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(null));
            }

            @Test
            @DisplayName("Get Messages - Invalid pagination (end <= start) - Throws BadRequestException")
            void getMessagesInBatches_InvalidPagination_EndLessThanStart() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(20);
                request.setEnd(10);

                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(request));
            }

            @Test
            @DisplayName("Get Messages - Invalid pagination (start equals end) - Throws BadRequestException")
            void getMessagesInBatches_InvalidPagination_StartEqualsEnd() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(10);
                request.setEnd(10);

                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(request));
            }

            @Test
            @DisplayName("Get Messages - Negative start - Throws BadRequestException")
            void getMessagesInBatches_NegativeStart_ThrowsBadRequest() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(-5);
                request.setEnd(10);

                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(request));
            }

            @Test
            @DisplayName("Get Messages - Negative end - Throws BadRequestException")
            void getMessagesInBatches_NegativeEnd_ThrowsBadRequest() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(0);
                request.setEnd(-10);

                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(request));
            }

            @Test
            @DisplayName("Get Messages - Zero start and end - Throws BadRequestException")
            void getMessagesInBatches_ZeroPagination_ThrowsBadRequest() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(0);
                request.setEnd(0);

                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                        () -> messageService.getMessagesInBatches(request));
            }

            @Test
            @DisplayName("Get Messages - Valid pagination - Success")
            void getMessagesInBatches_ValidPagination_Success() {
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(0);
                request.setEnd(10);

                Page<Message> page = new PageImpl<>(Collections.singletonList(testMessage));
                when(messageRepository.findByClientIdAndIsDeletedFalse(eq(TEST_CLIENT_ID), any(Pageable.class)))
                        .thenReturn(page);

                PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesInBatches(request);

                assertNotNull(result);
                verify(messageRepository).findByClientIdAndIsDeletedFalse(eq(TEST_CLIENT_ID), any(Pageable.class));
            }
        }

        @Nested
        @DisplayName("Schedule Message - Email Validation")
        class ScheduleMessageEmailTests {

            @Test
            @DisplayName("Schedule Message - Null request - Throws BadRequestException")
            void scheduleMessage_NullRequest_ThrowsBadRequest() {
                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidRequest,
                        () -> messageService.scheduleMessage(null));
            }

            @Test
            @DisplayName("Schedule Message - Null recipients - Throws BadRequestException")
            void scheduleMessage_NullRecipients_ThrowsBadRequest() {
                testMessageRequest.setRecipients(null);

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidRecipients,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Empty recipients list - Throws BadRequestException")
            void scheduleMessage_EmptyRecipients_ThrowsBadRequest() {
                testMessageRequest.setRecipients(Collections.emptyList());

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidRecipients,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Null subject - Throws BadRequestException")
            void scheduleMessage_NullSubject_ThrowsBadRequest() {
                testMessageRequest.setSubject(null);

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidSubject,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Empty subject - Throws BadRequestException")
            void scheduleMessage_EmptySubject_ThrowsBadRequest() {
                testMessageRequest.setSubject("");

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidSubject,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Null body - Throws BadRequestException")
            void scheduleMessage_NullBody_ThrowsBadRequest() {
                testMessageRequest.setBody(null);

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidBody,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Empty body - Throws BadRequestException")
            void scheduleMessage_EmptyBody_ThrowsBadRequest() {
                testMessageRequest.setBody("");

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidBody,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Null scheduled date - Throws BadRequestException")
            void scheduleMessage_NullScheduledDate_ThrowsBadRequest() {
                testMessageRequest.setScheduledDate(null);

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.InvalidScheduledDate,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Past scheduled date - Throws BadRequestException")
            void scheduleMessage_PastScheduledDate_ThrowsBadRequest() {
                testMessageRequest.setScheduledDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1));

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.ScheduledDatePast,
                        () -> messageService.scheduleMessage(testMessageRequest));
            }

            @Test
            @DisplayName("Schedule Message - Very large recipient count - Success")
            void scheduleMessage_LargeRecipientCount_Success() {
                List<Long> manyRecipients = new ArrayList<>();
                for (int i = 1; i <= 1000; i++) {
                    manyRecipients.add((long) i);
                }
                testMessageRequest.setRecipients(manyRecipients);

                when(userRepository.findByUserIdAndIsDeletedFalse(anyLong()))
                        .thenReturn(Optional.of(testUser));
                when(messageRepository.save(any(Message.class)))
                        .thenReturn(testMessage);

                Message result = messageService.scheduleMessage(testMessageRequest);

                assertNotNull(result);
                verify(messageRepository).save(any(Message.class));
            }
        }

        @Nested
        @DisplayName("Send Message - User Validation")
        class SendMessageUserTests {

            @Test
            @DisplayName("Send Message - User not found - Throws NotFoundException")
            void sendMessage_UserNotFound_ThrowsNotFound() {
                when(userRepository.findByUserIdAndIsDeletedFalse(TEST_USER_ID))
                        .thenReturn(Optional.empty());

                assertThrowsNotFound(String.format(ErrorMessages.UserErrorMessages.NotFound, TEST_USER_ID),
                        () -> messageService.sendMessage(testMessageRequest, TEST_USER_ID));
            }

            @Test
            @DisplayName("Send Message - Message not found - Throws NotFoundException")
            void sendMessage_MessageNotFound_ThrowsNotFound() {
                when(userRepository.findByUserIdAndIsDeletedFalse(TEST_USER_ID))
                        .thenReturn(Optional.of(testUser));
                when(messageRepository.findByMessageIdAndIsDeletedFalse(TEST_MESSAGE_ID))
                        .thenReturn(Optional.empty());

                assertThrowsNotFound(String.format(ErrorMessages.MessageErrorMessages.NotFound, TEST_MESSAGE_ID),
                        () -> messageService.sendMessage(testMessageRequest, TEST_USER_ID));
            }

            @Test
            @DisplayName("Send Message - Message already sent - Throws BadRequestException")
            void sendMessage_AlreadySent_ThrowsBadRequest() {
                testMessage.setSentAt(ZonedDateTime.now(ZoneOffset.UTC));
                when(userRepository.findByUserIdAndIsDeletedFalse(TEST_USER_ID))
                        .thenReturn(Optional.of(testUser));
                when(messageRepository.findByMessageIdAndIsDeletedFalse(TEST_MESSAGE_ID))
                        .thenReturn(Optional.of(testMessage));

                assertThrowsBadRequest(ErrorMessages.MessageErrorMessages.AlreadySent,
                        () -> messageService.sendMessage(testMessageRequest, TEST_USER_ID));
            }
        }

        @Nested
        @DisplayName("Get Unread Message Count - Validation")
        class GetUnreadMessageCountTests {

            @Test
            @DisplayName("Get Unread Message Count - Zero unread - Returns 0")
            void getUnreadMessageCount_NoUnread_ReturnsZero() {
                when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                        .thenReturn(0);

                int result = messageService.getUnreadMessageCount();

                assertEquals(0, result);
            }

            @Test
            @DisplayName("Get Unread Message Count - One unread - Returns 1")
            void getUnreadMessageCount_OneUnread_ReturnsOne() {
                when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                        .thenReturn(1);

                int result = messageService.getUnreadMessageCount();

                assertEquals(1, result);
            }

            @Test
            @DisplayName("Get Unread Message Count - Large count - Success")
            void getUnreadMessageCount_LargeCount_Success() {
                when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                        .thenReturn(999999);

                int result = messageService.getUnreadMessageCount();

                assertEquals(999999, result);
            }
        }
    }
}

