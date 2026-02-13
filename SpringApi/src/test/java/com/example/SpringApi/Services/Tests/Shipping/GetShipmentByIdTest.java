package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.getShipmentById().
 */
@DisplayName("GetShipmentById Tests")
class GetShipmentByIdTest extends ShippingServiceTestBase {


    // Total Tests: 15
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify valid shipment ID with different ID returns response.
     * Expected Result: ShipmentResponseModel is returned.
     * Assertions: Response shipmentId matches.
     */
    @Test
    @DisplayName("getShipmentById - Different ID - Success")
    void getShipmentById_DifferentId_Success() {
        // Arrange
        Shipment shipment = createTestShipment(999L);
        shipment.setShipRocketOrderId("SR-999");
        stubShipmentRepositoryFindById(shipment);

        // Act
        ShipmentResponseModel result = shippingService.getShipmentById(999L);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.getShipmentId());
    }

    /**
     * Purpose: Verify response when shipment has order id.
     * Expected Result: ShipmentResponseModel returned without error.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("getShipmentById - ShipRocket Order Id Present - Success")
    void getShipmentById_ShipRocketOrderIdPresent_Success() {
        // Arrange
        testShipment.setShipRocketOrderId("SR-123");
        stubShipmentRepositoryFindById(testShipment);

        // Act
        ShipmentResponseModel result = shippingService.getShipmentById(TEST_SHIPMENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
    }

    /**
     * Purpose: Verify valid shipment ID returns response.
     * Expected Result: ShipmentResponseModel is returned.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("getShipmentById - Valid ID - Success")
    void getShipmentById_ValidId_Success() {
        // Arrange
        stubShipmentRepositoryFindById(testShipment);

        // Act
        ShipmentResponseModel result = shippingService.getShipmentById(TEST_SHIPMENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
    }

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
         * Purpose: Verify client mismatch throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Client Mismatch - Throws NotFoundException")
        void getShipmentById_ClientMismatch_ThrowsNotFoundException() {
        // Arrange
        Shipment shipment = createTestShipment(TEST_SHIPMENT_ID);
        shipment.setClientId(999L);
        stubShipmentRepositoryFindById(shipment);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID), ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException for large ID when shipment not found.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Large ID Not Found - Throws NotFoundException")
        void getShipmentById_LargeIdNotFound_ThrowsNotFoundException() {
        // Arrange
        stubShipmentRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(Long.MAX_VALUE));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, Long.MAX_VALUE), ex.getMessage());
        }

        /**
         * Purpose: Verify multiple invalid calls still throw BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: Exception type and message for each call.
         */
        @Test
        @DisplayName("getShipmentById - Multiple Invalid IDs - Throws BadRequestException")
        void getShipmentById_MultipleInvalidIds_ThrowsBadRequestException() {
        // Arrange
        Long shipmentId1 = -5L;
        Long shipmentId2 = 0L;

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex1 = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId1));
        com.example.SpringApi.Exceptions.BadRequestException ex2 = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId2));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex1.getMessage());
        assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex2.getMessage());
        }

        /**
         * Purpose: Verify negative ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Negative ID - Throws BadRequestException")
        void getShipmentById_NegativeId_ThrowsBadRequestException() {
        // Arrange
        Long shipmentId = -1L;

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
        }

        /**
         * Purpose: Verify non-existent shipment throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Not Found - Throws NotFoundException")
        void getShipmentById_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubShipmentRepositoryFindById(null);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID), ex.getMessage());
        }

        /**
         * Purpose: Verify null ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Null ID - Throws BadRequestException")
        void getShipmentById_NullId_ThrowsBadRequestException() {
        // Arrange
        Long shipmentId = null;

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
        }

        /**
         * Purpose: Verify empty ShipRocket order ID throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - ShipRocket Order ID Empty - Throws NotFoundException")
        void getShipmentById_ShipRocketOrderIdEmpty_ThrowsNotFoundException() {
        // Arrange
        testShipment.setShipRocketOrderId("");
        stubShipmentRepositoryFindById(testShipment);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID), ex.getMessage());
        }

        /**
         * Purpose: Verify missing ShipRocket order ID throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - ShipRocket Order ID Null - Throws NotFoundException")
        void getShipmentById_ShipRocketOrderIdNull_ThrowsNotFoundException() {
        // Arrange
        testShipment.setShipRocketOrderId(null);
        stubShipmentRepositoryFindById(testShipment);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID), ex.getMessage());
        }

        /**
         * Purpose: Verify whitespace ShipRocket order ID throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - ShipRocket Order ID Whitespace - Throws NotFoundException")
        void getShipmentById_ShipRocketOrderIdWhitespace_ThrowsNotFoundException() {
        // Arrange
        testShipment.setShipRocketOrderId("   ");
        stubShipmentRepositoryFindById(testShipment);

        // Act
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
            com.example.SpringApi.Exceptions.NotFoundException.class,
            () -> shippingService.getShipmentById(TEST_SHIPMENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NOT_FOUND, TEST_SHIPMENT_ID), ex.getMessage());
        }

        /**
         * Purpose: Verify zero ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: Exception type and message.
         */
        @Test
        @DisplayName("getShipmentById - Zero ID - Throws BadRequestException")
        void getShipmentById_ZeroId_ThrowsBadRequestException() {
        // Arrange
        Long shipmentId = 0L;

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentById(shipmentId));

        // Assert
        assertEquals(ErrorMessages.ShipmentErrorMessages.INVALID_ID, ex.getMessage());
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
    @DisplayName("getShipmentById - Controller Permission - Unauthorized")
    void getShipmentById_controller_permission_unauthorized() {
        // Arrange
        ShippingController controller = new ShippingController(shippingServiceMock);
        stubShippingServiceMockGetShipmentByIdUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getShipmentById(TEST_SHIPMENT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for getShipmentById.
     * Expected Result: Annotation exists and includes VIEW_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("getShipmentById - Verify @PreAuthorize Annotation")
    void getShipmentById_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("getShipmentById", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_SHIPMENTS_PERMISSION));
    }
}
