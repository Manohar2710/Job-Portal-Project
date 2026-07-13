package com.learning.security.service.impl;

import static com.learning.common.util.LogMaskingUtils.maskEmail;

import com.learning.security.config.JwtProperties;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.RegisterRequest;
import com.learning.security.dto.AuthReponse.UserInfo;
import com.learning.security.entity.Role;
import com.learning.security.entity.User;
import com.learning.security.repository.UserRepository;
import com.learning.security.service.AuthService;
import com.learning.security.service.JwtService;
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

    @Override
    public AuthReponse register(@Valid RegisterRequest registerRequest) {
        log.info("Register attempt for email: {}", maskEmail(registerRequest.email()));

        // 1. logic to reject duplicate email
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            log.warn("Registration rejected — email already exists: {}", maskEmail(registerRequest.email()));
            throw new IllegalArgumentException("Email Address Already Exists " + registerRequest.email());
        }

        // 2. build and persist new user
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

        // 3. issue access token (token value is never logged)
        String accessToken = jwtService.generateToken(user);
        return buildResponse(savedUser, accessToken);
    }

    /*
    * Private Helper method to build AuthResponse
     */
    private AuthReponse buildResponse(User savedUser, String accessToken) {
        List<String> roles = savedUser.getAuthorities()
            .stream().map(role -> role.getAuthority()).toList();
            UserInfo userInfo = new UserInfo(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                roles
            );
            return new AuthReponse(
                accessToken,
                null,
                "Bearer",
                jwtProperties.getExpiration(),
                userInfo
            );

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

        // Issue access token (token value is never logged)
        String accessToken = jwtService.generateToken(user);
        return buildResponse(user, accessToken);
    }

}
