package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
