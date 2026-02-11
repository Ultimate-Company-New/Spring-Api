package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShippingOptionsResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.calculateShipping().
 */
@DisplayName("CalculateShipping Tests")
class CalculateShippingTest extends ShippingServiceTestBase {

    // Total Tests: 12

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify null pickup locations returns empty response.
     * Expected Result: Response contains empty location options.
     * Assertions: locationOptions is empty.
     */
    @Test
    @DisplayName("calculateShipping - Null Pickup Locations - Returns Empty Response")
    void calculateShipping_NullPickupLocations_ReturnsEmptyResponse() {
        // Arrange
        shippingRequest.setPickupLocations(null);

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getLocationOptions().isEmpty());
    }

    /**
     * Purpose: Verify empty pickup locations returns empty response.
     * Expected Result: Response contains empty location options.
     * Assertions: locationOptions is empty.
     */
    @Test
    @DisplayName("calculateShipping - Empty Pickup Locations - Returns Empty Response")
    void calculateShipping_EmptyPickupLocations_ReturnsEmptyResponse() {
        // Arrange
        shippingRequest.setPickupLocations(new ArrayList<>());

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getLocationOptions().isEmpty());
    }

    /**
     * Purpose: Verify valid request returns shipping options.
     * Expected Result: Response contains location options.
     * Assertions: locationOptions size is 1.
     */
    @Test
    @DisplayName("calculateShipping - Valid Request - Success")
    void calculateShipping_ValidRequest_Success() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(50.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLocationOptions().size());
    }

    /**
     * Purpose: Verify weight below minimum is adjusted to minimum.
     * Expected Result: Response returned without error.
     * Assertions: locationOptions size is 1.
     */
    @Test
    @DisplayName("calculateShipping - Weight Below Minimum - Success")
    void calculateShipping_WeightBelowMinimum_Success() {
        // Arrange
        shippingRequest.getPickupLocations().get(0).setTotalWeightKgs(new BigDecimal("0.1"));
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(40.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLocationOptions().size());
    }

    /**
     * Purpose: Verify selected courier is set to cheapest option.
     * Expected Result: Selected courier rate equals lowest rate.
     * Assertions: Selected courier rate is lowest.
     */
    @Test
    @DisplayName("calculateShipping - Select Cheapest Courier - Success")
    void calculateShipping_SelectCheapestCourier_Success() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(80.0, 20.0, 50.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("20.0"), result.getLocationOptions().get(0).getSelectedCourier().getRate());
    }

    /**
     * Purpose: Verify totalShippingCost sums selected couriers for multiple locations.
     * Expected Result: Total cost equals sum of selected courier rates.
     * Assertions: Total cost matches expected.
     */
    @Test
    @DisplayName("calculateShipping - Multiple Locations - Total Cost Sum")
    void calculateShipping_MultipleLocations_TotalCostSum() {
        // Arrange
        ShippingCalculationRequestModel.PickupLocationShipment second = new ShippingCalculationRequestModel.PickupLocationShipment();
        second.setPickupLocationId(999L);
        second.setPickupPostcode("400003");
        second.setTotalWeightKgs(new BigDecimal("2.0"));
        second.setTotalQuantity(1);
        second.setLocationName("WH2");
        second.setProductIds(List.of(TEST_PRODUCT_ID));
        shippingRequest.setPickupLocations(List.of(shippingRequest.getPickupLocations().get(0), second));
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(10.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("20.0"), result.getTotalShippingCost());
    }

    /**
     * Purpose: Verify helper exception does not break processing.
     * Expected Result: Response still contains location options.
     * Assertions: locationOptions size is 1.
     */
    @Test
    @DisplayName("calculateShipping - Helper Throws - Continues")
    void calculateShipping_HelperThrows_Continues() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptionsThrows(new RuntimeException("boom"));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLocationOptions().size());
    }

    /**
     * Purpose: Verify null couriers list results in no selected courier.
     * Expected Result: Selected courier is null.
     * Assertions: selected courier is null.
     */
    @Test
    @DisplayName("calculateShipping - Null Couriers - No Selection")
    void calculateShipping_NullCouriers_NoSelection() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        ShippingOptionsResponseModel response = new ShippingOptionsResponseModel();
        response.data = new ShippingOptionsResponseModel.Data();
        response.data.available_courier_companies = null;
        stubShipRocketHelperGetAvailableShippingOptions(response);

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getLocationOptions().get(0).getSelectedCourier());
    }

    /**
     * Purpose: Verify empty couriers list results in no selected courier.
     * Expected Result: Selected courier is null.
     * Assertions: selected courier is null.
     */
    @Test
    @DisplayName("calculateShipping - Empty Couriers - No Selection")
    void calculateShipping_EmptyCouriers_NoSelection() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        ShippingOptionsResponseModel response = new ShippingOptionsResponseModel();
        response.data = new ShippingOptionsResponseModel.Data();
        response.data.available_courier_companies = new ArrayList<>();
        stubShipRocketHelperGetAvailableShippingOptions(response);

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getLocationOptions().get(0).getSelectedCourier());
    }

    /**
     * Purpose: Verify COD flag true still returns response.
     * Expected Result: Response returned without error.
     * Assertions: locationOptions size is 1.
     */
    @Test
    @DisplayName("calculateShipping - COD True - Success")
    void calculateShipping_CodTrue_Success() {
        // Arrange
        shippingRequest.setIsCod(true);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(25.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLocationOptions().size());
    }

    /**
     * Purpose: Verify null delivery postcode still returns response.
     * Expected Result: Response returned without error.
     * Assertions: locationOptions size is 1.
     */
    @Test
    @DisplayName("calculateShipping - Null Delivery Postcode - Success")
    void calculateShipping_NullDeliveryPostcode_Success() {
        // Arrange
        shippingRequest.setDeliveryPostcode(null);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetAvailableShippingOptions(createShippingOptions(30.0));

        // Act
        ShippingCalculationResponseModel result = shippingService.calculateShipping(shippingRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getLocationOptions().size());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify controller has @PreAuthorize for calculateShipping.
     * Expected Result: Annotation exists and includes VIEW_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("calculateShipping - Verify @PreAuthorize Annotation")
    void calculateShipping_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("calculateShipping", ShippingCalculationRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION));
    }
}
