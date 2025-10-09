package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response model for UserLog data.
 * Contains all fields from the UserLog entity for API responses.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class UserLogsResponseModel {
    private long logId;
    private String change;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private Long auditUserId;
    private long userId;

    /**
     * Default constructor.
     */
    public UserLogsResponseModel() {
    }

    /**
     * Constructor that populates all fields from a UserLog entity.
     *
     * @param userLog The UserLog entity to copy data from
     */
    public UserLogsResponseModel(com.example.SpringApi.Models.DatabaseModels.UserLog userLog) {
        this.logId = userLog.getLogId();
        this.change = userLog.getChange();
        this.oldValue = userLog.getOldValue();
        this.newValue = userLog.getNewValue();
        this.createdAt = userLog.getCreatedAt();
        this.updatedAt = userLog.getUpdatedAt();
        this.notes = userLog.getNotes();
        this.auditUserId = userLog.getAuditUserId();
        this.userId = userLog.getUserId();
    }
}