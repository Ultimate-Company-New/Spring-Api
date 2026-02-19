package com.example.SpringApi.ServiceTests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import com.example.SpringApi.Models.DatabaseModels.ReturnShipment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Tests for ShippingService.cancelReturnShipment().
 */
@DisplayName("CancelReturnShipment Tests")
class CancelReturnShipmentTest extends ShippingServiceTestBase {


    // Total Tests: 10
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify return shipment save is called on success.
     * Expected Result: Return shipment saved.
     * Assertions: verify save called.
     */
    @Test
    @DisplayName("cancelReturnShipment - Saves Return Shipment - Success")
    void cancelReturnShipment_SavesReturnShipment_Success() {
        // Arrange
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperCancelOrders();
        stubReturnShipmentRepositorySave(testReturnShipment);

        // Act
        shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID);

        // Assert
        verify(returnShipmentRepository).save(any(ReturnShipment.class));
    }

    /**
     * Purpose: Verify cancel return succeeds with valid data.
     * Expected Result: No exception thrown.
     * Assertions: Return status set to RETURN_CANCELLED.
     */
    @Test
    @DisplayName("cancelReturnShipment - Valid Request - Success")
    void cancelReturnShipment_ValidRequest_Success() {
        // Arrange
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperCancelOrders();
        stubReturnShipmentRepositorySave(testReturnShipment);

        // Act
        assertDoesNotThrow(() -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue(),
                testReturnShipment.getShipRocketReturnStatus());
    }

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify already cancelled return shipment throws BadRequestException.
     * Expected Result: BadRequestException with AlreadyCancelled message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Already Cancelled - Throws BadRequestException")
    void cancelReturnShipment_AlreadyCancelled_ThrowsBadRequestException() {
        // Arrange
        testReturnShipment.setShipRocketReturnStatus(ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue());
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.ALREADY_CANCELLED, ex.getMessage());
    }

    /**
     * Purpose: Verify cancel API error throws BadRequestException.
     * Expected Result: BadRequestException with FailedToCancelReturn message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Cancel API Error - Throws BadRequestException")
    void cancelReturnShipment_CancelApiError_ThrowsBadRequestException() {
        // Arrange
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperCancelOrdersThrows(new RuntimeException(ErrorMessages.OPERATION_FAILED));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.FAILED_TO_CANCEL_RETURN,
                ErrorMessages.OPERATION_FAILED), ex.getMessage());
    }

    /**
     * Purpose: Verify missing ShipRocket return order id throws BadRequestException.
     * Expected Result: BadRequestException with NoShipRocketOrderId message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Missing Order Id - Throws BadRequestException")
    void cancelReturnShipment_MissingOrderId_ThrowsBadRequestException() {
        // Arrange
        testReturnShipment.setShipRocketReturnOrderId(null);
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.NO_SHIP_ROCKET_ORDER_ID, ex.getMessage());
    }

    /**
     * Purpose: Verify missing ShipRocket credentials throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Credentials Missing - Throws BadRequestException")
    void cancelReturnShipment_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testClientResponse.setShipRocketPassword(null);
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.SHIP_ROCKET_CREDENTIALS_NOT_CONFIGURED, ex.getMessage());
    }

    /**
     * Purpose: Verify non-numeric return order id throws BadRequestException.
     * Expected Result: BadRequestException with InvalidId format message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Invalid Return Order Id Format - Throws BadRequestException")
    void cancelReturnShipment_InvalidReturnOrderIdFormat_ThrowsBadRequestException() {
        // Arrange
        testReturnShipment.setShipRocketReturnOrderId("RET-XYZ");
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(testReturnShipment);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(ErrorMessages.ReturnShipmentErrorMessages.INVALID_ID + " Format error: RET-XYZ", ex.getMessage());
    }

    /**
     * Purpose: Verify return shipment not found throws NotFoundException.
     * Expected Result: NotFoundException with NotFound message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("cancelReturnShipment - Not Found - Throws NotFoundException")
    void cancelReturnShipment_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> shippingService.cancelReturnShipment(TEST_RETURN_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ReturnShipmentErrorMessages.NOT_FOUND, TEST_RETURN_SHIPMENT_ID),
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
    @DisplayName("cancelReturnShipment - Controller Permission - Unauthorized")
    void cancelReturnShipment_controller_permission_unauthorized() {
        // Arrange
        ShippingController controller = new ShippingController(shippingServiceMock);
        stubShippingServiceMockCancelReturnShipmentUnauthorized();

        // Act
        ResponseEntity<?> response = controller.cancelReturn(TEST_RETURN_SHIPMENT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for cancelReturn.
     * Expected Result: Annotation exists and includes MODIFY_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("cancelReturnShipment - Verify @PreAuthorize Annotation")
    void cancelReturnShipment_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("cancelReturn", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.MODIFY_SHIPMENTS_PERMISSION));
    }
}
