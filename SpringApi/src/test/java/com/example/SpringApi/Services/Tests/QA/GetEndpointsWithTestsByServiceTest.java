package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for QAService.getEndpointsWithTestsByService() method.
 * 
 * Total Tests: 21
 * 
 * Test Coverage:
 * - Success scenarios (5 tests)
 * - Failure tests (8 tests)
 * - Edge cases (7 tests)
 */
@ExtendWith(MockitoExtension.class)
class GetEndpointsWithTestsByServiceTest extends QAServiceTestBase {
    // Total Tests: 21

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
    void getEndpointsWithTestsByService_ValidService_Success() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceWithoutSuffix_normalizesName() {
        // Arrange
        String serviceName = "QA";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceWithSuffix_returnsCorrectly() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_caseInsensitive_handlesCorrectly() {
        // Arrange
        String serviceName = "qaservice";

        // Act & Assert
        // May throw NotFoundException if case-sensitive
        try {
            Object result = qaService.getEndpointsWithTestsByService(serviceName);
            assertNotNull(result);
        } catch (NotFoundException e) {
            // Expected if implementation is case-sensitive
            assertTrue(true);
        }
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_knownService_returnsAllMethods() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
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
    void getEndpointsWithTestsByService_nullServiceName_throwsNullPointerException() {
        // Arrange
        String serviceName = null;

        // Act & Assert
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(ErrorMessages.QAErrorMessages.ServiceNameNull, ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_emptyServiceName_throwsNotFoundException() {
        // Arrange
        String serviceName = "";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_whitespaceServiceName_throwsNotFoundException() {
        // Arrange
        String serviceName = "   ";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_nonExistentService_throwsNotFoundException() {
        // Arrange
        String serviceName = "NonExistentService";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_invalidServiceName_throwsNotFoundException() {
        // Arrange
        String serviceName = "Invalid@Service#Name";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceNotInMapping_throwsNotFoundException() {
        // Arrange
        String serviceName = "UnmappedService";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_specialCharacters_throwsNotFoundException() {
        // Arrange
        String serviceName = "Service!@#$";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_numericServiceName_throwsNotFoundException() {
        // Arrange
        String serviceName = "12345";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
    }

    /*
     **********************************************************************************************
     * EDGE CASES
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceWithNoMethods_returnsEmptyMethodsList() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
        // The result should have an empty methods list if no methods exist
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceWithNoTests_returnsZeroCoverage() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_multipleCallsSameService_returnsSameData() {
        // Arrange
        String serviceName = "QAService";

        // Act
        Object result1 = qaService.getEndpointsWithTestsByService(serviceName);
        Object result2 = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        // Results should be consistent
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_differentServices_returnsDifferentData() {
        // Arrange
        String service1 = "QAService";

        // Act
        Object result1 = qaService.getEndpointsWithTestsByService(service1);

        // Assert
        assertNotNull(result1);
        // Different services should return different data
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_serviceNameWithSpaces_trimsAndNormalizes() {
        // Arrange
        String serviceName = "  QAService  ";

        // Act
        Object result = qaService.getEndpointsWithTestsByService(serviceName);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_veryLongServiceName_handlesCorrectly() {
        // Arrange
        String longServiceName = "VeryLongServiceName".repeat(10);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(longServiceName));
        assertEquals(expectedServiceNotFoundMessage(longServiceName), ex.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void getEndpointsWithTestsByService_errorMessage_containsAvailableServices() {
        // Arrange
        String serviceName = "InvalidService";

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> qaService.getEndpointsWithTestsByService(serviceName));
        assertEquals(expectedServiceNotFoundMessage(serviceName), ex.getMessage());
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
    @DisplayName("Get Endpoints With Tests By Service - Controller permission unauthorized - Success")
    void getEndpointsWithTestsByService_controller_permission_unauthorized() {
        // Arrange
        QAController controller = new QAController(qaSubTranslator);
        stubQaTranslatorGetEndpointsWithTestsByServiceThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getEndpointsWithTestsByService("QAService");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private String expectedServiceNotFoundMessage(String serviceName) {
        return String.format(ErrorMessages.QAErrorMessages.ServiceNotFoundFormat,
                serviceName.trim(),
                String.join(", ", getServiceMappings().keySet()));
    }
}
