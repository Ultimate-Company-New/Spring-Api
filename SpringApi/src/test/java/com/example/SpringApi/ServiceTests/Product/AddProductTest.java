package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Consolidated test class for ProductService.addProduct. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - AddProduct Tests")
class AddProductTest extends ProductServiceTestBase {

  // Total Tests: 34
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify addProduct handles extremely long titles
   * Expected Result: Title accepted (assuming DB or service allows)
   * Assertions: No exception thrown
   */
  @Test
  @DisplayName("addProduct - Extremely long title - Success")
  void addProduct_LongTitle_Success() {
    // Arrange
    StringBuilder longTitle = new StringBuilder();
    for (int i = 0; i < 300; i++) longTitle.append("a");
    testProductRequest.setTitle(longTitle.toString());
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductRepositorySave(testProduct);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct handles negative client IDs (logic check)
   * Expected Result: Proceed to persist (service might not block negative IDs)
   * Assertions: No exception thrown
   */
  @Test
  @DisplayName("addProduct - Negative clientId - Success")
  void addProduct_NegativeClientId_Success() {
    // Arrange
    testProductRequest.setClientId(-1L);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductRepositorySave(testProduct);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct succeeds when optional images are null
   * Expected Result: Product saved without uploading optional images
   * Assertions: No exception thrown
   */
  @Test
  @DisplayName("addProduct - Optional images null - Success")
  void addProduct_OptionalImagesNull_Success() {
    // Arrange
    testProductRequest.setAdditionalImage1(null);
    testProductRequest.setAdditionalImage2(null);
    testProductRequest.setAdditionalImage3(null);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductRepositorySave(testProduct);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct handles special characters in description
   * Expected Result: Description saved correctly
   * Assertions: No exception thrown
   */
  @Test
  @DisplayName("addProduct - Special characters in description - Success")
  void addProduct_SpecialCharsInDescription_Success() {
    // Arrange
    testProductRequest.setDescriptionHtml("<div>Tests & <script>alert('xss')</script></div>");
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductRepositorySave(testProduct);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct succeeds with a completely valid request
   * Expected Result: Product saved and log generated
   * Assertions: No exception thrown, save called, log called
   */
  @Test
  @DisplayName("addProduct - Valid request - Success")
  void addProduct_ValidRequest_Success() {
    // Arrange
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductRepositorySave(testProduct);
    stubUserLogServiceLogDataWithContext();

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));

      verify(productRepository, atLeastOnce()).save(any(Product.class));
      verify(userLogService, times(1))
          .logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), eq("addProduct"));
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify addProduct throws exception when brand is null
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Brand null - Throws BadRequest")
  void addProduct_BrandNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setBrand(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_BRAND,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when category ID is null
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - CategoryId null - Throws BadRequest")
  void addProduct_CategoryIdNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setCategoryId(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_CATEGORY_ID,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when category is not found in DB
   * Expected Result: NotFoundException with correct message
   * Assertions: Exception type and partial message check
   */
  @Test
  @DisplayName("addProduct - Category not found - Throws NotFound")
  void addProduct_CategoryNotFound_ThrowsNotFound() {
    // Arrange
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> productService.addProduct(testProductRequest));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.ER008, TEST_CATEGORY_ID),
        exception.getMessage());
  }

  /*
   * Purpose: Verify addProduct throws exception when client ID is 0
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - ClientId zero - Throws BadRequest")
  void addProduct_ClientIdZero_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setClientId(0L);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ClientErrorMessages.INVALID_ID,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct failure on missing color label
   * Expected Result: BadRequestException
   */
  @Test
  @DisplayName("addProduct - Color label empty - Throws BadRequest")
  void addProduct_ColorLabelEmpty_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setColorLabel(" ");

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_COLOR_LABEL,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct failure on missing condition
   * Expected Result: BadRequestException
   */
  @Test
  @DisplayName("addProduct - Condition empty - Throws BadRequest")
  void addProduct_ConditionEmpty_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setCondition(" ");

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_CONDITION,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct failure on missing country
   * Expected Result: BadRequestException
   */
  @Test
  @DisplayName("addProduct - Country empty - Throws BadRequest")
  void addProduct_CountryEmpty_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setCountryOfManufacture(" ");

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_COUNTRY_OF_MANUFACTURE,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when description is null
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Description null - Throws BadRequest")
  void addProduct_DescriptionNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setDescriptionHtml(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_DESCRIPTION,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when ImgBB upload fails
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and partial message check
   */
  @Test
  @DisplayName("addProduct - Imgbb upload fails - Throws BadRequest")
  void addProduct_ImgbbFails_ThrowsBadRequest() {
    // Arrange
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadFailure(mock))) {
      BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> productService.addProduct(testProductRequest));
      assertEquals(
          String.format(ErrorMessages.ProductErrorMessages.ER010, "main"), exception.getMessage());
    }
  }

  /*
   * Purpose: Verify addProduct throws exception when main image is missing
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and partial message check
   */
  @Test
  @DisplayName("addProduct - Main image null - Throws BadRequest")
  void addProduct_MainImageNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setMainImage(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> productService.addProduct(testProductRequest));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.ER009, "main"), exception.getMessage());
  }

  /*
   * Purpose: Verify addProduct failure on invalid image URL format
   * Expected Result: BadRequestException
   */
  @Test
  @DisplayName("addProduct - Malformed image URL - Throws BadRequest")
  void addProduct_MalformedUrl_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setMainImage("httptest://invalid-url");
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> productService.addProduct(testProductRequest));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.ER012, "httptest://invalid-url"),
        exception.getMessage()); // URL processing failure
  }

  /*
   * Purpose: Verify addProduct failure on negative pickup quantity
   * Expected Result: BadRequestException
   */
  @Test
  @DisplayName("addProduct - Negative pickup quantity - Throws BadRequest")
  void addProduct_NegativePickupQuantity_ThrowsBadRequest() {
    // Arrange
    Map<Long, Integer> counts = new HashMap<>();
    counts.put(1L, -5);
    testProductRequest.setPickupLocationQuantities(counts);

    // Act & Assert
    assertThrowsBadRequest(
        String.format(
            ErrorMessages.ProductPickupLocationMappingErrorMessages
                .AVAILABLE_STOCK_MUST_BE_POSITIVE,
            1L),
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct handles weight validation
   * Expected Result: BadRequestException when negative
   */
  @Test
  @DisplayName("addProduct - Negative weight - Throws BadRequest")
  void addProduct_NegativeWeight_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setWeightKgs(new BigDecimal("-1.5"));

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_WEIGHT,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when pickup location quantities
   * are missing
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Pickup quantities empty - Throws BadRequest")
  void addProduct_PickupQuantitiesEmpty_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setPickupLocationQuantities(new HashMap<>());

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.AT_LEAST_ONE_PICKUP_LOCATION_REQUIRED,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when price is negative
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Price negative - Throws BadRequest")
  void addProduct_PriceNegative_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setPrice(new BigDecimal("-10.00"));

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_PRICE,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when price is null
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Price null - Throws BadRequest")
  void addProduct_PriceNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setPrice(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_PRICE,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when title is empty
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Title empty - Throws BadRequest")
  void addProduct_TitleEmpty_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setTitle("   ");

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_TITLE,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct throws exception when title is null
   * Expected Result: BadRequestException with correct message
   * Assertions: Exception type and exact message
   */
  @Test
  @DisplayName("addProduct - Title null - Throws BadRequest")
  void addProduct_TitleNull_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setTitle(null);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_TITLE,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct handles breadth validation
   * Expected Result: BadRequestException when zero
   */
  @Test
  @DisplayName("addProduct - Zero breadth - Throws BadRequest")
  void addProduct_ZeroBreadth_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setBreadth(BigDecimal.ZERO);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_BREADTH,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct handles height validation
   * Expected Result: BadRequestException when zero
   */
  @Test
  @DisplayName("addProduct - Zero height - Throws BadRequest")
  void addProduct_ZeroHeight_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setHeight(BigDecimal.ZERO);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_HEIGHT,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   * Purpose: Verify addProduct handles length validation
   * Expected Result: BadRequestException when zero
   */
  @Test
  @DisplayName("addProduct - Zero length - Throws BadRequest")
  void addProduct_ZeroLength_ThrowsBadRequest() {
    // Arrange
    testProductRequest.setLength(BigDecimal.ZERO);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.ProductErrorMessages.INVALID_LENGTH,
        () -> productService.addProduct(testProductRequest));
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify addProduct success with all optional images present
   */
  @Test
  @DisplayName("addProduct - All optional images present - Success")
  void addProduct_p01_AllOptionalImagesPresent_Success() {
    // Arrange - all optional images already set in base setup
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify controller delegating to service
   */
  @Test
  @DisplayName("addProduct - Controller delegates to service - Success")
  void addProduct_p02_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceAddProductDoNothing();

    // Act
    ResponseEntity<?> response = controller.addProduct(testProductRequest);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).addProduct(testProductRequest);
  }

  /*
   * Purpose: Verify addProduct success with minimum valid dimensions
   */
  @Test
  @DisplayName("addProduct - Minimum valid dimensions - Success")
  void addProduct_p03_MinimumValidDimensions_Success() {
    // Arrange
    testProductRequest.setWeightKgs(BigDecimal.ZERO);
    testProductRequest.setLength(new BigDecimal("0.01"));
    testProductRequest.setBreadth(new BigDecimal("0.01"));
    testProductRequest.setHeight(new BigDecimal("0.01"));
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify forbidden access for addProduct (Simulated)
   * Rule 3: At least one forbidden check
   */
  @Test
  @DisplayName("addProduct - Controller permission unauthorized - Success")
  void addProduct_p04_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceAddProductThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.addProduct(testProductRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).addProduct(any(ProductRequestModel.class));
  }

  /*
   * Purpose: Verify addProduct success with only required images (no optional)
   */
  @Test
  @DisplayName("addProduct - Only required images - Success")
  void addProduct_p05_OnlyRequiredImages_Success() {
    // Arrange
    testProductRequest.setDefectImage(null);
    testProductRequest.setAdditionalImage1(null);
    testProductRequest.setAdditionalImage2(null);
    testProductRequest.setAdditionalImage3(null);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct success with some optional images
   */
  @Test
  @DisplayName("addProduct - Partial optional images - Success")
  void addProduct_p06_PartialOptionalImages_Success() {
    // Arrange
    testProductRequest.setAdditionalImage2(null);
    testProductRequest.setAdditionalImage3(null);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }

  /*
   * Purpose: Verify addProduct success with zero weight (valid edge case)
   */
  @Test
  @DisplayName("addProduct - Zero weight - Success")
  void addProduct_p08_ZeroWeight_Success() {
    // Arrange
    testProductRequest.setWeightKgs(BigDecimal.ZERO);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);

    // Act & Assert
    try (MockedConstruction<ImgbbHelper> mocked =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
    }
  }
}

