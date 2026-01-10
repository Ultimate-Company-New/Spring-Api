package com.example.SpringApi.Services;

import com.example.SpringApi.Services.Interface.IMessageSubTranslator;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Message;
import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import com.example.SpringApi.Models.DatabaseModels.MessageUserMap;
import com.example.SpringApi.Models.DatabaseModels.MessageUserGroupMap;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Helpers.EmailHelper;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.MessageRepository;
import com.example.SpringApi.Repositories.MessageUserReadMapRepository;
import com.example.SpringApi.Repositories.MessageUserMapRepository;
import com.example.SpringApi.Repositories.MessageUserGroupMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Service implementation for Message operations.
 * 
 * This service handles all business logic related to message management
 * including CRUD operations, user targeting, and message distribution.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class MessageService extends BaseService implements IMessageSubTranslator {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageUserReadMapRepository messageUserReadMapRepository;
    private final MessageUserMapRepository messageUserMapRepository;
    private final MessageUserGroupMapRepository messageUserGroupMapRepository;
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    private final Environment environment;

    @Autowired
    public MessageService(MessageRepository messageRepository,
                         UserRepository userRepository,
                         MessageUserReadMapRepository messageUserReadMapRepository,
                         MessageUserMapRepository messageUserMapRepository,
                         MessageUserGroupMapRepository messageUserGroupMapRepository,
                         ClientRepository clientRepository,
                         UserLogService userLogService,
                         Environment environment) {
        super();
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageUserReadMapRepository = messageUserReadMapRepository;
        this.messageUserMapRepository = messageUserMapRepository;
        this.messageUserGroupMapRepository = messageUserGroupMapRepository;
        this.clientRepository = clientRepository;
        this.userLogService = userLogService;
        this.environment = environment;
    }

    /**
     * Retrieves messages in paginated batches with optional filtering and sorting.
     * Supports pagination, sorting by multiple fields, and filtering capabilities.
     * Uses JPQL with LEFT JOIN FETCH to eagerly load MessageUserMap and MessageUserGroupMap collections.
     *
     * @param paginationBaseRequestModel The request model containing pagination and filter parameters
     * @return PaginationBaseResponseModel containing paginated message data with targeting information
     */
    @Override
    public PaginationBaseResponseModel<MessageResponseModel> getMessagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Valid columns for filtering
        Set<String> validColumns = Set.of(
            "messageId", "title", "publishDate", "descriptionHtml", "sendAsEmail",
            "isDeleted", "createdByUserId", "sendgridEmailBatchId", "createdAt",
            "updatedAt", "notes", "createdUser", "modifiedUser"
        );

        // Validate filters if provided
        if (paginationBaseRequestModel.getFilters() != null) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                if (filter == null) {
                    continue;
                }
                String column = filter.getColumn();
                if (column == null || !validColumns.contains(column)) {
                    throw new BadRequestException("Invalid column name: " + column);
                }
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("messageId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Execute paginated query with LEFT JOIN FETCH (without filtering for now)
        Page<Message> page = messageRepository.findPaginatedMessages(
            getClientId(),
            null,
            null,
            null,
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Convert Message entities to MessageResponseModel
        List<MessageResponseModel> messageResponseModels = page.getContent().stream()
            .map(MessageResponseModel::new)
            .toList();

        PaginationBaseResponseModel<MessageResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(messageResponseModels);
        response.setTotalDataCount(page.getTotalElements());

        return response;
    }
    
    /**
     * Creates a new message with user and group targeting.
     * This operation is transactional to ensure all related records are created atomically.
     * 
     * @param messageRequestModel The message data including targeting information
     * @throws BadRequestException if validation fails
     */
    @Override
    @Transactional
    public void createMessage(MessageRequestModel messageRequestModel) {
        createMessageWithContext(messageRequestModel, getUserId(), getUser(), getClientId());
    }

    /**
     * Creates a message with explicit context values (for async operations).
     * This variant is used when the security context is not available (e.g., async methods).
     *
     * @param messageRequestModel The message request model
     * @param requestingUserId The ID of the user creating the message
     * @param requestingUserLoginName The loginName of the user creating the message
     * @param requestingClientId The client ID
     */
    public void createMessageWithContext(MessageRequestModel messageRequestModel, Long requestingUserId, String requestingUserLoginName, Long requestingClientId) {
        // Fetch client configuration
        Client client = clientRepository.findById(requestingClientId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
        
        // Use the Message constructor that handles validation and field mapping
        Message message = new Message(messageRequestModel, requestingUserId, requestingUserLoginName, requestingClientId);
        
        // Generate batch ID if sendAsEmail is true and publishDate is set
        if (Boolean.TRUE.equals(message.getSendAsEmail()) && message.getPublishDate() != null) {
            EmailHelper emailHelper = new EmailHelper(
                client.getSendGridEmailAddress(),
                client.getSendgridSenderName(),
                client.getSendGridApiKey()
            );
            String batchId = emailHelper.generateBatchId();
            message.setSendgridEmailBatchId(batchId);
        }
        
        // Save the message
        Message savedMessage = messageRepository.save(message);
        
        // Create MessageUserMap entries for targeted users
        if (messageRequestModel.getUserIds() != null && !messageRequestModel.getUserIds().isEmpty()) {
            for (Long userId : messageRequestModel.getUserIds()) {
                MessageUserMap userMap = new MessageUserMap(
                    savedMessage.getMessageId(),
                    userId,
                    "TO", // Default to TO
                    requestingUserLoginName,
                    messageRequestModel.getNotes()
                );
                messageUserMapRepository.save(userMap);
            }
        }
        
        // Create MessageUserGroupMap entries for targeted groups
        if (messageRequestModel.getUserGroupIds() != null && !messageRequestModel.getUserGroupIds().isEmpty()) {
            for (Long groupId : messageRequestModel.getUserGroupIds()) {
                MessageUserGroupMap groupMap = new MessageUserGroupMap(
                    savedMessage.getMessageId(),
                    groupId,
                    requestingUserLoginName,
                    messageRequestModel.getNotes()
                );
                messageUserGroupMapRepository.save(groupMap);
            }
        }
        
        // Schedule or send email if sendAsEmail is true
        if (Boolean.TRUE.equals(savedMessage.getSendAsEmail())) {
            // Fetch all recipient emails in a single query
            List<Long> userIds = messageRequestModel.getUserIds() != null ? messageRequestModel.getUserIds() : Collections.emptyList();
            List<Long> groupIds = messageRequestModel.getUserGroupIds() != null ? messageRequestModel.getUserGroupIds() : Collections.emptyList();
            List<String> recipientEmails = userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(
                requestingClientId,
                userIds,
                groupIds
            );
            
            if (!recipientEmails.isEmpty()) {
                // Use professional email template with logo and footer
                EmailTemplates emailTemplates = new EmailTemplates(
                    client.getSendgridSenderName(),
                    client.getSendGridEmailAddress(),
                    client.getSendGridApiKey(),
                    environment,
                    client
                );
                
                // Send/schedule the email with professional template
                emailTemplates.sendMessageEmail(
                    recipientEmails,
                    savedMessage.getTitle(),
                    savedMessage.getDescriptionHtml(),
                    savedMessage.getPublishDate(),
                    savedMessage.getSendgridEmailBatchId()
                );
            }
        }
        
        // Logging
        userLogService.logDataWithContext(
            requestingUserId,
            requestingUserLoginName,
            requestingClientId,
            SuccessMessages.MessagesSuccessMessages.InsertMessage + " " + savedMessage.getMessageId(),
            ApiRoutes.MessagesSubRoute.CREATE_MESSAGE
        );
    }
    
    @Override
    @Transactional
    public void updateMessage(MessageRequestModel messageRequestModel) {
        // Validate messageId is provided
        if (messageRequestModel.getMessageId() == null) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.InvalidId);
        }
        
        // Fetch client configuration
        Client client = clientRepository.findById(getClientId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
        
        // Fetch existing message and validate it belongs to current client
        Message existingMessage = messageRepository.findByMessageIdAndClientId(
            messageRequestModel.getMessageId(), 
            getClientId()
        ).orElseThrow(() -> new NotFoundException(ErrorMessages.MessagesErrorMessages.InvalidId));
        
        // Check if message can be edited (if scheduled email has already been sent, cannot edit)
        if (Boolean.TRUE.equals(existingMessage.getSendAsEmail()) 
            && existingMessage.getPublishDate() != null) {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime publishDateUtc = existingMessage.getPublishDate().atZone(ZoneOffset.UTC);
            
            // Only prevent editing if the scheduled time has passed
            if (now.isAfter(publishDateUtc)) {
                throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER011);
            }
        }
        
        // Cancel old scheduled email if it exists and settings are changing
        if (existingMessage.getSendgridEmailBatchId() != null) {
            // Check if email scheduling is being disabled or if we need to reschedule
            boolean emailSettingsChanged = !Boolean.TRUE.equals(messageRequestModel.getSendAsEmail())
                || (messageRequestModel.getPublishDate() != null
                    && !messageRequestModel.getPublishDate().equals(existingMessage.getPublishDate()));

            if (emailSettingsChanged) {
                EmailHelper emailHelper = new EmailHelper(
                    client.getSendGridEmailAddress(),
                    client.getSendgridSenderName(),
                    client.getSendGridApiKey()
                );
                emailHelper.cancelEmail(existingMessage.getSendgridEmailBatchId());
            }
        }
        
        // Create updated message using constructor (validation happens in constructor)
        Message updatedMessage = new Message(messageRequestModel, getUserId(), existingMessage);
        updatedMessage.setClientId(getClientId());
        updatedMessage.setModifiedUser(getUser());
        updatedMessage.setCreatedUser(existingMessage.getCreatedUser());
        
        // Generate new batch ID if sendAsEmail is true and publishDate is set
        if (Boolean.TRUE.equals(updatedMessage.getSendAsEmail()) && updatedMessage.getPublishDate() != null) {
            EmailHelper emailHelper = new EmailHelper(
                client.getSendGridEmailAddress(),
                client.getSendgridSenderName(),
                client.getSendGridApiKey()
            );
            String batchId = emailHelper.generateBatchId();
            updatedMessage.setSendgridEmailBatchId(batchId);
        } else {
            updatedMessage.setSendgridEmailBatchId(null);
        }
        
        // Save the updated message
        Message savedMessage = messageRepository.save(updatedMessage);
        
        // Delete old user and group mappings
        messageUserMapRepository.deleteByMessageId(savedMessage.getMessageId());
        messageUserGroupMapRepository.deleteByMessageId(savedMessage.getMessageId());
        
        // Flush to ensure deletes are executed before inserts
        messageUserMapRepository.flush();
        messageUserGroupMapRepository.flush();
        
        // Create new MessageUserMap entries for targeted users
        if (messageRequestModel.getUserIds() != null && !messageRequestModel.getUserIds().isEmpty()) {
            for (Long userId : messageRequestModel.getUserIds()) {
                MessageUserMap userMap = new MessageUserMap(
                    savedMessage.getMessageId(),
                    userId,
                    "TO", // Default to TO
                    getUser(),
                    messageRequestModel.getNotes()
                );
                messageUserMapRepository.save(userMap);
            }
        }
        
        // Create new MessageUserGroupMap entries for targeted groups
        if (messageRequestModel.getUserGroupIds() != null && !messageRequestModel.getUserGroupIds().isEmpty()) {
            for (Long groupId : messageRequestModel.getUserGroupIds()) {
                MessageUserGroupMap groupMap = new MessageUserGroupMap(
                    savedMessage.getMessageId(),
                    groupId,
                    getUser(),
                    messageRequestModel.getNotes()
                );
                messageUserGroupMapRepository.save(groupMap);
            }
        }
        
        // Schedule or send email if sendAsEmail is true
        if (Boolean.TRUE.equals(savedMessage.getSendAsEmail())) {
            // Fetch all recipient emails in a single query
            List<Long> userIds = messageRequestModel.getUserIds() != null ? messageRequestModel.getUserIds() : Collections.emptyList();
            List<Long> groupIds = messageRequestModel.getUserGroupIds() != null ? messageRequestModel.getUserGroupIds() : Collections.emptyList();
            List<String> recipientEmails = userRepository.findAllUserEmailsByClientAndUserIdsAndGroupIds(
                getClientId(),
                userIds,
                groupIds
            );
            
            if (!recipientEmails.isEmpty()) {
                // Use professional email template with logo and footer
                EmailTemplates emailTemplates = new EmailTemplates(
                    client.getSendgridSenderName(),
                    client.getSendGridEmailAddress(),
                    client.getSendGridApiKey(),
                    environment,
                    client
                );
                
                // Send/schedule the email with professional template
                emailTemplates.sendMessageEmail(
                    recipientEmails,
                    savedMessage.getTitle(),
                    savedMessage.getDescriptionHtml(),
                    savedMessage.getPublishDate(),
                    savedMessage.getSendgridEmailBatchId()
                );
            }
        }
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.MessagesSuccessMessages.UpdateMessage + " " + savedMessage.getMessageId(),
            ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE);
    }
    
    /**
     * Toggles the soft delete status of a message.
     * If the message is deleted, it will be restored. If not deleted, it will be marked as deleted.
     * 
     * @param id The message ID
     * @throws NotFoundException if the message doesn't exist or doesn't belong to the current client
     */
    @Override
    public void toggleMessage(long id) {
        // Validate message exists and belongs to current client (including deleted messages)
        Optional<Message> messageOptional = messageRepository.findByMessageIdAndClientIdIncludingDeleted(id, getClientId());
        if (messageOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.MessagesErrorMessages.InvalidId);
        }
        
        Message message = messageOptional.get();
        
        // Toggle the isDeleted flag
        message.setIsDeleted(!message.getIsDeleted());
        
        // Save the updated message
        messageRepository.save(message);
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.MessagesSuccessMessages.ToggleMessage + message.getMessageId(),
            ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE);
    }
    
    /**
     * Retrieves detailed information about a specific message by ID.
     * Includes targeting information (userIds and groupIds).
     * 
     * @param id The message ID
     * @return MessageResponseModel containing message details with targeting info
     * @throws NotFoundException if the message doesn't exist or doesn't belong to the current client
     */
    @Override
    public MessageResponseModel getMessageDetailsById(long id) {
        // Validate message exists and belongs to current client, fetch with targets
        Optional<Message> messageOptional = messageRepository.findByMessageIdAndClientIdWithTargets(id, getClientId());
        if (messageOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.MessagesErrorMessages.InvalidId);
        }
        
        // Return message response model with targeting info
        return new MessageResponseModel(messageOptional.get());
    }
    
    /**
     * Retrieves messages targeted to a specific user in paginated batches.
     * Includes messages where the user is directly targeted or belongs to a targeted group.
     * Only returns non-deleted messages.
     * Orders unread messages first, then read messages, both ordered by messageId DESC.
     * 
     * @param paginationBaseRequestModel The pagination parameters (only start/end are used)
     * @return PaginationBaseResponseModel containing paginated message data with read status
     * @throws NotFoundException if the user doesn't exist or doesn't belong to the current client
     */
    @Override
    public PaginationBaseResponseModel<MessageResponseModel> getMessagesByUserId(PaginationBaseRequestModel paginationBaseRequestModel) {
        long userId = paginationBaseRequestModel.getId();
        
        // Validate user exists and belongs to current client
        Optional<User> userOptional = userRepository.findByUserIdAndClientId(userId, getClientId());
        if (userOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
        }
        
        // Default page size
        int defaultPageSize = 10;
        
        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = (end - start > 0) ? (end - start) : defaultPageSize;
        
        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.unsorted()) {
            @Override
            public long getOffset() {
                return start;
            }
        };
        
        // Fetch paginated messages for the user
        Page<Message> page = messageRepository.findMessagesByUserIdPaginated(getClientId(), userId, pageable);
        
        // Convert to response models with read status
        List<MessageResponseModel> messageResponseModels = page.getContent().stream()
            .map(message -> {
                MessageResponseModel model = new MessageResponseModel(message);
                // Set read status by checking if record exists in MessageUserReadMap
                model.setIsRead(messageUserReadMapRepository.findByMessageIdAndUserId(message.getMessageId(), userId) != null);
                return model;
            })
            .toList();
        
        PaginationBaseResponseModel<MessageResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(messageResponseModels);
        response.setTotalDataCount(page.getTotalElements());
        
        return response;
    }
    
    /**
     * Marks a message as read for a specific user.
     * Creates a record in MessageUserReadMap to track when the user read the message.
     * 
     * @param userId The ID of the user who read the message
     * @param messageId The ID of the message that was read
     * @throws NotFoundException if the user or message doesn't exist or doesn't belong to the current client
     */
    @Override
    public void setMessageReadByUserIdAndMessageId(long userId, long messageId) {
        // 1. Validate user exists and belongs to current client
        Optional<User> userOptional = userRepository.findByUserIdAndClientId(userId, getClientId());
        if (userOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
        }
        
        // 2. Validate message exists and belongs to current client
        Optional<Message> messageOptional = messageRepository.findByMessageIdAndClientId(messageId, getClientId());
        if (messageOptional.isEmpty()) {
            throw new NotFoundException(ErrorMessages.MessagesErrorMessages.InvalidId);
        }
        
        // 3. Check if already marked as read
        MessageUserReadMap existingRead = messageUserReadMapRepository.findByMessageIdAndUserId(messageId, userId);
        if (existingRead != null) {
            // Already marked as read, return early
            return;
        }
        
        // 4. Create new read record
        MessageUserReadMap readRecord = new MessageUserReadMap(messageId, userId, getUser());
        messageUserReadMapRepository.save(readRecord);
        
        // Logging
        userLogService.logData(
            getUserId(),
            SuccessMessages.MessagesSuccessMessages.SetMessageRead + " MessageId: " + messageId + ", UserId: " + userId,
            ApiRoutes.MessagesSubRoute.SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID);
    }

    /**
     * Gets the count of unread messages for the current user.
     * 
     * This method counts all messages that:
     * - Belong to the current client
     * - Are not deleted
     * - Are targeted to the current user (directly or through user groups)
     * - Have not been marked as read by the current user
     * 
     * @return The number of unread messages
     */
    @Override
    public int getUnreadMessageCount() {
        long userId = getUserId();
        long clientId = getClientId();
        
        long count = messageRepository.countUnreadMessagesByUserId(clientId, userId);
        
        return (int) count;
    }
}