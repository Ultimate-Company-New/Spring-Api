package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Helpers.EmailTemplates;

import com.example.SpringApi.Models.RequestModels.LoginRequestModel;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.Interface.ILoginSubTranslator;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Helpers.PasswordHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Service class responsible for handling user authentication, registration, and related operations.
 * This includes sign-in, sign-up, password reset, email confirmation, and token generation.
 * Implements ILoginSubTranslator interface for standardized login sub-routes.
 */
@Service
public class LoginService implements ILoginSubTranslator {
    private final UserRepository userRepository;
    private final UserClientMappingRepository userClientMappingRepository;
    private final UserClientPermissionMappingRepository userClientPermissionMappingRepository;
    private final ClientRepository clientRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Environment environment;

    @Autowired
    public LoginService(UserRepository userRepository,
                             UserClientMappingRepository userClientMappingRepository,
                             UserClientPermissionMappingRepository userClientPermissionMappingRepository,
                             ClientRepository clientRepository,
                             JwtTokenProvider jwtTokenProvider,
                             Environment environment) {
        this.userRepository = userRepository;
        this.userClientMappingRepository = userClientMappingRepository;
        this.userClientPermissionMappingRepository = userClientPermissionMappingRepository;
        this.clientRepository = clientRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.environment = environment;
    }

    /**
     * Confirms the user's email address using the provided user ID and token.
     * This method verifies that the token matches the user's stored token and marks the email as confirmed.
     * After successful confirmation, the token is cleared from the database to prevent reuse.
     * If the token is invalid, expired, or the user is not found, appropriate exceptions are thrown.
     *
     * @param loginRequestModel The login request model containing the user ID and token for email confirmation.
     * @throws BadRequestException If the user ID is null or missing.
     * @throws UnauthorizedException If the provided token does not match the user's stored token, token is missing, or token has been used.
     * @throws NotFoundException If the user with the specified ID is not found.
     */
    @Override
    public void confirmEmail(LoginRequestModel loginRequestModel) {
        // Validate that userId is provided
        if(loginRequestModel.getUserId() == null) {
            throw new BadRequestException(ErrorMessages.LoginErrorMessages.InvalidId);
        }
        
        Optional<User> userResponse = userRepository.findById(loginRequestModel.getUserId());
        if(userResponse.isPresent())
        {
            User user = userResponse.get();
            
            // Check if token is null or empty (already used/expired)
            if(user.getToken() == null || user.getToken().isEmpty() || user.getToken().isBlank()) {
                throw new NotFoundException(ErrorMessages.LoginErrorMessages.InvalidToken);
            }
            
            if(user.getToken().equals(loginRequestModel.getToken())){
                user.setEmailConfirmed(true);
                user.setToken(null); // Clear the token after successful confirmation
                userRepository.save(user);
            }
            else{
                throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.InvalidToken);
            }
        }
        else {
            throw new NotFoundException(ErrorMessages.LoginErrorMessages.InvalidId);
        }
    }

    /**
     * Authenticates a user by verifying their login name and password.
     * Performs several checks: validates input, checks if user exists, email is confirmed, account is not locked,
     * password is set, and password matches. On successful authentication, returns a list of clients the user has access to.
     * On failed attempts, decrements locked attempts and locks the account if attempts reach zero.
     *
     * @param loginRequestModel The login request model containing the login name and password.
     * @return A list of ClientResponseModel containing logo, name, clientId, and apiKey for each client the user has access to.
     * @throws BadRequestException If login name or password is missing or invalid.
     * @throws NotFoundException If the user with the specified login name is not found.
     * @throws UnauthorizedException If email is not confirmed, account is locked, password is not set, or credentials are invalid.
     */
    @Override
    public List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> signIn(LoginRequestModel loginRequestModel) {
        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());

        if(!StringUtils.hasText(loginRequestModel.getLoginName())
        || !StringUtils.hasText(loginRequestModel.getPassword())){
            throw new BadRequestException(ErrorMessages.LoginErrorMessages.ER012);
        }

        //check if the user was found
        if(user == null) {
            throw new NotFoundException(ErrorMessages.LoginErrorMessages.InvalidEmail);
        }

        // check if the user's email has been confirmed
        if(user.getEmailConfirmed() == null || !user.getEmailConfirmed()) {
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.ER005);
        }

        // check if the user account is locked
        if(user.getLocked() != null && user.getLocked()) {
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.ER006);
        }

        // check if the user has a password
        if(user.getPassword() == null || user.getPassword().isBlank()){
            throw new BadRequestException(ErrorMessages.LoginErrorMessages.ER016);
        }

        // check if the password is correct
        if(PasswordHelper.checkPassword(loginRequestModel.getPassword(), user.getPassword(), user.getSalt())){
            // Reset login attempts to 5 after successful login and update lastLoginAt (UTC)
            user.setLoginAttempts(5);
            user.setLastLoginAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
            userRepository.save(user);
            
            // Get all UserClientMappings for this user
            List<UserClientMapping> userClientMappings = userClientMappingRepository.findByUserId(user.getUserId());
            
            // Build a list of ClientResponseModel with only logo, name, clientId, and apiKey
            List<com.example.SpringApi.Models.ResponseModels.ClientResponseModel> clientResponseList = new ArrayList<>();
            
            for (UserClientMapping mapping : userClientMappings) {
                // Get client details
                Optional<Client> clientOpt = clientRepository.findById(mapping.getClientId());
                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();
                    
                    // Create a minimal ClientResponseModel with only required fields
                    com.example.SpringApi.Models.ResponseModels.ClientResponseModel clientResponse = 
                        new com.example.SpringApi.Models.ResponseModels.ClientResponseModel();
                    clientResponse.setClientId(client.getClientId());
                    clientResponse.setName(client.getName());
                    clientResponse.setLogoUrl(client.getLogoUrl());
                    clientResponse.setApiKey(mapping.getApiKey());
                    
                    clientResponseList.add(clientResponse);
                }
            }
            
            // Sort clients by name in ascending order (A to Z)
            clientResponseList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
            
            return clientResponseList;
        }

        // do the procedure for invalid login
        user.setLoginAttempts(user.getLoginAttempts() - 1);
        if(user.getLoginAttempts() == 0) {
            user.setLocked(true);
            userRepository.save(user);
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.ER007);
        }
        else {
            userRepository.save(user);
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.InvalidCredentials);
        }
    }

    /**
     * Resets the password for an existing user.
     * Validates the login name, checks if the user exists and has a password set.
     * Generates a new random password, hashes it, updates the user record, unlocks the account, resets login attempts to 5,
     * sends a reset password email, and logs the action.
     * The client is determined dynamically (currently hardcoded to the first client).
     *
     * @param loginRequestModel The login request model containing the login name for password reset.
     * @return True if the password reset is successful.
     * @throws BadRequestException If the login name is missing or the user does not have a password set.
     * @throws NotFoundException If the user with the specified login name is not found.
     */
    @Override
    public Boolean resetPassword(LoginRequestModel loginRequestModel) {
        if(!StringUtils.hasText(loginRequestModel.getLoginName())){
            throw new BadRequestException(ErrorMessages.LoginErrorMessages.ER014);
        }

        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());
        if(user != null){
            if(user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()){
                throw new BadRequestException(ErrorMessages.LoginErrorMessages.ER003);
            }
            String randomPassword = PasswordHelper.getRandomPassword();
            String[] saltAndHash = PasswordHelper.getHashedPasswordAndSalt(randomPassword);

            // set user defaults
            user.setSalt(saltAndHash[0]);
            user.setPassword(saltAndHash[1]);
            user.setLocked(false);
            user.setLoginAttempts(5); // Reset login attempts to 5

            Client client = clientRepository.findFirstByOrderByClientIdAsc();
            if (client == null) {
                throw new RuntimeException(ErrorMessages.ConfigurationErrorMessages.NoClientConfigurationFound);
            }
            
            // Get email configuration from properties - all are required
            String senderEmail = environment.getProperty("email.sender.address");
            String senderName = environment.getProperty("email.sender.name");
            String sendGridApiKey = environment.getProperty("sendgrid.api.key");
            
            // Validate all required email configuration properties are present
            if (senderEmail == null || senderEmail.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.ConfigurationErrorMessages.SendGridEmailNotConfigured);
            }
            if (senderName == null || senderName.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.ConfigurationErrorMessages.SendGridNameNotConfigured);
            }
            if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.ConfigurationErrorMessages.SendGridApiKeyNotConfigured);
            }
            
            EmailTemplates emailTemplates = new EmailTemplates(senderName, senderEmail, sendGridApiKey, environment, client);
            boolean emailSent = emailTemplates.sendResetPasswordEmail(user.getLoginName(), randomPassword);
            
            // Verify that the email was sent successfully and contains the password
            if (!emailSent) {
                throw new RuntimeException("Failed to send reset password email");
            }
            
            // Save the user with updated password, salt, locked status, and login attempts
            userRepository.save(user);

            return true;
        }
        else{
            throw new NotFoundException(ErrorMessages.LoginErrorMessages.InvalidEmail);
        }
    }

    /**
     * Generates a JWT token for an authenticated user based on their API key.
     * 
     * This method:
     * 1. Validates the login name and API key are provided
     * 2. Finds the user by login name
     * 3. Finds the UserClientMapping by API key
     * 4. Verifies the API key belongs to the user (userId match)
     * 5. Extracts the clientId from the mapping
     * 6. Fetches user's permissions for that specific client
     * 7. Generates a JWT token with client-specific permissions
     *
     * @param loginRequestModel The login request model containing the login name and API key.
     * @return A JWT token string containing user permissions for the client associated with the API key.
     * @throws BadRequestException If login name or API key is missing.
     * @throws UnauthorizedException If the user is not found, API key is invalid, or API key doesn't belong to the user.
     */
    @Override
    public String getToken(LoginRequestModel loginRequestModel) {
        if(!StringUtils.hasText(loginRequestModel.getLoginName())
        || !StringUtils.hasText(loginRequestModel.getApiKey())){
            throw new BadRequestException(ErrorMessages.LoginErrorMessages.ER015);
        }

        // Find the user with the specified email
        User user = userRepository.findByLoginName(loginRequestModel.getLoginName());
        if(user == null){
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.InvalidCredentials);
        }

        // Find the UserClientMapping by apiKey
        UserClientMapping userClientMapping = userClientMappingRepository
            .findByApiKey(loginRequestModel.getApiKey())
            .orElseThrow(() -> new UnauthorizedException(ErrorMessages.LoginErrorMessages.InvalidCredentials));

        // Verify that the apiKey belongs to the user requesting it
        if(!userClientMapping.getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException(ErrorMessages.LoginErrorMessages.InvalidCredentials);
        }

        // Get the clientId from the mapping
        Long clientId = userClientMapping.getClientId();

        // Get user's permissions for this specific client
        List<UserClientPermissionMapping> userClientPermissionMappings = 
            userClientPermissionMappingRepository.findClientPermissionMappingByUserId(user.getUserId());
        
        // Filter permissions for the specific client
        List<Long> permissionIds = userClientPermissionMappings.stream()
            .filter(mapping -> mapping.getClientId().equals(clientId))
            .map(UserClientPermissionMapping::getPermissionId)
            .toList();

        return jwtTokenProvider.generateToken(user, permissionIds, clientId);
    }
}