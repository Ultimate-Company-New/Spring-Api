package springapi.services.interfaces;

import java.util.List;
import java.util.Map;
import springapi.models.requestmodels.TestExecutionRequestModel;
import springapi.models.requestmodels.TestRunRequestModel;
import springapi.models.responsemodels.LatestTestResultResponseModel;
import springapi.models.responsemodels.QaDashboardResponseModel;
import springapi.models.responsemodels.QaResponseModel;
import springapi.models.responsemodels.TestExecutionStatusModel;
import springapi.models.responsemodels.TestRunResponseModel;

/**
 * Interface for QA Service operations.
 *
 * <p>This interface defines the contract for QA-related operations including endpoint-to-test
 * mapping, coverage statistics, service discovery, and test run tracking.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface QaSubTranslator {

  /**
   * Returns all QA dashboard data in a single response. Includes services with methods/tests,.
   * coverage summary, and available services.
   *
   * @return QaDashboardResponseModel with all dashboard data
   */
  QaDashboardResponseModel getDashboardData();

  /**
   * Returns a comprehensive list of all services with their public methods and associated unit.
   * tests.
   *
   * @return List of QaResponseModel with full endpoint-to-test mapping
   */
  List<QaResponseModel> getAllEndpointsWithTests();

  /**
   * Returns endpoint-to-test mapping for a specific service.
   *
   * @param serviceName The name of the service (e.g., "AddressService", "UserService")
   * @return QaResponseModel for the specified service
   */
  QaResponseModel getEndpointsWithTestsByService(String serviceName);

  /**
   * Returns a summary of test coverage across all services.
   *
   * @return Map containing coverage statistics including totalServices, totalMethods,
   *     methodsWithCoverage, totalTests, overallCoveragePercentage, and serviceBreakdown
   */
  Map<String, Object> getCoverageSummary();

  /**
   * Returns a list of all available service names.
   *
   * @return List of service names
   */
  List<String> getAvailableServices();

  /**
   * Saves a test run with its individual results. Also updates the LatestTestResult table for each.
   * test.
   *
   * @param request The test run request containing service info and results
   * @return TestRunResponseModel with the saved test run data
   */
  TestRunResponseModel saveTestRun(TestRunRequestModel request);

  /**
   * Returns the latest test results for a service or all services.
   *
   * @param serviceName The service name to filter by (null or empty for all services)
   * @return List of LatestTestResultResponseModel
   */
  List<LatestTestResultResponseModel> getLatestTestResults(String serviceName);

  // ==================== TEST EXECUTION METHODS ====================

  /**
   * Starts an async test execution. Returns immediately with status object containing executionId.
   * for polling.
   *
   * @param request The test execution request (determines scope: all, service, method, or specific
   *     tests)
   * @return TestExecutionStatusModel with executionId and initial status
   */
  TestExecutionStatusModel startTestExecution(TestExecutionRequestModel request);

  /**
   * Gets the current status and progress of a test execution.
   *
   * @param executionId The execution ID
   * @return TestExecutionStatusModel with current status and results
   */
  TestExecutionStatusModel getTestExecutionStatus(String executionId);
}
