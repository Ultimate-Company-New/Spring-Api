package com.example.SpringApi.Services;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.Interface.IUserLogSubTranslator;

import jakarta.servlet.http.HttpServletRequest;
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
    public UserLogService(UserLogRepository userLogRepository,
                               HttpServletRequest request){
        super(request);
        this.userLogRepository = userLogRepository;
    }

    @Override
    public Boolean logData(String user, String change, String oldValue, String newValue) {
        try {
            long userId = Long.parseLong(user);
            UserLog userLog = new UserLog(userId, change, oldValue, newValue);
            userLogRepository.save(userLog);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Boolean logData(String user, String newValue, String endPoint) {
        try {
            long userId = Long.parseLong(user);
            UserLog userLog = new UserLog(userId, endPoint, null, newValue);
            Long auditUserId = getUserId();
            if(auditUserId != null){
                userLog.setAuditUserId(auditUserId);
            }
            // If auditUserId is null, leave it as null (no authentication)
            userLogRepository.save(userLog);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public Boolean logData(long userId, String newValue, String endPoint) {
        UserLog userLog = new UserLog(userId, endPoint, null, newValue);
        Long auditUserId = getUserId();
        if(auditUserId != null){
            userLog.setAuditUserId(auditUserId);
        }
        // If auditUserId is null, leave it as null (no authentication)
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
            Set<String> validColumns = new HashSet<>(Arrays.asList("change", "oldValue", "newValue"));

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