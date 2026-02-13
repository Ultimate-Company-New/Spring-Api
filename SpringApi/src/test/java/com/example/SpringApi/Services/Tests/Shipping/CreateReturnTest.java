package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import com.example.SpringApi.Models.RequestModels.CreateReturnRequestModel;
import com.example.SpringApi.Models.ResponseModels.ReturnShipmentResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.createReturn().
 */
@DisplayName("CreateReturn Tests")
class CreateReturnTest extends ShippingServiceTestBase {


    // Total Tests: 30
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */
/**
     * Purpose: Verify AWB assignment failure is ignored.
     * Expected Result: ReturnShipmentResponseModel returned.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("createReturn - AWB Assign Failure - Success")
    void createReturn_AwbAssignFailure_Success() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);
        stubReturnShipmentRepositorySave(testReturnShipment);
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubReturnShipmentProductRepositorySave(new ReturnShipmentProduct());
        stubShipRocketHelperCreateReturnOrderAsJson("{\"order_id\":1,\"shipment_id\":2}");
        stubShipRocketHelperAssignReturnAwbAsJsonThrows(
                new RuntimeException(ErrorMessages.CommonErrorMessages.CriticalFailure));

        // Act
        ReturnShipmentResponseModel result = shippingService.createReturn(createReturnRequest);

        // Assert
        assertNotNull(result);
    }
/**
         * Purpose: Verify delivered date null still allows return.
         * Expected Result: ReturnShipmentResponseModel returned.
         * Assertions: Response is not null.
         */
        @Test
        @DisplayName("createReturn - Delivered Date Null - Success")
        void createReturn_DeliveredDateNull_Success() {
                // Arrange
                testShipment.setShipRocketStatus("DELIVERED");
                testShipment.setDeliveredDate(null);
                testShipment.setShipmentProducts(List.of(testShipmentProduct));
                stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
                stubClientServiceGetClientById(testClientResponse);
                stubProductRepositoryFindById(testProduct);
                stubReturnShipmentRepositorySave(testReturnShipment);
                stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
                stubReturnShipmentProductRepositorySave(new ReturnShipmentProduct());
                stubShipRocketHelperCreateReturnOrderAsJson("{\"order_id\":1,\"shipment_id\":2}");
                stubShipRocketHelperAssignReturnAwbAsJson(createValidAwbJson());

                // Act
                ReturnShipmentResponseModel result = shippingService.createReturn(createReturnRequest);

                // Assert
                assertNotNull(result);
        }
/**
     * Purpose: Verify full return succeeds.
     * Expected Result: ReturnShipmentResponseModel returned.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("createReturn - Full Return - Success")
    void createReturn_FullReturn_Success() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        createReturnRequest.getProducts().get(0).setQuantity(2);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);
        stubReturnShipmentRepositorySave(testReturnShipment);
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubReturnShipmentProductRepositorySave(new ReturnShipmentProduct());
        stubShipRocketHelperCreateReturnOrderAsJson("{\"order_id\":1,\"shipment_id\":2}");
        stubShipRocketHelperAssignReturnAwbAsJson(createValidAwbJson());

        // Act
        ReturnShipmentResponseModel result = shippingService.createReturn(createReturnRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_RETURN_SHIPMENT_ID, result.getReturnShipmentId());
    }
/**
         * Purpose: Verify multiple valid items return succeeds.
         * Expected Result: ReturnShipmentResponseModel returned.
         * Assertions: Response is not null.
         */
        @Test
        @DisplayName("createReturn - Multiple Items - Success")
        void createReturn_MultipleItems_Success() {
                // Arrange
                Product secondProduct = new Product();
                secondProduct.setProductId(999L);
                secondProduct.setTitle("Second Product");
                secondProduct.setPrice(new java.math.BigDecimal("50"));
                secondProduct.setDiscount(java.math.BigDecimal.ZERO);
                secondProduct.setReturnWindowDays(7);
                ShipmentProduct secondShipmentProduct = new ShipmentProduct();
                secondShipmentProduct.setShipmentId(TEST_SHIPMENT_ID);
                secondShipmentProduct.setProductId(999L);
                secondShipmentProduct.setAllocatedQuantity(1);
                CreateReturnRequestModel.ReturnProductItem item2 = new CreateReturnRequestModel.ReturnProductItem();
                item2.setProductId(999L);
                item2.setQuantity(1);
                item2.setReason("Damaged");
                createReturnRequest.setProducts(List.of(createReturnRequest.getProducts().get(0), item2));
                testShipment.setShipRocketStatus("DELIVERED");
                testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
                testShipment.setShipmentProducts(List.of(testShipmentProduct, secondShipmentProduct));
                stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
                stubClientServiceGetClientById(testClientResponse);
                stubProductRepositoryFindById(testProduct);
                stubReturnShipmentRepositorySave(testReturnShipment);
                stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
                stubReturnShipmentProductRepositorySave(new ReturnShipmentProduct());
                stubShipRocketHelperCreateReturnOrderAsJson("{\"order_id\":1,\"shipment_id\":2}");
                stubShipRocketHelperAssignReturnAwbAsJson(createValidAwbJson());

                // Act
                ReturnShipmentResponseModel result = shippingService.createReturn(createReturnRequest);

                // Assert
                assertNotNull(result);
        }
/**
     * Purpose: Verify partial return succeeds.
     * Expected Result: ReturnShipmentResponseModel returned.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("createReturn - Partial Return - Success")
    void createReturn_PartialReturn_Success() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        createReturnRequest.getProducts().get(0).setQuantity(1);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);
        stubReturnShipmentRepositorySave(testReturnShipment);
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubReturnShipmentProductRepositorySave(new ReturnShipmentProduct());
        stubShipRocketHelperCreateReturnOrderAsJson("{\"order_id\":1,\"shipment_id\":2}");
        stubShipRocketHelperAssignReturnAwbAsJson(createValidAwbJson());

        // Act
        ReturnShipmentResponseModel result = shippingService.createReturn(createReturnRequest);

        // Assert
        assertNotNull(result);
    }
/*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */
/**
     * Purpose: Verify missing ShipRocket credentials throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Credentials Missing - Throws BadRequestException")
    void createReturn_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        testClientResponse.setShipRocketEmail(null);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
    }
/**
     * Purpose: Verify non-delivered shipment throws BadRequestException.
     * Expected Result: BadRequestException with OnlyDeliveredCanReturn message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Not Delivered - Throws BadRequestException")
    void createReturn_NotDelivered_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("NEW");
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.OnlyDeliveredCanReturn, "NEW"),
                ex.getMessage());
    }
/**
     * Purpose: Verify return window exceeded throws BadRequestException.
     * Expected Result: BadRequestException with ProductPastReturnWindow message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Past Return Window - Throws BadRequestException")
    void createReturn_PastReturnWindow_ThrowsBadRequestException() {
        // Arrange
        testProduct.setReturnWindowDays(1);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(10));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductPastReturnWindow,
                testProduct.getTitle(), testProduct.getReturnWindowDays()), ex.getMessage());
    }
/**
     * Purpose: Verify null productId throws BadRequestException.
     * Expected Result: BadRequestException with ProductIdRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - ProductId Null - Throws BadRequestException")
    void createReturn_ProductIdNull_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setProductId(null);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ProductIdRequired, ex.getMessage());
    }
/**
     * Purpose: Verify product not found throws NotFoundException.
     * Expected Result: NotFoundException with ProductErrorMessages.ER013 message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Product Not Found - Throws NotFoundException")
    void createReturn_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), ex.getMessage());
    }
/**
     * Purpose: Verify product not in shipment throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotInShipment message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Product Not In Shipment - Throws BadRequestException")
    void createReturn_ProductNotInShipment_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setProductId(999L);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotInShipment, 999L),
                ex.getMessage());
    }
/**
     * Purpose: Verify product not returnable throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotReturnable message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Product Not Returnable - Throws BadRequestException")
    void createReturn_ProductNotReturnable_ThrowsBadRequestException() {
        // Arrange
        testProduct.setReturnWindowDays(0);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotReturnable, testProduct.getTitle()),
                ex.getMessage());
    }
/**
     * Purpose: Verify empty products list throws BadRequestException.
     * Expected Result: BadRequestException with AtLeastOneProductRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Products Empty - Throws BadRequestException")
    void createReturn_ProductsEmpty_ThrowsBadRequestException() {
        // Arrange
        createReturnRequest.setProducts(List.of());

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.AtLeastOneProductRequired, ex.getMessage());
    }
/**
     * Purpose: Verify null products list throws BadRequestException.
     * Expected Result: BadRequestException with AtLeastOneProductRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Products Null - Throws BadRequestException")
    void createReturn_ProductsNull_ThrowsBadRequestException() {
        // Arrange
        createReturnRequest.setProducts(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.AtLeastOneProductRequired, ex.getMessage());
    }
/**
     * Purpose: Verify return quantity exceeds throws BadRequestException.
     * Expected Result: BadRequestException with ReturnQuantityExceeds message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Quantity Exceeds - Throws BadRequestException")
    void createReturn_QuantityExceeds_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setQuantity(99);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ReturnQuantityExceeds,
                99, testShipmentProduct.getAllocatedQuantity(), TEST_PRODUCT_ID), ex.getMessage());
    }
/**
     * Purpose: Verify negative quantity throws BadRequestException.
     * Expected Result: BadRequestException with ValidQuantityRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Quantity Negative - Throws BadRequestException")
    void createReturn_QuantityNegative_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setQuantity(-1);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ValidQuantityRequired, ex.getMessage());
    }
/**
     * Purpose: Verify null quantity throws BadRequestException.
     * Expected Result: BadRequestException with ValidQuantityRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Quantity Null - Throws BadRequestException")
    void createReturn_QuantityNull_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setQuantity(null);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ValidQuantityRequired, ex.getMessage());
    }
/**
     * Purpose: Verify quantity zero throws BadRequestException.
     * Expected Result: BadRequestException with ValidQuantityRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Quantity Zero - Throws BadRequestException")
    void createReturn_QuantityZero_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setQuantity(0);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ValidQuantityRequired, ex.getMessage());
    }
/**
     * Purpose: Verify empty reason throws BadRequestException.
     * Expected Result: BadRequestException with ReturnReasonRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Reason Empty - Throws BadRequestException")
    void createReturn_ReasonEmpty_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setReason("");
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ReturnReasonRequired, ex.getMessage());
    }
/**
     * Purpose: Verify null reason throws BadRequestException.
     * Expected Result: BadRequestException with ReturnReasonRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Reason Null - Throws BadRequestException")
    void createReturn_ReasonNull_ThrowsBadRequestException() {
        // Arrange
        CreateReturnRequestModel.ReturnProductItem item = createReturnRequest.getProducts().get(0);
        item.setReason(null);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ReturnReasonRequired, ex.getMessage());
    }
/**
     * Purpose: Verify null return window days throws BadRequestException.
     * Expected Result: BadRequestException with ProductNotReturnable message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Return Window Null - Throws BadRequestException")
    void createReturn_ReturnWindowNull_ThrowsBadRequestException() {
        // Arrange
        testProduct.setReturnWindowDays(null);
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotReturnable, testProduct.getTitle()),
                ex.getMessage());
    }
/**
         * Purpose: Verify second item with null productId throws BadRequestException.
         * Expected Result: BadRequestException with ProductIdRequired message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("createReturn - Second Item ProductId Null - Throws BadRequestException")
        void createReturn_SecondItemProductIdNull_ThrowsBadRequestException() {
                // Arrange
                CreateReturnRequestModel.ReturnProductItem item2 = new CreateReturnRequestModel.ReturnProductItem();
                item2.setProductId(null);
                item2.setQuantity(1);
                item2.setReason("Damaged");
                createReturnRequest.setProducts(List.of(createReturnRequest.getProducts().get(0), item2));
                testShipment.setShipRocketStatus("DELIVERED");
                testShipment.setShipmentProducts(List.of(testShipmentProduct));
                stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
                stubClientServiceGetClientById(testClientResponse);
                stubProductRepositoryFindById(testProduct);

                // Act
                com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                        com.example.SpringApi.Exceptions.BadRequestException.class,
                        () -> shippingService.createReturn(createReturnRequest));

                // Assert
                assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ProductIdRequired, ex.getMessage());
        }
/**
         * Purpose: Verify second item with null quantity throws BadRequestException.
         * Expected Result: BadRequestException with ValidQuantityRequired message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("createReturn - Second Item Quantity Null - Throws BadRequestException")
        void createReturn_SecondItemQuantityNull_ThrowsBadRequestException() {
                // Arrange
                CreateReturnRequestModel.ReturnProductItem item2 = new CreateReturnRequestModel.ReturnProductItem();
                item2.setProductId(999L);
                item2.setQuantity(null);
                item2.setReason("Damaged");
                createReturnRequest.setProducts(List.of(createReturnRequest.getProducts().get(0), item2));
                testShipment.setShipRocketStatus("DELIVERED");
                testShipment.setShipmentProducts(List.of(testShipmentProduct));
                stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
                stubClientServiceGetClientById(testClientResponse);
                stubProductRepositoryFindById(testProduct);

                // Act
                com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                        com.example.SpringApi.Exceptions.BadRequestException.class,
                        () -> shippingService.createReturn(createReturnRequest));

                // Assert
                assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ValidQuantityRequired, ex.getMessage());
        }
/**
         * Purpose: Verify second item with null reason throws BadRequestException.
         * Expected Result: BadRequestException with ReturnReasonRequired message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("createReturn - Second Item Reason Null - Throws BadRequestException")
        void createReturn_SecondItemReasonNull_ThrowsBadRequestException() {
                // Arrange
                CreateReturnRequestModel.ReturnProductItem item2 = new CreateReturnRequestModel.ReturnProductItem();
                item2.setProductId(999L);
                item2.setQuantity(1);
                item2.setReason(null);
                createReturnRequest.setProducts(List.of(createReturnRequest.getProducts().get(0), item2));
                testShipment.setShipRocketStatus("DELIVERED");
                testShipment.setShipmentProducts(List.of(testShipmentProduct));
                stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
                stubClientServiceGetClientById(testClientResponse);
                stubProductRepositoryFindById(testProduct);

                // Act
                com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                        com.example.SpringApi.Exceptions.BadRequestException.class,
                        () -> shippingService.createReturn(createReturnRequest));

                // Assert
                assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ReturnReasonRequired, ex.getMessage());
        }
/**
     * Purpose: Verify null shipmentId throws BadRequestException.
     * Expected Result: BadRequestException with ShipmentIdRequired message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - ShipmentId Null - Throws BadRequestException")
    void createReturn_ShipmentIdNull_ThrowsBadRequestException() {
        // Arrange
        createReturnRequest.setShipmentId(null);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ShipmentIdRequired, ex.getMessage());
    }
/**
     * Purpose: Verify shipment not found throws NotFoundException.
     * Expected Result: NotFoundException with NotFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Shipment Not Found - Throws NotFoundException")
    void createReturn_ShipmentNotFound_ThrowsNotFoundException() {
        // Arrange
        stubShipmentRepositoryFindByShipmentIdAndClientId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID), ex.getMessage());
    }
/**
     * Purpose: Verify shipment products null triggers ProductNotInShipment.
     * Expected Result: BadRequestException with ProductNotInShipment message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - Shipment Products Null - Throws BadRequestException")
    void createReturn_ShipmentProductsNull_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setShipmentProducts(null);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotInShipment, TEST_PRODUCT_ID),
                ex.getMessage());
    }
/**
     * Purpose: Verify ShipRocket create return failure throws BadRequestException.
     * Expected Result: BadRequestException with FailedToCreateReturn message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("createReturn - ShipRocket Failure - Throws BadRequestException")
    void createReturn_ShipRocketFailure_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("DELIVERED");
        testShipment.setDeliveredDate(LocalDateTime.now().minusDays(1));
        testShipment.setShipmentProducts(List.of(testShipmentProduct));
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubProductRepositoryFindById(testProduct);
        stubShipRocketHelperCreateReturnOrderAsJsonThrows(new RuntimeException(ErrorMessages.OPERATION_FAILED));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.createReturn(createReturnRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.FailedToCreateReturn,
                ErrorMessages.OPERATION_FAILED), ex.getMessage());
    }
/*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

        /**
         * Purpose: Verify unauthorized access is blocked at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("createReturn - Controller Permission - Unauthorized")
        void createReturn_controller_permission_unauthorized() {
                // Arrange
                ShippingController controller = new ShippingController(shippingServiceMock);
                stubShippingServiceMockCreateReturnUnauthorized();

                // Act
                ResponseEntity<?> response = controller.createReturn(createReturnRequest);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

    /**
     * Purpose: Verify controller has @PreAuthorize for createReturn.
     * Expected Result: Annotation exists and includes MODIFY_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("createReturn - Verify @PreAuthorize Annotation")
    void createReturn_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("createReturn", CreateReturnRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.MODIFY_SHIPMENTS_PERMISSION));
    }
}
