package com.example.SpringApi.Models.DTOs;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.ResponseModels.PaymentResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Holds a PurchaseOrder with all related data loaded in a single query. Used as the page object for
 * getPurchaseOrdersInBatches - extract and build response model.
 */
@Getter
public class PurchaseOrderWithDetails {
  private final PurchaseOrder purchaseOrder;
  private final OrderSummary orderSummary;
  private final List<Resources> attachments;
  private final List<Payment> payments;

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

  public PurchaseOrderResponseModel toResponseModel() {
    purchaseOrder.setAttachments(attachments);
    PurchaseOrderResponseModel model = new PurchaseOrderResponseModel(purchaseOrder, orderSummary);
    for (Payment payment : payments) {
      model.getPayments().add(new PaymentResponseModel(payment));
    }
    return model;
  }
}
