package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel;
import org.hibernate.exception.SQLGrammarException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Test class for ProductService.getProductStockAtLocationsByProductId.
 */
@DisplayName("ProductService - GetProductStockAtLocationsByProductId Tests")
class GetProductStockAtLocationsByProductIdTest extends ProductServiceTestBase {

    // Total Tests: 6
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify stock results returned when mappings exist.
     * Expected Result: Non-empty response list.
     * Assertions: Response list size matches mappings.
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - Valid product - Success")
    void getProductStockAtLocationsByProductId_ValidProduct_Success() {
        // Arrange
        stubProductRepositoryFindById(TEST_PRODUCT_ID, testProduct);
        ProductPickupLocationMapping mapping = new ProductPickupLocationMapping();
        mapping.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        mapping.setAvailableStock(5);
        mapping.setPickupLocation(testPickupLocation);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
                TEST_PRODUCT_ID, Collections.singletonList(mapping));
        stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
                Collections.singletonList(TEST_PICKUP_LOCATION_ID), Collections.emptyList());

        // Act
        List<ProductStockByLocationResponseModel> results = productService
                .getProductStockAtLocationsByProductId(TEST_PRODUCT_ID, 1, "12345", false);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify InvalidDataAccessResourceUsageException is handled.
     * Expected Result: Empty list returned.
     * Assertions: Result list is empty.
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - Invalid data access - Returns empty")
    void getProductStockAtLocationsByProductId_f01_InvalidDataAccess_ReturnsEmpty() {
        // Arrange
        stubProductRepositoryFindById(TEST_PRODUCT_ID, testProduct);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddressThrows(
                new InvalidDataAccessResourceUsageException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act
        List<ProductStockByLocationResponseModel> results = productService
                .getProductStockAtLocationsByProductId(TEST_PRODUCT_ID, 1, "12345", false);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Purpose: Verify SQLGrammarException is handled.
     * Expected Result: Empty list returned.
     * Assertions: Result list is empty.
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - SQL grammar error - Returns empty")
    void getProductStockAtLocationsByProductId_f02_SqlGrammar_ReturnsEmpty() {
        // Arrange
        stubProductRepositoryFindById(TEST_PRODUCT_ID, testProduct);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddressThrows(
                new SQLGrammarException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR,
                        new SQLException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR)));

        // Act
        List<ProductStockByLocationResponseModel> results = productService
                .getProductStockAtLocationsByProductId(TEST_PRODUCT_ID, 1, "12345", false);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Purpose: Verify generic exception is handled.
     * Expected Result: Empty list returned.
     * Assertions: Result list is empty.
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - Generic exception - Returns empty")
    void getProductStockAtLocationsByProductId_f03_GenericException_ReturnsEmpty() {
        // Arrange
        stubProductRepositoryFindById(TEST_PRODUCT_ID, testProduct);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddressThrows(
                new RuntimeException(ErrorMessages.CommonErrorMessages.CRITICAL_FAILURE));

        // Act
        List<ProductStockByLocationResponseModel> results = productService
                .getProductStockAtLocationsByProductId(TEST_PRODUCT_ID, 1, "12345", false);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify controller permission test for unauthorized access.
     * Expected Result: Error response returned when service throws unauthorized.
     * Assertions: Response status is INTERNAL_SERVER_ERROR (unauthorized not explicitly handled).
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - Controller permission unauthorized - Success")
    void getProductStockAtLocationsByProductId_controller_permission_unauthorized() {
        // Arrange
        ProductController controller = new ProductController(productServiceMock);
        stubProductServiceGetProductStockAtLocationsByProductIdThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getProductStockAtLocationsByProductId(
                TEST_PRODUCT_ID, 1, "12345", false);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(productServiceMock).getProductStockAtLocationsByProductId(anyLong(), any(), any(), any());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation exists.
     * Expected Result: Annotation includes required permissions.
     * Assertions: Annotation contains VIEW_PRODUCTS_PERMISSION.
     */
    @Test
    @DisplayName("getProductStockAtLocationsByProductId - Verify @PreAuthorize annotation - Success")
    void getProductStockAtLocationsByProductId_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ProductController.class.getMethod("getProductStockAtLocationsByProductId",
                Long.class, Integer.class, String.class, Boolean.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION));
    }
}
