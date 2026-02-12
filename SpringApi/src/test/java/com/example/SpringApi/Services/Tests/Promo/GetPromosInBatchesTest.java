package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromosInBatches method.
 */
@DisplayName("PromoService - GetPromosInBatches Tests")
class GetPromosInBatchesTest extends PromoServiceTestBase {

    // Total Tests: 9

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify include deleted request returns results.
     * Expected Result: Response contains promo data.
     * Assertions: data not empty.
     */
    @Test
    @DisplayName("getPromosInBatches - Include Deleted - Success")
    void getPromosInBatches_IncludeDeleted_Success() {
        // Arrange
        testPaginationRequest.setIncludeDeleted(true);
        Page<Promo> promoPage = new PageImpl<>(Arrays.asList(testPromo), PageRequest.of(0, 10), 1);
        stubPromoFilterQueryBuilderFindPaginatedEntities(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(1, result.getData().size());
    }




    /*
     * Purpose: Verify valid request returns results.
     * Expected Result: Response contains promo data.
     * Assertions: data not empty.
     */
    @Test
    @DisplayName("getPromosInBatches - Valid Request - Success")
    void getPromosInBatches_ValidRequest_Success() {
        // Arrange
        Page<Promo> promoPage = new PageImpl<>(Arrays.asList(testPromo), PageRequest.of(0, 10), 1);
        stubPromoFilterQueryBuilderFindPaginatedEntities(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(1, result.getData().size());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify invalid column throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Invalid Column - Throws BadRequestException")
    void getPromosInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.getFilters().get(0).setColumn("invalidColumn");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.InvalidColumnName, "invalidColumn"),
                ex.getMessage());
    }




    /*
     * Purpose: Verify negative start throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Negative Start - Throws BadRequestException")
    void getPromosInBatches_NegativeStart_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(-1);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, ex.getMessage());
    }




    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Null Request - Throws BadRequestException")
    void getPromosInBatches_NullRequest_ThrowsException() {
        // Arrange
        testPaginationRequest = null;

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify unauthorized access is blocked at controller level.
     * Expected Result: Unauthorized status.
     * Assertions: status is 401.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Permission - Unauthorized")
    void getPromosInBatches_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /*
     * Purpose: Verify controller delegation.
     * Expected Result: OK status.
     * Assertions: status is OK.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Delegation - Success")
    void getPromosInBatches_ControllerDelegation_Success() {
        // Arrange
        PaginationBaseResponseModel<PromoResponseModel> responseModel = new PaginationBaseResponseModel<>();
        responseModel.setData(List.of(new PromoResponseModel(testPromo)));
        stubServiceGetPromosInBatchesReturns(responseModel);

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }




    /*
     * Purpose: Verify controller handles bad request.
     * Expected Result: BAD_REQUEST status.
     * Assertions: status is 400.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Handles BadRequest - Success")
    void getPromosInBatches_ControllerHandlesBadRequest_Success() {
        // Arrange
        stubServiceGetPromosInBatchesThrowsBadRequest("Bad Filter");

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }




    /*
     * Purpose: Verify controller handles exception.
     * Expected Result: INTERNAL_SERVER_ERROR status.
     * Assertions: status is 500.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Handles Exception - Failure")
    void getPromosInBatches_ControllerHandlesException_Failure() {
        // Arrange
        stubServiceGetPromosInBatchesThrowsRuntime("Crash");

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
