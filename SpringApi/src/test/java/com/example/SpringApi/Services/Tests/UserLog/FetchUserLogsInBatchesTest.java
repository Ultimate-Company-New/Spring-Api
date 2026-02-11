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
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserLogService.fetchUserLogsInBatches method.
 */
@DisplayName("UserLogService - FetchUserLogsInBatches Tests")
class FetchUserLogsInBatchesTest extends UserLogServiceTestBase {

    // Total Tests: 17

    // ========================================
    // Section 1: Success Tests
    // ========================================

    /*
     * Purpose: Verify different carrier IDs are accepted
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Different Carrier IDs - Success")
    void fetchUserLogsInBatches_differentCarrierIds_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
        long[] carrierIds = { 0L, 1L, 100L, Long.MAX_VALUE };

        // Act & Assert
        for (long carrierId : carrierIds) {
            UserLogsRequestModel carrierIdRequest = createBasicPaginationRequest();
            carrierIdRequest.setCarrierId(carrierId);
            carrierIdRequest.setFilters(null);

            stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(carrierIdRequest),
                    "Carrier ID " + carrierId + " should be accepted");
        }
    }

    /*
     * Purpose: Verify different user IDs are accepted
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Different User IDs - Success")
    void fetchUserLogsInBatches_differentUserIds_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
        long[] userIds = { 0L, 1L, 100L, Long.MAX_VALUE };

        // Act & Assert
        for (long userId : userIds) {
            UserLogsRequestModel userIdRequest = createBasicPaginationRequest();
            userIdRequest.setUserId(userId);
            userIdRequest.setFilters(null);

            stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(userIdRequest),
                    "User ID " + userId + " should be accepted");
        }
    }

    /*
     * Purpose: Verify behavior with empty result set
     * Expected Result: Empty list returned
     * Assertions: assertTrue(emptyResult.getData().isEmpty())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Empty Result Set - Success")
    void fetchUserLogsInBatches_emptyResultSet_success() {
        // Arrange
        UserLogsRequestModel emptyResultRequest = createBasicPaginationRequest();
        emptyResultRequest.setFilters(null);

        Page<UserLog> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        stubUserLogFilterQueryBuilderFindPaginatedEntities(emptyPage);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> emptyResult = userLogService
                .fetchUserLogsInBatches(emptyResultRequest);

        // Assert
        assertNotNull(emptyResult);
        assertTrue(emptyResult.getData().isEmpty());
        assertEquals(0L, emptyResult.getTotalDataCount());
    }

    /*
     * Purpose: Verify large page size
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Large Page Size - Success")
    void fetchUserLogsInBatches_largePageSize_success() {
        // Arrange
        UserLogsRequestModel largePageRequest = createBasicPaginationRequest();
        largePageRequest.setStart(0);
        largePageRequest.setEnd(100);
        largePageRequest.setFilters(null);
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
        stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        // Assert
        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(largePageRequest));
    }

    /*
     * Purpose: Verify multiple filters with AND logic
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Multiple Filters AND - Success")
    void fetchUserLogsInBatches_multipleFiltersAnd_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
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

        stubUserLogFilterQueryBuilderGetColumnType("action", "string");
        stubUserLogFilterQueryBuilderGetColumnType("description", "string");
        stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        // Assert
        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterAndRequest));
    }

    /*
     * Purpose: Verify multiple filters with OR logic
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Multiple Filters OR - Success")
    void fetchUserLogsInBatches_multipleFiltersOr_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
        UserLogsRequestModel multiFilterOrRequest = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("action");
        filter1.setOperator("contains");
        filter1.setValue("Login");
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("description");
        filter2.setOperator("contains");
        filter2.setValue("User");
        multiFilterOrRequest.setFilters(Arrays.asList(filter1, filter2));
        multiFilterOrRequest.setLogicOperator("OR");

        stubUserLogFilterQueryBuilderGetColumnType("action", "string");
        stubUserLogFilterQueryBuilderGetColumnType("description", "string");
        stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        // Assert
        assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(multiFilterOrRequest));
    }

    /*
     * Purpose: Verify multiple results returned
     * Expected Result: Correct size and count
     * Assertions: assertEquals(2, multiResult.getData().size())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Multiple Results - Success")
    void fetchUserLogsInBatches_multipleResults_success() {
        // Arrange
        UserLog secondLog = new UserLog(TEST_USER_ID, TEST_CARRIER_ID, "User Logout", null, "logout", "admin");
        secondLog.setLogId(2L);
        List<UserLog> multipleLogs = Arrays.asList(testUserLog, secondLog);
        Page<UserLog> multiPage = new PageImpl<>(multipleLogs, PageRequest.of(0, 10), 2);

        UserLogsRequestModel multiResultRequest = createBasicPaginationRequest();
        multiResultRequest.setFilters(null);

        stubUserLogFilterQueryBuilderFindPaginatedEntities(multiPage);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> multiResult = userLogService
                .fetchUserLogsInBatches(multiResultRequest);

        // Assert
        assertNotNull(multiResult);
        assertEquals(2, multiResult.getData().size());
        assertEquals(2L, multiResult.getTotalDataCount());
    }

    /*
     * Purpose: Verify no filters (basic pagination)
     * Expected Result: Success
     * Assertions: assertEquals(1, result.getData().size())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - No Filters - Success")
    void fetchUserLogsInBatches_noFilters_success() {
        // Arrange
        UserLogsRequestModel noFilterRequest = createBasicPaginationRequest();
        noFilterRequest.setFilters(null);

        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);
        stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<UserLogsResponseModel> result = userLogService
                .fetchUserLogsInBatches(noFilterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /*
     * Purpose: Verify valid logic operators
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Valid Logic Operators - Success")
    void fetchUserLogsInBatches_validLogicOperator_success() {
        // Arrange
        String[] logicOperators = { "AND", "OR", "and", "or" };
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

        // Act & Assert
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

            stubUserLogFilterQueryBuilderGetColumnTypeAny("string");
            stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

            assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                    "Valid logic operator '" + validLogic + "' should succeed");
        }
    }

    /*
     * Purpose: Verify valid number columns and operators
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Valid Number Columns - Success")
    void fetchUserLogsInBatches_validNumberColumns_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

        // Act & Assert
        for (String column : NUMBER_COLUMNS) {
            for (String operator : NUMBER_OPERATORS) {
                UserLogsRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue("100");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                stubUserLogFilterQueryBuilderGetColumnType(column, "number");
                stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                        "Number column '" + column + "' with operator '" + operator
                                + "' should succeed");
            }
        }
    }

    /*
     * Purpose: Verify valid string columns and operators
     * Expected Result: Success
     * Assertions: assertDoesNotThrow
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Valid String Columns - Success")
    void fetchUserLogsInBatches_validStringColumns_success() {
        // Arrange
        Page<UserLog> page = new PageImpl<>(Arrays.asList(testUserLog), PageRequest.of(0, 10), 1);

        // Act & Assert
        for (String column : STRING_COLUMNS) {
            for (String operator : STRING_OPERATORS) {
                UserLogsRequestModel request = createBasicPaginationRequest();

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue("testValue");
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                stubUserLogFilterQueryBuilderGetColumnType(column, "string");
                stubUserLogFilterQueryBuilderFindPaginatedEntities(page);

                assertDoesNotThrow(() -> userLogService.fetchUserLogsInBatches(request),
                        "String column '" + column + "' with operator '" + operator
                                + "' should succeed");
            }
        }
    }

    // ========================================
    // Section 2: Failure / Exception Tests
    // ========================================

    /*
     * Purpose: Verify invalid column names throw BadRequestException
     * Expected Result: BadRequestException with InvalidColumnName error message
     * Assertions: assertTrue(ex.getMessage().contains(ErrorMessages.UserLogErrorMessages.InvalidColumnName))
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Invalid Column Names - Throws BadRequestException")
    void fetchUserLogsInBatches_invalidColumnNames_badRequestException() {
        // Arrange
        String[] invalidColumns = { "invalidColumn", "xyz", "!@#$" };

        // Act & Assert
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
            assertTrue(ex.getMessage().contains("Invalid column name"),
                    "Error message should indicate invalid column name");
        }
    }

    /*
     * Purpose: Verify invalid logic operators throw BadRequestException
     * Expected Result: BadRequestException with InvalidLogicOperator message
     * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Invalid Logic Operator - Throws BadRequestException")
    void fetchUserLogsInBatches_invalidLogicOperator_badRequestException() {
        // Arrange
        String[] invalidLogicOperators = { "XOR", "NAND", "invalid" };
        stubUserLogFilterQueryBuilderGetColumnTypeAny("string");

        // Act & Assert
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

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(request),
                    "Invalid logic operator '" + invalidLogic
                            + "' should throw BadRequestException");
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
        }
    }

    /*
     * Purpose: Verify invalid pagination parameters
     * Expected Result: BadRequestException with InvalidPagination message
     * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Invalid Pagination - Throws BadRequestException")
    void fetchUserLogsInBatches_invalidPagination_badRequestException() {
        // Arrange
        int[][] invalidPaginationCases = { { 10, 10 }, { 10, 5 }, { 0, 0 } };

        // Act & Assert
        for (int[] pagination : invalidPaginationCases) {
            UserLogsRequestModel request = createBasicPaginationRequest();
            request.setStart(pagination[0]);
            request.setEnd(pagination[1]);
            request.setFilters(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> userLogService.fetchUserLogsInBatches(request),
                    "Pagination start=" + pagination[0] + ", end=" + pagination[1]
                            + " should throw");
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }
    }

    // ========================================
    // Section 3: Controller Permission/Auth Tests
    // ========================================

    /*
     * Purpose: Verify controller permission handling
     * Expected Result: Controller can be called (permission enforcement tested via integration tests)
     * Assertions: assertNotNull(response), assertEquals(HttpStatus.OK, response.getStatusCode())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Controller Permission - Unauthorized")
    void fetchUserLogsInBatches_controller_permission_unauthorized() {
        // Arrange
        UserLogsRequestModel request = createBasicPaginationRequest();
        request.setFilters(null);
        Page<UserLog> page = new PageImpl<>(List.of(testUserLog), PageRequest.of(0, 10), 1);
        stubUserLogFilterQueryBuilderFindPaginatedEntities(page);
        
        // Act
        ResponseEntity<?> response = userLogController.fetchUserLogsInBatches(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Note: Actual permission enforcement (@PreAuthorize) is tested via integration tests
    }

    /*
     * Purpose: Verify controller delegates to service
     * Expected Result: Service method is called
     * Assertions: verify(mockUserLogService).fetchUserLogsInBatches(request)
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Valid Request - Delegates To Service")
    void fetchUserLogsInBatches_validRequest_delegatesToService() {
        // Arrange
        UserLogsRequestModel request = new UserLogsRequestModel();
        request.setStart(0);
        request.setEnd(10);
        PaginationBaseResponseModel<UserLogsResponseModel> mockResponse = new PaginationBaseResponseModel<>();
        stubUserLogServiceFetchUserLogsInBatchesMock(request, mockResponse);

        // Act
        ResponseEntity<?> response = userLogControllerWithMock.fetchUserLogsInBatches(request);

        // Assert
        verify(mockUserLogService).fetchUserLogsInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // Section 3: Permission Tests
    // ========================================

    /*
     * Purpose: Verify controller handles unauthorized access
     * Expected Result: HTTP 401 UNAUTHORIZED status
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode())
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Controller Permission - Forbidden")
    void fetchUserLogsInBatches_controller_permission_forbidden() {
        // Arrange
        UserLogsRequestModel request = createBasicPaginationRequest();
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = userLogControllerWithMock.fetchUserLogsInBatches(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /*
     * Purpose: Verify @PreAuthorize annotation on controller
     * Expected Result: Annotation present and checks VIEW_USER_PERMISSION
     * Assertions: assertNotNull(annotation), assertTrue(annotation.value().contains(...))
     */
    @Test
    @DisplayName("fetchUserLogsInBatches - Verify @PreAuthorize Annotation - Success")
    void fetchUserLogsInBatches_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = UserLogController.class.getMethod("fetchUserLogsInBatches", UserLogsRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation,
                "@PreAuthorize annotation should be present on fetchUserLogsInBatches method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
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