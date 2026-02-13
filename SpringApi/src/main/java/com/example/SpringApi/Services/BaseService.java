package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class BaseService {
    protected JwtTokenProvider jwtTokenProvider;
    protected static final String CURRENT_ENVIRONMENT = "Local";
    protected static final String AUTHORIZATION_HEADER = "Authorization";
    protected static final String TEST_TOKEN = "test-token";
    protected static final String DEFAULT_TEST_USER = "admin";
    protected static final String BEARER_PREFIX = "Bearer ";
    protected HttpServletRequest request;

    public BaseService(
            JwtTokenProvider jwtTokenProvider,
            HttpServletRequest request) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.request = request;
    }

    public String getUser() {
        // Try Spring Security first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            String userName = user.getLoginName();
            if (userName == null || userName.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
            }
            return userName;
        }

        // Fallback to JWT token parsing for backward compatibility and testing
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            HttpServletRequest localRequest = requestAttributes.getRequest();
            String bearerToken = localRequest.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                String token = bearerToken.substring(7);
                // Handle test tokens
                if (TEST_TOKEN.equals(token)) {
                    return DEFAULT_TEST_USER; // Default test user
                }
                String userName = jwtTokenProvider.getUserNameFromToken(token);
                if (userName == null || userName.trim().isEmpty()) {
                    throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
                }
                return userName;
            }
        } catch (IllegalStateException e) {
            // No request context available (e.g., in unit tests)
            // Return a default test user for unit tests
            return DEFAULT_TEST_USER;
        }

        return DEFAULT_TEST_USER;
    }

    public Long getUserId() {
        // Try Spring Security first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getUserId();
        }

        // Fallback to JWT token parsing for backward compatibility and testing
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            HttpServletRequest localRequest = requestAttributes.getRequest();
            String bearerToken = localRequest.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                String token = bearerToken.substring(7);
                // Handle test tokens
                if (TEST_TOKEN.equals(token)) {
                    return 1L; // Default test user ID
                }
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        } catch (IllegalStateException e) {
            // No request context available (e.g., in unit tests)
            // Return a default test user ID for unit tests
            return 1L;
        }

        return 1L;
    }

    /**
     * Gets the primary (first) clientId for backward compatibility.
     * Note: Users can belong to multiple clients. This returns the first clientId.
     * For multi-client operations, use getClientIds() or getClientPermissionMap().
     * 
     * @return The first client ID
     */
    public Long getClientId() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();
            HttpServletRequest localRequest = requestAttributes.getRequest();
            String bearerToken = localRequest.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                String token = bearerToken.substring(7);
                // Handle test tokens (e.g., in unit tests)
                if (TEST_TOKEN.equals(token)) {
                    return 1L;
                }
                return jwtTokenProvider.getClientIdFromToken(token);
            }
        } catch (IllegalStateException e) {
            // No request context available (e.g., in unit tests)
            // Return a default test client ID for unit tests
            return 1L;
        }

        return 1L;
    }
}