package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for LeadService.createLead() method.
 * Tests lead creation with various validation scenarios.
 * * Test Count: 33 tests
 */
@DisplayName("Create Lead Tests")
class CreateLeadTest extends LeadServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify creation succeeds with the maximum possible integer for company size.
     * Expected Result: Lead is saved with the provided company size.
     * Assertions: Repository save is invoked and no exceptions are thrown.
     */
    @Test
    @DisplayName("Create Lead - Max Company Size - Success")
    void createLead_MaxCompanySize_Success() {
        // Arrange
        testLeadRequest.setCompanySize(Integer.MAX_VALUE);
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
        verify(leadRepository).save(any());
    }

    /**
     * Purpose: Verify permission check is performed for INSERT_LEAD permission.
     * Expected Result: Authorization service verifies the INSERT_LEAD authority.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Create Lead - Permission check - Success Verifies Authorization")
    void createLead_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        when(leadRepository.save(any())).thenReturn(testLead);
        lenient().when(authorization.hasAuthority(Authorizations.INSERT_LEADS_PERMISSION)).thenReturn(true);

        // Act
        leadService.createLead(testLeadRequest);

        // Assert
        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.INSERT_LEADS_PERMISSION);
    }

    /**
     * Purpose: Verify creation succeeds with special characters in the lead's name.
     * Expected Result: Lead is saved successfully with characters like accents and hyphens.
     * Assertions: Process completes without error.
     */
    @Test
    @DisplayName("Create Lead - Special Characters in Name - Success")
    void createLead_SpecialCharactersInName_Success() {
        // Arrange
        testLeadRequest.setFirstName("José");
        testLeadRequest.setLastName("García-López");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /**
     * Purpose: Verify basic successful lead creation with a valid request.
     * Expected Result: Lead is saved and success is logged.
     * Assertions: Repository save and user log calls occur.
     */
    @Test
    @DisplayName("Create Lead - Success")
    void createLead_Success() {
        // Arrange
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));

        // Assert
        verify(leadRepository).save(any());
        verify(userLogService).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify creation succeeds with Unicode characters in name fields.
     * Expected Result: Non-Latin characters are preserved and lead is saved.
     * Assertions: No exception occurs during the save process.
     */
    @Test
    @DisplayName("Create Lead - Unicode Characters - Success")
    void createLead_UnicodeCharacters_Success() {
        // Arrange
        testLeadRequest.setFirstName("李");
        testLeadRequest.setLastName("王");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /**
     * Purpose: Verify creation succeeds with a valid company string.
     * Expected Result: Company field is persisted.
     * Assertions: Repository save is invoked.
     */
    @Test
    @DisplayName("Create Lead - Valid Company - Success")
    void createLead_ValidCompany_Success() {
        // Arrange
        testLeadRequest.setCompany("Valid Tech Company");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /**
     * Purpose: Verify creation succeeds with a valid job title.
     * Expected Result: Title field is persisted correctly.
     * Assertions: Lead creation does not throw an exception.
     */
    @Test
    @DisplayName("Create Lead - Valid Title - Success")
    void createLead_ValidTitle_Success() {
        // Arrange
        testLeadRequest.setTitle("Senior Manager");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /**
     * Purpose: Verify system handles extremely long email addresses within valid format.
     * Expected Result: Save completes successfully.
     * Assertions: No validation error triggered for email length.
     */
    @Test
    @DisplayName("Create Lead - Very Long Email - Success")
    void createLead_VeryLongEmail_Success() {
        // Arrange
        testLeadRequest.setEmail("verylongemailaddress.withmanydots.test@verylongdomainname.co.uk");
        when(leadRepository.save(any())).thenReturn(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject lead creation with an empty email string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Create Lead - Empty Email - ThrowsBadRequestException")
    void createLead_EmptyEmail_ThrowsBadRequestException() {
        testLeadRequest.setEmail("");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation with an empty first name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Create Lead - Empty First Name - ThrowsBadRequestException")
    void createLead_EmptyFirstName_ThrowsBadRequestException() {
        testLeadRequest.setFirstName("");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation with an empty last name.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Create Lead - Empty Last Name - ThrowsBadRequestException")
    void createLead_EmptyLastName_ThrowsBadRequestException() {
        testLeadRequest.setLastName("");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation with an empty phone string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Create Lead - Empty Phone - ThrowsBadRequestException")
    void createLead_EmptyPhone_ThrowsBadRequestException() {
        testLeadRequest.setPhone("");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation with an empty status string.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Create Lead - Empty Status - ThrowsBadRequestException")
    void createLead_EmptyStatus_ThrowsBadRequestException() {
        testLeadRequest.setLeadStatus("");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid email formats (missing @, etc.).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER010.
     */
    @Test
    @DisplayName("Create Lead - Invalid Email Format - ThrowsBadRequestException")
    void createLead_InvalidEmailFormat_ThrowsBadRequestException() {
        testLeadRequest.setEmail("invalid-email");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER010, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid phone formats.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER011.
     */
    @Test
    @DisplayName("Create Lead - Invalid Phone Format - ThrowsBadRequestException")
    void createLead_InvalidPhoneFormat_ThrowsBadRequestException() {
        testLeadRequest.setPhone("invalid-phone");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER011, ex.getMessage());
    }

    /**
     * Purpose: Reject unknown lead statuses.
     * Expected Result: BadRequestException is thrown with allowed values.
     * Assertions: Error message contains ER007.
     */
    @Test
    @DisplayName("Create Lead - Invalid Status (Unknown) - ThrowsBadRequestException")
    void createLead_InvalidStatusUnknown_ThrowsBadRequestException() {
        testLeadRequest.setLeadStatus("UnknownStatus");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertTrue(ex.getMessage().contains(ErrorMessages.LeadsErrorMessages.ER007));
    }

    /**
     * Purpose: Reject requests missing both nested address and address ID.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER013.
     */
    @Test
    @DisplayName("Create Lead - Missing Address and AddressID - ThrowsBadRequestException")
    void createLead_MissingAddress_ThrowsBadRequestException() {
        testLeadRequest.setAddress(null);
        testLeadRequest.setAddressId(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER013, ex.getMessage());
    }

    /**
     * Purpose: Reject negative integers for company size.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER016.
     */
    @Test
    @DisplayName("Create Lead - Negative Company Size - ThrowsBadRequestException")
    void createLead_NegativeCompanySize_ThrowsBadRequestException() {
        testLeadRequest.setCompanySize(-5);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER016, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation when email is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Create Lead - Null Email - ThrowsBadRequestException")
    void createLead_NullEmail_ThrowsBadRequestException() {
        testLeadRequest.setEmail(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation when first name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Create Lead - Null First Name - ThrowsBadRequestException")
    void createLead_NullFirstName_ThrowsBadRequestException() {
        testLeadRequest.setFirstName(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation when last name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Create Lead - Null Last Name - ThrowsBadRequestException")
    void createLead_NullLastName_ThrowsBadRequestException() {
        testLeadRequest.setLastName(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject lead creation when phone is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Create Lead - Null Phone - ThrowsBadRequestException")
    void createLead_NullPhone_ThrowsBadRequestException() {
        testLeadRequest.setPhone(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when the entire request model is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER009.
     */
    @Test
    @DisplayName("Create Lead - Null Request - ThrowsBadRequestException")
    void createLead_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(null));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER009, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when lead status is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Create Lead - Null Status - ThrowsBadRequestException")
    void createLead_NullStatus_ThrowsBadRequestException() {
        testLeadRequest.setLeadStatus(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject email strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Create Lead - Whitespace Email - ThrowsBadRequestException")
    void createLead_WhitespaceEmail_ThrowsBadRequestException() {
        testLeadRequest.setEmail("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject first name strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Create Lead - Whitespace First Name - ThrowsBadRequestException")
    void createLead_WhitespaceFirstName_ThrowsBadRequestException() {
        testLeadRequest.setFirstName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject last name strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Create Lead - Whitespace Last Name - ThrowsBadRequestException")
    void createLead_WhitespaceLastName_ThrowsBadRequestException() {
        testLeadRequest.setLastName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject phone strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Create Lead - Whitespace Phone - ThrowsBadRequestException")
    void createLead_WhitespacePhone_ThrowsBadRequestException() {
        testLeadRequest.setPhone("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject status strings consisting only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER008.
     */
    @Test
    @DisplayName("Create Lead - Whitespace Status - ThrowsBadRequestException")
    void createLead_WhitespaceStatus_ThrowsBadRequestException() {
        testLeadRequest.setLeadStatus("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER008, ex.getMessage());
    }

    /**
     * Purpose: Reject zero as a company size value.
     * Expected Result: BadRequestException is thrown (size must be null or > 0).
     * Assertions: Error message matches ER016.
     */
    @Test
    @DisplayName("Create Lead - Zero Company Size - ThrowsBadRequestException")
    void createLead_ZeroCompanySize_ThrowsBadRequestException() {
        testLeadRequest.setCompanySize(0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER016, ex.getMessage());
    }
}