package springapi.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.LatestTestResult;

/**
 * Repository for LatestTestResult entity. Provides methods for querying the latest test results.
 * summary.
 */
@Repository
public interface LatestTestResultRepository extends JpaRepository<LatestTestResult, Long> {

  /** Find all latest results for a client. */
  List<LatestTestResult> findByClientId(Long clientId);

  /** Find all latest results for a client ordered by service and method. */
  List<LatestTestResult> findByClientIdOrderByServiceNameAscTestMethodNameAsc(Long clientId);

  /** Find latest results for a specific service. */
  List<LatestTestResult> findByClientIdAndServiceNameOrderByTestMethodNameAsc(
      Long clientId, String serviceName);

  /** Find a specific test result by unique key. */
  Optional<LatestTestResult> findByClientIdAndServiceNameAndTestClassNameAndTestMethodName(
      Long clientId, String serviceName, String testClassName, String testMethodName);

  /** Find latest results by status. */
  List<LatestTestResult> findByClientIdAndStatusOrderByLastRunAtDesc(Long clientId, String status);

  /** Find failed tests for a client. */
  @Query(
      "SELECT ltr FROM LatestTestResult ltr WHERE ltr.clientId = :clientId "
          + "AND ltr.status IN ('FAILED', 'ERROR') ORDER BY ltr.lastRunAt DESC")
  List<LatestTestResult> findFailedTestsForClient(@Param("clientId") Long clientId);

  /** Count results by status for a client. */
  @Query(
      "SELECT ltr.status, COUNT(ltr) FROM LatestTestResult ltr WHERE ltr.clientId = :clientId "
          + "GROUP BY ltr.status")
  List<Object[]> countByStatusForClient(@Param("clientId") Long clientId);

  /** Count results by status for a service. */
  @Query(
      "SELECT ltr.status, COUNT(ltr) FROM LatestTestResult ltr "
          + "WHERE ltr.clientId = :clientId AND ltr.serviceName = :serviceName "
          + "GROUP BY ltr.status")
  List<Object[]> countByStatusForService(
      @Param("clientId") Long clientId, @Param("serviceName") String serviceName);

  /** Get summary statistics for a client. */
  @Query(
      "SELECT "
          + "COUNT(ltr) as total, "
          + "SUM(CASE WHEN ltr.status = 'PASSED' THEN 1 ELSE 0 END) as passed, "
          + "SUM(CASE WHEN ltr.status IN ('FAILED', 'ERROR') THEN 1 ELSE 0 END) as failed, "
          + "SUM(CASE WHEN ltr.status = 'NOT_RUN' THEN 1 ELSE 0 END) as notRun "
          + "FROM LatestTestResult ltr WHERE ltr.clientId = :clientId")
  Object[] getSummaryStatsForClient(@Param("clientId") Long clientId);

  /** Find tests run by a specific user. */
  List<LatestTestResult> findByClientIdAndLastRunByUserIdOrderByLastRunAtDesc(
      Long clientId, Long userId);

  /** Delete all results for a service (useful for cleanup). */
  void deleteByClientIdAndServiceName(Long clientId, String serviceName);
}
