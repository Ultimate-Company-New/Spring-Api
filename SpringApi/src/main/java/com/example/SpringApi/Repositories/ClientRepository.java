package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Client;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query(value = "SELECT c FROM Client c JOIN UserClientMapping ucm " +
            "ON c.clientId = ucm.clientId " +
            "WHERE c.isDeleted = false " +
            "AND ucm.userId = :userId " +
            "AND (:filteredText IS NULL OR :filteredText = '' OR TRIM(:filteredText) = '' OR c.name LIKE CONCAT('%', :filteredText, '%'))")
    Page<Client> findByUserIdAndNameContains(@Param("userId") Long userId,
                                              @Param("filteredText") String filteredText,
                                              Pageable pageable);

    @Query(value = "SELECT COUNT(c) FROM Client c JOIN UserClientMapping ucm ON c.clientId = ucm.clientId WHERE c.isDeleted = false AND ucm.userId = :userId AND c.name LIKE CONCAT('%', :filteredText, '%')")
    long countByUserIdAndNameContains(@Param("userId") Long userId, @Param("filteredText") String filteredText);

    @Query(value = "SELECT c FROM Client c JOIN UserClientMapping ucm ON c.clientId = ucm.clientId WHERE c.isDeleted = false AND ucm.userId = :userId")
    List<Client> findByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT c FROM Client c LEFT JOIN FETCH c.googleCred WHERE c.isDeleted = false ORDER BY c.clientId ASC LIMIT 1")
    Client findFirstByOrderByClientIdAsc();
}
