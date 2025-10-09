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
    @Column(name = "LogId", nullable = false)
    private long logId;

    @Column(name = "`Change`", nullable = false)
    private String change;

    @Column(name = "OldValue")
    private String oldValue;

    @Column(name = "NewValue")
    private String newValue;

    // Tracking Fields
    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "AuditUserId")
    private Long auditUserId;

    // mapping Fields
    @Column(name = "UserId", nullable = false)
    private long userId;

    /**
     * Constructor for creating UserLog with essential fields.
     *
     * @param userId The ID of the user performing the action
     * @param change The type of change or action
     * @param oldValue The previous value (can be null)
     * @param newValue The new value (can be null)
     */
    public UserLog(long userId, String change, String oldValue, String newValue) {
        this.userId = userId;
        this.change = change;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Default constructor required by JPA.
     */
    public UserLog() {
    }
}
