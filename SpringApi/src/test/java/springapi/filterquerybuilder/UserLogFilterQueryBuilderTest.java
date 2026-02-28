package springapi.filterquerybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import springapi.models.databasemodels.UserLog;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

@DisplayName("User Log Filter Query Builder Tests")
class UserLogFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 4

  /**
   * Purpose: Verify user-log query generation includes mandatory user/client filters, ordering, and
   * dynamic filter conditions. Expected Result: Main and count queries contain expected mandatory
   * and dynamic clauses. Assertions: Query fragments and page totals are validated.
   */
  @Test
  @DisplayName("userLogFilterQueryBuilder - RequiredClauses And DynamicFilters - Success")
  void userLogFilterQueryBuilder_s01_requiredClausesAndDynamicFilters_success() {
    // Arrange
    UserLogFilterQueryBuilder builder = new UserLogFilterQueryBuilder(entityManager);
    UserLog log = new UserLog();
    log.setLogId(71L);
    QueryFixture<UserLog> fixture = stubPagedQueries(UserLog.class, List.of(log), 21L);
    List<FilterCondition> filters =
        List.of(
            createFilter("action", "contains", "login"),
            createFilter("auditUserId", "equals", "10"));

    // Act
    Page<UserLog> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            7L, 9L, "AND", filters, createPageable(0, 10));

    // Assert
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("ul.userId = :userId"));
    assertTrue(
        fixture.getMainQueryString().contains("ul.clientId = :clientId OR ul.clientId IS NULL"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(ul.action) LIKE LOWER(:param0)"));
    assertTrue(fixture.getMainQueryString().contains("ul.auditUserId = :param1"));
    assertTrue(fixture.getMainQueryString().contains("ORDER BY ul.logId DESC"));
    assertTrue(fixture.getCountQueryString().contains("ul.userId = :userId"));
  }

  /**
   * Purpose: Verify fallback mapping for unknown columns and no-filter behavior boundaries.
   * Expected Result: Unknown columns map to ul.<column> and no dynamic params are generated when
   * filters are absent. Assertions: Query string contains fallback mapping and omits param
   * placeholders for null filters.
   */
  @Test
  @DisplayName("userLogFilterQueryBuilder - FallbackColumnMapping And NullFilters - Success")
  void userLogFilterQueryBuilder_s02_fallbackColumnMappingAndNullFilters_success() {
    // Arrange
    UserLogFilterQueryBuilder builder = new UserLogFilterQueryBuilder(entityManager);
    QueryFixture<UserLog> withFallbackFixture = stubPagedQueries(UserLog.class, List.of(), 0L);
    List<FilterCondition> fallbackFilters = List.of(createFilter("customField", "equals", "x"));

    // Act
    builder.findPaginatedEntitiesWithMultipleFilters(
        7L, 9L, "AND", fallbackFilters, createPageable(0, 5));

    // Assert
    assertTrue(
        withFallbackFixture
            .getMainQueryString()
            .contains("LOWER(ul.customField) = LOWER(:param0)"));

    // Arrange
    QueryFixture<UserLog> nullFilterFixture = stubPagedQueries(UserLog.class, List.of(), 0L);

    // Act
    builder.findPaginatedEntitiesWithMultipleFilters(7L, 9L, "OR", null, createPageable(1, 5));

    // Assert
    assertFalse(nullFilterFixture.getMainQueryString().contains("param0"));
    assertFalse(nullFilterFixture.getCountQueryString().contains("param0"));
  }

  /**
   * Purpose: Verify user-log column type classification. Expected Result: Date, number, and string
   * columns resolve to expected types. Assertions: Type strings match expected values.
   */
  @Test
  @DisplayName("userLogFilterQueryBuilder - GetColumnType ReturnsExpectedTypes - Success")
  void userLogFilterQueryBuilder_s03_getColumnTypeReturnsExpectedTypes_success() {
    // Arrange
    UserLogFilterQueryBuilder builder = new UserLogFilterQueryBuilder(entityManager);

    // Act
    String dateType = builder.getColumnType("createdAt");
    String numberType = builder.getColumnType("auditUserId");
    String stringType = builder.getColumnType("description");

    // Assert
    assertEquals("date", dateType);
    assertEquals("number", numberType);
    assertEquals("string", stringType);
  }

  /**
   * Purpose: Verify all explicit user-log column mappings are exercised. Expected Result: Each
   * mapped column contributes expected field/expression in whereClause. Assertions: whereClause
   * includes mapped field fragment for every explicit user-log column.
   */
  @Test
  @DisplayName(
      "userLogFilterQueryBuilder - ExplicitColumnMappings BuildExpectedConditions - Success")
  void userLogFilterQueryBuilder_s04_explicitColumnMappingsBuildExpectedConditions_success() {
    // Arrange
    UserLogFilterQueryBuilder builder = new UserLogFilterQueryBuilder(entityManager);
    Map<String, String> expectedMappings =
        Map.ofEntries(
            Map.entry("logId", "ul.logId"),
            Map.entry("userId", "ul.userId"),
            Map.entry("clientId", "ul.clientId"),
            Map.entry("action", "ul.action"),
            Map.entry("description", "ul.description"),
            Map.entry("ipAddress", "ul.ipAddress"),
            Map.entry("userAgent", "ul.userAgent"),
            Map.entry("sessionId", "ul.sessionId"),
            Map.entry("logLevel", "ul.logLevel"),
            Map.entry("createdAt", "ul.createdAt"),
            Map.entry("createdUser", "ul.createdUser"),
            Map.entry("updatedAt", "ul.updatedAt"),
            Map.entry("modifiedUser", "ul.modifiedUser"),
            Map.entry("notes", "ul.notes"),
            Map.entry("auditUserId", "ul.auditUserId"),
            Map.entry("change", "ul.change"),
            Map.entry("newValue", "ul.newValue"),
            Map.entry("oldValue", "ul.oldValue"));

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
