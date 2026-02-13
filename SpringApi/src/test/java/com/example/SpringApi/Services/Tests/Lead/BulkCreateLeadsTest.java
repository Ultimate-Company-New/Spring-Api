package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;

import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.bulkCreateLeads() method.
 * Tests bulk creation of leads with various validation scenarios.
 * * Test Count: 16 tests
 */
@DisplayName("Bulk Create Leads Tests")
class BulkCreateLeadsTest extends LeadServiceTestBase {


    // Total Tests: 16
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify bulk creation succeeds when all provided leads are valid.
     * Expected Result: Result model indicates 100% success rate.
     * Assertions: Total requested, success count, and repository save count match.
     */
    @Test
    @DisplayName("Bulk Create Leads - All Valid - Success")
    void bulkCreateLeads_AllValid_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
            leadReq.setEmail("bulklead" + i + "@test.com");
            leadReq.setFirstName("BulkFirst" + i);
            leadReq.setLastName("BulkLast" + i);
            leadReq.setPhone("555000000" + i);
            leads.add(leadReq);
        }

        stubLeadRepositorySaveAssignsId();
        stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead();
        stubUserLogServiceLogData(true);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getTotalRequested());
        assertEquals(5, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(leadRepository, times(5)).save(any(Lead.class));
    }

    /**
     * Purpose: Verify system handles large batches of leads successfully.
     * Expected Result: System processes all 50 leads without error.
     * Assertions: Result counts match the large input list size.
     */
    @Test
    @DisplayName("Bulk Create Leads - Many Leads - Success")
    void bulkCreateLeads_ManyLeads_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
            leadReq.setEmail("bulklead" + i + "@test.com");
            leadReq.setFirstName("First" + i);
            leadReq.setLastName("Last" + i);
            leadReq.setPhone("555" + String.format("%07d", i));
            leads.add(leadReq);
        }

        stubLeadRepositorySaveAssignsId();
        stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead();
        stubUserLogServiceLogData(true);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getTotalRequested());
        assertEquals(50, result.getSuccessCount());
        verify(leadRepository, times(50)).save(any(Lead.class));
    }

    /**
     * Purpose: Verify results for mixed valid and invalid leads.
     * Expected Result: Valid leads are saved, invalid leads are reported as
     * failures.
     * Assertions: Success count and failure count reflect the split input.
     */
    @Test
    @DisplayName("Bulk Create Leads - Mixed Valid And Invalid - PartialSuccess")
    void bulkCreateLeads_MixedValidAndInvalid_PartialSuccess() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        LeadRequestModel valid1 = createValidLeadRequest(null, TEST_CLIENT_ID);
        valid1.setEmail("valid1@test.com");
        leads.add(valid1);

        LeadRequestModel invalid1 = createValidLeadRequest(null, TEST_CLIENT_ID);
        invalid1.setEmail("bademail");
        leads.add(invalid1);

        LeadRequestModel valid2 = createValidLeadRequest(null, TEST_CLIENT_ID);
        valid2.setEmail("valid2@test.com");
        leads.add(valid2);

        LeadRequestModel invalid2 = createValidLeadRequest(null, TEST_CLIENT_ID);
        invalid2.setPhone("");
        leads.add(invalid2);

        stubLeadRepositorySaveAssignsId();
        stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead();
        stubUserLogServiceLogData(true);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(2, result.getFailureCount());
    }

    /**
     * Purpose: Verify results for partial success scenarios with three valid leads.
     * Expected Result: Three successes and one failure reported.
     * Assertions: Counts match expectations based on manual validity setup.
     */
    @Test
    @DisplayName("Bulk Create Leads - Partial Success")
    void bulkCreateLeads_PartialSuccess_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LeadRequestModel validLead = createValidLeadRequest(null, TEST_CLIENT_ID);
            validLead.setEmail("validlead" + i + "@test.com");
            leads.add(validLead);
        }
        LeadRequestModel invalidLead1 = createValidLeadRequest(null, TEST_CLIENT_ID);
        invalidLead1.setEmail("");
        leads.add(invalidLead1);

        stubLeadRepositorySaveAssignsId();
        stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead();
        stubUserLogServiceLogData(true);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertEquals(4, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Verify creation of a single lead via the bulk creation method.
     * Expected Result: Single lead is saved successfully.
     * Assertions: Total requested is 1 and success count is 1.
     */
    @Test
    @DisplayName("Bulk Create Leads - Single Lead - Success")
    void bulkCreateLeads_SingleLead_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
        leads.add(leadReq);

        stubLeadRepositorySaveAssignsId();
        stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead();
        stubUserLogServiceLogData(true);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
    }

    /**
     * Purpose: Verify logging is performed during bulk create operations.
     * Expected Result: User log entries are created for the process.
     * Assertions: logData is invoked at least once.
     */
    @Test
    @DisplayName("Bulk Create Leads - Verify Logging Called")
    void bulkCreateLeads_VerifyLoggingCalled_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        leads.add(createValidLeadRequest(null, TEST_CLIENT_ID));
        stubLeadRepositorySaveAssignsId();
        stubUserLogServiceLogData(true);

        // Act
        leadService.bulkCreateLeads(leads);

        // Assert
        verify(userLogService, atLeastOnce()).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify notification logic is skipped for synchronous bulk creation.
     * Expected Result: Message service is never called for context messages.
     * Assertions: createMessageWithContext call count is zero.
     */
    @Test
    @DisplayName("Bulk Create Leads - Verify Message Notification Sent")
    void bulkCreateLeads_VerifyMessageNotification_Success() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        leads.add(createValidLeadRequest(null, TEST_CLIENT_ID));
        stubLeadRepositorySaveAssignsId();

        // Act
        leadService.bulkCreateLeads(leads);

        // Assert
        verify(messageService, never()).createMessageWithContext(any(), anyLong(), anyString(), anyLong());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify result when all provided leads in the batch are invalid.
     * Expected Result: Result model indicates 100% failure rate.
     * Assertions: Success count is zero and repository save is never called.
     */
    @Test
    @DisplayName("Bulk Create Leads - All Invalid - AllFail")
    void bulkCreateLeads_AllInvalid_AllFail() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            LeadRequestModel invalidLead = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalidLead.setEmail("");
            leads.add(invalidLead);
        }

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertEquals(3, result.getFailureCount());
        verify(leadRepository, never()).save(any(Lead.class));
    }

    /**
     * Purpose: Reject empty lead list for bulk operations.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Leads - Empty List - ThrowsBadRequestException")
    void bulkCreateLeads_EmptyList_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.bulkCreateLeads(new ArrayList<>()));
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Lead"), ex.getMessage());
    }

    /**
     * Purpose: Reject invalid email format within a batch.
     * Expected Result: Specific lead is marked as failed in the result set.
     * Assertions: Failure count is incremented.
     */
    @Test
    @DisplayName("Bulk Create Leads - Invalid Email Format - Fails")
    void bulkCreateLeads_InvalidEmailFormat_Fails() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        LeadRequestModel invalidLead = createValidLeadRequest(null, TEST_CLIENT_ID);
        invalidLead.setEmail("invalid-email-format");
        leads.add(invalidLead);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject leads missing required fields in a bulk batch.
     * Expected Result: Leads with missing fields are reported as failures.
     * Assertions: Repository save is skipped for invalid entries.
     */
    @Test
    @DisplayName("Bulk Create Leads - Missing Required Fields - Fails")
    void bulkCreateLeads_MissingRequiredFields_Fails() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        LeadRequestModel noFirstName = createValidLeadRequest(null, TEST_CLIENT_ID);
        noFirstName.setFirstName(null);
        leads.add(noFirstName);

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert
        assertEquals(1, result.getFailureCount());
        verify(leadRepository, never()).save(any(Lead.class));
    }

    /**
     * Purpose: Reject null lead list for bulk operations.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Leads - Null List - ThrowsBadRequestException")
    void bulkCreateLeads_NullList_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.bulkCreateLeads(null));
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Lead"), ex.getMessage());
    }

    /**
     * Purpose: If some leads in batch fail validation/repository, service handles
     * appropriately.
     * Given: Batch of 5 leads, 1 invalid; stub repository to handle this
     * When: bulkCreateLeads is called
     * Then: Returned result contains failure entry for invalid lead
     */
    @Test
    @DisplayName("Bulk Create Leads - Partial Failure Handling")
    void bulkCreateLeads_unit_partialFailure_rollsBackOrReports() {
        // Arrange
        List<LeadRequestModel> leads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
            leadReq.setEmail("bulklead" + i + "@test.com");
            leadReq.setFirstName("BulkFirst" + i);
            leads.add(leadReq);
        }
        // Make one invalid
        leads.get(2).setEmail(null);

        stubLeadRepositorySaveAssignsId();

        // Act
        var result = leadService.bulkCreateLeads(leads);

        // Assert - verify at least one failure reported
        assertTrue(result.getFailureCount() >= 1, "Expected at least one failure in batch");
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on bulkCreateLeads
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references INSERT_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Bulk Create Leads - Verify @PreAuthorize annotation is configured correctly")
    void bulkCreateLeads_p01_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        var method = LeadController.class.getMethod("bulkCreateLeads",
                java.util.List.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "bulkCreateLeads method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.INSERT_LEADS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference INSERT_LEADS_PERMISSION");
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
    @DisplayName("Bulk Create Leads - Controller delegates to service correctly")
    void bulkCreateLeads_p02_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        List<LeadRequestModel> leads = new ArrayList<>();
        leads.add(createValidLeadRequest(null, TEST_CLIENT_ID));

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.bulkCreateLeads(leads);

        // Assert - Verify correct response returned (async method is fire-and-forget)
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");

        // Check if asynchronous method was called on the service
        // Since we are using a mock, we can verify this call
        // Note: bulkCreateLeads calls bulkCreateLeadsAsync
        verify(leadServiceMock).bulkCreateLeadsAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("Bulk Create Leads - Controller permission unauthorized - Success")
    void bulkCreateLeads_p03_controller_permission_unauthorized() {
        // Arrange
        stubLeadServiceBulkCreateLeadsAsyncThrowsUnauthorized();
        LeadController controller = new LeadController(leadServiceMock);
        List<LeadRequestModel> leads = new ArrayList<>();

        // Act
        ResponseEntity<?> response = controller.bulkCreateLeads(leads);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
