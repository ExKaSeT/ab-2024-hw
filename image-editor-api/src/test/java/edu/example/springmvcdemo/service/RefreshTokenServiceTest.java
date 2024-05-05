package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.RefreshTokenRepository;
import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class RefreshTokenServiceTest {

    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @BeforeEach
    @AfterEach
    public void clear() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    public void deleteExpired() {
        var username = "username";
        var registerResponse = authService.register(new RegisterRequestDto(username, "password"));
        var user = userRepository.findByUsername(registerResponse.getUsername()).get();
        refreshTokenService.deactivateUserTokens(user.getUsername());

        refreshTokenService.saveToken(user, Instant.now().minusSeconds(5));
        assertEquals(1, refreshTokenRepository.findAll().size());
        refreshTokenService.deleteExpiredRefreshTokens();

        assertEquals(0, refreshTokenRepository.findAll().size());
    }
}
