package com.example.springapi.models.responsemodels;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for UserLog data. Contains all fields from the UserLog entity for API responses.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class UserLogsResponseModel {
  private long logId;
  private long userId;
  private Long clientId;
  private String action;
  private String description;
  private String ipAddress;
  private String userAgent;
  private String sessionId;
  private String logLevel;
  private LocalDateTime createdAt;
  private String createdUser;
  private LocalDateTime updatedAt;
  private String modifiedUser;
  private String notes;

  // Legacy fields for backward compatibility
  private Long auditUserId;
  private String change;
  private String newValue;
  private String oldValue;

  /** Default constructor. */
  public UserLogsResponseModel() {}

  /**
   * Constructor that populates all fields from a UserLog entity.
   *
   * @param userLog The UserLog entity to copy data from
   */
  public UserLogsResponseModel(com.example.springapi.models.databasemodels.UserLog userLog) {
    this.logId = userLog.getLogId();
    this.userId = userLog.getUserId();
    this.clientId = userLog.getClientId();
    this.action = userLog.getAction();
    this.description = userLog.getDescription();
    this.ipAddress = userLog.getIpAddress();
    this.userAgent = userLog.getUserAgent();
    this.sessionId = userLog.getSessionId();
    this.logLevel = userLog.getLogLevel();
    this.createdAt = userLog.getCreatedAt();
    this.createdUser = userLog.getCreatedUser();
    this.updatedAt = userLog.getUpdatedAt();
    this.modifiedUser = userLog.getModifiedUser();
    this.notes = userLog.getNotes();

    // Legacy fields
    this.auditUserId = userLog.getAuditUserId();
    this.change = userLog.getChange();
    this.newValue = userLog.getNewValue();
    this.oldValue = userLog.getOldValue();
  }
}
