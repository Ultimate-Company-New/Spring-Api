package springapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.UserGridPreference;

/** Defines the user grid preference repository contract. */
@Repository
public interface UserGridPreferenceRepository extends JpaRepository<UserGridPreference, Long> {
  UserGridPreference findUserGridPreferenceByUserIdAndGridName(long userId, String gridName);
}
