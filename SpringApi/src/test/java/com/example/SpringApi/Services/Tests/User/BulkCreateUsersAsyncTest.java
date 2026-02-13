package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.ErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.bulkCreateUsersAsync method.
 * 
 */
@DisplayName("UserService - BulkCreateUsersAsync Tests")
class BulkCreateUsersAsyncTest extends UserServiceTestBase {

    // Total Tests: 10
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify bulk creation with all valid users does not throw.
     * Expected Result: Method completes without exception.
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - All Valid")
    void bulkCreateUsersAsync_allValid_success() {
        // Arrange
        List<UserRequestModel> users = createValidUserList(3);
        stubPasswordHelper();
        stubStandardCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify bulk creation with duplicate emails does not throw.
     * Expected Result: Method completes without exception (graceful handling).
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - Duplicate Email")
    void bulkCreateUsersAsync_duplicateEmail_success() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();
        users.add(createValidUserRequest("new@test.com"));
        users.add(createValidUserRequest("existing@test.com"));

        stubPasswordHelper();
        stubStandardCreateUserMocks();

        stubUserRepositoryFindByLoginName("existing@test.com", testUser);
        stubUserRepositoryFindByLoginName("new@test.com", null);

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify large batch bulk creation does not throw.
     * Expected Result: Method completes without exception.
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - Large Batch")
    void bulkCreateUsersAsync_largeBatch_success() {
        // Arrange
        List<UserRequestModel> users = createValidUserList(10);
        stubPasswordHelper();
        stubStandardCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify bulk creation with log activity.
     * Expected Result: Method completes without exception.
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - Logs Operation")
    void bulkCreateUsersAsync_logsOperation_success() {
        // Arrange
        List<UserRequestModel> users = createValidUserList(2);
        stubPasswordHelper();
        stubStandardCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify bulk creation handles partial success without throwing.
     * Expected Result: Method completes without exception.
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - Partial Success")
    void bulkCreateUsersAsync_partialSuccess_success() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();
        users.add(createValidUserRequest("valid@test.com"));
        users.add(new UserRequestModel());

        stubPasswordHelper();
        stubStandardCreateUserMocks();
        stubUserRepositoryFindByLoginName("valid@test.com", null);

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify single user bulk creation does not throw.
     * Expected Result: Method completes without exception.
     * Assertions: assertDoesNotThrow()
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Success - Single User")
    void bulkCreateUsersAsync_singleUser_success() {
        // Arrange
        List<UserRequestModel> users = createValidUserList(1);
        stubPasswordHelper();
        stubStandardCreateUserMocks();

        // Act & Assert
        assertDoesNotThrow(
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify empty list throws BadRequestException.
     * Expected Result: BadRequestException with ERROR_BAD_REQUEST.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Failure - Empty List")
    void bulkCreateUsersAsync_emptyList_throwsBadRequestException() {
        // Arrange
        List<UserRequestModel> users = new ArrayList<>();

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.bulkCreateUsersAsync(users, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User"),
                exception.getMessage());
    }

    /**
     * Purpose: Verify null list throws BadRequestException.
     * Expected Result: BadRequestException with ERROR_BAD_REQUEST.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Failure - Null List")
    void bulkCreateUsersAsync_nullList_throwsBadRequestException() {
        // Arrange

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.bulkCreateUsersAsync(null, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User"),
                exception.getMessage());
    }

    // ========================================
    // PERMISSION TESTS
    // ========================================

    /**
     * Purpose: Verify controller handles unauthorized access via HTTP status.
     * Expected Result: HTTP UNAUTHORIZED status returned and @PreAuthorize
     * verified.
     * Assertions: assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode()),
     * assertNotNull, assertTrue
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Controller permission forbidden")
    void bulkCreateUsersAsync_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        stubMockUserServiceGetUserId(TEST_USER_ID);
        stubMockUserServiceGetUser(TEST_LOGIN_NAME);
        stubMockUserServiceGetClientId(TEST_CLIENT_ID);
        stubMockUserServiceBulkCreateUsersAsyncThrowsUnauthorized(null);
        Method method = UserController.class.getMethod("bulkCreateUsers", List.class);

        // Act
        ResponseEntity<?> response = userControllerWithMock.bulkCreateUsers(new ArrayList<>());
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreateUsers method");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify
     */
    @Test
    @DisplayName("bulkCreateUsersAsync - Controller delegates to service")
    void bulkCreateUsersAsync_withValidRequest_delegatesToService() {
        // Arrange
        List<UserRequestModel> users = createValidUserList(1);

        // Act
        stubMockUserServiceGetUserId(TEST_USER_ID);
        stubMockUserServiceGetUser(TEST_LOGIN_NAME);
        stubMockUserServiceGetClientId(TEST_CLIENT_ID);
        stubMockUserServiceBulkCreateUsersAsync(users);
        ResponseEntity<?> response = userControllerWithMock.bulkCreateUsers(users);

        // Assert
        verify(mockUserService, times(1)).bulkCreateUsersAsync(eq(users), any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
