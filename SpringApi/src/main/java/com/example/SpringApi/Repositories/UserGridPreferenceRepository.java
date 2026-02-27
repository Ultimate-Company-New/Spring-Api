package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserGridPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGridPreferenceRepository extends JpaRepository<UserGridPreference, Long> {
  UserGridPreference findUserGridPreferenceByUserIdAndGridName(long userId, String gridName);
}
