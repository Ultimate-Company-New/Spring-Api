package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Services.Interface.IProductSubTranslator;
import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductCategoryWithPathResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductCategory;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Repositories.ProductRepository;
import com.example.SpringApi.Repositories.ProductCategoryRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Constants.ProductImageConstants;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;

import java.util.ArrayList;
import java.util.Collections;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;

/**
 * Service implementation for Product operations.
 * This service handles all business logic related to product management
 * including CRUD operations, product availability, and public product access.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ProductService extends BaseService implements IProductSubTranslator {

    private final Logger logger;

    private final ProductRepository productRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    private final ClientService clientService;
    private final ProductFilterQueryBuilder productFilterQueryBuilder;
    private final MessageService messageService;
    private final Environment environment;

    @Value("${imageLocation:firebase}")
    private String imageLocation;

    @Autowired
    public ProductService(ProductRepository productRepository,
            ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
            com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
            ProductCategoryRepository productCategoryRepository,
            ClientRepository clientRepository,
            UserLogService userLogService,
            ClientService clientService,
            ProductFilterQueryBuilder productFilterQueryBuilder,
            MessageService messageService,
            Environment environment,
            JwtTokenProvider jwtTokenProvider,
            HttpServletRequest request) {
        super(jwtTokenProvider, request);
        this.logger = LoggerFactory.getLogger(ProductService.class);
        this.productRepository = productRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.clientRepository = clientRepository;
        this.userLogService = userLogService;
        this.clientService = clientService;
        this.productFilterQueryBuilder = productFilterQueryBuilder;
        this.messageService = messageService;
        this.environment = environment;
    }

    /**
     * Adds a new product.
     * This method creates a new product with the provided details including
     * title, description, pricing, category, and other product attributes.
     * It validates the category existence, saves the product first to get the ID,
     * then handles multiple product images by processing URLs to base64 and
     * uploading them to ImgBB storage. Finally, it creates pickup location mappings
     * for the product based on the provided pickup location quantities.
     * 
     * @param productRequestModel The product to create
     * @throws BadRequestException if validation fails
     */
    @Override
    @Transactional
    public void addProduct(ProductRequestModel productRequestModel) {
        Long clientId = productRequestModel.getClientId() != null ? productRequestModel.getClientId() : getClientId();
        persistProduct(productRequestModel, getUser(), clientId);
    }

    /**
     * Edits an existing product.
     * This method updates an existing product with the provided details.
     * All product fields can be updated except the product ID.
     * Deletes old images from ImgBB before uploading new ones.
     * Updates pickup location mappings by deleting existing ones and creating new
     * ones.
     * 
     * @param productRequestModel The product data to update
     * @throws BadRequestException if validation fails
     * @throws NotFoundException   if the product is not found
     */
    @Override
    @Transactional
    public void editProduct(ProductRequestModel productRequestModel) {
        // Validate request
        if (productRequestModel.getProductId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidId);
        }

        // Find existing product
        Product existingProduct = productRepository.findByIdWithRelatedEntities(productRequestModel.getProductId(),
                getClientId());
        if (existingProduct == null) {
            throw new NotFoundException(
                    String.format(ErrorMessages.ProductErrorMessages.ER013, productRequestModel.getProductId()));
        }

        // Create updated product using constructor (validations including pickup
        // location quantities are handled in the constructor)
        Product updatedProduct = new Product(productRequestModel, getUser(), existingProduct);

        // Save the updated product
        productRepository.save(updatedProduct);

        // Update pickup location mappings (delete existing and create new)
        productPickupLocationMappingRepository.deleteByProductId(updatedProduct.getProductId());
        createPickupLocationMappings(updatedProduct.getProductId(), productRequestModel.getPickupLocationQuantities(),
                getUser());

        // Process and upload images to ImgBB using the updated product ID (delete old
        // images first)
        processAndUploadProductImages(productRequestModel, updatedProduct, true);

        // Log the operation
        userLogService.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.UpdateProduct + " " + updatedProduct.getProductId(),
                ApiRoutes.ProductsSubRoute.EDIT_PRODUCT);
    }

    /**
     * Toggles the deleted status of a product (soft delete/restore).
     * This method toggles the deleted flag of a product without permanently
     * removing it from the database. Deleted products are hidden from standard
     * queries.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException   if the product is not found
     */
    @Override
    @Transactional
    public void toggleDeleteProduct(long id) {
        // Find the product
        Product product = productRepository.findByIdWithRelatedEntities(id, getClientId());
        if (product == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }

        // Toggle the deleted status
        product.setIsDeleted(!product.getIsDeleted());
        product.setModifiedUser(getUser());

        // Save the updated product
        productRepository.save(product);

        // Log the operation
        userLogService.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.ToggleProduct + " " + product.getProductId(),
                ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT);
    }

    /**
     * Toggles the return eligibility status of a product.
     * This method toggles whether a product can be returned by customers.
     * This affects the return policy displayed to customers during checkout.
     * If returnWindowDays is 0 (no returns), it sets it to 30 days (default).
     * If returnWindowDays is > 0 (returns allowed), it sets it to 0 (no returns).
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException   if the product is not found
     */
    @Override
    @Transactional
    public void toggleReturnProduct(long id) {
        // Find the product
        Product product = productRepository.findByIdWithRelatedEntities(id, getClientId());
        if (product == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }

        // Toggle the return window days: 0 = no returns, >0 = returns allowed
        // Default return window is 30 days when enabling returns
        if (product.getReturnWindowDays() == null || product.getReturnWindowDays() == 0) {
            product.setReturnWindowDays(30); // Enable returns with 30-day window
        } else {
            product.setReturnWindowDays(0); // Disable returns
        }
        product.setModifiedUser(getUser());

        // Save the updated product
        productRepository.save(product);

        // Log the operation
        userLogService.logData(getUserId(),
                SuccessMessages.ProductsSuccessMessages.ToggleReturnProduct + " " + product.getProductId(),
                ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT);
    }

    /**
     * Retrieves detailed information about a specific product by ID.
     * This method returns comprehensive product details including title,
     * description, pricing, images, category, and availability information.
     * 
     * @param id The ID of the product to retrieve
     * @return The product details
     * @throws BadRequestException if validation fails
     * @throws NotFoundException   if the product is not found
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseModel getProductDetailsById(long id) {
        // Find the product with all related entities
        Product product = productRepository.findByIdWithRelatedEntities(id, getClientId());
        if (product == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }

        // Convert to response model
        ProductResponseModel response = new ProductResponseModel(product);

        // Build and set the full category path
        if (product.getCategoryId() != null) {
            String categoryFullPath = buildCategoryFullPath(product.getCategoryId());
            response.setCategoryFullPath(categoryFullPath);
        }

        return response;
    }

    /**
     * Retrieves products in batches with pagination support.
     * This method returns a paginated list of products based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return Paginated response containing product data
     * @throws BadRequestException if validation fails
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationBaseResponseModel<ProductResponseModel> getProductInBatches(
            PaginationBaseRequestModel paginationBaseRequestModel) {
        // Valid columns for filtering
        // Note: pickupLocationId filters through ProductPickupLocationMapping join
        Set<String> validColumns = new HashSet<>(Arrays.asList(
                "productId", "title", "descriptionHtml", "brand", "color", "colorLabel",
                "condition", "countryOfManufacture", "model", "upc", "modificationHtml",
                "price", "discount", "isDiscountPercent", "returnsAllowed", "length",
                "breadth", "height", "weightKgs", "categoryId",
                "isDeleted", "itemModified", "createdUser", "modifiedUser", "createdAt",
                "updatedAt", "notes", "pickupLocationId"));

        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException(String
                            .format(ErrorMessages.ProductErrorMessages.InvalidColumnNameFormat, filter.getColumn()));
                }

                // Validate operator using centralized validation from FilterCondition
                if (filter.getOperator() != null && !filter.isValidOperator()) {
                    throw new BadRequestException(String
                            .format(ErrorMessages.ProductErrorMessages.InvalidOperatorFormat, filter.getOperator()));
                }

                // Validate column type matches operator using centralized validation
                String columnType = productFilterQueryBuilder.getColumnType(filter.getColumn());
                if (columnType != null && filter.getOperator() != null && !filter.isValidOperatorForType(columnType)) {
                    throw new BadRequestException(
                            String.format(ErrorMessages.ProductErrorMessages.InvalidOperatorForColumnFormat,
                                    filter.getOperator(), columnType, filter.getColumn()));
                }
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("productId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Use filter query builder for dynamic filtering
        Page<Product> productPage = productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                getClientId(),
                paginationBaseRequestModel.getSelectedIds(),
                paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator()
                        : "AND",
                paginationBaseRequestModel.getFilters(),
                paginationBaseRequestModel.isIncludeDeleted(),
                pageable);

        // Convert to response models
        PaginationBaseResponseModel<ProductResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(productPage.getContent().stream()
                .map(ProductResponseModel::new)
                .collect(Collectors.toList()));

        // Set pagination metadata
        response.setTotalDataCount(productPage.getTotalElements());

        return response;
    }

    /**
     * Creates multiple products asynchronously in the system with partial success
     * support.
     * 
     * This method processes products in a background thread with the following
     * characteristics:
     * - Supports partial success: if some products fail validation, others still
     * succeed
     * - Sends detailed results to user via message notification after processing
     * completes
     * - NOT_SUPPORTED: Runs without a transaction to avoid rollback-only issues
     * when individual product creations fail
     * 
     * @param products                List of ProductRequestModel containing the
     *                                product data to insert
     * @param requestingUserId        The ID of the user making the request
     *                                (captured from security context)
     * @param requestingUserLoginName The loginName of the user making the request
     *                                (captured from security context)
     * @param requestingClientId      The client ID of the user making the request
     *                                (captured from security context)
     */

    @Override
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void bulkAddProductsAsync(java.util.List<ProductRequestModel> products, Long requestingUserId,
            String requestingUserLoginName, Long requestingClientId) {
        try {
            // Validate input
            if (products == null || products.isEmpty()) {
                throw new BadRequestException(
                        String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Product"));
            }

            com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
            response.setTotalRequested(products.size());

            int successCount = 0;
            int failureCount = 0;

            // Process each product individually
            for (ProductRequestModel productRequest : products) {
                try {
                    // Call persistProduct with explicit createdUser and shouldLog = false (bulk
                    // logs collectively)
                    Product createdProduct = persistProduct(productRequest, requestingUserLoginName, requestingClientId,
                            requestingUserId, false);
                    response.addSuccess(productRequest.getTitle(), createdProduct.getProductId());
                    successCount++;

                } catch (BadRequestException bre) {
                    // Validation or business logic error
                    response.addFailure(
                            productRequest.getTitle() != null ? productRequest.getTitle() : "unknown",
                            bre.getMessage());
                    failureCount++;
                } catch (Exception e) {
                    // Unexpected error
                    response.addFailure(
                            productRequest.getTitle() != null ? productRequest.getTitle() : "unknown",
                            "Error: " + e.getMessage());
                    failureCount++;
                }
            }

            // Log bulk product creation (using captured context values)
            userLogService.logDataWithContext(
                    requestingUserId,
                    requestingUserLoginName,
                    requestingClientId,
                    SuccessMessages.ProductsSuccessMessages.InsertProduct + " (Bulk: " + successCount + " succeeded, "
                            + failureCount + " failed)",
                    ApiRoutes.ProductsSubRoute.BULK_ADD_PRODUCT);

            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);

            // Create a message with the bulk insert results using the helper (using
            // captured context)
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                    response, "Product", "Products", "Title", "Product ID",
                    messageService, requestingUserId, requestingUserLoginName, requestingClientId);

        } catch (Exception e) {
            // Still send a message to user about the failure (using captured userId)
            com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> errorResponse = new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
            errorResponse.setTotalRequested(products != null ? products.size() : 0);
            errorResponse.setSuccessCount(0);
            errorResponse.setFailureCount(products != null ? products.size() : 0);
            errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                    errorResponse, "Product", "Products", "Title", "Product ID",
                    messageService, requestingUserId, requestingUserLoginName, requestingClientId);
        }
    }

    /**
     * Creates multiple products synchronously in a single operation (for testing).
     * This is a synchronous wrapper that processes products immediately and returns
     * results.
     * 
     * @param products List of ProductRequestModel containing the product data to
     *                 insert
     * @return BulkInsertResponseModel containing success/failure details for each
     *         product
     */
    @Override
    @Transactional
    public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkAddProducts(
            java.util.List<ProductRequestModel> products) {
        // Validate input
        if (products == null || products.isEmpty()) {
            throw new BadRequestException(
                    String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Product"));
        }

        com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
        response.setTotalRequested(products.size());

        int successCount = 0;
        int failureCount = 0;

        // Process each product individually
        for (ProductRequestModel productRequest : products) {
            try {
                Product createdProduct = persistProduct(productRequest, getUser(), getClientId());
                response.addSuccess(productRequest.getTitle(), createdProduct.getProductId());
                successCount++;
            } catch (BadRequestException bre) {
                response.addFailure(
                        productRequest.getTitle() != null ? productRequest.getTitle() : "unknown",
                        bre.getMessage());
                failureCount++;
            } catch (Exception e) {
                response.addFailure(
                        productRequest.getTitle() != null ? productRequest.getTitle() : "unknown",
                        "Error: " + e.getMessage());
                failureCount++;
            }
        }

        // Log bulk product creation
        userLogService.logData(
                getUserId(),
                SuccessMessages.ProductsSuccessMessages.InsertProduct + " (Bulk: " + successCount + " succeeded, "
                        + failureCount + " failed)",
                ApiRoutes.ProductsSubRoute.BULK_ADD_PRODUCT);

        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);

        return response;
    }

    /**
     * Retrieves categories based on parent ID for hierarchical navigation.
     * 
     * If parentId is null: Returns all root categories (categories with null
     * parentId)
     * If parentId is provided: Returns all child categories of that parent (where
     * isEnd=true)
     * 
     * This enables drill-down category navigation where users can browse the
     * hierarchy
     * level by level until they reach assignable leaf categories.
     * 
     * @param parentId The parent category ID (null for root categories)
     * @return List of ProductCategoryWithPathResponseModel containing categories
     *         with full paths
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryWithPathResponseModel> findCategoriesByParentId(Long parentId) {
        List<ProductCategory> categories;

        if (parentId == null) {
            // Fetch all root categories (categories with null parentId)
            categories = productCategoryRepository.findAll().stream()
                    .filter(cat -> cat.getParentId() == null)
                    .collect(Collectors.toList());
        } else {
            // Fetch ALL child categories with the specified parentId (both leaf and
            // non-leaf)
            // This allows drill-down navigation through the entire hierarchy
            categories = productCategoryRepository.findAll().stream()
                    .filter(cat -> cat.getParentId() != null &&
                            cat.getParentId().equals(parentId))
                    .collect(Collectors.toList());
        }

        // Build response models with full paths and isEnd flag
        List<ProductCategoryWithPathResponseModel> result = new ArrayList<>();
        for (ProductCategory category : categories) {
            String fullPath = buildFullPath(category);
            result.add(new ProductCategoryWithPathResponseModel(
                    category.getCategoryId(),
                    category.getName(),
                    fullPath,
                    category.getParentId(),
                    category.getIsEnd()));
        }

        return result;
    }

    /**
     * Retrieves full category paths for a list of category IDs.
     * 
     * This method takes a list of category IDs and returns a mapping of each ID
     * to its full hierarchical path (e.g., "Electronics › Computers › Laptops").
     * Uses the existing buildCategoryFullPath helper method for consistency.
     * 
     * @param categoryIds List of category IDs to get paths for
     * @return Map of category ID to full path string
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> getCategoryPathsByIds(List<Long> categoryIds) {
        Map<Long, String> result = new java.util.HashMap<>();

        if (categoryIds == null || categoryIds.isEmpty()) {
            return result;
        }

        // Remove duplicates and nulls
        Set<Long> uniqueIds = new HashSet<>(categoryIds);
        uniqueIds.remove(null);

        // Build path for each category ID
        for (Long categoryId : uniqueIds) {
            try {
                String fullPath = buildCategoryFullPath(categoryId);
                if (fullPath != null && !fullPath.isEmpty()) {
                    result.put(categoryId, fullPath);
                }
            } catch (Exception e) {
                // Skip categories that can't be found
            }
        }

        return result;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Processes and uploads product images to ImgBB storage.
     * This method handles multiple product images including required images
     * (main, top, bottom, front, back, right, left, details) and
     * optional images (defect, additional_1, additional_2, additional_3).
     * It converts URLs to base64 format and uploads them to ImgBB with
     * structured filenames using the saved product ID.
     * 
     * @param productRequestModel The product request model containing image data
     * @param savedProduct        The saved product entity with generated ID
     * @param isUpdate            Whether this is an update operation (to delete old
     *                            images)
     * @throws BadRequestException if required images are missing or upload fails
     */
    private void processAndUploadProductImages(ProductRequestModel productRequestModel, Product savedProduct,
            boolean isUpdate) {
        // Get client and validate ImgBB API key
        Long clientId = getClientId();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));

        if (client.getImgbbApiKey() == null || client.getImgbbApiKey().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ConfigurationErrorMessages.ImgbbApiKeyNotConfigured);
        }

        ClientResponseModel clientDetails = clientService.getClientById(clientId);
        ImgbbHelper imgbbHelper = new ImgbbHelper(client.getImgbbApiKey());
        String environmentName = environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0]
                : "default";

        // Define image types and their corresponding request fields
        // Required images (excluding defect which is optional)
        String[] requiredImageTypes = {
                ProductImageConstants.MAIN, ProductImageConstants.TOP, ProductImageConstants.BOTTOM,
                ProductImageConstants.FRONT, ProductImageConstants.BACK, ProductImageConstants.RIGHT,
                ProductImageConstants.LEFT, ProductImageConstants.DETAILS
        };
        String[] requiredImageData = {
                productRequestModel.getMainImage(),
                productRequestModel.getTopImage(),
                productRequestModel.getBottomImage(),
                productRequestModel.getFrontImage(),
                productRequestModel.getBackImage(),
                productRequestModel.getRightImage(),
                productRequestModel.getLeftImage(),
                productRequestModel.getDetailsImage()
        };

        // Optional images
        String[] optionalImageTypes = {
                ProductImageConstants.DEFECT, ProductImageConstants.ADDITIONAL_1,
                ProductImageConstants.ADDITIONAL_2, ProductImageConstants.ADDITIONAL_3
        };
        String[] optionalImageData = {
                productRequestModel.getDefectImage(),
                productRequestModel.getAdditionalImage1(),
                productRequestModel.getAdditionalImage2(),
                productRequestModel.getAdditionalImage3()
        };

        // Required images are already validated in persistProduct/editProduct before
        // calling this

        // Process required images - check if they changed before uploading
        for (int i = 0; i < requiredImageData.length; i++) {
            String incomingImageData = requiredImageData[i];

            // Check if this is an existing URL that hasn't changed
            boolean imageUnchanged = false;
            if (isUpdate && incomingImageData != null && incomingImageData.startsWith("http")) {
                String existingUrl = getExistingImageUrl(savedProduct, requiredImageTypes[i]);
                if (incomingImageData.equals(existingUrl)) {
                    imageUnchanged = true;
                }
            }

            if (!imageUnchanged) {
                // Delete old image from ImgBB if updating
                if (isUpdate) {
                    String deleteHash = getExistingImageDeleteHash(savedProduct, requiredImageTypes[i]);
                    if (deleteHash != null && !deleteHash.isEmpty()) {
                        imgbbHelper.deleteImage(deleteHash);
                    }
                }

                String base64Image = convertToBase64(incomingImageData);
                String customFileName = ImgbbHelper.generateCustomFileNameForProductImage(
                        environmentName,
                        clientDetails.getName(),
                        savedProduct.getProductId(),
                        requiredImageTypes[i]);

                ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                        base64Image,
                        customFileName);

                if (uploadResponse == null || uploadResponse.getUrl() == null) {
                    throw new BadRequestException(
                            String.format(ErrorMessages.ProductErrorMessages.ER010, requiredImageTypes[i]));
                }

                // Save URL and deleteHash to product entity
                setProductImageUrlAndHash(savedProduct, requiredImageTypes[i], uploadResponse.getUrl(),
                        uploadResponse.getDeleteHash());
            }
        }

        // Process optional images
        for (int i = 0; i < optionalImageData.length; i++) {
            if (optionalImageData[i] != null && !optionalImageData[i].trim().isEmpty()) {
                String incomingImageData = optionalImageData[i];

                // Check if this is an existing URL that hasn't changed
                boolean imageUnchanged = false;
                if (isUpdate && incomingImageData.startsWith("http")) {
                    String existingUrl = getExistingImageUrl(savedProduct, optionalImageTypes[i]);
                    if (incomingImageData.equals(existingUrl)) {
                        imageUnchanged = true;
                    }
                }

                if (!imageUnchanged) {
                    // Delete old image from ImgBB if updating
                    if (isUpdate) {
                        String deleteHash = getExistingImageDeleteHash(savedProduct, optionalImageTypes[i]);
                        if (deleteHash != null && !deleteHash.isEmpty()) {
                            imgbbHelper.deleteImage(deleteHash);
                        }
                    }

                    String base64Image = convertToBase64(incomingImageData);
                    String customFileName = ImgbbHelper.generateCustomFileNameForProductImage(
                            environmentName,
                            clientDetails.getName(),
                            savedProduct.getProductId(),
                            optionalImageTypes[i]);

                    ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                            base64Image,
                            customFileName);

                    if (uploadResponse == null || uploadResponse.getUrl() == null) {
                        String errorMsg = ProductImageConstants.DEFECT.equals(optionalImageTypes[i])
                                ? String.format(ErrorMessages.ProductErrorMessages.ER010, optionalImageTypes[i])
                                : String.format(ErrorMessages.ProductErrorMessages.ER011, i); // For additional images
                        throw new BadRequestException(errorMsg);
                    }

                    // Save URL and deleteHash to product entity
                    setProductImageUrlAndHash(savedProduct, optionalImageTypes[i], uploadResponse.getUrl(),
                            uploadResponse.getDeleteHash());
                }
            } else {
                // Clear optional image fields if not provided
                setProductImageUrlAndHash(savedProduct, optionalImageTypes[i], null, null);
            }
        }

        // Save the updated product with image URLs and deleteHashes
        productRepository.save(savedProduct);
    }

    /**
     * Gets the existing image URL for a specific image type from the product
     * entity.
     * 
     * @param product   The product entity
     * @param imageType The type of image (main, top, bottom, etc.)
     * @return The existing image URL, or null if not set
     */
    private String getExistingImageUrl(Product product, String imageType) {
        switch (imageType) {
            case ProductImageConstants.MAIN:
                return product.getMainImageUrl();
            case ProductImageConstants.TOP:
                return product.getTopImageUrl();
            case ProductImageConstants.BOTTOM:
                return product.getBottomImageUrl();
            case ProductImageConstants.FRONT:
                return product.getFrontImageUrl();
            case ProductImageConstants.BACK:
                return product.getBackImageUrl();
            case ProductImageConstants.RIGHT:
                return product.getRightImageUrl();
            case ProductImageConstants.LEFT:
                return product.getLeftImageUrl();
            case ProductImageConstants.DETAILS:
                return product.getDetailsImageUrl();
            case ProductImageConstants.DEFECT:
                return product.getDefectImageUrl();
            case ProductImageConstants.ADDITIONAL_1:
                return product.getAdditionalImage1Url();
            case ProductImageConstants.ADDITIONAL_2:
                return product.getAdditionalImage2Url();
            case ProductImageConstants.ADDITIONAL_3:
                return product.getAdditionalImage3Url();
            default:
                return null;
        }
    }

    /**
     * Gets the existing image delete hash for a specific image type from the
     * product entity.
     * 
     * @param product   The product entity
     * @param imageType The type of image (main, top, bottom, etc.)
     * @return The existing delete hash, or null if not set
     */
    private String getExistingImageDeleteHash(Product product, String imageType) {
        switch (imageType) {
            case ProductImageConstants.MAIN:
                return product.getMainImageDeleteHash();
            case ProductImageConstants.TOP:
                return product.getTopImageDeleteHash();
            case ProductImageConstants.BOTTOM:
                return product.getBottomImageDeleteHash();
            case ProductImageConstants.FRONT:
                return product.getFrontImageDeleteHash();
            case ProductImageConstants.BACK:
                return product.getBackImageDeleteHash();
            case ProductImageConstants.RIGHT:
                return product.getRightImageDeleteHash();
            case ProductImageConstants.LEFT:
                return product.getLeftImageDeleteHash();
            case ProductImageConstants.DETAILS:
                return product.getDetailsImageDeleteHash();
            case ProductImageConstants.DEFECT:
                return product.getDefectImageDeleteHash();
            case ProductImageConstants.ADDITIONAL_1:
                return product.getAdditionalImage1DeleteHash();
            case ProductImageConstants.ADDITIONAL_2:
                return product.getAdditionalImage2DeleteHash();
            case ProductImageConstants.ADDITIONAL_3:
                return product.getAdditionalImage3DeleteHash();
            default:
                return null;
        }
    }

    /**
     * Sets the image URL and deleteHash for a specific image type on the product
     * entity.
     * 
     * @param product    The product entity to update
     * @param imageType  The type of image (main, top, bottom, etc.)
     * @param url        The ImgBB URL
     * @param deleteHash The ImgBB delete hash
     */
    private void setProductImageUrlAndHash(Product product, String imageType, String url, String deleteHash) {
        switch (imageType) {
            case ProductImageConstants.MAIN:
                product.setMainImageUrl(url);
                product.setMainImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.TOP:
                product.setTopImageUrl(url);
                product.setTopImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.BOTTOM:
                product.setBottomImageUrl(url);
                product.setBottomImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.FRONT:
                product.setFrontImageUrl(url);
                product.setFrontImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.BACK:
                product.setBackImageUrl(url);
                product.setBackImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.RIGHT:
                product.setRightImageUrl(url);
                product.setRightImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.LEFT:
                product.setLeftImageUrl(url);
                product.setLeftImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.DETAILS:
                product.setDetailsImageUrl(url);
                product.setDetailsImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.DEFECT:
                product.setDefectImageUrl(url);
                product.setDefectImageDeleteHash(deleteHash);
                break;
            case ProductImageConstants.ADDITIONAL_1:
                product.setAdditionalImage1Url(url);
                product.setAdditionalImage1DeleteHash(deleteHash);
                break;
            case ProductImageConstants.ADDITIONAL_2:
                product.setAdditionalImage2Url(url);
                product.setAdditionalImage2DeleteHash(deleteHash);
                break;
            case ProductImageConstants.ADDITIONAL_3:
                product.setAdditionalImage3Url(url);
                product.setAdditionalImage3DeleteHash(deleteHash);
                break;
        }
    }

    /**
     * Converts a URL or base64 string to base64 format.
     * Strips the data:image/png;base64, prefix if present.
     * If the input is a URL, fetches the image and converts it to base64.
     * 
     * @param imageData The image data (URL or base64 string)
     * @return Base64 encoded image string (without data:image prefix)
     * @throws BadRequestException if conversion fails
     */
    private String convertToBase64(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            return null;
        }

        // Check if it's already base64 (starts with data:image or just base64 content)
        if (imageData.startsWith("data:image")) {
            // Strip the data:image/...;base64, prefix
            int commaIndex = imageData.indexOf(',');
            if (commaIndex != -1 && commaIndex < imageData.length() - 1) {
                String base64Data = imageData.substring(commaIndex + 1);
                return base64Data;
            }
            return imageData; // If no comma found, return as-is
        }

        if (!imageData.startsWith("http")) {
            // Already a base64 string without prefix
            return imageData;
        }

        // It's a URL, fetch and convert to base64
        try {
            URL url = URI.create(imageData).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException(String.format(ErrorMessages.ProductErrorMessages.HttpErrorWhenFetchingImageFormat,
                        responseCode));
            }

            try (InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                byte[] imageBytes = outputStream.toByteArray();
                String base64String = Base64.getEncoder().encodeToString(imageBytes);

                // Return just the base64 string (ImgBB doesn't need the data:image prefix)
                return base64String;
            }
        } catch (IOException e) {
            throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER012, imageData));
        }
    }

    /**
     * Creates pickup location mappings for a product.
     * Uses batch insert for optimized database performance.
     * 
     * @param productId                The product ID
     * @param pickupLocationQuantities Map of pickup location ID to available
     *                                 quantity
     * @param createdUser              The loginName of the user creating these
     *                                 mappings
     * @throws BadRequestException if validation fails
     */
    private void createPickupLocationMappings(Long productId, Map<Long, Integer> pickupLocationQuantities,
            String createdUser) {
        // Create all mappings using the static factory method (includes validation)
        List<ProductPickupLocationMapping> mappings = ProductPickupLocationMapping.createFromMap(
                productId,
                pickupLocationQuantities,
                createdUser);

        // Save all mappings in a single batch operation for better performance
        productPickupLocationMappingRepository.saveAll(mappings);
    }

    /**
     * Persists a product to the database.
     * This helper method is used by both single and bulk product creation.
     * 
     * @param productRequestModel The product to create
     * @param createdUser         The loginName of the user creating this product
     * @param clientId            The client ID from security context (passed
     *                            explicitly for async compatibility)
     * @return The saved product entity
     * @throws BadRequestException if validation fails
     */
    @Transactional
    protected Product persistProduct(ProductRequestModel productRequestModel, String createdUser, Long clientId) {
        // Default behavior: log the operation using security context
        return persistProduct(productRequestModel, createdUser, clientId, null, true);
    }

    /**
     * Helper method to persist a single product with explicit logging control.
     * 
     * @param productRequestModel The product data to persist
     * @param createdUser         The user creating the product (passed explicitly
     *                            for context)
     * @param clientId            The client ID from security context (passed
     *                            explicitly for context)
     * @param userId              The user ID for logging (null to use security
     *                            context)
     * @param shouldLog           Whether to log this individual operation (false
     *                            for bulk operations)
     * @return The saved product entity
     * @throws BadRequestException if validation fails
     */
    @Transactional
    protected Product persistProduct(ProductRequestModel productRequestModel, String createdUser, Long clientId,
            Long userId, boolean shouldLog) {
        // Validate category exists
        if (productRequestModel.getCategoryId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidCategoryId);
        }

        Optional<com.example.SpringApi.Models.DatabaseModels.ProductCategory> categoryOpt = productCategoryRepository
                .findById(productRequestModel.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new NotFoundException(
                    String.format(ErrorMessages.ProductErrorMessages.ER008, productRequestModel.getCategoryId()));
        }

        // Validate clientId first
        if (clientId == null || clientId == 0) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidId);
        }

        // Validate required images are present before any DB operations
        validateRequiredImagesPresent(productRequestModel);

        // Create new product using constructor (validations included)
        Product product = new Product(productRequestModel, createdUser, clientId);

        // Save the product first to get the ID
        Product savedProduct = productRepository.save(product);
        productRequestModel.setProductId(savedProduct.getProductId());

        // Create pickup location mappings
        createPickupLocationMappings(savedProduct.getProductId(), productRequestModel.getPickupLocationQuantities(),
                createdUser);

        // Process and upload images to ImgBB using the saved product ID
        processAndUploadProductImages(productRequestModel, savedProduct, false);

        // Log the operation only if shouldLog is true (skip for bulk as they log
        // collectively)
        if (shouldLog) {
            // Use provided userId or fall back to security context
            Long logUserId = userId != null ? userId : getUserId();
            userLogService.logDataWithContext(logUserId, createdUser, clientId,
                    SuccessMessages.ProductsSuccessMessages.InsertProduct + " " + savedProduct.getProductId(),
                    ApiRoutes.ProductsSubRoute.ADD_PRODUCT);
        }
        return savedProduct;
    }

    /**
     * Builds the full hierarchical path for a category by traversing parent
     * categories.
     * Uses " > " as the separator between category names.
     * 
     * @param categoryId The ID of the category to build the path for
     * @return The full path string (e.g., "Electronics > Computers > Laptops")
     */
    private String buildCategoryFullPath(Long categoryId) {
        List<String> pathParts = new ArrayList<>();
        Long currentId = categoryId;

        // Traverse up the hierarchy collecting names (with a safety limit to prevent
        // infinite loops)
        int maxDepth = 20;
        int depth = 0;

        while (currentId != null && depth < maxDepth) {
            Optional<ProductCategory> categoryOpt = productCategoryRepository.findById(currentId);
            if (categoryOpt.isPresent()) {
                ProductCategory category = categoryOpt.get();
                pathParts.add(category.getName());
                currentId = category.getParentId();
            } else {
                break;
            }
            depth++;
        }

        // Reverse to get root-to-leaf order
        Collections.reverse(pathParts);

        return String.join(" > ", pathParts);
    }

    /**
     * Builds the full hierarchical path for a category by traversing up the parent
     * chain.
     * 
     * This method recursively builds the path from root to leaf using the separator
     * " › ".
     * Example: "Electronics › Computers › Laptops"
     * 
     * @param category The category to build the path for
     * @return The full hierarchical path as a string
     */
    private String buildFullPath(ProductCategory category) {
        List<String> pathParts = new ArrayList<>();
        ProductCategory current = category;

        // Traverse up the parent chain
        while (current != null) {
            pathParts.add(0, current.getName()); // Add to beginning of list

            // Get parent if parentId exists
            if (current.getParentId() != null) {
                current = productCategoryRepository.findById(current.getParentId()).orElse(null);
            } else {
                break;
            }
        }

        // Join path parts with separator
        return String.join(" › ", pathParts);
    }

    /**
     * Gets product stock information across all pickup locations for a specific
     * product.
     * Returns stock availability with pickup location address details for distance
     * calculation.
     * Also includes package availability information for each location.
     * 
     * @param productId The product ID
     * @return List of ProductStockByLocationResponseModel with stock, location, and
     *         package details
     */
    @Transactional(readOnly = true)
    public java.util.List<com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel> getProductStockAtLocationsByProductId(
            Long productId,
            Integer requestedQuantity,
            String deliveryPostcode,
            Boolean isCod) {
        // Fetch product to get its dimensions
        Product product = productRepository.findById(productId).orElse(null);

        // Fetch product pickup location mappings with error handling for missing table
        java.util.List<ProductPickupLocationMapping> mappings;
        try {
            mappings = productPickupLocationMappingRepository
                    .findByProductIdWithPickupLocationAndAddress(productId);

            // Log when no mappings are found (helps debug stock availability issues)
            if (mappings.isEmpty()) {
                logger.info("No active ProductPickupLocationMapping found for productId: {}. " +
                        "This could mean: 1) No stock mappings exist, 2) All mappings are inactive (isActive=false), " +
                        "or 3) All pickup locations are deleted (isDeleted=true).", productId);
            } else {
                logger.debug("Found {} ProductPickupLocationMapping(s) for productId: {}", mappings.size(), productId);
            }
        } catch (InvalidDataAccessResourceUsageException e) {
            // Table doesn't exist - log warning and return empty list
            // Check if it's specifically a missing table error
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                logger.warn("ProductPickupLocationMapping table does not exist for productId: {}. " +
                        "Please run the database migration script. Returning empty stock list.", productId);
            } else {
                logger.warn("Database resource error for productId: {}. Returning empty stock list.", productId, e);
            }
            mappings = new java.util.ArrayList<>();
        } catch (SQLGrammarException e) {
            // Catch Hibernate SQL grammar exceptions (like missing table)
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                logger.warn("ProductPickupLocationMapping table does not exist for productId: {}. " +
                        "Please run the database migration script. Returning empty stock list.", productId);
            } else {
                logger.warn("SQL grammar error for productId: {}. Returning empty stock list.", productId, e);
            }
            mappings = new java.util.ArrayList<>();
        } catch (Exception e) {
            // Catch any other exceptions and log them
            logger.error("Error fetching ProductPickupLocationMapping for productId: {}. Returning empty stock list.",
                    productId, e);
            mappings = new java.util.ArrayList<>();
        }

        // Get all pickup location IDs
        java.util.List<Long> pickupLocationIds = mappings.stream()
                .map(ProductPickupLocationMapping::getPickupLocationId)
                .collect(java.util.stream.Collectors.toList());

        // Fetch package info for all locations in one query
        java.util.Map<Long, java.util.List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping>> packagesByLocation = new java.util.HashMap<>();

        if (!pickupLocationIds.isEmpty()) {
            java.util.List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> packageMappings = packagePickupLocationMappingRepository
                    .findByPickupLocationIdsWithPackages(pickupLocationIds);

            for (com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping pm : packageMappings) {
                packagesByLocation
                        .computeIfAbsent(pm.getPickupLocationId(), k -> new java.util.ArrayList<>())
                        .add(pm);
            }
        }

        // Create PackagingHelper for calculations
        com.example.SpringApi.Helpers.PackagingHelper packagingHelper = new com.example.SpringApi.Helpers.PackagingHelper();

        // Note: Shipping options are now calculated at the order level via
        // ShippingController
        // This allows combining products from the same pickup location for more
        // accurate shipping costs

        // Build response list
        java.util.List<com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel> responseList = new java.util.ArrayList<>();

        for (com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping mapping : mappings) {
            com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel response = new com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel(
                    mapping);

            // Set product dimensions
            setProductDimensions(response, product);

            // Get packages for this location
            java.util.List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> locationPackages = packagesByLocation
                    .getOrDefault(mapping.getPickupLocationId(), java.util.Collections.emptyList());

            // Add package info and get dimensions for packaging calculation
            java.util.List<com.example.SpringApi.Helpers.PackagingHelper.PackageDimension> packageDimensions = addPackageInfoToResponse(
                    response, locationPackages);

            // Calculate packaging estimate
            calculatePackagingEstimate(response, product, requestedQuantity, mapping.getAvailableStock(),
                    packageDimensions, packagingHelper);

            // Note: Shipping options are now calculated at the order level via
            // ShippingController
            // This allows combining products from the same pickup location for more
            // accurate shipping costs

            responseList.add(response);
        }

        return responseList;
    }

    private void setProductDimensions(
            com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel response,
            com.example.SpringApi.Models.DatabaseModels.Product product) {
        if (product != null) {
            response.setProductLength(product.getLength());
            response.setProductBreadth(product.getBreadth());
            response.setProductHeight(product.getHeight());
            response.setProductWeightKgs(product.getWeightKgs());
        }
    }

    private java.util.List<com.example.SpringApi.Helpers.PackagingHelper.PackageDimension> addPackageInfoToResponse(
            com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel response,
            java.util.List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> locationPackages) {

        java.util.List<com.example.SpringApi.Helpers.PackagingHelper.PackageDimension> packageDimensions = new java.util.ArrayList<>();

        for (com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping pm : locationPackages) {
            com.example.SpringApi.Models.DatabaseModels.Package pkg = pm.getPackageEntity();
            if (pkg == null)
                continue;

            // Add to response
            response.getAvailablePackages().add(
                    new com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel.PackageInfoModel(
                            pkg.getPackageId(), pkg.getPackageName(), pkg.getPackageType(),
                            pkg.getPricePerUnit(), pm.getAvailableQuantity(),
                            pkg.getLength(), pkg.getBreadth(), pkg.getHeight(), pkg.getMaxWeight()));

            // Add to packaging calculation list
            packageDimensions.add(new com.example.SpringApi.Helpers.PackagingHelper.PackageDimension(
                    pkg.getPackageId(), pkg.getPackageName(), pkg.getPackageType(),
                    pkg.getLength(), pkg.getBreadth(), pkg.getHeight(),
                    pkg.getMaxWeight(), pkg.getPricePerUnit(), pm.getAvailableQuantity()));
        }

        return packageDimensions;
    }

    private void calculatePackagingEstimate(
            com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel response,
            com.example.SpringApi.Models.DatabaseModels.Product product,
            Integer requestedQuantity,
            int availableStock,
            java.util.List<com.example.SpringApi.Helpers.PackagingHelper.PackageDimension> packageDimensions,
            com.example.SpringApi.Helpers.PackagingHelper packagingHelper) {

        if (product == null || requestedQuantity == null || requestedQuantity <= 0)
            return;

        int quantityToPackage = Math.min(requestedQuantity, availableStock);

        com.example.SpringApi.Helpers.PackagingHelper.ProductDimension productDim = new com.example.SpringApi.Helpers.PackagingHelper.ProductDimension(
                product.getLength(), product.getBreadth(), product.getHeight(),
                product.getWeightKgs(), quantityToPackage);

        com.example.SpringApi.Helpers.PackagingHelper.PackagingEstimateResult estimate = packagingHelper
                .calculatePackaging(productDim, packageDimensions);

        for (com.example.SpringApi.Helpers.PackagingHelper.PackageUsageResult usage : estimate.getPackagesUsed()) {
            response.getPackagingEstimate().add(
                    new com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel.PackageUsageModel(
                            usage.getPackageId(), usage.getPackageName(), usage.getPackageType(),
                            usage.getQuantityUsed(), usage.getPricePerUnit(), usage.getTotalCost()));
        }
        response.setTotalPackagingCost(estimate.getTotalPackagingCost());
        response.setMaxItemsPackable(estimate.getMaxItemsPackable());
    }

    private void validateRequiredImagesPresent(ProductRequestModel productRequestModel) {
        String[] requiredImageTypes = {
                ProductImageConstants.MAIN, ProductImageConstants.TOP, ProductImageConstants.BOTTOM,
                ProductImageConstants.FRONT, ProductImageConstants.BACK, ProductImageConstants.RIGHT,
                ProductImageConstants.LEFT, ProductImageConstants.DETAILS
        };
        String[] requiredImageData = {
                productRequestModel.getMainImage(), productRequestModel.getTopImage(),
                productRequestModel.getBottomImage(), productRequestModel.getFrontImage(),
                productRequestModel.getBackImage(), productRequestModel.getRightImage(),
                productRequestModel.getLeftImage(), productRequestModel.getDetailsImage()
        };

        for (int i = 0; i < requiredImageData.length; i++) {
            if (requiredImageData[i] == null || requiredImageData[i].trim().isEmpty()) {
                throw new BadRequestException(
                        String.format(ErrorMessages.ProductErrorMessages.ER009, requiredImageTypes[i]));
            }
        }
    }
}
