package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Fetch User Groups In Batches functionality.
 *
 * Refactored to use Parameterized Tests for high granularity and coverage.
 * Total Tests: 10
 */
@DisplayName("UserGroupService - FetchUserGroupsInBatches Tests")
class FetchUserGroupsInClientInBatchesTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserGroupsInBatches - Verify @PreAuthorize Annotation")
    void getUserGroupsInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("getUserGroupsInBatches", UserGroupRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserGroupsInBatches method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("getUserGroupsInBatches - Controller delegates to service")
    void getUserGroupsInBatches_WithValidRequest_DelegatesToService() {
        // Arrange
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(0);
        request.setEnd(10);
        PaginationBaseResponseModel<UserGroupResponseModel> mockResponse = new PaginationBaseResponseModel<>();

        // We need to stub the service method here since we are using a Spy
        doReturn(mockResponse).when(userGroupService).fetchUserGroupsInClientInBatches(request);

        // Act
        ResponseEntity<?> response = userGroupController.getUserGroupsInBatches(request);

        // Assert
        verify(userGroupService).fetchUserGroupsInClientInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // PARAMETERIZED TESTS - VALIDATIONS
    // ========================================

    // --- String Columns ---

    static Stream<Arguments> stringColumnAndOperatorProvider() {
        return Stream.of(STRING_COLUMNS)
                .flatMap(column -> Stream.of(STRING_OPERATORS).map(operator -> Arguments.of(column, operator)));
    }

    @ParameterizedTest(name = "String Column: {0}, Operator: {1}")
    @MethodSource("stringColumnAndOperatorProvider")
    @DisplayName("Success - String Columns with Valid Operators")
    void fetchUserGroupsInBatches_StringColumns_Success(String column, String operator) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue("testValue");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType(column, "string");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));
    }

    // --- Number Columns ---

    static Stream<Arguments> numberColumnAndOperatorProvider() {
        return Stream.of(NUMBER_COLUMNS)
                .flatMap(column -> Stream.of(NUMBER_OPERATORS).map(operator -> Arguments.of(column, operator)));
    }

    @ParameterizedTest(name = "Number Column: {0}, Operator: {1}")
    @MethodSource("numberColumnAndOperatorProvider")
    @DisplayName("Success - Number Columns with Valid Operators")
    void fetchUserGroupsInBatches_NumberColumns_Success(String column, String operator) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue("100");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType(column, "number");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));
    }

    // --- Boolean Columns ---

    static Stream<Arguments> booleanColumnAndOperatorProvider() {
        return Stream.of(BOOLEAN_COLUMNS)
                .flatMap(column -> Stream.of(BOOLEAN_OPERATORS).map(operator -> Arguments.of(column, operator)));
    }

    @ParameterizedTest(name = "Boolean Column: {0}, Operator: {1}")
    @MethodSource("booleanColumnAndOperatorProvider")
    @DisplayName("Success - Boolean Columns with Valid Operators")
    void fetchUserGroupsInBatches_BooleanColumns_Success(String column, String operator) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(column);
        filter.setOperator(operator);
        filter.setValue("true");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType(column, "boolean");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));
    }

    // --- Logic Operators ---

    @ParameterizedTest
    @ValueSource(strings = { "AND", "OR", "and", "or" })
    @DisplayName("Success - Valid Logic Operators")
    void fetchUserGroupsInBatches_ValidLogicOperators_Success(String logicOperator) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        request.setLogicOperator(logicOperator);
        // Add minimal filter to trigger logic operator usage check if needed
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("groupName");
        filter.setOperator("equals");
        filter.setValue("A");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);
        stubUserGroupFilterQueryBuilderGetColumnType("groupName", "string");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));
    }

    // ========================================
    // PARAMETERIZED TESTS - FAILURES
    // ========================================

    @ParameterizedTest
    @ValueSource(strings = { "invalidColumn", "xyz", "!@#$" })
    @DisplayName("Failure - Invalid Columns")
    void fetchUserGroupsInBatches_InvalidColumn_ThrowsBadRequestInService(String invalidColumn) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(invalidColumn);
        filter.setOperator("equals");
        filter.setValue("val");
        request.setFilters(Collections.singletonList(filter));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertTrue(ex.getMessage().startsWith(
            String.format(ErrorMessages.CommonErrorMessages.InvalidColumnName, invalidColumn)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "XOR", "NAND", "NOT", "invalid" })
    @DisplayName("Failure - Invalid Logic Operators")
    void fetchUserGroupsInBatches_InvalidLogicOperator_ThrowsBadRequestAndCommonMessage(String invalidOperator) {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        request.setLogicOperator(invalidOperator);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
    }

    @ParameterizedTest(name = "Start: {0}, End: {1}")
    @CsvSource({
            "10, 10",
            "10, 5",
            "0, 0"
    })
    @DisplayName("Failure - Invalid Pagination")
    void fetchUserGroupsInBatches_InvalidPagination_ThrowsBadRequestAndCommonMessage(int start, int end) {
        // Arrange
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(start);
        request.setEnd(end);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    // ========================================
    // BASIC TEST
    // ========================================

    @Test
    @DisplayName("Success - No Filters (Basic Pagination)")
    void fetchUserGroupsInBatches_NoFilters_Success() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        request.setFilters(null);

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act & Assert
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));
        verify(userGroupFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any());
    }

    private UserGroupRequestModel createBasicPaginationRequest() {
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setIncludeDeleted(false);
        return request;
    }
}
