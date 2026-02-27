package com.example.SpringApi.FilterQueryBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.SpringApi.Models.DTOs.PurchaseOrderWithDetails;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

@DisplayName("PurchaseOrderFilterQueryBuilder Tests")
class PurchaseOrderFilterQueryBuilderTest extends FilterQueryBuilderTestBase {

  // Total Tests: 4

  /**
   * Purpose: Verify the basic purchase-order paginated query applies all optional branches.
   * Expected Result: Query includes selectedIds, selectedProductIds, includeDeleted, and dynamic
   * filters. Assertions: Type mapping, page totals, and count/main query fragments.
   */
  @Test
  @DisplayName("findPaginatedEntitiesWithMultipleFilters - All Optional Branches - Success")
  void findPaginatedEntitiesWithMultipleFilters_s01_allOptionalBranches_success() {
    // Arrange
    PurchaseOrderFilterQueryBuilder builder = new PurchaseOrderFilterQueryBuilder(entityManager);
    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setPurchaseOrderId(100L);
    QueryFixture<PurchaseOrder> fixture =
        stubPagedQueries(PurchaseOrder.class, List.of(purchaseOrder), 21L);
    List<FilterCondition> filters = List.of(createFilter("vendorNumber", "contains", "VN-"));

    // Act
    Page<PurchaseOrder> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L,
            List.of(100L, 101L),
            List.of(501L, 502L),
            "AND",
            filters,
            false,
            createPageable(0, 10));

    // Assert
    assertEquals("number", builder.getColumnType("purchaseOrderId"));
    assertEquals("date", builder.getColumnType("expectedDeliveryDate"));
    assertEquals("boolean", builder.getColumnType("isDeleted"));
    assertEquals("string", builder.getColumnType("vendorNumber"));
    assertEquals(1, page.getContent().size());
    assertEquals(21L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("po.purchaseOrderId IN :selectedIds"));
    assertTrue(fixture.getMainQueryString().contains("sp.productId IN :selectedProductIds"));
    assertTrue(fixture.getMainQueryString().contains("po.isDeleted = false"));
    assertTrue(fixture.getMainQueryString().contains("LOWER(po.vendorNumber) LIKE LOWER(:param0)"));
    assertTrue(fixture.getCountQueryString().contains("COUNT(DISTINCT po)"));
  }

  /**
   * Purpose: Verify the basic purchase-order query path when optional filters are absent. Expected
   * Result: Query excludes selectedIds/selectedProduct and deletion constraints when includeDeleted
   * is true. Assertions: Page totals and absence/presence of expected query fragments.
   */
  @Test
  @DisplayName("findPaginatedEntitiesWithMultipleFilters - Minimal Branches - Success")
  void findPaginatedEntitiesWithMultipleFilters_s02_minimalBranches_success() {
    // Arrange
    PurchaseOrderFilterQueryBuilder builder = new PurchaseOrderFilterQueryBuilder(entityManager);
    QueryFixture<PurchaseOrder> fixture = stubPagedQueries(PurchaseOrder.class, List.of(), 0L);

    // Act
    Page<PurchaseOrder> page =
        builder.findPaginatedEntitiesWithMultipleFilters(
            1L, null, null, "OR", List.of(), true, createPageable(1, 25));

    // Assert
    assertEquals(0, page.getContent().size());
    assertEquals(0L, page.getTotalElements());
    assertTrue(fixture.getMainQueryString().contains("WHERE po.clientId = :clientId"));
    assertTrue(fixture.getMainQueryString().contains("ORDER BY po.purchaseOrderId DESC"));
    assertTrue(fixture.getCountQueryString().contains("WHERE po.clientId = :clientId"));
    assertTrue(!fixture.getMainQueryString().contains("selectedIds"));
    assertTrue(!fixture.getMainQueryString().contains("selectedProductIds"));
    assertTrue(!fixture.getMainQueryString().contains("po.isDeleted = false"));
  }

  /**
   * Purpose: Verify detailed purchase-order query path with shipment/package/resources/payment
   * aggregation. Expected Result: Distinct result construction with shipment packages and related
   * resources/payments populated. Assertions: Page structure, deduplication effect, nested wiring,
   * and auxiliary query strings.
   */
  @Test
  @DisplayName("findPaginatedWithDetails - Aggregates Related Data - Success")
  void findPaginatedWithDetails_s03_aggregatesRelatedData_success() {
    // Arrange
    PurchaseOrderFilterQueryBuilder builder = new PurchaseOrderFilterQueryBuilder(entityManager);

    PurchaseOrder purchaseOrder = new PurchaseOrder();
    purchaseOrder.setPurchaseOrderId(100L);

    Shipment shipment = new Shipment();
    shipment.setShipmentId(300L);

    OrderSummary orderSummary = new OrderSummary();
    orderSummary.setOrderSummaryId(200L);
    orderSummary.setShipments(new ArrayList<>(List.of(shipment)));

    purchaseOrder.setOrderSummary(orderSummary);

    ShipmentPackage shipmentPackage = new ShipmentPackage();
    shipmentPackage.setShipmentPackageId(400L);
    shipmentPackage.setShipmentId(300L);

    Resources resources = new Resources();
    resources.setResourceId(500L);
    resources.setEntityId(100L);

    Payment payment = new Payment();
    payment.setPaymentId(600L);
    payment.setEntityId(100L);

    PurchaseOrderDetailsQueryFixture fixture =
        stubPurchaseOrderDetailsQueries(
            List.of(purchaseOrder, purchaseOrder),
            22L,
            List.of(shipmentPackage),
            List.of(resources),
            List.of(payment));

    List<FilterCondition> filters = List.of(createFilter("vendorNumber", "contains", "VN"));

    // Act
    Page<PurchaseOrderWithDetails> page =
        builder.findPaginatedWithDetails(
            1L, List.of(100L), List.of(700L), "AND", filters, false, createPageable(0, 20));

    // Assert
    assertEquals(1, page.getContent().size());
    assertEquals(22L, page.getTotalElements());
    PurchaseOrderWithDetails result = page.getContent().get(0);
    assertEquals(100L, result.getPurchaseOrder().getPurchaseOrderId());
    assertEquals(1, result.getAttachments().size());
    assertEquals(1, result.getPayments().size());
    assertEquals(1, result.getOrderSummary().getShipments().size());
    assertEquals(1, result.getOrderSummary().getShipments().get(0).getShipmentPackages().size());

    assertTrue(fixture.getMainQueryString().contains("LEFT JOIN FETCH po.orderSummary os"));
    assertTrue(fixture.getMainQueryString().contains("sp2.productId IN :selectedProductIds"));
    assertTrue(fixture.getShipmentPackageQueryString().contains("FROM ShipmentPackage spkg"));
    assertTrue(fixture.getResourcesQueryString().contains("FROM Resources r"));
    assertTrue(fixture.getPaymentsQueryString().contains("FROM Payment p"));
  }

  /**
   * Purpose: Verify detailed purchase-order query path when no purchase orders are returned.
   * Expected Result: Empty page and no auxiliary shipment/resource/payment fetch queries.
   * Assertions: Empty results and null captured auxiliary query strings.
   */
  @Test
  @DisplayName("findPaginatedWithDetails - Empty Main Result Skips Auxiliary Queries - Success")
  void findPaginatedWithDetails_s04_emptyMainResultSkipsAuxiliaryQueries_success() {
    // Arrange
    PurchaseOrderFilterQueryBuilder builder = new PurchaseOrderFilterQueryBuilder(entityManager);
    stubPagedQueries(PurchaseOrder.class, List.of(), 0L);

    // Act
    Page<PurchaseOrderWithDetails> page =
        builder.findPaginatedWithDetails(
            1L, null, null, "OR", List.of(), true, createPageable(0, 10));

    // Assert
    assertEquals(0, page.getContent().size());
    assertEquals(0L, page.getTotalElements());
  }
}

