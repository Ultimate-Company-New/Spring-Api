package com.example.SpringApi.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;

import java.util.List;

@Repository
public interface UserGroupUserMapRepository extends JpaRepository<UserGroupUserMap, Long> {
    
    @Query("SELECT ugm FROM UserGroupUserMap ugm WHERE ugm.groupId IN :userGroupIds and ugm.userId = :userId")
    List<UserGroupUserMap> findUserGroupsUsersMapByGroupIdAndUserId(@Param("userGroupIds") List<Long> userGroupIds, @Param("userId") long userId);
   
    /**
     * Finds all user-group mappings for a specific group.
     * 
     * @param groupId The unique identifier of the user group
     * @return List of UserGroupUserMap entities for the group
     */
    List<UserGroupUserMap> findByGroupId(Long groupId);
    
    /**
     * Finds all user-group mappings for a specific user.
     * 
     * @param userId The unique identifier of the user
     * @return List of UserGroupUserMap entities for the user
     */
    List<UserGroupUserMap> findByUserId(Long userId);
}
