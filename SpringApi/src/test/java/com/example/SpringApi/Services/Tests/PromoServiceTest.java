package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
 * <p>This test class provides comprehensive coverage of PromoService methods including:
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
class PromoServiceTest {

    @Mock
    private PromoRepository promoRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private PromoService promoService;

    private Promo testPromo;
    private PromoRequestModel testPromoRequest;
    private PaginationBaseRequestModel testPaginationRequest;
    private static final Long TEST_PROMO_ID = 1L;
    private static final Long TEST_CLIENT_ID = 100L;
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

        // Initialize test promo using constructor
        testPromo = new Promo(testPromoRequest, CREATED_USER, TEST_CLIENT_ID);
        testPromo.setPromoId(TEST_PROMO_ID); // Set ID manually since constructor doesn't set it
        testPromo.setCreatedAt(LocalDateTime.now());
        testPromo.setUpdatedAt(LocalDateTime.now());

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setColumnName(TEST_VALID_COLUMN);
        testPaginationRequest.setCondition("equals");
        testPaginationRequest.setFilterExpr(TEST_PROMO_CODE);
        testPaginationRequest.setIncludeDeleted(false);
        
        // Mock getClientId() to return TEST_CLIENT_ID for multi-tenant filtering
        lenient().doReturn(TEST_CLIENT_ID).when(promoService).getClientId();
        lenient().doReturn(TEST_USER_ID).when(promoService).getUserId();
    }

    // ==================== Get Promos In Batches Tests ====================

    /**
     * Test successful retrieval of promos in batches.
     * Verifies that paginated promo data is correctly returned with valid parameters.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Should return paginated promo data")
    void getPromosInBatches_Success() {
        // Arrange
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);
        when(promoRepository.findPaginatedPromos(
            eq(TEST_CLIENT_ID), eq(TEST_VALID_COLUMN), eq("equals"), eq(TEST_PROMO_CODE), eq(false),
            any(PageRequest.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        assertEquals(testPromo, result.getData().get(0));
        verify(promoRepository).findPaginatedPromos(
            eq(TEST_CLIENT_ID), eq(TEST_VALID_COLUMN), eq("equals"), eq(TEST_PROMO_CODE), eq(false),
            any(PageRequest.class));
    }

    /**
     * Test get promos in batches with invalid column name.
     * Verifies that BadRequestException is thrown when column name is invalid.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Invalid column name")
    void getPromosInBatches_InvalidColumnName_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setColumnName(TEST_INVALID_COLUMN);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromosInBatches(testPaginationRequest);
        });
        assertTrue(exception.getMessage().contains("Invalid column name"));
    }

    /**
     * Test get promos in batches with null column name.
     * Verifies that method handles null column name gracefully.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Null column name handled")
    void getPromosInBatches_NullColumnName_Success() {
        // Arrange
        testPaginationRequest.setColumnName(null);
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);
        when(promoRepository.findPaginatedPromos(
            eq(TEST_CLIENT_ID), eq(null), eq("equals"), eq(TEST_PROMO_CODE), eq(false),
            any(PageRequest.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(promoRepository).findPaginatedPromos(
            eq(TEST_CLIENT_ID), eq(null), eq("equals"), eq(TEST_PROMO_CODE), eq(false),
            any(PageRequest.class));
    }

    /**
     * Test get promos in batches with empty result set.
     * Verifies that empty pagination response is returned when no promos found.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Empty result set")
    void getPromosInBatches_EmptyResult_Success() {
        // Arrange
        List<Promo> emptyList = new ArrayList<>();
        Page<Promo> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 0);
        when(promoRepository.findPaginatedPromos(
            eq(TEST_CLIENT_ID), eq(TEST_VALID_COLUMN), eq("equals"), eq(TEST_PROMO_CODE), eq(false),
            any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getData().size());
        assertEquals(0, result.getTotalDataCount());
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
        lenient().doReturn(CREATED_USER).when(promoService).getUser();
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
        lenient().doReturn(CREATED_USER).when(promoService).getUser();
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
        lenient().doReturn(CREATED_USER).when(promoService).getUser();
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
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

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
     * Verifies that method handles negative ID (though repository would handle this).
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
        lenient().doReturn(CREATED_USER).when(promoService).getUser();
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));
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
        when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

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
     * Verifies that method handles null promo code (though repository would handle this).
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
}