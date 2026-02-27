package com.example.SpringApi.ServiceTests.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Unit tests for UserService.fetchUsersInCarrierInBatches method. */
@DisplayName("UserService - FetchUsersInCarrierInBatches Tests")
class FetchUsersInCarrierInBatchesTest extends UserServiceTestBase {

  // Total Tests: 21
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify filtering users by address ID. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by AddressId - Success")
  void fetchUsersInCarrierInBatches_filterByAddressId_success() {
    // Arrange
    // Act & Assert
    testNumberFilter("addressId", "equals", "100");
  }

  /**
   * Purpose: Verify filtering users by email. Expected Result: Method completes without exception.
   * Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by Email - Success")
  void fetchUsersInCarrierInBatches_filterByEmail_success() {
    // Arrange
    // Act & Assert
    testStringFilter("email", "endsWith", "@example.com");
  }

  /**
   * Purpose: Verify filtering users by email confirmed status. Expected Result: Method completes
   * without exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by EmailConfirmed - Success")
  void fetchUsersInCarrierInBatches_filterByEmailConfirmed_success() {
    // Arrange
    // Act & Assert
    testBooleanFilter("emailConfirmed", "is", "true");
  }

  /**
   * Purpose: Verify filtering users by first name. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by FirstName - Success")
  void fetchUsersInCarrierInBatches_filterByFirstName_success() {
    // Arrange
    // Act & Assert
    testStringFilter("firstName", "contains", "John");
  }

  /**
   * Purpose: Verify filtering users by deleted status. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by IsDeleted - Success")
  void fetchUsersInCarrierInBatches_filterByIsDeleted_success() {
    // Arrange
    // Act & Assert
    testBooleanFilter("isDeleted", "is", "false");
  }

  /**
   * Purpose: Verify filtering users by last name. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by LastName - Success")
  void fetchUsersInCarrierInBatches_filterByLastName_success() {
    // Arrange
    // Act & Assert
    testStringFilter("lastName", "startsWith", "Doe");
  }

  /**
   * Purpose: Verify filtering users by locked status. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by Locked - Success")
  void fetchUsersInCarrierInBatches_filterByLocked_success() {
    // Arrange
    // Act & Assert
    testBooleanFilter("locked", "is", "false");
  }

  /**
   * Purpose: Verify filtering users by login attempts. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by LoginAttempts - Success")
  void fetchUsersInCarrierInBatches_filterByLoginAttempts_success() {
    // Arrange
    // Act & Assert
    testNumberFilter("loginAttempts", ">", "0");
  }

  /**
   * Purpose: Verify filtering users by login name. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by LoginName - Success")
  void fetchUsersInCarrierInBatches_filterByLoginName_success() {
    // Arrange
    // Act & Assert
    testStringFilter("loginName", "equals", "admin@example.com");
  }

  /**
   * Purpose: Verify filtering users by phone number. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by Phone - Success")
  void fetchUsersInCarrierInBatches_filterByPhone_success() {
    // Arrange
    // Act & Assert
    testStringFilter("phone", "contains", "555");
  }

  /**
   * Purpose: Verify filtering users by role. Expected Result: Method completes without exception.
   * Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by Role - Success")
  void fetchUsersInCarrierInBatches_filterByRole_success() {
    // Arrange
    // Act & Assert
    testStringFilter("role", "equals", "ADMIN");
  }

  /**
   * Purpose: Verify filtering users by user ID. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Filter by UserId - Success")
  void fetchUsersInCarrierInBatches_filterByUserId_success() {
    // Arrange
    // Act & Assert
    testNumberFilter("userId", "equals", "1");
  }

  /**
   * Purpose: Verify logic operator AND works correctly. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Logic Operator AND - Success")
  void fetchUsersInCarrierInBatches_logicOperatorAND_success() {
    // Arrange
    // Act & Assert
    testLogicOperator("AND");
  }

  /**
   * Purpose: Verify logic operator OR works correctly. Expected Result: Method completes without
   * exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Logic Operator OR - Success")
  void fetchUsersInCarrierInBatches_logicOperatorOR_success() {
    // Arrange
    // Act & Assert
    testLogicOperator("OR");
  }

  /**
   * Purpose: Verify valid pagination parameters work correctly. Expected Result: Method completes
   * without exception. Assertions: assertDoesNotThrow()
   */
  @Test
  @DisplayName("fetchUsersInBatches - Valid Pagination - Success")
  void fetchUsersInCarrierInBatches_validPagination_success() {
    // Arrange
    UserRequestModel request = new UserRequestModel();
    request.setStart(0);
    request.setEnd(20);

    stubUserFilterQueryBuilderFindPaginatedEntities(new PageImpl<>(Arrays.asList(testUser)));

    // Act & Assert
    assertDoesNotThrow(() -> userService.fetchUsersInCarrierInBatches(request));
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify invalid column name throws BadRequestException. Expected Result:
   * BadRequestException with invalid column message. Assertions: assertThrows, assertTrue
   */
  @Test
  @DisplayName("fetchUsersInBatches - Invalid Column - Throws BadRequestException")
  void fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException() {
    // Arrange
    UserRequestModel request = createBasicPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("invalidColumn");
    filter.setOperator("equals");
    filter.setValue("test");
    request.setFilters(Arrays.asList(filter));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> userService.fetchUsersInCarrierInBatches(request));

    // Assert
    assertNotNull(ex.getMessage());
    assertTrue(ex.getMessage().contains(filter.getColumn()));
  }

  /**
   * Purpose: Verify invalid logic operator throws BadRequestException. Expected Result:
   * BadRequestException with invalid logic operator message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("fetchUsersInBatches - Invalid Logic Operator - Throws BadRequestException")
  void fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException() {
    // Arrange
    UserRequestModel request = createBasicPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter1 =
        new PaginationBaseRequestModel.FilterCondition();
    filter1.setColumn("firstName");
    filter1.setOperator("equals");
    filter1.setValue("test");
    PaginationBaseRequestModel.FilterCondition filter2 =
        new PaginationBaseRequestModel.FilterCondition();
    filter2.setColumn("lastName");
    filter2.setOperator("equals");
    filter2.setValue("test");
    request.setFilters(Arrays.asList(filter1, filter2));
    request.setLogicOperator("INVALID");

    stubUserFilterQueryBuilderGetColumnType("firstName", "string");
    stubUserFilterQueryBuilderGetColumnType("lastName", "string");

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> userService.fetchUsersInCarrierInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR, ex.getMessage());
  }

  /**
   * Purpose: Verify invalid operator throws BadRequestException. Expected Result:
   * BadRequestException with invalid operator message. Assertions: assertThrows, assertTrue
   */
  @Test
  @DisplayName("fetchUsersInBatches - Invalid Operator - Throws BadRequestException")
  void fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException() {
    // Arrange
    UserRequestModel request = createBasicPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("firstName");
    filter.setOperator("invalidOp");
    filter.setValue("test");
    request.setFilters(Arrays.asList(filter));

    stubUserFilterQueryBuilderGetColumnType("firstName", "string");

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> userService.fetchUsersInCarrierInBatches(request));

    // Assert
    assertNotNull(ex.getMessage());
    assertTrue(ex.getMessage().contains(filter.getOperator()));
  }

  /**
   * Purpose: Verify invalid pagination parameters throw BadRequestException. Expected Result:
   * BadRequestException with invalid pagination message. Assertions: assertThrows, assertEquals
   */
  @Test
  @DisplayName("fetchUsersInBatches - Invalid Pagination - Throws BadRequestException")
  void fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException() {
    // Arrange
    UserRequestModel request = new UserRequestModel();
    request.setStart(10);
    request.setEnd(5);

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> userService.fetchUsersInCarrierInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify controller handles unauthorized access via HTTP status. Expected Result: HTTP
   * UNAUTHORIZED status returned. Assertions: assertEquals(HttpStatus.UNAUTHORIZED,
   * response.getStatusCode()), verify @PreAuthorize annotation.
   */
  @Test
  @DisplayName("fetchUsersInBatches - Controller permission forbidden")
  void fetchUsersInCarrierInBatches_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    stubMockUserServiceFetchUsersInCarrierInBatchesThrowsUnauthorized(null);
    Method method =
        UserController.class.getMethod("fetchUsersInCarrierInBatches", UserRequestModel.class);

    // Act
    ResponseEntity<?> response =
        userControllerWithMock.fetchUsersInCarrierInBatches(new UserRequestModel());
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(
        annotation,
        "@PreAuthorize annotation should be present on fetchUsersInCarrierInBatches method");
    assertTrue(
        annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
        "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service method is called.
   * Assertions: verify, HttpStatus.OK
   */
  @Test
  @DisplayName("fetchUsersInBatches - Controller delegates to service")
  void fetchUsersInCarrierInBatches_withValidRequest_delegatesToService() {
    // Arrange
    UserRequestModel request = new UserRequestModel();
    request.setStart(0);
    request.setEnd(10);
    stubMockUserServiceFetchUsersInCarrierInBatches(null);

    // Act
    ResponseEntity<?> response = userControllerWithMock.fetchUsersInCarrierInBatches(request);

    // Assert
    verify(mockUserService, times(1)).fetchUsersInCarrierInBatches(request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

