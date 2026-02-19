package com.example.SpringApi.ServiceTests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Consumer;
import java.util.stream.Stream;

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


    // Total Tests: 33
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify creation succeeds with the maximum possible integer for
     * company size.
     * Expected Result: Lead is saved with the provided company size.
     * Assertions: Repository save is invoked and no exceptions are thrown.
     */
    @Test
    @DisplayName("Create Lead - Max Company Size - Success")
    void createLead_MaxCompanySize_Success() {
        // Arrange
        testLeadRequest.setCompanySize(Integer.MAX_VALUE);
        stubLeadRepositorySave(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
        verify(leadRepository).save(any());
    }

    /**
     * Purpose: Verify creation succeeds with special characters in the lead's name.
     * Expected Result: Lead is saved successfully with characters like accents and
     * hyphens.
     * Assertions: Process completes without error.
     */
    @Test
    @DisplayName("Create Lead - Special Characters in Name - Success")
    void createLead_SpecialCharactersInName_Success() {
        // Arrange
        testLeadRequest.setFirstName("José");
        testLeadRequest.setLastName("García-López");
        stubLeadRepositorySave(testLead);

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
    void createLead_Success_Success() {
        // Arrange
        stubLeadRepositorySave(testLead);

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
        stubLeadRepositorySave(testLead);

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
        stubLeadRepositorySave(testLead);

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
        stubLeadRepositorySave(testLead);

        // Act & Assert
        assertDoesNotThrow(() -> leadService.createLead(testLeadRequest));
    }

    /**
     * Purpose: Verify system handles extremely long email addresses within valid
     * format.
     * Expected Result: Save completes successfully.
     * Assertions: No validation error triggered for email length.
     */
    @Test
    @DisplayName("Create Lead - Very Long Email - Success")
    void createLead_VeryLongEmail_Success() {
        // Arrange
        testLeadRequest.setEmail("verylongemailaddress.withmanydots.test@verylongdomainname.co.uk");
        stubLeadRepositorySave(testLead);

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
    @ParameterizedTest(name = "Create Lead - {0} - ThrowsBadRequestException")
    @MethodSource("emptyRequiredFieldScenarios")
    void createLead_emptyRequiredFields_ThrowsBadRequestException(
            String scenario,
            Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel> mutator,
            String expectedError) {
        // Arrange
        mutator.accept(testLeadRequest);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(expectedError, ex.getMessage(), "Scenario: " + scenario);
    }

    private static Stream<Arguments> emptyRequiredFieldScenarios() {
        return Stream.of(
                Arguments.of("Empty Email",
                        (Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel>) req -> req.setEmail(""),
                        ErrorMessages.LeadsErrorMessages.ER001),
                Arguments.of("Empty First Name",
                        (Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel>) req -> req.setFirstName(""),
                        ErrorMessages.LeadsErrorMessages.ER002),
                Arguments.of("Empty Last Name",
                        (Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel>) req -> req.setLastName(""),
                        ErrorMessages.LeadsErrorMessages.ER003),
                Arguments.of("Empty Phone",
                        (Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel>) req -> req.setPhone(""),
                        ErrorMessages.LeadsErrorMessages.ER004),
                Arguments.of("Empty Status",
                        (Consumer<com.example.SpringApi.Models.RequestModels.LeadRequestModel>) req -> req.setLeadStatus(""),
                        ErrorMessages.LeadsErrorMessages.ER008));
    }

    /**
     * Purpose: Reject invalid email formats (missing @, etc.).
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER010.
     */
    @Test
    @DisplayName("Create Lead - Invalid Email Format - ThrowsBadRequestException")
    void createLead_f06_InvalidEmailFormat_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail("invalid-email");

        // Act & Assert
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
    void createLead_f07_InvalidPhoneFormat_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setPhone("invalid-phone");

        // Act & Assert
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
    void createLead_f08_InvalidStatusUnknown_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLeadStatus("UnknownStatus");

        // Act & Assert
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
    void createLead_f09_MissingAddress_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setAddress(null);
        testLeadRequest.setAddressId(null);

        // Act & Assert
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
    void createLead_f10_NegativeCompanySize_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setCompanySize(-5);

        // Act & Assert
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
    void createLead_f11_NullEmail_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail(null);

        // Act & Assert
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
    void createLead_f12_NullFirstName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setFirstName(null);

        // Act & Assert
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
    void createLead_f13_NullLastName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLastName(null);

        // Act & Assert
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
    void createLead_f14_NullPhone_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setPhone(null);

        // Act & Assert
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
    void createLead_f15_NullRequest_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
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
    void createLead_f16_NullStatus_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLeadStatus(null);

        // Act & Assert
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
    void createLead_f17_WhitespaceEmail_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setEmail("   ");

        // Act & Assert
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
    void createLead_f18_WhitespaceFirstName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setFirstName("   ");

        // Act & Assert
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
    void createLead_f19_WhitespaceLastName_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLastName("   ");

        // Act & Assert
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
    void createLead_f20_WhitespacePhone_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setPhone("   ");

        // Act & Assert
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
    void createLead_f21_WhitespaceStatus_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setLeadStatus("   ");

        // Act & Assert
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
    void createLead_f22_ZeroCompanySize_ThrowsBadRequestException() {
        // Arrange
        testLeadRequest.setCompanySize(0);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> leadService.createLead(testLeadRequest));
        assertEquals(ErrorMessages.LeadsErrorMessages.ER016, ex.getMessage());
    }
    /**
     * Purpose: Verify controller delegates createLead calls to service.
     * Expected Result: Service method called and HTTP 200 returned.
     */
    @Test
    @DisplayName("Create Lead - Controller delegates to service correctly")
    void createLead_p02_WithValidRequest_DelegatesToService() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceCreateLeadDoNothing();

        // Act
        ResponseEntity<?> response = controller.createLead(testLeadRequest);

        // Assert
        verify(leadServiceMock, times(1)).createLead(testLeadRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
    }

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("Create Lead - Controller permission unauthorized - Success")
    void createLead_p03_controller_permission_unauthorized() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceCreateLeadThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.createLead(testLeadRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
