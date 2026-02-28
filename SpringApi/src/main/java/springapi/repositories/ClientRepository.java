package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines the client repository contract.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

  /**
   * Check if a client with the given name exists.
   *
   * @param name The client name to check
   * @return true if a client with this name exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Find a client by name.
   *
   * @param name The client name to search for
   * @return Optional containing the client if found
   */
  Optional<Client> findByName(String name);

  @Query(
      value =
          "SELECT c FROM Client c JOIN UserClientMapping ucm "
              + "ON c.clientId = ucm.clientId "
              + "WHERE c.isDeleted = false "
              + "AND ucm.userId = :userId "
              + "AND (:filteredText IS NULL OR :filteredText = '' OR TRIM(:"
              + "filteredText) = '' OR c.name LIKE CONCAT('%', :filteredText, '%'))")
  Page<Client> findByUserIdAndNameContains(
      @Param("userId") Long userId, @Param("filteredText") String filteredText, Pageable pageable);

  @Query(
      value =
          "SELECT c FROM Client c JOIN UserClientMapping ucm ON c.clientId = "
              + "ucm.clientId WHERE c.isDeleted = false AND ucm.userId = :userId "
              + "order by c.clientId desc")
  List<Client> findByUserId(@Param("userId") Long userId);

  @Query(
      value =
          "SELECT c FROM Client c LEFT JOIN FETCH c.googleCred WHERE "
              + "c.isDeleted = false ORDER BY c.clientId ASC LIMIT 1")
  Client findFirstByOrderByClientIdAsc();
}
