package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ProductPickupLocationMapping entity operations.
 */
@Repository
public interface ProductPickupLocationMappingRepository extends JpaRepository<ProductPickupLocationMapping, Long> {
}

