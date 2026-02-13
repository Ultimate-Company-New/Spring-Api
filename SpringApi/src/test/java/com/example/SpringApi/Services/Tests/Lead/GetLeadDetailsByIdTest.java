package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Controllers.LeadController;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LeadService.getLeadDetailsById() method.
 */
@DisplayName("Get Lead Details By ID Tests")
class GetLeadDetailsByIdTest extends LeadServiceTestBase {


    // Total Tests: 9
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify lead details retrieval with all fields populated.
     * Given: Valid Lead ID with complete data in repository.
     * When: getLeadDetailsById is called.
     * Then: Response model matches the entity data.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_allFieldsPopulated_success")
    void getLeadDetailsById_unit_allFieldsPopulated_success() {
        // Arrange
        testLead.setLeadId(DEFAULT_LEAD_ID);
        testLead.setFirstName("John");
        testLead.setLastName("Doe");
        testLead.setEmail(DEFAULT_EMAIL);
        testLead.setPhone("555-0100");
        testLead.setCompany("Tech Corp");
        testLead.setTitle("Manager");
        testLead.setLeadStatus("New");
        stubLeadRepositoryFindLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    /*
     * Purpose: Verify basic retrieval by ID succeeds for a valid lead.
     * Given: Valid Lead ID.
     * When: getLeadDetailsById is called.
     * Then: Success returns lead details.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_basic_success")
    void getLeadDetailsById_unit_basic_success() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(DEFAULT_LEAD_ID, TEST_CLIENT_ID, testLead);

        // Act
        LeadResponseModel result = leadService.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_LEAD_ID, result.getLeadId());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Reject retrieval attempts for the maximum long ID if not found.
     * Given: Long.MAX_VALUE lead ID.
     * When: getLeadDetailsById is called.
     * Then: NotFoundException is thrown with correct message.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_maxLongId_notFound")
    void getLeadDetailsById_unit_maxLongId_notFound() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     * Purpose: Reject retrieval attempts for the minimum long ID if not found.
     * Given: Long.MIN_VALUE lead ID.
     * When: getLeadDetailsById is called.
     * Then: NotFoundException is thrown.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_minLongId_notFound")
    void getLeadDetailsById_unit_minLongId_notFound() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(Long.MIN_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     * Purpose: Reject negative lead IDs.
     * Given: Negative lead ID.
     * When: getLeadDetailsById is called.
     * Then: NotFoundException is thrown.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_negativeId_notFound")
    void getLeadDetailsById_unit_negativeId_notFound() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(-1L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     * Purpose: Reject retrieval attempts when a lead ID is not found.
     * Given: Non-existent Lead ID.
     * When: getLeadDetailsById is called.
     * Then: NotFoundException is thrown.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_notFound_failure")
    void getLeadDetailsById_unit_notFound_failure() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(anyLong(), anyLong(), null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> leadService.getLeadDetailsById(DEFAULT_LEAD_ID));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     * Purpose: Reject zero as a lead ID.
     * Given: Lead ID 0.
     * When: getLeadDetailsById is called.
     * Then: NotFoundException is thrown.
     */
    @Test
    @DisplayName("getLeadDetailsById_unit_zeroId_notFound")
    void getLeadDetailsById_unit_zeroId_notFound() {
        // Arrange
        stubLeadRepositoryFindLeadWithDetailsById(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsById(0L));
        assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify controller calls service when authorization passes.
     * Given: Valid Lead ID.
     * When: Controller getLeadDetailsById is called.
     * Then: Service is invoked once and HTTP 200 returned.
     */
    @Test
    @DisplayName("getLeadDetailsById_controller_basic_success")
    void getLeadDetailsById_controller_basic_success() {
        // Arrange
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceGetLeadDetailsById(DEFAULT_LEAD_ID, new LeadResponseModel(testLead));

        // Act
        ResponseEntity<?> response = controller.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        verify(leadServiceMock, times(1)).getLeadDetailsById(DEFAULT_LEAD_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /*
     * Purpose: Verify @PreAuthorize annotation is declared correctly on the
     * controller.
     * Given: LeadController class.
     * When: Checking getLeadDetailsById method annotations.
     * Then: @PreAuthorize exists with VIEW_LEADS_PERMISSION.
     */
    @Test
    @DisplayName("getLeadDetailsById_controller_permission_configured")
    void getLeadDetailsById_controller_permission_configured() throws NoSuchMethodException {
        // Arrange
        var method = LeadController.class.getMethod("getLeadDetailsById", Long.class);
        LeadController controller = new LeadController(leadServiceMock);
        stubLeadServiceGetLeadDetailsById(DEFAULT_LEAD_ID, new LeadResponseModel(testLead));

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);
        ResponseEntity<?> response = controller.getLeadDetailsById(DEFAULT_LEAD_ID);

        // Assert
        assertNotNull(preAuthorizeAnnotation);
        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_LEADS_PERMISSION + "')";
        assertEquals(expectedPermission, preAuthorizeAnnotation.value());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
