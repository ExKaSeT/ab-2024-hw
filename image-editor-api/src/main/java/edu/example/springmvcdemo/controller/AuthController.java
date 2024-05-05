package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.dto.auth.AuthUserDto;
import edu.example.springmvcdemo.dto.auth.LoginRequestDto;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import edu.example.springmvcdemo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authorisation", description = "Register / login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenController tokenController;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto requestDto, HttpServletResponse response) {
        AuthUserDto authUser = authService.register(requestDto);
        tokenController.addTokenCookiesToResponse(response, authUser.getAccessToken(), authUser.getRefreshToken());
        return ResponseEntity.ok(authUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in as a user / administrator")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto requestDto, HttpServletResponse response) {
        AuthUserDto authUser = authService.login(requestDto);
        tokenController.addTokenCookiesToResponse(response, authUser.getAccessToken(), authUser.getRefreshToken());
        return ResponseEntity.ok(authUser);
    }
}