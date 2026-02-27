package com.example.springapi.controllers;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.requestmodels.PaginationBaseRequestModel;
import com.example.springapi.models.requestmodels.ProductRequestModel;
import com.example.springapi.models.responsemodels.ErrorResponseModel;
import com.example.springapi.services.interfaces.ProductSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Product operations.
 *
 * <p>This controller handles all HTTP requests related to product management including creating,
 * reading, updating, deleting, and managing product availability. All endpoints require appropriate
 * permissions for access.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT)
public class ProductController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(ProductController.class);
  private final ProductSubTranslator productService;

  @Autowired
  public ProductController(ProductSubTranslator productService) {
    this.productService = productService;
  }

  /**
   * Adds a new product.
   *
   * <p>This endpoint creates a new product with the provided details including title, description,
   * pricing, category, and other product attributes. Requires INSERT_PRODUCTS_PERMISSION to access.
   *
   * @param productRequestModel The product to create
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PRODUCTS_PERMISSION + "')")
  @PutMapping("/" + ApiRoutes.ProductsSubRoute.ADD_PRODUCT)
  public ResponseEntity<?> addProduct(@RequestBody ProductRequestModel productRequestModel) {
    try {
      productService.addProduct(productRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Executes bulk add products.
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PRODUCTS_PERMISSION + "')")
  @PutMapping("/" + ApiRoutes.ProductsSubRoute.BULK_ADD_PRODUCT)
  public ResponseEntity<?> bulkAddProducts(
      @RequestBody java.util.List<ProductRequestModel> products) {
    try {
      // Cast to ProductService to access BaseService methods
      com.example.springapi.services.ProductService service =
          (com.example.springapi.services.ProductService) productService;
      Long userId = service.getUserId();
      String loginName = service.getUser();
      Long clientId = service.getClientId();

      // Check if it's a large request to process asynchronously
      if (products != null && products.size() > 5) {
        // Trigger async processing - returns immediately
        productService.bulkAddProductsAsync(products, userId, loginName, clientId);

        // Return 202 Accepted (better for async but 200 OK is fine as per existing
        // code)
        return ResponseEntity.ok().build();
      } else {
        // Process synchronously
        return ResponseEntity.ok(productService.bulkAddProducts(products));
      }
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Edits an existing product.
   *
   * <p>This endpoint updates an existing product with the provided details. All product fields can
   * be updated except the product ID. Requires UPDATE_PRODUCTS_PERMISSION to access.
   *
   * @param productRequestModel The product data to update
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.UPDATE_PRODUCTS_PERMISSION + "')")
  @PostMapping("/" + ApiRoutes.ProductsSubRoute.EDIT_PRODUCT)
  public ResponseEntity<?> editProduct(@RequestBody ProductRequestModel productRequestModel) {
    try {
      productService.editProduct(productRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Toggles the deleted status of a product (soft delete/restore).
   *
   * <p>This endpoint toggles the deleted flag of a product without permanently removing it from the
   * database. Deleted products are hidden from standard queries. Requires
   * DELETE_PRODUCTS_PERMISSION to access.
   *
   * @param id The ID of the product to toggle
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.DELETE_PRODUCTS_PERMISSION + "')")
  @DeleteMapping("/" + ApiRoutes.ProductsSubRoute.TOGGLE_DELETE_PRODUCT + "/{id}")
  public ResponseEntity<?> toggleDeleteProduct(@PathVariable long id) {
    try {
      productService.toggleDeleteProduct(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Toggles the return eligibility status of a product.
   *
   * <p>This endpoint toggles whether a product can be returned by customers. This affects the
   * return policy displayed to customers during checkout. Requires
   * TOGGLE_PRODUCT_RETURNS_PERMISSION to access.
   *
   * @param id The ID of the product to toggle
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.TOGGLE_PRODUCT_RETURNS_PERMISSION
          + "')")
  @DeleteMapping("/" + ApiRoutes.ProductsSubRoute.TOGGLE_RETURN_PRODUCT + "/{id}")
  public ResponseEntity<?> toggleReturnProduct(@PathVariable long id) {
    try {
      productService.toggleReturnProduct(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Retrieves detailed information about a specific product by ID.
   *
   * <p>This endpoint returns comprehensive product details including title, description, pricing,
   * images, category, and availability information. Requires VIEW_PRODUCTS_PERMISSION to access.
   *
   * @param id The ID of the product to retrieve
   * @return ResponseEntity containing product details or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PRODUCTS_PERMISSION + "')")
  @GetMapping("/" + ApiRoutes.ProductsSubRoute.GET_PRODUCT_DETAILS_BY_ID + "/{id}")
  public ResponseEntity<?> getProductDetailsById(@PathVariable long id) {
    try {
      return ResponseEntity.ok(productService.getProductDetailsById(id));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Retrieves products in batches with pagination support.
   *
   * <p>This endpoint returns a paginated list of products based on the provided pagination
   * parameters. It supports filtering and sorting options. Requires VIEW_PRODUCTS_PERMISSION to
   * access.
   *
   * @param paginationBaseRequestModel The pagination parameters
   * @return ResponseEntity containing paginated product data or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PRODUCTS_PERMISSION + "')")
  @PostMapping("/" + ApiRoutes.ProductsSubRoute.GET_PRODUCTS_IN_BATCHES)
  public ResponseEntity<?> getProductInBatches(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(productService.getProductInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Retrieves categories based on parent ID for hierarchical navigation.
   *
   * <p>If parentId is null: Returns all root categories (categories with null parentId) If parentId
   * is provided: Returns all child categories of that parent (where isEnd=true)
   *
   * <p>This enables drill-down category navigation where users can browse the hierarchy level by
   * level until they reach assignable leaf categories. Requires INSERT_PRODUCTS_PERMISSION or
   * VIEW_PRODUCTS_PERMISSION to access.
   *
   * @param parentId The parent category ID (optional, null for root categories)
   * @return ResponseEntity containing list of categories with full paths or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PRODUCTS_PERMISSION
          + "') || "
          + "@customAuthorization.hasAuthority('"
          + Authorizations.VIEW_PRODUCTS_PERMISSION
          + "')")
  @GetMapping("/" + ApiRoutes.ProductCategorySubRoute.FIND_CATEGORIES_WITHOUT_CHILDREN)
  public ResponseEntity<?> findCategoriesByParentId(@RequestParam(required = false) Long parentId) {
    try {
      return ResponseEntity.ok(productService.findCategoriesByParentId(parentId));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Retrieves full category paths for a list of category IDs.
   *
   * <p>This endpoint takes a list of category IDs and returns a mapping of each ID to its full
   * hierarchical path (e.g., "Electronics > Computers > Laptops"). Useful for bulk operations like
   * product import where multiple category paths need to be resolved efficiently. Requires
   * INSERT_PRODUCTS_PERMISSION or VIEW_PRODUCTS_PERMISSION to access.
   *
   * @param categoryIds List of category IDs to get paths for
   * @return ResponseEntity containing Map of category ID to full path string
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PRODUCTS_PERMISSION
          + "') || "
          + "@customAuthorization.hasAuthority('"
          + Authorizations.VIEW_PRODUCTS_PERMISSION
          + "')")
  @PostMapping("/" + ApiRoutes.ProductCategorySubRoute.GET_CATEGORY_PATHS_BY_IDS)
  public ResponseEntity<?> getCategoryPathsByIds(@RequestBody java.util.List<Long> categoryIds) {
    try {
      return ResponseEntity.ok(productService.getCategoryPathsByIds(categoryIds));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Gets product stock information across all pickup locations for a specific product. Returns.
   * stock availability with pickup location address details for distance calculation. Requires
   * VIEW_PRODUCTS_PERMISSION to access.
   *
   * @param productId The product ID
   * @return ResponseEntity containing list of ProductStockByLocationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.VIEW_PRODUCTS_PERMISSION
          + "') || "
          + "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION
          + "') || "
          + "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @GetMapping(
      "/"
          + ApiRoutes.ProductsSubRoute.GET_PRODUCT_STOCK_AT_LOCATIONS_BY_PRODUCT_ID
          + "/{productId}/{quantity}/{deliveryPostcode}/{isCod}")
  public ResponseEntity<?> getProductStockAtLocationsByProductId(
      @PathVariable Long productId,
      @PathVariable Integer quantity,
      @PathVariable String deliveryPostcode,
      @PathVariable Boolean isCod) {
    try {
      com.example.springapi.services.ProductService service =
          (com.example.springapi.services.ProductService) productService;
      // Handle "0" or empty delivery postcode as null
      String effectivePostcode =
          (deliveryPostcode == null || deliveryPostcode.equals("0") || deliveryPostcode.isEmpty())
              ? null
              : deliveryPostcode;
      Integer effectiveQuantity = (quantity == null || quantity <= 0) ? null : quantity;
      return ResponseEntity.ok(
          service.getProductStockAtLocationsByProductId(
              productId, effectiveQuantity, effectivePostcode, isCod));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
