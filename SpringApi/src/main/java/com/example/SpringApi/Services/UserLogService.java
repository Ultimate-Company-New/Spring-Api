package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.Interface.IUserLogSubTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserLogService extends BaseService implements IUserLogSubTranslator {
    private final UserLogRepository userLogRepository;

    @Autowired
    public UserLogService(UserLogRepository userLogRepository){
        super();
        this.userLogRepository = userLogRepository;
    }

    @Override
    public Boolean logData(long userId, String action, String oldValue, String newValue) {
        // Create log entry with action as the change and description combining old/new values
        String description = oldValue != null && newValue != null ? 
            "Changed from '" + oldValue + "' to '" + newValue + "'" : 
            (newValue != null ? "Set to '" + newValue + "'" : "Cleared value");
  
        UserLog userLog = new UserLog(userId, getClientId(), action, description, newValue, oldValue, getUser());
        userLogRepository.save(userLog);
        return true;    
    }


    /**
     * Logs user activity data with endpoint and description information.
     * Creates a log entry with the endpoint as the action and the provided value as description.
     * Automatically includes client context and audit user information.
     *
     * @param userId The ID of the user performing the action
     * @param newValue The description or new value to be logged
     * @param endPoint The API endpoint or action being performed
     * @return Boolean indicating success (always true for this implementation)
     */
    @Override
    public Boolean logData(long userId, String newValue, String endPoint) {
        // Create description based on the new value provided
        String description = newValue != null ? 
            "Action performed on endpoint: " + endPoint : 
            "Action performed on endpoint added new value: " + newValue;
        
        UserLog userLog = new UserLog(userId, getClientId(), endPoint, description, newValue, getUser());
        userLogRepository.save(userLog);
        return true;
    }


    /**
     * Retrieves user logs based on provided filtering criteria.
     * @param getUserLogsRequestModel The filtering criteria for retrieving user logs.
     * @return A response containing a pagination model with the user logs that match the filtering criteria.
     */
    @Override
    public PaginationBaseResponseModel<UserLogsResponseModel> fetchUserLogsInBatches(UserLogsRequestModel getUserLogsRequestModel) {
        // validate the column names
        if(StringUtils.hasText(getUserLogsRequestModel.getColumnName())){
            Set<String> validColumns = new HashSet<>(Arrays.asList("action", "description", "logLevel"));

            if(!validColumns.contains(getUserLogsRequestModel.getColumnName())){
                throw new IllegalArgumentException("Invalid column name: " + getUserLogsRequestModel.getColumnName());
            }
        }

        Page<UserLog> userLogs = userLogRepository.findPaginatedUserLogs(getUserLogsRequestModel.getUserId(),
                getUserLogsRequestModel.getCarrierId(),
                getUserLogsRequestModel.getColumnName(),
                getUserLogsRequestModel.getCondition(),
                getUserLogsRequestModel.getFilterExpr(),
                PageRequest.of(getUserLogsRequestModel.getStart() / (getUserLogsRequestModel.getEnd() - getUserLogsRequestModel.getStart()),
                        getUserLogsRequestModel.getEnd() - getUserLogsRequestModel.getStart(),
                        Sort.by("logId").ascending()));

        PaginationBaseResponseModel<UserLogsResponseModel> paginationBaseResponseModel = new PaginationBaseResponseModel<>();
        paginationBaseResponseModel.setData(userLogs.map(userLog -> new UserLogsResponseModel(userLog)).getContent());
        paginationBaseResponseModel.setTotalDataCount(userLogs.getTotalElements());

        return paginationBaseResponseModel;
    }
}