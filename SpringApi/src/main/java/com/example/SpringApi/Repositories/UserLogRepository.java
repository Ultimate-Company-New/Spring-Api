package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.UserLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    @Query("select ul from UserLog ul " +
            "where ul.userId = :userId and ul.clientId = :clientId " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'action' THEN CONCAT(ul.action, '') " +
            "WHEN 'description' THEN CONCAT(ul.description, '') " +
            "WHEN 'logLevel' THEN CONCAT(ul.logLevel, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<UserLog> findPaginatedUserLogs(@Param("userId") long userId,
                                        @Param("clientId") long clientId,
                                        @Param("columnName") String columnName,
                                        @Param("condition") String condition,
                                        @Param("filterExpr") String filterExpr,
                                        Pageable pageable);
}
