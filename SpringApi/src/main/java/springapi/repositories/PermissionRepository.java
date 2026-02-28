package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.Permission;

/** Repository interface for Permission entity operations. */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {}
