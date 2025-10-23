package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "UserLog")
public class UserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logId", nullable = false)
    private long logId;

    @Column(name = "userId", nullable = false)
    private long userId;

    @Column(name = "clientId")
    private Long clientId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "description")
    private String description;

    @Column(name = "ipAddress", length = 45)
    private String ipAddress;

    @Column(name = "userAgent")
    private String userAgent;

    @Column(name = "sessionId")
    private String sessionId;

    @Column(name = "logLevel", nullable = false, length = 20)
    private String logLevel = "INFO";

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "createdUser", nullable = false)
    private String createdUser;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "modifiedUser", nullable = false)
    private String modifiedUser;

    @Column(name = "notes")
    private String notes;

    // Legacy fields for backward compatibility
    @Column(name = "AuditUserId")
    private Long auditUserId;

    @Column(name = "`Change`", nullable = false)
    private String change;

    @Column(name = "NewValue")
    private String newValue;

    @Column(name = "OldValue")
    private String oldValue;

    /**
     * Constructor for creating UserLog with new schema fields.
     *
     * @param userId The ID of the user performing the action
     * @param clientId The client ID context
     * @param action The action being logged
     * @param description The description of the action (can be null)
     * @param createdUser The user creating the log entry
     */
    public UserLog(long userId, Long clientId, String action, String description, String newValue, String createdUser) {
        this.userId = userId;
        this.clientId = clientId;
        this.action = action;
        this.description = description;
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
        this.change = action;
        this.newValue = newValue;
    }

    public UserLog(long userId, Long clientId, String action, String description, String newValue, String oldValue, String createdUser) {
        this.userId = userId;
        this.clientId = clientId;
        this.action = action;
        this.description = description;
        this.createdUser = createdUser;
        this.modifiedUser = createdUser;
        this.change = action;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    /**
     * Default constructor required by JPA.
     */
    public UserLog() {
    }
}
