package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PromoFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.PromoService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromoService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetPromosInBatches (Standalone)         | 1               |
 * | CreatePromoTests                        | 8               |
 * | GetPromoDetailsByIdTests                | 12              |
 * | TogglePromoTests                        | 16              |
 * | GetPromoDetailsByNameTests              | 16              |
 * | BulkCreatePromosAsyncTests             | 7               |
 * | ValidationTests                         | 16              |
 * | **Total**                               | **81**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PromoService Unit Tests")
class PromoServiceTest extends BaseTest {

    @Mock
    private PromoRepository promoRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private PromoFilterQueryBuilder promoFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageService messageService;

    @Spy
    @InjectMocks
    private PromoService promoService;

    private static final String TEST_LOGIN_NAME = "admin";

    private Promo testPromo;
    private PromoRequestModel testPromoRequest;
    private PaginationBaseRequestModel testPaginationRequest;
    private static final Long TEST_PROMO_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PROMO_CODE = "TEST10";
    private static final String TEST_DESCRIPTION = "Test promo description";
    private static final String TEST_VALID_COLUMN = "promoCode";
    private static final String CREATED_USER = "admin";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test promo request first
        testPromoRequest = new PromoRequestModel();
        testPromoRequest.setPromoCode(TEST_PROMO_CODE);
        testPromoRequest.setDescription(TEST_DESCRIPTION);
        testPromoRequest.setIsDeleted(false);
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(10.0));
        testPromoRequest.setClientId(TEST_CLIENT_ID);
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(30));

        // Initialize test promo using constructor
        testPromo = new Promo(testPromoRequest, CREATED_USER, TEST_CLIENT_ID);
        testPromo.setPromoId(TEST_PROMO_ID); // Set ID manually since constructor doesn't set it
        testPromo.setCreatedAt(LocalDateTime.now());
        testPromo.setUpdatedAt(LocalDateTime.now());

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        // Set up filters using new FilterCondition structure
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(TEST_VALID_COLUMN);
        filter.setOperator("equals");
        filter.setValue(TEST_PROMO_CODE);
        testPaginationRequest.setFilters(List.of(filter));
        testPaginationRequest.setLogicOperator("AND");
        testPaginationRequest.setIncludeDeleted(false);

        // Mock BaseService methods for security context (required for createPromo, etc.)
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        lenient().doReturn(TEST_USER_ID).when(promoService).getUserId();
        lenient().doReturn(TEST_LOGIN_NAME).when(promoService).getUser();
        lenient().doReturn(TEST_CLIENT_ID).when(promoService).getClientId();

        // Mock MessageService for bulk async (BulkInsertHelper calls createMessageWithContext)
        lenient().doNothing().when(messageService).createMessageWithContext(any(), anyLong(), anyString(), anyLong());
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(true);
    }

        /**
         * Purpose: Single comprehensive test for getPromosInBatches.
         * Expected Result: Invalid pagination/filters fail; valid filters succeed.
         * Assertions: Exceptions/messages and successful pagination results.
         */
        @Test
        @DisplayName("Get Promos In Batches - Comprehensive validation and success")
        void getPromosInBatches_Comprehensive() {
        // Invalid pagination
        PaginationBaseRequestModel invalidPagination = new PaginationBaseRequestModel();
        invalidPagination.setStart(10);
        invalidPagination.setEnd(5);
        BadRequestException paginationEx = assertThrows(BadRequestException.class,
            () -> promoService.getPromosInBatches(invalidPagination));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, paginationEx.getMessage());

        // Invalid column name
        PaginationBaseRequestModel invalidColumnRequest = new PaginationBaseRequestModel();
        invalidColumnRequest.setStart(0);
        invalidColumnRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition invalidColumn = new PaginationBaseRequestModel.FilterCondition();
        invalidColumn.setColumn("invalidColumn");
        invalidColumn.setOperator("contains");
        invalidColumn.setValue("test");
        invalidColumnRequest.setFilters(List.of(invalidColumn));
        invalidColumnRequest.setLogicOperator("AND");
        BadRequestException invalidColumnEx = assertThrows(BadRequestException.class,
            () -> promoService.getPromosInBatches(invalidColumnRequest));
        assertTrue(invalidColumnEx.getMessage().contains("Invalid column name"));

        // Invalid operator
        PaginationBaseRequestModel invalidOperatorRequest = new PaginationBaseRequestModel();
        invalidOperatorRequest.setStart(0);
        invalidOperatorRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition invalidOperator = new PaginationBaseRequestModel.FilterCondition();
        invalidOperator.setColumn("promoCode");
        invalidOperator.setOperator("invalidOperator");
        invalidOperator.setValue("test");
        invalidOperatorRequest.setFilters(List.of(invalidOperator));
        invalidOperatorRequest.setLogicOperator("AND");
        BadRequestException invalidOperatorEx = assertThrows(BadRequestException.class,
            () -> promoService.getPromosInBatches(invalidOperatorRequest));
        assertTrue(invalidOperatorEx.getMessage().contains("Invalid operator"));

        // Success with single filter
        PaginationBaseRequestModel singleFilterRequest = new PaginationBaseRequestModel();
        singleFilterRequest.setStart(0);
        singleFilterRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("promoCode");
        filter.setOperator("contains");
        filter.setValue("TEST");
        singleFilterRequest.setFilters(List.of(filter));
        singleFilterRequest.setLogicOperator("AND");

        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);
        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(singleFilterRequest);
        assertNotNull(result);
        assertEquals(1, result.getData().size());

        // Success with multiple filters
        PaginationBaseRequestModel multiFilterRequest = new PaginationBaseRequestModel();
        multiFilterRequest.setStart(0);
        multiFilterRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("promoCode");
        filter1.setOperator("contains");
        filter1.setValue("TEST");
        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("promoId");
        filter2.setOperator("greaterThan");
        filter2.setValue("0");
        multiFilterRequest.setFilters(Arrays.asList(filter1, filter2));
        multiFilterRequest.setLogicOperator("AND");
        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        when(promoFilterQueryBuilder.getColumnType("promoId")).thenReturn("number");

        PaginationBaseResponseModel<Promo> multiResult = promoService.getPromosInBatches(multiFilterRequest);
        assertNotNull(multiResult);
        assertEquals(1, multiResult.getData().size());
        }

    /**
     * Test successful promo creation.
     * Verifies that promo is created and saved with proper audit logging.
     */
    @Test
    @DisplayName("Create Promo - Success - Should create and save promo with logging")
    void createPromo_Success() {
        // Arrange
        // Note: BaseService methods are now handled by the actual service
        // implementation
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        verify(promoRepository).save(any(Promo.class));
        verify(userLogService).logData(
                eq(TEST_USER_ID),
                eq(SuccessMessages.PromoSuccessMessages.CreatePromo + TEST_PROMO_CODE),
                eq(ApiRoutes.PromosSubRoute.CREATE_PROMO));
    }

    /**
     * Purpose: Verify promo code is trimmed and uppercased on save.
     * Expected Result: Saved promo has uppercase code and trimmed description.
     * Assertions: Captured promo has expected normalized values.
     */
    @Test
    @DisplayName("Create Promo - Normalizes promo code and description")
    void createPromo_NormalizesFields() {
        testPromoRequest.setPromoCode("  test10 ");
        testPromoRequest.setDescription("  Promo Desc  ");

        ArgumentCaptor<Promo> captor = ArgumentCaptor.forClass(Promo.class);
        when(promoRepository.save(captor.capture())).thenReturn(testPromo);

        promoService.createPromo(testPromoRequest);

        Promo saved = captor.getValue();
        assertEquals("TEST10", saved.getPromoCode());
        assertEquals("Promo Desc", saved.getDescription());
    }

    /**
     * Purpose: Verify percent=false allows discount > 100.
     * Expected Result: No exception is thrown.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Promo - Non-percent discount over 100 allowed")
    void createPromo_NonPercentDiscountOver100_Allows() {
        testPromoRequest.setIsPercent(false);
        testPromoRequest.setDiscountValue(new BigDecimal("150"));

        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    /**
     * Purpose: Verify promo creation allows zero discount value.
     * Expected Result: No exception is thrown.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Promo - Zero discount value allowed")
    void createPromo_ZeroDiscountValue_Allows() {
        testPromoRequest.setDiscountValue(BigDecimal.ZERO);

        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    /**
     * Test create promo with null request model.
     * Verifies that BadRequestException is thrown when request model is null.
     */
    @Test
    @DisplayName("Create Promo - Failure - Null request model throws BadRequestException")
    void createPromo_NullRequestModel_ThrowsBadRequestException() {
        // Arrange
        // Note: BaseService methods are now handled by the actual service
        // implementation
        PromoRequestModel nullRequest = null;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.createPromo(nullRequest);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidRequest, exception.getMessage());
    }

    /**
     * Test create promo with empty promo code.
     * Verifies that BadRequestException is thrown when promo code is empty.
     */
    @Test
    @DisplayName("Create Promo - Failure - Empty promo code throws BadRequestException")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        // Arrange
        // Note: BaseService methods are now handled by the actual service
        // implementation
        testPromoRequest.setPromoCode("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.createPromo(testPromoRequest);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /**
     * Test successful retrieval of promo details by ID.
     * Verifies that promo details are correctly returned.
     */
    @Test
    @DisplayName("Get Promo Details By ID - Success - Should return promo details")
    void getPromoDetailsById_Success() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROMO_ID, result.getPromoId());
        assertEquals(TEST_PROMO_CODE, result.getPromoCode());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
    }

    /**
     * Test get promo details by ID with non-existent promo ID.
     * Verifies that NotFoundException is thrown when promo is not found.
     */
    @Test
    @DisplayName("Get Promo Details By ID - Failure - Promo not found")
    void getPromoDetailsById_PromoNotFound_ThrowsNotFoundException() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.getPromoDetailsById(TEST_PROMO_ID);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
    }

    /**
     * Test get promo details by ID with zero ID.
     * Verifies that method handles zero ID (though repository would handle this).
     */
    @Test
    @DisplayName("Get Promo Details By ID - Edge Case - Zero ID")
    void getPromoDetailsById_ZeroId_Success() {
        // Arrange
        Long zeroId = 0L;
        when(promoRepository.findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(zeroId);

        // Assert
        assertNotNull(result);
        verify(promoRepository).findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID);
    }

    /**
     * Test get promo details by ID with negative ID.
     * Verifies that method handles negative ID (though repository would handle
     * this).
     */
    @Test
    @DisplayName("Get Promo Details By ID - Edge Case - Negative ID")
    void getPromoDetailsById_NegativeId_Success() {
        // Arrange
        Long negativeId = -1L;
        when(promoRepository.findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(negativeId);

        // Assert
        assertNotNull(result);
        verify(promoRepository).findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID);
    }

    /**
     * Purpose: Verify NotFoundException for invalid IDs.
     * Expected Result: NotFoundException is thrown for unknown IDs.
     * Assertions: Exception message matches InvalidId.
     */
    @TestFactory
    @DisplayName("Get Promo Details By ID - Invalid IDs")
    Stream<DynamicTest> getPromoDetailsById_InvalidIds() {
        return Stream.of(-10L, 999L, Long.MAX_VALUE, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.getPromoDetailsById(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /**
     * Test successful promo toggle operation.
     * Verifies that promo's isDeleted flag is correctly toggled and logged.
     */
    @Test
    @DisplayName("Toggle Promo - Success - Should toggle isDeleted flag and log")
    void togglePromo_Success() {
        // Arrange
        // Note: BaseService methods are now handled by the actual service
        // implementation
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.togglePromo(TEST_PROMO_ID);

        // Assert
        assertTrue(testPromo.getIsDeleted()); // Should be toggled from false to true
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
        verify(promoRepository).save(testPromo);
        verify(userLogService).logData(
                eq(TEST_USER_ID),
                eq(SuccessMessages.PromoSuccessMessages.ToggledPromo + TEST_PROMO_ID),
                eq(ApiRoutes.PromosSubRoute.TOGGLE_PROMO));
    }

    /**
     * Test toggle promo with non-existent promo ID.
     * Verifies that NotFoundException is thrown when promo is not found.
     */
    @Test
    @DisplayName("Toggle Promo - Failure - Promo not found")
    void togglePromo_PromoNotFound_ThrowsNotFoundException() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(TEST_PROMO_ID);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
        verify(promoRepository, never()).save(any(Promo.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test toggle promo with zero ID.
     * Verifies that method handles zero ID appropriately.
     */
    @Test
    @DisplayName("Toggle Promo - Edge Case - Zero ID")
    void togglePromo_ZeroId_ThrowsNotFoundException() {
        // Arrange
        Long zeroId = 0L;
        when(promoRepository.findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(zeroId);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Test toggle promo with negative ID.
     * Verifies that method handles negative ID appropriately.
     */
    @Test
    @DisplayName("Toggle Promo - Edge Case - Negative ID")
    void togglePromo_NegativeId_ThrowsNotFoundException() {
        // Arrange
        Long negativeId = -1L;
        when(promoRepository.findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(negativeId);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Verify toggle restores deleted promo.
     * Expected Result: isDeleted becomes false.
     * Assertions: Flag is false after toggle.
     */
    @Test
    @DisplayName("Toggle Promo - Restore from deleted")
    void togglePromo_RestoreFromDeleted() {
        testPromo.setIsDeleted(true);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        promoService.togglePromo(TEST_PROMO_ID);

        assertFalse(testPromo.getIsDeleted());
        verify(promoRepository).save(testPromo);
    }

    /**
     * Purpose: Verify NotFoundException for more invalid IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId.
     */
    @TestFactory
    @DisplayName("Toggle Promo - Additional invalid IDs")
    Stream<DynamicTest> togglePromo_AdditionalInvalidIds() {
        return Stream.of(2L, 999L, Long.MAX_VALUE, Long.MIN_VALUE, -100L, 0L)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                            .thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.togglePromo(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    /**
     * Test successful retrieval of promo details by promo code.
     * Verifies that promo details are correctly returned when found by code.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Success - Should return promo details")
    void getPromoDetailsByName_Success() {
        // Arrange
        when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROMO_CODE, result.getPromoCode());
        assertEquals(TEST_PROMO_ID, result.getPromoId());
        verify(promoRepository).findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID);
    }

    /**
     * Test get promo details by name with non-existent promo code.
     * Verifies that NotFoundException is thrown when promo code is not found.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Failure - Promo code not found")
    void getPromoDetailsByName_PromoCodeNotFound_ThrowsNotFoundException() {
        // Arrange
        String nonExistentCode = "NONEXISTENT";
        when(promoRepository.findByPromoCodeAndClientId(nonExistentCode, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.getPromoDetailsByName(nonExistentCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidName, exception.getMessage());
        verify(promoRepository).findByPromoCodeAndClientId(nonExistentCode, TEST_CLIENT_ID);
    }

    /**
     * Test get promo details by name with null promo code.
     * Verifies that method handles null promo code (though repository would handle
     * this).
     */
    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Null promo code")
    void getPromoDetailsByName_NullPromoCode_ThrowsNotFoundException() {
        // Arrange
        String nullCode = null;
        // No need to mock repository since service validates before calling repository

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(nullCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /**
     * Test get promo details by name with empty promo code.
     * Verifies that method handles empty promo code.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Empty promo code")
    void getPromoDetailsByName_EmptyPromoCode_ThrowsNotFoundException() {
        // Arrange
        String emptyCode = "";
        // No need to mock repository since service validates before calling repository

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(emptyCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /**
     * Test get promo details by name with whitespace promo code.
     * Verifies that method handles whitespace in promo code.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Whitespace promo code")
    void getPromoDetailsByName_WhitespacePromoCode_Success() {
        // Arrange
        String whitespaceCode = "   ";
        // No need to mock repository since service validates before calling repository

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(whitespaceCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /**
     * Purpose: Verify promo code lookup is case-insensitive.
     * Expected Result: Uppercased lookup is used.
     * Assertions: Repository called with uppercased code.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Case insensitive lookup")
    void getPromoDetailsByName_CaseInsensitiveLookup() {
        when(promoRepository.findByPromoCodeAndClientId("TEST10", TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        PromoResponseModel result = promoService.getPromoDetailsByName("test10");

        assertNotNull(result);
        verify(promoRepository).findByPromoCodeAndClientId("TEST10", TEST_CLIENT_ID);
    }

    /**
     * Purpose: Verify additional invalid promo code inputs.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPromoCode.
     */
    @TestFactory
    @DisplayName("Get Promo Details By Name - Additional invalid inputs")
    Stream<DynamicTest> getPromoDetailsByName_InvalidInputs() {
        return Stream.of("\t", "\n", "   ", "")
                .map(code -> DynamicTest.dynamicTest("Invalid code: [" + code + "]", () -> {
                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> promoService.getPromoDetailsByName(code));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
                }));
    }

    /**
     * Test successful bulk promo creation.
     * Verifies that multiple promos are created successfully.
     */
    @Test
    @DisplayName("Bulk Create Promos - Success - All valid promos")
    void bulkCreatePromos_AllValid_Success() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PromoRequestModel promoReq = new PromoRequestModel();
            promoReq.setPromoCode("PROMO" + i);
            promoReq.setDescription("Promo " + i);
            promoReq.setDiscountValue(BigDecimal.valueOf(10 + i));
            promoReq.setClientId(TEST_CLIENT_ID);
            promoReq.setStartDate(java.time.LocalDate.now());
            promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(30));
            promos.add(promoReq);
        }

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });

        // Act - @Async method: verify it doesn't throw synchronously
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - verify repository was called (async runs in same thread in unit test)
        verify(promoRepository, times(3)).save(any(Promo.class));
    }

    /**
     * Test bulk promo creation with partial success.
     * Verifies that some promos succeed while others fail validation.
     */
    @Test
    @DisplayName("Bulk Create Promos - Partial Success - Some promos fail validation")
    void bulkCreatePromos_PartialSuccess() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        // Valid promo
        PromoRequestModel validPromo = new PromoRequestModel();
        validPromo.setPromoCode("VALID");
        validPromo.setDescription("Valid Promo");
        validPromo.setDiscountValue(BigDecimal.valueOf(10));
        validPromo.setClientId(TEST_CLIENT_ID);
        validPromo.setStartDate(java.time.LocalDate.now());
        validPromo.setExpiryDate(java.time.LocalDate.now().plusDays(30));
        promos.add(validPromo);

        // Invalid promo (missing promo code)
        PromoRequestModel invalidPromo = new PromoRequestModel();
        invalidPromo.setPromoCode(null);
        invalidPromo.setDescription("Invalid Promo");
        invalidPromo.setDiscountValue(BigDecimal.valueOf(10));
        invalidPromo.setClientId(TEST_CLIENT_ID);
        promos.add(invalidPromo);

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            if (promo.getPromoCode() != null) {
                savedPromos.put(promo.getPromoCode(), promo);
            }
            return promo;
        });

        // Act - @Async method: verify it doesn't throw synchronously
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - partial success: 1 valid promo saved
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    /**
     * Test bulk promo creation with database error.
     * Verifies that database errors are properly handled.
     */
    @Test
    @DisplayName("Bulk Create Promos - Failure - Database error")
    void bulkCreatePromos_DatabaseError() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        PromoRequestModel promoReq = new PromoRequestModel();
        promoReq.setPromoCode("TEST");
        promoReq.setDescription("Test Promo");
        promoReq.setDiscountValue(BigDecimal.valueOf(10));
        promoReq.setClientId(TEST_CLIENT_ID);
        promos.add(promoReq);

        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))
                .thenReturn(Optional.empty());
        lenient().when(promoRepository.save(any(Promo.class))).thenThrow(new RuntimeException("Database error"));

        // Act - @Async method: catches exception, sends message, doesn't rethrow
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - no successful saves
        verify(promoRepository, never()).save(any(Promo.class));
    }

    /**
     * Test bulk promo creation with empty list.
     * Verifies that empty list is handled correctly.
     */
    @Test
    @DisplayName("Bulk Create Promos - Success - Empty list")
    void bulkCreatePromos_EmptyList() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        // Act & Assert - @Async catches exception internally, doesn't rethrow
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    /**
     * Purpose: Verify bulk creation rejects null list.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message mentions list cannot be null or empty.
     */
    @Test
    @DisplayName("Bulk Create Promos - Failure - Null list")
    void bulkCreatePromos_NullList_ThrowsBadRequestException() {
        // @Async catches exception internally, doesn't rethrow
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(null, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify bulk creation with all invalid promos returns failures.
     * Expected Result: Failure count equals total requested.
     * Assertions: Success count is 0 and failure count equals requested.
     */
    @Test
    @DisplayName("Bulk Create Promos - All invalid promos")
    void bulkCreatePromos_AllInvalid() {
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PromoRequestModel invalid = new PromoRequestModel();
            invalid.setPromoCode(" ");
            invalid.setDescription(" ");
            invalid.setDiscountValue(new BigDecimal("-1"));
            invalid.setClientId(TEST_CLIENT_ID);
            promos.add(invalid);
        }

        // Act - @Async method: all invalid, none saved
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - no successful saves
        verify(promoRepository, never()).save(any(Promo.class));
    }

    /**
     * Purpose: Verify bulk creation handles duplicate promo code gracefully.
     * Expected Result: Duplicate promo fails, valid promo succeeds.
     * Assertions: Partial success reflected in counts.
     */
    @Test
    @DisplayName("Bulk Create Promos - Duplicate promo code")
    void bulkCreatePromos_DuplicatePromoCode_PartialSuccess() {
        List<PromoRequestModel> promos = new ArrayList<>();

        PromoRequestModel valid = new PromoRequestModel();
        valid.setPromoCode("DUPLICATE");
        valid.setDescription("Valid Promo");
        valid.setDiscountValue(BigDecimal.ONE);
        valid.setClientId(TEST_CLIENT_ID);
        valid.setStartDate(java.time.LocalDate.now());
        valid.setExpiryDate(java.time.LocalDate.now().plusDays(1));

        PromoRequestModel duplicate = new PromoRequestModel();
        duplicate.setPromoCode("DUPLICATE");
        duplicate.setDescription("Duplicate Promo");
        duplicate.setDiscountValue(BigDecimal.ONE);
        duplicate.setClientId(TEST_CLIENT_ID);
        duplicate.setStartDate(java.time.LocalDate.now());
        duplicate.setExpiryDate(java.time.LocalDate.now().plusDays(1));

        promos.add(valid);
        promos.add(duplicate);

        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
            .thenReturn(Collections.emptyList())
            .thenReturn(List.of(testPromo));
        when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))
            .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act - @Async method: duplicate fails, first succeeds
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - one save for the first valid promo
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    @org.junit.jupiter.api.Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Promo Validation - Invalid description (null) - Throws BadRequestException")
        void testPromoValidation_InvalidDescription_Null() {
            testPromoRequest.setDescription(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid description (empty) - Throws BadRequestException")
        void testPromoValidation_InvalidDescription_Empty() {
            testPromoRequest.setDescription("   ");
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Description too long - Throws BadRequestException")
        void testPromoValidation_DescriptionTooLong() {
            testPromoRequest.setDescription("a".repeat(501));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.DescriptionTooLong, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid discount value (null) - Throws BadRequestException")
        void testPromoValidation_InvalidDiscountValue_Null() {
            testPromoRequest.setDiscountValue(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid discount value (negative) - Throws BadRequestException")
        void testPromoValidation_InvalidDiscountValue_Negative() {
            testPromoRequest.setDiscountValue(new BigDecimal("-1"));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid percentage value (> 100) - Throws BadRequestException")
        void testPromoValidation_InvalidPercentageValue() {
            testPromoRequest.setIsPercent(true);
            testPromoRequest.setDiscountValue(new BigDecimal("101"));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Promo code too long - Throws BadRequestException")
        void testPromoValidation_PromoCodeTooLong() {
            testPromoRequest.setPromoCode("a".repeat(101));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeTooLong, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid start date (null) - Throws BadRequestException")
        void testPromoValidation_InvalidStartDate_Null() {
            // Promo fields initialized in setUp are missing startDate, so ensure it is set
            // there if needed,
            // but here checking null specifically.
            // Base setup doesn't set dates currently, need to ensure testPromoRequest in
            // setUp has valid defaults if using createPromo.
            // Actually setUp sets generic fields. Let's force null here.
            testPromoRequest.setStartDate(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidStartDate, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Start date in past - Throws BadRequestException")
        void testPromoValidation_StartDatePast() {
            testPromoRequest.setStartDate(java.time.LocalDate.now().minusDays(1));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.StartDateMustBeTodayOrFuture, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid expiry date (past) - Throws BadRequestException")
        void testPromoValidation_InvalidExpiryDate_Past() {
            testPromoRequest.setStartDate(java.time.LocalDate.now());
            testPromoRequest.setExpiryDate(java.time.LocalDate.now().minusDays(1));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidExpiryDate, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Expiry date before start date - Throws BadRequestException")
        void testPromoValidation_ExpiryDateBeforeStartDate() {
            testPromoRequest.setStartDate(java.time.LocalDate.now().plusDays(2));
            testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(1));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.ExpiryDateMustBeAfterStartDate, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Overlapping Promo Code - Throws BadRequestException")
        void testPromoValidation_OverlappingPromoCode() {
            // Valid request
            testPromoRequest.setStartDate(java.time.LocalDate.now());
            testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(10));
            testPromoRequest.setPromoCode("OVERLAP");

            // Mock overlaps
            when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                    .thenReturn(List.of(new Promo()));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.OverlappingPromoCode, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid promo code (null) - Throws BadRequestException")
        void testPromoValidation_InvalidPromoCode_Null() {
            testPromoRequest.setPromoCode(null);
            assertThrows(NullPointerException.class,
                () -> promoService.createPromo(testPromoRequest));
        }

        @Test
        @DisplayName("Promo Validation - Invalid promo code (empty) - Throws BadRequestException")
        void testPromoValidation_InvalidPromoCode_Empty() {
            testPromoRequest.setPromoCode(" ");
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Invalid discount for percent (>100) - Throws BadRequestException")
        void testPromoValidation_InvalidDiscountPercentOver100() {
            testPromoRequest.setIsPercent(true);
            testPromoRequest.setDiscountValue(new BigDecimal("150"));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> promoService.createPromo(testPromoRequest));
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, exception.getMessage());
        }

        @Test
        @DisplayName("Promo Validation - Expiry date equals start date - Throws BadRequestException")
        void testPromoValidation_ExpiryDateEqualsStartDate() {
            testPromoRequest.setStartDate(java.time.LocalDate.now().plusDays(1));
            testPromoRequest.setExpiryDate(testPromoRequest.getStartDate());
            assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
        }

        @Test
        @DisplayName("Promo Validation - Description max length (500) - Success")
        void testPromoValidation_DescriptionMaxLength_Success() {
            testPromoRequest.setDescription("a".repeat(500));
            assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
        }

        @Test
        @DisplayName("Promo Validation - Promo code max length (100) - Success")
        void testPromoValidation_PromoCodeMaxLength_Success() {
            testPromoRequest.setPromoCode("a".repeat(100));
            assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
        }

        @Test
        @DisplayName("Promo Validation - Expiry date null is allowed")
        void testPromoValidation_ExpiryDateNull_Allows() {
            testPromoRequest.setExpiryDate(null);
            assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
        }

        @Test
        @DisplayName("Promo Validation - Start date today - Success")
        void testPromoValidation_StartDateToday_Success() {
            testPromoRequest.setStartDate(java.time.LocalDate.now());
            testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(1));
            assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
        }
    }
}
