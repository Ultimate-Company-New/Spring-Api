package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for the Messages table.
 * 
 * This entity represents messages for user communication and notifications.
 * It includes user association and email integration capabilities.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Message`")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "messageId", nullable = false)
    private Long messageId;
    
    @Column(name = "clientId", nullable = false)
    private Long clientId;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "publishDate")
    private LocalDateTime publishDate;
    
    @Column(name = "descriptionHtml", nullable = false, columnDefinition = "TEXT")
    private String descriptionHtml;
    
    @Column(name = "sendAsEmail", nullable = false)
    private Boolean sendAsEmail = false;
    
    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "createdByUserId")
    private Long createdByUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdByUserId", referencedColumnName = "userId", insertable = false, updatable = false)
    private User createdByUser;
    
    @Column(name = "sendgridEmailBatchId", length = 255)
    private String sendgridEmailBatchId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "createdUser", nullable = false, length = 255)
    private String createdUser;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "modifiedUser", nullable = false, length = 255)
    private String modifiedUser;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private List<MessageUserMap> messageUserMaps = new ArrayList<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    private List<MessageUserGroupMap> messageUserGroupMaps = new ArrayList<>();

    public Message() {}
    
    /**
     * Constructor for creating a new message.
     * 
     * @param request The MessageRequestModel containing message data
     * @param createdByUserId The ID of the user creating this record
     * @param createdUser The login name of the user creating this record
     * @param clientId The client ID this message belongs to
     */
    public Message(MessageRequestModel request, Long createdByUserId, String createdUser, Long clientId) {
        validateRequest(request, null); // null existingMessage for create
        validateUserId(createdByUserId);

        setFieldsFromRequest(request);
        this.clientId = clientId;
        this.createdByUserId = createdByUserId;
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
    }
    
    /**
     * Constructor for updating an existing message.
     * 
     * @param request The MessageRequestModel containing updated message data
     * @param auditUserId The ID of the user modifying this record
     * @param existingMessage The existing message entity to be updated
     */
    public Message(MessageRequestModel request, Long auditUserId, Message existingMessage) {
        validateRequest(request, existingMessage); // Pass existingMessage for update validation
        validateUserId(auditUserId);
        
        // Copy existing fields
        this.messageId = existingMessage.getMessageId();
        this.createdAt = existingMessage.getCreatedAt();
        this.createdByUserId = existingMessage.getCreatedByUserId();
        
        // Update with new values
        setFieldsFromRequest(request);
    }
    
    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The MessageRequestModel to validate
     * @param existingMessage The existing message (null for create, non-null for update)
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(MessageRequestModel request, Message existingMessage) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.INVALID_ID);
        }
        
        // Validate title (required, length > 0, max 500 chars)
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER003);
        }
        if (request.getTitle().trim().length() > 500) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.TITLE_TOO_LONG);
        }
        
        // Validate description HTML (required, length > 0)
        if (request.getDescriptionHtml() == null || request.getDescriptionHtml().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER007);
        }
        
        // Validate that at least one userId or groupId is provided
        boolean hasUserIds = request.getUserIds() != null && !request.getUserIds().isEmpty();
        boolean hasGroupIds = request.getUserGroupIds() != null && !request.getUserGroupIds().isEmpty();
        
        if (!hasUserIds && !hasGroupIds) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER008);
        }
        
        // Validate publishDate and sendAsEmail relationship
        boolean requestSendAsEmail = request.getSendAsEmail() != null && request.getSendAsEmail();
        LocalDateTime requestPublishDate = request.getPublishDate();
        
        // Rule: If publishDate is not null, sendAsEmail must be true
        if (requestPublishDate != null && !requestSendAsEmail) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.PUBLISH_DATE_REQUIRES_SEND_AS_EMAIL);
        }
        
        // UPDATE PATH VALIDATIONS (only if existingMessage is provided)
        if (existingMessage != null) {
            boolean existingSendAsEmail = Boolean.TRUE.equals(existingMessage.getSendAsEmail());
            LocalDateTime existingPublishDate = existingMessage.getPublishDate();
            
            // Rule 1: If existing message has sendAsEmail=true, you cannot change it to false
            if (existingSendAsEmail && !requestSendAsEmail) {
                throw new BadRequestException(ErrorMessages.MessagesErrorMessages.CANNOT_DISABLE_SEND_AS_EMAIL_ONCE);
            }
            
            // Rule 2: If existing message has sendAsEmail=true, you cannot add/change publishDate
            if (existingSendAsEmail && existingPublishDate == null && requestPublishDate != null) {
                throw new BadRequestException(ErrorMessages.MessagesErrorMessages.CANNOT_ADD_PUBLISH_DATE_AFTER_SENT);
            }
            
            // Rule 3: If existing message has both sendAsEmail=true AND publishDate set, 
            // you cannot change/add publishDate or change sendAsEmail
            if (existingSendAsEmail && existingPublishDate != null) {
                // Cannot change publishDate
                if (requestPublishDate != null && !requestPublishDate.equals(existingPublishDate)) {
                    throw new BadRequestException(ErrorMessages.MessagesErrorMessages.CANNOT_MODIFY_SCHEDULED_PUBLISH_DATE);
                }
                // Cannot change sendAsEmail
                if (!requestSendAsEmail) {
                    throw new BadRequestException(ErrorMessages.MessagesErrorMessages.CANNOT_DISABLE_SEND_AS_EMAIL_SCHEDULED);
                }
            }
        }
        
        // Validate publishDate constraints (if being set)
        if (requestPublishDate != null) {
            validatePublishDate(requestPublishDate);
        }
    }
    
    /**
     * Validates the publish date constraints.
     * 
     * @param publishDate The publish date to validate
     * @throws BadRequestException if validation fails
     */
    private void validatePublishDate(LocalDateTime publishDate) {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        java.time.ZonedDateTime publishDateUtc = publishDate.atZone(java.time.ZoneOffset.UTC);
        
        // Cannot be in the past
        long hoursDifference = java.time.temporal.ChronoUnit.HOURS.between(now, publishDateUtc);
        if (hoursDifference < 0) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER009);
        }
        
        // Cannot be more than 72 hours in the future
        if (hoursDifference > 72) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.ER010);
        }
    }
    
    /**
     * Validates the user ID parameter.
     * 
     * @param userId The user ID to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BadRequestException(ErrorMessages.MessagesErrorMessages.INVALID_USER_ID);
        }
    }
    
    /**
     * Sets fields from the request model.
     * 
     * @param request The MessageRequestModel to extract fields from
     */
    private void setFieldsFromRequest(MessageRequestModel request) {
        this.title = request.getTitle().trim();
        this.publishDate = request.getPublishDate();
        this.descriptionHtml = request.getDescriptionHtml().trim();
        this.sendAsEmail = Boolean.TRUE.equals(request.getSendAsEmail());
        this.isDeleted = Boolean.TRUE.equals(request.getIsDeleted());
        this.sendgridEmailBatchId = request.getSendgridEmailBatchId() != null ? request.getSendgridEmailBatchId().trim() : null;
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}