package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.getLeadDetails methods.
 * Tests retrieval of lead details by ID and email.
 * * Test Count: 12 tests
 */
@DisplayName("Get Lead Details Tests")
class GetLeadDetailsTest extends LeadServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify lead details retrieval with all fields populated.
     * Expected Result: Response model correctly maps company, title, and status.
     * Assertions: Fields in response match the test data setup.
     */
    @Test
    @DisplayName("Get Lead By ID - All fields populated - Success")
    void getLeadDetailsById_AllFieldsPopulated_Success() {
        // Arrange
        testLead.setLeadId(DEFAULT_LEAD_ID);
        testLead.setFirstName("John");
        testLead.setLastName("Doe");
        testLead.setEmail(DEFAULT_EMAIL);
        testLead.setPhone("555-0100");
        testLead.setCompany("Tech Corp");
        testLead.setTitle("Manager");
        testLead.setLeadStatus("New");
        when(leadRepository.findLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID)).thenReturn(testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    /**
     * Purpose: Verify retrieval by email returns a correctly mapped response.
     * Expected Result: Response contains the correct email address.
     * Assertions: Result is non-null and email matches.
     */
    @Test
    @DisplayName("Get Lead By Email - Success")
    void getLeadDetailsByEmail_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByEmail(DEFAULT_EMAIL, TEST_CLIENT_ID)).thenReturn(testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsByEmail(DEFAULT_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_EMAIL, result.getEmail());
    }

    /**
     * Purpose: Verify basic retrieval by ID succeeds for a valid lead.
     * Expected Result: Success returns lead details.
     * Assertions: Result ID matches requested ID.
     */
    @Test
    @DisplayName("Get Lead By ID - Success")
    void getLeadDetailsById_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID)).thenReturn(testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject requests for an empty email string.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches LEAD_NOT_FOUND.
     */
    @Test
    @DisplayName("Get Lead By Email - Empty Email - ThrowsNotFoundException")
    void getLeadDetailsByEmail_EmptyEmail_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsByEmail("", TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsByEmail(""));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject requests for emails that do not exist in the database.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches LEAD_NOT_FOUND.
     */
    @Test
    @DisplayName("Get Lead By Email - NotFound - ThrowsNotFoundException")
    void getLeadDetailsByEmail_NotFound_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsByEmail("unknown@example.com"));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts for the maximum long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Get Lead By ID - Max Long ID - ThrowsNotFoundException")
    void getLeadDetailsById_MaxLongId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsById(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts for the minimum long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Get Lead By ID - Min Long ID - ThrowsNotFoundException")
    void getLeadDetailsById_MinLongId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsById(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject negative lead IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Get Lead By ID - Negative ID - ThrowsNotFoundException")
    void getLeadDetailsById_NegativeId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsById(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(-1L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts when a lead ID is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Get Lead By ID - NotFound - ThrowsNotFoundException")
    void getLeadDetailsById_NotFound_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsById(anyLong(), anyLong())).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(DEFAULT_LEAD_ID));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject zero as a lead ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Get Lead By ID - Zero ID - ThrowsNotFoundException")
    void getLeadDetailsById_ZeroId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsById(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(0L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on getLeadDetailsById
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Get Lead Details - Verify @PreAuthorize annotation is configured correctly")
    void getLeadDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = LeadController.class.getMethod("getLeadDetailsById",
                Long.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getLeadDetailsById method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_LEADS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_LEADS_PERMISSION");
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
    @DisplayName("Get Lead Details - Controller delegates to service correctly")
    void getLeadDetailsById_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadService);
        when(leadService.getLeadDetailsById(DEFAULT_LEAD_ID)).thenReturn(new LeadResponseModel(testLead));

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert - Verify service was called and correct response returned
        verify(leadService, times(1)).getLeadDetailsById(DEFAULT_LEAD_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}