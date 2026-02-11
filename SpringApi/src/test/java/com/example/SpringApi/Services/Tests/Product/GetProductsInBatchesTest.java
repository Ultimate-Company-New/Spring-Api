package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import org.junit.jupiter.api.BeforeEach;
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
import com.example.SpringApi.Services.ProductService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.getProductInBatches.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 14
@DisplayName("ProductService - GetProductsInBatches Tests")
class GetProductsInBatchesTest extends ProductServiceTestBase {

        @BeforeEach
        void setUpFilters() {
                // Stub ProductFilterQueryBuilder.getColumnType for various categories
                stubProductFilterQueryBuilderGetColumnType();

                // Default empty page stub
                stubProductFilterQueryBuilderFindPaginatedEntitiesEmptyPage();
        }

        // ==========================================
        // SECTION 1: SUCCESS TESTS
        // ==========================================

        /*
         * Purpose: Verify retrieval without filters
         */
        @Test
        @DisplayName("getProductInBatches - No filters - Success")
        void getProductInBatches_NoFilters_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                List<Product> list = Collections.singletonList(testProduct);
                Page<Product> page = new PageImpl<>(list, PageRequest.of(0, 10), 1);
                when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(),
                                any(), anyBoolean(), any(Pageable.class))).thenReturn(page);

                // Act
                PaginationBaseResponseModel<ProductResponseModel> result = productService.getProductInBatches(request);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
                verify(productFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(anyLong(), any(),
                                anyString(), any(), anyBoolean(), any(Pageable.class));
        }

        /*
         * Purpose: Verify title 'contains' filter
         */
        @Test
        @DisplayName("getProductInBatches - Title contains filter - Success")
        void getProductInBatches_TitleContains_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("title", "contains", "test")));

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
        }

        /*
         * Purpose: Verify price 'greaterThan' filter
         */
        @Test
        @DisplayName("getProductInBatches - Price greaterThan filter - Success")
        void getProductInBatches_PriceGreaterThan_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("price", "greaterThan", "100")));

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
        }

        /*
         * Purpose: Verify multiple filters with AND logic
         */
        @Test
        @DisplayName("getProductInBatches - Multiple filters AND logic - Success")
        void getProductInBatches_MultipleFiltersAnd_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setLogicOperator("AND");
                request.setFilters(Arrays.asList(
                                createFilter("brand", "equals", "Test"),
                                createFilter("price", "lessThan", "500")));

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
                verify(productFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), eq("AND"),
                                anyList(), anyBoolean(), any(Pageable.class));
        }

        /*
         * Purpose: Verify multiple filters with OR logic
         */
        @Test
        @DisplayName("getProductInBatches - Multiple filters OR logic - Success")
        void getProductInBatches_MultipleFiltersOr_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setLogicOperator("OR");
                request.setFilters(Arrays.asList(
                                createFilter("brand", "equals", "Test"),
                                createFilter("price", "lessThan", "500")));

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
                verify(productFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), eq("OR"),
                                anyList(), anyBoolean(), any(Pageable.class));
        }

        /*
         * Purpose: Verify includeDeleted flag propagation
         */
        @Test
        @DisplayName("getProductInBatches - IncludeDeleted flag - Success")
        void getProductInBatches_IncludeDeleted_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setIncludeDeleted(true);

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
                verify(productFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(anyLong(), any(),
                                anyString(), any(), eq(true), any(Pageable.class));
        }

        /*
         * Purpose: Verify filter by pickupLocationId (special join logic)
         */
        @Test
        @DisplayName("getProductInBatches - Filter by pickupLocationId - Success")
        void getProductInBatches_PickupLocationFilter_Success() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("pickupLocationId", "equals", "1")));

                // Act & Assert
                assertDoesNotThrow(() -> productService.getProductInBatches(request));
        }

        // ==========================================
        // SECTION 2: FAILURE TESTS
        // ==========================================

        /*
         * Purpose: Verify failure on invalid pagination (end <= start)
         */
        @Test
        @DisplayName("getProductInBatches - End before Start - Throws BadRequest")
        void getProductInBatches_InvalidRange_ThrowsBadRequest() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(10);
                request.setEnd(5);

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> productService.getProductInBatches(request));
        }

        /*
         * Purpose: Verify failure on zero page size
         */
        @Test
        @DisplayName("getProductInBatches - Zero page size - Throws BadRequest")
        void getProductInBatches_ZeroPageSize_ThrowsBadRequest() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setStart(5);
                request.setEnd(5);

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> productService.getProductInBatches(request));
        }

        /*
         * Purpose: Verify failure on invalid column name
         */
        @Test
        @DisplayName("getProductInBatches - Unknown column - Throws BadRequest")
        void getProductInBatches_UnknownColumn_ThrowsBadRequest() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("nonExistent", "equals", "val")));

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productService.getProductInBatches(request));
                assertTrue(exception.getMessage().contains("nonExistent"));
        }

        /*
         * Purpose: Verify failure on invalid operator for type (e.g., gt for String)
         */
        @Test
        @DisplayName("getProductInBatches - Invalid operator for type - Throws BadRequest")
        void getProductInBatches_InvalidOperatorForType_ThrowsBadRequest() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("title", "greaterThan", "val")));

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productService.getProductInBatches(request));
                assertTrue(exception.getMessage().contains("greaterThan"));
        }

        /*
         * Purpose: Verify failure on malformed operator string
         */
        @Test
        @DisplayName("getProductInBatches - Malformed operator - Throws BadRequest")
        void getProductInBatches_MalformedOperator_ThrowsBadRequest() {
                // Arrange
                PaginationBaseRequestModel request = createValidPaginationRequest();
                request.setFilters(Collections.singletonList(createFilter("title", "unknown_op", "val")));

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productService.getProductInBatches(request));
                assertTrue(exception.getMessage().contains("unknown_op"));
        }

        // ==========================================
        // SECTION 3: PERMISSION / DELEGATION
        // ==========================================

        @Test
        @DisplayName("getProductInBatches - Verify @PreAuthorize annotation")
        void getProductInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                Method method = ProductController.class.getMethod("getProductInBatches",
                                PaginationBaseRequestModel.class);
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertNotNull(annotation);
                assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION));
        }

        @Test
        @DisplayName("getProductInBatches - Controller delegation check")
        void getProductInBatches_ControllerDelegation_Success() {
                // Arrange
                ProductService mockService = mock(ProductService.class);
                ProductController controller = new ProductController(mockService);
                PaginationBaseRequestModel request = createValidPaginationRequest();
                when(mockService.getProductInBatches(request)).thenReturn(new PaginationBaseResponseModel<>());

                // Act
                ResponseEntity<?> response = controller.getProductInBatches(request);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
                verify(mockService).getProductInBatches(request);
        }

        @Test
        @DisplayName("getProductInBatches - No permission - Unauthorized")
        void getProductInBatches_NoPermission_Unauthorized() {
                // Arrange
                ProductService mockService = mock(ProductService.class);
                ProductController controller = new ProductController(mockService);
                PaginationBaseRequestModel request = createValidPaginationRequest();
                doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(mockService).getProductInBatches(request);

                // Act
                ResponseEntity<?> response = controller.getProductInBatches(request);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
                verify(mockService).getProductInBatches(request);
        }

        private PaginationBaseRequestModel.FilterCondition createFilter(String col, String op, String val) {
                PaginationBaseRequestModel.FilterCondition f = new PaginationBaseRequestModel.FilterCondition();
                f.setColumn(col);
                f.setOperator(op);
                f.setValue(val);
                return f;
        }
}
