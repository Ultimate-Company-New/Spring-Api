package springapi.modeltests.responsemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.databasemodels.LatestTestResult;
import springapi.models.responsemodels.QaResponseModel;

@DisplayName("QA Response Model Behavior Tests")
class QaResponseModelBehaviorTest {

  // Total Tests: 4

  /**
   * Purpose: Verify default constructor initializes baseline state for QA response. Expected
   * Result: Collections and counters are initialized to zero/default values. Assertions: Methods
   * list is non-null and all counters are zero.
   */
  @Test
  @DisplayName("qaResponseModel - DefaultConstructor Initializes State - Success")
  void qaResponseModel_s01_defaultConstructorInitializesState_success() {
    // Arrange
    QaResponseModel responseModel = new QaResponseModel();

    // Act
    List<QaResponseModel.MethodInfo> methods = responseModel.getMethods();

    // Assert
    assertNotNull(methods);
    assertTrue(methods.isEmpty());
    assertEquals(0, responseModel.getTotalMethods());
    assertEquals(0, responseModel.getMethodsWithCoverage());
    assertEquals(0, responseModel.getTotalTests());
    assertEquals(0.0, responseModel.getCoveragePercentage());
  }

  /**
   * Purpose: Verify adding methods updates aggregate totals and coverage metrics. Expected Result:
   * Method and test counters update based on per-method coverage. Assertions: Totals and coverage
   * percentage are recalculated correctly.
   */
  @Test
  @DisplayName("qaResponseModel - AddMethod Recalculates CoverageMetrics - Success")
  void qaResponseModel_s02_addMethodRecalculatesCoverageMetrics_success() {
    // Arrange
    QaResponseModel responseModel =
        new QaResponseModel("UserService", "UserController", "/api/User", "UserServiceTest");

    QaResponseModel.MethodInfo coveredMethod =
        new QaResponseModel.MethodInfo("getUsers", "GET", "/api/User/get", "Get users");
    coveredMethod.addUnitTest("getUsers_success", "Get Users - Success");
    coveredMethod.addUnitTest("getUsers_empty", "Get Users - Empty");

    QaResponseModel.MethodInfo uncoveredMethod =
        new QaResponseModel.MethodInfo("createUser", "POST", "/api/User/create", "Create user");

    // Act
    responseModel.addMethod(coveredMethod);
    responseModel.addMethod(uncoveredMethod);

    // Assert
    assertEquals(2, responseModel.getTotalMethods());
    assertEquals(1, responseModel.getMethodsWithCoverage());
    assertEquals(2, responseModel.getTotalTests());
    assertEquals(50.0, responseModel.getCoveragePercentage());
  }

  /**
   * Purpose: Verify MethodInfo unit-test addition ignores blank entries and supports bulk add.
   * Expected Result: Only valid test entries are counted. Assertions: testCount and hasCoverage
   * reflect valid additions only.
   */
  @Test
  @DisplayName("qaResponseModel - MethodInfo AddUnitTests Filters InvalidEntries - Success")
  void qaResponseModel_s03_methodInfoAddUnitTestsFiltersInvalidEntries_success() {
    // Arrange
    QaResponseModel.MethodInfo methodInfo = new QaResponseModel.MethodInfo();
    QaResponseModel.TestInfo validOne = new QaResponseModel.TestInfo("method_success", "Display 1");
    QaResponseModel.TestInfo validTwo = new QaResponseModel.TestInfo("method_failure", "Display 2");
    QaResponseModel.TestInfo invalidBlank = new QaResponseModel.TestInfo("   ", "Invalid");

    // Act
    methodInfo.addUnitTest((QaResponseModel.TestInfo) null);
    methodInfo.addUnitTest(invalidBlank);
    methodInfo.addUnitTests(List.of(validOne, validTwo));

    // Assert
    assertEquals(2, methodInfo.getTestCount());
    assertTrue(methodInfo.isHasCoverage());
    assertEquals(2, methodInfo.getAssociatedUnitTests().size());
  }

  /**
   * Purpose: Verify TestInfo can be hydrated from LatestTestResult and handles null safely.
   * Expected Result: Last-run fields populate when result exists and remain unchanged for null.
   * Assertions: Run metadata fields match LatestTestResult values.
   */
  @Test
  @DisplayName("qaResponseModel - TestInfo PopulateFromLatestResult MapsFields - Success")
  void qaResponseModel_s04_testInfoPopulateFromLatestResultMapsFields_success() {
    // Arrange
    QaResponseModel.TestInfo testInfo = new QaResponseModel.TestInfo("method_success");
    LatestTestResult latestResult = new LatestTestResult();
    LocalDateTime runAt = LocalDateTime.of(2026, 2, 28, 12, 30);
    latestResult.setStatus("PASSED");
    latestResult.setLastRunAt(runAt);
    latestResult.setLastRunByUserName("qa.user");
    latestResult.setLastRunByUserId(77L);
    latestResult.setDurationMs(321);
    latestResult.setErrorMessage(null);
    latestResult.setStackTrace(null);

    // Act
    testInfo.populateFromLatestResult(latestResult);
    testInfo.populateFromLatestResult(null);

    // Assert
    assertTrue(testInfo.isHasBeenRun());
    assertEquals("PASSED", testInfo.getLastRunStatus());
    assertEquals(runAt, testInfo.getLastRunAt());
    assertEquals("qa.user", testInfo.getLastRunByUserName());
    assertEquals(77L, testInfo.getLastRunByUserId());
    assertEquals(321, testInfo.getLastRunDurationMs());
    assertFalse(
        testInfo.getLastRunErrorMessage() != null && !testInfo.getLastRunErrorMessage().isEmpty());
  }
}
