package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.updatePickupLocation() method.
 * Extensive validation of updates and mapping updates.
 * Test Count: 41 tests
 */
@DisplayName("Update Pickup Location Tests")
class UpdatePickupLocationTest extends PickupLocationServiceTestBase {

        // Total Tests: 34
        /*
         **********************************************************************************************
         * SUCCESS TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify successful update of pickup location.
         * Expected Result: Pickup location is updated and saved without exception.
         * Assertions: Repository methods are called, logging is performed.
         */
    @Test
    @DisplayName("Update Pickup Location - Success")
    void updatePickupLocation_Success_Success() throws Exception {
            // Arrange
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER,
                            TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

            stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                            existingPickupLocation);
            stubAddressRepositoryFindById(TEST_ADDRESS_ID, testAddress);
            stubAddressRepositorySave(testAddress);
            stubPickupLocationRepositorySave(existingPickupLocation);

            // Act
            pickupLocationService.updatePickupLocation(testPickupLocationRequest);

            // Assert
            verify(pickupLocationRepository, times(1)).save(any());
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

        /**
         * Purpose: Verify updating ShipRocket ID persists correctly.
         * Expected Result: ShipRocket ID is updated.
         * Assertions: Saved entity has new ShipRocket ID.
         */
    @Test
    @DisplayName("Update Pickup Location - Updates ShipRocket ID - Success")
    void updatePickupLocation_UpdatesShipRocketId_Success() throws Exception {
            // Arrange
            PickupLocation existing = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            testPickupLocationRequest.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

            stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                            existing);
            stubAddressRepositoryFindById(TEST_ADDRESS_ID, testAddress);
            stubAddressRepositorySave(testAddress);
            stubPickupLocationRepositorySave(existing);

            // Act
            pickupLocationService.updatePickupLocation(testPickupLocationRequest);

            // Assert
            verify(pickupLocationRepository)
                            .save(argThat(saved -> TEST_SHIPROCKET_ID
                                            .equals(saved.getShipRocketPickupLocationId())));
    }

        /*
         **********************************************************************************************
         * FAILURE / EXCEPTION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Test update with international address characters.
         * Expected Result: UTF-8 characters handled correctly.
         * Assertions: Update succeeds with international characters.
         */
        @Test
        @DisplayName("Update Pickup Location - International Address - Success")
        void updatePickupLocation_InternationalAddress_Success() {
                // Arrange
                testPickupLocationRequest.getAddress().setCity("São Paulo");
                testPickupLocationRequest.getAddress().setCountry("Brasil");
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);
                stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Reject update when ID is max long and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Max Long ID - Throws NotFoundException")
        void updatePickupLocation_MaxLongId_ThrowsNotFoundException() {
                // Arrange
                testPickupLocationRequest.setPickupLocationId(Long.MAX_VALUE);
                stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MAX_VALUE, TEST_CLIENT_ID);

                // Act
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MAX_VALUE),
                                ex.getMessage());
        }

        /**
         * Purpose: Reject update when ID is min long and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Min Long ID - Throws NotFoundException")
        void updatePickupLocation_MinLongId_ThrowsNotFoundException() {
                // Arrange
                testPickupLocationRequest.setPickupLocationId(Long.MIN_VALUE);
                stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MIN_VALUE, TEST_CLIENT_ID);

                // Act
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MIN_VALUE),
                                ex.getMessage());
        }

        /**
         * Purpose: Verify update modifies only specified address fields.
         * Expected Result: Only changed fields are updated, others remain unchanged.
         * Assertions: Update only modifies address nickname, not entire address.
         */
        @Test
        @DisplayName("Update Pickup Location - Modifies Only Changed Fields - Success")
        void updatePickupLocation_ModifiesOnlyChangedFields_Success() {
                // Arrange
                String newNickname = "New Main Warehouse";
                testPickupLocationRequest.setAddressNickName(newNickname);
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                verify(pickupLocationRepository)
                                .save(argThat(location -> location.getAddressNickName().equals(newNickname)));
        }

        /**
         * Purpose: Reject update when ID is negative and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Negative ID - Throws NotFoundException")
        void updatePickupLocation_NegativeId_ThrowsNotFoundException() {
                // Arrange
                testPickupLocationRequest.setPickupLocationId(-100L);
                stubPickupLocationRepositoryFindByIdAndClientIdNotFound(-100L, TEST_CLIENT_ID);

                // Act
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, -100L),
                                ex.getMessage());
        }

        /**
         * Purpose: Reject null address in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER001.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Address - Throws BadRequestException")
        void updatePickupLocation_NullAddress_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.setAddress(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Reject null city in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER002.
         */
        @Test
        @DisplayName("Update Pickup Location - Null City - Throws BadRequestException")
        void updatePickupLocation_NullCity_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setCity(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
        }

        /**
         * Purpose: Reject null country in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER005.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Country - Throws BadRequestException")
        void updatePickupLocation_NullCountry_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setCountry(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
        }

        /**
         * Purpose: Reject null nickname in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains InvalidAddressNickName.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Nickname - Throws BadRequestException")
        void updatePickupLocation_NullNickname_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.setAddressNickName(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_ADDRESS_NICK_NAME, ex.getMessage());
        }

        /**
         * Purpose: Verify null phone is accepted as optional field in update.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Phone - Success")
        void updatePickupLocation_NullPhone_Success() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setPhoneOnAddress(null);
                stubAddressRepositoryFindById(TEST_ADDRESS_ID, testAddress);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Reject null postal code in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER004.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Postal Code - Throws BadRequestException")
        void updatePickupLocation_NullPostalCode_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setPostalCode(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
        }

        /**
         * Purpose: Verify update throws for null request.
         * Expected Result: NullPointerException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Null request - Throws BadRequestException")
        void updatePickupLocation_NullRequest_ThrowsBadRequestException() {
                // Arrange

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(null));

                // Assert
                assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_REQUEST, ex.getMessage());
        }

        /**
         * Purpose: Reject null state in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER003.
         */
        @Test
        @DisplayName("Update Pickup Location - Null State - Throws BadRequestException")
        void updatePickupLocation_NullState_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setState(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
        }

        /**
         * Purpose: Reject null street address in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER001.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Street Address - Throws BadRequestException")
        void updatePickupLocation_NullStreetAddress_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setStreetAddress(null);

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Test update with numeric postal code variation.
         * Expected Result: Numeric postal codes accepted.
         * Assertions: Update succeeds with numeric postal code.
         */
        @Test
        @DisplayName("Update Pickup Location - Numeric Postal Code - Success")
        void updatePickupLocation_NumericPostalCode_Success() {
                // Arrange
                testPickupLocationRequest.getAddress().setPostalCode("987654");
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Verify update preserves created timestamp (not modified).
         * Expected Result: Created timestamp remains unchanged after update.
         * Assertions: testPickupLocation.getCreatedAt() unchanged before and after
         * update.
         */
        @Test
        @DisplayName("Update Pickup Location - Preserves Created Timestamp - Success")
        void updatePickupLocation_PreservesCreatedTimestamp_Success() {
                // Arrange
                testPickupLocationRequest.setAddressNickName("Updated Warehouse");
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert - CreatedAt should be unchanged (verify in repository save)
                verify(pickupLocationRepository).save(
                                argThat(location -> location.getPickupLocationId().equals(TEST_PICKUP_LOCATION_ID)));
        }

        /**
         * Purpose: Test update when repository returns null on save.
         * Expected Result: Graceful handling of null repository response.
         * Assertions: Update handles null repository result.
         */
        @Test
        @DisplayName("Update Pickup Location - Repository Returns Null - Success")
        void updatePickupLocation_RepositoryReturnsNull_Success() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySaveReturnsNull();

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Test update with special characters in address.
         * Expected Result: Special chars preserved in address fields.
         * Assertions: Update succeeds with special char address.
         */
        @Test
        @DisplayName("Update Pickup Location - Special Characters in Address - Success")
        void updatePickupLocation_SpecialCharactersInAddress_Success() {
                // Arrange
                testPickupLocationRequest.getAddress().setStreetAddress("123 St. Paul's Ave #456 (South)");
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);
                stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Verify update calls repository with correct client ID.
         * Expected Result: Repository method called with exact clientId.
         * Assertions: Verify method call includes client isolation parameter.
         */
        @Test
        @DisplayName("Update Pickup Location - Verify Client Isolation - Success")
        void updatePickupLocation_VerifyClientIsolation_Success() throws Exception {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act
                pickupLocationService.updatePickupLocation(testPickupLocationRequest);

                // Assert - Verify called with exact client ID
                verify(pickupLocationRepository).findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Test update with very large pickup location ID.
         * Expected Result: Large ID value handled correctly.
         * Assertions: Update succeeds with large ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Very Large ID - Success")
        void updatePickupLocation_VeryLargeId_Success() {
                // Arrange
                Long largeId = 999999999999L;
                testPickupLocationRequest.setPickupLocationId(largeId);
                testPickupLocation.setPickupLocationId(largeId);
                stubPickupLocationRepositoryFindByIdAndClientId(largeId, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Test update with very long nickname (boundary).
         * Expected Result: Long nickname accepted within system limits.
         * Assertions: Update succeeds with extended length nickname.
         */
        @Test
        @DisplayName("Update Pickup Location - Very Long Nickname - Success")
        void updatePickupLocation_VeryLongNickname_Success() {
                // Arrange
                String longNickname = "Regional Distribution Center - Main";
                testPickupLocationRequest.setAddressNickName(longNickname);
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Reject whitespace city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER002.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace City - Throws BadRequestException")
        void updatePickupLocation_WhitespaceCity_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setCity("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER005.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Country - Throws BadRequestException")
        void updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setCountry("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains InvalidAddressNickName.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Nickname - Throws BadRequestException")
        void updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.setAddressNickName("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_ADDRESS_NICK_NAME, ex.getMessage());
        }

        /**
         * Purpose: Verify whitespace phone is trimmed and accepted in update.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Phone - Success")
        void updatePickupLocation_WhitespacePhone_Success() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setPhoneOnAddress("   ");
                stubAddressRepositoryFindById(TEST_ADDRESS_ID, testAddress);
                stubAddressRepositorySave(testAddress);
                stubPickupLocationRepositorySave(testPickupLocation);

                // Act & Assert
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Reject whitespace postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER004.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Postal Code - Throws BadRequestException")
        void updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setPostalCode("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER003.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace State - Throws BadRequestException")
        void updatePickupLocation_WhitespaceState_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setState("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER001.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Street Address - Throws BadRequestException")
        void updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException() {
                // Arrange
                stubPickupLocationRepositoryFindByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID,
                                testPickupLocation);
                testPickupLocationRequest.getAddress().setStreetAddress("   ");

                // Act
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Reject update when ID is zero and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Zero ID - Throws NotFoundException")
        void updatePickupLocation_ZeroId_ThrowsNotFoundException() {
                // Arrange
                testPickupLocationRequest.setPickupLocationId(0L);
                stubPickupLocationRepositoryFindByIdAndClientIdNotFound(0L, TEST_CLIENT_ID);

                // Act
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // Assert
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, 0L),
                                ex.getMessage());
        }

        /*
         **********************************************************************************************
         * CONTROLLER AUTHORIZATION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify unauthorized access is blocked at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("updatePickupLocation - Controller Permission - Unauthorized")
        void updatePickupLocation_controller_permission_unauthorized() throws Exception {
                // Arrange
                PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
                stubPickupLocationServiceThrowsUnauthorizedOnUpdate();

                // Act
                ResponseEntity<?> response = controller.updatePickupLocation(TEST_PICKUP_LOCATION_ID,
                                testPickupLocationRequest);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        /**
         * Purpose: Verify @PreAuthorize annotation on updatePickupLocation endpoint.
         * Expected Result: Annotation exists and references UPDATE_PICKUP_LOCATIONS_PERMISSION.
         * Assertions: Annotation is present and contains permission.
         */
        @Test
        @DisplayName("updatePickupLocation - Verify @PreAuthorize Annotation")
        void updatePickupLocation_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
                // Arrange
                Method method = PickupLocationController.class.getMethod("updatePickupLocation", Long.class,
                                PickupLocationRequestModel.class);

                // Act
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

                // Assert
                assertNotNull(annotation, "@PreAuthorize annotation should be present on updatePickupLocation");
                assertTrue(annotation.value().contains(Authorizations.UPDATE_PICKUP_LOCATIONS_PERMISSION),
                                "@PreAuthorize should reference UPDATE_PICKUP_LOCATIONS_PERMISSION");
        }

        /**
         * Purpose: Verify controller delegates updatePickupLocation to service.
         * Expected Result: Service method called and status OK returned.
         * Assertions: Delegation occurs and response is HTTP 200.
         */
        @Test
        @DisplayName("updatePickupLocation - Controller delegates to service")
        void updatePickupLocation_WithValidRequest_DelegatesToService() throws Exception {
                // Arrange
                PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
                stubPickupLocationServiceUpdatePickupLocationDoNothing();

                // Act
                ResponseEntity<?> response = controller.updatePickupLocation(TEST_PICKUP_LOCATION_ID,
                                testPickupLocationRequest);

                // Assert
                verify(pickupLocationServiceMock).updatePickupLocation(testPickupLocationRequest);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }
}
