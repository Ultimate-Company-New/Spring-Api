//package com.example.SpringApi.Services.Tests.Message;
//
//import com.example.SpringApi.Controllers.MessageController;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import com.example.SpringApi.Models.ResponseModels.MessageResponseModel;
//import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
//import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
//import com.example.SpringApi.Models.Authorizations;
//import com.example.SpringApi.Models.DatabaseModels.Message;
//import com.example.SpringApi.ErrorMessages;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.access.prepost.PreAuthorize;
//
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for MessageService.getMessagesInBatches method.
// */
//@DisplayName("GetMessagesInBatches Tests")
//public class GetMessagesInBatchesTest extends MessageServiceTestBase {
//
//    // Total Tests: 3
//
//    /*
//     **********************************************************************************************
//     * SUCCESS TESTS
//     **********************************************************************************************
//     */
//
//    /**
//     * Purpose: Validate pagination, filter column validation, and successful retrieval in one flow.
//     * Expected Result: Invalid inputs throw expected errors, and valid inputs return a populated response.
//     * Assertions: totalDataCount and first message ID are verified on success.
//     */
//    @Test
//    @DisplayName("Get Messages In Batches - Invalid pagination, success no filters, and column validation")
//    void getMessagesInBatches_SingleComprehensiveTest() {
//        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
//
//        // (1) Invalid pagination: end <= start
//        paginationRequest.setStart(10);
//        paginationRequest.setEnd(5);
//        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
//                () -> messageService.getMessagesInBatches(paginationRequest));
//
//        // (2) Success: simple retrieval without filters
//        paginationRequest.setStart(0);
//        paginationRequest.setEnd(10);
//        paginationRequest.setFilters(null);
//        paginationRequest.setIncludeDeleted(false);
//
//        List<Message> messages = Arrays.asList(testMessage);
//        Page<Message> messagePage = new PageImpl<>(messages);
//
//        when(messageRepository.findPaginatedMessages(anyLong(), isNull(), isNull(), isNull(), anyBoolean(), any(Pageable.class))).thenReturn(messagePage);
//
//        PaginationBaseResponseModel<MessageResponseModel> result = messageService.getMessagesInBatches(paginationRequest);
//
//        assertNotNull(result);
//        assertEquals(1, result.getData().size());
//        assertEquals(1, result.getTotalDataCount());
//        assertEquals(TEST_MESSAGE_ID, result.getData().get(0).getMessageId());
//
//        // (3) Column validation logic checks
//        String[] validColumns = {"messageId", "title", "publishDate", "descriptionHtml", "sendAsEmail", "isDeleted"};
//        String[] invalidColumns = {"invalidColumn", "nonExistentField"};
//
//        for (String invalidCol : invalidColumns) {
//            paginationRequest.setFilters(List.of(createFilterCondition(invalidCol, "equals", "test")));
//            assertThrowsBadRequest(String.format(ErrorMessages.CommonErrorMessages.InvalidColumnName, invalidCol), () -> messageService.getMessagesInBatches(paginationRequest));
//        }
//
//        for (String validCol : validColumns) {
//            paginationRequest.setFilters(List.of(createFilterCondition(validCol, "equals", "test")));
//            when(messageRepository.findPaginatedMessages(anyLong(), any(), any(), any(), anyBoolean(), any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList()));
//            assertDoesNotThrow(() -> messageService.getMessagesInBatches(paginationRequest));
//        }
//    }
//
//
//
//    /*
//     **********************************************************************************************
//     * CONTROLLER AUTHORIZATION TESTS
//     **********************************************************************************************
//     */
//
//    @Test
//    @DisplayName("getMessagesInBatches - Verify @PreAuthorize annotation")
//    void getMessagesInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
//        Method method = MessageController.class.getMethod(
//            "getMessagesInBatches",
//            PaginationBaseRequestModel.class
//        );
//
//        PreAuthorize preAuthorizeAnnotation = method.getAnnotation(PreAuthorize.class);
//
//        assertNotNull(preAuthorizeAnnotation,
//            "getMessagesInBatches method should have @PreAuthorize annotation");
//
//        String expectedPermission = "@customAuthorization.hasAuthority('"+
//            Authorizations.VIEW_MESSAGES_PERMISSION +"')";
//
//        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
//            "PreAuthorize annotation should reference VIEW_MESSAGES_PERMISSION");
//    }
//
//    @Test
//    @DisplayName("getMessagesInBatches - Controller delegates to service correctly")
//    void getMessagesInBatches_WithValidRequest_DelegatesToService() {
//        MessageController controller = new MessageController(messageServiceMock);
//        PaginationBaseRequestModel request = createValidPaginationRequest();
//        request.setFilters(null);
//
//        PaginationBaseResponseModel<MessageResponseModel> mockResponse = new PaginationBaseResponseModel<>();
//        when(messageServiceMock.getMessagesInBatches(any(PaginationBaseRequestModel.class))).thenReturn(mockResponse);
//
//        ResponseEntity<?> response = controller.getMessagesInBatches(request);
//
//        verify(messageServiceMock, times(1)).getMessagesInBatches(request);
//        assertEquals(HttpStatus.OK, response.getStatusCode(),
//            "Should return HTTP 200 OK");
//    }
//}