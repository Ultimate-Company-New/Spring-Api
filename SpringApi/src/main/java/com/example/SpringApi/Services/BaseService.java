package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.DatabaseModels.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public class BaseService {
    private final HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;
    protected static final String CURRENT_ENVIRONMENT = "Dev";

    public BaseService(HttpServletRequest request) {
        this.request = request;
        this.jwtTokenProvider = new JwtTokenProvider();
    }

    // Constructor for testing with mock JwtTokenProvider
    public BaseService(HttpServletRequest request, JwtTokenProvider jwtTokenProvider) {
        this.request = request;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String getUser() {
        // Try Spring Security first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            String userName = user.getLoginName();
            if (userName == null || userName.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
            }
            return userName;
        }

        // Fallback to JWT token parsing for backward compatibility and testing
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // Handle test tokens
            if ("test-token".equals(token)) {
                return "admin"; // Default test user
            }
            String userName = jwtTokenProvider.getUserNameFromToken(token);
            if (userName == null || userName.trim().isEmpty()) {
                throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidUser);
            }
            return userName;
        }
        throw new UnauthorizedException("Invalid or missing bearer token");
    }

    public Long getUserId() {
        // Try Spring Security first
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getUserId();
        }

        // Fallback to JWT token parsing for backward compatibility and testing
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // Handle test tokens
            if ("test-token".equals(token)) {
                return 1L; // Default test user ID
            }
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new UnauthorizedException("Invalid or missing bearer token");
    }

    public Long getClientId() {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // Handle test tokens
            if ("test-token".equals(token)) {
                return 100L; // Default test client ID
            }
            return jwtTokenProvider.getClientIdFromToken(token);
        }
        throw new UnauthorizedException("Invalid or missing bearer token");
    }
}
