package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "`UserClientMapping`")
public class UserClientMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "mappingId", nullable = false)
  private Long mappingId;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @Column(name = "apiKey", nullable = false, length = 500)
  private String apiKey;

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

  /** Default no-argument constructor required by JPA/Hibernate. */
  public UserClientMapping() {
    // Default constructor for JPA
  }

  // Constructor for creation
  public UserClientMapping(
      Long userId, Long clientId, String apiKey, String createdUser, String modifiedUser) {
    this.userId = userId;
    this.clientId = clientId;
    this.apiKey = apiKey;
    this.createdUser = createdUser;
    this.modifiedUser = modifiedUser;
  }

  // Constructor for update (do not touch createdUser)
  public UserClientMapping(Long userId, Long clientId, String apiKey, String modifiedUser) {
    this.userId = userId;
    this.clientId = clientId;
    this.apiKey = apiKey;
    this.modifiedUser = modifiedUser;
  }
}
