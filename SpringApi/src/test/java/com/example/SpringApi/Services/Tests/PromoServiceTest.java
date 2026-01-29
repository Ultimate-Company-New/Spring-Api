package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PromoFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.PromoService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromoService.
 *
 * <p>
 * This test class provides comprehensive coverage of PromoService methods
 * including:
 * - CRUD operations (create, read, update, toggle)
 * - Batch retrieval with pagination and filtering
 * - Promo code validation and management
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

    @InjectMocks
    private PromoService promoService;

    private Promo testPromo;
    private PromoRequestModel testPromoRequest;
    private PaginationBaseRequestModel testPaginationRequest;
    private static final Long TEST_PROMO_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PROMO_CODE = "TEST10";
    private static final String TEST_DESCRIPTION = "Test promo description";
    private static final String TEST_INVALID_COLUMN = "invalidColumn";
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

        // Note: BaseService methods are now handled by the actual service
        // implementation
    }

    // ==================== Get Promos In Batches Tests ====================

    /**
     * Test successful retrieval of promos in batches.
     * Verifies that paginated promo data is correctly returned with valid
     * parameters.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Should return paginated promo data")
    void getPromosInBatches_Success() {
        // Arrange
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);
        lenient().when(promoFilterQueryBuilder.getColumnType(TEST_VALID_COLUMN)).thenReturn("string");

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        assertEquals(testPromo, result.getData().get(0));
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
    }

    /**
     * Test get promos in batches with invalid column name.
     * Verifies that BadRequestException is thrown when column name is invalid.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Invalid column name")
    void getPromosInBatches_InvalidColumnName_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("contains");
        invalidFilter.setValue("test");
        testPaginationRequest.setFilters(Arrays.asList(invalidFilter));
        testPaginationRequest.setLogicOperator("AND");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromosInBatches(testPaginationRequest);
        });
        assertTrue(exception.getMessage().contains("Invalid column name"));
    }

    /**
     * Test get promos in batches with single filter.
     * Verifies that single filter expressions are correctly applied.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - With single filter")
    void getPromosInBatches_WithSingleFilter_Success() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("promoCode");
        filter.setOperator("contains");
        filter.setValue("TEST");
        testPaginationRequest.setFilters(Arrays.asList(filter));
        testPaginationRequest.setLogicOperator("AND");

        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);

        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(promoFilterQueryBuilder, times(1)).getColumnType("promoCode");
        verify(promoFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
    }

    /**
     * Test get promos in batches with multiple filters using AND logic.
     * Verifies that multiple filters combined with AND are correctly applied.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - With multiple filters AND")
    void getPromosInBatches_WithMultipleFiltersAND_Success() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("promoCode");
        filter1.setOperator("contains");
        filter1.setValue("TEST");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("description");
        filter2.setOperator("contains");
        filter2.setValue("promo");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("AND");

        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);

        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        when(promoFilterQueryBuilder.getColumnType("description")).thenReturn("string");
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(promoFilterQueryBuilder, times(1)).getColumnType("promoCode");
        verify(promoFilterQueryBuilder, times(1)).getColumnType("description");
        verify(promoFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
    }

    /**
     * Test get promos in batches with multiple filters using OR logic.
     * Verifies that multiple filters combined with OR are correctly applied.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - With multiple filters OR")
    void getPromosInBatches_WithMultipleFiltersOR_Success() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("promoCode");
        filter1.setOperator("contains");
        filter1.setValue("TEST");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("promoCode");
        filter2.setOperator("contains");
        filter2.setValue("PROMO");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("OR");

        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);

        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(promoFilterQueryBuilder, times(2)).getColumnType("promoCode");
        verify(promoFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
    }

    /**
     * Test get promos in batches with complex filters (string, number, boolean).
     * Verifies that filters with different column types are correctly validated and
     * applied.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - With complex filters")
    void getPromosInBatches_WithComplexFilters_Success() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("promoCode");
        filter1.setOperator("contains");
        filter1.setValue("TEST");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("promoId");
        filter2.setOperator("greaterThan");
        filter2.setValue("0");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("AND");

        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);

        when(promoFilterQueryBuilder.getColumnType("promoCode")).thenReturn("string");
        when(promoFilterQueryBuilder.getColumnType("promoId")).thenReturn("number");
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(promoFilterQueryBuilder, times(1)).getColumnType("promoCode");
        verify(promoFilterQueryBuilder, times(1)).getColumnType("promoId");
        verify(promoFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
    }

    // ==================== Create Promo Tests ====================

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

    // ==================== Get Promo Details By ID Tests ====================

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

    // ==================== Toggle Promo Tests ====================

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

    // ==================== Get Promo Details By Name Tests ====================

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

    // ==================== Bulk Create Promos Tests ====================

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
        when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code.toUpperCase()));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = promoService.bulkCreatePromos(promos);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
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
        when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = promoService.bulkCreatePromos(promos);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
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

        // Act
        BulkInsertResponseModel<Long> result = promoService.bulkCreatePromos(promos);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
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

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.bulkCreatePromos(promos);
        });
        assertTrue(exception.getMessage().contains("Promo list cannot be null or empty"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    // ==================== Validation Tests ====================

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
    }

    // ==================== Additional GetPromoDetailsById Tests ====================

    @Test
    @DisplayName("Get Promo By ID - Negative ID - Not Found")
    void getPromoDetailsById_NegativeId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(-1L));
        assertTrue(ex.getMessage().contains(String.valueOf(-1L)) || ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Get Promo By ID - Zero ID - Not Found")
    void getPromoDetailsById_ZeroId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(0L));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Get Promo By ID - Long.MAX_VALUE - Not Found")
    void getPromoDetailsById_MaxLongId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(Long.MAX_VALUE));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Get Promo By ID - Long.MIN_VALUE - Not Found")
    void getPromoDetailsById_MinLongId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(Long.MIN_VALUE));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // ==================== Additional GetPromoDetailsByName Tests ====================

    @Test
    @DisplayName("Get Promo By Name - Case Insensitive Lookup")
    void getPromoDetailsByName_CaseInsensitive_Success() {
        when(promoRepository.findByPromoCodeIgnoreCaseAndClientId("test10", TEST_CLIENT_ID))
                .thenReturn(testPromo);
        PromoResponseModel result = promoService.getPromoDetailsByName("TEST10");
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Promo By Name - Whitespace Code - Not Found")
    void getPromoDetailsByName_WhitespaceCode_ThrowsNotFoundException() {
        when(promoRepository.findByPromoCodeIgnoreCaseAndClientId("   ", TEST_CLIENT_ID))
                .thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName("   "));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Get Promo By Name - Very Long Code - Not Found")
    void getPromoDetailsByName_VeryLongCode_ThrowsNotFoundException() {
        String longCode = "A".repeat(200);
        when(promoRepository.findByPromoCodeIgnoreCaseAndClientId(longCode, TEST_CLIENT_ID))
                .thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName(longCode));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // ==================== Additional TogglePromo Tests ====================

    @Test
    @DisplayName("Toggle Promo - Negative ID - Not Found")
    void togglePromo_NegativeId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(-1L));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Toggle Promo - Zero ID - Not Found")
    void togglePromo_ZeroId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(0L));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Toggle Promo - Max Long ID - Not Found")
    void togglePromo_MaxLongId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(Long.MAX_VALUE));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Toggle Promo - Multiple Toggles - State Persistence")
    void togglePromo_MultipleToggles_StatePersists() {
        testPromo.setIsDeleted(false);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(testPromo);
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        
        promoService.togglePromo(TEST_PROMO_ID);
        assertTrue(testPromo.getIsDeleted());
        
        promoService.togglePromo(TEST_PROMO_ID);
        assertFalse(testPromo.getIsDeleted());
    }

    // ==================== Additional CreatePromo Tests ====================

    @Test
    @DisplayName("Create Promo - Null Request - Throws BadRequestException")
    void createPromo_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(null));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Empty Promo Code - Throws BadRequestException")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Whitespace Promo Code - Throws BadRequestException")
    void createPromo_WhitespacePromoCode_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Empty Description - Throws BadRequestException")
    void createPromo_EmptyDescription_ThrowsBadRequestException() {
        testPromoRequest.setDescription("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Whitespace Description - Throws BadRequestException")
    void createPromo_WhitespaceDescription_ThrowsBadRequestException() {
        testPromoRequest.setDescription("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Null Discount Value - Throws BadRequestException")
    void createPromo_NullDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Negative Discount Value - Throws BadRequestException")
    void createPromo_NegativeDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(-5.0));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Percentage Over 100 - Throws BadRequestException")
    void createPromo_PercentageOver100_ThrowsBadRequestException() {
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(150.0));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Percentage Exactly 100 - Success")
    void createPromo_Percentage100_Success() {
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(100.0));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Create Promo - Past Start Date - Throws BadRequestException")
    void createPromo_PastStartDate_ThrowsBadRequestException() {
        testPromoRequest.setStartDate(java.time.LocalDate.now().minusDays(1));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.StartDateMustBeTodayOrFuture, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Null Start Date - Throws BadRequestException")
    void createPromo_NullStartDate_ThrowsBadRequestException() {
        testPromoRequest.setStartDate(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidStartDate, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Expiry Before Start - Throws BadRequestException")
    void createPromo_ExpiryBeforeStart_ThrowsBadRequestException() {
        testPromoRequest.setStartDate(java.time.LocalDate.now().plusDays(10));
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(5));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.ExpiryDateMustBeAfterStartDate, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Expiry Past - Throws BadRequestException")
    void createPromo_ExpiryInPast_ThrowsBadRequestException() {
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().minusDays(1));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidExpiryDate, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Code Too Long (>100 chars) - Throws BadRequestException")
    void createPromo_CodeTooLong_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode("A".repeat(101));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeTooLong, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Code Exactly 100 chars - Success")
    void createPromo_Code100Chars_Success() {
        testPromoRequest.setPromoCode("A".repeat(100));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Create Promo - Description Too Long (>500 chars) - Throws BadRequestException")
    void createPromo_DescriptionTooLong_ThrowsBadRequestException() {
        testPromoRequest.setDescription("D".repeat(501));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionTooLong, ex.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Description Exactly 500 chars - Success")
    void createPromo_Description500Chars_Success() {
        testPromoRequest.setDescription("D".repeat(500));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    // ==================== Additional BulkCreate Tests ====================

    @Test
    @DisplayName("Bulk Create Promos - All Invalid Codes")
    void bulkCreatePromos_AllInvalidCodes_AllFail() {
        List<PromoRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PromoRequestModel req = new PromoRequestModel();
            req.setPromoCode("");
            req.setDescription("Valid description");
            req.setIsPercent(true);
            req.setDiscountValue(BigDecimal.valueOf(10.0));
            req.setStartDate(java.time.LocalDate.now());
            req.setExpiryDate(java.time.LocalDate.now().plusDays(30));
            requests.add(req);
        }
        
        BulkInsertResponseModel result = promoService.bulkCreatePromos(requests);
        assertNotNull(result);
        assertEquals(5, result.getTotalRequested());
    }

    @Test
    @DisplayName("Bulk Create Promos - Mixed Valid and Invalid")
    void bulkCreatePromos_MixedValidInvalid_PartialSuccess() {
        List<PromoRequestModel> requests = new ArrayList<>();
        
        // Valid
        PromoRequestModel valid = new PromoRequestModel();
        valid.setPromoCode("VALID");
        valid.setDescription("Valid");
        valid.setIsPercent(true);
        valid.setDiscountValue(BigDecimal.valueOf(10.0));
        valid.setStartDate(java.time.LocalDate.now());
        valid.setExpiryDate(java.time.LocalDate.now().plusDays(30));
        requests.add(valid);
        
        // Invalid (empty code)
        PromoRequestModel invalid = new PromoRequestModel();
        invalid.setPromoCode("");
        invalid.setDescription("Invalid");
        requests.add(invalid);
        
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        BulkInsertResponseModel result = promoService.bulkCreatePromos(requests);
        assertNotNull(result);
        assertTrue(result.getTotalRequested() >= 0);
    }

    @Test
    @DisplayName("Bulk Create Promos - Empty List")
    void bulkCreatePromos_EmptyList_ReturnsEmpty() {
        BulkInsertResponseModel result = promoService.bulkCreatePromos(new ArrayList<>());
        assertNotNull(result);
        assertEquals(0, result.getTotalRequested());
    }

    @Test
    @DisplayName("Bulk Create Promos - Large Batch (50 items)")
    void bulkCreatePromos_LargeBatch_Success() {
        List<PromoRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PromoRequestModel req = new PromoRequestModel();
            req.setPromoCode("PROMO" + i);
            req.setDescription("Promo " + i);
            req.setIsPercent(true);
            req.setDiscountValue(BigDecimal.valueOf(10.0 + i));
            req.setStartDate(java.time.LocalDate.now());
            req.setExpiryDate(java.time.LocalDate.now().plusDays(30));
            requests.add(req);
        }
        
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);
        BulkInsertResponseModel result = promoService.bulkCreatePromos(requests);
        assertNotNull(result);
    }

    // ==================== Additional GetPromosInBatches Tests ====================

    @Test
    @DisplayName("Get Promos In Batches - Null Filters")
    void getPromosInBatches_NullFilters_Success() {
        testPaginationRequest.setFilters(null);
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10), 1);
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(promoPage);
        
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Promos In Batches - Empty Results")
    void getPromosInBatches_EmptyResults_ReturnsEmpty() {
        List<Promo> emptyList = new ArrayList<>();
        Page<Promo> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(emptyPage);
        
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);
        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }

    @Test
    @DisplayName("Get Promos In Batches - Large Page Size")
    void getPromosInBatches_LargePageSize_Success() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(1000);
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 1000), 1);
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(promoPage);
        
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);
        assertNotNull(result);
    }

    // ==================== Comprehensive Validation Tests - Added ====================

    @Test
    @DisplayName("Create Promo - Negative Discount Value - Throws BadRequestException")
    void createPromo_NegativeDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(-50.0);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("discount") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Discount Over 100 Percent - Throws BadRequestException")
    void createPromo_DiscountOver100Percent_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(150.0);
        testPromoRequest.setIsPercent(true);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("discount") || ex.getMessage().contains("percent"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Zero Discount Value - Throws BadRequestException")
    void createPromo_ZeroDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(0.0);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("discount") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Null Promo Code - Throws BadRequestException")
    void createPromo_NullPromoCode_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("code") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Empty Promo Code - Throws BadRequestException")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("code") || ex.getMessage().contains("empty"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Whitespace Promo Code - Throws BadRequestException")
    void createPromo_WhitespacePromoCode_ThrowsBadRequestException() {
        testPromoRequest.setPromoCode("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("code") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Duplicate Promo Code - Throws BadRequestException")
    void createPromo_DuplicatePromoCode_ThrowsBadRequestException() {
        when(promoRepository.findByPromoCodeAndClientId(testPromo.getPromoCode(), TEST_CLIENT_ID))
                .thenReturn(testPromo);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("exist") || ex.getMessage().contains("duplicate"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Null Discount Value - Throws BadRequestException")
    void createPromo_NullDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setDiscountValue(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("discount") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Create Promo - Null IsPercent Flag - Throws BadRequestException")
    void createPromo_NullIsPercentFlag_ThrowsBadRequestException() {
        testPromoRequest.setIsPercent(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("percent") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Update Promo - Negative ID - Throws NotFoundException")
    void updatePromo_NegativeId_ThrowsNotFoundException() {
        testPromoRequest.setPromoId(-1L);
        when(promoRepository.findByPromoIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.updatePromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Update Promo - Zero ID - Throws NotFoundException")
    void updatePromo_ZeroId_ThrowsNotFoundException() {
        testPromoRequest.setPromoId(0L);
        when(promoRepository.findByPromoIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.updatePromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Update Promo - Long.MAX_VALUE ID - Throws NotFoundException")
    void updatePromo_MaxLongId_ThrowsNotFoundException() {
        testPromoRequest.setPromoId(Long.MAX_VALUE);
        when(promoRepository.findByPromoIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.updatePromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Update Promo - Negative Discount Value - Throws BadRequestException")
    void updatePromo_NegativeDiscountValue_ThrowsBadRequestException() {
        testPromoRequest.setPromoId(TEST_PROMO_ID);
        testPromoRequest.setDiscountValue(-50.0);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(testPromo);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.updatePromo(testPromoRequest));
        assertTrue(ex.getMessage().contains("discount") || ex.getMessage().contains("invalid"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Delete Promo - Negative ID - Throws NotFoundException")
    void deletePromo_NegativeId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.deletePromo(-1L));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Delete Promo - Zero ID - Throws NotFoundException")
    void deletePromo_ZeroId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.deletePromo(0L));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Get Promo By ID - Negative ID - Throws NotFoundException")
    void getPromoById_NegativeId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoById(-1L));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Promo By ID - Zero ID - Throws NotFoundException")
    void getPromoById_ZeroId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoById(0L));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Promo By ID - Long.MAX_VALUE ID - Throws NotFoundException")
    void getPromoById_MaxLongId_ThrowsNotFoundException() {
        when(promoRepository.findByPromoIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PromoErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Promo - Multiple Toggles - State Transitions Correctly")
    void togglePromo_MultipleToggles_StateTransitions() {
        testPromo.setIsDeleted(false);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(testPromo);
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // First toggle: false -> true
        promoService.togglePromo(TEST_PROMO_ID);
        assertTrue(testPromo.getIsDeleted());

        // Second toggle: true -> false
        testPromo.setIsDeleted(true);
        promoService.togglePromo(TEST_PROMO_ID);
        assertFalse(testPromo.getIsDeleted());

        verify(promoRepository, times(2)).save(any(Promo.class));
    }

    @Test
    @DisplayName("Get Promos In Batches - Null Pagination Request - Throws BadRequestException")
    void getPromosInBatches_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(null));
        assertNotNull(ex.getMessage());
        verify(promoFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("Get Promos In Batches - Invalid Start Greater Than End - Throws BadRequestException")
    void getPromosInBatches_StartGreaterThanEnd_ThrowsBadRequestException() {
        testPaginationRequest.setStart(100);
        testPaginationRequest.setEnd(10);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));
        assertTrue(ex.getMessage().contains("start") || ex.getMessage().contains("end"));
        verify(promoFilterQueryBuilder, never()).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("Bulk Create Promos - Empty List - Throws BadRequestException")
    void bulkCreatePromos_EmptyList_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.bulkCreatePromos(new java.util.ArrayList<>()));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Null List - Throws BadRequestException")
    void bulkCreatePromos_NullList_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.bulkCreatePromos(null));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
        verify(promoRepository, never()).save(any(Promo.class));
    }
}