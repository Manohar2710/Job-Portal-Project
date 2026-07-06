package com.learning.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.RegisterRequest;
import com.learning.security.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Tag(name = "Authentication", description = "Register and login endpoint")
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
    public ResponseEntity<AuthReponse> register(@Valid @RequestBody RegisterRequest RegisterRequest){
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
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
