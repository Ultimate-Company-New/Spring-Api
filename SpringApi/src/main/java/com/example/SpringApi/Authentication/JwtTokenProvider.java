package com.example.SpringApi.Authentication;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JwtTokenProvider {
    private final Environment env;
    private static final String ISSUER_URL = "https://localhost:4433/";
    
    @Autowired
    public JwtTokenProvider(Environment env) {
        this.env = env;
    }
    
    private String getJwtSecretKey() {
        String key = env.getProperty("JWT_TOKEN");
        if (key == null) {
            throw new RuntimeException("JWT_TOKEN property not found in application properties");
        }
        return key;
    }
    
    // public String generateToken(WebTemplateCarrierMapping webTemplateCarrierMapping) {
    //     Date now = new Date();
    //     Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000);

    //     return builder()
    //             .issuer(ISSUER_URL)
    //             .issuedAt(now)
    //             .audience().add(ISSUER_URL).and()
    //             .claim("wildCard", webTemplateCarrierMapping.getWildCard())
    //             .claim("webTemplateId", webTemplateCarrierMapping.getWebTemplateId())
    //             .claim("carrierId",webTemplateCarrierMapping.getCarrierId())
    //             .expiration(expiryDate)
    //             .signWith(PasswordHelper.getSecretKey(webTemplateCarrierMapping.getApiAccessKey()))
    //             .compact();
    // }

    /**
     * Generates a JWT token for a user with their client-permission mappings.
     * 
     * @param user The user entity
     * @param clientPermissionMap Map of clientId to list of permissionIds for that client
     * @param apiKey The API key for token signing
     * @return JWT token string
     */
    public String generateToken(User user, 
        List<Long> permissionIds, 
        Long clientId,
        String apiKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return builder()
                .issuer(ISSUER_URL)
                .issuedAt(now)
                .audience().add(ISSUER_URL).and()
                .claim("userId", user.getUserId())
                .claim("email", user.getLoginName())
                .claim("given_name", user.getFirstName())
                .claim("last_name", user.getLastName())
                .claim("role", user.getRole())
                .claim("clientId", clientId)
                .claim("permissionIds", permissionIds)
                .expiration(expiryDate)
                .signWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
                .compact();
    }

    public String getUserNameFromToken(String token){
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.get("email").toString();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * Gets the clientId the user belongs to from the JWT token.
     * 
     * @param token The JWT token
     * @return The client ID
     */
    public Long getClientIdFromToken(String token) {
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Object clientIdObj = claims.get("clientId");
        if (clientIdObj == null) {
            return null;
        }
        
        if (clientIdObj instanceof Number) {
            return ((Number) clientIdObj).longValue();
        } else {
            return Long.valueOf(clientIdObj.toString());
        }
    }

    /**
     * Gets the client-permission map from the JWT token.
     * 
     * @param token The JWT token
     * @return Map of clientId to list of permissionIds
     */
    public Map<Long, List<Long>> getClientPermissionMapFromToken(String token) {
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        Map<String, Object> rawMap = (Map<String, Object>) claims.get("clientPermissionMap");
        if (rawMap == null || rawMap.isEmpty()) {
            return Map.of();
        }
        
        Map<Long, List<Long>> result = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            Long clientId = Long.valueOf(entry.getKey());
            List<Long> permissionIds = new java.util.ArrayList<>();
            
            if (entry.getValue() instanceof List<?>) {
                List<?> rawList = (List<?>) entry.getValue();
                for (Object item : rawList) {
                    if (item instanceof Number) {
                        permissionIds.add(((Number) item).longValue());
                    } else {
                        permissionIds.add(Long.valueOf(item.toString()));
                    }
                }
            }
            result.put(clientId, permissionIds);
        }
        return result;
    }

    public List<Long> getUserPermissionIds(String token){
    Claims claims = parser()
        .verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey()))
        .build()
        .parseSignedClaims(token)
        .getPayload();

        Object raw = claims.get("permissionIds");
        if (raw instanceof List<?>) {
            List<?> rawList = (List<?>) raw;
            try {
                return rawList.stream()
                        .map(val -> {
                            if (val instanceof Number) {
                                return ((Number) val).longValue();
                            } else {
                                return Long.valueOf(val.toString());
                            }
                        })
                        .toList();
            } catch (Exception e) {
                // log or handle error if needed
                return List.of();
            }
        }
        return List.of();
    }

    public boolean validateToken(String token, String userName) {
        try {
            parser().verifyWith(PasswordHelper.getSecretKey(getJwtSecretKey())).build().parseSignedClaims(token);
            if(!userName.equals(getUserNameFromToken(token))){
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public boolean validateTokenForWebTemplate(String token, String wildCard, String apiAccessKey) {
        try {
        parser().verifyWith(PasswordHelper.getSecretKey(apiAccessKey)).build().parseSignedClaims(token);
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(apiAccessKey))
            .build()
            .parseSignedClaims(token)
            .getPayload();
            if(!wildCard.equals(claims.get("wildCard").toString())){
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
