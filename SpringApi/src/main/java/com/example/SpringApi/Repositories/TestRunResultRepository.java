package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.TestRunResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for TestRunResult entity. Provides methods for querying individual test results. */
@Repository
public interface TestRunResultRepository extends JpaRepository<TestRunResult, Long> {

  /** Find all results for a test run */
  List<TestRunResult> findByTestRunIdOrderByExecutedAtAsc(Long testRunId);

  /** Find results by service name */
  List<TestRunResult> findByClientIdAndServiceNameOrderByExecutedAtDesc(
      Long clientId, String serviceName);

  /** Find results by test method name */
  List<TestRunResult> findByClientIdAndTestMethodNameOrderByExecutedAtDesc(
      Long clientId, String testMethodName);

  /** Find results by status */
  List<TestRunResult> findByClientIdAndStatusOrderByExecutedAtDesc(Long clientId, String status);

  /** Find failed results for a client */
  @Query(
      "SELECT trr FROM TestRunResult trr WHERE trr.clientId = :clientId "
          + "AND trr.status IN ('FAILED', 'ERROR') ORDER BY trr.executedAt DESC")
  List<TestRunResult> findFailedResultsForClient(@Param("clientId") Long clientId);

  /** Count results by status for a test run */
  @Query(
      "SELECT trr.status, COUNT(trr) FROM TestRunResult trr WHERE trr.testRunId = :testRunId "
          + "GROUP BY trr.status")
  List<Object[]> countByStatusForTestRun(@Param("testRunId") Long testRunId);

  /** Find results for a specific service method */
  List<TestRunResult> findByClientIdAndServiceNameAndMethodNameOrderByExecutedAtDesc(
      Long clientId, String serviceName, String methodName);

  /** Get average duration for a test method */
  @Query(
      "SELECT AVG(trr.durationMs) FROM TestRunResult trr "
          + "WHERE trr.clientId = :clientId AND trr.testMethodName = :testMethodName")
  Double getAverageDurationForTestMethod(
      @Param("clientId") Long clientId, @Param("testMethodName") String testMethodName);
}

