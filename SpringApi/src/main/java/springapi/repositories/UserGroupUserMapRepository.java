package com.example.springapi.repositories;

import com.example.springapi.models.databasemodels.UserGroupUserMap;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Defines the user group user map repository contract.
 */
@Repository
public interface UserGroupUserMapRepository extends JpaRepository<UserGroupUserMap, Long> {

  @Query(
      "SELECT ugm FROM UserGroupUserMap ugm WHERE ugm.groupId IN :"
          + "userGroupIds and ugm.userId = :userId")
  List<UserGroupUserMap> findUserGroupsUsersMapByGroupIdAndUserId(
      @Param("userGroupIds") List<Long> userGroupIds, @Param("userId") long userId);

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

  /**
   * Deletes all user-group mappings for a specific user.
   *
   * @param userId The unique identifier of the user
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM UserGroupUserMap ugm WHERE ugm.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
