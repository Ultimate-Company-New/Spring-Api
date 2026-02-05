package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromosInBatches method.
 * 
 * Test count: 5 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 3 tests
 */
@DisplayName("PromoService - GetPromosInBatches Tests")
public class GetPromosInBatchesTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Get Promos In Batches - Success with multiple filters")
    void getPromosInBatches_MultipleFilters_Success() {
        // Arrange
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
        
        List<Promo> promoList = Arrays.asList(testPromo);
        Page<Promo> promoPage = new PageImpl<>(promoList, PageRequest.of(0, 10, Sort.by("promoId").descending()), 1);
        lenient().when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> multiResult = promoService.getPromosInBatches(multiFilterRequest);

        // Assert
        assertNotNull(multiResult);
        assertEquals(1, multiResult.getData().size());
    }

    @Test
    @DisplayName("Get Promos In Batches - Success with single filter")
    void getPromosInBatches_SingleFilter_Success() {
        // Arrange
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

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(singleFilterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Get Promos In Batches - Invalid column name")
    void getPromosInBatches_InvalidColumn() {
        // Arrange
        PaginationBaseRequestModel invalidColumnRequest = new PaginationBaseRequestModel();
        invalidColumnRequest.setStart(0);
        invalidColumnRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition invalidColumn = new PaginationBaseRequestModel.FilterCondition();
        invalidColumn.setColumn("invalidColumn");
        invalidColumn.setOperator("contains");
        invalidColumn.setValue("test");
        invalidColumnRequest.setFilters(List.of(invalidColumn));
        invalidColumnRequest.setLogicOperator("AND");
        
        // Act & Assert
        BadRequestException invalidColumnEx = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(invalidColumnRequest));
        assertTrue(invalidColumnEx.getMessage().contains("Invalid column name"));
    }

    @Test
    @DisplayName("Get Promos In Batches - Invalid operator")
    void getPromosInBatches_InvalidOperator() {
        // Arrange
        PaginationBaseRequestModel invalidOperatorRequest = new PaginationBaseRequestModel();
        invalidOperatorRequest.setStart(0);
        invalidOperatorRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition invalidOperator = new PaginationBaseRequestModel.FilterCondition();
        invalidOperator.setColumn("promoCode");
        invalidOperator.setOperator("invalidOperator");
        invalidOperator.setValue("test");
        invalidOperatorRequest.setFilters(List.of(invalidOperator));
        invalidOperatorRequest.setLogicOperator("AND");
        
        // Act & Assert
        BadRequestException invalidOperatorEx = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(invalidOperatorRequest));
        assertTrue(invalidOperatorEx.getMessage().contains("Invalid operator"));
    }

    @Test
    @DisplayName("Get Promos In Batches - Invalid pagination")
    void getPromosInBatches_InvalidPagination() {
        // Arrange
        PaginationBaseRequestModel invalidPagination = new PaginationBaseRequestModel();
        invalidPagination.setStart(10);
        invalidPagination.setEnd(5);
        
        // Act & Assert
        BadRequestException paginationEx = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(invalidPagination));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, paginationEx.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPromosInBatches - Verify @PreAuthorize Annotation")
    void getPromosInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PromoController.class.getMethod("getPromosInBatches", PaginationBaseRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPromosInBatches");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PROMOS_PERMISSION),
                "@PreAuthorize should reference VIEW_PROMOS_PERMISSION");
    }

    @Test
    @DisplayName("getPromosInBatches - Controller delegates to service")
    void getPromosInBatches_WithValidRequest_DelegatesToService() {
        PromoController controller = new PromoController(promoService);
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        @SuppressWarnings("unchecked")
        PaginationBaseResponseModel<Promo> mockResponse = mock(PaginationBaseResponseModel.class);
        when(promoService.getPromosInBatches(request)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getPromosInBatches(request);

        verify(promoService).getPromosInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
