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
import com.learning.security.exception.TokenRefreshException;
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

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

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

        // 3. Issue access token + refresh token (values are never logged)
        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);
        return buildResponse(savedUser, accessToken, refreshToken.getToken());
    }

    @Override
    public AuthReponse login(@Valid LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", maskEmail(loginRequest.email()));

        // Delegate credential validation to Spring Security AuthenticationManager.
        // Throws AuthenticationException (→ 401) on wrong credentials.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> {
                    // Should never happen — AuthenticationManager already verified the user exists,
                    // but guards against a race condition (e.g. account deleted between auth and lookup).
                    log.warn("Post-auth user lookup failed for email: {}", maskEmail(loginRequest.email()));
                    return new IllegalStateException("User not found for Email " + loginRequest.email());
                });

        log.info("Login successful, userId: {}", user.getId());

        // Issue access token + refresh token (values are never logged)
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return buildResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Validates the refresh token, issues a new access JWT, and rotates the
     * refresh token (delete old → insert new) to limit replay-attack windows.
     *
     * @throws TokenRefreshException if the token is not found or has expired (→ 401)
     */
    @Override
    public AuthReponse refresh(RefreshTokenRequest request) {
        // 1. Validate — throws TokenRefreshException if missing or expired
        RefreshToken verified = refreshTokenService.verifyExpiration(request.refreshToken());

        User user = verified.getUser();
        log.info("Refresh token validated for userId: {}", user.getId());

        // 2. Rotate — delete the verified token, issue a fresh one
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        // 3. Issue a new access JWT
        String newAccessToken = jwtService.generateToken(user);

        log.info("Token rotated — new access+refresh tokens issued for userId: {}", user.getId());
        return buildResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    /**
     * Invalidates the refresh token server-side (logout).
     * After this call the token cannot be used to obtain new access tokens.
     *
     * @throws TokenRefreshException if the token is not found (→ 401)
     */
    @Override
    public void logout(LogoutRequest request) {
        // Find the token record — reject unknown tokens so clients can't probe the endpoint
        RefreshToken refreshToken = refreshTokenService.verifyExpiration(request.refreshToken());

        User user = refreshToken.getUser();
        refreshTokenService.deleteByUser(user);
        log.info("User logged out — refresh token invalidated for userId: {}", user.getId());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Assembles the {@link AuthReponse} returned to the client on login, register, and refresh.
     * The refresh token is now always populated — never null.
     */
    private AuthReponse buildResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getAuthorities()
                .stream()
                .map(role -> role.getAuthority())
                .toList();

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );

        return new AuthReponse(
                accessToken,
                refreshToken,           // was always null before — now populated
                "Bearer",
                jwtProperties.getExpiration(),
                userInfo
        );
    }
}
