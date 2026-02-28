package springapi.controllers;

import freemarker.template.TemplateException;
import java.io.IOException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.PurchaseOrderRequestModel;
import springapi.services.interfaces.PurchaseOrderSubTranslator;

/**
 * REST Controller for PurchaseOrder operations.
 *
 * <p>This controller handles all HTTP requests related to purchase order management, including CRUD
 * operations, approval workflow, and PDF generation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PURCHASE_ORDER)
public class PurchaseOrderController extends BaseController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(PurchaseOrderController.class);
  private final PurchaseOrderSubTranslator purchaseOrderService;

  public PurchaseOrderController(PurchaseOrderSubTranslator purchaseOrderService) {
    this.purchaseOrderService = purchaseOrderService;
  }

  /**
   * Retrieves purchase orders in batches with pagination support.
   *
   * <p>This endpoint returns a paginated list of purchase orders based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * @param paginationBaseRequestModel The pagination parameters including page size, number,
   *     filters, and sorting
   * @return ResponseEntity containing paginated purchase order data
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @PostMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDERS_IN_BATCHES)
  public ResponseEntity<?> getPurchaseOrdersInBatches(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(
          purchaseOrderService.getPurchaseOrdersInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates a new purchase order.
   *
   * <p>This endpoint creates a new purchase order with the provided details including supplier
   * information, line items, and delivery details.
   *
   * @param purchaseOrderRequestModel The purchase order to create
   * @return ResponseEntity with no content on success
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user lacks INSERT_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PutMapping(ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER)
  public ResponseEntity<?> createPurchaseOrder(
      @RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
    try {
      purchaseOrderService.createPurchaseOrder(purchaseOrderRequestModel);
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
   * Creates multiple purchase orders asynchronously.
   *
   * <p>This endpoint triggers async processing of multiple purchase orders. Results will be sent to
   * the user via message when processing completes.
   *
   * @param purchaseOrders List of purchase orders to create
   * @return ResponseEntity with 200 OK if processing started successfully
   * @throws BadRequestException if validation fails
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PutMapping(ApiRoutes.PurchaseOrderSubRoute.BULK_CREATE_PURCHASE_ORDER)
  public ResponseEntity<?> bulkCreatePurchaseOrders(
      @RequestBody java.util.List<PurchaseOrderRequestModel> purchaseOrders) {
    try {
      // Cast to PurchaseOrderService to access BaseService methods
      springapi.services.PurchaseOrderService service =
          (springapi.services.PurchaseOrderService) purchaseOrderService;
      Long userId = service.getUserId();
      String loginName = service.getUser();
      Long clientId = service.getClientId();

      // Trigger async processing - returns immediately
      purchaseOrderService.bulkCreatePurchaseOrdersAsync(
          purchaseOrders, userId, loginName, clientId);

      // Return 200 OK - processing will continue in background
      return ResponseEntity.ok().build();
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Updates an existing purchase order.
   *
   * <p>This endpoint updates an existing purchase order's details including supplier information,
   * line items, and delivery details.
   *
   * @param purchaseOrderRequestModel The purchase order to update
   * @return ResponseEntity with no content on success
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user lacks UPDATE_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping(ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER)
  public ResponseEntity<?> updatePurchaseOrder(
      @RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
    try {
      purchaseOrderService.updatePurchaseOrder(purchaseOrderRequestModel);
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
   * Retrieves detailed information about a specific purchase order by ID.
   *
   * <p>This endpoint returns comprehensive purchase order details including supplier information,
   * line items, totals, and approval status.
   *
   * @param id The ID of the purchase order to retrieve
   * @return ResponseEntity containing the purchase order details
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_BY_ID + "/{id}")
  public ResponseEntity<?> getPurchaseOrderDetailsById(@PathVariable long id) {
    try {
      return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderDetailsById(id));
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
   * Toggles the deleted status of a purchase order (soft delete/restore).
   *
   * <p>This endpoint toggles the deleted flag of a purchase order without permanently removing it
   * from the database. Deleted purchase orders are hidden from standard queries.
   *
   * @param id The ID of the purchase order to toggle
   * @return ResponseEntity with no content on success
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user lacks TOGGLE_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.TOGGLE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER + "/{id}")
  public ResponseEntity<?> togglePurchaseOrder(@PathVariable long id) {
    try {
      purchaseOrderService.togglePurchaseOrder(id);
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
   * Approves a purchase order.
   *
   * <p>This endpoint marks a purchase order as approved, allowing it to proceed to the next stage
   * in the procurement workflow.
   *
   * @param id The ID of the purchase order to approve
   * @return ResponseEntity with no content on success
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user lacks UPDATE_PURCHASE_ORDERS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER + "/{id}")
  public ResponseEntity<?> approvedByPurchaseOrder(@PathVariable long id) {
    try {
      purchaseOrderService.approvedByPurchaseOrder(id);
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
   * Rejects a purchase order by ID.
   *
   * @param id The ID of the purchase order to reject
   * @return ResponseEntity with status 200 if successful
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.REJECTED_BY_PURCHASE_ORDER + "/{id}")
  public ResponseEntity<?> rejectedByPurchaseOrder(@PathVariable long id) {
    try {
      purchaseOrderService.rejectedByPurchaseOrder(id);
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
   * Generates a PDF document for a purchase order.
   *
   * <p>This endpoint generates a formatted PDF document containing all purchase order details
   * including supplier information, line items, totals, and terms.
   *
   * @param id The ID of the purchase order to generate PDF for
   * @return ResponseEntity containing the PDF file as downloadable content
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
   * @throws TemplateException if PDF template processing fails
   * @throws IOException if PDF generation fails
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_PDF + "/{id}")
  public ResponseEntity<?> getPurchaseOrderPdf(@PathVariable long id)
      throws TemplateException, IOException, com.itextpdf.text.DocumentException {
    try {
      byte[] pdfBytes = purchaseOrderService.getPurchaseOrderPdf(id);

      // Set headers for PDF download
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDisposition(
          ContentDisposition.builder("attachment")
              .filename("PurchaseOrder_" + id + ".pdf")
              .build());
      headers.setContentLength(pdfBytes.length);

      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (TemplateException | IOException e) {
      logger.error(e);
      throw e;
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
