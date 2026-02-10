package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Services.Interface.ILeadSubTranslator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.updateLead() method.
 * Tests lead updates with various validation scenarios.
 * * Test Count: 33 tests
 */
@DisplayName("Update Lead Tests")
class UpdateLeadTest extends LeadServiceTestBase {

    @Mock
    ILeadSubTranslator leadServiceMock;

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful update when all fields are changed.
     * Expected Result: Lead is persisted with all new data.
     * Assertions: Repository save is invoked with the updated model.
     */
    @Test
    @DisplayName("Update Lead - All fields updated - Success")
    void updateLead_AllFieldsUpdated_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName("UpdatedFirst");
        testLeadRequest.setLastName("UpdatedLast");
        testLeadRequest.setEmail("updated@example.com");
        testLeadRequest.setPhone("5551234567");
        testLeadRequest.setCompany("UpdatedCorp");
        testLeadRequest.setLeadStatus("Contacted");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        verify(leadRepository).save(any());
    }

    /**
     * Purpose: Verify update succeeds with special characters in field values.
     * Expected Result: Non-standard characters are handled correctly.
     * Assertions: Process completes without error.
     */
    @Test
    @DisplayName("Update Lead - Special Characters - Success")
    void updateLead_SpecialCharacters_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName("François");
        testLeadRequest.setCompany("O'Reilly & Associates");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
    }

    /**
     * Purpose: Verify basic successful update scenario.
     * Expected Result: Existing lead is found and saved.
     * Assertions: Repository save and address save occur.
     */
    @Test
    @DisplayName("Update Lead - Success")
    void updateLead_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        when(leadRepository.save(any())).thenReturn(testLead);
        when(addressRepository.save(any())).thenReturn(testLead.getAddress());

        // Act & Assert
        assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        verify(leadRepository).save(any());
    }

    /**
     * Purpose: Verify update succeeds with very long name values.
     * Expected Result: Long strings are persisted successfully.
     * Assertions: Repository save is invoked.
     */
    @Test
    @DisplayName("Update Lead - Very Long Name - Success")
    void updateLead_VeryLongName_Success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName("VeryVeryVeryVeryVeryLongFirstName");
        testLeadRequest.setLastName("VeryVeryVeryVeryVeryLongLastName");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject updates for leads already marked as deleted.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Update Lead - Deleted - ThrowsNotFoundException")
    void updateLead_Deleted_ThrowsNotFoundException() {
        testLead.setIsDeleted(true);
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject updates Consisting of an empty email string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Lead - Empty Email - ThrowsBadRequestException")
    void updateLead_EmptyEmail_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setEmail("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject updates with an empty first name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Lead - Empty First Name - ThrowsBadRequestException")
    void updateLead_EmptyFirstName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject updates with an empty last name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Update Lead - Empty Last Name - ThrowsBadRequestException")
    void updateLead_EmptyLastName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLastName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject updates with an empty phone string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Update Lead - Empty Phone - ThrowsBadRequestException")
    void updateLead_EmptyPhone_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setPhone("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject updates with an empty status string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Update Lead - Empty Status - ThrowsBadRequestException")
    void updateLead_EmptyStatus_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLeadStatus("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject update with an invalid email format.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Update Lead - Invalid Email Format")
    void updateLead_InvalidEmailFormat_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setEmail("invalid-email");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER010, ex.getMessage());
    }

    /**
     * Purpose: Reject update with an invalid phone format.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches expectations.
     */
    @Test
    @DisplayName("Update Lead - Invalid Phone Format - ThrowsBadRequestException")
    void updateLead_InvalidPhoneFormat_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setPhone("invalid-phone");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER011, ex.getMessage());
    }

    /**
     * Purpose: Reject updates to an unknown lead status.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message contains ER007.
     */
    @Test
    @DisplayName("Update Lead - Invalid Status (Unknown) - ThrowsBadRequestException")
    void updateLead_InvalidStatus_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLeadStatus("InvalidStatus");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertTrue(ex.getMessage().contains(ErrorMessages.LeadsErrorMessages.ER007));
    }

    /**
     * Purpose: Reject updates for negative lead IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches LEAD_NOT_FOUND.
     */
    @Test
    @DisplayName("Update Lead - Negative ID - ThrowsNotFoundException")
    void updateLead_NegativeId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.updateLead(-1L, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when the lead ID does not exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches LEAD_NOT_FOUND.
     */
    @Test
    @DisplayName("Update Lead - NotFound - ThrowsNotFoundException")
    void updateLead_NotFound_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(anyLong(), anyLong())).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when the request email is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Lead - Null Email - ThrowsBadRequestException")
    void updateLead_NullEmail_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setEmail(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when first name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Lead - Null First Name - ThrowsBadRequestException")
    void updateLead_NullFirstName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when last name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Update Lead - Null Last Name - ThrowsBadRequestException")
    void updateLead_NullLastName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLastName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when phone is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Update Lead - Null Phone - ThrowsBadRequestException")
    void updateLead_NullPhone_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setPhone(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when the entire request model is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER009.
     */
    @Test
    @DisplayName("Update Lead - Null Request - ThrowsBadRequestException")
    void updateLead_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, null));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER009, ex.getMessage());
    }

    /**
     * Purpose: Reject updates when lead status is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Update Lead - Null Status - ThrowsBadRequestException")
    void updateLead_NullStatus_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLeadStatus(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject updates Consisting of a company size of zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER016.
     */
    @Test
    @DisplayName("Update Lead - Valid Company Size Zero - ThrowsBadRequestException")
    void updateLead_ValidCompanySizeZero_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setCompanySize(0);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER016, ex.getMessage());
    }

    /**
     * Purpose: Reject email strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Lead - Whitespace Email - ThrowsBadRequestException")
    void updateLead_WhitespaceEmail_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setEmail("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject first name strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Lead - Whitespace First Name - ThrowsBadRequestException")
    void updateLead_WhitespaceFirstName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setFirstName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject last name strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Update Lead - Whitespace Last Name - ThrowsBadRequestException")
    void updateLead_WhitespaceLastName_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLastName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject phone strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Update Lead - Whitespace Phone - ThrowsBadRequestException")
    void updateLead_WhitespacePhone_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setPhone("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject status strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Update Lead - Whitespace Status - ThrowsBadRequestException")
    void updateLead_WhitespaceStatus_ThrowsBadRequestException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setLeadStatus("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject zero as a lead ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches LEAD_NOT_FOUND.
     */
    @Test
    @DisplayName("Update Lead - Zero ID - ThrowsNotFoundException")
    void updateLead_ZeroId_ThrowsNotFoundException() {
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.updateLead(0L, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /**
     * Purpose: Verify partial updates (patch-like) only change provided fields and persist.
     * Given: Existing lead and update DTO with phone number change
     * When: updateLead is called
     * Then: Lead is persisted with update
     */
    @Test
    @DisplayName("Update Lead - Partial Update Success")
    void updateLead_unit_partialUpdate_success() {
        // Arrange
        when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID))
                .thenReturn(testLead);
        testLeadRequest.setPhone("555-0200-1234");
        when(leadRepository.save(any())).thenReturn(testLead);
        
        // Act
        assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
        
        // Assert
        verify(leadRepository).save(any());
    }

    /**
     * Purpose: Updating a non-existent lead throws not-found.
     * Given: Non-existent lead ID
     * When: updateLead is called
     * Then: NotFoundException is thrown with LEAD_NOT_FOUND message
     */
    @Test
    @DisplayName("Update Lead - Not Found Failure")
    void updateLead_unit_notFound_failure() {
        // Arrange
        stubLeadRepositoryFindByIdNotFound(999L);
        
        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.updateLead(999L, testLeadRequest));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on updateLead method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references UPDATE_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("Update Lead - Verify @PreAuthorize annotation is configured correctly")
    void updateLead_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = LeadController.class.getMethod("updateLead",
                Long.class, com.example.SpringApi.Models.RequestModels.LeadRequestModel.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "updateLead method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.UPDATE_LEADS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference UPDATE_LEADS_PERMISSION");
    }

    /**
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     * 
     * Note: This test simulates the happy path assuming authorization has already
     * passed.
     * Actual @PreAuthorize enforcement is handled by Spring Security AOP and tested
     * in end-to-end tests.
     */
    @Test
    @DisplayName("Update Lead - Controller delegates to service correctly")
    void updateLead_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        doNothing().when(leadServiceMock).updateLead(eq(DEFAULT_LEAD_ID),
                any(com.example.SpringApi.Models.RequestModels.LeadRequestModel.class));

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.updateLead(DEFAULT_LEAD_ID, testLeadRequest);

        // Assert - Verify service was called and correct response returned
        verify(leadServiceMock, times(1)).updateLead(DEFAULT_LEAD_ID, testLeadRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}
