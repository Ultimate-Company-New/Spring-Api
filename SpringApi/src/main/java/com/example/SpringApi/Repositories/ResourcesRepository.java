package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Resources;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Resources entity.
 *
 * <p>This repository provides database access methods for file attachments/resources.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-10
 */
@Repository
public interface ResourcesRepository extends JpaRepository<Resources, Long> {

  /**
   * Find all resources for a specific entity.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   * @return List of resources for the entity
   */
  @Query("SELECT r FROM Resources r WHERE r.entityId = :entityId ORDER BY r.createdAt DESC")
  List<Resources> findByEntityId(@Param("entityId") Long entityId);

  /**
   * Find all resources for a specific entity filtered by entity type.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   * @param entityType The entity type (e.g., "PurchaseOrder", "Lead")
   * @return List of resources for the entity
   */
  @Query(
      "SELECT r FROM Resources r WHERE r.entityId = :entityId AND r.entityType = :entityType ORDER BY r.createdAt DESC")
  List<Resources> findByEntityIdAndEntityType(
      @Param("entityId") Long entityId, @Param("entityType") String entityType);

  /**
   * Find all resources for multiple entities (batch fetch).
   *
   * @param entityIds The entity IDs (e.g., purchaseOrderIds)
   * @param entityType The entity type (e.g., "PURCHASE_ORDER")
   * @return List of resources for the entities
   */
  @Query(
      "SELECT r FROM Resources r WHERE r.entityId IN :entityIds AND r.entityType = :entityType ORDER BY r.entityId, r.createdAt DESC")
  List<Resources> findByEntityIdInAndEntityType(
      @Param("entityIds") List<Long> entityIds, @Param("entityType") String entityType);

  /**
   * Delete all resources for a specific entity.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   */
  void deleteByEntityId(Long entityId);

  /**
   * Delete all resources for a specific entity and entity type.
   *
   * @param entityId The entity ID (e.g., purchaseOrderId)
   * @param entityType The entity type (e.g., "PurchaseOrder")
   */
  void deleteByEntityIdAndEntityType(Long entityId, String entityType);
}

