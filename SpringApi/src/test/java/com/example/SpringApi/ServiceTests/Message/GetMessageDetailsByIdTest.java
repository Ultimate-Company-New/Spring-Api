package com.example.SpringApi.ServiceTests.Message;

import com.example.SpringApi.Controllers.MessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
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
class GetMessageDetailsByIdTest extends MessageServiceTestBase {


        // Total Tests: 12
        /*
         **********************************************************************************************
         * SUCCESS TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify message details are correctly mapped from database model to
         * response model.
         * Scenario: Retrieve message and verify HTML description is preserved in
         * mapping.
         * Expected: Message response contains exact database values for title and
         * description.
         */
        @Test
        @DisplayName("Get Message Details By ID - Mapping Verification - Success")
        void getMessageDetailsById_MappingVerification_Success() {
                // Arrange
                testMessage.setDescriptionHtml("<b>Test</b>");
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.of(testMessage));

                // Act
                MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

                // Assert
                assertEquals("<b>Test</b>", result.getDescriptionHtml());
                assertEquals(TEST_TITLE, result.getTitle());
        }

        /**
         * Purpose: Verify that a message can be successfully retrieved by its ID.
         * Scenario: Call getMessageDetailsById with a valid existing message ID.
         * Expected: Correct message details returned with matching ID and title.
         */
        @Test
        @DisplayName("Get Message Details By ID - Success")
        void getMessageDetailsById_Success_Success() {
                // Arrange
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.of(testMessage));

                // Act
                MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

                // Assert
                assertNotNull(result);
                assertEquals(TEST_MESSAGE_ID, result.getMessageId());
                assertEquals(TEST_TITLE, result.getTitle());
        }

        /**
         * Purpose: Verify that the sendAsEmail flag is correctly mapped in the
         * response.
         * Scenario: Retrieve message where sendAsEmail is set to true.
         * Expected: Result object has sendAsEmail property set to true.
         */
        @Test
        @DisplayName("Get Message Details By ID - Verify SendAsEmail flag - Success")
        void getMessageDetailsById_VerifySendAsEmail_Success() {
                // Arrange
                testMessage.setSendAsEmail(true);
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.of(testMessage));

                // Act
                MessageResponseModel result = messageService.getMessageDetailsById(TEST_MESSAGE_ID);

                // Assert
                assertTrue(result.getSendAsEmail());
        }

        /**
         * Purpose: Verify that the repository method for retrieving message with
         * targets is called.
         * Scenario: Call getMessageDetailsById for any valid ID.
         * Expected: messageRepository.findByMessageIdAndClientIdWithTargets is invoked
         * with provided ID.
         */
        @Test
        @DisplayName("Get Message Details By ID - With Targets - Success")
        void getMessageDetailsById_WithTargets_Success() {
                // Arrange
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.of(testMessage));

                // Act & Assert
                assertDoesNotThrow(() -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
                verify(messageRepository).findByMessageIdAndClientIdWithTargets(TEST_MESSAGE_ID, TEST_CLIENT_ID);
        }

        /*
         **********************************************************************************************
         * FAILURE / EXCEPTION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Reject retrieval request when message ID does not exist.
         * Expected Result: NotFoundException with ER007/InvalidId error message.
         */
        @Test
        @DisplayName("Get Message Details By ID - Message not found - Throws NotFoundException")
        void getMessageDetailsById_MessageNotFound_ThrowsNotFoundException() {
                // Arrange
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.empty());

                // Act & Assert
                assertThrowsNotFound(ErrorMessages.MessagesErrorMessages.INVALID_ID,
                                () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        }

        /**
         * Purpose: Reject retrieval request when message ID is negative.
         * Expected Result: BadRequestException with InvalidId error code.
         */
        @Test
        @DisplayName("Get Message Details By ID - Negative message ID - Throws BadRequestException")
        void getMessageDetailsById_NegativeMessageId_ThrowsBadRequestException() {
                // Arrange

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.INVALID_ID,
                                () -> messageService.getMessageDetailsById(-1L));
        }

        /**
         * Purpose: Propagate repository exceptions when message lookup fails.
         * Expected Result: RuntimeException with original database error message.
         */
        @Test
        @DisplayName("Get Message Details By ID - Repository Error - Propagates Exception")
        void getMessageDetailsById_RepositoryError_Propagates() {
                // Arrange
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargetsThrows(
                                ErrorMessages.MessagesErrorMessages.LOOKUP_FAILED);

                // Act & Assert
                RuntimeException ex = assertThrows(RuntimeException.class,
                                () -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
                assertEquals(ErrorMessages.MessagesErrorMessages.LOOKUP_FAILED, ex.getMessage());
        }

        /**
         * Purpose: Verify that unauthorized access checks are handled at controller
         * level.
         * Scenario: Service layer is called for a valid ID (service does not enforce
         * auth).
         * Expected: Service returns valid result without throwing exceptions.
         */
        @Test
        @DisplayName("Get Message Details By ID - Unauthorized Access - Throws UnauthorizedException")
        void getMessageDetailsById_UnauthorizedAccess_ThrowsUnauthorizedException() {
                // Arrange
                stubMessageRepositoryFindByMessageIdAndClientIdWithTargets(Optional.of(testMessage));

                // Act & Assert
                assertDoesNotThrow(() -> messageService.getMessageDetailsById(TEST_MESSAGE_ID));
        }

        /**
         * Purpose: Reject retrieval request when message ID is zero.
         * Expected Result: BadRequestException with InvalidId error code.
         */
        @Test
        @DisplayName("Get Message Details By ID - Zero message ID - ThrowsBadRequestException")
        void getMessageDetailsById_ZeroMessageId_ThrowsBadRequestException() {
                // Arrange

                // Act & Assert
                assertThrowsBadRequest(ErrorMessages.MessagesErrorMessages.INVALID_ID,
                                () -> messageService.getMessageDetailsById(0L));
        }

        /*
         **********************************************************************************************
         * CONTROLLER AUTHORIZATION TESTS
         **********************************************************************************************
         */

        /**
         * Purpose: Verify unauthorized access is handled at the controller level.
         * Expected Result: Unauthorized status is returned.
         * Assertions: Response status is 401 UNAUTHORIZED.
         */
        @Test
        @DisplayName("getMessageDetailsById - Controller permission unauthorized - Success")
        void getMessageDetailsById_controller_permission_unauthorized() {
                // Arrange
                MessageController controller = new MessageController(messageServiceMock);
                PaginationBaseRequestModel pRequest = new PaginationBaseRequestModel();
                pRequest.setId(TEST_MESSAGE_ID);
                stubMessageServiceGetMessageDetailsByIdThrowsUnauthorized();

                // Act
                ResponseEntity<?> response = controller.getMessageDetailsById(pRequest);

                // Assert
                assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        }

        /**
         * Purpose: Verify that the getMessageDetailsById controller method is protected
         * by correct @PreAuthorize permission.
         * Expected: Method has @PreAuthorize referencing VIEW_MESSAGES_PERMISSION.
         */
        @Test
        @DisplayName("getMessageDetailsById - Verify @PreAuthorize annotation")
        void getMessageDetailsById_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
                // Arrange
                Method method = MessageController.class.getMethod(
                                "getMessageDetailsById",
                                com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class);

                // Act
                PreAuthorize preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);

                // Assert
                assertNotNull(preAuthorizeAnnotation,
                                "getMessageDetailsById method should have @PreAuthorize annotation");

                String expectedPermission = "@customAuthorization.hasAuthority('" +
                                Authorizations.VIEW_MESSAGES_PERMISSION + "')";

                assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                                "PreAuthorize annotation should reference VIEW_MESSAGES_PERMISSION");
        }

        /**
         * Purpose: Verify that the controller correctly delegates getMessageDetailsById
         * calls to the service layer.
         * Expected: Service method called with correct ID and HTTP 200 returned.
         */
        @Test
        @DisplayName("getMessageDetailsById - Controller delegates to service correctly")
        void getMessageDetailsById_WithValidRequest_DelegatesToService() {
                // Arrange
                MessageController controller = new MessageController(messageServiceMock);
                PaginationBaseRequestModel pRequest = new PaginationBaseRequestModel();
                pRequest.setId(TEST_MESSAGE_ID);
                stubMessageServiceGetMessageDetailsById(new MessageResponseModel(testMessage));

                // Act
                ResponseEntity<?> response = controller.getMessageDetailsById(pRequest);

                // Assert
                verify(messageServiceMock, times(1)).getMessageDetailsById(TEST_MESSAGE_ID);
                assertEquals(HttpStatus.OK, response.getStatusCode(),
                                "Should return HTTP 200 OK");
        }
}
