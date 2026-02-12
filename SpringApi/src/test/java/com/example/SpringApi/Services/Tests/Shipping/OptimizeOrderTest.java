package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.optimizeOrder().
 */
@DisplayName("OptimizeOrder Tests")
class OptimizeOrderTest extends ShippingServiceTestBase {

    // Total Tests: 12

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */
/**
     * Purpose: Verify optimization succeeds with custom allocations.
     * Expected Result: Response success is true.
     * Assertions: success flag true.
     */
    @Test
    @DisplayName("optimizeOrder - Custom Allocation - Success")
    void optimizeOrder_CustomAllocation_Success() {
        // Arrange
        Map<Long, Map<Long, Integer>> customAlloc = new HashMap<>();
        customAlloc.put(TEST_PRODUCT_ID, Map.of(TEST_PICKUP_LOCATION_ID, 1));
        optimizationRequest.setCustomAllocations(customAlloc);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindAllById(List.of(testProduct));
        ProductPickupLocationMapping mapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(List.of(mapping));
        stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
                List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
        stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(1, 1));
        stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertTrue(result.getSuccess());
    }
/**
     * Purpose: Verify optimization succeeds with valid request.
     * Expected Result: Response success is true.
     * Assertions: success flag true.
     */
    @Test
    @DisplayName("optimizeOrder - Valid Request - Success")
    void optimizeOrder_ValidRequest_Success() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindAllById(List.of(testProduct));
        ProductPickupLocationMapping mapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(List.of(mapping));
        stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
                List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
        stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
        stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertTrue(result.getSuccess());
    }
/*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */
/**
     * Purpose: Verify empty delivery postcode returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is DeliveryPostcodeRequired.
     */
    @Test
    @DisplayName("optimizeOrder - Empty Delivery Postcode - Error")
    void optimizeOrder_EmptyDeliveryPostcode_Error() {
        // Arrange
        optimizationRequest.setDeliveryPostcode("");

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(ErrorMessages.OrderOptimizationErrorMessages.DeliveryPostcodeRequired, result.getErrorMessage());
    }
/**
     * Purpose: Verify empty product quantities returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is NoProductsSpecified.
     */
    @Test
    @DisplayName("optimizeOrder - Empty Product Quantities - Error")
    void optimizeOrder_EmptyProductQuantities_Error() {
        // Arrange
        optimizationRequest.setProductQuantities(new HashMap<>());

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(ErrorMessages.OrderOptimizationErrorMessages.NoProductsSpecified, result.getErrorMessage());
    }
/**
     * Purpose: Verify exception path returns OptimizationFailedFormat.
     * Expected Result: Response success is false.
     * Assertions: Error message matches OptimizationFailedFormat.
     */
    @Test
    @DisplayName("optimizeOrder - Exception - Error")
    void optimizeOrder_Exception_Error() {
        // Arrange
        stubProductRepositoryFindAllByIdThrows(new RuntimeException(ErrorMessages.OPERATION_FAILED));

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(String.format(ErrorMessages.OrderOptimizationErrorMessages.OptimizationFailedFormat,
            ErrorMessages.OPERATION_FAILED), result.getErrorMessage());
    }
/**
     * Purpose: Verify missing product in repository triggers ProductNotFound error.
     * Expected Result: Response success is false.
     * Assertions: Error message matches ProductNotFoundFormat.
     */
    @Test
    @DisplayName("optimizeOrder - Missing Product - Error")
    void optimizeOrder_MissingProduct_Error() {
        // Arrange
        Map<Long, Integer> quantities = new HashMap<>();
        quantities.put(TEST_PRODUCT_ID, 1);
        quantities.put(999L, 1);
        optimizationRequest.setProductQuantities(quantities);
        stubProductRepositoryFindAllById(List.of(testProduct));
        ProductPickupLocationMapping mapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(List.of(mapping));
        stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
                List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
        stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(String.format(ErrorMessages.OrderOptimizationErrorMessages.ProductNotFoundFormat, 999L),
                result.getErrorMessage());
    }
/**
     * Purpose: Verify no packages configured returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is NoPackagesConfiguredFormat.
     */
    @Test
    @DisplayName("optimizeOrder - No Packages Configured - Error")
    void optimizeOrder_NoPackagesConfigured_Error() {
        // Arrange
        stubProductRepositoryFindAllById(List.of(testProduct));
        ProductPickupLocationMapping mapping = createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
        stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(List.of(mapping));
        stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(List.of());
        stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 0));

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(String.format(ErrorMessages.OrderOptimizationErrorMessages.NoPackagesConfiguredFormat,
                testProduct.getTitle(), 10, 2), result.getErrorMessage());
    }
/**
     * Purpose: Verify no valid products found returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is NoValidProductsFound.
     */
    @Test
    @DisplayName("optimizeOrder - No Valid Products - Error")
    void optimizeOrder_NoValidProducts_Error() {
        // Arrange
        stubProductRepositoryFindAllById(List.of());

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(ErrorMessages.OrderOptimizationErrorMessages.NoValidProductsFound, result.getErrorMessage());
    }
/**
     * Purpose: Verify null delivery postcode returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is DeliveryPostcodeRequired.
     */
    @Test
    @DisplayName("optimizeOrder - Null Delivery Postcode - Error")
    void optimizeOrder_NullDeliveryPostcode_Error() {
        // Arrange
        optimizationRequest.setDeliveryPostcode(null);

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(ErrorMessages.OrderOptimizationErrorMessages.DeliveryPostcodeRequired, result.getErrorMessage());
    }
/**
     * Purpose: Verify null product quantities returns error.
     * Expected Result: Response success is false.
     * Assertions: Error message is NoProductsSpecified.
     */
    @Test
    @DisplayName("optimizeOrder - Null Product Quantities - Error")
    void optimizeOrder_NullProductQuantities_Error() {
        // Arrange
        optimizationRequest.setProductQuantities(null);

        // Act
        OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

        // Assert
        assertFalse(result.getSuccess());
        assertEquals(ErrorMessages.OrderOptimizationErrorMessages.NoProductsSpecified, result.getErrorMessage());
    }
/*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("optimizeOrder - Controller Permission - Unauthorized")
    void optimizeOrder_controller_permission_unauthorized() {
        // Arrange
        ShippingController controller = new ShippingController(shippingServiceMock);
        stubShippingServiceMockOptimizeOrderUnauthorized();

        // Act
        ResponseEntity<?> response = controller.optimizeOrder(optimizationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for optimizeOrder.
     * Expected Result: Annotation exists and includes VIEW_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("optimizeOrder - Verify @PreAuthorize Annotation")
    void optimizeOrder_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("optimizeOrder", OrderOptimizationRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION));
    }
}
