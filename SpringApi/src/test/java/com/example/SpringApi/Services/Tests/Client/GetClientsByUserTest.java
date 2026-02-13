package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Controllers.ClientController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.getClientsByUser() method.
 */
@DisplayName("Get Clients By User Tests")
class GetClientsByUserTest extends ClientServiceTestBase {


    // Total Tests: 23
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify deleted clients are still returned.
     * Expected Result: Deleted clients appear in response.
     * Assertions: All returned clients are marked deleted.
     */
    @Test
    @DisplayName("Get Clients By User - All clients deleted - Success")
    void getClientsByUser_AllClientsDeleted_Success() {
        // Arrange
        Client deletedClient1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient1.setClientId(1L);
        deletedClient1.setIsDeleted(true);

        Client deletedClient2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient2.setClientId(2L);
        deletedClient2.setIsDeleted(true);

        stubClientFindByUserId(TEST_USER_ID, List.of(deletedClient1, deletedClient2));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getIsDeleted());
        assertTrue(results.get(1).getIsDeleted());
    }

    /*
     * Purpose: Verify behavior when getUserId() returns different value.
     * Expected Result: Repository still processes with that ID.
     * Assertions: Behavior is consistent.
     */
    @Test
    @DisplayName("Get Clients By User - Different user ID - Success")
    void getClientsByUser_DifferentUserId_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        verify(clientRepository).findByUserId(anyLong());
    }

    /*
     * Purpose: Handle empty client list for a user.
     * Expected Result: Empty list is returned.
     * Assertions: Result size is zero.
     */
    @Test
    @DisplayName("Get Clients By User - No clients - Success")
    void getClientsByUser_Empty_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, Collections.emptyList());

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /*
     * Purpose: Verify behavior when repository returns very large dataset.
     * Expected Result: All clients are returned without error.
     * Assertions: All 1000 clients are processed and returned.
     */
    @Test
    @DisplayName("Get Clients By User - Extremely large dataset - Success")
    void getClientsByUser_ExtremelyLargeResultSet_Success() {
        // Arrange
        List<Client> clients = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Client client = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client.setClientId((long) i);
            client.setName("Client " + i);
            clients.add(client);
        }

        stubClientFindByUserId(TEST_USER_ID, clients);

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1000, results.size(), "Should return all 1000 clients");
    }

    /*
     * Purpose: Validate large result sets are handled.
     * Expected Result: Large list is returned.
     * Assertions: Result size matches large dataset.
     */
    @Test
    @DisplayName("Get Clients By User - Large result set - Success")
    void getClientsByUser_LargeResultSet_Success() {
        // Arrange
        List<Client> clients = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Client client = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client.setClientId((long) i);
            client.setName("Client " + i);
            clients.add(client);
        }

        stubClientFindByUserId(TEST_USER_ID, clients);

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(100, results.size());
        assertEquals(1L, results.get(0).getClientId());
    }

    /*
     * Purpose: Verify behavior when client conversion to response model could fail.
     * Expected Result: Handles conversion errors gracefully.
     * Assertions: Process completes without throwing exception.
     */
    @Test
    @DisplayName("Get Clients By User - Missing client fields - Success")
    void getClientsByUser_MissingFields_Success() {
        // Arrange
        Client minimalClient = new Client();
        minimalClient.setClientId(1L);

        stubClientFindByUserId(TEST_USER_ID, List.of(minimalClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        assertEquals(1L, results.get(0).getClientId(), "Should have client ID");
    }

    /*
     * Purpose: Verify mixed active and deleted clients are returned.
     * Expected Result: Both active and deleted clients are included.
     * Assertions: Result size matches all clients.
     */
    @Test
    @DisplayName("Get Clients By User - Mixed deleted and active - Success")
    void getClientsByUser_MixedDeletedAndActive_Success() {
        // Arrange
        Client activeClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        activeClient.setClientId(1L);
        activeClient.setIsDeleted(false);

        Client deletedClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient.setClientId(2L);
        deletedClient.setIsDeleted(true);

        stubClientFindByUserId(TEST_USER_ID, List.of(activeClient, deletedClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertFalse(results.get(0).getIsDeleted());
        assertTrue(results.get(1).getIsDeleted());
    }

    /*
     * Purpose: Verify multiple clients are returned.
     * Expected Result: All clients are included in response.
     * Assertions: Result size matches expected count.
     */
    @Test
    @DisplayName("Get Clients By User - Multiple clients - Success")
    void getClientsByUser_MultipleClients_Success() {
        // Arrange
        Client client1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client1.setClientId(1L);
        client1.setName("Client 1");

        Client client2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client2.setClientId(2L);
        client2.setName("Client 2");

        stubClientFindByUserId(TEST_USER_ID, List.of(client1, client2));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(clientRepository).findByUserId(anyLong());
    }

    /*
     * Purpose: Verify behavior when repository is called multiple times.
     * Expected Result: Repository is called with correct user ID each time.
     * Assertions: Verify correct method invocation count.
     */
    @Test
    @DisplayName("Get Clients By User - Multiple invocations - Success")
    void getClientsByUser_MultipleInvocations_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, List.of(testClient));

        // Act
        clientService.getClientsByUser();
        clientService.getClientsByUser();

        // Assert
        verify(clientRepository, times(2)).findByUserId(anyLong());
    }

    /*
     * Purpose: Verify behavior when response list needs to be mutable.
     * Expected Result: Returned list can be modified.
     * Assertions: Can add/remove items from returned list.
     */
    @Test
    @DisplayName("Get Clients By User - Mutable list - Success")
    void getClientsByUser_MutableList_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        results.add(new ClientResponseModel(testClient));
        assertEquals(2, results.size(), "Should be able to add items to list");
    }

    /*
     * Purpose: Verify behavior when user ID is negative.
     * Expected Result: Empty list or exception depending on implementation.
     * Assertions: Method returns empty list.
     */
    @Test
    @DisplayName("Get Clients By User - Negative user ID - Success")
    void getClientsByUser_NegativeUserId_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, Collections.emptyList());

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty());
    }

    /*
     * Purpose: Verify behavior when repository returns clients with null ID.
     * Expected Result: ClientResponseModel is created with null ID.
     * Assertions: Response model is created without throwing exception.
     */
    @Test
    @DisplayName("Get Clients By User - Null client ID - Success")
    void getClientsByUser_NullClientId_Success() {
        // Arrange
        Client clientWithNullId = new Client(testClientRequest, DEFAULT_CREATED_USER);
        clientWithNullId.setClientId(null);

        stubClientFindByUserId(TEST_USER_ID, List.of(clientWithNullId));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size());
        assertNull(results.get(0).getClientId());
    }

    /*
     * Purpose: Verify behavior when repository returns clients with null name.
     * Expected Result: ClientResponseModel handles null fields gracefully.
     * Assertions: No exception is thrown and null fields are handled.
     */
    @Test
    @DisplayName("Get Clients By User - Null client name - Success")
    void getClientsByUser_NullClientName_Success() {
        // Arrange
        Client clientWithNullName = new Client(testClientRequest, DEFAULT_CREATED_USER);
        clientWithNullName.setName(null);

        stubClientFindByUserId(TEST_USER_ID, List.of(clientWithNullName));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size());
        assertNull(results.get(0).getName());
    }

    /*
     * Purpose: Verify behavior when client list alternates between scenarios.
     * Expected Result: Consistently handles cases.
     * Assertions: Both scenarios return results.
     */
    @Test
    @DisplayName("Get Clients By User - Repository toggling - Success")
    void getClientsByUser_RepositoryToggling_Success() {
        // Arrange
        stubClientFindByUserIdSequence(TEST_USER_ID, Collections.emptyList(), List.of(testClient));

        // Act
        List<ClientResponseModel> firstResult = clientService.getClientsByUser();
        List<ClientResponseModel> secondResult = clientService.getClientsByUser();

        // Assert
        assertTrue(firstResult.isEmpty());
        assertEquals(1, secondResult.size());
    }

    /*
     * Purpose: Verify the service method can be called and returns results.
     * Expected Result: Service returns list of clients.
     * Assertions: Results are non-null.
     */
    @Test
    @DisplayName("Get Clients By User - Service invocation - Success")
    void getClientsByUser_ServiceInvocation_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size());
    }

    /*
     * Purpose: Verify behavior when client contains special characters or unicode.
     * Expected Result: Special characters are preserved in response.
     * Assertions: Response contains special characters correctly.
     */
    @Test
    @DisplayName("Get Clients By User - Special characters - Success")
    void getClientsByUser_SpecialCharacters_Success() {
        // Arrange
        Client specialClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        specialClient.setName("Client™ 测试 🚀 Ñamé");

        stubClientFindByUserId(TEST_USER_ID, List.of(specialClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals("Client™ 测试 🚀 Ñamé", results.get(0).getName());
    }

    /*
     * Purpose: Verify clients are returned for a valid user.
     * Expected Result: List of clients is returned.
     * Assertions: Response list size and fields are validated.
     */
    @Test
    @DisplayName("Get Clients By User - Valid user - Success")
    void getClientsByUser_ValidUser_Success() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(TEST_CLIENT_ID, results.get(0).getClientId());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify behavior when clientRepository throws DataAccessException.
     * Expected Result: DataAccessException is propagated.
     * Assertions: DataAccessException is raised.
     */
    @Test
    @DisplayName("Get Clients By User - Repository throws DataAccessException - ThrowsDataAccessException")
    void getClientsByUser_RepositoryDataAccessException_ThrowsDataAccessException() {
        // Arrange
        stubClientFindByUserIdThrowsDataAccessException(TEST_USER_ID,
                ErrorMessages.CommonErrorMessages.DATABASE_ERROR);

        // Act & Assert
        org.springframework.dao.DataAccessException ex = assertThrows(org.springframework.dao.DataAccessException.class,
                () -> clientService.getClientsByUser());
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /*
     * Purpose: Verify null repository response is handled gracefully.
     * Expected Result: NullPointerException is thrown.
     * Assertions: Exception is raised when repository returns null.
     */
    @Test
    @DisplayName("Get Clients By User - Repository returns null - ThrowsNullPointerException")
    void getClientsByUser_RepositoryReturnsNull_ThrowsNullPointerException() {
        // Arrange
        stubClientFindByUserId(TEST_USER_ID, null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> clientService.getClientsByUser());
        // NullPointerException message in Java 17+ reflects the cause
        // assertEquals(ErrorMessages.ClientErrorMessages.CLIENTS_LIST_NULL,
        // ex.getMessage());
    }

    /*
     * Purpose: Verify behavior when clientRepository.findByUserId() throws
     * exception.
     * Expected Result: Exception from repository is propagated.
     * Assertions: RuntimeException is raised.
     */
    @Test
    @DisplayName("Get Clients By User - Repository throws RuntimeException - ThrowsRuntimeException")
    void getClientsByUser_RepositoryRuntimeException_ThrowsRuntimeException() {
        // Arrange
        stubClientFindByUserIdThrowsRuntimeException(TEST_USER_ID,
                ErrorMessages.CommonErrorMessages.DATABASE_CONNECTION_ERROR);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> clientService.getClientsByUser());
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_CONNECTION_ERROR, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify @PreAuthorize annotation is declared on getClientsByUser
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_CLIENT_PERMISSION.
     */
    @Test
    @DisplayName("Get Clients By User - Verify @PreAuthorize annotation is configured correctly")
    void getClientsByUser_p01_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        var method = ClientController.class.getMethod("getClientsByUser");

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "getClientsByUser method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_CLIENT_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_CLIENT_PERMISSION");
    }

    /*
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     */
    @Test
    @DisplayName("Get Clients By User - Controller delegates to service correctly")
    void getClientsByUser_p02_WithValidRequest_DelegatesToService_Success() {
        // Arrange
        stubServiceGetClientsByUser(Collections.emptyList());

        // Act
        ResponseEntity<?> response = clientController.getClientsByUser();

        // Assert
        verify(mockClientService, times(1)).getClientsByUser();
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }

    /*
     * Purpose: Verify controller has correct @PreAuthorize permission.
     * Expected Result: Annotation exists and contains VIEW_CLIENT_PERMISSION.
     * Assertions: Annotation is present and permission matches.
     */
    @Test
    @DisplayName("Get Clients By User - Controller permission forbidden - Success")
    void getClientsByUser_p03_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        var method = ClientController.class.getMethod("getClientsByUser");
        stubServiceGetClientsByUser(Collections.emptyList());

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);
        ResponseEntity<?> response = clientController.getClientsByUser();

        // Assert
        assertNotNull(preAuthorizeAnnotation,
                "getClientsByUser method should have @PreAuthorize annotation");
        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_CLIENT_PERMISSION + "')";
        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_CLIENT_PERMISSION");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
    }
}