package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Tests for ShippingService.cancelShipment().
 */
@DisplayName("CancelShipment Tests")
class CancelShipmentTest extends ShippingServiceTestBase {


    // Total Tests: 8
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify shipment save is called on success.
     * Expected Result: Shipment repository save called.
     * Assertions: verify save called.
     */
    @Test
    @DisplayName("cancelShipment - Saves Shipment - Success")
    void cancelShipment_SavesShipment_Success() {
        // Arrange
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperCancelOrders();
        stubShipmentRepositorySave(testShipment);

        // Act
        shippingService.cancelShipment(TEST_SHIPMENT_ID);


        // Assert
        assertEquals("CANCELLED", testShipment.getShipRocketStatus());
    }

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify already cancelled shipment throws BadRequestException.
     * Expected Result: BadRequestException with AlreadyCancelled message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelShipment - Already Cancelled - Throws BadRequestException")
    void cancelShipment_AlreadyCancelled_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketStatus("CANCELLED");
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelShipment(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.ALREADY_CANCELLED, ex.getMessage());
    }

    /**
     * Purpose: Verify cancel API exception throws BadRequestException.
     * Expected Result: BadRequestException with InvalidIdWithMessageFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelShipment - Cancel API Error - Throws BadRequestException")
    void cancelShipment_CancelApiError_ThrowsBadRequestException() {
        // Arrange
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperCancelOrdersThrows(new RuntimeException(ErrorMessages.OPERATION_FAILED));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelShipment(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.INVALID_ID_WITH_MESSAGE_FORMAT,
            ErrorMessages.OPERATION_FAILED),
                ex.getMessage());
    }

    /**
     * Purpose: Verify missing credentials throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelShipment - Credentials Missing - Throws BadRequestException")
    void cancelShipment_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testClientResponse.setShipRocketEmail(null);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelShipment(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED, ex.getMessage());
    }

    /**
     * Purpose: Verify missing ShipRocket order id throws BadRequestException.
     * Expected Result: BadRequestException with NoShipRocketOrderId message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelShipment - Missing Order Id - Throws BadRequestException")
    void cancelShipment_MissingOrderId_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketOrderId(null);
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelShipment(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.NO_SHIP_ROCKET_ORDER_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify non-numeric order id throws BadRequestException.
     * Expected Result: BadRequestException with InvalidIdFormatErrorFormat message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelShipment - Non Numeric Order Id - Throws BadRequestException")
    void cancelShipment_NonNumericOrderId_ThrowsBadRequestException() {
        // Arrange
        testShipment.setShipRocketOrderId("SR-ABC");
        stubShipmentRepositoryFindByShipmentIdAndClientId(testShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelShipment(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.INVALID_ID_FORMAT_ERROR_FORMAT, "SR-ABC"),
                ex.getMessage());
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
    @DisplayName("cancelShipment - Controller Permission - Unauthorized")
    void cancelShipment_controller_permission_unauthorized() {
        // Arrange
        ShippingController controller = new ShippingController(shippingServiceMock);
        stubShippingServiceMockCancelShipmentUnauthorized();

        // Act
        ResponseEntity<?> response = controller.cancelShipment(TEST_SHIPMENT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for cancelShipment.
     * Expected Result: Annotation exists and includes MODIFY_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("cancelShipment - Verify @PreAuthorize Annotation")
    void cancelShipment_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("cancelShipment", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.MODIFY_SHIPMENTS_PERMISSION));
    }
}
