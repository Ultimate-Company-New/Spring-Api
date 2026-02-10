package com.example.SpringApi.Services.Tests.UserLog;

import com.example.SpringApi.Services.UserLogService;

import com.example.SpringApi.Controllers.UserLogController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.fetchUserLogsInBatches method.
 * Tests: 1 (comprehensive combination test)
 */
@DisplayName("UserLogService - FetchUserLogsInBatches Tests")
class FetchUserLogsInBatchesTest extends UserLogServiceTestBase {

        // ========================================
        // CONTROLLER AUTHORIZATION TESTS
        // ========================================

        @Test
        @DisplayName("fetchUserLogsInBatches - Verify @PreAuthorize Annotation")
        void fetchUserLogsInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                Method method = UserLogController.class.getMethod("fetchUserLogsInBatches", UserLogsRequestModel.class);
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertNotNull(annotation,
                                "@PreAuthorize annotation should be present on fetchUserLogsInBatches method");
                assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
        }

        @Test
        @DisplayName("fetchUserLogsInBatches - Controller delegates to service")
        void fetchUserLogsInBatches_WithValidRequest_DelegatesToService() {
                UserLogService mockUserLogService = mock(UserLogService.class);
                UserLogController controller = new UserLogController(mockUserLogService);
                UserLogsRequestModel request = new UserLogsRequestModel();
                request.setStart(0);
                request.setEnd(10);
                PaginationBaseResponseModel<UserLogsResponseModel> mockResponse = new PaginationBaseResponseModel<>();
                when(mockUserLogService.fetchUserLogsInBatches(request)).thenReturn(mockResponse);

                ResponseEntity<?> response = controller.fetchUserLogsInBatches(request);

                verify(mockUserLogService).fetchUserLogsInBatches(request);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        // ========================================
        // SUCCESS Tests
        // ========================================

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators using nested loops.
         */
        @Test
        @DisplayName("Comprehensive Batch Filter Test - All Combinations")
        void fetchUserLogsInBatches_ComprehensiveCombinationTest() {
                int validTests = 0;
                int invalidTests = 0;

                String[] logicOperators = { "AND", "OR", "and", "or" };
                String[] invalidLogicOperators = { "XOR", "NAND", "invalid" };
                String[] invalidColumns = { "invalidColumn", "xyz", "!@#$" };

                // ============== TEST 1: Valid string column + operator combinations
                // ==============
                for (String column : STRING_COLUMNS) {
                        for (String operator : STRING_OPERATORS) {
                                UserLogsRequestModel request = createBasicPaginationRequest();

                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                filter.setColumn(column);
                                filter.setOperator(operator);
                                filter.setValue("testValue");
                                request.setFilters(Arrays.asList(filter));
                                request.setLogicOperator("AND");

                                Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10),
                                                1);

                                lenient().when(userLogFilterQueryBuilder.getColumnType(column)).thenReturn("string");
                                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class)))
                                                .thenReturn(page);

                                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                                                "String column '" + column + "' with operator '" + operator
                                                                + "' should succeed");
                                validTests++;
                        }
                }

                // ============== TEST 2: Valid number column + operator combinations
                // ==============
                for (String column : NUMBER_COLUMNS) {
                        for (String operator : NUMBER_OPERATORS) {
                                UserLogsRequestModel request = createBasicPaginationRequest();

                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                filter.setColumn(column);
                                filter.setOperator(operator);
                                filter.setValue("100");
                                request.setFilters(Arrays.asList(filter));
                                request.setLogicOperator("AND");

                                Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10),
                                                1);

                                lenient().when(userLogFilterQueryBuilder.getColumnType(column)).thenReturn("number");
                                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class)))
                                                .thenReturn(page);

                                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                                                "Number column '" + column + "' with operator '" + operator
                                                                + "' should succeed");
                                validTests++;
                        }
                }

                // ============== TEST 3: Invalid column names ==============
                for (String invalidColumn : invalidColumns) {
                        UserLogsRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(invalidColumn);
                        filter.setOperator("equals");
                        filter.setValue("test");
                        request.setFilters(Arrays.asList(filter));
                        request.setLogicOperator("AND");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                        () -> userLogService.fetchUserLogsInBatches(request),
                                        "Invalid column '" + invalidColumn + "' should throw BadRequestException");
                        assertTrue(ex.getMessage().contains("Invalid column name"));
                        invalidTests++;
                }

                // ============== TEST 4: Invalid logic operators ==============
                for (String invalidLogic : invalidLogicOperators) {
                        UserLogsRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                        filter1.setColumn("action");
                        filter1.setOperator("equals");
                        filter1.setValue("test");

                        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                        filter2.setColumn("description");
                        filter2.setOperator("equals");
                        filter2.setValue("test");

                        request.setFilters(Arrays.asList(filter1, filter2));
                        request.setLogicOperator(invalidLogic);

                        lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                        () -> userLogService.fetchUserLogsInBatches(request),
                                        "Invalid logic operator '" + invalidLogic
                                                        + "' should throw BadRequestException");
                        assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
                        invalidTests++;
                }

                // ============== TEST 5: Valid logic operators ==============
                for (String validLogic : logicOperators) {
                        UserLogsRequestModel request = createBasicPaginationRequest();

                        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                        filter1.setColumn("action");
                        filter1.setOperator("equals");
                        filter1.setValue("test");

                        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                        filter2.setColumn("description");
                        filter2.setOperator("equals");
                        filter2.setValue("test");

                        request.setFilters(Arrays.asList(filter1, filter2));
                        request.setLogicOperator(validLogic);

                        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

                        lenient().when(userLogFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                        lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), anyLong(), anyString(), anyList(), any(PageRequest.class)))
                                        .thenReturn(page);

                        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                                        "Valid logic operator '" + validLogic + "' should succeed");
                        validTests++;
                }

                // ============== TEST 6: Pagination edge cases ==============
                int[][] invalidPaginationCases = { { 10, 10 }, { 10, 5 }, { 0, 0 } };

                for (int[] pagination : invalidPaginationCases) {
                        if (pagination[1] - pagination[0] <= 0) {
                                UserLogsRequestModel request = createBasicPaginationRequest();
                                request.setStart(pagination[0]);
                                request.setEnd(pagination[1]);
                                request.setFilters(null);

                                BadRequestException ex = assertThrows(BadRequestException.class,
                                                () -> userLogService.fetchUserLogsInBatches(request),
                                                "Pagination start=" + pagination[0] + ", end=" + pagination[1]
                                                                + " should throw");
                                assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                                invalidTests++;
                        }
                }

                // ============== TEST 7: No filters (basic pagination) ==============
                UserLogsRequestModel noFilterRequest = createBasicPaginationRequest();
                noFilterRequest.setFilters(null);

                Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

                PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                                .fetchUserLogsInBatches(noFilterRequest);
                assertNotNull(result);
                assertEquals(1, result.getData().size());
                validTests++;

                // ============== TEST 8: Multiple filters with AND ==============
                UserLogsRequestModel multiFilterAndRequest = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
                filter1.setColumn("action");
                filter1.setOperator("contains");
                filter1.setValue("Login");
                PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
                filter2.setColumn("description");
                filter2.setOperator("contains");
                filter2.setValue("User");
                multiFilterAndRequest.setFilters(Arrays.asList(filter1, filter2));
                multiFilterAndRequest.setLogicOperator("AND");

                lenient().when(userLogFilterQueryBuilder.getColumnType("action")).thenReturn("string");
                lenient().when(userLogFilterQueryBuilder.getColumnType("description")).thenReturn("string");
                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), eq("AND"), anyList(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterAndRequest));
                validTests++;

                // ============== TEST 9: Multiple filters with OR ==============
                UserLogsRequestModel multiFilterOrRequest = createBasicPaginationRequest();
                multiFilterOrRequest.setFilters(Arrays.asList(filter1, filter2));
                multiFilterOrRequest.setLogicOperator("OR");

                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), eq("OR"), anyList(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterOrRequest));
                validTests++;

                // ============== TEST 10: Empty result set ==============
                UserLogsRequestModel emptyResultRequest = createBasicPaginationRequest();
                emptyResultRequest.setFilters(null);

                Page<UserLog> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class)))
                                .thenReturn(emptyPage);

                PaginationBaseResponseModel<UserLogsResponseModel> emptyResult = userLogService
                                .fetchUserLogsInBatches(emptyResultRequest);
                assertNotNull(emptyResult);
                assertTrue(emptyResult.getData().isEmpty());
                assertEquals(0L, emptyResult.getTotalDataCount());
                validTests++;

                // ============== TEST 11: Multiple results ==============
                UserLog secondLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, "User Logout", null, "logout", "admin");
                secondLog.setLogId(2L);

                List<UserLog> multipleLogs = Arrays.asList(testUserLog, secondLog);
                Page<UserLog> multiPage = new PageImpl<>(multipleLogs, PageRequest.of(0, 10), 2);

                UserLogsRequestModel multiResultRequest = createBasicPaginationRequest();
                multiResultRequest.setFilters(null);

                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class)))
                                .thenReturn(multiPage);

                PaginationBaseResponseModel<UserLogsResponseModel> multiResult = userLogService
                                .fetchUserLogsInBatches(multiResultRequest);
                assertNotNull(multiResult);
                assertEquals(2, multiResult.getData().size());
                assertEquals(2L, multiResult.getTotalDataCount());
                validTests++;

                // ============== TEST 12: Large page size ==============
                UserLogsRequestModel largePageRequest = createBasicPaginationRequest();
                largePageRequest.setStart(0);
                largePageRequest.setEnd(100);
                largePageRequest.setFilters(null);

                lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), anyLong(), anyString(), isNull(), any(PageRequest.class))).thenReturn(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(largePageRequest));
                validTests++;

                // ============== TEST 13: Different user IDs ==============
                long[] userIds = { 0L, 1L, 100L, Long.MAX_VALUE };
                for (long userId : userIds) {
                        UserLogsRequestModel userIdRequest = createBasicPaginationRequest();
                        userIdRequest.setUserId(userId);
                        userIdRequest.setFilters(null);

                        lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        eq(userId), anyLong(), anyString(), isNull(), any(PageRequest.class)))
                                        .thenReturn(page);

                        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(userIdRequest),
                                        "User ID " + userId + " should be accepted");
                        validTests++;
                }

                // ============== TEST 14: Different carrier IDs ==============
                long[] carrierIds = { 0L, 1L, 100L, Long.MAX_VALUE };
                for (long carrierId : carrierIds) {
                        UserLogsRequestModel carrierIdRequest = createBasicPaginationRequest();
                        carrierIdRequest.setCarrierId(carrierId);
                        carrierIdRequest.setFilters(null);

                        lenient().when(userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), eq(carrierId), anyString(), isNull(), any(PageRequest.class)))
                                        .thenReturn(page);

                        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(carrierIdRequest),
                                        "Carrier ID " + carrierId + " should be accepted");
                        validTests++;
                }

                assertTrue(validTests >= 60, "Should have at least 60 valid test cases");
                assertTrue(invalidTests >= 5, "Should have at least 5 invalid test cases");
        }

        private UserLogsRequestModel createBasicPaginationRequest() {
                UserLogsRequestModel request = new UserLogsRequestModel();
                request.setUserId(TEST_USER_ID);
                request.setCarrierId(TEST_CARRIER_ID);
                request.setStart(0);
                request.setEnd(10);
                return request;
        }
}
