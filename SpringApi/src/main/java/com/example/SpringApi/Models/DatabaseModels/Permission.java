package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the Permission table. Represents system permissions with categories.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "`Permission`")
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "permissionId", nullable = false)
  private Long permissionId;

  @Column(name = "permissionName", nullable = false, unique = true, length = 100)
  private String permissionName;

  @Column(name = "permissionCode", nullable = false, unique = true, length = 50)
  private String permissionCode;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "category", nullable = false, length = 50)
  private String category = "GENERAL";

  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted = false;

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
  @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<UserClientPermissionMapping> userClientPermissionMappings;

  /** Default no-argument constructor required by JPA/Hibernate. */
  public Permission() {
    // Default constructor for JPA
  }
}

