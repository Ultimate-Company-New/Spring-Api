package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Paginated query for users with filtering and sorting.
     * Used by: UserService.fetchAllUsersInSystem
     */
    @Query("select u from User u " +
        "join UserClientMapping ucm on u.userId = ucm.userId " +
        "left join Address a on u.addressId = a.addressId " +
        "where ucm.clientId = :carrierId " +
        "and (:selectedUsers IS NULL OR u.userId IN (:selectedUsers)) " +
        "and (:includeDeleted = true OR u.isDeleted = false) " +
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
           "LEFT JOIN FETCH u.addresses " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.userId = :userId " +
           "AND (ucpm.clientId = :clientId OR ucpm.clientId IS NULL) " +
           "AND (ug.clientId = :clientId OR ug.clientId IS NULL)")
    User findByIdWithAllRelations(@Param("userId") Long userId, @Param("clientId") Long clientId);

    /**
     * SUPER OPTIMIZED: Fetches user by email with client-specific data.
     * Filters permissions and usergroups by clientId in ONE query.
     * Addresses are user-specific (not filtered by client).
     * 
     * Query fetches:
     * - User data
     * - All addresses for the user
     * - Permissions ONLY for the specified clientId
     * - User groups ONLY for the specified clientId
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.addresses " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.email = :email " +
           "AND (ucpm.clientId = :clientId OR ucpm.clientId IS NULL) " +
           "AND (ug.clientId = :clientId OR ug.clientId IS NULL)")
    User findByEmailWithAllRelations(@Param("email") String email, @Param("clientId") Long clientId);
}
