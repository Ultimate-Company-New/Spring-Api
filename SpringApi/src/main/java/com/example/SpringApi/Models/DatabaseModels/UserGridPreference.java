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
@Table(name = "`UserGridPreference`")
public class UserGridPreference {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "preferenceId", nullable = false)
  private Long preferenceId;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Column(name = "clientId")
  private Long clientId;

  @Column(name = "gridName", nullable = false)
  private String gridName;

  @Column(name = "columnOrder", columnDefinition = "TEXT")
  private String columnOrder;

  @Column(name = "columnWidths", columnDefinition = "TEXT")
  private String columnWidths;

  @Column(name = "visibleColumns", columnDefinition = "TEXT")
  private String visibleColumns;

  @Column(name = "sortColumn")
  private String sortColumn;

  @Column(name = "sortDirection")
  private String sortDirection;

  @Column(name = "pageSize")
  private Integer pageSize;

  @Column(name = "filters", columnDefinition = "TEXT")
  private String filters;

  @Column(name = "isDefault", nullable = false)
  private Boolean isDefault;

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
}
