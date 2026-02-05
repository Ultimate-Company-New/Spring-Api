package com.example.SpringApi.Services.Tests.UserGroup;

import com.example.SpringApi.Controllers.UserGroupController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.UserGroup;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.RequestModels.UserGroupRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserGroupService - Update User Group functionality.
 * 
 * Contains 13 tests covering:
 * - Successful user group updates with various inputs
 * - Validation for non-existent groups, invalid IDs, and duplicate names
 * - Edge cases (special characters, same name updates, max long ID)
 * - Repository interaction verification (delete old mappings, save new ones)
 */
@DisplayName("UserGroupService - UpdateUserGroup Tests")
public class UpdateUserGroupTest extends UserGroupServiceTestBase {

    // ========================================
    // CONTROLLER AUTHORIZATION TESTS
    // ========================================

    @Test
    @DisplayName("updateUserGroup - Verify @PreAuthorize Annotation")
    void updateUserGroup_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = UserGroupController.class.getMethod("updateUserGroup", Long.class, UserGroupRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on updateUserGroup method");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_GROUPS_PERMISSION),
                "@PreAuthorize annotation should check for UPDATE_GROUPS_PERMISSION");
    }

    @Test
    @DisplayName("updateUserGroup - Controller delegates to service")
    void updateUserGroup_WithValidRequest_DelegatesToService() {
        UserGroupController controller = new UserGroupController(userGroupService);
        Long groupId = 1L;
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setId(groupId);
        doNothing().when(userGroupService).updateUserGroup(request);

        ResponseEntity<?> response = controller.updateUserGroup(groupId, request);

        verify(userGroupService).updateUserGroup(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ========================================
    // SUCCESS TESTS
    // ========================================

    @Test
    @DisplayName("Update User Group - Same Name Different Group - Allowed")
    void updateUserGroup_SameNameSameGroup_Allowed() {
        testUserGroupRequest.setGroupName(TEST_GROUP_NAME);
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupRepository.findByGroupName(TEST_GROUP_NAME)).thenReturn(testUserGroup);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
    }

    @Test
    @DisplayName("Update User Group - Special chars in name - Success")
    void updateUserGroup_SpecialCharsInName_Success() {
        testUserGroupRequest.setGroupName("Updated @#$ Group");
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupRepository.findByGroupName("Updated @#$ Group")).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
    }

    @Test
    @DisplayName("Update User Group - Success")
    void updateUserGroup_Success() {
        testUserGroupRequest.setGroupName("Updated Group Name");
        testUserGroupRequest.setDescription("Updated Description");
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        lenient().when(userGroupRepository.findByGroupName("Updated Group Name")).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> userGroupService.updateUserGroup(testUserGroupRequest));
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("Update User Group - Verify new mappings created")
    void updateUserGroup_VerifyNewMappingsCreated() {
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        lenient().when(userGroupRepository.findByGroupName(anyString())).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        userGroupService.updateUserGroup(testUserGroupRequest);

        verify(userGroupUserMapRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Update User Group - Verify old mappings deleted")
    void updateUserGroup_VerifyOldMappingsDeleted() {
        List<UserGroupUserMap> existingMappings = Arrays.asList(testMapping);

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        lenient().when(userGroupRepository.findByGroupName(anyString())).thenReturn(null);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(testUserGroup);
        when(userGroupUserMapRepository.findByGroupId(TEST_GROUP_ID)).thenReturn(existingMappings);
        doNothing().when(userGroupUserMapRepository).deleteAll(anyList());
        when(userGroupUserMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        userGroupService.updateUserGroup(testUserGroupRequest);

        verify(userGroupUserMapRepository).deleteAll(anyList());
    }

    // ========================================
    // FAILURE TESTS
    // ========================================

    @Test
    @DisplayName("Update User Group - Duplicate Name - Throws BadRequestException")
    void updateUserGroup_DuplicateName_ThrowsBadRequestException() {
        testUserGroupRequest.setGroupName("Existing Group");
        UserGroup existingGroupWithSameName = new UserGroup();
        existingGroupWithSameName.setGroupId(999L); // Different ID
        existingGroupWithSameName.setGroupName("Existing Group");

        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        when(userGroupRepository.findByGroupName("Existing Group")).thenReturn(existingGroupWithSameName);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.GroupNameExists, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Max Long ID - Throws NotFoundException")
    void updateUserGroup_MaxLongId_ThrowsNotFoundException() {
        testUserGroupRequest.setGroupId(Long.MAX_VALUE);
        when(userGroupRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Negative ID - Throws NotFoundException")
    void updateUserGroup_NegativeId_ThrowsNotFoundException() {
        testUserGroupRequest.setGroupId(-1L);
        when(userGroupRepository.findById(-1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - No Users - Throws BadRequestException")
    void updateUserGroup_NoUsers_ThrowsBadRequestException() {
        testUserGroupRequest.setUserIds(new ArrayList<>());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Not Found - Throws NotFoundException")
    void updateUserGroup_NotFound_ThrowsNotFoundException() {
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Null Name - Throws BadRequestException")
    void updateUserGroup_NullName_ThrowsBadRequestException() {
        testUserGroupRequest.setGroupName(null);
        // Need to mock findById to return the existing group so validation proceeds to the name check
        when(userGroupRepository.findById(TEST_GROUP_ID)).thenReturn(Optional.of(testUserGroup));
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER002, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Null Users - Throws BadRequestException")
    void updateUserGroup_NullUsers_ThrowsBadRequestException() {
        testUserGroupRequest.setUserIds(null);
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Update User Group - Zero ID - Throws NotFoundException")
    void updateUserGroup_ZeroId_ThrowsNotFoundException() {
        testUserGroupRequest.setGroupId(0L);
        when(userGroupRepository.findById(0L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userGroupService.updateUserGroup(testUserGroupRequest));
        assertEquals(ErrorMessages.UserGroupErrorMessages.InvalidId, ex.getMessage());
    }
}
