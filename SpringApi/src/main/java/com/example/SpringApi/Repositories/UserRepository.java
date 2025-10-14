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
        "WHEN 'apiKey' THEN u.apiKey " +
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

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.loginName IN :loginNames")
    List<User> findByLoginNames(@Param("loginNames") List<String> loginNames);

    @Query(value = "SELECT u from User u where (:includeDeleted = true or u.isDeleted = true)")
    List<User> findAllWithIncludeDeleted(@Param("includeDeleted") boolean includeDeleted);

    @Query(value = "SELECT u from User u JOIN UserClientMapping ucm on u.userId = ucm.userId where ucm.clientId = :carrierId and (:includeDeleted = true or u.isDeleted = false)")
    List<User> findAllWithIncludeDeletedInCarrier(@Param("includeDeleted") boolean includeDeleted, @Param("carrierId")long carrierId);

    @Query("SELECT p.permissionId, p.permissionName, p.permissionCode, p.description, p.category " +
           "FROM UserClientPermissionMapping ucp " +
           "JOIN ucp.permission p " +
           "WHERE ucp.user.userId = :userId")
    List<Object[]> findUserPermissions(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ug FROM UserGroup ug " +
           "JOIN UserGroupUserMap ugum ON ug.groupId = ugum.groupId " +
           "WHERE ugum.userId = :userId AND ug.isDeleted = false")
    List<com.example.SpringApi.Models.DatabaseModels.UserGroup> findUserGroups(@Param("userId") Long userId);

    /**
     * Optimized query to fetch user with all related data in ONE single database call.
     * Uses LEFT JOIN FETCH to eagerly load address, permissions, and user groups.
     * This reduces database calls from 4 to 1, fetching:
     * - User details
     * - Primary address (via addressId)
     * - All permissions through UserClientPermissionMapping and Permission
     * - All user groups through UserGroupUserMap and UserGroup
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.primaryAddress " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.userId = :userId")
    Optional<User> findByIdWithAllRelations(@Param("userId") Long userId);

    /**
     * Optimized query to fetch user by email with all related data in ONE single database call.
     * Uses LEFT JOIN FETCH to eagerly load address, permissions, and user groups.
     * This reduces database calls from 4 to 1, fetching:
     * - User details
     * - Primary address (via addressId)
     * - All permissions through UserClientPermissionMapping and Permission
     * - All user groups through UserGroupUserMap and UserGroup
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.primaryAddress " +
           "LEFT JOIN FETCH u.userClientPermissionMappings ucpm " +
           "LEFT JOIN FETCH ucpm.permission p " +
           "LEFT JOIN FETCH u.userGroupMappings ugm " +
           "LEFT JOIN FETCH ugm.userGroup ug " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithAllRelations(@Param("email") String email);
}
