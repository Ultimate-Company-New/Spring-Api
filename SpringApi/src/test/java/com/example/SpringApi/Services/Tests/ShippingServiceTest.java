package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
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
 * Unit tests for {@link ShippingService}.
 * 
 * Tests shipping calculations, order optimization, shipment cancellation,
 * return order creation, and wallet balance retrieval.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingService Tests")
class ShippingServiceTest {

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

        // ==================== Calculate Shipping Tests ====================

        @Nested
        @DisplayName("Calculate Shipping - Validation Tests")
        class CalculateShippingValidationTests {

                @Test
                @DisplayName("Calculate Shipping - Null Request - Throws BadRequestException")
                void calculateShipping_NullRequest_ThrowsBadRequestException() {
                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.NullRequest,
                                () -> shippingService.calculateShipping(null));
                }

                @Test
                @DisplayName("Calculate Shipping - Null Delivery Postcode - Throws BadRequestException")
                void calculateShipping_NullDeliveryPostcode_ThrowsBadRequestException() {
                        testShippingRequest.setDeliveryPostcode(null);

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }

                @Test
                @DisplayName("Calculate Shipping - Empty Delivery Postcode - Throws BadRequestException")
                void calculateShipping_EmptyDeliveryPostcode_ThrowsBadRequestException() {
                        testShippingRequest.setDeliveryPostcode("");

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }

                @Test
                @DisplayName("Calculate Shipping - Null Pickup Locations - Throws BadRequestException")
                void calculateShipping_NullPickupLocations_ThrowsBadRequestException() {
                        testShippingRequest.setPickupLocations(null);

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidPickupLocations,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }

                @Test
                @DisplayName("Calculate Shipping - Empty Pickup Locations - Throws BadRequestException")
                void calculateShipping_EmptyPickupLocations_ThrowsBadRequestException() {
                        testShippingRequest.setPickupLocations(Collections.emptyList());

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidPickupLocations,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }

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

                @Test
                @DisplayName("Calculate Shipping - Invalid Pickup Location Weight - Throws BadRequestException")
                void calculateShipping_InvalidPickupLocationWeight_ThrowsBadRequestException() {
                        testShippingRequest.getPickupLocations().get(0).setTotalWeightKgs(null);

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidWeight,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }

                @Test
                @DisplayName("Calculate Shipping - Negative Weight - Throws BadRequestException")
                void calculateShipping_NegativeWeight_ThrowsBadRequestException() {
                        testShippingRequest.getPickupLocations().get(0).setTotalWeightKgs(new BigDecimal("-1.00"));

                        assertThrowsBadRequest(ErrorMessages.ShippingErrorMessages.InvalidWeight,
                                () -> shippingService.calculateShipping(testShippingRequest));
                }
        }

        // ==================== Optimize Order Tests ====================

        @Nested
        @DisplayName("Optimize Order - Validation Tests")
        class OptimizeOrderValidationTests {

                @Test
                @DisplayName("Optimize Order - Null Request - Throws BadRequestException")
                void optimizeOrder_NullRequest_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(null));

                        assertNotNull(exception.getMessage());
                }

                @Test
                @DisplayName("Optimize Order - Null Product Quantities - Throws BadRequestException")
                void optimizeOrder_NullProductQuantities_ThrowsBadRequestException() {
                        testOptimizationRequest.setProductQuantities(null);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty,
                                        "Product IDs"), exception.getMessage());
                }

                @Test
                @DisplayName("Optimize Order - Empty Product Quantities - Throws BadRequestException")
                void optimizeOrder_EmptyProductQuantities_ThrowsBadRequestException() {
                        testOptimizationRequest.setProductQuantities(Collections.emptyMap());

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty,
                                        "Product IDs"), exception.getMessage());
                }

                @Test
                @DisplayName("Optimize Order - Null Delivery Postcode - Throws BadRequestException")
                void optimizeOrder_NullDeliveryPostcode_ThrowsBadRequestException() {
                        testOptimizationRequest.setDeliveryPostcode(null);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.optimizeOrder(testOptimizationRequest));

                        assertNotNull(exception.getMessage());
                }

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

        // ==================== Cancel Shipment Tests ====================

        @Nested
        @DisplayName("Cancel Shipment - Validation Tests")
        class CancelShipmentValidationTests {

                @Test
                @DisplayName("Cancel Shipment - Null Shipment ID - Throws BadRequestException")
                void cancelShipment_NullShipmentId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(null));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                @Test
                @DisplayName("Cancel Shipment - Zero Shipment ID - Throws BadRequestException")
                void cancelShipment_ZeroShipmentId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(0L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                @Test
                @DisplayName("Cancel Shipment - Shipment Not Found - Throws NotFoundException")
                void cancelShipment_ShipmentNotFound_ThrowsNotFoundException() {
                        Long shipmentId = 999L;
                        when(shipmentRepository.findByShipmentIdAndClientId(shipmentId, TEST_CLIENT_ID))
                                        .thenReturn(Optional.empty());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shippingService.cancelShipment(shipmentId));

                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId),
                                        exception.getMessage());
                }

                @Test
                @DisplayName("Cancel Shipment - Already Cancelled - Throws BadRequestException")
                void cancelShipment_AlreadyCancelled_ThrowsBadRequestException() {
                        Shipment cancelledShipment = new Shipment();
                        cancelledShipment.setShipmentId(1L);
                        cancelledShipment.setClientId(TEST_CLIENT_ID);
                        cancelledShipment.setIsCancelled(true);

                        when(shipmentRepository.findByShipmentIdAndClientId(1L, TEST_CLIENT_ID))
                                        .thenReturn(Optional.of(cancelledShipment));

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(1L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.AlreadyCancelled, exception.getMessage());
                }

                @Test
                @DisplayName("Cancel Shipment - No ShipRocket Order ID - Throws BadRequestException")
                void cancelShipment_NoShipRocketOrderId_ThrowsBadRequestException() {
                        Shipment shipment = new Shipment();
                        shipment.setShipmentId(1L);
                        shipment.setClientId(TEST_CLIENT_ID);
                        shipment.setShipRocketOrderId(null);
                        shipment.setIsCancelled(false);

                        when(shipmentRepository.findByShipmentIdAndClientId(1L, TEST_CLIENT_ID))
                                        .thenReturn(Optional.of(shipment));

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shippingService.cancelShipment(1L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.NoShipRocketOrderId, exception.getMessage());
                }
        }

        // ==================== Get Wallet Balance Tests ====================

        @Nested
        @DisplayName("Get Wallet Balance - Validation Tests")
        class GetWalletBalanceValidationTests {

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

                @Test
                @DisplayName("Get Wallet Balance - Client Not Found - Throws NotFoundException")
                void getWalletBalance_ClientNotFound_ThrowsNotFoundException() {
                        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shippingService.getWalletBalance());

                        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
                }
        }

        // ==================== Additional CalculateShipping Tests ====================

        @Test
        @DisplayName("Calculate Shipping - Null Request - Throws BadRequestException")
        void calculateShipping_NullRequest_ThrowsBadRequestException() {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(null));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidRequest, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Null Delivery Postcode - Throws BadRequestException")
        void calculateShipping_NullDeliveryPostcode_ThrowsBadRequestException() {
                testShippingRequest.setDeliveryPostcode(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Empty Delivery Postcode - Throws BadRequestException")
        void calculateShipping_EmptyDeliveryPostcode_ThrowsBadRequestException() {
                testShippingRequest.setDeliveryPostcode("");
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Null Pickup Locations - Throws BadRequestException")
        void calculateShipping_NullPickupLocations_ThrowsBadRequestException() {
                testShippingRequest.setPickupLocations(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.NoPickupLocations, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Empty Pickup Locations - Throws BadRequestException")
        void calculateShipping_EmptyPickupLocations_ThrowsBadRequestException() {
                testShippingRequest.setPickupLocations(new ArrayList<>());
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.NoPickupLocations, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Negative Weight - Throws BadRequestException")
        void calculateShipping_NegativeWeight_ThrowsBadRequestException() {
                testShippingRequest.getPickupLocations().get(0).setTotalWeightKgs(new BigDecimal("-1.0"));
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidWeight, ex.getMessage());
        }

        @Test
        @DisplayName("Calculate Shipping - Zero Weight - Throws BadRequestException")
        void calculateShipping_ZeroWeight_ThrowsBadRequestException() {
                testShippingRequest.getPickupLocations().get(0).setTotalWeightKgs(BigDecimal.ZERO);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.calculateShipping(testShippingRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidWeight, ex.getMessage());
        }

        // ==================== Additional OptimizeOrder Tests ====================

        @Test
        @DisplayName("Optimize Order - Null Request - Throws BadRequestException")
        void optimizeOrder_NullRequest_ThrowsBadRequestException() {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.optimizeOrder(null));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidRequest, ex.getMessage());
        }

        @Test
        @DisplayName("Optimize Order - Null Delivery Postcode - Throws BadRequestException")
        void optimizeOrder_NullDeliveryPostcode_ThrowsBadRequestException() {
                testOptimizationRequest.setDeliveryPostcode(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.optimizeOrder(testOptimizationRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode, ex.getMessage());
        }

        @Test
        @DisplayName("Optimize Order - Empty Delivery Postcode - Throws BadRequestException")
        void optimizeOrder_EmptyDeliveryPostcode_ThrowsBadRequestException() {
                testOptimizationRequest.setDeliveryPostcode("");
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.optimizeOrder(testOptimizationRequest));
                assertEquals(ErrorMessages.ShippingErrorMessages.InvalidDeliveryPostcode, ex.getMessage());
        }

        // ==================== Additional CancelShipment Tests ====================

        @Test
        @DisplayName("Cancel Shipment - Negative ID - Not Found")
        void cancelShipment_NegativeId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(-1L, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shippingService.cancelShipment(-1L));
                assertEquals(ErrorMessages.ShipmentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Cancel Shipment - Zero ID - Not Found")
        void cancelShipment_ZeroId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(0L, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shippingService.cancelShipment(0L));
                assertEquals(ErrorMessages.ShipmentErrorMessages.NotFound, ex.getMessage());
        }

        @Test
        @DisplayName("Cancel Shipment - Max Long ID - Not Found")
        void cancelShipment_MaxLongId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shippingService.cancelShipment(Long.MAX_VALUE));
                assertEquals(ErrorMessages.ShipmentErrorMessages.NotFound, ex.getMessage());
        }

        // ==================== Additional GetWalletBalance Tests ====================

        @Test
        @DisplayName("Get Wallet Balance - Null Password - Throws BadRequestException")
        void getWalletBalance_NullPassword_ThrowsBadRequestException() {
                testClient.setShipRocketPassword(null);
                when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
                
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shippingService.getWalletBalance());
                assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
        }

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
