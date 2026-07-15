package com.learning.security.service.impl;

import static com.learning.common.util.LogMaskingUtils.maskEmail;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.learning.security.config.JwtProperties;
import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.AuthReponse.UserInfo;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.LogoutRequest;
import com.learning.security.dto.RefreshTokenRequest;
import com.learning.security.dto.RegisterRequest;
import com.learning.security.entity.RefreshToken;
import com.learning.security.entity.User;
import com.learning.security.repository.UserRepository;
import com.learning.security.service.AuthService;
import com.learning.security.service.JwtService;
import com.learning.security.service.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthReponse register(@Valid RegisterRequest registerRequest) {
        log.info("Register attempt for email: {}", maskEmail(registerRequest.email()));

        // 1. Reject duplicate email
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            log.warn("Registration rejected — email already exists: {}", maskEmail(registerRequest.email()));
            throw new IllegalArgumentException("Email Address Already Exists " + registerRequest.email());
        }

        // 2. Build and persist new user
        User user = User.builder()
            .email(registerRequest.email())
            .firstName(registerRequest.firstName())
            .lastName(registerRequest.lastname())
            .password(passwordEncoder.encode(registerRequest.password()))
            .phone(registerRequest.phone())
            .role(registerRequest.role())
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully, userId: {}", savedUser.getId());

        // 3. Issue access + refresh tokens (values are never logged)
        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);
        return buildResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Override
    public AuthReponse login(@Valid LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", maskEmail(loginRequest.email()));

        // Delegate credential validation to Spring Security AuthenticationManager.
        // Throws AuthenticationException (401) on wrong credentials.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        User user = userRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> {
                // Should never happen — AuthenticationManager already verified the user exists,
                // but guard against a race condition (e.g. account deleted between auth and lookup).
                log.warn("Post-auth user lookup failed for email: {}", maskEmail(loginRequest.email()));
                return new IllegalStateException("User not found for Email " + loginRequest.email());
            });

        log.info("Login successful, userId: {}", user.getId());

        // Issue access + refresh tokens (values are never logged)
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    public AuthReponse refresh(RefreshTokenRequest request) {
        // 1. Verify the token exists and has not expired (throws TokenRefreshException if invalid)
        RefreshToken existingToken = refreshTokenService.verifyExpiration(request.refreshToken());
        User user = existingToken.getUser();

        // 2. Issue a new access JWT
        String newAccessToken = jwtService.generateToken(user);

        // 3. Rotate: delete old refresh token and issue a fresh one
        refreshTokenService.deleteByUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Token refreshed for userId: {}", user.getId());
        return buildResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Override
    public void logout(LogoutRequest request) {
        // Verify the token is valid (not already revoked/expired) and retrieve the owner
        RefreshToken refreshToken = refreshTokenService.verifyExpiration(request.refreshToken());
        refreshTokenService.deleteByUser(refreshToken.getUser());
        log.info("User logged out, userId: {}", refreshToken.getUser().getId());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AuthReponse buildResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getAuthorities()
            .stream().map(a -> a.getAuthority()).toList();
        UserInfo userInfo = new UserInfo(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            roles
        );
        return new AuthReponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtProperties.getExpiration(),
            userInfo
        );
    }

}
