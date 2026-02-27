package com.example.SpringApi.Authentication;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.PermissionException;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Models.DatabaseModels.Permission;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Repositories.PermissionRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("customAuthorization")
public class Authorization {
  private static final ContextualLogger logger = ContextualLogger.getLogger(Authorization.class);

  private final HttpServletRequest request;
  private final JwtTokenProvider jwtTokenProvider;
  private final PermissionRepository permissionRepository;
  private final UserClientMappingRepository userClientMappingRepository;

  @Autowired
  public Authorization(
      HttpServletRequest request,
      JwtTokenProvider jwtTokenProvider,
      PermissionRepository permissionRepository,
      UserClientMappingRepository userClientMappingRepository) {
    this.request = request;
    this.jwtTokenProvider = jwtTokenProvider;
    this.permissionRepository = permissionRepository;
    this.userClientMappingRepository = userClientMappingRepository;
  }

  private String getJwtFromRequest() {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private void validateToken() {
    PermissionException permissionException = new PermissionException(ErrorMessages.UNAUTHORIZED);
    String token = getJwtFromRequest();

    Long userId = jwtTokenProvider.getUserIdFromToken(token);
    Long clientId = jwtTokenProvider.getClientIdFromToken(token);
    if (userId == null || clientId == null) {
      logger.error(permissionException);
      throw permissionException;
    }

    // Check if user is part of the client mapping
    List<UserClientMapping> mapping =
        userClientMappingRepository.findByUserIdsAndClientId(
            Collections.singletonList(userId), clientId);
    if (mapping == null || mapping.isEmpty()) {
      logger.error(permissionException);
      throw permissionException;
    }
  }

  public boolean hasAuthority(String userPermission) {
    validateToken();
    if (userPermission == null || userPermission.isEmpty()) {
      return true;
    }
    List<Long> permissionIds = jwtTokenProvider.getUserPermissionIds(getJwtFromRequest());
    if (!isAllowed(userPermission, permissionIds)) {
      PermissionException permissionException = new PermissionException(ErrorMessages.UNAUTHORIZED);
      logger.error(permissionException);
      return false;
    }
    return true;
  }

  public boolean isAllowed(String userPermission, List<Long> permissionIds) {
    if (permissionIds != null && !permissionIds.isEmpty()) {
      List<Permission> permissions = permissionRepository.findAllById(permissionIds);
      SortedSet<String> userPermissionCodes =
          permissions.stream()
              .map(Permission::getPermissionCode)
              .collect(Collectors.toCollection(TreeSet::new));
      String[] requiredPermissions = userPermission.split(",");
      for (String perm : requiredPermissions) {
        if (!userPermissionCodes.contains(perm.trim())) {
          return false;
        }
      }
    }
    return true;
  }
}
