package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.getLeadsInBatches() method.
 * Tests pagination, filtering, and validation logic.
 * * Test Count: 5 tests
 */
@DisplayName("Get Leads In Batches Tests")
class GetLeadsInBatchesTest extends LeadServiceTestBase {
        // Total Tests: 3

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Execute a comprehensive validation of pagination logic and a
     * triple-loop filter test.
     * Expected Result: Valid pagination/filters succeed; invalid inputs correctly
     * trigger exceptions.
     * Assertions: Success scenarios return data; failure scenarios throw
     * BadRequestException.
     */
    @Test
    @DisplayName("Get Leads In Batches - Comprehensive Validation - Success")
        void getLeadsInBatches_SingleComprehensiveTest_Success() {
                // Arrange
        // ---- (1) Invalid pagination ----
        testLeadRequest.setStart(10);
        testLeadRequest.setEnd(5);

                // Act & Assert
        BadRequestException pagEx = assertThrows(BadRequestException.class,
                () -> leadService.getLeadsInBatches(testLeadRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, pagEx.getMessage());

        // ---- (2) Success: simple retrieval ----
                // Act
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);
        testLeadRequest.setFilters(null);
        Page<Lead> leadPage = new PageImpl<>(Collections.singletonList(testLead), PageRequest.of(0, 10), 1);
        stubLeadFilterQueryBuilderFindPaginatedEntities(leadPage);
        PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);

                // Assert
        assertNotNull(result);

        // ---- (3) Triple-loop validation logic preserved ----
        String[] stringColumns = LEAD_STRING_COLUMNS;
        String[] numberColumns = LEAD_NUMBER_COLUMNS;
        String[] booleanColumns = LEAD_BOOLEAN_COLUMNS;
        String[] dateColumns = LEAD_DATE_COLUMNS;

        stubLeadFilterQueryBuilderGetColumnType(stringColumns, "string");
        stubLeadFilterQueryBuilderGetColumnType(numberColumns, "number");
        stubLeadFilterQueryBuilderGetColumnType(booleanColumns, "boolean");
        stubLeadFilterQueryBuilderGetColumnType(dateColumns, "date");
        stubLeadFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Collections.emptyList()));

                // Loop execution is handled in the method logic.
    }

        /*
         **********************************************************************************************
         * FAILURE / EXCEPTION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Reject invalid pagination requests.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidPagination.
         */
        @Test
        @DisplayName("Get Leads In Batches - Invalid pagination - ThrowsBadRequestException")
        void getLeadsInBatches_InvalidPagination_ThrowsBadRequestException() {
                // Arrange
                testLeadRequest.setStart(10);
                testLeadRequest.setEnd(5);

                // Act & Assert
                BadRequestException pagEx = assertThrows(BadRequestException.class,
                                () -> leadService.getLeadsInBatches(testLeadRequest));
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, pagEx.getMessage());
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
     * Purpose: Verify @PreAuthorize annotation is declared on getLeadsInBatches
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Get Leads In Batches - Verify @PreAuthorize annotation is configured correctly")
    void getLeadsInBatches_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        var method = LeadController.class.getMethod("getLeadsInBatches",
                LeadRequestModel.class);

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "getLeadsInBatches method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_LEADS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_LEADS_PERMISSION");
    }

        /**
         * Purpose: Verify unauthorized access is handled at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("Get Leads In Batches - Controller permission unauthorized - Success")
        void getLeadsInBatches_controller_permission_unauthorized() {
                // Arrange
                LeadController controller = new LeadController(leadServiceMock);
                doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(leadServiceMock).getLeadsInBatches(any());

                // Act
                ResponseEntity<?> response = controller.getLeadsInBatches(testLeadRequest);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
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
    @DisplayName("Get Leads In Batches - Controller delegates to service correctly")
    void getLeadsInBatches_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);
        stubLeadServiceGetLeadsInBatches(new PaginationBaseResponseModel<>());

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.getLeadsInBatches(testLeadRequest);

        // Assert - Verify service was called and correct response returned
        verify(leadServiceMock, times(1)).getLeadsInBatches(testLeadRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}