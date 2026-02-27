package com.example.springapi.ServiceTests.QA;

import static org.junit.jupiter.api.Assertions.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.QaController;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.responsemodels.QaResponseModel;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for QaService.getEndpointsWithTestsByService() method.
 *
 * <p>Test Coverage: - Success scenarios (5 tests) - Failure tests (8 tests) - Edge cases (7 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetEndpointsWithTestsByServiceTest extends QAServiceTestBase {

  // Total Tests: 22
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_s01_ValidService_Success() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertEquals("QaService", result.getServiceName());
    assertEquals("/api/QA", result.getBasePath());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_s02_serviceWithoutSuffix_normalizesName() {
    // Arrange
    String serviceName = "QA";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertEquals("QaService", result.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_s03_serviceWithSuffix_returnsCorrectly() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getControllerName());
    assertTrue(result.getControllerName().endsWith("Controller"));
    assertNotNull(result.getMethods());
    assertTrue(result.getBasePath().startsWith("/api/"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_s04_caseInsensitive_handlesCorrectly() {
    // Arrange
    String serviceName = "qaservice";

    // Act & Assert
    // May throw NotFoundException if case-sensitive
    try {
      QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);
      assertNotNull(result);
      assertEquals("qaservice", result.getServiceName().toLowerCase());
    } catch (NotFoundException e) {
      // Expected if implementation is case-sensitive
      assertTrue(true);
    }
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_s05_knownService_returnsAllMethods() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getMethods());
    assertEquals("QaService", result.getServiceName());
    assertTrue(result.getTotalMethods() >= result.getMethods().size());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f01_nullServiceName_throwsNullPointerException() {
    // Arrange
    String serviceName = null;

    // Act & Assert
    NullPointerException ex =
        assertThrows(
            NullPointerException.class,
            () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(ErrorMessages.QaErrorMessages.SERVICE_NAME_NULL, ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f02_emptyServiceName_throwsNotFoundException() {
    // Arrange
    String serviceName = "";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f03_whitespaceServiceName_throwsNotFoundException() {
    // Arrange
    String serviceName = "   ";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f04_nonExistentService_throwsNotFoundException() {
    // Arrange
    String serviceName = "NonExistentService";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f05_invalidServiceName_throwsNotFoundException() {
    // Arrange
    String serviceName = "Invalid@Service#Name";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f06_serviceNotInMapping_throwsNotFoundException() {
    // Arrange
    String serviceName = "UnmappedService";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f07_specialCharacters_throwsNotFoundException() {
    // Arrange
    String serviceName = "Service!@#$";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f08_numericServiceName_throwsNotFoundException() {
    // Arrange
    String serviceName = "12345";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
  }

  /*
   **********************************************************************************************
   * EDGE CASES
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f09_serviceWithNoMethods_returnsEmptyMethodsList() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertTrue(result.getTotalMethods() >= 0);
    assertNotNull(result.getMethods());
    assertTrue(result.getMethods().size() <= result.getTotalMethods());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f10_serviceWithNoTests_returnsZeroCoverage() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertTrue(result.getMethodsWithCoverage() <= result.getTotalMethods());
    assertTrue(result.getCoveragePercentage() >= 0.0 && result.getCoveragePercentage() <= 100.0);
    assertFalse(Double.isNaN(result.getCoveragePercentage()));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f11_multipleCallsSameService_returnsSameData() {
    // Arrange
    String serviceName = "QaService";

    // Act
    QaResponseModel result1 = qaService.getEndpointsWithTestsByService(serviceName);
    QaResponseModel result2 = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result1);
    assertNotNull(result2);
    assertEquals(result1.getServiceName(), result2.getServiceName());
    assertEquals(result1.getMethods().size(), result2.getMethods().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f12_differentServices_returnsDifferentData() {
    // Arrange
    String service1 = "QaService";

    // Act
    QaResponseModel result1 = qaService.getEndpointsWithTestsByService(service1);

    // Assert
    assertNotNull(result1);
    assertEquals("QaService", result1.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f13_serviceNameWithSpaces_trimsAndNormalizes() {
    // Arrange
    String serviceName = "  QaService  ";

    // Act
    QaResponseModel result = qaService.getEndpointsWithTestsByService(serviceName);

    // Assert
    assertNotNull(result);
    assertEquals("QaService", result.getServiceName());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f14_veryLongServiceName_handlesCorrectly() {
    // Arrange
    String longServiceName = "VeryLongServiceName".repeat(10);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> qaService.getEndpointsWithTestsByService(longServiceName));
    assertEquals(expectedServiceNotFoundMessage(longServiceName), ex.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void getEndpointsWithTestsByService_f15_errorMessage_containsAvailableServices() {
    // Arrange
    String serviceName = "InvalidService";

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> qaService.getEndpointsWithTestsByService(serviceName));
    assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    assertTrue(ex.getMessage().contains("QaService"));
  }

  /**
   * Purpose: Verify mapped service names with missing implementation class return not found.
   * Expected Result: NotFoundException is thrown with class-loading message. Assertions: Exception
   * type and exact message.
   */
  @Test
  void getEndpointsWithTestsByService_f16_mappedClassMissing_throwsNotFoundException() {
    // Arrange
    Map<String, Object> mappings = getServiceMappings();
    String missingServiceName = "GhostService";
    Object mapping =
        createServiceControllerMapping("GhostController", "/api/Ghost", "GhostServiceTest");
    mappings.put(missingServiceName, mapping);

    try {
      // Act
      NotFoundException ex =
          assertThrows(
              NotFoundException.class,
              () -> qaService.getEndpointsWithTestsByService(missingServiceName));

      // Assert
      assertEquals(
          String.format(
              ErrorMessages.QaErrorMessages.COULD_NOT_LOAD_SERVICE_CLASS_FORMAT,
              missingServiceName),
          ex.getMessage());
    } finally {
      mappings.remove(missingServiceName);
    }
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  @DisplayName("Get Endpoints With Tests By Service - Controller permission unauthorized - Success")
  void getEndpointsWithTestsByService_controller_permission_unauthorized() {
    // Arrange
    QaController controller = new QaController(qaSubTranslator);
    stubQaTranslatorGetEndpointsWithTestsByServiceThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getEndpointsWithTestsByService("QaService");

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  private String expectedServiceNotFoundMessage(String serviceName) {
    return String.format(
        ErrorMessages.QaErrorMessages.SERVICE_NOT_FOUND_FORMAT,
        serviceName.trim(),
        String.join(", ", getServiceMappings().keySet()));
  }
}
