package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import springapi.models.databasemodels.UserClientPermissionMapping;

/** Defines the user client permission mapping repository contract. */
@Repository
public interface UserClientPermissionMappingRepository
    extends JpaRepository<UserClientPermissionMapping, Long> {
  /**
   * Finds all permission mappings for a specific user and client.
   *
   * @param userId The unique identifier of the user
   * @param clientId The unique identifier of the client
   * @return List of UserClientPermissionMapping entities
   */
  List<UserClientPermissionMapping> findByUserIdAndClientId(Long userId, Long clientId);

  @Query(
      "SELECT DISTINCT uc " + "FROM UserClientPermissionMapping uc " + "WHERE uc.userId = :userId")
  List<UserClientPermissionMapping> findClientPermissionMappingByUserId(
      @Param("userId") long userId);

  /**
   * Deletes all permission mappings for a specific user and client.
   *
   * @param userId The unique identifier of the user
   * @param clientId The unique identifier of the client
   */
  @Modifying
  @Transactional
  @Query(
      "DELETE FROM UserClientPermissionMapping uc WHERE uc.userId = :"
          + "userId AND uc.clientId = :clientId")
  void deleteByUserIdAndClientId(@Param("userId") Long userId, @Param("clientId") Long clientId);
}
