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
}
