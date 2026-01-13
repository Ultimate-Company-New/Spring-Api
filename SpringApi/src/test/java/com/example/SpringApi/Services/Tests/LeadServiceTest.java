package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.LeadFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Services.LeadService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for LeadService.
 *
 * This test class covers:
 * - CRUD operations (create, read, update, toggle)
 * - Detailed lead retrieval (by ID, by Email)
 * - Complex filtering logic (GetLeadsInBatches with verified filter
 * combinations)
 * - Validation and error handling
 * - Integration with Address and UserLog services
 *
 * Uses BaseTest factory methods for consistent test data.
 *
 * @author SpringApi Team
 * @version 2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LeadService Unit Tests")
class LeadServiceTest extends BaseTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private LeadFilterQueryBuilder leadFilterQueryBuilder;

    @Mock
    private MessageService messageService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LeadService leadService;

    // Use 1L to match the default behavior of BaseService.getClientId() in test
    // environment
    private static final Long TEST_CLIENT_ID = 1L;

    private Lead testLead;
    private LeadRequestModel testLeadRequest;

    @BeforeEach
    void setUp() {
        // Create standard test data using BaseTest factory methods
        testLeadRequest = createValidLeadRequest(DEFAULT_LEAD_ID, TEST_CLIENT_ID);
        testLeadRequest.setStart(0);
        testLeadRequest.setEnd(10);

        testLead = createTestLead(testLeadRequest, DEFAULT_CREATED_USER);
        testLead.setClientId(TEST_CLIENT_ID); // Ensure entity has matching clientId

        // Mock Authorization header for BaseService authentication behavior
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock generic logData to avoid NPEs
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Mock AddressRepository save to return a valid address with ID to avoid NPEs
        // in service
        lenient().when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setAddressId(DEFAULT_ADDRESS_ID);
            return a;
        });
    }

    // ==================== GET LEADS IN BATCHES TESTS ====================

    @Nested
    @DisplayName("GetLeadsInBatches Tests")
    class GetLeadsInBatchesTests {

        @Test
        @DisplayName("Get Leads In Batches - Success - Simple retrieval without extra filters")
        void getLeadsInBatches_Success() {
            // Arrange
            List<Lead> leadList = Collections.singletonList(testLead);
            Page<Lead> leadPage = new PageImpl<>(leadList, PageRequest.of(0, 10), 1);

            when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(leadPage);

            // Act
            PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(testLead.getLeadId(), result.getData().get(0).getLeadId());
        }

        @Test
        @DisplayName("Get Leads In Batches - Invalid Pagination - ThrowsBadRequestException")
        void getLeadsInBatches_InvalidPagination_ThrowsBadRequestException() {
            // Arrange
            testLeadRequest.setStart(10);
            testLeadRequest.setEnd(5); // Invalid range

            // Act & Assert
            assertThrows(BadRequestException.class, () -> leadService.getLeadsInBatches(testLeadRequest));
        }

        /**
         * Comprehensive Triple Loop Test for Filter Validation.
         * Iterates through combinations of Columns, Operators, and Values to ensure
         * robust validation in the service layer.
         */
        @Test
        @DisplayName("Get Leads In Batches - Filter Logic Triple Loop Validation")
        void getLeadsInBatches_TripleLoopValidation() {
            // 1. Columns by Type
            String[] stringColumns = { "firstName", "email", "company", "annualRevenue", "fax", "lastName",
                    "leadStatus", "phone", "title", "website", "notes", "createdUser", "modifiedUser" };
            String[] numberColumns = { "leadId", "companySize", "clientId", "addressId", "createdById",
                    "assignedAgentId" };
            String[] booleanColumns = { "isDeleted" };
            String[] dateColumns = { "createdAt", "updatedAt" };
            String[] invalidColumns = { "invalidCol", "DROP TABLE" };

            // 2. Operators by Type Compatibility
            String[] stringOperators = {
                    PaginationBaseRequestModel.OP_CONTAINS, PaginationBaseRequestModel.OP_EQUALS,
                    PaginationBaseRequestModel.OP_STARTS_WITH, PaginationBaseRequestModel.OP_ENDS_WITH,
                    PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY,
                    PaginationBaseRequestModel.OP_IS_ONE_OF, PaginationBaseRequestModel.OP_IS_NOT_ONE_OF,
                    PaginationBaseRequestModel.OP_CONTAINS_ONE_OF
            };

            String[] numberOperators = {
                    PaginationBaseRequestModel.OP_EQUAL, PaginationBaseRequestModel.OP_NOT_EQUAL,
                    PaginationBaseRequestModel.OP_GREATER_THAN, PaginationBaseRequestModel.OP_GREATER_THAN_OR_EQUAL,
                    PaginationBaseRequestModel.OP_LESS_THAN, PaginationBaseRequestModel.OP_LESS_THAN_OR_EQUAL,
                    PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY,
                    PaginationBaseRequestModel.OP_NUMBER_IS_ONE_OF, PaginationBaseRequestModel.OP_NUMBER_IS_NOT_ONE_OF
            };

            String[] booleanOperators = { PaginationBaseRequestModel.OP_IS };

            String[] dateOperators = {
                    PaginationBaseRequestModel.OP_IS, PaginationBaseRequestModel.OP_IS_NOT,
                    PaginationBaseRequestModel.OP_IS_AFTER, PaginationBaseRequestModel.OP_IS_ON_OR_AFTER,
                    PaginationBaseRequestModel.OP_IS_BEFORE, PaginationBaseRequestModel.OP_IS_ON_OR_BEFORE,
                    PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY
            };

            String[] invalidOperators = { "INVALID_OP", "Unknown" };

            // 3. Values
            String[] validValues = { "test", "100", "2023-01-01", "true" };
            String[] emptyValues = { null, "" };

            // Setup common mocks
            Page<Lead> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), any(),
                    anyBoolean(), any())).thenReturn(emptyPage);

            // Mock column types
            lenient()
                    .when(leadFilterQueryBuilder
                            .getColumnType(argThat(arg -> Arrays.asList(stringColumns).contains(arg))))
                    .thenReturn("string");
            lenient()
                    .when(leadFilterQueryBuilder
                            .getColumnType(argThat(arg -> Arrays.asList(numberColumns).contains(arg))))
                    .thenReturn("number");
            lenient()
                    .when(leadFilterQueryBuilder
                            .getColumnType(argThat(arg -> Arrays.asList(booleanColumns).contains(arg))))
                    .thenReturn("boolean");
            lenient()
                    .when(leadFilterQueryBuilder
                            .getColumnType(argThat(arg -> Arrays.asList(dateColumns).contains(arg))))
                    .thenReturn("date");

            // Combine all inputs
            String[] allColumns = joinArrays(stringColumns, numberColumns, booleanColumns, dateColumns, invalidColumns);
            String[] allOperators = joinArrays(stringOperators, numberOperators, booleanOperators, dateOperators,
                    invalidOperators);
            // Use set to dedup operators for the loop to avoid redundant checks
            Set<String> uniqueOperators = new HashSet<>(Arrays.asList(allOperators));

            // Loop 1: Columns
            for (String column : allColumns) {
                // Loop 2: Operators
                for (String operator : uniqueOperators) {
                    // Loop 3: Values
                    for (String value : joinArrays(validValues, emptyValues)) {

                        // Prepare Request
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        testLeadRequest.setFilters(Collections.singletonList(filter));

                        // 4. Determine Validity
                        boolean isColumnKnown = !Arrays.asList(invalidColumns).contains(column);

                        boolean isValidForString = Arrays.asList(stringColumns).contains(column)
                                && Arrays.asList(stringOperators).contains(operator);
                        boolean isValidForNumber = Arrays.asList(numberColumns).contains(column)
                                && Arrays.asList(numberOperators).contains(operator);
                        boolean isValidForBoolean = Arrays.asList(booleanColumns).contains(column)
                                && Arrays.asList(booleanOperators).contains(operator);
                        boolean isValidForDate = Arrays.asList(dateColumns).contains(column)
                                && Arrays.asList(dateOperators).contains(operator);

                        boolean isOperatorValidForType = isValidForString || isValidForNumber || isValidForBoolean
                                || isValidForDate;

                        // Check value requirement
                        boolean isValueRequired = true;
                        if (operator.equals(PaginationBaseRequestModel.OP_IS_EMPTY) ||
                                operator.equals(PaginationBaseRequestModel.OP_IS_NOT_EMPTY)) {
                            isValueRequired = false;
                        }

                        boolean isValuePresent = value != null; // Service allows empty strings as "present" usually, or
                                                                // at least doesn't throw immediate NPE before logic

                        boolean shouldSucceed = isColumnKnown && isOperatorValidForType
                                && (!isValueRequired || isValuePresent);

                        // Execute
                        try {
                            leadService.getLeadsInBatches(testLeadRequest);

                            if (!shouldSucceed) {
                                // Fail if it succeeded but shouldn't have
                                String reason = "";
                                if (!isColumnKnown)
                                    reason = "Invalid column: " + column;
                                else if (!isOperatorValidForType)
                                    reason = "Invalid operator '" + operator + "' for column '" + column + "'";
                                else if (isValueRequired && !isValuePresent)
                                    reason = "Missing value for operator " + operator;

                                fail("Expected failure but succeeded. Context: " + reason);
                            }
                        } catch (BadRequestException | IllegalArgumentException e) {
                            if (shouldSucceed) {
                                fail("Expected success but failed: Col=" + column + " Op=" + operator + " Val=" + value
                                        + ". Error: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper to join arrays
    private String[] joinArrays(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays)
            length += array.length;
        String[] result = new String[length];
        int offset = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    // ==================== GET LEAD DETAILS TESTS ====================

    @Nested
    @DisplayName("GetLeadDetails Tests")
    class GetLeadDetailsTests {

        @Test
        @DisplayName("Get Lead By ID - Success")
        void getLeadDetailsById_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID)).thenReturn(testLead);

            // Act
            LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

            // Assert
            assertNotNull(result);
            assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
        }

        @Test
        @DisplayName("Get Lead By ID - NotFound - ThrowsNotFoundException")
        void getLeadDetailsById_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(anyLong(), anyLong())).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(DEFAULT_LEAD_ID));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By ID - Negative ID - ThrowsNotFoundException")
        void getLeadDetailsById_NegativeId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(-1L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(-1L));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By ID - Zero ID - ThrowsNotFoundException")
        void getLeadDetailsById_ZeroId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(0L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(0L));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By ID - Max Long ID - ThrowsNotFoundException")
        void getLeadDetailsById_MaxLongId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By ID - Min Long ID - ThrowsNotFoundException")
        void getLeadDetailsById_MinLongId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsById(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By ID - All fields populated - Success")
        void getLeadDetailsById_AllFieldsPopulated_Success() {
            // Arrange
            testLead.setLeadId(DEFAULT_LEAD_ID);
            testLead.setFirstName("John");
            testLead.setLastName("Doe");
            testLead.setEmail(DEFAULT_EMAIL);
            testLead.setPhone("555-0100");
            testLead.setCompany("Tech Corp");
            testLead.setTitle("Manager");
            testLead.setLeadStatus("New");
            when(leadRepository.findLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID)).thenReturn(testLead);

            // Act
            LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

            // Assert
            assertNotNull(result);
            assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals(DEFAULT_EMAIL, result.getEmail());
            assertEquals("555-0100", result.getPhone());
            assertEquals("Tech Corp", result.getCompany());
            assertEquals("Manager", result.getTitle());
            assertEquals("New", result.getLeadStatus());
        }

        @Test
        @DisplayName("Get Lead By Email - Success")
        void getLeadDetailsByEmail_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByEmail(DEFAULT_EMAIL, TEST_CLIENT_ID)).thenReturn(testLead);

            // Act
            LeadResponseModel result = leadService.getLeadDetailsByEmail(DEFAULT_EMAIL);

            // Assert
            assertNotNull(result);
            assertEquals(DEFAULT_EMAIL, result.getEmail());
        }

        @Test
        @DisplayName("Get Lead By Email - NotFound - ThrowsNotFoundException")
        void getLeadDetailsByEmail_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsByEmail("unknown@example.com"));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Get Lead By Email - Empty Email - ThrowsNotFoundException")
        void getLeadDetailsByEmail_EmptyEmail_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByEmail("", TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsByEmail(""));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }
    }

    // ==================== CREATE LEAD TESTS ====================

    @Nested
    @DisplayName("CreateLead Tests")
    class CreateLeadTests {

        @Test
        @DisplayName("Create Lead - Success")
        void createLead_Success() {
            // Arrange
            // Arrange
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
            verify(userLogService).logData(anyLong(), anyString(), anyString());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Lead - Invalid Email - ThrowsBadRequestException")
        void createLead_InvalidEmail_ThrowsBadRequestException(String email) {
            testLeadRequest.setEmail(email);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER001,
                    () -> leadService.createLead(testLeadRequest));
        }

        @Test
        @DisplayName("Create Lead - Invalid Email Format - ThrowsBadRequestException")
        void createLead_InvalidEmailFormat_ThrowsBadRequestException() {
            testLeadRequest.setEmail("invalid-email");
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER010,
                    () -> leadService.createLead(testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Lead - Invalid First Name - ThrowsBadRequestException")
        void createLead_InvalidFirstName_ThrowsBadRequestException(String firstName) {
            testLeadRequest.setFirstName(firstName);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER002,
                    () -> leadService.createLead(testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Lead - Invalid Last Name - ThrowsBadRequestException")
        void createLead_InvalidLastName_ThrowsBadRequestException(String lastName) {
            testLeadRequest.setLastName(lastName);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER003,
                    () -> leadService.createLead(testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Lead - Invalid Phone - ThrowsBadRequestException")
        void createLead_InvalidPhone_ThrowsBadRequestException(String phone) {
            testLeadRequest.setPhone(phone);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER004,
                    () -> leadService.createLead(testLeadRequest));
        }

        @Test
        @DisplayName("Create Lead - Invalid Phone Format - ThrowsBadRequestException")
        void createLead_InvalidPhoneFormat_ThrowsBadRequestException() {
            testLeadRequest.setPhone("invalid-phone");
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER011,
                    () -> leadService.createLead(testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Create Lead - Invalid Status (Empty) - ThrowsBadRequestException")
        void createLead_InvalidStatusEmpty_ThrowsBadRequestException(String status) {
            testLeadRequest.setLeadStatus(status);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER008,
                    () -> leadService.createLead(testLeadRequest));
        }

        @Test
        @DisplayName("Create Lead - Invalid Status (Unknown) - ThrowsBadRequestException")
        void createLead_InvalidStatusUnknown_ThrowsBadRequestException() {
            testLeadRequest.setLeadStatus("UnknownStatus");
            // ER007 message usually appends allowed statuses, so we check using contains
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> leadService.createLead(testLeadRequest));
            assertTrue(ex.getMessage().contains(ErrorMessages.LeadsErrorMessages.ER007));
        }

        @Test
        @DisplayName("Create Lead - Invalid Logic - Missing Address and AddressID")
        void createLead_MissingAddress_ThrowsBadRequestException() {
            testLeadRequest.setAddress(null);
            testLeadRequest.setAddressId(null);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER013,
                    () -> leadService.createLead(testLeadRequest));
        }

        @Test
        @DisplayName("Create Lead - Invalid Company Size - Negative")
        void createLead_NegativeCompanySize_ThrowsBadRequestException() {
            testLeadRequest.setCompanySize(-5);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER016,
                    () -> leadService.createLead(testLeadRequest));
        }

        @Test
        @DisplayName("Create Lead - Null Request - ThrowsBadRequestException")
        void createLead_NullRequest_ThrowsBadRequestException() {
            assertThrows(BadRequestException.class, () -> leadService.createLead(null));
        }

        @Test
        @DisplayName("Create Lead - Valid Company - Success")
        void createLead_ValidCompany_Success() {
            // Arrange
            testLeadRequest.setCompany("Valid Tech Company");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Valid Title - Success")
        void createLead_ValidTitle_Success() {
            // Arrange
            testLeadRequest.setTitle("Senior Manager");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Very Long Email - Success")
        void createLead_VeryLongEmail_Success() {
            // Arrange
            testLeadRequest.setEmail("verylongemailaddress.withmanydots.test@verylongdomainname.co.uk");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Special Characters in Name - Success")
        void createLead_SpecialCharactersInName_Success() {
            // Arrange
            testLeadRequest.setFirstName("José");
            testLeadRequest.setLastName("García-López");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Unicode Characters - Success")
        void createLead_UnicodeCharacters_Success() {
            // Arrange
            testLeadRequest.setFirstName("李");
            testLeadRequest.setLastName("王");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Max Company Size - Success")
        void createLead_MaxCompanySize_Success() {
            // Arrange
            testLeadRequest.setCompanySize(Integer.MAX_VALUE);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Create Lead - Zero Company Size - Success")
        void createLead_ZeroCompanySize_Success() {
            // Arrange
            testLeadRequest.setCompanySize(0);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

    }

    // ==================== UPDATE LEAD TESTS ====================

    @Nested
    @DisplayName("UpdateLead Tests")
    class UpdateLeadTests {

        @Test
        @DisplayName("Update Lead - Success")
        void updateLead_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);
            when(addressRepository.save(any(Address.class))).thenReturn(testLead.getAddress());

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Update Lead - NotFound - ThrowsNotFoundException")
        void updateLead_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(anyLong(), anyLong())).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Update Lead - Deleted - ThrowsNotFoundException")
        void updateLead_Deleted_ThrowsNotFoundException() {
            // Arrange
            testLead.setIsDeleted(true);
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Lead - Invalid Email - ThrowsBadRequestException")
        void updateLead_InvalidEmail_ThrowsBadRequestException(String email) {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setEmail(email);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER001,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @Test
        @DisplayName("Update Lead - Invalid Email Format")
        void updateLead_InvalidEmailFormat_ThrowsBadRequestException() {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setEmail("invalid-email");
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER010,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Lead - Invalid First Name")
        void updateLead_InvalidFirstName_ThrowsBadRequestException(String firstName) {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setFirstName(firstName);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER002,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Lead - Invalid Last Name - ThrowsBadRequestException")
        void updateLead_InvalidLastName_ThrowsBadRequestException(String lastName) {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setLastName(lastName);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER003,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   " })
        @DisplayName("Update Lead - Invalid Phone")
        void updateLead_InvalidPhone_ThrowsBadRequestException(String phone) {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setPhone(phone);
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER004,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @Test
        @DisplayName("Update Lead - Invalid Phone Format - ThrowsBadRequestException")
        void updateLead_InvalidPhoneFormat_ThrowsBadRequestException() {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setPhone("invalid-phone");
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER011,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = { "", "   ", "InvalidStatus" })
        @DisplayName("Update Lead - Invalid Status")
        void updateLead_InvalidStatus_ThrowsBadRequestException(String status) {
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setLeadStatus(status);
            // Handling specific error for status (Null/Empty vs Invalid)
            if (status == null || status.trim().isEmpty()) {
                assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER008,
                        () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
            } else {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
                assertTrue(ex.getMessage().contains(ErrorMessages.LeadsErrorMessages.ER007));
            }
        }

        @Test
        @DisplayName("Update Lead - Negative ID - ThrowsNotFoundException")
        void updateLead_NegativeId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(-1L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.updateLead(-1L, testLeadRequest));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Update Lead - Zero ID - ThrowsNotFoundException")
        void updateLead_ZeroId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(0L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.updateLead(0L, testLeadRequest));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Update Lead - Null Request - ThrowsBadRequestException")
        void updateLead_NullRequest_ThrowsBadRequestException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);

            // Act & Assert
            assertThrows(BadRequestException.class, () -> leadService.updateLead(DEFAULT_LEAD_ID, null));
        }

        @Test
        @DisplayName("Update Lead - All fields updated - Success")
        void updateLead_AllFieldsUpdated_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setFirstName("UpdatedFirst");
            testLeadRequest.setLastName("UpdatedLast");
            testLeadRequest.setEmail("updated@example.com");
            testLeadRequest.setPhone("555-9999");
            testLeadRequest.setCompany("UpdatedCorp");
            testLeadRequest.setTitle("Director");
            testLeadRequest.setLeadStatus("Qualified");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Update Lead - Valid Company Size Zero - Success")
        void updateLead_ValidCompanySizeZero_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setCompanySize(0);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Update Lead - Very Long Name - Success")
        void updateLead_VeryLongName_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setFirstName("VeryVeryVeryVeryVeryLongFirstName");
            testLeadRequest.setLastName("VeryVeryVeryVeryVeryLongLastName");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Update Lead - Special Characters - Success")
        void updateLead_SpecialCharacters_Success() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setFirstName("François");
            testLeadRequest.setCompany("O'Reilly & Associates");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }
    }

    // ==================== TOGGLE LEAD TESTS ====================

    @Nested
    @DisplayName("ToggleLead Tests")
    class ToggleLeadTests {

        @Test
        @DisplayName("Toggle Lead - Success")
        void toggleLead_Success() {
            // Arrange
            testLead.setIsDeleted(false);
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            leadService.toggleLead(DEFAULT_LEAD_ID);

            // Assert
            assertTrue(testLead.getIsDeleted());
            verify(leadRepository).save(testLead);
        }

        @Test
        @DisplayName("Toggle Lead - NotFound - ThrowsNotFoundException")
        void toggleLead_NotFound_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(anyLong(), anyLong())).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(DEFAULT_LEAD_ID));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle Lead - Negative ID - ThrowsNotFoundException")
        void toggleLead_NegativeId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(-1L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(-1L));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle Lead - Zero ID - ThrowsNotFoundException")
        void toggleLead_ZeroId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(0L, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(0L));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle Lead - Max Long ID - ThrowsNotFoundException")
        void toggleLead_MaxLongId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(Long.MAX_VALUE));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle Lead - Min Long ID - ThrowsNotFoundException")
        void toggleLead_MinLongId_ThrowsNotFoundException() {
            // Arrange
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.toggleLead(Long.MIN_VALUE));
            assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
        }

        @Test
        @DisplayName("Toggle Lead - Multiple Toggles - State changes correctly")
        void toggleLead_MultipleToggles_Success() {
            // Arrange
            testLead.setIsDeleted(false);
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act & Assert - First toggle: false -> true
            leadService.toggleLead(DEFAULT_LEAD_ID);
            assertTrue(testLead.getIsDeleted());
            verify(leadRepository, times(1)).save(testLead);

            // Second toggle: true -> false
            testLead.setIsDeleted(true);
            leadService.toggleLead(DEFAULT_LEAD_ID);
            assertFalse(testLead.getIsDeleted());
            verify(leadRepository, times(2)).save(testLead);
        }
    }

    // Helper method for asserting BadRequestException with specific message
    private void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }
}
