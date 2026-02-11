package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import com.example.SpringApi.Models.DatabaseModels.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PurchaseOrderService.getPurchaseOrderPDF method.
 *
 * Test count: 11 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 7 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - GetPurchaseOrderPDF Tests")
public class GetPurchaseOrderPDFTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 11

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify PDF generation succeeds for valid data.
     * Expected Result: PDF bytes are returned.
     * Assertions: Returned byte array is not null and not empty.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Success")
    void getPurchaseOrderPDF_Success() throws Exception {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.of(testAddress));

        User createdBy = new User();
        createdBy.setUserId(TEST_USER_ID);
        createdBy.setFirstName("Test");
        createdBy.setLastName("User");
        createdBy.setLoginName("testuser");
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(createdBy));

        Lead lead = new Lead();
        lead.setLeadId(TEST_LEAD_ID);
        lead.setFirstName("Lead");
        lead.setLastName("User");
        stubLeadRepositoryFindLeadWithDetails(lead);

        Client client = testClient;
        client.setGoogleCred(new GoogleCred());
        stubClientRepositoryFindById(Optional.of(client));

        stubShipmentRepositoryFindByOrderSummaryId(Collections.<Shipment>emptyList());
        stubShipmentProductRepositoryFindByShipmentId(Collections.<ShipmentProduct>emptyList());

        byte[] pdfBytes = new byte[] { 1, 2, 3 };

        try (org.mockito.MockedStatic<com.example.SpringApi.Helpers.HTMLHelper> htmlMock =
                     stubHtmlHelperReplaceBrTags("<html>ok</html>");
             org.mockito.MockedStatic<com.example.SpringApi.Helpers.PDFHelper> pdfMock =
                     stubPdfHelperConvertPurchaseOrderHtmlToPdf(pdfBytes)) {
            // ACT
            byte[] result = purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID);

            // ASSERT
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject missing purchase order.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Purchase Order Not Found - Throws NotFoundException")
    void getPurchaseOrderPDF_PurchaseOrderNotFound_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.PurchaseOrderErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /**
     * Purpose: Reject when OrderSummary is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message contains OrderSummary not found.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - OrderSummary Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_OrderSummaryMissing_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.empty());

        // ACT & ASSERT
        com.example.SpringApi.Exceptions.NotFoundException ex = assertThrows(
                com.example.SpringApi.Exceptions.NotFoundException.class,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
        assertTrue(ex.getMessage().contains("OrderSummary not found"));
    }

    /**
     * Purpose: Reject when shipping address is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId for address.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Address Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_AddressMissing_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /**
     * Purpose: Reject when created-by user is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId for user.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Created By User Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_CreatedByMissing_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.of(testAddress));
        stubUserRepositoryFindByUserIdAndClientId(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /**
     * Purpose: Reject when approved-by user is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId for user.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Approved By User Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_ApprovedByMissing_ThrowsNotFoundException() {
        // ARRANGE
        testPurchaseOrder.setApprovedByUserId(99L);
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.of(testAddress));
        stubUserRepositoryFindByUserIdAndClientId(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.UserErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /**
     * Purpose: Reject when lead is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId for lead.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Lead Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_LeadMissing_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.of(testAddress));
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubLeadRepositoryFindLeadWithDetails(null);

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.LeadsErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /**
     * Purpose: Reject when client is missing.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Message matches InvalidId for client.
     */
    @Test
    @DisplayName("Get Purchase Order PDF - Client Missing - Throws NotFoundException")
    void getPurchaseOrderPDF_ClientMissing_ThrowsNotFoundException() {
        // ARRANGE
        stubPurchaseOrderRepositoryFindByIdWithRelations(Optional.of(testPurchaseOrder));
        stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.of(testOrderSummary));
        stubAddressRepositoryFindById(Optional.of(testAddress));
        stubUserRepositoryFindByUserIdAndClientId(Optional.of(testUser));
        stubLeadRepositoryFindLeadWithDetails(testLead);
        stubClientRepositoryFindById(Optional.empty());

        // ACT & ASSERT
        assertThrowsNotFound(ErrorMessages.ClientErrorMessages.InvalidId,
                () -> purchaseOrderService.getPurchaseOrderPDF(TEST_PO_ID));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("getPurchaseOrderPDF - Controller Permission - Unauthorized")
    void getPurchaseOrderPDF_controller_permission_unauthorized() throws Exception {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnGetPdf();

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrderPDF(TEST_PO_ID);

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("getPurchaseOrderPDF - Verify @PreAuthorize Annotation")
    void getPurchaseOrderPDF_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("getPurchaseOrderPDF", long.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPurchaseOrderPDF");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference VIEW_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("getPurchaseOrderPDF - Controller delegates to service")
    void getPurchaseOrderPDF_WithValidId_DelegatesToService() throws Exception {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceGetPurchaseOrderPdf(new byte[] { 1 });

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrderPDF(TEST_PO_ID);

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}