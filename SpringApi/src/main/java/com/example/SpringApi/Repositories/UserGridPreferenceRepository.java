package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.UserGridPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Defines the user grid preference repository contract.
 */
@Repository
public interface UserGridPreferenceRepository extends JpaRepository<UserGridPreference, Long> {
  UserGridPreference findUserGridPreferenceByUserIdAndGridName(long userId, String gridName);
}
