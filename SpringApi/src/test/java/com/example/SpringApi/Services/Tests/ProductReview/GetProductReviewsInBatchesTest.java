package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;

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
 * Test class for ProductReviewService.getProductReviewsInBatchesGivenProductId
 * method.
 * 
 * Test count: 22 tests
 */
@DisplayName("ProductReviewService - GetProductReviewsInBatches Tests")
class GetProductReviewsInBatchesTest extends ProductReviewServiceTestBase {

        // Total Tests: 22

        @Test
        @DisplayName("Get Product Reviews In Batches - Success")
        void getProductReviewsInBatches_Success() {
                // Arrange
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertNotNull(result.getData());
                assertEquals(1, result.getData().size());
                assertEquals(1L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Empty Results - Success")
        void getProductReviewsInBatches_EmptyResults_Success() {
                // Arrange
                Page<ProductReview> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(emptyPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertNotNull(result.getData());
                assertEquals(0, result.getData().size());
                assertEquals(0L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Invalid Pagination - Throws BadRequestException")
        void getProductReviewsInBatches_InvalidPagination_ThrowsBadRequestException() {
                // Arrange
                testPaginationRequest.setStart(10);
                testPaginationRequest.setEnd(5);

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productReviewService.getProductReviewsInBatchesGivenProductId(
                                                testPaginationRequest,
                                                TEST_PRODUCT_ID));
                assertTrue(exception.getMessage().contains("Invalid pagination"));
        }

        // ========================================
        // ADDITIONAL EDGE CASE Tests
        // ========================================

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 0 End 1 Single Item - Success")
        void getProductReviewsInBatches_SingleItem_Success() {
                // Arrange
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(1);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 1),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 0 End 100 Large Page - Success")
        void getProductReviewsInBatches_LargePage_Success() {
                // Arrange
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(100);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview),
                                PageRequest.of(0, 100), 1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertNotNull(result.getData());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 50 End 60 Middle Page - Success")
        void getProductReviewsInBatches_MiddlePage_Success() {
                // Arrange
                testPaginationRequest.setStart(50);
                testPaginationRequest.setEnd(60);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                100);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(100L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 0 End 0 Zero Page Size - Throws BadRequestException")
        void getProductReviewsInBatches_ZeroPageSize_ThrowsBadRequestException() {
                // Arrange
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(0);

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productReviewService.getProductReviewsInBatchesGivenProductId(
                                                testPaginationRequest,
                                                TEST_PRODUCT_ID));
                assertTrue(exception.getMessage().contains("Invalid pagination"));
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 5 End 5 Equal Start End - Throws BadRequestException")
        void getProductReviewsInBatches_EqualStartEnd_ThrowsBadRequestException() {
                // Arrange
                testPaginationRequest.setStart(5);
                testPaginationRequest.setEnd(5);

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productReviewService.getProductReviewsInBatchesGivenProductId(
                                                testPaginationRequest,
                                                TEST_PRODUCT_ID));
                assertTrue(exception.getMessage().contains("Invalid pagination"));
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 100 End 50 Reversed - Throws BadRequestException")
        void getProductReviewsInBatches_ReversedPagination_ThrowsBadRequestException() {
                // Arrange
                testPaginationRequest.setStart(100);
                testPaginationRequest.setEnd(50);

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                                () -> productReviewService.getProductReviewsInBatchesGivenProductId(
                                                testPaginationRequest,
                                                TEST_PRODUCT_ID));
                assertTrue(exception.getMessage().contains("Invalid pagination"));
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Product ID 1 - Success")
        void getProductReviewsInBatches_ProductIdOne_Success() {
                // Arrange
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(1L), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, 1L);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Product ID Large Value - Success")
        void getProductReviewsInBatches_ProductIdLarge_Success() {
                // Arrange
                long largeProductId = 999999L;
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(largeProductId), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, largeProductId);

                // Assert
                assertNotNull(result);
                assertNotNull(result.getData());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Multiple Results - Success")
        void getProductReviewsInBatches_MultipleResults_Success() {
                // Arrange
                ProductReview review2 = new ProductReview();
                review2.setReviewId(2L);
                ProductReview review3 = new ProductReview();
                review3.setReviewId(3L);

                Page<ProductReview> reviewPage = new PageImpl<>(
                                Arrays.asList(testProductReview, review2, review3),
                                PageRequest.of(0, 10),
                                3);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(3, result.getData().size());
                assertEquals(3L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Large Total Count - Success")
        void getProductReviewsInBatches_LargeTotalCount_Success() {
                // Arrange
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1000);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1000L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 0 End 50 First Page - Success")
        void getProductReviewsInBatches_FirstPage_Success() {
                // Arrange
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(50);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 50),
                                100);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(100L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 950 End 1000 Last Page - Success")
        void getProductReviewsInBatches_LastPage_Success() {
                // Arrange
                testPaginationRequest.setStart(950);
                testPaginationRequest.setEnd(1000);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 50),
                                1000);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1000L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 0 End 1000 Full Dataset - Success")
        void getProductReviewsInBatches_FullDataset_Success() {
                // Arrange
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(1000);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview),
                                PageRequest.of(0, 1000), 1000);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1000L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Null Filters - Success")
        void getProductReviewsInBatches_NullFilters_Success() {
                // Arrange
                testPaginationRequest.setFilters(null);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Include Deleted True - Success")
        void getProductReviewsInBatches_IncludeDeletedTrue_Success() {
                // Arrange
                testPaginationRequest.setIncludeDeleted(true);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), eq(true),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Include Deleted False - Success")
        void getProductReviewsInBatches_IncludeDeletedFalse_Success() {
                // Arrange
                testPaginationRequest.setIncludeDeleted(false);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10),
                                1);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), eq(false),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 1 End 2 Offset Pagination - Success")
        void getProductReviewsInBatches_OffsetPagination_Success() {
                // Arrange
                testPaginationRequest.setStart(1);
                testPaginationRequest.setEnd(2);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 1),
                                10);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(10L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start 999 End 1000 Near End - Success")
        void getProductReviewsInBatches_NearEnd_Success() {
                // Arrange
                testPaginationRequest.setStart(999);
                testPaginationRequest.setEnd(1000);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 1),
                                1000);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(1000L, result.getTotalDataCount());
        }

        @Test
        @DisplayName("Get Product Reviews In Batches - Start Negative 1 End 10 - Success")
        void getProductReviewsInBatches_NegativeStart_Success() {
                // Arrange
                testPaginationRequest.setStart(-1);
                testPaginationRequest.setEnd(10);
                Page<ProductReview> reviewPage = new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 11),
                                100);
                when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(),
                                any(Pageable.class)))
                                .thenReturn(reviewPage);

                // Act
                PaginationBaseResponseModel<ProductReviewResponseModel> result = productReviewService
                                .getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

                // Assert
                assertNotNull(result);
                assertEquals(100L, result.getTotalDataCount());
        }
}
