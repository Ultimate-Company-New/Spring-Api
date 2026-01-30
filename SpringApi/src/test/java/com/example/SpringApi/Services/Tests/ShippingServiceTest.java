package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ShippingService;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShippingService.
 * 
 * Test Group Summary:
 * | Group Name | Number of Tests |
 * | :--- | :--- |
 * | CalculateShippingValidationTests | 4 |
 * | OptimizeOrderValidationTests | 7 |
 * | CancelShipmentValidationTests | 8 |
 * | GetWalletBalanceValidationTests | 4 |
 * | **Total** | **23** |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingService Tests")
class ShippingServiceTest extends BaseTest {

        @Mock
        private ClientRepository clientRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

        @Mock
        private PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

        @Mock
        private PickupLocationRepository pickupLocationRepository;

        @Mock
        private ShipmentRepository shipmentRepository;

        @Mock
        private UserLogService userLogService;

        @Mock
        private Environment environment;

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
        @DisplayName("CalculateShippingValidationTests")
        class CalculateShippingValidationTests {

                /**
                 * Purpose: Verify that calculating shipping with null request throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Calculate Shipping - Null Request - Throws BadRequestException")
                void calculateShipping_NullRequest_ThrowsBadRequestException() {
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shippingService.calculateShipping(null));
                        assertNotNull(ex.getMessage());
                }

                /**
                 * Purpose: Verify that calculating shipping without ShipRocket credentials throws BadRequestException.
                 * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured.
                 */
                @Test
                @DisplayName("Calculate Shipping - ShipRocket Credentials Not Configured - Throws BadRequestException")
                void calculateShipping_ShipRocketNotConfigured_ThrowsBadRequestException() {
                        testClient.setShipRocketEmail(null);
                        testClient.setShipRocketPassword(null);

                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.calculateShipping(testShippingRequest));

                        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured,
                                        exception.getMessage());
                }

                /**
                 * Purpose: Verify that calculating shipping with null delivery postcode throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Calculate Shipping - Null Delivery Postcode - Throws BadRequestException")
                void calculateShipping_NullDeliveryPostcode_ThrowsBadRequestException() {
                        testShippingRequest.setDeliveryPostcode(null);

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shippingService.calculateShipping(testShippingRequest));
                        assertNotNull(ex.getMessage());
                }

                /**
                 * Purpose: Verify that calculating shipping with empty delivery postcode throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Calculate Shipping - Empty Delivery Postcode - Throws BadRequestException")
                void calculateShipping_EmptyDeliveryPostcode_ThrowsBadRequestException() {
                        testShippingRequest.setDeliveryPostcode("");

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shippingService.calculateShipping(testShippingRequest));
                        assertNotNull(ex.getMessage());
                }
        }

        @Nested
        @DisplayName("OptimizeOrderValidationTests")
        class OptimizeOrderValidationTests {

                /**
                 * Purpose: Verify that optimizing order with null request throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Optimize Order - Null Request - Throws BadRequestException")
                void optimizeOrder_NullRequest_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(null));

                        assertNotNull(exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with null product quantities throws BadRequestException.
                 * Expected Result: BadRequestException with ListCannotBeNullOrEmpty message is thrown.
                 * Assertions: Exception message equals formatted ListCannotBeNullOrEmpty error message.
                 */
                @Test
                @DisplayName("Optimize Order - Null Product Quantities - Throws BadRequestException")
                void optimizeOrder_NullProductQuantities_ThrowsBadRequestException() {
                        testOptimizationRequest.setProductQuantities(null);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty,
                                        "Product IDs"), exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with empty product quantities throws BadRequestException.
                 * Expected Result: BadRequestException with ListCannotBeNullOrEmpty message is thrown.
                 * Assertions: Exception message equals formatted ListCannotBeNullOrEmpty error message.
                 */
                @Test
                @DisplayName("Optimize Order - Empty Product Quantities - Throws BadRequestException")
                void optimizeOrder_EmptyProductQuantities_ThrowsBadRequestException() {
                        testOptimizationRequest.setProductQuantities(Collections.emptyMap());

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty,
                                        "Product IDs"), exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with null delivery postcode throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Optimize Order - Null Delivery Postcode - Throws BadRequestException")
                void optimizeOrder_NullDeliveryPostcode_ThrowsBadRequestException() {
                        testOptimizationRequest.setDeliveryPostcode(null);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertNotNull(exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with zero quantity product throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Optimize Order - Zero Quantity Product - Throws BadRequestException")
                void optimizeOrder_ZeroQuantityProduct_ThrowsBadRequestException() {
                        Map<Long, Integer> productQuantities = new HashMap<>();
                        productQuantities.put(TEST_PRODUCT_ID, 0);
                        testOptimizationRequest.setProductQuantities(productQuantities);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertNotNull(exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with negative quantity product throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: Exception is not null.
                 */
                @Test
                @DisplayName("Optimize Order - Negative Quantity Product - Throws BadRequestException")
                void optimizeOrder_NegativeQuantityProduct_ThrowsBadRequestException() {
                        Map<Long, Integer> productQuantities = new HashMap<>();
                        productQuantities.put(TEST_PRODUCT_ID, -5);
                        testOptimizationRequest.setProductQuantities(productQuantities);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertNotNull(exception.getMessage());
                }

                /**
                 * Purpose: Verify that optimizing order with non-existent product throws NotFoundException.
                 * Expected Result: NotFoundException with ER013 message is thrown.
                 * Assertions: Exception message equals formatted ER013 error message.
                 */
                @Test
                @DisplayName("Optimize Order - Product Not Found - Throws NotFoundException")
                void optimizeOrder_ProductNotFound_ThrowsNotFoundException() {
                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                        when(productRepository.findAllById(anySet())).thenReturn(Collections.emptyList());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                                        exception.getMessage());
                }
        }

        @Nested
        @DisplayName("CancelShipmentValidationTests")
        class CancelShipmentValidationTests {

                /**
                 * Purpose: Verify that cancelling shipment with null shipment ID throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Cancel Shipment - Null Shipment ID - Throws BadRequestException")
                void cancelShipment_NullShipmentId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(null));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling shipment with zero shipment ID throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Cancel Shipment - Zero Shipment ID - Throws BadRequestException")
                void cancelShipment_ZeroShipmentId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(0L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling non-existent shipment throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Cancel Shipment - Shipment Not Found - Throws NotFoundException")
                void cancelShipment_ShipmentNotFound_ThrowsNotFoundException() {
                        Long shipmentId = 999L;
                        when(shipmentRepository.findByShipmentIdAndClientId(shipmentId, TEST_CLIENT_ID))
                                        .thenReturn(null);

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shippingService.cancelShipment(shipmentId));

                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId),
                                        exception.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling already cancelled shipment throws BadRequestException.
                 * Expected Result: BadRequestException with AlreadyCancelled message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.AlreadyCancelled.
                 */
                @Test
                @DisplayName("Cancel Shipment - Already Cancelled - Throws BadRequestException")
                void cancelShipment_AlreadyCancelled_ThrowsBadRequestException() {
                        Shipment cancelledShipment = new Shipment();
                        cancelledShipment.setShipmentId(1L);
                        cancelledShipment.setClientId(TEST_CLIENT_ID);
                        cancelledShipment.setShipRocketStatus("CANCELLED");

                        when(shipmentRepository.findByShipmentIdAndClientId(1L, TEST_CLIENT_ID))
                                        .thenReturn(cancelledShipment);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(1L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.AlreadyCancelled, exception.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling shipment without ShipRocket order ID throws BadRequestException.
                 * Expected Result: BadRequestException with NoShipRocketOrderId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.NoShipRocketOrderId.
                 */
                @Test
                @DisplayName("Cancel Shipment - No ShipRocket Order ID - Throws BadRequestException")
                void cancelShipment_NoShipRocketOrderId_ThrowsBadRequestException() {
                        Shipment shipment = new Shipment();
                        shipment.setShipmentId(1L);
                        shipment.setClientId(TEST_CLIENT_ID);
                        shipment.setShipRocketOrderId(null);
                        shipment.setShipRocketStatus("NEW");

                        when(shipmentRepository.findByShipmentIdAndClientId(1L, TEST_CLIENT_ID))
                                        .thenReturn(shipment);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(1L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipRocketOrderId, exception.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling shipment with negative ID throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Cancel Shipment - Negative ID - Not Found")
                void cancelShipment_NegativeId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(-1L, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shippingService.cancelShipment(-1L));
                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, -1L), ex.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling shipment with zero ID throws NotFoundException when service doesn't validate first.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Cancel Shipment - Zero ID - Not Found")
                void cancelShipment_ZeroId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(0L, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shippingService.cancelShipment(0L));
                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, 0L), ex.getMessage());
                }

                /**
                 * Purpose: Verify that cancelling shipment with Long.MAX_VALUE ID throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Cancel Shipment - Max Long ID - Not Found")
                void cancelShipment_MaxLongId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shippingService.cancelShipment(Long.MAX_VALUE));
                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, Long.MAX_VALUE), ex.getMessage());
                }
        }

        @Nested
        @DisplayName("GetWalletBalanceValidationTests")
        class GetWalletBalanceValidationTests {

                /**
                 * Purpose: Verify that getting wallet balance without ShipRocket credentials throws BadRequestException.
                 * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured.
                 */
                @Test
                @DisplayName("Get Wallet Balance - ShipRocket Not Configured - Throws BadRequestException")
                void getWalletBalance_ShipRocketNotConfigured_ThrowsBadRequestException() {
                        testClient.setShipRocketEmail(null);

                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.getWalletBalance());

                        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured,
                                        exception.getMessage());
                }

                /**
                 * Purpose: Verify that getting wallet balance when client not found throws NotFoundException.
                 * Expected Result: NotFoundException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ClientErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Get Wallet Balance - Client Not Found - Throws NotFoundException")
                void getWalletBalance_ClientNotFound_ThrowsNotFoundException() {
                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shippingService.getWalletBalance());

                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
                }

                /**
                 * Purpose: Verify that getting wallet balance with null ShipRocket password throws BadRequestException.
                 * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured.
                 */
                @Test
                @DisplayName("Get Wallet Balance - Null Password - Throws BadRequestException")
                void getWalletBalance_NullPassword_ThrowsBadRequestException() {
                        testClient.setShipRocketPassword(null);
                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                        
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shippingService.getWalletBalance());
                        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
                }

                /**
                 * Purpose: Verify that getting wallet balance with empty ShipRocket password throws BadRequestException.
                 * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured.
                 */
                @Test
                @DisplayName("Get Wallet Balance - Empty Password - Throws BadRequestException")
                void getWalletBalance_EmptyPassword_ThrowsBadRequestException() {
                        testClient.setShipRocketPassword("");
                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                        
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shippingService.getWalletBalance());
                        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
                }
        }
}
