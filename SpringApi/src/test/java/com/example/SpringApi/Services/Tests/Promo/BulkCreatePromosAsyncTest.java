package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.bulkCreatePromosAsync method.
 */
@DisplayName("PromoService - BulkCreatePromosAsync Tests")
class BulkCreatePromosAsyncTest extends PromoServiceTestBase {

        // Total Tests: 21
        private static final AtomicLong idCounter = new AtomicLong(100);

        /*
         **********************************************************************************************
         * SECTION 1: SUCCESS TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify successful asynchronous bulk creation of valid promos.
         */
        @Test
        @DisplayName("Bulk Create Promos - Success - All valid promos")
        void bulkCreatePromosAsync_AllValid_Success() {
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
                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
                stubPromoRepositoryFindByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID),
                                Optional.empty());
                when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))
                                .thenAnswer(invocation -> {
                                        String code = invocation.getArgument(0);
                                        return Optional.ofNullable(
                                                        savedPromos.get(code != null ? code.toUpperCase() : null));
                                });
                when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
                        Promo promo = invocation.getArgument(0);
                        promo.setPromoId(idCounter.incrementAndGet());
                        savedPromos.put(promo.getPromoCode(), promo);
                        return promo;
                });

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(3)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify that providing an empty list to bulk creation does not cause
         * errors.
         */
        @Test
        @DisplayName("Bulk Create Promos - Success - Empty list")
        void bulkCreatePromosAsync_EmptyList_Success() {
                // Arrange
                List<PromoRequestModel> promos = new ArrayList<>();

                // Act & Assert
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));
                verify(promoRepository, never()).save(any(Promo.class));
        }

        /**
         * Purpose: Verify that a large batch of promos can be processed asynchronously.
         */
        @Test
        @DisplayName("Bulk Create Promos - Success - Large batch (50 promos)")
        void bulkCreatePromosAsync_LargeBatch_Success() {
                // Arrange
                List<PromoRequestModel> promos = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                        PromoRequestModel promoReq = new PromoRequestModel();
                        promoReq.setPromoCode("BULK" + i);
                        promoReq.setDescription("Bulk Description " + i);
                        promoReq.setDiscountValue(BigDecimal.valueOf(5));
                        promoReq.setClientId(TEST_CLIENT_ID);
                        promoReq.setStartDate(java.time.LocalDate.now());
                        promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(7));
                        promos.add(promoReq);
                }

                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
                when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(50)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify asynchronous processing with 100 promos (Performance test).
         */
        @Test
        @DisplayName("Bulk Create Promos - Success - Performance test with 100 promos")
        void bulkCreatePromosAsync_PerformanceTest_Success() {
                // Arrange
                List<PromoRequestModel> promos = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                        PromoRequestModel promoReq = new PromoRequestModel();
                        promoReq.setPromoCode("PERF" + String.format("%02d", i)); // Ensure 3+ chars
                        if (promoReq.getPromoCode().length() < 3)
                                promoReq.setPromoCode("CODE" + i);
                        promoReq.setDescription("Perf test promo " + i);
                        promoReq.setDiscountValue(BigDecimal.valueOf(i % 100 + 1));
                        promoReq.setClientId(TEST_CLIENT_ID);
                        promoReq.setStartDate(java.time.LocalDate.now());
                        promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(i % 30 + 1));
                        promos.add(promoReq);
                }

                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
                when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(100)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify bulk creation with a single promo batch.
         */
        @Test
        @DisplayName("Bulk Create Promos - Success - Single promo batch")
        void bulkCreatePromosAsync_Single_Success() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(1)).save(any(Promo.class));
        }

        /*
         **********************************************************************************************
         * SECTION 2: FAILURE / EXCEPTION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify that invalid promos in a bulk request are not saved.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - All invalid promos")
        void bulkCreatePromosAsync_AllInvalid_NoSaves() {
                // Arrange
                List<PromoRequestModel> promos = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                        PromoRequestModel invalid = new PromoRequestModel();
                        invalid.setPromoCode(" ");
                        invalid.setDiscountValue(new BigDecimal("-1"));
                        invalid.setClientId(TEST_CLIENT_ID);
                        promos.add(invalid);
                }

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, never()).save(any(Promo.class));
        }

        /**
         * Purpose: Verify graceful handling of repository exceptions during bulk
         * creation.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Database error")
        void bulkCreatePromosAsync_DatabaseError_CapturesFailure() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                stubPromoRepositoryFindByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID),
                                Optional.empty());
                // Service should attempt to save even if it fails later
                stubPromoRepositorySave(testPromo);
                when(promoRepository.save(any(Promo.class)))
                                .thenThrow(new RuntimeException("Database error"));

                // Act & Assert
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));
                // We verify save was CALLED (because service tries until fail)
                verify(promoRepository, atLeastOnce()).save(any(Promo.class));
        }

        /**
         * Purpose: Verify partial success when some promo codes overlap.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Overlapping codes in some")
        void bulkCreatePromosAsync_Overlapping_PartialSuccess() {
                // Arrange
                PromoRequestModel p1 = new PromoRequestModel();
                p1.setPromoCode("P11");
                p1.setDescription("D1");
                p1.setDiscountValue(BigDecimal.ONE);
                p1.setClientId(TEST_CLIENT_ID);
                p1.setStartDate(java.time.LocalDate.now());
                p1.setExpiryDate(java.time.LocalDate.now().plusDays(1));

                PromoRequestModel p2 = new PromoRequestModel(); // Overlapping
                p2.setPromoCode("P22");
                p2.setDescription("D2");
                p2.setDiscountValue(BigDecimal.ONE);
                p2.setClientId(TEST_CLIENT_ID);
                p2.setStartDate(java.time.LocalDate.now());
                p2.setExpiryDate(java.time.LocalDate.now().plusDays(1));

                List<PromoRequestModel> promos = List.of(p1, p2);

                when(promoRepository.findOverlappingPromos(eq("P11"), anyLong(), any(), any()))
                                .thenReturn(Collections.emptyList());
                when(promoRepository.findOverlappingPromos(eq("P22"), anyLong(), any(), any()))
                                .thenReturn(List.of(testPromo));

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(1)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify partial success when one promo is valid and another is a
         * duplicate.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Duplicate promo code")
        void bulkCreatePromosAsync_DuplicatePromoCode_PartialSuccess() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest, testPromoRequest);

                when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                                .thenReturn(Collections.emptyList())
                                .thenReturn(List.of(testPromo));

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(1)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify graceful handling of a null promos list.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Null list")
        void bulkCreatePromosAsync_NullList_CapturesFailure() {
                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(null, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert - Should call BulkInsertHelper to notify user of failure
                verify(messageService, times(1)).createMessageWithContext(any(), eq(TEST_USER_ID), eq(TEST_LOGIN_NAME),
                                eq(TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify partial failure when some promos have invalid date ranges.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Expiry before start")
        void bulkCreatePromosAsync_PartialFailure_InvalidDateRange() {
                // Arrange
                PromoRequestModel valid = new PromoRequestModel();
                valid.setPromoCode("VALID");
                valid.setDescription("Valid promo");
                valid.setDiscountValue(BigDecimal.TEN);
                valid.setClientId(TEST_CLIENT_ID);
                valid.setStartDate(java.time.LocalDate.now());
                valid.setExpiryDate(java.time.LocalDate.now().plusDays(30));

                PromoRequestModel invalid = new PromoRequestModel();
                invalid.setPromoCode("INVALID");
                invalid.setDescription("Expiry before start");
                invalid.setDiscountValue(BigDecimal.TEN);
                invalid.setClientId(TEST_CLIENT_ID);
                invalid.setStartDate(java.time.LocalDate.now());
                invalid.setExpiryDate(java.time.LocalDate.now().minusDays(5)); // Before start

                List<PromoRequestModel> promos = List.of(valid, invalid);

                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert - Only valid promo saved
                verify(promoRepository, times(1)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify partial success when some promos fail basic validation.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Some fail validation")
        void bulkCreatePromosAsync_SomeFailValidation_PartialSuccess() {
                // Arrange
                PromoRequestModel valid = testPromoRequest;
                PromoRequestModel invalid = new PromoRequestModel();
                invalid.setPromoCode(null);
                invalid.setDescription("Invalid promo");
                invalid.setDiscountValue(BigDecimal.ONE);
                invalid.setClientId(TEST_CLIENT_ID);
                invalid.setStartDate(java.time.LocalDate.now());
                invalid.setExpiryDate(java.time.LocalDate.now().plusDays(1));

                List<PromoRequestModel> promos = List.of(valid, invalid);
                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, atMost(1)).save(any(Promo.class));
        }

        /**
         * Purpose: Verify partial success reporting when some promos fail validation.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Partial failure reporting")
        void bulkCreatePromosAsync_PartialFailure_Reporting_Success() {
                // Arrange
                PromoRequestModel p1 = new PromoRequestModel();
                p1.setPromoCode("SUCCESS");
                p1.setDescription("Valid");
                p1.setDiscountValue(BigDecimal.TEN);
                p1.setClientId(TEST_CLIENT_ID);
                p1.setStartDate(java.time.LocalDate.now());

                PromoRequestModel p2 = new PromoRequestModel(); // Invalid (no code)
                p2.setPromoCode(null);

                List<PromoRequestModel> promos = List.of(p1, p2);

                when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                                .thenReturn(Collections.emptyList());
                when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // Stub findByPromoCode for the success one
                when(promoRepository.findByPromoCodeAndClientId(eq("SUCCESS"), eq(TEST_CLIENT_ID)))
                                .thenReturn(Optional.of(testPromo));

                // Act
                promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID);

                // Assert
                verify(promoRepository, times(1)).save(any(Promo.class));
                // Verify notification was sent with results
                verify(messageService).createMessageWithContext(
                                argThat(req -> req.getTitle().contains("1/2 Succeeded")),
                                eq(TEST_USER_ID), eq(TEST_LOGIN_NAME), eq(TEST_CLIENT_ID));
        }

        /**
         * Purpose: Verify bulk creation with mixed validity promos.
         */
        @Test
        @DisplayName("Bulk Create Promos - Failure - Mixed validity")
        void bulkCreatePromosAsync_MixedValidity() {
                // Arrange
                List<PromoRequestModel> promos = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                        PromoRequestModel p = new PromoRequestModel();
                        p.setPromoCode(i % 2 == 0 ? "VALID" + i : "IV"); // IV is too short
                        p.setDescription("Desc " + i);
                        p.setDiscountValue(BigDecimal.TEN);
                        p.setClientId(TEST_CLIENT_ID);
                        p.setStartDate(java.time.LocalDate.now());
                        p.setExpiryDate(java.time.LocalDate.now().plusDays(1));
                        promos.add(p);
                }
                stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());

                // Act
                assertDoesNotThrow(
                                () -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME,
                                                TEST_CLIENT_ID));

                // Assert
                verify(promoRepository, times(5)).save(any(Promo.class));
        }

        /*
         **********************************************************************************************
         * SECTION 3: CONTROLLER PERMISSION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify that the controller correctly delegates bulkCreatePromos
         * calls to the service layer.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller delegates to service")
        void bulkCreatePromosAsync_ControllerDelegation_Success() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                // Using doNothing because service is a spy and we want to avoid real method
                // execution
                doNothing().when(promoService).bulkCreatePromosAsync(anyList(), anyLong(), anyString(), anyLong());

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(promos);

                // Assert
                verify(promoService).bulkCreatePromosAsync(eq(promos), anyLong(), anyString(), anyLong());
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Purpose: Verify unauthorized access is blocked at the controller level.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller Permission - Unauthorized")
        void bulkCreatePromosAsync_controller_permission_unauthorized() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                stubServiceThrowsUnauthorizedException();

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(promos);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        /**
         * Purpose: Verify controller handles BadRequestException from service.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller handles BadRequestException")
        void bulkCreatePromosAsync_ControllerHandlesBadRequest() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Empty list"))
                                .when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(promos);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        /**
         * Purpose: Verify controller handles RuntimeException from service.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller handles internal error")
        void bulkCreatePromosAsync_ControllerHandlesInternalError() {
                // Arrange
                List<PromoRequestModel> promos = List.of(testPromoRequest);
                doThrow(new RuntimeException("Critical failure"))
                                .when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(promos);

                // Assert
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        /**
         * Purpose: Verify controller handles context retrieval failure.
         */
        @Test
        @DisplayName("bulkCreatePromos - Failure - Context retrieval error")
        void bulkCreatePromosAsync_ContextRetrievalError() {
                // Arrange
                doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException("Context missing"))
                                .when(promoService).getUserId();

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(List.of(testPromoRequest));

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        /**
         * Purpose: Verify that the controller works with an empty list by passing it to
         * service.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller with empty list")
        void bulkCreatePromosAsync_ControllerWithEmptyList() {
                // Arrange
                List<PromoRequestModel> emptyList = Collections.emptyList();
                doNothing().when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(emptyList);

                // Assert
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        /**
         * Purpose: Verify that null promos list in controller handles exception.
         */
        @Test
        @DisplayName("bulkCreatePromos - Controller with null list")
        void bulkCreatePromosAsync_ControllerWithNullList() {
                // Arrange
                doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Null list"))
                                .when(promoService).bulkCreatePromosAsync(isNull(), anyLong(), anyString(), anyLong());

                // Act
                ResponseEntity<?> response = promoController.bulkCreatePromos(null);

                // Assert
                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
}
