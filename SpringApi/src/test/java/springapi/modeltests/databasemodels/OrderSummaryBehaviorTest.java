package springapi.modeltests.databasemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.OrderSummary;
import springapi.models.requestmodels.PurchaseOrderRequestModel;

@DisplayName("Order Summary Behavior Tests")
class OrderSummaryBehaviorTest {

  // Total Tests: 5

  /**
   * Purpose: Verify create constructor computes defaulted totals and trims fields. Expected Result:
   * Derived values and trimmed strings are correctly assigned. Assertions: Subtotal/GST/GrandTotal
   * calculations, pending amount, and trimmed values match expected.
   */
  @Test
  @DisplayName("orderSummary - Create Constructor ComputesAndTrims - Success")
  void orderSummary_s01_createConstructorComputesAndTrims_success() {
    // Arrange
    PurchaseOrderRequestModel.OrderSummaryData data =
        new PurchaseOrderRequestModel.OrderSummaryData();
    data.setProductsSubtotal(new BigDecimal("100.00"));
    data.setTotalDiscount(null);
    data.setPackagingFee(null);
    data.setTotalShipping(null);
    data.setServiceFee(null);
    data.setGstPercentage(null);
    data.setPriority(" HIGH ");
    data.setExpectedDeliveryDate(LocalDateTime.of(2026, 1, 1, 10, 0));
    data.setPromoId(77L);
    data.setTermsConditionsHtml("  terms  ");
    data.setNotes("  notes  ");

    // Act
    OrderSummary summary = new OrderSummary(" PURCHASE_ORDER ", 10L, data, 20L, 1L, "creator");

    // Assert
    assertEquals("PURCHASE_ORDER", summary.getEntityType());
    assertEquals("HIGH", summary.getPriority());
    assertEquals(new BigDecimal("100.00"), summary.getSubtotal());
    assertEquals(new BigDecimal("18.00"), summary.getGstAmount());
    assertEquals(new BigDecimal("118.00"), summary.getGrandTotal());
    assertEquals(new BigDecimal("118.00"), summary.getPendingAmount());
    assertEquals("terms", summary.getTermsConditionsHtml());
    assertEquals("notes", summary.getNotes());
  }

  /**
   * Purpose: Verify update constructor preserves immutable fields and recalculates totals. Expected
   * Result: Existing identifiers are preserved and financial amounts are recomputed. Assertions:
   * Preserved fields and recalculated totals match expected values.
   */
  @Test
  @DisplayName("orderSummary - Update Constructor PreservesAndRecalculates - Success")
  void orderSummary_s02_updateConstructorPreservesAndRecalculates_success() {
    // Arrange
    OrderSummary existing = new OrderSummary();
    existing.setOrderSummaryId(5L);
    existing.setCreatedUser("original");
    existing.setCreatedAt(LocalDateTime.of(2025, 12, 1, 8, 0));
    existing.setClientId(9L);
    existing.setPendingAmount(new BigDecimal("42.00"));

    PurchaseOrderRequestModel.OrderSummaryData data =
        new PurchaseOrderRequestModel.OrderSummaryData();
    data.setProductsSubtotal(new BigDecimal("80.00"));
    data.setTotalDiscount(new BigDecimal("10.00"));
    data.setPackagingFee(new BigDecimal("5.00"));
    data.setTotalShipping(new BigDecimal("5.00"));
    data.setServiceFee(BigDecimal.ZERO);
    data.setGstPercentage(new BigDecimal("10.00"));
    data.setPriority(" MEDIUM ");

    // Act
    OrderSummary summary = new OrderSummary("ORDER", 11L, data, 22L, "modifier", existing);

    // Assert
    assertEquals(5L, summary.getOrderSummaryId());
    assertEquals("original", summary.getCreatedUser());
    assertEquals(LocalDateTime.of(2025, 12, 1, 8, 0), summary.getCreatedAt());
    assertEquals(9L, summary.getClientId());
    assertEquals(new BigDecimal("42.00"), summary.getPendingAmount());
    assertEquals(new BigDecimal("80.00"), summary.getSubtotal());
    assertEquals(new BigDecimal("8.00"), summary.getGstAmount());
    assertEquals(new BigDecimal("88.00"), summary.getGrandTotal());
    assertEquals("modifier", summary.getModifiedUser());
    assertEquals("MEDIUM", summary.getPriority());
  }

  /**
   * Purpose: Verify enum validation helpers accept only supported values. Expected Result: Valid
   * enum values return true and invalid or null values return false. Assertions: isValid checks for
   * entity type and priority behave correctly.
   */
  @Test
  @DisplayName("orderSummary - EnumIsValid Checks - Success")
  void orderSummary_s03_enumIsValidChecks_success() {
    // Arrange/Act/Assert
    assertTrue(OrderSummary.EntityType.isValid("PURCHASE_ORDER"));
    assertTrue(OrderSummary.EntityType.isValid("ORDER"));
    assertFalse(OrderSummary.EntityType.isValid(null));
    assertFalse(OrderSummary.EntityType.isValid("INVALID"));

    assertTrue(OrderSummary.Priority.isValid("LOW"));
    assertTrue(OrderSummary.Priority.isValid("URGENT"));
    assertFalse(OrderSummary.Priority.isValid(null));
    assertFalse(OrderSummary.Priority.isValid("INVALID"));
  }

  /**
   * Purpose: Verify pending amount update logic validates input and computes balance. Expected
   * Result: Valid payment updates pending amount; invalid values throw BadRequestException.
   * Assertions: New pending amount and validation error messages are correct.
   */
  @Test
  @DisplayName("orderSummary - UpdatePendingAmount ValidatesAndUpdates - Success")
  void orderSummary_s04_updatePendingAmountValidatesAndUpdates_success() {
    // Arrange
    OrderSummary summary = new OrderSummary();
    summary.setGrandTotal(new BigDecimal("200.00"));

    // Act
    summary.updatePendingAmount(new BigDecimal("50.00"));

    // Assert
    assertEquals(new BigDecimal("150.00"), summary.getPendingAmount());

    BadRequestException nullAmountEx =
        assertThrows(BadRequestException.class, () -> summary.updatePendingAmount(null));
    assertEquals(
        ErrorMessages.OrderSummaryErrorMessages.PAID_AMOUNT_INVALID, nullAmountEx.getMessage());

    BigDecimal negativePaidAmount = new BigDecimal("-1");
    BadRequestException negativeAmountEx =
        assertThrows(
            BadRequestException.class, () -> summary.updatePendingAmount(negativePaidAmount));
    assertEquals(
        ErrorMessages.OrderSummaryErrorMessages.PAID_AMOUNT_INVALID, negativeAmountEx.getMessage());

    BigDecimal excessivePaidAmount = new BigDecimal("201.00");
    BadRequestException exceedAmountEx =
        assertThrows(
            BadRequestException.class, () -> summary.updatePendingAmount(excessivePaidAmount));
    assertEquals(
        ErrorMessages.OrderSummaryErrorMessages.PAID_AMOUNT_EXCEEDS_GRAND_TOTAL,
        exceedAmountEx.getMessage());
  }

  /**
   * Purpose: Verify constructor validation fails for invalid entity type input. Expected Result:
   * BadRequestException is thrown before object creation. Assertions: Exception message matches
   * invalid entity type error.
   */
  @Test
  @DisplayName("orderSummary - Create Constructor InvalidEntityType Throws - Success")
  void orderSummary_s05_createConstructorInvalidEntityTypeThrows_success() {
    // Arrange
    PurchaseOrderRequestModel.OrderSummaryData data =
        new PurchaseOrderRequestModel.OrderSummaryData();
    data.setProductsSubtotal(new BigDecimal("1.00"));
    data.setPriority("LOW");

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> new OrderSummary("BAD", 1L, data, 1L, 1L, "u"));

    // Assert
    assertEquals(ErrorMessages.OrderSummaryErrorMessages.INVALID_ENTITY_TYPE, ex.getMessage());
  }
}
