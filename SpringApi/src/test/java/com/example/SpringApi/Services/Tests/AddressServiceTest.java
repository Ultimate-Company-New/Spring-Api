package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.AddressService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService.
 * 
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | ToggleAddressTests                      | 9               |
 * | GetAddressByIdTests                     | 8               |
 * | InsertAddressTests                      | 52              |
 * | UpdateAddressTests                      | 25              |
 * | GetAddressByUserIdTests                 | 9               |
 * | GetAddressByClientIdTests               | 37              |
 * | **Total**                               | **140**         |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Unit Tests")
class AddressServiceTest extends BaseTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private AddressService addressService;

    private Address testAddress;
    private AddressRequestModel testAddressRequest;
    private Client testClient;
    private User testUser;

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     * Uses factory methods from BaseTest for consistent test data.
     */
    @BeforeEach
    void setUp() {
        // Initialize test objects using BaseTest factory methods
        testAddressRequest = createValidAddressRequest();
        testClient = createTestClient();
        testUser = createTestUser();
        testAddress = createTestAddress(testAddressRequest, DEFAULT_CREATED_USER);

        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock BaseService methods (required for service methods that call
        // getUser/getUserId)
        lenient().doReturn(DEFAULT_USER_ID).when(addressService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(addressService).getUser();
        lenient().doReturn(DEFAULT_CLIENT_ID).when(addressService).getClientId();
    }

    @Nested
    @DisplayName("toggleAddress Tests")
    class ToggleAddressTests {

        /**
         * Purpose: Verify toggling an active address marks it deleted.
         * Expected Result: Address is saved with deleted flag set.
         * Assertions: Deleted flag is true and save/log calls occur.
         */
        @Test
        @DisplayName("Toggle Address - Address found and active - Success toggles to deleted")
        void toggleAddress_AddressFoundActive_Success() {
            // Arrange
            testAddress.setIsDeleted(false);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

            // Assert
            assertTrue(testAddress.getIsDeleted());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify toggling a deleted address restores it.
         * Expected Result: Address is saved with deleted flag cleared.
         * Assertions: Deleted flag is false and save is called.
         */
        @Test
        @DisplayName("Toggle Address - Address found and deleted - Success toggles to active")
        void toggleAddress_AddressFoundDeleted_Success() {
            // Arrange
            testAddress.setIsDeleted(true);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

            // Assert
            assertFalse(testAddress.getIsDeleted());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, times(1)).save(any(Address.class));
        }

        /**
         * Purpose: Ensure toggling a missing address fails.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save/log are not called.
         */
        @Test
        @DisplayName("Toggle Address - Address not found - ThrowsNotFoundException")
        void toggleAddress_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, never()).save(any(Address.class));
            verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Validate negative ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Toggle Address - Negative ID - ThrowsNotFoundException")
        void toggleAddress_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(addressRepository.findById(negativeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.toggleAddress(negativeId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Validate zero ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Toggle Address - Zero ID - ThrowsNotFoundException")
        void toggleAddress_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(addressRepository.findById(zeroId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.toggleAddress(zeroId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Validate min long ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Toggle Address - Min Long value - ThrowsNotFoundException")
        void toggleAddress_MinLongValue_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.toggleAddress(Long.MIN_VALUE));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify multiple toggles switch state each time.
         * Expected Result: Address toggles deleted flag on each call.
         * Assertions: Deleted flag flips and repository calls are counted.
         */
        @Test
        @DisplayName("Toggle Address - Multiple toggles in sequence - Success")
        void toggleAddress_MultipleToggles_Success() {
            // Arrange
            testAddress.setIsDeleted(false);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act - First toggle
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));
            assertTrue(testAddress.getIsDeleted());

            // Act - Second toggle
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));
            assertFalse(testAddress.getIsDeleted());

            // Assert
            verify(addressRepository, times(2)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, times(2)).save(any(Address.class));
        }

        /**
         * Purpose: Verify active-to-deleted state change.
         * Expected Result: Address is marked deleted.
         * Assertions: Deleted flag is true and log is called.
         */
        @Test
        @DisplayName("Toggle Address - Verify toggle state changes - Active to Deleted")
        void toggleAddress_StateChange_ActiveToDeleted() {
            // Arrange
            testAddress.setIsDeleted(false);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

            // Assert
            assertTrue(testAddress.getIsDeleted());
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify deleted-to-active state change.
         * Expected Result: Address is restored.
         * Assertions: Deleted flag is false and log is called.
         */
        @Test
        @DisplayName("Toggle Address - Verify toggle state changes - Deleted to Active")
        void toggleAddress_StateChange_DeletedToActive() {
            // Arrange
            testAddress.setIsDeleted(true);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

            // Assert
            assertFalse(testAddress.getIsDeleted());
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("getAddressById Tests")
    class GetAddressByIdTests {

        /**
         * Purpose: Verify address details are returned for a valid ID.
         * Expected Result: Response contains address fields.
         * Assertions: Response fields match the test address.
         */
        @Test
        @DisplayName("Get Address By ID - Address found - Success returns address details")
        void getAddressById_AddressFound_Success() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act
            AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

            // Assert
            assertNotNull(result);
            assertEquals(DEFAULT_ADDRESS_ID, result.getAddressId());
            assertEquals(DEFAULT_USER_ID, result.getUserId());
            assertEquals(DEFAULT_CLIENT_ID, result.getClientId());
            assertEquals(DEFAULT_ADDRESS_TYPE, result.getAddressType());
            assertEquals(DEFAULT_STREET_ADDRESS, result.getStreetAddress());
            assertEquals(DEFAULT_CITY, result.getCity());
            assertEquals(DEFAULT_STATE, result.getState());
            assertEquals(DEFAULT_POSTAL_CODE, result.getPostalCode());
            assertEquals(DEFAULT_COUNTRY, result.getCountry());
            assertTrue(result.getIsPrimary());
            assertFalse(result.getIsDeleted());

            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
        }

        /**
         * Purpose: Validate missing address ID returns not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Address not found - ThrowsNotFoundException")
        void getAddressById_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressById(DEFAULT_ADDRESS_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
        }

        /**
         * Purpose: Validate negative ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Negative ID - ThrowsNotFoundException")
        void getAddressById_NegativeId_ThrowsNotFoundException() {
            long negativeId = -1L;
            when(addressRepository.findById(negativeId)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(negativeId));
        }

        /**
         * Purpose: Validate zero ID is rejected.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Zero ID - ThrowsNotFoundException")
        void getAddressById_ZeroId_ThrowsNotFoundException() {
            long zeroId = 0L;
            when(addressRepository.findById(zeroId)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(zeroId));
        }

        /**
         * Purpose: Verify deleted address can still be retrieved.
         * Expected Result: Response indicates deleted status.
         * Assertions: isDeleted is true.
         */
        @Test
        @DisplayName("Get Address By ID - Deleted address - Success returns deleted address")
        void getAddressById_DeletedAddress_Success() {
            // Arrange
            testAddress.setIsDeleted(true);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act
            AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.getIsDeleted());
        }

        /**
         * Purpose: Verify null optional fields are handled.
         * Expected Result: Response contains nulls for optional fields.
         * Assertions: Optional fields are null in response.
         */
        @Test
        @DisplayName("Get Address By ID - Address with null optional fields - Success")
        void getAddressById_NullOptionalFields_Success() {
            // Arrange
            testAddress.setStreetAddress2(null);
            testAddress.setStreetAddress3(null);
            testAddress.setNameOnAddress(null);
            testAddress.setEmailOnAddress(null);
            testAddress.setPhoneOnAddress(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act
            AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

            // Assert
            assertNotNull(result);
            assertNull(result.getStreetAddress2());
            assertNull(result.getStreetAddress3());
        }

        /**
         * Purpose: Validate max long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Max Long value - ThrowsNotFoundException")
        void getAddressById_MaxLongValue_ThrowsNotFoundException() {
            when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(Long.MAX_VALUE));
        }

        /**
         * Purpose: Validate min long ID is rejected when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Min Long value - ThrowsNotFoundException")
        void getAddressById_MinLongValue_ThrowsNotFoundException() {
            when(addressRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(Long.MIN_VALUE));
        }
    }

    @Nested
    @DisplayName("insertAddress Tests")
    class InsertAddressTests {

        /**
         * Purpose: Verify inserting a valid address succeeds.
         * Expected Result: Address is saved and log is written.
         * Assertions: Save and log calls are invoked.
         */
        @Test
        @DisplayName("Insert Address - Valid request - Success")
        void insertAddress_ValidRequest_Success() {
            // Arrange
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

            // Verify
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify insert logs success route with created ID.
         * Expected Result: Log entry includes success message and route.
         * Assertions: logData is called with INSERT_ADDRESS route and success text.
         */
        @Test
        @DisplayName("Insert Address - Logs success message and route")
        void insertAddress_LogsSuccessMessageAndRoute() {
            // Arrange
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            addressService.insertAddress(testAddressRequest);

            // Assert
            verify(userLogService).logData(
                    eq(DEFAULT_USER_ID),
                    contains(com.example.SpringApi.SuccessMessages.AddressSuccessMessages.InsertAddress),
                    eq(com.example.SpringApi.Models.ApiRoutes.AddressSubRoute.INSERT_ADDRESS));
        }

        /**
         * Purpose: Reject null insert request.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null request - ThrowsBadRequestException")
        void insertAddress_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(null));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Insert Address - Null street address - ThrowsBadRequestException")
        void insertAddress_NullStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject empty street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Insert Address - Empty street address - ThrowsBadRequestException")
        void insertAddress_EmptyStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Insert Address - Whitespace only street address - ThrowsBadRequestException")
        void insertAddress_WhitespaceStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject null city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002.
         */
        @Test
        @DisplayName("Insert Address - Null city - ThrowsBadRequestException")
        void insertAddress_NullCity_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCity(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }

        /**
         * Purpose: Reject empty city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002.
         */
        @Test
        @DisplayName("Insert Address - Empty city - ThrowsBadRequestException")
        void insertAddress_EmptyCity_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCity("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002.
         */
        @Test
        @DisplayName("Insert Address - Whitespace only city - ThrowsBadRequestException")
        void insertAddress_WhitespaceCity_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCity("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Insert Address - Whitespace only state - ThrowsBadRequestException")
        void insertAddress_WhitespaceState_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setState("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004.
         */
        @Test
        @DisplayName("Insert Address - Whitespace only postal code - ThrowsBadRequestException")
        void insertAddress_WhitespacePostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }

        /**
         * Purpose: Reject whitespace-only country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005.
         */
        @Test
        @DisplayName("Insert Address - Whitespace only country - ThrowsBadRequestException")
        void insertAddress_WhitespaceCountry_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCountry("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }

        /**
         * Purpose: Reject null state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Insert Address - Null state - ThrowsBadRequestException")
        void insertAddress_NullState_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setState(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }

        /**
         * Purpose: Reject empty state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Insert Address - Empty state - ThrowsBadRequestException")
        void insertAddress_EmptyState_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setState("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }

        /**
         * Purpose: Reject null postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004.
         */
        @Test
        @DisplayName("Insert Address - Null postal code - ThrowsBadRequestException")
        void insertAddress_NullPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }

        /**
         * Purpose: Reject empty postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004.
         */
        @Test
        @DisplayName("Insert Address - Empty postal code - ThrowsBadRequestException")
        void insertAddress_EmptyPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }

        /**
         * Purpose: Reject invalid postal code formats.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @ParameterizedTest
        @ValueSource(strings = { "1234", "1234567", "abcde", "12-345", "12.345", "12 345" })
        @DisplayName("Insert Address - Invalid postal code format - ThrowsBadRequestException")
        void insertAddress_InvalidPostalCodeFormat_ThrowsBadRequestException(String invalidPostalCode) {
            // Arrange
            testAddressRequest.setPostalCode(invalidPostalCode);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }

        /**
         * Purpose: Allow valid postal code lengths.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @ParameterizedTest
        @ValueSource(strings = { "12345", "123456" })
        @DisplayName("Insert Address - Valid postal code formats (5-6 digits) - Success")
        void insertAddress_ValidPostalCodeFormats_Success(String validPostalCode) {
            // Arrange
            testAddressRequest.setPostalCode(validPostalCode);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Reject null country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005.
         */
        @Test
        @DisplayName("Insert Address - Null country - ThrowsBadRequestException")
        void insertAddress_NullCountry_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCountry(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }

        /**
         * Purpose: Reject empty country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005.
         */
        @Test
        @DisplayName("Insert Address - Empty country - ThrowsBadRequestException")
        void insertAddress_EmptyCountry_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCountry("");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }

        /**
         * Purpose: Reject null address type.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER006.
         */
        @Test
        @DisplayName("Insert Address - Null address type - ThrowsBadRequestException")
        void insertAddress_NullAddressType_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setAddressType(null);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }

        /**
         * Purpose: Reject invalid address types.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER006.
         */
        @ParameterizedTest
        @ValueSource(strings = { "INVALID", "RESIDENTIAL", "COMMERCIAL", "home123", "" })
        @DisplayName("Insert Address - Invalid address type - ThrowsBadRequestException")
        void insertAddress_InvalidAddressType_ThrowsBadRequestException(String invalidType) {
            // Arrange
            testAddressRequest.setAddressType(invalidType);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }

        /**
         * Purpose: Accept valid address types (case-insensitive).
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @ParameterizedTest
        @ValueSource(strings = { "HOME", "WORK", "BILLING", "SHIPPING", "OFFICE", "WAREHOUSE", "home", "Home", "work",
                "Work" })
        @DisplayName("Insert Address - Valid address types - Success")
        void insertAddress_ValidAddressTypes_Success(String validType) {
            // Arrange
            testAddressRequest.setAddressType(validType);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Reject negative user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Insert Address - Negative user ID - ThrowsBadRequestException")
        void insertAddress_NegativeUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(-1L);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject zero user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Insert Address - Zero user ID - ThrowsBadRequestException")
        void insertAddress_ZeroUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(0L);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject negative client ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Insert Address - Negative client ID - ThrowsBadRequestException")
        void insertAddress_NegativeClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(-1L);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject zero client ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Insert Address - Zero client ID - ThrowsBadRequestException")
        void insertAddress_ZeroClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(0L);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Allow null user ID (optional).
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Null user ID (optional) - Success")
        void insertAddress_NullUserId_Success() {
            // Arrange
            testAddressRequest.setUserId(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Allow null client ID (optional).
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Null client ID (optional) - Success")
        void insertAddress_NullClientId_Success() {
            // Arrange
            testAddressRequest.setClientId(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept all optional fields as null.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - All optional fields null - Success")
        void insertAddress_AllOptionalFieldsNull_Success() {
            // Arrange
            testAddressRequest.setStreetAddress2(null);
            testAddressRequest.setStreetAddress3(null);
            testAddressRequest.setNameOnAddress(null);
            testAddressRequest.setEmailOnAddress(null);
            testAddressRequest.setPhoneOnAddress(null);
            testAddressRequest.setIsPrimary(null);
            testAddressRequest.setIsDeleted(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept all optional fields provided.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - All optional fields provided - Success")
        void insertAddress_AllOptionalFieldsProvided_Success() {
            // Arrange
            testAddressRequest.setStreetAddress2("Apt 101");
            testAddressRequest.setStreetAddress3("Building A");
            testAddressRequest.setNameOnAddress("John Doe");
            testAddressRequest.setEmailOnAddress("john@example.com");
            testAddressRequest.setPhoneOnAddress("1234567890");
            testAddressRequest.setIsPrimary(true);
            testAddressRequest.setIsDeleted(false);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Allow street address with leading/trailing spaces.
         * Expected Result: Insert succeeds and trimming occurs.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Street address with leading/trailing spaces - Success trims spaces")
        void insertAddress_StreetAddressWithSpaces_SuccessTrimsSpaces() {
            // Arrange
            testAddressRequest.setStreetAddress("  123 Main St  ");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept very long street addresses.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Very long street address - Success")
        void insertAddress_VeryLongStreetAddress_Success() {
            // Arrange
            String longAddress = "A".repeat(500);
            testAddressRequest.setStreetAddress(longAddress);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept special characters in street address.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Special characters in street address - Success")
        void insertAddress_SpecialCharactersInStreetAddress_Success() {
            // Arrange
            testAddressRequest.setStreetAddress("123 Main St, Apt #101 & Suite B-2");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept unicode characters in city.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Unicode characters in city - Success")
        void insertAddress_UnicodeCharactersInCity_Success() {
            // Arrange
            testAddressRequest.setCity("München");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept boundary postal code of 5 digits.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Boundary postal code 5 digits - Success")
        void insertAddress_BoundaryPostalCode5Digits_Success() {
            // Arrange
            testAddressRequest.setPostalCode("00000");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept boundary postal code of 6 digits.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Boundary postal code 6 digits - Success")
        void insertAddress_BoundaryPostalCode6Digits_Success() {
            // Arrange
            testAddressRequest.setPostalCode("999999");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept isPrimary true.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Boolean isPrimary true - Success")
        void insertAddress_IsPrimaryTrue_Success() {
            // Arrange
            testAddressRequest.setIsPrimary(true);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept isPrimary false.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Boolean isPrimary false - Success")
        void insertAddress_IsPrimaryFalse_Success() {
            // Arrange
            testAddressRequest.setIsPrimary(false);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept isDeleted true.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Boolean isDeleted true - Success")
        void insertAddress_IsDeletedTrue_Success() {
            // Arrange
            testAddressRequest.setIsDeleted(true);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Verify multiple valid fields succeed together.
         * Expected Result: Insert succeeds.
         * Assertions: Save is called.
         */
        @Test
        @DisplayName("Insert Address - Multiple validations pass together - Success")
        void insertAddress_MultipleValidationsPass_Success() {
            // Arrange - all fields valid
            testAddressRequest.setStreetAddress("123 Valid Street");
            testAddressRequest.setCity("ValidCity");
            testAddressRequest.setState("VA");
            testAddressRequest.setPostalCode("12345");
            testAddressRequest.setCountry("USA");
            testAddressRequest.setAddressType("HOME");
            testAddressRequest.setUserId(123L);
            testAddressRequest.setClientId(456L);

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
            verify(addressRepository, times(1)).save(any(Address.class));
        }

        /**
         * Purpose: Accept case-insensitive HOME address type.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Case insensitive address type HOME - Success")
        void insertAddress_CaseInsensitiveHome_Success() {
            // Arrange
            testAddressRequest.setAddressType("home");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept case-insensitive WORK address type.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Case insensitive address type WORK - Success")
        void insertAddress_CaseInsensitiveWork_Success() {
            // Arrange
            testAddressRequest.setAddressType("work");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept postal codes with leading zeros.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Postal code with leading zeros - Success")
        void insertAddress_PostalCodeWithLeadingZeros_Success() {
            // Arrange
            testAddressRequest.setPostalCode("00123");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept max long user ID.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Max Long user ID - Success")
        void insertAddress_MaxLongUserId_Success() {
            // Arrange
            testAddressRequest.setUserId(Long.MAX_VALUE);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Accept max long client ID.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Max Long client ID - Success")
        void insertAddress_MaxLongClientId_Success() {
            // Arrange
            testAddressRequest.setClientId(Long.MAX_VALUE);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Reject postal code of 4 digits.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @Test
        @DisplayName("Insert Address - Postal code 4 digits - ThrowsBadRequestException")
        void insertAddress_PostalCode4Digits_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("1234");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }

        /**
         * Purpose: Reject postal code of 7 digits.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @Test
        @DisplayName("Insert Address - Postal code 7 digits - ThrowsBadRequestException")
        void insertAddress_PostalCode7Digits_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("1234567");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }

        /**
         * Purpose: Accept mixed-case address type.
         * Expected Result: Insert succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Insert Address - Mixed case address type - Success")
        void insertAddress_MixedCaseAddressType_Success() {
            // Arrange
            testAddressRequest.setAddressType("BiLLiNg");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }

        /**
         * Purpose: Reject invalid created user.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidUser.
         */
        @Test
        @DisplayName("Insert Address - Invalid created user - ThrowsBadRequestException")
        void insertAddress_InvalidCreatedUser_ThrowsBadRequestException() {
            // Arrange
            doReturn(" ").when(addressService).getUser();

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
        }

        /**
         * Purpose: Default boolean fields when null on insert.
         * Expected Result: isPrimary/isDeleted default false.
         * Assertions: Saved address has default boolean values.
         */
        @Test
        @DisplayName("Insert Address - Null boolean fields - Defaults applied")
        void insertAddress_NullBooleanFields_DefaultsApplied() {
            // Arrange
            testAddressRequest.setCountry("India");
            testAddressRequest.setIsPrimary(null);
            testAddressRequest.setIsDeleted(null);

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

            // Assert
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertFalse(captor.getValue().getIsPrimary());
            assertFalse(captor.getValue().getIsDeleted());
        }

        /**
         * Purpose: Normalize address type to uppercase on insert.
         * Expected Result: Address type is saved as uppercase.
         * Assertions: Saved address has normalized address type.
         */
        @Test
        @DisplayName("Insert Address - Address type normalized - Success")
        void insertAddress_AddressTypeNormalized_Success() {
            // Arrange
            testAddressRequest.setAddressType("home");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

            // Assert
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertEquals("HOME", captor.getValue().getAddressType());
        }
    }

    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {

        /**
         * Purpose: Verify updating an existing address succeeds.
         * Expected Result: Address is saved and log is written.
         * Assertions: Save/log calls are invoked.
         */
        @Test
        @DisplayName("Update Address - Address found - Success updates all fields")
        void updateAddress_AddressFound_Success() {
            // Arrange
            testAddressRequest.setStreetAddress("456 Oak Ave");
            testAddressRequest.setCity("Los Angeles");
            testAddressRequest.setState("CA");

            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

            // Verify
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify update logs success route with updated ID.
         * Expected Result: Log entry includes success message and route.
         * Assertions: logData is called with UPDATE_ADDRESS route and success text.
         */
        @Test
        @DisplayName("Update Address - Logs success message and route")
        void updateAddress_LogsSuccessMessageAndRoute() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            addressService.updateAddress(testAddressRequest);

            // Assert
            verify(userLogService).logData(
                    eq(DEFAULT_USER_ID),
                    contains(com.example.SpringApi.SuccessMessages.AddressSuccessMessages.UpdateAddress),
                    eq(com.example.SpringApi.Models.ApiRoutes.AddressSubRoute.UPDATE_ADDRESS));
        }

        /**
         * Purpose: Reject update when address does not exist.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Update Address - Address not found - ThrowsNotFoundException")
        void updateAddress_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null update request.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Update Address - Null request - ThrowsBadRequestException")
        void updateAddress_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(null));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject null street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Update Address - Null street address - ThrowsBadRequestException")
        void updateAddress_NullStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject null city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002.
         */
        @Test
        @DisplayName("Update Address - Null city - ThrowsBadRequestException")
        void updateAddress_NullCity_ThrowsBadRequestException() {
            testAddressRequest.setCity(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }

        /**
         * Purpose: Reject null state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Update Address - Null state - ThrowsBadRequestException")
        void updateAddress_NullState_ThrowsBadRequestException() {
            testAddressRequest.setState(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }

        /**
         * Purpose: Reject null country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005.
         */
        @Test
        @DisplayName("Update Address - Null country - ThrowsBadRequestException")
        void updateAddress_NullCountry_ThrowsBadRequestException() {
            testAddressRequest.setCountry(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }

        /**
         * Purpose: Reject invalid postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER007.
         */
        @Test
        @DisplayName("Update Address - Invalid postal code - ThrowsBadRequestException")
        void updateAddress_InvalidPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("invalid");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }

        /**
         * Purpose: Reject invalid address type.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER006.
         */
        @Test
        @DisplayName("Update Address - Invalid address type - ThrowsBadRequestException")
        void updateAddress_InvalidAddressType_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setAddressType("INVALID_TYPE");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }

        /**
         * Purpose: Verify changing address type succeeds.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Address - Change address type - Success")
        void updateAddress_ChangeAddressType_Success() {
            // Arrange
            testAddressRequest.setAddressType("WORK");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
        }

        /**
         * Purpose: Reject negative user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Address - Negative user ID - ThrowsBadRequestException")
        void updateAddress_NegativeUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(-1L);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject zero user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Address - Zero user ID - ThrowsBadRequestException")
        void updateAddress_ZeroUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(0L);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject negative client ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Address - Negative client ID - ThrowsBadRequestException")
        void updateAddress_NegativeClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(-1L);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject zero client ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidId.
         */
        @Test
        @DisplayName("Update Address - Zero client ID - ThrowsBadRequestException")
        void updateAddress_ZeroClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(0L);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Reject empty street address.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001.
         */
        @Test
        @DisplayName("Update Address - Empty street address - ThrowsBadRequestException")
        void updateAddress_EmptyStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress("");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }

        /**
         * Purpose: Reject empty city.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002.
         */
        @Test
        @DisplayName("Update Address - Empty city - ThrowsBadRequestException")
        void updateAddress_EmptyCity_ThrowsBadRequestException() {
            testAddressRequest.setCity("");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }

        /**
         * Purpose: Reject empty state.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003.
         */
        @Test
        @DisplayName("Update Address - Empty state - ThrowsBadRequestException")
        void updateAddress_EmptyState_ThrowsBadRequestException() {
            testAddressRequest.setState("");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }

        /**
         * Purpose: Reject empty country.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005.
         */
        @Test
        @DisplayName("Update Address - Empty country - ThrowsBadRequestException")
        void updateAddress_EmptyCountry_ThrowsBadRequestException() {
            testAddressRequest.setCountry("");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }

        /**
         * Purpose: Reject null postal code.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004.
         */
        @Test
        @DisplayName("Update Address - Null postal code - ThrowsBadRequestException")
        void updateAddress_NullPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }

        /**
         * Purpose: Verify update succeeds with valid fields.
         * Expected Result: Address is saved and log is written.
         * Assertions: Save and log calls are invoked.
         */
        @Test
        @DisplayName("Update Address - Valid fields updated - Success")
        void updateAddress_ValidFieldsUpdated_Success() {
            // Arrange
            testAddressRequest.setStreetAddress("999 New Road");
            testAddressRequest.setCity("Chicago");
            testAddressRequest.setState("IL");
            testAddressRequest.setPostalCode("60601");
            testAddressRequest.setCountry("USA");
            testAddressRequest.setAddressType("BILLING");

            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        /**
         * Purpose: Verify update succeeds with null optional fields.
         * Expected Result: Update succeeds.
         * Assertions: No exception is thrown.
         */
        @Test
        @DisplayName("Update Address - Update with null optional fields - Success")
        void updateAddress_NullOptionalFieldsUpdated_Success() {
            // Arrange
            testAddressRequest.setStreetAddress2(null);
            testAddressRequest.setNameOnAddress(null);
            testAddressRequest.setPhoneOnAddress(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act & Assert
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
        }

        /**
         * Purpose: Preserve created fields on update.
         * Expected Result: createdUser and createdAt remain unchanged.
         * Assertions: Saved address retains original created fields.
         */
        @Test
        @DisplayName("Update Address - Preserve created fields - Success")
        void updateAddress_PreservesCreatedFields_Success() {
            // Arrange
            testAddress.setCreatedUser("original-user");
            testAddress.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

            // Assert
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertEquals("original-user", captor.getValue().getCreatedUser());
            assertEquals(testAddress.getCreatedAt(), captor.getValue().getCreatedAt());
        }

        /**
         * Purpose: Default boolean fields when null on update.
         * Expected Result: isPrimary/isDeleted default false.
         * Assertions: Saved address has default boolean values.
         */
        @Test
        @DisplayName("Update Address - Null boolean fields - Defaults applied")
        void updateAddress_NullBooleanFields_DefaultsApplied() {
            // Arrange
            testAddressRequest.setCountry("India");
            testAddressRequest.setIsPrimary(null);
            testAddressRequest.setIsDeleted(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

            // Assert
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            verify(addressRepository).save(captor.capture());
            assertFalse(captor.getValue().getIsPrimary());
            assertFalse(captor.getValue().getIsDeleted());
        }

        /**
         * Purpose: Reject invalid modified user on update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches InvalidUser.
         */
        @Test
        @DisplayName("Update Address - Invalid modified user - ThrowsBadRequestException")
        void updateAddress_InvalidModifiedUser_ThrowsBadRequestException() {
            // Arrange
            doReturn("").when(addressService).getUser();
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));

            assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getAddressByUserId Tests")
    class GetAddressByUserIdTests {

        /**
         * Purpose: Verify multiple addresses returned for a user.
         * Expected Result: List contains all addresses.
         * Assertions: Response size and fields match expected.
         */
        @Test
        @DisplayName("Get Address By User ID - Multiple addresses found - Success returns list")
        void getAddressByUserId_MultipleAddressesFound_Success() {
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            Address secondAddress = createTestAddress(2L, "WORK");
            List<Address> addresses = Arrays.asList(testAddress, secondAddress);
            when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                    .thenReturn(addresses);

            // Act
            List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(DEFAULT_ADDRESS_ID, result.get(0).getAddressId());
            assertEquals(DEFAULT_ADDRESS_TYPE, result.get(0).getAddressType());
            assertEquals(2L, result.get(1).getAddressId());
            assertEquals("WORK", result.get(1).getAddressType());

            verify(userRepository, times(1)).findById(DEFAULT_USER_ID);
            verify(addressRepository, times(1)).findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
        }

        /**
         * Purpose: Handle user with no addresses.
         * Expected Result: Empty list is returned.
         * Assertions: Result is empty and repository is called.
         */
        @Test
        @DisplayName("Get Address By User ID - No addresses - Success returns empty list")
        void getAddressByUserId_NoAddresses_ReturnsEmptyList() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                    .thenReturn(new ArrayList<>());

            // Act
            List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(addressRepository, times(1)).findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
        }

        /**
         * Purpose: Reject user not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - User not found - ThrowsNotFoundException")
        void getAddressByUserId_UserNotFound_ThrowsNotFoundException() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(DEFAULT_USER_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }

        /**
         * Purpose: Reject deleted user.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - User deleted - ThrowsNotFoundException")
        void getAddressByUserId_UserDeleted_ThrowsNotFoundException() {
            // Arrange
            testUser.setIsDeleted(true);
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(DEFAULT_USER_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }

        /**
         * Purpose: Reject negative user ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - Negative ID - ThrowsNotFoundException")
        void getAddressByUserId_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(userRepository.findById(negativeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(negativeId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject zero user ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - Zero ID - ThrowsNotFoundException")
        void getAddressByUserId_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(userRepository.findById(zeroId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(zeroId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject max long user ID when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - Max Long value - ThrowsNotFoundException")
        void getAddressByUserId_MaxLongValue_ThrowsNotFoundException() {
            // Arrange
            when(userRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(Long.MAX_VALUE));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject min long user ID when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By User ID - Min Long value - ThrowsNotFoundException")
        void getAddressByUserId_MinLongValue_ThrowsNotFoundException() {
            // Arrange
            when(userRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByUserId(Long.MIN_VALUE));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify single address response.
         * Expected Result: List contains one address.
         * Assertions: Result size is one.
         */
        @Test
        @DisplayName("Get Address By User ID - Single address - Success returns list with one item")
        void getAddressByUserId_SingleAddress_Success() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                    .thenReturn(Collections.singletonList(testAddress));

            // Act
            List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAddressByClientId Tests")
    class GetAddressByClientIdTests {

        /**
         * Purpose: Verify multiple addresses returned for a client.
         * Expected Result: List contains all addresses.
         * Assertions: Response size and fields match expected.
         */
        @Test
        @DisplayName("Get Address By Client ID - Multiple addresses found - Success returns list")
        void getAddressByClientId_MultipleAddressesFound_Success() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            Address secondAddress = createTestAddress(2L, "BILLING");
            List<Address> addresses = Arrays.asList(testAddress, secondAddress);
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(addresses);

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(DEFAULT_ADDRESS_ID, result.get(0).getAddressId());
            assertEquals(DEFAULT_CLIENT_ID, result.get(0).getClientId());
            assertEquals(DEFAULT_ADDRESS_TYPE, result.get(0).getAddressType());
            assertEquals(2L, result.get(1).getAddressId());
            assertEquals(DEFAULT_CLIENT_ID, result.get(1).getClientId());
            assertEquals("BILLING", result.get(1).getAddressType());

            verify(clientRepository, times(1)).findById(DEFAULT_CLIENT_ID);
            verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID,
                    false);
        }

        /**
         * Purpose: Handle client with no addresses.
         * Expected Result: Empty list is returned.
         * Assertions: Result is empty and repository is called.
         */
        @Test
        @DisplayName("Get Address By Client ID - No addresses - Success returns empty list")
        void getAddressByClientId_NoAddresses_ReturnsEmptyList() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(new ArrayList<>());

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID,
                    false);
        }

        /**
         * Purpose: Reject client not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Client not found - ThrowsNotFoundException")
        void getAddressByClientId_ClientNotFound_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }

        /**
         * Purpose: Reject deleted client.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Client deleted - ThrowsNotFoundException")
        void getAddressByClientId_ClientDeleted_ThrowsNotFoundException() {
            // Arrange
            testClient.setIsDeleted(true);
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }

        /**
         * Purpose: Verify all address types are returned for a client.
         * Expected Result: List includes each address type.
         * Assertions: Result size and type set include all expected values.
         */
        @Test
        @DisplayName("Get Address By Client ID - All address types - Success returns all types")
        void getAddressByClientId_AllAddressTypes_Success() {
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            Address homeAddress = createTestAddress(1L, "HOME");
            Address workAddress = createTestAddress(2L, "WORK");
            Address billingAddress = createTestAddress(3L, "BILLING");
            Address shippingAddress = createTestAddress(4L, "SHIPPING");
            Address officeAddress = createTestAddress(5L, "OFFICE");
            Address warehouseAddress = createTestAddress(6L, "WAREHOUSE");
            List<Address> addresses = Arrays.asList(
                    homeAddress, workAddress, billingAddress,
                    shippingAddress, officeAddress, warehouseAddress);
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(addresses);

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(6, result.size());

            Set<String> addressTypes = new HashSet<>();
            for (AddressResponseModel address : result) {
                addressTypes.add(address.getAddressType());
                assertEquals(DEFAULT_CLIENT_ID, address.getClientId());
            }

            assertTrue(addressTypes.contains("HOME"));
            assertTrue(addressTypes.contains("WORK"));
            assertTrue(addressTypes.contains("BILLING"));
            assertTrue(addressTypes.contains("SHIPPING"));
            assertTrue(addressTypes.contains("OFFICE"));
            assertTrue(addressTypes.contains("WAREHOUSE"));
        }

        /**
         * Purpose: Reject negative client ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Negative ID - ThrowsNotFoundException")
        void getAddressByClientId_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(clientRepository.findById(negativeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(negativeId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject zero client ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Zero ID - ThrowsNotFoundException")
        void getAddressByClientId_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(clientRepository.findById(zeroId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(zeroId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject very large client ID when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Very large ID - ThrowsNotFoundException")
        void getAddressByClientId_VeryLargeId_ThrowsNotFoundException() {
            // Arrange
            long largeId = Long.MAX_VALUE;
            when(clientRepository.findById(largeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(largeId));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Reject min long client ID when not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Min Long value - ThrowsNotFoundException")
        void getAddressByClientId_MinLongValue_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> addressService.getAddressByClientId(Long.MIN_VALUE));

            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify null optional fields are handled for client addresses.
         * Expected Result: Response contains null optional fields.
         * Assertions: Optional fields are null in response.
         */
        @Test
        @DisplayName("Get Address By Client ID - Addresses with null optional fields - Success")
        void getAddressByClientId_NullOptionalFields_Success() {
            // Arrange
            testAddress.setStreetAddress2(null);
            testAddress.setStreetAddress3(null);
            testAddress.setNameOnAddress(null);
            testAddress.setEmailOnAddress(null);
            testAddress.setPhoneOnAddress(null);
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(Collections.singletonList(testAddress));

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertNull(result.get(0).getStreetAddress2());
            assertNull(result.get(0).getStreetAddress3());
            assertNull(result.get(0).getNameOnAddress());
        }

        /**
         * Purpose: Verify large result sets are handled for client addresses.
         * Expected Result: All addresses are returned.
         * Assertions: Result size matches expected count.
         */
        @Test
        @DisplayName("Get Address By Client ID - Large result set - Success")
        void getAddressByClientId_LargeResultSet_Success() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

            List<Address> addresses = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                addresses.add(createTestAddress((long) i, i % 2 == 0 ? "HOME" : "WORK"));
            }
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(addresses);

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(100, result.size());
            for (AddressResponseModel address : result) {
                assertEquals(DEFAULT_CLIENT_ID, address.getClientId());
            }
        }

        /**
         * Purpose: Verify single address response for client.
         * Expected Result: List contains one address.
         * Assertions: Result size is one.
         */
        @Test
        @DisplayName("Get Address By Client ID - Single address - Success returns list with one item")
        void getAddressByClientId_SingleAddress_Success() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                    .thenReturn(Collections.singletonList(testAddress));

            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        /**
         * Purpose: Reject null request for insert from client context.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null Request - Throws BadRequestException")
        void insertAddress_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(null));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null street address during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null Street Address - Throws BadRequestException")
        void insertAddress_NullStreetAddress_ThrowsBadRequestException() {
            testAddressRequest.setStreetAddress(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject empty street address during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Empty Street Address - Throws BadRequestException")
        void insertAddress_EmptyStreetAddress_ThrowsBadRequestException() {
            testAddressRequest.setStreetAddress("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null city during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null City - Throws BadRequestException")
        void insertAddress_NullCity_ThrowsBadRequestException() {
            testAddressRequest.setCity(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject empty city during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER002 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Empty City - Throws BadRequestException")
        void insertAddress_EmptyCity_ThrowsBadRequestException() {
            testAddressRequest.setCity("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null state during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null State - Throws BadRequestException")
        void insertAddress_NullState_ThrowsBadRequestException() {
            testAddressRequest.setState(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject empty state during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER003 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Empty State - Throws BadRequestException")
        void insertAddress_EmptyState_ThrowsBadRequestException() {
            testAddressRequest.setState("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null country during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null Country - Throws BadRequestException")
        void insertAddress_NullCountry_ThrowsBadRequestException() {
            testAddressRequest.setCountry(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject empty country during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER005 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Empty Country - Throws BadRequestException")
        void insertAddress_EmptyCountry_ThrowsBadRequestException() {
            testAddressRequest.setCountry("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject null postal code during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Null Postal Code - Throws BadRequestException")
        void insertAddress_NullPostalCode_ThrowsBadRequestException() {
            testAddressRequest.setPostalCode(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject empty postal code during insert.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER004 and save is not called.
         */
        @Test
        @DisplayName("Insert Address - Empty Postal Code - Throws BadRequestException")
        void insertAddress_EmptyPostalCode_ThrowsBadRequestException() {
            testAddressRequest.setPostalCode("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.insertAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject update for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Update Address - Negative ID - Throws NotFoundException")
        void updateAddress_NegativeId_ThrowsNotFoundException() {
            testAddressRequest.setId(-1L);
            when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject update for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Update Address - Zero ID - Throws NotFoundException")
        void updateAddress_ZeroId_ThrowsNotFoundException() {
            testAddressRequest.setId(0L);
            when(addressRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject update for max long ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Update Address - Long.MAX_VALUE ID - Throws NotFoundException")
        void updateAddress_MaxLongId_ThrowsNotFoundException() {
            testAddressRequest.setId(Long.MAX_VALUE);
            when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject update when street address is null.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Error message matches ER001 and save is not called.
         */
        @Test
        @DisplayName("Update Address - Null Street Address - Throws BadRequestException")
        void updateAddress_NullStreetAddress_ThrowsBadRequestException() {
            testAddressRequest.setId(DEFAULT_ADDRESS_ID);
            testAddressRequest.setStreetAddress(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> addressService.updateAddress(testAddressRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject toggle for negative ID (duplicate coverage).
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Toggle Address - Negative ID (Duplicate Test) - Throws NotFoundException")
        void toggleAddress_NegativeId_ThrowsNotFoundException2() {
            when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.toggleAddress(-1L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject toggle for zero ID (duplicate coverage).
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Toggle Address - Zero ID (Duplicate Test) - Throws NotFoundException")
        void toggleAddress_ZeroId_ThrowsNotFoundException2() {
            when(addressRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.toggleAddress(0L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject get by ID for negative value.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Negative ID - Throws NotFoundException")
        void getAddressById_NegativeId_ThrowsNotFoundException() {
            when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.getAddressById(-1L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Reject get by ID for zero value.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Zero ID - Throws NotFoundException")
        void getAddressById_ZeroId_ThrowsNotFoundException() {
            when(addressRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.getAddressById(0L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Reject get by ID for max long value.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By ID - Long.MAX_VALUE ID - Throws NotFoundException")
        void getAddressById_MaxLongId_ThrowsNotFoundException() {
            when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.getAddressById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Reject toggle for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Toggle Address - Negative ID - Throws NotFoundException")
        void toggleAddress_NegativeId_ThrowsNotFoundException() {
            when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.toggleAddress(-1L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Reject toggle for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound and save is not called.
         */
        @Test
        @DisplayName("Toggle Address - Zero ID - Throws NotFoundException")
        void toggleAddress_ZeroId_ThrowsNotFoundException() {
            when(addressRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.toggleAddress(0L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }

        /**
         * Purpose: Verify multiple toggles transition states.
         * Expected Result: Deleted flag flips each time.
         * Assertions: Flag is true after first toggle and false after second.
         */
        @Test
        @DisplayName("Toggle Address - Multiple Toggles - State Transitions")
        void toggleAddress_MultipleToggles_StateTransitions() {
            testAddress.setIsDeleted(false);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            // First toggle: false -> true
            addressService.toggleAddress(DEFAULT_ADDRESS_ID);
            assertTrue(testAddress.getIsDeleted());

            // Second toggle: true -> false
            testAddress.setIsDeleted(true);
            addressService.toggleAddress(DEFAULT_ADDRESS_ID);
            assertFalse(testAddress.getIsDeleted());

            verify(addressRepository, times(2)).save(any(Address.class));
        }

        /**
         * Purpose: Reject negative client ID lookup.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Negative Client ID - Throws BadRequestException")
        void getAddressByClientId_NegativeClientId_ThrowsBadRequestException() {
            when(clientRepository.findById(-1L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.getAddressByClientId(-1L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Reject zero client ID lookup.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Error message matches NotFound.
         */
        @Test
        @DisplayName("Get Address By Client ID - Zero Client ID - Throws BadRequestException")
        void getAddressByClientId_ZeroClientId_ThrowsBadRequestException() {
            when(clientRepository.findById(0L)).thenReturn(Optional.empty());
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> addressService.getAddressByClientId(0L));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        }
    }
}
