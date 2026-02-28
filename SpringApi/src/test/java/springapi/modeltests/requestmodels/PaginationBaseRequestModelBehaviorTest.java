package springapi.modeltests.requestmodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.requestmodels.PaginationBaseRequestModel;

@DisplayName("Pagination Base Request Model Behavior Tests")
class PaginationBaseRequestModelBehaviorTest {

  // Total Tests: 5

  /**
   * Purpose: Verify logic operator validation accepts null and AND/OR (case-insensitive) and
   * rejects invalid operators. Expected Result: Null/AND/OR are valid; unsupported operators are
   * invalid. Assertions: Validation results match expected truth values.
   */
  @Test
  @DisplayName("paginationBaseRequestModel - LogicOperatorValidation - Success")
  void paginationBaseRequestModel_s01_logicOperatorValidation_success() {
    // Arrange
    PaginationBaseRequestModel model = new PaginationBaseRequestModel();

    // Act
    model.setLogicOperator(null);
    boolean nullValid = model.isValidLogicOperator();
    model.setLogicOperator("AND");
    boolean andValid = model.isValidLogicOperator();
    model.setLogicOperator("or");
    boolean orValid = model.isValidLogicOperator();
    model.setLogicOperator("XOR");
    boolean invalid = model.isValidLogicOperator();

    // Assert
    assertTrue(nullValid);
    assertTrue(andValid);
    assertTrue(orValid);
    assertFalse(invalid);
  }

  /**
   * Purpose: Verify multiple-filter mode detection. Expected Result: Only non-empty filter lists
   * enable multi-filter mode. Assertions: hasMultipleFilters reflects filter list state.
   */
  @Test
  @DisplayName("paginationBaseRequestModel - HasMultipleFilters Checks ListState - Success")
  void paginationBaseRequestModel_s02_hasMultipleFiltersChecksListState_success() {
    // Arrange
    PaginationBaseRequestModel model = new PaginationBaseRequestModel();
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();

    // Act
    model.setFilters(null);
    boolean nullState = model.hasMultipleFilters();
    model.setFilters(List.of());
    boolean emptyState = model.hasMultipleFilters();
    model.setFilters(List.of(filter));
    boolean populatedState = model.hasMultipleFilters();

    // Assert
    assertFalse(nullState);
    assertFalse(emptyState);
    assertTrue(populatedState);
  }

  /**
   * Purpose: Verify symbol operators normalize to word-format operators for query-builder
   * compatibility. Expected Result: Symbol operators map to corresponding normalized values.
   * Assertions: Normalized operators match expected constants.
   */
  @Test
  @DisplayName("paginationBaseRequestModel - FilterCondition Normalizes SymbolOperators - Success")
  void paginationBaseRequestModel_s03_filterConditionNormalizesSymbolOperators_success() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();

    // Act
    filter.setOperator(">=");
    String first = filter.getOperator();
    filter.setOperator("!=");
    String second = filter.getOperator();
    filter.setOperator("contains");
    String third = filter.getOperator();

    // Assert
    assertEquals(PaginationBaseRequestModel.OP_GREATER_THAN_OR_EQUAL, first);
    assertEquals(PaginationBaseRequestModel.OP_NOT_EQUAL, second);
    assertEquals(PaginationBaseRequestModel.OP_CONTAINS, third);
  }

  /**
   * Purpose: Verify operator/type validation, including exact exception message for invalid
   * combinations. Expected Result: Valid combinations pass; invalid combinations throw
   * IllegalArgumentException. Assertions: Type checks and exact exception message are validated.
   */
  @Test
  @DisplayName("paginationBaseRequestModel - FilterCondition OperatorTypeValidation - Success")
  void paginationBaseRequestModel_s04_filterConditionOperatorTypeValidation_success() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("amount");
    filter.setOperator("greaterThan");

    // Act
    boolean validForNumber = filter.isValidOperatorForType("number");
    boolean invalidForString = filter.isValidOperatorForType("string");
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> filter.validateOperatorForType("string", "amount"));

    // Assert
    assertTrue(validForNumber);
    assertFalse(invalidForString);
    assertEquals(
        "Invalid operator 'greaterThan' for string column 'amount'. Valid operators: contains, "
            + "equals, startsWith, endsWith, isEmpty, isNotEmpty, isOneOf, isNotOneOf, "
            + "containsOneOf",
        ex.getMessage());
  }

  /**
   * Purpose: Verify value presence validation for operators that require values and operators that
   * do not. Expected Result: isEmpty/isNotEmpty allow null; value-required operators throw when
   * value is missing. Assertions: Exact exception message is validated for missing required value.
   */
  @Test
  @DisplayName("paginationBaseRequestModel - FilterCondition ValuePresenceValidation - Success")
  void paginationBaseRequestModel_s05_filterConditionValuePresenceValidation_success() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("title");
    filter.setOperator("isEmpty");
    filter.setValue(null);

    // Act
    filter.validateValuePresence();
    filter.setOperator("equals");
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, filter::validateValuePresence);

    // Assert
    assertEquals(
        "Filter value is required for operator 'equals' on column 'title'", ex.getMessage());
  }
}
