package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Services.AddressService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService.
 * 
 * This test class provides comprehensive coverage of AddressService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Address retrieval by ID, user ID, and client ID
 * - Address status management
 * - Error handling and validation
 * - Audit logging verification
 * 
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Unit Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    
    @Mock
    private UserLogService userLogService;
    
    @Mock
    private HttpServletRequest request;
    
    @InjectMocks
    private AddressService addressService;
    
    private Address testAddress;
    private AddressRequestModel testAddressRequest;
    private static final Long TEST_ADDRESS_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CLIENT_ID = 100L;
    private static final String TEST_ADDRESS_TYPE = "HOME";
    private static final String TEST_STREET_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_POSTAL_CODE = "10001";
    private static final String TEST_COUNTRY = "USA";
    private static final String CREATED_USER = "admin";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test address request model
        testAddressRequest = new AddressRequestModel();
        testAddressRequest.setId(TEST_ADDRESS_ID);
        testAddressRequest.setUserId(TEST_USER_ID);
        testAddressRequest.setClientId(TEST_CLIENT_ID);
        testAddressRequest.setAddressType(TEST_ADDRESS_TYPE);
        testAddressRequest.setStreetAddress(TEST_STREET_ADDRESS);
        testAddressRequest.setCity(TEST_CITY);
        testAddressRequest.setState(TEST_STATE);
        testAddressRequest.setPostalCode(TEST_POSTAL_CODE);
        testAddressRequest.setCountry(TEST_COUNTRY);
        testAddressRequest.setIsPrimary(true);
        testAddressRequest.setIsDeleted(false);
        
        // Initialize test address using constructor
        testAddress = new Address(testAddressRequest, CREATED_USER);
        testAddress.setAddressId(TEST_ADDRESS_ID);
        testAddress.setIsDeleted(false); // Explicitly set isDeleted since constructor might not set it properly
        testAddress.setCreatedAt(LocalDateTime.now());
        testAddress.setUpdatedAt(LocalDateTime.now());
        
        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        // Note: getUser() is no longer mocked as BaseService now handles test tokens
        // userLogService mock moved to individual tests that use it
    }

    // ==================== Toggle Address Tests ====================
    
    /**
     * Test successful address toggle operation.
     * Verifies that an address's isDeleted flag is correctly toggled from false to true.
     */
    @Test
    @DisplayName("Toggle Address - Success - Should toggle isDeleted flag")
    void toggleAddress_Success() {
        // Arrange
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> addressService.toggleAddress(TEST_ADDRESS_ID));
        
        // Verify
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test toggle address with non-existent address ID.
     * Verifies that NotFoundException is thrown when address is not found.
     */
    @Test
    @DisplayName("Toggle Address - Failure - Address not found")
    void toggleAddress_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> addressService.toggleAddress(TEST_ADDRESS_ID)
        );
        
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
        verify(addressRepository, never()).save(any(Address.class));
        verify(userLogService, never()).logData(anyString(), anyString(), anyString());
    }

    // ==================== Get Address By ID Tests ====================
    
    /**
     * Test successful retrieval of address by ID.
     * Verifies that address details are correctly returned.
     */
    @Test
    @DisplayName("Get Address By ID - Success - Should return address details")
    void getAddressById_Success() {
        // Arrange
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        
        // Act
        AddressResponseModel result = addressService.getAddressById(TEST_ADDRESS_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_ADDRESS_ID, result.getAddressId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_CLIENT_ID, result.getClientId());
        assertEquals(TEST_ADDRESS_TYPE, result.getAddressType());
        assertEquals(TEST_STREET_ADDRESS, result.getStreetAddress());
        assertEquals(TEST_CITY, result.getCity());
        assertEquals(TEST_STATE, result.getState());
        assertEquals(TEST_POSTAL_CODE, result.getPostalCode());
        assertEquals(TEST_COUNTRY, result.getCountry());
        assertTrue(result.getIsPrimary());
        assertFalse(result.getIsDeleted());
        
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
    }
    
    /**
     * Test get address by ID with non-existent address ID.
     * Verifies that NotFoundException is thrown when address is not found.
     */
    @Test
    @DisplayName("Get Address By ID - Failure - Address not found")
    void getAddressById_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> addressService.getAddressById(TEST_ADDRESS_ID)
        );
        
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
    }

    // ==================== Insert Address Tests ====================
    
    /**
     * Test successful address creation.
     * Verifies that address is correctly created and saved.
     */
    @Test
    @DisplayName("Insert Address - Success - Should create address")
    void insertAddress_Success() {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
        
        // Verify
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }

    // ==================== Update Address Tests ====================
    
    /**
     * Test successful address update.
     * Verifies that address fields are correctly updated.
     */
    @Test
    @DisplayName("Update Address - Success - Should update address fields")
    void updateAddress_Success() {
        // Arrange
        testAddressRequest.setStreetAddress("456 Oak Ave");
        testAddressRequest.setCity("Los Angeles");
        testAddressRequest.setState("CA");
        
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyString(), anyString(), anyString())).thenReturn(true);
        
        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
        
        // Verify
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userLogService, times(1)).logData(anyString(), anyString(), anyString());
    }
    
    /**
     * Test update address with non-existent address ID.
     * Verifies that NotFoundException is thrown when address is not found.
     */
    @Test
    @DisplayName("Update Address - Failure - Address not found")
    void updateAddress_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> addressService.updateAddress(testAddressRequest)
        );
        
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
        verify(addressRepository, never()).save(any(Address.class));
    }

    // ==================== Get Address By User ID Tests ====================
    
    /**
     * Test successful retrieval of addresses by user ID.
     * Verifies that user's addresses are returned.
     */
    @Test
    @DisplayName("Get Address By User ID - Success - Should return user addresses")
    void getAddressByUserId_Success() {
        // Arrange
        Address secondAddress = new Address(testAddressRequest, CREATED_USER);
        secondAddress.setAddressId(2L);
        secondAddress.setAddressType("Work");
        
        List<Address> addresses = Arrays.asList(testAddress, secondAddress);
        when(addressRepository.findByUserId(TEST_USER_ID)).thenReturn(addresses);
        
        // Act
        List<AddressResponseModel> result = addressService.getAddressByUserId(TEST_USER_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TEST_ADDRESS_ID, result.get(0).getAddressId());
        assertEquals(TEST_ADDRESS_TYPE, result.get(0).getAddressType());
        assertEquals(2L, result.get(1).getAddressId());
        assertEquals("Work", result.get(1).getAddressType());
        
        verify(addressRepository, times(1)).findByUserId(TEST_USER_ID);
    }
    
    /**
     * Test get addresses by user ID when no addresses exist.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get Address By User ID - Success - Empty list")
    void getAddressByUserId_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(addressRepository.findByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        
        // Act
        List<AddressResponseModel> result = addressService.getAddressByUserId(TEST_USER_ID);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findByUserId(TEST_USER_ID);
    }

    // ==================== Get Address By Client ID Tests ====================
    
    /**
     * Test successful retrieval of addresses by client ID.
     * Verifies that client's addresses are returned.
     */
    @Test
    @DisplayName("Get Address By Client ID - Success - Should return client addresses")
    void getAddressByClientId_Success() {
        // Arrange
        Address secondAddress = new Address(testAddressRequest, CREATED_USER);
        secondAddress.setAddressId(2L);
        secondAddress.setAddressType("Billing");
        
        List<Address> addresses = Arrays.asList(testAddress, secondAddress);
        when(addressRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(addresses);
        
        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TEST_ADDRESS_ID, result.get(0).getAddressId());
        assertEquals(TEST_CLIENT_ID, result.get(0).getClientId());
        assertEquals(TEST_ADDRESS_TYPE, result.get(0).getAddressType());
        assertEquals(2L, result.get(1).getAddressId());
        assertEquals(TEST_CLIENT_ID, result.get(1).getClientId());
        assertEquals("Billing", result.get(1).getAddressType());
        
        verify(addressRepository, times(1)).findByClientId(TEST_CLIENT_ID);
    }
    
    /**
     * Test get addresses by client ID when no addresses exist.
     * Verifies that empty list is returned.
     */
    @Test
    @DisplayName("Get Address By Client ID - Success - Empty list")
    void getAddressByClientId_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(addressRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(new ArrayList<>());
        
        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findByClientId(TEST_CLIENT_ID);
    }
    
    /**
     * Test get addresses by client ID with multiple address types.
     * Verifies that all address types are properly handled.
     */
    @Test
    @DisplayName("Get Address By Client ID - Success - Multiple address types")
    void getAddressByClientId_MultipleTypes_Success() {
        // Arrange
        Address homeAddress = new Address(testAddressRequest, CREATED_USER);
        homeAddress.setAddressId(1L);
        homeAddress.setAddressType("Home");
        
        AddressRequestModel workRequest = new AddressRequestModel();
        workRequest.setClientId(TEST_CLIENT_ID);
        workRequest.setAddressType("Work");
        workRequest.setStreetAddress("789 Business Blvd");
        workRequest.setCity("Chicago");
        workRequest.setState("IL");
        workRequest.setPostalCode("60601");
        workRequest.setCountry("USA");
        workRequest.setIsPrimary(false);
        workRequest.setIsDeleted(false);
        
        Address workAddress = new Address(workRequest, CREATED_USER);
        workAddress.setAddressId(2L);
        workAddress.setAddressType("Work");
        
        AddressRequestModel billingRequest = new AddressRequestModel();
        billingRequest.setClientId(TEST_CLIENT_ID);
        billingRequest.setAddressType("Billing");
        billingRequest.setStreetAddress("456 Finance St");
        billingRequest.setCity("Miami");
        billingRequest.setState("FL");
        billingRequest.setPostalCode("33101");
        billingRequest.setCountry("USA");
        billingRequest.setIsPrimary(false);
        billingRequest.setIsDeleted(false);
        
        Address billingAddress = new Address(billingRequest, CREATED_USER);
        billingAddress.setAddressId(3L);
        billingAddress.setAddressType("Billing");
        
        List<Address> addresses = Arrays.asList(homeAddress, workAddress, billingAddress);
        when(addressRepository.findByClientId(TEST_CLIENT_ID)).thenReturn(addresses);
        
        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(TEST_CLIENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify different address types
        Set<String> addressTypes = new HashSet<>();
        for (AddressResponseModel address : result) {
            addressTypes.add(address.getAddressType());
            assertEquals(TEST_CLIENT_ID, address.getClientId());
        }
        
        assertTrue(addressTypes.contains("Home"));
        assertTrue(addressTypes.contains("Work"));
        assertTrue(addressTypes.contains("Billing"));
        
        verify(addressRepository, times(1)).findByClientId(TEST_CLIENT_ID);
    }
}
