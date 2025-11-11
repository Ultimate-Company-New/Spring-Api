package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Services.Interface.IPurchaseOrderSubTranslator;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import freemarker.template.TemplateException;
import com.example.SpringApi.Logging.ContextualLogger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for PurchaseOrder operations.
 * 
 * This controller handles all HTTP requests related to purchase order management,
 * including CRUD operations, approval workflow, and PDF generation.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PURCHASE_ORDER)
public class PurchaseOrderController {
    
    private static final ContextualLogger logger = ContextualLogger.getLogger(PurchaseOrderController.class);
    private final IPurchaseOrderSubTranslator purchaseOrderService;
    
    public PurchaseOrderController(IPurchaseOrderSubTranslator purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }
    
    /**
     * Retrieves purchase orders in batches with pagination support.
     * 
     * This endpoint returns a paginated list of purchase orders based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * 
     * @param paginationBaseRequestModel The pagination parameters including page size, number, filters, and sorting
     * @return ResponseEntity containing paginated purchase order data
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDERS_IN_BATCHES)
    public ResponseEntity<?> getPurchaseOrdersInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersInBatches(paginationBaseRequestModel));
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
     * Creates a new purchase order.
     * 
     * This endpoint creates a new purchase order with the provided details including
     * supplier information, line items, and delivery details.
     * 
     * @param purchaseOrderRequestModel The purchase order to create
     * @return ResponseEntity with no content on success
     * @throws BadRequestException if validation fails
     * @throws UnauthorizedException if user lacks INSERT_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION +"')")
    @PutMapping(ApiRoutes.PurchaseOrderSubRoute.CREATE_PURCHASE_ORDER)
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
        try {
            purchaseOrderService.createPurchaseOrder(purchaseOrderRequestModel);
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
     * Updates an existing purchase order.
     * 
     * This endpoint updates an existing purchase order's details including
     * supplier information, line items, and delivery details.
     * 
     * @param purchaseOrderRequestModel The purchase order to update
     * @return ResponseEntity with no content on success
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user lacks UPDATE_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.PurchaseOrderSubRoute.UPDATE_PURCHASE_ORDER)
    public ResponseEntity<?> updatePurchaseOrder(@RequestBody PurchaseOrderRequestModel purchaseOrderRequestModel) {
        try {
            purchaseOrderService.updatePurchaseOrder(purchaseOrderRequestModel);
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
     * Retrieves detailed information about a specific purchase order by ID.
     * 
     * This endpoint returns comprehensive purchase order details including
     * supplier information, line items, totals, and approval status.
     * 
     * @param id The ID of the purchase order to retrieve
     * @return ResponseEntity containing the purchase order details
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_BY_ID + "/{id}")
    public ResponseEntity<?> getPurchaseOrderDetailsById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderDetailsById(id));
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
     * Toggles the deleted status of a purchase order (soft delete/restore).
     * 
     * This endpoint toggles the deleted flag of a purchase order without permanently
     * removing it from the database. Deleted purchase orders are hidden from standard queries.
     * 
     * @param id The ID of the purchase order to toggle
     * @return ResponseEntity with no content on success
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user lacks TOGGLE_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.TOGGLE_PURCHASE_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.TOGGLE_PURCHASE_ORDER + "/{id}")
    public ResponseEntity<?> togglePurchaseOrder(@PathVariable long id) {
        try {
            purchaseOrderService.togglePurchaseOrder(id);
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
     * Approves a purchase order.
     * 
     * This endpoint marks a purchase order as approved, allowing it to proceed
     * to the next stage in the procurement workflow.
     * 
     * @param id The ID of the purchase order to approve
     * @return ResponseEntity with no content on success
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user lacks UPDATE_PURCHASE_ORDERS_PERMISSION
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.APPROVED_BY_PURCHASE_ORDER + "/{id}")
    public ResponseEntity<?> approvedByPurchaseOrder(@PathVariable long id) {
        try {
            purchaseOrderService.approvedByPurchaseOrder(id);
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
     * Rejects a purchase order by ID.
     * 
     * @param id The ID of the purchase order to reject
     * @return ResponseEntity with status 200 if successful
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION +"')")
    @DeleteMapping(ApiRoutes.PurchaseOrderSubRoute.REJECTED_BY_PURCHASE_ORDER + "/{id}")
    public ResponseEntity<?> rejectedByPurchaseOrder(@PathVariable long id) {
        try {
            purchaseOrderService.rejectedByPurchaseOrder(id);
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
     * Generates a PDF document for a purchase order.
     * 
     * This endpoint generates a formatted PDF document containing all purchase order
     * details including supplier information, line items, totals, and terms.
     * 
     * @param id The ID of the purchase order to generate PDF for
     * @return ResponseEntity containing the PDF file as downloadable content
     * @throws BadRequestException if validation fails
     * @throws NotFoundException if the purchase order is not found
     * @throws UnauthorizedException if user lacks VIEW_PURCHASE_ORDERS_PERMISSION
     * @throws TemplateException if PDF template processing fails
     * @throws IOException if PDF generation fails
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @GetMapping(ApiRoutes.PurchaseOrderSubRoute.GET_PURCHASE_ORDER_PDF + "/{id}")
    public ResponseEntity<?> getPurchaseOrderPDF(@PathVariable long id) throws TemplateException, IOException, com.itextpdf.text.DocumentException {
        try {
            byte[] pdfBytes = purchaseOrderService.getPurchaseOrderPDF(id);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("PurchaseOrder_" + id + ".pdf")
                    .build()
            );
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (TemplateException e) {
            logger.error(e);
            throw e;
        } catch (IOException e) {
            logger.error(e);
            throw e;
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
