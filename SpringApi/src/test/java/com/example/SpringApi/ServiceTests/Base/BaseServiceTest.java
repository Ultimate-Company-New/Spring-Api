package com.example.SpringApi.ServiceTests.Base;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Services.BaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    private BaseService baseService;

    @BeforeEach
    void setUp() {
        baseService = new BaseService(jwtTokenProvider, request);
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    // Total Tests: 10
    @Test
    void getUser_FromSecurityContext_ReturnsLoginName() {
        User principal = new User();
        principal.setUserId(10L);
        principal.setLoginName("secure-user");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

        assertEquals("secure-user", baseService.getUser());
    }

    @Test
    void getUser_FromSecurityContextWithBlankLogin_ThrowsBadRequest() {
        User principal = new User();
        principal.setLoginName("   ");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> baseService.getUser());
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_USER, exception.getMessage());
    }

    @Test
    void getUser_FromTestToken_ReturnsDefaultTestUser() {
        setBearerToken("test-token");

        assertEquals("admin", baseService.getUser());
    }

    @Test
    void getUser_FromJwtTokenProvider_ReturnsProviderValue() {
        setBearerToken("jwt-abc");
        when(jwtTokenProvider.getUserNameFromToken("jwt-abc")).thenReturn("token-user");

        assertEquals("token-user", baseService.getUser());
    }

    @Test
    void getUser_FromJwtTokenProviderBlankUser_ThrowsBadRequest() {
        setBearerToken("jwt-empty");
        when(jwtTokenProvider.getUserNameFromToken("jwt-empty")).thenReturn(" ");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> baseService.getUser());
        assertEquals(ErrorMessages.UserErrorMessages.INVALID_USER, exception.getMessage());
    }

    @Test
    void getUser_NoRequestContext_ReturnsDefaultTestUser() {
        RequestContextHolder.resetRequestAttributes();

        assertEquals("admin", baseService.getUser());
    }

    @Test
    void getUserId_FromSecurityContext_ReturnsUserId() {
        User principal = new User();
        principal.setUserId(55L);
        principal.setLoginName("user55");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

        assertEquals(55L, baseService.getUserId());
    }

    @Test
    void getUserId_FromJwtTokenProviderAndTestToken_ReturnExpectedValues() {
        setBearerToken("jwt-user-id");
        when(jwtTokenProvider.getUserIdFromToken("jwt-user-id")).thenReturn(88L);
        assertEquals(88L, baseService.getUserId());

        setBearerToken("test-token");
        assertEquals(1L, baseService.getUserId());
    }

    @Test
    void getClientId_FromJwtTokenProviderAndTestToken_ReturnExpectedValues() {
        setBearerToken("jwt-client");
        when(jwtTokenProvider.getClientIdFromToken("jwt-client")).thenReturn(999L);
        assertEquals(999L, baseService.getClientId());

        setBearerToken("test-token");
        assertEquals(1L, baseService.getClientId());
    }

    @Test
    void getUserIdAndClientId_NoRequestContext_ReturnDefaultOne() {
        RequestContextHolder.resetRequestAttributes();

        assertEquals(1L, baseService.getUserId());
        assertEquals(1L, baseService.getClientId());
    }

    private void setBearerToken(String token) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer " + token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }
}
