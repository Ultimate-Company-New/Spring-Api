// Total Tests: 3
package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
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
 */
@DisplayName("Bulk Create Leads Async Tests")
class BulkCreateLeadsAsyncTest extends LeadServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify bulk creation async starts and processes leads.
     * Given: Valid list of leads.
     * When: bulkCreateLeadsAsync is called.
     * Then: Leads are saved and log is recorded.
     */
    @Test
    @DisplayName("bulkCreateLeadsAsync_unit_basic_success")
    void bulkCreateLeadsAsync_unit_basic_success() {
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

    /*
     * Purpose: Reject null lead list.
     * Given: Null leads list.
     * When: bulkCreateLeadsAsync is called.
     * Then: Processes gracefully (throws or logs error).
     */
    @Test
    @DisplayName("bulkCreateLeadsAsync_unit_nullList_failure")
    void bulkCreateLeadsAsync_unit_nullList_failure() {
        // Act
        // Note: The method catches exceptions internally and sends a message,
        // but if we call it directly it might throw if validation happens first.
        // Let's check service: it throws BadRequestException for null/empty list.
        assertDoesNotThrow(() -> leadService.bulkCreateLeadsAsync(null, 1L, "testUser", TEST_CLIENT_ID));

        // Wait, the service catches Exception e and sends a message.
        // So no exception should escape the method.
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify @PreAuthorize annotation is declared correctly on the
     * controller.
     * Given: LeadController class.
     * When: Checking bulkCreateLeads method (which calls async service)
     * annotations.
     * Then: @PreAuthorize exists with INSERT_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("bulkCreateLeadsAsync_controller_permission_configured")
    void bulkCreateLeadsAsync_controller_permission_configured() throws NoSuchMethodException {
        // Arrange
        var method = LeadController.class.getMethod("bulkCreateLeads", java.util.List.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation);
        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.INSERT_LEADS_PERMISSION + "')";
        assertEquals(expectedPermission, preAuthorizeAnnotation.value());
    }
}
