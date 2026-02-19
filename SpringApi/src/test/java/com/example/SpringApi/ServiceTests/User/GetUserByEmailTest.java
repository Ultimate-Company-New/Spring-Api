package com.example.SpringApi.ServiceTests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.getUserByEmail method.
 * 
 */
@DisplayName("UserService - GetUserByEmail Tests")
class GetUserByEmailTest extends UserServiceTestBase {

    // Total Tests: 11
    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify case sensitivity in email lookup logic.
     * Expected Result: Returns user when email matches.
     * Assertions: assertNotNull, verify
     */
    @Test
    @DisplayName("getUserByEmail - Success - Case Sensitive")
    void getUserByEmail_caseSensitive_returnsUser() {
        // Arrange
        String upperEmail = "TEST@EXAMPLE.COM";
        stubUserRepositoryFindByEmailWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserByEmail(upperEmail);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmailWithAllRelations(eq(upperEmail), anyLong());
    }

    /**
     * Purpose: Verify repository is called exactly once.
     * Expected Result: findByEmailWithAllRelations called once.
     * Assertions: verify
     */
    @Test
    @DisplayName("getUserByEmail - Success - Repository Success")
    void getUserByEmail_repository_success() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(testUser);

        // Act
        userService.getUserByEmail(TEST_EMAIL);

        // Assert
        verify(userRepository, times(1)).findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong());
    }

    /**
     * Purpose: Verify permissions are returned with email lookup.
     * Expected Result: Permissions list is populated.
     * Assertions: assertNotNull, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Success - Returns Permissions")
    void getUserByEmail_success_returnsPermissions() {
        // Arrange
        com.example.SpringApi.Models.DatabaseModels.Permission p1 = new com.example.SpringApi.Models.DatabaseModels.Permission();
        p1.setPermissionId(1L);
        com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping m1 = new com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping();
        m1.setPermission(p1);

        com.example.SpringApi.Models.DatabaseModels.Permission p2 = new com.example.SpringApi.Models.DatabaseModels.Permission();
        p2.setPermissionId(2L);
        com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping m2 = new com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping();
        m2.setPermission(p2);

        testUser.setUserClientPermissionMappings(new HashSet<>());
        testUser.getUserClientPermissionMappings().add(m1);
        testUser.getUserClientPermissionMappings().add(m2);

        stubUserRepositoryFindByEmailWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());
    }

    /**
     * Purpose: Verify successful user retrieval by email.
     * Expected Result: UserResponseModel is returned with correct data.
     * Assertions: assertNotNull, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Success - Returns User")
    void getUserByEmail_success_returnsUser() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USER_ID, result.getUserId());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify non-existent email throws NotFoundException.
     * Expected Result: NotFoundException with InvalidEmail message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Failure - Email Not Found")
    void getUserByEmail_emailNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(TEST_EMAIL));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_EMAIL, ex.getMessage());
    }

    /**
     * Purpose: Verify empty email throws NotFoundException.
     * Expected Result: NotFoundException with InvalidEmail message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Failure - Empty Email")
    void getUserByEmail_emptyEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(""));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_EMAIL, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid email format throws NotFoundException.
     * Expected Result: NotFoundException with InvalidEmail message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Failure - Invalid Format")
    void getUserByEmail_invalidFormat_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("not-an-email"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_EMAIL, ex.getMessage());
    }

    /**
     * Purpose: Verify null email throws NotFoundException.
     * Expected Result: NotFoundException with InvalidEmail message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Failure - Null Email")
    void getUserByEmail_nullEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(null));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_EMAIL, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace email throws NotFoundException.
     * Expected Result: NotFoundException with InvalidEmail message.
     * Assertions: assertThrows, assertEquals
     */
    @Test
    @DisplayName("getUserByEmail - Failure - Whitespace Email")
    void getUserByEmail_whitespaceEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("   "));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_EMAIL, ex.getMessage());
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
    @DisplayName("getUserByEmail - Controller permission forbidden")
    void getUserByEmail_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        stubMockUserServiceGetUserByEmailThrowsUnauthorized(null);
        Method method = UserController.class.getMethod("getUserByEmail", String.class);

        // Act
        ResponseEntity<?> response = userControllerWithMock.getUserByEmail(TEST_EMAIL);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "getUserByEmail method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("getUserByEmail - Controller delegates to service")
    void getUserByEmail_withValidEmail_delegatesToService() {
        // Arrange
        String email = "test@example.com";
        stubMockUserServiceGetUserByEmail(email, new UserResponseModel());

        // Act
        ResponseEntity<?> response = userControllerWithMock.getUserByEmail(email);

        // Assert
        verify(mockUserService, times(1)).getUserByEmail(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
