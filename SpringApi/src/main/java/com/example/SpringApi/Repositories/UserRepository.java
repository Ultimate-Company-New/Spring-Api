package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Standard JPA Repository for User entity. Uses @Query annotations for custom queries.
 *
 * <p>For multi-filter queries, use UserFilterQueryBuilder directly from the service layer.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByLoginName(String loginName);

  /**
   * Optimized query to fetch user with client-specific data by userId. Filters permissions and
   * usergroups by clientId. Addresses are user-specific (not filtered by client).
   */
  @Query(
      "SELECT DISTINCT u FROM User u "
          + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
          + "LEFT JOIN FETCH u.addresses "
          + "LEFT JOIN FETCH u.userClientPermissionMappings ucpm "
          + "LEFT JOIN FETCH ucpm.permission p "
          + "LEFT JOIN FETCH u.userGroupMappings ugm "
          + "LEFT JOIN FETCH ugm.userGroup ug "
          + "WHERE u.userId = :userId "
          + "AND ucm.clientId = :clientId "
          + "AND (ucpm IS NULL OR ucpm.clientId = :clientId) "
          + "AND (ugm IS NULL OR ug.clientId = :clientId)")
  User findByIdWithAllRelations(@Param("userId") Long userId, @Param("clientId") Long clientId);

  /**
   * SUPER OPTIMIZED: Fetches user by email with client-specific data. Filters permissions and
   * usergroups by clientId in ONE query.
   */
  @Query(
      "SELECT DISTINCT u FROM User u "
          + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
          + "LEFT JOIN FETCH u.addresses "
          + "LEFT JOIN FETCH u.userClientPermissionMappings ucpm "
          + "LEFT JOIN FETCH ucpm.permission p "
          + "LEFT JOIN FETCH u.userGroupMappings ugm "
          + "LEFT JOIN FETCH ugm.userGroup ug "
          + "WHERE u.email = :email "
          + "AND ucm.clientId = :clientId "
          + "AND (ucpm IS NULL OR ucpm.clientId = :clientId) "
          + "AND (ugm IS NULL OR ug.clientId = :clientId)")
  User findByEmailWithAllRelations(@Param("email") String email, @Param("clientId") Long clientId);

  @Query(
      "SELECT u FROM User u "
          + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
          + "WHERE u.userId = :userId AND ucm.clientId = :clientId")
  Optional<User> findByUserIdAndClientId(
      @Param("userId") Long userId, @Param("clientId") Long clientId);

  /** Fetches all user emails in a client by user IDs and group IDs. */
  @Query(
      "SELECT DISTINCT u.email FROM User u "
          + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
          + "LEFT JOIN UserGroupUserMap ugm ON u.userId = ugm.userId "
          + "LEFT JOIN UserGroup ug ON ugm.groupId = ug.groupId "
          + "WHERE ucm.clientId = :clientId "
          + "AND u.isDeleted = false "
          + "AND u.email IS NOT NULL "
          + "AND TRIM(u.email) <> '' "
          + "AND ("
          + "  (u.userId IN :userIds) "
          + "  OR "
          + "  (ugm.groupId IN :groupIds AND (ug.isDeleted = false OR ug.isDeleted IS NULL))"
          + ")")
  List<String> findAllUserEmailsByClientAndUserIdsAndGroupIds(
      @Param("clientId") Long clientId,
      @Param("userIds") List<Long> userIds,
      @Param("groupIds") List<Long> groupIds);
}
