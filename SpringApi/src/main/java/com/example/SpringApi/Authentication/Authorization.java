package com.example.SpringApi.Authentication;

import com.example.SpringApi.Exceptions.PermissionException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Logging.ContextualLogger;
import jakarta.servlet.http.HttpServletRequest;

import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Permission;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.DatabaseModels.UserClientMapping;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.PermissionRepository;
import com.example.SpringApi.Repositories.UserClientMappingRepository;
import com.example.SpringApi.Repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service("customAuthorization")
public class Authorization{
    private static final ContextualLogger logger = ContextualLogger.getLogger(Authorization.class);

    private final HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final ClientRepository clientRepository;
    private final UserClientMappingRepository userClientMappingRepository;

    @Autowired
    public Authorization(HttpServletRequest request,
                        JwtTokenProvider jwtTokenProvider,
                        UserRepository userRepository,
                        PermissionRepository permissionRepository,
                        ClientRepository clientRepository,
                        UserClientMappingRepository userClientMappingRepository
    ){
        this.request = request;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.userClientMappingRepository = userClientMappingRepository;
        this.clientRepository = clientRepository;
    }

    private String getJwtFromRequest() {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    private void validateToken() {
        PermissionException permissionException = new PermissionException(ErrorMessages.Unauthorized);
        String token = getJwtFromRequest();
    
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Long clientId = jwtTokenProvider.getClientIdFromToken(token);
        if (userId == null || clientId == null) {
            logger.error(permissionException);
            throw permissionException;
        }

        // Check if user exists
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent()) {
            logger.error(permissionException);
            throw permissionException;
        }

        // Check client exists
        Optional<Client> client = clientRepository.findById(clientId);  
        if(!client.isPresent()) {
            logger.error(permissionException);
            throw permissionException;
        }

        //Check if user is part of the client mapping
        List<UserClientMapping> mapping = userClientMappingRepository.findByUserIdsAndClientId(Collections.singletonList(userId), clientId);
        if(mapping == null || mapping.isEmpty()) {
            logger.error(permissionException);
            throw permissionException;
        }
    }

    public boolean hasAuthority(String userPermission) {
        validateToken();
        List<Long> permissionIds = jwtTokenProvider.getUserPermissionIds(getJwtFromRequest());  
        boolean isUserAuthorized = isAllowed(userPermission, permissionIds);
        if(!isUserAuthorized) {
            PermissionException permissionException = new PermissionException(ErrorMessages.Unauthorized);
            logger.error(permissionException);
            throw permissionException;
        }   
        return true;
    }

    public boolean isAllowed(String userPermission, List<Long> permissionIds) {
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            Set<String> userPermissionCodes = new HashSet<>();
            for (Permission p : permissions) {
                userPermissionCodes.add(p.getPermissionCode());
            }
            String[] requiredPermissions = userPermission.split(",");
            for (String perm : requiredPermissions) {
                if (userPermissionCodes.contains(perm.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}