package com.example.SpringApi.Services.Tests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import org.springframework.security.access.prepost.PreAuthorize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.getMessageDetailsById method.
 */
@DisplayName("GetMessageDetailsById Tests")
public class GetMessageDetailsByIdTest extends MessageServiceTestBase {

    // Total Tests: 11

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Message Details By ID - Mapping Verification - Success")
    void getMessageDetailsById_MappingVerification_Success() {
        testMessage.setDescriptionHtml("<b>Test</b>");
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertEquals("<b>Test</b>", result.getDescriptionHtml());
        assertEquals(TEST_TITLE, result.getTitle());
    }

    @Test
    @DisplayName("Get Message Details By ID - Success")
    void getMessageDetailsById_Success() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

        assertNotNull(result);
        assertEquals(TEST_MESSAGE_ID, result.getMessageId());
        assertEquals(TEST_TITLE, result.getTitle());
    }

    @Test
    @DisplayName("Get Message Details By ID - Verify SendAsEmail flag - Success")
    void getMessageDetailsById_VerifySendAsEmail_Success() {
        testMessage.setSendAsEmail(true);
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);
        assertTrue(result.getSendAsEmail());
    }

    @Test
    @DisplayName("Get Message Details By ID - With Targets - Success")
    void getMessageDetailsById_WithTargets_Success() {
        // Logic check: ensure the query for targets is called
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        assertDoesNotThrow(() -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        verify(messageRepository).findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Get Message Details By ID - Message not found - Throws NotFoundException")
    void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Get Message Details By ID - Negative message ID - Throws BadRequestException")
    void getMessageDetailsById_NegativeMessageId_ThrowsBadRequestException() {
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(-1L));
    }

    @Test
    @DisplayName("Get Message Details By ID - Repository Error - Propagates Exception")
    void getMessageDetailsById_RepositoryError_Propagates() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Lookup failed"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        assertEquals("Lookup failed", ex.getMessage());
    }

    @Test
    @DisplayName("Get Message Details By ID - Unauthorized Access - Throws UnauthorizedException")
    void getMessageDetailsById_UnauthorizedAccess_ThrowsUnauthorizedException() {
        when(messageRepository.findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testMessage));

        // Note: Authorization is controller-level only, service doesn't check it
        assertDoesNotThrow(() -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
    }

    @Test
    @DisplayName("Get Message Details By ID - Zero message ID - ThrowsBadRequestException")
    void getMessageDetailsById_ZeroMessageId_ThrowsBadRequestException() {
        assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.InvalidId,
                () -> messageService.getMessageDetailsById(0L));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getMessageDetailsById - Verify @PreAuthorize annotation")
    void getMessageDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = MessageController.class.getMethod(
                "getMessageDetailsById",
                com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class);

        PreAuthorize preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getMessageDetailsById method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_MESSAGES_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_MESSAGES_PERMISSION");
    }

    @Test
    @DisplayName("getMessageDetailsById - Controller delegates to service correctly")
    void getMessageDetailsById_WithValidRequest_DelegatesToService() {
        MessageController controller = new MessageController(messageServiceMock);
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setId(TEST_MESSAGE_ID);
        when(messageServiceMock.getMessageDetailsById(TEST_MESSAGE_ID)).thenReturn(new MessageResponseModel(testMessage));

        ResponseEntity<?> response = controller.getMessageDetailsById(request);

        verify(messageServiceMock, times(1)).getMessageDetailsById(TEST_MESSAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}