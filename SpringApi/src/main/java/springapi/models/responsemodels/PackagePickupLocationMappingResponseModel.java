package springapi.models.responsemodels;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import springapi.models.databasemodels.PackagePickupLocationMapping;

/**
 * Response model for Package Pickup Location Mapping operations.
 *
 * <p>This model is used for returning package inventory information at pickup locations in API
 * responses. Includes all audit fields for complete information.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PackagePickupLocationMappingResponseModel {

  /** Unique identifier for this mapping. */
  private Long packagePickupLocationMappingId;

  /** Available quantity of packages at this location. Maps to the availableQuantity column. */
  private Integer quantity;

  /** When to reorder packages - alert threshold. Maps to the reorderLevel column (default 10). */
  private Integer reorderLevel;

  /**
   * Maximum stock level to maintain at this location. Maps to the maxStockLevel column (default.
   * 1000).
   */
  private Integer maxStockLevel;

  /**
   * When packages were last restocked at this location. Maps to the lastRestockDate column.
   * (nullable).
   */
  private LocalDateTime lastRestockDate;

  /** The package ID this mapping belongs to. */
  private Long packageId;

  /** The pickup location ID this mapping belongs to. */
  private Long pickupLocationId;

  /** Optional notes about this mapping. */
  private String notes;

  // ==================== Audit Fields ====================

  /** The user who created this mapping. */
  private String createdUser;

  /** The user who last modified this mapping. */
  private String modifiedUser;

  /** When this mapping was created. */
  private LocalDateTime createdAt;

  /** When this mapping was last updated. */
  private LocalDateTime updatedAt;

  /** Default constructor. */
  public PackagePickupLocationMappingResponseModel() {}

  /**
   * Constructor with essential inventory fields (for backward compatibility). Used when only.
   * inventory data is needed without full audit info.
   *
   * @param quantity Available quantity of packages
   * @param reorderLevel Threshold for reorder alerts
   * @param maxStockLevel Maximum stock level to maintain
   * @param lastRestockDate When packages were last restocked
   */
  public PackagePickupLocationMappingResponseModel(
      Integer quantity,
      Integer reorderLevel,
      Integer maxStockLevel,
      LocalDateTime lastRestockDate) {
    this.quantity = quantity;
    this.reorderLevel = reorderLevel;
    this.maxStockLevel = maxStockLevel;
    this.lastRestockDate = lastRestockDate;
  }

  /**
   * Constructor that creates a response model from a PackagePickupLocationMapping entity.
   * Populates. all fields including audit fields.
   *
   * @param mapping The PackagePickupLocationMapping entity to convert
   */
  public PackagePickupLocationMappingResponseModel(PackagePickupLocationMapping mapping) {
    if (mapping != null) {
      this.packagePickupLocationMappingId = mapping.getPackagePickupLocationMappingId();
      this.quantity = mapping.getAvailableQuantity();
      this.reorderLevel = mapping.getReorderLevel();
      this.maxStockLevel = mapping.getMaxStockLevel();
      this.lastRestockDate = mapping.getLastRestockDate();
      this.packageId = mapping.getPackageId();
      this.pickupLocationId = mapping.getPickupLocationId();
      this.notes = mapping.getNotes();
      this.createdUser = mapping.getCreatedUser();
      this.modifiedUser = mapping.getModifiedUser();
      this.createdAt = mapping.getCreatedAt();
      this.updatedAt = mapping.getUpdatedAt();
    }
  }
}
