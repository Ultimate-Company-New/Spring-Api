package springapi.filterquerybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import springapi.models.databasemodels.Shipment;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

@DisplayName("Shipment Filter Query Builder Tests")
class ShipmentFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 5

  /**
   * Purpose: Verify shipment query includes required ShipRocket constraints, selectedIds, and
   * dynamic filters. Expected Result: Main and count queries include expected clauses and ordering.
   * Assertions: Query fragments and page totals match expectations.
   */
  @Test
  @DisplayName("shipmentFilterQueryBuilder - SelectedIds With Filters Builds Query - Success")
  void shipmentFilterQueryBuilder_s01_selectedIdsWithFiltersBuildsQuery_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);
    Shipment shipment = new Shipment();
    shipment.setShipmentId(91L);
    QueryFixture<Shipment> fixture = stubPagedQueries(Shipment.class, List.of(shipment), 21L);
    List<FilterCondition> filters =
        List.of(
            createFilter("shipRocketStatus", "equals", "Delivered"),
            createFilter("totalQuantity", "greaterThan", "10"));

    // Act
    Page<Shipment> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(91L), "AND", filters, createPageable(0, 10));

    // Assert
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("s.shipRocketOrderId IS NOT NULL"));
    assertTrue(fixture.getMainQueryString().contains("s.shipRocketOrderId != ''"));
    assertTrue(fixture.getMainQueryString().contains("AND s.shipmentId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(s.shipRocketStatus) = LOWER(:param0)"));
    assertTrue(fixture.getMainQueryString().contains("s.totalQuantity > :param1"));
    assertTrue(fixture.getMainQueryString().contains("ORDER BY s.createdAt DESC"));
    assertTrue(fixture.getCountQueryString().contains("AND s.shipmentId IN :selectedIds"));
  }

  /**
   * Purpose: Verify base shipment query behavior when no selectedIds or filters are provided.
   * Expected Result: Query contains only base constraints and ordering. Assertions: selectedIds and
   * dynamic filter clauses are absent.
   */
  @Test
  @DisplayName("shipmentFilterQueryBuilder - NoSelectedIds NoFilters Uses BaseQuery - Success")
  void shipmentFilterQueryBuilder_s02_noSelectedIdsNoFiltersUsesBaseQuery_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);
    QueryFixture<Shipment> fixture = stubPagedQueries(Shipment.class, List.of(), 0L);

    // Act
    builder.findPaginatedEntitiesWithMultipleFilters(1L, null, "AND", null, createPageable(1, 20));

    // Assert
    assertFalse(fixture.getMainQueryString().contains("AND s.shipmentId IN :selectedIds"));
    assertFalse(fixture.getMainQueryString().contains("param0"));
    assertTrue(fixture.getMainQueryString().contains("ORDER BY s.createdAt DESC"));
    assertTrue(fixture.getCountQueryString().contains("s.shipRocketOrderId IS NOT NULL"));
  }

  /**
   * Purpose: Verify column type classification across date, number, decimal, and string columns.
   * Expected Result: Decimal columns are treated as number for validation. Assertions: Type strings
   * match expected values.
   */
  @Test
  @DisplayName("shipmentFilterQueryBuilder - GetColumnType Returns Expected Types - Success")
  void shipmentFilterQueryBuilder_s03_getColumnTypeReturnsExpectedTypes_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);

    // Act
    String dateType = builder.getColumnType("createdAt");
    String numberType = builder.getColumnType("shipmentId");
    String decimalType = builder.getColumnType("shippingCost");
    String stringType = builder.getColumnType("shipRocketStatus");

    // Assert
    assertEquals("date", dateType);
    assertEquals("number", numberType);
    assertEquals("number", decimalType);
    assertEquals("string", stringType);
  }

  /**
   * Purpose: Verify invalid filter columns fail fast. Expected Result: IllegalArgumentException is
   * thrown for unsupported column names. Assertions: Exception type and exact message are
   * validated.
   */
  @Test
  @DisplayName(
      "shipmentFilterQueryBuilder - InvalidColumn Throws IllegalArgumentException - Success")
  void shipmentFilterQueryBuilder_s04_invalidColumnThrowsIllegalArgumentException_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);
    List<FilterCondition> filters = List.of(createFilter("invalidColumn", "equals", "x"));

    // Act
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> builder.buildFilterConditions(filters, "AND"));

    // Assert
    assertEquals("Invalid filter column: invalidColumn", ex.getMessage());
  }

  /**
   * Purpose: Verify all explicit shipment column mappings are exercised. Expected Result: Each
   * mapped column appears in generated whereClause using mapped field path. Assertions: whereClause
   * contains mapped field fragment for every explicit shipment column.
   */
  @Test
  @DisplayName(
      "shipmentFilterQueryBuilder - ExplicitColumnMappings BuildExpectedConditions - Success")
  void shipmentFilterQueryBuilder_s05_explicitColumnMappingsBuildExpectedConditions_success() {
    // Arrange
    ShipmentFilterQueryBuilder builder = new ShipmentFilterQueryBuilder(entityManager);
    Map<String, String> expectedMappings =
        Map.ofEntries(
            Map.entry("shipmentId", "s.shipmentId"),
            Map.entry("orderSummaryId", "s.orderSummaryId"),
            Map.entry("pickupLocationId", "s.pickupLocationId"),
            Map.entry("totalWeightKgs", "s.totalWeightKgs"),
            Map.entry("totalQuantity", "s.totalQuantity"),
            Map.entry("expectedDeliveryDate", "s.expectedDeliveryDate"),
            Map.entry("packagingCost", "s.packagingCost"),
            Map.entry("shippingCost", "s.shippingCost"),
            Map.entry("totalCost", "s.totalCost"),
            Map.entry("selectedCourierCompanyId", "s.selectedCourierCompanyId"),
            Map.entry("selectedCourierName", "s.selectedCourierName"),
            Map.entry("selectedCourierRate", "s.selectedCourierRate"),
            Map.entry("selectedCourierMinWeight", "s.selectedCourierMinWeight"),
            Map.entry("shipRocketOrderId", "s.shipRocketOrderId"),
            Map.entry("shipRocketShipmentId", "s.shipRocketShipmentId"),
            Map.entry("shipRocketAwbCode", "s.shipRocketAwbCode"),
            Map.entry("shipRocketTrackingId", "s.shipRocketTrackingId"),
            Map.entry("shipRocketStatus", "s.shipRocketStatus"),
            Map.entry("clientId", "s.clientId"),
            Map.entry("createdUser", "s.createdUser"),
            Map.entry("modifiedUser", "s.modifiedUser"),
            Map.entry("createdAt", "s.createdAt"),
            Map.entry("updatedAt", "s.updatedAt"));

    // Act + Assert
    for (Map.Entry<String, String> entry : expectedMappings.entrySet()) {
      String column = entry.getKey();
      String type = builder.getColumnType(column);
      FilterCondition filterCondition;
      if ("date".equals(type)) {
        filterCondition = createFilter(column, "is", "2025-01-01");
      } else if ("number".equals(type)) {
        filterCondition = createFilter(column, "equals", "1");
      } else {
        filterCondition = createFilter(column, "contains", "x");
      }

      BaseFilterQueryBuilder.QueryResult result =
          builder.buildFilterConditions(List.of(filterCondition), "AND");
      assertTrue(result.getWhereClause().contains(entry.getValue()));
    }
  }
}
