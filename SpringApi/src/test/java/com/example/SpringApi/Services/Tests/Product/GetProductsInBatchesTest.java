package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.Authorizations;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.getProductInBatches method.
 * 
 * Test count: 1 comprehensive test
 * - SUCCESS: Partial (includes success paths)
 * - FAILURE / EXCEPTION: Partial (includes failure paths)
 */
@DisplayName("ProductService - GetProductsInBatches Tests")
public class GetProductsInBatchesTest extends ProductServiceTestBase {

        private static final String[] BATCH_INVALID_COLUMNS = { "invalidColumn", "unknown", "notAField" };
        private static final String[] BATCH_INVALID_OPERATORS = { "invalidOp", "notAnOperator" };
        private static final String[] BATCH_VALID_VALUES = { "testValue", "123", "true", "2023-01-01" };
        private static final String[] BATCH_EMPTY_VALUES = { "", " " };

        @Test
        @DisplayName("Get Products In Batches - Invalid pagination, success no filters, and triple-loop filter validation")
        void getProductInBatches_SingleComprehensiveTest() {
                // ---- (1) Invalid pagination: end <= start ----
                PaginationBaseRequestModel invalidRequest = createValidPaginationRequest();
                invalidRequest.setStart(10);
                invalidRequest.setEnd(5);
                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> productService.getProductInBatches(invalidRequest));

                // ---- (2) Success: simple retrieval without filters ----
                PaginationBaseRequestModel successRequest = createValidPaginationRequest();
                successRequest.setStart(0);
                successRequest.setEnd(10);
                successRequest.setFilters(null);
                List<Product> productList = Collections.singletonList(testProduct);
                Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
                when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                                .thenReturn(productPage);
                PaginationBaseResponseModel<ProductResponseModel> result = productService
                                .getProductInBatches(successRequest);
                assertNotNull(result);
                assertEquals(1, result.getData().size());
                assertEquals(testProduct.getProductId(), result.getData().get(0).getProductId());

                // ---- (3) Triple-loop: valid columns × operators × types + invalid
                // combinations ----
                String[] stringColumns = {
                                "title", "descriptionHtml", "brand", "color", "colorLabel", "condition",
                                "countryOfManufacture", "model", "upc", "modificationHtml", "notes",
                                "createdUser", "modifiedUser"
                };
                String[] numberColumns = {
                                "productId", "price", "discount", "length", "breadth",
                                "height", "weightKgs", "categoryId", "pickupLocationId"
                };
                String[] booleanColumns = { "isDiscountPercent", "returnsAllowed", "itemModified", "isDeleted" };
                String[] dateColumns = { "createdAt", "updatedAt" };
                String[] invalidColumns = BATCH_INVALID_COLUMNS;
                String[] stringOperators = {
                                "contains", "equals", "startsWith", "endsWith", "isEmpty", "isNotEmpty",
                                "isOneOf", "isNotOneOf", "containsOneOf"
                };
                String[] numberOperators = {
                                "equals", "notEquals", "greaterThan", "greaterThanOrEqual", "lessThan",
                                "lessThanOrEqual",
                                "isEmpty", "isNotEmpty", "isOneOf", "isNotOneOf"
                };
                String[] booleanOperators = { "is" };
                String[] dateOperators = {
                                "is", "isNot", "isAfter", "isOnOrAfter", "isBefore", "isOnOrBefore", "isEmpty",
                                "isNotEmpty"
                };
                String[] invalidOperators = BATCH_INVALID_OPERATORS;
                String[] validValues = BATCH_VALID_VALUES;
                String[] emptyValues = BATCH_EMPTY_VALUES;

                Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
                lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                                .thenReturn(emptyPage);
                lenient()
                                .when(productFilterQueryBuilder.getColumnType(
                                                argThat(arg -> Arrays.asList(stringColumns).contains(arg))))
                                .thenReturn("string");
                lenient()
                                .when(productFilterQueryBuilder.getColumnType(
                                                argThat(arg -> Arrays.asList(numberColumns).contains(arg))))
                                .thenReturn("number");
                lenient()
                                .when(productFilterQueryBuilder.getColumnType(
                                                argThat(arg -> Arrays.asList(booleanColumns).contains(arg))))
                                .thenReturn("boolean");
                lenient()
                                .when(productFilterQueryBuilder.getColumnType(
                                                argThat(arg -> Arrays.asList(dateColumns).contains(arg))))
                                .thenReturn("date");

                String[] allColumns = joinArrays(stringColumns, numberColumns, booleanColumns, dateColumns,
                                invalidColumns);
                String[] allOperators = joinArrays(stringOperators, numberOperators, booleanOperators, dateOperators,
                                invalidOperators);
                Set<String> uniqueOperators = new HashSet<>(Arrays.asList(allOperators));
                String[] allValues = joinArrays(validValues, emptyValues);

                for (String column : allColumns) {
                        for (String operator : uniqueOperators) {
                                for (String value : allValues) {
                                        PaginationBaseRequestModel testRequest = createValidPaginationRequest();
                                        testRequest.setStart(0);
                                        testRequest.setEnd(10);
                                        PaginationBaseRequestModel.FilterCondition filter = createFilterCondition(
                                                        column, operator, value);
                                        testRequest.setFilters(Collections.singletonList(filter));

                                        boolean isColumnKnown = !Arrays.asList(invalidColumns).contains(column);
                                        boolean isValidForString = Arrays.asList(stringColumns).contains(column)
                                                        && Arrays.asList(stringOperators).contains(operator);
                                        boolean isValidForNumber = Arrays.asList(numberColumns).contains(column)
                                                        && Arrays.asList(numberOperators).contains(operator);
                                        boolean isValidForBoolean = Arrays.asList(booleanColumns).contains(column)
                                                        && Arrays.asList(booleanOperators).contains(operator);
                                        boolean isValidForDate = Arrays.asList(dateColumns).contains(column)
                                                        && Arrays.asList(dateOperators).contains(operator);
                                        boolean isOperatorValidForType = isValidForString || isValidForNumber
                                                        || isValidForBoolean || isValidForDate;

                                        boolean shouldSucceed = isColumnKnown && isOperatorValidForType;

                                        try {
                                                productService.getProductInBatches(testRequest);
                                                if (!shouldSucceed) {
                                                        String reason = !isColumnKnown ? "Invalid column: " + column
                                                                        : "Invalid operator '" + operator
                                                                                        + "' for column '" + column
                                                                                        + "'";
                                                        fail("Expected failure but succeeded. Context: " + reason);
                                                }
                                        } catch (Exception e) {
                                                if (shouldSucceed) {
                                                        fail("Expected success but failed: Col=" + column + " Op="
                                                                        + operator + " Val=" + value + ". Error: "
                                                                        + e.getMessage());
                                                }
                                        }
                                }
                        }
                }
        }

        protected PaginationBaseRequestModel createValidPaginationRequest() {
                PaginationBaseRequestModel request = new PaginationBaseRequestModel();
                request.setStart(0);
                request.setEnd(10);
                return request;
        }

        protected PaginationBaseRequestModel.FilterCondition createFilterCondition(String column, String operator,
                        String value) {
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator(operator);
                filter.setValue(value);
                return filter;
        }

        protected String[] joinArrays(String[]... arrays) {
                return Arrays.stream(arrays)
                                .flatMap(Arrays::stream)
                                .toArray(String[]::new);
        }

        /*
         **********************************************************************************************
         * CONTROLLER AUTHORIZATION TESTS
         **********************************************************************************************
         */

        @Test
        @DisplayName("getProductInBatches - Verify @PreAuthorize Annotation")
        void getProductInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                Method method = ProductController.class.getMethod("getProductInBatches",
                                PaginationBaseRequestModel.class);
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertNotNull(annotation, "@PreAuthorize annotation should be present on getProductInBatches");
                assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION),
                                "@PreAuthorize should reference VIEW_PRODUCTS_PERMISSION");
        }

        @Test
        @DisplayName("getProductInBatches - Controller delegates to service")
        void getProductInBatches_WithValidRequest_DelegatesToService() {
                ProductService mockProductService = mock(ProductService.class);
                ProductController controller = new ProductController(mockProductService);
                PaginationBaseRequestModel request = createValidPaginationRequest();
                PaginationBaseResponseModel<ProductResponseModel> mockResponse = new PaginationBaseResponseModel<>();
                when(mockProductService.getProductInBatches(request)).thenReturn(mockResponse);

                ResponseEntity<?> response = controller.getProductInBatches(request);

                verify(mockProductService).getProductInBatches(request);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }
}
