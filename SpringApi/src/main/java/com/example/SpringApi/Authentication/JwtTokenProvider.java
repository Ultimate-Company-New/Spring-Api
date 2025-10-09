package com.example.SpringApi.Authentication;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import com.example.SpringApi.Helpers.PasswordHelper;
import com.example.SpringApi.Models.DatabaseModels.User;

import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JwtTokenProvider {
    private static final String DEFAULT_API_KEY = "defaultKey";
    private static final String ISSUER_URL = "https://localhost:4433/";
    
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

    public String generateToken(User user, List<Long> permissionIds, String apiKey) {
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
                .claim("permissionIds", permissionIds)
                .expiration(expiryDate)
                .signWith(PasswordHelper.getSecretKey(apiKey))
                .compact();
    }

    public String getUserNameFromToken(String token){
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(DEFAULT_API_KEY))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return claims.get("email").toString();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(DEFAULT_API_KEY))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return Long.valueOf(claims.get("userId").toString());
    }

    public Long getClientIdFromToken(String token) {
        Claims claims = parser()
            .verifyWith(PasswordHelper.getSecretKey(DEFAULT_API_KEY))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return Long.valueOf(claims.get("carrierId").toString());
    }

    public List<Long> getUserPermissionIds(String token){
    Claims claims = parser()
        .verifyWith(PasswordHelper.getSecretKey(DEFAULT_API_KEY))
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
            parser().verifyWith(PasswordHelper.getSecretKey(DEFAULT_API_KEY)).build().parseSignedClaims(token);
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
