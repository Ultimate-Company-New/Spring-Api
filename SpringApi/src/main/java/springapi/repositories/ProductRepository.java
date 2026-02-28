package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Product entity.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      "SELECT DISTINCT p FROM Product p "
          + "LEFT JOIN FETCH p.category "
          + "LEFT JOIN FETCH p.createdByUser "
          + "LEFT JOIN FETCH p.productPickupLocationMappings pplm "
          + "LEFT JOIN FETCH pplm.pickupLocation pl "
          + "LEFT JOIN FETCH pl.address "
          + "WHERE p.productId = :id AND p.clientId = :clientId")
  Product findByIdWithRelatedEntities(@Param("id") Long id, @Param("clientId") Long clientId);
}
