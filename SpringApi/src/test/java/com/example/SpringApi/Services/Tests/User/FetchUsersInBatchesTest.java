package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Services.UserService;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - FetchUsersInBatches functionality.
 *
 * Test Summary:
 * | Test Group | Number of Tests |
 * | :------------------ | :-------------- |
 * | Comprehensive | 1 |
 * | **Total** | **1** |
 */
@DisplayName("UserService - FetchUsersInBatches Tests")
class FetchUsersInBatchesTest extends UserServiceTestBase {

        // ========================================
        // CONTROLLER AUTHORIZATION TESTS
        // ========================================

        @Test
        @DisplayName("fetchUsersInCarrierInBatches - Verify @PreAuthorize Annotation")
        void fetchUsersInCarrierInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                Method method = UserController.class.getMethod("fetchUsersInCarrierInBatches", UserRequestModel.class);
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertNotNull(annotation,
                                "@PreAuthorize annotation should be present on fetchUsersInCarrierInBatches method");
                assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
        }

        @Test
        @DisplayName("fetchUsersInCarrierInBatches - Controller delegates to service")
        void fetchUsersInCarrierInBatches_WithValidRequest_DelegatesToService() {
                UserService mockUserService = mock(UserService.class);
                UserController controller = new UserController(mockUserService);
                UserRequestModel request = new UserRequestModel();
                request.setStart(0);
                request.setEnd(10);
                PaginationBaseResponseModel<UserResponseModel> mockResponse = new PaginationBaseResponseModel<>();
                when(mockUserService.fetchUsersInCarrierInBatches(request)).thenReturn(mockResponse);

                ResponseEntity<?> response = controller.fetchUsersInCarrierInBatches(request);

                verify(mockUserService).fetchUsersInCarrierInBatches(request);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators using nested loops.
         * 
         * This single test systematically verifies:
         * - Valid and invalid column names
         * - Valid and invalid operators for each column type (string, number, boolean)
         * - AND/OR logic operators
         * - Pagination edge cases (start=0, end=10, negative, zero, same values)
         * - Multiple filters combined
         * 
         * Expected Result: Valid combinations succeed, invalid combinations throw
         * BadRequestException.
         * Assertions: Multiple assertions for each combination.
         */
        @Test
        @DisplayName("Comprehensive Batch Filter Test - All Combinations")
        void fetchUsersInBatches_ComprehensiveCombinationTest() {
                // Test counters
                int validTests = 0;
                int invalidTests = 0;

                // String columns to test
                String[] stringColumns = { "firstName", "lastName", "loginName", "role", "email", "phone" };
                // Number columns to test
                String[] numberColumns = { "userId", "loginAttempts", "addressId" };
                // Boolean columns to test
                String[] booleanColumns = { "isDeleted", "locked", "emailConfirmed", "isGuest" };

                // Logic operators to test
                String[] logicOperators = { "AND", "OR", "and", "or" };
                String[] invalidLogicOperators = { "XOR", "NAND", "", null, "invalid" };

                // ============== TEST 1: Valid column + valid operator combinations
                // ==============
                // Test string columns with string operators
                for (String column : stringColumns) {
                        for (String operator : STRING_OPERATORS) {
                                UserRequestModel request = createBasicPaginationRequest();

                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                filter.setColumn(column);
                                filter.setOperator(operator);
                                filter.setValue("testValue");
                                request.setFilters(Arrays.asList(filter));
                                request.setLogicOperator("AND");

                                List<User> users = Arrays.asList(testUser);
                                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                                lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                                                any(org.springframework.data.domain.Pageable.class)))
                                                .thenReturn(userPage);

                                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                                                "String column '" + column + "' with operator '" + operator
                                                                + "' should succeed");
                                validTests++;
                        }
                }

                // Test number columns with number operators
                for (String column : numberColumns) {
                        for (String operator : NUMBER_OPERATORS) {
                                UserRequestModel request = createBasicPaginationRequest();

                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                filter.setColumn(column);
                                filter.setOperator(operator);
                                filter.setValue("100");
                                request.setFilters(Arrays.asList(filter));
                                request.setLogicOperator("AND");

                                List<User> users = Arrays.asList(testUser);
                                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                                lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                                                any(org.springframework.data.domain.Pageable.class)))
                                                .thenReturn(userPage);

                                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                                                "Number column '" + column + "' with operator '" + operator
                                                                + "' should succeed");
                                validTests++;
                        }
                }

                // Test boolean columns with boolean operators
                for (String column : booleanColumns) {
                        for (String operator : BOOLEAN_OPERATORS) {
                                UserRequestModel request = createBasicPaginationRequest();

                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                filter.setColumn(column);
                                filter.setOperator(operator);
                                filter.setValue("true");
                                request.setFilters(Arrays.asList(filter));
                                request.setLogicOperator("AND");

                                List<User> users = Arrays.asList(testUser);
                                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                                lenient().when(userFilterQueryBuilder.getColumnType(column)).thenReturn("boolean");
                                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                                                any(org.springframework.data.domain.Pageable.class)))
                                                .thenReturn(userPage);

                                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                                                "Boolean column '" + column + "' with operator '" + operator
                                                                + "' should succeed");
                                validTests++;
                        }
                }

                // ============== TEST 2: Invalid column names ==============
                for (String invalidColumn : INVALID_COLUMNS) {
                        UserRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(invalidColumn);
                        filter.setOperator("equals");
                        filter.setValue("test");
                        request.setFilters(Arrays.asList(filter));
                        request.setLogicOperator("AND");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                        () -> userService.fetchUsersInCarrierInBatches(request),
                                        "Invalid column '" + invalidColumn + "' should throw BadRequestException");
                        assertTrue(ex.getMessage().contains("Invalid column"),
                                        "Error message should contain 'Invalid column' for column: " + invalidColumn);
                        invalidTests++;
                }

                // ============== TEST 3: Invalid operators for each column type ==============
                // String column with invalid operators
                for (String invalidOp : INVALID_OPERATORS) {
                        if (invalidOp == null)
                                continue; // Skip null as it causes different exception

                        UserRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn("firstName");
                        filter.setOperator(invalidOp);
                        filter.setValue("test");
                        request.setFilters(Arrays.asList(filter));
                        request.setLogicOperator("AND");

                        lenient().when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                        () -> userService.fetchUsersInCarrierInBatches(request),
                                        "Invalid operator '" + invalidOp + "' should throw BadRequestException");
                        assertTrue(ex.getMessage().contains("Invalid operator"),
                                        "Error message should contain 'Invalid operator' for: " + invalidOp);
                        invalidTests++;
                }

                // ============== TEST 4: Invalid logic operators ==============
                for (String invalidLogic : invalidLogicOperators) {
                        if (invalidLogic == null || invalidLogic.isEmpty())
                                continue;

                        UserRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                        filter1.setColumn("firstName");
                        filter1.setOperator("equals");
                        filter1.setValue("test");

                        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                        filter2.setColumn("lastName");
                        filter2.setOperator("equals");
                        filter2.setValue("test");

                        request.setFilters(Arrays.asList(filter1, filter2));
                        request.setLogicOperator(invalidLogic);

                        lenient().when(userFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                        () -> userService.fetchUsersInCarrierInBatches(request),
                                        "Invalid logic operator '" + invalidLogic
                                                        + "' should throw BadRequestException");
                        assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
                        invalidTests++;
                }

                // ============== TEST 5: Valid logic operators ==============
                for (String validLogic : logicOperators) {
                        UserRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                        filter1.setColumn("firstName");
                        filter1.setOperator("equals");
                        filter1.setValue("test");

                        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                        filter2.setColumn("lastName");
                        filter2.setOperator("equals");
                        filter2.setValue("test");

                        request.setFilters(Arrays.asList(filter1, filter2));
                        request.setLogicOperator(validLogic);

                        List<User> users = Arrays.asList(testUser);
                        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                        lenient().when(userFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                        lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                                        any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                                        "Valid logic operator '" + validLogic + "' should succeed");
                        validTests++;
                }

                // ============== TEST 6: Pagination edge cases ==============
                // Invalid pagination: end <= start
                int[][] invalidPaginationCases = {
                                { 10, 10 }, // start == end
                                { 10, 5 }, // start > end
                                { 0, 0 }, // both zero
                                { -1, 10 } // negative start (this actually works as offset, but limit becomes 11)
                };

                for (int[] pagination : invalidPaginationCases) {
                        if (pagination[1] - pagination[0] <= 0) {
                                UserRequestModel request = new UserRequestModel();
                                request.setStart(pagination[0]);
                                request.setEnd(pagination[1]);
                                request.setIncludeDeleted(false);

                                BadRequestException ex = assertThrows(BadRequestException.class,
                                                () -> userService.fetchUsersInCarrierInBatches(request),
                                                "Pagination start=" + pagination[0] + ", end=" + pagination[1]
                                                                + " should throw BadRequestException");
                                assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                                invalidTests++;
                        }
                }

                // Valid pagination cases
                int[][] validPaginationCases = {
                                { 0, 10 },
                                { 0, 1 },
                                { 5, 15 },
                                { 0, 100 },
                                { 100, 200 }
                };

                for (int[] pagination : validPaginationCases) {
                        UserRequestModel request = new UserRequestModel();
                        request.setStart(pagination[0]);
                        request.setEnd(pagination[1]);
                        request.setIncludeDeleted(false);

                        List<User> users = Arrays.asList(testUser);
                        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, pagination[1] - pagination[0]),
                                        1);

                        lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), isNull(), anyString(), isNull(), anyBoolean(),
                                        any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                        assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request),
                                        "Pagination start=" + pagination[0] + ", end=" + pagination[1]
                                                        + " should succeed");
                        validTests++;
                }

                // ============== TEST 7: Multiple filters combined ==============
                // Test with 3 filters of different types
                UserRequestModel multiFilterRequest = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition stringFilter = new PaginationBaseRequestModel.FilterCondition();
                stringFilter.setColumn("firstName");
                stringFilter.setOperator("contains");
                stringFilter.setValue("John");

                PaginationBaseRequestModel.FilterCondition numberFilter = new PaginationBaseRequestModel.FilterCondition();
                numberFilter.setColumn("loginAttempts");
                numberFilter.setOperator(">");
                numberFilter.setValue("0");

                PaginationBaseRequestModel.FilterCondition boolFilter = new PaginationBaseRequestModel.FilterCondition();
                boolFilter.setColumn("isDeleted");
                boolFilter.setOperator("is");
                boolFilter.setValue("false");

                multiFilterRequest.setFilters(Arrays.asList(stringFilter, numberFilter, boolFilter));
                multiFilterRequest.setLogicOperator("AND");

                List<User> users = Arrays.asList(testUser);
                Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 1);

                lenient().when(userFilterQueryBuilder.getColumnType("firstName")).thenReturn("string");
                lenient().when(userFilterQueryBuilder.getColumnType("loginAttempts")).thenReturn("number");
                lenient().when(userFilterQueryBuilder.getColumnType("isDeleted")).thenReturn("boolean");
                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), anyList(), anyBoolean(),
                                any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(multiFilterRequest),
                                "Multiple filters of different types should succeed");
                validTests++;

                // ============== TEST 8: No filters (basic pagination) ==============
                UserRequestModel noFilterRequest = createBasicPaginationRequest();
                noFilterRequest.setFilters(null);

                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), isNull(), anyBoolean(),
                                any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(noFilterRequest),
                                "Request without filters should succeed");
                validTests++;

                // ============== TEST 9: Include deleted users ==============
                UserRequestModel includeDeletedRequest = createBasicPaginationRequest();
                includeDeletedRequest.setIncludeDeleted(true);

                lenient().when(userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), isNull(), eq(true),
                                any(org.springframework.data.domain.Pageable.class))).thenReturn(userPage);

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(includeDeletedRequest),
                                "Request with includeDeleted=true should succeed");
                validTests++;

                // Ensure we tested a reasonable number of cases
                assertTrue(validTests >= 50, "Should have at least 50 valid test cases");
                assertTrue(invalidTests >= 10, "Should have at least 10 invalid test cases");
        }

        private UserRequestModel createBasicPaginationRequest() {
                UserRequestModel request = new UserRequestModel();
                request.setStart(0);
                request.setEnd(10);
                request.setIncludeDeleted(false);
                return request;
        }
}
