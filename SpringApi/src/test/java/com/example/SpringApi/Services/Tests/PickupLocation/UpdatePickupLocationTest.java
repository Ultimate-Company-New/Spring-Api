package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Services.PickupLocationService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.updatePickupLocation() method.
 * Extensive validation of updates and mapping updates.
 * Test Count: 40 tests
 */
@DisplayName("Update Pickup Location Tests")
class UpdatePickupLocationTest extends PickupLocationServiceTestBase {

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
        void updatePickupLocation_Success() throws Exception {
                PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER,
                                TEST_CLIENT_ID);
                existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(existingPickupLocation);
                when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));
                when(addressRepository.save(any())).thenReturn(testAddress);
                when(pickupLocationRepository.save(any())).thenReturn(existingPickupLocation);

                pickupLocationService.updatePickupLocation(testPickupLocationRequest);

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
                PickupLocation existing = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
                testPickupLocationRequest.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(existing);
                when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));
                when(addressRepository.save(any())).thenReturn(testAddress);
                when(pickupLocationRepository.save(any())).thenReturn(existing);

                pickupLocationService.updatePickupLocation(testPickupLocationRequest);

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
         * Purpose: Reject update when ID is max long and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Max Long ID - Throws NotFoundException")
        void updatePickupLocation_MaxLongId_ThrowsNotFoundException() {
                testPickupLocationRequest.setPickupLocationId(Long.MAX_VALUE);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE,
                                TEST_CLIENT_ID))
                                .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertTrue(ex.getMessage().contains(String.valueOf(Long.MAX_VALUE)));
        }

        /**
         * Purpose: Reject update when ID is min long and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Min Long ID - Throws NotFoundException")
        void updatePickupLocation_MinLongId_ThrowsNotFoundException() {
                testPickupLocationRequest.setPickupLocationId(Long.MIN_VALUE);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE,
                                TEST_CLIENT_ID))
                                .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertTrue(ex.getMessage().contains(String.valueOf(Long.MIN_VALUE)));
        }

        /**
         * Purpose: Reject update when ID is negative and not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Negative ID - Throws NotFoundException")
        void updatePickupLocation_NegativeId_ThrowsNotFoundException() {
                testPickupLocationRequest.setPickupLocationId(-100L);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(-100L, TEST_CLIENT_ID))
                                .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertTrue(ex.getMessage().contains("-100"));
        }

        /**
         * Purpose: Reject null address in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER001.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Address - Throws BadRequestException")
        void updatePickupLocation_NullAddress_ThrowsBadRequestException() {
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.setAddress(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setCity(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setCountry(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.setAddressNickName(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify null phone is accepted as optional field in update.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Phone - Success")
        void updatePickupLocation_NullPhone_Success() {
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setPhoneOnAddress(null);
                when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));
                when(addressRepository.save(any())).thenReturn(testAddress);
                when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setPostalCode(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(null));
                assertEquals("Pickup location request cannot be null.", ex.getMessage());
        }

        /**
         * Purpose: Reject null state in update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER003.
         */
        @Test
        @DisplayName("Update Pickup Location - Null State - Throws BadRequestException")
        void updatePickupLocation_NullState_ThrowsBadRequestException() {
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setState(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setStreetAddress(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Reject whitespace city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Message contains ER002.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace City - Throws BadRequestException")
        void updatePickupLocation_WhitespaceCity_ThrowsBadRequestException() {
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setCity("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setCountry("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.setAddressNickName("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify whitespace phone is trimmed and accepted in update.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Whitespace Phone - Success")
        void updatePickupLocation_WhitespacePhone_Success() {
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setPhoneOnAddress("   ");
                when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));
                when(addressRepository.save(any())).thenReturn(testAddress);
                when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setPostalCode("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setState("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                lenient()
                                .when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                                TEST_PICKUP_LOCATION_ID,
                                                TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                testPickupLocationRequest.getAddress().setStreetAddress("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
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
                testPickupLocationRequest.setPickupLocationId(0L);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID))
                                .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertTrue(ex.getMessage().contains("0"));
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
                // ARRANGE
                testPickupLocationRequest.setAddressNickName("Updated Warehouse");
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // ASSERT - CreatedAt should be unchanged (verify in repository save)
                verify(pickupLocationRepository).save(
                                argThat(location -> location.getPickupLocationId().equals(TEST_PICKUP_LOCATION_ID)));
        }

        /**
         * Purpose: Verify update modifies only specified address fields.
         * Expected Result: Only changed fields are updated, others remain unchanged.
         * Assertions: Update only modifies address nickname, not entire address.
         */
        @Test
        @DisplayName("Update Pickup Location - Modifies Only Changed Fields - Success")
        void updatePickupLocation_ModifiesOnlyChangedFields_Success() {
                // ARRANGE
                String newNickname = "New Main Warehouse";
                testPickupLocationRequest.setAddressNickName(newNickname);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

                // ASSERT
                verify(pickupLocationRepository)
                                .save(argThat(location -> location.getAddressNickName().equals(newNickname)));
        }

        /**
         * Purpose: Test update with very long nickname (boundary).
         * Expected Result: Long nickname accepted within system limits.
         * Assertions: Update succeeds with extended length nickname.
         */
        @Test
        @DisplayName("Update Pickup Location - Very Long Nickname - Success")
        void updatePickupLocation_VeryLongNickname_Success() {
                // ARRANGE
                String longNickname = "Regional Distribution Center - Main";
                testPickupLocationRequest.setAddressNickName(longNickname);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT & ASSERT
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
                // ARRANGE
                testPickupLocationRequest.getAddress().setStreetAddress("123 St. Paul's Ave #456 (South)");
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
                lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

                // ACT & ASSERT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Test update with international address characters.
         * Expected Result: UTF-8 characters handled correctly.
         * Assertions: Update succeeds with international characters.
         */
        @Test
        @DisplayName("Update Pickup Location - International Address - Success")
        void updatePickupLocation_InternationalAddress_Success() {
                // ARRANGE
                testPickupLocationRequest.getAddress().setCity("São Paulo");
                testPickupLocationRequest.getAddress().setCountry("Brasil");
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
                lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

                // ACT & ASSERT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Test update with very large pickup location ID.
         * Expected Result: Large ID value handled correctly.
         * Assertions: Update succeeds with large ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Very Large ID - Success")
        void updatePickupLocation_VeryLargeId_Success() {
                // ARRANGE
                Long largeId = 999999999999L;
                testPickupLocationRequest.setPickupLocationId(largeId);
                testPickupLocation.setPickupLocationId(largeId);
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                largeId, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT & ASSERT
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
                // ARRANGE
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT
                pickupLocationService.updatePickupLocation(testPickupLocationRequest);

                // ASSERT - Verify called with exact client ID
                verify(pickupLocationRepository).findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Test update when repository returns null on save.
         * Expected Result: Graceful handling of null repository response.
         * Assertions: Update handles null repository result.
         */
        @Test
        @DisplayName("Update Pickup Location - Repository Returns Null - Success")
        void updatePickupLocation_RepositoryReturnsNull_Success() {
                // ARRANGE
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(null);

                // ACT & ASSERT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Test update with numeric postal code variation.
         * Expected Result: Numeric postal codes accepted.
         * Assertions: Update succeeds with numeric postal code.
         */
        @Test
        @DisplayName("Update Pickup Location - Numeric Postal Code - Success")
        void updatePickupLocation_NumericPostalCode_Success() {
                // ARRANGE
                testPickupLocationRequest.getAddress().setPostalCode("987654");
                lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(
                                TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                                .thenReturn(testPickupLocation);
                lenient().when(addressRepository.save(any())).thenReturn(testAddress);
                lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

                // ACT & ASSERT
                assertDoesNotThrow(() -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        }

        /*
         **********************************************************************************************
         * CONTROLLER AUTHORIZATION TESTS
         **********************************************************************************************
         */

        @Test
        @DisplayName("updatePickupLocation - Verify @PreAuthorize Annotation")
        void updatePickupLocation_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                Method method = PickupLocationController.class.getMethod("updatePickupLocation", Long.class,
                                PickupLocationRequestModel.class);
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                assertNotNull(annotation, "@PreAuthorize annotation should be present on updatePickupLocation");
                assertTrue(annotation.value().contains(Authorizations.UPDATE_PICKUP_LOCATIONS_PERMISSION),
                                "@PreAuthorize should reference UPDATE_PICKUP_LOCATIONS_PERMISSION");
        }

        @Test
        @DisplayName("updatePickupLocation - Controller delegates to service")
        void updatePickupLocation_WithValidRequest_DelegatesToService() throws Exception {
                PickupLocationService mockService = mock(PickupLocationService.class);
                PickupLocationController controller = new PickupLocationController(mockService);
                doNothing().when(mockService).updatePickupLocation(testPickupLocationRequest);

                ResponseEntity<?> response = controller.updatePickupLocation(TEST_PICKUP_LOCATION_ID,
                                testPickupLocationRequest);

                verify(mockService).updatePickupLocation(testPickupLocationRequest);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }
}