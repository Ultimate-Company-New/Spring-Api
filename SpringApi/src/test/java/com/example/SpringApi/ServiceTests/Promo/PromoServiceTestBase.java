package com.example.SpringApi.ServiceTests.Promo;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.FilterQueryBuilder.PromoFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.PromoService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base test class for PromoService tests. Contains common mocks, dependencies, and setup logic
 * shared across all PromoService test classes.
 */
@ExtendWith(MockitoExtension.class)
abstract class PromoServiceTestBase {

  @Mock protected PromoRepository promoRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected PromoFilterQueryBuilder promoFilterQueryBuilder;

  @Mock protected HttpServletRequest request;

  @Mock protected MessageService messageService;

  @Spy @InjectMocks protected PromoService promoService;

  protected PromoController promoController;

  protected Promo testPromo;
  protected PromoRequestModel testPromoRequest;
  protected PaginationBaseRequestModel testPaginationRequest;

  protected static final Long TEST_PROMO_ID = 1L;
  protected static final Long TEST_CLIENT_ID = 1L;
  protected static final Long TEST_USER_ID = 1L;
  protected static final String TEST_PROMO_CODE = "TEST10";
  protected static final String TEST_DESCRIPTION = "Test promo description";
  protected static final String TEST_VALID_COLUMN = "promoCode";
  protected static final String CREATED_USER = "admin";
  protected static final String TEST_LOGIN_NAME = "admin";

  @BeforeEach
  void setUp() {
    // Initialize test data
    initializeTestData();

    // Infrastructure stubs (lenient) - used only for base service functionality
    stubSecurityContext();
    stubInfrastructureServices();

    // Initialize controller with spied service
    promoController = new PromoController(promoService);
  }

  /** Initializes common test data objects. */
  private void initializeTestData() {
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
    testPromo.setPromoId(TEST_PROMO_ID);
    testPromo.setCreatedAt(LocalDateTime.now());
    testPromo.setUpdatedAt(LocalDateTime.now());

    // Initialize test pagination request
    testPaginationRequest = new PaginationBaseRequestModel();
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);

    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn(TEST_VALID_COLUMN);
    filter.setOperator("equals");
    filter.setValue(TEST_PROMO_CODE);
    testPaginationRequest.setFilters(List.of(filter));
    testPaginationRequest.setLogicOperator("AND");
    testPaginationRequest.setIncludeDeleted(false);
  }

  /** Stubs security context methods on the spy service. */
  protected void stubSecurityContext() {
    lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    lenient().doReturn(TEST_USER_ID).when(promoService).getUserId();
    lenient().doReturn(TEST_LOGIN_NAME).when(promoService).getUser();
    lenient().doReturn(TEST_CLIENT_ID).when(promoService).getClientId();
  }

  /** Stubs infrastructure services like MessageService and UserLogService. */
  protected void stubInfrastructureServices() {
    lenient()
        .doNothing()
        .when(messageService)
        .createMessageWithContext(any(), anyLong(), anyString(), anyLong());
    lenient()
        .when(
            userLogService.logDataWithContext(
                anyLong(), anyString(), anyLong(), anyString(), anyString()))
        .thenReturn(true);
    lenient().when(promoFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
    lenient().when(promoFilterQueryBuilder.getColumnType("promoId")).thenReturn("number");
    lenient().when(promoFilterQueryBuilder.getColumnType("discountValue")).thenReturn("number");
    lenient().when(promoFilterQueryBuilder.getColumnType("isPercent")).thenReturn("boolean");
    lenient().when(promoFilterQueryBuilder.getColumnType("isDeleted")).thenReturn("boolean");
    lenient().when(promoFilterQueryBuilder.getColumnType("startDate")).thenReturn("date");
    lenient().when(promoFilterQueryBuilder.getColumnType("expiryDate")).thenReturn("date");
    lenient().when(promoFilterQueryBuilder.getColumnType("createdAt")).thenReturn("date");
    lenient().when(promoFilterQueryBuilder.getColumnType("updatedAt")).thenReturn("date");
  }

  /** Stubs the service to throw UnauthorizedException for controller permission tests. */
  protected void stubServiceThrowsUnauthorizedException() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .createPromo(any());
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .togglePromo(anyLong());
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .getPromoDetailsById(anyLong());
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .getPromoDetailsByName(anyString());
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .getPromosInBatches(any());
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(promoService)
        .bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());
  }

  // ==========================================
  // ADDITIONAL STUBS
  // ==========================================

  protected void stubPromoRepositoryFindOverlappingPromos(List<Promo> result) {
    lenient()
        .when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
        .thenReturn(result);
  }

  protected void stubPromoRepositorySave(Promo result) {
    lenient().when(promoRepository.save(any(Promo.class))).thenReturn(result);
  }

  protected void stubPromoRepositoryFindByPromoIdAndClientId(java.util.Optional<Promo> result) {
    lenient()
        .when(promoRepository.findByPromoIdAndClientId(anyLong(), anyLong()))
        .thenReturn(result);
  }

  protected void stubPromoRepositoryFindByPromoIdAndClientId(
      Long promoId, Long clientId, java.util.Optional<Promo> result) {
    lenient().when(promoRepository.findByPromoIdAndClientId(promoId, clientId)).thenReturn(result);
  }

  protected void stubPromoRepositoryFindByPromoCodeAndClientId(
      String code, Long clientId, java.util.Optional<Promo> result) {
    lenient().when(promoRepository.findByPromoCodeAndClientId(code, clientId)).thenReturn(result);
  }

  protected void stubPromoRepositoryFindByPromoCodeAndClientIdAny(
      java.util.Optional<Promo> result) {
    lenient()
        .when(promoRepository.findByPromoCodeAndClientId(anyString(), anyLong()))
        .thenReturn(result);
  }

  protected void stubPromoRepositoryFindByPromoCodeAndClientIdFromMap(
      java.util.Map<String, Promo> savedPromos) {
    lenient()
        .when(promoRepository.findByPromoCodeAndClientId(anyString(), anyLong()))
        .thenAnswer(
            invocation -> {
              String code = invocation.getArgument(0);
              return java.util.Optional.ofNullable(
                  savedPromos.get(code != null ? code.toUpperCase() : null));
            });
  }

  protected void stubPromoRepositorySaveAssigningId(
      java.util.concurrent.atomic.AtomicLong counter, java.util.Map<String, Promo> savedPromos) {
    lenient()
        .when(promoRepository.save(any(Promo.class)))
        .thenAnswer(
            invocation -> {
              Promo promo = invocation.getArgument(0);
              promo.setPromoId(counter.incrementAndGet());
              savedPromos.put(promo.getPromoCode(), promo);
              return promo;
            });
  }

  protected void stubPromoRepositorySaveReturnsArgument() {
    lenient()
        .when(promoRepository.save(any(Promo.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubPromoRepositorySaveThrows(RuntimeException exception) {
    lenient().when(promoRepository.save(any(Promo.class))).thenThrow(exception);
  }

  protected void stubPromoRepositoryFindOverlappingPromosForCode(
      String code, java.util.List<Promo> result) {
    lenient()
        .when(promoRepository.findOverlappingPromos(eq(code), anyLong(), any(), any()))
        .thenReturn(result);
  }

  protected void stubPromoRepositoryFindOverlappingPromosSequence(
      java.util.List<Promo> first, java.util.List<Promo> second) {
    lenient()
        .when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
        .thenReturn(first, second);
  }

  protected void stubPromoFilterQueryBuilderFindPaginatedEntities(
      org.springframework.data.domain.Page<Promo> result) {
    lenient()
        .when(
            promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(),
                any(),
                anyString(),
                any(),
                anyBoolean(),
                any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(result);
  }

  protected void stubServiceCreatePromoDoNothing() {
    doNothing().when(promoService).createPromo(any(PromoRequestModel.class));
  }

  protected void stubServiceTogglePromoDoNothing() {
    doNothing().when(promoService).togglePromo(anyLong());
  }

  protected void stubServiceGetPromoDetailsByIdReturns(
      com.example.SpringApi.Models.ResponseModels.PromoResponseModel result) {
    doReturn(result).when(promoService).getPromoDetailsById(anyLong());
  }

  protected void stubServiceGetPromoDetailsByNameReturns(
      com.example.SpringApi.Models.ResponseModels.PromoResponseModel result) {
    doReturn(result).when(promoService).getPromoDetailsByName(anyString());
  }

  protected void stubServiceGetPromosInBatchesReturns(
      com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<?> result) {
    doReturn(result).when(promoService).getPromosInBatches(any(PaginationBaseRequestModel.class));
  }

  protected void stubServiceCreatePromoThrowsBadRequest(String message) {
    doThrow(new com.example.SpringApi.Exceptions.BadRequestException(message))
        .when(promoService)
        .createPromo(any(PromoRequestModel.class));
  }

  protected void stubServiceCreatePromoThrowsRuntime(String message) {
    doThrow(new RuntimeException(message))
        .when(promoService)
        .createPromo(any(PromoRequestModel.class));
  }

  protected void stubServiceTogglePromoThrowsNotFound(String message) {
    doThrow(new com.example.SpringApi.Exceptions.NotFoundException(message))
        .when(promoService)
        .togglePromo(anyLong());
  }

  protected void stubServiceTogglePromoThrowsRuntime(String message) {
    doThrow(new RuntimeException(message)).when(promoService).togglePromo(anyLong());
  }

  protected void stubServiceGetPromoDetailsByIdThrowsNotFound(String message) {
    doThrow(new com.example.SpringApi.Exceptions.NotFoundException(message))
        .when(promoService)
        .getPromoDetailsById(anyLong());
  }

  protected void stubServiceGetPromoDetailsByIdThrowsBadRequest(String message) {
    doThrow(new com.example.SpringApi.Exceptions.BadRequestException(message))
        .when(promoService)
        .getPromoDetailsById(anyLong());
  }

  protected void stubServiceGetPromoDetailsByIdThrowsRuntime(String message) {
    doThrow(new RuntimeException(message)).when(promoService).getPromoDetailsById(anyLong());
  }

  protected void stubServiceGetPromoDetailsByNameThrowsNotFound(String message) {
    doThrow(new com.example.SpringApi.Exceptions.NotFoundException(message))
        .when(promoService)
        .getPromoDetailsByName(anyString());
  }

  protected void stubServiceGetPromoDetailsByNameThrowsBadRequest(String message) {
    doThrow(new com.example.SpringApi.Exceptions.BadRequestException(message))
        .when(promoService)
        .getPromoDetailsByName(anyString());
  }

  protected void stubServiceGetPromoDetailsByNameThrowsUnauthorized(String message) {
    doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(message))
        .when(promoService)
        .getClientId();
  }

  protected void stubServiceGetPromoDetailsByNameThrowsRuntime(String message) {
    doThrow(new RuntimeException(message)).when(promoService).getPromoDetailsByName(anyString());
  }

  protected void stubServiceGetPromosInBatchesThrowsBadRequest(String message) {
    doThrow(new com.example.SpringApi.Exceptions.BadRequestException(message))
        .when(promoService)
        .getPromosInBatches(any(PaginationBaseRequestModel.class));
  }

  protected void stubServiceGetPromosInBatchesThrowsRuntime(String message) {
    doThrow(new RuntimeException(message))
        .when(promoService)
        .getPromosInBatches(any(PaginationBaseRequestModel.class));
  }

  protected void stubServiceBulkCreatePromosAsyncDoNothing() {
    doNothing()
        .when(promoService)
        .bulkCreatePromosAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  protected void stubServiceBulkCreatePromosAsyncThrowsBadRequest(String message) {
    doThrow(new com.example.SpringApi.Exceptions.BadRequestException(message))
        .when(promoService)
        .bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());
  }

  protected void stubServiceBulkCreatePromosAsyncThrowsRuntime(String message) {
    doThrow(new RuntimeException(message))
        .when(promoService)
        .bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());
  }

  protected void stubServiceGetUserIdThrowsUnauthorized(String message) {
    doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(message))
        .when(promoService)
        .getUserId();
  }
}

