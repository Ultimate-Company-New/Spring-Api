package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.PickupLocation;

/**
 * Repository interface for PickupLocation entity.
 *
 * <p>This interface provides CRUD operations for PickupLocation entities and custom queries for
 * pickup location management.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface PickupLocationRepository extends JpaRepository<PickupLocation, Long> {

  @Query(
      "SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE "
          + "p.addressNickName = :addressNickName AND p.clientId = :clientId")
  PickupLocation findByAddressNickNameAndClientId(
      @Param("addressNickName") String addressNickName, @Param("clientId") Long clientId);

  @Query(
      "SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE "
          + "p.pickupLocationId = :pickupLocationId AND p.clientId = :clientId")
  PickupLocation findPickupLocationByIdAndClientId(
      @Param("pickupLocationId") Long pickupLocationId, @Param("clientId") Long clientId);

  @Query(
      "SELECT COUNT(p.pickupLocationId) FROM PickupLocation p WHERE "
          + "p.pickupLocationId = :pickupLocationId AND p.clientId = :clientId")
  long countByPickupLocationIdAndClientId(
      @Param("pickupLocationId") Long pickupLocationId, @Param("clientId") Long clientId);

  @Query(
      "SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE "
          + "p.clientId = :clientId AND (:includeDeleted = true OR p.isDeleted = "
          + "false)")
  List<PickupLocation> findAllWithAddressesByClientId(
      @Param("clientId") Long clientId, @Param("includeDeleted") boolean includeDeleted);
}
