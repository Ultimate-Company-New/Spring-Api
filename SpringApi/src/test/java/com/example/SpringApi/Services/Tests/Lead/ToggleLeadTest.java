package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.toggleLead() method.
 * Tests lead deletion toggle functionality.
 * * Test Count: 11 tests
 */
@DisplayName("Toggle Lead Tests")
class ToggleLeadTest extends LeadServiceTestBase {

    // Total Tests: 11

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify multiple toggles correctly switch state back and forth.
     * Expected Result: State transition follows: active -> deleted -> active.
     * Assertions: State flips correct and save is called twice.
     */
    @Test
    @DisplayName("Toggle Lead - Multiple Toggles - State changes correctly")
    void toggleLead_MultipleToggles_Success() {
        // Arrange
        testLead.setIsDeleted(false);
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
        stubLeadRepositorySave(testLead);

        // Act - First toggle: false -> true
        leadService.toggleLead(DEFAULT_LEAD_ID);
        assertTrue(testLead.getIsDeleted());

        // Act - Second toggle: true -> false
        testLead.setIsDeleted(true);
        leadService.toggleLead(DEFAULT_LEAD_ID);
        assertFalse(testLead.getIsDeleted());

        // Assert
        verify(leadRepository, times(2)).save(testLead);
    }

    /**
     * Purpose: Verify successful toggle of a lead from active to deleted status.
     * Expected Result: isDeleted property is set to true.
     * Assertions: Final deleted state is true and save is invoked.
     */
    @Test
    @DisplayName("Toggle Lead - Success")
    void toggleLead_Success_Success() {
        // Arrange
        testLead.setIsDeleted(false);
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
        stubLeadRepositorySave(testLead);

        // Act
        leadService.toggleLead(DEFAULT_LEAD_ID);

        // Assert
        assertTrue(testLead.getIsDeleted());
        verify(leadRepository).save(testLead);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject toggle attempts using the maximum possible long ID if not
     * found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Toggle Lead - Max Long ID - ThrowsNotFoundException")
    void toggleLead_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(Long.MAX_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts using the minimum possible long ID if not
     * found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Toggle Lead - Min Long ID - ThrowsNotFoundException")
    void toggleLead_MinLongId_ThrowsNotFoundException() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(Long.MIN_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(Long.MIN_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for negative lead IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Toggle Lead - Negative ID - ThrowsNotFoundException")
    void toggleLead_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(-1L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts when the lead ID does not exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Toggle Lead - NotFound - ThrowsNotFoundException")
    void toggleLead_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubLeadRepositoryFindByIdIncludingDeletedAny(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(DEFAULT_LEAD_ID));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject zero as a lead ID for toggle.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Toggle Lead - Zero ID - ThrowsNotFoundException")
    void toggleLead_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(0L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify forbidden access is handled at the controller level.
     * Expected Result: Forbidden status is returned.
     * Assertions: Response status is 403 FORBIDDEN.
     */
    @Test
    @DisplayName("Toggle Lead - Controller permission forbidden - Success")
    void toggleLead_controller_permission_forbidden() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceToggleLeadThrowsForbidden(DEFAULT_LEAD_ID);

        // Act
        ResponseEntity<?> response = controller.toggleLead(DEFAULT_LEAD_ID);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("Toggle Lead - Controller permission unauthorized - Success")
    void toggleLead_controller_permission_unauthorized() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceToggleLeadThrowsUnauthorized(DEFAULT_LEAD_ID);

        // Act
        ResponseEntity<?> response = controller.toggleLead(DEFAULT_LEAD_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on toggleLead method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references TOGGLE_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Toggle Lead - Verify @PreAuthorize annotation is configured correctly")
    void toggleLead_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange - Use reflection to verify the @PreAuthorize annotation is present
        var method = LeadController.class.getMethod("toggleLead",
                Long.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "toggleLead method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.TOGGLE_LEADS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference TOGGLE_LEADS_PERMISSION");
    }

    /**
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     * 
     * Note: This test simulates the happy path assuming authorization has already
     * passed.
     * Actual @PreAuthorize enforcement is handled by Spring Security AOP and tested
     * in end-to-end tests.
     */
    @Test
    @DisplayName("Toggle Lead - Controller delegates to service correctly")
    void toggleLead_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceToggleLeadDoNothing(DEFAULT_LEAD_ID);

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.toggleLead(DEFAULT_LEAD_ID);

        // Assert - Verify service was called and correct response returned
        verify(leadServiceMock, times(1)).toggleLead(DEFAULT_LEAD_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}
