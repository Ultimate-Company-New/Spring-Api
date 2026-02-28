package springapi.filterquerybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import springapi.models.databasemodels.Lead;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

@DisplayName("Lead Filter Query Builder Tests")
class LeadFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 4

  /**
   * Purpose: Verify lead query generation includes non-deleted clause, address join usage, and
   * ordering when filters are present. Expected Result: Main and count queries contain expected
   * clauses. Assertions: Query fragments and page totals are validated.
   */
  @Test
  @DisplayName("leadFilterQueryBuilder - IncludeDeletedFalse With Filters BuildsQuery - Success")
  void leadFilterQueryBuilder_s01_includeDeletedFalseWithFiltersBuildsQuery_success() {
    // Arrange
    LeadFilterQueryBuilder builder = new LeadFilterQueryBuilder(entityManager);
    Lead lead = new Lead();
    lead.setLeadId(1L);
    QueryFixture<Lead> fixture = stubPagedQueries(Lead.class, List.of(lead), 21L);
    List<FilterCondition> filters =
        List.of(
            createFilter("leadStatus", "equals", "Open"),
            createFilter("createdAt", "is", "2025-01-01"));

    // Act
    Page<Lead> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, "AND", filters, false, createPageable(0, 10));

    // Assert
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("AND l.isDeleted = false"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(l.leadStatus) = LOWER(:param0)"));
    assertTrue(fixture.getMainQueryString().contains("DATE(l.createdAt) = :param1"));
    assertTrue(fixture.getMainQueryString().contains("ORDER BY l.leadId DESC"));
    assertTrue(fixture.getCountQueryString().contains("LEFT JOIN l.address a"));
  }

  /**
   * Purpose: Verify includeDeleted=true path skips deleted clause and unknown columns fallback to
   * default lead alias mapping. Expected Result: Query omits deleted predicate and includes
   * l.<column> mapping. Assertions: Query fragments confirm fallback behavior.
   */
  @Test
  @DisplayName("leadFilterQueryBuilder - IncludeDeletedTrue Uses FallbackColumn - Success")
  void leadFilterQueryBuilder_s02_includeDeletedTrueUsesFallbackColumn_success() {
    // Arrange
    LeadFilterQueryBuilder builder = new LeadFilterQueryBuilder(entityManager);
    QueryFixture<Lead> fixture = stubPagedQueries(Lead.class, List.of(), 0L);
    List<FilterCondition> filters = List.of(createFilter("customAttr", "contains", "vip"));

    // Act
    builder.findPaginatedEntitiesWithMultipleFilters(
        1L, "OR", filters, true, createPageable(0, 20));

    // Assert
    assertFalse(fixture.getMainQueryString().contains("AND l.isDeleted = false"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(l.customAttr) LIKE LOWER(:param0)"));
  }

  /**
   * Purpose: Verify lead column type classification. Expected Result: Date, boolean, number, and
   * string columns map to expected types. Assertions: Type strings match expected values.
   */
  @Test
  @DisplayName("leadFilterQueryBuilder - GetColumnType ReturnsExpectedTypes - Success")
  void leadFilterQueryBuilder_s03_getColumnTypeReturnsExpectedTypes_success() {
    // Arrange
    LeadFilterQueryBuilder builder = new LeadFilterQueryBuilder(entityManager);

    // Act
    String dateType = builder.getColumnType("createdAt");
    String booleanType = builder.getColumnType("isDeleted");
    String numberType = builder.getColumnType("leadId");
    String stringType = builder.getColumnType("email");

    // Assert
    assertEquals("date", dateType);
    assertEquals("boolean", booleanType);
    assertEquals("number", numberType);
    assertEquals("string", stringType);
  }

  /**
   * Purpose: Verify all explicit lead column mappings are exercised. Expected Result: Each mapped
   * column contributes expected field/expression in whereClause. Assertions: whereClause contains
   * mapped field fragment for every explicit lead column.
   */
  @Test
  @DisplayName("leadFilterQueryBuilder - ExplicitColumnMappings BuildExpectedConditions - Success")
  void leadFilterQueryBuilder_s04_explicitColumnMappingsBuildExpectedConditions_success() {
    // Arrange
    LeadFilterQueryBuilder builder = new LeadFilterQueryBuilder(entityManager);
    Map<String, String> expectedMappings =
        Map.ofEntries(
            Map.entry("leadId", "l.leadId"),
            Map.entry("firstName", "l.firstName"),
            Map.entry("lastName", "l.lastName"),
            Map.entry("email", "l.email"),
            Map.entry("phone", "l.phone"),
            Map.entry("company", "l.company"),
            Map.entry("companySize", "l.companySize"),
            Map.entry("title", "l.title"),
            Map.entry("leadStatus", "l.leadStatus"),
            Map.entry("annualRevenue", "l.annualRevenue"),
            Map.entry("fax", "l.fax"),
            Map.entry("website", "l.website"),
            Map.entry("isDeleted", "l.isDeleted"),
            Map.entry("clientId", "l.clientId"),
            Map.entry("addressId", "l.addressId"),
            Map.entry("createdById", "l.createdById"),
            Map.entry("assignedAgentId", "l.assignedAgentId"),
            Map.entry("createdUser", "l.createdUser"),
            Map.entry("modifiedUser", "l.modifiedUser"),
            Map.entry("createdAt", "l.createdAt"),
            Map.entry("updatedAt", "l.updatedAt"),
            Map.entry("notes", "l.notes"),
            Map.entry("address", "COALESCE(a.streetAddress, '')"));

    // Act + Assert
    for (Map.Entry<String, String> entry : expectedMappings.entrySet()) {
      String column = entry.getKey();
      String type = builder.getColumnType(column);
      FilterCondition filterCondition;
      if ("date".equals(type)) {
        filterCondition = createFilter(column, "is", "2025-01-01");
      } else if ("boolean".equals(type)) {
        filterCondition = createFilter(column, "is", "true");
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
