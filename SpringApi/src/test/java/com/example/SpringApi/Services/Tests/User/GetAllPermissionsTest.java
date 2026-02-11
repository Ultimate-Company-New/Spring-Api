package com.example.SpringApi.Services.Tests.User;

import com.example.SpringApi.Controllers.UserController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Permission;
import com.example.SpringApi.Models.ResponseModels.PermissionResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Total Tests: 7
@DisplayName("UserService - GetAllPermissions Tests")
class GetAllPermissionsTest extends UserServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getAllPermissions - Verify @PreAuthorize Annotation")
    void getAllPermissions_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        Method method = UserController.class.getMethod("getAllPermissions");

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "getAllPermissions method should have @PreAuthorize annotation");
        assertTrue(annotation.value().contains(Authorizations.CREATE_USER_PERMISSION),
                "@PreAuthorize annotation should check for CREATE_USER_PERMISSION");
    }

    @Test
    @DisplayName("getAllPermissions - Controller delegates to service")
    void getAllPermissions_WithValidRequest_DelegatesToService() {
        // Arrange
        com.example.SpringApi.Services.UserService mockUserService = mock(
                com.example.SpringApi.Services.UserService.class);
        UserController localController = new UserController(mockUserService);
        when(mockUserService.getAllPermissions()).thenReturn(new ArrayList<>());

        // Act
        localController.getAllPermissions();

        // Assert
        verify(mockUserService, times(1)).getAllPermissions();
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("getAllPermissions - Success - Returns non-deleted permissions")
    void getAllPermissions_Success_ReturnsNonDeletedPermissions() {
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

    @Test
    @DisplayName("getAllPermissions - Success - Sorts by Permission ID Ascending")
    void getAllPermissions_Success_SortsByPermissionIdAsc() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", false);
        Permission p2 = createPermission(2L, "p2", "desc2", false);
        Permission p3 = createPermission(3L, "p3", "desc3", false);
        // Return unsorted list from repo to verify service sorting logic
        stubPermissionRepositoryFindAll(Arrays.asList(p3, p1, p2));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getPermissionId());
        assertEquals(2L, result.get(1).getPermissionId());
        assertEquals(3L, result.get(2).getPermissionId());
    }

    @Test
    @DisplayName("getAllPermissions - Success - Filters deleted permissions")
    void getAllPermissions_Success_FiltersDeletedPermissions() {
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

    @Test
    @DisplayName("getAllPermissions - Success - Empty list")
    void getAllPermissions_Success_EmptyList() {
        // Arrange
        stubPermissionRepositoryFindAll(Collections.emptyList());

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllPermissions - Success - All deleted returns empty")
    void getAllPermissions_Success_AllDeletedReturnsEmpty() {
        // Arrange
        Permission p1 = createPermission(1L, "p1", "desc1", true);
        stubPermissionRepositoryFindAll(Collections.singletonList(p1));

        // Act
        List<PermissionResponseModel> result = userService.getAllPermissions();

        // Assert
        assertTrue(result.isEmpty());
    }

    private Permission createPermission(Long id, String name, String desc, boolean isDeleted) {
        Permission p = new Permission();
        p.setPermissionId(id);
        p.setPermissionName(name);
        p.setPermissionCode(name.toUpperCase());
        p.setDescription(desc);
        p.setIsDeleted(isDeleted);
        return p;
    }
}
