package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import freemarker.template.TemplateException;
import java.io.IOException;

/**
 * Interface for PurchaseOrder operations and data access.
 *
 * <p>This interface defines the contract for all purchase order-related business operations
 * including CRUD operations, approval workflow, and PDF generation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IPurchaseOrderSubTranslator {

  /**
   * Retrieves purchase orders in batches with pagination support.
   *
   * <p>This method returns a paginated list of purchase orders based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * @param paginationBaseRequestModel The pagination parameters including page size, number,
   *     filters, and sorting
   * @return Paginated response containing purchase order data
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  PaginationBaseResponseModel<PurchaseOrderResponseModel> getPurchaseOrdersInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel);

  /**
   * Creates a new purchase order.
   *
   * <p>This method creates a new purchase order with the provided details including supplier
   * information, line items, and delivery details.
   *
   * @param purchaseOrderRequestModel The purchase order to create
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user is not authorized
   */
  void createPurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel);

  /**
   * Updates an existing purchase order.
   *
   * <p>This method updates an existing purchase order's details including supplier information,
   * line items, and delivery details.
   *
   * @param purchaseOrderRequestModel The purchase order to update
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user is not authorized
   */
  void updatePurchaseOrder(PurchaseOrderRequestModel purchaseOrderRequestModel);

  /**
   * Retrieves detailed information about a specific purchase order by ID.
   *
   * <p>This method returns comprehensive purchase order details including supplier information,
   * line items, totals, and approval status.
   *
   * @param id The ID of the purchase order to retrieve
   * @return The purchase order details
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user is not authorized
   */
  PurchaseOrderResponseModel getPurchaseOrderDetailsById(long id);

  /**
   * Toggles the deleted status of a purchase order (soft delete/restore).
   *
   * <p>This method toggles the deleted flag of a purchase order without permanently removing it
   * from the database. Deleted purchase orders are hidden from standard queries.
   *
   * @param id The ID of the purchase order to toggle
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user is not authorized
   */
  void togglePurchaseOrder(long id);

  /**
   * Approves a purchase order.
   *
   * <p>This method marks a purchase order as approved, allowing it to proceed to the next stage in
   * the procurement workflow.
   *
   * @param id The ID of the purchase order to approve
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user is not authorized
   */
  void approvedByPurchaseOrder(long id);

  /**
   * Rejects a purchase order by ID.
   *
   * @param id The ID of the purchase order to reject
   */
  void rejectedByPurchaseOrder(long id);

  /**
   * Generates a PDF document for a purchase order.
   *
   * <p>This method generates a formatted PDF document containing all purchase order details
   * including supplier information, line items, totals, and terms.
   *
   * @param id The ID of the purchase order to generate PDF for
   * @return The PDF as a base64 encoded string
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if the purchase order is not found
   * @throws UnauthorizedException if user is not authorized
   * @throws TemplateException if PDF template processing fails
   * @throws IOException if PDF generation fails
   * @throws com.itextpdf.text.DocumentException if PDF document creation fails
   */
  byte[] getPurchaseOrderPDF(long id)
      throws TemplateException, IOException, com.itextpdf.text.DocumentException;

  /**
   * Creates multiple purchase orders asynchronously with explicit security context.
   *
   * <p>This method runs in a separate thread using @Async annotation and processes each purchase
   * order individually. Results are sent via message to the requesting user. Uses
   * Propagation.NOT_SUPPORTED to prevent rollback-only issues.
   *
   * @param purchaseOrders List of PurchaseOrderRequestModel containing the purchase order data to
   *     insert
   * @param requestingUserId The user ID of the user making the request (captured from security
   *     context)
   * @param requestingUserLoginName The login name of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  void bulkCreatePurchaseOrdersAsync(
      java.util.List<PurchaseOrderRequestModel> purchaseOrders,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId);
}
