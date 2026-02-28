package springapi.ServiceTests.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.MessageController;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.Message;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.responsemodels.MessageResponseModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;

/** Unit tests for MessageService.getMessagesInBatches method. */
@DisplayName("GetMessagesInBatches Tests")
class GetMessagesInBatchesTest extends MessageServiceTestBase {

  // Total Tests: 7
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful retrieval of messages in batches without filters. Scenario: Call
   * getMessagesInBatches with valid pagination range. Expected: Paginated response containing list
   * of messages and total count.
   */
  @Test
  @DisplayName("Get Messages In Batches - Simple Retrieval - Success")
  void getMessagesInBatches_SimpleRetrieval_Success() {
    // Arrange
    PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
    paginationRequest.setStart(0);
    paginationRequest.setEnd(10);
    paginationRequest.setFilters(null);
    paginationRequest.setIncludeDeleted(false);

    List<Message> messages = Arrays.asList(testMessage);
    Page<Message> messagePage = new PageImpl<>(messages);

    stubMessageRepositoryFindPaginatedMessages(messagePage);

    // Act
    PaginationBaseResponseModel<MessageResponseModel> result =
        messageService.getMessagesInBatches(paginationRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertEquals(1, result.getTotalDataCount());
    assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
  }

  /**
   * Purpose: Verify that batch retrieval works correctly with all supported column filters.
   * Scenario: Call getMessagesInBatches for each valid filter column. Expected: No exceptions
   * thrown and repository called for each valid column.
   */
  @Test
  @DisplayName("Get Messages In Batches - Valid Column Filters - Success")
  void getMessagesInBatches_ValidColumnFilters_Success() {
    // Arrange
    PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
    String[] validColumns = {
      "messageId", "title", "publishDate", "descriptionHtml", "sendAsEmail", "isDeleted"
    };

    for (String validCol : validColumns) {
      paginationRequest.setFilters(List.of(createFilterCondition(validCol, "equals", "test")));
      stubMessageRepositoryFindPaginatedMessages(new PageImpl<>(Arrays.asList()));

      // Act & Assert
      assertDoesNotThrow(() -> messageService.getMessagesInBatches(paginationRequest));
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject batch retrieval request when filter column names are not recognized. Expected
   * Result: BadRequestException with InvalidColumnName error message.
   */
  @Test
  @DisplayName("Get Messages In Batches - Invalid Column Filters - Throws BadRequestException")
  void getMessagesInBatches_InvalidColumnFilters_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
    String[] invalidColumns = {"invalidColumn", "nonExistentField"};

    for (String invalidCol : invalidColumns) {
      // Arrange
      paginationRequest.setFilters(List.of(createFilterCondition(invalidCol, "equals", "test")));

      // Act & Assert
      BadRequestException ex =
          assertThrows(
              BadRequestException.class,
              () -> messageService.getMessagesInBatches(paginationRequest));
      assertEquals(
          String.format(ErrorMessages.CommonErrorMessages.INVALID_COLUMN_NAME, invalidCol),
          ex.getMessage());
    }
  }

  /**
   * Purpose: Reject batch retrieval request when end index is less than or equal to start index.
   * Expected Result: BadRequestException with InvalidPagination error message.
   */
  @Test
  @DisplayName("Get Messages In Batches - Invalid Pagination Range - Throws BadRequestException")
  void getMessagesInBatches_InvalidPagination_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
    paginationRequest.setStart(10);
    paginationRequest.setEnd(5);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.CommonErrorMessages.INVALID_PAGINATION,
        () -> messageService.getMessagesInBatches(paginationRequest));
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getMessagesInBatches - Controller permission unauthorized - Success")
  void getMessagesInBatches_controller_permission_unauthorized() {
    // Arrange
    MessageController controller = new MessageController(messageServiceMock);
    stubMessageServiceGetMessagesInBatchesThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getMessagesInBatches(createValidPaginationRequest());

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify that the controller correctly delegates getMessagesInBatches calls to the
   * service layer. Expected: Service method called with correct request and HTTP 200 returned.
   */
  @Test
  @DisplayName("getMessagesInBatches - Controller delegates to service correctly")
  void getMessagesInBatches_WithValidRequest_DelegatesToService() {
    // Arrange
    MessageController controller = new MessageController(messageServiceMock);
    PaginationBaseRequestModel pRequest = createValidPaginationRequest();
    pRequest.setFilters(null);

    PaginationBaseResponseModel<MessageResponseModel> mockResponse =
        new PaginationBaseResponseModel<>();
    stubMessageServiceGetMessagesInBatches(mockResponse);

    // Act
    ResponseEntity<?> response = controller.getMessagesInBatches(pRequest);

    // Assert
    verify(messageServiceMock, times(1)).getMessagesInBatches(pRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
