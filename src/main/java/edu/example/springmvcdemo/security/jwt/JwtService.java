package edu.example.springmvcdemo.security.jwt;

import edu.example.springmvcdemo.dto.auth.AuthUserDto;
import edu.example.springmvcdemo.model.User;
import edu.example.springmvcdemo.security.exception.InvalidTokenException;
import edu.example.springmvcdemo.service.RefreshTokenService;
import edu.example.springmvcdemo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private final RefreshTokenService tokenService;
    private final UserService userService;

    private SecretKey signingKey;

    @PostConstruct
    private void setSigningKey() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecretKey()));
    }

    public Tokens buildTokens(User user) {
        Instant currentDate = Instant.now();
        Instant refreshTokenExpirationDate = currentDate.plus(properties.getRefreshTokenExpirationMin(), ChronoUnit.MINUTES);
        var token = tokenService.saveToken(user, refreshTokenExpirationDate);
        String refreshToken = Jwts.builder()
                .claims().add("tokenId", token.getId())
                .and()
                .subject(user.getUsername())
                .expiration(Date.from(refreshTokenExpirationDate))
                .signWith(this.signingKey)
                .compact();

        Instant accessTokenExpirationDate = currentDate.plus(properties.getAccessTokenExpirationMin(), ChronoUnit.MINUTES);
        String accessToken = Jwts.builder()
                .subject(user.getUsername())
                .expiration(Date.from(accessTokenExpirationDate))
                .signWith(this.signingKey)
                .compact();

        return new Tokens(accessToken, refreshToken);
    }

    @Data
    @AllArgsConstructor
    public static class Tokens {
        private String accessToken;
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    public static class TokenPayload {
        private String username;
        /**
         Null in access token
         */
        private Long tokenId;

        public boolean isAccessToken() {
            return tokenId == null;
        }
    }

    public TokenPayload parseToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(this.signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenPayload(claims.getSubject(),  claims.get("tokenId", Long.class));
        } catch (RuntimeException ex) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthUserDto updateTokens(String refreshToken) {
        var payload = this.parseToken(refreshToken);
        if (payload.isAccessToken()) {
            throw new InvalidTokenException("Invalid token");
        }

        var token = tokenService.getToken(payload.tokenId);
        var user = userService.getUserByUsername(payload.username);
        if (token.isWasUsed()) {
            tokenService.deactivateUserTokens(user.getUsername());
            throw new InvalidTokenException("Refresh token already was used");
        }
        tokenService.setUsed(token);

        var tokens = this.buildTokens(user);
        return new AuthUserDto(tokens.getAccessToken(), tokens.getRefreshToken(), user.getUsername());
    }

    public void deactivateRefreshToken(String token) {
        var payload = this.parseToken(token);
        if (payload.isAccessToken()) {
            throw new InvalidTokenException("Invalid token");
        }
        var tokenObj = tokenService.getToken(payload.tokenId);
        tokenService.setUsed(tokenObj);
    }
}
