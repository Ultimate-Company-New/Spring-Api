package springapi.models.dtos;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import springapi.models.databasemodels.OrderSummary;
import springapi.models.databasemodels.Payment;
import springapi.models.databasemodels.PurchaseOrder;
import springapi.models.databasemodels.Resources;
import springapi.models.responsemodels.PaymentResponseModel;
import springapi.models.responsemodels.PurchaseOrderResponseModel;

/**
 * Holds a PurchaseOrder with all related data loaded in a single query. Used as the page object
 * for. getPurchaseOrdersInBatches - extract and build response model.
 */
@Getter
public class PurchaseOrderWithDetails {
  private final PurchaseOrder purchaseOrder;
  private final OrderSummary orderSummary;
  private final List<Resources> attachments;
  private final List<Payment> payments;

  /** Initializes PurchaseOrderWithDetails. */
  public PurchaseOrderWithDetails(
      PurchaseOrder purchaseOrder,
      OrderSummary orderSummary,
      List<Resources> attachments,
      List<Payment> payments) {
    this.purchaseOrder = purchaseOrder;
    this.orderSummary = orderSummary;
    this.attachments = attachments != null ? attachments : new ArrayList<>();
    this.payments = payments != null ? payments : new ArrayList<>();
  }

  /** Executes to response model. */
  public PurchaseOrderResponseModel toResponseModel() {
    purchaseOrder.setAttachments(attachments);
    PurchaseOrderResponseModel model = new PurchaseOrderResponseModel(purchaseOrder, orderSummary);
    for (Payment payment : payments) {
      model.getPayments().add(new PaymentResponseModel(payment));
    }
    return model;
  }
}
