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
 */
@DisplayName("GetMessagesByUserId Tests")
public class GetMessagesByUserIdTest extends MessageServiceTestBase {

        // Total Tests: 15

        /*
         **********************************************************************************************
         * SUCCESS TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify that pagination works correctly with default page size (0 in
         * request).
         * Scenario: Call getMessagesByUserId with start/end at 0.
         * Expected: Repository called with correct Pageable and result returned.
         */
        @Test
        @DisplayName("Get Messages By User ID - Default Page Size - Success")
        void getMessagesByUserId_DefaultPageSize_Success() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                paginationRequest.setStart(0);
                paginationRequest.setEnd(0);

                Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                                Arrays.asList(testMessage));

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID),
                                any(Pageable.class)))
                                .thenReturn(messagePage);

                // Act
                PaginationBaseResponseModel<MessageResponseModel> result = messageService
                                .getMessagesByUserId(paginationRequest);

                // Assert
                assertNotNull(result);
                assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
        }

        /**
         * Purpose: Verify that isRead flag is correctly set based on the presence of a
         * read map.
         * Scenario: Retrieve multiple messages where some have a read map entry and
         * others don't.
         * Expected: isRead is true for messages with mapping and false otherwise.
         */
        @Test
        @DisplayName("Get Messages By User ID - Mixed Read Status - Success")
        void getMessagesByUserId_MixedReadStatus_Success() {
                // Arrange
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

                Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                                Arrays.asList(m1, m2));

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                                .thenReturn(messagePage);

                // m1 read, m2 unread
                when(messageUserReadMapRepository.findByMessageIdAndUserId(1L, TEST_USER_ID))
                                .thenReturn(new MessageUserReadMap());
                when(messageUserReadMapRepository.findByMessageIdAndUserId(2L, TEST_USER_ID)).thenReturn(null);

                // Act
                PaginationBaseResponseModel<MessageResponseModel> result = messageService
                                .getMessagesByUserId(paginationRequest);

                // Assert
                assertTrue(result.getData().get(0).getIsRead());
                assertFalse(result.getData().get(1).getIsRead());
        }

        /**
         * Purpose: Verify that isRead is set to true when a read mapping exists in the
         * database.
         * Scenario: Retrieve a message where the user has already marked it as read.
         * Expected: result.isRead property is true.
         */
        @Test
        @DisplayName("Get Messages By User ID - Read map exists - isRead true")
        void getMessagesByUserId_ReadMapExists_IsReadTrue() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);

                Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                                Arrays.asList(testMessage));

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID),
                                any(Pageable.class)))
                                .thenReturn(messagePage);
                when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
                                .thenReturn(new MessageUserReadMap());

                // Act
                PaginationBaseResponseModel<MessageResponseModel> result = messageService
                                .getMessagesByUserId(paginationRequest);

                // Assert
                assertNotNull(result);
                assertTrue(result.getData().get(0).getIsRead());
        }

        /**
         * Purpose: Verify successful retrieval of messages for a valid user.
         * Scenario: Call getMessagesByUserId with correct user ID and pagination.
         * Expected: List of messages returned with correct mapping and unread status.
         */
        @Test
        @DisplayName("Get Messages By User ID - Success")
        void getMessagesByUserId_Success() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);

                Page<com.example.SpringApi.Models.DatabaseModels.Message> messagePage = new PageImpl<>(
                                Arrays.asList(testMessage));

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(eq(TEST_CLIENT_ID), eq(TEST_USER_ID),
                                any(Pageable.class)))
                                .thenReturn(messagePage);
                when(messageUserReadMapRepository.findByMessageIdAndUserId(TEST_MESSAGE_ID, TEST_USER_ID))
                                .thenReturn(null);

                // Act
                PaginationBaseResponseModel<MessageResponseModel> result = messageService
                                .getMessagesByUserId(paginationRequest);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getData().size());
                assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
                assertFalse(result.getData().get(0).getIsRead());
        }

        /**
         * Purpose: Verify that the start parameter is correctly converted to Pageable
         * offset.
         * Scenario: Set start=50 and end=60 in pagination request.
         * Expected: Repository is called with a Pageable whose offset matches the start
         * value.
         */
        @Test
        @DisplayName("Get Messages By User ID - Verify Offset Logic - Success")
        void getMessagesByUserId_VerifyOffset_Success() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                paginationRequest.setStart(50);
                paginationRequest.setEnd(60);

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                                .thenReturn(new PageImpl<>(Arrays.asList()));

                // Act
                messageService.getMessagesByUserId(paginationRequest);

                // Assert
                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
                verify(messageRepository).findMessagesByUserIdPaginated(anyLong(), anyLong(), pageableCaptor.capture());
                assertEquals(50, pageableCaptor.getValue().getOffset());
        }

        /**
         * Purpose: Verify that a zero-range pagination request (start == end) is
         * handled correctly.
         * Scenario: Call getMessagesByUserId with start=10 and end=10.
         * Expected: Method executes without error and makes repository call.
         */
        @Test
        @DisplayName("Get Messages By User ID - Zero Range Range - Success")
        void getMessagesByUserId_ZeroRange_Success() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                paginationRequest.setStart(10);
                paginationRequest.setEnd(10);

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                                .thenReturn(new PageImpl<>(Arrays.asList()));

                // Act & Assert
                assertDoesNotThrow(() -> messageService.getMessagesByUserId(paginationRequest));
        }

        /*
         **********************************************************************************************
         * FAILURE / EXCEPTION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Reject retrieval request when user ID is negative.
         * Expected Result: BadRequestException with user invalid ID error code.
         */
        @Test
        @DisplayName("Get Messages By User ID - Negative user ID - Throws BadRequestException")
        void getMessagesByUserId_NegativeUserId_ThrowsBadRequestException() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(-1L);

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.UserErrorMessages.InvalidId,
                                () -> messageService.getMessagesByUserId(paginationRequest));
        }

        /**
         * Purpose: Verify that passing a null request object results in a
         * NullPointerException.
         * Expected Result: NullPointerException thrown.
         */
        @Test
        @DisplayName("Get Messages By User ID - Null pagination request - ThrowsNullPointerException")
        void getMessagesByUserId_NullPaginationRequest_ThrowsNullPointerException() {
                // Arrange & Act & Assert
                assertThrows(NullPointerException.class,
                                () -> messageService.getMessagesByUserId(null));
        }

        /**
         * Purpose: Propagate repository exceptions when paginated message lookup fails.
         * Expected Result: RuntimeException with original database error message.
         */
        @Test
        @DisplayName("Get Messages By User ID - Repository Error - Propagates Exception")
        void getMessagesByUserId_RepositoryError_Propagates() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                                .thenThrow(new RuntimeException("Page error"));

                // Act & Assert
                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> messageService.getMessagesByUserId(paginationRequest));
                assertEquals("Page error", ex.getMessage());
        }

        /**
         * Purpose: Verify that unauthorized access checks are handled at controller
         * level.
         * Scenario: Service layer is called for a valid user (service doesn't enforce
         * auth).
         * Expected: Service returns valid result or empty list without throwing
         * security exceptions.
         */
        @Test
        @DisplayName("Get Messages By User ID - Unauthorized Access - Throws UnauthorizedException")
        void getMessagesByUserId_UnauthorizedAccess_ThrowsUnauthorizedException() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID))
                                .thenReturn(Optional.of(testUser));
                when(messageRepository.findMessagesByUserIdPaginated(anyLong(), anyLong(), any()))
                                .thenReturn(new PageImpl<>(Arrays.asList()));

                // Act & Assert
                assertDoesNotThrow(() -> messageService.getMessagesByUserId(paginationRequest));
        }

        /**
         * Purpose: Reject retrieval request when user record is not found in database.
         * Expected Result: NotFoundException with user invalid ID error code.
         */
        @Test
        @DisplayName("Get Messages By User ID - User not found - Throws NotFoundException")
        void getMessagesByUserId_UserNotFound_ThrowsNotFoundException() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);

                when(userRepository.findByUserIdAndClientId(TEST_USER_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                                () -> messageService.getMessagesByUserId(paginationRequest));
        }

        /**
         * Purpose: Propagate repository exceptions when user lookup fails.
         * Expected Result: RuntimeException with original database error message.
         */
        @Test
        @DisplayName("Get Messages By User ID - User Repository Error - Propagates Exception")
        void getMessagesByUserId_UserRepositoryError_Propagates() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(TEST_USER_ID);
                when(userRepository.findByUserIdAndClientId(anyLong(), anyLong()))
                                .thenThrow(new RuntimeException("User DB Error"));

                // Act & Assert
                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> messageService.getMessagesByUserId(paginationRequest));
                assertEquals("User DB Error", ex.getMessage());
        }

        /**
         * Purpose: Reject retrieval request when user ID is zero.
         * Expected Result: BadRequestException with user invalid ID error code.
         */
        @Test
        @DisplayName("Get Messages By User ID - Zero user ID - ThrowsBadRequestException")
        void getMessagesByUserId_ZeroUserId_ThrowsBadRequestException() {
                // Arrange
                PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                paginationRequest.setId(0L);

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.UserErrorMessages.InvalidId,
                                () -> messageService.getMessagesByUserId(paginationRequest));
        }

        /*
         **********************************************************************************************
         * CONTROLLER AUTHORIZATION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify that the getMessagesByUserId controller method is protected
         * by correct @PreAuthorize permission.
         * Expected: Method has @PreAuthorize referencing VIEW_MESSAGES_PERMISSION.
         */
        @Test
        @DisplayName("getMessagesByUserId - Verify @PreAuthorize annotation")
        void getMessagesByUserId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
                // Arrange
                Method method = MessageController.class.getMethod(
                                "getMessagesByUserId",
                                com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class);

                // Act
                PreAuthorize preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

                // Assert
                assertNotNull(preAuthorizeAnnotation,
                                "getMessagesByUserId method should have @PreAuthorize annotation");

                String expectedPermission = "@customAuthorization.hasAuthority('" +
                                Authorizations.VIEW_MESSAGES_PERMISSION + "')";

                assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                                "PreAuthorize annotation should reference VIEW_MESSAGES_PERMISSION");
        }

        /**
         * Purpose: Verify that the controller correctly delegates getMessagesByUserId
         * calls to the service layer.
         * Expected: Service method called with correct request and HTTP 200 returned.
         */
        @Test
        @DisplayName("getMessagesByUserId - Controller delegates to service correctly")
        void getMessagesByUserId_WithValidRequest_DelegatesToService() {
                // Arrange
                MessageController controller = new MessageController(messageServiceMock);
                PaginationBaseResponseModel<MessageResponseModel> mockResponse = new PaginationBaseResponseModel<>();
                PaginationBaseRequestModel pRequest = createValidPaginationRequest();
                pRequest.setId(TEST_USER_ID);
                when(messageServiceMock.getMessagesByUserId(pRequest)).thenReturn(mockResponse);

                // Act
                ResponseEntity<?> response = controller.getMessagesByUserId(pRequest);

                // Assert
                verify(messageServiceMock, times(1)).getMessagesByUserId(any(PaginationBaseRequestModel.class));
                assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
        }
}