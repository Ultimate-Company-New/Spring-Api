package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.LeadFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeadService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetLeadsInBatchesTests                  | 8               |
 * | GetLeadDetailsTests                     | 10              |
 * | CreateLeadTests                         | 15              |
 * | UpdateLeadTests                         | 16              |
 * | ToggleLeadTests                         | 8               |
 * | BulkCreateLeadsTests                    | 12              |
 * | **Total**                               | **69**          |
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

        // Set up RequestContextHolder so BaseService.getClientId()/getUserId()/getUser() work
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

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


    @Nested
    @DisplayName("GetLeadsInBatches Tests")
    class GetLeadsInBatchesTests {

        /**
         * Single comprehensive unit test for getLeadsInBatches.
         * Covers: (1) invalid pagination → BadRequestException with message,
         * (2) success without filters, (3) triple-loop validation over all valid
         * column×operator×type combinations plus invalid column/operator/value combinations.
         */
        @Test
        @DisplayName("Get Leads In Batches - Invalid pagination, success no filters, and triple-loop filter validation")
        void getLeadsInBatches_SingleComprehensiveTest() {
            // ---- (1) Invalid pagination: end <= start ----
            testLeadRequest.setStart(10);
            testLeadRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> leadService.getLeadsInBatches(testLeadRequest));

            // ---- (2) Success: simple retrieval without filters ----
            testLeadRequest.setStart(0);
            testLeadRequest.setEnd(10);
            testLeadRequest.setFilters(null);
            List<Lead> leadList = Collections.singletonList(testLead);
            Page<Lead> leadPage = new PageImpl<>(leadList, PageRequest.of(0, 10), 1);
            when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(leadPage);
            PaginationBaseResponseModel<LeadResponseModel> result = leadService.getLeadsInBatches(testLeadRequest);
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(testLead.getLeadId(), result.getData().get(0).getLeadId());

            // ---- (3) Triple-loop: valid columns × operators × types + invalid combinations ----
            String[] stringColumns = LEAD_STRING_COLUMNS;
            String[] numberColumns = LEAD_NUMBER_COLUMNS;
            String[] booleanColumns = LEAD_BOOLEAN_COLUMNS;
            String[] dateColumns = LEAD_DATE_COLUMNS;
            String[] invalidColumns = BATCH_INVALID_COLUMNS;
            String[] stringOperators = BATCH_STRING_OPERATORS;
            String[] numberOperators = BATCH_NUMBER_OPERATORS;
            String[] booleanOperators = BATCH_BOOLEAN_OPERATORS;
            String[] dateOperators = BATCH_DATE_OPERATORS;
            String[] invalidOperators = BATCH_INVALID_OPERATORS;
            String[] validValues = BATCH_VALID_VALUES;
            String[] emptyValues = BATCH_EMPTY_VALUES;

            Page<Lead> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), any(),
                    anyBoolean(), any())).thenReturn(emptyPage);
            lenient()
                    .when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(stringColumns).contains(arg))))
                    .thenReturn("string");
            lenient()
                    .when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(numberColumns).contains(arg))))
                    .thenReturn("number");
            lenient()
                    .when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(booleanColumns).contains(arg))))
                    .thenReturn("boolean");
            lenient()
                    .when(leadFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(dateColumns).contains(arg))))
                    .thenReturn("date");

            String[] allColumns = joinArrays(stringColumns, numberColumns, booleanColumns, dateColumns, invalidColumns);
            String[] allOperators = joinArrays(stringOperators, numberOperators, booleanOperators, dateOperators, invalidOperators);
            Set<String> uniqueOperators = new HashSet<>(Arrays.asList(allOperators));
            String[] allValues = joinArrays(validValues, emptyValues);

            for (String column : allColumns) {
                for (String operator : uniqueOperators) {
                    for (String value : allValues) {
                        testLeadRequest.setStart(0);
                        testLeadRequest.setEnd(10);
                        FilterCondition filter = createFilterCondition(column, operator, value);
                        testLeadRequest.setFilters(Collections.singletonList(filter));

                        boolean isColumnKnown = !Arrays.asList(invalidColumns).contains(column);
                        boolean isValidForString = Arrays.asList(stringColumns).contains(column)
                                && Arrays.asList(stringOperators).contains(operator);
                        boolean isValidForNumber = Arrays.asList(numberColumns).contains(column)
                                && Arrays.asList(numberOperators).contains(operator);
                        boolean isValidForBoolean = Arrays.asList(booleanColumns).contains(column)
                                && Arrays.asList(booleanOperators).contains(operator);
                        boolean isValidForDate = Arrays.asList(dateColumns).contains(column)
                                && Arrays.asList(dateOperators).contains(operator);
                        boolean isOperatorValidForType = isValidForString || isValidForNumber || isValidForBoolean || isValidForDate;

                        boolean isValueRequired = !PaginationBaseRequestModel.OP_IS_EMPTY.equals(operator)
                                && !PaginationBaseRequestModel.OP_IS_NOT_EMPTY.equals(operator);
                        boolean isValuePresent = value != null;
                        boolean shouldSucceed = isColumnKnown && isOperatorValidForType && (!isValueRequired || isValuePresent);

                        try {
                            leadService.getLeadsInBatches(testLeadRequest);
                            if (!shouldSucceed) {
                                String reason = !isColumnKnown ? "Invalid column: " + column
                                        : !isOperatorValidForType ? "Invalid operator '" + operator + "' for column '" + column + "'"
                                        : "Missing value for operator " + operator;
                                fail("Expected failure but succeeded. Context: " + reason);
                            }
                        } catch (BadRequestException | IllegalArgumentException e) {
                            if (shouldSucceed) {
                                fail("Expected success but failed: Col=" + column + " Op=" + operator + " Val=" + value + ". Error: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }


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
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER009, () -> leadService.createLead(null));
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
        @DisplayName("Create Lead - Zero Company Size - ThrowsBadRequestException")
        void createLead_ZeroCompanySize_ThrowsBadRequestException() {
            // Arrange - Lead model rejects company size 0 (must be null or > 0)
            testLeadRequest.setCompanySize(0);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER016,
                    () -> leadService.createLead(testLeadRequest));
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
            // Service validates null before accessing repository
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER009, () -> leadService.updateLead(DEFAULT_LEAD_ID, null));
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
            testLeadRequest.setPhone("5551234567");
            testLeadRequest.setCompany("UpdatedCorp");
            testLeadRequest.setTitle("Director");
            testLeadRequest.setLeadStatus("Contacted");
            when(leadRepository.save(any(Lead.class))).thenReturn(testLead);

            // Act
            assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

            // Assert
            verify(leadRepository).save(any(Lead.class));
        }

        @Test
        @DisplayName("Update Lead - Valid Company Size Zero - ThrowsBadRequestException")
        void updateLead_ValidCompanySizeZero_ThrowsBadRequestException() {
            // Arrange - Lead model rejects company size 0 (must be null or > 0)
            when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                    .thenReturn(testLead);
            testLeadRequest.setCompanySize(0);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.LeadsErrorMessages.ER016,
                    () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
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

    @Nested
    @DisplayName("Bulk Create Leads Tests")
    class BulkCreateLeadsTests {

        @Test
        @DisplayName("Bulk Create Leads - All Valid - Success")
        void bulkCreateLeads_AllValid_Success() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
                leadReq.setEmail("bulklead" + i + "@test.com");
                leadReq.setFirstName("BulkFirst" + i);
                leadReq.setLastName("BulkLast" + i);
                leadReq.setPhone("555000000" + i); // 10 digits required by PHONE_REGEX
                leads.add(leadReq);
            }

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 1000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 1000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getTotalRequested());
            assertEquals(5, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
            verify(leadRepository, times(5)).save(any(Lead.class));
        }

        @Test
        @DisplayName("Bulk Create Leads - Partial Success")
        void bulkCreateLeads_PartialSuccess() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            
            // Add 3 valid leads
            for (int i = 0; i < 3; i++) {
                LeadRequestModel validLead = createValidLeadRequest(null, TEST_CLIENT_ID);
                validLead.setEmail("validlead" + i + "@test.com");
                leads.add(validLead);
            }
            
            // Add 2 invalid leads
            LeadRequestModel invalidLead1 = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalidLead1.setEmail(""); // Invalid email
            leads.add(invalidLead1);
            
            LeadRequestModel invalidLead2 = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalidLead2.setFirstName(null); // Invalid first name
            leads.add(invalidLead2);

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 1000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 1000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getTotalRequested());
            assertEquals(3, result.getSuccessCount());
            assertEquals(2, result.getFailureCount());
            verify(leadRepository, times(3)).save(any(Lead.class));
        }

        @Test
        @DisplayName("Bulk Create Leads - Empty List - ThrowsBadRequestException")
        void bulkCreateLeads_EmptyList_ThrowsBadRequestException() {
            // Act & Assert
            assertThrowsBadRequest(
                    String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Lead"),
                    () -> leadService.bulkCreateLeads(new ArrayList<>()));
        }

        @Test
        @DisplayName("Bulk Create Leads - Null List - ThrowsBadRequestException")
        void bulkCreateLeads_NullList_ThrowsBadRequestException() {
            // Act & Assert
            assertThrowsBadRequest(
                    String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Lead"),
                    () -> leadService.bulkCreateLeads(null));
        }

        @Test
        @DisplayName("Bulk Create Leads - Single Lead - Success")
        void bulkCreateLeads_SingleLead_Success() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
            leads.add(leadReq);

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId(100L);
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId(100L);
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create Leads - All Invalid - AllFail")
        void bulkCreateLeads_AllInvalid_AllFail() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            
            for (int i = 0; i < 3; i++) {
                LeadRequestModel invalidLead = createValidLeadRequest(null, TEST_CLIENT_ID);
                invalidLead.setEmail(""); // All have invalid email
                leads.add(invalidLead);
            }

            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(3, result.getFailureCount());
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Bulk Create Leads - Many Leads - Success")
        void bulkCreateLeads_ManyLeads_Success() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
                leadReq.setEmail("bulklead" + i + "@test.com");
                leadReq.setFirstName("First" + i);
                leadReq.setLastName("Last" + i);
                leadReq.setPhone("555" + String.format("%07d", i)); // 10 digits required by PHONE_REGEX
                leads.add(leadReq);
            }

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 10000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 10000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(50, result.getTotalRequested());
            assertEquals(50, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
            verify(leadRepository, times(50)).save(any(Lead.class));
        }

        @Test
        @DisplayName("Bulk Create Leads - Verify Logging Called")
        void bulkCreateLeads_VerifyLoggingCalled() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
                leadReq.setEmail("lead" + i + "@test.com");
                leads.add(leadReq);
            }

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 1000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 1000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            leadService.bulkCreateLeads(leads);

            // Assert
            verify(userLogService, atLeastOnce()).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Bulk Create Leads - Invalid Email Format - Fails")
        void bulkCreateLeads_InvalidEmailFormat_Fails() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            
            LeadRequestModel validLead = createValidLeadRequest(null, TEST_CLIENT_ID);
            validLead.setEmail("valid@test.com");
            leads.add(validLead);
            
            LeadRequestModel invalidLead = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalidLead.setEmail("invalid-email-format");
            leads.add(invalidLead);

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId(100L);
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(eq("valid@test.com"), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId(100L);
                lead.setEmail("valid@test.com");
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        @Test
        @DisplayName("Bulk Create Leads - Missing Required Fields - Fails")
        void bulkCreateLeads_MissingRequiredFields_Fails() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            
            LeadRequestModel noFirstName = createValidLeadRequest(null, TEST_CLIENT_ID);
            noFirstName.setFirstName(null);
            leads.add(noFirstName);
            
            LeadRequestModel noLastName = createValidLeadRequest(null, TEST_CLIENT_ID);
            noLastName.setLastName("");
            leads.add(noLastName);
            
            LeadRequestModel noPhone = createValidLeadRequest(null, TEST_CLIENT_ID);
            noPhone.setPhone("   ");
            leads.add(noPhone);

            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertEquals(3, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(3, result.getFailureCount());
            verify(leadRepository, never()).save(any(Lead.class));
        }

        @Test
        @DisplayName("Bulk Create Leads - Verify Message Notification Sent")
        void bulkCreateLeads_VerifyMessageNotification() {
            // Arrange - sync bulkCreateLeads does not send message notifications (only async does)
            List<LeadRequestModel> leads = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                LeadRequestModel leadReq = createValidLeadRequest(null, TEST_CLIENT_ID);
                leadReq.setEmail("lead" + i + "@test.com");
                leads.add(leadReq);
            }

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 1000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 1000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert - sync bulkCreateLeads returns results but does not call createMessageWithContext
            assertNotNull(result);
            assertEquals(3, result.getSuccessCount());
            verify(messageService, never()).createMessageWithContext(any(), anyLong(), anyString(), anyLong());
        }

        @Test
        @DisplayName("Bulk Create Leads - Mixed Valid And Invalid - PartialSuccess")
        void bulkCreateLeads_MixedValidAndInvalid_PartialSuccess() {
            // Arrange
            List<LeadRequestModel> leads = new ArrayList<>();
            
            // Valid
            LeadRequestModel valid1 = createValidLeadRequest(null, TEST_CLIENT_ID);
            valid1.setEmail("valid1@test.com");
            leads.add(valid1);
            
            // Invalid - bad email
            LeadRequestModel invalid1 = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalid1.setEmail("bademail");
            leads.add(invalid1);
            
            // Valid
            LeadRequestModel valid2 = createValidLeadRequest(null, TEST_CLIENT_ID);
            valid2.setEmail("valid2@test.com");
            leads.add(valid2);
            
            // Invalid - missing phone
            LeadRequestModel invalid2 = createValidLeadRequest(null, TEST_CLIENT_ID);
            invalid2.setPhone("");
            leads.add(invalid2);
            
            // Valid
            LeadRequestModel valid3 = createValidLeadRequest(null, TEST_CLIENT_ID);
            valid3.setEmail("valid3@test.com");
            leads.add(valid3);

            when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
                Lead lead = inv.getArgument(0);
                lead.setLeadId((long) (Math.random() * 1000));
                return lead;
            });
            when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong())).thenAnswer(inv -> {
                Lead lead = new Lead();
                lead.setLeadId((long) (Math.random() * 1000));
                lead.setEmail(inv.getArgument(0));
                return lead;
            });
            lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            var result = leadService.bulkCreateLeads(leads);

            // Assert
            assertNotNull(result);
            assertEquals(5, result.getTotalRequested());
            assertEquals(3, result.getSuccessCount());
            assertEquals(2, result.getFailureCount());
            verify(leadRepository, times(3)).save(any(Lead.class));
            
            // Verify results contain error messages for failures
            assertEquals(5, result.getResults().size());
            long failureCount = result.getResults().stream()
                    .filter(r -> !r.isSuccess())
                    .count();
            assertEquals(2, failureCount);
        }
    }
}
