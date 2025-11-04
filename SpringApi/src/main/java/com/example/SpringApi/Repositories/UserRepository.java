package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Paginated query for users with filtering and sorting.
     * Fetches all related entities (addresses, permissions, user groups) with client filtering.
     * Used by: UserService.fetchAllUsersInSystem
     */
    @Query("SELECT DISTINCT u FROM User u " +
        "JOIN UserClientMapping ucm ON u.userId = ucm.userId " +
        "LEFT JOIN FETCH u.addresses " +
        "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
        "LEFT JOIN FETCH ucpm.permission p " +
        "LEFT JOIN FETCH u.userGroupMappings ugm " +
        "LEFT JOIN FETCH ugm.userGroup ug " +
        "LEFT JOIN Address a ON u.addressId = a.addressId " +
        "WHERE ucm.clientId = :carrierId " +
        "AND (ucpm IS NULL OR ucpm.clientId = :carrierId) " +
        "AND (ugm IS NULL OR ug.clientId = :carrierId) " +
        "AND (:selectedUsers IS NULL OR u.userId IN (:selectedUsers)) " +
        "AND (:includeDeleted = true OR u.isDeleted = false) " +
        "AND (COALESCE(:filterExpr, '') = '' OR " +
        "(CASE :columnName " +
        "WHEN 'userId' THEN CONCAT(u.userId, '') " +
        "WHEN 'firstName' THEN CONCAT(u.firstName, '') " +
        "WHEN 'lastName' THEN CONCAT(u.lastName, '') " +
        "WHEN 'loginName' THEN CONCAT(u.loginName, '') " +
        "WHEN 'role' THEN CONCAT(u.role, '') " +
        "WHEN 'dob' THEN CONCAT(u.dob, '') " +
        "WHEN 'phone' THEN CONCAT(u.phone, '') " +
        "WHEN 'address' THEN CONCAT(COALESCE(a.streetAddress, ''), ' ', COALESCE(a.streetAddress2, ''), ' ', COALESCE(a.streetAddress3, ''), ' ', COALESCE(a.city, ''), ' ', COALESCE(a.state, ''), ' ', COALESCE(a.postalCode, ''), ' ', COALESCE(a.country, '')) " +
        "WHEN 'datePasswordChanges' THEN CONCAT(u.datePasswordChanges, '') " +
        "WHEN 'loginAttempts' THEN CONCAT(u.loginAttempts, '') " +
        "WHEN 'isDeleted' THEN CONCAT(u.isDeleted, '') " +
        "WHEN 'locked' THEN CONCAT(u.locked, '') " +
        "WHEN 'emailConfirmed' THEN CONCAT(u.emailConfirmed, '') " +
        "WHEN 'token' THEN u.token " +
        "WHEN 'isGuest' THEN CONCAT(u.isGuest, '') " +
        "WHEN 'email' THEN u.email " +
        "WHEN 'addressId' THEN CONCAT(u.addressId, '') " +
        "WHEN 'profilePicture' THEN u.profilePicture " +
        "WHEN 'lastLoginAt' THEN CONCAT(u.lastLoginAt, '') " +
        "WHEN 'createdAt' THEN CONCAT(u.createdAt, '') " +
        "WHEN 'createdUser' THEN u.createdUser " +
        "WHEN 'updatedAt' THEN CONCAT(u.updatedAt, '') " +
        "WHEN 'modifiedUser' THEN u.modifiedUser " +
        "WHEN 'notes' THEN u.notes " +
        "ELSE '' END) LIKE " +
        "(CASE :condition " +
        "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
        "WHEN 'equals' THEN :filterExpr " +
        "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
        "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
        "WHEN 'isEmpty' THEN '' " +
        "WHEN 'isNotEmpty' THEN '%' " +
        "ELSE '' END))")
    Page<User> findPaginatedUsers(@Param("carrierId") long carrierId,
                  @Param("selectedUsers") List<Long> selectedUsers,
                  @Param("columnName") String columnName,
                  @Param("condition") String condition,
                  @Param("filterExpr") String filterExpr,
                  @Param("includeDeleted") boolean includeDeleted,
                  Pageable pageable);

    User findByLoginName(String loginName);

    /**
     * Optimized query to fetch user with client-specific data by userId.
     * Filters permissions and usergroups by clientId.
     * Addresses are user-specific (not filtered by client).
     * Used by: UserService (getUserById, updateUser, toggleUser)
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN UserClientMapping ucm ON u.userId = ucm.userId " +
           "LEFT JOIN FETCH u.addresses " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.userId = :userId " +
           "AND ucm.clientId = :clientId " +
           "AND (ucpm IS NULL OR ucpm.clientId = :clientId) " +
           "AND (ugm IS NULL OR ug.clientId = :clientId)")
    User findByIdWithAllRelations(@Param("userId") Long userId, @Param("clientId") Long clientId);

    /**
     * SUPER OPTIMIZED: Fetches user by email with client-specific data.
     * Filters permissions and usergroups by clientId in ONE query.
     * Addresses are user-specific (not filtered by client).
     * 
     * Query fetches:
     * - User data (ONLY if user is mapped to the specified clientId)
     * - All addresses for the user
     * - Permissions ONLY for the specified clientId
     * - User groups ONLY for the specified clientId
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN UserClientMapping ucm ON u.userId = ucm.userId " +
           "LEFT JOIN FETCH u.addresses " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.email = :email " +
           "AND ucm.clientId = :clientId " +
           "AND (ucpm IS NULL OR ucpm.clientId = :clientId) " +
           "AND (ugm IS NULL OR ug.clientId = :clientId)")
    User findByEmailWithAllRelations(@Param("email") String email, @Param("clientId") Long clientId);

    @Query("SELECT u FROM User u " +
           "JOIN UserClientMapping ucm ON u.userId = ucm.userId " +
           "WHERE u.userId = :userId AND ucm.clientId = :clientId")
    Optional<User> findByUserIdAndClientId(@Param("userId") Long userId, @Param("clientId") Long clientId);

    /**
     * Fetches all user emails in a client by user IDs and group IDs.
     * Only returns emails from non-deleted users who belong to non-deleted groups.
     * Combines direct user targeting and group-based targeting in a single query.
     * 
     * @param clientId The client ID
     * @param userIds List of user IDs (can be null or empty)
     * @param groupIds List of group IDs (can be null or empty)
     * @return List of distinct email addresses
     */
    @Query("SELECT DISTINCT u.email FROM User u " +
           "JOIN UserClientMapping ucm ON u.userId = ucm.userId " +
           "LEFT JOIN UserGroupUserMap ugm ON u.userId = ugm.userId " +
           "LEFT JOIN UserGroup ug ON ugm.groupId = ug.groupId " +
           "WHERE ucm.clientId = :clientId " +
           "AND u.isDeleted = false " +
           "AND u.email IS NOT NULL " +
           "AND TRIM(u.email) <> '' " +
           "AND (" +
           "  (u.userId IN :userIds) " +
           "  OR " +
           "  (ugm.groupId IN :groupIds AND (ug.isDeleted = false OR ug.isDeleted IS NULL))" +
           ")")
    List<String> findAllUserEmailsByClientAndUserIdsAndGroupIds(@Param("clientId") Long clientId,
                                                                  @Param("userIds") List<Long> userIds,
                                                                  @Param("groupIds") List<Long> groupIds);
}
