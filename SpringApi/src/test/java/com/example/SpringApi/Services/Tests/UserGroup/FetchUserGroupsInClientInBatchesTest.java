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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService.fetchUserGroupsInClientInBatches method.
 * 
 */
@DisplayName("UserGroupService - FetchUserGroupsInClientInBatches Tests")
class FetchUserGroupsInClientInBatchesTest extends UserGroupServiceTestBase {

    // Total Tests: 9
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify boolean column filtering works.
     * Expected Result: Success result returned.
     * Assertions: assertNotNull
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Success - Boolean Filter")
    void fetchUserGroupsInClientInBatches_BooleanColumnFilter_Success() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("isActive");
        filter.setOperator("is");
        filter.setValue("true");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType("isActive", "boolean");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                .fetchUserGroupsInClientInBatches(request);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify no filters (basic pagination) works.
     * Expected Result: Query builder called with correct params.
     * Assertions: verify
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Success - No Filters")
    void fetchUserGroupsInClientInBatches_NoFilters_Success() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        request.setFilters(null);

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        assertDoesNotThrow(() -> userGroupService.fetchUserGroupsInClientInBatches(request));

        // Assert
        verify(userGroupFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any());
    }

    /**
     * Purpose: Verify number column filtering works.
     * Expected Result: Success result returned.
     * Assertions: assertNotNull
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Success - Number Filter")
    void fetchUserGroupsInClientInBatches_NumberColumnFilter_Success() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("groupId");
        filter.setOperator("equals");
        filter.setValue("100");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType("groupId", "number");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                .fetchUserGroupsInClientInBatches(request);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify string column filtering works.
     * Expected Result: Success result returned.
     * Assertions: assertNotNull
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Success - String Filter")
    void fetchUserGroupsInClientInBatches_StringColumnFilter_Success() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("groupName");
        filter.setOperator("equals");
        filter.setValue("testValue");
        request.setFilters(Collections.singletonList(filter));

        Page<UserGroup> page = new PageImpl<>(Collections.singletonList(testUserGroup), PageRequest.of(0, 10), 1);

        stubUserGroupFilterQueryBuilderGetColumnType("groupName", "string");
        stubUserGroupFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<UserGroupResponseModel> result = userGroupService
                .fetchUserGroupsInClientInBatches(request);

        // Assert
        assertNotNull(result);
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify invalid column throws BadRequestException.
     * Expected Result: BadRequestException with InvalidColumnName message.
     * Assertions: assertThrows, assertTrue
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Failure - Invalid Column")
    void fetchUserGroupsInClientInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("invalidColumn");
        filter.setOperator("equals");
        filter.setValue("val");
        request.setFilters(Collections.singletonList(filter));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertTrue(ex.getMessage().startsWith(
                String.format(ErrorMessages.CommonErrorMessages.INVALID_COLUMN_NAME, "invalidColumn")));
    }

    /**
     * Purpose: Verify invalid logic operator throws BadRequestException.
     * Expected Result: BadRequestException with InvalidLogicOperator message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Failure - Invalid Logic Operator")
    void fetchUserGroupsInClientInBatches_InvalidLogicOperator_ThrowsBadRequestException() {
        // Arrange
        UserGroupRequestModel request = createBasicPaginationRequest();
        request.setLogicOperator("INVALID");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertEquals(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid pagination throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Failure - Invalid Pagination")
    void fetchUserGroupsInClientInBatches_InvalidPagination_ThrowsBadRequestException() {
        // Arrange
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(10);
        request.setEnd(5);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.fetchUserGroupsInClientInBatches(request));
        assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned and @PreAuthorize
     * verified.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
     * assertNotNull, assertTrue
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Controller permission forbidden")
    void fetchUserGroupsInClientInBatches_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        UserGroupRequestModel request = new UserGroupRequestModel();
        stubServiceThrowsUnauthorizedException();
        Method method = UserGroupController.class.getMethod("getUserGroupsInBatches", UserGroupRequestModel.class);

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.getUserGroupsInBatches(request);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserGroupsInBatches method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called and HTTP 200 is returned.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("fetchUserGroupsInClientInBatches - Controller delegates to service")
    void fetchUserGroupsInClientInBatches_withValidRequest_delegatesToService() {
        // Arrange
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setStart(0);
        request.setEnd(10);
        stubMockUserGroupServiceFetchUserGroupsInClientInBatches(new PaginationBaseResponseModel<>());

        // Act
        ResponseEntity<?> response = userGroupControllerWithMock.getUserGroupsInBatches(request);

        // Assert
        verify(mockUserGroupService, times(1)).fetchUserGroupsInClientInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
