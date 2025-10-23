package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;

/**
 * Interface for User Log-related business operations.
 *
 * This interface defines the contract for user log management operations including
 * logging user activities and retrieving user logs in batches.
 * All implementations should handle proper validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IUserLogSubTranslator {

    /**
     * Logs user data with full change details.
     *
     * @param user The user identifier as string
     * @param change The type of change
     * @param oldValue The old value before change
     * @param newValue The new value after change
     * @return true if logging was successful, false otherwise
     */
    Boolean logData(long user, String change, String oldValue, String newValue);

    /**
     * Logs user data with endpoint information using user ID.
     *
     * @param userId The user identifier as long
     * @param newValue The new value
     * @param endPoint The endpoint accessed
     * @return true if logging was successful, false otherwise
     */
    Boolean logData(long userId, String newValue, String endPoint);

    /**
     * Retrieves user logs in paginated batches based on filtering criteria.
     *
     * @param userLogsRequestModel The request model containing filtering and pagination parameters
     * @return PaginationBaseResponseModel containing the user logs
     */
    PaginationBaseResponseModel<UserLogsResponseModel> fetchUserLogsInBatches(UserLogsRequestModel userLogsRequestModel);
}