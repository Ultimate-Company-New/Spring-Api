package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Fetch User Groups In Batches functionality.
 * 
 * Contains 1 comprehensive test covering:
 * - All valid column and operator combinations for string, number, and boolean fields
 * - Invalid column names and logic operators
 * - Pagination edge cases
 * - Basic pagination without filters
 * 
 * This single test validates 30+ valid cases and 5+ invalid cases through nested loops.
 */
@DisplayName("UserGroupService - FetchUserGroupsInBatches Tests")
public class FetchUserGroupsInBatchesTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserGroupsInBatches - Verify @PreAuthorize Annotation")
    void getUserGroupsInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserGroupController.class.getMethod("getUserGroupsInBatches", UserGroupRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserGroupsInBatches method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("getUserGroupsInBatches - Controller delegates to service")
    @SuppressWarnings("unchecked")
    void getUserGroupsInBatches_WithValidRequest_DelegatesToService() {
        UserGroupController controller = new UserGroupController(userGroupService);
        UserGroupRequestModel request = new UserGroupRequestModel();
        PaginationBaseResponseModel mockResponse = new PaginationBaseResponseModel<>();
        when(userGroupService.fetchUserGroupsInClientInBatches(request)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getUserGroupsInBatches(request);

        verify(userGroupService).fetchUserGroupsInClientInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Purpose: Comprehensive test covering all combinations of filters, operators,
     * columns, pagination, and logic operators using nested loops.
     */
    @Test
    @DisplayName("Comprehensive Batch Filter Test - All Combinations")
    void fetchUserGroupsInBatches_ComprehensiveCombinationTest() {
        int validTests = 0;
        int invalidTests = 0;

        String[] logicOperators = {"AND", "OR", "and", "or"};
        String[] invalidLogicOperators = {"XOR", "NAND", "invalid"};
        String[] invalidColumns = {"invalidColumn", "xyz", "!@#$"};

        // ============== TEST 1: Valid column + valid operator combinations ==============
        for (String column : STRING_COLUMNS) {
            for (String operator : STRING_OPERATORS) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue("testValue");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "String column '" + column + "' with operator '" + operator + "' should succeed");
                validTests++;
            }
        }

        // Test number columns
        for (String column : NUMBER_COLUMNS) {
            for (String operator : NUMBER_OPERATORS) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue("100");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Number column '" + column + "' with operator '" + operator + "' should succeed");
                validTests++;
            }
        }

        // Test boolean columns
        for (String column : BOOLEAN_COLUMNS) {
            for (String operator : BOOLEAN_OPERATORS) {
                UserGroupRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue("true");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

                lenient().when(userGroupFilterQueryBuilder.getColumnType(column)).thenReturn("boolean");
                lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Boolean column '" + column + "' with operator '" + operator + "' should succeed");
                validTests++;
            }
        }

        // ============== TEST 2: Invalid column names ==============
        for (String invalidColumn : invalidColumns) {
            UserGroupRequestModel request = createBasicPaginationRequest();

            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn(invalidColumn);
            filter.setOperator("equals");
            filter.setValue("test");
            request.setFilters(Arrays.asList(filter));
            request.setLogicOperator("AND");

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                    "Invalid column '" + invalidColumn + "' should throw BadRequestException");
            assertTrue(ex.getMessage().contains("Invalid column name"));
            invalidTests++;
        }

        // ============== TEST 3: Invalid logic operators ==============
        for (String invalidLogic : invalidLogicOperators) {
            UserGroupRequestModel request = createBasicPaginationRequest();

            PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
            filter1.setColumn("groupName");
            filter1.setOperator("equals");
            filter1.setValue("test");

            PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
            filter2.setColumn("description");
            filter2.setOperator("equals");
            filter2.setValue("test");

            request.setFilters(Arrays.asList(filter1, filter2));
            request.setLogicOperator(invalidLogic);

            lenient().when(userGroupFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                    "Invalid logic operator '" + invalidLogic + "' should throw BadRequestException");
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
            invalidTests++;
        }

        // ============== TEST 4: Valid logic operators ==============
        for (String validLogic : logicOperators) {
            UserGroupRequestModel request = createBasicPaginationRequest();

            PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
            filter1.setColumn("groupName");
            filter1.setOperator("equals");
            filter1.setValue("test");

            PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
            filter2.setColumn("description");
            filter2.setOperator("equals");
            filter2.setValue("test");

            request.setFilters(Arrays.asList(filter1, filter2));
            request.setLogicOperator(validLogic);

            Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);

            lenient().when(userGroupFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
            lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);

            assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request),
                    "Valid logic operator '" + validLogic + "' should succeed");
            validTests++;
        }

        // ============== TEST 5: Pagination edge cases ==============
        int[][] invalidPaginationCases = {{10, 10}, {10, 5}, {0, 0}};

        for (int[] pagination : invalidPaginationCases) {
            if (pagination[1] - pagination[0] <= 0) {
                UserGroupRequestModel request = new UserGroupRequestModel();
                request.setStart(pagination[0]);
                request.setEnd(pagination[1]);
                request.setIncludeDeleted(false);

                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> userGroupService.fetchUserGroupsInClientInBatches(request),
                        "Pagination start=" + pagination[0] + ", end=" + pagination[1] + " should throw");
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                invalidTests++;
            }
        }

        // ============== TEST 6: No filters (basic pagination) ==============
        UserGroupRequestModel noFilterRequest = createBasicPaginationRequest();
        noFilterRequest.setFilters(null);

        Page<UserGroup> page = new PageImpl<>(Arrays.asList(testUserGroup), PageRequest.of(0, 10), 1);
        lenient().when(userGroupFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), isNull(), anyBoolean(), any(Pageable.class))).thenReturn(page);

        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(noFilterRequest));
        validTests++;

        System.out.println("Comprehensive UserGroup Batch Filter Test Summary:");
        System.out.println("  Valid test cases passed: " + validTests);
        System.out.println("  Invalid test cases (expected failures): " + invalidTests);

        assertTrue(validTests >= 30, "Should have at least 30 valid test cases");
        assertTrue(invalidTests >= 5, "Should have at least 5 invalid test cases");
    }

    private UserGroupRequestModel createBasicPaginationRequest() {
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setIncludeDeleted(false);
        return request;
    }
}
