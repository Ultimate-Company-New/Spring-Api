package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Constants.ImageLocationConstants;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import org.springframework.core.env.Environment;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Services.Interface.IClientSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.ImgbbHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

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
    
    @Value("${imageLocation:firebase}")
    private String imageLocation;

    @Autowired
    public ClientService(UserLogService userLogService,
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
            userLogService.logData(
                getUserId(),
                SuccessMessages.ClientSuccessMessages.ToggleClient + " " + client.get().getClientId(),
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
     * @throws BadRequestException if a client with the same name already exists
     */
    @Override
    public void createClient(ClientRequestModel clientRequest) {
        // Check for duplicate name
        if (clientRepository.existsByName(clientRequest.getName())) {
            throw new BadRequestException("A client with the name '" + clientRequest.getName() + "' already exists.");
        }
        
        Client client = new Client(clientRequest, getUser());
        Client savedClient = clientRepository.save(client);
        
        // Upload logo if present
        if (clientRequest.getLogoBase64() != null &&
                !clientRequest.getLogoBase64().isEmpty() &&
                !clientRequest.getLogoBase64().isBlank()) {
            
            String environmentName = environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0]
                : "default";
            
            boolean isSuccess = false;
            
            // Use ImgBB or Firebase based on configuration
            if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
                // Validate ImgBB API key is configured
                if (savedClient.getImgbbApiKey() == null || savedClient.getImgbbApiKey().trim().isEmpty()) {
                    throw new BadRequestException("ImgBB API key is not configured for this client");
                }
                
                // Generate custom filename for ImgBB
                String customFileName = ImgbbHelper.generateCustomFileNameForClientLogo(environmentName, savedClient.getName());
                
                ImgbbHelper imgbbHelper = new ImgbbHelper(savedClient.getImgbbApiKey());
                ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                    clientRequest.getLogoBase64(),
                    customFileName
                );
                
                if (uploadResponse != null && uploadResponse.getUrl() != null) {
                    // Save both the logo URL and delete hash to the database
                    savedClient.setLogoUrl(uploadResponse.getUrl());
                    savedClient.setLogoDeleteHash(uploadResponse.getDeleteHash());
                    clientRepository.save(savedClient);
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            } else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
                String filePath = FirebaseHelper.getClientLogoPath(
                    environmentName,
                    savedClient.getName(),
                    savedClient.getClientId()
                );
                // Get GoogleCred for Firebase
                Optional<GoogleCred> googleCred = googleCredRepository.findById(savedClient.getGoogleCredId());
                
                if (googleCred.isPresent()) {
                    GoogleCred googleCredData = googleCred.get();
                    FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);
                    isSuccess = firebaseHelper.uploadFileToFirebase(
                        clientRequest.getLogoBase64(),
                        filePath
                    );
                } else {
                    throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
                }
            } else {
                throw new BadRequestException("Invalid imageLocation configuration: " + imageLocation);
            }

            if (!isSuccess) {
                throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidLogoUpload);
            }
        }
        
        userLogService.logData(
            getUserId(),
            SuccessMessages.ClientSuccessMessages.CreateClient + " " + savedClient.getClientId(),
            ApiRoutes.ClientSubRoute.CREATE_CLIENT);
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
     * @throws BadRequestException if the new name conflicts with another existing client
     */
    @Override
    public void updateClient(ClientRequestModel clientRequest) {
        Optional<Client> existingClient = clientRepository.findById(clientRequest.getClientId());
        if (existingClient.isPresent()) {
            // Check for duplicate name (excluding the current client)
            Optional<Client> duplicateClient = clientRepository.findByName(clientRequest.getName());
            if (duplicateClient.isPresent() && !duplicateClient.get().getClientId().equals(clientRequest.getClientId())) {
                throw new BadRequestException("A client with the name '" + clientRequest.getName() + "' already exists.");
            }
            
            Client client = new Client(clientRequest, getUser(), existingClient.get());
            Client updatedClient = clientRepository.save(client);

            String environmentName = environment.getActiveProfiles().length > 0
                ? environment.getActiveProfiles()[0]
                : "default";

            // Use ImgBB or Firebase based on configuration
            if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
                // ImgBB-based logo management
                
                // Handle logo update based on request
                if (clientRequest.getLogoBase64() == null ||
                    clientRequest.getLogoBase64().isEmpty() ||
                    clientRequest.getLogoBase64().isBlank()) {
                    // If no logo in request, delete the old logo from ImgBB and clear from database
                    if (updatedClient.getLogoDeleteHash() != null && !updatedClient.getLogoDeleteHash().isEmpty()) {
                        // Validate ImgBB API key only when we need to delete
                        if (updatedClient.getImgbbApiKey() == null || updatedClient.getImgbbApiKey().trim().isEmpty()) {
                            throw new BadRequestException("ImgBB API key is not configured for this client");
                        }
                        ImgbbHelper imgbbHelper = new ImgbbHelper(updatedClient.getImgbbApiKey());
                        imgbbHelper.deleteImage(updatedClient.getLogoDeleteHash());
                    }
                    updatedClient.setLogoUrl(null);
                    updatedClient.setLogoDeleteHash(null);
                    clientRepository.save(updatedClient);
                } else {
                    // Validate ImgBB API key before uploading
                    if (updatedClient.getImgbbApiKey() == null || updatedClient.getImgbbApiKey().trim().isEmpty()) {
                        throw new BadRequestException("ImgBB API key is not configured for this client");
                    }
                    
                    ImgbbHelper imgbbHelper = new ImgbbHelper(updatedClient.getImgbbApiKey());
                    
                    // Delete old logo from ImgBB before uploading new one
                    if (updatedClient.getLogoDeleteHash() != null && !updatedClient.getLogoDeleteHash().isEmpty()) {
                        imgbbHelper.deleteImage(updatedClient.getLogoDeleteHash());
                    }
                    
                    // Generate custom filename for ImgBB
                    String customFileName = ImgbbHelper.generateCustomFileNameForClientLogo(environmentName, updatedClient.getName());
                    
                    // Upload new logo to ImgBB
                    ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(
                        clientRequest.getLogoBase64(),
                        customFileName
                    );

                    if (uploadResponse != null && uploadResponse.getUrl() != null) {
                        // Save both the new logo URL and delete hash to the database
                        updatedClient.setLogoUrl(uploadResponse.getUrl());
                        updatedClient.setLogoDeleteHash(uploadResponse.getDeleteHash());
                        clientRepository.save(updatedClient);
                    } else {
                        throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidLogoUpload);
                    }
                }
            } else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
                String filePath = FirebaseHelper.getClientLogoPath(
                    environmentName,
                    updatedClient.getName(),
                    updatedClient.getClientId()
                );
                // Firebase-based logo management
                Optional<GoogleCred> googleCred = googleCredRepository.findById(updatedClient.getGoogleCredId());

                if (googleCred.isPresent()) {
                    GoogleCred googleCredData = googleCred.get();
                    FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);

                    // Check if logo exists on Firebase
                    byte[] existingLogo = firebaseHelper.downloadFileAsBytesFromFirebase(filePath);
                    boolean logoExists = existingLogo != null;

                    // Handle logo update based on request
                    if (clientRequest.getLogoBase64() == null ||
                        clientRequest.getLogoBase64().isEmpty() ||
                        clientRequest.getLogoBase64().isBlank()) {
                        // If no logo in request and logo exists, delete it
                        if (logoExists) {
                            firebaseHelper.deleteFile(filePath);
                        }
                    } else {
                        // If logo exists, delete it first before uploading new one
                        if (logoExists) {
                            firebaseHelper.deleteFile(filePath);
                        }

                        // Upload new logo
                        boolean isSuccess = firebaseHelper.uploadFileToFirebase(
                            clientRequest.getLogoBase64(),
                            filePath
                        );

                        if (!isSuccess) {
                            throw new BadRequestException(ErrorMessages.ClientErrorMessages.InvalidLogoUpload);
                        }
                    }
                } else {
                    throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
                }
            } else {
                throw new BadRequestException("Invalid imageLocation configuration: " + imageLocation);
            }

            userLogService.logData(
                getUserId(),
                SuccessMessages.ClientSuccessMessages.UpdateClient + " " + updatedClient.getClientId(),
                ApiRoutes.ClientSubRoute.UPDATE_CLIENT);
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