package com.example.SpringApi.Config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS Configuration for Spring API Allows frontend applications to access the API from different
 * origins
 */
@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // Allow localhost origins - explicit ports
    config.setAllowedOrigins(
        Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"));

    // Allow credentials
    config.setAllowCredentials(true);

    // Allow all headers
    config.setAllowedHeaders(List.of("*"));

    // Allow all HTTP methods
    config.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

    // Expose headers
    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Total-Count"));

    // Cache preflight for 1 hour
    config.setMaxAge(3600L);

    // Register for all paths
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}

