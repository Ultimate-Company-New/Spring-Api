package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PaginationBaseRequestModel {
    
    // ==================== Operator Constants ====================
    
    // String operators
    public static final String OP_CONTAINS = "contains";
    public static final String OP_EQUALS = "equals";
    public static final String OP_STARTS_WITH = "startsWith";
    public static final String OP_ENDS_WITH = "endsWith";
    public static final String OP_IS_EMPTY = "isEmpty";
    public static final String OP_IS_NOT_EMPTY = "isNotEmpty";
    public static final String OP_IS_ONE_OF = "isOneOf";
    public static final String OP_IS_NOT_ONE_OF = "isNotOneOf";
    public static final String OP_CONTAINS_ONE_OF = "containsOneOf";
    
    // Number operators (word format for query builders)
    public static final String OP_EQUAL = "equals";
    public static final String OP_NOT_EQUAL = "notEquals";
    public static final String OP_GREATER_THAN = "greaterThan";
    public static final String OP_GREATER_THAN_OR_EQUAL = "greaterThanOrEqual";
    public static final String OP_LESS_THAN = "lessThan";
    public static final String OP_LESS_THAN_OR_EQUAL = "lessThanOrEqual";
    public static final String OP_NUMBER_IS_ONE_OF = "isOneOf";
    public static final String OP_NUMBER_IS_NOT_ONE_OF = "isNotOneOf";
    
    // Symbol operators (accepted from frontend, normalized to word format)
    public static final String OP_SYMBOL_EQUAL = "=";
    public static final String OP_SYMBOL_NOT_EQUAL = "!=";
    public static final String OP_SYMBOL_GREATER_THAN = ">";
    public static final String OP_SYMBOL_GREATER_THAN_OR_EQUAL = ">=";
    public static final String OP_SYMBOL_LESS_THAN = "<";
    public static final String OP_SYMBOL_LESS_THAN_OR_EQUAL = "<=";
    
    // Date operators
    public static final String OP_IS = "is";
    public static final String OP_IS_NOT = "isNot";
    public static final String OP_IS_AFTER = "isAfter";
    public static final String OP_IS_ON_OR_AFTER = "isOnOrAfter";
    public static final String OP_IS_BEFORE = "isBefore";
    public static final String OP_IS_ON_OR_BEFORE = "isOnOrBefore";
    
    // Logic operators
    public static final String LOGIC_AND = "AND";
    public static final String LOGIC_OR = "OR";
    
    // ==================== Fields ====================
    
    private Long id; // Used for specific entity lookups (e.g., messageId, userId)
    private int start;
    private int end;
    
    
    private int pageSize;
    private boolean includeDeleted;
    private boolean includeExpired;
    private List<Long> selectedIds;
    
    // Multi-filter support
    private String logicOperator; // "AND" or "OR"
    private List<FilterCondition> filters;
    
    /**
     * Validates the logic operator
     */
    public boolean isValidLogicOperator() {
        if (logicOperator == null) {
            return true; // Optional field
        }
        return "AND".equalsIgnoreCase(logicOperator) || "OR".equalsIgnoreCase(logicOperator);
    }
    
    /**
     * Checks if multi-filter mode is enabled
     */
    public boolean hasMultipleFilters() {
        return filters != null && !filters.isEmpty();
    }
    
    /**
     * Inner class representing a single filter condition
     */
    @Getter
    public static class FilterCondition {
        private String column;
        private String operator;
        private Object value;
        
        // Symbol operators that can be sent from frontend (will be normalized)
        public static final List<String> SYMBOL_OPERATORS = Arrays.asList(
            OP_SYMBOL_EQUAL, OP_SYMBOL_NOT_EQUAL, OP_SYMBOL_GREATER_THAN, 
            OP_SYMBOL_GREATER_THAN_OR_EQUAL, OP_SYMBOL_LESS_THAN, OP_SYMBOL_LESS_THAN_OR_EQUAL
        );
        
        // Valid operators for different column types (using constants)
        public static final List<String> STRING_OPERATORS = Arrays.asList(
            OP_CONTAINS, OP_EQUALS, OP_STARTS_WITH, OP_ENDS_WITH, OP_IS_EMPTY, OP_IS_NOT_EMPTY,
            OP_IS_ONE_OF, OP_IS_NOT_ONE_OF, OP_CONTAINS_ONE_OF
        );
        
        public static final List<String> NUMBER_OPERATORS = Arrays.asList(
            OP_EQUAL, OP_NOT_EQUAL, OP_GREATER_THAN, OP_GREATER_THAN_OR_EQUAL, 
            OP_LESS_THAN, OP_LESS_THAN_OR_EQUAL, OP_IS_EMPTY, OP_IS_NOT_EMPTY,
            OP_NUMBER_IS_ONE_OF, OP_NUMBER_IS_NOT_ONE_OF
        );
        
        public static final List<String> DATE_OPERATORS = Arrays.asList(
            OP_IS, OP_IS_NOT, OP_IS_AFTER, OP_IS_ON_OR_AFTER, 
            OP_IS_BEFORE, OP_IS_ON_OR_BEFORE, OP_IS_EMPTY, OP_IS_NOT_EMPTY
        );
        
        public static final List<String> BOOLEAN_OPERATORS = Arrays.asList(OP_IS);
        
        // Setters with normalization
        public void setColumn(String column) {
            this.column = column;
        }
        
        public void setValue(Object value) {
            this.value = value;
        }
        
        /**
         * Sets the operator and normalizes symbol operators to word format.
         * This ensures query builders always receive word format operators.
         */
        public void setOperator(String operator) {
            this.operator = normalizeOperator(operator);
        }
        
        /**
         * Normalizes symbol operators to word format for query builder compatibility.
         * 
         * @param op The operator (may be symbol or word format)
         * @return The normalized operator in word format
         */
        private String normalizeOperator(String op) {
            if (op == null) {
                return null;
            }
            return switch (op) {
                case "=" -> OP_EQUAL;
                case "!=" -> OP_NOT_EQUAL;
                case ">" -> OP_GREATER_THAN;
                case ">=" -> OP_GREATER_THAN_OR_EQUAL;
                case "<" -> OP_LESS_THAN;
                case "<=" -> OP_LESS_THAN_OR_EQUAL;
                default -> op;
            };
        }
        
        /**
         * Validates if the operator is valid for any column type.
         * Accepts both symbol and word format operators.
         */
        public boolean isValidOperator() {
            if (operator == null) {
                return false;
            }
            // After normalization, operator should be in word format
            // But also accept symbols in case validation is called before normalization
            return STRING_OPERATORS.contains(operator) || 
                   NUMBER_OPERATORS.contains(operator) || 
                   DATE_OPERATORS.contains(operator) || 
                   BOOLEAN_OPERATORS.contains(operator) ||
                   SYMBOL_OPERATORS.contains(operator);
        }
        
        /**
         * Validates if the operator is valid for a specific column type
         * 
         * @param columnType "string", "number", "date", or "boolean"
         * @return true if operator is valid for the column type
         */
        public boolean isValidOperatorForType(String columnType) {
            if (operator == null || columnType == null) {
                return false;
            }
            
            switch (columnType.toLowerCase()) {
                case "string":
                    return STRING_OPERATORS.contains(operator);
                case "number":
                    return NUMBER_OPERATORS.contains(operator);
                case "date":
                    return DATE_OPERATORS.contains(operator);
                case "boolean":
                    return BOOLEAN_OPERATORS.contains(operator);
                default:
                    return false;
            }
        }
        
        /**
         * Validates that the operator matches the column type and throws exception if invalid
         * 
         * @param columnType "string", "number", "date", or "boolean"
         * @param columnName Name of the column for error message
         * @throws IllegalArgumentException if operator is invalid for the column type
         */
        public void validateOperatorForType(String columnType, String columnName) {
            if (!isValidOperatorForType(columnType)) {
                String validOperators;
                switch (columnType.toLowerCase()) {
                    case "string":
                        validOperators = String.join(", ", STRING_OPERATORS);
                        break;
                    case "number":
                        validOperators = String.join(", ", NUMBER_OPERATORS);
                        break;
                    case "date":
                        validOperators = String.join(", ", DATE_OPERATORS);
                        break;
                    case "boolean":
                        validOperators = String.join(", ", BOOLEAN_OPERATORS);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid column type: " + columnType);
                }
                
                throw new IllegalArgumentException(
                    String.format("Invalid operator '%s' for %s column '%s'. Valid operators: %s",
                        operator, columnType, columnName, validOperators)
                );
            }
        }
        
        /**
         * Validates that a value is provided when required by the operator
         * 
         * @throws IllegalArgumentException if value is missing when required
         */
        public void validateValuePresence() {
            // isEmpty and isNotEmpty operators don't require a value
            if (OP_IS_EMPTY.equals(operator) || OP_IS_NOT_EMPTY.equals(operator)) {
                return;
            }
            
            if (value == null) {
                throw new IllegalArgumentException(
                    String.format("Filter value is required for operator '%s' on column '%s'", 
                        operator, column)
                );
            }
        }
    }
}
