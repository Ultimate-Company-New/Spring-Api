package com.example.springapi.models.databasemodels;

import com.example.springapi.models.requestmodels.CreateReturnRequestModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the ReturnShipmentProduct table.
 *
 * <p>This entity stores individual products being returned in a return shipment. Each return
 * shipment can have multiple products with different quantities and reasons.
 *
 * @author SpringApi Team
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ReturnShipmentProduct")
public class ReturnShipmentProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "returnShipmentProductId")
  private Long returnShipmentProductId;

  // Foreign Key to ReturnShipment
  @Column(name = "returnShipmentId", nullable = false)
  private Long returnShipmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "returnShipmentId", insertable = false, updatable = false)
  private ReturnShipment returnShipment;

  // Foreign Key to Product
  @Column(name = "productId", nullable = false)
  private Long productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "productId", insertable = false, updatable = false)
  private Product product;

  // Return Details
  @Column(name = "returnQuantity", nullable = false)
  private Integer returnQuantity;

  @Column(name = "returnReason", nullable = false, length = 255)
  private String returnReason;

  @Column(name = "returnComments", columnDefinition = "TEXT")
  private String returnComments;

  // Product Details at time of return (denormalized for historical record)
  @Column(name = "productName", nullable = false, length = 255)
  private String productName;

  @Column(name = "productSku", nullable = false, length = 100)
  private String productSku;

  @Column(name = "productSellingPrice", precision = 15, scale = 2, nullable = false)
  private BigDecimal productSellingPrice;

  // Soft Delete
  @Column(name = "isDeleted", nullable = false)
  private Boolean isDeleted = false;

  // Standard Audit Fields
  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @CreationTimestamp
  @Column(name = "createdAt", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updatedAt", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "createdUser", nullable = false, length = 255)
  private String createdUser;

  @Column(name = "modifiedUser", nullable = false, length = 255)
  private String modifiedUser;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  /**
   * Creates a ReturnShipmentProduct entity from a return item and product.
   *
   * @param returnShipmentId The parent return shipment ID
   * @param item Return product item from request (productId, quantity, reason, comments)
   * @param product Product entity for denormalized name, SKU, and price
   * @param clientId Client ID
   * @param currentUser User creating the return
   * @return Populated ReturnShipmentProduct entity ready to persist
   */
  public static ReturnShipmentProduct fromReturnItem(
      Long returnShipmentId,
      CreateReturnRequestModel.ReturnProductItem item,
      Product product,
      Long clientId,
      String currentUser) {
    ReturnShipmentProduct rsp = new ReturnShipmentProduct();
    rsp.setReturnShipmentId(returnShipmentId);
    rsp.setProductId(item.getProductId());
    rsp.setReturnQuantity(item.getQuantity());
    rsp.setReturnReason(item.getReason());
    rsp.setReturnComments(item.getComments());
    rsp.setProductName(product.getTitle());
    rsp.setProductSku(
        product.getUpc() != null ? product.getUpc() : "SKU-" + product.getProductId());
    rsp.setProductSellingPrice(product.getPrice().subtract(product.getDiscount()));
    rsp.setClientId(clientId);
    rsp.setCreatedUser(currentUser);
    rsp.setModifiedUser(currentUser);
    return rsp;
  }
}
