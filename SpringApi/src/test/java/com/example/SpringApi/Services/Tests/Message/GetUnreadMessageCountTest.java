package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.getUnreadMessageCount method.
 * Test Count: 10 tests
 */
@DisplayName("GetUnreadMessageCount Tests")
public class GetUnreadMessageCountTest extends MessageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Unread Message Count - Large count - Success")
    void getUnreadMessageCount_LargeCount_Success() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID)).thenReturn(1000L);

        int result = messageService.getUnreadMessageCount();

        assertEquals(1000, result);
        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("Get Unread Message Count - Max Integer Boundary - Success")
    void getUnreadMessageCount_MaxIntegerBoundary_Success() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID)).thenReturn((long)Integer.MAX_VALUE);
        int result = messageService.getUnreadMessageCount();
        assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    @DisplayName("Get Unread Message Count - No unread messages - Returns zero")
    void getUnreadMessageCount_NoUnreadMessages_ReturnsZero() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID)).thenReturn(0L);

        int result = messageService.getUnreadMessageCount();

        assertEquals(0, result);
        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("Get Unread Message Count - Permission check - Success Verifies Authorization")
    void getUnreadMessageCount_PermissionCheck_SuccessVerifiesAuthorization() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID)).thenReturn(0L);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(true);

        messageService.getUnreadMessageCount();

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Unread Message Count - Success")
    void getUnreadMessageCount_Success() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID)).thenReturn(5L);

        int result = messageService.getUnreadMessageCount();

        assertEquals(5, result);
        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Unread Message Count - Authorization denied - ThrowsUnauthorizedException")
    void getUnreadMessageCount_AuthorizationDenied_ThrowsUnauthorizedException() {
        when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> messageService.getUnreadMessageCount());

        verify(authorization).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Unread Message Count - Client Lookup Error - Propagates")
    void getUnreadMessageCount_ClientLookupError_Propagates() {
        // Simulating error getting client context through repository call
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                .thenThrow(new RuntimeException("Context error"));
        assertThrows(RuntimeException.class, () -> messageService.getUnreadMessageCount());
    }

    @Test
    @DisplayName("Get Unread Message Count - Repository exception - Throws Exception")
    void getUnreadMessageCount_RepositoryException_ThrowsException() {
        when(messageRepository.countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class,
                () -> messageService.getUnreadMessageCount());

        verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("Get Unread Message Count - Unauthorized Context - Throws UnauthorizedException")
    void getUnreadMessageCount_UnauthorizedContext_ThrowsUnauthorizedException() {
        when(authorization.hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> messageService.getUnreadMessageCount());

        verify(authorization).hasAuthority(Authorizations.VIEW_MESSAGES_PERMISSION);
    }

    @Test
    @DisplayName("Get Unread Message Count - User ID Lookup Failure - Propagates")
    void getUnreadMessageCount_UserIdLookupFailure_Propagates() {
        doThrow(new RuntimeException("User lookup failed")).when(messageRepository).countUnreadMessagesByUserId(anyLong(), anyLong());
        assertThrows(RuntimeException.class, () -> messageService.getUnreadMessageCount());
    }
}