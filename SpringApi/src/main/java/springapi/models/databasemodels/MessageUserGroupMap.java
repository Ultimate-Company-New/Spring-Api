package com.example.springapi.models.databasemodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the MessageUserGroupMap table. Maps messages to user groups for targeted.
 * messaging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`MessageUserGroupMap`")
public class MessageUserGroupMap {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "mappingId", nullable = false)
  private Long mappingId;

  @Column(name = "messageId", nullable = false)
  private Long messageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "messageId", insertable = false, updatable = false)
  private Message message;

  @Column(name = "groupId", nullable = false)
  private Long groupId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "groupId", insertable = false, updatable = false)
  private UserGroup userGroup;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  /** Default no-argument constructor required by JPA/Hibernate. */
  public MessageUserGroupMap() {
    // Default constructor for JPA
  }

  /**
   * Constructor for creating a new MessageUserGroupMap.
   *
   * @param messageId The message ID
   * @param groupId The group ID
   * @param createdUser The username creating this record
   * @param notes Optional notes
   */
  public MessageUserGroupMap(Long messageId, Long groupId, String createdUser, String notes) {
    this.messageId = messageId;
    this.groupId = groupId;
    this.createdUser = createdUser;
    this.modifiedUser = createdUser;
    this.notes = notes;
  }
}
