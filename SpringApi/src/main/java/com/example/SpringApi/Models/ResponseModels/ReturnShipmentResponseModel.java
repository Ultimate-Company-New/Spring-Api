package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.ReturnShipment;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

/**
 * Response model for ReturnShipment data. Used to return return shipment information in API
 * responses.
 */
@Getter
@Setter
public class ReturnShipmentResponseModel {

  private Long returnShipmentId;
  private Long shipmentId;
  private String returnType;

  // ShipRocket Details
  private String shipRocketReturnOrderId;
  private Long shipRocketReturnShipmentId;
  private String shipRocketReturnStatus;
  private Integer shipRocketReturnStatusCode;
  private String shipRocketReturnAwbCode;
  private String shipRocketReturnAwbMetadata;
  private String shipRocketReturnOrderMetadata;

  // Dimensions
  private BigDecimal returnWeightKgs;
  private BigDecimal returnLength;
  private BigDecimal returnBreadth;
  private BigDecimal returnHeight;

  // Products
  private List<ReturnProductResponseModel> products;

  // Audit
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /** Default constructor */
  public ReturnShipmentResponseModel() {
    this.products = new ArrayList<>();
  }

  /** Constructor from entity */
  public ReturnShipmentResponseModel(ReturnShipment returnShipment) {
    this.returnShipmentId = returnShipment.getReturnShipmentId();
    this.shipmentId = returnShipment.getShipmentId();
    this.returnType =
        returnShipment.getReturnType() != null ? returnShipment.getReturnType().getValue() : null;

    this.shipRocketReturnOrderId = returnShipment.getShipRocketReturnOrderId();
    this.shipRocketReturnShipmentId = returnShipment.getShipRocketReturnShipmentId();
    this.shipRocketReturnStatus = returnShipment.getShipRocketReturnStatus();
    this.shipRocketReturnStatusCode = returnShipment.getShipRocketReturnStatusCode();
    this.shipRocketReturnAwbCode = returnShipment.getShipRocketReturnAwbCode();
    this.shipRocketReturnAwbMetadata = returnShipment.getShipRocketReturnAwbMetadata();
    this.shipRocketReturnOrderMetadata = returnShipment.getShipRocketReturnOrderMetadata();

    this.returnWeightKgs = returnShipment.getReturnWeightKgs();
    this.returnLength = returnShipment.getReturnLength();
    this.returnBreadth = returnShipment.getReturnBreadth();
    this.returnHeight = returnShipment.getReturnHeight();

    this.createdAt = returnShipment.getCreatedAt();
    this.updatedAt = returnShipment.getUpdatedAt();

    // Initialize products
    this.products = new ArrayList<>();
    if (returnShipment.getReturnProducts() != null) {
      Hibernate.initialize(returnShipment.getReturnProducts());
      for (ReturnShipmentProduct rsp : returnShipment.getReturnProducts()) {
        if (!rsp.getIsDeleted()) {
          this.products.add(new ReturnProductResponseModel(rsp));
        }
      }
    }
  }

  /** Inner class for return product details */
  @Getter
  @Setter
  public static class ReturnProductResponseModel {
    private Long returnShipmentProductId;
    private Long productId;
    private Integer returnQuantity;
    private String returnReason;
    private String returnComments;
    private String productName;
    private String productSku;
    private BigDecimal productSellingPrice;

    public ReturnProductResponseModel() {}

    public ReturnProductResponseModel(ReturnShipmentProduct rsp) {
      this.returnShipmentProductId = rsp.getReturnShipmentProductId();
      this.productId = rsp.getProductId();
      this.returnQuantity = rsp.getReturnQuantity();
      this.returnReason = rsp.getReturnReason();
      this.returnComments = rsp.getReturnComments();
      this.productName = rsp.getProductName();
      this.productSku = rsp.getProductSku();
      this.productSellingPrice = rsp.getProductSellingPrice();
    }
  }
}
