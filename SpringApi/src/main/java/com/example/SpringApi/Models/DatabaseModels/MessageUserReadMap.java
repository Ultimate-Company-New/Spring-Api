package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the MessageUserReadMap table. Tracks which users have read which messages.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`MessageUserReadMap`")
public class MessageUserReadMap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "readId", nullable = false)
  private Long readId;

  @Column(name = "messageId", nullable = false)
  private Long messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "messageId", insertable = false, updatable = false)
  private Message message;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", insertable = false, updatable = false)
  private User user;

  @Column(name = "readAt", nullable = false)
  private LocalDateTime readAt;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  /** Default no-argument constructor required by JPA/Hibernate. */
  public MessageUserReadMap() {
    // Default constructor for JPA
  }

  /**
   * Constructor for creating a new MessageUserReadMap record.
   *
   * @param messageId The message ID
   * @param userId The user ID
   * @param createdUser The username creating this record
   */
  public MessageUserReadMap(Long messageId, Long userId, String createdUser) {
    this.messageId = messageId;
    this.userId = userId;
    this.readAt = LocalDateTime.now();
    this.createdUser = createdUser;
    this.modifiedUser = createdUser;
  }
}

