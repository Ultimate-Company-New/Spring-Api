package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Services.UserService;

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
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService - Get User By ID functionality.
 * 
 * Tests: 8
 * 
 * @author SpringApi Team
 * @version 2.0
 * @since 2024-01-15
 */
@DisplayName("UserService - Get User By ID Tests")
class GetUserByIdTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserById - Verify @PreAuthorize Annotation")
    void getUserById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserController.class.getMethod("getUserById", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserById method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    @Test
    @DisplayName("getUserById - Controller delegates to service")
    void getUserById_WithValidId_DelegatesToService() {
        UserService mockUserService = mock(UserService.class);
        UserController controller = new UserController(mockUserService);
        Long userId = 1L;
        UserResponseModel mockResponse = new UserResponseModel();
        when(mockUserService.getUserById(userId)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getUserById(userId);

        verify(mockUserService).getUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS Tests
    // ========================================

    /**
     * Purpose: Verify user with no permissions returns empty or null permissions.
     * Expected Result: Empty or null permissions list.
     * Assertions: Permissions is null or empty.
     */
    @Test
    @DisplayName("Get User By ID - No Permissions - Returns empty or null permissions")
    void getUserById_NoPermissions_ReturnsEmptyPermissionsList() {
        testUser.setUserClientPermissionMappings(new HashSet<>());
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        assertNotNull(result);
        // Permissions can be null or empty depending on implementation
        assertTrue(result.getPermissions() == null || result.getPermissions().isEmpty());
    }

    /**
     * Purpose: Verify repository is called exactly once.
     * Expected Result: findByIdWithAllRelations called once.
     * Assertions: verify(userRepository, times(1)).findByIdWithAllRelations(...);
     */
    @Test
    @DisplayName("Get User By ID - Repository called once")
    void getUserById_RepositoryCalledOnce() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

        userService.getUserById(TEST_USER_ID);

        verify(userRepository, times(1)).findByIdWithAllRelations(eq(TEST_USER_ID), anyLong());
    }

    /**
     * Purpose: Verify user permissions are returned.
     * Expected Result: Permissions list is populated.
     * Assertions: assertNotNull(result.getPermissions());
     * assertEquals(2, result.getPermissions().size());
     */
    @Test
    @DisplayName("Get User By ID - Success - Returns permissions")
    void getUserById_Success_ReturnsPermissions() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());
    }

    /**
     * Purpose: Verify successful user retrieval by ID.
     * Expected Result: UserResponseModel is returned with correct data.
     * Assertions: assertNotNull(result); assertEquals(TEST_USER_ID,
     * result.getUserId());
     */
    @Test
    @DisplayName("Get User By ID - Success - Returns user with all details")
    void getUserById_Success_ReturnsUserWithDetails() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(testUser);

        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_LOGIN_NAME, result.getLoginName());
    }

    // ========================================
    // FAILURE Tests
    // ========================================

    /**
     * Purpose: Verify max long ID throws NotFoundException when not found.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By ID - Max Long ID - Throws NotFoundException")
    void getUserById_MaxLongId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(Long.MAX_VALUE), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(Long.MAX_VALUE));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By ID - Negative ID - Throws NotFoundException")
    void getUserById_NegativeId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(-1L), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(-1L));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By ID - User Not Found - Throws NotFoundException")
    void getUserById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(TEST_USER_ID), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(TEST_USER_ID));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     * Assertions: assertEquals(ErrorMessages.UserErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User By ID - Zero ID - Throws NotFoundException")
    void getUserById_ZeroId_ThrowsNotFoundException() {
        when(userRepository.findByIdWithAllRelations(eq(0L), anyLong())).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(0L));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }
}
