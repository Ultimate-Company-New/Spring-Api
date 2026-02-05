package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromoDetailsByName method.
 * 
 * Test count: 16 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 14 tests
 */
@DisplayName("PromoService - GetPromoDetailsByName Tests")
public class GetPromoDetailsByNameTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Get Promo Details By Name - Case insensitive lookup")
    void getPromoDetailsByName_CaseInsensitiveLookup() {
        // Arrange
        when(promoRepository.findByPromoCodeAndClientId("TEST10", TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName("test10");

        // Assert
        assertNotNull(result);
        verify(promoRepository).findByPromoCodeAndClientId("TEST10", TEST_CLIENT_ID);
    }

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

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Empty promo code")
    void getPromoDetailsByName_EmptyPromoCode_ThrowsNotFoundException() {
        // Arrange
        String emptyCode = "";

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(emptyCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    @TestFactory
    @DisplayName("Get Promo Details By Name - Invalid inputs (tab, newline, spaces)")
    Stream<DynamicTest> getPromoDetailsByName_InvalidInputs() {
        return Stream.of("\t", "\n", "   ", "")
                .map(code -> DynamicTest.dynamicTest("Invalid code: [" + code + "]", () -> {
                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> promoService.getPromoDetailsByName(code));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
                }));
    }

    @TestFactory
    @DisplayName("Get Promo Details By Name - Invalid whitespace variations")
    Stream<DynamicTest> getPromoDetailsByName_InvalidWhitespaceVariations() {
        return Stream.of("  ", "\t\t", "\n\n", "    ", "\t\n")
                .map(code -> DynamicTest.dynamicTest("Whitespace code: [" + code + "]", () -> {
                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> promoService.getPromoDetailsByName(code));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
                }));
    }

    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Null promo code")
    void getPromoDetailsByName_NullPromoCode_ThrowsNotFoundException() {
        // Arrange
        String nullCode = null;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(nullCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    @TestFactory
    @DisplayName("Get Promo Details By Name - Promo code not found for multiple codes")
    Stream<DynamicTest> getPromoDetailsByName_PromoCodeNotFound_MultipleCodes() {
        return Stream.of("NOTFOUND", "INVALID", "DOESNOTEXIST", "NOPROMO")
                .map(code -> DynamicTest.dynamicTest("Non-existent code: " + code, () -> {
                    when(promoRepository.findByPromoCodeAndClientId(code.toUpperCase(), TEST_CLIENT_ID))
                            .thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.getPromoDetailsByName(code));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidName, ex.getMessage());
                }));
    }

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

    @Test
    @DisplayName("Get Promo Details By Name - Edge Case - Whitespace promo code")
    void getPromoDetailsByName_WhitespacePromoCode_Success() {
        // Arrange
        String whitespaceCode = "   ";

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.getPromoDetailsByName(whitespaceCode);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPromoDetailsByName - Verify @PreAuthorize Annotation")
    void getPromoDetailsByName_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PromoController.class.getMethod("getPromoDetailsByName", String.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPromoDetailsByName");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PROMOS_PERMISSION),
                "@PreAuthorize should reference VIEW_PROMOS_PERMISSION");
    }

    @Test
    @DisplayName("getPromoDetailsByName - Controller delegates to service")
    void getPromoDetailsByName_WithValidCode_DelegatesToService() {
        PromoController controller = new PromoController(promoService);
        String promoCode = "TEST10";
        when(promoService.getPromoDetailsByName(promoCode))
                .thenReturn(mock(PromoResponseModel.class));

        ResponseEntity<?> response = controller.getPromoDetailsByName(promoCode);

        verify(promoService).getPromoDetailsByName(promoCode);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
