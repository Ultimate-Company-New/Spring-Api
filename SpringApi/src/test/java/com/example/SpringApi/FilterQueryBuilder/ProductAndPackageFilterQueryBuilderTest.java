package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Product And Package Filter Query Builder Tests")
class ProductAndPackageFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

    // Total Tests: 5

    /**
     * Purpose: Verify product query builder uses INNER JOIN path when pickupLocationId equals filter has valid numeric value.
     * Expected Result: Main and count queries include pickup-location inner join fragments.
     * Assertions: Query fragments and page totals.
     */
    @Test
    @DisplayName("productFilterQueryBuilder - Pickup Equals Valid Uses Inner Join - Success")
    void productFilterQueryBuilder_s01_pickupEqualsValidUsesInnerJoin_success() {
        // Arrange
        ProductFilterQueryBuilder builder = new ProductFilterQueryBuilder(entityManager);
        Product product = new Product();
        product.setProductId(1L);
        QueryFixture<Product> fixture = stubPagedQueries(Product.class, List.of(product), 21L);
        List<FilterCondition> filters = List.of(
                createFilter("pickupLocationId", "equals", "7"),
                createFilter("title", "contains", "Box")
        );

        // Act
        Page<Product> page = builder.findPaginatedEntitiesWithMultipleFilters(
                1L,
                List.of(1L, 2L),
                "AND",
                filters,
                false,
                createPageable(0, 10)
        );

        // Assert
        assertEquals("number", builder.getColumnType("pickupLocationId"));
        assertEquals("boolean", builder.getColumnType("returnsAllowed"));
        assertEquals(1, page.getContent().size());
        assertEquals(21L, page.getTotalElements());
        assertTrue(fixture.getMainQueryString().contains("INNER JOIN FETCH p.productPickupLocationMappings pplm"));
        assertTrue(fixture.getMainQueryString().contains("AND p.productId IN :selectedIds"));
        assertTrue(fixture.getMainQueryString().contains("AND p.isDeleted = false"));
        assertTrue(fixture.getCountQueryString().contains("INNER JOIN p.productPickupLocationMappings pplm"));
        assertTrue(fixture.getCountQueryString().contains("LOWER(p.title) LIKE LOWER(:param1)"));
    }

    /**
     * Purpose: Verify product query builder falls back to LEFT JOIN path when pickupLocationId equals filter is non-numeric.
     * Expected Result: Pickup filter still applies, but join strategy remains LEFT JOIN.
     * Assertions: Main and count query join fragments.
     */
    @Test
    @DisplayName("productFilterQueryBuilder - Pickup Equals Invalid Uses Left Join - Success")
    void productFilterQueryBuilder_s02_pickupEqualsInvalidUsesLeftJoin_success() {
        // Arrange
        ProductFilterQueryBuilder builder = new ProductFilterQueryBuilder(entityManager);
        QueryFixture<Product> fixture = stubPagedQueries(Product.class, List.of(), 0L);
        List<FilterCondition> filters = List.of(createFilter("pickupLocationId", "equals", "invalid-id"));

        // Act
        Page<Product> page = builder.findPaginatedEntitiesWithMultipleFilters(
                1L,
                null,
                "AND",
                filters,
                false,
                createPageable(0, 10)
        );

        // Assert
        assertEquals(0, page.getContent().size());
        assertEquals(0L, page.getTotalElements());
        assertTrue(fixture.getMainQueryString().contains("LEFT JOIN FETCH p.productPickupLocationMappings pplm"));
        assertTrue(fixture.getCountQueryString().contains("LEFT JOIN p.productPickupLocationMappings pplm"));
        assertTrue(fixture.getMainQueryString().contains("pplm.pickupLocationId = :param0"));
    }

    /**
     * Purpose: Verify product query builder omits pickup-location join in count query when no pickup filter is present.
     * Expected Result: Count query remains on Product table only.
     * Assertions: Absence of mapping table join and includeDeleted=true behavior.
     */
    @Test
    @DisplayName("productFilterQueryBuilder - No Pickup Filter Skips Count Join - Success")
    void productFilterQueryBuilder_s03_noPickupFilterSkipsCountJoin_success() {
        // Arrange
        ProductFilterQueryBuilder builder = new ProductFilterQueryBuilder(entityManager);
        QueryFixture<Product> fixture = stubPagedQueries(Product.class, List.of(), 0L);
        List<FilterCondition> filters = List.of(createFilter("brand", "contains", "Acme"));

        // Act
        builder.findPaginatedEntitiesWithMultipleFilters(
                1L,
                null,
                "OR",
                filters,
                true,
                createPageable(0, 10)
        );

        // Assert
        assertFalse(fixture.getCountQueryString().contains("productPickupLocationMappings"));
        assertFalse(fixture.getMainQueryString().contains("AND p.isDeleted = false"));
        assertTrue(fixture.getMainQueryString().contains("LOWER(p.brand) LIKE LOWER(:param0)"));
    }

    /**
     * Purpose: Verify package query builder uses INNER JOIN when pickupLocationId equals filter is valid.
     * Expected Result: Main/count queries include PackagePickupLocationMapping joins and extract helper returns parsed id.
     * Assertions: Query fragments, extracted pickup id, and page totals.
     */
    @Test
    @DisplayName("packageFilterQueryBuilder - Pickup Equals Valid Uses Inner Join - Success")
    void packageFilterQueryBuilder_s04_pickupEqualsValidUsesInnerJoin_success() {
        // Arrange
        PackageFilterQueryBuilder builder = new PackageFilterQueryBuilder(entityManager);
        com.example.SpringApi.Models.DatabaseModels.Package packageModel = new com.example.SpringApi.Models.DatabaseModels.Package();
        packageModel.setPackageId(18L);
        QueryFixture<com.example.SpringApi.Models.DatabaseModels.Package> fixture = stubPagedQueries(
                com.example.SpringApi.Models.DatabaseModels.Package.class,
                List.of(packageModel),
                21L
        );
        List<FilterCondition> filters = List.of(
                createFilter("pickupLocationId", "equals", "22"),
                createFilter("packageName", "contains", "Crate")
        );

        // Act
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = builder.findPaginatedEntitiesWithMultipleFilters(
                1L,
                List.of(18L),
                "AND",
                filters,
                false,
                createPageable(0, 10)
        );

        // Assert
        assertEquals("number", builder.getColumnType("pickupLocationId"));
        assertEquals("string", builder.getColumnType("dimensions"));
        assertEquals(Long.valueOf(22L), builder.extractPickupLocationIdFilter(filters));
        assertEquals(1, page.getContent().size());
        assertEquals(21L, page.getTotalElements());
        assertTrue(fixture.getMainQueryString().contains("INNER JOIN PackagePickupLocationMapping pplm ON pkg.packageId = pplm.packageId"));
        assertTrue(fixture.getCountQueryString().contains("INNER JOIN PackagePickupLocationMapping pplm ON pkg.packageId = pplm.packageId"));
        assertTrue(fixture.getMainQueryString().contains("LOWER(pkg.packageName) LIKE LOWER(:param1)"));
    }

    /**
     * Purpose: Verify package pickup-location extraction handles invalid or missing numeric values safely.
     * Expected Result: Null extraction and no join path in generated queries when filter value is invalid.
     * Assertions: Extract result is null and count query omits mapping join.
     */
    @Test
    @DisplayName("packageFilterQueryBuilder - Pickup Extraction Invalid Returns Null - Success")
    void packageFilterQueryBuilder_s05_pickupExtractionInvalidReturnsNull_success() {
        // Arrange
        PackageFilterQueryBuilder builder = new PackageFilterQueryBuilder(entityManager);
        QueryFixture<com.example.SpringApi.Models.DatabaseModels.Package> fixture = stubPagedQueries(
                com.example.SpringApi.Models.DatabaseModels.Package.class,
                List.of(),
                0L
        );
        List<FilterCondition> filters = List.of(createFilter("pickupLocationId", "equals", "NaN"));

        // Act
        builder.findPaginatedEntitiesWithMultipleFilters(
                1L,
                null,
                "AND",
                filters,
                true,
                createPageable(0, 10)
        );

        // Assert
        assertNull(builder.extractPickupLocationIdFilter(filters));
        assertNull(builder.extractPickupLocationIdFilter(null));
        assertFalse(fixture.getCountQueryString().contains("PackagePickupLocationMapping"));
        assertTrue(fixture.getMainQueryString().contains("pplm.pickupLocationId = :param0"));
    }
}
