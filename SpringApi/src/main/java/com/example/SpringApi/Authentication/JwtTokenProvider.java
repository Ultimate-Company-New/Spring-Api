package com.example.SpringApi.Authentication;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JwtTokenProvider {
    @Value("${JWT_TOKEN}")
    private String jwtSecret;

    @Value("${jwt.issuer.url}")
    private String issuerUrl;

    /**
     * Generates a JWT token for a user with their client-permission mappings.
     * * @param user The user entity
     * @param permissionIds List of permissionIds for the client
     * @param clientId The client ID
     * @return JWT token string
     */
    public String generateToken(User user,
                                List<Long> permissionIds,
                                Long clientId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return builder()
                .issuer(issuerUrl)
                .issuedAt(now)
                .audience().add(issuerUrl).and()
                .claim("userId", user.getUserId())
                .claim("email", user.getLoginName())
                .claim("given_name", user.getFirstName())
                .claim("last_name", user.getLastName())
                .claim("role", user.getRole())
                .claim("clientId", clientId)
                .claim("permissionIds", permissionIds)
                .expiration(expiryDate)
                .signWith(PasswordHelper.getSecretKey(jwtSecret))
                .compact();
    }

    public String getUserNameFromToken(String token){
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email").toString();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * Gets the clientId the user belongs to from the JWT token.
     * * @param token The JWT token
     * @return The client ID
     */
    public Long getClientIdFromToken(String token) {
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object clientIdObj = claims.get("clientId");
        if (clientIdObj == null) {
            return null;
        }

        if (clientIdObj instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(clientIdObj.toString());
    }

    /**
     * Gets the client-permission map from the JWT token.
     * * @param token The JWT token
     * @return Map of clientId to list of permissionIds
     */
    public Map<Long, List<Long>> getClientPermissionMapFromToken(String token) {
        Claims claims = parser()
                .verifyWith(PasswordHelper.getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rawMapObj = claims.get("clientPermissionMap");
        if (!(rawMapObj instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> result = new java.util.HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            Long clientId = Long.valueOf(entry.getKey().toString());
            List<Long> permissionIds = new java.util.ArrayList<>();

            if (entry.getValue() instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Number number) {
                        permissionIds.add(number.longValue());
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
                .verifyWith(PasswordHelper.getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object raw = claims.get("permissionIds");
        if (raw instanceof List<?> rawList) {
            try {
                return rawList.stream()
                        .map(val -> {
                            if (val instanceof Number number) {
                                return number.longValue();
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
            parser().verifyWith(PasswordHelper.getSecretKey(jwtSecret)).build().parseSignedClaims(token);
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