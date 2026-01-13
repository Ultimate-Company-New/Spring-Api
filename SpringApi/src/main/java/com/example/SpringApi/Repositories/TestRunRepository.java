package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TestRun entity.
 * Provides methods for querying test run batch data.
 */
@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {

    /**
     * Find all test runs for a client, ordered by creation date descending
     */
    List<TestRun> findByClientIdOrderByCreatedDateDesc(Long clientId);

    /**
     * Find test runs for a specific service
     */
    List<TestRun> findByClientIdAndServiceNameOrderByCreatedDateDesc(Long clientId, String serviceName);

    /**
     * Find the latest test run for a service
     */
    Optional<TestRun> findFirstByClientIdAndServiceNameOrderByCreatedDateDesc(Long clientId, String serviceName);

    /**
     * Find test runs by status
     */
    List<TestRun> findByClientIdAndStatusOrderByCreatedDateDesc(Long clientId, String status);

    /**
     * Find test runs by user
     */
    List<TestRun> findByClientIdAndUserIdOrderByCreatedDateDesc(Long clientId, Long userId);

    /**
     * Find test runs within a date range
     */
    @Query("SELECT tr FROM TestRun tr WHERE tr.clientId = :clientId " +
           "AND tr.createdDate >= :startDate AND tr.createdDate <= :endDate " +
           "ORDER BY tr.createdDate DESC")
    List<TestRun> findByClientIdAndDateRange(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get count of test runs by status for a client
     */
    @Query("SELECT tr.status, COUNT(tr) FROM TestRun tr WHERE tr.clientId = :clientId " +
           "GROUP BY tr.status")
    List<Object[]> countByStatusForClient(@Param("clientId") Long clientId);

    /**
     * Find the most recent N test runs for a client
     */
    @Query("SELECT tr FROM TestRun tr WHERE tr.clientId = :clientId " +
           "ORDER BY tr.createdDate DESC LIMIT :limit")
    List<TestRun> findRecentTestRuns(@Param("clientId") Long clientId, @Param("limit") int limit);
}
