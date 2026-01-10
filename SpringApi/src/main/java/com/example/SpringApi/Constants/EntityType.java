package com.example.SpringApi.Constants;

/**
 * Constants for entity types used in the Resources table.
 * 
 * This class defines the valid entity types that can be associated with resources (attachments).
 * These values must match the CHECK constraint in the Resources table.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-10
 */
public final class EntityType {
    
    // Private constructor to prevent instantiation
    private EntityType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * Entity type for Purchase Order resources
     */
    public static final String PURCHASE_ORDER = "PurchaseOrder";
    
    /**
     * Entity type for Lead resources
     */
    public static final String LEAD = "Lead";
    
    /**
     * Entity type for User resources
     */
    public static final String USER = "User";
    
    /**
     * Entity type for Product resources
     */
    public static final String PRODUCT = "Product";
    
    /**
     * Entity type for other/miscellaneous resources
     */
    public static final String OTHER = "Other";
}

