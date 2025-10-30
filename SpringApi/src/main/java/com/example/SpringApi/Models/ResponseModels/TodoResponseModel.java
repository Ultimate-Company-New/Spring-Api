package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Todo;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Response model for Todo operations.
 * 
 * This model contains all the fields returned when retrieving todo information.
 * It includes related entities and calculated fields for the UI.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class TodoResponseModel {
    
    private Long todoId;
    private String task;
    private Boolean isDone;
    private Long userId;
    private LocalDateTime createdAt;
    private String createdUser;
    private LocalDateTime updatedAt;
    private String modifiedUser;
    private String notes;
    
    // Additional computed fields
    private String taskPreview;
    private String statusText;
    private Integer daysOld;
    
    /**
     * Constructor to create response model from database entity.
     * 
     * @param todo The Todo entity
     */
    public TodoResponseModel(Todo todo) {
        if (todo != null) {
            this.todoId = todo.getTodoId();
            this.task = todo.getTask();
            this.isDone = todo.getIsDone();
            this.userId = todo.getUserId();
            this.createdAt = todo.getCreatedAt();
            this.createdUser = todo.getCreatedUser();
            this.updatedAt = todo.getUpdatedAt();
            this.modifiedUser = todo.getModifiedUser();
            this.notes = todo.getNotes();
                        
            // Compute additional fields
            this.taskPreview = buildTaskPreview();
            this.statusText = this.isDone ? "Completed" : "Pending";
            this.daysOld = calculateDaysOld();
        }
    }
    
    /**
     * Builds a preview of the task (truncated if too long).
     * 
     * @return Task preview string
     */
    private String buildTaskPreview() {
        if (this.task != null && this.task.length() > 50) {
            return this.task.substring(0, 47) + "...";
        }
        return this.task;
    }
    
    /**
     * Calculates the number of days since the todo was created.
     * 
     * @return Number of days old
     */
    private Integer calculateDaysOld() {
        if (this.createdAt != null) {
            LocalDateTime now = LocalDateTime.now();
            return (int) java.time.Duration.between(this.createdAt, now).toDays();
        }
        return 0;
    }
}