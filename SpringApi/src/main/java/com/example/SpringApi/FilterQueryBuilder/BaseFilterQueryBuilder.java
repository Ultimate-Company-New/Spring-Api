package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to build dynamic filter queries for JPA repositories.
 * This class generates WHERE clause conditions based on FilterCondition objects.
 * 
 * Usage:
 * 1. Create a subclass for each entity (UserFilterQueryBuilder, LeadFilterQueryBuilder, etc.)
 * 2. Implement the abstract methods for column mappings
 * 3. Call buildFilterConditions() to get the WHERE clause and parameters
 * 4. Use the generated query string in @Query annotation with native query or JPQL
 * 
 * Example:
 * ```java
 * UserFilterQueryBuilder builder = new UserFilterQueryBuilder();
 * QueryResult result = builder.buildFilterConditions(filters, "AND");
 * String whereClause = result.getWhereClause();
 * Map<String, Object> params = result.getParameters();
 * ```
 */
public abstract class BaseFilterQueryBuilder {

    /**
     * Maps frontend column names to database field paths.
     * Example: "firstName" -> "u.first_name" or "u.firstName" depending on your entity mapping
     */
    protected abstract String mapColumnToField(String column);

    /**
     * Returns list of date column names
     */
    protected abstract List<String> getDateColumns();

    /**
     * Returns list of boolean column names
     */
    protected abstract List<String> getBooleanColumns();

    /**
     * Returns list of number column names
     */
    protected abstract List<String> getNumberColumns();

    /**
     * Builds filter conditions for a list of FilterCondition objects.
     * Returns a QueryResult containing the WHERE clause and parameter map.
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
                whereClause.append(buildDateCondition(fieldPath, filterOperator, value, paramName, parameters));
            } else if (getBooleanColumns().contains(column)) {
                whereClause.append(buildBooleanCondition(fieldPath, filterOperator, value, paramName, parameters));
            } else if (getNumberColumns().contains(column)) {
                whereClause.append(buildNumberCondition(fieldPath, filterOperator, value, paramName, parameters));
            } else {
                whereClause.append(buildStringCondition(fieldPath, filterOperator, value, paramName, parameters));
            }
        }

        return new QueryResult(whereClause.toString(), parameters);
    }

    /**
     * Builds a string filter condition
     */
    private String buildStringCondition(String fieldPath, String operator, Object value, String paramName, Map<String, Object> parameters) {
        switch (operator) {
            case "contains":
                parameters.put(paramName, "%" + value + "%");
                return "LOWER(" + fieldPath + ") LIKE LOWER(:" + paramName + ")";
            case "equals":
                parameters.put(paramName, value);
                return "LOWER(" + fieldPath + ") = LOWER(:" + paramName + ")";
            case "startsWith":
                parameters.put(paramName, value + "%");
                return "LOWER(" + fieldPath + ") LIKE LOWER(:" + paramName + ")";
            case "endsWith":
                parameters.put(paramName, "%" + value);
                return "LOWER(" + fieldPath + ") LIKE LOWER(:" + paramName + ")";
            case "isEmpty":
                return "(" + fieldPath + " IS NULL OR " + fieldPath + " = '')";
            case "isNotEmpty":
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
     * Builds "is one of" or "is not one of" condition for semicolon-separated values
     * Example: value = "A;B;C" becomes: field IN ('A', 'B', 'C') or field NOT IN ('A', 'B', 'C')
     */
    private String buildIsOneOfCondition(String fieldPath, Object value, String paramName, Map<String, Object> parameters, boolean negate) {
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
            return "LOWER(" + fieldPath + ") NOT IN (:" + paramName + ")";
        } else {
            return "LOWER(" + fieldPath + ") IN (:" + paramName + ")";
        }
    }
    
    /**
     * Builds "contains one of" condition for semicolon-separated values
     * Example: value = "A;B;C" becomes: (field LIKE '%A%' OR field LIKE '%B%' OR field LIKE '%C%')
     */
    private String buildContainsOneOfCondition(String fieldPath, Object value, String paramName, Map<String, Object> parameters) {
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
                condition.append("LOWER(").append(fieldPath).append(") LIKE LOWER(:").append(subParamName).append(")");
                
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
     * Builds a number filter condition
     */
    private String buildNumberCondition(String fieldPath, String operator, Object value, String paramName, Map<String, Object> parameters) {
        switch (operator) {
            case "=":
                parameters.put(paramName, value);
                return fieldPath + " = :" + paramName;
            case "!=":
                parameters.put(paramName, value);
                return fieldPath + " != :" + paramName;
            case ">":
                parameters.put(paramName, value);
                return fieldPath + " > :" + paramName;
            case ">=":
                parameters.put(paramName, value);
                return fieldPath + " >= :" + paramName;
            case "<":
                parameters.put(paramName, value);
                return fieldPath + " < :" + paramName;
            case "<=":
                parameters.put(paramName, value);
                return fieldPath + " <= :" + paramName;
            case "isEmpty":
                return fieldPath + " IS NULL";
            case "isNotEmpty":
                return fieldPath + " IS NOT NULL";
            default:
                return "1=1";
        }
    }

    /**
     * Builds a date filter condition
     */
    private String buildDateCondition(String fieldPath, String operator, Object value, String paramName, Map<String, Object> parameters) {
        // Parse date value
        LocalDate dateValue = null;
        if (value instanceof String) {
            try {
                dateValue = LocalDate.parse((String) value);
            } catch (Exception e) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse((String) value);
                    dateValue = dateTime.toLocalDate();
                } catch (Exception ex) {
                    return "1=1"; // Invalid date, return no-op
                }
            }
        }

        switch (operator) {
            case "is":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") = :" + paramName;
            case "isNot":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") != :" + paramName;
            case "isAfter":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") > :" + paramName;
            case "isOnOrAfter":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") >= :" + paramName;
            case "isBefore":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") < :" + paramName;
            case "isOnOrBefore":
                parameters.put(paramName, dateValue);
                return "DATE(" + fieldPath + ") <= :" + paramName;
            case "isEmpty":
                return fieldPath + " IS NULL";
            case "isNotEmpty":
                return fieldPath + " IS NOT NULL";
            default:
                return "1=1";
        }
    }

    /**
     * Builds a boolean filter condition
     */
    private String buildBooleanCondition(String fieldPath, String operator, Object value, String paramName, Map<String, Object> parameters) {
        if ("is".equals(operator)) {
            Boolean boolValue = null;
            if (value instanceof String) {
                boolValue = Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                boolValue = (Boolean) value;
            }
            parameters.put(paramName, boolValue);
            return fieldPath + " = :" + paramName;
        }
        return "1=1";
    }

    /**
     * Result class containing the WHERE clause and parameters
     */
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

