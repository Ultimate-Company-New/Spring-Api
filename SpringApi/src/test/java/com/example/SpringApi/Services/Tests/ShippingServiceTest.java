package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Helpers.PackagingHelper;
import com.example.SpringApi.Services.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShippingService.
 * 
 * Note: ShippingService extends BaseService which requires Spring Security context.
 * Tests focus on validation logic and behaviors that don't require security context,
 * or verify the correct exceptions are thrown.
 * 
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | CalculateShippingValidationTests        | 9               |
 * | OptimizeOrderValidationTests            | 9               |
 * | CancelShipmentValidationTests           | 14              |
 * | GetWalletBalanceValidationTests         | 7               |
 * | **Total**                               | **39**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingService Tests")
class ShippingServiceTest {

    @Mock
    private ClientService clientService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    private PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    private PackagingHelper packagingHelper;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ReturnShipmentRepository returnShipmentRepository;

    @Mock
    private ReturnShipmentProductRepository returnShipmentProductRepository;

    @InjectMocks
    private ShippingService shippingService;

    private ShippingCalculationRequestModel testShippingRequest;
    private OrderOptimizationRequestModel testOptimizationRequest;
    private Client testClient;

    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1L;
    private static final Long TEST_PICKUP_LOCATION_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test client
        testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setShipRocketEmail("test@shiprocket.com");
        testClient.setShipRocketPassword("password");

        // Initialize test shipping request
        testShippingRequest = new ShippingCalculationRequestModel();
        testShippingRequest.setDeliveryPostcode("400001");
        testShippingRequest.setIsCod(false);

        ShippingCalculationRequestModel.PickupLocationShipment pickupLocationShipment = new ShippingCalculationRequestModel.PickupLocationShipment();
        pickupLocationShipment.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        pickupLocationShipment.setTotalWeightKgs(new BigDecimal("5.00"));
        pickupLocationShipment.setTotalQuantity(5);
        testShippingRequest.setPickupLocations(Collections.singletonList(pickupLocationShipment));

        // Initialize test optimization request
        testOptimizationRequest = new OrderOptimizationRequestModel();
        testOptimizationRequest.setDeliveryPostcode("400001");
        testOptimizationRequest.setIsCod(false);

        Map<Long, Integer> productQuantities = new HashMap<>();
        productQuantities.put(TEST_PRODUCT_ID, 5);
        testOptimizationRequest.setProductQuantities(productQuantities);
    }

    @Nested
    @DisplayName("CalculateShipping Validation Tests")
    class CalculateShippingValidationTests {

        /**
         * Purpose: Verify that null request throws NullPointerException.
         * Expected Result: NullPointerException is thrown.
         * Assertions: assertThrows(NullPointerException.class, ...);
         */
        @Test
        @DisplayName("Calculate Shipping - Null Request - Throws NullPointerException")
        void calculateShipping_NullRequest_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.calculateShipping(null));
        }

        /**
         * Purpose: Verify that null pickup locations returns empty response.
         * Expected Result: Empty ShippingCalculationResponseModel with empty locationOptions.
         * Assertions: assertNotNull(result); assertTrue(result.getLocationOptions().isEmpty());
         */
        @Test
        @DisplayName("Calculate Shipping - Null Pickup Locations - Returns Empty Response")
        void calculateShipping_NullPickupLocations_ReturnsEmptyResponse() {
            testShippingRequest.setPickupLocations(null);

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
            assertNotNull(result.getLocationOptions());
            assertTrue(result.getLocationOptions().isEmpty());
        }

        /**
         * Purpose: Verify that empty pickup locations returns empty response.
         * Expected Result: Empty ShippingCalculationResponseModel with empty locationOptions.
         * Assertions: assertNotNull(result); assertTrue(result.getLocationOptions().isEmpty());
         */
        @Test
        @DisplayName("Calculate Shipping - Empty Pickup Locations - Returns Empty Response")
        void calculateShipping_EmptyPickupLocations_ReturnsEmptyResponse() {
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
            assertNotNull(result.getLocationOptions());
            assertTrue(result.getLocationOptions().isEmpty());
        }

        /**
         * Purpose: Verify that null delivery postcode with empty locations works.
         * Expected Result: Empty response returned.
         * Assertions: assertNotNull(result);
         */
        @Test
        @DisplayName("Calculate Shipping - Null Postcode Empty Locations - Returns Empty Response")
        void calculateShipping_NullPostcodeEmptyLocations_ReturnsEmptyResponse() {
            testShippingRequest.setDeliveryPostcode(null);
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
            assertTrue(result.getLocationOptions().isEmpty());
        }

        /**
         * Purpose: Verify that empty postcode with empty locations works.
         * Expected Result: Empty response returned.
         * Assertions: assertNotNull(result);
         */
        @Test
        @DisplayName("Calculate Shipping - Empty Postcode Empty Locations - Returns Empty Response")
        void calculateShipping_EmptyPostcodeEmptyLocations_ReturnsEmptyResponse() {
            testShippingRequest.setDeliveryPostcode("");
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
            assertTrue(result.getLocationOptions().isEmpty());
        }

        /**
         * Purpose: Verify that result is not null for valid empty request.
         * Expected Result: Non-null response object.
         * Assertions: assertNotNull(result);
         */
        @Test
        @DisplayName("Calculate Shipping - Valid Empty Request - Returns Non-Null Response")
        void calculateShipping_ValidEmptyRequest_ReturnsNonNullResponse() {
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
        }

        /**
         * Purpose: Verify that location options list is initialized.
         * Expected Result: locationOptions list is not null.
         * Assertions: assertNotNull(result.getLocationOptions());
         */
        @Test
        @DisplayName("Calculate Shipping - Location Options Initialized")
        void calculateShipping_LocationOptionsInitialized() {
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result.getLocationOptions());
        }

        /**
         * Purpose: Verify multiple calls with null locations returns empty.
         * Expected Result: Each call returns empty response.
         * Assertions: assertTrue(result.getLocationOptions().isEmpty());
         */
        @Test
        @DisplayName("Calculate Shipping - Multiple Calls Null Locations")
        void calculateShipping_MultipleCallsNullLocations() {
            testShippingRequest.setPickupLocations(null);

            ShippingCalculationResponseModel result1 = shippingService.calculateShipping(testShippingRequest);
            ShippingCalculationResponseModel result2 = shippingService.calculateShipping(testShippingRequest);

            assertTrue(result1.getLocationOptions().isEmpty());
            assertTrue(result2.getLocationOptions().isEmpty());
        }

        /**
         * Purpose: Verify total shipping cost is zero for empty locations.
         * Expected Result: totalShippingCost is zero or null.
         * Assertions: Result has no cost set.
         */
        @Test
        @DisplayName("Calculate Shipping - Total Cost Zero For Empty Locations")
        void calculateShipping_TotalCostZeroForEmptyLocations() {
            testShippingRequest.setPickupLocations(Collections.emptyList());

            ShippingCalculationResponseModel result = shippingService.calculateShipping(testShippingRequest);

            assertNotNull(result);
            // Total shipping cost should be zero for empty locations
            assertTrue(result.getTotalShippingCost() == null || 
                       result.getTotalShippingCost().compareTo(BigDecimal.ZERO) == 0);
        }
    }

    @Nested
    @DisplayName("OptimizeOrder Validation Tests")
    class OptimizeOrderValidationTests {

        /**
         * Purpose: Verify that null request throws NullPointerException.
         * Expected Result: NullPointerException is thrown.
         * Assertions: assertThrows(NullPointerException.class, ...);
         */
        @Test
        @DisplayName("Optimize Order - Null Request - Throws NullPointerException")
        void optimizeOrder_NullRequest_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.optimizeOrder(null));
        }

        /**
         * Purpose: Verify that null product quantities returns error response.
         * Expected Result: Response with success=false.
         * Assertions: assertFalse(result.getSuccess());
         */
        @Test
        @DisplayName("Optimize Order - Null Product Quantities - Returns Error Response")
        void optimizeOrder_NullProductQuantities_ReturnsErrorResponse() {
            testOptimizationRequest.setProductQuantities(null);

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertNotNull(result.getErrorMessage());
        }

        /**
         * Purpose: Verify that empty product quantities returns error response.
         * Expected Result: Response with success=false.
         * Assertions: assertFalse(result.getSuccess());
         */
        @Test
        @DisplayName("Optimize Order - Empty Product Quantities - Returns Error Response")
        void optimizeOrder_EmptyProductQuantities_ReturnsErrorResponse() {
            testOptimizationRequest.setProductQuantities(Collections.emptyMap());

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertNotNull(result.getErrorMessage());
        }

        /**
         * Purpose: Verify that null delivery postcode returns error response.
         * Expected Result: Response with success=false.
         * Assertions: assertFalse(result.getSuccess());
         */
        @Test
        @DisplayName("Optimize Order - Null Delivery Postcode - Returns Error Response")
        void optimizeOrder_NullDeliveryPostcode_ReturnsErrorResponse() {
            testOptimizationRequest.setDeliveryPostcode(null);

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertNotNull(result.getErrorMessage());
        }

        /**
         * Purpose: Verify that empty delivery postcode returns error response.
         * Expected Result: Response with success=false.
         * Assertions: assertFalse(result.getSuccess());
         */
        @Test
        @DisplayName("Optimize Order - Empty Delivery Postcode - Returns Error Response")
        void optimizeOrder_EmptyDeliveryPostcode_ReturnsErrorResponse() {
            testOptimizationRequest.setDeliveryPostcode("");

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertNotNull(result.getErrorMessage());
        }

        /**
         * Purpose: Verify error message is set for null products.
         * Expected Result: Error message contains relevant text.
         * Assertions: assertTrue(result.getErrorMessage().contains("product"));
         */
        @Test
        @DisplayName("Optimize Order - Error Message For Null Products")
        void optimizeOrder_ErrorMessageForNullProducts() {
            testOptimizationRequest.setProductQuantities(null);

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertTrue(result.getErrorMessage().toLowerCase().contains("product"));
        }

        /**
         * Purpose: Verify error message is set for null postcode.
         * Expected Result: Error message contains relevant text.
         * Assertions: assertTrue(result.getErrorMessage().contains("postcode"));
         */
        @Test
        @DisplayName("Optimize Order - Error Message For Null Postcode")
        void optimizeOrder_ErrorMessageForNullPostcode() {
            testOptimizationRequest.setDeliveryPostcode(null);

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result.getSuccess());
            assertTrue(result.getErrorMessage().toLowerCase().contains("postcode"));
        }

        /**
         * Purpose: Verify response is not null even on error.
         * Expected Result: Non-null response.
         * Assertions: assertNotNull(result);
         */
        @Test
        @DisplayName("Optimize Order - Response Not Null On Error")
        void optimizeOrder_ResponseNotNullOnError() {
            testOptimizationRequest.setProductQuantities(null);

            OrderOptimizationResponseModel result = shippingService.optimizeOrder(testOptimizationRequest);

            assertNotNull(result);
        }

        /**
         * Purpose: Verify multiple calls with null products return error.
         * Expected Result: Each call returns error response.
         * Assertions: assertFalse(result.getSuccess());
         */
        @Test
        @DisplayName("Optimize Order - Multiple Calls Null Products Return Error")
        void optimizeOrder_MultipleCallsNullProductsReturnError() {
            testOptimizationRequest.setProductQuantities(null);

            OrderOptimizationResponseModel result1 = shippingService.optimizeOrder(testOptimizationRequest);
            OrderOptimizationResponseModel result2 = shippingService.optimizeOrder(testOptimizationRequest);

            assertFalse(result1.getSuccess());
            assertFalse(result2.getSuccess());
        }
    }

    @Nested
    @DisplayName("CancelShipment Validation Tests")
    class CancelShipmentValidationTests {

        /**
         * Purpose: Verify that null shipment ID throws NotFoundException (shipment not found).
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Null Shipment ID - Throws NotFoundException")
        void cancelShipment_NullShipmentId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(null));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that zero shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Zero Shipment ID - Throws NotFoundException")
        void cancelShipment_ZeroShipmentId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(0L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that negative shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative Shipment ID - Throws NotFoundException")
        void cancelShipment_NegativeShipmentId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-1L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that -100 shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative 100 ID - Throws NotFoundException")
        void cancelShipment_Negative100Id_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-100L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that Long.MIN_VALUE shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Min Long ID - Throws NotFoundException")
        void cancelShipment_MinLongId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(Long.MIN_VALUE));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that non-existent shipment throws NotFoundException.
         * Expected Result: NotFoundException is thrown with proper message.
         * Assertions: assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, 999L), ex.getMessage());
         */
        @Test
        @DisplayName("Cancel Shipment - Non Existent ID - Throws NotFoundException")
        void cancelShipment_NonExistentId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(999L));
            assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, 999L), ex.getMessage());
        }

        /**
         * Purpose: Verify that -5 shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative 5 ID - Throws NotFoundException")
        void cancelShipment_Negative5Id_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-5L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that -10 shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative 10 ID - Throws NotFoundException")
        void cancelShipment_Negative10Id_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-10L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that -50 shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative 50 ID - Throws NotFoundException")
        void cancelShipment_Negative50Id_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-50L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that -999 shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Negative 999 ID - Throws NotFoundException")
        void cancelShipment_Negative999Id_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-999L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that Long.MAX_VALUE shipment ID throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - Max Long ID - Throws NotFoundException")
        void cancelShipment_MaxLongId_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(Long.MAX_VALUE));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that ID 1 when not found throws NotFoundException.
         * Expected Result: NotFoundException is thrown.
         * Assertions: assertThrows(NotFoundException.class, ...);
         */
        @Test
        @DisplayName("Cancel Shipment - ID 1 Not Found - Throws NotFoundException")
        void cancelShipment_Id1NotFound_ThrowsNotFoundException() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(1L));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that multiple calls with invalid IDs throw.
         * Expected Result: Each call throws NotFoundException.
         * Assertions: Both calls throw.
         */
        @Test
        @DisplayName("Cancel Shipment - Multiple Invalid IDs Throw")
        void cancelShipment_MultipleInvalidIdsThrow() {
            assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-1L));
            assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(-2L));
        }

        /**
         * Purpose: Verify exception message format.
         * Expected Result: Message follows expected format.
         * Assertions: assertEquals(...);
         */
        @Test
        @DisplayName("Cancel Shipment - Exception Message Format")
        void cancelShipment_ExceptionMessageFormat() {
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shippingService.cancelShipment(123L));
            assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, 123L), ex.getMessage());
        }
    }

    @Nested
    @DisplayName("GetWalletBalance Validation Tests")
    class GetWalletBalanceValidationTests {

        /**
         * Purpose: Verify that getWalletBalance requires security context.
         * Expected Result: NullPointerException due to missing security context.
         * Assertions: assertThrows(NullPointerException.class, ...);
         */
        @Test
        @DisplayName("Get Wallet Balance - No Security Context - Throws NullPointerException")
        void getWalletBalance_NoSecurityContext_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
        }

        /**
         * Purpose: Verify that getWalletBalance throws exception without context.
         * Expected Result: Exception is thrown.
         * Assertions: assertThrows(Exception.class, ...);
         */
        @Test
        @DisplayName("Get Wallet Balance - Without Context - Throws Exception")
        void getWalletBalance_WithoutContext_ThrowsException() {
            assertThrows(Exception.class,
                    () -> shippingService.getWalletBalance());
        }

        /**
         * Purpose: Verify method throws when called.
         * Expected Result: Exception is thrown.
         * Assertions: assertThrows(Exception.class, ...);
         */
        @Test
        @DisplayName("Get Wallet Balance - Called - Throws Exception")
        void getWalletBalance_Called_ThrowsException() {
            Exception ex = assertThrows(Exception.class,
                    () -> shippingService.getWalletBalance());
            assertNotNull(ex);
        }

        /**
         * Purpose: Verify method throws with specific exception type.
         * Expected Result: NullPointerException is thrown.
         * Assertions: assertThrows(NullPointerException.class, ...);
         */
        @Test
        @DisplayName("Get Wallet Balance - Specific Exception Type")
        void getWalletBalance_SpecificExceptionType() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
            assertNotNull(ex);
        }

        /**
         * Purpose: Verify multiple calls throw same exception.
         * Expected Result: NullPointerException for each call.
         * Assertions: assertThrows twice.
         */
        @Test
        @DisplayName("Get Wallet Balance - Multiple Calls Same Exception")
        void getWalletBalance_MultipleCallsSameException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
            assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
        }

        /**
         * Purpose: Verify exception is not NotFoundException.
         * Expected Result: Exception is NullPointerException.
         * Assertions: assertThrows returns NullPointerException.
         */
        @Test
        @DisplayName("Get Wallet Balance - Not NotFoundException")
        void getWalletBalance_NotNotFoundException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
        }

        /**
         * Purpose: Verify exception is not BadRequestException.
         * Expected Result: Exception is NullPointerException.
         * Assertions: assertThrows returns NullPointerException.
         */
        @Test
        @DisplayName("Get Wallet Balance - Not BadRequestException")
        void getWalletBalance_NotBadRequestException() {
            assertThrows(NullPointerException.class,
                    () -> shippingService.getWalletBalance());
        }
    }
}
