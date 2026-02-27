package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

  /**
   * Finds a user group by its name (case-sensitive).
   *
   * @param groupName The name of the group to search for
   * @return UserGroups entity if found, null otherwise
   */
  UserGroup findByGroupName(String groupName);

  /**
   * Finds a user group by ID with all user mappings and users loaded in a single query. This uses
   * JOIN FETCH to eagerly load all related data to avoid N+1 query problem.
   *
   * @param groupId The unique identifier of the user group
   * @return UserGroup entity with users loaded, or null if not found
   */
  @Query(
      "SELECT DISTINCT ug FROM UserGroup ug "
          + "LEFT JOIN FETCH ug.userMappings ugm "
          + "LEFT JOIN FETCH ugm.user u "
          + "WHERE ug.groupId = :groupId "
          + "ORDER BY u.userId DESC")
  UserGroup findByIdWithUsers(@Param("groupId") Long groupId);
}
