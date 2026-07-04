package com.learning.security.service.impl;

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
        // 1. logic to regect duplicate email
        if(userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Email Address Already Exists " + registerRequest.email() );
        }
        // 2. build and persist new user
        User user = User.builder()
            .email(registerRequest.email())
            .firstName(registerRequest.firstName())
            .lastName(registerRequest.lastname())
            .password(passwordEncoder.encode(registerRequest.password()))
            .phone(registerRequest.phone())
            .role(Role.ROLE_USER)
            .build();

        User savedUser = userRepository.save(user);
        // 3. issue access token 
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
        // Delegation email validation to spring security authentication manager
        // throws 401 authentication exception is wrong credentials
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );

        User user = userRepository.findByEmail(
            loginRequest.email())
                .orElseThrow(() -> new IllegalStateException("User not found for Email "+ loginRequest.email())
            );
        String accessToken = jwtService.generateToken(user);
        return buildResponse(user, accessToken);
    }

}
