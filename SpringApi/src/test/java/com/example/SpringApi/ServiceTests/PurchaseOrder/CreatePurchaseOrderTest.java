package com.example.SpringApi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Resources;
import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test class for PurchaseOrderService.createPurchaseOrder method.
 *
 * <p>Test count: 8 tests - SUCCESS: 3 tests - FAILURE / EXCEPTION: 2 tests - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - CreatePurchaseOrder Tests")
class CreatePurchaseOrderTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 22
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */
  /**
   * Purpose: Verify canonical custom price from request.products[] is persisted. Expected Result:
   * ShipmentProduct allocatedPrice equals product price per unit. Assertions: Captured shipment
   * products include the canonical price.
   */
  @Test
  @DisplayName("Create PO - Persists canonical custom price")
  void createPurchaseOrder_PersistsCanonicalCustomPrice_Success() {
    // Arrange
    stubSuccessfulPurchaseOrderCreate();
    ArgumentCaptor<List<ShipmentProduct>> captor = ArgumentCaptor.forClass((Class) List.class);
    stubShipmentProductRepositorySaveAllCapture(captor);

    // Act
    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

    // Assert
    List<ShipmentProduct> saved = captor.getValue();
    assertNotNull(saved);
    assertFalse(saved.isEmpty());
    assertEquals(
        testPurchaseOrderRequest.getProducts().get(0).getPricePerUnit(),
        saved.get(0).getAllocatedPrice());
  }

  /**
   * Purpose: Verify create succeeds with attachments. Expected Result: Attachments are uploaded and
   * purchase order is created. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Create PO - Success With Attachments")
  void createPurchaseOrder_Success_WithAttachments_Success() {
    // Arrange
    Map<String, String> attachments = new HashMap<>();
    attachments.put("receipt.pdf", "base64-data-here");
    testPurchaseOrderRequest.setAttachments(attachments);

    stubSuccessfulPurchaseOrderCreate();
    stubClientRepositoryFindById(Optional.of(testClient));
    stubResourcesRepositorySave(new Resources());

    List<ImgbbHelper.AttachmentUploadResult> results =
        List.of(
            new ImgbbHelper.AttachmentUploadResult(
                "https://i.ibb.co/test/receipt.pdf", "delete-hash-123", null));

    try (MockedConstruction<ImgbbHelper> ignored = stubImgbbHelperUploadResults(results)) {
      // Act & Assert
      assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
    }
  }

  /**
   * Purpose: Verify create succeeds without attachments. Expected Result: Purchase order is created
   * and logs are written. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Create PO - Success Without Attachments")
  void createPurchaseOrder_Success_WithoutAttachments_Success() {
    // Arrange
    testPurchaseOrderRequest.setAttachments(null);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify null order summary is rejected. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches OrderSummary InvalidRequest.
   */
  @Test
  @DisplayName("Create PO - Null Order Summary - Throws BadRequestException")
  void createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setOrderSummary(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.OrderSummaryErrorMessages.INVALID_REQUEST,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify null request is rejected. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches InvalidRequest.
   */
  @Test
  @DisplayName("Create PO - Null Request - Throws BadRequestException")
  void createPurchaseOrder_NullRequest_ThrowsBadRequestException() {
    // Arrange
    PurchaseOrderRequestModel request = null;

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.INVALID_REQUEST,
        () -> purchaseOrderService.createPurchaseOrder(request));
  }

  /**
   * Purpose: Verify duplicate address is reused instead of creating a new record. Expected Result:
   * Purchase order creation succeeds using existing address id. Assertions: Address save is not
   * required for duplicate path.
   */
  @Test
  @DisplayName("Create PO - Reuse Duplicate Address - Success")
  void createPurchaseOrder_ReuseDuplicateAddress_Success() {
    // Arrange
    stubSuccessfulPurchaseOrderCreate();
    stubAddressRepositoryFindExactDuplicate(Optional.of(testAddress));

    // Act & Assert
    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify missing address data is rejected. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches AddressDataRequired.
   */
  @Test
  @DisplayName("Create PO - Address Missing - Throws BadRequestException")
  void createPurchaseOrder_AddressMissing_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getOrderSummary().setAddress(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.ADDRESS_DATA_REQUIRED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify shipment must include selected courier. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches CourierSelectionRequired.
   */
  @Test
  @DisplayName("Create PO - Courier Missing In Shipment - Throws BadRequestException")
  void createPurchaseOrder_CourierMissingInShipment_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getShipments().get(0).setSelectedCourier(null);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ShipmentErrorMessages.COURIER_SELECTION_REQUIRED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify shipment product must exist in request.products pricing map. Expected Result:
   * BadRequestException is thrown with explicit missing product message. Assertions: Exception
   * message matches expected canonical pricing message.
   */
  @Test
  @DisplayName("Create PO - Shipment Product Missing In Price Map - Throws BadRequestException")
  void createPurchaseOrder_ShipmentProductMissingInPriceMap_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getShipments().get(0).getProducts().get(0).setProductId(999L);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        "Allocated price not found for productId 999. Ensure request.products[] includes this product with pricePerUnit.",
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify each shipment package must include at least one package product. Expected
   * Result: BadRequestException is thrown. Assertions: Exception message matches
   * AtLeastOneProductRequired.
   */
  @Test
  @DisplayName("Create PO - Package Products Empty - Throws BadRequestException")
  void createPurchaseOrder_PackageProductsEmpty_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getShipments().get(0).getPackages().get(0).setProducts(List.of());
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ShipmentPackageProductErrorMessages.AT_LEAST_ONE_PRODUCT_REQUIRED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify each shipment must include at least one package. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches AtLeastOnePackageRequired.
   */
  @Test
  @DisplayName("Create PO - Shipment Packages Empty - Throws BadRequestException")
  void createPurchaseOrder_ShipmentPackagesEmpty_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getShipments().get(0).setPackages(List.of());
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ShipmentPackageErrorMessages.AT_LEAST_ONE_PACKAGE_REQUIRED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify purchase order must include at least one shipment. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches
   * AtLeastOneShipmentRequired.
   */
  @Test
  @DisplayName("Create PO - Shipments Empty - Throws BadRequestException")
  void createPurchaseOrder_ShipmentsEmpty_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setShipments(List.of());
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.AT_LEAST_ONE_SHIPMENT_REQUIRED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify request products cannot be null. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches AtLeastOneProductRequired.
   */
  @Test
  @DisplayName("Create PO - Products Null - Throws BadRequestException")
  void createPurchaseOrder_ProductsNull_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setProducts(null);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        "There should be at least one product, quantity mapping.",
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify request product id must be valid (>0). Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches Product invalid id.
   */
  @Test
  @DisplayName("Create PO - Invalid Product Id In Pricing Map - Throws BadRequestException")
  void createPurchaseOrder_InvalidProductIdInPricingMap_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getProducts().get(0).setProductId(0L);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_ID,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify price per unit cannot be null. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches PricePerUnitRequiredForProductFormat.
   */
  @Test
  @DisplayName("Create PO - Price Per Unit Null - Throws BadRequestException")
  void createPurchaseOrder_PricePerUnitNull_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(null);
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        String.format(
            ErrorMessages.PurchaseOrderErrorMessages.PRICE_PER_UNIT_REQUIRED_FOR_PRODUCT_FORMAT,
            TEST_PRODUCT_ID),
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify price per unit cannot be negative. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches PricePerUnitMustBeNonNegativeForProductFormat.
   */
  @Test
  @DisplayName("Create PO - Price Per Unit Negative - Throws BadRequestException")
  void createPurchaseOrder_PricePerUnitNegative_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(new java.math.BigDecimal("-1"));
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        String.format(
            ErrorMessages.PurchaseOrderErrorMessages
                .PRICE_PER_UNIT_MUST_BE_NON_NEGATIVE_FOR_PRODUCT_FORMAT,
            TEST_PRODUCT_ID),
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify duplicate product ids in pricing map are rejected. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches DuplicateProductIdFormat.
   */
  @Test
  @DisplayName("Create PO - Duplicate Product Id In Pricing Map - Throws BadRequestException")
  void createPurchaseOrder_DuplicateProductIdInPricingMap_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setProducts(
        List.of(
            new com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem(
                TEST_PRODUCT_ID, new java.math.BigDecimal("10"), 1),
            new com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem(
                TEST_PRODUCT_ID, new java.math.BigDecimal("20"), 1)));
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        String.format(
            ErrorMessages.PurchaseOrderErrorMessages.DUPLICATE_PRODUCT_ID_FORMAT, TEST_PRODUCT_ID),
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify attachment validation rejects empty names or payloads. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches InvalidAttachmentData.
   */
  @Test
  @DisplayName("Create PO - Invalid Attachment Data - Throws BadRequestException")
  void createPurchaseOrder_InvalidAttachmentData_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setAttachments(Map.of(" ", " "));
    stubSuccessfulPurchaseOrderCreate();

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.INVALID_ATTACHMENT_DATA,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify existing attachment URLs are stored directly and skip upload processing.
   * Expected Result: Resource record is saved with original URL value. Assertions: Saved resource
   * contains the existing URL.
   */
  @Test
  @DisplayName("Create PO - Existing Attachment URL - Saves Directly")
  void createPurchaseOrder_ExistingAttachmentUrl_SavesDirectly() {
    // Arrange
    String existingUrl = "http://cdn.example.com/invoice.pdf";
    testPurchaseOrderRequest.setAttachments(Map.of("invoice.pdf", existingUrl));
    stubSuccessfulPurchaseOrderCreate();
    ArgumentCaptor<Resources> captor = ArgumentCaptor.forClass(Resources.class);

    // Act
    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

    // Assert
    verify(resourcesRepository, atLeastOnce()).save(captor.capture());
    assertEquals(existingUrl, captor.getValue().getValue());
  }

  /**
   * Purpose: Verify non-ImgBB mode stores base64 payload directly in resources. Expected Result:
   * Resource record is saved with data URI prefix. Assertions: Saved value starts with
   * data:image/png;base64, prefix.
   */
  @Test
  @DisplayName("Create PO - Non ImgBB Attachment Storage - Success")
  void createPurchaseOrder_NonImgbbAttachmentStorage_Success() {
    // Arrange
    testPurchaseOrderRequest.setAttachments(Map.of("photo.png", "abc123"));
    stubSuccessfulPurchaseOrderCreate();
    stubEnvironmentGetProperty("imageLocation", "local");
    ArgumentCaptor<Resources> captor = ArgumentCaptor.forClass(Resources.class);

    // Act
    assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

    // Assert
    verify(resourcesRepository, atLeastOnce()).save(captor.capture());
    assertTrue(captor.getValue().getValue().startsWith("data:image/png;base64,"));
  }

  /**
   * Purpose: Verify ImgBB upload requires configured API key. Expected Result: BadRequestException
   * is thrown. Assertions: Exception message matches ImgBB API key missing error.
   */
  @Test
  @DisplayName("Create PO - ImgBB API Key Missing - Throws BadRequestException")
  void createPurchaseOrder_ImgbbApiKeyMissing_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setAttachments(Map.of("photo.png", "abc123"));
    stubSuccessfulPurchaseOrderCreate();
    stubEnvironmentGetProperty("imageLocation", "imgbb");
    testClient.setImgbbApiKey(null);
    stubClientRepositoryFindById(Optional.of(testClient));

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.PurchaseOrderErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED,
        () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
  }

  /**
   * Purpose: Verify ImgBB upload IO failures are wrapped as BadRequestException. Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches FailedToUploadAttachments
   * format.
   */
  @Test
  @DisplayName("Create PO - ImgBB Upload IOException - Throws BadRequestException")
  void createPurchaseOrder_ImgbbUploadIOException_ThrowsBadRequestException() {
    // Arrange
    testPurchaseOrderRequest.setAttachments(Map.of("photo.png", "abc123"));
    stubSuccessfulPurchaseOrderCreate();
    stubEnvironmentGetProperty("imageLocation", "imgbb");
    testClient.setImgbbApiKey("imgbb-key");
    stubClientRepositoryFindById(Optional.of(testClient));

    try (MockedConstruction<ImgbbHelper> ignored =
        org.mockito.Mockito.mockConstruction(
            ImgbbHelper.class,
            (mock, context) ->
                org.mockito.Mockito.lenient()
                    .when(
                        mock.uploadPurchaseOrderAttachments(
                            anyList(), anyString(), anyString(), anyLong()))
                    .thenThrow(new IOException("io-failed")))) {

      // Act & Assert
      assertThrowsBadRequest(
          String.format(
              ErrorMessages.PurchaseOrderErrorMessages.FAILED_TO_UPLOAD_ATTACHMENTS, "io-failed"),
          () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
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
  @DisplayName("createPurchaseOrder - Controller Permission - Unauthorized")
  void createPurchaseOrder_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceThrowsUnauthorizedOnCreate();

    // Act
    ResponseEntity<?> response = controller.createPurchaseOrder(testPurchaseOrderRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service is called once and
   * HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("createPurchaseOrder - Controller delegates to service")
  void createPurchaseOrder_WithValidRequest_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceCreateDoNothing();

    // Act
    ResponseEntity<?> response = controller.createPurchaseOrder(testPurchaseOrderRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
