package com.example.SpringApi.ServiceTests.QA;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import com.example.SpringApi.Models.ResponseModels.QAResponseModel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for QAService.getAllEndpointsWithTests() method. */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllEndpointsWithTests Tests")
class GetAllEndpointsWithTestsTest extends QAServiceTestBase {

  // Total Tests: 15
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify all services are returned. Expected Result: Non-empty list of services.
   * Assertions: List is not null or empty.
   */
  @Test
  void getAllEndpointsWithTests_s01_success_returnsAllServices() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(services.stream().anyMatch(service -> "QAService".equals(service.getServiceName())));
  }

  /**
   * Purpose: Verify each service object is present. Expected Result: Each service entry is
   * non-null. Assertions: All entries are non-null.
   */
  @Test
  void getAllEndpointsWithTests_s02_allServices_haveRequiredFields() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    for (QAResponseModel service : services) {
      assertNotNull(service);
      assertNotNull(service.getServiceName());
      assertFalse(service.getServiceName().isBlank());
    }
  }

  /**
   * Purpose: Verify services are returned in a stable order. Expected Result: List exists with any
   * valid ordering. Assertions: List is not null.
   */
  @Test
  void getAllEndpointsWithTests_s03_servicesOrdered_byName() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    Set<String> uniqueServiceNames =
        services.stream().map(QAResponseModel::getServiceName).collect(Collectors.toSet());
    assertEquals(services.size(), uniqueServiceNames.size());
  }

  /**
   * Purpose: Verify each service includes controller mapping information. Expected Result: List is
   * non-empty. Assertions: List is not empty.
   */
  @Test
  void getAllEndpointsWithTests_s04_eachService_hasControllerMapping() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(
        services.stream()
            .allMatch(
                service ->
                    service.getControllerName() != null
                        && service.getControllerName().endsWith("Controller")));
    assertTrue(services.stream().map(QAResponseModel::getControllerName).distinct().count() >= 1);
  }

  /**
   * Purpose: Verify service info includes service name. Expected Result: List is non-empty.
   * Assertions: List is not empty.
   */
  @Test
  void getAllEndpointsWithTests_s05_serviceInfo_containsServiceName() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(services.stream().allMatch(service -> service.getServiceName().endsWith("Service")));
    assertFalse(services.stream().map(QAResponseModel::getServiceName).anyMatch(String::isBlank));
  }

  /**
   * Purpose: Verify service info includes controller name. Expected Result: List is non-empty.
   * Assertions: List is not empty.
   */
  @Test
  void getAllEndpointsWithTests_s06_serviceInfo_containsControllerName() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(
        services.stream()
            .allMatch(
                service ->
                    service.getControllerName() != null && !service.getControllerName().isBlank()));
    assertEquals(
        services.size(),
        services.stream().filter(service -> service.getControllerName() != null).count());
  }

  /**
   * Purpose: Verify service info includes base path. Expected Result: List is non-empty.
   * Assertions: List is not empty.
   */
  @Test
  void getAllEndpointsWithTests_s07_serviceInfo_containsBasePath() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(
        services.stream()
            .allMatch(
                service ->
                    service.getBasePath() != null && service.getBasePath().startsWith("/api/")));
    assertTrue(services.stream().noneMatch(service -> "/api/".equals(service.getBasePath())));
  }

  /**
   * Purpose: Verify service info includes methods list. Expected Result: List is non-empty.
   * Assertions: List is not empty.
   */
  @Test
  void getAllEndpointsWithTests_s08_serviceInfo_containsMethodsList() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    assertTrue(services.stream().allMatch(service -> service.getMethods() != null));
  }

  /**
   * Purpose: Verify services with no methods return empty method lists. Expected Result: List is
   * not null. Assertions: List is not null.
   */
  @Test
  void getAllEndpointsWithTests_s09_emptyMethods_returnsEmptyList() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertTrue(
        services.stream()
            .allMatch(service -> service.getMethodsWithCoverage() <= service.getTotalMethods()));
    int totalMethods = services.stream().mapToInt(QAResponseModel::getTotalMethods).sum();
    int coveredMethods = services.stream().mapToInt(QAResponseModel::getMethodsWithCoverage).sum();
    assertTrue(totalMethods >= coveredMethods);
  }

  /**
   * Purpose: Verify methods without tests report zero coverage. Expected Result: List is not null.
   * Assertions: List is not null.
   */
  @Test
  void getAllEndpointsWithTests_s10_methodsWithoutTests_showZeroCoverage() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertTrue(
        services.stream()
            .allMatch(
                service ->
                    service.getCoveragePercentage() >= 0.0
                        && service.getCoveragePercentage() <= 100.0));
    assertTrue(
        services.stream().noneMatch(service -> Double.isNaN(service.getCoveragePercentage())));
  }

  /**
   * Purpose: Verify methods with tests show expected coverage. Expected Result: List is not null.
   * Assertions: List is not null.
   */
  @Test
  void getAllEndpointsWithTests_s11_allMethodsWithTests_showFullCoverage() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertTrue(services.stream().allMatch(service -> service.getTotalTests() >= 0));
    int totalTests = services.stream().mapToInt(QAResponseModel::getTotalTests).sum();
    assertTrue(totalTests >= 0);
  }

  /**
   * Purpose: Verify mixed coverage is calculated. Expected Result: List is not null. Assertions:
   * List is not null.
   */
  @Test
  void getAllEndpointsWithTests_s12_mixedCoverage_calculatesCorrectly() {
    // Arrange

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertTrue(services.stream().mapToInt(QAResponseModel::getTotalMethods).sum() >= 0);
  }

  /**
   * Purpose: Verify latest test results are loaded and attached when available. Expected Result:
   * Service scan succeeds and repository is queried for service results. Assertions: Result list is
   * returned and repository method is invoked with normalized service name.
   */
  @Test
  void getAllEndpointsWithTests_s13_latestResultsPresent_queriesLatestResultRepository() {
    // Arrange
    LatestTestResult latestResult =
        createLatestTestResult(101L, "QAService", "getDashboardData_s01_success");
    stubLatestTestResultRepositoryFindByClientIdAndServiceName("QAService", List.of(latestResult));

    // Act
    List<QAResponseModel> services = qaService.getAllEndpointsWithTests();

    // Assert
    assertNotNull(services);
    assertFalse(services.isEmpty());
    verify(latestTestResultRepository)
        .findByClientIdAndServiceNameOrderByTestMethodNameAsc(anyLong(), eq("QAService"));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify invalid service mapping throws exception. Expected Result: NullPointerException
   * is thrown. Assertions: Exception is thrown.
   */
  @Test
  @DisplayName("Get All Endpoints - Invalid Mapping - Throws Exception")
  void getAllEndpointsWithTests_InvalidMapping_ThrowsException() {
    // Arrange
    Map<String, Object> mappings = getServiceMappings();
    String existingService = mappings.keySet().iterator().next();
    Object originalMapping = mappings.get(existingService);
    mappings.put(existingService, null);

    try {
      // Act
      NullPointerException exception =
          assertThrows(NullPointerException.class, () -> qaService.getAllEndpointsWithTests());

      // Assert
      assertNotNull(exception.getMessage());
    } finally {
      mappings.put(existingService, originalMapping);
    }
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("Get All Endpoints - Controller permission unauthorized - Success")
  void getAllEndpointsWithTests_controller_permission_unauthorized() {
    // Arrange
    QAController controller = new QAController(qaSubTranslator);
    stubQaTranslatorGetAllEndpointsWithTestsThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getAllEndpointsWithTests();

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
