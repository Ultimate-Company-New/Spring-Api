package springapi.ServiceTests.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import springapi.authentication.SecurityConfig;

@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

  // Total Tests: 1

  /**
   * Purpose: Verify CORS configuration source exposes expected origins, methods, and credential
   * settings. Expected Result: Registered configuration resolves for arbitrary path with configured
   * values. Assertions: Origins, methods, headers, credentials, and max-age.
   */
  @Test
  @DisplayName("corsConfigurationSource - Returns Expected CORS Policy - Success")
  void corsConfigurationSource_s01_returnsExpectedCorsPolicy_success() {
    // Arrange
    SecurityConfig securityConfig = new SecurityConfig();

    // Act
    CorsConfigurationSource source = securityConfig.corsConfigurationSource();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/test");
    CorsConfiguration configuration = source.getCorsConfiguration(request);

    // Assert
    assertNotNull(source);
    assertTrue(source instanceof UrlBasedCorsConfigurationSource);
    assertNotNull(configuration);
    assertTrue(configuration.getAllowedOrigins().contains("http://localhost:3000"));
    assertTrue(configuration.getAllowedOrigins().contains("http://127.0.0.1:3001"));
    assertTrue(configuration.getAllowedMethods().contains("GET"));
    assertTrue(configuration.getAllowedMethods().contains("PATCH"));
    assertEquals(java.util.List.of("*"), configuration.getAllowedHeaders());
    assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    assertEquals(3600L, configuration.getMaxAge());
  }
}
