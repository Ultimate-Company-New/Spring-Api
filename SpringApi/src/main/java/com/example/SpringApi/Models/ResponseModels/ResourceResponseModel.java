package com.example.springapi.models.responsemodels;

import com.example.springapi.models.databasemodels.Resources;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for Resources operations.
 *
 * <p>This model contains all the fields returned when retrieving resource (attachment) information.
 * Resources are files/attachments linked to entities like Purchase Orders.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ResourceResponseModel {

  private Long resourceId;
  private Long entityId;
  private String entityType;
  private String key;
  private String value;
  private String deleteHashValue;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String notes;

  /** Default constructor. */
  public ResourceResponseModel() {}

  /**
   * Constructor that populates fields from a Resources entity.
   *
   * @param resource The Resources entity to populate from
   */
  public ResourceResponseModel(Resources resource) {
    if (resource != null) {
      this.resourceId = resource.getResourceId();
      this.entityId = resource.getEntityId();
      this.entityType = resource.getEntityType();
      this.key = resource.getKey();
      this.value = resource.getValue();
      this.deleteHashValue = resource.getDeleteHashValue();
      this.createdAt = resource.getCreatedAt();
      this.updatedAt = resource.getUpdatedAt();
      this.notes = resource.getNotes();
    }
  }
}
