package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.ProductReviewRequestModel;
import springapi.services.interfaces.ProductReviewSubTranslator;

/**
 * REST Controller for ProductReview operations.
 *
 * <p>This controller handles all HTTP requests related to product review management including
 * creating, reading, updating, deleting reviews, and managing review scores. All endpoints require
 * token validation for access.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PRODUCT_REVIEW)
public class ProductReviewController extends BaseController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(ProductReviewController.class);
  private final ProductReviewSubTranslator productReviewService;

  @Autowired
  public ProductReviewController(ProductReviewSubTranslator productReviewService) {
    this.productReviewService = productReviewService;
  }

  /**
   * Inserts a new product review.
   *
   * <p>This endpoint creates a new product review with the provided details including ratings,
   * review text, and associated product and user information. Requires token validation to access.
   *
   * @param productReviewRequestModel The product review to create
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  @PutMapping("/" + ApiRoutes.ProductReviewSubRoute.INSERT_PRODUCT_REVIEW)
  public ResponseEntity<?> insertProductReview(
      @RequestBody ProductReviewRequestModel productReviewRequestModel) {
    try {
      productReviewService.insertProductReview(productReviewRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves product reviews for a specific product with pagination support.
   *
   * <p>This endpoint returns a paginated list of reviews for a given product ID. It supports
   * filtering and sorting options through the pagination parameters. Requires token validation to
   * access.
   *
   * @param paginationBaseRequestModel The pagination parameters
   * @param id The product ID to retrieve reviews for
   * @return ResponseEntity containing paginated product review data or error
   */
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  @PostMapping(
      "/" + ApiRoutes.ProductReviewSubRoute.GET_PRODUCT_REVIEWS_IN_BATCHES_GIVEN_PRODUCT_ID)
  public ResponseEntity<?> getProductReviewsGivenProductId(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel, @RequestParam long id) {
    try {
      return ResponseEntity.ok(
          productReviewService.getProductReviewsInBatchesGivenProductId(
              paginationBaseRequestModel, id));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Toggles the deleted status of a product review (soft delete/restore).
   *
   * <p>This endpoint toggles the deleted flag of a product review without permanently removing it
   * from the database. Deleted reviews are hidden from standard queries. Requires token validation
   * to access.
   *
   * @param id The ID of the product review to toggle
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  @DeleteMapping("/" + ApiRoutes.ProductReviewSubRoute.TOGGLE_PRODUCT_REVIEW)
  public ResponseEntity<?> toggleProductReview(@RequestParam long id) {
    try {
      productReviewService.toggleProductReview(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Sets the score of a product review (helpful/not helpful).
   *
   * <p>This endpoint increases or decreases the score of a product review based on user feedback.
   * The score indicates how helpful other users found the review. Requires token validation to
   * access.
   *
   * @param id The ID of the product review
   * @param increaseScore True to increase score, false to decrease
   * @return ResponseEntity indicating success or error
   */
  @PreAuthorize("@customAuthorization.hasAuthority(null)")
  @PostMapping("/" + ApiRoutes.ProductReviewSubRoute.SET_PRODUCT_REVIEW_SCORE)
  public ResponseEntity<?> setProductReviewScore(
      @RequestParam long id, @RequestParam boolean increaseScore) {
    try {
      productReviewService.setProductReviewScore(id, increaseScore);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
