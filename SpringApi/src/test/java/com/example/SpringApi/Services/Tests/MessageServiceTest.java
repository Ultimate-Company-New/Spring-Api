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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
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
class MessageServiceTest {

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

    @Spy
    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private MessageRequestModel validRequest;
    private Client testClient;
    private User testUser;
    
    private static final Long TEST_MESSAGE_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_TITLE = "Test Message Title";
    private static final String TEST_DESC_HTML = "<p>Test message description</p>";
    private static final String CREATED_USER = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    
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
        validRequest.setUserIds(Arrays.asList(TEST_USER_ID));
        validRequest.setUserGroupIds(Arrays.asList(1L, 2L));

        // Initialize test message
        testMessage = new Message(validRequest, TEST_USER_ID, CREATED_USER, TEST_CLIENT_ID);
        testMessage.setMessageId(TEST_MESSAGE_ID);
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());

        // Initialize test client
        testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setSendGridApiKey("test-api-key");
        testClient.setSendGridEmailAddress("test@sendgrid.com");
        testClient.setSendgridSenderName("Test Sender");

        // Initialize test user
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setIsDeleted(false);
    }

    // ==================== GET MESSAGES IN BATCHES TESTS ====================

    @Test
    @DisplayName("Should successfully retrieve messages in batches")
    void testGetMessagesInBatches_Success() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("title");
        paginationRequest.setCondition("contains");
        paginationRequest.setFilterExpr("Test");
        paginationRequest.setIncludeDeleted(false);

        List<Message> messages = Arrays.asList(testMessage);
        Page<Message> messagePage = new PageImpl<>(messages);

        when(messageRepository.findPaginatedMessages(
            eq(TEST_CLIENT_ID), anyString(), anyString(), anyString(), anyBoolean(), any(Pageable.class)))
            .thenReturn(messagePage);

        PaginationBaseResponseModel<MessageResponseModel> result = 
            messageService.getMessagesInBatches(paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid column name")
    void testGetMessagesInBatches_InvalidColumnName() {
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("invalidColumn");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.getMessagesInBatches(paginationRequest);
        });

        assertTrue(exception.getMessage().contains("Invalid column name"));
    }

    @Test
    @DisplayName("Should throw BadRequestException for invalid pagination (end <= start)")
    void testGetMessagesInBatches_InvalidPagination() {
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(10);
        paginationRequest.setEnd(5);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.getMessagesInBatches(paginationRequest);
        });

        assertEquals("Invalid pagination: end must be greater than start", exception.getMessage());
    }

    // ==================== CREATE MESSAGE TESTS ====================

    @Test
    @DisplayName("Should successfully create message without email")
    void testCreateMessage_Success_NoEmail() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        lenient().when(messageService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(messageService.getUser()).thenReturn(CREATED_USER);

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
    @DisplayName("Should throw NotFoundException when client not found")
    void testCreateMessage_ClientNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.createMessage(validRequest);
        });

        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when scheduling email in the past")
    void testCreateMessage_EmailInPast() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        lenient().when(messageService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(messageService.getUser()).thenReturn(CREATED_USER);

        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().minusHours(1));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.createMessage(validRequest);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.ER009, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when scheduling email beyond 72 hours")
    void testCreateMessage_EmailBeyond72Hours() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        lenient().when(messageService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(messageService.getUser()).thenReturn(CREATED_USER);

        validRequest.setSendAsEmail(true);
        validRequest.setPublishDate(LocalDateTime.now().plusHours(73));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.createMessage(validRequest);
        });

        assertTrue(exception.getMessage().contains("Failed to generate batch ID"));
    }

    // ==================== UPDATE MESSAGE TESTS ====================

    @Test
    @DisplayName("Should successfully update message")
    void testUpdateMessage_Success() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        lenient().when(messageService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(messageService.getUser()).thenReturn(CREATED_USER);

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
    @DisplayName("Should throw BadRequestException when messageId is null")
    void testUpdateMessage_NullMessageId() {
        validRequest.setMessageId(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.updateMessage(validRequest);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NotFoundException when message not found")
    void testUpdateMessage_MessageNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.updateMessage(validRequest);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to edit sent message")
    void testUpdateMessage_MessageAlreadySent() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        testMessage.setSendAsEmail(true);
        testMessage.setPublishDate(LocalDateTime.now().minusHours(1));

        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(messageRepository.findByMessageIdAndClientId(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            messageService.updateMessage(validRequest);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.ER011, exception.getMessage());
    }

    // ==================== TOGGLE MESSAGE TESTS ====================

    @Test
    @DisplayName("Should successfully toggle message")
    void testToggleMessage_Success() {
        when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(messageService.getUserId()).thenReturn(TEST_USER_ID);

        testMessage.setIsDeleted(false);

        when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(eq(TEST_MESSAGE_ID), eq(TEST_CLIENT_ID)))
            .thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> messageService.toggleMessage(TEST_MESSAGE_ID));

        verify(messageRepository).save(argThat(msg -> msg.getIsDeleted() == true));
        verify(userLogService).logData(
            eq(TEST_USER_ID),
            contains("Successfully toggled message"),
            eq(ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE)
        );
    }

    @Test
    @DisplayName("Should throw NotFoundException when toggling non-existent message")
    void testToggleMessage_MessageNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        lenient().when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.toggleMessage(TEST_MESSAGE_ID);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== GET MESSAGE DETAILS BY ID TESTS ====================

    @Test
    @DisplayName("Should successfully get message details by ID")
    void testGetMessageDetailsById_Success() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getMessageId());
        assertEquals(TEST_TITLE, result.getTitle());
    }

    @Test
    @DisplayName("Should throw NotFoundException when getting details of non-existent message")
    void testGetMessageDetailsById_MessageNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.getMessageDetailsById(TEST_MESSAGE_ID);
        });

        assertEquals(ErrorMessages.MessagesErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== GET MESSAGES BY USER ID TESTS ====================

    @Test
    @DisplayName("Should successfully get messages by user ID")
    void testGetMessagesByUserId_Success() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

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
    @DisplayName("Should throw NotFoundException when user not found")
    void testGetMessagesByUserId_UserNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.getMessagesByUserId(paginationRequest);
        });

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== SET MESSAGE READ TESTS ====================

    @Test
    @DisplayName("Should successfully mark message as read")
    void testSetMessageReadByUserIdAndMessageId_Success() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);
        lenient().when(messageService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(messageService.getUser()).thenReturn(CREATED_USER);

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
    @DisplayName("Should not create duplicate read record when already marked as read")
    void testSetMessageReadByUserIdAndMessageId_AlreadyRead() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

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
    @DisplayName("Should throw NotFoundException when user not found for setting read status")
    void testSetMessageReadByUserIdAndMessageId_UserNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            messageService.setMessageReadByUserIdAndMessageId(TEST_USER_ID, TEST_MESSAGE_ID);
        });

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NotFoundException when message not found for setting read status")
    void testSetMessageReadByUserIdAndMessageId_MessageNotFound() {
        lenient().when(messageService.getClientId()).thenReturn(TEST_CLIENT_ID);

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

