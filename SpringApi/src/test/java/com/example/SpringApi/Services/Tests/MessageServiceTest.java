package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
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
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 *
 * <p>
 * This test class provides comprehensive coverage of MessageService methods
 * including:
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
 * @version 2.0
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

    private static final Long TEST_MESSAGE_ID = 1L;
    private static final String TEST_TITLE = "Test Message Title";
    private static final String TEST_DESC_HTML = "<p>Test message description</p>";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize valid request
        validRequest = new MessageRequestModel();
        validRequest.setMessageId(TEST_MESSAGE_ID);
        validRequest.setTitle(TEST_TITLE);
        validRequest.setDescriptionHtml(TEST_DESC_HTML);
        validRequest.setPublishDate(null);
        validRequest.setSendAsEmail(false);
        validRequest.setIsDeleted(false);
        validRequest.setUserIds(Arrays.asList(DEFAULT_USER_ID));
        validRequest.setUserGroupIds(Arrays.asList(1L, 2L));

        // Initialize test message
        testMessage = new Message(validRequest, DEFAULT_USER_ID, DEFAULT_CREATED_USER, DEFAULT_CLIENT_ID);
        testMessage.setMessageId(TEST_MESSAGE_ID);
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());

        // Initialize test client using BaseTest factory
        testClient = createTestClient(DEFAULT_CLIENT_ID);
        testClient.setSendGridApiKey("test-api-key");
        testClient.setSendGridEmailAddress("test@sendgrid.com");
        testClient.setSendgridSenderName("Test Sender");

        // Initialize test user using BaseTest factory
        testUser = createTestUser(DEFAULT_USER_ID, DEFAULT_LOGIN_NAME, DEFAULT_EMAIL);

        // Common Mocks
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        // Mock BaseService behavior (if strictly needed, though usually handled by
        // Service's getUserId())
        // Assuming BaseService extracts from token or we mock calls that use it.
        // Since MessageService extends BaseService, and we are injecting mocks, we rely
        // on partial mocking or expected behavior.
        // However, standard Mockito @InjectMocks doesn't mock the superclass methods.
        // If BaseService.getUserId() is called, it might fail if request mocking isn't
        // enough.
        // But for this test class, we can usually assume `getUserId()` returns a value
        // if `request` is mocked correctly or if we don't spy.
        // Let's assume request mocking handles it. If not, we might need a Spy.
        // For safely, let's leniently mock clientRepository.findById(DEFAULT_CLIENT_ID)
        // as it is used often.
        lenient().when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
    }

    // ==================== GET MESSAGES IN BATCHES TESTS ====================

    @Nested
    @DisplayName("getMessagesInBatches Tests")
    class GetMessagesInBatchesTests {

        @Test
        @DisplayName("Get Messages In Batches - Success - Retrieve messages")
        void getMessagesInBatches_Success() {
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("title");
            filter.setOperator("contains");
            filter.setValue("Test");
            paginationRequest.setFilters(List.of(filter));
            paginationRequest.setLogicOperator("AND");
            paginationRequest.setIncludeDeleted(false);

            List<Message> messages = Arrays.asList(testMessage);
            Page<Message> messagePage = new PageImpl<>(messages);

            lenient().when(messageRepository.findPaginatedMessages(
                    anyLong(), isNull(), isNull(), isNull(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(messagePage);

            PaginationBaseResponseModel<MessageResponseModel> result = messageService
                    .getMessagesInBatches(paginationRequest);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getTotalDataCount());
            assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
        }

        @Test
        @DisplayName("Get Messages In Batches - Invalid Column Name - ThrowsBadRequestException")
        void getMessagesInBatches_InvalidColumnName_ThrowsBadRequestException() {
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
            invalidFilter.setColumn("invalidColumn");
            invalidFilter.setOperator("equals");
            invalidFilter.setValue("Test");
            paginationRequest.setFilters(List.of(invalidFilter));

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.getMessagesInBatches(paginationRequest);
            });

            assertTrue(exception.getMessage().contains("Invalid column name"));
        }

        @Test
        @DisplayName("Get Messages In Batches - Invalid Pagination - ThrowsBadRequestException")
        void getMessagesInBatches_InvalidPagination_ThrowsBadRequestException() {
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(10);
            paginationRequest.setEnd(5);

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.getMessagesInBatches(paginationRequest);
            });

            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, exception.getMessage());
        }

        /**
         * Triple Loop Test for Filter Validation (Columns).
         * MessageService explicitly validates columns.
         */
        @Test
        @DisplayName("Get Messages In Batches - Filter Logic Triple Loop Validation")
        void getMessagesInBatches_TripleLoopValidation() {
            // 1. Columns
            String[] validColumns = {
                    "messageId", "title", "publishDate", "descriptionHtml", "sendAsEmail",
                    "isDeleted", "createdByUserId", "sendgridEmailBatchId", "createdAt",
                    "updatedAt", "notes", "createdUser", "modifiedUser"
            };
            String[] invalidColumns = { "invalidCol", "DROP TABLE", "unknown" };

            // 2. Operators (Service doesn't validate these strictly, but we iterate for
            // completeness)
            String[] operators = { "equals", "contains", "starts_with" };

            // 3. Values
            String[] values = { "val", "" };

            // Mock repository response to avoid NPEs when validation passes
            Page<Message> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(messageRepository.findPaginatedMessages(
                    anyLong(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            for (String column : joinArrays(validColumns, invalidColumns)) {
                for (String operator : operators) {
                    for (String value : values) {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(List.of(filter));

                        boolean isValidColumn = Arrays.asList(validColumns).contains(column);

                        if (isValidColumn) {
                            assertDoesNotThrow(() -> messageService.getMessagesInBatches(req),
                                    "Failed for valid column: " + column);
                        } else {
                            BadRequestException ex = assertThrows(BadRequestException.class,
                                    () -> messageService.getMessagesInBatches(req),
                                    "Expected BadRequest for invalid column: " + column);
                            assertTrue(ex.getMessage().contains("Invalid column name"));
                        }
                    }
                }
            }
        }
    }

    private String[] joinArrays(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays)
            length += array.length;
        String[] result = new String[length];
        int offset = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    // ==================== CREATE MESSAGE TESTS ====================

    @Nested
    @DisplayName("createMessage Tests")
    class CreateMessageTests {

        @Test
        @DisplayName("Create Message - Success - No Email")
        void createMessage_Success_NoEmail() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(new MessageUserMap());
            when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class)))
                    .thenReturn(new MessageUserGroupMap());

            assertDoesNotThrow(() -> messageService.createMessage(validRequest));

            verify(messageRepository).save(any(Message.class));
            verify(messageUserMapRepository, times(1)).save(any(MessageUserMap.class));
            verify(messageUserGroupMapRepository, times(2)).save(any(MessageUserGroupMap.class));
            verify(userLogService).logDataWithContext(
                    anyLong(), anyString(), anyLong(),
                    contains("Successfully inserted message"),
                    eq(ApiRoutes.MessagesSubRoute.CREATE_MESSAGE));
        }

        @Test
        @DisplayName("Create Message - Client Not Found - ThrowsNotFoundException")
        void createMessage_ClientNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.createMessage(validRequest);
            });

            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Create Message - Email In Past - ThrowsBadRequestException")
        void createMessage_EmailInPast_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now().minusHours(1));

            // Don't need to mock client findById as Message constructor does validation
            // first?
            // Wait, logic in service:
            // 1. fetch client
            // 2. new Message() -> check publish date

            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.createMessage(validRequest);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.ER009, exception.getMessage());
        }

        @Test
        @DisplayName("Create Message - Email Beyond 72 Hours - ThrowsBadRequestException")
        void createMessage_EmailBeyond72Hours_ThrowsBadRequestException() {
            validRequest.setSendAsEmail(true);
            validRequest.setPublishDate(LocalDateTime.now().plusHours(73));

            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.createMessage(validRequest);
            });

            assertTrue(exception.getMessage().contains("Failed to generate batch ID"));
        }
    }

    // ==================== UPDATE MESSAGE TESTS ====================

    @Nested
    @DisplayName("updateMessage Tests")
    class UpdateMessageTests {

        @Test
        @DisplayName("Update Message - Success")
        void updateMessage_Success() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(new MessageUserMap());
            when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class)))
                    .thenReturn(new MessageUserGroupMap());

            assertDoesNotThrow(() -> messageService.updateMessage(validRequest));

            verify(messageRepository).save(any(Message.class));
            verify(messageUserMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
            verify(messageUserGroupMapRepository).deleteByMessageId(TEST_MESSAGE_ID);
            verify(userLogService).logData(
                    anyLong(),
                    contains("Successfully updated message"),
                    eq(ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE));
        }

        @Test
        @DisplayName("Update Message - Null MessageId - ThrowsBadRequestException")
        void updateMessage_NullMessageId_ThrowsBadRequestException() {
            validRequest.setMessageId(null);

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.updateMessage(validRequest);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Update Message - Message Not Found - ThrowsNotFoundException")
        void updateMessage_MessageNotFound_ThrowsNotFoundException() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.updateMessage(validRequest);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Update Message - Message Already Sent - ThrowsBadRequestException")
        void updateMessage_MessageAlreadySent_ThrowsBadRequestException() {
            testMessage.setSendAsEmail(true);
            testMessage.setPublishDate(LocalDateTime.now().minusHours(1));

            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testMessage));

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                messageService.updateMessage(validRequest);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.ER011, exception.getMessage());
        }
    }

    // ==================== TOGGLE MESSAGE TESTS ====================

    @Nested
    @DisplayName("toggleMessage Tests")
    class ToggleMessageTests {

        @Test
        @DisplayName("Toggle Message - Success")
        void toggleMessage_Success() {
            testMessage.setIsDeleted(false);

            when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID),
                    eq(DEFAULT_CLIENT_ID)))
                    .thenReturn(Optional.of(testMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

            verify(messageRepository).save(argThat(msg -> msg.getIsDeleted() == true));
            verify(userLogService).logData(
                    anyLong(),
                    contains("Successfully toggled message"),
                    eq(ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE));
        }

        @Test
        @DisplayName("Toggle Message - Message Not Found - ThrowsNotFoundException")
        void toggleMessage_MessageNotFound_ThrowsNotFoundException() {
            lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID,
                    DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.toggleMessage(TEST_MESSAGE_ID);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== GET MESSAGE DETAILS BY ID TESTS ====================

    @Nested
    @DisplayName("getMessageDetailsById Tests")
    class GetMessageDetailsByIdTests {

        @Test
        @DisplayName("Get Message Details By ID - Success")
        void getMessageDetailsById_Success() {
            when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testMessage));

            MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

            assertNotNull(result);
            assertEquals(TEST_MESSAGE_ID, result.getMessageId());
            assertEquals(TEST_TITLE, result.getTitle());
        }

        @Test
        @DisplayName("Get Message Details By ID - Message Not Found - ThrowsNotFoundException")
        void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
            when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.getMessageDetailsById(TEST_MESSAGE_ID);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== GET MESSAGES BY USER ID TESTS ====================

    @Nested
    @DisplayName("getMessagesByUserId Tests")
    class GetMessagesByUserIdTests {

        @Test
        @DisplayName("Get Messages By User ID - Success")
        void getMessagesByUserId_Success() {
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setId(DEFAULT_USER_ID);
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);

            List<Message> messages = Arrays.asList(testMessage);
            Page<Message> messagePage = new PageImpl<>(messages);

            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testUser));
            when(messageRepository.findMessagesByUserIdPaginated(
                    eq(DEFAULT_CLIENT_ID), eq(DEFAULT_USER_ID), any(Pageable.class)))
                    .thenReturn(messagePage);
            when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, DEFAULT_USER_ID))
                    .thenReturn(null);

            PaginationBaseResponseModel<MessageResponseModel> result = messageService
                    .getMessagesByUserId(paginationRequest);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
            assertFalse(result.getData().get(0).getIsRead());
        }

        @Test
        @DisplayName("Get Messages By User ID - User Not Found - ThrowsNotFoundException")
        void getMessagesByUserId_UserNotFound_ThrowsNotFoundException() {
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setId(DEFAULT_USER_ID);
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);

            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.getMessagesByUserId(paginationRequest);
            });

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== SET MESSAGE READ TESTS ====================

    @Nested
    @DisplayName("setMessageReadByUserIdAndMessageId Tests")
    class SetMessageReadByUserIdAndMessageIdTests {

        @Test
        @DisplayName("Set Message Read - Success")
        void setMessageReadByUserIdAndMessageId_Success() {
            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testUser));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testMessage));
            when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, DEFAULT_USER_ID))
                    .thenReturn(null);
            when(messageUserReadMapRepository.save(any(MessageUserReadMap.class)))
                    .thenReturn(new MessageUserReadMap());

            assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(DEFAULT_USER_ID, TEST_MESSAGE_ID));

            verify(messageUserReadMapRepository).save(any(MessageUserReadMap.class));
            verify(userLogService).logData(
                    anyLong(),
                    contains("Successfully marked message as read"),
                    eq(ApiRoutes.MessagesSubRoute.SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID));
        }

        @Test
        @DisplayName("Set Message Read - Already Read - Does Not Duplicate")
        void setMessageReadByUserIdAndMessageId_AlreadyRead() {
            MessageUserReadMap existingRead = new MessageUserReadMap();

            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testUser));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testMessage));
            when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, DEFAULT_USER_ID))
                    .thenReturn(existingRead);

            assertDoesNotThrow(() -> messageService.setMessageReadByUserIdAndMessageId(DEFAULT_USER_ID, TEST_MESSAGE_ID));

            verify(messageUserReadMapRepository, never()).save(any(MessageUserReadMap.class));
        }

        @Test
        @DisplayName("Set Message Read - User Not Found - ThrowsNotFoundException")
        void setMessageReadByUserIdAndMessageId_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.setMessageReadByUserIdAndMessageId(DEFAULT_USER_ID, TEST_MESSAGE_ID);
            });

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Set Message Read - Message Not Found - ThrowsNotFoundException")
        void setMessageReadByUserIdAndMessageId_MessageNotFound_ThrowsNotFoundException() {
            when(userRepository.findByUserIdAndClientId(DEFAULT_USER_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testUser));
            when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                messageService.setMessageReadByUserIdAndMessageId(DEFAULT_USER_ID, TEST_MESSAGE_ID);
            });

            assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== GET UNREAD MESSAGE COUNT TESTS ====================

    @Nested
    @DisplayName("getUnreadMessageCount Tests")
    class GetUnreadMessageCountTests {

        @Test
        @DisplayName("Get Unread Message Count - Success")
        void getUnreadMessageCount_Success() {
            when(messageRepository.countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID))
                    .thenReturn(5L);

            int result = messageService.getUnreadMessageCount();

            assertEquals(5, result);
            verify(messageRepository).countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID);
        }

        @Test
        @DisplayName("Get Unread Message Count - No Unread Messages")
        void getUnreadMessageCount_NoUnreadMessages() {
            when(messageRepository.countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID))
                    .thenReturn(0L);

            int result = messageService.getUnreadMessageCount();

            assertEquals(0, result);
            verify(messageRepository).countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID);
        }

        @Test
        @DisplayName("Get Unread Message Count - Large Count")
        void getUnreadMessageCount_LargeCount() {
            when(messageRepository.countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID))
                    .thenReturn(1000L);

            int result = messageService.getUnreadMessageCount();

            assertEquals(1000, result);
            verify(messageRepository).countUnreadMessagesByUserId(DEFAULT_CLIENT_ID, DEFAULT_USER_ID);
        }
    }
}
