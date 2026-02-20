package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity representing the GoogleCred table.
 *
 * <p>This entity maps to the GoogleCred table in the UltimateCompany database. Stores Google Cloud
 * credentials for Firebase and other Google services.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "GoogleCred")
public class GoogleCred {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "googleCredId", nullable = false)
  private Long googleCredId;

  @Column(name = "type", nullable = false, columnDefinition = "TEXT")
  private String type;

  @Column(name = "projectId", nullable = false, columnDefinition = "TEXT")
  private String projectId;

  @Column(name = "privateKeyId", nullable = false, columnDefinition = "TEXT")
  private String privateKeyId;

  @Column(name = "privateKey", nullable = false, columnDefinition = "LONGTEXT")
  private String privateKey;

  @Column(name = "clientEmail", nullable = false, columnDefinition = "TEXT")
  private String clientEmail;

  @Column(name = "clientId", nullable = false, columnDefinition = "TEXT")
  private String clientId;

  @Column(name = "authUri", nullable = false, columnDefinition = "TEXT")
  private String authUri;

  @Column(name = "tokenUri", nullable = false, columnDefinition = "TEXT")
  private String tokenUri;

  @Column(name = "authProviderx509CertUrl", nullable = false, columnDefinition = "TEXT")
  private String authProviderx509CertUrl;

  @Column(name = "clientx509CertUrl", nullable = false, columnDefinition = "TEXT")
  private String clientx509CertUrl;

  @Column(name = "universeDomain", nullable = false, columnDefinition = "TEXT")
  private String universeDomain;

  // Tracking Fields
  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "createdUser", nullable = false)
  private String createdUser;

  @Column(name = "modifiedUser", nullable = false)
  private String modifiedUser;

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "createdUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User createdByUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "modifiedUser",
      referencedColumnName = "loginName",
      insertable = false,
      updatable = false)
  private User modifiedByUser;

  @OneToMany(mappedBy = "googleCred", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private java.util.List<Client> clients;

  /** Default constructor. Required by JPA for entity instantiation. */
  public GoogleCred() {
    // Required by JPA.
  }
}
