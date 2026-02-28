package springapi.filterquerybuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import springapi.models.databasemodels.User;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

@DisplayName("User Filter Query Builder Tests")
class UserFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 4

  /**
   * Purpose: Verify query generation includes selectedIds, non-deleted constraint, and dynamic
   * string filters including address mapping. Expected Result: Main and count queries include
   * expected clauses and pagination works. Assertions: Query fragments exist and returned page
   * metadata is correct.
   */
  @Test
  @DisplayName("userFilterQueryBuilder - SelectedIds IncludeDeletedFalse With Filters - Success")
  void userFilterQueryBuilder_s01_selectedIdsIncludeDeletedFalseWithFilters_success() {
    // Arrange
    UserFilterQueryBuilder builder = new UserFilterQueryBuilder(entityManager);
    User user = new User();
    user.setUserId(11L);
    QueryFixture<User> fixture = stubPagedQueries(User.class, List.of(user), 14L);
    List<FilterCondition> filters =
        List.of(
            createFilter("firstName", "contains", "john"),
            createFilter("address", "contains", "main"));

    // Act
    Page<User> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, List.of(11L, 12L), "AND", filters, false, createPageable(0, 10));

    // Assert
    assertEquals(1, page.getContent().size());
    assertEquals(14L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("AND u.userId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("AND u.isDeleted = false"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(u.firstName) LIKE LOWER(:param0)"));
    assertTrue(fixture.getMainQueryString().contains("COALESCE(a.streetAddress, '')"));
    assertTrue(
        fixture.getCountQueryString().contains("LEFT JOIN Address a ON u.addressId = a.addressId"));
  }

  /**
   * Purpose: Verify includeDeleted=true path skips deleted filter and unknown columns fallback to
   * default user alias mapping. Expected Result: Query excludes deleted predicate and includes
   * fallback mapped field. Assertions: Query string omits deleted clause and contains u.<column>
   * field mapping.
   */
  @Test
  @DisplayName("userFilterQueryBuilder - IncludeDeletedTrue Uses Fallback Column Mapping - Success")
  void userFilterQueryBuilder_s02_includeDeletedTrueUsesFallbackColumnMapping_success() {
    // Arrange
    UserFilterQueryBuilder builder = new UserFilterQueryBuilder(entityManager);
    QueryFixture<User> fixture = stubPagedQueries(User.class, List.of(), 0L);
    List<FilterCondition> filters = List.of(createFilter("customField", "equals", "value"));

    // Act
    builder.findPaginatedEntitiesWithMultipleFilters(
        1L, null, "OR", filters, true, createPageable(0, 25));

    // Assert
    assertFalse(fixture.getMainQueryString().contains("AND u.isDeleted = false"));
    assertFalse(fixture.getMainQueryString().contains("AND u.userId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(u.customField) = LOWER(:param0)"));
  }

  /**
   * Purpose: Verify column type classification for date, boolean, number, and string columns.
   * Expected Result: Column types are returned according to configured column lists. Assertions:
   * Type strings match expected values.
   */
  @Test
  @DisplayName("userFilterQueryBuilder - GetColumnType Returns Expected Types - Success")
  void userFilterQueryBuilder_s03_getColumnTypeReturnsExpectedTypes_success() {
    // Arrange
    UserFilterQueryBuilder builder = new UserFilterQueryBuilder(entityManager);

    // Act
    String dateType = builder.getColumnType("dob");
    String booleanType = builder.getColumnType("locked");
    String numberType = builder.getColumnType("userId");
    String stringType = builder.getColumnType("email");

    // Assert
    assertEquals("date", dateType);
    assertEquals("boolean", booleanType);
    assertEquals("number", numberType);
    assertEquals("string", stringType);
  }

  /**
   * Purpose: Verify all explicit user column mappings are exercised and build valid conditions.
   * Expected Result: Each mapped column resolves to the expected field/expression in whereClause.
   * Assertions: whereClause includes expected mapped field fragment for every mapped column.
   */
  @Test
  @DisplayName("userFilterQueryBuilder - ExplicitColumnMappings BuildExpectedConditions - Success")
  void userFilterQueryBuilder_s04_explicitColumnMappingsBuildExpectedConditions_success() {
    // Arrange
    UserFilterQueryBuilder builder = new UserFilterQueryBuilder(entityManager);
    Map<String, String> expectedMappings =
        Map.ofEntries(
            Map.entry("userId", "u.userId"),
            Map.entry("firstName", "u.firstName"),
            Map.entry("lastName", "u.lastName"),
            Map.entry("loginName", "u.loginName"),
            Map.entry("role", "u.role"),
            Map.entry("dob", "u.dob"),
            Map.entry("phone", "u.phone"),
            Map.entry("datePasswordChanges", "u.datePasswordChanges"),
            Map.entry("loginAttempts", "u.loginAttempts"),
            Map.entry("isDeleted", "u.isDeleted"),
            Map.entry("locked", "u.locked"),
            Map.entry("emailConfirmed", "u.emailConfirmed"),
            Map.entry("token", "u.token"),
            Map.entry("isGuest", "u.isGuest"),
            Map.entry("apiKey", "u.apiKey"),
            Map.entry("email", "u.email"),
            Map.entry("addressId", "u.addressId"),
            Map.entry("profilePicture", "u.profilePicture"),
            Map.entry("lastLoginAt", "u.lastLoginAt"),
            Map.entry("createdAt", "u.createdAt"),
            Map.entry("createdUser", "u.createdUser"),
            Map.entry("updatedAt", "u.updatedAt"),
            Map.entry("modifiedUser", "u.modifiedUser"),
            Map.entry("notes", "u.notes"),
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
