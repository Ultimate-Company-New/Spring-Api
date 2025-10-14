package com.example.SpringApi.Services;

import com.example.SpringApi.Services.Interface.IProductSubTranslator;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Repositories.ProductRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Repositories.ProductCategoryRepository;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Service implementation for Product operations.
 * 
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
    private final UserLogService userLogService;
    private final ProductCategoryRepository productCategoryRepository;
    private final GoogleCredRepository googleCredRepository;
    private final ClientService clientService;
    private final Environment environment;
    
    @Autowired
    public ProductService(ProductRepository productRepository,
                         UserLogService userLogService,
                         ProductCategoryRepository productCategoryRepository,
                         GoogleCredRepository googleCredRepository,
                         ClientService clientService,
                         Environment environment,
                         HttpServletRequest request) {
        super();
        this.productRepository = productRepository;
        this.userLogService = userLogService;
        this.productCategoryRepository = productCategoryRepository;
        this.googleCredRepository = googleCredRepository;
        this.clientService = clientService;
        this.environment = environment;
    }

    /**
     * Processes and uploads product images to Firebase storage.
     * 
     * This method handles multiple product images including required images
     * (main, top, bottom, front, back, right, left, details, defect) and
     * optional images (additional_1, additional_2, additional_3).
     * It converts URLs to base64 format and uploads them to Firebase with
     * structured paths using the saved product ID.
     * 
     * @param productRequestModel The product request model containing image data
     * @param savedProduct The saved product entity with generated ID
     * @throws BadRequestException if required images are missing or upload fails
     */
    private void processAndUploadProductImages(ProductRequestModel productRequestModel, Product savedProduct) {
        // Get GoogleCred for Firebase access
        Long clientId = getClientId();
        Optional<GoogleCred> googleCredOpt = googleCredRepository.findById(clientId);
        if (googleCredOpt.isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
        }
        
        GoogleCred googleCred = googleCredOpt.get();
        ClientResponseModel clientDetails = clientService.getClientById(clientId);
        FirebaseHelper firebaseHelper = new FirebaseHelper(googleCred);
        String[] requiredImages = {
            productRequestModel.getMainImage(),
            productRequestModel.getTopImage(),
            productRequestModel.getBottomImage(),
            productRequestModel.getFrontImage(),
            productRequestModel.getBackImage(),
            productRequestModel.getRightImage(),
            productRequestModel.getLeftImage(),
            productRequestModel.getDetailsImage(),
            productRequestModel.getDefectImage()
        };
        
        String[] optionalImages = {
            productRequestModel.getAdditionalImage1(),
            productRequestModel.getAdditionalImage2(),
            productRequestModel.getAdditionalImage3()
        };
        
        // Validate required images
        String[] imageTypes = {"main", "top", "bottom", "front", "back", "right", "left", "details", "defect"};
        for (int i = 0; i < requiredImages.length; i++) {
            if (requiredImages[i] == null || requiredImages[i].trim().isEmpty()) {
                throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER009, imageTypes[i]));
            }
        }
        
        // Process required images
        for (int i = 0; i < requiredImages.length; i++) {
            String base64Image = convertToBase64(requiredImages[i]);
            String filePath = clientDetails.getName() + " - " + clientId
                            + File.separator
                            + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default")
                            + File.separator
                            + "Products"
                            + File.separator
                            + savedProduct.getProductId() + "-" + imageTypes[i] + ".png";
            
            // Delete existing file if it exists, then upload new one
            firebaseHelper.deleteFile(filePath);
            boolean uploadSuccess = firebaseHelper.uploadFileToFirebase(base64Image, filePath);
            if (!uploadSuccess) {
                throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER010, imageTypes[i]));
            }
        }
        
        // Process optional images
        for (int i = 0; i < optionalImages.length; i++) {
            if (optionalImages[i] != null && !optionalImages[i].trim().isEmpty()) {
                String base64Image = convertToBase64(optionalImages[i]);
                String filePath = clientDetails.getName() + " - " + clientId
                                + File.separator
                                + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default")
                                + File.separator
                                + "Products"
                                + File.separator
                                + savedProduct.getProductId() + "-additional_" + (i + 1) + ".png";
                
                // Delete existing file if it exists, then upload new one
                firebaseHelper.deleteFile(filePath);
                boolean uploadSuccess = firebaseHelper.uploadFileToFirebase(base64Image, filePath);
                if (!uploadSuccess) {
                    throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER011, (i + 1)));
                }
            }
        }
    }

    /**
     * Converts a URL or base64 string to base64 format.
     * 
     * If the input is already a base64 string, returns it as-is.
     * If the input is a URL, fetches the image and converts it to base64.
     * 
     * @param imageData The image data (URL or base64 string)
     * @return Base64 encoded image string
     * @throws BadRequestException if conversion fails
     */
    private String convertToBase64(String imageData) {
        if (imageData == null || imageData.trim().isEmpty()) {
            return null;
        }
        
        // Check if it's already base64 (starts with data:image or just base64 content)
        if (imageData.startsWith("data:image") || !imageData.startsWith("http")) {
            return imageData;
        }
        
        // It's a URL, fetch and convert to base64
        try {
            URL url = URI.create(imageData).toURL();
            try (InputStream inputStream = url.openStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                byte[] imageBytes = outputStream.toByteArray();
                String base64String = Base64.getEncoder().encodeToString(imageBytes);
                
                // Return as data URL format
                return "data:image/png;base64," + base64String;
            }
        } catch (IOException e) {
            throw new BadRequestException(String.format(ErrorMessages.ProductErrorMessages.ER012, imageData));
        }
    }
    
    /**
     * Adds a new product.
     * 
     * This method creates a new product with the provided details including
     * title, description, pricing, category, and other product attributes.
     * It validates the category existence, saves the product first to get the ID,
     * then handles multiple product images by processing URLs to base64 and 
     * uploading them to Firebase storage.
     * 
     * @param productRequestModel The product to create
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user is not authorized
     */
    @Override
    @Transactional
    public void addProduct(ProductRequestModel productRequestModel) {
        // Validate category exists
        if (productRequestModel.getCategoryId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidCategoryId);
        }
        
        Optional<com.example.SpringApi.Models.DatabaseModels.ProductCategory> categoryOpt = 
            productCategoryRepository.findById(productRequestModel.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER008, productRequestModel.getCategoryId()));
        }
        
        // Create new product using constructor (validations are handled in the constructor)
        Product product = new Product(productRequestModel, getUser());
        
        // Save the product first to get the ID
        Product savedProduct = productRepository.save(product);
        
        // Process and upload images to Firebase using the saved product ID
        processAndUploadProductImages(productRequestModel, savedProduct);
        
        // Log the operation
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.InsertProduct + " " + savedProduct.getProductId(), ApiRoutes.ProductsSubRoute.ADD_PRODUCT);
    }
    
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
    @Override
    @Transactional
    public void editProduct(ProductRequestModel productRequestModel) {
        // Validate request
        if (productRequestModel.getProductId() == null) {
            throw new BadRequestException(ErrorMessages.ProductErrorMessages.InvalidId);
        }
        
        // Find existing product
        Optional<Product> existingProductOpt = productRepository.findById(productRequestModel.getProductId());
        if (existingProductOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, productRequestModel.getProductId()));
        }
        
        // Create updated product using constructor (validations are handled in the constructor)
        Product updatedProduct = new Product(productRequestModel, getUser(), existingProductOpt.get());
        
        // Save the updated product
        productRepository.save(updatedProduct);
        
        // Process and upload images to Firebase using the updated product ID
        processAndUploadProductImages(productRequestModel, updatedProduct);
        
        // Log the operation
        userLogService.logData(getUserId(), SuccessMessages.ProductsSuccessMessages.UpdateProduct + " " + updatedProduct.getProductId(), ApiRoutes.ProductsSubRoute.EDIT_PRODUCT);
    }
    
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
    @Override
    @Transactional
    public void toggleDeleteProduct(long id) {
        // Find the product
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }
        
        Product product = productOpt.get();
        
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
     * 
     * This method toggles whether a product can be returned by customers.
     * This affects the return policy displayed to customers during checkout.
     * 
     * @param id The ID of the product to toggle
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the product is not found
     * @throws UnauthorizedException if user is not authorized
     */
    @Override
    @Transactional
    public void toggleReturnProduct(long id) {
        // Find the product
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }
        
        Product product = productOpt.get();
        
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
    @Override
    public ProductResponseModel getProductDetailsById(long id) {
        // Find the product with all related entities
        Optional<Product> productOpt = productRepository.findByIdWithRelatedEntities(id);
        if (productOpt.isEmpty()) {
            throw new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, id));
        }
        
        Product product = productOpt.get();
        
        // Convert to response model
        return new ProductResponseModel(product);
    }
    
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
    @Override
    public PaginationBaseResponseModel<ProductResponseModel> getProductInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Validate the column name if provided
        if (paginationBaseRequestModel.getColumnName() != null && !paginationBaseRequestModel.getColumnName().trim().isEmpty()) {
            Set<String> validColumns = new HashSet<>(Arrays.asList(
                "productId",
                "title", "type", "upc", "price",
                "discount", "availableStock", "dimensions"));
            
            if (!validColumns.contains(paginationBaseRequestModel.getColumnName())) {
                throw new BadRequestException(
                    ErrorMessages.InvalidColumn + String.join(",", validColumns)
                );
            }
        }

        // Calculate page number and size
        int pageSize = paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart();
        int pageNumber = paginationBaseRequestModel.getStart() / pageSize;

        // Create pageable with default sorting (most recent first)
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Get paginated products with filtering
        Page<Product> productPage = productRepository.findPaginatedProducts(
            paginationBaseRequestModel.getColumnName(),
            paginationBaseRequestModel.getCondition(),
            paginationBaseRequestModel.getFilterExpr(),
            paginationBaseRequestModel.isIncludeDeleted(),
            paginationBaseRequestModel.getSelectedIds() != null ? 
                new HashSet<>(paginationBaseRequestModel.getSelectedIds()) : null,
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
}