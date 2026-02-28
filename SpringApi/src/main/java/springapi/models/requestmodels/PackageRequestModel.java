package springapi.models.requestmodels;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for Package operations.
 *
 * <p>This model is used for creating and updating package information.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class PackageRequestModel {
  private Long packageId;
  private String packageName;
  private Integer length;
  private Integer breadth;
  private Integer height;
  private BigDecimal maxWeight;
  private Integer standardCapacity;
  private BigDecimal pricePerUnit;
  private String packageType;
  private Boolean isDeleted;
  private String notes;

  /**
   * Map of pickup location ID to pickup location inventory data. A package can be stocked at.
   * multiple pickup locations with different inventory settings. Uses
   * PackagePickupLocationMappingRequestModel for each location's inventory configuration.
   */
  private Map<Long, PackagePickupLocationMappingRequestModel> pickupLocationQuantities;
}
