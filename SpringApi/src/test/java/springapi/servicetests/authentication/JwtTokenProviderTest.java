package springapi.servicetests.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.authentication.JwtTokenProvider;
import springapi.helpers.PasswordHelper;
import springapi.models.databasemodels.User;

@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

  // Total Tests: 6
  private static final String JWT_SECRET_32 = generateSecret();
  private static final String WEB_API_KEY_32 = generateSecret();

  /**
   * Purpose: Verify generated JWT token can be parsed for core user/client/permission claims.
   * Expected Result: All claim accessors return expected values and validateToken passes for
   * matching username. Assertions: Extracted claims and validation outcomes.
   */
  @Test
  @DisplayName("generateToken - Extract Core Claims - Success")
  void generateToken_s01_extractCoreClaims_success() throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    User user = new User();
    user.setUserId(101L);
    user.setLoginName("nahush@example.com");
    user.setFirstName("Nahush");
    user.setLastName("Raichura");
    user.setRole("ADMIN");

    // Act
    String token = provider.generateToken(user, List.of(1L, 2L, 3L), 55L);

    // Assert
    assertEquals("nahush@example.com", provider.getUserNameFromToken(token));
    assertEquals(101L, provider.getUserIdFromToken(token));
    assertEquals(55L, provider.getClientIdFromToken(token));
    assertEquals(List.of(1L, 2L, 3L), provider.getUserPermissionIds(token));
    assertTrue(provider.validateToken(token, "nahush@example.com"));
    assertFalse(provider.validateToken(token, "someone-else@example.com"));
  }

  /**
   * Purpose: Verify client ID extraction handles null, numeric, and string claim variants. Expected
   * Result: Null returns null; number and string convert to Long. Assertions: getClientIdFromToken
   * return values.
   */
  @Test
  @DisplayName("getClientIdFromToken - Null Number String Branches - Success")
  void getClientIdFromToken_s02_nullNumberStringBranches_success() throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    String tokenWithoutClient =
        createToken(Map.of("email", "a@b.com", "userId", 1L), JWT_SECRET_32);
    String tokenWithNumberClient =
        createToken(Map.of("email", "a@b.com", "userId", 1L, "clientId", 77L), JWT_SECRET_32);
    String tokenWithStringClient =
        createToken(Map.of("email", "a@b.com", "userId", 1L, "clientId", "88"), JWT_SECRET_32);

    // Act
    Long noClient = provider.getClientIdFromToken(tokenWithoutClient);
    Long numberClient = provider.getClientIdFromToken(tokenWithNumberClient);
    Long stringClient = provider.getClientIdFromToken(tokenWithStringClient);

    // Assert
    assertNull(noClient);
    assertEquals(77L, numberClient);
    assertEquals(88L, stringClient);
  }

  /**
   * Purpose: Verify client-permission map extraction handles valid map and missing/invalid claim
   * values. Expected Result: Valid map converts keys and values to Long types; missing claim
   * returns empty map. Assertions: Parsed map contents and empty fallback behavior.
   */
  @Test
  @DisplayName("getClientPermissionMapFromToken - Map Conversion And Empty Fallback - Success")
  void getClientPermissionMapFromToken_s03_mapConversionAndEmptyFallback_success()
      throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    Map<String, Object> rawMap =
        Map.of(
            "10", List.of(1, 2L, "3"),
            "20", List.of(9));
    String tokenWithMap =
        createToken(
            Map.of("email", "a@b.com", "userId", 1L, "clientPermissionMap", rawMap), JWT_SECRET_32);
    String tokenWithoutMap = createToken(Map.of("email", "a@b.com", "userId", 1L), JWT_SECRET_32);

    // Act
    Map<Long, List<Long>> parsedMap = provider.getClientPermissionMapFromToken(tokenWithMap);
    Map<Long, List<Long>> emptyMap = provider.getClientPermissionMapFromToken(tokenWithoutMap);

    // Assert
    assertEquals(List.of(1L, 2L, 3L), parsedMap.get(10L));
    assertEquals(List.of(9L), parsedMap.get(20L));
    assertTrue(emptyMap.isEmpty());
  }

  /**
   * Purpose: Verify permission extraction handles non-list claims and conversion failures
   * gracefully. Expected Result: Invalid permission structures return an empty list. Assertions:
   * getUserPermissionIds fallback behavior.
   */
  @Test
  @DisplayName("getUserPermissionIds - Invalid Structures Return Empty - Success")
  void getUserPermissionIds_s04_invalidStructuresReturnEmpty_success() throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    String tokenNonList =
        createToken(
            Map.of("email", "a@b.com", "userId", 1L, "permissionIds", "not-a-list"), JWT_SECRET_32);
    String tokenBadList =
        createToken(
            Map.of("email", "a@b.com", "userId", 1L, "permissionIds", List.of("x", "y")),
            JWT_SECRET_32);

    // Act
    List<Long> nonListResult = provider.getUserPermissionIds(tokenNonList);
    List<Long> badListResult = provider.getUserPermissionIds(tokenBadList);

    // Assert
    assertTrue(nonListResult.isEmpty());
    assertTrue(badListResult.isEmpty());
  }

  /**
   * Purpose: Verify validateToken returns false on malformed token input. Expected Result:
   * Validation fails safely without exception leak. Assertions: validateToken false result.
   */
  @Test
  @DisplayName("validateToken - Malformed Token - Success")
  void validateToken_s05_malformedToken_success() throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    // Act
    boolean valid = provider.validateToken("not-a-jwt", "user@example.com");

    // Assert
    assertFalse(valid);
  }

  /**
   * Purpose: Verify web-template token validation checks wildcard and signature correctly. Expected
   * Result: True for matching wildcard/signature, false otherwise. Assertions:
   * validateTokenForWebTemplate outcomes.
   */
  @Test
  @DisplayName("validateTokenForWebTemplate - Wildcard And Signature Checks - Success")
  void validateTokenForWebTemplate_s06_wildcardAndSignatureChecks_success() throws Exception {
    // Arrange
    JwtTokenProvider provider = new JwtTokenProvider();
    setField(provider, "jwtSecret", JWT_SECRET_32);
    setField(provider, "issuerUrl", "https://issuer.example.com");

    String webToken = createToken(Map.of("wildCard", "allowed-route"), WEB_API_KEY_32);

    // Act
    boolean valid = provider.validateTokenForWebTemplate(webToken, "allowed-route", WEB_API_KEY_32);
    boolean wrongWildcard =
        provider.validateTokenForWebTemplate(webToken, "different", WEB_API_KEY_32);
    boolean wrongSecret =
        provider.validateTokenForWebTemplate(webToken, "allowed-route", "wrong-key");

    // Assert
    assertTrue(valid);
    assertFalse(wrongWildcard);
    assertFalse(wrongSecret);
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static String createToken(Map<String, Object> claims, String secret) {
    io.jsonwebtoken.JwtBuilder builder =
        Jwts.builder()
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 600_000L));

    for (Map.Entry<String, Object> claim : claims.entrySet()) {
      builder.claim(claim.getKey(), claim.getValue());
    }

    return builder.signWith(PasswordHelper.getSecretKey(secret)).compact();
  }

  private static String generateSecret() {
    byte[] secretBytes = new byte[32];
    new SecureRandom().nextBytes(secretBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
  }
}
