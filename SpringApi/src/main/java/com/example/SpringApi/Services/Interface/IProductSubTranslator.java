package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductCategoryWithPathResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import java.util.List;

/**
 * Interface for Product operations and data access.
 *
 * <p>This interface defines the contract for all product-related business operations including CRUD
 * operations, product management, and public product access.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IProductSubTranslator {

  /**
   * Adds a new product.
   *
   * <p>This method creates a new product with the provided details including title, description,
   * pricing, category, and other product attributes.
   *
   * @param productRequestModel The product to create
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  void addProduct(ProductRequestModel productRequestModel);

  /**
   * Edits an existing product.
   *
   * <p>This method updates an existing product with the provided details. All product fields can be
   * updated except the product ID.
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
   * <p>This method toggles the deleted flag of a product without permanently removing it from the
   * database. Deleted products are hidden from standard queries.
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
   * <p>This method toggles whether a product can be returned by customers. This affects the return
   * policy displayed to customers during checkout.
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
   * <p>This method returns comprehensive product details including title, description, pricing,
   * images, category, and availability information.
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
   * <p>This method returns a paginated list of products based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * @param paginationBaseRequestModel The pagination parameters
   * @return Paginated response containing product data
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  PaginationBaseResponseModel<ProductResponseModel> getProductInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel);

  /**
   * Creates multiple products asynchronously in a single operation with partial success support.
   *
   * <p>This method processes products in a background thread with the following characteristics: -
   * Supports partial success: if some products fail validation, others still succeed - Sends
   * detailed results to user via message notification after processing completes - NOT_SUPPORTED:
   * Runs without a transaction to avoid rollback-only issues when individual product creations fail
   *
   * @param products List of ProductRequestModel containing the product data to insert
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  void bulkAddProductsAsync(
      java.util.List<ProductRequestModel> products,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId);

  /**
   * Creates multiple products synchronously in a single operation (for testing).
   *
   * <p>This is a synchronous wrapper for unit tests that processes products immediately and returns
   * the bulk insert result with success/failure details for each product.
   *
   * @param products List of ProductRequestModel containing the product data to insert
   * @return BulkInsertResponseModel containing success/failure details for each product
   */
  com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkAddProducts(
      java.util.List<ProductRequestModel> products);

  /**
   * Retrieves categories based on parent ID for hierarchical navigation.
   *
   * <p>If parentId is null: Returns all root categories (categories with null parentId) If parentId
   * is provided: Returns all child categories of that parent (where isEnd=true)
   *
   * <p>This enables drill-down category navigation where users can browse the hierarchy level by
   * level until they reach assignable leaf categories.
   *
   * @param parentId The parent category ID (null for root categories)
   * @return List of ProductCategoryWithPathResponseModel containing categories with full paths
   */
  List<ProductCategoryWithPathResponseModel> findCategoriesByParentId(Long parentId);

  /**
   * Retrieves full category paths for a list of category IDs.
   *
   * <p>This method takes a list of category IDs and returns a mapping of each ID to its full
   * hierarchical path (e.g., "Electronics > Computers > Laptops"). Useful for bulk operations like
   * product import where multiple category paths need to be resolved efficiently.
   *
   * @param categoryIds List of category IDs to get paths for
   * @return Map of category ID to full path string
   */
  java.util.Map<Long, String> getCategoryPathsByIds(List<Long> categoryIds);
}
