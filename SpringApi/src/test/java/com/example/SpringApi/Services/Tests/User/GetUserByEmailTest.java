package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Total Tests: 9
@DisplayName("UserService - Get User By Email Tests")
class GetUserByEmailTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with VIEW_USER_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("getUserByEmail - Verify @PreAuthorize Annotation")
    void getUserByEmail_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("getUserByEmail", String.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "getUserByEmail method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).getUserByEmail(email);
     */
    @Test
    @DisplayName("getUserByEmail - Controller delegates to service")
    void getUserByEmail_WithValidEmail_DelegatesToService() {
        // Arrange
        String email = "test@example.com";
        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        doReturn(new UserResponseModel()).when(mockUserService).getUserByEmail(email);

        // Act
        localController.getUserByEmail(email);

        // Assert
        verify(mockUserService, times(1)).getUserByEmail(email);
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify case sensitivity in email lookup logic (verify service passes
     * string).
     * Expected Result: Returns user when email matches.
     * Assertions: assertNotNull(result); verify repo called with uppercase email.
     */
    @Test
    @DisplayName("getUserByEmail - Case Sensitive - Returns user")
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
     * Assertions: verify(userRepository,
     * times(1)).findByEmailWithAllRelations(...);
     */
    @Test
    @DisplayName("getUserByEmail - Repository called once")
    void getUserByEmail_repositoryCalledOnce() {
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
     * Assertions: assertNotNull(result.getPermissions());
     */
    @Test
    @DisplayName("getUserByEmail - Success - Returns permissions")
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
     * Assertions: assertNotNull(result); assertEquals(TEST_EMAIL,
     * result.getEmail());
     */
    @Test
    @DisplayName("getUserByEmail - Success - Returns user")
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
     * Expected Result: NotFoundException with "Invalid Email" message.
     */
    @Test
    @DisplayName("getUserByEmail - Email Not Found - Throws NotFoundException")
    void getUserByEmail_emailNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(TEST_EMAIL));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify empty email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     */
    @Test
    @DisplayName("getUserByEmail - Empty Email - Throws NotFoundException")
    void getUserByEmail_emptyEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(""));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid email format throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     */
    @Test
    @DisplayName("getUserByEmail - Invalid Format - Throws NotFoundException")
    void getUserByEmail_invalidFormat_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("not-an-email"));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify null email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     */
    @Test
    @DisplayName("getUserByEmail - Null Email - Throws NotFoundException")
    void getUserByEmail_nullEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(null));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace email throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid Email" message.
     */
    @Test
    @DisplayName("getUserByEmail - Whitespace Email - Throws NotFoundException")
    void getUserByEmail_whitespaceEmail_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByEmailWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail("   "));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidEmail, ex.getMessage());
    }
}
