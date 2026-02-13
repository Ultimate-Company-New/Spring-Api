package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Authentication.JwtTokenProvider;
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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final GoogleCredRepository googleCredRepository;
    private final UserLogService userLogService;
    private final Environment environment;

    @Value("${imageLocation:firebase}")
    private String imageLocation;

    @Autowired
    public ClientService(
            ClientRepository clientRepository,
            GoogleCredRepository googleCredRepository,
            UserLogService userLogService,
            Environment environment,
            JwtTokenProvider jwtTokenProvider,
            HttpServletRequest request) {
        super(jwtTokenProvider, request);
        this.clientRepository = clientRepository;
        this.googleCredRepository = googleCredRepository;
        this.userLogService = userLogService;
        this.environment = environment;
    }

    /**
     * Toggles the deletion status of a client by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the client is currently active (isDeleted = false), it will be marked as
     * deleted.
     * If the client is currently deleted (isDeleted = true), it will be restored.
     * The operation is logged for audit purposes.
     * 
     * @param clientId The unique identifier of the client to toggle
     * @throws NotFoundException if the client was not found
     */
    @Override
    @Transactional
    public void toggleClient(long clientId) {
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            client.get().setIsDeleted(!client.get().getIsDeleted());
            clientRepository.save(client.get());
            userLogService.logData(
                    getUserId(),
                    SuccessMessages.ClientSuccessMessages.TOGGLE_CLIENT + " " + client.get().getClientId(),
                    ApiRoutes.ClientSubRoute.TOGGLE_CLIENT);
        } else {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID);
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
    @Transactional(readOnly = true)
    public ClientResponseModel getClientById(long clientId) {
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isPresent()) {
            return new ClientResponseModel(client.get());
        } else {
            throw new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID);
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
     * @param clientRequest The ClientRequestModel containing the client data to
     *                      insert
     * @throws BadRequestException if a client with the same name already exists
     */
    @Override
    @Transactional
    public void createClient(ClientRequestModel clientRequest) {
        if (clientRequest == null) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_REQUEST);
        }

        validateUniqueClientName(clientRequest.getName(), null);

        Client client = new Client(clientRequest, getUser());
        Client savedClient = clientRepository.save(client);

        if (hasLogo(clientRequest)) {
            processLogoManagement(savedClient, clientRequest.getLogoBase64());
        }

        userLogService.logData(
                getUserId(),
                SuccessMessages.ClientSuccessMessages.CREATE_CLIENT + " " + savedClient.getClientId(),
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
     * @param clientRequest The ClientRequestModel containing the updated client
     *                      data
     * @throws NotFoundException   if no client exists with the given ID
     * @throws BadRequestException if the new name conflicts with another existing
     *                             client
     */
    @Override
    @Transactional
    public void updateClient(ClientRequestModel clientRequest) {
        if (clientRequest == null) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_REQUEST);
        }

        Client existingClient = clientRepository.findById(clientRequest.getClientId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));

        validateUniqueClientName(clientRequest.getName(), clientRequest.getClientId());

        Client client = new Client(clientRequest, getUser(), existingClient);
        Client updatedClient = clientRepository.save(client);

        processLogoManagement(updatedClient, clientRequest.getLogoBase64());

        userLogService.logData(
                getUserId(),
                SuccessMessages.ClientSuccessMessages.UPDATE_CLIENT + " " + updatedClient.getClientId(),
                ApiRoutes.ClientSubRoute.UPDATE_CLIENT);
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
    @Transactional(readOnly = true)
    public List<ClientResponseModel> getClientsByUser() {
        List<Client> clients = clientRepository.findByUserId(getUserId());
        List<ClientResponseModel> responseModels = new ArrayList<>();

        for (Client client : clients) {
            responseModels.add(new ClientResponseModel(client));
        }

        return responseModels;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Validates that the client name is unique.
     * 
     * @param name     The name to check
     * @param clientId The current client's ID (null for new clients)
     */
    private void validateUniqueClientName(String name, Long clientId) {
        clientRepository.findByName(name).ifPresent(duplicateClient -> {
            if (clientId == null || !duplicateClient.getClientId().equals(clientId)) {
                throw new BadRequestException(
                        String.format(ErrorMessages.ClientErrorMessages.DUPLICATE_CLIENT_NAME_FORMAT, name));
            }
        });
    }

    /**
     * Checks if the request contains a logo to be processed.
     */
    private boolean hasLogo(ClientRequestModel request) {
        return request.getLogoBase64() != null &&
                !request.getLogoBase64().isEmpty() &&
                !request.getLogoBase64().isBlank();
    }

    /**
     * Orchestrates logo management based on configuration.
     */
    private void processLogoManagement(Client client, String logoBase64) {
        boolean hasNewLogo = logoBase64 != null && !logoBase64.isEmpty() && !logoBase64.isBlank();
        String environmentName = getEnvironmentName();

        if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
            handleImgbbLogo(client, logoBase64, environmentName, hasNewLogo);
        } else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
            handleFirebaseLogo(client, logoBase64, environmentName, hasNewLogo);
        } else {
            throw new BadRequestException(String.format(
                    ErrorMessages.ConfigurationErrorMessages.INVALID_IMAGE_LOCATION_CONFIG_FORMAT, imageLocation));
        }
    }

    /**
     * Retrieves the current active profile name.
     */
    private String getEnvironmentName() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length > 0 ? activeProfiles[0] : "default";
    }

    /**
     * Handles ImgBB logo upload and deletion.
     */
    private void handleImgbbLogo(Client client, String logoBase64, String env, boolean hasNewLogo) {
        if (client.getImgbbApiKey() == null || client.getImgbbApiKey().trim().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED);
        }

        ImgbbHelper imgbbHelper = new ImgbbHelper(client.getImgbbApiKey());

        // Always delete existing logo if we have a hash (for both update and removal)
        if (client.getLogoDeleteHash() != null && !client.getLogoDeleteHash().isEmpty()) {
            imgbbHelper.deleteImage(client.getLogoDeleteHash());
        }

        if (!hasNewLogo) {
            client.setLogoUrl(null);
            client.setLogoDeleteHash(null);
        } else {
            String customFileName = ImgbbHelper.generateCustomFileNameForClientLogo(env, client.getName());
            ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(logoBase64, customFileName);

            if (uploadResponse != null && uploadResponse.getUrl() != null) {
                client.setLogoUrl(uploadResponse.getUrl());
                client.setLogoDeleteHash(uploadResponse.getDeleteHash());
            } else {
                throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_LOGO_UPLOAD);
            }
        }
        clientRepository.save(client);
    }

    /**
     * Handles Firebase logo upload and deletion.
     */
    private void handleFirebaseLogo(Client client, String logoBase64, String env, boolean hasNewLogo) {
        String filePath = FirebaseHelper.getClientLogoPath(env, client.getName(), client.getClientId());
        Optional<GoogleCred> googleCred = googleCredRepository.findById(client.getGoogleCredId());

        if (googleCred.isEmpty()) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
        }

        FirebaseHelper firebaseHelper = new FirebaseHelper(googleCred.get());

        // For Firebase, we always delete at the current path.
        // If it's a new logo, it will be replaced. If it's removal, it will stay
        // deleted.
        firebaseHelper.deleteFile(filePath);

        if (hasNewLogo && !firebaseHelper.uploadFileToFirebase(logoBase64, filePath)) {
            throw new BadRequestException(ErrorMessages.ClientErrorMessages.INVALID_LOGO_UPLOAD);
        }
    }
}
