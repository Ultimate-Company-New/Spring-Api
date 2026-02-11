package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.getWalletBalance().
 */
@DisplayName("GetWalletBalance Tests")
class GetWalletBalanceTest extends ShippingServiceTestBase {

    // Total Tests: 3

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify wallet balance returns value.
     * Expected Result: Balance returned.
     * Assertions: Balance equals expected.
     */
    @Test
    @DisplayName("getWalletBalance - Success")
    void getWalletBalance_Success() {
        // Arrange
        stubClientServiceGetClientById(testClientResponse);
        stubShipRocketHelperGetWalletBalance(123.45);

        // Act
        Double result = shippingService.getWalletBalance();

        // Assert
        assertEquals(123.45, result);
    }

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify missing credentials throws BadRequestException.
     * Expected Result: BadRequestException with ShipRocketCredentialsNotConfigured message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getWalletBalance - Credentials Missing - Throws BadRequestException")
    void getWalletBalance_CredentialsMissing_ThrowsBadRequestException() {
        // Arrange
        testClientResponse.setShipRocketPassword(null);
        stubClientServiceGetClientById(testClientResponse);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> shippingService.getWalletBalance());

        // Assert
        assertEquals(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify controller has @PreAuthorize for getWalletBalance.
     * Expected Result: Annotation exists and includes VIEW_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("getWalletBalance - Verify @PreAuthorize Annotation")
    void getWalletBalance_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("getWalletBalance");

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_SHIPMENTS_PERMISSION));
    }
}
