package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines the product category repository contract.
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
