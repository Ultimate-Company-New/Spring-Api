package com.example.SpringApi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Test class for PurchaseOrderService.updatePurchaseOrder method.
 *
 * <p>Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - UpdatePurchaseOrder Tests")
class UpdatePurchaseOrderTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 6
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify update succeeds with attachments. Expected Result: Attachments are uploaded,
   * resources saved, and update completes. Assertions: Repository saves and logging are performed.
   */
  @Test
  @DisplayName("Update PO - Success With Attachments")
  void updatePurchaseOrder_WithAttachments_Success() {
    // Arrange
    Map<String, String> attachments = new HashMap<>();
    attachments.put("invoice.pdf", "base64-data");
    testPurchaseOrderRequest.setAttachments(attachments);

    Resources existingResource = new Resources();
    existingResource.setResourceId(200L);
    existingResource.setDeleteHashValue("hash-to-delete");
    stubResourcesRepositoryFindByEntityIdAndEntityType(List.of(existingResource));
    stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
    stubClientRepositoryFindById(Optional.of(testClient));
    stubAddressRepositoryFindExactDuplicate(Optional.empty());
    stubAddressRepositorySave(testAddress);
    stubPurchaseOrderRepositorySave(testPurchaseOrder);
    stubOrderSummaryRepositorySave(testOrderSummary);
    stubShipmentRepositorySaveAssigningId(1L);
    stubShipmentProductRepositorySaveAll();
    stubShipmentPackageRepositorySaveAssigningId(1L);
    stubShipmentPackageProductRepositorySaveAll();
    stubResourcesRepositorySave(new Resources());

    List<ImgbbHelper.AttachmentUploadResult> results =
        List.of(
            new ImgbbHelper.AttachmentUploadResult(
                "https://i.ibb.co/test/invoice.pdf", "delete-hash-456", null));

    try (MockedConstruction<ImgbbHelper> ignored =
        stubImgbbHelperUploadResultsWithDelete(results, 1)) {
      // Act
      assertDoesNotThrow(() -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

      // Assert
      verify(purchaseOrderRepository, atLeastOnce()).save(any(PurchaseOrder.class));
      verify(orderSummaryRepository, atLeastOnce()).save(any(OrderSummary.class));
      verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify client missing during attachment cleanup is rejected. Expected Result:
   * NotFoundException is thrown. Assertions: Exception message matches InvalidId.
   */
  @Test
  @DisplayName("Update PO - Client Missing For Attachment Cleanup")
  void updatePurchaseOrder_ClientMissingForCleanup_Success() {
    // Arrange
    Map<String, String> attachments = new HashMap<>();
    attachments.put("doc.pdf", "base64-data");
    testPurchaseOrderRequest.setAttachments(attachments);

    Resources existingResource = new Resources();
    existingResource.setResourceId(200L);
    existingResource.setDeleteHashValue("hash-to-delete");
    stubResourcesRepositoryFindByEntityIdAndEntityType(List.of(existingResource));
    stubPurchaseOrderRepositoryFindById(Optional.of(testPurchaseOrder));
    stubClientRepositoryFindById(Optional.empty());

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.ClientErrorMessages.INVALID_ID,
        () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify not found is thrown for missing ID. Expected Result: NotFoundException is
   * thrown. Assertions: Exception message matches InvalidId.
   */
  @Test
  @DisplayName("Update PO - Not Found")
  void updatePurchaseOrder_NotFound_Failure() {
    // Arrange
    List<Long> invalidIds = List.of(0L, -1L, -100L, Long.MAX_VALUE, Long.MIN_VALUE);

    // Act & Assert
    for (Long id : invalidIds) {
      initializeTestData();
      testPurchaseOrderRequest.setPurchaseOrderId(id);
      stubPurchaseOrderRepositoryFindById(Optional.empty());

      assertThrowsNotFound(
          ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID,
          () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));
    }
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("updatePurchaseOrder - Controller Permission - Unauthorized")
  void updatePurchaseOrder_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceThrowsUnauthorizedOnUpdate();

    // Act
    ResponseEntity<?> response = controller.updatePurchaseOrder(testPurchaseOrderRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller has @PreAuthorize for updatePurchaseOrder. Expected Result:
   * Annotation exists and includes UPDATE_PURCHASE_ORDERS_PERMISSION. Assertions: Annotation is
   * present and contains permission.
   */
  @Test
  @DisplayName("updatePurchaseOrder - Verify @PreAuthorize Annotation")
  void updatePurchaseOrder_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
    // Arrange
    Method method =
        PurchaseOrderController.class.getMethod(
            "updatePurchaseOrder", PurchaseOrderRequestModel.class);

    // Act
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertNotNull(annotation, "@PreAuthorize annotation should be present on updatePurchaseOrder");
    assertTrue(
        annotation.value().contains(Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION),
        "@PreAuthorize should reference UPDATE_PURCHASE_ORDERS_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service is called once and
   * HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("updatePurchaseOrder - Controller delegates to service")
  void updatePurchaseOrder_WithValidRequest_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceUpdateDoNothing();

    // Act
    ResponseEntity<?> response = controller.updatePurchaseOrder(testPurchaseOrderRequest);

    // Assert
    verify(purchaseOrderServiceMock).updatePurchaseOrder(testPurchaseOrderRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
