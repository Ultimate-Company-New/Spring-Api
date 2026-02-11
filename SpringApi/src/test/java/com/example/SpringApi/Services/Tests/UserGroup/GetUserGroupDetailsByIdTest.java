package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - GetUserGroupDetailsById functionality.
 *
 * Test Summary:
 * - Controller authorization and delegation
 * - Successful retrieval of user group details
 * - Verification of repository interaction
 * - Failure cases (group not found, invalid IDs)
 *
 * Total Tests: 11
 */
@DisplayName("UserGroupService - GetUserGroupDetailsById Tests")
public class GetUserGroupDetailsByIdTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserGroupDetailsById - Verify @PreAuthorize Annotation")
    void getUserGroupDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Arrange
        Method method = UserGroupController.class.getMethod("getUserGroupDetailsById", Long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserGroupDetailsById method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("getUserGroupDetailsById - Controller delegates to service")
    void getUserGroupDetailsById_WithValidId_DelegatesToService() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

        // Act
        ResponseEntity<?> response = userGroupController.getUserGroupDetailsById(TEST_GROUP_ID);

        // Assert
        verify(userGroupService).getUserGroupDetailsById(TEST_GROUP_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    /**
     * Purpose: Verify repository is called once.
     * Expected Result: findByIdWithUsers is called exactly once.
     * Assertions: verify(userGroupRepository,
     * times(1)).findByIdWithUsers(TEST_GROUP_ID);
     */
    @Test
    @DisplayName("Get User Group By ID - Repository called once")
    void getUserGroupDetailsById_RepositoryCalledOnce() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

        // Act
        userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

        // Assert
        verify(userGroupRepository, times(1)).findByIdWithUsers(TEST_GROUP_ID);
    }

    /**
     * Purpose: Verify correct fields are returned.
     * Expected Result: Response contains all correct fields.
     * Assertions: assertEquals for each field.
     */
    @Test
    @DisplayName("Get User Group By ID - Returns correct fields")
    void getUserGroupDetailsById_ReturnsCorrectFields() {
        // Arrange
        testUserGroup.setDescription("Specific Description");
        stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

        // Act
        UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

        // Assert
        assertEquals(TEST_GROUP_ID, result.getGroupId());
        assertEquals(TEST_GROUP_NAME, result.getGroupName());
        assertEquals("Specific Description", result.getDescription());
    }

    /**
     * Purpose: Verify non-null response.
     * Expected Result: Response and its fields are not null.
     * Assertions: assertNotNull for response and fields.
     */
    @Test
    @DisplayName("Get User Group By ID - Returns non-null response")
    void getUserGroupDetailsById_ReturnsNonNullResponse() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

        // Act
        UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGroupId());
        assertNotNull(result.getGroupName());
    }

    /**
     * Purpose: Verify successful retrieval of user group details.
     * Expected Result: UserGroupResponseModel is returned with correct data.
     * Assertions: assertNotNull(result); assertEquals for fields.
     */
    @Test
    @DisplayName("Get User Group Details By ID - Success")
    void getUserGroupDetailsById_Success() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(testUserGroup);

        // Act
        UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_GROUP_ID, result.getGroupId());
        assertEquals(TEST_GROUP_NAME, result.getGroupName());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    /**
     * Purpose: Verify max long ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User Group By ID - Max Long ID - Not Found")
    void getUserGroupDetailsById_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify min long ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User Group By ID - Min Long ID - Not Found")
    void getUserGroupDetailsById_MinLongId_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify negative ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User Group By ID - Negative ID - Not Found")
    void getUserGroupDetailsById_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(-1L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify group not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User Group Details By ID - Not Found")
    void getUserGroupDetailsById_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(TEST_GROUP_ID));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify zero ID not found throws NotFoundException.
     * Expected Result: NotFoundException with InvalidId message.
     * Assertions: assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId,
     * ex.getMessage());
     */
    @Test
    @DisplayName("Get User Group By ID - Zero ID - Not Found")
    void getUserGroupDetailsById_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubUserGroupRepositoryFindByIdWithUsers(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.getUserGroupDetailsById(0L));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }
}
