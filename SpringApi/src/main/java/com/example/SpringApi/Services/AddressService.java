package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;

import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.IAddressSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SpringApi.Exceptions.NotFoundException;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

/**
 * Service class for managing Address-related business operations.
 * 
 * This service implements the IAddressSubTranslator interface and provides
 * comprehensive address management functionality including CRUD operations,
 * user/client address retrieval, and address status management. The service
 * handles validation, error handling, audit logging, and database persistence.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class AddressService extends BaseService implements IAddressSubTranslator {
    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final UserLogService userLogService;

    @Autowired
    public AddressService(HttpServletRequest request,
                        UserLogService userLogService,
                        AddressRepository addressRepository,
                        ClientRepository clientRepository,
                        UserRepository userRepository) {
        super();
        this.userLogService = userLogService;
        this.addressRepository = addressRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    /**
     * Toggles the deletion status of an address by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the address is currently active (isDeleted = false), it will be marked as deleted.
     * If the address is currently deleted (isDeleted = true), it will be restored.
     * The operation is logged for audit purposes.
     * 
     * @param addressId The unique identifier of the address to toggle
     * @throws NotFoundException if the address was not found
     */
    @Override
    public void toggleAddress(long addressId) {
        Optional<Address> address = addressRepository.findById(addressId);
        if (address.isPresent()) {
            address.get().setIsDeleted(!address.get().getIsDeleted());
            addressRepository.save(address.get());
            userLogService.logData(getUserId(), SuccessMessages.AddressSuccessMessages.ToggleAddress + " " + address.get().getAddressId(),
                    ApiRoutes.AddressSubRoute.TOGGLE_ADDRESS);
        } else {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
    }

    /**
     * Retrieves a single address by its unique identifier.
     * 
     * This method fetches an address from the database using the provided ID.
     * The returned AddressResponseModel contains all address details including
     * street address, city, state, postal code, country, and metadata.
     * 
     * @param addressId The unique identifier of the address to retrieve
     * @return AddressResponseModel containing the address information
     * @throws NotFoundException if no address exists with the given ID
     */
    @Override
    public AddressResponseModel getAddressById(long addressId) {
        Optional<Address> address = addressRepository.findById(addressId);
        if (address.isPresent()) {
            return new AddressResponseModel(address.get());
        } else {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
    }

    /**
     * Creates a new address in the system.
     * 
     * This method validates the provided address data, creates a new Address entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. The operation is logged
     * for audit purposes.
     * 
     * @param addressRequest The AddressRequestModel containing the address data to insert
     */
    @Override
    public void insertAddress(AddressRequestModel addressRequest) {
        Address address = new Address(addressRequest, getUser());
        Address savedAddress = addressRepository.save(address);
        userLogService.logData(getUserId(), SuccessMessages.AddressSuccessMessages.InsertAddress + " " + savedAddress.getAddressId(),
            ApiRoutes.AddressSubRoute.INSERT_ADDRESS);
    }

    /**
     * Updates an existing address with new information.
     * 
     * This method retrieves the existing address by ID, validates the new data,
     * and updates the address while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * The operation is logged for audit purposes.
     * 
     * @param addressRequest The AddressRequestModel containing the updated address data
     * @throws NotFoundException if no address exists with the given ID
     */
    @Override
    public void updateAddress(AddressRequestModel addressRequest) {
        Optional<Address> existingAddress = addressRepository.findById(addressRequest.getId());
        if (existingAddress.isPresent()) {
            Address address = new Address(addressRequest, getUser(), existingAddress.get());
            Address updatedAddress = addressRepository.save(address);
            userLogService.logData(getUserId(), SuccessMessages.AddressSuccessMessages.UpdateAddress + " " + updatedAddress.getAddressId(),
                    ApiRoutes.AddressSubRoute.UPDATE_ADDRESS);
        } else {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
    }

    /**
     * Retrieves all addresses associated with a specific user.
     * 
     * This method first validates that the user exists and is not deleted.
     * Then fetches all addresses where the userId matches the provided parameter
     * and isDeleted is false (only active addresses). The results are ordered by addressId
     * in descending order. The method returns a list of AddressResponseModel objects, each containing
     * complete address information. Returns an empty list if no addresses are found for a valid user.
     * 
     * @param userId The unique identifier of the user
     * @return List of AddressResponseModel objects for the user (non-deleted only, sorted by addressId desc)
     * @throws NotFoundException if the user does not exist or is deleted
     */
    @Override
    public List<AddressResponseModel> getAddressByUserId(long userId) {
        // Validate that user exists and is not deleted
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
        if (user.get().getIsDeleted()) {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
        
        List<Address> addresses = addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(userId, false);
        List<AddressResponseModel> responseModels = new ArrayList<>();
        
        for (Address address : addresses) {
            responseModels.add(new AddressResponseModel(address));
        }
        
        return responseModels;
    }

    /**
     * Retrieves all addresses associated with a specific client.
     * 
     * This method first validates that the client exists and is not deleted.
     * Then fetches all addresses where the clientId matches the provided parameter
     * and isDeleted is false (only active addresses). The results are ordered by addressId
     * in descending order. The method returns a list of AddressResponseModel objects, each containing
     * complete address information. Returns an empty list if no addresses are found for a valid client.
     * 
     * @param clientId The unique identifier of the client
     * @return List of AddressResponseModel objects for the client (non-deleted only, sorted by addressId desc)
     * @throws NotFoundException if the client does not exist or is deleted
     */
    @Override
    public List<AddressResponseModel> getAddressByClientId(long clientId) {
        // Validate that client exists and is not deleted
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
        if (client.get().getIsDeleted()) {
            throw new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound);
        }
        
        List<Address> addresses = addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(clientId, false);
        List<AddressResponseModel> responseModels = new ArrayList<>();
        
        for (Address address : addresses) {
            responseModels.add(new AddressResponseModel(address));
        }
        
        return responseModels;
    }
}