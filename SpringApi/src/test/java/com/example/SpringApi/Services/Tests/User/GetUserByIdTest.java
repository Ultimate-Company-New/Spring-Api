package com.example.SpringApi.Services.Tests.User;

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
import static org.mockito.Mockito.*;

// Total Tests: 10
@DisplayName("UserService - Get User By ID Tests")
class GetUserByIdTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation.
     * Expected Result: The method should be annotated with VIEW_USER_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("getUserById - Verify @PreAuthorize Annotation")
    void getUserById_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("getUserById", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "getUserById method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.VIEW_USER_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify(userService).getUserById(userId);
     */
    @Test
    @DisplayName("getUserById - Controller delegates to service")
    void getUserById_WithValidId_DelegatesToService() {
        // Arrange
        Long userId = 1L;
        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        doReturn(new UserResponseModel()).when(mockUserService).getUserById(userId);

        // Act
        ResponseEntity<?> response = localController.getUserById(userId);

        // Assert
        verify(mockUserService, times(1)).getUserById(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify user with no permissions returns empty or null permissions.
     * Expected Result: Empty or null permissions list.
     * Assertions: Permissions is null or empty.
     */
    @Test
    @DisplayName("getUserById - No Permissions - Returns empty or null permissions")
    void getUserById_noPermissions_returnsEmptyPermissionsList() {
        // Arrange
        testUser.setUserClientPermissionMappings(new HashSet<>());
        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getPermissions() == null || result.getPermissions().isEmpty());
    }

    /**
     * Purpose: Verify repository is called exactly once.
     * Expected Result: findByIdWithAllRelations called once.
     * Assertions: verify(userRepository, times(1)).findByIdWithAllRelations(...);
     */
    @Test
    @DisplayName("getUserById - Repository called once")
    void getUserById_repositoryCalledOnce() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        userService.getUserById(TEST_USER_ID);

        // Assert
        verify(userRepository, times(1)).findByIdWithAllRelations(anyLong(), anyLong());
    }

    /**
     * Purpose: Verify user permissions are returned.
     * Expected Result: Permissions list is populated.
     * Assertions: assertNotNull(result.getPermissions());
     */
    @Test
    @DisplayName("getUserById - Success - Returns permissions")
    void getUserById_success_returnsPermissions() {
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

        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        // Assert
        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());
    }

    /**
     * Purpose: Verify successful user retrieval by ID.
     * Expected Result: UserResponseModel is returned with correct data.
     * Assertions: correct ID, email, login name.
     */
    @Test
    @DisplayName("getUserById - Success - Returns user with all details")
    void getUserById_success_returnsUserWithDetails() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(testUser);

        // Act
        UserResponseModel result = userService.getUserById(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_LOGIN_NAME, result.getLoginName());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify max long ID throws NotFoundException when not found.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("getUserById - Max Long ID - Throws NotFoundException")
    void getUserById_maxLongId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("getUserById - Negative ID - Throws NotFoundException")
    void getUserById_negativeId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(-1L));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that non-existent user throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("getUserById - User Not Found - Throws NotFoundException")
    void getUserById_userNotFound_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(TEST_USER_ID));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException with "Invalid User Id" message.
     */
    @Test
    @DisplayName("getUserById - Zero ID - Throws NotFoundException")
    void getUserById_zeroId_throwsNotFoundException() {
        // Arrange
        stubUserRepositoryFindByIdWithAllRelations(null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserById(0L));

        // Assert
        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, ex.getMessage());
    }
}
