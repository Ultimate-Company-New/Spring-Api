package com.example.springapi.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PackagingHelper Tests")
class PackagingHelperTest {

  // Total Tests: 10

  /**
   * Purpose: Verify zero or negative requested quantity returns empty packaging result. Expected
   * Result: No packages used and canPackAllItems true. Assertions: Empty packages list, zero
   * totals, and null error message.
   */
  @Test
  @DisplayName("calculatePackaging - Zero Quantity Returns Empty Result - Success")
  void calculatePackaging_s01_zeroQuantityReturnsEmptyResult_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();
    PackagingHelper.ProductDimension product =
        new PackagingHelper.ProductDimension(
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0);

    // Act
    PackagingHelper.PackagingEstimateResult result = helper.calculatePackaging(product, List.of());

    // Assert
    assertTrue(result.getPackagesUsed().isEmpty());
    assertEquals(0, result.getTotalPackagesUsed());
    assertEquals(BigDecimal.ZERO, result.getTotalPackagingCost());
    assertTrue(result.isCanPackAllItems());
    assertNull(result.getErrorMessage());
  }

  /**
   * Purpose: Verify algorithm prefers cost-efficient package and packs all items when capacity
   * allows. Expected Result: Cheapest suitable package type is used with correct quantity and cost.
   * Assertions: Package ID usage, count, and total cost.
   */
  @Test
  @DisplayName("calculatePackaging - Packs All Using Cost Efficient Package - Success")
  void calculatePackaging_s02_packsAllUsingCostEfficientPackage_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();
    PackagingHelper.ProductDimension product =
        new PackagingHelper.ProductDimension(
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 3);

    PackagingHelper.PackageDimension cheap =
        packageDim(1L, "Cheap Box", 2, 1, 1, "SMALL", "2", "1", 2);
    PackagingHelper.PackageDimension expensive =
        packageDim(2L, "Expensive Box", 5, 2, 1, "LARGE", "10", "20", 5);

    // Act
    PackagingHelper.PackagingEstimateResult result =
        helper.calculatePackaging(product, List.of(cheap, expensive));

    // Assert
    assertTrue(result.isCanPackAllItems());
    assertEquals(2, result.getTotalPackagesUsed());
    assertEquals(new BigDecimal("2"), result.getTotalPackagingCost());
    assertEquals(1, result.getPackagesUsed().size());
    assertEquals(1L, result.getPackagesUsed().get(0).getPackageId());
    assertEquals(2, result.getPackagesUsed().get(0).getQuantityUsed());
  }

  /**
   * Purpose: Verify insufficient package supply returns partial packing result with error message.
   * Expected Result: canPackAllItems false and maxItemsPackable less than requested. Assertions:
   * Packing counts and error text.
   */
  @Test
  @DisplayName("calculatePackaging - Insufficient Packages Returns Partial Result - Success")
  void calculatePackaging_s03_insufficientPackagesReturnsPartialResult_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();
    PackagingHelper.ProductDimension product =
        new PackagingHelper.ProductDimension(
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 5);
    PackagingHelper.PackageDimension onlyOne =
        packageDim(3L, "Single Box", 1, 1, 1, "MINI", "1", "2", 1);

    // Act
    PackagingHelper.PackagingEstimateResult result =
        helper.calculatePackaging(product, List.of(onlyOne));

    // Assert
    assertFalse(result.isCanPackAllItems());
    assertEquals(1, result.getMaxItemsPackable());
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getErrorMessage().contains("Can only pack 1 of 5"));
  }

  /**
   * Purpose: Verify no directly suitable package falls back to the largest available package
   * branch. Expected Result: Largest available package is selected for packing. Assertions:
   * Selected package ID and count.
   */
  @Test
  @DisplayName("calculatePackaging - Falls Back To Largest Available Package - Success")
  void calculatePackaging_s04_fallsBackToLargestAvailablePackage_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();
    PackagingHelper.ProductDimension oversizedProduct =
        new PackagingHelper.ProductDimension(
            new BigDecimal("100"), BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("100"), 1);

    PackagingHelper.PackageDimension small = packageDim(4L, "Small", 2, 1, 1, "S", "5", "1", 1);
    PackagingHelper.PackageDimension large = packageDim(5L, "Large", 3, 1, 1, "L", "10", "2", 1);

    // Act
    PackagingHelper.PackagingEstimateResult result =
        helper.calculatePackaging(oversizedProduct, List.of(small, large));

    // Assert
    assertEquals(1, result.getPackagesUsed().size());
    assertEquals(5L, result.getPackagesUsed().get(0).getPackageId());
    assertEquals(1, result.getPackagesUsed().get(0).getQuantityUsed());
  }

  /**
   * Purpose: Verify multi-product flow returns empty result when products map is empty. Expected
   * Result: No package usage and canPackAllItems true. Assertions: Empty result fields and zero
   * totals.
   */
  @Test
  @DisplayName("calculatePackagingForMultipleProducts - Empty Products Map - Success")
  void calculatePackagingForMultipleProducts_s05_emptyProductsMap_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();

    // Act
    PackagingHelper.MultiProductPackagingResult result =
        helper.calculatePackagingForMultipleProducts(Map.of(), List.of());

    // Assert
    assertTrue(result.getPackagesUsed().isEmpty());
    assertEquals(0, result.getTotalPackagesUsed());
    assertTrue(result.getPackedItemsByProduct().isEmpty());
    assertTrue(result.isCanPackAllItems());
  }

  /**
   * Purpose: Verify multi-product algorithm packs mixed products and aggregates product quantities
   * by package. Expected Result: All requested items packed with expected per-product counts.
   * Assertions: canPackAllItems, packed counts, and package usage details.
   */
  @Test
  @DisplayName("calculatePackagingForMultipleProducts - Mixed Product Aggregation - Success")
  void calculatePackagingForMultipleProducts_s06_mixedProductAggregation_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();

    Map<Long, PackagingHelper.ProductDimension> products = new LinkedHashMap<>();
    products.put(
        11L,
        new PackagingHelper.ProductDimension(
            BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 2));
    products.put(
        22L,
        new PackagingHelper.ProductDimension(
            BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1));

    PackagingHelper.PackageDimension packageType =
        packageDim(10L, "Combo Box", 3, 1, 1, "COMBO", "3", "5", 3);

    // Act
    PackagingHelper.MultiProductPackagingResult result =
        helper.calculatePackagingForMultipleProducts(products, List.of(packageType));

    // Assert
    assertTrue(result.isCanPackAllItems());
    assertEquals(2, result.getPackedItemsByProduct().get(11L));
    assertEquals(1, result.getPackedItemsByProduct().get(22L));
    assertTrue(result.getTotalPackagesUsed() > 0);
    assertEquals(10L, result.getPackagesUsed().get(0).getPackageId());
    assertTrue(result.getPackagesUsed().get(0).getProductQuantities().containsKey(11L));
  }

  /**
   * Purpose: Verify multi-product flow reports partial packing when package stock is insufficient.
   * Expected Result: canPackAllItems false with informative error message. Assertions: Error state
   * and packed-item shortfall.
   */
  @Test
  @DisplayName("calculatePackagingForMultipleProducts - Insufficient Package Stock - Success")
  void calculatePackagingForMultipleProducts_s07_insufficientPackageStock_success() {
    // Arrange
    PackagingHelper helper = new PackagingHelper();

    Map<Long, PackagingHelper.ProductDimension> products =
        Map.of(
            99L,
            new PackagingHelper.ProductDimension(
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 5));

    PackagingHelper.PackageDimension packageType =
        packageDim(20L, "Limited Box", 1, 1, 1, "LIMITED", "1", "1", 2);

    // Act
    PackagingHelper.MultiProductPackagingResult result =
        helper.calculatePackagingForMultipleProducts(products, List.of(packageType));

    // Assert
    assertFalse(result.isCanPackAllItems());
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getPackedItemsByProduct().get(99L) < 5);
  }

  /**
   * Purpose: Verify nested usage-result models compute total costs and expose getters correctly.
   * Expected Result: Total cost reflects quantity * unit price and getters return assigned values.
   * Assertions: Cost math and getter outputs.
   */
  @Test
  @DisplayName("nestedResultModels - Total Cost And Getter Semantics - Success")
  void nestedResultModels_s08_totalCostAndGetterSemantics_success() {
    // Arrange
    PackagingHelper.PackageUsageResult usageResult =
        new PackagingHelper.PackageUsageResult(31L, "Mailer", "M", 3, new BigDecimal("2.50"));

    PackagingHelper.MultiProductPackageUsageResult multiUsageResult =
        new PackagingHelper.MultiProductPackageUsageResult(
            41L, "Bulk Box", "B", 2, new BigDecimal("7.25"), Map.of(1L, 2));

    // Act

    // Assert
    assertEquals(new BigDecimal("7.50"), usageResult.getTotalCost());
    assertEquals("Mailer", usageResult.getPackageName());
    assertEquals(new BigDecimal("14.50"), multiUsageResult.getTotalCost());
    assertEquals(2, multiUsageResult.getProductQuantities().get(1L));
  }

  /**
   * Purpose: Verify package dimension null-safe constructor defaults and decrement behavior.
   * Expected Result: Null size/weight/price default to zero values and quantity decrements by one.
   * Assertions: Volume, maxWeight, price, and available quantity checks.
   */
  @Test
  @DisplayName("packageDimension - Null Safety And Decrement - Success")
  void packageDimension_s09_nullSafetyAndDecrement_success() {
    // Arrange
    PackagingHelper.PackageDimension packageDimension =
        new PackagingHelper.PackageDimension(51L, "NullSafe", "N", null, null, null, 3);

    // Act
    packageDimension.decrementQuantity();

    // Assert
    assertEquals(0.0D, packageDimension.getVolume());
    assertEquals(0.0D, packageDimension.getMaxWeight());
    assertEquals(BigDecimal.ZERO, packageDimension.getPricePerUnit());
    assertEquals(2, packageDimension.getAvailableQuantity());
  }

  /**
   * Purpose: Verify product dimension null-safe constructor defaults and numeric getters. Expected
   * Result: Null dimensions default to zero with provided quantity retained. Assertions: Volume,
   * weight, and quantity values.
   */
  @Test
  @DisplayName("productDimension - Null Safety Defaults - Success")
  void productDimension_s10_nullSafetyDefaults_success() {
    // Arrange
    PackagingHelper.ProductDimension productDimension =
        new PackagingHelper.ProductDimension(null, null, null, null, 4);

    // Act

    // Assert
    assertEquals(0.0D, productDimension.getVolume());
    assertEquals(0.0D, productDimension.getWeight());
    assertEquals(4, productDimension.getQuantity());
  }

  private PackagingHelper.PackageDimension packageDim(
      Long id,
      String name,
      int length,
      int breadth,
      int height,
      String type,
      String maxWeight,
      String unitPrice,
      int qty) {
    return new PackagingHelper.PackageDimension(
        id,
        name,
        type,
        new PackagingHelper.PackageDimension.PackageSize(length, breadth, height),
        new BigDecimal(maxWeight),
        new BigDecimal(unitPrice),
        qty);
  }
}
