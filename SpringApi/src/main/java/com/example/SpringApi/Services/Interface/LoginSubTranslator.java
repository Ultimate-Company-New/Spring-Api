package com.example.springapi.services.interfaces;

import com.example.springapi.models.requestmodels.LoginRequestModel;
import com.example.springapi.models.responsemodels.ClientResponseModel;
import java.util.List;

/**
 * Interface for login-related operations, defining the contract for user authentication and.
 * management services. Implementations handle sign-in, password reset, email confirmation, and
 * token generation.
 */
public interface LoginSubTranslator {
  /**
   * Confirms the user's email address using the provided user ID and token. This method verifies.
   * that the token matches the user's stored token and marks the email as confirmed. If the token
   * is invalid or the user is not found, appropriate exceptions are thrown.
   *
   * @param loginRequestModel The login request model containing the user ID and token for email
   *     confirmation.
   * @throws UnauthorizedException If the provided token does not match the user's stored token.
   * @throws NotFoundException If the user with the specified ID is not found.
   */
  void confirmEmail(LoginRequestModel loginRequestModel);

  /**
   * Authenticates a user by verifying their login name and password. Performs several checks:.
   * validates input, checks if user exists, email is confirmed, account is not locked, password is
   * set, and password matches. On successful authentication, returns a list of clients the user has
   * access to. On failed attempts, decrements locked attempts and locks the account if attempts
   * reach zero.
   *
   * @param loginRequestModel The login request model containing the login name and password.
   * @return A list of ClientResponseModel containing logo, name, clientId, and apiKey for each
   *     client the user has access to.
   * @throws BadRequestException If login name or password is missing or invalid.
   * @throws NotFoundException If the user with the specified login name is not found.
   * @throws UnauthorizedException If email is not confirmed, account is locked, password is not
   *     set, or credentials are invalid.
   */
  List<ClientResponseModel> signIn(LoginRequestModel loginRequestModel);

  /**
   * Resets the password for an existing user. Validates the login name, checks if the user exists.
   * and has a password set. Generates a new random password, hashes it, updates the user record,
   * resets locked attempts and unlocks the account, sends a reset password email, and logs the
   * action. The client is determined dynamically (currently hardcoded to the first client).
   *
   * @param loginRequestModel The login request model containing the login name for password reset.
   * @return True if the password reset is successful.
   * @throws BadRequestException If the login name is missing or the user does not have a password
   *     set.
   * @throws NotFoundException If the user with the specified login name is not found.
   */
  Boolean resetPassword(LoginRequestModel loginRequestModel);

  /**
   * Generates a JWT token for an authenticated user based on their API key. Validates the login.
   * name and API key, retrieves the user, verifies the API key matches, fetches the user's client
   * permissions, and generates a token with the permissions. Logs the token generation action.
   *
   * @param loginRequestModel The login request model containing the login name and API key.
   * @return A JWT token string containing user permissions.
   * @throws BadRequestException If login name or API key is missing.
   * @throws UnauthorizedException If the user is not found or the API key is invalid.
   */
  String getToken(LoginRequestModel loginRequestModel);
}
