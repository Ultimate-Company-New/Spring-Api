package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserClientMappingRepository extends JpaRepository<UserClientMapping, Long> {

  @Query("SELECT u FROM UserClientMapping u WHERE u.userId IN :userIds AND u.clientId = :clientId")
  List<UserClientMapping> findByUserIdsAndClientId(
      @Param("userIds") List<Long> userIds, @Param("clientId") Long clientId);

  /**
   * Finds a UserClientMapping by userId and clientId.
   *
   * @param userId The user ID
   * @param clientId The client ID
   * @return Optional containing the mapping if found
   */
  Optional<UserClientMapping> findByUserIdAndClientId(Long userId, Long clientId);

  /**
   * Finds a UserClientMapping by apiKey.
   *
   * @param apiKey The API key
   * @return Optional containing the mapping if found
   */
  Optional<UserClientMapping> findByApiKey(String apiKey);

  /**
   * Finds all UserClientMappings for a given user ID.
   *
   * @param userId The user ID
   * @return List of UserClientMappings for the user
   */
  List<UserClientMapping> findByUserId(Long userId);
}
