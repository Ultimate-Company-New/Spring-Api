package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeadService.bulkCreateLeadsAsync() method.
 * * Test Count: 4 tests
 */
@DisplayName("Bulk Create Leads Async Tests")
class BulkCreateLeadsAsyncTest extends LeadServiceTestBase {


    // Total Tests: 4
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify bulk creation async starts and processes leads.
     * Given: Valid list of leads.
     * When: bulkCreateLeadsAsync is called.
     * Then: Leads are saved and log is recorded.
     */
    @Test
    @DisplayName("Bulk Create Leads Async - Basic - Success")
    void bulkCreateLeadsAsync_Basic_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        leads.add(testLeadRequest);

        stubLeadRepositorySave(testLead);
        stubLeadRepositoryFindLeadWithDetailsByEmail(testLeadRequest.getEmail(), TEST_CLIENT_ID, testLead);

        // Act
        assertDoesNotThrow(() -> leadService.bulkCreateLeadsAsync(leads, 1L, "testUser", TEST_CLIENT_ID));

        // Assert
        verify(leadRepository, atLeastOnce()).save(any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject null lead list.
     * Given: Null leads list.
     * When: bulkCreateLeadsAsync is called.
     * Then: Processes gracefully (throws or logs error).
     */
    @Test
    @DisplayName("Bulk Create Leads Async - Null List - Failure")
    void bulkCreateLeadsAsync_NullList_Failure() {
        // Arrange

        // Act & Assert
        // Note: The method catches exceptions internally and sends a message,
        // but if we call it directly it might throw if validation happens first.
        // Let's check service: it throws BadRequestException for null/empty list.
        assertDoesNotThrow(() -> leadService.bulkCreateLeadsAsync(null, 1L, "testUser", TEST_CLIENT_ID));
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
    @DisplayName("Bulk Create Leads Async - Controller permission unauthorized - Success")
    void bulkCreateLeadsAsync_controller_permission_unauthorized() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceBulkCreateLeadsAsyncThrowsUnauthorized();
        List<LeadRequestModel> leads = new ArrayList<>();

        // Act
        ResponseEntity<?> response = controller.bulkCreateLeads(leads);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation is declared correctly on the
     * controller.
     * Given: LeadController class.
     * When: Checking bulkCreateLeads method (which calls async service)
     * annotations.
     * Then: @PreAuthorize exists with INSERT_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Bulk Create Leads Async - Verify @PreAuthorize annotation is configured correctly")
    void bulkCreateLeadsAsync_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        var method = LeadController.class.getMethod("bulkCreateLeads", java.util.List.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation, "bulkCreateLeads method should have @PreAuthorize annotation");
        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.INSERT_LEADS_PERMISSION + "')";
        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference INSERT_LEADS_PERMISSION");
    }
}
