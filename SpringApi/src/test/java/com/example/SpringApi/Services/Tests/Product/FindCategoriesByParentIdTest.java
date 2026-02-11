package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import com.example.SpringApi.Models.ResponseModels.ProductCategoryWithPathResponseModel;
import com.example.SpringApi.Controllers.ProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.example.SpringApi.Services.ProductService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.findCategoriesByParentId.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 6
@DisplayName("ProductService - FindCategoriesByParentId Tests")
class FindCategoriesByParentIdTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify root level categories retrieval
     */
    @Test
    @DisplayName("findCategoriesByParentId - Parent null - Success")
    void findCategoriesByParentId_Root_Success() {
        // Arrange
        testCategory.setParentId(null);
        when(productCategoryRepository.findAll())
                .thenReturn(Collections.singletonList(testCategory));

        // Act
        List<ProductCategoryWithPathResponseModel> results = productService.findCategoriesByParentId(null);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Test Category", results.get(0).getName());
    }

    /*
     * Purpose: Verify child categories retrieval
     */
    @Test
    @DisplayName("findCategoriesByParentId - Valid parent ID - Success")
    void findCategoriesByParentId_Child_Success() {
        // Arrange
        ProductCategory child = new ProductCategory();
        child.setCategoryId(11L);
        child.setName("Child Category");
        child.setParentId(10L);
        child.setIsEnd(true);
        when(productCategoryRepository.findAll())
                .thenReturn(Collections.singletonList(child));
        // Mocking buildFullPath internal calls
        when(productCategoryRepository.findById(10L)).thenReturn(Optional.empty()); // Stop path traversal

        // Act
        List<ProductCategoryWithPathResponseModel> results = productService.findCategoriesByParentId(10L);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Child Category", results.get(0).getName());
    }

    // ==========================================
    // SECTION 2: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("findCategoriesByParentId - Verify @PreAuthorize annotation")
    void findCategoriesByParentId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("findCategoriesByParentId", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION));
    }

    @Test
    @DisplayName("findCategoriesByParentId - Controller delegation check")
    void findCategoriesByParentId_ControllerDelegation_Success() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        when(mockService.findCategoriesByParentId(any())).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<?> response = controller.findCategoriesByParentId(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockService).findCategoriesByParentId(null);
    }

    @Test
    @DisplayName("findCategoriesByParentId - Empty results - Success")
    void findCategoriesByParentId_EmptyResults_Success() {
        // Arrange
        when(productCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        // Act
        List<ProductCategoryWithPathResponseModel> results = productService.findCategoriesByParentId(null);
        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("findCategoriesByParentId - Invalid parent ID (not in DB) - Success (Empty)")
    void findCategoriesByParentId_InvalidParentId_Success() {
        // Arrange
        when(productCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        // Act
        List<ProductCategoryWithPathResponseModel> results = productService.findCategoriesByParentId(999L);
        // Assert
        assertTrue(results.isEmpty());
    }
}
