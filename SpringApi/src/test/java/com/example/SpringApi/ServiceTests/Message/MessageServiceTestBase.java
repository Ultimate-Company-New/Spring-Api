package com.example.SpringApi.ServiceTests.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Message;
import com.example.SpringApi.Models.DatabaseModels.MessageUserGroupMap;
import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.Interface.IMessageSubTranslator;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base test class for MessageService tests. Contains common mocks, dependencies, and setup logic
 * shared across all MessageService test classes.
 */
@ExtendWith(MockitoExtension.class)
abstract class MessageServiceTestBase {

  @Mock protected MessageRepository messageRepository;

  @Mock protected UserRepository userRepository;

  @Mock protected MessageUserReadMapRepository messageUserReadMapRepository;

  @Mock protected MessageUserMapRepository messageUserMapRepository;

  @Mock protected MessageUserGroupMapRepository messageUserGroupMapRepository;

  @Mock protected ClientRepository clientRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected HttpServletRequest request;

  @Mock protected Authorization authorization;

  @Mock protected Environment environment;

  @Mock IMessageSubTranslator messageServiceMock;

  @InjectMocks protected MessageService messageService;

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_MESSAGE_ID = 1L;
  protected static final Long DEFAULT_USER_ID = 1L;
  protected static final Long DEFAULT_CLIENT_ID = 100L;
  protected static final String DEFAULT_CREATED_USER = "admin";
  protected static final String DEFAULT_LOGIN_NAME = "testuser";
  protected static final String DEFAULT_EMAIL = "test@example.com";
  protected static final String DEFAULT_FIRST_NAME = "Test";
  protected static final String DEFAULT_LAST_NAME = "User";
  protected static final String DEFAULT_CLIENT_NAME = "Test Client";
  protected static final String DEFAULT_CLIENT_DESCRIPTION = "Test Client Description";
  protected static final String DEFAULT_SUPPORT_EMAIL = "support@testclient.com";
  protected static final String DEFAULT_WEBSITE = "https://testclient.com";

  protected Message testMessage;
  protected MessageRequestModel validRequest;
  protected Client testClient;
  protected User testUser;

  protected static final Long TEST_MESSAGE_ID = DEFAULT_MESSAGE_ID;
  protected static final Long TEST_CLIENT_ID = 1L;
  protected static final Long TEST_USER_ID = DEFAULT_USER_ID;
  protected static final String TEST_TITLE = "Test Message Title";
  protected static final String TEST_DESC_HTML = "<p>Test message description</p>";
  protected static final String TEST_EMAIL = "test@example.com";

  @BeforeEach
  void setUp() {
    // Initialize valid request
    validRequest = stubValidMessageRequest();
    validRequest.setMessageId(TEST_MESSAGE_ID);
    validRequest.setTitle(TEST_TITLE);
    validRequest.setDescriptionHtml(TEST_DESC_HTML);
    validRequest.setPublishDate(null);
    validRequest.setSendAsEmail(false);
    validRequest.setIsDeleted(false);
    validRequest.setUserIds(Arrays.asList(TEST_USER_ID));
    validRequest.setUserGroupIds(Arrays.asList(1L, 2L));

    // Initialize test message
    testMessage = stubTestMessage();
    testMessage.setMessageId(TEST_MESSAGE_ID);
    testMessage.setTitle(TEST_TITLE);
    testMessage.setCreatedAt(LocalDateTime.now());
    testMessage.setUpdatedAt(LocalDateTime.now());

    // Initialize test client
    testClient = stubTestClient();
    testClient.setClientId(TEST_CLIENT_ID);
    testClient.setSendGridApiKey("test-api-key");
    testClient.setSendGridEmailAddress("test@sendgrid.com");
    testClient.setSendgridSenderName("Test Sender");

    // Initialize test user
    testUser = stubTestUser();
    testUser.setUserId(TEST_USER_ID);
    testUser.setEmail(TEST_EMAIL);
    testUser.setIsDeleted(false);

    stubUserLogServiceSuccess();
    stubRequestHeaderSuccess();
    stubEnvironmentPropertySuccess();

    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addHeader("Authorization", "Bearer test-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
  }

  /** Stub for valid message request initialization. */
  protected MessageRequestModel stubValidMessageRequest() {
    return createValidMessageRequest();
  }

  /** Stub for test message initialization. */
  protected Message stubTestMessage() {
    return createTestMessage();
  }

  /** Stub for test client initialization. */
  protected Client stubTestClient() {
    return createTestClient();
  }

  /** Stub for test user initialization. */
  protected User stubTestUser() {
    return createTestUser();
  }

  /** Stub for UserLogService.logData mocking. */
  protected void stubUserLogServiceSuccess() {
    lenient()
        .when(userLogService.logData(eq(DEFAULT_USER_ID), eq("Message"), anyString()))
        .thenReturn(true);
  }

  /** Stub for HttpServletRequest header mocking. */
  protected void stubRequestHeaderSuccess() {
    lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
  }

  /** Stub for Environment property mocking. */
  protected void stubEnvironmentPropertySuccess() {
    lenient()
        .when(environment.getProperty(eq("email.service"), anyString()))
        .thenReturn("sendgrid");
  }

  /** Stub clientRepository.findById. */
  protected void stubClientRepositoryFindById(Optional<Client> client) {
    lenient().when(clientRepository.findById(anyLong())).thenReturn(client);
  }

  /** Stub messageRepository.findByMessageIdAndClientId. */
  protected void stubMessageRepositoryFindByMessageIdAndClientId(Optional<Message> message) {
    lenient()
        .when(messageRepository.findByMessageIdAndClientId(anyLong(), anyLong()))
        .thenReturn(message);
  }

  /** Stub messageRepository.findByMessageIdAndClientIdIncludingDeleted. */
  protected void stubMessageRepositoryFindByMessageIdAndClientIdIncludingDeleted(
      Optional<Message> message) {
    lenient()
        .when(messageRepository.findByMessageIdAndClientIdIncludingDeleted(anyLong(), anyLong()))
        .thenReturn(message);
  }

  /** Stub messageRepository.findByMessageIdAndClientIdWithTargets. */
  protected void stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(
      Optional<Message> message) {
    lenient()
        .when(messageRepository.findByMessageIdAndClientIdWithTargets(anyLong(), anyLong()))
        .thenReturn(message);
  }

  /** Stub messageRepository.findByMessageIdAndClientIdWithTargets to throw. */
  protected void stubMessageRepositoryFindByMessageIdAndClientIdWithTargetsThrows(String message) {
    lenient()
        .when(messageRepository.findByMessageIdAndClientIdWithTargets(anyLong(), anyLong()))
        .thenThrow(new RuntimeException(message));
  }

  /** Stub messageRepository.save to return provided message. */
  protected void stubMessageRepositorySave(Message message) {
    lenient().when(messageRepository.save(any())).thenReturn(message);
  }

  /** Stub messageRepository.findPaginatedMessages. */
  protected void stubMessageRepositoryFindPaginatedMessages(
      org.springframework.data.domain.Page<Message> page) {
    lenient()
        .when(
            messageRepository.findPaginatedMessages(
                anyLong(),
                any(),
                any(),
                any(),
                anyBoolean(),
                any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(page);
  }

  /** Stub messageRepository.findMessagesByUserIdPaginated. */
  protected void stubMessageRepositoryFindMessagesByUserIdPaginated(
      org.springframework.data.domain.Page<Message> page) {
    lenient()
        .when(
            messageRepository.findMessagesByUserIdPaginated(
                anyLong(), anyLong(), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(page);
  }

  /** Stub messageRepository.findMessagesByUserIdPaginated to throw. */
  protected void stubMessageRepositoryFindMessagesByUserIdPaginatedThrows(String message) {
    lenient()
        .when(
            messageRepository.findMessagesByUserIdPaginated(
                anyLong(), anyLong(), any(org.springframework.data.domain.Pageable.class)))
        .thenThrow(new RuntimeException(message));
  }

  /** Stub messageRepository.countUnreadMessagesByUserId. */
  protected void stubMessageRepositoryCountUnreadMessagesByUserId(Long count) {
    lenient()
        .when(messageRepository.countUnreadMessagesByUserId(anyLong(), anyLong()))
        .thenReturn(count);
  }

  /** Stub messageRepository.countUnreadMessagesByUserId to throw. */
  protected void stubMessageRepositoryCountUnreadMessagesByUserIdThrows(String message) {
    lenient()
        .when(messageRepository.countUnreadMessagesByUserId(anyLong(), anyLong()))
        .thenThrow(new RuntimeException(message));
  }

  /** Stub messageRepository.save to return passed message. */
  protected void stubMessageRepositorySaveReturnsArgument() {
    lenient()
        .when(messageRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  /** Stub userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds. */
  protected void stubUserRepositoryFindAllUserEmails(List<String> emails) {
    lenient()
        .when(
            userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(
                anyLong(), anyList(), anyList()))
        .thenReturn(emails);
  }

  /** Stub userRepository.findByUserIdAndClientId. */
  protected void stubUserRepositoryFindByUserIdAndClientId(Optional<User> user) {
    lenient().when(userRepository.findByUserIdAndClientId(anyLong(), anyLong())).thenReturn(user);
  }

  /** Stub userRepository.findByUserIdAndClientId to throw. */
  protected void stubUserRepositoryFindByUserIdAndClientIdThrows(String message) {
    lenient()
        .when(userRepository.findByUserIdAndClientId(anyLong(), anyLong()))
        .thenThrow(new RuntimeException(message));
  }

  /** Stub messageUserMapRepository.save. */
  protected void stubMessageUserMapRepositorySave(MessageUserMap map) {
    lenient().when(messageUserMapRepository.save(any(MessageUserMap.class))).thenReturn(map);
  }

  /** Stub messageUserGroupMapRepository.save. */
  protected void stubMessageUserGroupMapRepositorySave(MessageUserGroupMap map) {
    lenient()
        .when(messageUserGroupMapRepository.save(any(MessageUserGroupMap.class)))
        .thenReturn(map);
  }

  /** Stub messageUserReadMapRepository.findByMessageIdAndUserId. */
  protected void stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(MessageUserReadMap map) {
    lenient()
        .when(messageUserReadMapRepository.findByMessageIdAndUserId(anyLong(), anyLong()))
        .thenReturn(map);
  }

  /** Stub messageUserReadMapRepository.findByMessageIdAndUserId for specific IDs. */
  protected void stubMessageUserReadMapRepositoryFindByMessageIdAndUserId(
      Long messageId, Long userId, MessageUserReadMap map) {
    lenient()
        .when(messageUserReadMapRepository.findByMessageIdAndUserId(messageId, userId))
        .thenReturn(map);
  }

  /** Stub messageUserReadMapRepository.save. */
  protected void stubMessageUserReadMapRepositorySave(MessageUserReadMap map) {
    lenient()
        .when(messageUserReadMapRepository.save(any(MessageUserReadMap.class)))
        .thenReturn(map);
  }

  /** Stub messageRepository.save to throw runtime exception. */
  protected void stubMessageRepositorySaveThrowsRuntimeException(String message) {
    lenient().when(messageRepository.save(any())).thenThrow(new RuntimeException(message));
  }

  /** Stub EmailHelper.cancelEmail. */
  protected org.mockito.MockedConstruction<EmailHelper> stubEmailHelperCancelEmail() {
    return mockConstruction(
        EmailHelper.class,
        (mock, context) -> lenient().doNothing().when(mock).cancelEmail(anyString()));
  }

  /** Stub EmailHelper.generateBatchId. */
  protected org.mockito.MockedConstruction<EmailHelper> stubEmailHelperGenerateBatchId(
      String batchId) {
    return mockConstruction(
        EmailHelper.class,
        (mock, context) -> lenient().when(mock.generateBatchId()).thenReturn(batchId));
  }

  /** Stub EmailTemplates.sendEmail. */
  protected org.mockito.MockedConstruction<EmailTemplates> stubEmailTemplatesSendEmail(
      boolean result) {
    return mockConstruction(
        EmailTemplates.class,
        (mock, context) ->
            lenient()
                .when(
                    mock.sendMessageEmail(
                        anyList(),
                        anyString(),
                        anyString(),
                        any(java.time.LocalDateTime.class),
                        anyString()))
                .thenReturn(result));
  }

  /** Stub controller service updateMessage. */
  protected void stubMessageServiceUpdateMessageDoNothing() {
    lenient().doNothing().when(messageServiceMock).updateMessage(any(MessageRequestModel.class));
  }

  /** Stub controller service updateMessage to throw UnauthorizedException. */
  protected void stubMessageServiceUpdateMessageThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .updateMessage(any(MessageRequestModel.class));
  }

  /** Stub controller service createMessage. */
  protected void stubMessageServiceCreateMessageDoNothing() {
    lenient().doNothing().when(messageServiceMock).createMessage(any(MessageRequestModel.class));
  }

  /** Stub controller service createMessage to throw UnauthorizedException. */
  protected void stubMessageServiceCreateMessageThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .createMessage(any(MessageRequestModel.class));
  }

  /** Stub controller service getMessagesInBatches to throw UnauthorizedException. */
  protected void stubMessageServiceGetMessagesInBatchesThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .getMessagesInBatches(
            any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class));
  }

  /** Stub controller service getUnreadMessageCount to throw UnauthorizedException. */
  protected void stubMessageServiceGetUnreadMessageCountThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .getUnreadMessageCount();
  }

  /** Stub controller service getMessagesByUserId to throw UnauthorizedException. */
  protected void stubMessageServiceGetMessagesByUserIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .getMessagesByUserId(
            any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class));
  }

  /** Stub controller service setMessageReadByUserIdAndMessageId to throw UnauthorizedException. */
  protected void stubMessageServiceSetMessageReadByUserIdAndMessageIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .setMessageReadByUserIdAndMessageId(anyLong(), anyLong());
  }

  /** Stub controller service toggleMessage to throw UnauthorizedException. */
  protected void stubMessageServiceToggleMessageThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .toggleMessage(anyLong());
  }

  /** Stub controller service getMessageDetailsById to throw UnauthorizedException. */
  protected void stubMessageServiceGetMessageDetailsByIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(messageServiceMock)
        .getMessageDetailsById(anyLong());
  }

  /** Stub controller service toggleMessage. */
  protected void stubMessageServiceToggleMessageDoNothing() {
    lenient().doNothing().when(messageServiceMock).toggleMessage(anyLong());
  }

  /** Stub controller service getUnreadMessageCount. */
  protected void stubMessageServiceGetUnreadMessageCount(int count) {
    lenient().when(messageServiceMock.getUnreadMessageCount()).thenReturn(count);
  }

  /** Stub controller service getMessageDetailsById. */
  protected void stubMessageServiceGetMessageDetailsById(
      com.example.SpringApi.Models.ResponseModels.MessageResponseModel response) {
    lenient().when(messageServiceMock.getMessageDetailsById(anyLong())).thenReturn(response);
  }

  /** Stub controller service getMessagesInBatches. */
  protected void stubMessageServiceGetMessagesInBatches(
      com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<
              com.example.SpringApi.Models.ResponseModels.MessageResponseModel>
          response) {
    lenient()
        .when(
            messageServiceMock.getMessagesInBatches(
                any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class)))
        .thenReturn(response);
  }

  /** Stub controller service getMessagesByUserId. */
  protected void stubMessageServiceGetMessagesByUserId(
      com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<
              com.example.SpringApi.Models.ResponseModels.MessageResponseModel>
          response) {
    lenient()
        .when(
            messageServiceMock.getMessagesByUserId(
                any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class)))
        .thenReturn(response);
  }

  /** Stub controller service setMessageReadByUserIdAndMessageId. */
  protected void stubMessageServiceSetMessageReadByUserIdAndMessageIdDoNothing() {
    lenient()
        .doNothing()
        .when(messageServiceMock)
        .setMessageReadByUserIdAndMessageId(anyLong(), anyLong());
  }

  // ==================== FACTORY METHODS ====================

  protected MessageRequestModel createValidMessageRequest() {
    MessageRequestModel messageRequest = new MessageRequestModel();
    messageRequest.setMessageId(DEFAULT_MESSAGE_ID);
    messageRequest.setTitle("Test Message");
    messageRequest.setDescriptionHtml("<p>Test message content</p>");
    messageRequest.setSendAsEmail(false);
    messageRequest.setIsDeleted(false);
    messageRequest.setPublishDate(LocalDateTime.now().plusDays(1));
    return messageRequest;
  }

  protected Message createTestMessage() {
    return createTestMessage(DEFAULT_MESSAGE_ID);
  }

  protected Message createTestMessage(Long messageId) {
    Message message = new Message();
    message.setMessageId(messageId);
    message.setClientId(DEFAULT_CLIENT_ID);
    message.setTitle("Test Message");
    message.setDescriptionHtml("<p>Test message content</p>");
    message.setSendAsEmail(false);
    message.setIsDeleted(false);
    message.setPublishDate(LocalDateTime.now().plusDays(1));
    message.setCreatedUser(DEFAULT_CREATED_USER);
    message.setModifiedUser(DEFAULT_CREATED_USER);
    message.setCreatedAt(LocalDateTime.now());
    message.setUpdatedAt(LocalDateTime.now());
    return message;
  }

  protected Client createTestClient() {
    Client client = new Client();
    client.setClientId(DEFAULT_CLIENT_ID);
    client.setName(DEFAULT_CLIENT_NAME);
    client.setDescription(DEFAULT_CLIENT_DESCRIPTION);
    client.setSupportEmail(DEFAULT_SUPPORT_EMAIL);
    client.setWebsite(DEFAULT_WEBSITE);
    client.setIsDeleted(false);
    client.setCreatedUser(DEFAULT_CREATED_USER);
    client.setModifiedUser(DEFAULT_CREATED_USER);
    client.setCreatedAt(LocalDateTime.now());
    client.setUpdatedAt(LocalDateTime.now());
    return client;
  }

  protected User createTestUser() {
    User user = new User();
    user.setUserId(DEFAULT_USER_ID);
    user.setLoginName(DEFAULT_LOGIN_NAME);
    user.setFirstName(DEFAULT_FIRST_NAME);
    user.setLastName(DEFAULT_LAST_NAME);
    user.setEmail(DEFAULT_EMAIL);
    user.setIsDeleted(false);
    user.setCreatedUser(DEFAULT_CREATED_USER);
    user.setModifiedUser(DEFAULT_CREATED_USER);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return user;
  }

  protected PaginationBaseRequestModel createValidPaginationRequest() {
    PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
    paginationRequest.setStart(0);
    paginationRequest.setEnd(10);
    paginationRequest.setFilters(new java.util.ArrayList<>());
    return paginationRequest;
  }

  protected PaginationBaseRequestModel.FilterCondition createFilterCondition(
      String column, String operator, String value) {
    PaginationBaseRequestModel.FilterCondition condition =
        new PaginationBaseRequestModel.FilterCondition();
    condition.setColumn(column);
    condition.setOperator(operator);
    condition.setValue(value);
    return condition;
  }

  protected void assertThrowsBadRequest(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    BadRequestException ex = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  protected void assertThrowsNotFound(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    NotFoundException ex = assertThrows(NotFoundException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }
}
