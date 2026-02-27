package com.example.springapi.services;

import com.example.springapi.ErrorMessages;
import com.example.springapi.SuccessMessages;
import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.databasemodels.Client;
import com.example.springapi.models.databasemodels.User;
import com.example.springapi.models.requestmodels.AddressRequestModel;
import com.example.springapi.models.responsemodels.AddressResponseModel;
import com.example.springapi.repositories.AddressRepository;
import com.example.springapi.repositories.ClientRepository;
import com.example.springapi.repositories.UserRepository;
import com.example.springapi.services.interfaces.AddressSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Address-related business operations.
 *
 * <p>This service implements the AddressSubTranslator interface and provides comprehensive address
 * management functionality including CRUD operations, user/client address retrieval, and address
 * status management. The service handles validation, error handling, audit logging, and database
 * persistence.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class AddressService extends BaseService implements AddressSubTranslator {
  private final AddressRepository addressRepository;
  private final ClientRepository clientRepository;
  private final UserRepository userRepository;
  private final UserLogService userLogService;

  /**
   * Initializes AddressService.
   */
  @Autowired
  public AddressService(
      AddressRepository addressRepository,
      ClientRepository clientRepository,
      UserRepository userRepository,
      UserLogService userLogService,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.addressRepository = addressRepository;
    this.clientRepository = clientRepository;
    this.userRepository = userRepository;
    this.userLogService = userLogService;
  }

  /**
   * Toggles the deletion status of an address by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the address
   * is currently active (isDeleted = false), it will be marked as deleted. If the address is
   * currently deleted (isDeleted = true), it will be restored. The operation is logged for audit
   * purposes.
   *
   * @param addressId The unique identifier of the address to toggle
   * @throws NotFoundException if the address was not found
   */
  @Override
  @Transactional
  public void toggleAddress(long addressId) {
    Optional<Address> address = addressRepository.findById(addressId);
    if (address.isPresent()) {
      address.get().setIsDeleted(!address.get().getIsDeleted());
      addressRepository.save(address.get());
      userLogService.logData(
          getUserId(),
          SuccessMessages.AddressSuccessMessages.TOGGLE_ADDRESS
              + " "
              + address.get().getAddressId(),
          ApiRoutes.AddressSubRoute.TOGGLE_ADDRESS);
    } else {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
  }

  /**
   * Retrieves a single address by its unique identifier.
   *
   * <p>This method fetches an address from the database using the provided ID. The returned
   * AddressResponseModel contains all address details including street address, city, state, postal
   * code, country, and metadata.
   *
   * @param addressId The unique identifier of the address to retrieve
   * @return AddressResponseModel containing the address information
   * @throws NotFoundException if no address exists with the given ID
   */
  @Override
  @Transactional(readOnly = true)
  public AddressResponseModel getAddressById(long addressId) {
    Optional<Address> address = addressRepository.findById(addressId);
    if (address.isEmpty()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
    if (address.get().getIsDeleted()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
    return new AddressResponseModel(address.get());
  }

  /**
   * Creates a new address in the system.
   *
   * <p>This method validates the provided address data, creates a new Address entity, and persists
   * it to the database. The method automatically sets audit fields such as createdUser,
   * modifiedUser, and timestamps. The operation is logged for audit purposes.
   *
   * @param addressRequest The AddressRequestModel containing the address data to insert
   */
  @Override
  @Transactional
  public void insertAddress(AddressRequestModel addressRequest) {
    if (addressRequest == null) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }

    Address address = new Address(addressRequest, getUser());
    Address savedAddress = addressRepository.save(address);
    userLogService.logData(
        getUserId(),
        SuccessMessages.AddressSuccessMessages.INSERT_ADDRESS + " " + savedAddress.getAddressId(),
        ApiRoutes.AddressSubRoute.INSERT_ADDRESS);
  }

  /**
   * Updates an existing address with new information.
   *
   * <p>This method retrieves the existing address by ID, validates the new data, and updates the
   * address while preserving audit information like createdUser and createdAt. Only the
   * modifiedUser and updatedAt fields are updated. The operation is logged for audit purposes.
   *
   * @param addressRequest The AddressRequestModel containing the updated address data
   * @throws NotFoundException if no address exists with the given ID
   */
  @Override
  @Transactional
  public void updateAddress(AddressRequestModel addressRequest) {
    if (addressRequest == null) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }
    Optional<Address> existingAddress = addressRepository.findById(addressRequest.getId());
    if (existingAddress.isPresent()) {
      Address address = new Address(addressRequest, getUser(), existingAddress.get());
      Address updatedAddress = addressRepository.save(address);
      userLogService.logData(
          getUserId(),
          SuccessMessages.AddressSuccessMessages.UPDATE_ADDRESS
              + " "
              + updatedAddress.getAddressId(),
          ApiRoutes.AddressSubRoute.UPDATE_ADDRESS);
    } else {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
  }

  /**
   * Retrieves all addresses associated with a specific user.
   *
   * <p>This method first validates that the user exists and is not deleted. Then fetches all
   * addresses where the userId matches the provided parameter and isDeleted is false (only active
   * addresses). The results are ordered by addressId in descending order. The method returns a list
   * of AddressResponseModel objects, each containing complete address information. Returns an empty
   * list if no addresses are found for a valid user.
   *
   * @param userId The unique identifier of the user
   * @return List of AddressResponseModel objects for the user (non-deleted only, sorted by
   *     addressId desc)
   * @throws NotFoundException if the user does not exist or is deleted
   */
  @Override
  @Transactional(readOnly = true)
  public List<AddressResponseModel> getAddressByUserId(long userId) {
    // Validate that user exists and is not deleted
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
    if (user.get().getIsDeleted()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }

    List<Address> addresses =
        addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(userId, false);
    List<AddressResponseModel> responseModels = new ArrayList<>();

    for (Address address : addresses) {
      responseModels.add(new AddressResponseModel(address));
    }

    return responseModels;
  }

  /**
   * Retrieves all addresses associated with a specific client.
   *
   * <p>This method first validates that the client exists and is not deleted. Then fetches all
   * addresses where the clientId matches the provided parameter and isDeleted is false (only active
   * addresses). The results are ordered by addressId in descending order. The method returns a list
   * of AddressResponseModel objects, each containing complete address information. Returns an empty
   * list if no addresses are found for a valid client.
   *
   * @param clientId The unique identifier of the client
   * @return List of AddressResponseModel objects for the client (non-deleted only, sorted by
   *     addressId desc)
   * @throws NotFoundException if the client does not exist or is deleted
   */
  @Override
  @Transactional(readOnly = true)
  public List<AddressResponseModel> getAddressByClientId(long clientId) {
    // Validate that client exists and is not deleted
    Optional<Client> client = clientRepository.findById(clientId);
    if (client.isEmpty()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }
    if (client.get().getIsDeleted()) {
      throw new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND);
    }

    List<Address> addresses =
        addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(clientId, false);
    List<AddressResponseModel> responseModels = new ArrayList<>();

    for (Address address : addresses) {
      responseModels.add(new AddressResponseModel(address));
    }

    return responseModels;
  }
}
