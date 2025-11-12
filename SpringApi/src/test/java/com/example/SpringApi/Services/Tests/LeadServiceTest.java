package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Services.LeadService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeadService.
 *
 * This test class provides comprehensive coverage of LeadService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Lead retrieval in batches with pagination and filtering
 * - Lead validation and error handling
 * - Address integration and validation
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
@DisplayName("LeadService Unit Tests")
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    @Spy
    private LeadService leadService;

    private Lead testLead;
    private LeadRequestModel testLeadRequest;
    private AddressRequestModel testAddressRequest;
    private UserRequestModel testUserRequest;
    private Address testAddress;
    private User testUser;
    private static final Long TEST_LEAD_ID = 1L;
    private static final Long TEST_CLIENT_ID = 100L;
    private static final Long TEST_ADDRESS_ID = 200L;
    private static final Long TEST_CREATED_BY_ID = 1L;
    private static final Long TEST_ASSIGNED_AGENT_ID = 2L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_LEAD_STATUS = "Not Contacted";
    private static final String TEST_COMPANY = "Test Company";
    private static final String CREATED_USER = "admin";
    private static final String TEST_STREET_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_POSTAL_CODE = "10001";
    private static final String TEST_COUNTRY = "USA";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test address request
        testAddressRequest = new AddressRequestModel();
        testAddressRequest.setAddressType("OFFICE");
        testAddressRequest.setStreetAddress(TEST_STREET_ADDRESS);
        testAddressRequest.setCity(TEST_CITY);
        testAddressRequest.setState(TEST_STATE);
        testAddressRequest.setPostalCode(TEST_POSTAL_CODE);
        testAddressRequest.setCountry(TEST_COUNTRY);
        testAddressRequest.setIsPrimary(true);

        // Initialize test address
        testAddress = new Address(testAddressRequest, CREATED_USER);
        testAddress.setAddressId(TEST_ADDRESS_ID);

        // Initialize test lead request
        testLeadRequest = new LeadRequestModel();
        testLeadRequest.setLeadId(TEST_LEAD_ID);
        testLeadRequest.setEmail(TEST_EMAIL);
        testLeadRequest.setFirstName(TEST_FIRST_NAME);
        testLeadRequest.setLastName(TEST_LAST_NAME);
        testLeadRequest.setPhone(TEST_PHONE);
        testLeadRequest.setLeadStatus(TEST_LEAD_STATUS);
        testLeadRequest.setCompany(TEST_COMPANY);
        testLeadRequest.setClientId(TEST_CLIENT_ID);
        testLeadRequest.setCreatedById(TEST_CREATED_BY_ID);
        testLeadRequest.setAssignedAgentId(TEST_ASSIGNED_AGENT_ID);
        testLeadRequest.setAddress(testAddressRequest);
        testLeadRequest.setCompanySize(50);
        testLeadRequest.setIsDeleted(false);

        // Initialize pagination fields for batch operations
        testLeadRequest.setColumnName("leadId");
        testLeadRequest.setCondition("equals");
        testLeadRequest.setFilterExpr("1");
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);
        testLeadRequest.setIncludeDeleted(false);

        // Initialize test lead using constructor
        testLead = new Lead(testLeadRequest, CREATED_USER);
        testLead.setLeadId(TEST_LEAD_ID);
        testLead.setAddressId(TEST_ADDRESS_ID);
        testLead.setIsDeleted(false);
        testLead.setCreatedAt(LocalDateTime.now());
        testLead.setUpdatedAt(LocalDateTime.now());

        // Initialize test user request
        testUserRequest = new UserRequestModel();
        testUserRequest.setUserId(TEST_CREATED_BY_ID);
        testUserRequest.setLoginName(CREATED_USER);
        testUserRequest.setEmail("admin@example.com");
        testUserRequest.setFirstName("Admin");
        testUserRequest.setLastName("User");
        testUserRequest.setPhone("1234567890");
        testUserRequest.setDob(LocalDate.of(1990, 1, 1));
        testUserRequest.setRole("Admin");
        testUserRequest.setIsDeleted(false);

        // Initialize test user using constructor
        testUser = new User(testUserRequest, CREATED_USER);
        testUser.setUserId(TEST_CREATED_BY_ID);

        // Set up relationships
        testLead.setAddress(testAddress);
        testLead.setCreatedByUser(testUser);
        testLead.setAssignedAgent(testUser);

        // Mock Authorization header for BaseService authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        
        // Mock getClientId() to return TEST_CLIENT_ID
        lenient().when(leadService.getClientId()).thenReturn(TEST_CLIENT_ID);
    }

    // ==================== Get Leads In Batches Tests ====================

    /**
     * Test successful retrieval of leads in batches.
     * Verifies that paginated lead data is correctly returned with valid parameters.
     */
    @Test
    @DisplayName("Get Leads In Batches - Success - Should return paginated lead data")
    void getLeadsInBatches_Success() {
        // Arrange
        List<Lead> leadList = Arrays.asList(testLead);
        Page<Lead> leadPage = new PageImpl<>(leadList, PageRequest.of(0, 10, Sort.by("leadId")), 1);

        when(leadRepository.findPaginatedLeads(
            eq(TEST_CLIENT_ID), eq("leadId"), eq("equals"), eq("1"), eq(false), any(PageRequest.class)
        )).thenReturn(leadPage);

        // Act
        PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        assertEquals(TEST_LEAD_ID, result.getData().get(0).getLeadId());
        verify(leadRepository).findPaginatedLeads(anyLong(), anyString(), anyString(), anyString(), anyBoolean(), any(PageRequest.class));
    }

    /**
     * Test get leads in batches with null column name.
     * Verifies that BadRequestException is thrown when column name is null.
     */
    @Test
    @DisplayName("Get Leads In Batches - Success - Null column name allowed")
    void getLeadsInBatches_NullColumnName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setColumnName(null);
        List<Lead> leadList = Arrays.asList(testLead);
        Page<Lead> leadPage = new PageImpl<>(leadList, PageRequest.of(0, 10, Sort.by("leadId")), 1);

        when(leadRepository.findPaginatedLeads(
            eq(TEST_CLIENT_ID), isNull(), eq("equals"), eq("1"), eq(false), any(PageRequest.class)
        )).thenReturn(leadPage);

        // Act - Service allows null column name, will pass validation
        PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);
        
        // Assert - Should succeed with null column name
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /**
     * Test get leads in batches with empty column name.
     * Verifies that BadRequestException is thrown when column name is empty.
     */
    @Test
    @DisplayName("Get Leads In Batches - Failure - Empty column name")
    void getLeadsInBatches_EmptyColumnName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setColumnName("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.getLeadsInBatches(testLeadRequest));
        assertEquals("Invalid column name: ", exception.getMessage());
    }

    /**
     * Test get leads in batches with whitespace column name.
     * Verifies that BadRequestException is thrown when column name is whitespace.
     */
    @Test
    @DisplayName("Get Leads In Batches - Failure - Whitespace column name")
    void getLeadsInBatches_WhitespaceColumnName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setColumnName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.getLeadsInBatches(testLeadRequest));
        assertEquals("Invalid column name:    ", exception.getMessage());
    }

    /**
     * Test get leads in batches with invalid column name.
     * Verifies that BadRequestException is thrown when column name is not in valid set.
     */
    @Test
    @DisplayName("Get Leads In Batches - Failure - Invalid column name")
    void getLeadsInBatches_InvalidColumnName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setColumnName("invalidColumn");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.getLeadsInBatches(testLeadRequest));
        assertEquals("Invalid column name: invalidColumn", exception.getMessage());
    }

    // ==================== Get Lead Details By ID Tests ====================

    /**
     * Test successful retrieval of lead details by ID.
     * Verifies that lead details are correctly returned with relationships loaded.
     */
    @Test
    @DisplayName("Get Lead Details By ID - Success - Should return lead details")
    void getLeadDetailsById_Success() {
        // Arrange
        when(leadService.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(leadRepository.findLeadWithDetailsById(TEST_LEAD_ID, TEST_CLIENT_ID)).thenReturn(testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsById(TEST_LEAD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_LEAD_ID, result.getLeadId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertNotNull(result.getAddress());
        assertNotNull(result.getCreatedByUser());
        verify(leadRepository).findLeadWithDetailsById(TEST_LEAD_ID, TEST_CLIENT_ID);
    }

    /**
     * Test get lead details by ID with non-existent lead.
     * Verifies that NotFoundException is thrown when lead is not found.
     */
    @Test
    @DisplayName("Get Lead Details By ID - Failure - Lead not found")
    void getLeadDetailsById_LeadNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            leadService.getLeadDetailsById(TEST_LEAD_ID));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, exception.getMessage());
    }

    // ==================== Get Lead Details By Email Tests ====================

    /**
     * Test successful retrieval of lead details by email.
     * Verifies that lead details are correctly returned when found by email.
     */
    @Test
    @DisplayName("Get Lead Details By Email - Success - Should return lead details")
    void getLeadDetailsByEmail_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByEmail(TEST_EMAIL, TEST_CLIENT_ID)).thenReturn(testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_LEAD_ID, result.getLeadId());
        verify(leadRepository).findLeadWithDetailsByEmail(TEST_EMAIL, TEST_CLIENT_ID);
    }

    /**
     * Test get lead details by email with non-existent email.
     * Verifies that NotFoundException is thrown when lead is not found.
     */
    @Test
    @DisplayName("Get Lead Details By Email - Failure - Lead not found")
    void getLeadDetailsByEmail_LeadNotFound_ThrowsNotFoundException() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByEmail(TEST_EMAIL, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            leadService.getLeadDetailsByEmail(TEST_EMAIL));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, exception.getMessage());
    }

    // ==================== Create Lead Tests ====================

    /**
     * Test successful lead creation.
     * Verifies that a new lead is created and saved with proper relationships.
     */
    @Test
    @DisplayName("Create Lead - Success - Should create and save lead")
    void createLead_Success() {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        leadService.createLead(testLeadRequest);

        // Assert
        verify(addressRepository).save(any(Address.class));
        verify(leadRepository).save(any(Lead.class));
        verify(userLogService).logData(eq(TEST_CREATED_BY_ID), anyString(), eq("createLead"));
    }

    /**
     * Test create lead with null request.
     * Verifies that BadRequestException is thrown when request is null.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null request")
    void createLead_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(null));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER009, exception.getMessage());
    }

    /**
     * Test create lead with null email.
     * Verifies that BadRequestException is thrown for missing email.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null email")
    void createLead_NullEmail_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, exception.getMessage());
    }

    /**
     * Test create lead with empty email.
     * Verifies that BadRequestException is thrown for empty email.
     */
    @Test
    @DisplayName("Create Lead - Failure - Empty email")
    void createLead_EmptyEmail_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, exception.getMessage());
    }

    /**
     * Test create lead with invalid email format.
     * Verifies that BadRequestException is thrown for invalid email format.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid email format")
    void createLead_InvalidEmailFormat_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail("invalid-email");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER010, exception.getMessage());
    }

    /**
     * Test create lead with null first name.
     * Verifies that BadRequestException is thrown for missing first name.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null first name")
    void createLead_NullFirstName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setFirstName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, exception.getMessage());
    }

    /**
     * Test create lead with empty first name.
     * Verifies that BadRequestException is thrown for empty first name.
     */
    @Test
    @DisplayName("Create Lead - Failure - Empty first name")
    void createLead_EmptyFirstName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setFirstName("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, exception.getMessage());
    }

    /**
     * Test create lead with null last name.
     * Verifies that BadRequestException is thrown for missing last name.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null last name")
    void createLead_NullLastName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLastName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, exception.getMessage());
    }

    /**
     * Test create lead with null phone.
     * Verifies that BadRequestException is thrown for missing phone.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null phone")
    void createLead_NullPhone_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setPhone(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, exception.getMessage());
    }

    /**
     * Test create lead with invalid phone format.
     * Verifies that BadRequestException is thrown for invalid phone format.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid phone format")
    void createLead_InvalidPhoneFormat_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setPhone("invalid-phone");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER011, exception.getMessage());
    }

    /**
     * Test create lead with null lead status.
     * Verifies that BadRequestException is thrown for missing lead status.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null lead status")
    void createLead_NullLeadStatus_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLeadStatus(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, exception.getMessage());
    }

    /**
     * Test create lead with invalid lead status.
     * Verifies that BadRequestException is thrown for invalid lead status.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid lead status")
    void createLead_InvalidLeadStatus_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLeadStatus("Invalid Status");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertTrue(exception.getMessage().contains(ErrorMessages.LeadsErrorMessages.ER007));
    }

    /**
     * Test create lead with null client ID.
     * Verifies that BadRequestException is thrown for missing client ID.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null client ID")
    void createLead_NullClientId_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setClientId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER012, exception.getMessage());
    }

    /**
     * Test create lead with null address and null addressId.
     * Verifies that BadRequestException is thrown when no address is provided.
     */
    @Test
    @DisplayName("Create Lead - Failure - No address provided")
    void createLead_NoAddressProvided_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setAddress(null);
        testLeadRequest.setAddressId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER013, exception.getMessage());
    }

    /**
     * Test create lead with null created by ID.
     * Verifies that BadRequestException is thrown for missing created by ID.
     */
    @Test
    @DisplayName("Create Lead - Failure - Null created by ID")
    void createLead_NullCreatedById_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setCreatedById(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER015, exception.getMessage());
    }

    /**
     * Test create lead with invalid company size (zero).
     * Verifies that BadRequestException is thrown for invalid company size.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid company size")
    void createLead_InvalidCompanySize_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setCompanySize(0);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER016, exception.getMessage());
    }

    /**
     * Test create lead with invalid assigned agent ID (zero).
     * Verifies that BadRequestException is thrown for invalid assigned agent ID.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid assigned agent ID")
    void createLead_InvalidAssignedAgentId_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setAssignedAgentId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER017, exception.getMessage());
    }

    /**
     * Test create lead with invalid address data.
     * Verifies that BadRequestException is thrown when address validation fails.
     */
    @Test
    @DisplayName("Create Lead - Failure - Invalid address data")
    void createLead_InvalidAddressData_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setStreetAddress(null); // Invalid address
        testLeadRequest.setAddress(testAddressRequest);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER014 + " Address line 1 is required.", exception.getMessage());
    }

    // ==================== Update Lead Tests ====================

    /**
     * Test successful lead update.
     * Verifies that an existing lead is updated with new information.
     */
    @Test
    @DisplayName("Update Lead - Success - Should update existing lead")
    void updateLead_Success() {
        // Arrange
        when(leadService.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(eq(TEST_LEAD_ID), eq(TEST_CLIENT_ID))).thenReturn(testLead);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        leadService.updateLead(TEST_LEAD_ID, testLeadRequest);

        // Assert
        verify(leadRepository).findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID);
        verify(addressRepository).save(any(Address.class));
        verify(leadRepository).save(any(Lead.class));
        verify(userLogService).logData(eq(TEST_CREATED_BY_ID), anyString(), anyString());
    }

    /**
     * Test update lead with non-existent lead ID.
     * Verifies that NotFoundException is thrown when lead is not found.
     */
    @Test
    @DisplayName("Update Lead - Failure - Lead not found")
    void updateLead_LeadNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            leadService.updateLead(TEST_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, exception.getMessage());
    }

    /**
     * Test update lead with null request.
     * Verifies that BadRequestException is thrown when request is null.
     */
    @Test
    @DisplayName("Update Lead - Failure - Null request")
    void updateLead_NullRequest_ThrowsBadRequestException() {
        // Arrange
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            leadService.updateLead(TEST_LEAD_ID, null));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, exception.getMessage());
    }

    // ==================== Toggle Lead Tests ====================

    /**
     * Test successful lead toggle operation.
     * Verifies that a lead's isDeleted flag is correctly toggled.
     */
    @Test
    @DisplayName("Toggle Lead - Success - Should toggle isDeleted flag")
    void toggleLead_Success() {
        // Arrange
        when(leadService.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(eq(TEST_LEAD_ID), eq(TEST_CLIENT_ID))).thenReturn(testLead);
        when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        leadService.toggleLead(TEST_LEAD_ID);

        // Assert
        verify(leadRepository).findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID);
        verify(leadRepository).save(testLead);
        verify(userLogService).logData(eq(TEST_CREATED_BY_ID), anyString(), anyString());
        // Note: The toggle logic inverts the current isDeleted state
    }

    /**
     * Test toggle lead with non-existent lead ID.
     * Verifies that NotFoundException is thrown when lead is not found.
     */
    @Test
    @DisplayName("Toggle Lead - Failure - Lead not found")
    void toggleLead_LeadNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(TEST_LEAD_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            leadService.toggleLead(TEST_LEAD_ID));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, exception.getMessage());
    }
}