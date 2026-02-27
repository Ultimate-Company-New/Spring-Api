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
 * Represents the user client permission mapping component.
 */
@Getter
@Setter
@Entity
@Table(name = "`UserClientPermissionMapping`")
public class UserClientPermissionMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permissionMappingId", nullable = false)
  private Long permissionMappingId;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @Column(name = "permissionId", nullable = false)
  private Long permissionId;

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
  @JoinColumn(name = "clientId", insertable = false, updatable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permissionId", insertable = false, updatable = false)
  private Permission permission;

  /** Default no-argument constructor required by JPA/Hibernate. */
  public UserClientPermissionMapping() {
    // Default constructor for JPA
  }

  // Constructor for creation
  /**
   * Executes user client permission mapping.
   */
  public UserClientPermissionMapping(
      Long userId, Long clientId, Long permissionId, String createdUser, String modifiedUser) {
    this.userId = userId;
    this.clientId = clientId;
    this.permissionId = permissionId;
    this.createdUser = createdUser;
    this.modifiedUser = modifiedUser;
  }

  // Constructor for update (do not touch createdUser)
  /**
   * Executes user client permission mapping.
   */
  public UserClientPermissionMapping(
      Long userId, Long clientId, Long permissionId, String modifiedUser) {
    this.userId = userId;
    this.clientId = clientId;
    this.permissionId = permissionId;
    this.modifiedUser = modifiedUser;
  }
}
