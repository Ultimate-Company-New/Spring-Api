package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Models.DatabaseModels.MessageUserReadMap;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.getMessagesByUserId method.
 * Test Count: 14 tests
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

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class))).thenReturn(messagePage);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
    }

    @Test
    @DisplayName("Get Messages By User ID - Mixed Read Status - Success")
    void getMessagesByUserId_MixedReadStatus_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        com.example.SpringApi.Models.DatabaseModels.Message m1 = new com.example.SpringApi.Models.DatabaseModels.Message();
        m1.setMessageId(1L); m1.setTitle("T1"); m1.setDescriptionHtml("D");
        com.example.SpringApi.Models.DatabaseModels.Message m2 = new com.example.SpringApi.Models.DatabaseModels.Message();
        m2.setMessageId(2L); m2.setTitle("T2"); m2.setDescriptionHtml("D");

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(Arrays.asList(m1, m2));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any())).thenReturn(messagePage);
        
        // m1 read, m2 unread
        when(messageUserReadMapRepository.findByMessageIdAndUserId(1L, TEST_USER_ID)).thenReturn(new MessageUserReadMap());
        when(messageUserReadMapRepository.findByMessageIdAndUserId(2L, TEST_USER_ID)).thenReturn(null);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesByUserId(paginationRequest);

        assertTrue(result.getData().get(0).getIsRead());
        assertFalse(result.getData().get(1).getIsRead());
    }

    @Test
    @DisplayName("Get Messages By User ID - Permission check - Success Verifies Authorization")
    void getMessagesByUserId_PermissionCheck_SuccessVerifiesAuthorization() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(true);

        messageService.getMessagesByUserId(paginationRequest);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Messages By User ID - Read map exists - isRead true")
    void getMessagesByUserId_ReadMapExists_IsReadTrue() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class))).thenReturn(messagePage);
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(new MessageUserReadMap());

        PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesByUserId(paginationRequest);

        assertNotNull(result);
        assertTrue(result.getData().get(0).getIsRead());
    }

    @Test
    @DisplayName("Get Messages By User ID - Success")
    void getMessagesByUserId_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);

        Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID), any(Pageable.class))).thenReturn(messagePage);
        when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID)).thenReturn(null);

        PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesByUserId(paginationRequest);

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
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any())).thenReturn(new PageImpl<>(Arrays.asList()));

        messageService.getMessagesByUserId(paginationRequest);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findMessagesByUserIdPaginated(anyLong(), anyLong(), pageableCaptor.capture());
        assertEquals(5, pageableCaptor.getValue().getPageNumber()); // (50 / 10)
    }

    @Test
    @DisplayName("Get Messages By User ID - Zero Range Range - Success")
    void getMessagesByUserId_ZeroRange_Success() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        paginationRequest.setStart(10);
        paginationRequest.setEnd(10);

        when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.of(testUser));
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any())).thenReturn(new PageImpl<>(Arrays.asList()));

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
        when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any())).thenThrow(new RuntimeException("Page error"));
        assertThrows(RuntimeException.class, () -> messageService.getMessagesByUserId(paginationRequest));
    }

    @Test
    @DisplayName("Get Messages By User ID - Unauthorized Access - Throws UnauthorizedException")
    void getMessagesByUserId_UnauthorizedAccess_ThrowsUnauthorizedException() {
        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
        paginationRequest.setId(TEST_USER_ID);
        when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> messageService.getMessagesByUserId(paginationRequest));

        verify(authorization).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
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
        when(userRepository.findByUserIdAndClientId(anyLong(), anyLong())).thenThrow(new RuntimeException("User DB Error"));
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
}