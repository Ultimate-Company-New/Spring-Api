package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Consolidated test class for ProductService.editProduct. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - EditProduct Tests")
class EditProductTest extends ProductServiceTestBase {

  // Total Tests: 12
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify editProduct succeeds when optional images are null. Expected Result: Update
   * completes without error. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("editProduct - Optional images null - Success")
  void editProduct_OptionalImagesNull_Success() {
    // Arrange
    testProductRequest.setAdditionalImage1(null);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);
    stubProductPickupLocationMappingRepositoryDeleteByProductId(TEST_PRODUCT_ID);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.editProduct(testProductRequest));
    }
  }

  /**
   * Purpose: Verify image upload is skipped if image is already a URL. Expected Result: ImgbbHelper
   * not invoked for unchanged URLs. Assertions: Upload method not called.
   */
  @Test
  @DisplayName("editProduct - Skip upload for URL images - Success")
  void editProduct_SkipUploadForUrl_Success() {
    // Arrange
    final String EXISTING_URL = "https://i.ibb.co/test/image.png";

    // Set all images to the same URL in request and entity to skip upload
    testProductRequest.setMainImage(EXISTING_URL);
    testProductRequest.setTopImage(EXISTING_URL);
    testProductRequest.setBottomImage(EXISTING_URL);
    testProductRequest.setFrontImage(EXISTING_URL);
    testProductRequest.setBackImage(EXISTING_URL);
    testProductRequest.setRightImage(EXISTING_URL);
    testProductRequest.setLeftImage(EXISTING_URL);
    testProductRequest.setDetailsImage(EXISTING_URL);
    testProductRequest.setDefectImage(EXISTING_URL);
    testProductRequest.setAdditionalImage1(EXISTING_URL);
    testProductRequest.setAdditionalImage2(EXISTING_URL);
    testProductRequest.setAdditionalImage3(EXISTING_URL);

    testProduct.setMainImageUrl(EXISTING_URL);
    testProduct.setTopImageUrl(EXISTING_URL);
    testProduct.setBottomImageUrl(EXISTING_URL);
    testProduct.setFrontImageUrl(EXISTING_URL);
    testProduct.setBackImageUrl(EXISTING_URL);
    testProduct.setRightImageUrl(EXISTING_URL);
    testProduct.setLeftImageUrl(EXISTING_URL);
    testProduct.setDetailsImageUrl(EXISTING_URL);
    testProduct.setDefectImageUrl(EXISTING_URL);
    testProduct.setAdditionalImage1Url(EXISTING_URL);
    testProduct.setAdditionalImage2Url(EXISTING_URL);
    testProduct.setAdditionalImage3Url(EXISTING_URL);

    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);
    stubProductPickupLocationMappingRepositoryDeleteByProductId(TEST_PRODUCT_ID);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.editProduct(testProductRequest));

      // Assert
      if (!mocked.constructed().isEmpty()) {
        verify(mocked.constructed().get(0), never()).uploadFileToImgbb(anyString(), anyString());
      }
    }
  }

  /**
   * Purpose: Verify editProduct succeeds with a completely valid request. Expected Result: Product
   * updated and logged. Assertions: Repository save and logging called.
   */
  @Test
  @DisplayName("editProduct - Valid request - Success")
  void editProduct_ValidRequest_Success() {
    // Arrange
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);
    stubProductPickupLocationMappingRepositoryDeleteByProductId(TEST_PRODUCT_ID);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.editProduct(testProductRequest));

      verify(productRepository, times(1))
          .findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
      verify(productRepository, atLeastOnce()).save(any(Product.class));
      verify(userLogService, times(1)).logData(anyLong(), anyString(), eq("editProduct"));
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify failure on invalid brand during edit. Expected Result: BadRequestException
   * thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Brand invalid - Throws BadRequest")
  void editProduct_BrandInvalid_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setBrand("");
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_BRAND,
        () -> productService.editProduct(testProductRequest));
  }

  /**
   * Purpose: Verify failure when malformed Base64 image provided. Expected Result:
   * BadRequestException thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Malformed image Base64 - Throws BadRequest")
  void editProduct_MalformedBase64_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setMainImage("not-a-base64-string");
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> productService.editProduct(testProductRequest));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.ER010, "main"), exception.getMessage());
  }

  /**
   * Purpose: Verify failure when price is negative during edit. Expected Result:
   * BadRequestException thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Negative price - Throws BadRequest")
  void editProduct_NegativePrice_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setPrice(new BigDecimal("-1.00"));
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_PRICE,
        () -> productService.editProduct(testProductRequest));
  }

  /**
   * Purpose: Verify failure when weight is negative during edit. Expected Result:
   * BadRequestException thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Negative weight - Throws BadRequest")
  void editProduct_NegativeWeight_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setWeightKgs(new BigDecimal("-5.0"));
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_WEIGHT,
        () -> productService.editProduct(testProductRequest));
  }

  /**
   * Purpose: Verify failure when product is not found. Expected Result: NotFoundException thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Product not found - Throws NotFound")
  void editProduct_NotFound_ThrowsNotFound() {
    // Arrange
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
        () -> productService.editProduct(testProductRequest));
  }

  /**
   * Purpose: Verify failure when product ID is null. Expected Result: BadRequestException thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("editProduct - Null product ID - Throws BadRequest")
  void editProduct_NullId_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setProductId(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_ID,
        () -> productService.editProduct(testProductRequest));
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("editProduct - Controller permission unauthorized - Success")
  void editProduct_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceEditProductThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.editProduct(testProductRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).editProduct(any(ProductRequestModel.class));
  }

  /**
   * Purpose: Verify controller delegation to service. Expected Result: OK status returned.
   * Assertions: Service method invoked and response is OK.
   */
  @Test
  @DisplayName("editProduct - Controller delegation check - Success")
  void editProduct_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceEditProductDoNothing();

    // Act
    ResponseEntity<?> response = controller.editProduct(testProductRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).editProduct(testProductRequest);
  }
}

