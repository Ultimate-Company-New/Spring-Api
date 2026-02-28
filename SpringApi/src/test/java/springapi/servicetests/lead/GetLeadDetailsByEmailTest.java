package springapi.ServiceTests.Lead;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.LeadController;
import springapi.exceptions.NotFoundException;
import springapi.models.Authorizations;
import springapi.models.responsemodels.LeadResponseModel;

/** Unit tests for LeadService.getLeadDetailsByEmail() method. */
@DisplayName("Get Lead Details By Email Tests")
class GetLeadDetailsByEmailTest extends LeadServiceTestBase {

  // Total Tests: 5
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify retrieval by email returns a correctly mapped response.
   * Given: Valid email address.
   * When: getLeadDetailsByEmail is called.
   * Then: Response contains the correct email address.
   */
  @Test
  @DisplayName("getLeadDetailsByEmail_unit_basic_success")
  void getLeadDetailsByEmail_unit_basic_success() {
    // Arrange
    stubLeadRepositoryFindLeadWithDetailsByEmail(DEFAULT_EMAIL, TEST_CLIENT_ID, testLead);

    // Act
    LeadResponseModel result = leadService.getLeadDetailsByEmail(DEFAULT_EMAIL);

    // Assert
    assertNotNull(result);
    assertEquals(DEFAULT_EMAIL, result.getEmail());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Reject requests for an empty email string.
   * Given: Empty string as email.
   * When: getLeadDetailsByEmail is called.
   * Then: NotFoundException is thrown.
   */
  @Test
  @DisplayName("getLeadDetailsByEmail_unit_emptyEmail_notFound")
  void getLeadDetailsByEmail_unit_emptyEmail_notFound() {
    // Arrange
    stubLeadRepositoryFindLeadWithDetailsByEmail("", TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> leadService.getLeadDetailsByEmail(""));
    assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Reject requests for emails that do not exist in the database.
   * Given: Non-existent email.
   * When: getLeadDetailsByEmail is called.
   * Then: NotFoundException is thrown.
   */
  @Test
  @DisplayName("getLeadDetailsByEmail_unit_notFound_failure")
  void getLeadDetailsByEmail_unit_notFound_failure() {
    // Arrange
    stubLeadRepositoryFindLeadWithDetailsByEmail(anyString(), anyLong(), null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> leadService.getLeadDetailsByEmail("unknown@example.com"));
    assertEquals(ErrorMessages.LEAD_NOT_FOUND, ex.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify controller calls service when authorization passes.
   * Given: Valid email address.
   * When: Controller getLeadDetailsByEmail is called.
   * Then: Service is invoked once and HTTP 200 returned.
   */
  @Test
  @DisplayName("getLeadDetailsByEmail_controller_basic_success")
  void getLeadDetailsByEmail_controller_basic_success() {
    // Arrange
    LeadController controller = new LeadController(leadServiceMock);
    stubLeadServiceGetLeadDetailsByEmail(DEFAULT_EMAIL, new LeadResponseModel(testLead));

    // Act
    ResponseEntity<?> response = controller.getLeadDetailsByEmail(DEFAULT_EMAIL);

    // Assert
    verify(leadServiceMock, times(1)).getLeadDetailsByEmail(DEFAULT_EMAIL);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /*
   * Purpose: Verify @PreAuthorize annotation is declared correctly on the
   * controller.
   * Given: LeadController class.
   * When: Checking getLeadDetailsByEmail method annotations.
   * Then: @PreAuthorize exists with VIEW_LEADS_PERMISSION.
   */
  @Test
  @DisplayName("getLeadDetailsByEmail_controller_permission_configured")
  void getLeadDetailsByEmail_controller_permission_configured() throws NoSuchMethodException {
    // Arrange
    var method = LeadController.class.getMethod("getLeadDetailsByEmail", String.class);
    LeadController controller = new LeadController(leadServiceMock);
    stubLeadServiceGetLeadDetailsByEmail(DEFAULT_EMAIL, new LeadResponseModel(testLead));

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = controller.getLeadDetailsByEmail(DEFAULT_EMAIL);

    // Assert
    assertNotNull(preAuthorizeAnnotation);
    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.VIEW_LEADS_PERMISSION + "')";
    assertEquals(expectedPermission, preAuthorizeAnnotation.value());
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
