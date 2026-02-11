package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Permission;
import com.example.SpringApi.Models.ResponseModels.PermissionResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit cases for UserService.getAllPermissions method.
 */
@DisplayName("UserService - GetAllPermissions Tests")
class GetAllPermissionsTest extends UserServiceTestBase {
    // Total Tests: 7

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify empty list is returned when all permissions are deleted.
     * Expected Result: Returns empty list.
     * Assertions: assertTrue
     */
    @Test
    @DisplayName("getAllPermissions - Success - All Deleted Returns Empty")
    void getAllPermissions_allDeletedReturnsEmpty_success() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", true);
        stubPermissionRepositoryFindAll(Collections.singletonList(p1));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Purpose: Verify empty list is returned when no permissions exist.
     * Expected Result: Returns empty list.
     * Assertions: assertTrue
     */
    @Test
    @DisplayName("getAllPermissions - Success - Empty List")
    void getAllPermissions_emptyList_success() {
        // Arrange
        stubPermissionRepositoryFindAll(Collections.emptyList());

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertTrue(result.isEmpty());
    }

    /**
     * Purpose: Verify that the deleted permissions are filtered out.
     * Expected Result: Returns only non-deleted permissions.
     * Assertions: assertEquals
     */
    @Test
    @DisplayName("getAllPermissions - Success - Filters Deleted Permissions")
    void getAllPermissions_filtersDeletedPermissions_success() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", false);
        Permission p2 = createPermission(2L, "p2", "desc2", true); // Deleted
        stubPermissionRepositoryFindAll(Arrays.asList(p1, p2));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getPermissionName());
    }

    /**
     * Purpose: Verify successful retrieval of all non-deleted permissions.
     * Expected Result: Returns list of non-deleted permissions.
     * Assertions: assertEquals
     */
    @Test
    @DisplayName("getAllPermissions - Success - Returns Non-Deleted Permissions")
    void getAllPermissions_returnsNonDeletedPermissions_success() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", false);
        Permission p2 = createPermission(2L, "p2", "desc2", false);
        stubPermissionRepositoryFindAll(Arrays.asList(p1, p2));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertEquals(2, result.size());
        assertEquals("p1", result.get(0).getPermissionName());
        assertEquals("p2", result.get(1).getPermissionName());
    }

    /**
     * Purpose: Verify permissions are sorted by permission ID in ascending order.
     * Expected Result: Returns permissions sorted by ID.
     * Assertions: assertEquals
     */
    @Test
    @DisplayName("getAllPermissions - Success - Sorts by Permission ID Ascending")
    void getAllPermissions_sortsByPermissionIdAsc_success() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", false);
        Permission p2 = createPermission(2L, "p2", "desc2", false);
        Permission p3 = createPermission(3L, "p3", "desc3", false);
        stubPermissionRepositoryFindAll(Arrays.asList(p3, p1, p2));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getPermissionId());
        assertEquals(2L, result.get(1).getPermissionId());
        assertEquals(3L, result.get(2).getPermissionId());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

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
    @DisplayName("getAllPermissions - Controller permission forbidden")
    void getAllPermissions_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        stubMockUserServiceGetAllPermissionsThrowsUnauthorized();
        Method method = UserController.class.getMethod("getAllPermissions");

        // Act
        ResponseEntity<?> response = userControllerWithMock.getAllPermissions();
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(annotation, "getAllPermissions method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service method is called.
     * Assertions: verify, HttpStatus.OK
     */
    @Test
    @DisplayName("getAllPermissions - Controller delegates to service")
    void getAllPermissions_withValidRequest_delegatesToService() {
        // Arrange
        stubMockUserServiceGetAllPermissions(new ArrayList<>());

        // Act
        ResponseEntity<?> response = userControllerWithMock.getAllPermissions();

        // Assert
        verify(mockUserService, times(1)).getAllPermissions();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
