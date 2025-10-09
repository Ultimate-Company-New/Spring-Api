package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.MessageRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import java.time.LocalDateTime;

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
@Table(name = "`Messages`")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "messageId", nullable = false)
    private Long messageId;
    
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
    
    @Column(name = "sendgridEmailBatchId", length = 255)
    private String sendgridEmailBatchId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "auditUserId")
    private Long auditUserId;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdByUserId", insertable = false, updatable = false)
    private User createdByUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditUserId", insertable = false, updatable = false)
    private User auditUser;
    
    public Message() {}
    
    /**
     * Constructor for creating a new message.
     * 
     * @param request The MessageRequestModel containing message data
     * @param createdByUserId The ID of the user creating this record
     */
    public Message(MessageRequestModel request, Long createdByUserId) {
        validateRequest(request);
        validateUserId(createdByUserId);
        
        setFieldsFromRequest(request);
        this.createdByUserId = createdByUserId;
        this.auditUserId = createdByUserId;  // When creating, audit user is same as created user
    }
    
    /**
     * Constructor for updating an existing message.
     * 
     * @param request The MessageRequestModel containing updated message data
     * @param auditUserId The ID of the user modifying this record
     * @param existingMessage The existing message entity to be updated
     */
    public Message(MessageRequestModel request, Long auditUserId, Message existingMessage) {
        validateRequest(request);
        validateUserId(auditUserId);
        
        // Copy existing fields
        this.messageId = existingMessage.getMessageId();
        this.createdAt = existingMessage.getCreatedAt();
        this.createdByUserId = existingMessage.getCreatedByUserId();
        
        // Update with new values
        setFieldsFromRequest(request);
        this.auditUserId = auditUserId;
    }
    
    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The MessageRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(MessageRequestModel request) {
        if (request == null) {
            throw new BadRequestException("Invalid message request");
        }
        
        // Validate title (required, length > 0, max 500 chars)
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Message title is required");
        }
        if (request.getTitle().trim().length() > 500) {
            throw new BadRequestException("Message title is too long");
        }
        
        // Validate description HTML (required, length > 0)
        if (request.getDescriptionHtml() == null || request.getDescriptionHtml().trim().isEmpty()) {
            throw new BadRequestException("Message description is required");
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
            throw new BadRequestException("Invalid user ID");
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
        this.sendAsEmail = request.getSendAsEmail() != null ? request.getSendAsEmail() : false;
        this.isDeleted = request.getIsDeleted() != null ? request.getIsDeleted() : false;
        this.sendgridEmailBatchId = request.getSendgridEmailBatchId() != null ? request.getSendgridEmailBatchId().trim() : null;
        this.notes = request.getNotes() != null ? request.getNotes().trim() : null;
    }
}