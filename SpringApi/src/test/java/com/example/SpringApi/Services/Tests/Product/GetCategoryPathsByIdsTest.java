package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.SpringApi.Services.ProductService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.getCategoryPathsByIds.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 7
@DisplayName("ProductService - GetCategoryPathsByIds Tests")
class GetCategoryPathsByIdsTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify path building for multiple levels
     */
    @Test
    @DisplayName("getCategoryPathsByIds - Multiple levels - Success")
    void getCategoryPathsByIds_MultipleLevels_Success() {
        // Arrange
        ProductCategory parent = new ProductCategory();
        parent.setName("Electronics");
        testCategory.setParent(parent);
        testCategory.setParentId(1L); // Assume parent has ID 1
        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        // Act
        Map<Long, String> results = productService
                .getCategoryPathsByIds(Collections.singletonList(TEST_CATEGORY_ID));

        // Assert
        assertEquals(1, results.size());
        assertEquals("Electronics > Test Category", results.get(TEST_CATEGORY_ID));
    }

    /*
     * Purpose: Verify path building for root level
     */
    @Test
    @DisplayName("getCategoryPathsByIds - Root level - Success")
    void getCategoryPathsByIds_RootLevel_Success() {
        // Arrange
        testCategory.setParent(null);
        testCategory.setParentId(null);
        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));

        // Act
        Map<Long, String> results = productService
                .getCategoryPathsByIds(Collections.singletonList(TEST_CATEGORY_ID));

        // Assert
        assertEquals("Test Category", results.get(TEST_CATEGORY_ID));
    }

    /*
     * Purpose: Verify empty input behavior
     */
    @Test
    @DisplayName("getCategoryPathsByIds - Empty input - Success")
    void getCategoryPathsByIds_EmptyInput_Success() {
        // Act
        Map<Long, String> result = productService
                .getCategoryPathsByIds(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
    }

    /*
     * Purpose: Verify null list input behavior
     */
    @Test
    @DisplayName("getCategoryPathsByIds - Null list - Success")
    void getCategoryPathsByIds_NullList_Success() {
        // Act
        Map<Long, String> result = productService.getCategoryPathsByIds(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ==========================================
    // SECTION 2: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("getCategoryPathsByIds - Verify @PreAuthorize annotation")
    void getCategoryPathsByIds_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("getCategoryPathsByIds", List.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION));
    }

    @Test
    @DisplayName("getCategoryPathsByIds - Controller delegation check")
    void getCategoryPathsByIds_ControllerDelegation_Success() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        when(mockService.getCategoryPathsByIds(any())).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<?> response = controller.getCategoryPathsByIds(Collections.singletonList(1L));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockService).getCategoryPathsByIds(any());
    }

    @Test
    @DisplayName("getCategoryPathsByIds - No permission - Unauthorized")
    void getCategoryPathsByIds_NoPermission_Unauthorized() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        when(mockService.getCategoryPathsByIds(any()))
                .thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED));

        // Act
        ResponseEntity<?> response = controller.getCategoryPathsByIds(Collections.singletonList(1L));

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(mockService).getCategoryPathsByIds(any());
    }

    @Test
    @DisplayName("getCategoryPathsByIds - Invalid ID in list - Skips")
    void getCategoryPathsByIds_InvalidId_Skips() {
        // Arrange
        when(productCategoryRepository.findById(999L)).thenReturn(Optional.empty());
        // Act
        Map<Long, String> result = productService
                .getCategoryPathsByIds(Collections.singletonList(999L));
        // Assert
        assertTrue(result.isEmpty());
    }

}
