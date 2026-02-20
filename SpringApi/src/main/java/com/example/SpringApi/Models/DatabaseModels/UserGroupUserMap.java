package com.example.SpringApi.Models.DatabaseModels;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "UserGroupUsersMap")
public class UserGroupUserMap {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "mappingId", nullable = false)
  private Long mappingId;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Column(name = "groupId", nullable = false)
  private Long groupId;

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

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", insertable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "groupId", insertable = false, updatable = false)
  private UserGroup userGroup;

  public UserGroupUserMap() {}

  // Constructor for creating new mapping
  public UserGroupUserMap(Long userId, Long groupId, String createdUser) {
    validateUserId(userId);
    validateGroupId(groupId);
    validateUser(createdUser);

    this.userId = userId;
    this.groupId = groupId;
    this.createdUser = createdUser;
    this.modifiedUser = createdUser; // When creating, modified user is same as created user
  }

  // Constructor for updating existing mapping
  public UserGroupUserMap(
      Long userId, Long groupId, String modifiedUser, UserGroupUserMap existingMapping) {
    validateUserId(userId);
    validateGroupId(groupId);
    validateUser(modifiedUser);

    // Copy existing values that shouldn't change
    this.mappingId = existingMapping.getMappingId();
    this.createdUser = existingMapping.getCreatedUser();
    this.createdAt = existingMapping.getCreatedAt();

    this.userId = userId;
    this.groupId = groupId;
    this.modifiedUser = modifiedUser; // When updating, use the provided modified user
  }

  private void validateUserId(Long userId) {
    if (userId == null || userId <= 0) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }
  }

  private void validateGroupId(Long groupId) {
    if (groupId == null || groupId <= 0) {
      throw new BadRequestException(ErrorMessages.UserGroupErrorMessages.INVALID_ID);
    }
  }

  private void validateUser(String user) {
    if (user == null || user.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.INVALID_USER);
    }
  }
}
