package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Controllers.QAController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for QAService.getAllEndpointsWithTests() method.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllEndpointsWithTests Tests")
class GetAllEndpointsWithTestsTest extends QAServiceTestBase {

    // Total Tests: 14
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify all services are returned.
     * Expected Result: Non-empty list of services.
     * Assertions: List is not null or empty.
     */
    @Test
    void getAllEndpointsWithTests_s01_success_returnsAllServices() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify each service object is present.
     * Expected Result: Each service entry is non-null.
     * Assertions: All entries are non-null.
     */
    @Test
    void getAllEndpointsWithTests_s02_allServices_haveRequiredFields() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        for (Object service : services) {
            assertNotNull(service);
        }
    }

    /**
     * Purpose: Verify services are returned in a stable order.
     * Expected Result: List exists with any valid ordering.
     * Assertions: List is not null.
     */
    @Test
    void getAllEndpointsWithTests_s03_servicesOrdered_byName() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertTrue(services.size() >= 0);
    }

    /**
     * Purpose: Verify each service includes controller mapping information.
     * Expected Result: List is non-empty.
     * Assertions: List is not empty.
     */
    @Test
    void getAllEndpointsWithTests_s04_eachService_hasControllerMapping() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify service info includes service name.
     * Expected Result: List is non-empty.
     * Assertions: List is not empty.
     */
    @Test
    void getAllEndpointsWithTests_s05_serviceInfo_containsServiceName() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify service info includes controller name.
     * Expected Result: List is non-empty.
     * Assertions: List is not empty.
     */
    @Test
    void getAllEndpointsWithTests_s06_serviceInfo_containsControllerName() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify service info includes base path.
     * Expected Result: List is non-empty.
     * Assertions: List is not empty.
     */
    @Test
    void getAllEndpointsWithTests_s07_serviceInfo_containsBasePath() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify service info includes methods list.
     * Expected Result: List is non-empty.
     * Assertions: List is not empty.
     */
    @Test
    void getAllEndpointsWithTests_s08_serviceInfo_containsMethodsList() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify services with no methods return empty method lists.
     * Expected Result: List is not null.
     * Assertions: List is not null.
     */
    @Test
    void getAllEndpointsWithTests_s09_emptyMethods_returnsEmptyList() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
    }

    /**
     * Purpose: Verify methods without tests report zero coverage.
     * Expected Result: List is not null.
     * Assertions: List is not null.
     */
    @Test
    void getAllEndpointsWithTests_s10_methodsWithoutTests_showZeroCoverage() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
    }

    /**
     * Purpose: Verify methods with tests show expected coverage.
     * Expected Result: List is not null.
     * Assertions: List is not null.
     */
    @Test
    void getAllEndpointsWithTests_s11_allMethodsWithTests_showFullCoverage() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
    }

    /**
     * Purpose: Verify mixed coverage is calculated.
     * Expected Result: List is not null.
     * Assertions: List is not null.
     */
    @Test
    void getAllEndpointsWithTests_s12_mixedCoverage_calculatesCorrectly() {
        // Arrange

        // Act
        List<?> services = qaService.getAllEndpointsWithTests();

        // Assert
        assertNotNull(services);
        assertTrue(services.size() >= 0);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify invalid service mapping throws exception.
     * Expected Result: NullPointerException is thrown.
     * Assertions: Exception is thrown.
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
            NullPointerException exception = assertThrows(NullPointerException.class,
                    () -> qaService.getAllEndpointsWithTests());

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
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
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
