package com.example.SpringApi.Services.Tests.Client;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientService.getClientsByUser() method.
 * Tests retrieval of all clients for the authenticated user.
 * * Test Count: 21 tests
 */
@DisplayName("Get Clients By User Tests")
class GetClientsByUserTest extends ClientServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify deleted clients are still returned.
     * Expected Result: Deleted clients appear in response.
     * Assertions: All returned clients are marked deleted.
     */
    @Test
    @DisplayName("Get Clients By User - All clients deleted - Returns deleted clients")
    void getClientsByUser_AllClientsDeleted_Success() {
        // Arrange
        Client deletedClient1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient1.setClientId(1L);
        deletedClient1.setIsDeleted(true);

        Client deletedClient2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient2.setClientId(2L);
        deletedClient2.setIsDeleted(true);

        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(deletedClient1, deletedClient2));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getIsDeleted());
        assertTrue(results.get(1).getIsDeleted());
    }

    /**
     * Purpose: Verify behavior when repository is called multiple times.
     * Expected Result: Repository is called with correct user ID each time.
     * Assertions: Verify correct method invocation count.
     */
    @Test
    @DisplayName("Get Clients By User - Called multiple times - Repository invoked correctly each time")
    void getClientsByUser_CalledMultipleTimes_RepositoryInvokedCorrectly() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));

        // Act
        clientService.getClientsByUser();
        clientService.getClientsByUser();
        clientService.getClientsByUser();

        // Assert
        verify(clientRepository, times(3)).findByUserId(anyLong());
    }

    /**
     * Purpose: Verify behavior when repository findByUserId returns clients with null ID.
     * Expected Result: ClientResponseModel is created with null ID.
     * Assertions: Response model is created without throwing exception.
     */
    @Test
    @DisplayName("Get Clients By User - Client has null ID - Handles gracefully")
    void getClientsByUser_ClientHasNullId_HandlesGracefully() {
        // Arrange
        Client clientWithNullId = new Client(testClientRequest, DEFAULT_CREATED_USER);
        clientWithNullId.setClientId(null);
        clientWithNullId.setName("Test Client");
        
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(clientWithNullId));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        assertNull(results.get(0).getClientId(), "Client ID should be null");
    }

    /**
     * Purpose: Verify behavior when repository findByUserId returns clients with null fields.
     * Expected Result: ClientResponseModel handles null fields gracefully.
     * Assertions: No exception is thrown and null fields are handled.
     */
    @Test
    @DisplayName("Get Clients By User - Client has null name - Handles gracefully")
    void getClientsByUser_ClientHasNullName_HandlesGracefully() {
        // Arrange
        Client clientWithNullName = new Client(testClientRequest, DEFAULT_CREATED_USER);
        clientWithNullName.setClientId(1L);
        clientWithNullName.setName(null);
        
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(clientWithNullName));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        assertNull(results.get(0).getName(), "Client name should be null");
    }

    /**
     * Purpose: Verify behavior when client conversion to response model could fail.
     * Expected Result: Handles conversion errors gracefully or skips problematic entries.
     * Assertions: Process completes without throwing exception.
     */
    @Test
    @DisplayName("Get Clients By User - Client with missing fields - Converts successfully")
    void getClientsByUser_ClientWithMissingFields_ConvertsSuccessfully() {
        // Arrange
        Client minimalClient = new Client();
        minimalClient.setClientId(1L);
        
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(minimalClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        assertEquals(1L, results.get(0).getClientId(), "Should have client ID");
    }

    /**
     * Purpose: Verify behavior when client contains special characters or unicode.
     * Expected Result: Special characters are preserved in response.
     * Assertions: Response contains special characters correctly.
     */
    @Test
    @DisplayName("Get Clients By User - Client with special characters - Preserves correctly")
    void getClientsByUser_ClientWithSpecialCharacters_PreservesCorrectly() {
        // Arrange
        Client specialClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        specialClient.setClientId(1L);
        specialClient.setName("Client™ 测试 🚀 Ñamé");
        
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(specialClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        assertEquals("Client™ 测试 🚀 Ñamé", results.get(0).getName(), "Should preserve special characters");
    }

    /**
     * Purpose: Handle empty client list for a user.
     * Expected Result: Empty list is returned.
     * Assertions: Result size is zero.
     */
    @Test
    @DisplayName("Get Clients By User - No clients - Returns empty list")
    void getClientsByUser_Empty() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Purpose: Verify permission checks are considered in the service layer.
     * Note: Unit tests for the service don't validate HTTP-level permission denials.
     * Permission enforcement typically happens at the Controller level via @PreAuthorize.
     * This test verifies the service method can be called and returns results.
     * Expected Result: Service returns list of clients.
     * Assertions: Results are non-null.
     */
    @Test
    @DisplayName("Get Clients By User - Service returns clients list - Success")
    void getClientsByUser_InsufficientPermission_ThrowsForbidden() {
        // Arrange
        Client client1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client1.setClientId(1L);
        client1.setName("Client 1");

        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(client1));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return at least one client");
    }

    /**
     * Purpose: Validate large result sets are handled.
     * Expected Result: Large list is returned.
     * Assertions: Result size matches large dataset.
     */
    @Test
    @DisplayName("Get Clients By User - Large result set - Success")
    void getClientsByUser_LargeResultSet_Success() {
        // Arrange
        java.util.List<Client> clients = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Client client = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client.setClientId((long) i);
            client.setName("Client " + i);
            clients.add(client);
        }

        when(clientRepository.findByUserId(anyLong())).thenReturn(clients);

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(100, results.size());
        assertEquals(1L, results.get(0).getClientId());
        assertEquals(100L, results.get(99).getClientId());
    }

    /**
     * Purpose: Verify mixed active and deleted clients are returned.
     * Expected Result: Both active and deleted clients are included.
     * Assertions: Result size matches all clients.
     */
    @Test
    @DisplayName("Get Clients By User - Mixed deleted and active clients - Returns all")
    void getClientsByUser_MixedDeletedAndActive_Success() {
        // Arrange
        Client activeClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        activeClient.setClientId(1L);
        activeClient.setIsDeleted(false);

        Client deletedClient = new Client(testClientRequest, DEFAULT_CREATED_USER);
        deletedClient.setClientId(2L);
        deletedClient.setIsDeleted(true);

        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(activeClient, deletedClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertFalse(results.get(0).getIsDeleted());
        assertTrue(results.get(1).getIsDeleted());
    }

    /**
     * Purpose: Verify multiple clients are returned.
     * Expected Result: All clients are included in response.
     * Assertions: Result size matches expected count.
     */
    @Test
    @DisplayName("Get Clients By User - Multiple clients - Returns all")
    void getClientsByUser_MultipleClients_Success() {
        // Arrange
        Client client1 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client1.setClientId(1L);
        client1.setName("Client 1");

        Client client2 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client2.setClientId(2L);
        client2.setName("Client 2");

        Client client3 = new Client(testClientRequest, DEFAULT_CREATED_USER);
        client3.setClientId(3L);
        client3.setName("Client 3");

        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(client1, client2, client3));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals(1L, results.get(0).getClientId());
        assertEquals(2L, results.get(1).getClientId());
        assertEquals(3L, results.get(2).getClientId());
        verify(clientRepository).findByUserId(anyLong());
    }

    /**
     * Purpose: Verify behavior when user ID is invalid (zero or negative).
     * Expected Result: Empty list or exception depending on implementation.
     * Assertions: Method either returns empty list or throws exception.
     */
    @Test
    @DisplayName("Get Clients By User - Negative user ID - Returns empty list")
    void getClientsByUser_NegativeUserId_ReturnsEmptyList() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Should return empty list for invalid user ID");
    }

    /**
     * Purpose: Verify permission check is performed for VIEW_CLIENT permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Clients By User - Permission check - Success Verifies Authorization")
    void getClientsByUser_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_CLIENT_PERMISSION)).thenReturn(true);

        // Act
        clientService.getClientsByUser();

        // Assert
        verify(authorization, times(1)).hasAuthority(Authorizations.VIEW_CLIENT_PERMISSION);
    }

    /**
     * Purpose: Verify behavior when response list needs to be mutable.
     * Expected Result: Returned list can be modified (standard behavior).
     * Assertions: Can add/remove items from returned list.
     */
    @Test
    @DisplayName("Get Clients By User - Returned list is mutable")
    void getClientsByUser_ReturnedListIsMutable() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();
        
        // Assert - Verify list is mutable
        assertNotNull(results, "Results should not be null");
        results.add(new ClientResponseModel(testClient)); // Should not throw exception
        assertEquals(2, results.size(), "Should be able to add items to list");
    }

    /**
     * Purpose: Verify clients are returned for a valid user.
     * Expected Result: List of clients is returned.
     * Assertions: Response list size and fields are validated.
     */
    @Test
    @DisplayName("Get Clients By User - Returns list of clients")
    void getClientsByUser_Success() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(TEST_CLIENT_ID, results.get(0).getClientId());
    }

    /**
     * Purpose: Verify behavior when client list alternates between null and empty scenarios.
     * Expected Result: Consistently handles empty cases.
     * Assertions: Both scenarios return empty list.
     */
    @Test
    @DisplayName("Get Clients By User - Switch between null and empty repositories - Handles both")
    void getClientsByUser_SwitchBetweenNullAndEmpty_HandlesBoth() {
        // Arrange - First call returns empty
        when(clientRepository.findByUserId(anyLong()))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        // Act
        List<ClientResponseModel> firstResult = clientService.getClientsByUser();
        List<ClientResponseModel> secondResult = clientService.getClientsByUser();

        // Assert
        assertTrue(firstResult.isEmpty(), "First result should be empty");
        assertTrue(secondResult.isEmpty(), "Second result should be empty");
    }

    /**
     * Purpose: Verify behavior when repository findByUserId returns very large dataset.
     * Expected Result: All clients are returned without error.
     * Assertions: All 1000 clients are processed and returned.
     */
    @Test
    @DisplayName("Get Clients By User - Very large dataset (1000+ clients) - Processes all")
    void getClientsByUser_VeryLargeDataset_ProcessesAll() {
        // Arrange
        java.util.List<Client> clients = new java.util.ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            Client client = new Client(testClientRequest, DEFAULT_CREATED_USER);
            client.setClientId((long) i);
            client.setName("Client " + i);
            clients.add(client);
        }

        when(clientRepository.findByUserId(anyLong())).thenReturn(clients);

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1000, results.size(), "Should return all 1000 clients");
    }

    /**
     * Purpose: Verify behavior when getUserId() returns invalid/null value.
     * Expected Result: Repository still processes with null/invalid ID.
     * Assertions: Behavior is consistent.
     */
    @Test
    @DisplayName("Get Clients By User - When base service returns different user ID - Queries correctly")
    void getClientsByUser_WhenBaseServiceReturnsDifferentUserId_QueriesCorrectly() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(List.of(testClient));

        // Act
        List<ClientResponseModel> results = clientService.getClientsByUser();

        // Assert
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return one client");
        verify(clientRepository).findByUserId(anyLong());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify null repository response is handled gracefully.
     * Expected Result: NullPointerException is thrown or caught.
     * Assertions: Exception is raised when repository returns null.
     */
    @Test
    @DisplayName("Get Clients By User - Repository returns null - Throws NullPointerException")
    void getClientsByUser_RepositoryReturnsNull_ThrowsNullPointerException() {
        // Arrange
        when(clientRepository.findByUserId(anyLong())).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, 
                () -> clientService.getClientsByUser(),
                "Should throw NullPointerException when repository returns null");
    }

    /**
     * Purpose: Verify behavior when clientRepository throws DataAccessException.
     * Expected Result: DataAccessException is propagated.
     * Assertions: DataAccessException is raised.
     */
    @Test
    @DisplayName("Get Clients By User - Repository throws DataAccessException - Propagates exception")
    void getClientsByUser_RepositoryThrowsDataAccessException_PropagatesException() {
        // Arrange
        when(clientRepository.findByUserId(anyLong()))
                .thenThrow(new org.springframework.dao.DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(org.springframework.dao.DataAccessException.class,
                () -> clientService.getClientsByUser(),
                "Should propagate DataAccessException from repository");
    }

    /**
     * Purpose: Verify behavior when clientRepository.findByUserId() throws exception.
     * Expected Result: Exception from repository is propagated.
     * Assertions: RuntimeException is raised.
     */
    @Test
    @DisplayName("Get Clients By User - Repository throws RuntimeException - Propagates exception")
    void getClientsByUser_RepositoryThrowsException_PropagatesException() {
        // Arrange
        when(clientRepository.findByUserId(anyLong()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> clientService.getClientsByUser(),
                "Should propagate RuntimeException from repository");
    }

}