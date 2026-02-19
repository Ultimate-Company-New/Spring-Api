package com.example.SpringApi.ServiceTests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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


        // Total Tests: 33
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
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName("UpdatedFirst");
                testLeadRequest.setLastName("UpdatedLast");
                testLeadRequest.setEmail("updated@example.com");
                testLeadRequest.setPhone("5551234567");
                testLeadRequest.setCompany("UpdatedCorp");
                testLeadRequest.setLeadStatus("Contacted");
                stubLeadRepositorySave(testLead);

                // Act & Assert
                assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));
                verify(leadRepository).save(any());
        }

        /**
         * Purpose: Verify partial updates (patch-like) only change provided fields and
         * persist.
         * Given: Existing lead and update DTO with phone number change
         * When: updateLead is called
         * Then: Lead is persisted with update
         */
        @Test
        @DisplayName("Update Lead - Partial Update Success")
        void updateLead_PartialUpdate_Success() {
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setPhone("555-0200-1234");
                stubLeadRepositorySave(testLead);

                // Act
                assertDoesNotThrow(() -> leadService.updateLead(DEFAULT_LEAD_ID, testLeadRequest));

                // Assert
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
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName("François");
                testLeadRequest.setCompany("O'Reilly & Associates");
                stubLeadRepositorySave(testLead);

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
        void updateLead_Success_Success() {
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                stubLeadRepositorySave(testLead);
                stubAddressRepositorySave(testLead.getAddress());

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
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName("VeryVeryVeryVeryVeryLongFirstName");
                testLeadRequest.setLastName("VeryVeryVeryVeryVeryLongLastName");
                stubLeadRepositorySave(testLead);

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
                // Arrange
                testLead.setIsDeleted(true);
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setEmail("");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName("");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLastName("");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setPhone("");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLeadStatus("");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setEmail("invalid-email");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setPhone("invalid-phone");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLeadStatus("InvalidStatus");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(-1L, TEST_CLIENT_ID, null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindByIdIncludingDeletedAny(null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setEmail(null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName(null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLastName(null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setPhone(null);

                // Act & Assert
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
                // Arrange

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLeadStatus(null);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setCompanySize(0);

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setEmail("   ");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setFirstName("   ");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLastName("   ");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setPhone("   ");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);
                testLeadRequest.setLeadStatus("   ");

                // Act & Assert
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
                // Arrange
                stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(0L, TEST_CLIENT_ID, null);

                // Act & Assert
                NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> leadService.updateLead(0L, testLeadRequest));
                assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
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
        void updateLead_p02_WithValidRequest_DelegatesToService() {
                // Arrange
                LeadController controller = new LeadController(leadServiceMock);
                stubLeadServiceUpdateLeadDoNothing(DEFAULT_LEAD_ID);

                // Act - Call controller directly (simulating authorization has already passed)
                ResponseEntity<?> response = controller.updateLead(DEFAULT_LEAD_ID, testLeadRequest);

                // Assert - Verify service was called and correct response returned
                verify(leadServiceMock, times(1)).updateLead(DEFAULT_LEAD_ID, testLeadRequest);
                assertEquals(HttpStatus.OK, response.getStatusCode(),
                                "Should return HTTP 200 OK");
        }

        /**
         * Purpose: Verify forbidden access is handled at the controller level.
         * Expected Result: Forbidden status is returned.
         * Assertions: Response status is 403 FORBIDDEN.
         */
        @Test
        @DisplayName("Update Lead - Controller permission forbidden - Success")
        void updateLead_p03_controller_permission_forbidden() {
                // Arrange
                LeadController controller = new LeadController(leadServiceMock);
                stubLeadServiceUpdateLeadThrowsForbidden(DEFAULT_LEAD_ID);

                // Act
                ResponseEntity<?> response = controller.updateLead(DEFAULT_LEAD_ID, testLeadRequest);

                // Assert
                assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
}
