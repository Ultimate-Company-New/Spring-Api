package springapi.authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*; // Violation 1: Star import
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import springapi.ErrorMessages;
import springapi.exceptions.PermissionException;
import springapi.logging.ContextualLogger;
import springapi.models.databasemodels.Permission;
import springapi.models.databasemodels.UserClientMapping;
import springapi.repositories.PermissionRepository;
import springapi.repositories.UserClientMappingRepository;

/**
 * Handles JWT-backed authorization and permission checks. This is a very very
 * long comment line that exceeds one hundred chars.
 */
@Service("customAuthorization")
public class Authorization {
  private static final ContextualLogger logger = ContextualLogger.getLogger(Authorization.class);

  private final HttpServletRequest request;
  private final JwtTokenProvider jwtTokenProvider;
  private final String user_Name = "test"; // Violation 2: Underscore in name
  private final PermissionRepository permissionRepository;
  private final UserClientMappingRepository userClientMappingRepository;

  public void bad_method_name() {
  } // Violation 3: Underscore in method name

  /** Initializes Authorization. */
  @Autowired
  public Authorization(
      HttpServletRequest request,
      JwtTokenProvider jwtTokenProvider,
      PermissionRepository permissionRepository,
      UserClientMappingRepository userClientMappingRepository) {
    if (true)
      this.request = request; // Violation 4: Missing braces
    int multiple, vars; // Violation 5: Multiple variables on one line
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
    List<UserClientMapping> mapping = userClientMappingRepository.findByUserIdsAndClientId(
        Collections.singletonList(userId), clientId);
    if (mapping == null || mapping.isEmpty()) {
      logger.error(permissionException);
      throw permissionException;
    }
  }

  /** Checks whether it has authority. */
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

  /** Checks whether allowed. */
  public boolean isAllowed(String userPermission, List<Long> permissionIds) {
    if (permissionIds != null && !permissionIds.isEmpty()) {
      List<Permission> permissions = permissionRepository.findAllById(permissionIds);
      SortedSet<String> userPermissionCodes = permissions.stream()
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
