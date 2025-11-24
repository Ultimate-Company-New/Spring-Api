package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;

/**
 * Interface for Product operations and data access.
 * 
 * This interface defines the contract for all product-related business operations
 * including CRUD operations, product management, and public product access.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IProductSubTranslator {
    
    /**
     * Adds a new product.
     * 
     * This method creates a new product with the provided details including
     * title, description, pricing, category, and other product attributes.
     * 
     * @param productRequestModel The product to create
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user is not authorized
     */
    void addProduct(ProductRequestModel productRequestModel);
    
    /**
     * Edits an existing product.
     * 
     * This method updates an existing product with the provided details.
     * All product fields can be updated except the product ID.
     * 
     * @param productRequestModel The product data to update
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     * @throws UnauthorizedException if user is not authorized
     */
    void editProduct(ProductRequestModel productRequestModel);
    
    /**
     * Toggles the deleted status of a product (soft delete/restore).
     * 
     * This method toggles the deleted flag of a product without permanently
     * removing it from the database. Deleted products are hidden from standard queries.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     * @throws UnauthorizedException if user is not authorized
     */
    void toggleDeleteProduct(long id);
    
    /**
     * Toggles the return eligibility status of a product.
     * 
     * This method toggles whether a product can be returned by customers.
     * This affects the return policy displayed to customers during checkout.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     * @throws UnauthorizedException if user is not authorized
     */
    void toggleReturnProduct(long id);
    
    /**
     * Retrieves detailed information about a specific product by ID.
     * 
     * This method returns comprehensive product details including title,
     * description, pricing, images, category, and availability information.
     * 
     * @param id The ID of the product to retrieve
     * @return The product details
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     * @throws UnauthorizedException if user is not authorized
     */
    ProductResponseModel getProductDetailsById(long id);
    
    /**
     * Retrieves products in batches with pagination support.
     * 
     * This method returns a paginated list of products based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return Paginated response containing product data
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user is not authorized
     */
    PaginationBaseResponseModel<ProductResponseModel> getProductInBatches(PaginationBaseRequestModel paginationBaseRequestModel);
    
    /**
     * Creates multiple products in a single operation.
     * 
     * @param products List of ProductRequestModel containing the product data to insert
     * @return BulkInsertResponseModel containing success/failure details for each product
     */
    com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkAddProducts(java.util.List<ProductRequestModel> products);
}
