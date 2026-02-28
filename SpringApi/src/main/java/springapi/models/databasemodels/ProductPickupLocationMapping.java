package springapi.models.databasemodels;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;

/**
 * JPA Entity for the ProductPickupLocationMapping table.
 *
 * <p>This entity manages the many-to-many relationship between products and pickup locations.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
@Entity
@Table(name = "ProductPickupLocationMapping")
public class ProductPickupLocationMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "productPickupLocationMappingId", nullable = false)
  private Long productPickupLocationMappingId;

  @Column(name = "productId", nullable = false)
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productId", insertable = false, updatable = false)
  private Product product;

  @Column(name = "pickupLocationId", nullable = false)
  private Long pickupLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickupLocationId", insertable = false, updatable = false)
  private PickupLocation pickupLocation;

  @Column(name = "availableStock", nullable = false)
  private Integer availableStock;

  @Column(name = "itemAvailableFrom", nullable = false)
  private LocalDateTime itemAvailableFrom;

  @Column(name = "isActive", nullable = false)
  private Boolean isActive;

  @Column(name = "lastStockUpdate", nullable = false)
  private LocalDateTime lastStockUpdate;

  @Column(name = "minStockLevel", nullable = false)
  private Integer minStockLevel;

  @Column(name = "maxStockLevel", nullable = false)
  private Integer maxStockLevel;

  @Column(name = "reorderLevel", nullable = false)
  private Integer reorderLevel;

  @Column(name = "stockNotes", columnDefinition = "TEXT")
  private String stockNotes;

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

  /** Default constructor. */
  public ProductPickupLocationMapping() {}

  /**
   * Constructor for creating a new ProductPickupLocationMapping.
   *
   * @param productId The product ID
   * @param pickupLocationId The pickup location ID
   * @param availableStock The available stock at this pickup location
   * @param createdUser The user creating the mapping
   * @throws BadRequestException if validation fails
   */
  public ProductPickupLocationMapping(
      Long productId, Long pickupLocationId, Integer availableStock, String createdUser) {
    // Validate inputs
    if (productId == null) {
      throw new BadRequestException(
          ErrorMessages.ProductPickupLocationMappingErrorMessages.PRODUCT_ID_REQUIRED);
    }
    if (pickupLocationId == null) {
      throw new BadRequestException(
          ErrorMessages.ProductPickupLocationMappingErrorMessages.PICKUP_LOCATION_ID_REQUIRED);
    }
    if (availableStock == null || availableStock <= 0) {
      throw new BadRequestException(
          String.format(
              ErrorMessages.ProductPickupLocationMappingErrorMessages
                  .AVAILABLE_STOCK_MUST_BE_POSITIVE,
              pickupLocationId));
    }
    if (createdUser == null || createdUser.trim().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.ProductPickupLocationMappingErrorMessages.CREATED_USER_REQUIRED);
    }

    this.productId = productId;
    this.pickupLocationId = pickupLocationId;
    this.availableStock = availableStock;
    this.createdUser = createdUser;
    this.modifiedUser = createdUser;

    // Set default values for required fields
    this.itemAvailableFrom = LocalDateTime.now();
    this.isActive = true;
    this.lastStockUpdate = LocalDateTime.now();
    this.minStockLevel = 0;
    this.maxStockLevel = availableStock * 2; // Default to 2x current stock
    this.reorderLevel = availableStock / 2; // Default to 50% of current stock
  }

  /**
   * Creates a list of ProductPickupLocationMapping entities from a map of pickup location IDs to.
   * quantities.
   *
   * @param productId The product ID
   * @param pickupLocationQuantities Map of pickup location ID to available stock
   * @param createdUser The user creating the mappings
   * @return List of ProductPickupLocationMapping entities
   * @throws BadRequestException if validation fails
   */
  public static List<ProductPickupLocationMapping> createFromMap(
      Long productId, Map<Long, Integer> pickupLocationQuantities, String createdUser) {
    if (pickupLocationQuantities == null || pickupLocationQuantities.isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.ProductPickupLocationMappingErrorMessages
              .AT_LEAST_ONE_PICKUP_LOCATION_REQUIRED);
    }

    List<ProductPickupLocationMapping> mappings = new ArrayList<>();
    for (Map.Entry<Long, Integer> entry : pickupLocationQuantities.entrySet()) {
      mappings.add(
          new ProductPickupLocationMapping(
              productId, entry.getKey(), entry.getValue(), createdUser));
    }

    return mappings;
  }

  /**
   * Reduces available stock by the given quantity and updates audit fields.
   *
   * @param quantityToReduce The quantity to deduct from available stock
   * @param modifiedUser The user performing the update
   */
  public void reduceStock(int quantityToReduce, String modifiedUser) {
    this.availableStock = this.availableStock - quantityToReduce;
    this.lastStockUpdate = LocalDateTime.now();
    this.modifiedUser = modifiedUser;
  }
}
