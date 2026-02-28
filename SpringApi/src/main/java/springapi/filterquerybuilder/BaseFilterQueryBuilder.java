package com.example.springapi.filterquerybuilder;

import com.example.springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the base filter query builder component.
 */
public abstract class BaseFilterQueryBuilder {
  private static final String OP_EQUALS = "equals";
  private static final String OP_IS_EMPTY = "isEmpty";
  private static final String OP_IS_NOT_EMPTY = "isNotEmpty";
  private static final String LOWER_PREFIX = "LOWER(";
  private static final String LIKE_LOWER_PARAM = ") LIKE LOWER(:";
  private static final String DATE_PREFIX = "DATE(";

  /**
   * Maps frontend column names to database field paths. Example: "firstName" -> "u.first_name" or.
   * "u.firstName" depending on your entity mapping
   */
  protected abstract String mapColumnToField(String column);

  /** Returns list of date column names. */
  protected abstract List<String> getDateColumns();

  /** Returns list of boolean column names. */
  protected abstract List<String> getBooleanColumns();

  /** Returns list of number column names. */
  protected abstract List<String> getNumberColumns();

  /**
   * Builds filter conditions for a list of FilterCondition objects. Returns a QueryResult.
   * containing the WHERE clause and parameter map.
   *
   * @param filters List of filter conditions
   * @param logicOperator "AND" or "OR" to combine conditions
   * @return QueryResult with WHERE clause and parameters
   */
  public QueryResult buildFilterConditions(List<FilterCondition> filters, String logicOperator) {
    if (filters == null || filters.isEmpty()) {
      return new QueryResult("", new HashMap<>());
    }

    StringBuilder whereClause = new StringBuilder();
    Map<String, Object> parameters = new HashMap<>();
    String operator = " " + logicOperator.toUpperCase() + " ";

    for (int i = 0; i < filters.size(); i++) {
      FilterCondition filter = filters.get(i);
      String column = filter.getColumn();
      String filterOperator = filter.getOperator();
      Object value = filter.getValue();

      String fieldPath = mapColumnToField(column);
      String paramName = "param" + i;

      if (i > 0) {
        whereClause.append(operator);
      }

      // Build condition based on column type
      if (getDateColumns().contains(column)) {
        whereClause.append(
            buildDateCondition(fieldPath, filterOperator, value, paramName, parameters));
      } else if (getBooleanColumns().contains(column)) {
        whereClause.append(
            buildBooleanCondition(fieldPath, filterOperator, value, paramName, parameters));
      } else if (getNumberColumns().contains(column)) {
        whereClause.append(
            buildNumberCondition(fieldPath, filterOperator, value, paramName, parameters));
      } else {
        whereClause.append(
            buildStringCondition(fieldPath, filterOperator, value, paramName, parameters));
      }
    }

    return new QueryResult(whereClause.toString(), parameters);
  }

  /** Builds a string filter condition. */
  private String buildStringCondition(
      String fieldPath,
      String operator,
      Object value,
      String paramName,
      Map<String, Object> parameters) {
    switch (operator) {
      case "contains":
        parameters.put(paramName, "%" + value + "%");
        return LOWER_PREFIX + fieldPath + LIKE_LOWER_PARAM + paramName + ")";
      case OP_EQUALS:
        parameters.put(paramName, value);
        return LOWER_PREFIX + fieldPath + ") = LOWER(:" + paramName + ")";
      case "startsWith":
        parameters.put(paramName, value + "%");
        return LOWER_PREFIX + fieldPath + LIKE_LOWER_PARAM + paramName + ")";
      case "endsWith":
        parameters.put(paramName, "%" + value);
        return LOWER_PREFIX + fieldPath + LIKE_LOWER_PARAM + paramName + ")";
      case OP_IS_EMPTY:
        return "(" + fieldPath + " IS NULL OR " + fieldPath + " = '')";
      case OP_IS_NOT_EMPTY:
        return "(" + fieldPath + " IS NOT NULL AND " + fieldPath + " != '')";
      case "isOneOf":
        return buildIsOneOfCondition(fieldPath, value, paramName, parameters, false);
      case "isNotOneOf":
        return buildIsOneOfCondition(fieldPath, value, paramName, parameters, true);
      case "containsOneOf":
        return buildContainsOneOfCondition(fieldPath, value, paramName, parameters);
      default:
        return "1=1"; // No-op condition
    }
  }

  /**
   * Builds "is one of" or "is not one of" condition for semicolon-separated values Example: value
   * =. "A;B;C" becomes: field IN ('A', 'B', 'C') or field NOT IN ('A', 'B', 'C')
   */
  private String buildIsOneOfCondition(
      String fieldPath,
      Object value,
      String paramName,
      Map<String, Object> parameters,
      boolean negate) {
    if (value == null) {
      return "1=1";
    }

    String valueStr = String.valueOf(value);
    String[] values = valueStr.split(";");

    // Trim and filter empty values
    List<String> cleanedValues = new java.util.ArrayList<>();
    for (String val : values) {
      String trimmed = val.trim();
      if (!trimmed.isEmpty()) {
        cleanedValues.add(trimmed.toLowerCase());
      }
    }

    if (cleanedValues.isEmpty()) {
      return "1=1";
    }

    // Store the list of values as a parameter
    parameters.put(paramName, cleanedValues);

    if (negate) {
      return LOWER_PREFIX + fieldPath + ") NOT IN (:" + paramName + ")";
    } else {
      return LOWER_PREFIX + fieldPath + ") IN (:" + paramName + ")";
    }
  }

  /**
   * Builds "contains one of" condition for semicolon-separated values Example: value = "A;B;C".
   * becomes: (field LIKE '%A%' OR field LIKE '%B%' OR field LIKE '%C%')
   */
  private String buildContainsOneOfCondition(
      String fieldPath, Object value, String paramName, Map<String, Object> parameters) {
    if (value == null) {
      return "1=1";
    }

    String valueStr = String.valueOf(value);
    String[] values = valueStr.split(";");

    // Build OR conditions for each value
    StringBuilder condition = new StringBuilder("(");
    boolean first = true;
    int index = 0;

    for (String val : values) {
      String trimmed = val.trim();
      if (!trimmed.isEmpty()) {
        if (!first) {
          condition.append(" OR ");
        }

        String subParamName = paramName + "_" + index;
        parameters.put(subParamName, "%" + trimmed + "%");
        condition
            .append(LOWER_PREFIX)
            .append(fieldPath)
            .append(LIKE_LOWER_PARAM)
            .append(subParamName)
            .append(")");

        first = false;
        index++;
      }
    }

    condition.append(")");

    // If no valid values, return no-op
    if (index == 0) {
      return "1=1";
    }

    return condition.toString();
  }

  /**
   * Builds a number filter condition. Accepts both symbol operators (=, !=, etc.) and word.
   * operators (equals, notEquals, etc.) for backwards compatibility, though word format is
   * preferred.
   */
  private String buildNumberCondition(
      String fieldPath,
      String operator,
      Object value,
      String paramName,
      Map<String, Object> parameters) {
    // Convert string values to numbers for proper comparison
    Object numericValue = parseNumericValue(value);

    switch (operator) {
      case OP_EQUALS, "=":
        parameters.put(paramName, numericValue);
        return fieldPath + " = :" + paramName;
      case "notEquals", "!=":
        parameters.put(paramName, numericValue);
        return fieldPath + " != :" + paramName;
      case "greaterThan", ">":
        parameters.put(paramName, numericValue);
        return fieldPath + " > :" + paramName;
      case "greaterThanOrEqual", ">=":
        parameters.put(paramName, numericValue);
        return fieldPath + " >= :" + paramName;
      case "lessThan", "<":
        parameters.put(paramName, numericValue);
        return fieldPath + " < :" + paramName;
      case "lessThanOrEqual", "<=":
        parameters.put(paramName, numericValue);
        return fieldPath + " <= :" + paramName;
      case OP_IS_EMPTY:
        return fieldPath + " IS NULL";
      case OP_IS_NOT_EMPTY:
        return fieldPath + " IS NOT NULL";
      case "isOneOf":
        return buildNumberIsOneOfCondition(fieldPath, value, paramName, parameters, false);
      case "isNotOneOf":
        return buildNumberIsOneOfCondition(fieldPath, value, paramName, parameters, true);
      default:
        return "1=1";
    }
  }

  /**
   * Builds "is one of" or "is not one of" condition for semicolon or comma-separated numeric.
   * values. Example: value = "1,2,3" or "1;2;3" becomes: field IN (1, 2, 3) or field NOT IN (1, 2,
   * 3)
   */
  private String buildNumberIsOneOfCondition(
      String fieldPath,
      Object value,
      String paramName,
      Map<String, Object> parameters,
      boolean negate) {
    if (value == null) {
      return "1=1";
    }

    String valueStr = String.valueOf(value);
    // Support both comma and semicolon as separators
    String[] values = valueStr.contains(";") ? valueStr.split(";") : valueStr.split(",");

    // Parse and filter valid numeric values
    List<Long> numericValues = new java.util.ArrayList<>();
    for (String val : values) {
      String trimmed = val.trim();
      if (!trimmed.isEmpty()) {
        try {
          numericValues.add(Long.parseLong(trimmed));
        } catch (NumberFormatException e) {
          // Skip invalid numbers
        }
      }
    }

    if (numericValues.isEmpty()) {
      return "1=1";
    }

    // Store the list of values as a parameter
    parameters.put(paramName, numericValues);

    if (negate) {
      return fieldPath + " NOT IN (:" + paramName + ")";
    } else {
      return fieldPath + " IN (:" + paramName + ")";
    }
  }

  /**
   * Parses a value to a numeric type (Long or Double). If already a number, returns as-is. If a.
   * string, attempts to parse.
   */
  private Object parseNumericValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number;
    }
    if (value instanceof String stringValue) {
      String strValue = stringValue.trim();
      try {
        // Try parsing as Long first (for IDs)
        if (!strValue.contains(".")) {
          return Long.parseLong(strValue);
        }
        // Otherwise parse as Double
        return Double.parseDouble(strValue);
      } catch (NumberFormatException e) {
        return value; // Return original if parsing fails
      }
    }
    return value;
  }

  /**
   * Builds a date filter condition. Supports both date-specific operators (is, isNot, etc.) and.
   * number-style operators (equals, greaterThan, etc.)
   */
  private String buildDateCondition(
      String fieldPath,
      String operator,
      Object value,
      String paramName,
      Map<String, Object> parameters) {
    // Parse date value
    LocalDate dateValue = null;
    if (value instanceof String stringValue) {
      try {
        dateValue = LocalDate.parse(stringValue);
      } catch (Exception e) {
        try {
          LocalDateTime dateTime = LocalDateTime.parse(stringValue);
          dateValue = dateTime.toLocalDate();
        } catch (Exception ex) {
          return "1=1"; // Invalid date, return no-op
        }
      }
    }

    switch (operator) {
      case "is", OP_EQUALS:
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") = :" + paramName;
      case "isNot", "notEquals":
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") != :" + paramName;
      case "isAfter", "greaterThan":
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") > :" + paramName;
      case "isOnOrAfter", "greaterThanOrEqual":
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") >= :" + paramName;
      case "isBefore", "lessThan":
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") < :" + paramName;
      case "isOnOrBefore", "lessThanOrEqual":
        parameters.put(paramName, dateValue);
        return DATE_PREFIX + fieldPath + ") <= :" + paramName;
      case OP_IS_EMPTY:
        return fieldPath + " IS NULL";
      case OP_IS_NOT_EMPTY:
        return fieldPath + " IS NOT NULL";
      default:
        return "1=1";
    }
  }

  /** Builds a boolean filter condition. */
  private String buildBooleanCondition(
      String fieldPath,
      String operator,
      Object value,
      String paramName,
      Map<String, Object> parameters) {
    if ("is".equals(operator)) {
      Boolean boolValue = null;
      if (value instanceof String stringValue) {
        boolValue = Boolean.parseBoolean(stringValue);
      } else if (value instanceof Boolean booleanValue) {
        boolValue = booleanValue;
      }
      parameters.put(paramName, boolValue);
      return fieldPath + " = :" + paramName;
    }
    return "1=1";
  }

  /** Result class containing the WHERE clause and parameters. */
  public static class QueryResult {
    private final String whereClause;
    private final Map<String, Object> parameters;

    public QueryResult(String whereClause, Map<String, Object> parameters) {
      this.whereClause = whereClause;
      this.parameters = parameters;
    }

    public String getWhereClause() {
      return whereClause;
    }

    public Map<String, Object> getParameters() {
      return parameters;
    }

    public boolean hasConditions() {
      return whereClause != null && !whereClause.isEmpty();
    }
  }
}
