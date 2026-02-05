package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductReviewResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test class for ProductReviewService.getProductReviewsInBatchesGivenProductId method.
 * 
 * Test count: 1 comprehensive test
 * - SUCCESS: Partial (includes success paths)
 * - FAILURE / EXCEPTION: Partial (includes failure paths)
 */
@DisplayName("ProductReviewService - GetProductReviewsInBatches Tests")
public class GetProductReviewsInBatchesTest extends ProductReviewServiceTestBase {

    @Test
    @DisplayName("Get Product Reviews In Batches - Invalid pagination, success, empty")
    void getProductReviewsInBatchesGivenProductId_Comprehensive() {
        // Invalid pagination (end <= start)
        PaginationBaseRequestModel invalid = new PaginationBaseRequestModel();
        invalid.setStart(10);
        invalid.setEnd(5);
        BadRequestException invalidEx = assertThrows(BadRequestException.class,
                () -> productReviewService.getProductReviewsInBatchesGivenProductId(invalid, TEST_PRODUCT_ID));
        assertTrue(invalidEx.getMessage().contains("Invalid pagination"));

        // Success with one review
        Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10), 1);

        when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(reviewPage);

        PaginationBaseResponseModel<ProductReviewResponseModel> success =
            productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

        assertNotNull(success);
        assertNotNull(success.getData());
        assertEquals(1, success.getData().size());
        assertEquals(1L, success.getTotalDataCount());

        // Empty results
        Page<ProductReview> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(emptyPage);

        PaginationBaseResponseModel<ProductReviewResponseModel> empty =
            productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

        assertNotNull(empty);
        assertNotNull(empty.getData());
        assertEquals(0, empty.getData().size());
        assertEquals(0L, empty.getTotalDataCount());
    }
}
