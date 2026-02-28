package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.ProductCategory;

/** Defines the product category repository contract. */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {}
