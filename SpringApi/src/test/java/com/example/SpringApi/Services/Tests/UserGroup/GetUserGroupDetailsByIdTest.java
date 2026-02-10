package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Services.UserGroupService;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ResponseModels.UserGroupResponseModel;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * | Test Group | Number of Tests |
 * | :------------------ | :-------------- |
 * | SUCCESS Tests | 4 |
 * | FAILURE Tests | 5 |
 * | **Total** | **9** |
 */
@DisplayName("UserGroupService - GetUserGroupDetailsById Tests")
class GetUserGroupDetailsByIdTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("getUserGroupDetailsById - Verify @PreAuthorize Annotation")
    void getUserGroupDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserGroupController.class.getMethod("getUserGroupDetailsById", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getUserGroupDetailsById method");
        assertTrue(annotation.value().contains(Authorizations.VIEW_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for VIEW_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("getUserGroupDetailsById - Controller delegates to service")
    void getUserGroupDetailsById_WithValidId_DelegatesToService() {
        UserGroupService mockUserGroupService = mock(UserGroupService.class);
        UserGroupController controller = new UserGroupController(mockUserGroupService);
        Long groupId = 1L;
        UserGroupResponseModel mockResponse = new UserGroupResponseModel();
        when(mockUserGroupService.getUserGroupDetailsById(groupId)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getUserGroupDetailsById(groupId);

        verify(mockUserGroupService).getUserGroupDetailsById(groupId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Nested
    @DisplayName("SUCCESS Tests")
    class SuccessTests {

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
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

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
            testUserGroup.setDescription("Specific Description");
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

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
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

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
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(testUserGroup);

            UserGroupResponseModel result = userGroupService.getUserGroupDetailsById(TEST_GROUP_ID);

            assertNotNull(result);
            assertEquals(TEST_GROUP_ID, result.getGroupId());
            assertEquals(TEST_GROUP_NAME, result.getGroupName());
        }
    }

    @Nested
    @DisplayName("FAILURE Tests")
    class FailureTests {

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
            when(userGroupRepository.findByIdWithUsers(Long.MAX_VALUE)).thenReturn(null);

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
            when(userGroupRepository.findByIdWithUsers(Long.MIN_VALUE)).thenReturn(null);

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
            when(userGroupRepository.findByIdWithUsers(-1L)).thenReturn(null);

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
            when(userGroupRepository.findByIdWithUsers(TEST_GROUP_ID)).thenReturn(null);

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
            when(userGroupRepository.findByIdWithUsers(0L)).thenReturn(null);

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> userGroupService.getUserGroupDetailsById(0L));
            assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
        }
    }
}
