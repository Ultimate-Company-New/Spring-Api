package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserClientPermissionMappingRepository extends JpaRepository<UserClientPermissionMapping, Long> {
    /**
     * Finds all permission mappings for a specific user and client.
     * 
     * @param userId The unique identifier of the user
     * @param clientId The unique identifier of the client
     * @return List of UserClientPermissionMapping entities
     */
    List<UserClientPermissionMapping> findByUserIdAndClientId(Long userId, Long clientId);

    @Query("SELECT DISTINCT uc " +
            "FROM UserClientPermissionMapping uc " +
            "WHERE uc.userId = :userId")
    List<UserClientPermissionMapping> findClientPermissionMappingByUserId(@Param("userId") long userId);
}
