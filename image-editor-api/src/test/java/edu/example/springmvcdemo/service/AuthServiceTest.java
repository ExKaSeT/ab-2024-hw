package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.RefreshTokenRepository;
import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.dto.auth.LoginRequestDto;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class AuthServiceTest {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    @AfterEach
    public void clear() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    public void registerAndLogin() {
        String username = "username";
        String password = "password";

        authService.register(new RegisterRequestDto(username, password));
        authService.login(new LoginRequestDto(username, password));

        var users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals(username, users.get(0).getUsername());

        var tokens = refreshTokenRepository.findAll();
        assertEquals(2, tokens.size());
        assertEquals(username, tokens.get(0).getUser().getUsername());
        assertEquals(username, tokens.get(1).getUser().getUsername());
    }

    @Test
    public void registerWithExistingUsername() {
        String username = "username";
        String password = "password";

        authService.register(new RegisterRequestDto(username, password));

        assertThrows(ConstraintViolationException.class,
                () -> authService.register(new RegisterRequestDto(username, password)));
    }

    @Test
    public void loginUnregistered() {
        String username = "username";
        String password = "password";

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequestDto(username, password)));
    }
}
