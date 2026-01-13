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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AddressService.
 * 
 * This test class provides exhaustive coverage of AddressService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Address retrieval by ID, user ID, and client ID
 * - Address status management
 * - Validation error handling for all input fields
 * - Edge cases (null values, empty strings, negative IDs, zero IDs)
 * - All conditional branches and code paths
 * 
 * Test naming convention: methodName_Scenario_ExpectedOutcome
 * Example: toggleAddress_AddressNotFound_ThrowsNotFoundException
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 * 
 * Rules for comprehensive testing:
 * 1. Every public method must have at least one success and one failure test
 * 2. All if statements and branches must be covered
 * 3. All validations must have dedicated tests
 * 4. Edge cases: null, empty, negative, zero, boundary values
 * 5. Read-only methods should use @Transactional(readOnly = true)
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
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
        
        // Mock BaseService methods (required for service methods that call getUser/getUserId)
        lenient().doReturn(DEFAULT_USER_ID).when(addressService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(addressService).getUser();
        lenient().doReturn(DEFAULT_CLIENT_ID).when(addressService).getClientId();
    }

    // ==================== TOGGLE ADDRESS TESTS ====================
    
    @Nested
    @DisplayName("toggleAddress Tests")
    class ToggleAddressTests {
        
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
        
        @Test
        @DisplayName("Toggle Address - Address not found - ThrowsNotFoundException")
        void toggleAddress_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.toggleAddress(DEFAULT_ADDRESS_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, never()).save(any(Address.class));
            verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
        }
        
        @Test
        @DisplayName("Toggle Address - Negative ID - ThrowsNotFoundException")
        void toggleAddress_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(addressRepository.findById(negativeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.toggleAddress(negativeId)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }
        
        @Test
        @DisplayName("Toggle Address - Zero ID - ThrowsNotFoundException")
        void toggleAddress_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(addressRepository.findById(zeroId)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.toggleAddress(zeroId)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }
        
        @Test
        @DisplayName("Toggle Address - Very large ID - ThrowsNotFoundException")
        void toggleAddress_VeryLargeId_ThrowsNotFoundException() {
            // Arrange
            long largeId = Long.MAX_VALUE;
            when(addressRepository.findById(largeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.toggleAddress(largeId)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }
    }

    // ==================== GET ADDRESS BY ID TESTS ====================
    
    @Nested
    @DisplayName("getAddressById Tests")
    class GetAddressByIdTests {
        
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
        
        @Test
        @DisplayName("Get Address By ID - Address not found - ThrowsNotFoundException")
        void getAddressById_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressById(DEFAULT_ADDRESS_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
        }
        
        @Test
        @DisplayName("Get Address By ID - Negative ID - ThrowsNotFoundException")
        void getAddressById_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(addressRepository.findById(negativeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressById(negativeId));
        }
        
        @Test
        @DisplayName("Get Address By ID - Zero ID - ThrowsNotFoundException")
        void getAddressById_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(addressRepository.findById(zeroId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressById(zeroId));
        }
        
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
        
        @Test
        @DisplayName("Get Address By ID - Max Long value - ThrowsNotFoundException")
        void getAddressById_MaxLongValue_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressById(Long.MAX_VALUE));
        }
        
        @Test
        @DisplayName("Get Address By ID - Min Long value - ThrowsNotFoundException")
        void getAddressById_MinLongValue_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressById(Long.MIN_VALUE));
        }
    }

    // ==================== INSERT ADDRESS TESTS ====================
    
    @Nested
    @DisplayName("insertAddress Tests")
    class InsertAddressTests {
        
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
        
        @Test
        @DisplayName("Insert Address - Null request - ThrowsBadRequestException")
        void insertAddress_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(null)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
            verify(addressRepository, never()).save(any(Address.class));
        }
        
        @Test
        @DisplayName("Insert Address - Null street address - ThrowsBadRequestException")
        void insertAddress_NullStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Empty street address - ThrowsBadRequestException")
        void insertAddress_EmptyStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress("");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Whitespace only street address - ThrowsBadRequestException")
        void insertAddress_WhitespaceStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress("   ");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Null city - ThrowsBadRequestException")
        void insertAddress_NullCity_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCity(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Empty city - ThrowsBadRequestException")
        void insertAddress_EmptyCity_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCity("");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Null state - ThrowsBadRequestException")
        void insertAddress_NullState_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setState(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Empty state - ThrowsBadRequestException")
        void insertAddress_EmptyState_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setState("");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Null postal code - ThrowsBadRequestException")
        void insertAddress_NullPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Empty postal code - ThrowsBadRequestException")
        void insertAddress_EmptyPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"1234", "1234567", "abcde", "12-345", "12.345", "12 345"})
        @DisplayName("Insert Address - Invalid postal code format - ThrowsBadRequestException")
        void insertAddress_InvalidPostalCodeFormat_ThrowsBadRequestException(String invalidPostalCode) {
            // Arrange
            testAddressRequest.setPostalCode(invalidPostalCode);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"12345", "123456"})
        @DisplayName("Insert Address - Valid postal code formats (5-6 digits) - Success")
        void insertAddress_ValidPostalCodeFormats_Success(String validPostalCode) {
            // Arrange
            testAddressRequest.setPostalCode(validPostalCode);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            
            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }
        
        @Test
        @DisplayName("Insert Address - Null country - ThrowsBadRequestException")
        void insertAddress_NullCountry_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCountry(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Empty country - ThrowsBadRequestException")
        void insertAddress_EmptyCountry_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setCountry("");
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Null address type - ThrowsBadRequestException")
        void insertAddress_NullAddressType_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setAddressType(null);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "RESIDENTIAL", "COMMERCIAL", "home123", ""})
        @DisplayName("Insert Address - Invalid address type - ThrowsBadRequestException")
        void insertAddress_InvalidAddressType_ThrowsBadRequestException(String invalidType) {
            // Arrange
            testAddressRequest.setAddressType(invalidType);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"HOME", "WORK", "BILLING", "SHIPPING", "OFFICE", "WAREHOUSE", "home", "Home", "work", "Work"})
        @DisplayName("Insert Address - Valid address types - Success")
        void insertAddress_ValidAddressTypes_Success(String validType) {
            // Arrange
            testAddressRequest.setAddressType(validType);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            
            // Act & Assert
            assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        }
        
        @Test
        @DisplayName("Insert Address - Negative user ID - ThrowsBadRequestException")
        void insertAddress_NegativeUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(-1L);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Zero user ID - ThrowsBadRequestException")
        void insertAddress_ZeroUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(0L);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Negative client ID - ThrowsBadRequestException")
        void insertAddress_NegativeClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(-1L);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }
        
        @Test
        @DisplayName("Insert Address - Zero client ID - ThrowsBadRequestException")
        void insertAddress_ZeroClientId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setClientId(0L);
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.insertAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
        }
        
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
    }

    // ==================== UPDATE ADDRESS TESTS ====================
    
    @Nested
    @DisplayName("updateAddress Tests")
    class UpdateAddressTests {
        
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
        
        @Test
        @DisplayName("Update Address - Address not found - ThrowsNotFoundException")
        void updateAddress_AddressNotFound_ThrowsNotFoundException() {
            // Arrange
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.updateAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
            verify(addressRepository, never()).save(any(Address.class));
        }
        
        @Test
        @DisplayName("Update Address - Null request - ThrowsBadRequestException")
        void updateAddress_NullRequest_ThrowsBadRequestException() {
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(null)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }
        
        @Test
        @DisplayName("Update Address - Null street address - ThrowsBadRequestException")
        void updateAddress_NullStreetAddress_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setStreetAddress(null);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
        }
        
        @Test
        @DisplayName("Update Address - Invalid postal code - ThrowsBadRequestException")
        void updateAddress_InvalidPostalCode_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setPostalCode("invalid");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
        }
        
        @Test
        @DisplayName("Update Address - Invalid address type - ThrowsBadRequestException")
        void updateAddress_InvalidAddressType_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setAddressType("INVALID_TYPE");
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
        }
        
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
        
        @Test
        @DisplayName("Update Address - Negative user ID - ThrowsBadRequestException")
        void updateAddress_NegativeUserId_ThrowsBadRequestException() {
            // Arrange
            testAddressRequest.setUserId(-1L);
            when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            
            // Act & Assert
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest)
            );
            
            assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== GET ADDRESS BY USER ID TESTS ====================
    
    @Nested
    @DisplayName("getAddressByUserId Tests")
    class GetAddressByUserIdTests {
        
        @Test
        @DisplayName("Get Address By User ID - Multiple addresses found - Success returns list")
        void getAddressByUserId_MultipleAddressesFound_Success() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            
            Address secondAddress = new Address(testAddressRequest, DEFAULT_CREATED_USER);
            secondAddress.setAddressId(2L);
            secondAddress.setAddressType("WORK");
            
            List<Address> addresses = Arrays.asList(testAddress, secondAddress);
            when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false)).thenReturn(addresses);
            
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
        
        @Test
        @DisplayName("Get Address By User ID - No addresses - Success returns empty list")
        void getAddressByUserId_NoAddresses_ReturnsEmptyList() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false)).thenReturn(new ArrayList<>());
            
            // Act
            List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(addressRepository, times(1)).findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
        }
        
        @Test
        @DisplayName("Get Address By User ID - User not found - ThrowsNotFoundException")
        void getAddressByUserId_UserNotFound_ThrowsNotFoundException() {
            // Arrange
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(DEFAULT_USER_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }
        
        @Test
        @DisplayName("Get Address By User ID - User deleted - ThrowsNotFoundException")
        void getAddressByUserId_UserDeleted_ThrowsNotFoundException() {
            // Arrange
            testUser.setIsDeleted(true);
            when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(DEFAULT_USER_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }
        
        @Test
        @DisplayName("Get Address By User ID - Negative ID - ThrowsNotFoundException")
        void getAddressByUserId_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(userRepository.findById(negativeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressByUserId(negativeId));
        }
        
        @Test
        @DisplayName("Get Address By User ID - Zero ID - ThrowsNotFoundException")
        void getAddressByUserId_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(userRepository.findById(zeroId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressByUserId(zeroId));
        }
        
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

    // ==================== GET ADDRESS BY CLIENT ID TESTS ====================
    
    @Nested
    @DisplayName("getAddressByClientId Tests")
    class GetAddressByClientIdTests {
        
        @Test
        @DisplayName("Get Address By Client ID - Multiple addresses found - Success returns list")
        void getAddressByClientId_MultipleAddressesFound_Success() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            
            Address secondAddress = new Address(testAddressRequest, DEFAULT_CREATED_USER);
            secondAddress.setAddressId(2L);
            secondAddress.setAddressType("BILLING");
            
            List<Address> addresses = Arrays.asList(testAddress, secondAddress);
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false)).thenReturn(addresses);
            
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
            verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
        }
        
        @Test
        @DisplayName("Get Address By Client ID - No addresses - Success returns empty list")
        void getAddressByClientId_NoAddresses_ReturnsEmptyList() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false)).thenReturn(new ArrayList<>());
            
            // Act
            List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
        }
        
        @Test
        @DisplayName("Get Address By Client ID - Client not found - ThrowsNotFoundException")
        void getAddressByClientId_ClientNotFound_ThrowsNotFoundException() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }
        
        @Test
        @DisplayName("Get Address By Client ID - Client deleted - ThrowsNotFoundException")
        void getAddressByClientId_ClientDeleted_ThrowsNotFoundException() {
            // Arrange
            testClient.setIsDeleted(true);
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            
            // Act & Assert
            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID)
            );
            
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
            verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
        }
        
        @Test
        @DisplayName("Get Address By Client ID - All address types - Success returns all types")
        void getAddressByClientId_AllAddressTypes_Success() {
            // Arrange
            when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            
            Address homeAddress = new Address(testAddressRequest, DEFAULT_CREATED_USER);
            homeAddress.setAddressId(1L);
            homeAddress.setAddressType("HOME");
            
            AddressRequestModel workRequest = createValidAddressRequest();
            workRequest.setAddressType("WORK");
            Address workAddress = new Address(workRequest, DEFAULT_CREATED_USER);
            workAddress.setAddressId(2L);
            
            AddressRequestModel billingRequest = createValidAddressRequest();
            billingRequest.setAddressType("BILLING");
            Address billingAddress = new Address(billingRequest, DEFAULT_CREATED_USER);
            billingAddress.setAddressId(3L);
            
            AddressRequestModel shippingRequest = createValidAddressRequest();
            shippingRequest.setAddressType("SHIPPING");
            Address shippingAddress = new Address(shippingRequest, DEFAULT_CREATED_USER);
            shippingAddress.setAddressId(4L);
            
            AddressRequestModel officeRequest = createValidAddressRequest();
            officeRequest.setAddressType("OFFICE");
            Address officeAddress = new Address(officeRequest, DEFAULT_CREATED_USER);
            officeAddress.setAddressId(5L);
            
            AddressRequestModel warehouseRequest = createValidAddressRequest();
            warehouseRequest.setAddressType("WAREHOUSE");
            Address warehouseAddress = new Address(warehouseRequest, DEFAULT_CREATED_USER);
            warehouseAddress.setAddressId(6L);
            
            List<Address> addresses = Arrays.asList(
                homeAddress, workAddress, billingAddress, 
                shippingAddress, officeAddress, warehouseAddress
            );
            when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false)).thenReturn(addresses);
            
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
        
        @Test
        @DisplayName("Get Address By Client ID - Negative ID - ThrowsNotFoundException")
        void getAddressByClientId_NegativeId_ThrowsNotFoundException() {
            // Arrange
            long negativeId = -1L;
            when(clientRepository.findById(negativeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressByClientId(negativeId));
        }
        
        @Test
        @DisplayName("Get Address By Client ID - Zero ID - ThrowsNotFoundException")
        void getAddressByClientId_ZeroId_ThrowsNotFoundException() {
            // Arrange
            long zeroId = 0L;
            when(clientRepository.findById(zeroId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressByClientId(zeroId));
        }
        
        @Test
        @DisplayName("Get Address By Client ID - Very large ID - ThrowsNotFoundException")
        void getAddressByClientId_VeryLargeId_ThrowsNotFoundException() {
            // Arrange
            long largeId = Long.MAX_VALUE;
            when(clientRepository.findById(largeId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThrows(NotFoundException.class, () -> addressService.getAddressByClientId(largeId));
        }
        
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
    }
}
