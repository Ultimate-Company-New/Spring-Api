package com.example.SpringApi.ServiceTests.Authentication;

import com.example.SpringApi.Authentication.JwtAuthenticationFilter;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    // Total Tests: 5

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Purpose: Verify OPTIONS requests bypass JWT processing and continue filter chain.
     * Expected Result: Filter chain called once; no authentication set.
     * Assertions: Chain invocation and null security context authentication.
     */
    @Test
    @DisplayName("doFilterInternal - OPTIONS Request Bypasses JWT - Success")
    void doFilterInternal_s01_optionsRequestBypassesJwt_success() throws ServletException, IOException {
        // Arrange
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        when(request.getMethod()).thenReturn("OPTIONS");

        // Act
        filter.doFilterInternalPublic(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Purpose: Verify valid bearer token sets authenticated user in security context.
     * Expected Result: Authentication object created and attached to context.
     * Assertions: Non-null authentication with expected principal username.
     */
    @Test
    @DisplayName("doFilterInternal - Valid Bearer Token Sets Authentication - Success")
    void doFilterInternal_s02_validBearerTokenSetsAuthentication_success() throws ServletException, IOException {
        // Arrange
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtTokenProvider.getUserNameFromToken(eq("jwt-token"))).thenReturn("nahush@example.com");

        UserDetails userDetails = new User("nahush@example.com", "password", List.of());
        when(userDetailsService.loadUserByUsername(eq("nahush@example.com"))).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(eq("jwt-token"), eq("nahush@example.com"))).thenReturn(true);

        // Act
        filter.doFilterInternalPublic(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("nahush@example.com", ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Purpose: Verify token parsing exceptions do not break the filter chain.
     * Expected Result: No authentication is set and chain continues.
     * Assertions: Context remains unauthenticated and chain invoked.
     */
    @Test
    @DisplayName("doFilterInternal - Username Extraction Exception - Success")
    void doFilterInternal_s03_usernameExtractionException_success() throws ServletException, IOException {
        // Arrange
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtTokenProvider.getUserNameFromToken(eq("jwt-token"))).thenThrow(new RuntimeException("bad token"));

        // Act
        filter.doFilterInternalPublic(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * Purpose: Verify failed token validation prevents authentication creation.
     * Expected Result: Security context remains unauthenticated.
     * Assertions: Null authentication and chain invocation.
     */
    @Test
    @DisplayName("doFilterInternal - Token Validation Fails - Success")
    void doFilterInternal_s04_tokenValidationFails_success() throws ServletException, IOException {
        // Arrange
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtTokenProvider.getUserNameFromToken(eq("jwt-token"))).thenReturn("nahush@example.com");

        UserDetails userDetails = new User("nahush@example.com", "password", List.of());
        when(userDetailsService.loadUserByUsername(eq("nahush@example.com"))).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(eq("jwt-token"), eq("nahush@example.com"))).thenReturn(false);

        // Act
        filter.doFilterInternalPublic(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Purpose: Verify filter does not overwrite an already authenticated security context.
     * Expected Result: Existing authentication remains and user-details lookup is skipped.
     * Assertions: Existing principal unchanged and no user-details-service invocation.
     */
    @Test
    @DisplayName("doFilterInternal - Existing Authentication Is Preserved - Success")
    void doFilterInternal_s05_existingAuthenticationIsPreserved_success() throws ServletException, IOException {
        // Arrange
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(jwtTokenProvider.getUserNameFromToken(eq("jwt-token"))).thenReturn("nahush@example.com");

        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // Act
        filter.doFilterInternalPublic(request, response, filterChain);

        // Assert
        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    private static final class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {

        TestableJwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
            super(jwtTokenProvider, userDetailsService);
        }

        void doFilterInternalPublic(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}
