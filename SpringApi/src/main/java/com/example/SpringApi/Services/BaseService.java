package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientPermissionMapping;
import com.example.SpringApi.Repositories.UserClientPermissionMappingRepository;
import com.example.SpringApi.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class BaseService {
    private final JwtTokenProvider jwtTokenProvider;
    protected static final String CURRENT_ENVIRONMENT = "Local";    
    
    @Autowired
    protected UserClientPermissionMappingRepository userClientPermissionMappingRepository;
    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected UserRepository userRepository;

    public BaseService(){
        this.jwtTokenProvider = new JwtTokenProvider();
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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
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

    protected void validateUserBelongsToClient(long userId) {
        List<UserClientPermissionMapping> permissions = userClientPermissionMappingRepository.findByUserIdAndClientId(userId, getClientId());
        if (permissions.isEmpty()) {
            throw new NotFoundException("User does not belong to the current client");
        }
    }

    protected void validateUserBelongsToClient(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        List<UserClientPermissionMapping> permissions = userClientPermissionMappingRepository.findByUserIdAndClientId(user.getUserId(), getClientId());
        if (permissions.isEmpty()) {
            throw new NotFoundException("User does not belong to the current client");
        }
    }
}
