package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.auth.AuthUserDto;
import edu.example.springmvcdemo.security.jwt.JwtProperties;
import edu.example.springmvcdemo.security.jwt.JwtService;
import edu.example.springmvcdemo.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(JwtProperties.PATH_TO_UPDATE_TOKENS)
@Tag(name = "Token", description = "Update tokens")
@RequiredArgsConstructor
public class TokenController {

    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final RefreshTokenService tokenService;

    @PostMapping("/update")
    @Operation(summary = "Update tokens using refresh token")
    public ResponseEntity<?> updateTokens(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = tokenService.extractFromRequest(request);
        AuthUserDto authUser = jwtService.updateTokens(refreshToken);
        addTokenCookiesToResponse(response, authUser.getAccessToken(), authUser.getRefreshToken());
        return ResponseEntity.ok(authUser);
    }

    public void addTokenCookiesToResponse(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie(JwtProperties.JWT_ACCESS_COOKIE_NAME, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setSecure(jwtProperties.isCookieHttpsOnly());
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie(JwtProperties.JWT_REFRESH_COOKIE_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath(JwtProperties.PATH_TO_UPDATE_TOKENS);
        refreshCookie.setSecure(jwtProperties.isCookieHttpsOnly());
        response.addCookie(refreshCookie);
    }
}