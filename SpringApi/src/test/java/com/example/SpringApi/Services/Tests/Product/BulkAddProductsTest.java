package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.SpringApi.Services.ProductService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.bulkAddProducts.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 7
@DisplayName("ProductService - BulkAddProducts Tests")
class BulkAddProductsTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify success when all products are valid
     */
    @Test
    @DisplayName("bulkAddProducts - All products valid - Success")
    void bulkAddProducts_AllValid_Success() {
        // Arrange
        List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
        stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
        stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
        stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);
        stubProductRepositorySave(testProduct);

        // Act
        try (MockedConstruction<ImgbbHelper> imgbbMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
            BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

            // Assert
            assertEquals(1, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }
    }

    /*
     * Purpose: Verify partial success when one product is invalid
     */
    @Test
    @DisplayName("bulkAddProducts - Partial success - One failure")
    void bulkAddProducts_PartialSuccess_Success() {
        // Arrange
        ProductRequestModel invalid = new ProductRequestModel(); // Missing title
        invalid.setClientId(TEST_CLIENT_ID);
        List<ProductRequestModel> requests = new ArrayList<>();
        requests.add(testProductRequest);
        requests.add(invalid);

        stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
        stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
        stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);
        stubProductRepositorySave(testProduct);

        // Act
        try (MockedConstruction<ImgbbHelper> imgbbMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
            BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

            // Assert
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }
    }

    // ==========================================
    // SECTION 2: FAILURE TESTS
    // ==========================================

    /*
     * Purpose: Verify failure when list is empty
     */
    @Test
    @DisplayName("bulkAddProducts - Empty list - Throws BadRequest")
    void bulkAddProducts_EmptyList_ThrowsBadRequest() {
        assertThrowsBadRequest(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Product"),
                () -> productService.bulkAddProducts(new ArrayList<>()));
    }

    /*
     * Purpose: Verify failure when list is null
     */
    @Test
    @DisplayName("bulkAddProducts - Null list - Throws BadRequest")
    void bulkAddProducts_NullList_ThrowsBadRequest() {
        assertThrowsBadRequest(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Product"),
                () -> productService.bulkAddProducts(null));
    }

    /*
     * Purpose: Verify failure on database error
     */
    @Test
    @DisplayName("bulkAddProducts - Database error - Failure recorded")
    void bulkAddProducts_DatabaseError_Success() {
        // Arrange
        List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
        stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
        when(productRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        // Act
        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    // ==========================================
    // SECTION 3: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("bulkAddProducts - Verify @PreAuthorize annotation")
    void bulkAddProducts_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("bulkAddProducts", java.util.List.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.INSERT_PRODUCTS_PERMISSION));
    }

    @Test
    @DisplayName("bulkAddProducts - No permission - Unauthorized")
    void bulkAddProducts_NoPermission_Unauthorized() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
        doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockService).bulkAddProducts(requests);

        // Act
        ResponseEntity<?> response = controller.bulkAddProducts(requests);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(mockService).bulkAddProducts(requests);
    }
}
