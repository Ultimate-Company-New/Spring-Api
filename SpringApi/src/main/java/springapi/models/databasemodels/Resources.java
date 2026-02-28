package springapi.models.databasemodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the Resources table.
 *
 * <p>This entity represents file attachments and resources for various entities such as
 * PurchaseOrder, Lead, User, Product, etc.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-10
 */
@Getter
@Setter
@Entity
@Table(name = "Resources")
public class Resources {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "resourceId", nullable = false)
  private Long resourceId;

  @Column(name = "entityId", nullable = false)
  private Long entityId;

  @Column(name = "entityType", nullable = false, length = 50)
  private String entityType; // Type of entity (PurchaseOrder, Lead, User, Product, etc.)

  @Column(name = "`key`", nullable = false, length = 500)
  private String key; // File name or identifier

  @Column(name = "`value`", nullable = false, columnDefinition = "TEXT")
  private String value; // URL of the file

  @Column(name = "deleteHashValue", length = 500)
  private String deleteHashValue; // Delete hash for ImgBB

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  /** Default constructor. */
  public Resources() {}

  /**
   * Constructor for creating a new resource.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   * @param entityType The entity type (e.g., "PurchaseOrder", "Lead")
   * @param key The file name or identifier
   * @param value The URL of the file
   * @param deleteHashValue The delete hash (optional)
   */
  public Resources(
      Long entityId, String entityType, String key, String value, String deleteHashValue) {
    this.entityId = entityId;
    this.entityType = entityType;
    this.key = key;
    this.value = value;
    this.deleteHashValue = deleteHashValue;
  }

  /**
   * Constructor for creating a new resource with notes.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   * @param entityType The entity type (e.g., "PurchaseOrder", "Lead")
   * @param key The file name or identifier
   * @param value The URL of the file
   * @param deleteHashValue The delete hash (optional)
   * @param notes Additional notes (optional)
   */
  public Resources(
      Long entityId,
      String entityType,
      String key,
      String value,
      String deleteHashValue,
      String notes) {
    this.entityId = entityId;
    this.entityType = entityType;
    this.key = key;
    this.value = value;
    this.deleteHashValue = deleteHashValue;
    this.notes = notes;
  }
}
