package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import java.util.List;

/**
 * Interface for Message operations and data access.
 * 
 * This interface defines the contract for all message-related business operations
 * including CRUD operations, user management, and message targeting functionality.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IMessageSubTranslator {
    
    /**
     * Retrieves messages in batches with pagination support.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return Paginated response containing message data
     */
    PaginationBaseResponseModel<MessageResponseModel> getMessagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel);
    
    /**
     * Creates a new message.
     * 
     * @param messageRequestModel The message to create
     * @throws NotFoundException if required dependencies are not found
     */
    void createMessage(MessageRequestModel messageRequestModel);
    
    /**
     * Updates an existing message.
     * 
     * @param messageRequestModel The message data to update
     * @throws NotFoundException if the message is not found
     */
    void updateMessage(MessageRequestModel messageRequestModel);
    
    /**
     * Toggles the status of a message (soft delete/restore).
     * 
     * @param id The ID of the message to toggle
     * @throws NotFoundException if the message is not found
     */
    void toggleMessage(long id);
    
    /**
     * Retrieves detailed information about a specific message by ID.
     * 
     * @param id The ID of the message to retrieve
     * @return The message details
     */
    MessageResponseModel getMessageDetailsById(long id);
    
    /**
     * Gets list of user IDs that are targeted by a specific message.
     * 
     * @param id The ID of the message
     * @return List of user IDs
     */
    List<Long> getUsersInMessages(long id);
    
    /**
     * Gets list of user group IDs that are targeted by a specific message.
     * 
     * @param id The ID of the message
     * @return List of user group IDs
     */
    List<Long> getUserGroupsInMessage(long id);
    
    /**
     * Retrieves all messages for a specific user.
     * 
     * @param id The user ID
     * @return List of messages for the user
     */
    List<MessageResponseModel> getMessagesByUserId(long id);
    
    /**
     * Marks a message as read for a specific user.
     * 
     * @param userId The ID of the user
     * @param messageId The ID of the message
     * @return True if operation was successful, false otherwise
     */
    Boolean setMessageReadByUserIdAndMessageId(long userId, long messageId);
}