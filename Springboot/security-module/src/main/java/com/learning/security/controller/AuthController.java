package com.learning.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.LogoutRequest;
import com.learning.security.dto.RefreshTokenRequest;
import com.learning.security.dto.RegisterRequest;
import com.learning.security.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Tag(name = "Authentication", description = "Register, login, token refresh and logout endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Register new user",
        responses = {
            @ApiResponse(responseCode = "201", description = "User Registered", content = @Content(schema = @Schema(implementation = AuthReponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "408", description = "Email Already exists")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthReponse> register(@Valid @RequestBody RegisterRequest RegisterRequest) {
        log.debug("POST /api/auth/register received");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(RegisterRequest));
    }

    @Operation(
        summary = "Login with email and password",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login Successfull", content = @Content(schema = @Schema(implementation = AuthReponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid Credentials")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthReponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("POST /api/auth/login received");
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(
        summary = "Refresh access token",
        description = "Exchange a valid refresh token for a new access token + rotated refresh token pair. "
                    + "The old refresh token is invalidated immediately after use.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed",
                    content = @Content(schema = @Schema(implementation = AuthReponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — refreshToken field is blank"),
            @ApiResponse(responseCode = "401", description = "Refresh token not found or expired")
        }
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthReponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("POST /api/auth/refresh received");
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
        summary = "Logout — invalidate refresh token",
        description = "Deletes the refresh token from the server so it can never be used again. "
                    + "The client must discard both the access token and refresh token after calling this endpoint.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Logged out successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — refreshToken field is blank"),
            @ApiResponse(responseCode = "401", description = "Refresh token not found or already expired")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        log.debug("POST /api/auth/logout received");
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
