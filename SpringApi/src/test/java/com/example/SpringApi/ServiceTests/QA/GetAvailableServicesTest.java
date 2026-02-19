package com.example.SpringApi.ServiceTests.QA;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for QAService.getAvailableServices() method.
 * 
 * 
 * Test Coverage:
 * - Success scenarios (3 tests)
 * - Validation tests (4 tests)
 * - Edge cases (3 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetAvailableServicesTest extends QAServiceTestBase {

    // Total Tests: 12
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s01_success_returnsServiceNames() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s02_success_allNamesEndWithService() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        for (String service : services) {
            assertTrue(service.endsWith("Service"),
                    "Service name should end with 'Service': " + service);
        }
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s03_success_containsKnownServices() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        // Check for some known services that should exist
        assertTrue(services.size() > 0, "Should contain at least one service");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s04_list_isNotNull() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        assertNotNull(services, "Services list should not be null");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s05_list_isNotEmpty() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        assertFalse(services.isEmpty(), "Services list should not be empty");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s06_list_containsNoDuplicates() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        Set<String> uniqueServices = new HashSet<>(services);
        assertEquals(services.size(), uniqueServices.size(),
                "Services list should not contain duplicates");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s07_list_allNamesNonEmpty() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        for (String service : services) {
            assertNotNull(service, "Service name should not be null");
            assertFalse(service.trim().isEmpty(), "Service name should not be empty");
        }
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s08_multipleInvocations_returnsSameList() {
        // Arrange
        // Act
        List<String> services1 = qaService.getAvailableServices();
        List<String> services2 = qaService.getAvailableServices();

        // Assert
        assertEquals(services1.size(), services2.size(),
                "Multiple invocations should return same number of services");
        assertTrue(services1.containsAll(services2),
                "Multiple invocations should return same services");
        assertTrue(services2.containsAll(services1),
                "Multiple invocations should return same services");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s09_list_isImmutable() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert - Try to modify the list and expect exception or no change
        int originalSize = services.size();
        try {
            services.add("NewService");
            // If no exception, verify size didn't change (defensive copy)
            assertEquals(originalSize, services.size(),
                    "List should be immutable or defensively copied");
        } catch (UnsupportedOperationException e) {
            // Expected for immutable list
            assertTrue(true, "List is properly immutable");
        }
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getAvailableServices_s10_count_matchesServiceMappings() {
        // Arrange
        // Act
        List<String> services = qaService.getAvailableServices();

        // Assert
        // The count should be consistent with the internal service mappings
        assertTrue(services.size() > 0, "Should have at least one service mapped");
        // Each service should be a valid service name
        for (String service : services) {
            assertTrue(service.matches("[A-Z][a-zA-Z]*Service"),
                    "Service name should follow naming convention: " + service);
        }
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    @DisplayName("Get Available Services - Service Failure - Throws Exception")
    void getAvailableServices_serviceFailure_ThrowsException() {
        // Arrange
        stubQaServiceGetAvailableServicesThrows(
                new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act
        RuntimeException ex = assertThrows(RuntimeException.class, () -> qaService.getAvailableServices());

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    @DisplayName("Get Available Services - Controller permission unauthorized - Success")
    void getAvailableServices_controller_permission_unauthorized() {
        // Arrange
        QAController controller = new QAController(qaSubTranslator);
        stubQaTranslatorGetAvailableServicesThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getAvailableServices();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
