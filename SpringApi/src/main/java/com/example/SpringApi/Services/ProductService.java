package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Services.Interface.IProductSubTranslator;
import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Repositories.ProductRepository;
import com.example.SpringApi.Repositories.ProductCategoryRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
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
    
    private final ProductRepository productRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final UserLogService userLogService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final Environment environment;
    private final ProductFilterQueryBuilder productFilterQueryBuilder;
    private final MessageService messageService;
    
    @Value("${imageLocation:firebase}")
    private String imageLocation;
    
    @Autowired
    public ProductService(ProductRepository productRepository,
                         ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
                         UserLogService userLogService,
                         ProductCategoryRepository productCategoryRepository,
                         ClientRepository clientRepository,
                         ClientService clientService,
                         ProductFilterQueryBuilder productFilterQueryBuilder,
                         MessageService messageService,
                         Environment environment,
                         HttpServletRequest request) {
        super();
        this.productRepository = productRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.userLogService = userLogService;
        this.productCategoryRepository = productCategoryRepository;
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.productFilterQueryBuilder = productFilterQueryBuilder;
        this.messageService = messageService;
        this.environment = environment;
    }

    /**
     * Processes and uploads product images to ImgBB storage.
     * This method handles multiple product images including required images
     * (main, top, bottom, front, back, right, left, details) and
     * optional images (defect, additional_1, additional_2, additional_3).
     * It converts URLs to base64 format and uploads them to ImgBB with
     * structured filenames using the saved product ID.
     * 
     * @param productRequestModel The product request model containing image data
     * @param savedProduct The saved product entity with generated ID
     * @param isUpdate Whether this is an update operation (to delete old images)
     * @throws BadRequestException if required images are missing or upload fails
     */
    private void processAndUploadProductImages(ProductRequestModel productRequestModel, Product savedProduct, boolean isUpdate) {
        // Get client and validate ImgBB API key
        Long clientId = getClientId();
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId));
            
        if (client.getImgbbApiKey() == null || client.getImgbbApiKey().trim().isEmpty()) {
            throw new BadRequestException("ImgBB API key is not configured for this client");
        }
        
        ClientResponseModel clientDetails = clientService.getClientById(clientId);
        ImgbbHelper imgbbHelper = new ImgbbHelper(client.getImgbbApiKey());
        String environmentName = environment.getActiveProfiles().length > 0 
            ? environment.getActiveProfiles()[0] 
            : "default";
        
        // Define image types and their corresponding request fields
        // Required images (excluding defect which is optional)
        String[] requiredImageTypes = {"main", "top", "bottom", "front", "back", "right", "left", "details"};
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
        String[] optionalImageTypes = {"defect", "additional_1", "additional_2", "additional_3"};
        String[] optionalImageData = {
            productRequestModel.getDefectImage(),
            productRequestModel.getAdditionalImage1(),
            productRequestModel.getAdditionalImage2(),
            productRequestModel.getAdditionalImage3()
        };
        
        // Validate required images are present
        for (int i = 0; i < requiredImageData.length; i++) {
            if (requiredImageData[i] == null || requiredImageData[i].trim().isEmpty()) {
                throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER009, requiredImageTypes[i]));
            }
        }
        
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
                    requiredImageTypes[i]
                );
                
                ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                    base64Image,
                    customFileName
                );
                
                if (uploadResponse == null || uploadResponse.getUrl() == null) {
                    throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER010, requiredImageTypes[i]));
                }
                
                // Save URL and deleteHash to product entity
                setProductImageUrlAndHash(savedProduct, requiredImageTypes[i], uploadResponse.getUrl(), uploadResponse.getDeleteHash());
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
                        optionalImageTypes[i]
                    );
                    
                    ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                        base64Image,
                        customFileName
                    );
                    
                    if (uploadResponse == null || uploadResponse.getUrl() == null) {
                        String errorMsg = optionalImageTypes[i].equals("defect") 
                            ? String.format(ErrorMessages.ProductErrorMessages.ER010, optionalImageTypes[i])
                            : String.format(ErrorMessages.ProductErrorMessages.ER011, i); // For additional images
                        throw new BadRequestException(errorMsg);
                    }
                    
                    // Save URL and deleteHash to product entity
                    setProductImageUrlAndHash(savedProduct, optionalImageTypes[i], uploadResponse.getUrl(), uploadResponse.getDeleteHash());
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
     * Gets the existing image URL for a specific image type from the product entity.
     * 
     * @param product The product entity
     * @param imageType The type of image (main, top, bottom, etc.)
     * @return The existing image URL, or null if not set
     */
    private String getExistingImageUrl(Product product, String imageType) {
        switch (imageType) {
            case "main": return product.getMainImageUrl();
            case "top": return product.getTopImageUrl();
            case "bottom": return product.getBottomImageUrl();
            case "front": return product.getFrontImageUrl();
            case "back": return product.getBackImageUrl();
            case "right": return product.getRightImageUrl();
            case "left": return product.getLeftImageUrl();
            case "details": return product.getDetailsImageUrl();
            case "defect": return product.getDefectImageUrl();
            case "additional_1": return product.getAdditionalImage1Url();
            case "additional_2": return product.getAdditionalImage2Url();
            case "additional_3": return product.getAdditionalImage3Url();
            default: return null;
        }
    }
    
    /**
     * Gets the existing image delete hash for a specific image type from the product entity.
     * 
     * @param product The product entity
     * @param imageType The type of image (main, top, bottom, etc.)
     * @return The existing delete hash, or null if not set
     */
    private String getExistingImageDeleteHash(Product product, String imageType) {
        switch (imageType) {
            case "main": return product.getMainImageDeleteHash();
            case "top": return product.getTopImageDeleteHash();
            case "bottom": return product.getBottomImageDeleteHash();
            case "front": return product.getFrontImageDeleteHash();
            case "back": return product.getBackImageDeleteHash();
            case "right": return product.getRightImageDeleteHash();
            case "left": return product.getLeftImageDeleteHash();
            case "details": return product.getDetailsImageDeleteHash();
            case "defect": return product.getDefectImageDeleteHash();
            case "additional_1": return product.getAdditionalImage1DeleteHash();
            case "additional_2": return product.getAdditionalImage2DeleteHash();
            case "additional_3": return product.getAdditionalImage3DeleteHash();
            default: return null;
        }
    }
    
    /**
     * Sets the image URL and deleteHash for a specific image type on the product entity.
     * 
     * @param product The product entity to update
     * @param imageType The type of image (main, top, bottom, etc.)
     * @param url The ImgBB URL
     * @param deleteHash The ImgBB delete hash
     */
    private void setProductImageUrlAndHash(Product product, String imageType, String url, String deleteHash) {
        switch (imageType) {
            case "main":
                product.setMainImageUrl(url);
                product.setMainImageDeleteHash(deleteHash);
                break;
            case "top":
                product.setTopImageUrl(url);
                product.setTopImageDeleteHash(deleteHash);
                break;
            case "bottom":
                product.setBottomImageUrl(url);
                product.setBottomImageDeleteHash(deleteHash);
                break;
            case "front":
                product.setFrontImageUrl(url);
                product.setFrontImageDeleteHash(deleteHash);
                break;
            case "back":
                product.setBackImageUrl(url);
                product.setBackImageDeleteHash(deleteHash);
                break;
            case "right":
                product.setRightImageUrl(url);
                product.setRightImageDeleteHash(deleteHash);
                break;
            case "left":
                product.setLeftImageUrl(url);
                product.setLeftImageDeleteHash(deleteHash);
                break;
            case "details":
                product.setDetailsImageUrl(url);
                product.setDetailsImageDeleteHash(deleteHash);
                break;
            case "defect":
                product.setDefectImageUrl(url);
                product.setDefectImageDeleteHash(deleteHash);
                break;
            case "additional_1":
                product.setAdditionalImage1Url(url);
                product.setAdditionalImage1DeleteHash(deleteHash);
                break;
            case "additional_2":
                product.setAdditionalImage2Url(url);
                product.setAdditionalImage2DeleteHash(deleteHash);
                break;
            case "additional_3":
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
                throw new IOException("HTTP " + responseCode + " when fetching image");
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
     * @param productId The product ID
     * @param pickupLocationQuantities Map of pickup location ID to available quantity
     * @throws BadRequestException if validation fails
     */
    private void createPickupLocationMappings(Long productId, Map<Long, Integer> pickupLocationQuantities) {
        // Create all mappings using the static factory method (includes validation)
        List<ProductPickupLocationMapping> mappings = ProductPickupLocationMapping.createFromMap(
            productId, 
            pickupLocationQuantities, 
            getUser()
        );
        
        // Save all mappings in a single batch operation for better performance
        productPickupLocationMappingRepository.saveAll(mappings);
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
        persistProduct(productRequestModel);
    }

    private Product persistProduct(ProductRequestModel productRequestModel) {
        // Validate category exists
        if (productRequestModel.getCategoryId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidCategoryId);
        }
        
        Optional<com.example.SpringApi.Models.DatabaseModels.ProductCategory> categoryOpt = 
            productCategoryRepository.findById(productRequestModel.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER008, productRequestModel.getCategoryId()));
        }
        
        // Create new product using constructor (validations including pickup location quantities are handled in the constructor)
        Product product = new Product(productRequestModel, getUser());
        
        // Save the product first to get the ID
        Product savedProduct = productRepository.save(product);
        productRequestModel.setProductId(savedProduct.getProductId());
        
        // Create pickup location mappings
        createPickupLocationMappings(savedProduct.getProductId(), productRequestModel.getPickupLocationQuantities());
        
        // Process and upload images to ImgBB using the saved product ID
        processAndUploadProductImages(productRequestModel, savedProduct, false);
        
        // Log the operation
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.InsertProduct + " " + savedProduct.getProductId(), ApiRoutes.ProductsSubRoute.ADD_PRODUCT);
        return savedProduct;
    }
    
    /**
     * Edits an existing product.
     * This method updates an existing product with the provided details.
     * All product fields can be updated except the product ID.
     * Deletes old images from ImgBB before uploading new ones.
     * Updates pickup location mappings by deleting existing ones and creating new ones.
     * 
     * @param productRequestModel The product data to update
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     */
    @Override
    @Transactional
    public void editProduct(ProductRequestModel productRequestModel) {
        // Validate request
        if (productRequestModel.getProductId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidId);
        }
        
        // Find existing product
        Product existingProduct = productRepository.findByIdWithRelatedEntities(productRequestModel.getProductId(), getClientId());
        if (existingProduct == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, productRequestModel.getProductId()));
        }
        
        // Create updated product using constructor (validations including pickup location quantities are handled in the constructor)
        Product updatedProduct = new Product(productRequestModel, getUser(), existingProduct);
        
        // Save the updated product
        productRepository.save(updatedProduct);
        
        // Update pickup location mappings (delete existing and create new)
        productPickupLocationMappingRepository.deleteByProductId(updatedProduct.getProductId());
        createPickupLocationMappings(updatedProduct.getProductId(), productRequestModel.getPickupLocationQuantities());
        
        // Process and upload images to ImgBB using the updated product ID (delete old images first)
        processAndUploadProductImages(productRequestModel, updatedProduct, true);
        
        // Log the operation
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.UpdateProduct + " " + updatedProduct.getProductId(), ApiRoutes.ProductsSubRoute.EDIT_PRODUCT);
    }
    
    /**
     * Toggles the deleted status of a product (soft delete/restore).
     * This method toggles the deleted flag of a product without permanently
     * removing it from the database. Deleted products are hidden from standard queries.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
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
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.ToggleProduct + " " + product.getProductId(), ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT);
    }
    
    /**
     * Toggles the return eligibility status of a product.
     * This method toggles whether a product can be returned by customers.
     * This affects the return policy displayed to customers during checkout.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     */
    @Override
    @Transactional
    public void toggleReturnProduct(long id) {
        // Find the product
        Product product = productRepository.findByIdWithRelatedEntities(id, getClientId());
        if (product == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }
        
        // Toggle the returns allowed status
        product.setReturnsAllowed(!product.getReturnsAllowed());
        product.setModifiedUser(getUser());
        
        // Save the updated product
        productRepository.save(product);
        
        // Log the operation
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.ToggleReturnProduct + " " + product.getProductId(), ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT);
    }
    
    /**
     * Retrieves detailed information about a specific product by ID.
     * This method returns comprehensive product details including title,
     * description, pricing, images, category, and availability information.
     * 
     * @param id The ID of the product to retrieve
     * @return The product details
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     */
    @Override
    public ProductResponseModel getProductDetailsById(long id) {
        // Find the product with all related entities
        Product product = productRepository.findByIdWithRelatedEntities(id, getClientId());
        if (product == null) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }
        
        // Convert to response model
        return new ProductResponseModel(product);
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
    public PaginationBaseResponseModel<ProductResponseModel> getProductInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Valid columns for filtering
        Set<String> validColumns = new HashSet<>(Arrays.asList(
            "productId", "title", "descriptionHtml", "brand", "color", "colorLabel",
            "condition", "countryOfManufacture", "model", "upc", "modificationHtml",
            "price", "discount", "isDiscountPercent", "returnsAllowed", "length",
            "breadth", "height", "weightKgs", "categoryId",
            "isDeleted", "itemModified", "createdUser", "modifiedUser", "createdAt",
            "updatedAt", "notes"
        ));

        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException("Invalid column name: " + filter.getColumn());
                }

                // Validate operator
                Set<String> validOperators = new HashSet<>(Arrays.asList(
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
                ));
                if (filter.getOperator() != null && !validOperators.contains(filter.getOperator())) {
                    throw new BadRequestException("Invalid operator: " + filter.getOperator());
                }

                // Validate column type matches operator
                String columnType = productFilterQueryBuilder.getColumnType(filter.getColumn());
                if ("boolean".equals(columnType) && !filter.getOperator().equals("equals") && !filter.getOperator().equals("notEquals")) {
                    throw new BadRequestException("Boolean columns only support 'equals' and 'notEquals' operators");
                }
                if ("date".equals(columnType) || "number".equals(columnType)) {
                    Set<String> numericDateOperators = new HashSet<>(Arrays.asList(
                        "equals", "notEquals", "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
                    ));
                    if (!numericDateOperators.contains(filter.getOperator())) {
                        throw new BadRequestException(columnType + " columns only support numeric comparison operators");
                    }
                }
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException("Invalid pagination: end must be greater than start");
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
            paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

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
     * Creates multiple products in a single operation.
     * 
     * @param products List of ProductRequestModel containing the product data to insert
     * @return BulkInsertResponseModel containing success/failure details for each product
     */
    @Override
    public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkAddProducts(java.util.List<ProductRequestModel> products) {
        if (products == null || products.isEmpty()) {
            throw new BadRequestException("Product list cannot be null or empty");
        }

        com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = 
            new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
        response.setTotalRequested(products.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (ProductRequestModel productRequest : products) {
            try {
                Product createdProduct = persistProduct(productRequest);
                response.addSuccess(productRequest.getTitle(), createdProduct.getProductId());
                successCount++;
            } catch (BadRequestException bre) {
                response.addFailure(
                    productRequest.getTitle() != null ? productRequest.getTitle() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                response.addFailure(
                    productRequest.getTitle() != null ? productRequest.getTitle() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        userLogService.logData(getUserId(), 
            SuccessMessages.ProductsSuccessMessages.InsertProduct + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.ProductsSubRoute.BULK_ADD_PRODUCT);
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        BulkInsertHelper.createBulkInsertResultMessage(response, "Product", messageService, getUserId(), getUser(), getClientId());
        
        return response;
    }
}