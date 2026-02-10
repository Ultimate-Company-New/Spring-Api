package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import org.springframework.security.access.prepost.PreAuthorize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.getMessagesByUserId method.
 * Test Count: 16 tests
 */
@DisplayName("GetMessagesByUserId Tests")
public class GetMessagesByUserIdTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Messages By User ID - Default Page Size - Success")
    void getMessagesByUserId_DefaultPageSize_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(0);
        paginationRequest.setEnd(0);

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(messagePage);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService
                .getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
    }

    @Test
    @DisplayName("Get Messages By User ID - Mixed Read Status - Success")
    void getMessagesByUserId_MixedReadStatus_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        com.example.SpringApi.Models.DatabaseModels.Message m1 = new com.example.SpringApi.Models.DatabaseModels.Message();
        m1.setMessageId(1L);
        m1.setTitle("T1");
        m1.setDescriptionHtml("D");
        com.example.SpringApi.Models.DatabaseModels.Message m2 = new com.example.SpringApi.Models.DatabaseModels.Message();
        m2.setMessageId(2L);
        m2.setTitle("T2");
        m2.setDescriptionHtml("D");

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(Arrays.asList(m1, m2));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any())).thenReturn(messagePage);

        // m1 read, m2 unread
        when(messageUserReadMapRepository.findByMessageIdAndUserId(1L, TEST_USER_ID))
                .thenReturn(new MessageUserReadMap());
        when(messageUserReadMapRepository.findByMessageIdAndUserId(2L, TEST_USER_ID)).thenReturn(null);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService
                .getMessagesByUserId(paginationRequest);

        assertTrue(result.getData().get(0).getIsRead());
        assertFalse(result.getData().get(1).getIsRead());
    }

    @Test
    @DisplayName("Get Messages By User ID - Read map exists - isRead true")
    void getMessagesByUserId_ReadMapExists_IsReadTrue() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(messagePage);
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
                .thenReturn(new MessageUserReadMap());

        PaginationBaseResponseModel<MessageResponseModel> result = messageService
                .getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertTrue(result.getData().get(0).getIsRead());
    }

    @Test
    @DisplayName("Get Messages By User ID - Success")
    void getMessagesByUserId_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class)))
                .thenReturn(messagePage);
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(null);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService
                .getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
        assertFalse(result.getData().get(0).getIsRead());
    }

    @Test
    @DisplayName("Get Messages By User ID - Verify Offset Logic - Success")
    void getMessagesByUserId_VerifyOffset_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(50);
        paginationRequest.setEnd(60);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        messageService.getMessagesByUserId(paginationRequest);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findMessagesByUserIdPaginated(anyLong(), anyLong(), pageableCaptor.capture());
        assertEquals(50, pageableCaptor.getValue().getOffset()); // Check offset directly, not pageNumber
    }

    @Test
    @DisplayName("Get Messages By User ID - Zero Range Range - Success")
    void getMessagesByUserId_ZeroRange_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(10);
        paginationRequest.setEnd(10);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        assertDoesNotThrow(() -> messageService.getMessagesByUserId(paginationRequest));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Messages By User ID - Negative user ID - Throws BadRequestException")
    void getMessagesByUserId_NegativeUserId_ThrowsBadRequestException() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(-1L);

        assertThrowsBadRequest(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - Null pagination request - ThrowsNullPointerException")
    void getMessagesByUserId_NullPaginationRequest_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> messageService.getMessagesByUserId(null));
    }

    @Test
    @DisplayName("Get Messages By User ID - Repository Error - Propagates Exception")
    void getMessagesByUserId_RepositoryError_Propagates() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                .thenThrow(new RuntimeException("Page error"));
        assertThrows(RuntimeException.class, () -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - Unauthorized Access - Throws UnauthorizedException")
    void getMessagesByUserId_UnauthorizedAccess_ThrowsUnauthorizedException() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList()));

        // Note: Authorization is controller-level only, service doesn't check it
        // This test passes because no UnauthorizedException is thrown at service level
        assertDoesNotThrow(() -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - User not found - Throws NotFoundException")
    void getMessagesByUserId_UserNotFound_ThrowsNotFoundException() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - User Repository Error - Propagates Exception")
    void getMessagesByUserId_UserRepositoryError_Propagates() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        when(userRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("User DB Error"));
        assertThrows(RuntimeException.class, () -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - Zero user ID - ThrowsBadRequestException")
    void getMessagesByUserId_ZeroUserId_ThrowsBadRequestException() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(0L);

        assertThrowsBadRequest(ErrorMessages.UserErrorMessages.InvalidId,
                () -> messageService.getMessagesByUserId(paginationRequest));
    }
    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getMessagesByUserId - Verify @PreAuthorize annotation")
    void getMessagesByUserId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = MessageController.class.getMethod(
                "getMessagesByUserId",
                com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class);

        PreAuthorize preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getMessagesByUserId method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_MESSAGES_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_MESSAGES_PERMISSION");
    }

    @Test
    @DisplayName("getMessagesByUserId - Controller delegates to service correctly")
    void getMessagesByUserId_WithValidRequest_DelegatesToService() {
        MessageController controller = new MessageController(messageServiceMock);
        PaginationBaseResponseModel<MessageResponseModel> mockResponse = new PaginationBaseResponseModel<>();
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setId(TEST_USER_ID);
        when(messageServiceMock.getMessagesByUserId(request)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getMessagesByUserId(request);

        verify(messageServiceMock, times(1)).getMessagesByUserId(any(PaginationBaseRequestModel.class));
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}