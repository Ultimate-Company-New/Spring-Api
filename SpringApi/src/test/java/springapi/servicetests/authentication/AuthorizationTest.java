package springapi.servicetests.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import springapi.ErrorMessages;
import springapi.authentication.Authorization;
import springapi.authentication.JwtTokenProvider;
import springapi.exceptions.PermissionException;
import springapi.models.databasemodels.Permission;
import springapi.models.databasemodels.UserClientMapping;
import springapi.repositories.PermissionRepository;
import springapi.repositories.UserClientMappingRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authorization Tests")
class AuthorizationTest {

  // Total Tests: 6

  @Mock private HttpServletRequest request;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private PermissionRepository permissionRepository;

  @Mock private UserClientMappingRepository userClientMappingRepository;

  /**
   * Purpose: Verify hasAuthority returns true when requested permission is empty after token
   * validation. Expected Result: Authorization passes. Assertions: hasAuthority returns true.
   */
  @Test
  @DisplayName("hasAuthority - Empty Permission Returns True - Success")
  void hasAuthority_s01_emptyPermissionReturnsTrue_success() {
    // Arrange
    Authorization authorization = createAuthorization();
    stubValidTokenAndMapping(1L, 10L);

    // Act
    boolean allowed = authorization.hasAuthority("");

    // Assert
    assertTrue(allowed);
  }

  /**
   * Purpose: Verify hasAuthority returns false when required permission is not present. Expected
   * Result: Authorization denied. Assertions: hasAuthority returns false.
   */
  @Test
  @DisplayName("hasAuthority - Missing Permission Returns False - Success")
  void hasAuthority_s02_missingPermissionReturnsFalse_success() {
    // Arrange
    Authorization authorization = createAuthorization();
    stubValidTokenAndMapping(2L, 20L);
    when(jwtTokenProvider.getUserPermissionIds(eq("token-value"))).thenReturn(List.of(101L));

    Permission permission = new Permission();
    permission.setPermissionId(101L);
    permission.setPermissionCode("READ_ONLY");
    when(permissionRepository.findAllById(eq(List.of(101L)))).thenReturn(List.of(permission));

    // Act
    boolean allowed = authorization.hasAuthority("WRITE");

    // Assert
    assertFalse(allowed);
  }

  /**
   * Purpose: Verify hasAuthority supports comma-separated required permissions and trims spaces.
   * Expected Result: Authorization granted when all required permissions exist. Assertions:
   * hasAuthority returns true.
   */
  @Test
  @DisplayName("hasAuthority - Comma Separated Permissions All Present - Success")
  void hasAuthority_s03_commaSeparatedPermissionsAllPresent_success() {
    // Arrange
    Authorization authorization = createAuthorization();
    stubValidTokenAndMapping(3L, 30L);
    when(jwtTokenProvider.getUserPermissionIds(eq("token-value"))).thenReturn(List.of(201L, 202L));

    Permission read = new Permission();
    read.setPermissionId(201L);
    read.setPermissionCode("READ");

    Permission write = new Permission();
    write.setPermissionId(202L);
    write.setPermissionCode("WRITE");

    when(permissionRepository.findAllById(eq(List.of(201L, 202L))))
        .thenReturn(List.of(read, write));

    // Act
    boolean allowed = authorization.hasAuthority("READ, WRITE");

    // Assert
    assertTrue(allowed);
  }

  /**
   * Purpose: Verify invalid token claims (missing userId/clientId) raise PermissionException.
   * Expected Result: Unauthorized exception thrown. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("hasAuthority - Missing Token Claims Throws PermissionException - Success")
  void hasAuthority_s04_missingTokenClaimsThrowsPermissionException_success() {
    // Arrange
    Authorization authorization = createAuthorization();
    when(request.getHeader("Authorization")).thenReturn("Bearer token-value");
    when(jwtTokenProvider.getUserIdFromToken(eq("token-value"))).thenReturn(null);
    when(jwtTokenProvider.getClientIdFromToken(eq("token-value"))).thenReturn(10L);

    // Act
    PermissionException exception =
        assertThrows(PermissionException.class, () -> authorization.hasAuthority("READ"));

    // Assert
    assertEquals(ErrorMessages.UNAUTHORIZED, exception.getMessage());
  }

  /**
   * Purpose: Verify invalid user-client mapping raises PermissionException even with valid claims.
   * Expected Result: Unauthorized exception thrown. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("hasAuthority - Missing UserClientMapping Throws PermissionException - Success")
  void hasAuthority_s05_missingUserClientMappingThrowsPermissionException_success() {
    // Arrange
    Authorization authorization = createAuthorization();
    when(request.getHeader("Authorization")).thenReturn("Bearer token-value");
    when(jwtTokenProvider.getUserIdFromToken(eq("token-value"))).thenReturn(7L);
    when(jwtTokenProvider.getClientIdFromToken(eq("token-value"))).thenReturn(77L);
    when(userClientMappingRepository.findByUserIdsAndClientId(eq(List.of(7L)), eq(77L)))
        .thenReturn(List.of());

    // Act
    PermissionException exception =
        assertThrows(PermissionException.class, () -> authorization.hasAuthority("READ"));

    // Assert
    assertEquals(ErrorMessages.UNAUTHORIZED, exception.getMessage());
  }

  /**
   * Purpose: Verify isAllowed handles empty permission IDs and missing required permission paths.
   * Expected Result: True when no permission IDs provided; false when required code missing.
   * Assertions: isAllowed boolean outcomes.
   */
  @Test
  @DisplayName("isAllowed - Empty And Missing Permission Paths - Success")
  void isAllowed_s06_emptyAndMissingPermissionPaths_success() {
    // Arrange
    Authorization authorization = createAuthorization();

    Permission permission = new Permission();
    permission.setPermissionId(301L);
    permission.setPermissionCode("VIEW");
    when(permissionRepository.findAllById(eq(List.of(301L)))).thenReturn(List.of(permission));

    // Act
    boolean emptyPermissionIds = authorization.isAllowed("ANY", List.of());
    boolean missingRequired = authorization.isAllowed("EDIT", List.of(301L));

    // Assert
    assertTrue(emptyPermissionIds);
    assertFalse(missingRequired);
  }

  private Authorization createAuthorization() {
    return new Authorization(
        request, jwtTokenProvider, permissionRepository, userClientMappingRepository);
  }

  private void stubValidTokenAndMapping(Long userId, Long clientId) {
    when(request.getHeader("Authorization")).thenReturn("Bearer token-value");
    when(jwtTokenProvider.getUserIdFromToken(eq("token-value"))).thenReturn(userId);
    when(jwtTokenProvider.getClientIdFromToken(eq("token-value"))).thenReturn(clientId);

    UserClientMapping mapping = new UserClientMapping();
    mapping.setUserId(userId);
    mapping.setClientId(clientId);
    when(userClientMappingRepository.findByUserIdsAndClientId(eq(List.of(userId)), eq(clientId)))
        .thenReturn(List.of(mapping));
  }
}
