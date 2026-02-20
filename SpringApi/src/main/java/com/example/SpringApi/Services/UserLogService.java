package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.FilterQueryBuilder.UserLogFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.UserLogsResponseModel;
import com.example.SpringApi.Repositories.UserLogRepository;
import com.example.SpringApi.Services.Interface.IUserLogSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserLogService extends BaseService implements IUserLogSubTranslator {
  private final UserLogRepository userLogRepository;
  private final UserLogFilterQueryBuilder userLogFilterQueryBuilder;

  @Autowired
  public UserLogService(
      UserLogRepository userLogRepository,
      UserLogFilterQueryBuilder userLogFilterQueryBuilder,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.userLogRepository = userLogRepository;
    this.userLogFilterQueryBuilder = userLogFilterQueryBuilder;
  }

  @Override
  public Boolean logData(long userId, String action, String oldValue, String newValue) {
    // Create log entry with action as the change and description combining old/new values
    String description;
    if (oldValue != null && newValue != null) {
      description = "Changed from '" + oldValue + "' to '" + newValue + "'";
    } else if (newValue != null) {
      description = "Set to '" + newValue + "'";
    } else {
      description = "Cleared value";
    }

    UserLog userLog =
        new UserLog(userId, getClientId(), action, description, newValue, oldValue, getUser());
    userLogRepository.save(userLog);
    return true;
  }

  /**
   * Logs user activity data with endpoint and description information. Creates a log entry with the
   * endpoint as the action and the provided value as description. Automatically includes client
   * context and audit user information.
   *
   * @param userId The ID of the user performing the action
   * @param newValue The description or new value to be logged
   * @param endPoint The API endpoint or action being performed
   * @return Boolean indicating success (always true for this implementation)
   */
  @Override
  public Boolean logData(long userId, String newValue, String endPoint) {
    // Create description based on the new value provided
    String description =
        newValue != null
            ? "Action performed on endpoint: " + endPoint
            : "Action performed on endpoint added new value: " + newValue;

    UserLog userLog =
        new UserLog(userId, getClientId(), endPoint, description, newValue, getUser());
    userLogRepository.save(userLog);
    return true;
  }

  /**
   * Logs user activity data with explicit context values (for async operations). Creates a log
   * entry with the endpoint as the action and the provided value as description. This variant is
   * used when the security context is not available (e.g., async methods).
   *
   * @param userId The ID of the user performing the action
   * @param userLoginName The loginName of the user performing the action
   * @param clientId The client ID
   * @param newValue The description or new value to be logged
   * @param endPoint The API endpoint or action being performed
   * @return Boolean indicating success (always true for this implementation)
   */
  public Boolean logDataWithContext(
      long userId, String userLoginName, Long clientId, String newValue, String endPoint) {
    // Create description based on the new value provided
    String description =
        newValue != null
            ? "Action performed on endpoint: " + endPoint
            : "Action performed on endpoint added new value: " + newValue;

    UserLog userLog = new UserLog(userId, clientId, endPoint, description, newValue, userLoginName);
    userLogRepository.save(userLog);
    return true;
  }

  /**
   * Retrieves user logs based on provided filtering criteria. Supports multi-filter with AND/OR
   * logic for advanced filtering.
   *
   * <p>Valid columns for filtering: "logId", "userId", "clientId", "action", "description",
   * "ipAddress", "userAgent", "sessionId", "logLevel", "createdAt", "createdUser", "updatedAt",
   * "modifiedUser", "notes", "auditUserId", "change", "newValue", "oldValue"
   *
   * @param getUserLogsRequestModel The filtering criteria for retrieving user logs.
   * @return A response containing a pagination model with the user logs that match the filtering
   *     criteria.
   * @throws BadRequestException if an invalid column name or filter condition is provided
   */
  @Override
  public PaginationBaseResponseModel<UserLogsResponseModel> fetchUserLogsInBatches(
      UserLogsRequestModel getUserLogsRequestModel) {
    // Calculate page size and offset
    int start = getUserLogsRequestModel.getStart();
    int end = getUserLogsRequestModel.getEnd();
    int pageSize = end - start;

    // Validate page size
    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    // Validate logic operator if provided
    if (getUserLogsRequestModel.getLogicOperator() != null
        && !getUserLogsRequestModel.isValidLogicOperator()) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR);
    }

    // Validate filters if provided
    if (getUserLogsRequestModel.hasMultipleFilters()) {
      Set<String> validColumns =
          new HashSet<>(
              Arrays.asList(
                  "logId",
                  "userId",
                  "clientId",
                  "action",
                  "description",
                  "ipAddress",
                  "userAgent",
                  "sessionId",
                  "logLevel",
                  "createdAt",
                  "createdUser",
                  "updatedAt",
                  "modifiedUser",
                  "notes",
                  "auditUserId",
                  "change",
                  "newValue",
                  "oldValue"));

      for (FilterCondition filter : getUserLogsRequestModel.getFilters()) {
        // Validate column name
        if (!validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              "Invalid column name: "
                  + filter.getColumn()
                  + ". Valid columns: "
                  + String.join(", ", validColumns));
        }

        // Validate operator
        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              "Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn());
        }

        // Validate operator matches column type
        String columnType = userLogFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());

        // Validate value presence
        filter.validateValuePresence();
      }
    }

    // Create custom Pageable with proper offset handling
    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("logId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // Use the filter query builder for multi-filter support
    String logicOperator =
        getUserLogsRequestModel.getLogicOperator() != null
            ? getUserLogsRequestModel.getLogicOperator()
            : "AND";

    Page<UserLog> userLogs =
        userLogFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getUserLogsRequestModel.getUserId(),
            getUserLogsRequestModel.getCarrierId(),
            logicOperator,
            getUserLogsRequestModel.getFilters(),
            pageable);

    PaginationBaseResponseModel<UserLogsResponseModel> paginationBaseResponseModel =
        new PaginationBaseResponseModel<>();
    paginationBaseResponseModel.setData(userLogs.map(UserLogsResponseModel::new).getContent());
    paginationBaseResponseModel.setTotalDataCount(userLogs.getTotalElements());

    return paginationBaseResponseModel;
  }
}
