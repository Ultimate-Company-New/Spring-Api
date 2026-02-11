package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Total Tests: 21
@DisplayName("UserService - FetchUsersInCarrierInBatches Tests")
class FetchUsersInCarrierInBatchesTest extends UserServiceTestBase {

        // ========================================
        // CONTROLLER AUTHORIZATION TESTS
        // ========================================

        /**
         * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
         * Expected Result: The method should be annotated with VIEW_USER_PERMISSION.
         * Assertions: Annotation is present and contains expected permission string.
         */
        @Test
        @DisplayName("fetchUsersInCarrierInBatches - Verify @PreAuthorize Annotation")
        void fetchUsersInCarrierInBatches_controller_permission_forbidden() throws NoSuchMethodException {
                // Arrange
                Method method = UserController.class.getMethod("fetchUsersInCarrierInBatches", UserRequestModel.class);

                // Act
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

                // Assert
                assertNotNull(annotation,
                                "@PreAuthorize annotation should be present on fetchUsersInCarrierInBatches method");
                assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
        }

        /**
         * Purpose: Verify controller delegates to service.
         * Expected Result: Service method is called.
         * Assertions: verify(userService).fetchUsersInCarrierInBatches(request);
         */
        @Test
        @DisplayName("fetchUsersInCarrierInBatches - Controller delegates to service")
        void fetchUsersInCarrierInBatches_withValidRequest_delegatesToService() {
                // Arrange
                UserRequestModel request = new UserRequestModel();
                request.setStart(0);
                request.setEnd(10);

                // Mock the service call using the spy/mock from base (but for delegation we
                // usually just verify call)
                // Since userService is @InjectMocks, we can't easily mock it unless we spy it
                // or assume it's mocked if we declared it as @Mock (but it's @InjectMocks).
                // Best approach for delegation test is to use a local controller with a Mock
                // service,
                // OR rely on the structure if we modified UserServiceTestBase to have
                // userService as a Spy/Mock.
                // It's defined as @InjectMocks.
                // Let's use a local controller with a mock here for safety, just like
                // UpdateUserTest.

                com.example.SpringApi.Services.UserService mockService = mock(
                                com.example.SpringApi.Services.UserService.class);
                UserController localController = new UserController(mockService);

                // Act
                ResponseEntity<?> response = localController.fetchUsersInCarrierInBatches(request);

                // Assert
                verify(mockService, times(1)).fetchUsersInCarrierInBatches(request);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        // ========================================
        // COMPREHENSIVE TEST
        // ========================================

        /**
         * Purpose: Comprehensive test covering all combinations of filters, operators,
         * columns, pagination, and logic operators.
         * 
         * Expected Result: Valid combinations succeed, invalid combinations throw
         * BadRequestException.
         * Assertions: Multiple assertions for each combination.
         */

        // ========================================
        // GRANULAR TESTS
        // ========================================

        // --- User String Filters ---

        @Test
        @DisplayName("fetchUsersInBatches - Filter by FirstName - Success")
        void fetchUsersInBatches_filterByFirstName_success() {
                testStringFilter("firstName", "contains", "John");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by LastName - Success")
        void fetchUsersInBatches_filterByLastName_success() {
                testStringFilter("lastName", "startsWith", "Doe");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by Email - Success")
        void fetchUsersInBatches_filterByEmail_success() {
                testStringFilter("email", "endsWith", "@example.com");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by Role - Success")
        void fetchUsersInBatches_filterByRole_success() {
                testStringFilter("role", "equals", "ADMIN");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by Phone - Success")
        void fetchUsersInBatches_filterByPhone_success() {
                testStringFilter("phone", "contains", "555");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by LoginName - Success")
        void fetchUsersInBatches_filterByLoginName_success() {
                testStringFilter("loginName", "equals", "admin@example.com");
        }

        private void testStringFilter(String column, String operator, String value) {
                UserRequestModel request = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue(value);
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                stubUserFilterQueryBuilderGetColumnType(column, "string");
                stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
        }

        // --- Numeric Filters ---

        @Test
        @DisplayName("fetchUsersInBatches - Filter by UserId - Success")
        void fetchUsersInBatches_filterByUserId_success() {
                testNumberFilter("userId", "equals", "1");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by LoginAttempts - Success")
        void fetchUsersInBatches_filterByLoginAttempts_success() {
                testNumberFilter("loginAttempts", ">", "0");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by AddressId - Success")
        void fetchUsersInBatches_filterByAddressId_success() {
                testNumberFilter("addressId", "equals", "100");
        }

        private void testNumberFilter(String column, String operator, String value) {
                UserRequestModel request = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue(value);
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                stubUserFilterQueryBuilderGetColumnType(column, "number");
                stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
        }

        // --- Boolean Filters ---

        @Test
        @DisplayName("fetchUsersInBatches - Filter by IsDeleted - Success")
        void fetchUsersInBatches_filterByIsDeleted_success() {
                testBooleanFilter("isDeleted", "is", "false");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by Locked - Success")
        void fetchUsersInBatches_filterByLocked_success() {
                testBooleanFilter("locked", "is", "false");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Filter by EmailConfirmed - Success")
        void fetchUsersInBatches_filterByEmailConfirmed_success() {
                testBooleanFilter("emailConfirmed", "is", "true");
        }

        private void testBooleanFilter(String column, String operator, String value) {
                UserRequestModel request = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue(value);
                request.setFilters(Arrays.asList(filter));
                request.setLogicOperator("AND");

                stubUserFilterQueryBuilderGetColumnType(column, "boolean");
                stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
        }

        // --- Operator Logic ---

        @Test
        @DisplayName("fetchUsersInBatches - Logic Operator AND - Success")
        void fetchUsersInBatches_logicOperatorAND_success() {
                testLogicOperator("AND");
        }

        @Test
        @DisplayName("fetchUsersInBatches - Logic Operator OR - Success")
        void fetchUsersInBatches_logicOperatorOR_success() {
                testLogicOperator("OR");
        }

        private void testLogicOperator(String logicOperator) {
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
                request.setLogicOperator(logicOperator);

                stubUserFilterQueryBuilderGetColumnType("firstName", "string");
                stubUserFilterQueryBuilderGetColumnType("lastName", "string");
                stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
        }

        // --- Error Cases ---

        @Test
        @DisplayName("fetchUsersInBatches - Invalid Column - Throws BadRequestException")
        void fetchUsersInBatches_invalidColumn_throwsBadRequestException() {
                UserRequestModel request = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn("invalidColumn");
                filter.setOperator("equals");
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> userService.fetchUsersInCarrierInBatches(request));
                assertTrue(ex.getMessage().contains("Invalid column"));
        }

        @Test
        @DisplayName("fetchUsersInBatches - Invalid Operator - Throws BadRequestException")
        void fetchUsersInBatches_invalidOperator_throwsBadRequestException() {
                UserRequestModel request = createBasicPaginationRequest();
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn("firstName");
                filter.setOperator("invalidOp");
                filter.setValue("test");
                request.setFilters(Arrays.asList(filter));

                stubUserFilterQueryBuilderGetColumnType("firstName", "string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> userService.fetchUsersInCarrierInBatches(request));
                assertTrue(ex.getMessage().contains("Invalid operator"));
        }

        @Test
        @DisplayName("fetchUsersInBatches - Invalid Logic Operator - Throws BadRequestException")
        void fetchUsersInBatches_invalidLogicOperator_throwsBadRequestException() {
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
                request.setLogicOperator("INVALID");

                stubUserFilterQueryBuilderGetColumnType("firstName", "string");
                stubUserFilterQueryBuilderGetColumnType("lastName", "string");

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> userService.fetchUsersInCarrierInBatches(request));
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidLogicOperator, ex.getMessage());
        }

        // --- Pagination ---

        @Test
        @DisplayName("fetchUsersInBatches - Invalid Pagination - Throws BadRequestException")
        void fetchUsersInBatches_invalidPagination_throwsBadRequestException() {
                UserRequestModel request = new UserRequestModel();
                request.setStart(10);
                request.setEnd(5); // Invalid: start > end

                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> userService.fetchUsersInCarrierInBatches(request));
                assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        @Test
        @DisplayName("fetchUsersInBatches - Valid Pagination - Success")
        void fetchUsersInBatches_validPagination_success() {
                UserRequestModel request = new UserRequestModel();
                request.setStart(0);
                request.setEnd(20);

                stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

                assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
        }

        private UserRequestModel createBasicPaginationRequest() {
                UserRequestModel request = new UserRequestModel();
                request.setStart(0);
                request.setEnd(10);
                request.setIncludeDeleted(false);
                return request;
        }
}
