package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Defines the lead repository contract.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

  @Query(
      "SELECT l FROM Lead l "
          + "LEFT JOIN FETCH l.address "
          + "LEFT JOIN FETCH l.createdByUser "
          + "LEFT JOIN FETCH l.assignedAgent "
          + "WHERE l.leadId = :leadId AND l.clientId = :clientId AND l.isDeleted = false")
  Lead findLeadWithDetailsById(@Param("leadId") Long leadId, @Param("clientId") Long clientId);

  @Query(
      "SELECT l FROM Lead l "
          + "LEFT JOIN FETCH l.address "
          + "LEFT JOIN FETCH l.createdByUser "
          + "LEFT JOIN FETCH l.assignedAgent "
          + "WHERE l.email = :email AND l.clientId = :clientId AND l.isDeleted = false")
  Lead findLeadWithDetailsByEmail(@Param("email") String email, @Param("clientId") Long clientId);

  @Query(
      "SELECT l FROM Lead l "
          + "LEFT JOIN FETCH l.address "
          + "LEFT JOIN FETCH l.createdByUser "
          + "LEFT JOIN FETCH l.assignedAgent "
          + "WHERE l.leadId = :leadId AND l.clientId = :clientId")
  Lead findLeadWithDetailsByIdIncludingDeleted(
      @Param("leadId") Long leadId, @Param("clientId") Long clientId);
}
