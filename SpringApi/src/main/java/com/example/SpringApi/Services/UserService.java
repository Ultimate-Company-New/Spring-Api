package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping;
import com.example.SpringApi.Models.DatabaseModels.UserGroupUserMap;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.DatabaseModels.GoogleCred;
import com.example.SpringApi.Models.RequestModels.UserRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.GoogleCredRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserGroupUserMapRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.IUserSubTranslator;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.EmailTemplates;
import com.example.SpringApi.Helpers.FirebaseHelper;
import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.io.File;

/**
 * Service class for User-related business operations.
 * 
 * This service implements the IUserSubTranslator interface and provides
 * comprehensive user management functionality including CRUD operations,
 * user retrieval, and user status management. It follows the established
 * pattern from AddressService and ClientService with proper error handling,
 * validation, and audit logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional
public class UserService extends BaseService implements IUserSubTranslator {
    
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserGroupUserMapRepository userGroupUserMapRepository;
    private final UserClientMappingRepository userClientMappingRepository;
    private final UserClientPermissionMappingRepository userClientPermissionMappingRepository;
    private final GoogleCredRepository googleCredRepository;
    private final ClientRepository clientRepository;
    private final Environment environment;
    private final UserLogService userLogService;
    private final ClientService clientService;
    
    @Autowired
    public UserService(UserRepository userRepository, 
                      AddressRepository addressRepository,
                      UserGroupUserMapRepository userGroupUserMapRepository,
                      UserClientMappingRepository userClientMappingRepository,
                      UserClientPermissionMappingRepository userClientPermissionMappingRepository,
                      GoogleCredRepository googleCredRepository,
                      ClientRepository clientRepository,
                      Environment environment,
                      UserLogService userLogService,
                      ClientService clientService,
                      HttpServletRequest request) {
        super();
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.userGroupUserMapRepository = userGroupUserMapRepository;
        this.userClientMappingRepository = userClientMappingRepository;
        this.userClientPermissionMappingRepository = userClientPermissionMappingRepository;
        this.googleCredRepository = googleCredRepository;
        this.clientRepository = clientRepository;
        this.environment = environment;
        this.userLogService = userLogService;
        this.clientService = clientService;
    }

    /**
     * Toggles the deletion status of a user by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the user is currently active (isDeleted = false), it will be marked as deleted.
     * If the user is currently deleted (isDeleted = true), it will be restored.
     * 
     * @param id The unique identifier of the user to toggle
     * @return true if the operation was successful, false if the user was not found
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    @Override
    public void toggleUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId));
        
        user.setIsDeleted(!user.getIsDeleted());
        user.setModifiedUser(getUser());
        userRepository.save(user);
        
        // Log user toggle operation
        userLogService.logData(getUser(), SuccessMessages.UserSuccessMessages.ToggleUser + " " + user.getUserId() + " deletion status to " + user.getIsDeleted(),
                ApiRoutes.UserSubRoute.TOGGLE_USER);
    }

    /**
     * Retrieves a single user by its unique identifier.
     * 
     * This method fetches a user from the database using the provided ID.
     * The returned UserResponseModel contains all user details including
     * login name, personal information, role, metadata, and permissions.
     * 
     * @param id The unique identifier of the user to retrieve
     * @return UserResponseModel containing the user information
     * @throws NotFoundException if no user exists with the given ID
     * @throws IllegalArgumentException if the provided ID is null or invalid
     */
    @Override
    public UserResponseModel getUserById(long id) {
        // Fetch user with ALL relations in a SINGLE database call (filtered by clientId)
        // This includes: user data, addresses, permissions (for this client), and user groups (for this client)
        User user = userRepository.findByIdWithAllRelations(id, getClientId());
        if (user == null) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
        }
        
        return new UserResponseModel(user);
    }

    /**
     * Retrieves a single user by email address.
     * 
     * This method fetches a user from the database using the provided email.
     * The returned UserResponseModel contains all user details and permissions.
     * 
     * @param email The email address of the user to retrieve
     * @return UserResponseModel containing the user information
     * @throws NotFoundException if no user exists with the given email
     * @throws IllegalArgumentException if the provided email is null or invalid
     */
    @Override
    public UserResponseModel getUserByEmail(String email) {
        // Fetch user with ALL relations in ONE query (filtered by clientId)
        // Permissions and usergroups are filtered by current client session
        User user = userRepository.findByEmailWithAllRelations(email, getClientId());
        if (user == null) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidEmail);
        }
        
        return new UserResponseModel(user);
    }

    /**
     * Creates a new user in the system.
     * 
     * This method validates the provided user data, creates a new User entity,
     * and persists it to the database along with associated address, permissions,
     * and user group mappings. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps.
     * 
     * @param userRequestModel The UserRequestModel containing the user data to create
     * @return true if the user was successfully created, false otherwise
     * @throws BadRequestException if the user data is invalid or incomplete
     * @throws IllegalArgumentException if the user parameter is null
     */
    @Override
    @Transactional
    public void createUser(UserRequestModel userRequestModel) {
        // 1. Check if user email already exists
        if (userRepository.findByLoginName(userRequestModel.getEmail()) != null) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidEmail + " - Email already exists");
        }
        
        // 2. Generate password and set security fields
        String password = PasswordHelper.getRandomPassword();
        String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(password);
        userRequestModel.setSalt(saltAndHash[0]);
        userRequestModel.setPassword(saltAndHash[1]);
        userRequestModel.setApiKey(PasswordHelper.getToken(userRequestModel.getLoginName()));
        userRequestModel.setToken(PasswordHelper.getToken(userRequestModel.getLoginName()));
        
        // 3. Create and save the user
        User newUser = new User(userRequestModel, getUser());
        User savedUser = userRepository.save(newUser);
        
        // 4. Create address if provided
        if (userRequestModel.getAddress() != null) {
            Address address = new Address(userRequestModel.getAddress(), getUser());
            Address savedAddress = addressRepository.save(address);
            savedUser.setAddressId(savedAddress.getAddressId());
            userRepository.save(savedUser);
        }
        
        // 5. Create permission mappings if provided
        if (userRequestModel.getPermissionIds() == null || userRequestModel.getPermissionIds().isEmpty()) {
            throw new BadRequestException("At least one permission mapping is required for the user.");
        } else {
            List<UserClientPermissionMapping> permissionMappings = new ArrayList<>();
            for (Long permissionId : userRequestModel.getPermissionIds()) {
                permissionMappings.add(new UserClientPermissionMapping(
                    savedUser.getUserId(),
                    getClientId(),
                    permissionId,
                    getUser(),
                    getUser()
                ));
            }
            userClientPermissionMappingRepository.saveAll(permissionMappings);
        }
        
        // 6. Create user group mappings if provided
        if (userRequestModel.getSelectedGroupIds() != null && !userRequestModel.getSelectedGroupIds().isEmpty()) {
            List<UserGroupUserMap> groupMappings = new ArrayList<>();
            for (Long groupId : userRequestModel.getSelectedGroupIds()) {
                groupMappings.add(new UserGroupUserMap(
                    savedUser.getUserId(),
                    groupId,
                    getUser()
                ));
            }
            userGroupUserMapRepository.saveAll(groupMappings);
        }
        
        // 7. Create user-client mapping
        UserClientMapping userClientMapping = new UserClientMapping(
            savedUser.getUserId(),
            getClientId(),
            getUser(),
            getUser()
        );
        userClientMappingRepository.save(userClientMapping);
        
        // 8. Upload profile picture if present
        if (userRequestModel.getProfilePictureBase64() != null &&
                !userRequestModel.getProfilePictureBase64().isEmpty() &&
                !userRequestModel.getProfilePictureBase64().isBlank()) {
            
            // Get client details to access GoogleCred
            Long clientId = getClientId();
            Optional<GoogleCred> googleCred = googleCredRepository.findById(clientId);
            ClientResponseModel clientDetails = clientService.getClientById(clientId);

            if (googleCred.isPresent()) {
                String filePath = clientDetails.getName() + " - " + clientId
                        + File.separator
                        + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default")
                        + File.separator
                        + "UserProfiles"
                        + File.separator
                        + savedUser.getUserId() + "-" + savedUser.getLastName() + ".png";

                // Create FirebaseHelper with GoogleCred object
                GoogleCred googleCredData = googleCred.get();
                FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);
                boolean isSuccess = firebaseHelper.uploadFileToFirebase(userRequestModel.getProfilePictureBase64(), filePath);
                if (!isSuccess) {
                    throw new BadRequestException(ErrorMessages.UserErrorMessages.ER010);
                }
            } else {
                throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
            }
        }
        
        // 9. Send account confirmation email
        Long clientId = getClientId();
        ClientResponseModel clientDetails = clientService.getClientById(clientId);
        Optional<GoogleCred> googleCred = googleCredRepository.findById(clientId);
        Client client = clientRepository.findById(clientId).get();

        EmailTemplates emailTemplates = new EmailTemplates(
                clientDetails.getSendgridSenderName(),
                clientDetails.getSendGridEmailAddress(),
                clientDetails.getSendGridApiKey(),
                environment,
                client,
                googleCred.get());
        
        try {
            boolean sendAccountConfirmationEmailResponse = emailTemplates.sendNewUserAccountConfirmation(
                    savedUser.getUserId(),
                    savedUser.getToken(),
                    savedUser.getLoginName(),
                    password);
            
            if (!sendAccountConfirmationEmailResponse) {
                throw new BadRequestException("Failed to send confirmation email");
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to send confirmation email: " + e.getMessage());
        }
        
        // 10. Log user creation
        userLogService.logData(getUser(), SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
                ApiRoutes.UserSubRoute.CREATE_USER);
    }

    /**
     * Updates an existing user with new information.
     * 
     * This method retrieves the existing user by ID, validates the new data,
     * and updates the user while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * 
     * @param user The UserRequestModel containing the updated user data
     * @return The unique identifier of the updated user
     * @throws NotFoundException if no user exists with the given ID
     * @throws BadRequestException if the user data is invalid or incomplete
     * @throws IllegalArgumentException if the user parameter is null
     */
    @Override
    @Transactional
    public void updateUser(UserRequestModel user) {
        User existingUser = userRepository.findByIdWithAllRelations(user.getUserId(), getClientId());
        if (existingUser == null) {
            throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
        }

        // 1. Email cannot be changed
        if (!existingUser.getEmail().equals(user.getEmail())) {
            throw new BadRequestException("User email cannot be changed.");
        }

        // 2. Update address
       if (user.getAddress() != null) {
            if (existingUser.getAddressId() != null) {
                Address existingAddress = addressRepository.findById(existingUser.getAddressId())
                    .orElse(null);
                if (existingAddress != null) {
                    // Use the update constructor
                    Address updatedAddress = new Address(user.getAddress(), getUser(), existingAddress);
                    addressRepository.save(updatedAddress);
                } else {
                    Address newAddress = new Address(user.getAddress(), getUser());
                    Address savedAddress = addressRepository.save(newAddress);
                    existingUser.setAddressId(savedAddress.getAddressId());
                }
            } else {
                Address newAddress = new Address(user.getAddress(), getUser());
                Address savedAddress = addressRepository.save(newAddress);
                existingUser.setAddressId(savedAddress.getAddressId());
            }
        }

        // 3. Update permissions (must have at least one)
        if (user.getPermissionIds() == null || user.getPermissionIds().isEmpty()) {
            throw new BadRequestException("At least one permission mapping is required for the user.");
        }
        // Remove all existing permission mappings for this user and client
        List<UserClientPermissionMapping> existingPerms = userClientPermissionMappingRepository.findByUserIdAndClientId(existingUser.getUserId(), getClientId());
        if (existingPerms != null && !existingPerms.isEmpty()) {
            userClientPermissionMappingRepository.deleteAll(existingPerms);
        }
        // Add new permission mappings
        List<UserClientPermissionMapping> newPerms = new ArrayList<>();
        for (Long permissionId : user.getPermissionIds()) {
            newPerms.add(new UserClientPermissionMapping(
                existingUser.getUserId(),
                getClientId(),
                permissionId,
                getUser()
            ));
        }
        userClientPermissionMappingRepository.saveAll(newPerms);

        // 4. Update group mappings (remove all, add new if any)
        List<UserGroupUserMap> existingGroups = userGroupUserMapRepository.findByUserId(existingUser.getUserId());
        if (existingGroups != null && !existingGroups.isEmpty()) {
            userGroupUserMapRepository.deleteAll(existingGroups);
        }
        if (user.getSelectedGroupIds() != null && !user.getSelectedGroupIds().isEmpty()) {
            List<UserGroupUserMap> newGroups = new ArrayList<>();
            for (Long groupId : user.getSelectedGroupIds()) {
                newGroups.add(new UserGroupUserMap(
                    existingUser.getUserId(),
                    groupId,
                    getUser()
                ));
            }
            userGroupUserMapRepository.saveAll(newGroups);
        }

        // 5. Profile picture handling
        // If profile picture is coming in, delete old and upload new
        boolean hasNewProfilePic = user.getProfilePictureBase64() != null && !user.getProfilePictureBase64().isEmpty() && !user.getProfilePictureBase64().isBlank();
        Long clientId = getClientId();
        Optional<GoogleCred> googleCred = googleCredRepository.findById(clientId);
        ClientResponseModel clientDetails = clientService.getClientById(clientId);
        String filePath = null;
        if (googleCred.isPresent()) {
            filePath = clientDetails.getName() + " - " + clientId
                    + File.separator
                    + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default")
                    + File.separator
                    + "UserProfiles"
                    + File.separator
                    + existingUser.getUserId() + "-" + existingUser.getLastName() + ".png";
            GoogleCred googleCredData = googleCred.get();
            FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);
            if (hasNewProfilePic) {
                // Delete old profile pic if exists
                firebaseHelper.deleteFile(filePath);
                boolean isSuccess = firebaseHelper.uploadFileToFirebase(user.getProfilePictureBase64(), filePath);
                if (!isSuccess) {
                    throw new BadRequestException(ErrorMessages.UserErrorMessages.ER010);
                }
            } else {
                // No new profile pic, delete existing if exists
                firebaseHelper.deleteFile(filePath);
            }
        }

        // 6. Update user fields (except email)
        User updatedUser = new User(user, getUser(), existingUser);
        User savedUser = userRepository.save(updatedUser);

        // 7. User log
        userLogService.logData(getUser(), SuccessMessages.UserSuccessMessages.UpdateUser + " " + savedUser.getUserId(), ApiRoutes.UserSubRoute.UPDATE_USER);
    }

    /**
     * Retrieves users in the carrier in paginated batches with advanced filtering and sorting.
     * Fetches users associated with the current carrier in a paginated, filterable, and sortable manner.
     * Accepts a {@link UserRequestModel} containing pagination parameters (start, end), filter expressions,
     * column names for sorting, and other options. Returns a {@link PaginationBaseResponseModel} with user data
     * and total count, enabling efficient client-side pagination and search. Ideal for large user lists in admin panels.
     *
     * @param userRequestModel The request model containing pagination, filter, and sort options.
     * @return {@link PaginationBaseResponseModel} of {@link UserResponseModel} for the requested batch.
     */
    @Override
    public PaginationBaseResponseModel<UserResponseModel> fetchUsersInCarrierInBatches(UserRequestModel userRequestModel) {
        // validate the column names
        if(StringUtils.hasText(userRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList(
                "userId",
                "firstName", "lastName", "loginName",
                "role", "dob", "phone", "address",
                "datePasswordChanges", "loginAttempts", "isDeleted",
                "locked", "emailConfirmed", "token", "isGuest",
                "apiKey", "email", "addressId", "profilePicture",
                "lastLoginAt", "createdAt", "createdUser",
                "updatedAt", "modifiedUser", "notes"));

            if(!validColumns.contains(userRequestModel.getColumnName())){
                throw new BadRequestException(
                    ErrorMessages.InvalidColumn + String.join(",", validColumns)
                );
            }
        }

        int start = userRequestModel.getStart();
        int end = userRequestModel.getEnd();
        int limit = end - start;
        
        // Create custom Pageable with exact OFFSET and LIMIT for database-level pagination
        // Spring's PageRequest.of(page, size) uses: OFFSET = page * size, LIMIT = size
        // For arbitrary offsets (e.g., start=5, end=15), we need OFFSET=5, LIMIT=10
        // Solution: Override getOffset() to return the exact start position
        org.springframework.data.domain.Pageable pageable = new org.springframework.data.domain.PageRequest(0, limit, Sort.by("userId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        Page<User> page = userRepository.findPaginatedUsers(
            getClientId(),
            userRequestModel.getSelectedUserIds(),
            userRequestModel.getColumnName(),
            userRequestModel.getCondition(),
            userRequestModel.getFilterExpr(),
            userRequestModel.isIncludeDeleted(),
            pageable
        );

        PaginationBaseResponseModel<UserResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        List<UserResponseModel> userResponseModels = new ArrayList<>();
        for (User user : page.getContent()) {
            UserResponseModel userResponseModel = new UserResponseModel(user);
            userResponseModels.add(userResponseModel);
        }
        
        // Return the actual count of users in this page, not the total count
        paginationBaseResponseModel.setData(userResponseModels);
        paginationBaseResponseModel.setTotalDataCount(userResponseModels.size());
        return paginationBaseResponseModel;
    }
}
