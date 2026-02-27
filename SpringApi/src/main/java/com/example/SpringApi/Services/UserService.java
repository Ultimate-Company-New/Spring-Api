package com.example.springapi.services;

import com.example.springapi.ErrorMessages;
import com.example.springapi.SuccessMessages;
import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.constants.ImageLocationConstants;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.filterquerybuilder.UserFilterQueryBuilder;
import com.example.springapi.helpers.BulkInsertHelper;
import com.example.springapi.helpers.EmailTemplates;
import com.example.springapi.helpers.FirebaseHelper;
import com.example.springapi.helpers.ImgbbHelper;
import com.example.springapi.helpers.PasswordHelper;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.databasemodels.Client;
import com.example.springapi.models.databasemodels.GoogleCred;
import com.example.springapi.models.databasemodels.User;
import com.example.springapi.models.databasemodels.UserClientMapping;
import com.example.springapi.models.databasemodels.UserClientPermissionMapping;
import com.example.springapi.models.databasemodels.UserGroupUserMap;
import com.example.springapi.models.requestmodels.PaginationBaseRequestModel;
import com.example.springapi.models.requestmodels.UserRequestModel;
import com.example.springapi.models.responsemodels.BulkUserInsertResponseModel;
import com.example.springapi.models.responsemodels.ClientResponseModel;
import com.example.springapi.models.responsemodels.PaginationBaseResponseModel;
import com.example.springapi.models.responsemodels.PermissionResponseModel;
import com.example.springapi.models.responsemodels.UserResponseModel;
import com.example.springapi.repositories.AddressRepository;
import com.example.springapi.repositories.ClientRepository;
import com.example.springapi.repositories.GoogleCredRepository;
import com.example.springapi.repositories.PermissionRepository;
import com.example.springapi.repositories.UserClientMappingRepository;
import com.example.springapi.repositories.UserClientPermissionMappingRepository;
import com.example.springapi.repositories.UserGroupUserMapRepository;
import com.example.springapi.repositories.UserRepository;
import com.example.springapi.services.interfaces.UserSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for User-related business operations.
 *
 * <p>This service implements the UserSubTranslator interface and provides comprehensive user
 * management functionality including CRUD operations, user retrieval, and user status management.
 * It follows the established pattern from AddressService and ClientService with proper error
 * handling, validation, and audit logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
@Transactional
public class UserService extends BaseService implements UserSubTranslator {

  private final UserRepository userRepository;
  private final UserFilterQueryBuilder userFilterQueryBuilder;
  private final AddressRepository addressRepository;
  private final UserGroupUserMapRepository userGroupUserMapRepository;
  private final UserClientMappingRepository userClientMappingRepository;
  private final UserClientPermissionMappingRepository userClientPermissionMappingRepository;
  private final PermissionRepository permissionRepository;
  private final GoogleCredRepository googleCredRepository;
  private final ClientRepository clientRepository;
  private final Environment environment;
  private final UserLogService userLogService;
  private final ClientService clientService;
  private final MessageService messageService;
  private final ContextualLogger logger;

  @Value("${imageLocation:firebase}")
  private String imageLocation;

  /**
   * Executes user service.
   */
  @Autowired
  public UserService(
      UserRepository userRepository,
      UserFilterQueryBuilder userFilterQueryBuilder,
      AddressRepository addressRepository,
      UserGroupUserMapRepository userGroupUserMapRepository,
      UserClientMappingRepository userClientMappingRepository,
      UserClientPermissionMappingRepository userClientPermissionMappingRepository,
      PermissionRepository permissionRepository,
      GoogleCredRepository googleCredRepository,
      ClientRepository clientRepository,
      Environment environment,
      UserLogService userLogService,
      ClientService clientService,
      MessageService messageService,
      HttpServletRequest request,
      JwtTokenProvider jwtTokenProvider) {
    super(jwtTokenProvider, request);
    this.userRepository = userRepository;
    this.userFilterQueryBuilder = userFilterQueryBuilder;
    this.addressRepository = addressRepository;
    this.userGroupUserMapRepository = userGroupUserMapRepository;
    this.userClientMappingRepository = userClientMappingRepository;
    this.userClientPermissionMappingRepository = userClientPermissionMappingRepository;
    this.permissionRepository = permissionRepository;
    this.googleCredRepository = googleCredRepository;
    this.clientRepository = clientRepository;
    this.environment = environment;
    this.userLogService = userLogService;
    this.clientService = clientService;
    this.messageService = messageService;
    this.logger = ContextualLogger.getLogger(UserService.class);
  }

  /**
   * Toggles user.
   */
  @Override
  public void toggleUser(long id) {
    User user = userRepository.findByIdWithAllRelations(id, getClientId());
    if (user == null) {
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    user.setIsDeleted(!user.getIsDeleted());
    user.setModifiedUser(getUser());
    userRepository.save(user);

    // Log user toggle operation
    userLogService.logData(
        getUserId(),
        SuccessMessages.UserSuccessMessages.TOGGLE_USER
            + " "
            + user.getUserId()
            + " deletion status to "
            + user.getIsDeleted(),
        ApiRoutes.UserSubRoute.TOGGLE_USER);
  }

  /**
   * Retrieves a single user by its unique identifier.
   *
   * <p>This method fetches a user from the database using the provided ID. The returned
   * UserResponseModel contains all user details including login name, personal information, role,
   * metadata, and permissions.
   *
   * @param id The unique identifier of the user to retrieve
   * @return UserResponseModel containing the user information
   * @throws NotFoundException if no user exists with the given ID
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  @Override
  public UserResponseModel getUserById(long id) {
    // Fetch user with ALL relations in a SINGLE database call (filtered by
    // clientId)
    // This includes: user data, addresses, permissions (for this client), and user
    // groups (for this client)
    User user = userRepository.findByIdWithAllRelations(id, getClientId());
    if (user == null) {
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    return new UserResponseModel(user);
  }

  /**
   * Retrieves a single user by email address.
   *
   * <p>This method fetches a user from the database using the provided email. The returned
   * UserResponseModel contains all user details and permissions.
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
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_EMAIL);
    }

    return new UserResponseModel(user);
  }

  /**
   * Creates a new user in the system.
   *
   * <p>This method validates the provided user data, creates a new User entity, and persists it to
   * the database along with associated address, permissions, and user group mappings. The method
   * automatically sets audit fields such as createdUser, modifiedUser, and timestamps.
   *
   * @param userRequestModel The UserRequestModel containing the user data to create
   * @throws BadRequestException if the user data is invalid or incomplete
   * @throws IllegalArgumentException if the user parameter is null
   */
  @Override
  @Transactional
  public void createUser(UserRequestModel userRequestModel) {
    createUserInternal(userRequestModel, true);
  }

  /**
   * Updates user.
   */
  @Override
  @Transactional
  public void updateUser(UserRequestModel user) {
    User existingUser = userRepository.findByIdWithAllRelations(user.getUserId(), getClientId());
    if (existingUser == null) {
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    // 1. Update address
    updateOrCreateUserAddress(user, existingUser);

    // 2. Update permissions
    updateUserPermissions(user, existingUser);

    // 3. Update group mappings
    updateUserGroups(user, existingUser);

    // 4. Update profile picture
    updateUserProfilePicture(user, existingUser);

    // 5. Update user fields (except email)
    User updatedUser = new User(user, getUser(), existingUser);
    User savedUser = userRepository.save(updatedUser);

    // 6. User log
    userLogService.logData(
        getUserId(),
        SuccessMessages.UserSuccessMessages.UPDATE_USER + " " + savedUser.getUserId(),
        ApiRoutes.UserSubRoute.UPDATE_USER);
  }

  /**
   * Retrieves users in the carrier in paginated batches with advanced filtering and sorting.
   * Fetches users associated with the current carrier in a paginated, filterable, and sortable
   * manner. Accepts a {@link UserRequestModel} containing pagination parameters (start, end),
   * filter expressions, column names for sorting, and other options. Returns a {@link
   * PaginationBaseResponseModel} with user data and total count, enabling efficient client-side
   * pagination and search. Ideal for large user lists in admin panels.
   *
   * @param userRequestModel The request model containing pagination, filter, and sort options.
   * @return {@link PaginationBaseResponseModel} of {@link UserResponseModel} for the requested
   *     batch.
   */
  @Override
  public PaginationBaseResponseModel<UserResponseModel> fetchUsersInCarrierInBatches(
      UserRequestModel userRequestModel) {
    // Validate pagination parameters
    int start = userRequestModel.getStart();
    int end = userRequestModel.getEnd();
    int limit = end - start;

    if (limit <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    // Define valid columns
    Set<String> validColumns =
        new HashSet<>(
            Arrays.asList(
                "userId",
                "firstName",
                "lastName",
                "loginName",
                "role",
                "dob",
                "phone",
                "address",
                "datePasswordChanges",
                "loginAttempts",
                "isDeleted",
                "locked",
                "emailConfirmed",
                "token",
                "isGuest",
                "apiKey",
                "email",
                "addressId",
                "profilePicture",
                "lastLoginAt",
                "createdAt",
                "createdUser",
                "updatedAt",
                "modifiedUser",
                "notes"));

    // Validate multi-filter mode
    if (userRequestModel.hasMultipleFilters()) {
      // Validate logic operator
      if (!userRequestModel.isValidLogicOperator()) {
        throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR);
      }

      // Validate each filter condition
      for (PaginationBaseRequestModel.FilterCondition filter : userRequestModel.getFilters()) {
        // Validate column name
        if (!validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              "Invalid column in filter: "
                  + filter.getColumn()
                  + ". Valid columns: "
                  + String.join(",", validColumns));
        }

        // Validate operator
        if (!filter.isValidOperator()) {
          throw new BadRequestException("Invalid operator in filter: " + filter.getOperator());
        }

        // Validate operator matches column type
        try {
          String columnType = userFilterQueryBuilder.getColumnType(filter.getColumn());
          filter.validateOperatorForType(columnType, filter.getColumn());
        } catch (IllegalArgumentException e) {
          throw new BadRequestException(e.getMessage());
        }

        // Validate value presence
        try {
          filter.validateValuePresence();
        } catch (IllegalArgumentException e) {
          throw new BadRequestException(e.getMessage());
        }
      }
    }

    // Create custom Pageable with exact OFFSET and LIMIT for database-level
    // pagination
    // Spring's PageRequest.of(page, size) uses: OFFSET = page * size, LIMIT = size
    // For arbitrary offsets (e.g., start=5, end=15), we need OFFSET=5, LIMIT=10
    // Solution: Override getOffset() to return the exact start position
    // Default sort: userId DESC (newest users first)
    org.springframework.data.domain.Pageable pageable =
        new org.springframework.data.domain.PageRequest(
            0, limit, Sort.by(Sort.Direction.DESC, "userId")) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // Always use UserFilterQueryBuilder for dynamic filtering
    Page<User> page =
        userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            userRequestModel.getSelectedUserIds(),
            userRequestModel.getLogicOperator() != null
                ? userRequestModel.getLogicOperator().toUpperCase()
                : "AND",
            userRequestModel.getFilters(),
            userRequestModel.isIncludeDeleted(),
            pageable);

    PaginationBaseResponseModel<UserResponseModel> paginationBaseResponseModel =
        new PaginationBaseResponseModel<>();
    List<UserResponseModel> userResponseModels = new ArrayList<>();
    for (User user : page.getContent()) {
      UserResponseModel userResponseModel = new UserResponseModel(user);
      userResponseModels.add(userResponseModel);
    }

    // Return the total count of all matching users, not just the current page
    paginationBaseResponseModel.setData(userResponseModels);
    paginationBaseResponseModel.setTotalDataCount(page.getTotalElements());
    return paginationBaseResponseModel;
  }

  /**
   * Confirms a user's email address using the verification token. This is a public endpoint (no.
   * authentication required) as users haven't logged in yet.
   */
  @Override
  public void confirmEmail(Long userId, String token) {
    // 1. Validate that user exists (without client check since this is a public
    // endpoint)
    Optional<User> userOptional = userRepository.findById(userId);
    if (userOptional.isEmpty()) {
      throw new NotFoundException(ErrorMessages.UserErrorMessages.INVALID_ID);
    }

    User user = userOptional.get();

    // 2. Verify the token matches
    if (user.getToken() == null || !user.getToken().equals(token)) {
      throw new BadRequestException(ErrorMessages.LoginErrorMessages.INVALID_TOKEN);
    }

    // 3. Check if email is already confirmed
    if (user.getEmailConfirmed() != null && user.getEmailConfirmed()) {
      throw new BadRequestException(ErrorMessages.LoginErrorMessages.ACCOUNT_CONFIRMED);
    }

    // 4. Set emailConfirmed to true
    user.setEmailConfirmed(true);
    userRepository.save(user);

    // Note: Skipping user log for this public endpoint as there's no authenticated
    // user context
  }

  /**
   * Retrieves all permissions available in the system.
   *
   * <p>This method fetches all permissions from the database including their permission ID, name,
   * code, description, and category. This is useful for populating permission selection dropdowns
   * in user management interfaces. Only non-deleted permissions are returned, sorted by
   * permissionId ascending.
   *
   * @return List of PermissionResponseModel containing all permissions
   */
  @Override
  public List<PermissionResponseModel> getAllPermissions() {
    return permissionRepository.findAll().stream()
        .filter(permission -> !permission.getIsDeleted())
        .map(PermissionResponseModel::new)
        .sorted((p1, p2) -> Long.compare(p1.getPermissionId(), p2.getPermissionId()))
        .toList();
  }

  /**
   * Creates multiple users asynchronously in the system with partial success support.
   *
   * <p>This method processes users in a background thread with the following characteristics: -
   * Supports partial success: if some users fail validation, others still succeed - Does NOT send
   * email confirmations (unlike createUser) - Sends detailed results to user via message
   * notification after processing completes - NOT_SUPPORTED: Runs without a transaction to avoid
   * rollback-only issues when individual user creations fail
   *
   * @param users List of UserRequestModel containing the user data to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  @Override
  @org.springframework.scheduling.annotation.Async
  @org.springframework.transaction.annotation.Transactional(
      propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  public void bulkCreateUsersAsync(
      List<UserRequestModel> users,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId) {
    // Validate input immediately - should throw BadRequestException even if async
    if (users == null || users.isEmpty()) {
      throw new BadRequestException(
          String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "User"));
    }

    try {
      BulkUserInsertResponseModel response = new BulkUserInsertResponseModel();
      response.setTotalRequested(users.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each user individually
      for (UserRequestModel userRequest : users) {
        try {
          // Call createUser with sendEmail = false, explicit createdUser, and shouldLog =
          // false (bulk logs collectively)
          createUserInternal(userRequest, false, requestingUserLoginName, false);

          // If we get here, user was created successfully
          // Fetch the created user to get the userId
          User createdUser = userRepository.findByLoginName(userRequest.getLoginName());
          response.addSuccess(userRequest.getLoginName(), createdUser.getUserId());
          successCount++;

        } catch (BadRequestException bre) {
          // Validation or business logic error
          response.addFailure(
              userRequest.getLoginName() != null ? userRequest.getLoginName() : "unknown",
              bre.getMessage());
          failureCount++;
        } catch (Exception e) {
          // Unexpected error
          response.addFailure(
              userRequest.getLoginName() != null ? userRequest.getLoginName() : "unknown",
              "Error: " + e.getMessage());
          failureCount++;
        }
      }

      // Log bulk user creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.UserSuccessMessages.CREATE_USER
              + " (Bulk: "
              + successCount
              + " succeeded, "
              + failureCount
              + " failed)",
          ApiRoutes.UsersSubRoute.BULK_CREATE_USER);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the helper (using
      // captured context)
      BulkInsertHelper.createBulkUserInsertResultMessage(
          response, messageService, requestingUserId, requestingUserLoginName, requestingClientId);

    } catch (Exception e) {
      logger.error(e);
      // Still send a message to user about the failure (using captured userId)
      BulkUserInsertResponseModel errorResponse = new BulkUserInsertResponseModel();
      errorResponse.setTotalRequested(users != null ? users.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(users != null ? users.size() : 0);
      errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
      BulkInsertHelper.createBulkUserInsertResultMessage(
          errorResponse,
          messageService,
          requestingUserId,
          requestingUserLoginName,
          requestingClientId);
    }
  }

  // ==================== HELPER METHODS ====================

  /**
   * Creates a new user in the system with optional email sending. Helper method that delegates to.
   * the overloaded createUser with explicit createdUser.
   *
   * @param userRequestModel The UserRequestModel containing the user data to create
   * @param sendEmail Whether to send confirmation email to the user
   * @throws BadRequestException if the user data is invalid or incomplete
   */
  @Transactional
  protected void createUserInternal(UserRequestModel userRequestModel, boolean sendEmail) {
    createUserInternal(userRequestModel, sendEmail, getUser(), true);
  }

  /**
   * Creates a new user in the system with optional email sending and explicit createdUser. This.
   * variant is used for async operations where security context is not available.
   *
   * @param userRequestModel The UserRequestModel containing the user data to create
   * @param sendEmail Whether to send confirmation email to the user
   * @param createdUser The loginName of the user creating this user (for async operations)
   * @param shouldLog Whether to log this individual user creation (false for bulk operations)
   * @throws BadRequestException if the user data is invalid or incomplete
   */
  @Transactional
  protected void createUserInternal(
      UserRequestModel userRequestModel, boolean sendEmail, String createdUser, boolean shouldLog) {
    // 1. Check if user email already exists
    if (userRepository.findByLoginName(userRequestModel.getLoginName()) != null) {
      throw new BadRequestException(
          ErrorMessages.UserErrorMessages.INVALID_EMAIL + " - Login name (email) already exists");
    }

    // 2. Generate password and set security fields
    String password = PasswordHelper.getRandomPassword();
    String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(password);
    userRequestModel.setSalt(saltAndHash[0]);
    userRequestModel.setPassword(saltAndHash[1]);
    userRequestModel.setApiKey(PasswordHelper.getToken(userRequestModel.getLoginName()));
    userRequestModel.setToken(PasswordHelper.getToken(userRequestModel.getLoginName()));

    // 3. Create and save the user
    User newUser = new User(userRequestModel, createdUser);
    User savedUser = userRepository.save(newUser);

    // 4. Create address if provided
    createUserAddress(userRequestModel, savedUser, createdUser);

    // 5. Create permission mappings
    createUserPermissions(userRequestModel, savedUser, createdUser);

    // 6. Create user group mappings
    createUserGroups(userRequestModel, savedUser, createdUser);

    // 7. Create user-client mapping
    createUserClientMapping(userRequestModel, savedUser, createdUser);

    // 8. Upload profile picture if present
    uploadUserProfilePicture(userRequestModel, savedUser);

    // 9. Send account confirmation email (if requested)
    if (sendEmail) {
      sendUserConfirmationEmail(savedUser, password);
    }

    // 10. Log user creation (skip for bulk operations as they log collectively)
    if (shouldLog) {
      userLogService.logData(
          getUserId(),
          SuccessMessages.UserSuccessMessages.CREATE_USER + " " + savedUser.getUserId(),
          ApiRoutes.UserSubRoute.CREATE_USER);
    }
  }

  /**
   * Creates address for a user if provided in the request.
   *
   * @param userRequestModel The user request containing address data
   * @param savedUser The saved user entity
   * @param createdUser The loginName of the user creating this address
   * @return Updated user entity with addressId set
   */
  private User createUserAddress(
      UserRequestModel userRequestModel, User savedUser, String createdUser) {
    if (userRequestModel.getAddress() != null) {
      if (userRequestModel.getAddress().getUserId() == null) {
        userRequestModel.getAddress().setUserId(savedUser.getUserId());
      }
      if (userRequestModel.getAddress().getClientId() == null) {
        userRequestModel.getAddress().setClientId(getClientId());
      }

      Address address = new Address(userRequestModel.getAddress(), createdUser);
      Address savedAddress = addressRepository.save(address);
      savedUser.setAddressId(savedAddress.getAddressId());
      userRepository.save(savedUser);
    }
    return savedUser;
  }

  /**
   * Creates permission mappings for a user.
   *
   * @param userRequestModel The user request containing permission IDs
   * @param savedUser The saved user entity
   * @param createdUser The loginName of the user creating these permissions
   */
  private void createUserPermissions(
      UserRequestModel userRequestModel, User savedUser, String createdUser) {
    if (userRequestModel.getPermissionIds() == null
        || userRequestModel.getPermissionIds().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED);
    }

    List<UserClientPermissionMapping> permissionMappings = new ArrayList<>();
    for (Long permissionId : userRequestModel.getPermissionIds()) {
      permissionMappings.add(
          new UserClientPermissionMapping(
              savedUser.getUserId(), getClientId(), permissionId, createdUser, createdUser));
    }
    userClientPermissionMappingRepository.saveAll(permissionMappings);
  }

  /**
   * Creates user group mappings for a user.
   *
   * @param userRequestModel The user request containing group IDs
   * @param savedUser The saved user entity
   * @param createdUser The loginName of the user creating these group mappings
   */
  private void createUserGroups(
      UserRequestModel userRequestModel, User savedUser, String createdUser) {
    if (userRequestModel.getSelectedGroupIds() != null
        && !userRequestModel.getSelectedGroupIds().isEmpty()) {
      List<UserGroupUserMap> groupMappings = new ArrayList<>();
      for (Long groupId : userRequestModel.getSelectedGroupIds()) {
        groupMappings.add(new UserGroupUserMap(savedUser.getUserId(), groupId, createdUser));
      }
      userGroupUserMapRepository.saveAll(groupMappings);
    }
  }

  /**
   * Creates user-client mapping.
   *
   * @param userRequestModel The user request containing API key
   * @param savedUser The saved user entity
   * @param createdUser The loginName of the user creating this mapping
   */
  private void createUserClientMapping(
      UserRequestModel userRequestModel, User savedUser, String createdUser) {
    UserClientMapping userClientMapping =
        new UserClientMapping(
            savedUser.getUserId(),
            getClientId(),
            userRequestModel.getApiKey(),
            createdUser,
            createdUser);
    userClientMappingRepository.save(userClientMapping);
  }

  /**
   * Uploads profile picture for a user if provided.
   *
   * @param userRequestModel The user request containing profile picture base64
   * @param savedUser The saved user entity
   */
  private void uploadUserProfilePicture(UserRequestModel userRequestModel, User savedUser) {
    if (userRequestModel.getProfilePictureBase64() == null
        || userRequestModel.getProfilePictureBase64().isEmpty()
        || userRequestModel.getProfilePictureBase64().isBlank()) {
      return;
    }

    Long clientId = getClientId();
    ClientResponseModel clientDetails = clientService.getClientById(clientId);
    String environmentName =
        environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";

    boolean isSuccess;

    if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
      Client client =
          clientRepository
              .findById(clientId)
              .orElseThrow(
                  () -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));

      if (client.getImgbbApiKey() == null || client.getImgbbApiKey().trim().isEmpty()) {
        throw new BadRequestException(
            ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED);
      }

      String customFileName =
          ImgbbHelper.generateCustomFileNameForUserProfile(
              environmentName, clientDetails.getName(), savedUser.getUserId());

      ImgbbHelper imgbbHelper = new ImgbbHelper(client.getImgbbApiKey());
      ImgbbHelper.ImgbbUploadResponse uploadResponse =
          imgbbHelper.uploadFileToImgbb(userRequestModel.getProfilePictureBase64(), customFileName);

      if (uploadResponse != null && uploadResponse.getUrl() != null) {
        savedUser.setProfilePicture(uploadResponse.getUrl());
        savedUser.setProfilePictureDeleteHash(uploadResponse.getDeleteHash());
        userRepository.save(savedUser);
        isSuccess = true;
      } else {
        isSuccess = false;
      }

    } else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
      Optional<GoogleCred> googleCred = googleCredRepository.findById(clientId);

      if (googleCred.isPresent()) {
        String filePath =
            clientDetails.getName()
                + " - "
                + clientId
                + File.separator
                + environmentName
                + File.separator
                + "UserProfiles"
                + File.separator
                + savedUser.getUserId()
                + "-"
                + savedUser.getLastName()
                + ".png";

        GoogleCred googleCredData = googleCred.get();
        FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);
        isSuccess =
            firebaseHelper.uploadFileToFirebase(
                userRequestModel.getProfilePictureBase64(), filePath);
      } else {
        throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
      }
    } else {
      throw new BadRequestException("Invalid imageLocation configuration: " + imageLocation);
    }

    if (!isSuccess) {
      throw new BadRequestException(ErrorMessages.UserErrorMessages.ER010);
    }
  }

  /**
   * Sends confirmation email to newly created user.
   *
   * @param savedUser The saved user entity
   * @param password The generated password
   */
  private void sendUserConfirmationEmail(User savedUser, String password) {
    Long clientId = getClientId();
    ClientResponseModel clientDetails = clientService.getClientById(clientId);
    Client client = clientRepository.findById(clientId).get();

    EmailTemplates emailTemplates =
        new EmailTemplates(
            clientDetails.getSendgridSenderName(),
            clientDetails.getSendGridEmailAddress(),
            clientDetails.getSendGridApiKey(),
            environment,
            client);

    try {
      boolean sendAccountConfirmationEmailResponse =
          emailTemplates.sendNewUserAccountConfirmation(
              savedUser.getUserId(), savedUser.getToken(), savedUser.getLoginName(), password);

      if (!sendAccountConfirmationEmailResponse) {
        throw new BadRequestException(
            ErrorMessages.CommonErrorMessages.FAILED_TO_SEND_CONFIRMATION_EMAIL);
      }
    } catch (Exception e) {
      throw new BadRequestException("Failed to send confirmation email: " + e.getMessage());
    }
  }

  /**
   * Updates or creates address for an existing user.
   *
   * @param userRequestModel The user request containing address data
   * @param existingUser The existing user entity
   */
  private void updateOrCreateUserAddress(UserRequestModel userRequestModel, User existingUser) {
    if (userRequestModel.getAddress() != null) {
      // Set userId and clientId on the address request model if not already set
      if (userRequestModel.getAddress().getUserId() == null) {
        userRequestModel.getAddress().setUserId(existingUser.getUserId());
      }
      if (userRequestModel.getAddress().getClientId() == null) {
        userRequestModel.getAddress().setClientId(getClientId());
      }

      if (existingUser.getAddressId() != null) {
        Address existingAddress =
            addressRepository.findById(existingUser.getAddressId()).orElse(null);
        if (existingAddress != null) {
          // Use the update constructor
          Address updatedAddress =
              new Address(userRequestModel.getAddress(), getUser(), existingAddress);
          addressRepository.save(updatedAddress);
        } else {
          Address newAddress = new Address(userRequestModel.getAddress(), getUser());
          Address savedAddress = addressRepository.save(newAddress);
          existingUser.setAddressId(savedAddress.getAddressId());
          userRepository.save(existingUser);
        }
      } else {
        Address newAddress = new Address(userRequestModel.getAddress(), getUser());
        Address savedAddress = addressRepository.save(newAddress);
        existingUser.setAddressId(savedAddress.getAddressId());
        userRepository.save(existingUser);
      }
    }
  }

  /**
   * Updates permission mappings for an existing user. Deletes all existing permissions and creates.
   * new ones.
   *
   * @param userRequestModel The user request containing permission IDs
   * @param existingUser The existing user entity
   */
  private void updateUserPermissions(UserRequestModel userRequestModel, User existingUser) {
    if (userRequestModel.getPermissionIds() == null
        || userRequestModel.getPermissionIds().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.CommonErrorMessages.AT_LEAST_ONE_PERMISSION_REQUIRED);
    }

    // Remove all existing permission mappings for this user and client
    userClientPermissionMappingRepository.deleteByUserIdAndClientId(
        existingUser.getUserId(), getClientId());

    // Add new permission mappings
    List<UserClientPermissionMapping> newPerms = new ArrayList<>();
    for (Long permissionId : userRequestModel.getPermissionIds()) {
      newPerms.add(
          new UserClientPermissionMapping(
              existingUser.getUserId(), getClientId(), permissionId, getUser(), getUser()));
    }
    userClientPermissionMappingRepository.saveAll(newPerms);
  }

  /**
   * Updates user group mappings for an existing user. Deletes all existing group mappings and.
   * creates new ones if provided.
   *
   * @param userRequestModel The user request containing group IDs
   * @param existingUser The existing user entity
   */
  private void updateUserGroups(UserRequestModel userRequestModel, User existingUser) {
    // Remove all existing group mappings
    userGroupUserMapRepository.deleteByUserId(existingUser.getUserId());

    // Add new group mappings if any
    if (userRequestModel.getSelectedGroupIds() != null
        && !userRequestModel.getSelectedGroupIds().isEmpty()) {
      List<UserGroupUserMap> newGroups = new ArrayList<>();
      for (Long groupId : userRequestModel.getSelectedGroupIds()) {
        newGroups.add(new UserGroupUserMap(existingUser.getUserId(), groupId, getUser()));
      }
      userGroupUserMapRepository.saveAll(newGroups);
    }
  }

  /**
   * Updates profile picture for an existing user. Handles deletion of old picture and upload of
   * new. one based on the request.
   *
   * @param userRequestModel The user request containing profile picture base64
   * @param existingUser The existing user entity
   */
  private void updateUserProfilePicture(UserRequestModel userRequestModel, User existingUser) {
    boolean hasNewProfilePic =
        userRequestModel.getProfilePictureBase64() != null
            && !userRequestModel.getProfilePictureBase64().isEmpty()
            && !userRequestModel.getProfilePictureBase64().isBlank();

    Long clientId = getClientId();
    ClientResponseModel clientDetails = clientService.getClientById(clientId);
    String environmentName =
        environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";

    // Use ImgBB or Firebase based on configuration
    if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
      // ImgBB-based profile picture management
      Client client =
          clientRepository
              .findById(clientId)
              .orElseThrow(
                  () -> new NotFoundException(ErrorMessages.ClientErrorMessages.INVALID_ID));

      if (client.getImgbbApiKey() == null || client.getImgbbApiKey().trim().isEmpty()) {
        throw new BadRequestException(
            ErrorMessages.ConfigurationErrorMessages.IMGBB_API_KEY_NOT_CONFIGURED);
      }

      ImgbbHelper imgbbHelper = new ImgbbHelper(client.getImgbbApiKey());

      // Handle profile picture update based on request
      if (!hasNewProfilePic) {
        // If no profile picture in request, delete the old picture from ImgBB and clear
        // from database
        if (existingUser.getProfilePictureDeleteHash() != null
            && !existingUser.getProfilePictureDeleteHash().isEmpty()) {
          imgbbHelper.deleteImage(existingUser.getProfilePictureDeleteHash());
        }
        existingUser.setProfilePicture(null);
        existingUser.setProfilePictureDeleteHash(null);
        userRepository.save(existingUser);
      } else {
        // Delete old profile picture from ImgBB before uploading new one
        if (existingUser.getProfilePictureDeleteHash() != null
            && !existingUser.getProfilePictureDeleteHash().isEmpty()) {
          imgbbHelper.deleteImage(existingUser.getProfilePictureDeleteHash());
        }

        // Generate custom filename for ImgBB
        String customFileName =
            ImgbbHelper.generateCustomFileNameForUserProfile(
                environmentName, clientDetails.getName(), existingUser.getUserId());

        // Upload new profile picture to ImgBB
        ImgbbHelper.ImgbbUploadResponse uploadResponse =
            imgbbHelper.uploadFileToImgbb(
                userRequestModel.getProfilePictureBase64(), customFileName);

        if (uploadResponse != null && uploadResponse.getUrl() != null) {
          // Save both the new profile picture URL and delete hash to the database
          existingUser.setProfilePicture(uploadResponse.getUrl());
          existingUser.setProfilePictureDeleteHash(uploadResponse.getDeleteHash());
          userRepository.save(existingUser);
        } else {
          throw new BadRequestException(ErrorMessages.UserErrorMessages.ER010);
        }
      }

    } else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
      // Firebase-based profile picture management
      Optional<GoogleCred> googleCred = googleCredRepository.findById(clientId);

      if (googleCred.isPresent()) {
        String filePath =
            clientDetails.getName()
                + " - "
                + clientId
                + File.separator
                + environmentName
                + File.separator
                + "UserProfiles"
                + File.separator
                + existingUser.getUserId()
                + "-"
                + existingUser.getLastName()
                + ".png";

        GoogleCred googleCredData = googleCred.get();
        FirebaseHelper firebaseHelper = new FirebaseHelper(googleCredData);

        if (hasNewProfilePic) {
          // Delete old profile pic if exists
          firebaseHelper.deleteFile(filePath);
          boolean isSuccess =
              firebaseHelper.uploadFileToFirebase(
                  userRequestModel.getProfilePictureBase64(), filePath);
          if (!isSuccess) {
            throw new BadRequestException(ErrorMessages.UserErrorMessages.ER010);
          }
        } else {
          // No new profile pic, delete existing if exists
          firebaseHelper.deleteFile(filePath);
        }
      } else {
        throw new BadRequestException(ErrorMessages.UserErrorMessages.ER011);
      }
    } else {
      throw new BadRequestException("Invalid imageLocation configuration: " + imageLocation);
    }
  }
}
