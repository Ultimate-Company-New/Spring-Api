package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Services.Interface.IProductSubTranslator;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Product operations.
 * 
 * This controller handles all HTTP requests related to product management
 * including creating, reading, updating, deleting, and managing product availability.
 * All endpoints require appropriate permissions for access.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT)
public class ProductController {

    private static final ContextualLogger logger = ContextualLogger.getLogger(ProductController.class);
    private final IProductSubTranslator productService;

    @Autowired
    public ProductController(IProductSubTranslator productService) {
        this.productService = productService;
    }

    /**
     * Adds a new product.
     * 
     * This endpoint creates a new product with the provided details including
     * title, description, pricing, category, and other product attributes.
     * Requires INSERT_PRODUCTS_PERMISSION to access.
     * 
     * @param productRequestModel The product to create
     * @return ResponseEntity indicating success or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PRODUCTS_PERMISSION +"')")
    @PutMapping("/" + ApiRoutes.ProductsSubRoute.ADD_PRODUCT)
    public ResponseEntity<?> addProduct(@RequestBody ProductRequestModel productRequestModel) {
        try {
            productService.addProduct(productRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Edits an existing product.
     * 
     * This endpoint updates an existing product with the provided details.
     * All product fields can be updated except the product ID.
     * Requires UPDATE_PRODUCTS_PERMISSION to access.
     * 
     * @param productRequestModel The product data to update
     * @return ResponseEntity indicating success or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PRODUCTS_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.ProductsSubRoute.EDIT_PRODUCT)
    public ResponseEntity<?> editProduct(@RequestBody ProductRequestModel productRequestModel) {
        try {
            productService.editProduct(productRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the deleted status of a product (soft delete/restore).
     * 
     * This endpoint toggles the deleted flag of a product without permanently
     * removing it from the database. Deleted products are hidden from standard queries.
     * Requires DELETE_PRODUCTS_PERMISSION to access.
     * 
     * @param id The ID of the product to toggle
     * @return ResponseEntity indicating success or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_PRODUCTS_PERMISSION +"')")
    @DeleteMapping("/" + ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT)
    public ResponseEntity<?> toggleDeleteProduct(@RequestParam long id) {
        try {
            productService.toggleDeleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the return eligibility status of a product.
     * 
     * This endpoint toggles whether a product can be returned by customers.
     * This affects the return policy displayed to customers during checkout.
     * Requires TOGGLE_PRODUCT_RETURNS_PERMISSION to access.
     * 
     * @param id The ID of the product to toggle
     * @return ResponseEntity indicating success or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_PRODUCT_RETURNS_PERMISSION +"')")
    @DeleteMapping("/" + ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT)
    public ResponseEntity<?> toggleReturnProduct(@RequestParam long id) {
        try {
            productService.toggleReturnProduct(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information about a specific product by ID.
     * 
     * This endpoint returns comprehensive product details including title,
     * description, pricing, images, category, and availability information.
     * Requires VIEW_PRODUCTS_PERMISSION to access.
     * 
     * @param id The ID of the product to retrieve
     * @return ResponseEntity containing product details or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PRODUCTS_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_ID)
    public ResponseEntity<?> getProductDetailsById(@RequestParam long id) {
        try {
            return ResponseEntity.ok(productService.getProductDetailsById(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves products in batches with pagination support.
     * 
     * This endpoint returns a paginated list of products based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * Requires VIEW_PRODUCTS_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return ResponseEntity containing paginated product data or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PRODUCTS_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.ProductsSubRoute.GET_PRODUCTS_IN_BATCHES)
    public ResponseEntity<?> getProductInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(productService.getProductInBatches(paginationBaseRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
