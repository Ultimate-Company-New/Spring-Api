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
 * @version 2.0
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
    private static final String TEST_PROMO_CODE = "TEST10";
    private static final String TEST_DESCRIPTION = "Test promo description";
    private static final String TEST_VALID_COLUMN = "promoCode";

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
        testPromoRequest.setClientId(DEFAULT_CLIENT_ID);
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(30));

        // Initialize test promo using constructor
        testPromo = new Promo(testPromoRequest, DEFAULT_CREATED_USER, DEFAULT_CLIENT_ID);
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

        // Mock Authorization header
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock BaseService behavior
        lenient().doReturn(DEFAULT_CLIENT_ID).when(promoService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(promoService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(promoService).getUser();
    }

    // ==================== Get Promos In Batches Tests ====================

    @Nested
    @DisplayName("getPromosInBatches Tests")
    class GetPromosInBatchesTests {

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
         * Triple Loop Test for Filter Validation.
         * Iterates through combinations of Columns, Operators, and Values.
         */
        @Test
        @DisplayName("Get Promos In Batches - Filter Logic Triple Loop Validation")
        void getPromosInBatches_TripleLoopValidation() {
            // 1. Columns
            String[] validColumns = {
                    "promoId", "promoCode", "description", "discountValue", "isPercent",
                    "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes"
            };
            String[] invalidColumns = { "invalidCol", "DROP TABLE", "unknown" };

            // 2. Operators
            String[] validOperators = {
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
            };
            String[] invalidOperators = { "invalidOp", "like" };

            // 3. Values
            String[] values = { "val", "" };

            // Mock response
            Page<Promo> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Mock column types (simplified)
            lenient().when(promoFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            for (String column : joinArrays(validColumns, invalidColumns)) {
                for (String operator : joinArrays(validOperators, invalidOperators)) {
                    for (String value : values) {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(List.of(filter));

                        boolean isValidColumn = Arrays.asList(validColumns).contains(column);
                        boolean isValidOperator = Arrays.asList(validOperators).contains(operator);

                        if (isValidColumn && isValidOperator) {
                            assertDoesNotThrow(() -> promoService.getPromosInBatches(req),
                                    "Failed for valid column/operator: " + column + "/" + operator);
                        } else {
                            BadRequestException ex = assertThrows(BadRequestException.class,
                                    () -> promoService.getPromosInBatches(req),
                                    "Expected BadRequest for invalid input: " + column + "/" + operator);
                            assertTrue(ex.getMessage().contains("Invalid"));
                        }
                    }
                }
            }
        }
    }

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

    // ==================== Create Promo Tests ====================

    @Nested
    @DisplayName("createPromo Tests")
    class CreatePromoTests {

        @Test
        @DisplayName("Create Promo - Success - Should create and save promo with logging")
        void createPromo_Success() {
            // Arrange
            when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

            // Act
            promoService.createPromo(testPromoRequest);

            // Assert
            verify(promoRepository).save(any(Promo.class));
            verify(userLogService).logData(
                    eq(DEFAULT_USER_ID),
                    eq(SuccessMessages.PromoSuccessMessages.CreatePromo + TEST_PROMO_CODE),
                    eq(ApiRoutes.PromosSubRoute.CREATE_PROMO));
        }

        @Test
        @DisplayName("Create Promo - Failure - Null request model throws BadRequestException")
        void createPromo_NullRequestModel_ThrowsBadRequestException() {
            // Arrange
            PromoRequestModel nullRequest = null;

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                promoService.createPromo(nullRequest);
            });
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidRequest, exception.getMessage());
        }

        @Test
        @DisplayName("Create Promo - Failure - Empty promo code throws BadRequestException")
        void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
            // Arrange
            testPromoRequest.setPromoCode("");

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                promoService.createPromo(testPromoRequest);
            });
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
        }
    }

    // ==================== Get Promo Details By ID Tests ====================

    @Nested
    @DisplayName("getPromoDetailsById Tests")
    class GetPromoDetailsByIdTests {

        @Test
        @DisplayName("Get Promo Details By ID - Success - Should return promo details")
        void getPromoDetailsById_Success() {
            // Arrange
            when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPromo));

            // Act
            PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PROMO_ID, result.getPromoId());
            assertEquals(TEST_PROMO_CODE, result.getPromoCode());
            verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID);
        }

        @Test
        @DisplayName("Get Promo Details By ID - Failure - Promo not found")
        void getPromoDetailsById_PromoNotFound_ThrowsNotFoundException() {
            // Arrange
            when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                promoService.getPromoDetailsById(TEST_PROMO_ID);
            });
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
            verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID);
        }
    }

    // ==================== Toggle Promo Tests ====================

    @Nested
    @DisplayName("togglePromo Tests")
    class TogglePromoTests {

        @Test
        @DisplayName("Toggle Promo - Success - Should toggle isDeleted flag and log")
        void togglePromo_Success() {
            // Arrange
            when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPromo));
            when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

            // Act
            promoService.togglePromo(TEST_PROMO_ID);

            // Assert
            assertTrue(testPromo.getIsDeleted()); // Should be toggled from false to true
            verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID);
            verify(promoRepository).save(testPromo);
            verify(userLogService).logData(
                    eq(DEFAULT_USER_ID),
                    eq(SuccessMessages.PromoSuccessMessages.ToggledPromo + TEST_PROMO_ID),
                    eq(ApiRoutes.PromosSubRoute.TOGGLE_PROMO));
        }

        @Test
        @DisplayName("Toggle Promo - Failure - Promo not found")
        void togglePromo_PromoNotFound_ThrowsNotFoundException() {
            // Arrange
            when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                promoService.togglePromo(TEST_PROMO_ID);
            });
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
            verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, DEFAULT_CLIENT_ID);
            verify(promoRepository, never()).save(any(Promo.class));
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }
    }

    // ==================== Get Promo Details By Name Tests ====================

    @Nested
    @DisplayName("getPromoDetailsByName Tests")
    class GetPromoDetailsByNameTests {

        @Test
        @DisplayName("Get Promo Details By Name - Success - Should return promo details")
        void getPromoDetailsByName_Success() {
            // Arrange
            when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPromo));

            // Act
            PromoResponseModel result = promoService.getPromoDetailsByName(TEST_PROMO_CODE);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PROMO_CODE, result.getPromoCode());
            assertEquals(TEST_PROMO_ID, result.getPromoId());
            verify(promoRepository).findByPromoCodeAndClientId(TEST_PROMO_CODE, DEFAULT_CLIENT_ID);
        }

        @Test
        @DisplayName("Get Promo Details By Name - Failure - Promo code not found")
        void getPromoDetailsByName_PromoCodeNotFound_ThrowsNotFoundException() {
            // Arrange
            String nonExistentCode = "NONEXISTENT";
            when(promoRepository.findByPromoCodeAndClientId(nonExistentCode, DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                promoService.getPromoDetailsByName(nonExistentCode);
            });
            assertEquals(ErrorMessages.PromoErrorMessages.InvalidName, exception.getMessage());
            verify(promoRepository).findByPromoCodeAndClientId(nonExistentCode, DEFAULT_CLIENT_ID);
        }
    }

    // ==================== Bulk Create Promos Tests ====================

    @Nested
    @DisplayName("bulkCreatePromos Tests")
    class BulkCreatePromosTests {

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
                promoReq.setClientId(DEFAULT_CLIENT_ID);
                promoReq.setStartDate(java.time.LocalDate.now());
                promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(30));
                promos.add(promoReq);
            }

            Map<String, Promo> savedPromos = new HashMap<>();
            when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(DEFAULT_CLIENT_ID))).thenAnswer(invocation -> {
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
    }
}
