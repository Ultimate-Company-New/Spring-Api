package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.Package;

/** Defines the package repository contract. */
@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

  @Query("SELECT p FROM Package p WHERE p.clientId = :clientId AND p.isDeleted = false")
  List<Package> findByClientIdAndIsDeletedFalse(@Param("clientId") Long clientId);

  @Query("select p from Package p where p.packageId = :packageId and p.clientId = :clientId")
  Package findByPackageIdAndClientId(
      @Param("packageId") Long packageId, @Param("clientId") Long clientId);
}
