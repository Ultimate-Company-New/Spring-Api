package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - Get User By Email functionality.
 * 
 * Tests: 9
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@DisplayName("UserService - Get User By Email Tests")
class GetUserByEmailTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserByEmail - Verify @PreAuthorize Annotation")
    void getUserByEmail_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("getUserByEmail", String.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserByEmail method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    @Test
    @DisplayName("getUserByEmail - Controller delegates to service")
    void getUserByEmail_WithValidEmail_DelegatesToService() {
        UserController controller = new UserController(userService);
        String email = "test@example.com";
        UserResponseModel mockResponse = new UserResponseModel();
        when(userService.getUserByEmail(email)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getUserByEmail(email);

        verify(userService).getUserByEmail(email);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS Tests
    // ========================================

    /**
     * Purpose: Verify case sensitivity in email lookup.
     * Expected Result: Returns user when email matches.
     * Assertions: assertNotNull(result);
     */
    @Test
    @DisplayName("Get User By Email - Case Sensitive - Returns user")
    void getUserByEmail_CaseSensitive_ReturnsUser() {
        when(userRepository.findByEmailWithAllRelations(eq("TEST@EXAMPLE.COM"), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserByEmail("TEST@EXAMPLE.COM");

        assertNotNull(result);
    }

    /**
     * Purpose: Verify repository is called exactly once.
     * Expected Result: findByEmailWithAllRelations called once.
     * Assertions: verify(userRepository, times(1)).findByEmailWithAllRelations(...);
     */
    @Test
    @DisplayName("Get User By Email - Repository called once")
    void getUserByEmail_RepositoryCalledOnce() {
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

        userService.getUserByEmail(TEST_EMAIL);

        verify(userRepository, times(1)).findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong());
    }

    /**
     * Purpose: Verify permissions are returned with email lookup.
     * Expected Result: Permissions list is populated.
     * Assertions: assertNotNull(result.getPermissions());
     */
    @Test
    @DisplayName("Get User By Email - Success - Returns permissions")
    void getUserByEmail_Success_ReturnsPermissions() {
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());
    }

    /**
     * Purpose: Verify successful user retrieval by email.
     * Expected Result: UserResponseModel is returned with correct data.
     * Assertions: assertNotNull(result); assertEquals(TEST_EMAIL, result.getEmail());
     */
    @Test
    @DisplayName("Get User By Email - Success - Returns user")
    void getUserByEmail_Success_ReturnsUser() {
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserByEmail(TEST_EMAIL);

        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USER_ID, result.getUserId());
    }

    // ========================================
    // FAILURE Tests
    // ========================================

    /**
     * Purpose: Verify non-existent email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By Email - Email Not Found - Throws NotFoundException")
    void getUserByEmail_EmailNotFound_ThrowsNotFoundException() {
        when(userRepository.findByEmailWithAllRelations(eq(TEST_EMAIL), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(TEST_EMAIL));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify empty email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By Email - Empty Email - Throws NotFoundException")
    void getUserByEmail_EmptyEmail_ThrowsNotFoundException() {
        when(userRepository.findByEmailWithAllRelations(eq(""), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(""));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid email format throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By Email - Invalid Format - Throws NotFoundException")
    void getUserByEmail_InvalidFormat_ThrowsNotFoundException() {
        when(userRepository.findByEmailWithAllRelations(eq("not-an-email"), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("not-an-email"));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify null email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By Email - Null Email - Throws NotFoundException")
    void getUserByEmail_NullEmail_ThrowsNotFoundException() {
        when(userRepository.findByEmailWithAllRelations(isNull(), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(null));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By Email - Whitespace Email - Throws NotFoundException")
    void getUserByEmail_WhitespaceEmail_ThrowsNotFoundException() {
        when(userRepository.findByEmailWithAllRelations(eq("   "), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("   "));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }
}
