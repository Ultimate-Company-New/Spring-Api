package com.example.SpringApi.FilterQueryBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.SpringApi.FilterQueryBuilder.BaseFilterQueryBuilder.QueryResult;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BaseFilterQueryBuilder Tests")
class BaseFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 20

  private final BaseFilterQueryBuilder queryBuilder = new TestableFilterQueryBuilder();

  /**
   * Purpose: Verify null filters return an empty query result. Expected Result: Empty where clause
   * and empty parameters. Assertions: hasConditions false and parameters map empty.
   */
  @Test
  @DisplayName("buildFilterConditions - Null Filters - Success")
  void buildFilterConditions_s01_nullFilters_success() {
    // Arrange

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(null, "AND");

    // Assert
    assertEquals("", result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
    assertFalse(result.hasConditions());
  }

  /**
   * Purpose: Verify empty filters return an empty query result. Expected Result: Empty where clause
   * and empty parameters. Assertions: hasConditions false and parameters map empty.
   */
  @Test
  @DisplayName("buildFilterConditions - Empty Filters - Success")
  void buildFilterConditions_s02_emptyFilters_success() {
    // Arrange

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(List.of(), "AND");

    // Assert
    assertEquals("", result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
    assertFalse(result.hasConditions());
  }

  /**
   * Purpose: Verify string contains and equals operators produce expected clauses with AND join.
   * Expected Result: Lower-cased contains and equals conditions. Assertions: Clause and parameter
   * values for both filters.
   */
  @Test
  @DisplayName("buildFilterConditions - String Contains And Equals - Success")
  void buildFilterConditions_s03_stringContainsAndEquals_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(createFilter("name", "contains", "Alpha"), createFilter("name", "equals", "Beta"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "and");

    // Assert
    assertEquals(
        "LOWER(e.name) LIKE LOWER(:param0) AND LOWER(e.name) = LOWER(:param1)",
        result.getWhereClause());
    assertEquals("%Alpha%", result.getParameters().get("param0"));
    assertEquals("Beta", result.getParameters().get("param1"));
  }

  /**
   * Purpose: Verify string startsWith and endsWith operators produce expected wildcard patterns.
   * Expected Result: Prefix and suffix wildcard parameters. Assertions: Clause and parameter
   * values.
   */
  @Test
  @DisplayName("buildFilterConditions - String StartsWith And EndsWith - Success")
  void buildFilterConditions_s04_stringStartsWithAndEndsWith_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(createFilter("name", "startsWith", "Al"), createFilter("name", "endsWith", "ha"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "OR");

    // Assert
    assertEquals(
        "LOWER(e.name) LIKE LOWER(:param0) OR LOWER(e.name) LIKE LOWER(:param1)",
        result.getWhereClause());
    assertEquals("Al%", result.getParameters().get("param0"));
    assertEquals("%ha", result.getParameters().get("param1"));
  }

  /**
   * Purpose: Verify string empty operators produce null/blank checks without parameters. Expected
   * Result: IS NULL / IS NOT NULL style clauses. Assertions: Clause and empty parameter map.
   */
  @Test
  @DisplayName("buildFilterConditions - String Empty Operators - Success")
  void buildFilterConditions_s05_stringEmptyOperators_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(createFilter("name", "isEmpty", null), createFilter("name", "isNotEmpty", null));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals(
        "(e.name IS NULL OR e.name = '') AND (e.name IS NOT NULL AND e.name != '')",
        result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
  }

  /**
   * Purpose: Verify string isOneOf trims, lowercases, and keeps only non-empty values. Expected
   * Result: IN clause with cleaned list parameter. Assertions: Clause and cleaned list contents.
   */
  @Test
  @DisplayName("buildFilterConditions - String IsOneOf Cleans Values - Success")
  void buildFilterConditions_s06_stringIsOneOfCleansValues_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(createFilter("name", "isOneOf", " Alpha ; ; beta ; GAMMA "));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("LOWER(e.name) IN (:param0)", result.getWhereClause());
    assertEquals(List.of("alpha", "beta", "gamma"), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify string isNotOneOf uses NOT IN and preserves cleaned values. Expected Result:
   * NOT IN clause. Assertions: Clause and cleaned list parameter.
   */
  @Test
  @DisplayName("buildFilterConditions - String IsNotOneOf - Success")
  void buildFilterConditions_s07_stringIsNotOneOf_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("name", "isNotOneOf", "A;B"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("LOWER(e.name) NOT IN (:param0)", result.getWhereClause());
    assertEquals(List.of("a", "b"), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify string isOneOf with null value yields a no-op condition. Expected Result: 1=1
   * clause. Assertions: Clause equals 1=1 and no parameters.
   */
  @Test
  @DisplayName("buildFilterConditions - String IsOneOf Null Value - Success")
  void buildFilterConditions_s08_stringIsOneOfNullValue_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("name", "isOneOf", null));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("1=1", result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
  }

  /**
   * Purpose: Verify containsOneOf creates OR conditions and sub-parameters. Expected Result: OR
   * expression with one parameter per token. Assertions: Clause and generated parameter
   * keys/values.
   */
  @Test
  @DisplayName("buildFilterConditions - String ContainsOneOf - Success")
  void buildFilterConditions_s09_stringContainsOneOf_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("name", "containsOneOf", "Red; Blue"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals(
        "(LOWER(e.name) LIKE LOWER(:param0_0) OR LOWER(e.name) LIKE LOWER(:param0_1))",
        result.getWhereClause());
    assertEquals("%Red%", result.getParameters().get("param0_0"));
    assertEquals("%Blue%", result.getParameters().get("param0_1"));
  }

  /**
   * Purpose: Verify containsOneOf with only separators falls back to no-op. Expected Result: 1=1
   * clause. Assertions: Clause equals 1=1 and no parameters.
   */
  @Test
  @DisplayName("buildFilterConditions - String ContainsOneOf Blank Values - Success")
  void buildFilterConditions_s10_stringContainsOneOfBlankValues_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("name", "containsOneOf", "; ; ;"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("1=1", result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
  }

  /**
   * Purpose: Verify number equals parses integral strings to Long. Expected Result: Number
   * comparison clause with Long parameter. Assertions: Clause and parameter type/value.
   */
  @Test
  @DisplayName("buildFilterConditions - Number Equals Parses Long - Success")
  void buildFilterConditions_s11_numberEqualsParsesLong_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("age", "equals", "42"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("e.age = :param0", result.getWhereClause());
    assertInstanceOf(Long.class, result.getParameters().get("param0"));
    assertEquals(42L, result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify number lessThan parses decimal strings to Double. Expected Result: Numeric
   * clause with Double parameter. Assertions: Clause and parameter type/value.
   */
  @Test
  @DisplayName("buildFilterConditions - Number LessThan Parses Double - Success")
  void buildFilterConditions_s12_numberLessThanParsesDouble_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("price", "lessThan", "12.75"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("e.price < :param0", result.getWhereClause());
    assertInstanceOf(Double.class, result.getParameters().get("param0"));
    assertEquals(12.75D, result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify unsupported number operator returns no-op. Expected Result: 1=1 clause.
   * Assertions: Clause equals 1=1.
   */
  @Test
  @DisplayName("buildFilterConditions - Number Invalid Operator - Success")
  void buildFilterConditions_s13_numberInvalidOperator_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("age", "between", "1,10"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("1=1", result.getWhereClause());
  }

  /**
   * Purpose: Verify number isOneOf handles semicolon-separated values and skips invalid numbers.
   * Expected Result: IN clause with valid parsed numeric entries. Assertions: Clause and cleaned
   * numeric parameter list.
   */
  @Test
  @DisplayName("buildFilterConditions - Number IsOneOf Semicolon - Success")
  void buildFilterConditions_s14_numberIsOneOfSemicolon_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("age", "isOneOf", "1;abc;2; 3"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("e.age IN (:param0)", result.getWhereClause());
    assertEquals(List.of(1L, 2L, 3L), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify number isNotOneOf supports comma-separated values. Expected Result: NOT IN
   * clause with parsed values. Assertions: Clause and parsed list values.
   */
  @Test
  @DisplayName("buildFilterConditions - Number IsNotOneOf Comma - Success")
  void buildFilterConditions_s15_numberIsNotOneOfComma_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("age", "isNotOneOf", "4, 5, 6"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("e.age NOT IN (:param0)", result.getWhereClause());
    assertEquals(List.of(4L, 5L, 6L), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify date operators parse yyyy-MM-dd values. Expected Result: DATE comparison with
   * LocalDate parameter. Assertions: Clause and parameter value.
   */
  @Test
  @DisplayName("buildFilterConditions - Date Is Operator - Success")
  void buildFilterConditions_s16_dateIsOperator_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("createdAt", "is", "2026-01-15"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("DATE(e.createdAt) = :param0", result.getWhereClause());
    assertEquals(LocalDate.of(2026, 1, 15), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify date operators parse datetime strings by converting to LocalDate. Expected
   * Result: DATE comparison with LocalDate extracted from datetime. Assertions: Clause and
   * LocalDate parameter.
   */
  @Test
  @DisplayName("buildFilterConditions - Date DateTime Parsing - Success")
  void buildFilterConditions_s17_dateDateTimeParsing_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(createFilter("createdAt", "isAfter", "2026-02-10T09:15:00"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("DATE(e.createdAt) > :param0", result.getWhereClause());
    assertEquals(LocalDate.of(2026, 2, 10), result.getParameters().get("param0"));
  }

  /**
   * Purpose: Verify invalid date values return no-op conditions. Expected Result: 1=1 clause for
   * invalid date parsing. Assertions: Clause equals 1=1 and no date parameter set.
   */
  @Test
  @DisplayName("buildFilterConditions - Date Invalid Value - Success")
  void buildFilterConditions_s18_dateInvalidValue_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("createdAt", "isBefore", "not-a-date"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("1=1", result.getWhereClause());
    assertTrue(result.getParameters().isEmpty());
  }

  /**
   * Purpose: Verify boolean is operator accepts both String and Boolean input values. Expected
   * Result: Boolean equality conditions with parsed booleans. Assertions: Clauses and boolean
   * parameter values.
   */
  @Test
  @DisplayName("buildFilterConditions - Boolean Is Operator - Success")
  void buildFilterConditions_s19_booleanIsOperator_success() {
    // Arrange
    List<FilterCondition> filters =
        List.of(
            createFilter("isDeleted", "is", "true"),
            createFilter("isDeleted", "is", Boolean.FALSE));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");

    // Assert
    assertEquals("e.isDeleted = :param0 AND e.isDeleted = :param1", result.getWhereClause());
    assertEquals(Boolean.TRUE, result.getParameters().get("param0"));
    assertEquals(Boolean.FALSE, result.getParameters().get("param1"));
  }

  /**
   * Purpose: Verify unknown operators and QueryResult hasConditions behavior. Expected Result:
   * No-op for unknown operator and correct hasConditions flags. Assertions: Clause fallback and
   * hasConditions values.
   */
  @Test
  @DisplayName("buildFilterConditions - Unknown Operator And QueryResult Flags - Success")
  void buildFilterConditions_s20_unknownOperatorAndQueryResultFlags_success() {
    // Arrange
    List<FilterCondition> filters = List.of(createFilter("name", "unknown", "x"));

    // Act
    QueryResult result = queryBuilder.buildFilterConditions(filters, "AND");
    QueryResult emptyResult = new QueryResult("", Map.of());
    QueryResult nullResult = new QueryResult(null, Map.of());

    // Assert
    assertEquals("1=1", result.getWhereClause());
    assertTrue(result.hasConditions());
    assertFalse(emptyResult.hasConditions());
    assertFalse(nullResult.hasConditions());
    assertNull(nullResult.getWhereClause());
  }

  private static final class TestableFilterQueryBuilder extends BaseFilterQueryBuilder {

    @Override
    protected String mapColumnToField(String column) {
      return "e." + column;
    }

    @Override
    protected List<String> getDateColumns() {
      return List.of("createdAt");
    }

    @Override
    protected List<String> getBooleanColumns() {
      return List.of("isDeleted");
    }

    @Override
    protected List<String> getNumberColumns() {
      return List.of("age", "price");
    }
  }
}
