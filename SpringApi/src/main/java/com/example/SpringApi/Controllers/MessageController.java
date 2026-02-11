package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Services.Interface.IMessageSubTranslator;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Message operations.
 * 
 * This controller handles all HTTP requests related to message management
 * including creating, reading, updating, deleting, and managing message distribution.
 * All endpoints require appropriate permissions for access.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.MESSAGE)
public class MessageController {

    private static final ContextualLogger logger = ContextualLogger.getLogger(MessageController.class);
    private final IMessageSubTranslator messageService;

    @Autowired
    public MessageController(IMessageSubTranslator messageService) {
        this.messageService = messageService;
    }

    /**
     * Retrieves messages in batches with pagination support.
     * 
     * This endpoint returns a paginated list of messages based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * Requires VIEW_MESSAGES_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return ResponseEntity containing paginated message data or error
     */
    @PostMapping("/" + ApiRoutes.MessagesSubRoute.GET_MESSAGES_IN_BATCHES)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> getMessagesInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(messageService.getMessagesInBatches(paginationBaseRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new message.
     * 
     * This endpoint creates a new message with the provided details including
     * title, content, targeting information, and scheduling options.
     * Requires INSERT_MESSAGES_PERMISSION to access.
     * 
     * @param messageRequestModel The message to create
     * @return ResponseEntity containing the ID of the newly created message or error
     */
    @PutMapping("/" + ApiRoutes.MessagesSubRoute.CREATE_MESSAGE)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> createMessage(@RequestBody MessageRequestModel messageRequestModel) {
        try {
            messageService.createMessage(messageRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing message.
     * 
     * This endpoint updates message details including title, content, 
     * targeting information, and scheduling. Only draft messages can be updated.
     * Requires UPDATE_MESSAGES_PERMISSION to access.
     * 
     * @param messageRequestModel The message data to update
     * @return ResponseEntity containing the ID of the updated message or error
     */
    @PostMapping("/" + ApiRoutes.MessagesSubRoute.UPDATE_MESSAGE)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> updateMessage(@RequestBody MessageRequestModel messageRequestModel) {
        try {
            messageService.updateMessage(messageRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the status of a message (soft delete/restore).
     * 
     * This endpoint toggles the isDeleted flag of a message, effectively
     * archiving or restoring the message without permanent deletion.
     * Requires DELETE_MESSAGES_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The request containing message ID
     * @return ResponseEntity containing success status or error
     */
    @DeleteMapping("/" + ApiRoutes.MessagesSubRoute.TOGGLE_MESSAGE + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> toggleMessage(@PathVariable Long id) {
        try {
            messageService.toggleMessage(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information about a specific message by ID.
     * 
     * This endpoint returns comprehensive details about a message including
     * content, targeting information, scheduling, and distribution status.
     * Requires VIEW_MESSAGES_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The request containing message ID
     * @return ResponseEntity containing message details or error
     */
    @PostMapping("/" + ApiRoutes.MessagesSubRoute.GET_MESSAGE_DETAILS_BY_ID)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> getMessageDetailsById(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(messageService.getMessageDetailsById(paginationBaseRequestModel.getId()));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves messages for a specific user in paginated batches.
     * 
     * This endpoint returns messages that have been sent to or are visible
     * to the specified user, including both direct messages and group messages.
     * Returns unread messages first, then read messages, ordered by messageId DESC.
     * Requires VIEW_MESSAGES_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The pagination parameters (id=userId, start, end)
     * @return ResponseEntity containing paginated list of messages with read status or error
     */
    @PostMapping("/" + ApiRoutes.MessagesSubRoute.GET_MESSAGES_BY_USER_ID)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> getMessagesByUserId(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(messageService.getMessagesByUserId(paginationBaseRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Marks a message as read for a specific user.
     * 
     * This endpoint records that a user has read a message, updating the read
     * status for tracking purposes and potentially affecting message visibility.
     * Requires VIEW_MESSAGES_PERMISSION to access.
     * 
     * @param messageRequestModel The request containing userId and messageId
     * @return ResponseEntity containing success status or error
     */
    @PostMapping("/" + ApiRoutes.MessagesSubRoute.SET_MESSAGE_READ_BY_USER_ID_AND_MESSAGE_ID + "/{userId}/{messageId}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> setMessageReadByUserIdAndMessageId(@PathVariable Long userId, @PathVariable Long messageId) {
        try {
            messageService.setMessageReadByUserIdAndMessageId(userId, messageId);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Gets the count of unread messages for the current user.
     * 
     * This endpoint returns the number of unread messages for the authenticated user.
     * Messages are considered unread if they have been sent to the user (directly or
     * through user groups) and have not been marked as read.
     * Requires VIEW_MESSAGES_PERMISSION to access.
     * 
     * @return ResponseEntity containing the unread message count or error
     */
    @GetMapping("/" + ApiRoutes.MessagesSubRoute.GET_UNREAD_MESSAGE_COUNT)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_MESSAGES_PERMISSION +"')")
    public ResponseEntity<?> getUnreadMessageCount() {
        try {
            int count = messageService.getUnreadMessageCount();
            return ResponseEntity.ok(count);
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}