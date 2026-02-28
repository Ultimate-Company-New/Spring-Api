package springapi.ServiceTests.Shipping;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.ShippingController;
import springapi.helpers.PackagingHelper;
import springapi.models.databasemodels.PackagePickupLocationMapping;
import springapi.models.databasemodels.ProductPickupLocationMapping;
import springapi.models.responsemodels.OrderOptimizationResponseModel;
import springapi.models.shippingresponsemodel.ShippingOptionsResponseModel;

/** Tests for ShippingService.optimizeOrder(). */
@DisplayName("OptimizeOrder Tests")
class OptimizeOrderTest extends ShippingServiceTestBase {

  // Total Tests: 26
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */
  /**
   * Purpose: Verify optimization succeeds with custom allocations. Expected Result: Response
   * success is true. Assertions: success flag true.
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
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
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
   * Purpose: Verify optimization succeeds with valid request. Expected Result: Response success is
   * true. Assertions: success flag true.
   */
  @Test
  @DisplayName("optimizeOrder - Valid Request - Success")
  void optimizeOrder_ValidRequest_Success() {
    // Arrange
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
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
   * Purpose: Verify empty delivery postcode returns error. Expected Result: Response success is
   * false. Assertions: Error message is DeliveryPostcodeRequired.
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
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.DELIVERY_POSTCODE_REQUIRED,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify empty product quantities returns error. Expected Result: Response success is
   * false. Assertions: Error message is NoProductsSpecified.
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
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_PRODUCTS_SPECIFIED,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify exception path returns OptimizationFailedFormat. Expected Result: Response
   * success is false. Assertions: Error message matches OptimizationFailedFormat.
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
    assertEquals(
        String.format(
            ErrorMessages.OrderOptimizationErrorMessages.OPTIMIZATION_FAILED_FORMAT,
            ErrorMessages.OPERATION_FAILED),
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify missing product in repository triggers ProductNotFound error. Expected Result:
   * Response success is false. Assertions: Error message matches ProductNotFoundFormat.
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
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertEquals(
        String.format(ErrorMessages.OrderOptimizationErrorMessages.PRODUCT_NOT_FOUND_FORMAT, 999L),
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify no packages configured returns error. Expected Result: Response success is
   * false. Assertions: Error message is NoPackagesConfiguredFormat.
   */
  @Test
  @DisplayName("optimizeOrder - No Packages Configured - Error")
  void optimizeOrder_NoPackagesConfigured_Error() {
    // Arrange
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(List.of());
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertEquals(
        String.format(
            ErrorMessages.OrderOptimizationErrorMessages.NO_PACKAGES_CONFIGURED_FORMAT,
            testProduct.getTitle(),
            10,
            2),
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify no valid products found returns error. Expected Result: Response success is
   * false. Assertions: Error message is NoValidProductsFound.
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
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_VALID_PRODUCTS_FOUND,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify null delivery postcode returns error. Expected Result: Response success is
   * false. Assertions: Error message is DeliveryPostcodeRequired.
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
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.DELIVERY_POSTCODE_REQUIRED,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify null product quantities returns error. Expected Result: Response success is
   * false. Assertions: Error message is NoProductsSpecified.
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
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_PRODUCTS_SPECIFIED,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify custom allocation with unknown product returns validation error. Expected
   * Result: Response success is false. Assertions: Error contains unknown product id.
   */
  @Test
  @DisplayName("optimizeOrder - Custom Unknown Product - Error")
  void optimizeOrder_CustomUnknownProduct_Error() {
    // Arrange
    optimizationRequest.setCustomAllocations(Map.of(999L, Map.of(TEST_PICKUP_LOCATION_ID, 1)));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(List.of());

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertTrue(result.getErrorMessage().contains("Product ID 999 not found"));
  }

  /**
   * Purpose: Verify custom allocation with unknown location returns validation error. Expected
   * Result: Response success is false. Assertions: Error contains unknown location id.
   */
  @Test
  @DisplayName("optimizeOrder - Custom Unknown Location - Error")
  void optimizeOrder_CustomUnknownLocation_Error() {
    // Arrange
    optimizationRequest.setCustomAllocations(Map.of(TEST_PRODUCT_ID, Map.of(999L, 1)));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(List.of());

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertTrue(result.getErrorMessage().contains("Location ID 999 not found"));
  }

  /**
   * Purpose: Verify custom allocation with insufficient stock returns validation error. Expected
   * Result: Response success is false. Assertions: Error contains insufficient stock details.
   */
  @Test
  @DisplayName("optimizeOrder - Custom Insufficient Stock - Error")
  void optimizeOrder_CustomInsufficientStock_Error() {
    // Arrange
    optimizationRequest.setCustomAllocations(
        Map.of(TEST_PRODUCT_ID, Map.of(TEST_PICKUP_LOCATION_ID, 11)));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(10, 10));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertTrue(result.getErrorMessage().contains("Insufficient stock"));
  }

  /**
   * Purpose: Verify custom allocation with only zero quantities returns validation error. Expected
   * Result: Response success is false. Assertions: Error equals NoValidAllocationsSpecified.
   */
  @Test
  @DisplayName("optimizeOrder - Custom No Valid Quantities - Error")
  void optimizeOrder_CustomNoValidQuantities_Error() {
    // Arrange
    optimizationRequest.setCustomAllocations(
        Map.of(TEST_PRODUCT_ID, Map.of(TEST_PICKUP_LOCATION_ID, 0)));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(List.of());

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_VALID_ALLOCATIONS_SPECIFIED,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify route with no couriers marks response as unavailable. Expected Result: Response
   * success is true with courier unavailability metadata. Assertions: allCouriersAvailable false
   * and error message populated.
   */
  @Test
  @DisplayName("optimizeOrder - No Courier Options - Unavailable Strategy")
  void optimizeOrder_NoCourierOptions_UnavailableStrategy() {
    // Arrange
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
    ShippingOptionsResponseModel emptyOptions = new ShippingOptionsResponseModel();
    ShippingOptionsResponseModel.Data data = new ShippingOptionsResponseModel.Data();
    data.setAvailableCourierCompanies(List.of());
    emptyOptions.setData(data);
    stubShipRocketHelperGetAvailableShippingOptions(emptyOptions);

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertFalse(result.getAllCouriersAvailable());
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_SHIPPING_OPTIONS_FOR_ANY_STRATEGY,
        result.getErrorMessage());
    assertNotNull(result.getUnavailabilityReason());
  }

  /**
   * Purpose: Verify heavy shipments are split when route max weight is limited. Expected Result:
   * Optimization succeeds with multiple shipments. Assertions: shipment count is greater than one.
   */
  @Test
  @DisplayName("optimizeOrder - Split Shipments By Route Max Weight - Success")
  void optimizeOrder_SplitShipmentsByRouteMaxWeight_Success() {
    // Arrange
    testProduct.setWeightKgs(new BigDecimal("60"));
    optimizationRequest.setProductQuantities(Map.of(TEST_PRODUCT_ID, 3));
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 3);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    springapi.models.databasemodels.Package heavyPackage =
        new springapi.models.databasemodels.Package();
    heavyPackage.setPackageId(TEST_PACKAGE_ID);
    heavyPackage.setPackageName("HEAVY-BOX");
    heavyPackage.setPackageType("BOX");
    heavyPackage.setLength(100);
    heavyPackage.setBreadth(100);
    heavyPackage.setHeight(100);
    heavyPackage.setMaxWeight(new BigDecimal("500"));
    heavyPackage.setPricePerUnit(new BigDecimal("5"));
    PackagePickupLocationMapping packageMapping = new PackagePickupLocationMapping();
    packageMapping.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
    packageMapping.setPackageId(TEST_PACKAGE_ID);
    packageMapping.setAvailableQuantity(10);
    packageMapping.setPackageEntity(heavyPackage);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(packageMapping));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(3, 3));

    PackagingHelper.MultiProductPackageUsageResult usage =
        new PackagingHelper.MultiProductPackageUsageResult(
            TEST_PACKAGE_ID, "PKG", "BOX", 3, new BigDecimal("5"), Map.of(TEST_PRODUCT_ID, 3));
    PackagingHelper.MultiProductPackagingResult multiProductPackagingResult =
        new PackagingHelper.MultiProductPackagingResult(
            List.of(usage), Map.of(TEST_PRODUCT_ID, 3), Map.of(TEST_PRODUCT_ID, 3));
    stubPackagingHelperCalculatePackagingForMultipleProducts(multiProductPackagingResult);

    ShippingOptionsResponseModel emptyOptions = new ShippingOptionsResponseModel();
    ShippingOptionsResponseModel.Data emptyData = new ShippingOptionsResponseModel.Data();
    emptyData.setAvailableCourierCompanies(List.of());
    emptyOptions.setData(emptyData);

    Map<String, ShippingOptionsResponseModel> optionsByRouteAndWeight = new HashMap<>();
    optionsByRouteAndWeight.put("*|500.0", emptyOptions);
    optionsByRouteAndWeight.put("*|400.0", emptyOptions);
    optionsByRouteAndWeight.put("*|300.0", emptyOptions);
    optionsByRouteAndWeight.put("*|200.0", emptyOptions);
    optionsByRouteAndWeight.put("*|100.0", createShippingOptions(20.0));
    stubShipRocketHelperGetAvailableShippingOptionsByRouteAndWeight(
        optionsByRouteAndWeight, createShippingOptions(20.0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertTrue(result.getShipmentCount() > 1);
  }

  /**
   * Purpose: Verify product exceeding package limits returns explicit error. Expected Result:
   * Response success is false. Assertions: Error message matches ProductExceedsPackageLimitsFormat.
   */
  @Test
  @DisplayName("optimizeOrder - Product Exceeds Package Limits - Error")
  void optimizeOrder_ProductExceedsPackageLimits_Error() {
    // Arrange
    testProduct.setLength(new BigDecimal("999"));
    testProduct.setBreadth(new BigDecimal("999"));
    testProduct.setHeight(new BigDecimal("999"));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 5);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 5)));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertEquals(
        String.format(
            ErrorMessages.OrderOptimizationErrorMessages.PRODUCT_EXCEEDS_PACKAGE_LIMITS_FORMAT,
            testProduct.getTitle(),
            5,
            2),
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify no available package quantities returns explicit error. Expected Result:
   * Response success is false. Assertions: Error message matches NoPackagesAvailableFormat.
   */
  @Test
  @DisplayName("optimizeOrder - No Available Package Quantities - Error")
  void optimizeOrder_NoAvailablePackageQuantities_Error() {
    // Arrange
    optimizationRequest.setProductQuantities(Map.of(TEST_PRODUCT_ID, 2));
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 1);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 0)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(1, 0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertFalse(result.getSuccess());
    assertEquals(
        String.format(
            ErrorMessages.OrderOptimizationErrorMessages.NO_PACKAGES_AVAILABLE_FORMAT,
            testProduct.getTitle(),
            1,
            2),
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify token retrieval failure does not break optimization flow. Expected Result:
   * Response success remains true. Assertions: success flag true.
   */
  @Test
  @DisplayName("optimizeOrder - ShipRocket Token Failure - Success")
  void optimizeOrder_ShipRocketTokenFailure_Success() {
    // Arrange
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
    stubShipRocketHelperGetTokenThrows(new RuntimeException("token-error"));
    stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
  }

  /**
   * Purpose: Verify custom allocation retains unserviceable-route reason when no courier exists for
   * route. Expected Result: Response success true with allCouriersAvailable false. Assertions:
   * Unavailability reason and error message are populated.
   */
  @Test
  @DisplayName("optimizeOrder - Custom Unserviceable Route - Unavailable")
  void optimizeOrder_CustomUnserviceableRoute_Unavailable() {
    // Arrange
    optimizationRequest.setCustomAllocations(
        Map.of(TEST_PRODUCT_ID, Map.of(TEST_PICKUP_LOCATION_ID, 1)));
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
    stubShipRocketHelperGetAvailableShippingOptions(createNoCourierOptions());

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertFalse(result.getAllCouriersAvailable());
    assertNotNull(result.getUnavailabilityReason());
    assertEquals(
        ErrorMessages.OrderOptimizationErrorMessages.NO_SHIPPING_OPTIONS_FOR_ANY_STRATEGY,
        result.getErrorMessage());
  }

  /**
   * Purpose: Verify package-less shipment candidates are marked unavailable with clear reason.
   * Expected Result: Response success true with no-courier availability. Assertions: Unavailability
   * reason includes package-fit warning.
   */
  @Test
  @DisplayName("optimizeOrder - No Package Usage - Unavailable")
  void optimizeOrder_NoPackageUsage_Unavailable() {
    // Arrange
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    PackagingHelper.MultiProductPackagingResult emptyPackages =
        new PackagingHelper.MultiProductPackagingResult(
            List.of(), Map.of(TEST_PRODUCT_ID, 2), Map.of(TEST_PRODUCT_ID, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(emptyPackages);
    stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertFalse(result.getAllCouriersAvailable());
    assertNotNull(result.getUnavailabilityReason());
    assertTrue(result.getUnavailabilityReason().contains("No packages available at"));
  }

  /**
   * Purpose: Verify missing pickup postal code marks candidate unavailable with explicit reason.
   * Expected Result: Response success true with courier unavailability metadata. Assertions:
   * Unavailability reason mentions missing postal code.
   */
  @Test
  @DisplayName("optimizeOrder - Missing Pickup Postal Code - Unavailable")
  void optimizeOrder_MissingPickupPostalCode_Unavailable() {
    // Arrange
    testPickupAddress.setPostalCode(null);
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
    stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertFalse(result.getAllCouriersAvailable());
    assertNotNull(result.getUnavailabilityReason());
    assertTrue(result.getUnavailabilityReason().contains("Missing postal code for shipment"));
  }

  /**
   * Purpose: Verify route probing handles provider exceptions and continues optimization. Expected
   * Result: Response success true with unavailability metadata. Assertions: Response remains
   * non-null and marked unavailable.
   */
  @Test
  @DisplayName("optimizeOrder - Route Probe Exception - Unavailable")
  void optimizeOrder_RouteProbeException_Unavailable() {
    // Arrange
    optimizationRequest.setCustomAllocations(
        Map.of(TEST_PRODUCT_ID, Map.of(TEST_PICKUP_LOCATION_ID, 1)));
    stubClientServiceGetClientById(testClientResponse);
    stubProductRepositoryFindAllById(List.of(testProduct));
    ProductPickupLocationMapping mapping =
        createProductPickupLocationMapping(TEST_PRODUCT_ID, TEST_PICKUP_LOCATION_ID, 10);
    stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
        List.of(mapping));
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
        List.of(createPackagePickupLocationMapping(TEST_PACKAGE_ID, TEST_PICKUP_LOCATION_ID, 10)));
    stubPackagingHelperCalculatePackaging(createPackagingEstimateResult(2, 2));
    stubPackagingHelperCalculatePackagingForMultipleProducts(createMultiProductPackagingResult());
    stubShipRocketHelperGetAvailableShippingOptionsThrows(new RuntimeException("shiprocket-down"));

    // Act
    OrderOptimizationResponseModel result = shippingService.optimizeOrder(optimizationRequest);

    // Assert
    assertTrue(result.getSuccess());
    assertFalse(result.getAllCouriersAvailable());
    assertNotNull(result.getUnavailabilityReason());
  }

  private ShippingOptionsResponseModel createNoCourierOptions() {
    ShippingOptionsResponseModel response = new ShippingOptionsResponseModel();
    ShippingOptionsResponseModel.Data data = new ShippingOptionsResponseModel.Data();
    data.setAvailableCourierCompanies(List.of());
    response.setData(data);
    return response;
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
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
}
