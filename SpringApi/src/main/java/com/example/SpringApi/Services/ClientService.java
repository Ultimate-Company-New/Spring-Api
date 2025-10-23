package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import org.springframework.core.env.Environment;
import jakarta.servlet.http.HttpServletRequest;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Services.Interface.IClientSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.FirebaseHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.io.File;

/**
 * Service class for managing Client-related business operations.
 * 
 * This service implements the IClientSubTranslator interface and provides
 * comprehensive client management functionality including CRUD operations,
 * client retrieval, and client status management. The service handles
 * validation, error handling, audit logging, and database persistence.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ClientService extends BaseService implements IClientSubTranslator {
    private final ClientRepository clientRepository;
    private final UserLogService userLogService;
    private final GoogleCredRepository googleCredRepository;
    private final Environment environment;

    @Autowired
    public ClientService(HttpServletRequest request,
                        UserLogService userLogService,
                        ClientRepository clientRepository,
                        GoogleCredRepository googleCredRepository,
                        Environment environment) {
        super();
        this.userLogService = userLogService;
        this.clientRepository = clientRepository;
        this.googleCredRepository = googleCredRepository;
        this.environment = environment;
    }

    /**
     * Toggles the deletion status of a client by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the client is currently active (isDeleted = false), it will be marked as deleted.
     * If the client is currently deleted (isDeleted = true), it will be restored.
     * The operation is logged for audit purposes.
     * 
     * @param clientId The unique identifier of the client to toggle
     * @throws NotFoundException if the client was not found
     */
    @Override
    public void toggleClient(long clientId) {
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            client.get().setIsDeleted(!client.get().getIsDeleted());
            clientRepository.save(client.get());
            userLogService.logData(getUserId(), SuccessMessages.ClientSuccessMessages.ToggleClient + " " + client.get().getClientId(),
                    ApiRoutes.ClientSubRoute.TOGGLE_CLIENT);
        } else {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
    }

    /**
     * Retrieves a single client by its unique identifier.
     * 
     * This method fetches a client from the database using the provided ID.
     * The returned ClientResponseModel contains all client details including
     * name, description, integration settings, and metadata.
     * 
     * @param clientId The unique identifier of the client to retrieve
     * @return ClientResponseModel containing the client information
     * @throws NotFoundException if no client exists with the given ID
     */
    @Override
    public ClientResponseModel getClientById(long clientId) {
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            return new ClientResponseModel(client.get());
        } else {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
    }

    /**
     * Creates a new client in the system.
     * 
     * This method validates the provided client data, creates a new Client entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. The operation is logged
     * for audit purposes.
     * 
     * @param clientRequest The ClientRequestModel containing the client data to insert
     */
    @Override
    public void createClient(ClientRequestModel clientRequest) {
        Client client = new Client(clientRequest, getUser());
        Client savedClient = clientRepository.save(client);
        
        // Upload logo if present
        if (clientRequest.getLogoBase64() != null &&
                !clientRequest.getLogoBase64().isEmpty() &&
                !clientRequest.getLogoBase64().isBlank()) {
            
            // Get GoogleCred for the client
            Optional<GoogleCred> googleCred = googleCredRepository.findById(savedClient.getGoogleCredId());
            
            if (googleCred.isPresent()) {
                String filePath = savedClient.getName() + " - " + savedClient.getClientId()
                        + File.separator
                        + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default")
                        + File.separator
                        + "Logo.png";

                // Create FirebaseHelper with GoogleCred object
                GoogleCred googleCredData = googleCred.get();
                FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);
                boolean isSuccess = firebaseHelper.uploadFileToFirebase(clientRequest.getLogoBase64(), filePath);
                if (!isSuccess) {
                    throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidLogoUpload);
                }
            } else {
                throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
            }
        }
        
        userLogService.logData(getUserId(), SuccessMessages.ClientSuccessMessages.CreateClient + " " + savedClient.getClientId(),
            "createClient");
    }

    /**
     * Updates an existing client with new information.
     * 
     * This method retrieves the existing client by ID, validates the new data,
     * and updates the client while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * The operation is logged for audit purposes.
     * 
     * @param clientRequest The ClientRequestModel containing the updated client data
     * @throws NotFoundException if no client exists with the given ID
     */
    @Override
    public void updateClient(ClientRequestModel clientRequest) {
        // TODO: We need to add code to update the client logo
        Optional<Client> existingClient = clientRepository.findById(clientRequest.getClientId());
        if (existingClient.isPresent()) {
            Client client = new Client(clientRequest, getUser(), existingClient.get());
            Client updatedClient = clientRepository.save(client);
            userLogService.logData(getUserId(), SuccessMessages.ClientSuccessMessages.UpdateClient + " " + updatedClient.getClientId(),
                    "updateClient");
        } else {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.InvalidId);
        }
    }

    /**
     * Retrieves all clients mapped to the current user.
     * 
     * This method fetches all clients where the current user has a mapping
     * in the UserClientMapping table. The method returns a list of 
     * ClientResponseModel objects, each containing complete client information.
     * Returns an empty list if no clients are mapped to the user.
     * 
     * @return List of ClientResponseModel objects for the user's mapped clients
     */
    @Override
    public List<ClientResponseModel> getClientsByUser() {
        List<Client> clients = clientRepository.findByUserId(getUserId());
        List<ClientResponseModel> responseModels = new ArrayList<>();
        
        for (Client client : clients) {
            responseModels.add(new ClientResponseModel(client));
        }
        
        return responseModels;
    }
}