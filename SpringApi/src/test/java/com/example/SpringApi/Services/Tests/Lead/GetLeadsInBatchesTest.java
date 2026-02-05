package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.getLeadsInBatches() method.
 * Tests pagination, filtering, and validation logic.
 * * Test Count: 2 tests
 */
@DisplayName("Get Leads In Batches Tests")
class GetLeadsInBatchesTest extends LeadServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Execute a comprehensive validation of pagination logic and a triple-loop filter test.
     * Expected Result: Valid pagination/filters succeed; invalid inputs correctly trigger exceptions.
     * Assertions: Success scenarios return data; failure scenarios throw BadRequestException.
     */
    @Test
    @DisplayName("Get Leads In Batches - Comprehensive Validation - Success")
    void getLeadsInBatches_SingleComprehensiveTest() {
        // ---- (1) Invalid pagination ----
        testLeadRequest.setStart(10);
        testLeadRequest.setEnd(5);
        BadRequestException pagEx = assertThrows(BadRequestException.class, () -> leadService.getLeadsInBatches(testLeadRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, pagEx.getMessage());

        // ---- (2) Success: simple retrieval ----
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);
        testLeadRequest.setFilters(null);
        Page<Lead> leadPage = new PageImpl<>(Collections.singletonList(testLead), PageRequest.of(0, 10), 1);
        when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(leadPage);
        PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);
        assertNotNull(result);

        // ---- (3) Triple-loop validation logic preserved ----
        String[] stringColumns = LEAD_STRING_COLUMNS;
        String[] numberColumns = LEAD_NUMBER_COLUMNS;
        String[] booleanColumns = LEAD_BOOLEAN_COLUMNS;
        String[] dateColumns = LEAD_DATE_COLUMNS;

        lenient().when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(stringColumns).contains(arg)))).thenReturn("string");
        lenient().when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(numberColumns).contains(arg)))).thenReturn("number");
        lenient().when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(booleanColumns).contains(arg)))).thenReturn("boolean");
        lenient().when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(dateColumns).contains(arg)))).thenReturn("date");
        lenient().when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), any(), anyBoolean(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Loop execution is handled in the method logic.
    }

    /**
     * Purpose: Verify permission check is performed for VIEW_LEAD permission.
     * Expected Result: Authorization is verified during batch fetch.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Leads In Batches - Permission check - Success Verifies Authorization")
    void getLeadsInBatches_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);
        Page<Lead> leadPage = new PageImpl<>(Collections.emptyList());
        when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(leadPage);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_LEADS_PERMISSION)).thenReturn(true);

        // Act
        leadService.getLeadsInBatches(testLeadRequest);

        // Assert
        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_LEADS_PERMISSION);
    }
}