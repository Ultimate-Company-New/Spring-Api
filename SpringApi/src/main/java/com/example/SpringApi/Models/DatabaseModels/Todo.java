package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.example.SpringApi.Models.RequestModels.TodoRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import java.time.LocalDateTime;

/**
 * JPA Entity for the Todo table.
 * 
 * This entity represents todo items for task management and personal productivity.
 * It includes user association and completion tracking.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Todo`")
public class Todo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todoId", nullable = false)
    private Long todoId;
    
    @Column(name = "task", nullable = false, length = 500)
    private String task;
    
    @Column(name = "isDone", nullable = false)
    private Boolean isDone = false;
    
    @Column(name = "userId", nullable = false)
    private Long userId;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "createdUser", nullable = false, length = 255)
    private String createdUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUser", referencedColumnName = "loginName", insertable = false, updatable = false)
    private User createdByUser;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "modifiedUser", nullable = false, length = 255)
    private String modifiedUser;  
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
    
    public Todo() {}
    
    /**
     * Constructor for creating a new todo item.
     * 
     * @param request The TodoRequestModel containing todo data
     * @param createdUser The username of the user creating this record
     */
    public Todo(TodoRequestModel request, String createdUser, long userId) {
        validateRequest(request);
        validateUser(createdUser);
        
        setFieldsFromRequest(request, userId);
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;  // When creating, modified user is same as created user
        this.notes = "Created Via SpringApi";
    }
    
    /**
     * Constructor for updating an existing todo item.
     * 
     * @param request The TodoRequestModel containing updated todo data
     * @param modifiedUser The username of the user modifying this record
     * @param existingTodo The existing todo entity to be updated
     */
    public Todo(TodoRequestModel request, String modifiedUser, Todo existingTodo, long userId) {
        validateRequest(request);
        validateUser(modifiedUser);
        
        // Copy existing fields
        this.todoId = existingTodo.getTodoId();
        this.createdAt = existingTodo.getCreatedAt();
        this.createdUser = existingTodo.getCreatedUser();
        this.notes = "Updated via SpringApi";
        
        // Update with new values
        setFieldsFromRequest(request, userId);
        this.modifiedUser = modifiedUser;
    }
    
    /**
     * Validates the request model for required fields and constraints.
     * 
     * @param request The TodoRequestModel to validate
     * @throws BadRequestException if validation fails
     */
    private void validateRequest(TodoRequestModel request) {
        if (request == null) {
            throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_REQUEST);
        }
        
        // Validate task (required, length > 0, max 500 chars)
        if (request.getTask() == null || request.getTask().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.TodoErrorMessages.INVALID_TASK);
        }
        if (request.getTask().trim().length() > 500) {
            throw new BadRequestException(ErrorMessages.TodoErrorMessages.TASK_TOO_LONG);
        }
    }
    
    /**
     * Validates the user parameter for audit fields.
     * 
     * @param user The username to validate
     * @throws BadRequestException if validation fails
     */
    private void validateUser(String user) {
        if (user == null || user.trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
        }
    }
    
    /**
     * Sets fields from the request model.
     * 
     * @param request The TodoRequestModel to extract fields from
     */
    private void setFieldsFromRequest(TodoRequestModel request, long userId) {
        this.task = request.getTask().trim();
        this.isDone = Boolean.TRUE.equals(request.getIsDone());
        this.userId = userId;
    }
}
